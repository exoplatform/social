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

package org.exoplatform.social.core.jpa.storage.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.*;

import org.exoplatform.commons.api.persistence.ExoEntity;

@Entity(name = "SocGroupSpaceBindingReportUser")
@ExoEntity
@Table(name = "SOC_GROUP_SPACE_BINDING_REPORT_USER")
@NamedQueries({
     @NamedQuery(name = "SocGroupSpaceBindingReportUser.findReportForCSV", query
     = "SELECT groupSpaceBindingReportUser FROM SocGroupSpaceBindingReportUser groupSpaceBindingReportUser"
     + " WHERE groupSpaceBindingReportUser.groupSpaceBindingReportAction.groupSpaceBindingId = :groupSpaceBindingId"
     + " AND groupSpaceBindingReportUser.groupSpaceBindingReportAction.space.id = :spaceId"
     + " AND groupSpaceBindingReportUser.groupSpaceBindingReportAction.group = :group"
     + " AND groupSpaceBindingReportUser.groupSpaceBindingReportAction.action = :action"
     + " ORDER BY groupSpaceBindingReportUser.date ASC"),
    @NamedQuery(name = "SocGroupSpaceBindingReportUser.findBindingReportUsersByBindingReportAction", query = "SELECT userReport "
        + " FROM SocGroupSpaceBindingReportUser userReport WHERE userReport.groupSpaceBindingReportAction.id = :bindingReportActionId"
        + " AND userReport.stillInSpace = false") })
public class GroupSpaceBindingReportUserEntity implements Serializable {
  @Id
  @SequenceGenerator(name = "SEQ_SOC_GROUP_SPACE_BINDING_REPORT_USER_ID", sequenceName = "SEQ_SOC_GROUP_SPACE_BINDING_REPORT_USER_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_GROUP_SPACE_BINDING_REPORT_USER_ID")
  @Column(name = "GROUP_SPACE_BINDING_REPORT_USER_ID")
  private long    id;

  @ManyToOne
  @JoinColumn(name = "GROUP_SPACE_BINDING_REPORT_ACTION_ID", referencedColumnName = "GROUP_SPACE_BINDING_REPORT_ACTION_ID", nullable = false)
  private GroupSpaceBindingReportActionEntity groupSpaceBindingReportAction;

  @Column(name = "USERNAME")
  private String  user;

  @Column(name = "ACTION")
  private String  action;

  @Column(name = "WAS_PRESENT_BEFORE")
  private boolean wasPresentBefore;

  @Column(name = "STILL_IN_SPACE")
  private boolean stillInSpace;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "DATE")
  private Date    date;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public GroupSpaceBindingReportActionEntity getGroupSpaceBindingReportAction() {
    return groupSpaceBindingReportAction;
  }

  public void setGroupSpaceBindingReportAction(GroupSpaceBindingReportActionEntity groupSpaceBindingReportAction) {
    this.groupSpaceBindingReportAction = groupSpaceBindingReportAction;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public boolean isWasPresentBefore() {
    return wasPresentBefore;
  }

  public void setWasPresentBefore(boolean wasPresentBefore) {
    this.wasPresentBefore = wasPresentBefore;
  }

  public boolean isStillInSpace() {
    return stillInSpace;
  }

  public void setStillInSpace(boolean stillInSpace) {
    this.stillInSpace = stillInSpace;
  }
}
