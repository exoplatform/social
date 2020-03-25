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

import java.util.List;

/**
 * Group Node Model (for Organization group)
 */

public class GroupNode {

  /** The group id */
  private String          id;

  /** The group name (the organization group's label) */
  private String          groupName;

  /** The group parent id */
  private String          parentId;

  private List<GroupNode> childGroupNodes;

  public GroupNode() {
  }

  public GroupNode(String id, String groupName, String parentId) {
    this.id = id;
    this.groupName = groupName;
    this.parentId = parentId;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getGroupName() {
    return groupName;
  }

  public void setGroupName(String groupName) {
    this.groupName = groupName;
  }

  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public List<GroupNode> getChildGroupNodes() {
    return childGroupNodes;
  }

  public void setChildGroupNodes(List<GroupNode> childGroupNodes) {
    this.childGroupNodes = childGroupNodes;
  }
}
