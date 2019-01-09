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

@Entity(name = "SocUserSpaceBinding")
@ExoEntity
@Table(name = "SOC_USER_SPACE_BINDING")
public class UserSpaceBindingEntity implements Serializable {

  private static final long       serialVersionUID = -3088537806368295223L;

  @Id
  @SequenceGenerator(name = "SEQ_SOC_USER_SPACE_BINDING_ID", sequenceName = "SEQ_SOC_USER_SPACE_BINDING_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_USER_SPACE_BINDING_ID")
  @Column(name = "USER_SPACE_BINDING_ID")
  private long                    id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "SPACE_ID", referencedColumnName = "SPACE_ID")
  private SpaceEntity             space;

  @Column(name = "USERNAME")
  private String                  user;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "GROUP_SPACE_BINDING_ID", referencedColumnName = "GROUP_SPACE_BINDING_ID")
  private GroupSpaceBindingEntity groupSpaceBinding;

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

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public GroupSpaceBindingEntity getGroupSpaceBinding() {
    return groupSpaceBinding;
  }

  public void setGroupSpaceBinding(GroupSpaceBindingEntity groupSpaceBinding) {
    this.groupSpaceBinding = groupSpaceBinding;
  }
}
