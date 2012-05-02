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
package org.exoplatform.social.core.service;

import org.apache.commons.lang.Validate;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.model.AvatarAttachment;

public class LinkProvider {

  private IdentityManager identityManager;
  private static Log LOG = ExoLogger.getLogger(LinkProvider.class);

  public static String DEFAULT_PORTAL_OWNER = "classic";
  
  /**
   * Constructor with parameter to inject the default portal owner name
   * @param params
   * @since 1.1.6
   */
  public LinkProvider(InitParams params){
    if(params.getValueParam("predefinedOwner") != null) DEFAULT_PORTAL_OWNER = params.getValueParam("predefinedOwner").getValue();
  }
  
  public LinkProvider(InitParams params, IdentityManager identityManager) {
    this.identityManager = identityManager;
    init(params);
  }

  private void init(InitParams params) {
    if(params.getValueParam("predefinedOwner") != null) DEFAULT_PORTAL_OWNER = params.getValueParam("predefinedOwner").getValue();
  }

  public String getProfileUri(String username) {
    return getProfileUri(username, null);
  }

  public String getProfileUri(String username, String portalOwner) {
    String url = null;
    if(portalOwner == null || portalOwner.trim().length() == 0) portalOwner = DEFAULT_PORTAL_OWNER;

    try {
      url = getBaseUri(portalOwner) + "/profile/" + username;
    } catch (Exception e) {
      LOG.warn("failed to substitute username for " + username + ": " + e.getMessage());
    }
    return url;
  }

  public String getProfileLink(String username) {
    return getProfileLink(username, null);
  }

  public String getProfileLink(String username, String portalOwner) {
    String link = null;
    if(portalOwner == null || portalOwner.trim().length() == 0) portalOwner = DEFAULT_PORTAL_OWNER;

    try {
      Identity identity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, true);
      Validate.notNull(identity, "Identity must not be null.");
      String url = getBaseUri(portalOwner) + "/profile/" + identity.getRemoteId();
      link = "<a href=\"" + url + "\" target=\"_parent\">" + identity.getProfile().getFullName() + "</a>";
    } catch (Exception e) {
      LOG.warn("failed to substitute username for " + username + ": " + e.getMessage());
    }
    return link;
  }

  public String getAbsoluteProfileUrl(String userName, String portalName, String portalOwner, String host) {
    String url = null;
    if(portalOwner == null || portalOwner.trim().length() == 0) portalOwner = DEFAULT_PORTAL_OWNER;

    try {
      Identity identity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName, true);
      Validate.notNull(identity, "Identity must not be null.");

      url = host + getBaseUri(portalOwner) + "/profile/" + identity.getRemoteId();
    } catch (Exception e) {
      LOG.warn("failed to substitute username for " + userName + ": " + e.getMessage());
    }
    return url;
  }

  /**
   * @param avatarAttachment
   * @return url to avatar
   */
  public static String buildAvatarUrl(AvatarAttachment avatarAttachment) {
    String avatarUrl = null;
    try {
      String repository = ((RepositoryService) PortalContainer.getComponent(RepositoryService.class)).getCurrentRepository()
                                                                                                                   .getConfiguration()
                                                                                                                   .getName();
      avatarUrl = "/" + PortalContainer.getCurrentRestContextName() + "/jcr/" + repository + "/"
          + avatarAttachment.getWorkspace() + avatarAttachment.getDataPath() + "/?rnd="
          + avatarAttachment.getLastModified();
    } catch (Exception e) {
      LOG.warn("Failed to build avatar url from avatar attachment for: " + e.getMessage());
    }
    return avatarUrl;
  }
 
  /**
   * Builds profile uri from userName and portalName and portalOwner.
   *
   * @param portalOwner
   * @return
   */
  private static String getBaseUri(String portalOwner) {
    String portalName = PortalContainer.getCurrentPortalContainerName();
    return "/" + portalName + "/private/" + getPortalOwner(portalOwner);
  }
  
  /**
   * Gets portal owner, if parameter is null or "", the method return default portal owner.
   *
   * @param portalOwner
   * @return portalOwner
   */
  private static String getPortalOwner(String portalOwner) {
    if (portalOwner == null || portalOwner.trim().length() == 0) {
      try {
        return Util.getPortalRequestContext().getPortalOwner();
      } catch (Exception e) {
        return DEFAULT_PORTAL_OWNER;
      }
    }
    return portalOwner;
  }
  
  /**
   * Gets IdentityManager instance.
   *
   * @return identityManager
   */
  private IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = (IdentityManager) PortalContainer.getInstance()
      .getComponentInstanceOfType(IdentityManager.class);
    }
    return identityManager;
  }
}
