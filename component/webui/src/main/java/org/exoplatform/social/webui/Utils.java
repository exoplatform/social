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

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.application.RequestContext;

/**
 * Contains some common methods for using as utility.<br>
 *
 */
public class Utils {
  /**
   * Gets remote id of owner user (depends on URL: .../remoteId). If owner user is null, return viewer remote id
   *
   * @return remoteId of owner user
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
   */
  public static String getViewerRemoteId() {
    return RequestContext.getCurrentInstance().getRemoteUser();
  }

  /**
   * Checks if the owner user is the viewer user.
   * 
   * @return true if the viewer user is the same as owner user.
   */
  public static boolean isOwner() {
    return Utils.getViewerRemoteId().equals(Utils.getOwnerRemoteId());
  }

  /**
   * Gets identity of owner user.
   *
   * @param loadProfile
   * @return identity
   */
  public static Identity getOwnerIdentity(boolean loadProfile) {
    return getUserIdentity(getOwnerRemoteId(), loadProfile);
  }

  /**
   * Gets identity of viewer user (logged-in user).
   *
   * @param loadProfile
   * @return identity
   */
  public static Identity getViewerIdentity(boolean loadProfile) {
    return getUserIdentity(getViewerRemoteId(), loadProfile);
  }

  /**
   * Gets identity of owner user. Do not load profile.
   *
   * @return identity
   */
  public static Identity getOwnerIdentity() {
    return getUserIdentity(getOwnerRemoteId(), false);
  }

  /**
   * Gets identity of viewer user (logged-in user). Do not load profile.
   *
   * @return identity
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
   */
  public static Identity getUserIdentity(String userName, boolean loadProfile) {
    return Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName, loadProfile);
  }

  /**
   * Gets space identity of the owner space (from remote id)
   * 
   * @return space identity
   */
  public static Identity getOwnerSpaceIdentity() {
    return Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, getOwnerRemoteId(), true);
  }

  /**
   * Gets list of friends of owner user
   * 
   * @return list of friends
   * @throws Exception
   */
  public static List<Identity> getOwnerFriends() throws Exception {
    return Utils.getIdentityManager().getConnections(getOwnerIdentity());
  }

  /**
   * Gets list of friends of viewer user
   * 
   * @return list of friends
   * @throws Exception
   */
  public static List<Identity> getViewerFriends() throws Exception {
    return Utils.getIdentityManager().getConnections(getViewerIdentity());
  }

  /**
   * Updates working work space.
   *
   */
  public static void updateWorkingWorkSpace() {
    UIWorkingWorkspace uiWorkingWS = Util.getUIPortalApplication().getChildById(UIPortalApplication.UI_WORKING_WS_ID);
    PortalRequestContext pContext = Util.getPortalRequestContext();
    pContext.addUIComponentToUpdateByAjax(uiWorkingWS);
    pContext.setFullRender(true);
  }

  /**
   * Gets activityManager
   * @return
   */
  public static final ActivityManager getActivityManager() {
    return (ActivityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ActivityManager.class);
  }

  /**
   * Gets identityManager
   * @return
   */
  public static final IdentityManager getIdentityManager() {
    return (IdentityManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityManager.class);
  }

  /**
   * Gets relationshipManager
   * @return
   */
  public static final RelationshipManager getRelationshipManager() {
    return (RelationshipManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RelationshipManager.class);
  }

  /**
   * Gets SpaceService
   * @return
   */
  public static final SpaceService getSpaceService() {
    return (SpaceService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SpaceService.class);
  }

  /**
   * Gets the current portal name.
   *
   * @return name of current portal.
   *
   */
  public static final String getCurrentPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance();
    return pcontainer.getPortalContainerInfo().getContainerName();
  }

  /**
   * Gets the current repository name.
   *
   * @return current repository through repository service.
   *
   * @throws Exception
   */
  public static final String getCurrentRepositoryName() throws Exception {
    RepositoryService rService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class) ;
    return rService.getCurrentRepository().getConfiguration().getName() ;
  }

  /**
   * Gets the rest context.
   *
   * @return the rest context
   */
  public static final String getRestContextName() {
     return PortalContainer.getInstance().getRestContextName();
   }
}
