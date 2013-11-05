/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.social.core.listeners;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.organization.UserProfileEventListener;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.api.IdentityStorage;

public class SocialUserProfileEventListenerImpl extends UserProfileEventListener {

  
  @Override
  public void postSave(UserProfile userProfile, boolean isNew) throws Exception {
    RequestLifeCycle.begin(PortalContainer.getInstance());
    try{
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      //
      IdentityManager idm = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
      Identity identity = idm.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userProfile.getUserName(), true);
         
      //
      Profile profile = identity.getProfile();
      
      //
      String uGender = null;
      String uPosition = null;
      if (userProfile != null) {
        uGender = userProfile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[4]);//"user.gender"
        uPosition = userProfile.getAttribute(UserProfile.PERSONAL_INFO_KEYS[7]);//user.jobtitle
      }
      
      //
      String pGender = (String) profile.getProperty(Profile.GENDER);
      String pPosition = (String) profile.getProperty(Profile.POSITION);
      
      //
      boolean hasUpdated = false;
  
      //
      if (uGender != null && !uGender.equals(pGender)) {
        profile.setProperty(Profile.GENDER, uGender);
        hasUpdated = true;
      }
      
      if (uPosition != null && !uPosition.equals(pPosition)) {
        profile.setProperty(Profile.POSITION, uPosition);
        hasUpdated = true;
      }
  
      if (hasUpdated) {
        IdentityStorage storage = (IdentityStorage) container.getComponentInstanceOfType(IdentityStorage.class);
        storage.updateProfile(profile);
      }
    }finally{
      RequestLifeCycle.end();
    }
  }
}
