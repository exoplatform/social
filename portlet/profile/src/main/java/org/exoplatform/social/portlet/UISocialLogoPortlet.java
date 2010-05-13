/**
 * Copyright (C) 2009 eXo Platform SAS.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.social.portlet;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.ProfileAttachment;
import org.exoplatform.social.webui.URLUtils;
import org.exoplatform.web.CacheUserProfileFilter;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.organization.OrganizationUtils;

import javax.portlet.PortletPreferences;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform October 2, 2009
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/portal/webui/component/UISocialLogoPortlet.gtmpl"
)
public class UISocialLogoPortlet extends UIPortletApplication {
  private Profile                profile;

  private static IdentityManager im;

  public UISocialLogoPortlet() throws Exception {
    // addChild(UILogoEditMode.class, null, null);
  }

  public String getURL() {
    PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences pref = pcontext.getRequest().getPreferences();
    String imageSource = null;
    try {
      imageSource = getImageSource();
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (imageSource == null) {
      return pref.getValue("url", ""); // url
      // /eXoResourcesSocial/skin/ShareImages/Avartar.gif
    } else {
      return imageSource;
    }
  }

  public String getNavigationTitle() throws Exception {
    PageNavigation navigation = Util.getUIPortal().getSelectedNavigation();
    if (navigation.getOwnerType().equals(PortalConfig.GROUP_TYPE)) {
      return OrganizationUtils.getGroupLabel(navigation.getOwnerId());
    } else if (navigation.getOwnerType().equals(PortalConfig.USER_TYPE)) {
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
   * Gets the source of image.
   * 
   * @return imageSource link
   */
  protected String getImageSource() throws Exception {
    Profile p = getProfile(true);
    ProfileAttachment att = (ProfileAttachment) p.getProperty(Profile.AVATAR);
    if (att != null) {
      return "/" + getRestContext() + "/jcr/" + getRepository() + "/" + att.getWorkspace()
          + att.getDataPath() + "/?rnd=" + System.currentTimeMillis();
    }
    return null;
  }

  /**
   * Gets the current repository.<br>
   * 
   * @return current repository through repository service.
   * @throws Exception
   */
  private String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class);
    return rService.getCurrentRepository().getConfiguration().getName();
  }

  /**
   * Gets the rest context.
   * 
   * @return the rest context
   */
  private String getRestContext() {
    return PortalContainer.getInstance().getRestContextName();
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
    String username = URLUtils.getCurrentUser();
    if (username != null) {
      return username;
    }
    // if we are not on the page of a user, we display the profile of the
    // current user
    RequestContext context = RequestContext.getCurrentInstance();
    return context.getRemoteUser();
  }
}
