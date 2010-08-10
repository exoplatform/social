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
package org.exoplatform.social.core.relationship.model;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.social.core.identity.model.Identity;

/**
 * Relationship between 2 social identities
 */
public class Relationship {

  /** The id. */
  private String         id;

  /** The properties. */
  private List<Property> properties;

  /** The sender. */
  private Identity       sender;

  /** The receiver. */
  private Identity       receiver;

  /** The status. */
  private Type           status = Type.PENDING;

  /**
   * The Relationship Type Enum
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
    /** The REQUIRE_VALIDATION. */
    REQUIRE_VALIDATION,
    /** The SELF. */
    SELF,
    /** The SPACE_MEMBER type **/
    SPACE_MEMBER
  }

  /**
   * Instantiates a new relationship.
   * 
   * @param sender the sender
   * @param receiver the receiver
   */
  public Relationship(Identity sender, Identity receiver) {
    this(sender, receiver, new ArrayList<Property>());
  }

  /**
   * Instantiates a new relationship.
   * 
   * @param sender the sender
   * @param receiver the receiver
   * @param properties the properties
   */
  public Relationship(Identity sender, Identity receiver, List<Property> properties) {
    this.sender = sender;
    this.receiver = receiver;
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
   * Gets the sender.
   * 
   * @return the sender
   */
  public Identity getSender() {
    return sender;
  }

  /**
   * Gets the receiver.
   * 
   * @return the receiver
   */
  public Identity getReceiver() {
    return receiver;
  }

  /**
   * Sets the sender.
   * 
   * @param sender the new sender
   */
  public void setSender(Identity sender) {
    this.sender = sender;
  }

  /**
   * Sets the receiver.
   * 
   * @param receiver the new receiver
   */
  public void setReceiver(Identity receiver) {
    this.receiver = receiver;
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

  public String toString() {
    return sender + "--[" + status + "]--" + receiver;
  }
}
