/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.core.identity.impl.organization;

import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.JCRStorage;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.commons.utils.PageList;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class OrganizationIdentityProvider extends IdentityProvider {
  private JCRStorage storage;
    private OrganizationService organizationService;
    public final static String NAME = "organization";

  public OrganizationIdentityProvider(JCRStorage storage, OrganizationService organizationService) {
    this.storage = storage;
    this.organizationService = organizationService;
  }

  public final String getName() {
    return NAME;
  }



  public final Identity  getIdentityByRemoteId(final Identity identity) throws Exception {
    //TODO: tung.dang need to review again.
    User user = null;
    try {
      UserHandler userHandler = organizationService.getUserHandler();
      user = userHandler.findUserByName(identity.getRemoteId());
    } catch (Exception e) {
      return null;
    }
    if (user == null) {
      return null;
    }

    loadIdentity(user, identity);

    return identity;
  }

  private Identity loadIdentity(User user, final Identity identity) throws Exception {
      Profile profile = identity.getProfile();

      profile.setProperty("firstName", user.getFirstName());
      profile.setProperty("lastName", user.getLastName());

      profile.setProperty("username", user.getUserName());

      storage.loadProfile(profile);

      if (user.getEmail() != null && !profile.contains("emails")) {
        List emails = new ArrayList();
        Map email = new HashMap();
        email.put("key", "work");
        email.put("value", user.getEmail());

        emails.add(email);
        profile.setProperty("emails", emails);
      }

    return identity;
  }

  public void saveProfile(Profile p) throws Exception {
    this.storage.saveProfile(p); 
  }

  public List<String> getAllUserId() throws Exception {
    PageList pl = organizationService.getUserHandler().getUserPageList(20);
    List<User> userList = pl.getAll();
    List<String> userIds = new ArrayList<String>();

    for (User user : userList) {
      userIds.add(user.getUserName());
    }
    return userIds;
  }
}
