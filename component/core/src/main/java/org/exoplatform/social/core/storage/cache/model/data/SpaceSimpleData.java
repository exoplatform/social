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

/**
 * Immutable space simple data.
 * It only contains the briefing data for Space.
 *
 * @author <a href="mailto:thanhvc@exoplatform.com">Thanh Vu</a>
 * @version $Revision$
 */
public class SpaceSimpleData implements CacheData<Space> {

  private final String id;
  private final String app;
  private final String prettyName;
  private final String displayName;
  private final String description;
  private final String avatarUrl;
  private final String groupId;
  private final String url;
  private final Long avatarLastUpdated;

  public SpaceSimpleData(final Space space) {

    id = space.getId();
    app = space.getApp();
    prettyName = space.getPrettyName();
    displayName = space.getDisplayName();
    description = space.getDescription();
    avatarLastUpdated = space.getAvatarLastUpdated();
    avatarUrl = space.getAvatarUrl();
    groupId = space.getGroupId();
    url = space.getUrl();
  }

  public Space build() {

    Space space = new Space();
    space.setId(id);
    space.setApp(app);
    space.setDisplayName(displayName);
    space.setPrettyName(prettyName);
    space.setDescription(description);
    space.setAvatarLastUpdated(avatarLastUpdated);
    space.setAvatarUrl(avatarUrl);
    space.setGroupId(groupId);
    space.setUrl(url);
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

  public String getDescription() {
    return description;
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

}
