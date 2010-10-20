/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

/**
 * After saving a user, an identity for that user should be saved in Social.
 *
 * When a user is deleted, identity in Social of that user should be deleted, too.
 *
 * @author    <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since     Oct 20, 2010
 * @since     1.1.1
 * @copyright eXo SAS
 */
public class UserToIdentityEventListener extends UserEventListener {

  private static final Log LOG = ExoLogger.getLogger(UserToIdentityEventListener.class);

  private IdentityManager identityManager;

  /**
   * constructor to init identityManager
   * @param identityManager
   */
  public UserToIdentityEventListener(IdentityManager identityManager) {
    this.identityManager = identityManager;
  }

  @Override
  public void postSave(User user, boolean isNew) {
    if (isNew) {
      identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user.getUserName());
    }
  }

  @Override
  public void postDelete(User user) {
    //FIXME hoatle: failed to delete because not found in store of Social
    //identityManager.deleteIdentity(identityManager.getIdentity(OrganizationIdentityProvider.NAME, user.getUserName(), false));
    //LOG.info("identity for user: " + user.getUserName() + " deleted.");
  }

}
