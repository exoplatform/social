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
package org.exoplatform.social.core.identity.model;

import java.util.Map;
import java.util.HashMap;


// TODO: Auto-generated Javadoc
/**
 * The Class PropertiesDefinition.
 */
public class PropertiesDefinition {
  
  /** The properties def. */
  private Map<String, String> propertiesDef = new HashMap<String, String>();

  // this will be replaced by an xml configuration file that will define all
  // the properties
  /**
   * Instantiates a new properties definition.
   */
  public PropertiesDefinition(){
    propertiesDef.put("firstName", "firstName");
    propertiesDef.put("lastName", "lastName");

    // multi value: List<String>
    propertiesDef.put("emails", "emails");
    
    propertiesDef.put("username", "username");

  }

  /**
   * Gets the.
   * 
   * @param name the name
   * @return the string
   */
  public String get(String name){
    return propertiesDef.get(name);
  }


}
