/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.social.service.rest;

import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;

/**
 * @since 1.2.2
 */
public class SpaceRest {
  
  /**
   * The name of the space. 
   */
  private String name;
  
  /**
   * The display name of the space. 
   */
  private String displayName;
  
  /**
   * The space url with new navigation controller.
   */
  private String spaceUrl;
  
  /**
   * The group id of the space.
   */
  private String groupId;
  
  /**
   * The url of the space.
   */
  private String url;
  
  /**
   * The url of the space's avatar.
   */
  private String avatarUrl;

  /**
   * The constructor.
   * 
   * @param space
   * 
   */
  public SpaceRest(Space space) {
    this.url = space.getUrl();
    this.groupId = space.getGroupId();
    this.name = space.getName();
    this.displayName = space.getDisplayName();
    
    this.avatarUrl = space.getAvatarUrl() == null ? LinkProvider.SPACE_DEFAULT_AVATAR_URL : space.getAvatarUrl();
  }
  
  /**
   * Gets the space url after building url with new navigation controller (used only for MySpaces gadget).
   * 
   * @return
   * 
   */
  public String getSpaceUrl() {
    return spaceUrl;
  }
  
  /**
   * Gets the space's avatar url
   * @return
   */
  public String getAvatarUrl() {
    return avatarUrl;
  }

  /**
   * Sets the space url with new navigation controller (used only for MySpaces gadget).
   * 
   * @param spaceUrl
   * 
   */
  public void setSpaceUrl(String spaceUrl) {
    this.spaceUrl = spaceUrl;
  }

  /**
   * Get the group id.
   * 
   * @return
   */
  public String getGroupId() {
    return groupId;
  }

  /**
   * Sets the group id.
   * 
   * @param groupId
   */
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * Gets the url.
   * 
   * @return
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets the url.
   * 
   * @param url
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Gets the name of the space.
   * 
   * @return
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of the space.
   * 
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the display name of the space.
   * 
   * @return
   */
  public String getDisplayName() {
    return displayName;
  }
  
  
}
