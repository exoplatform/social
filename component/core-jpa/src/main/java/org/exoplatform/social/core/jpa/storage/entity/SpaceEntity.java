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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.exoplatform.commons.api.persistence.ExoEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity.Status;
import org.exoplatform.social.core.space.model.Space;

@Entity(name = "SocSpaceEntity")
@ExoEntity
@Table(name = "SOC_SPACES")
@NamedQueries({
    @NamedQuery(name = "SpaceEntity.getLastSpaces", query = "SELECT sp FROM SocSpaceEntity sp ORDER BY sp.createdDate DESC"),
    @NamedQuery(name = "SpaceEntity.getSpaceByGroupId", query = "SELECT sp FROM SocSpaceEntity sp WHERE sp.groupId = :groupId"),
    @NamedQuery(name = "SpaceEntity.getSpaceByPrettyName", query = "SELECT sp FROM SocSpaceEntity sp WHERE sp.prettyName = :prettyName"),
    @NamedQuery(name = "SpaceEntity.getSpaceByDisplayName", query = "SELECT sp FROM SocSpaceEntity sp WHERE sp.displayName = :displayName"),
    @NamedQuery(name = "SpaceEntity.getSpaceByURL", query = "SELECT sp FROM SocSpaceEntity sp WHERE sp.url = :url") })
public class SpaceEntity implements Serializable {

  private static final long serialVersionUID = 3223615477747436986L;

  @Id
  @SequenceGenerator(name = "SEQ_SOC_SPACES_ID", sequenceName = "SEQ_SOC_SPACES_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_SOC_SPACES_ID")
  @Column(name = "SPACE_ID")
  private Long              id;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "space", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<SpaceMemberEntity>  members          = new HashSet<>();

  /**
   * The list of applications with portlet Id, application name, and its state
   * (installed, activated, deactivated).
   */
  @ElementCollection
  @CollectionTable(name = "SOC_APPS", joinColumns = @JoinColumn(name = "SPACE_ID") )
  private Set<AppEntity>    app              = new HashSet<>();

  @Column(name = "PRETTY_NAME")
  private String            prettyName;

  @Column(name = "DISPLAY_NAME")
  private String            displayName;

  @Column(name = "REGISTRATION")
  private REGISTRATION            registration;

  @Column(name = "DESCRIPTION")
  private String            description;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "AVATAR_LAST_UPDATED")
  private Date              avatarLastUpdated;

  @Column(name = "VISIBILITY")
  public VISIBILITY         visibility;

  @Column(name = "PRIORITY")
  public PRIORITY           priority;

  @Column(name = "GROUP_ID")
  public String             groupId;

  @Column(name = "URL")
  public String             url;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "CREATED_DATE", nullable = false)
  private Date              createdDate      = new Date();

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Set<AppEntity> getApp() {
    return app;
  }

  public void setApp(Set<AppEntity> app) {
    this.app = app;
  }

  public String getPrettyName() {
    return prettyName;
  }

  public void setPrettyName(String prettyName) {
    this.prettyName = prettyName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public REGISTRATION getRegistration() {
    return registration;
  }

  public void setRegistration(REGISTRATION registration) {
    this.registration = registration;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Date getAvatarLastUpdated() {
    return avatarLastUpdated;
  }

  public void setAvatarLastUpdated(Date avatarLastUpdated) {
    this.avatarLastUpdated = avatarLastUpdated;
  }

  public VISIBILITY getVisibility() {
    return visibility;
  }

  public void setVisibility(VISIBILITY visibility) {
    this.visibility = visibility;
  }

  public PRIORITY getPriority() {
    return priority;
  }

  public void setPriority(PRIORITY priority) {
    this.priority = priority;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public Set<SpaceMemberEntity> getMembers() {
    return members;
  }

  public SpaceEntity buildFrom(Space space) {
    this.setApp(AppEntity.parse(space.getApp()));
    if (space.getAvatarLastUpdated() != null) {
      this.setAvatarLastUpdated(space.getAvatarLastUpdated() > 0 ? new Date(space.getAvatarLastUpdated()) : null);
    }
    this.setCreatedDate(space.getCreatedTime() > 0 ? new Date(space.getCreatedTime()) : new Date());
    this.setDescription(space.getDescription());
    this.setDisplayName(space.getDisplayName());
    this.setGroupId(space.getGroupId());
    this.setPrettyName(space.getPrettyName());
    PRIORITY priority = null;
    if (Space.HIGH_PRIORITY.equals(space.getPriority())) {
      priority = PRIORITY.HIGH;
    } else if (Space.INTERMEDIATE_PRIORITY.equals(space.getPriority())) {
      priority = PRIORITY.INTERMEDIATE;
    } else if (Space.LOW_PRIORITY.equals(space.getPriority())) {
      priority = PRIORITY.LOW;
    }
    this.setPriority(priority);
    if (space.getRegistration() != null) {
      this.setRegistration(REGISTRATION.valueOf(space.getRegistration().toUpperCase()));      
    }
    this.setUrl(space.getUrl());
    VISIBILITY visibility = null;
    if (space.getVisibility() != null) {
      visibility = VISIBILITY.valueOf(space.getVisibility().toUpperCase());
    }
    this.setVisibility(visibility);
    buildMembers(space);
    return this;
  }

  public String[] getPendingMembersId() {
    return getUserIds(Status.PENDING);
  }

  public String[] getInvitedMembersId() {
    return getUserIds(Status.INVITED);
  }

  public String[] getMembersId() {
    return getUserIds(Status.MEMBER);
  }

  public String[] getManagerMembersId() {
    return getUserIds(Status.MANAGER);
  }

  public static enum VISIBILITY {
    PUBLIC, PRIVATE, HIDDEN
  }

  public static enum PRIORITY {
    HIGH, INTERMEDIATE, LOW
  }
  
  public static enum REGISTRATION {
    OPEN, VALIDATION, CLOSE
  }

  private void buildMembers(Space space) {
    Set<SpaceMemberEntity> invited = this.getMembers(Status.INVITED);
    merge(invited, space.getInvitedUsers(), Status.INVITED);

    Set<SpaceMemberEntity> manager = this.getMembers(Status.MANAGER);
    merge(manager, space.getManagers(), Status.MANAGER);

    Set<SpaceMemberEntity> member = this.getMembers(Status.MEMBER);
    merge(member, space.getMembers(), Status.MEMBER);

    Set<SpaceMemberEntity> pending = this.getMembers(Status.PENDING);
    merge(pending, space.getPendingUsers(), Status.PENDING);
  }

  private void merge(Set<SpaceMemberEntity> spaceMembers, String[] userIds, Status status) {
    Set<String> ids = new HashSet<>(userIds != null ? Arrays.asList(userIds) : Collections.<String> emptyList());

    Iterator<SpaceMemberEntity> mems = spaceMembers.iterator();
    while (mems.hasNext()) {
      SpaceMemberEntity mem = mems.next();
      String id = mem.getUserId();

      if (ids.contains(mem.getUserId())) {
        ids.remove(id);
      } else {
        this.getMembers().remove(mem);
      }
    }

    for (String id : ids) {
      this.getMembers().add(new SpaceMemberEntity(this, id, status));
    }
  }

  private Set<SpaceMemberEntity> getMembers(Status status) {
    Set<SpaceMemberEntity> mems = new HashSet<>();
    for (SpaceMemberEntity mem : getMembers()) {
      if (mem.getStatus().equals(status)) {
        mems.add(mem);
      }
    }
    return mems;
  }

  private String[] getUserIds(Status status) {
    List<String> ids = new LinkedList<>();
    for (SpaceMemberEntity mem : getMembers(status)) {
      ids.add(mem.getUserId());
    }
    return ids.toArray(new String[ids.size()]);
  }
}
