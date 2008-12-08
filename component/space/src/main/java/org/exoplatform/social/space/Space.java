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
package org.exoplatform.social.space;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * August 29, 2008          
 */
public class Space {
  private String id;
  private String name;
  private String groupId;
  private String app;
  private String parent;
  private String description;
  private String tag;
  private String pendingUser;
  private String invitedUser;
  private String type;
  
//  public final static String WEBOS = "webos";
//  public final static String CLASSIC = "classic";
  public final static String ACTIVE_STATUS = "actived";
  public final static String DEACTIVE_STATUS = "deactived";
  public final static String INSTALL_STATUS = "install";
  
  public Space() {
    id = "Space" + IdGenerator.generate();
  }
  
  public void setId(String id) {
    this.id = id;
  }
  
  public String getId() {
    return id;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getName() {
    return name;
  }
  
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }
  
  public String getGroupId() {
    return groupId;
  }
  
  public void setApp(String app) {
    this.app = app;
  }
  
  public String getApp() {
    return app;
  }
  
  public void setParent(String parent) {
    this.parent = parent;
  }
  
  public String getParent() {
    return parent;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public String getDescription() {
    return description;
  }
  
  public void setTag(String tag) {
    this.tag = tag;
  }
  
  public String getTag() {
    return tag;
  }

  public void setPendingUser(String pendingUser) {
    this.pendingUser = pendingUser;
  }

  //TODO: why only one pending user?
  public String getPendingUser() {
    return pendingUser;
  }

  public void setInvitedUser(String invitedUser) {
    this.invitedUser = invitedUser;
  }
  
  public String getInvitedUser() {
    return invitedUser;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public String getType() {
    return type;
  }
  
  public String getShortName() {
    return groupId.substring(groupId.lastIndexOf("/")+1);
  }
}