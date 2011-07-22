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

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;

import java.util.Collections;
import java.util.Map;

/**
 * Immutable profile data.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class ProfileData implements CacheData<Profile> {

  private final String profileId;

  private final String identityId;

  private final String providerId;

  private final String remoteId;

  private final boolean hasChanged;

  private final Map<String, Object> data;

  public ProfileData(final Profile profile) {
    this.profileId = profile.getId();
    this.identityId = profile.getIdentity().getId();
    this.providerId = profile.getIdentity().getProviderId();
    this.remoteId = profile.getIdentity().getRemoteId();
    this.hasChanged = profile.hasChanged();
    this.data = Collections.unmodifiableMap(profile.getProperties());
  }

  public String getProfileId() {
    return profileId;
  }

  public String getIdentityId() {
    return identityId;
  }

  public String getProviderId() {
    return providerId;
  }

  public String getRemoteId() {
    return remoteId;
  }

  public Map<String, Object> getData() {
    return data;
  }

  public Profile build() {
    Identity identity = new Identity(identityId);
    identity.setProviderId(providerId);
    identity.setRemoteId(remoteId);

    Profile profile = new Profile(identity);
    profile.setId(profileId);
    for(String key : data.keySet()) {
      profile.setProperty(key, data.get(key));
    }

    identity.setProfile(profile);
    if (!hasChanged) {
      profile.clearHasChanged();
    }
    return profile;
  }

}
