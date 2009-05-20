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

package org.exoplatform.gadgets.WebFileEditor.client.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.Window;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

/**
 * Created by The eXo Platform SAS        .
 * 
 * PROPFIND's response data 
 * 
 * @version $Id: $
 */

public class PropfindResponse {
  
  private static String SUCCESS = "HTTP/1.1 200 OK";
  private Resource parent;
  private List<Resource> children;
  
  public PropfindResponse() {
    children = new ArrayList <Resource>();
  }

  /**
   * @return this resource
   */
  public final Resource getResource() {
    return parent;
  }

  /**
   * @return child resources
   */
  public final List<Resource> getChildren() {
    return children;
  }
  
  public String getAsString() {
    String ch = "";
    for(int i = 0; i < children.size(); i++) {
      ch += "" + children.get(i).href + "\n";
    }
    String pt = "";
    for(int i = 0; i < parent.properties.size(); i++) {
      Property p = parent.properties.get(i);
      pt+= "" + p.localName + "=" + p.value + " childProperties num: " + p.childProperties.size() + "\n";
    }   
    
    return "PROPFIND RESPONSE: " +
    "\n href: "+parent.href +
    "\n properties number: "+parent.properties.size() +
    "\n properties: "+pt+
    "\n children number: "+children.size()+
    "\n children: "+ch;
  }
 
  /**
   * parser for incoming XML text
   * @param text
   * @return
   */
  public static PropfindResponse parse(String text) {
    
    PropfindResponse response = new PropfindResponse();
    
    Document dom = XMLParser.parse(text);
    
    NodeList responseElements = dom.getElementsByTagName("response");
    
    for(int i = 0; i < responseElements.getLength(); i++) {
      
      Element resourceElement = (Element)responseElements.item(i);

      Element propstat = (Element)resourceElement.getElementsByTagName("propstat").item(0);
      String status = propstat.getElementsByTagName("status").item(0).getFirstChild().toString();
      

      if(!status.equals(SUCCESS))
        continue;
      
      String href = resourceElement.getElementsByTagName("href").item(0).getFirstChild().toString();

      Element prop = (Element)propstat.getElementsByTagName("prop").item(0);      
      
      Resource resource = new Resource(href);
      
      recurseProperties(prop, resource, null);
      
      if(i == 0) {
        response.parent = resource;
      } else {
        response.children.add(resource);
      }
    }
    Window.alert(response.getAsString());
    return response;
  }

  private static void recurseProperties(Element element,
      Resource resource, Property curProperty) {

    NodeList nodes = element.getChildNodes();

    for (int i = 0; i < nodes.getLength(); i++) {
      Node node = nodes.item(i);

      if (node.getNodeType() == Node.TEXT_NODE) {
        curProperty.value = node.getNodeValue();
      } else if (node.getNodeType() == Node.ELEMENT_NODE) {
        Property p = new Property(node.getNamespaceURI(), node.getNodeName());
        if(curProperty == null) {
          resource.properties.add(p);
        } else {
          curProperty.childProperties.add(p);
        }
        recurseProperties((Element) node, resource, p);
      }
    }
  }
  
  static class Resource {  
    final String href;
    List <Property> properties = new ArrayList<Property>();
    
    public Resource(String href) {
      this.href = href;
    }

    public String getHref() {
      return href;
    }
        
    public List <Property> getProperties() {
      return properties;
    }
  }

  static class Property {
    String namespace;
    String localName;
    String value;
    List<Property>childProperties;
    
    public Property(String namespace, String name) {
      this.namespace = namespace;
      int ind = name.indexOf(":");
      this.localName = (ind > 0)?name.substring(ind+1):name;
      this.childProperties = new ArrayList<Property>();
    }

    public final String getNamespace() {
      return namespace;
    }

    public final String getLocalName() {
      return localName;
    }

    public final String getValue() {
      return value;
    }

    public final List<Property> getChildProperties() {
      return childProperties;
    }  
  }
}