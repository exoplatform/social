package org.exoplatform.social.rest.entity;

public class GroupSpaceBindingOperationReportEntity extends BaseEntity {

  private static final long serialVersionUID = -8049916455960643317L;

  public GroupSpaceBindingOperationReportEntity() {
  }

  public GroupSpaceBindingOperationReportEntity setSpace(DataEntity spaceEntity) {
    setProperty("space", spaceEntity);
    return this;
  }

  public String getSpace() {
    return getString("space");
  }

  public GroupSpaceBindingOperationReportEntity setGroup(DataEntity group) {
    setProperty("group", group);
    return this;
  }

  public String getGroup() {
    return getString("group");
  }

  public GroupSpaceBindingOperationReportEntity setOperationType(String operationType) {
    setProperty("operationType", operationType);
    return this;
  }

  public String getOperationType() {
    return getString("operationType");
  }

  public GroupSpaceBindingOperationReportEntity setBindingId(String bindingId) {
    setProperty("bindingId", bindingId);
    return this;
  }

  public String getBindingId() {
    return getString("bindingId");
  }

  public GroupSpaceBindingOperationReportEntity setAddedUsersCount(String addedUsers) {
    setProperty("addedUsers", addedUsers);
    return this;
  }

  public String getAddedUsersCount() {
    return getString("addedUsers");
  }

  public GroupSpaceBindingOperationReportEntity setRemovedUsersCount(String removedUsers) {
    setProperty("removedUsers", removedUsers);
    return this;
  }

  public String getRemovedUsersCount() {
    return getString("removedUsers");
  }

  public GroupSpaceBindingOperationReportEntity setStartDate(String startDate) {
    setProperty("startDate", startDate);
    return this;
  }

  public String getStartDate() {
    return getString("startDate");
  }

  public GroupSpaceBindingOperationReportEntity setEndDate(String endDate) {
    setProperty("endDate", endDate);
    return this;
  }

  public String getEndDate() {
    return getString("endDate");
  }
}
