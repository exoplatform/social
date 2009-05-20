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

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridEditEvent;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.VisibilityMode;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.layout.SectionStack;
import com.smartgwt.client.widgets.layout.SectionStackSection;

public class RightPanel {

  private WebFileEditor mainPanel;  
  
  private SectionStackSection contentPanel;
  private SectionStackSection propertiesPanel;  
  private VerticalPanel innerVerticalPanel;
  private Canvas innerContentCanvas;
  private TextArea simpleTextEditor = new TextArea();
  private SectionStack rightPanel;  
  public ListGrid propertiesTable;
    
  public RightPanel(final WebFileEditor mainPanel) {
    this.mainPanel = mainPanel;
  }
   
  public SectionStack getRightPanel() {
    rightPanel = new SectionStack();
    rightPanel.setVisibilityMode(VisibilityMode.MUTEX);
    rightPanel.setAlign(Alignment.CENTER);
    rightPanel.setOverflow(Overflow.HIDDEN);
    rightPanel.setWidth("60%");
    rightPanel.setHeight(440);    

    createContentPanel();
    createPropertiesPanel(); 
    rightPanel.setSections(contentPanel, propertiesPanel);
    
    return rightPanel;
  }

  private void createContentPanel() {
    contentPanel = new SectionStackSection();

    innerContentCanvas = new Canvas();
    innerContentCanvas.setHeight100();
    innerContentCanvas.setWidth100();    
     
    contentPanel.addItem(innerContentCanvas);    
    contentPanel.setTitle("Content");
    contentPanel.setExpanded(true);
    contentPanel.setCanCollapse(true);    
  }

  public void clearEditorPanel() {
    if ( innerVerticalPanel != null) {
      innerVerticalPanel.removeFromParent();
    }    
  }
  
  public void reCreateEditorPanel() {  
    clearEditorPanel();
    
    innerVerticalPanel = new VerticalPanel();
    innerVerticalPanel.setHeight("100%");
    innerVerticalPanel.setWidth("100%");    
    
    simpleTextEditor = new TextArea();
    simpleTextEditor.setHeight("100%");
    simpleTextEditor.setWidth("100%");    
     
    DOM.setElementAttribute(simpleTextEditor.getElement(), "id", "WebFileEditor_editorTextArea");
    simpleTextEditor.addStyleName("WebFileEditor_editorTextArea");

    innerVerticalPanel.add(simpleTextEditor);

    innerContentCanvas.addChild(innerVerticalPanel);

    rightPanel.collapseSection(1);
    rightPanel.expandSection(0);
  };

  private void createPropertiesPanel() {
    propertiesPanel = new SectionStackSection();
    propertiesPanel.setTitle("Properties");

    propertiesTable = new ListGrid();
    propertiesTable.setShowAllRecords(true);

    ListGridField nameField = new ListGridField("Name");
    nameField.setCanEdit(false);
    
    ListGridField valueField = new ListGridField("Value");
    valueField.setCanEdit(true);
    
    propertiesTable.setFields(nameField, valueField);  
    propertiesTable.setCanEdit(true);  
    propertiesTable.setEditEvent(ListGridEditEvent.CLICK);  
    propertiesTable.setEditByCell(true);
    propertiesTable.setLeaveScrollbarGap(false); 
     
    propertiesPanel.setItems(propertiesTable);
    
    propertiesPanel.setExpanded(false);
    propertiesPanel.setCanCollapse(true);
  }   
}