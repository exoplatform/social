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
 * Group Binding Model (between space ang organization group)
 */

public class GroupSpaceBinding {
  /** The id */
  private long   id;

  /** The space id */
  private String spaceId;

  /** The group id. */
  private String group;

  public GroupSpaceBinding() {
  }

  public GroupSpaceBinding(String spaceId, String group) {
    this.spaceId = spaceId;
    this.group = group;
  }

  public GroupSpaceBinding(String id, String spaceId, String group) {
    this.id = Long.parseLong(id);
    this.spaceId = spaceId;
    this.group = group;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public void setSpaceId(String spaceId) {
    this.spaceId = spaceId;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }
}
