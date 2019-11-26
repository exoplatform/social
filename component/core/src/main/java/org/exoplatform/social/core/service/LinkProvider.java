/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.Validate;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.model.BannerAttachment;
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
  
  private static Log             LOG = ExoLogger.getLogger(LinkProvider.class);

  private static final  String BASE_URL_SOCIAL_REST_API = "/v1/social";

  public LinkProvider() {
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
    if (ctx != null && space != null) {
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
                .append(StringEscapeUtils.escapeHtml(identity.getProfile().getFullName())).append("</a>").toString();
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
   * Builds a profile URI from userName and portalOwner.
   *
   * @param userName The name of user.
   * @param portalName The name of portal.
   * @param portalOwner The owner of portal (for example, classic or public).
   *        
   * @return The profile URI.
   */
  private static String buildProfileUri(final String userName, final String portalName, String portalOwner) {
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
    return CommonsUtils.getService(IdentityManager.class);
  }
  
  /**
   * Builds the avatar URL for a given profile
   * @param providerId
   * @param remoteId
   * @return
   */
  public static String buildAvatarURL(String providerId, String remoteId) {
    return buildAttachmentUrl(providerId, remoteId, AvatarAttachment.TYPE);
  }

  /**
   * Builds the banner URL for a given profile
   * @param providerId
   * @param remoteId
   * @return
   */
  public static String buildBannerURL(String providerId, String remoteId) {
    return buildAttachmentUrl(providerId, remoteId, BannerAttachment.TYPE);
  }

  private static String buildAttachmentUrl(String providerId, String remoteId, String type) {
    if (providerId == null || remoteId == null) {
      return null;
    }

    String username = remoteId;

    try {
      username = URLEncoder.encode(username, "UTF-8");
    } catch (UnsupportedEncodingException ex) {
      LOG.warn("Failure to encode username for build URL", ex);
    }

    if(providerId.equals(OrganizationIdentityProvider.NAME)) {
      return new StringBuilder("/").append(CommonsUtils.getRestContextName()).append(BASE_URL_SOCIAL_REST_API).append("/users")
              .append("/").append(username)
              .append("/").append(type)
              .toString();
    } else if(providerId.equals(SpaceIdentityProvider.NAME)) {
      return new StringBuilder("/").append(CommonsUtils.getRestContextName()).append(BASE_URL_SOCIAL_REST_API).append("/spaces")
              .append("/").append(username)
              .append("/").append(type)
              .toString();
    }
    return null;
  }

  /**
   * Gets a portal owner. If the parameter is null or "", the method returns a default portal owner.
   *
   * @param portalOwner The owner of portal (for example, classic or public).
   * @return The portal owner.
   */
  private static String getPortalOwner(String portalOwner) {
    if (portalOwner == null || portalOwner.trim().length() == 0) {
      portalOwner = CommonsUtils.getCurrentPortalOwner();
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
