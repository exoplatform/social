/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.exoplatform.commons.api.persistence.ExoEntity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;

@Entity(name = "SocSpaceMember")
@ExoEntity
@Table(name = "SOC_SPACES_MEMBERS")
@NamedQueries({
        @NamedQuery(name = "SpaceMember.deleteBySpace", query = "DELETE FROM SocSpaceMember mem WHERE mem.space.id = :spaceId"),
        @NamedQuery(name = "SpaceMember.getSpaceIdentitiesIdByMemberId",
                query = "SELECT DISTINCT identity.id FROM SocIdentityEntity AS identity WHERE "
                        + " identity.providerId = '" + SpaceIdentityProvider.NAME + "' AND "
                        + " identity.remoteId IN "
                        + "   (SELECT DISTINCT spaceMember.space.prettyName FROM SocSpaceMember AS spaceMember where "
                        + "     spaceMember.userId = :userId AND "
                        + "     spaceMember.status = :status "
                        + "   ) "),
        @NamedQuery(name = "SpaceMember.getSpaceMembersByStatus",
                query = "SELECT spaceMember.userId FROM SocSpaceMember AS spaceMember "
                        + " WHERE spaceMember.status = :status "
                        + " AND   spaceMember.space.id = :spaceId "),
        @NamedQuery(name = "SpaceMember.countSpaceMembersByStatus",
                query = "SELECT count(*) FROM SocSpaceMember AS spaceMember "
                        + " WHERE spaceMember.status = :status "
                        + " AND   spaceMember.space.id = :spaceId "),
        @NamedQuery(name = "SpaceMember.getMember", query = "SELECT mem FROM SocSpaceMember mem WHERE mem.userId = :userId AND mem.space.id = :spaceId AND mem.status = :status"),
        @NamedQuery(name = "SpaceMember.deleteByUsername", query = "DELETE FROM SocSpaceMember sm WHERE sm.userId = :username"),
        @NamedQuery(name = "SpaceMember.getSpaceMemberShip", query = "SELECT mem FROM SocSpaceMember mem WHERE mem.userId = :userId AND mem.space.id = :spaceId")})
public class SpaceMemberEntity implements Serializable {

  private static final long serialVersionUID = 1015703779692801839L;

  @Id
  @SequenceGenerator(name = "SEQ_SOC_SPACE_MEMBER_ID", sequenceName = "SEQ_SOC_SPACE_MEMBER_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_SPACE_MEMBER_ID")
  @Column(name = "SPACE_MEMBER_ID")
  private Long              id;

  @ManyToOne
  @JoinColumn(name = "SPACE_ID", nullable = false)
  private SpaceEntity       space;

  @Column(name = "USER_ID", nullable = false)
  private String            userId;

  @Column(name = "STATUS", nullable = false)
  private Status            status;

  /**
   * We can not set default lastAccess is Date(0L)
   * because some rdbms does not allow to store date as '1970-01-01 01:00:00 UTC' (e.g MySQL)
   * We can not set default value is null because the sort with null value is different between rdbms
   * and we can not control it in JPA Query
   *
   * Use the default date is '1970-01-02 01:00:00 UTC' is valid for both rdbms and requirement
   */
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "LAST_ACCESS")
  private Date              lastAccess = new Date(86400000L);

  @Column(name = "VISITED")
  private boolean           visited;

  public SpaceMemberEntity() {
    this(null, null, null);
  }

  public SpaceMemberEntity(SpaceEntity space, String userId, Status status) {
    this.setSpace(space);
    this.setUserId(userId);
    this.setStatus(status);
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public SpaceEntity getSpace() {
    return space;
  }

  public void setSpace(SpaceEntity space) {
    this.space = space;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public Date getLastAccess() {
    return lastAccess;
  }

  public void setLastAccess(Date lastAccess) {
    this.lastAccess = lastAccess;
  }

  public boolean isVisited() {
    return visited;
  }

  public void setVisited(boolean visited) {
    this.visited = visited;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SpaceMemberEntity that = (SpaceMemberEntity) o;

    if (!space.equals(that.space)) return false;
    if (!userId.equals(that.userId)) return false;
    return status == that.status;

  }

  @Override
  public int hashCode() {
    int result = space.hashCode();
    result = 31 * result + userId.hashCode();
    result = 31 * result + status.hashCode();
    return result;
  }

  public static enum Status {
    MEMBER, MANAGER, PENDING, INVITED, IGNORED
  }
}
