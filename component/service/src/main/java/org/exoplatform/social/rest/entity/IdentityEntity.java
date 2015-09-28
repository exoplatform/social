/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
*/

package org.exoplatform.social.rest.entity;

import org.exoplatform.social.core.identity.model.GlobalId;

public class IdentityEntity extends BaseEntity {
  private static final long serialVersionUID = -1900502217691572027L;

  public IdentityEntity() {
  }

  public IdentityEntity(String id) {
    super(id);
  }

  public IdentityEntity setRemoteId(String remoteId) {
    setProperty("remoteId", remoteId);
    return this;
  }

  public String getRemoteId() {
    return getString("remoteId");
  }

  public IdentityEntity setProviderId(String providerId) {
    setProperty("providerId", providerId);
    return this;
  }

  public String getProviderId() {
    return getString("providerId");
  }

  public IdentityEntity setGlobalId(GlobalId globalId) {
    setProperty("globalId", globalId);
    return this;
  }

  public IdentityEntity setDeleted(Boolean deleted) {
    setProperty("deleted", deleted);
    return this;
  }

  public String getDeleted() {
    return getString("deleted");
  }

  public IdentityEntity setProfile(DataEntity profile) {
    setProperty("profile", profile);
    return this;
  }

  public DataEntity getProfile() {
    return (DataEntity) getProperty("profile");
  }
}
