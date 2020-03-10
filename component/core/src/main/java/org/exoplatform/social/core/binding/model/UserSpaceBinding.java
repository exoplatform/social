/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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

package org.exoplatform.social.core.binding.model;

/**
 * User Binding Model (Member of space bind by the Space Binding Feature)
 */

public class UserSpaceBinding {
  /** The id. */
  private long              id;

  /** The user */
  private String            user;

  /** Is the user a member of the space before the binding. */
  private Boolean           isMemberBefore;

  /** The group binding */
  private GroupSpaceBinding groupBinding;

  public UserSpaceBinding() {
  }

  public UserSpaceBinding(String user, GroupSpaceBinding groupBinding) {
    this.user = user;
    this.groupBinding = groupBinding;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public Boolean isMemberBefore() {
    return isMemberBefore;
  }

  public void setIsMemberBefore(Boolean memberBefore) {
    isMemberBefore = memberBefore;
  }

  public GroupSpaceBinding getGroupBinding() {
    return groupBinding;
  }

  public void setGroupBinding(GroupSpaceBinding groupBinding) {
    this.groupBinding = groupBinding;
  }
}
