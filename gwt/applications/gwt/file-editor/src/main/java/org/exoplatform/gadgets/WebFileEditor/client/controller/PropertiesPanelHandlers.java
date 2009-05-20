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

import java.util.Set;

import org.exoplatform.gadgets.WebFileEditor.client.WebFileEditor;

import com.google.gwt.json.client.JSONObject;
import com.smartgwt.client.widgets.grid.ListGridRecord;

/**
 * Created by The eXo Platform SAS.
 * 
 * Handlers of PropertiesPanel events
 * 
 * @author <a href="mailto:dmitry.ndp@exoplatform.com.ua">Dmytro Nochevnov</a>
 * @version $Id: $
*/
public class PropertiesPanelHandlers {

  private WebFileEditor mainPanel;
  
  public PropertiesPanelHandlers(WebFileEditor mainPanel) {
    this.mainPanel = mainPanel;
  }
  
  public void updatePropertiesTable(JSONObject properties) {
    // clear propertiesTable
    clearPropertiesTable();

    // update propertiesTable
    if ( properties == null ) return;
    
    Set<String> keys = properties.keySet();
    for (String key : keys) {
      ResourceProperty newRow = new ResourceProperty(key, properties.get(key).toString());
      mainPanel.rightPanel.propertiesTable.addData(newRow);      
    }
    
  }

  public void clearPropertiesTable() {
    int recordCount = mainPanel.rightPanel.propertiesTable.getRecords().length;
    for (int i = 0; i < recordCount; i++) {
      mainPanel.rightPanel.propertiesTable.removeData( mainPanel.rightPanel.propertiesTable.getRecord(i) );
    }
  }  
}

  
class ResourceProperty extends ListGridRecord{
  public ResourceProperty(String name, String value) {  
    setAttribute("Name", name);
    setAttribute("Value", value);
  }  
}