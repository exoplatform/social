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

public class GroupSpaceBindingQueue {
  /** The id */
  private long   id;

  /** The space id */
  private String spaceId;

  /** The group id. */
  private String group;
  
  /** The action. */
  private String action;
  
  public static String ACTION_CREATE = "create";
  public static String ACTION_REMOVE = "remove";

  public GroupSpaceBindingQueue() {
  }

  public GroupSpaceBindingQueue(String id, String spaceId, String group, String action) {
    this.id = Long.parseLong(id);
    this.spaceId = spaceId;
    this.group = group;
    this.action=action;
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
  
  public String getAction() {
    return action;
  }
  
  public void setAction(String action) {
    this.action = action;
  }
}
