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

import javax.persistence.*;

import org.exoplatform.commons.api.persistence.ExoEntity;

@Entity(name = "SocGroupSpaceBindingQueue")
@ExoEntity
@Table(name = "SOC_GROUP_SPACE_BINDING_QUEUE")
@NamedQueries({
    @NamedQuery(name = "SocGroupSpaceBindingQueue.findFirstGroupSpaceBindingQueue", query = "SELECT q FROM SocGroupSpaceBindingQueue q "
        + " ORDER BY q.id ASC"),
    @NamedQuery(name = "SocGroupSpaceBindingQueue.getGroupSpaceBindingsFromQueueByAction", query = "SELECT q.groupSpaceBindingEntity FROM SocGroupSpaceBindingQueue q "
        + " where q.action = :action"),
    @NamedQuery(name = "SocGroupSpaceBindingQueue.getAllFromBindingQueueOrderedById", query = "SELECT q FROM SocGroupSpaceBindingQueue q "
        + " ORDER BY q.id DESC "), })
public class GroupSpaceBindingQueueEntity implements Serializable {

  @Id
  @SequenceGenerator(name = "SEQ_SOC_GROUP_SPACE_BINDING_QUEUE_ID", sequenceName = "SEQ_SOC_GROUP_SPACE_BINDING_QUEUE_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_GROUP_SPACE_BINDING_QUEUE_ID")
  @Column(name = "GROUP_SPACE_BINDING_QUEUE_ID")
  private long                    id;

  @ManyToOne
  @JoinColumn(name = "GROUP_SPACE_BINDING_ID", referencedColumnName = "GROUP_SPACE_BINDING_ID", nullable = false)
  private GroupSpaceBindingEntity groupSpaceBindingEntity;

  @Column(name = "ACTION")
  private String                  action;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public GroupSpaceBindingEntity getGroupSpaceBindingEntity() {
    return groupSpaceBindingEntity;
  }

  public void setGroupSpaceBindingEntity(GroupSpaceBindingEntity groupSpaceBindingEntity) {
    this.groupSpaceBindingEntity = groupSpaceBindingEntity;
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }
}
