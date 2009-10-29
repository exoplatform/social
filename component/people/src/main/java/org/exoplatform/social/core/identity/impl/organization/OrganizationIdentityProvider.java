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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.JCRStorage;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;

public class OrganizationIdentityProvider extends IdentityProvider {
  private JCRStorage storage;
  private OrganizationService organizationService;
  public final static String NAME = "organization";

  public OrganizationIdentityProvider(JCRStorage storage, OrganizationService organizationService) {
    this.storage = storage;
    this.organizationService = organizationService;
  }

  public String getName() {
    return NAME;
  }



  public Identity getIdentityByRemoteId(Identity identity) throws Exception {
    //TODO: tung.dang need to review again.
    User user = null;
    try {
      UserHandler userHandler = organizationService.getUserHandler();
      String remote = identity.getRemoteId();
      System.out.println("\n\n\n\n find user by name: " + remote);
      user = userHandler.findUserByName(remote);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    if (user == null) {
      return null;
    }

    loadIdentity(user, identity);
    
    //TODO dang.tung need to save profile in database if node doesn't exist
    saveProfile(identity.getProfile());
    return identity;
  }

  private Identity loadIdentity(User user, Identity identity) throws Exception {
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
    //TODO: dang tung - need to review again.
    PageList pl = organizationService.getUserHandler().getUserPageList(20);
    List<User> userList = pl.getAll();
    List<String> userIds = new ArrayList<String>();

    for (User user : userList) {
      userIds.add(user.getUserName());
    }
    return userIds;
  }
}
