/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

package org.exoplatform.social.core.storage.cache.model.data;

import org.exoplatform.social.core.space.model.Space;

import java.util.Arrays;
import java.util.Objects;

/**
 * Immutable space data.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SpaceData implements CacheData<Space> {
  private static final long serialVersionUID = 6109309246791818373L;

  private final String id;
  private final String app;
  private final String prettyName;
  private final String displayName;
  private final String registration;
  private final String description;
  private final String type;
  private final String visibility;
  private final String priority;
  private final String avatarUrl;
  private final String bannerUrl;
  private final String groupId;
  private final String url;
  private final Long avatarLastUpdated;
  private final Long bannerLastUpdated;
  private final Long createdTime;

  private final String[] members;
  private final String[] managers;
  private final String[] pendingUser;
  private final String[] invitedUser;

  public SpaceData(final Space space) {

    id = space.getId();
    app = space.getApp();
    prettyName = space.getPrettyName();
    displayName = space.getDisplayName();
    registration = space.getRegistration();
    description = space.getDescription();
    type = space.getType();
    visibility = space.getVisibility();
    priority = space.getPriority();
    avatarLastUpdated = space.getAvatarLastUpdated();
    bannerLastUpdated = space.getBannerLastUpdated();
    avatarUrl = space.getAvatarUrl();
    bannerUrl = space.getBannerUrl();
    groupId = space.getGroupId();
    url = space.getUrl();

    members = space.getMembers();
    managers = space.getManagers();
    pendingUser = space.getPendingUsers();
    invitedUser = space.getInvitedUsers();
    createdTime = space.getCreatedTime();

  }

  public Space build() {

    Space space = new Space();

    space.setId(id);
    space.setApp(app);
    space.setDisplayName(displayName);
    space.setPrettyName(prettyName);
    space.setRegistration(registration);
    space.setDescription(description);
    space.setType(type);
    space.setVisibility(visibility);
    space.setPriority(priority);
    space.setAvatarLastUpdated(avatarLastUpdated);
    space.setBannerLastUpdated(bannerLastUpdated);
    space.setAvatarUrl(avatarUrl);
    space.setBannerUrl(bannerUrl);
    space.setGroupId(groupId);
    space.setUrl(url);
    space.setMembers(members);
    space.setManagers(managers);
    space.setPendingUsers(pendingUser);
    space.setInvitedUsers(invitedUser);
    space.setCreatedTime(createdTime);

    return space;

  }

  public String getId() {
    return id;
  }

  public String getApp() {
    return app;
  }

  public String getPrettyName() {
    return prettyName;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getRegistration() {
    return registration;
  }

  public String getDescription() {
    return description;
  }

  public String getType() {
    return type;
  }

  public String getVisibility() {
    return visibility;
  }

  public String getPriority() {
    return priority;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public String getGroupId() {
    return groupId;
  }

  public String getUrl() {
    return url;
  }

  public String[] getMembers() {
    return members;
  }

  public String[] getManagers() {
    return managers;
  }

  public String[] getPendingUser() {
    return pendingUser;
  }

  public String[] getInvitedUser() {
    return invitedUser;
  }

  public String getBannerUrl() {
    return bannerUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SpaceData spaceData = (SpaceData) o;
    return Objects.equals(id, spaceData.id) &&
            Objects.equals(app, spaceData.app) &&
            Objects.equals(prettyName, spaceData.prettyName) &&
            Objects.equals(displayName, spaceData.displayName) &&
            Objects.equals(registration, spaceData.registration) &&
            Objects.equals(description, spaceData.description) &&
            Objects.equals(type, spaceData.type) &&
            Objects.equals(visibility, spaceData.visibility) &&
            Objects.equals(priority, spaceData.priority) &&
            Objects.equals(avatarUrl, spaceData.avatarUrl) &&
            Objects.equals(bannerUrl, spaceData.bannerUrl) &&
            Objects.equals(groupId, spaceData.groupId) &&
            Objects.equals(url, spaceData.url) &&
            Objects.equals(avatarLastUpdated, spaceData.avatarLastUpdated) &&
            Objects.equals(bannerLastUpdated, spaceData.bannerLastUpdated) &&
            Objects.equals(createdTime, spaceData.createdTime) &&
            Arrays.equals(members, spaceData.members) &&
            Arrays.equals(managers, spaceData.managers) &&
            Arrays.equals(pendingUser, spaceData.pendingUser) &&
            Arrays.equals(invitedUser, spaceData.invitedUser);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, app, prettyName, displayName, registration, description, type, visibility,
            priority, avatarUrl, bannerUrl, groupId, url, avatarLastUpdated, bannerLastUpdated, createdTime,
            members, managers, pendingUser, invitedUser);
  }
}
