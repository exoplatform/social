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

import org.exoplatform.social.core.identity.model.Identity;

// TODO: Auto-generated Javadoc
/**
 * The Class Property.
 */
public class Property {
  
  /** The id. */
  protected String id;
  
  /** The name. */
  protected String name;
  
  /** The is symetric. */
  protected boolean isSymetric;
  
  /** The status. */
  protected Relationship.Type status;
  
  /** The initiator. */
  protected Identity initiator;

  /**
   * Instantiates a new property.
   */
  public Property() {
    this(null, false, Relationship.Type.PENDING, null);
  }

  /**
   * Instantiates a new property.
   * 
   * @param name the name
   */
  public Property(String name) {
    this(name, false, Relationship.Type.PENDING, null);
  }

  /**
   * Instantiates a new property.
   * 
   * @param name the name
   * @param isSymetric the is symetric
   */
  public Property(String name, boolean isSymetric) {
    this(name, isSymetric, Relationship.Type.PENDING, null);
  }

  /**
   * Instantiates a new property.
   * 
   * @param name the name
   * @param isSymetric the is symetric
   * @param status the status
   */
  public Property(String name, boolean isSymetric, Relationship.Type status) {
    this(name, isSymetric, status, null);
  }

  /**
   * Instantiates a new property.
   * 
   * @param name the name
   * @param isSymetric the is symetric
   * @param status the status
   * @param initiator the initiator
   */
  public Property(String name, boolean isSymetric, Relationship.Type status, Identity initiator) {
    this.name = name;
    this.isSymetric = isSymetric;
    this.status = status;
    this.initiator = initiator;
  }

  /**
   * Checks if is symetric.
   * 
   * @return true, if is symetric
   */
  public boolean isSymetric() {
    return isSymetric;
  }

  /**
   * Sets the symetric.
   * 
   * @param symetric the new symetric
   * @throws Exception the exception
   */
  public void setSymetric(boolean symetric) throws Exception {
    isSymetric = symetric;
  }

  /**
   * if the relation is not symetric, it always return Relationship.Type.CONFIRM
   * 
   * @return the status
   * @return
   */
  public Relationship.Type getStatus() {
    if (!isSymetric)
      return Relationship.Type.CONFIRM;
    return status;
  }

  /**
   * Sets the status.
   * 
   * @param status the new status
   */
  public void setStatus(Relationship.Type status) {
    this.status = status;
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
   * Gets the name.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name.
   * 
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the initiator.
   * 
   * @return the initiator
   */
  public Identity getInitiator() {
    return initiator;
  }

  /**
   * Sets the initiator.
   * 
   * @param initiator the new initiator
   */
  public void setInitiator(Identity initiator) {
    this.initiator = initiator;
  }
}
