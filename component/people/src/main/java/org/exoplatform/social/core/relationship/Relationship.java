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

import java.util.List;
import java.util.ArrayList;

public class Relationship {
  private String id;
  private List<Property> properties;
  private Identity identity1;
  private Identity identity2;
  private Type status = Type.PENDING;


  public enum Type {
    PENDING,
    CONFIRM,
    IGNORE;
  }

  public Relationship(Identity identity1, Identity identity2) {
    this(identity1, identity2, new ArrayList<Property>());
  }

  public Relationship(Identity identity1, Identity identity2, List<Property> properties) {
    this.identity1 = identity1;
    this.identity2 = identity2;
    this.properties = properties;
  }

  public Relationship(String uuid) {
    this.id = uuid;
    this.properties = new ArrayList<Property>();
  }

  public Identity getIdentity1() {
    return identity1;
  }

  public Identity getIdentity2() {
    return identity2;
  }

  public void setIdentity1(Identity identity1) {
    this.identity1 = identity1;
  }

  public void setIdentity2(Identity identity2) {
    this.identity2 = identity2;
  }

  public List<Property> getProperties() {
    return properties;
  }

  public void setProperties(List<Property> properties) {

    this.properties = properties;
  }

  public void addProperty(Property property) {
    properties.add(property);
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Type getStatus() {
    return status;
  }

  public void setStatus(Type status) {
    this.status = status;
  }

  public List<Property> getProperties(Type status){
    List<Property> pendingProps = new ArrayList<Property>();
    for(Property prop:properties) {
      if (prop.getStatus() == status)
        pendingProps.add(prop);
    }
    return pendingProps;
  }
}
