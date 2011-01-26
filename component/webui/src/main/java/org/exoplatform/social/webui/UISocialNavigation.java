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
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.navigation.PageNavigationUtils;
import org.exoplatform.portal.webui.page.UIPageBody;
import org.exoplatform.portal.webui.portal.PageNodeEvent;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * {@link UISocialNavigation} used as child of UISocialNavigationPortlet.
 * Created by The eXo Platform SARL
 */
public class UISocialNavigation extends UIComponent {
  private boolean    useAJAX = true;

  protected PageNode selectedNode_;

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
  public List<PageNavigation> getNavigations() throws Exception {
    List<PageNavigation> result = new ArrayList<PageNavigation>();
    List<PageNavigation> navigations = Util.getUIPortalApplication().getNavigations();

    PortalRequestContext portalRequest = Util.getPortalRequestContext();
    for (PageNavigation pageNavigation : navigations) {
      if (pageNavigation.getOwner().equals(portalRequest.getPortalOwner())
          && pageNavigation.getOwnerType().equals("portal")) {
        result.add(pageNavigation);
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
  public PageNavigation getSelectedNavigation() throws Exception {
    UIPortal uiPortal = Util.getUIPortal();
    PageNavigation nav = uiPortal.getSelectedNavigation();
    if (nav == null && uiPortal.getNavigations().size() > 0 ) {
      nav = uiPortal.getNavigations().get(0);
    }
    return nav;
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
  public PageNode getSelectedPageNode() throws Exception {
    if (selectedNode_ == null) {
      selectedNode_ = Util.getUIPortal().getSelectedNode();
    }
    return selectedNode_;
  }

  /**
   * Checks if a node is a selected node
   * 
   * @param node
   * @return true or false
   */
  public boolean isSelectedNode(PageNode node) {
    if (selectedNode_ != null && node.getUri().equals(selectedNode_.getUri())) {
      return true;
    }
    if (selectedParent_ != null && selectedParent_ instanceof PageNode) {
      return node.getUri().equals(((PageNode) selectedParent_).getUri());
    }
    return false;
  }

  public void processRender(WebuiRequestContext context) throws Exception {
    UIPortal uiPortal = Util.getUIPortal();
    if (uiPortal.getSelectedNode() != selectedNode_) {
      setSelectedPageNode(uiPortal.getSelectedNode());
    }
    super.processRender(context);
  }

  /**
   * sets selected page node
   * 
   * @param selectedNode
   * @throws Exception
   */
  private void setSelectedPageNode(PageNode selectedNode) throws Exception {
    selectedNode_ = selectedNode;
    selectedParent_ = null;
    String seletctUri = selectedNode.getUri();
    int index = seletctUri.lastIndexOf("/");
    String parentUri = null;
    if (index > 0)
      parentUri = seletctUri.substring(0, seletctUri.lastIndexOf("/"));
    List<PageNavigation> pageNavs = getNavigations();
    for (PageNavigation pageNav : pageNavs) {
      if (PageNavigationUtils.searchPageNodeByUri(pageNav, selectedNode.getUri()) != null) {
        if (parentUri == null || parentUri.length() < 1)
          selectedParent_ = pageNav;
        else
          selectedParent_ = PageNavigationUtils.searchPageNodeByUri(pageNav, parentUri);
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
      UISocialNavigation uiNavigation = event.getSource();
      UIPortal uiPortal = Util.getUIPortal();
      String uri = event.getRequestContext().getRequestParameter(OBJECTID);
      int index = uri.lastIndexOf("::");
      String id = uri.substring(index + 2);
      PageNavigation selectNav = null;
      if (index <= 0) {
        selectNav = uiPortal.getSelectedNavigation();
      } else {
        String navId = uri.substring(0, index);
        // TODO dang.tung 3.0
        // selectNav = uiPortal.getPageNavigation(Integer.parseInt(navId));
        // TODO dang.tung
      }
      PageNode selectNode = PageNavigationUtils.searchPageNodeByUri(selectNav, id);
      uiNavigation.selectedNode_ = selectNode;
      String parentUri = null;
      index = uri.lastIndexOf("/");
      if (index > 0)
        parentUri = uri.substring(0, index);
      if (parentUri == null || parentUri.length() < 1)
        uiNavigation.selectedParent_ = selectNav;
      else
        uiNavigation.selectedParent_ = PageNavigationUtils.searchPageNodeByUri(selectNav, parentUri);
      UIPageBody uiPageBody = uiPortal.findFirstComponentOfType(UIPageBody.class);
      if (uiPageBody != null) {
        if (uiPageBody.getMaximizedUIComponent() != null) {
          UIPortlet currentPortlet = (UIPortlet) uiPageBody.getMaximizedUIComponent();
          currentPortlet.setCurrentWindowState(WindowState.NORMAL);
          uiPageBody.setMaximizedUIComponent(null);
        }
      }
      PageNodeEvent<UIPortal> pnevent;
      pnevent = new PageNodeEvent<UIPortal>(uiPortal, PageNodeEvent.CHANGE_PAGE_NODE, uri);
      uiPortal.broadcast(pnevent, Event.Phase.PROCESS);
    }
  }
}