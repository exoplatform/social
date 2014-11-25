/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.identity.model;

import org.exoplatform.social.core.storage.cache.model.key.ActiveIdentityKey;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Oct 8, 2014  
 */
public class ActiveIdentityFilter {
  
  public final static String COMMA_SEPARATOR = ",";
  
  /** */
  private final int days;
  /** */
  private final String userGroups;

  public ActiveIdentityFilter(final int days) {
    this.days = days;
    this.userGroups = null;
  }
  
  public ActiveIdentityFilter(final String userGroups) {
    this.days = 0;
    this.userGroups = userGroups;
  }
  /**
   * Provides multiple user groups separates by comma
   * For example: /platform/users,/platform/administrators...etc
   * 
   * @param days
   * @param userGroups
   */
  public ActiveIdentityFilter(final int days, final String userGroups) {
    this.days = days;
    this.userGroups = userGroups;
  }

  public int getDays() {
    return days;
  }
  
  /**
   * Gets multiple user groups separates by comma
   * For example: /platform/users,/platform/administrators...etc
   * @return
   */
  public String getUserGroups() {
    return userGroups;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ActiveIdentityKey)) {
      return false;
    }

    ActiveIdentityFilter that = (ActiveIdentityFilter) o;

    if (days != that.days) {
      return false;
    }
    
    if (userGroups != null ? !userGroups.equals(that.userGroups) : that.userGroups != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + days;
    result = 31 * result + (userGroups != null ? userGroups.hashCode() : 0);
    return result;
  }
}
