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

package org.exoplatform.gadgets.WebFileEditor.client.view;

import org.exoplatform.gadgets.WebFileEditor.client.WebFileEditor;

import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.TreeModelType;
import com.smartgwt.client.types.VisibilityMode;
import com.smartgwt.client.widgets.ImgButton;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.FormItem;
import com.smartgwt.client.widgets.form.fields.FormItemIcon;
import com.smartgwt.client.widgets.form.fields.TextItem;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.SectionStackSection;
import com.smartgwt.client.widgets.tree.Tree;
import com.smartgwt.client.widgets.tree.TreeGrid;
import com.smartgwt.client.widgets.tree.TreeGridField;

public class LeftPanel {

  private WebFileEditor mainPanel;
  
  public TreeGrid browserTreeGrid;
  private SectionStackSection browserPanel;
  private SectionStackSection searchPanel;
  private TextItem searchTextItem;
  private ImgButton refreshButton;
  public Tree browserTree;
  
  
  public LeftPanel(final WebFileEditor mainPanel) {
    this.mainPanel = mainPanel;
  }

  public SectionStack getLeftPanel() {  
    SectionStack leftPanel = new SectionStack();   
    leftPanel.setVisibilityMode(VisibilityMode.MULTIPLE);
    leftPanel.setOverflow(Overflow.HIDDEN);
    leftPanel.setAlign(Alignment.CENTER);
    leftPanel.setWidth("40%");
    leftPanel.setHeight("100%");    
    leftPanel.setShowResizeBar(true);
  
    createBrowserPanel();   
    createSearchPanel();
  
    leftPanel.setSections(browserPanel, searchPanel);
    
    return leftPanel;
  }
  
  private void createBrowserPanel() {  
    browserPanel = new SectionStackSection();
    browserPanel.setTitle("Browse");
        
    TreeGridField nameField = new TreeGridField("name");
    
    browserTree = new Tree();
    browserTree.setModelType(TreeModelType.PARENT);  
    browserTree.setRootValue(1);  
    browserTree.setNameProperty("name");
    browserTree.setIdField("itemId");  
    browserTree.setParentIdField("parentId");
    
    browserTreeGrid = new TreeGrid();  
    // treeItem.setFolderIcon(folderIcon);
    // treeItem.setNodeIcon(folderIcon);
    browserTreeGrid.setShowHeader(false);
    browserTreeGrid.setAutoFetchData(true);  
    browserTreeGrid.setCanReorderRecords(true);  
    browserTreeGrid.setCanAcceptDroppedRecords(false);
    browserTreeGrid.setCanFreezeFields(true);  
    browserTreeGrid.setCanReparentNodes(true);     
    browserTreeGrid.setShowOpenIcons(true);
    browserTreeGrid.setLeaveScrollbarGap(false);    
    browserTreeGrid.setFields(nameField);
    browserTreeGrid.setData(browserTree);

    browserTreeGrid.addDoubleClickHandler( 
      mainPanel.browserPanelHandlers.getBrowserDoubleClickHandler()
    );
    
    createRefreshButton();
        
    browserPanel.setControls(refreshButton);
    browserPanel.setItems(browserTreeGrid);
    browserPanel.setExpanded(true);
    browserPanel.setCanCollapse(false);
  }
      
  private void createRefreshButton() {
    refreshButton = new ImgButton();
    refreshButton.setSrc(mainPanel.gadgetImagesURL + "images/RefreshIcon.gif");
    refreshButton.setShowRollOver(false);  
    refreshButton.setShowDown(false);
    refreshButton.setShowFocusedAsOver(true);
    refreshButton.setWidth(18);  
    refreshButton.setHeight(18);    
    refreshButton.setTooltip("Refresh");    
    refreshButton.addClickHandler( getRefreshButtonHandler() );
  }

  private ClickHandler getRefreshButtonHandler() {
    return new ClickHandler() {
      public void onClick(ClickEvent event) {
        mainPanel.browserPanelHandlers.updateFileList( null );
        mainPanel.propertiesPanelHandlers.updatePropertiesTable(null);
      }
    };
  }

  private void createSearchPanel() {
    searchPanel = new SectionStackSection();
    searchPanel.setTitle("Search");
    
    DynamicForm searchForm = new DynamicForm(); 
    createSearchTextItem();
    searchForm.setFields(new FormItem[] {searchTextItem});
    
    Label advancedSearchLabel = new Label("Advanced Search");
    
    searchPanel.setItems(advancedSearchLabel);
    searchPanel.setControls(searchForm);
    searchPanel.setExpanded(false);
    searchPanel.setCanCollapse(true);    
  }

  private void createSearchTextItem() {
    searchTextItem = new TextItem();
    FormItemIcon searchIcon = new FormItemIcon();
    searchIcon.setSrc(mainPanel.gadgetImagesURL + "images/LightBulbOn.gif");
    searchTextItem.setIcons(searchIcon);
    searchTextItem.setDefaultValue("search");
    searchTextItem.setShowTitle(false);
  }  
}