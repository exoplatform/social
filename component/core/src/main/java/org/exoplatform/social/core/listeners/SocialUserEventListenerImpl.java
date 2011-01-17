/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.social.core.listeners;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

/**
 * Listens to user updating events.
 * 
 * Created by hanh.vi@exoplatform.com
 * 
 * Jan 17, 2011
 * @since  1.2.0-GA
 */
public class SocialUserEventListenerImpl extends UserEventListener {
  
  /**
   * Listens to postSave action for updating profile.
   *
   * @param user
   * @param isNew
   * @throws Exception
   */
  public void postSave(User user, boolean isNew) throws Exception {
    if (!isNew) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      IdentityManager idm = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
      Identity identity = idm.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user.getUserName());

      Profile profile = identity.getProfile();
      String pFirstName = (String) profile.getProperty(Profile.FIRST_NAME);
      String pLastName = (String) profile.getProperty(Profile.LAST_NAME);
      String pEmail = (String) profile.getProperty(Profile.EMAIL);

      String uFirstName = user.getFirstName();
      String uLastName = user.getLastName();
      String uEmail = user.getEmail();

      boolean hasUpdated = false;
      
      if ((pFirstName == null) || (!pFirstName.equals(uFirstName))) {
        profile.setProperty(Profile.FIRST_NAME, uFirstName);
        hasUpdated = true;
      }

      if ((pLastName == null) || (!pLastName.equals(uLastName))) {
        profile.setProperty(Profile.LAST_NAME, uLastName);
        hasUpdated = true;
      }

      if ((pEmail == null) || (!pEmail.equals(uEmail))) {
        profile.setProperty(Profile.EMAIL, uEmail);
        hasUpdated = true;
      }

      if (hasUpdated) {
        idm.saveProfile(profile);
      }
    }
  }
}
