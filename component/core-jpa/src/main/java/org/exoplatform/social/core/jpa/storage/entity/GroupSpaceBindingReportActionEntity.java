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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import org.exoplatform.commons.api.persistence.ExoEntity;

@Entity(name = "SocGroupSpaceBindingReportAction")
@ExoEntity
@Table(name = "SOC_GROUP_SPACE_BINDING_REPORT_ACTION")
@NamedQueries({
    @NamedQuery(name = "SocGroupSpaceBindingReportAction.findGroupSpaceBindingReportAction", query = "SELECT report FROM SocGroupSpaceBindingReportAction report "
        + " WHERE report.groupSpaceBindingId = :bindingId AND report.action = :action "),
    @NamedQuery(name = "SocGroupSpaceBindingReportAction.getGroupSpaceBindingReportActions", query = "SELECT report FROM SocGroupSpaceBindingReportAction report "
        + " ORDER BY report.endDate DESC NULLS FIRST") })
public class GroupSpaceBindingReportActionEntity implements Serializable {
  @Id
  @SequenceGenerator(name = "SEQ_SOC_GROUP_SPACE_BINDING_REPORT_ACTION_ID", sequenceName = "SEQ_SOC_GROUP_SPACE_BINDING_REPORT_ACTION_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_GROUP_SPACE_BINDING_REPORT_ACTION_ID")
  @Column(name = "GROUP_SPACE_BINDING_REPORT_ACTION_ID")
  private long                                    id;

  @Column(name = "GROUP_SPACE_BINDING_ID")
  private long                                    groupSpaceBindingId;

  @ManyToOne
  @JoinColumn(name = "SPACE_ID", referencedColumnName = "SPACE_ID", nullable = false)
  private SpaceEntity                             space;

  @Column(name = "GROUP_ID")
  private String                                  group;

  @OneToMany(mappedBy = "groupSpaceBindingReportAction", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<GroupSpaceBindingReportUserEntity> bindingReportUserEntities = new ArrayList<>();

  @Column(name = "ACTION")
  private String                                  action;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "START_DATE")
  private Date                                    startDate                 = new Date();

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "END_DATE")
  private Date                                    endDate;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getGroupSpaceBindingId() {
    return groupSpaceBindingId;
  }

  public void setGroupSpaceBindingId(long groupSpaceBindingId) {
    this.groupSpaceBindingId = groupSpaceBindingId;
  }

  public SpaceEntity getSpace() {
    return space;
  }

  public void setSpace(SpaceEntity space) {
    this.space = space;
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
}
