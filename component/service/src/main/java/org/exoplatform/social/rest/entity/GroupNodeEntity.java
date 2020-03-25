package org.exoplatform.social.rest.entity;

import java.util.List;

public class GroupNodeEntity extends BaseEntity {
  private static final long serialVersionUID = 2513730351488599651L;

  public GroupNodeEntity() {
  }

  public GroupNodeEntity(String id) {
    super(id);
  }

  public GroupNodeEntity setGroupName(String groupName) {
    setProperty("name", groupName);
    return this;
  }

  public String getGroupName() {
    return getString("name");
  }

  public GroupNodeEntity setParentId(String parentId) {
    setProperty("parentId", parentId);
    return this;
  }

  public String getParentId() {
    return getString("parentId");
  }

  public GroupNodeEntity setChildGroupNodesEntities(List<DataEntity> childrenEntities) {
    setProperty("children", childrenEntities);
    return this;
  }

  public String getChildGroupNodes() {
    return getString("children");
  }

}
