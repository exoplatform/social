/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.gadgets.WebFileEditor.client.controller;

import org.exoplatform.gadgets.WebFileEditor.client.WebFileEditor;
import org.exoplatform.gadgets.WebFileEditor.client.model.FileModel;
import org.exoplatform.gadgets.WebFileEditor.client.model.ServerModel;
import org.exoplatform.gadgets.WebFileEditor.client.model.ServerModel.FileDataHandler;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.menu.events.MenuItemClickEvent;
import com.smartgwt.client.widgets.tree.TreeNode;

/**
 * Created by The eXo Platform SAS.
 * 
 * Handlers of MenuPanel events
 * 
 * @author <a href="mailto:dmitry.ndp@exoplatform.com.ua">Dmytro Nochevnov</a>
 * @version $Id: $
*/
public class MenuPanelHandlers {

  public WebFileEditor mainPanel;
  
  public MenuPanelHandlers(WebFileEditor mainPanel) {
    this.mainPanel = mainPanel;
  }  

  public ClickHandler getUploadButtonHandler() {
    return new ClickHandler() {
      public void onClick(ClickEvent event) {
        mainPanel.uploadWindow.show();
      }
    };
  } 
  
  public com.smartgwt.client.widgets.menu.events.ClickHandler getNewFileButtonHandler(final int fileType) {
    return new com.smartgwt.client.widgets.menu.events.ClickHandler() {
      public void onClick(MenuItemClickEvent event) {
        if ( ! mainPanel.confirmRewritingTheChangedFile() ) return;
        
        String sampleContent = FileModel.getSampleContent( fileType );
        String contentType = FileModel.getMIMETypeOnFileType(fileType);
        
        mainPanel.refreshEditor(sampleContent, contentType, null);
        mainPanel.menuPanel.saveAsButton.setDisabled(false);        
        mainPanel.isFileNew = true;
      }
    };    
  }

  public com.smartgwt.client.widgets.menu.events.ClickHandler getNewFolderButtonHandler() {
    return new com.smartgwt.client.widgets.menu.events.ClickHandler() {

      private String newFolderName;
      private String folderURL;
      private TreeNode selectedNode;      
      
      public void onClick(MenuItemClickEvent event) {
        folderURL = mainPanel.webDAVURL; // set webDAV root folder as default url of folder
        selectedNode = mainPanel.browserPanelHandlers.getSelectedNode();
        if ( selectedNode != null ) {
          folderURL = FileModel.getPathOnUrl( selectedNode.getAttributeAsString("url") );
        }
        
        newFolderName = Window.prompt("Please, type new file name " + folderURL + "/", "NewFolder");
        if ( ! FileModel.checkFileName(newFolderName) ) return;

        try {
          ServerModel.createFolder(folderURL + "/" + newFolderName, new FileDataHandler() {
            
            public void onError(Request request, Throwable e) {
              Window.alert("Can't create folder '" + folderURL + "/" + newFolderName + "': " + e.getMessage());
            }

            public void onSuccess(Request request, Response response) {               
              mainPanel.browserPanelHandlers.updateFileList( null ); // update current Tree Node in the BrowserPanel
            }

          });
        } catch (RequestException e) {
          Window.alert("Creating folder '" + folderURL + "/" + newFolderName + "' was caused an error: " + e);
        }
      }
      
    };
  }
  
  public ClickHandler getSaveAsButtonHandler() {
    return new ClickHandler() {
      private String folderURL;
      private String newFileName; 
      private TreeNode selectedNode;        
      
      public void onClick(ClickEvent event) {
        folderURL = mainPanel.webDAVURL; // set webDAV root folder as default url of folder
        selectedNode = mainPanel.browserPanelHandlers.getSelectedNode();
        if ( selectedNode != null ) {
          folderURL = FileModel.getPathOnUrl( selectedNode.getAttributeAsString("url") );
        }
        
        String fileExtension = FileModel.getFileExtensionOnFileType( FileModel.getFileTypeOnMIMEType( mainPanel.currentFileMIMEType ) );
        newFileName = Window.prompt("Please, type new file name " + folderURL + "/", "example." + fileExtension);
        if ( ! FileModel.checkFileName(newFileName) ) return;
        
        String content = mainPanel.currentContentEditor.getCode();
        String contentType = FileModel.getContentTypeOnMIMEAndCharset(mainPanel.currentFileMIMEType, mainPanel.currentFileEncodingCharset);

        try {
          ServerModel.saveFile(folderURL + "/" + newFileName, content, contentType, new FileDataHandler() {
            
            public void onError(Request request, Throwable e) {
              Window.alert("Can't save file '" + folderURL + "/" + newFileName + "': " + e.getMessage());
            }

            public void onSuccess(Request request, Response response) {               
              mainPanel.browserPanelHandlers.updateFileList( null ); // update current Tree Node in the BrowserPanel
              String contentType = FileModel.getContentTypeOnMIMEAndCharset(mainPanel.currentFileMIMEType, mainPanel.currentFileEncodingCharset);

              if ( mainPanel.isFileNew ) { 
                mainPanel.setCurrentFileProperties(folderURL, contentType, newFileName);
                mainPanel.isFileNew = false;
              }
              
              mainPanel.menuPanel.saveButton.setDisabled(true);
            }

          });
        } catch (RequestException e) {
          Window.alert("Saving file '" + folderURL + "/" + newFileName + "' was caused an error: " + e);
        }
      }
    };
  }
  
  public ClickHandler getSaveButtonHandler() {
    return new ClickHandler() {

      public void onClick(ClickEvent event) {      
        String content = mainPanel.currentContentEditor.getCode();
        String contentType = FileModel.getContentTypeOnMIMEAndCharset(mainPanel.currentFileMIMEType, mainPanel.currentFileEncodingCharset);

        try {
          ServerModel.saveFile(mainPanel.currentFolderURL + "/" + mainPanel.currentFileName, content, contentType, new FileDataHandler() {
            
            public void onError(Request request, Throwable e) {
              Window.alert("Can't save file '" + mainPanel.currentFolderURL  + "/" + mainPanel.currentFileName + "': " + e.getMessage());
            }

            public void onSuccess(Request request, Response response) {
              mainPanel.menuPanel.saveButton.setDisabled(true);
            }
          });
        } catch (RequestException e) {
          Window.alert("Saving file was caused an error: " + e);
        }
      }
    }; 
  }

  public ClickHandler getDeleteButtonHandler() {
    return new ClickHandler() {

      private String url;
      private boolean isSelectedFileEditing;
      private TreeNode parentNode;
      private TreeNode selectedNode;
      
      public void onClick(ClickEvent event) {
        selectedNode = mainPanel.browserPanelHandlers.getSelectedNode();
        if (selectedNode == null) return;

        url = selectedNode.getAttributeAsString("url");
        
        parentNode = mainPanel.leftPanel.browserTree.getParent(selectedNode);
        
        if (url == null || url.length() < 1) return;

        // test if selected file is editing file
        isSelectedFileEditing = false;
        if ( (mainPanel.currentFolderURL + "/" + mainPanel.currentFileName).equals(url) ) {
          if ( mainPanel.confirmDeletingTheEditingFile() ) {
            return;
          } else {
            isSelectedFileEditing = true;
          }
        }
        
        try {
          ServerModel.delete(url, new FileDataHandler() {
            
            public void onError(Request request, Throwable e) {
              Window.alert("Can't delete '" + url + "': " + e.getMessage());
            }

            public void onSuccess(Request request, Response response) {               
              
              if ( parentNode != null ) {
                mainPanel.browserPanelHandlers.updateFileList( parentNode ); // update parent Tree Node in the BrowserPanel                
              }

              if (isSelectedFileEditing) {
                mainPanel.initWebFileEditor();                
              }
            }

          });
        } catch (RequestException e) {
          Window.alert("Deleting '" + url + "' was caused an error: " + e);
        }       
      }
      
    };
  }
}