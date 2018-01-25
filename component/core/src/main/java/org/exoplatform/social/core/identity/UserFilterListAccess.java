/*
 * Copyright (C) 2003-2017 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.identity;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.storage.api.IdentityStorage;

/**
 * ListAccess is used in loading users with the input profile filter. With this
 * list we can manage the size of returned list by offset and limit.
 */
public class UserFilterListAccess implements ListAccess<User> {

  private IdentityStorage identityStorage;

  private OrganizationService organizationService;

  private ProfileFilter   profileFilter;

  public UserFilterListAccess(OrganizationService organizationService, IdentityStorage identityStorage, ProfileFilter profileFilter) {
    this.identityStorage = identityStorage;
    this.organizationService = organizationService;
    this.profileFilter = profileFilter;
  }

  /**
   * {@inheritDoc}
   */
  public User[] load(int offset, int limit) throws Exception, IllegalArgumentException {
    List<? extends Identity> identities = null;
    //
    if (profileFilter.isEmpty()) {
        identities = identityStorage.getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME,
                                                                  profileFilter,
                                                                  offset,
                                                                  limit,
                                                                  false);
    } else {
      identities = identityStorage.getIdentitiesForMentions(OrganizationIdentityProvider.NAME,
                                                            profileFilter,
                                                            null,
                                                            offset,
                                                            limit,
                                                            false);
    }

    if (identities == null || identities.isEmpty()) {
      return new User[0];
    } else {
      List<User> users = new ArrayList<User>();
      int i = 0;
      for (Identity identity : identities) {
        String userId = identity.getRemoteId();
        User user = organizationService.getUserHandler().findUserByName(userId);
        if (user != null) {
          users.add(user);
        }
      }
      return users.toArray(new User[0]);
    }
  }

  /**
   * {@inheritDoc}
   */
  public int getSize() throws Exception {
    int size = 0;
    if (profileFilter.isEmpty()) {
      size = identityStorage.getIdentitiesByProfileFilterCount(OrganizationIdentityProvider.NAME, profileFilter);
    } else {
      size = identityStorage.getIdentitiesForMentionsCount(OrganizationIdentityProvider.NAME, profileFilter, null);
    }
    return size;
  }
}
