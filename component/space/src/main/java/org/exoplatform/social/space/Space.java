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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;


// TODO: Auto-generated Javadoc
/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * August 29, 2008
 */
public class Space {
  
  /** The id. */
  private String id;
  
  /** The name. */
  private String name;
  
  /** The group id. */
  private String groupId;
  
  /** The app. */
  private String app;
  
  /** The parent. */
  private String parent;
  
  /** The description. */
  private String description;
  
  /** The tag. */
  private String tag;
  
  /** The pending users. */
  private String[] pendingUsers;
  
  /** The invited users. */
  private String[] invitedUsers;
  
  /** The type. */
  private String type;
  
  /** The url. */
  private String url;
  
  /** The visibility. */
  private String visibility;
  
  /** The registration. */
  private String registration;
  
  /** The priority. */
  private String priority;
  
  /** The space attachment. */
  private SpaceAttachment spaceAttachment;

  /** The Constant ACTIVE_STATUS. */
  public final static String ACTIVE_STATUS = "active";
  
  /** The Constant DEACTIVE_STATUS. */
  public final static String DEACTIVE_STATUS = "deactive";
  
  /** The Constant INSTALL_STATUS. */
  public final static String INSTALL_STATUS = "installed";
  
  /** The Constant PUBLIC. */
  public final static String PUBLIC = "public";
  
  /** The Constant PRIVATE. */
  public final static String PRIVATE = "private";
  
  /** The Constant HIDDEN. */
  public final static String HIDDEN = "hidden";
  
  /** The Constant OPEN. */
  public final static String OPEN = "open";
  
  /** The Constant VALIDATION. */
  public final static String VALIDATION = "validation";
  
  /** The Constant CLOSE. */
  public final static String CLOSE = "close";
  
  /** The Constant HIGH_PRIORITY. */
  public final static String HIGH_PRIORITY = "1";
  
  /** The Constant INTERMEDIATE_PRIORITY. */
  public final static String INTERMEDIATE_PRIORITY = "2";
  
  /** The Constant LOW_PRIORITY. */
  public final static String LOW_PRIORITY = "3";
  
  /**
   * Instantiates a new space.
   */
  public Space() {}
  
  /**
   * Sets the id.
   * 
   * @param id the new id
   */
  public void setId(String id) {
    this.id = id;
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
   * Sets the name.
   * 
   * @param name the new name
   */
  public void setName(String name) {
    this.name = name;
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
   * Sets the group id.
   * 
   * @param groupId the new group id
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }
  
  /**
   * Gets the group id.
   * 
   * @return the group id
   */
  public String getGroupId() {
    return groupId;
  }
  
  /**
   * Sets the app.
   * 
   * @param app the new app
   */
  public void setApp(String app) {
    this.app = app;
  }
  
  /**
   * Gets the app.
   * 
   * @return the app
   */
  public String getApp() {
    return app;
  }
  
  /**
   * Sets the parent.
   * 
   * @param parent the new parent
   */
  public void setParent(String parent) {
    this.parent = parent;
  }
  
  /**
   * Gets the parent.
   * 
   * @return the parent
   */
  public String getParent() {
    return parent;
  }
  
  /**
   * Sets the description.
   * 
   * @param description the new description
   */
  public void setDescription(String description) {
    this.description = description;
  }
  
  /**
   * Gets the description.
   * 
   * @return the description
   */
  public String getDescription() {
    return description;
  }
  
  /**
   * Sets the tag.
   * 
   * @param tag the new tag
   */
  public void setTag(String tag) {
    this.tag = tag;
  }
  
  /**
   * Gets the tag.
   * 
   * @return the tag
   */
  public String getTag() {
    return tag;
  }

  /**
   * Sets the pending users.
   * 
   * @param pendingUsers the new pending users
   */
  public void setPendingUsers(String[] pendingUsers) {
    this.pendingUsers = pendingUsers;
  }

  /**
   * Gets the pending users.
   * 
   * @return the pending users
   */
  public String[] getPendingUsers() {
    return pendingUsers;
  }

  /**
   * Sets the invited users.
   * 
   * @param invitedUsers the new invited users
   */
  public void setInvitedUsers(String[] invitedUsers) {
    this.invitedUsers = invitedUsers;
  }
  
  /**
   * Gets the invited users.
   * 
   * @return the invited users
   */
  public String[] getInvitedUsers() {
    return invitedUsers;
  }
  
  /**
   * Sets the type.
   * 
   * @param type the new type
   */
  public void setType(String type) {
    this.type = type;
  }
  
  /**
   * Gets the type.
   * 
   * @return the type
   */
  public String getType() {
    return type;
  }
  
  /**
   * Gets the short name.
   * 
   * @return the short name
   */
  public String getShortName() {
    return groupId.substring(groupId.lastIndexOf("/")+1);
  }
  
  /**
   * Gets the url.
   * 
   * @return the url
   */
  public String getUrl() {
    return url;
  }
  
  /**
   * Sets the url.
   * 
   * @param url the new url
   */
  public void setUrl(String url) {
    this.url = url;
  }
  
  /**
   * Gets the visibility.
   * 
   * @return the visibility
   */
  public String getVisibility() {
    return visibility;
  }
  
  /**
   * Sets the visibility.
   * 
   * @param visibility the new visibility
   */
  public void setVisibility(String visibility) {
    this.visibility = visibility;
  }
  
  public String toString() {
    return name + " (" + groupId + ")";
  }
  
  /**
   * Gets the registration.
   * 
   * @return the registration
   */
  public String getRegistration() {
    return registration;
  }
  
  /**
   * Sets the registration.
   * 
   * @param registration the new registration
   */
  public void setRegistration(String registration) {
    this.registration = registration;
  }
  
  /**
   * Gets the priority.
   * 
   * @return the priority
   */
  public String getPriority() {
    return priority;
  }
  
  /**
   * Sets the priority.
   * 
   * @param priority the new priority
   */
  public void setPriority(String priority) {
    this.priority = priority;
  }

  /**
   * Sets the space attachment.
   * 
   * @param spaceAttachment the new space attachment
   */
  public void setSpaceAttachment(SpaceAttachment spaceAttachment) {
    this.spaceAttachment = spaceAttachment;
  }

  /**
   * Gets the space attachment.
   * 
   * @return the space attachment
   */
  public SpaceAttachment getSpaceAttachment() {
    return spaceAttachment;
  }
  
  /**
   * Gets space's image source url if available or null
   * @return
   * @throws Exception
   */
  public String getImageSource()  {
    try {
    SpaceAttachment spaceAttachment = getSpaceAttachment();
    if (spaceAttachment != null) {
      return "/" + PortalContainer.getCurrentRestContextName() + "/jcr/" + getRepository() + "/"
      + spaceAttachment.getWorkspace() + spaceAttachment.getDataPath() + "/?rnd="
      + System.currentTimeMillis();
    }
    } catch (Exception e) {
      ;
    }
    return null;
  }
  
  /**
   * Gets current repository name
   * 
   * @return repository name
   * @throws Exception
   */
  private String getRepository() throws Exception {
    PortalContainer portalContainer = PortalContainer.getInstance();
    RepositoryService rService = (RepositoryService) portalContainer.getComponentInstanceOfType(RepositoryService.class);
    return rService.getCurrentRepository().getConfiguration().getName();
  }
}