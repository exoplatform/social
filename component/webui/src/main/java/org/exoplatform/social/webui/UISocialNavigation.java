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
package org.exoplatform.social.webui;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.WindowState;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.page.UIPageBody;
import org.exoplatform.portal.webui.portal.PageNodeEvent;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;


public class UISocialNavigation extends UIComponent {
  private boolean    useAJAX = true;

  protected UserNode selectedNode_;

  protected Object   selectedParent_;

  /**
   * Gets viewModeUIComponent
   * 
   * @return viewModeUIComponent
   */
  public UIComponent getViewModeUIComponent() {
    return null;
  }

  /**
   * Sets useAjax
   * 
   * @param bl true or false
   */
  public void setUseAjax(boolean bl) {
    useAJAX = bl;
  }

  /**
   * Checks if use ajax or not
   * 
   * @return true or false
   */
  public boolean isUseAjax() {
    return useAJAX;
  }

  /**
   * Gets navigation page list
   * 
   * @return navigation page list
   * @throws Exception
   */
  public List<UserNavigation> getNavigations() throws Exception {
    List<UserNavigation> result = new ArrayList<UserNavigation>();
    List<UserNavigation> navigations = SpaceUtils.getUserPortal().getNavigations();

    PortalRequestContext portalRequest = Util.getPortalRequestContext();
    for (UserNavigation userNavigation : navigations) {
      if (userNavigation.getKey().getName().equals(portalRequest.getPortalOwner())
          && userNavigation.getKey().getTypeName().equals("portal")) {
        result.add(userNavigation);
      }
    }
    return result;
  }

  /**
   * gets selected navigation page
   * 
   * @return selected navigation page
   * @throws Exception 
   */
  public UserNavigation getSelectedNavigation() throws Exception {
    UIPortal uiPortal = Util.getUIPortal();
    UserNode selectedNode = uiPortal.getSelectedUserNode();
    return selectedNode.getNavigation();
  }

  /**
   * Gets selected parent
   * 
   * @return selected parent
   */
  public Object getSelectedParent() {
    return selectedParent_;
  }

  /**
   * Gets selected page node
   * 
   * @return selected page node
   * @throws Exception 
   */
  public UserNode getSelectedPageNode() throws Exception {
    if (selectedNode_ == null) {
      selectedNode_ = Util.getUIPortal().getSelectedUserNode();
    }
    return selectedNode_;
  }

  /**
   * Checks if a node is a selected node
   * 
   * @param node
   * @return true or false
   */
  public boolean isSelectedNode(UserNode node) {
    if (selectedNode_ != null && node.getURI().equals(selectedNode_.getURI())) {
      return true;
    }
    if (selectedParent_ != null && selectedParent_ instanceof PageNode) {
      return node.getURI().equals(((UserNode) selectedParent_).getURI());
    }
    return false;
  }

  public void processRender(WebuiRequestContext context) throws Exception {
    UIPortal uiPortal = Util.getUIPortal();
    if (uiPortal.getSelectedUserNode() != selectedNode_) {
      setSelectedPageNode(uiPortal.getSelectedUserNode());
    }
    super.processRender(context);
  }

  /**
   * sets selected page node
   * 
   * @param selectedNode
   * @throws Exception
   */
  private void setSelectedPageNode(UserNode selectedNode) throws Exception {
    UserPortal userPortal = SpaceUtils.getUserPortal();
    selectedNode_ = selectedNode;
    selectedParent_ = null;
    String seletctUri = selectedNode.getURI();
    int index = seletctUri.lastIndexOf("/");
    String parentUri = null;
    if (index > 0)
      parentUri = seletctUri.substring(0, seletctUri.lastIndexOf("/"));
    List<UserNavigation> pageNavs = getNavigations();
    for (UserNavigation pageNav : pageNavs) {
      if (userPortal.resolvePath(pageNav, null, selectedNode.getURI()) != null) {
        if (parentUri == null || parentUri.length() < 1)
          selectedParent_ = pageNav;
        else
          selectedParent_ = userPortal.resolvePath(pageNav, null, parentUri);
        break;
      }
    }
  }

  /**
   * triggers this action when user click on select node event link
   * 
   * @author hoatle
   */
  public static class SelectNodeActionListener extends EventListener<UISocialNavigation> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UISocialNavigation> event) throws Exception {
      UserPortal userPortal = Util.getUIPortalApplication().getUserPortalConfig().getUserPortal();
      UISocialNavigation uiNavigation = event.getSource();
      UIPortal uiPortal = Util.getUIPortal();
      String uri = event.getRequestContext().getRequestParameter(OBJECTID);
      int index = uri.lastIndexOf("::");
      String id = uri.substring(index + 2);
      UserNavigation selectNav = null;
      if (index <= 0) {
        UserNode selectedNode = uiPortal.getSelectedUserNode();
        selectNav = selectedNode.getNavigation();
      } else {
        String navId = uri.substring(0, index);
      }
      UserNode selectNode = userPortal.resolvePath(selectNav, null, id);
      uiNavigation.selectedNode_ = selectNode;
      String parentUri = null;
      index = uri.lastIndexOf("/");
      if (index > 0)
        parentUri = uri.substring(0, index);
      if (parentUri == null || parentUri.length() < 1)
        uiNavigation.selectedParent_ = selectNav;
      else
        uiNavigation.selectedParent_ = userPortal.resolvePath(selectNav, null, parentUri);
        UIPage uiPage = uiPortal.findFirstComponentOfType(UIPage.class);
        if (uiPage != null) {
            if (uiPage.getMaximizedUIPortlet() != null) {
                UIPortlet currentPortlet = (UIPortlet) uiPage.getMaximizedUIPortlet();
                currentPortlet.setCurrentWindowState(WindowState.NORMAL);
                uiPage.setMaximizedUIPortlet(null);
        }
      }
      PageNodeEvent<UIPortal> pnevent;
      pnevent = new PageNodeEvent<UIPortal>(uiPortal, PageNodeEvent.CHANGE_NODE, selectNode.getNavigation().getKey(), uri);
      uiPortal.broadcast(pnevent, Event.Phase.PROCESS);
    }
  }
}