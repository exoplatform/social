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

package social.portal.webui.component.space;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.mop.Visibility;
import org.exoplatform.portal.webui.navigation.PageNavigationUtils;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.resources.ResourceBundleManager;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SAS
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/portal/webui/component/UISocialUserToolBarGroupPortlet.gtmpl")
public class UISocialUserToolBarGroupPortlet extends UIPortletApplication {
  SpaceService spaceService;

  public UISocialUserToolBarGroupPortlet() throws Exception {
  }

  public List<PageNavigation> getGroupNavigations() throws Exception {
    String remoteUser = Util.getPortalRequestContext().getRemoteUser();
    List<PageNavigation> allNavigations = Util.getUIPortalApplication().getNavigations();
    List<PageNavigation> navigations = new ArrayList<PageNavigation>();
    SpaceService spaceSrv = getSpaceService();
    List<Space> spaces = spaceSrv.getAllSpaces();

    for (PageNavigation navigation : allNavigations) {
      if (navigation.getOwnerType().equals(PortalConfig.GROUP_TYPE)) {
        navigations.add(PageNavigationUtils.filter(navigation, remoteUser));
      }
    }

    for (Space space : spaces) {
      for (PageNavigation navigation : navigations) {
        if (navigation.getOwnerId().equals(space.getGroupId())) {
          navigations.remove(navigation);
          break;
        }
      }
    }

    for (PageNavigation navi : navigations) {
      List<PageNode> nodes = navi.getNodes();
      for (PageNode node : nodes) {
        if (!isRender(node)) {
          node.setVisibility(Visibility.HIDDEN);
        }
      }
    }

    return navigations;
  }

  public PageNode getSelectedPageNode() throws Exception {
    return Util.getUIPortal().getSelectedNode();
  }

  protected void localizePageNavigation(PageNavigation nav) {
    Locale locale = Util.getUIPortalApplication().getLocale();
    ResourceBundleManager mgr = getApplicationComponent(ResourceBundleManager.class);
    if (nav.getOwnerType().equals(PortalConfig.USER_TYPE))
      return;
    ResourceBundle res = mgr.getNavigationResourceBundle(locale.getLanguage(),
                                                         nav.getOwnerType(),
                                                         nav.getOwnerId());
    for (PageNode node : nav.getNodes()) {
      resolveLabel(res, node);
    }
  }

  private void resolveLabel(ResourceBundle res, PageNode node) {
    node.setResolvedLabel(res);
    if (node.getChildren() == null)
      return;
    for (PageNode childNode : node.getChildren()) {
      resolveLabel(res, childNode);
    }
  }

  private boolean isRender(PageNode node) throws SpaceException {
    SpaceService spaceSrv = getSpaceService();
    List<Space> spaces = spaceSrv.getAllSpaces();
    for (Space space : spaces) {
      if (space.getName().equals(node.getUri()))
        return false;
    }
    return true;
  }

  private SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = getApplicationComponent(SpaceService.class);
    }
    return spaceService;
  }

}
