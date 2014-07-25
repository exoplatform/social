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
package org.exoplatform.social.core.space.model;

import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.space.SpaceUtils;

/**
 * Space Model
 *
 * @author <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 * @since Aug 29, 2008
 */
public class Space {
  /** The id. */
  private String id;

  /** The display name. */
  private String displayName;

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
  private AvatarAttachment avatarAttachment;

  /** Created time. */
  private long createdTime;

  /**
   * The pretty name of space.
   *
   * @since 1.2.0-GA
   */
  private String prettyName;

  /**
   * The url of avatar.
   *
   * @since 1.2.0-GA
   */
  private String avatarUrl;
  
  /**
   * The creator of space.
   * 
   * @since 1.2.0-GA
   * @deprecated Use {@link managers} instead.
   *             Will be removed by 1.2.8
   */
  @Deprecated
  private String creator;
  
  /**
   * The editor of space.
   * 
   * @since 4.0.0.Alpha1
   */
  private String editor;
  
  /** 
   * The managers of a space.
   * 
   * @since 1.2.0-GA
   */
  private String[] managers;
  
  /**
   * The last updated time of avatar ( in millisecond)
   * 
   * @since 1.2.1
   */
   private Long avatarLastUpdated;
  
  /**
   * The members of a space.
   * 
   * @since 1.2.0-GA
   */
  private String[] members;
  
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
  
  public static final String CREATOR = "space_creator";
  
  /** Types of updating of space. */
  public static enum                UpdatedField 
                                      {
                                        DESCRIPTION(true);
                                        
                                        private boolean type;
                                        
                                        private UpdatedField(boolean type) {
                                          this.type = type;
                                        }
                                        public boolean value() {
                                          return this.type;
                                        }
                                        
                                      };
                                      
  private UpdatedField field;

  public UpdatedField getField() {
    return field;
  }

  public void setField(UpdatedField field) {
    this.field = field;
  }

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
   * Sets the space display name. The space pretty name also set.
   *
   * @param spaceDisplayName the space Display Name
   */
  public void setDisplayName(String spaceDisplayName) {
    displayName = spaceDisplayName;
  }

  /**
   * Gets the name.
   *
   * @return the name
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * Gets space name id for used as space url, space identity remote id.
   *
   * @return
   * @deprecated use #getPrettyName() instead. To be removed at 1.3.x
   */
  public String getName() {
    return getPrettyName();
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
    return displayName + " (" + groupId + ")";
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
   * @param avatarAttachment the new space attachment
   */
  public void setAvatarAttachment(AvatarAttachment avatarAttachment) {
    this.avatarAttachment = avatarAttachment;
    this.setAvatarLastUpdated(avatarAttachment.getLastModified());
  }

  /**
   * Gets the space attachment.
   *
   * @return the space attachment
   */
  public AvatarAttachment getAvatarAttachment() {
    return avatarAttachment;
  }

  /**
   * Gets the pretty name of space.
   *
   * @return the name pretty of space
   * @since 1.2.0-GA
   */
  public String getPrettyName() {
    return prettyName;
  }

  /**
   * Sets the pretty name of space.
   *
   * @param prettyName
   * @since 1.2.0-GA
   */
  public void setPrettyName(String prettyName) {
    this.prettyName = SpaceUtils.cleanString(prettyName);
  }

  /**
   * Gets the url of avatar.
   *
   * @since 1.2.0-GA
   */
  public String getAvatarUrl() {
    return this.avatarUrl;
  }

  /** Sets the url of avatar.
   *
   * @param avatarUrl
   * @since 1.2.0-GA
   */
  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }
  
  /**
   * Gets the creator of a space.
   *  
   * @return
   * @since 1.2.0-GA
   * @deprecated Use {@link #getManagers()} instead.
   *             Will be removed by 1.2.8
   */
  @Deprecated
  public String getCreator() {
    return creator;
  }

  /**
   * Sets the creator of a space.
   *  
   * @return
   * @since 1.2.0-GA
   * @deprecated Use {@link #getManagers()} instead.
   *             Will be removed by 1.2.8
   */
  @Deprecated
  public void setCreator(String creator) {
    this.creator = creator;
  }
  
  /**
   * Gets the editor of a space.
   * 
   * @since 4.0.0.Alpha1
   * @return the editor of space
   */
  public String getEditor() {
    return editor;
  }

  /**
   * Sets the editor of a space.
   * 
   * @param editor the editor of space
   * @since 4.0.0.Alpha1
   */
  public void setEditor(String editor) {
    this.editor = editor;
  }

  /**
   * Gets the managers of a space.
   * 
   * @return
   * @since 1.2.0-GA
   */
  public String[] getManagers() {
    return managers;
  }
  
  /**
   * Sets the managers of a space.
   * 
   * @return
   * @since 1.2.0-GA
   */
  public void setManagers(String[] managers) {
    this.managers = managers;
  }

  /**
   * Gets the members of a space.
   * 
   * @return
   * @since 1.2.0-GA
   */
  public String[] getMembers() {
    return members;
  }

  /**
   * Sets the members of a space.
   * 
   * @return
   * @since 1.2.0-GA
   */
  public void setMembers(String[] members) {
    this.members = members;
  }
  
  /**
   * Gets the last updated time in milliseconds of avatar in a space
   * @return {@link Void}
   * @since 1.2.1
   */
  public Long getAvatarLastUpdated() {
    return avatarLastUpdated;
  }

  /**
   * Sets the last updated time in milliseconds of avatar in a space
   * @param avatarLastUpdatedTime
   * @since 1.2.1
   */
  public void setAvatarLastUpdated(Long avatarLastUpdatedTime) {
    this.avatarLastUpdated = avatarLastUpdatedTime;
  }

  public long getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(Long createdTime) {
    if (createdTime != null) {
      this.createdTime = createdTime;
    } else {
      this.createdTime = System.currentTimeMillis();
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj){
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Space other = (Space)obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }
}