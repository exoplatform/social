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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
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
  private static Log LOG = ExoLogger.getLogger(LinkProvider.class);

  public static String getProfileUri(String username) {
    return getProfileUri(username, null);
  }

  public static String getProfileUri(String username, String portalOwner) {
    String url = null;
    try {
      IdentityManager identityManager = (IdentityManager) PortalContainer.getComponent(IdentityManager.class);
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

  public static String getProfileLink(String username) {
    return getProfileLink(username, null);
  }

  public static String getProfileLink(String username, String portalOwner) {
    String link;

    try {
      IdentityManager identityManager = (IdentityManager) PortalContainer.getComponent(IdentityManager.class);
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


  public static String getAbsoluteProfileUrl(String userName, String portalName, String portalOwner, String host) {
    String url = null;
    try {
      IdentityManager identityManager = (IdentityManager) PortalContainer.getComponent(IdentityManager.class);
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

  public static String getAvatarImageSource(AvatarAttachment avatarAttachment) {
    return getAvatarImageSource(PortalContainer.getInstance(), avatarAttachment);
  }

  /**
   * @param container
   * @param avatarAttachment   
   * @return url to avatar
   */
  public static String getAvatarImageSource(PortalContainer container, AvatarAttachment avatarAttachment) {
    String avatarUrl = null;
    try {
      if (avatarAttachment != null) {
        String repository = ((RepositoryService) container.getComponentInstanceOfType(RepositoryService.class)).getCurrentRepository()
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
   * @param profile
   * @return null or an url if available
   */
  public static String getAvatarImageSource(Profile profile) {
    String avatarUrl = (String) profile.getProperty(Profile.AVATAR_URL);
    if (avatarUrl != null) {
      return avatarUrl;
    }
    AvatarAttachment avatarAttachment = (AvatarAttachment) profile.getProperty(Profile.AVATAR);
    if (avatarAttachment != null) {
      return getAvatarImageSource(avatarAttachment);
    }
    return null;
  }

  /**
   * If the resized image does not exist, the method will lazily create a new scaled image
   * file, store it as a property of the profile and return the url to view that resized avatar file.
   *
   * @param profile
   * @param width
   * @param height
   * @author tuan_nguyenxuan Oct 26, 2010
   * @return null or an url if available
   * @throws Exception
   */
  public static String getAvatarImageSource(Profile profile,int width, int height) {
    // Determine the key of avatar file and avatar url like avatar_30x30 and
    // avatar_30x30Url
    String postfix = ImageUtils.buildImagePostfix(width, height);
    String keyFile = Profile.AVATAR + postfix;
    String keyURL = Profile.AVATAR + postfix + Profile.URL_POSTFIX;
    // When the resized avatar with params size is exist, we return immediately
    String avatarUrl = (String) profile.getProperty(keyURL);
    if (avatarUrl != null) {
      return avatarUrl;
    }
    IdentityManager identityManager = (IdentityManager) PortalContainer.getInstance()
                                                                       .getComponentInstanceOfType(IdentityManager.class);
    // When had resized avatar but hadn't avatar url we build the avatar url
    // then return
    AvatarAttachment avatarAttachment = (AvatarAttachment) profile.getProperty(keyURL);
    if (avatarAttachment != null) {
      avatarUrl = getAvatarImageSource(avatarAttachment);
      profile.setProperty(keyURL, avatarUrl);
      identityManager.saveProfile(profile);
      return avatarUrl;
    }
    // When hadn't avatar yet we return null
    avatarAttachment = (AvatarAttachment) profile.getProperty(Profile.AVATAR);
    if (avatarAttachment == null)
      return null;
    // Otherwise we create the resize avatar then return the avatar url
    InputStream inputStream = new ByteArrayInputStream(avatarAttachment.getImageBytes());
    String mimeType = avatarAttachment.getMimeType();
    AvatarAttachment newAvatarAttachment = ImageUtils.createResizedAvatarAttachment(inputStream,
                                                                                   width,
                                                                                   height,
                                                                                   avatarAttachment.getId()
                                                                                       + postfix,
                                                                                   ImageUtils.buildFileName(avatarAttachment.getFileName(),
                                                                                                           Profile.RESIZED_SUBFIX,
                                                                                                           postfix),
                                                                                   mimeType,
                                                                                   avatarAttachment.getWorkspace());

    if (newAvatarAttachment == null)
      return getAvatarImageSource(profile);
    // Set property that contain resized avatar file to profile
    profile.setProperty(keyFile, newAvatarAttachment);
    identityManager.saveProfile(profile);
    // Build the url to that resized avatar file then save and return
    avatarUrl = getAvatarImageSource(newAvatarAttachment);
    profile.setProperty(keyURL, avatarUrl);
    identityManager.saveProfile(profile);
    return avatarUrl;
  }

  private static String escapeJCRSpecialCharacters(String string) {
    return string.replace("[", "%5B")
                .replace("]", "%5D")
                .replace(":", "%3A");
  }

 private static String getPortalOwner(String portalOwner) {
    if(portalOwner == null || portalOwner.equals("")){
      PortalRequestContext context = Util.getPortalRequestContext();
      portalOwner = context.getPortalOwner();
    }
    return portalOwner;
  }
}