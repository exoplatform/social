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
package org.exoplatform.social.core.relationship;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.social.core.identity.model.Identity;

// TODO: Auto-generated Javadoc
/**
 * The Class Relationship.
 */
public class Relationship {
  
  /** The id. */
  private String         id;

  /** The properties. */
  private List<Property> properties;

  /** The identity1. */
  private Identity       identity1;

  /** The identity2. */
  private Identity       identity2;

  /** The status. */
  private Type           status = Type.PENDING;

  /**
   * The Enum Type.
   */
  public enum Type {
    
    /** The PENDING. */
    PENDING, 
 /** The CONFIRM. */
 CONFIRM, 
 /** The IGNORE. */
 IGNORE, 
 /** The ALIEN. */
 ALIEN, 
 /** The REQUIR e_ validation. */
 REQUIRE_VALIDATION, 
 /** The SELF. */
 SELF
  }

  /**
   * Instantiates a new relationship.
   * 
   * @param identity1 the identity1
   * @param identity2 the identity2
   */
  public Relationship(Identity identity1, Identity identity2) {
    this(identity1, identity2, new ArrayList<Property>());
  }

  /**
   * Instantiates a new relationship.
   * 
   * @param identity1 the identity1
   * @param identity2 the identity2
   * @param properties the properties
   */
  public Relationship(Identity identity1, Identity identity2, List<Property> properties) {
    this.identity1 = identity1;
    this.identity2 = identity2;
    this.properties = properties;
  }

  /**
   * Instantiates a new relationship.
   * 
   * @param uuid the uuid
   */
  public Relationship(String uuid) {
    this.id = uuid;
    this.properties = new ArrayList<Property>();
  }

  /**
   * Gets the identity1.
   * 
   * @return the identity1
   */
  public Identity getIdentity1() {
    return identity1;
  }

  /**
   * Gets the identity2.
   * 
   * @return the identity2
   */
  public Identity getIdentity2() {
    return identity2;
  }

  /**
   * Sets the identity1.
   * 
   * @param identity1 the new identity1
   */
  public void setIdentity1(Identity identity1) {
    this.identity1 = identity1;
  }

  /**
   * Sets the identity2.
   * 
   * @param identity2 the new identity2
   */
  public void setIdentity2(Identity identity2) {
    this.identity2 = identity2;
  }

  /**
   * Gets the properties.
   * 
   * @return the properties
   */
  public List<Property> getProperties() {
    return properties;
  }

  /**
   * Sets the properties.
   * 
   * @param properties the new properties
   */
  public void setProperties(List<Property> properties) {

    this.properties = properties;
  }

  /**
   * Adds the property.
   * 
   * @param property the property
   */
  public void addProperty(Property property) {
    properties.add(property);
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
   * Gets the status.
   * 
   * @return the status
   */
  public Type getStatus() {
    return status;
  }

  /**
   * Sets the status.
   * 
   * @param status the new status
   */
  public void setStatus(Type status) {
    this.status = status;
  }

  /**
   * Gets the properties.
   * 
   * @param status the status
   * @return the properties
   */
  public List<Property> getProperties(Type status) {
    List<Property> pendingProps = new ArrayList<Property>();
    for (Property prop : properties) {
      if (prop.getStatus() == status)
        pendingProps.add(prop);
    }
    return pendingProps;
  }
}
