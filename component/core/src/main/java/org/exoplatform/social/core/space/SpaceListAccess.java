/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.social.core.space;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.common.ListAccessValidator;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.SpaceStorage;

/**
 * SpaceListAccess for LazyPageList usage
 * 
 * Created by The eXo Platform SAS
 * Author : hoatle
 *          hoatlevan@gmail.com
 * Aug 28, 2009  
 */
public class SpaceListAccess implements ListAccess<Space> {
  /** The space activityStorage. */
  private SpaceStorage spaceStorage;
  
  /** The user id. */
  private String userId;
  
  /** The space filter */
  private SpaceFilter spaceFilter;
  
  /** The type. */
  Type type;
  
  /**
   * The space list access Type Enum.
   */
  public enum Type {
    /** Gets the all spaces (for super user). */
    ALL,
    /** Gets the all spaces by filter. */
    ALL_FILTER,
    /** Gets the accessible spaces of the user. */
    ACCESSIBLE,
    /** Gets the accessible spaces of the user by filter. */
    ACCESSIBLE_FILTER,
    /** Gets the invited spaces of the user. */
    INVITED,
    /** Gets the invited spaces of the user by filter. */
    INVITED_FILTER,
    /** Gets the pending spaces of the user. */
    PENDING,
    /** Gets the pending spaces of the user by filter. */
    PENDING_FILTER,
    /** Gets the public spaces of the user. */
    PUBLIC,
    /** Gets the public spaces of the user by filter. */
    PUBLIC_FILTER,
    /** Gets the public spaces of the super user. */
    PUBLIC_SUPER_USER,
    /** Gets the spaces which the user has setting permission. */
    SETTING,
    /** Gets the spaces which the user has setting permission by filter. */
    SETTING_FILTER,
    /** Gets the spaces which the user has the "member" role. */
    MEMBER,
    /** Gets the spaces which the user has the "member" role by filter. */
    MEMBER_FILTER,
    /** Gets the spaces which are visible and not include these spaces hidden */
    VISIBLE
  }
  
  /**
   * The constructor.
   * 
   * @since 1.2.0-GA
   */
  public SpaceListAccess() {
    this.spaceStorage = null;
    this.userId = null;
    this.spaceFilter = null;
    this.type = null;
  }
  
  /**
   * The constructor.
   * 
   * @param spaceStorage
   * @param spaceFilter
   * @param type
   * @since 1.2.0-GA
   */
  public SpaceListAccess(SpaceStorage spaceStorage, String userId, SpaceFilter spaceFilter, Type type) {
    this.spaceStorage = spaceStorage;
    this.userId = userId;
    this.spaceFilter = spaceFilter;
    this.type = type;
  }
  
  /**
   * The constructor.
   * 
   * @param spaceStorage
   * @param type
   * @since 1.2.0-GA
   */
  public SpaceListAccess(SpaceStorage spaceStorage, Type type) {
    this.spaceStorage = spaceStorage;
    this.type = type;
  }
  
  /**
   * The constructor.
   * 
   * @param spaceStorage
   * @param userId
   * @param type
   * @since 1.2.0-GA
   */
  public SpaceListAccess(SpaceStorage spaceStorage, String userId, Type type) {
    this.spaceStorage = spaceStorage;
    this.userId = userId;
    this.type = type;
  }
  
  /**
   * The constructor.
   * 
   * @param spaceStorage
   * @param spaceFilter
   * @param type
   * @since 1.2.0-GA
   */
  public SpaceListAccess(SpaceStorage spaceStorage, SpaceFilter spaceFilter, Type type) {
    this.spaceStorage = spaceStorage;
    this.spaceFilter = spaceFilter;
    this.type = type;
  }
  
  /**
   * {@inheritDoc}
   */
  public int getSize() throws Exception {
    switch (type) {
      case ALL: return spaceStorage.getAllSpacesCount();
      case ALL_FILTER: return spaceStorage.getAllSpacesByFilterCount(this.spaceFilter);
      case ACCESSIBLE: return spaceStorage.getAccessibleSpacesCount(this.userId);
      case ACCESSIBLE_FILTER: return spaceStorage.getAccessibleSpacesByFilterCount(this.userId, this.spaceFilter);
      case INVITED: return spaceStorage.getInvitedSpacesCount(userId);
      case INVITED_FILTER: return spaceStorage.getInvitedSpacesByFilterCount(userId, spaceFilter);
      case PENDING: return spaceStorage.getPendingSpacesCount(this.userId);
      case PENDING_FILTER: return spaceStorage.getPendingSpacesByFilterCount(this.userId, this.spaceFilter);
      case PUBLIC: return spaceStorage.getPublicSpacesCount(this.userId);
      case PUBLIC_FILTER: return spaceStorage.getPublicSpacesByFilterCount(this.userId, this.spaceFilter);
      case PUBLIC_SUPER_USER: return 0;
      case SETTING: return spaceStorage.getEditableSpacesCount(this.userId);
      case SETTING_FILTER: return spaceStorage.getEditableSpacesByFilterCount(this.userId, this.spaceFilter);
      case MEMBER: return spaceStorage.getMemberSpacesCount(this.userId);
      case MEMBER_FILTER: return spaceStorage.getMemberSpacesByFilterCount(this.userId, this.spaceFilter);
      case VISIBLE: return spaceStorage.getVisibleSpacesCount(this.userId, this.spaceFilter);
      default: return 0;
    }
  }

  /**
   * {@inheritDoc}
   */
  public Space[] load(int offset, int limit) throws Exception, IllegalArgumentException {
    ListAccessValidator.validateIndex(offset, limit, this.getSize());
    List<Space> listSpaces = null;
    switch (type) {
      case ALL: listSpaces = spaceStorage.getSpaces(offset, limit);
        break;
      case ALL_FILTER: listSpaces = spaceStorage.getSpacesByFilter(this.spaceFilter, offset, limit);
        break;
      case ACCESSIBLE: listSpaces = spaceStorage.getAccessibleSpaces(this.userId, offset, limit);
        break;
      case ACCESSIBLE_FILTER: listSpaces = spaceStorage.getAccessibleSpacesByFilter(this.userId, this.spaceFilter, offset, limit);
        break;
      case INVITED: listSpaces = spaceStorage.getInvitedSpaces(this.userId, offset, limit);
        break;
      case INVITED_FILTER: listSpaces = spaceStorage.getInvitedSpacesByFilter(this.userId, this.spaceFilter, offset, limit);
        break;
      case PENDING: listSpaces = spaceStorage.getPendingSpaces(this.userId, offset, limit);
        break;
      case PENDING_FILTER: listSpaces = spaceStorage.getPendingSpacesByFilter(this.userId, this.spaceFilter, offset, limit);
        break;
      case PUBLIC: listSpaces = spaceStorage.getPublicSpaces(this.userId, offset, limit);
        break;
      case PUBLIC_FILTER: listSpaces = spaceStorage.getPublicSpacesByFilter(this.userId, this.spaceFilter, offset, limit);
        break;
      case PUBLIC_SUPER_USER: listSpaces = new ArrayList<Space> ();
        break;
      case SETTING: listSpaces = spaceStorage.getEditableSpaces(this.userId, offset, limit);
        break;
      case SETTING_FILTER: listSpaces = spaceStorage.getEditableSpacesByFilter(this.userId, this.spaceFilter, offset, limit);
        break;
      case MEMBER: listSpaces = spaceStorage.getMemberSpaces(this.userId, offset, limit);
        break;
      case MEMBER_FILTER: listSpaces = spaceStorage.getMemberSpacesByFilter(this.userId, this.spaceFilter, offset, limit);
        break;
      case VISIBLE: listSpaces = spaceStorage.getVisibleSpaces(this.userId, this.spaceFilter, offset, limit);
        break;
    }
    return listSpaces.toArray(new Space[listSpaces.size()]);
  }
  
  /**
   * Gets the type.
   * 
   * @return
   * @since 1.2.0-GA
   */
  public Type gettype() {
    return type;
  }

  /**
   * Sets the type.
   * 
   * @param type
   * @since 1.2.0-GA
   */
  public void settype(Type type) {
    this.type = type;
  }
  
  /**
   * Gets the user id.
   * 
   * @return
   * @since 1.2.0-GA
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Sets the user id.
   * 
   * @param userId
   * @since 1.2.0-GA
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }
}
