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
  private String[] pendingUsers;
  private String[] invitedUsers;
  private String type;
  private String url;
  private String visibility;
  private String registration;

  public final static String ACTIVE_STATUS = "actived";
  public final static String DEACTIVE_STATUS = "deactived";
  public final static String INSTALL_STATUS = "install";
  public final static String PUBLIC = "public";
  public final static String PRIVATE = "private";
  public final static String HIDDEN = "hidden";
  public final static String OPEN = "open";
  public final static String VALIDATION = "validation";
  public final static String CLOSE = "close";
  
  public Space() {}
  
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

  public void setPendingUsers(String[] pendingUsers) {
    this.pendingUsers = pendingUsers;
  }

  public String[] getPendingUsers() {
    return pendingUsers;
  }

  public void setInvitedUsers(String[] invitedUsers) {
    this.invitedUsers = invitedUsers;
  }
  
  public String[] getInvitedUsers() {
    return invitedUsers;
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
  
  public String getUrl() {
    return url;
  }
  
  public void setUrl(String url) {
    this.url = url;
  }
  
  public String getVisibility() {
    return visibility;
  }
  
  public void setVisibility(String visibility) {
    this.visibility = visibility;
  }
  
  public String getRegistration() {
    return registration;
  }
  
  public void setRegistration(String registration) {
    this.registration = registration;
  }
}