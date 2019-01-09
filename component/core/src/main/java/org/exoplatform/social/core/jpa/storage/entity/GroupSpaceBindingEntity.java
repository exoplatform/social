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

@Entity(name = "SocGroupSpaceBinding")
@ExoEntity
@Table(name = "SOC_GROUP_SPACE_BINDING")
@NamedQueries({ @NamedQuery(name = "SocGroupSpaceBinding.findSpaceBindings", query = "SELECT groupSpacebinding "
    + " FROM SocGroupSpaceBinding groupSpacebinding "
    + " WHERE groupSpacebinding.space.id = :spaceId and groupSpacebinding.spaceRole = :role") })
public class GroupSpaceBindingEntity implements Serializable {

  private static final long serialVersionUID = -1901782610164740670L;

  @Id
  @SequenceGenerator(name = "SEQ_SOC_GROUP_SPACE_BINDING_ID", sequenceName = "SEQ_SOC_GROUP_SPACE_BINDING_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_GROUP_SPACE_BINDING_ID")
  @Column(name = "GROUP_SPACE_BINDING_ID")
  private long              id;

  @ManyToOne
  @JoinColumn(name = "SPACE_ID", referencedColumnName = "SPACE_ID")
  private SpaceEntity       space;

  @Column(name = "GROUP_NAME")
  private String            group;

  @Column(name = "SPACE_ROLE")
  private String            spaceRole;

  @Column(name = "GROUP_ROLE")
  private String            groupRole;

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

  public String getSpaceRole() {
    return spaceRole;
  }

  public void setSpaceRole(String spaceRole) {
    this.spaceRole = spaceRole;
  }

  public String getGroupRole() {
    return groupRole;
  }

  public void setGroupRole(String groupRole) {
    this.groupRole = groupRole;
  }
}
