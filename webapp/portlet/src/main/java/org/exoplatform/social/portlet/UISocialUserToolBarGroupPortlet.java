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

import java.util.LinkedList;
import java.util.List;

import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/social/portlet/UISocialUserToolBarGroupPortlet.gtmpl")
public class UISocialUserToolBarGroupPortlet extends UIPortletApplication {

  public UISocialUserToolBarGroupPortlet() throws Exception {
  }

  /**
   * Retrieving all of UserNavigation.
   * @return
   * @throws Exception
   */
  public List<UserNavigation> getGroupNavigations() throws Exception {
    UserPortal userPortal = SpaceUtils.getUserPortal();
    List<UserNavigation> allNavs = userPortal.getNavigations();

    List<UserNavigation> groupNav = new LinkedList<UserNavigation>();
    for (UserNavigation nav : allNavs) {
      if (nav.getKey().getType().equals(SiteType.GROUP)) {
        groupNav.add(nav);
      }
    }
    return groupNav;
  }

  /**
   * Retrieving the UserNode which is selected.
   * @return
   * @throws Exception
   */
  public UserNode getSelectedPageNode() throws Exception {
    return Util.getUIPortal().getSelectedUserNode();
  }
 
}
