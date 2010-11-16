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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.model.AvatarAttachment;

public class LinkProvider {

  private IdentityManager identityManager;
  private static Log LOG = ExoLogger.getLogger(LinkProvider.class);

  public LinkProvider(InitParams params, IdentityManager identityManager) {
    this.identityManager = identityManager;
    init(params);
  }

  private void init(InitParams params) {
  }

  public String getProfileUri(String username) {
    return getProfileUri(username, null);
  }

  public String getProfileUri(String username, String portalOwner) {
    String url = null;
    try {
      Identity identity = identityManager.getIdentity(OrganizationIdentityProvider.NAME + ":" + username, true);
      if (identity == null) {
        LOG.warn("could not find a user identity for " + username);
        return null;
      }

      String container = PortalContainer.getCurrentPortalContainerName();

      portalOwner = getPortalOwner(portalOwner);

      url = "/"+ container +"/private/"+portalOwner+"/profile/" + identity.getRemoteId();
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

    try {
      Identity identity = identityManager.getIdentity(OrganizationIdentityProvider.NAME + ":" + username, true);
      if (identity == null) {
        LOG.warn("could not find a user identity for " + username);
        return null;
      }

      String container = PortalContainer.getCurrentPortalContainerName();

      portalOwner = getPortalOwner(portalOwner);

      String url = "/"+ container +"/private/"+portalOwner+"/profile/" + identity.getRemoteId();
      link = "<a href=\"" + url + "\" target=\"_parent\">" + identity.getProfile().getFullName() + "</a>";
    } catch (Exception e) {
      LOG.warn("failed to substitute username for " + username + ": " + e.getMessage());
      return null;
    }
    return link;
  }


  public String getActivityUri(String providerId, String remoteId) {
    return getActivityUri(providerId, remoteId, null);
  }

  /**
   * Gets activity link of space or user; remoteId should be the id name.
   * For example: organization:root or space:abc_def
   * @param remoteId
   * @return
   */
  public String getActivityUri(String providerId, String remoteId, String portalOwner) {
    final String container = PortalContainer.getCurrentPortalContainerName();
    portalOwner = getPortalOwner(portalOwner);
    if (providerId.equals(OrganizationIdentityProvider.NAME)) {
      return "/"+ container +"/private/"+portalOwner+"/activities/" + remoteId;
    } else if (providerId.equals(SpaceIdentityProvider.NAME)) {
      return "/" + container + "/private/" + portalOwner + "/" + remoteId;
    } else {
      LOG.warn("Failed to getActivityLink with providerId: " + providerId);
    }
    return null;
  }


  public String getAbsoluteProfileUrl(String userName, String portalName, String portalOwner, String host) {
    String url = null;
    try {
      Identity identity = identityManager.getIdentity(OrganizationIdentityProvider.NAME + ":" + userName, true);
      if (identity == null) {
        throw new RuntimeException("could not find a user identity for " + userName);
      }

      url = host + "/"+ portalName +"/private/" + portalOwner + "/profile/" + identity.getRemoteId();
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


  private String getPortalOwner(String portalOwner) {
    if(portalOwner == null || portalOwner.equals("")){
      PortalRequestContext context = Util.getPortalRequestContext();
      portalOwner = context.getPortalOwner();
    }
    return portalOwner;
  }

}