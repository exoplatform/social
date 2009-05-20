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
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;
import com.smartgwt.client.widgets.events.DoubleClickEvent;
import com.smartgwt.client.widgets.events.DoubleClickHandler;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.tree.TreeNode;

/**
 * Created by The eXo Platform SAS.
 * 
 * Handlers of BrowserPanel events
 * 
 * @author <a href="mailto:dmitry.ndp@exoplatform.com.ua">Dmytro Nochevnov</a>
 * @version $Id: $
*/
public class BrowserPanelHandlers {

  private WebFileEditor mainPanel;
  int currentNodeId = 0;
  public TreeNode selectedNode;

  public BrowserPanelHandlers(WebFileEditor mainPanel) {
    this.mainPanel = mainPanel;
  }

  public DoubleClickHandler getBrowserDoubleClickHandler() {
    return new DoubleClickHandler() {

      public void onDoubleClick(DoubleClickEvent event) {
        // open the selected node
        selectedNode = getSelectedNode();
        
        if (selectedNode.getAttributeAsBoolean("isFolder")) {
          if (!selectedNode.getAttributeAsBoolean("isFetched")) {
            // mainPanel.currentBrowserNode = (TreeNode) selectedRecord;        
            mainPanel.browserPanelHandlers.updateFileList( null );
            // mainPanel.leftPanel.browserTree.openFolder(mainPanel.currentBrowserNode);
            // mainPanel.propertiesPanelHandlers.updatePropertiesTable( (JSONObject) mainPanel.currentBrowserNode.getAttributeAsObject("resourceProperties") );            
          }
        } else {
          if ( ! mainPanel.confirmRewritingTheChangedFile() ) return;
          mainPanel.isFileNew = false;
          loadFileIntoEditorPanel();
          // mainPanel.propertiesPanelHandlers.updatePropertiesTable( (JSONObject) mainPanel.currentBrowserNode.getAttributeAsObject("resourceProperties") );          
        }
      }
      
    };
  }  
  
  public TreeNode getSelectedNode() {
    ListGridRecord selectedRecord = mainPanel.leftPanel.browserTreeGrid.getSelectedRecord();
    return (TreeNode) selectedRecord;
  }
 
  // create root node and select it at the start of gadget
  public TreeNode getNewRootNode() {    
    // create root node
    TreeNode selectedNode = mainPanel.leftPanel.browserTree.getRoot();
    selectedNode.setAttribute("url", mainPanel.webDAVURL);
    selectedNode.setAttribute("itemId", currentNodeId);
    selectedNode.setAttribute("isFolder", true);
    selectedNode.setAttribute("isFetched", false);
    selectedNode.setAttribute("resourceProperties", new JSONObject());
    
    return selectedNode;
  }

  // if treeNode = null, than selected node will be updated
  public void updateFileList(TreeNode treeNode) {
    if ( treeNode != null ) {
      selectedNode = treeNode;
    } else {
      selectedNode = getSelectedNode();
      if ( selectedNode == null) return;
    }    
    
    try {
      final String url = selectedNode.getAttribute("url");

      ServerModel.getChildrenProperties(url, new FileDataHandler() {
  
        public void onError(Request request, Throwable e) {
          Window.alert("Can't get file list " + e.getMessage() + " " + url);
          updateBrowserNode(null);
        }

        public void onSuccess(Request request, Response response) {
          com.google.gwt.xml.client.Document xml = XMLParser.parse(response.getText());                 
          updateBrowserNode(xml);
        }
      });
    } catch (RequestException e) {
      Window.alert("Getting file list was caused an error: " + e);
    }
  }

  private void updateBrowserNode(com.google.gwt.xml.client.Document xml) {
    // clear currentBrowserNode
    mainPanel.leftPanel.browserTree.removeList( mainPanel.leftPanel.browserTree.getChildren(selectedNode) );

    if (xml == null) return;

    // update currentBrowserNode
    int currentNodeParrentId = selectedNode.getAttributeAsInt("itemId");

    NodeList itemList = xml.getElementsByTagName("response");
    for (int i = 1; i < itemList.getLength(); i++) {
      NodeList responseItem = itemList.item(i).getChildNodes();
      
      if ( responseItem.item(0) == null || responseItem.item(0).getFirstChild() == null ) continue;
      
      String url = responseItem.item(0).getFirstChild().getNodeValue(); // read href subnode
      currentNodeId++;
      String name = FileModel.getNameOnUrl(url);
      boolean isFolder = FileModel.isFolder(url);
      boolean isFetched = false;

      JSONObject resourceProperties = fetchProperties(responseItem.item(1).getChildNodes().item(0).getChildNodes()); // read <D:propstat><D:prop> subnode
      
      // create node element
      TreeNode newNode = new BrowserTreeNode(name, currentNodeId, currentNodeParrentId, url, isFolder, isFetched, resourceProperties);
      mainPanel.leftPanel.browserTree.add(newNode, selectedNode);
    }

    selectedNode.setAttribute("isFetched", true);
  }

  private JSONObject fetchProperties(NodeList propertyItems) {
    JSONObject properties = new JSONObject();
    for (int i = 1; i < propertyItems.getLength(); i++) {
      Node propertyItem = propertyItems.item(i);
      String propertyName = propertyItem.getNodeName();
      JSONString propertyValue;
      
      if ( propertyItem.getFirstChild() == null ) continue;
      
      switch (propertyItem.getFirstChild().getNodeType()) {
        case Node.TEXT_NODE:
          propertyValue = new JSONString(propertyItem.getFirstChild().getNodeValue());
          properties.put(propertyName, propertyValue);
          break;
        case Node.ELEMENT_NODE:
          if ( propertyItem.getChildNodes().item(0) != null ) {
            propertyValue = new JSONString(propertyItem.getChildNodes().item(0).getNodeName());
            properties.put(propertyName, propertyValue);
          }
          break;
        }
    }

    return properties;
  }

  private void loadFileIntoEditorPanel() {  
    if ( ! mainPanel.confirmRewritingTheChangedFile() ) return;
    
    try {
      //mainPanel.JSdebugger();
      
      ServerModel.getFile(selectedNode.getAttributeAsString("url"), new FileDataHandler(){

        public void onError(Request request, Throwable e) {
          Window.alert("Can't load file '" + selectedNode.getAttributeAsString("url") + "': " + e.getMessage());
        }

        public void onSuccess(Request request, Response response) {
          // set Current File Properties

          mainPanel.JSdebugger();

          String contentType = "text/plain";
          JSONObject resourceProperties = (JSONObject) selectedNode.getAttributeAsObject("resourceProperties");
          if ( resourceProperties != null && resourceProperties.containsKey("D:getcontenttype") ) {
            contentType = resourceProperties.get("D:getcontenttype").toString();
          } 
          
          mainPanel.refreshEditor( response.getText(), contentType, selectedNode ); 
        }
      });
    } catch (RequestException e) {
      Window.alert("Loading file was caused an error: " + e);
    }
  }
  
}

class BrowserTreeNode extends TreeNode {
  public BrowserTreeNode(String name, int itemId, int parentId, String url, boolean isFolder, boolean isFetched, JSONObject resourceProperties) {
    setAttribute("name", name);
    setAttribute("itemId", itemId);
    setAttribute("parentId", parentId);
    setAttribute("url", url);    
    setAttribute("isFolder", isFolder);
    setAttribute("isFetched", isFetched);
    setAttribute("resourceProperties", resourceProperties);
  }
}