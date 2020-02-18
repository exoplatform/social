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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.exoplatform.commons.api.persistence.ExoEntity;

@Entity(name = "SocGroupSpaceBindingQueue")
@ExoEntity
@Table(name = "SOC_GROUP_SPACE_BINDING_QUEUE")
@NamedQueries({
    @NamedQuery(name = "SocGroupSpaceBindingQueue.findFirstGroupSpaceBindingQueue", query = "SELECT q FROM SocGroupSpaceBindingQueue q "
        + " ORDER BY q.id ASC")
    })
public class GroupSpaceBindingQueueEntity implements Serializable {
  
  @Id
  @SequenceGenerator(name = "SEQ_SOC_GROUP_SPACE_BINDING_QUEUE_ID", sequenceName = "SEQ_SOC_GROUP_SPACE_BINDING_QUEUE_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_GROUP_SPACE_BINDING_QUEUE_ID")
  @Column(name = "GROUP_SPACE_BINDING_QUEUE_ID")
  private long                        id;

  @ManyToOne
  @JoinColumn(name = "SPACE_ID", referencedColumnName = "SPACE_ID", nullable = false)
  private SpaceEntity                 space;

  @Column(name = "GROUP_NAME")
  private String                      group;
  
  @Column(name = "ACTION")
  private String                      action;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
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
}
