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
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;

/**
 * Builds and provides default links and links of users, spaces and activities.
 * Links are built basing on provided information as name or Id of the target user or space.
 * In case something is wrong when building, the default links will be returned.
 */
public class LinkProvider {
  public static final String RESOURCE_URL = "/social-resources";
  public static final String JAVASCRIPT_RESOURCE_URL = RESOURCE_URL + "/javascript/";
  public static final String PROFILE_DEFAULT_AVATAR_URL = "/eXoSkin/skin/images/system/UserAvtDefault.png";
  public static final String SPACE_DEFAULT_AVATAR_URL = "/eXoSkin/skin/images/system/SpaceAvtDefault.png";
  public static final String HAS_CONNECTION_ICON =
          RESOURCE_URL + "/eXoSkin/skin/images/themes/default/social/skin/UIManageUsers/StatusIcon.png";
  public static final String WAITING_CONFIRMATION_ICON =
          RESOURCE_URL + "/eXoSkin/skin/images/themes/default/social/skin/UIManageUsers/WaittingConfirm.png";
  public static final String SPACE_MANAGER_ICON =
          RESOURCE_URL + "/eXoSkin/skin/images/themes/default/social/skin/UIManageSpaces/Manager.png";
  public static final String SPACE_MEMBER_ICON =
          RESOURCE_URL + "/eXoSkin/skin/images/themes/default/social/skin/UIManageSpaces/Member.png";
  public static final String SPACE_WAITING_CONFIRM_ICON =
          RESOURCE_URL + "/eXoSkin/skin/images/themes/default/social/skin/UIManageSpaces/WaitingConfirm.png";
  public static final String STARTER_ACTIVITY_AVATAR = "/eXoSkin/skin/images/themes/default/social/skin/Activity/starterAvt.png";
  public static final String STARTER_ACTIVITY_IMAGE = "/eXoSkin/skin/images/themes/default/social/skin/Activity/starterActImg.png";

  public static final String ROUTE_DELIMITER = "/";
  
  private static IdentityManager identityManager;
  private static Log             LOG = ExoLogger.getLogger(LinkProvider.class);

  /**
   * Hacks for unit test to work.
   */
  public static String DEFAULT_PORTAL_OWNER = "classic";

  /**
   * Constructor with parameter to inject the default portal owner name.
   * @param params The params got from configuration.
   * 
   * @since 1.2.0 GA
   */
  public LinkProvider(InitParams params){
    if(params.getValueParam("predefinedOwner") != null) DEFAULT_PORTAL_OWNER = params.getValueParam("predefinedOwner").getValue();
  }

  /**
   * Gets URI to a space profile by its pretty name.
   *
   * @param prettyName The pretty name of space.
   * @return The URI.
   * @LevelAPI Platform
   * @since 1.2.0 GA
   */
  public static String getSpaceUri(final String prettyName) {
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    Space space = spaceService.getSpaceByPrettyName(prettyName);
    RequestContext ctx = RequestContext.getCurrentInstance();
    if (ctx != null) {
      NodeURL nodeURL = ctx.createURL(NodeURL.TYPE);
      NavigationResource resource = new NavigationResource(SiteType.GROUP, space.getGroupId(), space.getUrl());
      return nodeURL.setResource(resource).toString();
    } else {
      return null;
    }
  }

  /**
   * Gets URI to a user profile by username.
   *
   * @param username The name of user (remoteId).
   * @return The URI.
   * @LevelAPI Platform
   */
  public static String getProfileUri(final String username) {
    return buildProfileUri(username, null, null);
  }

  /**
   * Gets URI to a user profile by username and owner portal.
   *
   * @param username The name of user (remoteId).
   * @param portalOwner The portal owner (for example, classic or public).
   * @return The URI.
   * @LevelAPI Platform
   */
  public static String getProfileUri(final String username, final String portalOwner) {
    return buildProfileUri(username, null, portalOwner);
  }

  /**
   * Gets a link to the user profile.
   *
   * @param username The name of user (remoteId).
   * @return The link.
   * @LevelAPI Platform
   */
  public static String getProfileLink(final String username) {
    return getProfileLink(username, null);
  }

  /**
   * Gets a link to the user profile on a portal.
   *
   * @param username The name of user (remoteId).
   * @param portalOwner The portal owner (for example, classic or public).
   * @return The link.
   * @LevelAPI Platform
   */
  public static String getProfileLink(final String username, final String portalOwner) {
    Identity identity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, username, true);
    Validate.notNull(identity, "Identity must not be null.");

    //
    String configured_domain_url = null;
    try {
      configured_domain_url = CommonsUtils.getCurrentDomain();
    } catch (NullPointerException e) {
      configured_domain_url = null;
    }

    return new StringBuilder("<a href=\"").append((configured_domain_url != null) ? configured_domain_url : "")
                .append(buildProfileUri(identity.getRemoteId(), null, portalOwner)).append("\" target=\"_parent\">")
                .append(identity.getProfile().getFullName()).append("</a>").toString();
  }

  /**
   * Gets an absolute profile URL of a user.
   *
   * @param userName The name of user (remoteId).
   * @param portalName The name of current portal.
   * @param portalOwner The portal owner (for example, classic or public).
   * @param host The name of the provided host.
   * @return The absolute profile URL.
   * @LevelAPI Platform
   */
  public static String getAbsoluteProfileUrl(final String userName, final String portalName,
                                             final String portalOwner, final String host) {
    return host + buildProfileUri(userName, portalName, portalOwner);
  }

  /**
   * Gets the activity URI of a user.
   * 
   * @param remoteId Name of the user, for example root.
   * @return The activity link.
   * @LevelAPI Platform
   */
  public static String getUserActivityUri(final String remoteId) {
    return getActivityUri(OrganizationIdentityProvider.NAME,remoteId);
  }

  /**
   * Gets URI to connections of a user and all people.
   * 
   * @param remoteId Name of the user (remoteId), for example root.
   * @return The URI.
   * @LevelAPI Platform
   */
  public static String getUserConnectionsUri(final String remoteId) {
    return getBaseUri(null, null) + "/connections/all-people" + ROUTE_DELIMITER + remoteId;
  }
  
  /**
   * Gets URI to connections of a user.
   * 
   * @param remoteId The name of user (remoteId), for example root.
   * @return The link to network of provided user who has connection with the current user. 
   */
  public static String getUserConnectionsYoursUri(final String remoteId) {
    return getBaseUri(null, null) + "/connections/network" + ROUTE_DELIMITER + remoteId;
  }
  
  /**
   * Gets URI to a user profile.
   *
   * @param remoteId The name of user (remoteId), for example root.
   * @return The link to profile of provided user.
   * @LevelAPI Platform
   */
  public static String getUserProfileUri(final String remoteId) {
    return getBaseUri(null, null) + "/profile" + ROUTE_DELIMITER + remoteId;
  }

  /**
   * Gets URI to an activity stream of space or user.
   *
   * @param providerId The provider information.
   * @param remoteId Id of the target identity, for example organization:root or space:abc_def.
   * @return The link to activity of provided user on the provided provider.
   * @LevelAPI Platform
   */
  public static String getActivityUri(final String providerId, final String remoteId) {
    final String prefix = getBaseUri(null, null) + "/";
    if (providerId.equals(OrganizationIdentityProvider.NAME)) {
      return String.format("%sactivities/%s",prefix,remoteId);
    } else if (providerId.equals(SpaceIdentityProvider.NAME)) {
      return String.format("/%s/g/:spaces:%s/%s",getPortalName(null),remoteId,remoteId);
    } else {
      LOG.warn("Failed to getActivityLink with providerId: " + providerId);
    }
    return null;
  }
  
  /**
   * @param activityId
   * @return
   */
  public static String getSingleActivityUrl(String activityId) {
    return getBaseUri(null, null) + "/activity?id=" + activityId;
  }

  /**
   * Gets an activity URI of the space.
   *
   * @param remoteId The Id of target space.
   * @param groupId The group Id of target space.
   * @return The URI.
   * @LevelAPI Platform
   * @since 1.2.8
   */
  public static String getActivityUriForSpace(final String remoteId, final String groupId) {
    return String.format("/%s/g/:spaces:%s/%s", getPortalName(null), groupId, remoteId);
  }
  
  /**
   * Gets URI to the provided space's avatar.
   *
   * @param space The target object to get its avatar based on the attachment information.
   * @return The URI.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  public static String buildAvatarImageUri(final Space space) {
    return buildAvatarImageUri(space.getAvatarAttachment());
  }

  /**
   * Gets URI to an avatar from the identity name.
   *
   * @param identityName The name of target identity to build the URL link to the avatar.
   * @return Link to avatar of the target provided identity name.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  public static String buildAvatarImageUri(final String identityName) {
    return String.format(
        "/rest/jcr/repository/social/production/soc:providers/soc:space/soc:%s/soc:profile/soc:avatar",
        identityName);
  }

  /**
   * Escapes the JCR special characters.
   *
   * @param string The set of characters to be escaped.
   * @return Null if the string value is null; or a set of corresponding characters are returned after they have been escaped.
   * @LevelAPI Platform
   */
  public static String escapeJCRSpecialCharacters(String string) {
    if (string == null) {
      return null;
    }
    return string.replace("[", "%5B").replace("]", "%5D").replace(":", "%3A");
  }
  
  /**
   * Gets URL to the profile's avatar image in a portalContainer.
   *
   * @param profile The user profile.
   * @param portalContainer The portal container.
   * @return Null or the URL.
   * @LevelAPI Provisional
   * @deprecated use {@link Profile#getAvatarUrl()}. Will be removed by 4.0.x.
   */
  public static String getAvatarImageSource(final PortalContainer portalContainer, final Profile profile) {
    final AvatarAttachment avatarAttachment = (AvatarAttachment) profile.getProperty(Profile.AVATAR);
    if (avatarAttachment != null) {
      return buildAvatarImageUri(avatarAttachment);
    }
    return null;
  }
  
  /**
   * Gets URL to the profile's avatar image.
   *
   * @param profile The user profile.
   * @return Null or the URL.
   * @LevelAPI Provisional
   * @deprecated use {@link Profile#getAvatarUrl()}. Will be removed by 4.0.x.
   */
  public static String getAvatarImageSource(final Profile profile) {
    String avatarUrl = profile.getAvatarUrl();
    if (avatarUrl != null) {
      return avatarUrl;
    }

    final AvatarAttachment avatarAttachment = (AvatarAttachment) profile.getProperty(Profile.AVATAR);
    if (avatarAttachment != null) {
      avatarUrl = buildAvatarImageUri(avatarAttachment);
      profile.setAvatarUrl(avatarUrl);
      getIdentityManager().saveProfile(profile);
      return avatarUrl;
    }
    return null;
  }

  /**
   * Builds URI to the avatar image from avatarAttachment.
   *
   * @param avatarAttachment The object which stores information of the avatar image.
   * @return The URI.
   */
  public static String buildAvatarImageUri(final AvatarAttachment avatarAttachment) {
    String avatarUrl = null;
    try {
      if (avatarAttachment != null) {
        final String repository = CommonsUtils.getRepository().getConfiguration().getName();
        //
        avatarUrl = escapeJCRSpecialCharacters(new StringBuilder("/").append(CommonsUtils.getRestContextName())
                                                                     .append("/jcr/")
                                                                     .append(repository)
                                                                     .append("/")
                                                                     .append(avatarAttachment.getWorkspace())
                                                                     .append(avatarAttachment.getDataPath())
                                                                     .append("/?upd=")
                                                                     .append(avatarAttachment.getLastModified())
                                                                     .toString());
      }
    } catch (Exception e) {
      LOG.warn("Failed to build avatar url from avatar attachment for: " + e.getMessage());
    }
    return avatarUrl;
  }

  /**
   * Builds a profile URI from userName and portalOwner.
   *
   * @param userName The name of user.
   * @param portalName The name of portal.
   * @param portalOwner The owner of portal (for example, classic or public).
   *        
   * @return The profile URI.
   */
  private static String buildProfileUri(final String userName, final String portalName, String portalOwner) {
    if(portalOwner == null || portalOwner.trim().length() == 0) portalOwner = DEFAULT_PORTAL_OWNER;
    return getBaseUri(portalName, portalOwner) + "/profile" + ROUTE_DELIMITER + userName;
  }

  /**
   * Builds a profile URI from userName and portalName and portalOwner.
   *
   * @param portalName The name of portal.
   * @param portalOwner The owner of portal (for example, classic or public).
   *        
   * @return The profile URI.
   */
  private static String getBaseUri(final String portalName, String portalOwner) {
    return "/" + getPortalName(portalName) + "/" + getPortalOwner(portalOwner);
  }

  private static String getSpaceBaseUri(final String portalName, String portalOwner) {
    return "/" + getPortalName(portalName);
  }

  /**
   * Gets the link of notification settings page
   * 
   * @param remoteId
   * @return
   */
  public static String getUserNotificationSettingUri(final String remoteId) {
    return getBaseUri(null, null) + "/notifications" + ROUTE_DELIMITER + remoteId;
  }
  
  /**
   * Gets the link of all spaces page
   *
   * @return
   */
  public static String getRedirectUri(String type) {
    if (type.isEmpty()) {
      return getBaseUri(null, null);
    }
    return getBaseUri(null, null) + "/" + type;
  }

  public static String getRedirectSpaceUri(String type) {
    if (type.isEmpty()) {
      return getSpaceBaseUri(null, null);
    }
    return getSpaceBaseUri(null, null) + "/" + type;
  }

  /**
   * Gets an IdentityManager instance.
   *
   * @return The IdentityManager.
   */
  public static IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = CommonsUtils.getService(IdentityManager.class);
    }
    return LinkProvider.identityManager;
  }

  /**
   * Gets a portal owner. If the parameter is null or "", the method returns a default portal owner.
   *
   * @param portalOwner The owner of portal (for example, classic or public).
   * @return The portal owner.
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
   * Gets a portal name. If the parameter is null or "", the method returns a default portal name.
   * 
   * @param portalName The name of portal.
   * @return The portal name.
   */
  private static String getPortalName(String portalName) {
    if (portalName == null || portalName.trim().length() == 0) {
      return PortalContainer.getCurrentPortalContainerName();
    }
    return portalName;
  }
}
