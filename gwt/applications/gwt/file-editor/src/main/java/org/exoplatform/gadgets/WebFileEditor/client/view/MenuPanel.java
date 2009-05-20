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
import org.exoplatform.gadgets.WebFileEditor.client.model.FileModel;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.menu.Menu;
import com.smartgwt.client.widgets.menu.MenuButton;
import com.smartgwt.client.widgets.menu.MenuItem;

public class MenuPanel extends Composite {

  private WebFileEditor mainPanel;
  public MenuButton saveButton;
  public MenuButton saveAsButton;
  private MenuButton newButton;
  private MenuItem newXMLFileButton;
  private MenuItem newGroovyFileButton;  
  private MenuItem newFolderButton;  
  private MenuButton deleteButton;
  private MenuButton uploadButton;
  
  public MenuPanel(final WebFileEditor mainPanel) {
    this.mainPanel = mainPanel;
    
    HorizontalPanel buttonsPanel = new HorizontalPanel();
    buttonsPanel.addStyleName("WebFileEditor-buttonsPanel");
            
    createSaveButton();  
    buttonsPanel.add(saveButton);
    
    createSaveAsButton();  
    buttonsPanel.add(saveAsButton);

    createNewButton();  
    buttonsPanel.add(newButton);

    createDeleteButton();  
    buttonsPanel.add(deleteButton);

    createUploadButton();  
    buttonsPanel.add(uploadButton);
    
    initWidget(buttonsPanel);
  }

  private void createSaveButton() {
    saveButton = new MenuButton("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Save");

    Image image = new Image(mainPanel.gadgetImagesURL + "images/Save.gif");
    image.addStyleName("WebFileEditor-buttonImage");
    
    saveButton.addClickHandler( mainPanel.menuPanelHandlers.getSaveButtonHandler() );
    
    saveButton.addChild(image);
    saveButton.setCanHover(false);
    saveButton.setBorder("1px solid white");
  }
  
  private void createSaveAsButton() {
    saveAsButton = new MenuButton("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Save&nbsp;As");
    
    Image image = new Image(mainPanel.gadgetImagesURL + "images/Save.gif");
    image.addStyleName("WebFileEditor-buttonImage");
    
    saveAsButton.addClickHandler( mainPanel.menuPanelHandlers.getSaveAsButtonHandler() );
    saveAsButton.addChild(image);
    saveAsButton.setCanHover(false);
    saveAsButton.setBorder("1px solid white");
  }
  
  private void createNewButton() {
    Menu menu = new Menu();
    menu.setShowShadow(true);  
    menu.setShadowDepth(10);
    
    newXMLFileButton = new MenuItem("New XML File", mainPanel.gadgetImagesURL + "images/AddPage.gif");
    newXMLFileButton.addClickHandler( mainPanel.menuPanelHandlers.getNewFileButtonHandler(FileModel.XML) );
        
    newGroovyFileButton = new MenuItem("New Groovy File", mainPanel.gadgetImagesURL + "images/AddPage.gif");
    newGroovyFileButton.addClickHandler( mainPanel.menuPanelHandlers.getNewFileButtonHandler(FileModel.GROOVY) );
    
    newFolderButton = new MenuItem("New Folder", mainPanel.gadgetImagesURL + "images/AddNewCategory.gif");
    newFolderButton.addClickHandler( mainPanel.menuPanelHandlers.getNewFolderButtonHandler() );
    
    menu.setItems(newXMLFileButton, newGroovyFileButton, newFolderButton);   
    
    newButton = new MenuButton("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;New", menu);

    Image image = new Image(mainPanel.gadgetImagesURL + "images/AddPage.gif");
    image.addStyleName("WebFileEditor-buttonImage");
    
    newButton.addChild(image);
    newButton.setBorder("1px solid white");    
  }
  
  private void createDeleteButton() {
    deleteButton = new MenuButton("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Delete");
    deleteButton.addClickHandler( mainPanel.menuPanelHandlers.getDeleteButtonHandler() );
    
    Image image = new Image(mainPanel.gadgetImagesURL + "images/DustBin.png");
    image.addStyleName("WebFileEditor-buttonImage");

    deleteButton.addChild(image);
    deleteButton.setCanHover(false);
    deleteButton.setBorder("1px solid white");
  }
  
  private void createUploadButton() {
    uploadButton = new MenuButton("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Upload");
//    uploadButton.setTitleStyle("WebFileEditor-menuButtonTitle");
    
    Image image = new Image(mainPanel.gadgetImagesURL + "images/UploadFile.png");
    image.addStyleName("WebFileEditor-buttonImage");
    uploadButton.addChild(image);
//    uploadButton.setMenuButtonImage(mainPanel.gadgetImagesURL + "UploadFile.png");
//    uploadButton.setIcon(mainPanel.gadgetImagesURL + "UploadFile.png");
//    uploadButton.setIconSize(16);
    uploadButton.setCanHover(false);
    uploadButton.addClickHandler( mainPanel.menuPanelHandlers.getUploadButtonHandler() );
    uploadButton.setBorder("1px solid white");
  }  

  
}