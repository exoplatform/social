/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.webui;

import java.util.List;

import javax.servlet.http.Cookie;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;

/**
 * Contains some common methods for using as utility.<br>
 *
 */
public class Utils {
  /** . */
  public static final String ACTIVITY_STREAM_TAB_SELECTED_COOKIED = "exo_social_activity_stream_tab_selected_%s";
  
  
  /**
   * Gets remote id of owner user (depends on URL: .../remoteId). If owner user is null, return viewer remote id
   *
   * @return remoteId of owner user
   * @since 1.2.0 GA
   */
  public static String getOwnerRemoteId() {
    String currentUserName = URLUtils.getCurrentUser();
    if (currentUserName == null || currentUserName.equals("")) {
      return getViewerRemoteId();
    }
    return currentUserName;
  }

  /**
   * Gets remote id of viewer user.
   *
   * @return remote id
   * @since 1.2.0 GA
   */
  public static String getViewerRemoteId() {
    return RequestContext.getCurrentInstance().getRemoteUser();
  }

  /**
   * Checks if the owner user is the viewer user.
   * 
   * @return true if the viewer user is the same as owner user.
   * @since 1.2.0 GA
   */
  public static boolean isOwner() {
    return Utils.getViewerRemoteId().equals(Utils.getOwnerRemoteId());
  }

  /**
   * Gets identity of owner user.
   *
   * @param loadProfile
   * @return identity
   * @since 1.2.0 GA
   */
  public static Identity getOwnerIdentity(boolean loadProfile) {
    return getUserIdentity(getOwnerRemoteId(), loadProfile);
  }

  /**
   * Gets identity of viewer user (logged-in user).
   *
   * @param loadProfile
   * @return identity
   * @since 1.2.0 GA
   */
  public static Identity getViewerIdentity(boolean loadProfile) {
    return getUserIdentity(getViewerRemoteId(), loadProfile);
  }

  /**
   * Gets identity of owner user. Do not load profile.
   *
   * @return identity
   * @since 1.2.0 GA
   */
  public static Identity getOwnerIdentity() {
    return getUserIdentity(getOwnerRemoteId(), false);
  }

  /**
   * Gets identity of viewer user (logged-in user). Do not load profile.
   *
   * @return identity
   * @since 1.2.0 GA
   */
  public static Identity getViewerIdentity() {
    return getUserIdentity(getViewerRemoteId(), false);
  }

  /**
   * Gets identity from the remote id (user name)
   * 
   * @param userName
   * @param loadProfile
   * @return identity
   * @since 1.2.0 GA
   */
  public static Identity getUserIdentity(String userName, boolean loadProfile) {
    return Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName, loadProfile);
  }

  /**
   * Gets space identity of the owner space (from remote id)
   * 
   * @return space identity
   * @since 1.2.0 GA
   */
  public static Identity getOwnerSpaceIdentity() {
    return Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, getOwnerRemoteId(), true);
  }

  /**
   * Gets list of friends of owner user
   * 
   * @return list of friends
   * @throws Exception
   * @since 1.2.0 GA
   */
  public static List<Identity> getOwnerFriends() throws Exception {
    return Utils.getIdentityManager().getConnections(getOwnerIdentity());
  }

  /**
   * Gets list of friends of viewer user
   * 
   * @return list of friends
   * @throws Exception
   * @since 1.2.0 GA
   */
  public static List<Identity> getViewerFriends() throws Exception {
    return Utils.getIdentityManager().getConnections(getViewerIdentity());
  }

  /**
   * Updates working work space.
   *
   * @since 1.2.0 GA
   */
  public static void updateWorkingWorkSpace() {
    UIWorkingWorkspace uiWorkingWS = Util.getUIPortalApplication().getChildById(UIPortalApplication.UI_WORKING_WS_ID);
    PortalRequestContext pContext = Util.getPortalRequestContext();
    pContext.addUIComponentToUpdateByAjax(uiWorkingWS);
    pContext.setFullRender(true);
  }

  /**
   * Gets activityManager
   * @return activityManager
   * @since 1.2.0 GA
   */
  public static final ActivityManager getActivityManager() {
    return (ActivityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ActivityManager.class);
  }

  /**
   * Gets identityManager
   * @return identityManager
   * @since 1.2.0 GA
   */
  public static final IdentityManager getIdentityManager() {
    return (IdentityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
  }

  /**
   * Gets relationshipManager
   * @return relationshipManager
   * @since 1.2.0 GA
   */
  public static final RelationshipManager getRelationshipManager() {
    return (RelationshipManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RelationshipManager.class);
  }

  /**
   * Gets spaceService
   * @return spaceService
   * @since 1.2.0 GA
   */
  public static final SpaceService getSpaceService() {
    return (SpaceService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);
  }
  
  /**
   * Get the uri.
   * 
   * @param url
   * @return
   * @since 1.2.1
   */
  public static String getURI(String url) {
    RequestContext ctx = RequestContext.getCurrentInstance();
    NodeURL nodeURL =  ctx.createURL(NodeURL.TYPE);
    NavigationResource resource = new NavigationResource(SiteType.PORTAL, Util.getPortalRequestContext().getPortalOwner(), url);
    return nodeURL.setResource(resource).toString(); 
  }
  
  /**
   * Gets selected node name.
   * 
   * @returns
   * @since 1.2.2
   */
  public static String getSelectedNode() {
    PortalRequestContext request = Util.getPortalRequestContext() ;
    return request.getControllerContext().getParameter(QualifiedName.parse("gtn:path"));
  }
  
  /**
   * Get the space url.
   * 
   * @param node
   * @return
   * @since 1.2.1
   */
  public static String getSpaceURL(UserNode node) {
    RequestContext ctx = RequestContext.getCurrentInstance();
    NodeURL nodeURL =  ctx.createURL(NodeURL.TYPE);
    return nodeURL.setNode(node).toString();
  }
  
  /**
   * Gets the space home url of a space.
   * 
   * @param space
   * @return
   * @since 1.2.1
   */
  public static String getSpaceHomeURL(Space space) {
    // work-around for SOC-2366 when rename existing space
    String groupId = space.getGroupId();
    String permanentSpaceName = groupId.split("/")[2];
    
    RequestContext ctx = RequestContext.getCurrentInstance();
    NodeURL nodeURL =  ctx.createURL(NodeURL.TYPE);
    NavigationResource resource = null;
    if (permanentSpaceName.equals(space.getPrettyName())) {
      //work-around for SOC-2366 when delete space after that create new space with the same name
      resource = new NavigationResource(SiteType.GROUP, SpaceUtils.SPACE_GROUP + "/"
                                        + permanentSpaceName, permanentSpaceName);
    } else {
      resource = new NavigationResource(SiteType.GROUP, SpaceUtils.SPACE_GROUP + "/"
                                        + permanentSpaceName, space.getPrettyName());
    }
    
    return nodeURL.setResource(resource).toString(); 
  }
  
  /**
   * 
   * @param value
   */
  public static void setCookies(String key, String value, boolean override) {
    if (override == false) {
      if (hasCookies(key)) return;
    }
    
    if (hasCookies(key)) {
      eraseCookie(key);
    }

    PortalRequestContext request = Util.getPortalRequestContext() ;
    Cookie cookie = new Cookie(key, value);
    cookie.setPath(request.getRequest().getContextPath());
    cookie.setMaxAge(Integer.MAX_VALUE);
    request.getResponse().addCookie(cookie);
  }
  
  public static void eraseCookie(String key) {
    PortalRequestContext request = Util.getPortalRequestContext();
    Cookie[] cookies = request.getRequest().getCookies();
    if (cookies != null) {
      for (int i = 0; i < cookies.length; i++) {
        if (key.equals(cookies[i].getName())) {
          cookies[i].setValue("");
          cookies[i].setPath("/");
          cookies[i].setMaxAge(0);
          request.getResponse().addCookie(cookies[i]);
        }
      }
    }
  }

  /**
   * 
   * @param value
   */
  public static String getCookies(String key) {
    PortalRequestContext request = Util.getPortalRequestContext() ;

    Cookie[] cookies = request.getRequest().getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (key.equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    return null;
  }
  
  public static boolean hasCookies(String key) {
    return (getCookies(key) != null);
  }
  
}
