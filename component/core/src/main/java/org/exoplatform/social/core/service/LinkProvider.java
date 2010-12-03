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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

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
import org.exoplatform.social.core.image.ImageUtils;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.model.AvatarAttachment;

public class LinkProvider {
  private static IdentityManager identityManager;
  private static Log             LOG = ExoLogger.getLogger(LinkProvider.class);

  /**
   * The method return the uri link to user profile
   * 
   * @param username
   * @return the uri link to user profile
   */
  public static String getProfileUri(final String username) {
    return getProfileUri(username, null);
  }

  /**
   * The method return the uri link to user profile on portalOwner
   * 
   * @param userName
   * @param portalOwner
   * @return the uri link to user profile on portalOwner
   */
  public static String getProfileUri(final String userName, final String portalOwner) {
    return buildProfileUri(userName, portalOwner);
  }

  /**
   * The method return tag <a> with a link to profile of userName
   * 
   * @param username
   * @return tag <a> with a link to profile of userName
   */
  public static String getProfileLink(final String username) {
    return getProfileLink(username, null);
  }

  /**
   * The method return tag <a> with a link to profile of userName on portalName
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
   * Get absolute profile uri of userName
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
   * Gets activity link of space or user; remoteId should be the id name.
   * For example: organization:root or space:abc_def
   * 
   * @param providerId
   * @param remoteId
   * @return
   */
  public static String getActivityUri(final String providerId, final String remoteId) {
    return getActivityUri(providerId, remoteId, null);
  }

  /**
   * Gets activity link of space or user; remoteId should be the id name. For
   * example: organization:root or space:abc_def
   * 
   * @param remoteId
   * @return
   */
  public static String getActivityUri(final String providerId, final String remoteId, String portalOwner) {
    final String prefix = "/" + PortalContainer.getCurrentPortalContainerName() + "/private/" + getPortalOwner(portalOwner) + "/";
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
   * Build avatar image uri from avatarAttachment
   * 
   * @param avatarAttachment
   * @return uri
   */
  public static String buildAvatarImageUri(final AvatarAttachment avatarAttachment) {
    return buildAvatarImageUri(PortalContainer.getInstance(), avatarAttachment);
  }

  /**
   * Build avatar image uri from avatarAttachment
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
   * Get avatar image uri of profile in a portalContainer
   * 
   * @param profile
   * @param portalContainer
   * @return null or an url if available
   */
  public static String getAvatarImageSource(final PortalContainer portalContainer, final Profile profile) {
    String avatarUrl = (String) profile.getProperty(Profile.AVATAR_URL);
    if (avatarUrl != null) {
      return avatarUrl;
    }

    final AvatarAttachment avatarAttachment = (AvatarAttachment) profile.getProperty(Profile.AVATAR);
    if (avatarAttachment != null) {
      avatarUrl = buildAvatarImageUri(portalContainer, avatarAttachment);
      profile.setProperty(Profile.AVATAR_URL, avatarUrl);
      getIdentityManager().updateAvatar(profile);
      return avatarUrl;
    }
    return null;
  }

  /**
   * Get avatar image uri of profile
   * @param profile
   * @return null or an url if available
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
      getIdentityManager().updateAvatar(profile);
      return avatarUrl;
    }
    return null;
  }

  /**
   * If the resized image does not exist, the method will lazily create a new
   * scaled image file, store it as a property of the profile and return the url
   * to view that resized avatar file.
   * 
   * @param profile
   * @param width
   * @param height
   * @return null or an url if available
   * @throws Exception
   */
  public static String getAvatarImageSource(Profile profile, int width, int height) {
    // Determine the key of avatar file and avatar url like avatar_30x30 and avatar_30x30Url
    final String postfix = ImageUtils.buildImagePostfix(width, height);
    final String keyFile = Profile.AVATAR + postfix;
    final String keyURL = Profile.AVATAR + postfix + Profile.URL_POSTFIX;
    // When the resized avatar with params size is exist, we return immediately
    String avatarUrl = (String) profile.getProperty(keyURL);
    if (avatarUrl != null) {
      return avatarUrl;
    }
    // When had resized avatar but hadn't avatar url we build the avatar url then return
    AvatarAttachment avatarAttachment = (AvatarAttachment) profile.getProperty(keyURL);
    if (avatarAttachment != null) {
      avatarUrl = buildAvatarImageUri(avatarAttachment);
      profile.setProperty(keyURL, avatarUrl);
      getIdentityManager().updateAvatar(profile);
      return avatarUrl;
    }
    // When hadn't avatar yet we return null
    avatarAttachment = (AvatarAttachment) profile.getProperty(Profile.AVATAR);
    if (avatarAttachment == null)
      return null;
    // Otherwise we create the resize avatar then return the avatar url
    InputStream inputStream = new ByteArrayInputStream(avatarAttachment.getImageBytes());
    avatarAttachment = ImageUtils.createResizedAvatarAttachment(inputStream,
                                                                width,
                                                                height,
                                                                avatarAttachment.getId() + postfix,
                                                                ImageUtils.buildFileName(avatarAttachment.getFileName(),
                                                                                         Profile.RESIZED_SUBFIX,
                                                                                         postfix),
                                                                avatarAttachment.getMimeType(),
                                                                avatarAttachment.getWorkspace());

    if (avatarAttachment == null)
      return getAvatarImageSource(profile);
    // Build the url to that resized avatar file then save and return
    avatarUrl = buildAvatarImageUri(avatarAttachment);

    // Set property that contain resized avatar file and url to profile
    profile.setProperty(keyFile, avatarAttachment);
    profile.setProperty(keyURL, avatarUrl);

    getIdentityManager().updateAvatar(profile);
    return avatarUrl;
  }

  /**
   * Build profile uri from userName and portalOwner
   * 
   * @param userName
   * @param portalOwner
   * @return profile uri
   */
  private static String buildProfileUri(final String userName, final String portalOwner) {
    return buildProfileUri(userName, PortalContainer.getCurrentPortalContainerName(), portalOwner);
  }

  /**
   * Build profile uri from userName and portalName and portalOwner
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
   * Escape jcr special characters
   * 
   * @param string
   * @return
   */
  private static String escapeJCRSpecialCharacters(String string) {
    return string.replace("[", "%5B").replace("]", "%5D").replace(":", "%3A");
  }

  /**
   * get IdentityManager instance
   * 
   * @return identityManager
   */
  private static IdentityManager getIdentityManager() {
    if(LinkProvider.identityManager == null)
    {
      LinkProvider.identityManager = (IdentityManager) PortalContainer.getInstance()
                                    .getComponentInstanceOfType(IdentityManager.class);
    }
    return LinkProvider.identityManager;
  }

  /**
   * Get portal owner, if parameter is null or "", the method return default portal owner
   * 
   * @param portalOwner
   * @return portalOwner
   */
  private static String getPortalOwner(String portalOwner) {
    if (portalOwner == null || "".equals(portalOwner)) {
      portalOwner = Util.getPortalRequestContext().getPortalOwner();
    }
    return portalOwner;
  }
}