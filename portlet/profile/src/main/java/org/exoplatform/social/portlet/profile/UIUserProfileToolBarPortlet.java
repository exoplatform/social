/*
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
package org.exoplatform.social.portlet.profile;

import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Provides links (account settings, activity link, profile link) for remote user
 *
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class, 
  template = "app:/groovy/portal/webui/component/UIUserProfileToolBarPortlet.gtmpl"
)
public class UIUserProfileToolBarPortlet extends UIPortletApplication {

  /**
   * constructor
   * @throws Exception
   */
  public UIUserProfileToolBarPortlet() throws Exception {
  }
  
  /**
   * Gets user for template to render link corresponding with remote user
   * @return user
   * @throws Exception
   */
  public User getUser() throws Exception {
    OrganizationService service = getApplicationComponent(OrganizationService.class);
    String userName = Util.getPortalRequestContext().getRemoteUser();
    User user = service.getUserHandler().findUserByName(userName);
    return user;
  }
}
