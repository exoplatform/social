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

public class Property {
  protected String id;
  protected String name;
  protected boolean isSymetric;
  protected Relationship.Type status;
  protected Identity initiator;

  public Property() {
    this(null, false, Relationship.Type.PENDING, null);
  }

  public Property(String name) {
    this(name, false, Relationship.Type.PENDING, null);
  }

  public Property(String name, boolean isSymetric) {
    this(name, isSymetric, Relationship.Type.PENDING, null);
  }

  public Property(String name, boolean isSymetric, Relationship.Type status) {
    this(name, isSymetric, status, null);
  }

  public Property(String name, boolean isSymetric, Relationship.Type status, Identity initiator) {
    this.name = name;
    this.isSymetric = isSymetric;
    this.status = status;
    this.initiator = initiator;
  }

  public boolean isSymetric() {
    return isSymetric;
  }

  public void setSymetric(boolean symetric) throws Exception {
    isSymetric = symetric;
  }

  /**
   * if the relation is not symetric, it always return Relationship.Type.CONFIRM
   *
   * @return
   */
  public Relationship.Type getStatus() {
    if (!isSymetric)
      return Relationship.Type.CONFIRM;
    return status;
  }

  public void setStatus(Relationship.Type status) {
    this.status = status;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Identity getInitiator() {
    return initiator;
  }

  public void setInitiator(Identity initiator) {
    this.initiator = initiator;
  }
}
