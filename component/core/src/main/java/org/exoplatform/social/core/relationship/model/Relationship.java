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

import org.exoplatform.social.core.identity.model.Identity;

/**
 * Relationship between 2 social identities
 */
public class Relationship {

  /** The id. */
  private String   id;

  /** The sender. */
  private Identity sender;

  /** The receiver. */
  private Identity receiver;

  /** Is symetric. */
  private boolean  isSymetric;

  /** The status. */
  private Type     status;

  /**
   * The Relationship Type Enum
   */
  public enum Type {
    PENDING,  // A connection request is sent and waiting for the other's approval.
    CONFIRMED,  // A connection is setup
    ALL,
    IGNORED,  // Ignore user in the list of connection suggestion.
    INCOMING,
    OUTGOING
  }

  /**
   * Instantiates a new relationship.
   * 
   * @param sender the sender
   * @param receiver the receiver
   */
  public Relationship(Identity sender, Identity receiver) {
    this.sender = sender;
    this.receiver = receiver;
    this.status = Type.PENDING;
  }

  /**
   * Instantiates a new relationship.
   * 
   * @param sender the sender
   * @param receiver the receiver
   * @param status the status
   */
  public Relationship(Identity sender, Identity receiver, Type status) {
    this.sender = sender;
    this.receiver = receiver;
    this.status = status;
  }

  /**
   * Instantiates a new relationship.
   * 
   * @param uuid the uuid
   */
  public Relationship(String uuid) {
    this.id = uuid;
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
   * Gets the isSymetric.
   * 
   * @param isSymetric
   */
  public void setSymetric(boolean isSymetric) {
    this.isSymetric = isSymetric;
  }

  /**
   * Sets the isSymetric
   * 
   * @return isSymetric
   */
  public boolean isSymetric() {
    return isSymetric;
  }

  /**
   * Gets string sender + "--[" + status + "]--" + receiver
   * @return string
   */
  public String toString() {
    return sender + "--[" + status + "]--" + receiver;
  }

  /**
   * Gets the partner of relationship. Returns null if not found any identity in this relationship
   * 
   * @param identity
   * @return identity
   */
  public Identity getPartner(Identity identity) {
    if (identity.equals(sender))
      return receiver;
    if (identity.equals(receiver))
      return sender;
    return null;
  }

  /**
   * Checks the identity is the sender of relationship
   * 
   * @param identity
   * @return boolean
   */
  public boolean isSender(Identity identity) {
    if (identity.equals(sender))
      return true;
    return false;
  }

  /**
   * Checks the identity is the receiver of relationship
   * 
   * @param identity
   * @return boolean
   */
  public boolean isReceiver(Identity identity) {
    if (identity.equals(receiver))
      return true;
    return false;
  }

  /**
   * Compares to this object
   * 
   * return true if parameter object have the same id with this
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Relationship) {
      return getId().equals(((Relationship)obj).getId());
    }
    return super.equals(obj);
  }
  
  
  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
