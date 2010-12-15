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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.model.AvatarAttachment;

public class LinkProvider {
  private static IdentityManager identityManager;
  private static Log             LOG = ExoLogger.getLogger(LinkProvider.class);

  /**
   * Hacks for unit test to work
   */
  private static String DEFAULT_PORTAL_OWNER = "classic";

  /**
   * returns the uri link to user profile
   * 
   * @param username
   * @return the uri link to user profile
   */
  public static String getProfileUri(final String username) {
    return buildProfileUri(username, null);
  }

  /**
   * Returns the uri link to user profile in a portalOwner
   * 
   * @param username
   * @param portalOwner
   * @return the uri link to user profile
   */
  public static String getProfileUri(final String username, final String portalOwner) {
    return buildProfileUri(username, portalOwner);
  }

  /**
   * Returns tag <a> with a link to profile of userName
   * 
   * @param username
   * @return tag <a> with a link to profile of userName
   */
  public static String getProfileLink(final String username) {
    return getProfileLink(username, null);
  }

  /**
   * Returns tag <a> with a link to profile of userName on portalName
   * 
   * @param username
   * @param portalOwner
   * @return tag <a> with a link to profile of userName on portalName
   */
  public static String getProfileLink(final String username, final String portalOwner) {
    Identity identity = getIdentityManager().getIdentity(OrganizationIdentityProvider.NAME + ":" + username, true);
    Validate.notNull(identity, "Identity must not be null.");
    return "<a href=\"" + buildProfileUri(identity.getRemoteId(), portalOwner)
        + "\" target=\"_parent\">" + identity.getProfile().getFullName() + "</a>";
  }

  /**
   * Gets absolute profile uri of userName
   * 
   * @param userName
   * @param portalName
   * @param portalOwner
   * @param host
   * @return absolute profile uri of userName
   */
  public static String getAbsoluteProfileUrl(final String userName, final String portalName, final String portalOwner, final String host) {
    return host + buildProfileUri(userName, portalName, portalOwner);
  }

  /**
   * Gets activity link of space or user; remoteId should be the id name. For
   * example: organization:root or space:abc_def
   * 
   * @param providerId
   * @param remoteId
   * @return
   */
  public static String getActivityUri(final String providerId, final String remoteId) {
    final String prefix = "/" + PortalContainer.getCurrentPortalContainerName() + "/private/" + getPortalOwner(null) + "/";
    if (providerId.equals(OrganizationIdentityProvider.NAME)) {
      return prefix + "activities/" + remoteId;
    } else if (providerId.equals(SpaceIdentityProvider.NAME)) {
      return prefix + remoteId;
    } else {
      LOG.warn("Failed to getActivityLink with providerId: " + providerId);
    }
    return null;
  }

  /**
   * Builds avatar image uri from avatarAttachment
   * 
   * @param avatarAttachment
   * @return uri
   */
  public static String buildAvatarImageUri(final AvatarAttachment avatarAttachment) {
    return buildAvatarImageUri(PortalContainer.getInstance(), avatarAttachment);
  }

  /**
   * Builds avatar image uri from avatarAttachment
   * 
   * @param container
   * @param avatarAttachment
   * @return url to avatar
   */
  public static String buildAvatarImageUri(final PortalContainer container, final AvatarAttachment avatarAttachment) {
    String avatarUrl = null;
    try {
      if (avatarAttachment != null) {
        final String repository = ((RepositoryService) container.getComponentInstanceOfType(RepositoryService.class)).getCurrentRepository()
                                                                                                               .getConfiguration()
                                                                                                               .getName();
        avatarUrl = "/" + container.getRestContextName() + "/jcr/" + repository + "/"
            + avatarAttachment.getWorkspace() + avatarAttachment.getDataPath() + "/?upd="
            + avatarAttachment.getLastModified();
        avatarUrl = escapeJCRSpecialCharacters(avatarUrl);
      }
    } catch (Exception e) {
      LOG.warn("Failed to build avatar url from avatar attachment for: " + e.getMessage());
    }
    return avatarUrl;
  }

  /**
   * Gets avatar image uri of profile in a portalContainer
   * 
   * @param profile
   * @param portalContainer
   * @return null or an url if available
   * @deprecated use {@link #getAvatarUrl()}. Will be removed at 1.3.x
   */
  public static String getAvatarImageSource(final PortalContainer portalContainer, final Profile profile) {
    final AvatarAttachment avatarAttachment = (AvatarAttachment) profile.getProperty(Profile.AVATAR);
    if (avatarAttachment != null) {
      return buildAvatarImageUri(portalContainer, avatarAttachment);
    }
    return null;
  }

  /**
   * Gets avatar image uri of profile
   * @param profile
   * @return null or an url if available
   * @deprecated use {@link #getAvatarUrl()}. Will be removed at 1.3.x
   */
  public static String getAvatarImageSource(final Profile profile) {
    String avatarUrl = (String) profile.getProperty(Profile.AVATAR_URL);
    if (avatarUrl != null) {
      return avatarUrl;
    }

    final AvatarAttachment avatarAttachment = (AvatarAttachment) profile.getProperty(Profile.AVATAR);
    if (avatarAttachment != null) {
      avatarUrl = buildAvatarImageUri(avatarAttachment);
      profile.setProperty(Profile.AVATAR_URL, avatarUrl);
      getIdentityManager().saveProfile(profile);
      return avatarUrl;
    }
    return null;
  }

  /**
   * Builds profile uri from userName and portalOwner
   * 
   * @param userName
   * @param portalOwner
   * @return profile uri
   */
  private static String buildProfileUri(final String userName, final String portalOwner) {
    return buildProfileUri(userName, PortalContainer.getCurrentPortalContainerName(), portalOwner);
  }

  /**
   * Builds profile uri from userName and portalName and portalOwner
   * 
   * @param userName
   * @param portalName
   * @param portalOwner
   * @return profile uri
   */
  private static String buildProfileUri(final String userName, final String portalName, String portalOwner) {
    return "/" + StringUtils.trimToEmpty(portalName) + "/private/" + getPortalOwner(portalOwner)
        + "/profile/" + StringUtils.trimToEmpty(userName);
  }

  /**
   * Escapes jcr special characters
   * 
   * @param string
   * @return
   */
  private static String escapeJCRSpecialCharacters(String string) {
    return string.replace("[", "%5B").replace("]", "%5D").replace(":", "%3A");
  }

  /**
   * Gets IdentityManager instance
   * 
   * @return identityManager
   */
  private static IdentityManager getIdentityManager() {
    if (LinkProvider.identityManager == null) {
      LinkProvider.identityManager = (IdentityManager) PortalContainer.getInstance()
                                    .getComponentInstanceOfType(IdentityManager.class);
    }
    return LinkProvider.identityManager;
  }

  /**
   * Gets portal owner, if parameter is null or "", the method return default portal owner
   * 
   * @param portalOwner
   * @return portalOwner
   */
  private static String getPortalOwner(String portalOwner) {
    if (portalOwner == null || "".equals(portalOwner)) {
      try {
        return Util.getPortalRequestContext().getPortalOwner();
      } catch (Exception e) {
        return DEFAULT_PORTAL_OWNER;
      }
    }
    return portalOwner;
  }
}