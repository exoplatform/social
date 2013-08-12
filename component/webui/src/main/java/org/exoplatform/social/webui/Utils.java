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

import java.util.Calendar;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.application.RequestNavigationData;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.social.common.router.ExoRouter;
import org.exoplatform.social.common.router.ExoRouter.Route;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.web.url.navigation.NavigationResource;
import org.exoplatform.web.url.navigation.NodeURL;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;

/**
 * Contains some common methods for using as utility.<br>
 *
 */
public class Utils {
  /** . */
  public static final String ACTIVITY_STREAM_TAB_SELECTED_COOKIED = "exo_social_activity_stream_tab_selected_%s";
  public static final String ACTIVITY_STREAM_VISITED_PREFIX_COOKIED = "exo_social_activity_stream_%s_visited_%s_%s";
  public static final String LAST_UPDATED_ACTIVITIES_NUM = "exo_social_last_updated_activities_num_on_%s_of_%s";
  public static final String FROM = "from";
  public static final String OLD_FROM = "old_from";
  public static final String TO = "to";
  private static final String   HOME = "home";
  
  /** . */
  public static final String NOT_SEEN_ACTIVITIES_COOKIES = "exo_social_not_seen_activities_%s";
  public static final String SEEN_ACTIVITIES_COOKIES = "exo_social_seen_activities_%s";
  
  /** */
  private static RequestNavInfo lastRequestNavData = null;
  
  /** */
  private static RequestNavInfo currentRequestNavData = null;
  
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
   * @param identity
   * @return
   * @throws Exception
   */
  public static final Relationship getRelationship(Identity identity) throws Exception {
    if (identity.equals(getViewerIdentity())) {
      return null;
    }
    return getRelationshipManager().get(identity, getViewerIdentity());
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
  public static void setCookies(String key, String value) {
    //
    removeCookie(key);

    //
    PortalRequestContext request = Util.getPortalRequestContext() ;
    Cookie cookie = new Cookie(key, value);
    cookie.setPath(request.getRequest().getContextPath());
    cookie.setMaxAge(Integer.MAX_VALUE);
    request.getResponse().addCookie(cookie);
  }
  
  private static Cookie[] removeCookie(String key) {
    PortalRequestContext request = Util.getPortalRequestContext();
    Cookie[] cookies = request.getRequest().getCookies();
    if (cookies != null) {
      int found = -1;
      for (int i = 0; i < cookies.length; i++) {
        if (key.equals(cookies[i].getName())) {
          found = i;
          break;
        }
      }
      if (found > -1) {
        ArrayUtils.remove(cookies, found);
      }
      
    }
    return cookies;
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
  
  public static long getLastVisited(String key, String mode) {
    long currentVisited = Calendar.getInstance().getTimeInMillis();
    String cookieKey = String.format(Utils.ACTIVITY_STREAM_VISITED_PREFIX_COOKIED, mode, Utils.getViewerRemoteId(), key);
    String strValue = Utils.getCookies(cookieKey);
    if(strValue == null) {
      return currentVisited;
    }
    
    return Long.parseLong(strValue);
  }
  
  private static String getCookieValue(String key, String mode) {
    long currentVisited = Calendar.getInstance().getTimeInMillis();
    String cookieKey = String.format(Utils.ACTIVITY_STREAM_VISITED_PREFIX_COOKIED, mode, Utils.getViewerRemoteId(), key);
    String strValue = Utils.getCookies(cookieKey);
    if(strValue == null) {
      return "" + currentVisited;
    }
    
    return strValue;
  }
  
  
  public static void setLastVisited(String mode) {
    String gotTo = getCookieValue(TO, mode);
    String gotFrom = getCookieValue(FROM, mode);
    
    //
    setCookie(OLD_FROM, mode, gotFrom);
    
    //
    setCookie(FROM, mode, gotTo);
    
    //
    long nextTo = Calendar.getInstance().getTimeInMillis();
    setCookie(TO, mode, "" + nextTo);
  }
  
  private static void setCookie(String key, String mode, String value) {
    String cookieKey = String.format(Utils.ACTIVITY_STREAM_VISITED_PREFIX_COOKIED, mode, Utils.getViewerRemoteId(), key);
    Utils.setCookies(cookieKey, value);
  }
  
  public static String listToString(List<String> list, String separator) {
    StringBuilder sb = new StringBuilder();
    int lastIdx = 0;
    for (String s : list) {
        if ( s == null || s.length() == 0 ) {
          continue;
        }
        lastIdx += 1;
        sb.append(s);
        if ( lastIdx < list.size() ) {
          sb.append(separator);
        }
    }
    return sb.toString();
  }
  
  /**
   * 
   * @param requestNavData
   */
  public static void setCurrentNavigationData(String siteType, String siteName, String path) {
    lastRequestNavData = currentRequestNavData;
    currentRequestNavData = new RequestNavInfo(siteType, siteName, path);
    
  }

  /**
   * Checks the page in refresh context or switch from other one to it.
   * 
   * @return IF refresh TRUE; Otherwise FALSE
   * 
   */
  public static boolean isRefreshPage() {
    
    if (lastRequestNavData == null || currentRequestNavData == null) {
      return false;
    }
    
    return lastRequestNavData.equals(currentRequestNavData);
  }
  
  /**
   * Determines current displayed page is Home or not base on selected node.
   * 
   * @return
   */
  public static boolean isHomePage() {
    String selectedNode = Utils.getSelectedNode(); 
    return ( selectedNode == null || selectedNode.length() == 0 || HOME.equals(selectedNode));  
  }
  
  /**
   * Truncates large Strings showing a portion of the String's head and tail
   * with the center cut out and replaced with '...'. Also displays the total
   * length of the truncated string so size of '...' can be interpreted.
   * Useful for large strings in UIs or hex dumps to log files.
   * 
   * @param str
   *            the string to truncate
   * @param head
   *            the amount of the head to display
   * @param tail
   *            the amount of the tail to display
   * @return the center truncated string
   */
  public static final String centerTrunc( String str, int head, int tail ) {
      StringBuffer buf = null;

      // Return as-is if String is smaller than or equal to the head plus the
      // tail plus the number of characters added to the trunc representation
      // plus the number of digits in the string length.
      if ( str.length() <= ( head + tail + 7 + str.length() / 10 ) )
      {
          return str;
      }

      buf = new StringBuffer();
      buf.append( str.substring( 0, head ) ).append( "..." );
      buf.append( str.substring( str.length() - tail ) );
      return buf.toString();
  }
  
  /**
   * Truncates large Strings showing a portion of the String's head and tail
   * with the head cut out and replaced with '...'.
   * 
   * @param str
   *            the string to truncate
   * @param head
   *            the amount of the head to display
   * @return the head truncated string
   */
  public static final String trunc( String str, int head) {
      StringBuffer buf = null;

      // Return as-is if String is smaller than or equal to the head plus the
      // tail plus the number of characters added to the trunc representation
      // plus the number of digits in the string length.
      if ( str.length() <= ( head + 7 + str.length() / 10 ) )
      {
          return str;
      }

      buf = new StringBuffer();
      buf.append( str.substring( 0, head ) ).append( "..." );
      return buf.toString();
  }
  
  /**
   * Trim space characters at the beginning and end of string. Replace multiple spaces by a single space character.
   * 
   * @param str
   * @return
   * @since 4.0.0-RC1
   */
  public static String normalizeString(String str) {
    if(str != null) {
      return str.trim().replaceAll("(\\s){2,}"," ");
    }
    return null;
  }
  
  /**
   * Check whether is being in a space context or not.
   * 
   * @return
   * @since 4.0.0-RC2
   */
  public static boolean isSpaceContext() {
    return (getSpaceByContext() != null);
  }

  /**
   * Gets the space url based on the current context.
   * 
   * @return
   * @since 4.0.0-RC2
   */
  public static String getSpaceUrlByContext() {
    Space space = getSpaceByContext();
    return (space != null ? space.getUrl() : null);
  }

  /**
   * Resizes the height of Home page.
   * 
   * @since 4.0.1-GA
   */
  public static void resizeHomePage() {
    PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    JavascriptManager jm = pContext.getJavascriptManager();

    StringBuilder script = new StringBuilder("setTimeout(function() {")
      .append("jq('.LeftNavigationTDContainer:first').css('height', 'auto');")
      .append("jq('#UIUserActivityStreamPortlet').css('height', 'auto');")
      .append("platformLeftNavigation.resize();")
      .append("}, 200);");
    
    jm.require("SHARED/jquery", "jq")
      .require("SHARED/platform-left-navigation", "platformLeftNavigation")
      .addScripts(script.toString());
  }
  
  /**
   * Initializes user profile popup.
   * 
   * @param uiActivityId Id of activity component.
   * @since 4.0.0-GA
   */
  public static void initUserProfilePopup(String uiActivityId) {
    PortletRequestContext pContext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    JavascriptManager jm = pContext.getJavascriptManager();
    jm.require("SHARED/social-ui-profile", "profile")
      .addScripts("profile.initUserProfilePopup('" + uiActivityId + "', null);");
  }
  
  private static Space getSpaceByContext() {
    //
    SpaceService spaceService = (SpaceService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);
    PortalRequestContext pcontext = Util.getPortalRequestContext();
    String requestPath = pcontext.getControllerContext().getParameter(RequestNavigationData.REQUEST_PATH);
    Route route = ExoRouter.route(requestPath);
    
    if (route == null) {
      String groupId = pcontext.getControllerContext().getParameter(RequestNavigationData.REQUEST_SITE_NAME);
      return spaceService.getSpaceByGroupId(groupId);
    }

    //
    String spacePrettyName = route.localArgs.get("spacePrettyName");
    Space space = spaceService.getSpaceByPrettyName(spacePrettyName);
    if (space == null) {
      String groupId = String.format("%s/%s", SpaceUtils.SPACE_GROUP, spacePrettyName);
      space = spaceService.getSpaceByGroupId(groupId); 
    }
     
    
    return space;
  }
}
