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
package org.exoplatform.social.core.storage.cache.model.key;

import org.exoplatform.social.core.identity.model.ActiveIdentityFilter;


/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Oct 8, 2014  
 */
public class ActiveIdentityKey extends ScopeCacheKey {

  private final int days;
  
  private final String userGroup;

  public ActiveIdentityKey(final int days) {
    this.days = days;
    this.userGroup = null;
  }
  
  public ActiveIdentityKey(final ActiveIdentityFilter filter) {
    this.days = filter.getDays();
    this.userGroup = filter.getUserGroups();
  }
  
  public ActiveIdentityKey(final String userGroup) {
    this.days = 0;
    this.userGroup = userGroup;
  }
  
  public ActiveIdentityKey(final int days, final String userGroup) {
    this.days = days;
    this.userGroup = userGroup;
  }

  public int getDays() {
    return days;
  }
  
  public String getUserGroup() {
    return userGroup;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ActiveIdentityKey)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    ActiveIdentityKey that = (ActiveIdentityKey) o;

    if (days != that.days) {
      return false;
    }
    
    if (userGroup != null ? !userGroup.equals(that.userGroup) : that.userGroup != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + days;
    result = 31 * result + (userGroup != null ? userGroup.hashCode() : 0);
    return result;
  }

}
