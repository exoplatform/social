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

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.api.IdentityStorage;

/**
 * Listens to user updating events.
 * 
 * Created by hanh.vi@exoplatform.com
 * 
 * Jan 17, 2011
 * @since  1.2.0-GA
 */
public class SocialUserEventListenerImpl extends UserEventListener {

  private static final Log LOG = ExoLogger.getLogger(SocialUserEventListenerImpl.class);
  
  @Override
  public void preSave(User user, boolean isNew) throws Exception {

    RequestLifeCycle.begin(PortalContainer.getInstance());
    try{
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      IdentityStorage ids = (IdentityStorage) container.getComponentInstanceOfType(IdentityStorage.class);
  
      Identity identity = ids.findIdentity(OrganizationIdentityProvider.NAME, user.getUserName());
  
      if (isNew && identity != null) {
        throw new RuntimeException("Unable to create a previously deleted user : " + user.getUserName());
      }

    }
    finally{
      RequestLifeCycle.end();
    }
  }

  /**
   * Listens to postSave action for updating profile.
   *
   * @param user
   * @param isNew
   * @throws Exception
   */
  public void postSave(User user, boolean isNew) throws Exception {
    RequestLifeCycle.begin(PortalContainer.getInstance());
    
    try{
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      //
      IdentityManager idm = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
      Identity identity = idm.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user.getUserName(), true);
       
      //
      Profile profile = identity.getProfile();
      
      //
      boolean hasUpdated = false;
      
      if(!isNew) {
        String uFirstName = user.getFirstName();
        String uLastName = user.getLastName();
        String uDisplayName = user.getDisplayName();
        String uEmail = user.getEmail();
  
        //
        String pFirstName = (String) profile.getProperty(Profile.FIRST_NAME);
        String pLastName = (String) profile.getProperty(Profile.LAST_NAME);
        String pFullName = (String) profile.getProperty(Profile.FULL_NAME);
        String pEmail = (String) profile.getProperty(Profile.EMAIL);
       
  
        if ((pFirstName == null) || (!pFirstName.equals(uFirstName))) {
          profile.setProperty(Profile.FIRST_NAME, uFirstName);
          hasUpdated = true;
        }
  
        if ((pLastName == null) || (!pLastName.equals(uLastName))) {
          profile.setProperty(Profile.LAST_NAME, uLastName);
          hasUpdated = true;
        }
        
        if (uDisplayName == null || StringUtils.isEmpty(uDisplayName)) {
          uDisplayName = uFirstName + " " + uLastName;
        } 
        
        if(!uDisplayName.equals(pFullName)) {
          profile.setProperty(Profile.FULL_NAME, uDisplayName);
          hasUpdated = true;
        }
        
        if ((pEmail == null) || (!pEmail.equals(uEmail))) {
          profile.setProperty(Profile.EMAIL, uEmail);
          hasUpdated = true;
        }
        
      }
      
      if (hasUpdated) {
        profile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.CONTACT));
        idm.updateProfile(profile);
      }
    }
    finally{
      RequestLifeCycle.end();
    }
  }

  @Override
  public void preDelete(final User user) throws Exception {

    RequestLifeCycle.begin(PortalContainer.getInstance());
    try{
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      IdentityManager idm = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
      Identity identity = idm.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user.getUserName(), true);
      
      try {
        idm.hardDeleteIdentity(identity);
      } catch (Exception e) {
        // TODO: Send an alert email to super admin to manage spaces in case deleted user is the last manager.
        // Nothing executed (user not deleted) when facing this case now with code commit by SOC-1507.
        // Will be implemented by SOC-2276.
        
        LOG.debug("Problem occurred when deleting user named " + identity.getRemoteId());
      }
      
    }finally{
      RequestLifeCycle.end();
    }

  }
}
