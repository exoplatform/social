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

import com.google.gwt.user.client.Window;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.Encoding;
import com.smartgwt.client.types.VerticalAlignment;
import com.smartgwt.client.widgets.IButton;
import com.smartgwt.client.widgets.events.ClickEvent;
import com.smartgwt.client.widgets.events.ClickHandler;
import com.smartgwt.client.widgets.form.DynamicForm;
import com.smartgwt.client.widgets.form.fields.ToolbarItem;
import com.smartgwt.client.widgets.form.fields.UploadItem;

public class UploadWindow {
  private WebFileEditor mainPanel;
  private com.smartgwt.client.widgets.Window uploadWindow;
  
  public UploadWindow(WebFileEditor mainPanel) {
    this.mainPanel = mainPanel;
  }
  
  public com.smartgwt.client.widgets.Window getUploadWindow() {  
    uploadWindow = new com.smartgwt.client.widgets.Window();    
    uploadWindow.setWidth(300);  
    uploadWindow.setHeight(140);  
    uploadWindow.setTitle("File upload");
    uploadWindow.setShowMinimizeButton(false);  
    uploadWindow.setIsModal(true);
    uploadWindow.centerInPage();
    
    // create upload form
    final DynamicForm uploadForm = new DynamicForm();
    uploadForm.setAction("/");
    uploadForm.setEncoding(Encoding.MULTIPART);
    uploadForm.setWidth(200);
    uploadForm.setPadding(15);
    uploadForm.setLayoutAlign(VerticalAlignment.BOTTOM);
    uploadForm.setLayoutAlign(Alignment.CENTER);
    
    final UploadItem uploadItem = new UploadItem();
    uploadItem.setShowTitle(false);
    uploadItem.setTitleAlign(Alignment.LEFT);
    uploadForm.setFields(uploadItem);
    
    // create upload buttons
    DynamicForm uploadWindowButtonsForm = new DynamicForm();
    uploadWindowButtonsForm.setWidth(200);
    uploadWindowButtonsForm.setPadding(15);
    uploadWindowButtonsForm.setLayoutAlign(VerticalAlignment.BOTTOM);
    uploadWindowButtonsForm.setLayoutAlign(Alignment.CENTER);

    IButton uploadButton = new IButton("Save");
    uploadButton.setIcon(mainPanel.gadgetImagesURL + "images/UploadFile.png");
    uploadButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        uploadForm.submitForm();
      }
    });
  
    uploadForm.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        String filename = FileModel.getNameOnUrl(uploadItem.getValue().toString());
        if (filename.length() == 0) {
          Window.alert("Please, select file to upload!");         
        } else {
        }
      }
    });
  
    IButton closeButton = new IButton("Close");
    closeButton.setIcon(mainPanel.gadgetImagesURL + "images/BlackClose.gif");
    closeButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        uploadWindow.hide();
      }
    });
  
    ToolbarItem buttonToolbar = new ToolbarItem();
    buttonToolbar.setButtons(uploadButton, closeButton);

    uploadWindowButtonsForm.setFields(buttonToolbar);
 
    uploadWindow.addItem(uploadForm);
    uploadWindow.addItem(uploadWindowButtonsForm);
    
    return uploadWindow;
  }
}