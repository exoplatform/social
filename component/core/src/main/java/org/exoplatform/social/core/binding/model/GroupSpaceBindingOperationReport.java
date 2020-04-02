package org.exoplatform.social.core.binding.model;

import java.util.Date;
import java.util.Objects;

/**
 * Group Binding Operation Report Model (report operation of binding between
 * space ang organization group)
 */

public class GroupSpaceBindingOperationReport {
  /** The space id */
  private long   spaceId;

  /** The group id. */
  private String group;

  /** The action */
  private String action;

  /** The groupSpaceBinding id */
  private long   groupSpaceBindingId;

  /** The number of users added by the operation */
  private long   addedUsers;

  /** The number of users removed by the operation */
  private long   removedUsers;

  /** The start date */
  private Date   startDate;

  /** The end date */
  private Date   endDate;

  public GroupSpaceBindingOperationReport() {
  }

  public GroupSpaceBindingOperationReport(long spaceId,
                                          String group,
                                          String action,
                                          long groupSpaceBindingId,
                                          long addedUsers,
                                          long removedUsers,
                                          Date startDate,
                                          Date endDate) {
    this.spaceId = spaceId;
    this.group = group;
    this.action = action;
    this.groupSpaceBindingId = groupSpaceBindingId;
    this.addedUsers = addedUsers;
    this.removedUsers = removedUsers;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public long getSpaceId() {
    return spaceId;
  }

  public void setSpaceId(long spaceId) {
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

  public long getGroupSpaceBindingId() {
    return groupSpaceBindingId;
  }

  public void setGroupSpaceBindingId(long groupSpaceBindingId) {
    this.groupSpaceBindingId = groupSpaceBindingId;
  }

  public long getAddedUsers() {
    return addedUsers;
  }

  public void setAddedUsers(long addedUsers) {
    this.addedUsers = addedUsers;
  }

  public long getRemovedUsers() {
    return removedUsers;
  }

  public void setRemovedUsers(long removedUsers) {
    this.removedUsers = removedUsers;
  }

  public Date getStartDate() {
    return startDate;
  }

  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  public Date getEndDate() {
    return endDate;
  }

  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof GroupSpaceBindingOperationReport))
      return false;
    GroupSpaceBindingOperationReport that = (GroupSpaceBindingOperationReport) o;
    return groupSpaceBindingId == that.groupSpaceBindingId && action.equals(that.action);
  }

  @Override
  public int hashCode() {
    return Objects.hash(action, groupSpaceBindingId);
  }
}
