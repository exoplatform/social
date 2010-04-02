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
 * The Class Profile.
 */
public class Profile {
  
  public static String AVATAR = "avatar";
  
  public static final String USERNAME = "username";

  public static final String FIRST_NAME = "firstName";
  
  public static final String LAST_NAME = "lastName";
  
  /** The properties. */
  private Map<String, Object> properties = new HashMap<String, Object>();
  
  /** The identity. */
  private Identity identity;
  
  /** The id. */
  private String id;

  /**
   * Instantiates a new profile.
   * 
   * @param id the id
   */
  public Profile(Identity id) {
    this.identity = id;
  }

  /**
   * Gets the identity.
   * 
   * @return the identity
   */
  public Identity getIdentity() {
    return identity;
  }

  /**
   * Gets the id.
   * 
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the id.
   * 
   * @param id the new id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the property.
   * 
   * @param name the name
   * @return the property
   */
  public Object getProperty(String name) {
    return properties.get(name);
  }

  /**
   * Sets the property.
   * 
   * @param name the name
   * @param value the value
   */
  public void setProperty(String name, Object value) {
    properties.put(name, value);
  }

  /**
   * Contains.
   * 
   * @param name the name
   * @return true, if successful
   */
  public boolean contains(String name) {
    return properties.containsKey(name);  
  }

  /**
   * Gets the properties.
   * 
   * @return the properties
   */
  public Map<String, Object> getProperties() {
    return properties;
  }

  /**
   * Removes the property.
   * 
   * @param name the name
   */
  public void removeProperty(String name) {
    properties.remove(name);
  }

  /**
   * Gets the property value.
   * 
   * @param name the name
   * @return the property value
   * @deprecated
   * @return
   */
  public Object getPropertyValue(String name) {
    return getProperty(name);
  }

  /**
   * Gets the full name.
   * 
   * @return the full name
   */
  public String getFullName() {
    String first = (String) getProperty(FIRST_NAME);
    String last = (String) getProperty(LAST_NAME);
    String all = (first != null) ? first : "";
    all += (last != null) ? " " + last : "";
    return all;
  }

}
