package org.exoplatform.gadgets.WebFileEditor.client;

import org.exoplatform.gadgets.WebFileEditor.client.controller.BrowserPanelHandlers;
import org.exoplatform.gadgets.WebFileEditor.client.controller.MenuPanelHandlers;
import org.exoplatform.gadgets.WebFileEditor.client.controller.PropertiesPanelHandlers;
import org.exoplatform.gadgets.WebFileEditor.client.model.FileModel;
import org.exoplatform.gadgets.WebFileEditor.client.view.LeftPanel;
import org.exoplatform.gadgets.WebFileEditor.client.view.MenuPanel;
import org.exoplatform.gadgets.WebFileEditor.client.view.RightPanel;
import org.exoplatform.gadgets.WebFileEditor.client.view.UploadWindow;
import org.exoplatform.gadgets.WebFileEditor.client.view.editor.ContentEditor;

import com.google.gwt.gadgets.client.Gadget;
import com.google.gwt.gadgets.client.StringPreference;
import com.google.gwt.gadgets.client.UserPreferences;
import com.google.gwt.gadgets.client.Gadget.ModulePrefs;
import com.google.gwt.gadgets.client.UserPreferences.PreferenceAttributes.Options;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.smartgwt.client.widgets.layout.HLayout;
import com.smartgwt.client.widgets.tree.TreeNode;

/**
 * Created by The eXo Platform SAS .
 * @author <a href="mailto:dmitry.ndp@exoplatform.com.ua">Dmytro Nochevnov</a>
 * @version $Id: $
*/
@ModulePrefs(title = "WebFileEditor", author = "Dmytro Nochevnov", author_email = "dmitry.ndp@exoplatform.com.ua",
    height = 515, description = "WebFileEditor" )

public class WebFileEditor extends Gadget<WFUserPreferences> { 
  // model instances

  // view instances
  public LeftPanel leftPanel;
  public MenuPanel menuPanel;
  public RightPanel rightPanel;
  public com.smartgwt.client.widgets.Window uploadWindow;  

  // controller instances
  public BrowserPanelHandlers browserPanelHandlers;
  public PropertiesPanelHandlers propertiesPanelHandlers; 
  public MenuPanelHandlers menuPanelHandlers;  
  
  // properties
  public String gadgetURL;
  public String gadgetImagesURL;
  public String webDAVURL;  
  
  
  /**
   * Opened in EditorPanel file properties
   */
  public ContentEditor currentContentEditor;

  /**
   * ContentType: 1 (FileModel.TEXT) for text-file, 2 (FileModel.XML) for xml-file, 3 (FileModel.GROOVY) for groovy script ....
   */
  public int currentFileType;  
  
  /**
   * MIMEType: "text/plain" for text-file, "text/xml" for xml-file, "script/groovy" for groovy script  
   */  
  public String currentFileMIMEType;  
  
  /**
   * EncodingCharset: "utf-8" for UTF8 charset, ...  
   */    
  public String currentFileEncodingCharset;

  /**
   * Folder where the file will be saved  
   */    
  public String currentFolderURL;  

  /**
   * Current file name 
   */  
  public String currentFileName;

  /**
   * Is current file new 
   */  
  public boolean isFileNew;

  protected void init(WFUserPreferences preferences) {      
    // init WebFileEditor
    initGadget();
    gadgetURL = getGadgetUrl();
    gadgetImagesURL = (gadgetURL != "") ? gadgetURL + "/" : "";
    webDAVURL = getWebDAVURL(preferences);
    
    // create controller instances
    browserPanelHandlers = new BrowserPanelHandlers(this);
    propertiesPanelHandlers = new PropertiesPanelHandlers(this);
    menuPanelHandlers = new MenuPanelHandlers(this);    
    
    // create view instances
    uploadWindow = (new UploadWindow(this)).getUploadWindow();    
    
    VerticalPanel mainPanel = new VerticalPanel();
    mainPanel.addStyleName("WebFileEditor-mainPanel");   
    mainPanel.setWidth("100%");
    mainPanel.setHeight("100%");
    
    // create buttons panel
    menuPanel = new MenuPanel(this);

    // create middle panel    
    VerticalPanel outerMiddlePanel = new VerticalPanel();
    
    HLayout middlePanel = new HLayout();
    middlePanel.setWidth("100%");
    middlePanel.setHeight(440);
    
    leftPanel = new LeftPanel(this);   
    rightPanel = new RightPanel(this);

    middlePanel.addMember(leftPanel.getLeftPanel());
    middlePanel.addMember(rightPanel.getRightPanel());

    outerMiddlePanel.add(middlePanel);
    
    mainPanel.add(menuPanel);
    mainPanel.add(outerMiddlePanel);
    
    RootPanel.get().add(mainPanel);

    browserPanelHandlers.updateFileList( browserPanelHandlers.getNewRootNode() );          // create RootNode if the browserTree 
    initWebFileEditor();                            // init editor buttons' states, clear EditorPanel, PropertiesPanel and setup file properties
  }

  // call in time of gadget loading
  public void initWebFileEditor() {
    // init MenuPanel buttons
    menuPanel.saveButton.setDisabled(true);
    menuPanel.saveAsButton.setDisabled(true);

    // clear ContentPanel
    rightPanel.clearEditorPanel();
    
    // clear PropertiesPanel
    propertiesPanelHandlers.clearPropertiesTable();
    
    // clear file properties
    setCurrentFileProperties("", "", "");
  }
  
  // call in time of file loading
  public void refreshEditor(String content, String contentType, TreeNode fileNode) {      
    // refresh current file properties
    if ( fileNode == null ) {
      setCurrentFileProperties("", contentType, "");     // if this is new file
    } else {
      setCurrentFileProperties(fileNode.getAttributeAsString("url"), contentType, fileNode.getAttributeAsString("name"));
    }
    
    // update editor panel
    rightPanel.reCreateEditorPanel();
    currentContentEditor.init( content );
    menuPanel.saveButton.setDisabled(true);    
  }

  public void setCurrentFileProperties(String folderURL, String contentType, String fileName) {
    currentFileMIMEType = FileModel.getMIMETypeOnContentType( contentType );
    currentFileEncodingCharset = FileModel.getEncodingCharsetOnContentType( contentType );
    currentFileType = FileModel.getFileTypeOnMIMEType( currentFileMIMEType );
    currentContentEditor = FileModel.getContentEditor( currentFileType, this );
    currentFolderURL = folderURL;
    currentFileName = fileName;
  }

  
  public native void JSdebugger() /*-{
    debugger;
  }-*/;
  
  public boolean confirmRewritingTheChangedFile() {
    if (isFileChanged()) {
      return Window.confirm("The currently loaded file is modified. Do you want to discard changes?");
    }
    return true;
  }

  public boolean confirmDeletingTheEditingFile() {
    return Window.confirm("The selected file is opened in the editor. Do you want to delete this one?");
  }
  
  
  public boolean isFileChanged() {
    if ( currentContentEditor != null ) {
      return currentContentEditor.isContentModified();
    } else {
      return false;
    }
  }  
  
  // ------------- JSNI methods
  private native void initGadget() /*-{
    // set width of gadget to 100%
    if ($wnd.frameElement == null) {
      return;
    }
    $wnd.frameElement.style.width = '100%';
  }-*/;
  
  private native String getGadgetUrl() /*-{ 
    // gathering the gadget's URL from the properties url of document.URL
    if ($wnd.gadgets == null) {
      return "";
    }
    return $wnd.gadgets.util.getUrlParameters().url.match(/(.*)\//)[1];
  }-*/;  

  private String getWebDAVURL(WFUserPreferences preferences) {

    String contextName = preferences.context().getValue();
    String repositoryName = preferences.repository().getValue();
    String workspaceName = preferences.workspace().getValue();
    return contextName + "/" + repositoryName + "/" + workspaceName + "/";

//    return "http://localhost:8080/rest/private/jcr/repository/collaboration";
  }
}

interface WFUserPreferences extends UserPreferences {

  @PreferenceAttributes(display_name = "context", //
      default_value = "/rest/private/jcr",
//      default_value = "/lightportal/rest/jcr",
      options = Options.HIDDEN)
  StringPreference context();

  @PreferenceAttributes(display_name = "repository", //
      default_value = "repository",
//      default_value = "lightrep",
      options = Options.HIDDEN)
  StringPreference repository();

  @PreferenceAttributes(display_name = "workspace", //
      default_value = "collaboration",
//      default_value = "production",
      options = Options.HIDDEN)
  StringPreference workspace();

}