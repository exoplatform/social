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
package org.exoplatform.social.core;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;

public class LinkProvider {

  private IdentityManager identityManager;
  private static Log LOG = ExoLogger.getLogger(LinkProvider.class);

  public LinkProvider(InitParams params, IdentityManager identityManager) {
    this.identityManager = identityManager;
    init(params);
  }

  private void init(InitParams params) {

  }

  public String getProfileLink(String username) {

    String link = username;
      try {
        Identity identity = identityManager.getIdentity(OrganizationIdentityProvider.NAME + ":" + username, true);
      if (identity == null) {
        throw new RuntimeException("could not find a user identity for " + username);
      }

      String container = PortalContainer.getCurrentPortalContainerName();
      String url = "/"+ container +"/private/classic/profile/" + identity.getRemoteId();
      link = "<a href=\"" + url + "\" target=\"_parent\">" + identity.getProfile().getFullName() + "</a>";

      } catch (Exception e) {
        LOG.error("failed to substitute username for " + username + ": " + e.getMessage());
      }

      return link;
    }

}
