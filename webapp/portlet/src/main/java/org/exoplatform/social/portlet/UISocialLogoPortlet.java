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

package org.exoplatform.social.portlet;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.web.CacheUserProfileFilter;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.organization.OrganizationUtils;

@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/social/portlet/UISocialLogoPortlet.gtmpl"
)
public class UISocialLogoPortlet extends UIPortletApplication {
  private Profile                profile;

  private static IdentityManager im;

  public UISocialLogoPortlet() throws Exception {
    // addChild(UILogoEditMode.class, null, null);
  }

  public String getURL() throws Exception {
    String imageSource = getProfile(true).getAvatarUrl();
    if (imageSource == null) {
      imageSource = LinkProvider.PROFILE_DEFAULT_AVATAR_URL;
    }
    return imageSource;
  }

  public String getNavigationTitle() throws Exception {
    UserNode selectedNode = Util.getUIPortal().getSelectedUserNode();
    UserNavigation userNav = selectedNode.getNavigation();
    if (userNav.getKey().getType().equals(PortalConfig.GROUP_TYPE)) {
      return OrganizationUtils.getGroupLabel(userNav.getKey().getName());
    } else if (userNav.getKey().getType().equals(PortalConfig.USER_TYPE)) {
      ConversationState state = ConversationState.getCurrent();
      User user = (User) state.getAttribute(CacheUserProfileFilter.USER_PROFILE);
      return user.getFullName();
    }
    return "";
  }

  /**
   * gets profile with forceReload.
   *
   * @param forceReload
   * @return
   * @throws Exception
   */
  public Profile getProfile(boolean forceReload) throws Exception {
    if (forceReload == true || profile == null) {
      Identity id = getIdentity();
      profile = id.getProfile();
    }
    return profile;
  }

  /**
   * Gets current identity of login user.<br>
   *
   * @return current identity.
   * @throws Exception
   */
  private Identity getIdentity() throws Exception {
    if (im == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    }
    return im.getOrCreateIdentity(OrganizationIdentityProvider.NAME, getCurrentProfileID());
  }

  /**
   * Gets current profile Id.<br>
   *
   * @return id of current profile.
   */
  private String getCurrentProfileID() {
    /*
    String username = URLUtils.getCurrentUser();
    if (username != null) {
      return username;
    }
    */
    // if we are not on the page of a user, we display the profile of the
    // current user
    RequestContext context = RequestContext.getCurrentInstance();
    return context.getRemoteUser();
  }
}
