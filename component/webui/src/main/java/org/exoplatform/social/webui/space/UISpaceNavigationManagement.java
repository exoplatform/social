/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.social.webui.space;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.SiteType;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIRightClickPopupMenu;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;

@ComponentConfigs({
  @ComponentConfig(
    template = "war:/groovy/social/webui/space/UISpaceNavigationManagement.gtmpl",
    events = {
      @EventConfig(listeners = UISpaceNavigationManagement.AddRootNodeActionListener.class),
      @EventConfig(listeners = UISpaceNavigationManagement.AddNodeActionListener.class),
      @EventConfig(listeners = UISpaceNavigationManagement.SaveActionListener.class),
      @EventConfig(listeners = UISpaceNavigationManagement.SelectNodeActionListener.class)
    }
  ),
  @ComponentConfig(
    type = UIPageNodeForm.class,
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIFormTabPane.gtmpl",
    events = {
      @EventConfig(listeners = UIPageNodeForm.SaveActionListener.class),
      @EventConfig(listeners = UISpaceNavigationManagement.BackActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIPageNodeForm.ChangeLanguageActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIPageNodeForm.SwitchLabelModeActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIPageNodeForm.SwitchPublicationDateActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIPageNodeForm.SwitchVisibleActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIPageNodeForm.ClearPageActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIPageNodeForm.CreatePageActionListener.class, phase = Phase.DECODE),
      @EventConfig(listeners = UIPageNodeForm.SelectTabActionListener.class, phase = Phase.DECODE)
    }
  ),
  @ComponentConfig(
    type = UIPopupWindow.class,
    id = "AddNode",
    template =  "system:/groovy/webui/core/UIPopupWindow.gtmpl",
    events =
      @EventConfig(listeners = UISpaceNavigationManagement.ClosePopupActionListener.class, name = "ClosePopup")
  )
})
                  
/**
 * Editor : hanhvq@exoplatfor.com Jun 22, 2011 
 */
public class UISpaceNavigationManagement extends UIContainer {
  private static final String SPACE_LABEL = "Space";
  
  private String owner;

  private String ownerType;

  private Space space;
  
  public UISpaceNavigationManagement() throws Exception {
    UIPopupWindow uiPopup = createUIComponent(UIPopupWindow.class, "AddNode", null);
    uiPopup.setWindowSize(800, 445);
    uiPopup.setShow(false);
    addChild(uiPopup);
    
    addChild(UISpaceNavigationNodeSelector.class, null, null);
    reloadTreeData();
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getOwner() {
    return this.owner;
  }

  public <T extends UIComponent> T setRendered(boolean b) {
    return super.<T> setRendered(b);
  }

  protected String getBreadcrumb() throws Exception {
    UISpaceNavigationNodeSelector selector = getChild(UISpaceNavigationNodeSelector.class);
    UITree uiTree = selector.getChild(UITree.class);
    TreeNode selectedNode = uiTree.getSelected();
    TreeNode rootNode = selector.getRootNode();
    List<TreeNode> nodes = new ArrayList<TreeNode>();
    
    while (selectedNode.getId() != rootNode.getId()) {
      nodes.add(selectedNode);
      selectedNode = selectedNode.getParent();
    }
    
    StringBuffer sb = new StringBuffer();
    sb.append("<li>");
    sb.append("<a href='javascript:void(0);'>").append(SPACE_LABEL).append("</a>");
    sb.append("</li>");
    
    for (int idx = nodes.size() - 1; idx >= 0; idx--) {
      String nodeLabel = nodes.get(idx).getResolvedLabel();
      nodeLabel = StringEscapeUtils.escapeHtml(nodeLabel);
      sb.append("<li>");
      sb.append("<span class='uiIconMiniArrowRight'>&nbsp;</span>");
      sb.append("</li>");
      if (idx == 0) {
        sb.append("<li class=\"active\">");
      } else {
        sb.append("<li>");
      }
      sb.append("<a href=\"javascript:void(0);\" onclick=\"").append(this.event("SelectNode", nodes.get(idx).getId())).append("\"");
      sb.append(">").append(nodeLabel).append("</a>");
      sb.append("</li>");
    }
    
    return sb.toString();
  }

  public void reloadTreeData() throws Exception {
    UserPortal userPortal = Util.getUIPortalApplication().getUserPortalConfig().getUserPortal();
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    String spaceUrl = Utils.getSpaceUrlByContext();
    Space space = spaceService.getSpaceByUrl(spaceUrl);

    UserNavigation groupNav = SpaceUtils.getGroupNavigation(space.getGroupId());

    setOwner(groupNav.getKey().getName());
    setOwnerType(groupNav.getKey().getTypeName());

    UISpaceNavigationNodeSelector selector = getChild(UISpaceNavigationNodeSelector.class);
    selector.setEdittedNavigation(groupNav);
    selector.setUserPortal(userPortal);
    selector.initTreeData();
  }
  
  public void loadView(Event<? extends UIComponent> event) throws Exception {
    UISpaceNavigationNodeSelector uiNodeSelector = getChild(UISpaceNavigationNodeSelector.class);
    UITree uiTree = uiNodeSelector.getChild(UITree.class);
    uiTree.createEvent("ChangeNode", event.getExecutionPhase(), event.getRequestContext())
          .broadcast();
  }

  public void setOwnerType(String ownerType) {
    this.ownerType = ownerType;
  }

  public String getOwnerType() {
    return this.ownerType;
  }

  static public class AddRootNodeActionListener extends EventListener<UISpaceNavigationManagement> {

    @Override
    public void execute(Event<UISpaceNavigationManagement> event) throws Exception {
      UISpaceNavigationManagement uiManagement = event.getSource();
      UISpaceNavigationNodeSelector uiNodeSelector = uiManagement.getChild(UISpaceNavigationNodeSelector.class);
      UIRightClickPopupMenu menu = uiNodeSelector.getChild(UIRightClickPopupMenu.class);
      menu.createEvent("AddNode", Phase.PROCESS, event.getRequestContext()).broadcast();
    }

  }

  static public class AddNodeActionListener extends EventListener<UISpaceNavigationManagement> {

    @Override
    public void execute(Event<UISpaceNavigationManagement> event) throws Exception {
      UISpaceNavigationManagement uiManagement = event.getSource();
      UISpaceNavigationNodeSelector uiNodeSelector = uiManagement.getChild(UISpaceNavigationNodeSelector.class);
      UIRightClickPopupMenu menu = uiNodeSelector.getChild(UIRightClickPopupMenu.class);
      menu.createEvent("AddNode", Phase.PROCESS, event.getRequestContext()).broadcast();
    }

  }
  
  static public class SaveActionListener extends EventListener<UISpaceNavigationManagement> {

    public void execute(Event<UISpaceNavigationManagement> event) throws Exception {
      PortalRequestContext prContext = Util.getPortalRequestContext();
      UISpaceNavigationManagement uiManagement = event.getSource();
      UISpaceNavigationNodeSelector uiNodeSelector = uiManagement.getChild(UISpaceNavigationNodeSelector.class);
      UserPortalConfigService portalConfigService = uiManagement.getApplicationComponent(UserPortalConfigService.class);

      UIPortalApplication uiPortalApp = (UIPortalApplication) prContext.getUIApplication();
      UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
      prContext.addUIComponentToUpdateByAjax(uiWorkingWS);
      prContext.ignoreAJAXUpdateOnPortlets(true);

      UserNavigation navigation = uiNodeSelector.getEdittedNavigation();
      SiteKey siteKey = navigation.getKey();
      String editedOwnerId = siteKey.getName();

      // Check existed
      UserPortalConfig userPortalConfig;
      if (SiteType.PORTAL.equals(siteKey.getType())) {
        userPortalConfig = portalConfigService.getUserPortalConfig(editedOwnerId,
                                                                   event.getRequestContext()
                                                                        .getRemoteUser());
        if (userPortalConfig == null) {
          prContext.getUIApplication()
                   .addMessage(new ApplicationMessage("UIPortalForm.msg.notExistAnymore",
                                                      null,
                                                      ApplicationMessage.ERROR));
          return;
        }
      } else {
        userPortalConfig = portalConfigService.getUserPortalConfig(prContext.getPortalOwner(),
                                                                   event.getRequestContext()
                                                                        .getRemoteUser());
      }

      UserNavigation persistNavigation = userPortalConfig.getUserPortal().getNavigation(siteKey);
      if (persistNavigation == null) {
        prContext.getUIApplication()
                 .addMessage(new ApplicationMessage("UINavigationManagement.msg.NavigationNotExistAnymore",
                                                    null,
                                                    ApplicationMessage.ERROR));
        return;
      }

      uiNodeSelector.save();
    }
  }
  
  static public class SelectNodeActionListener extends EventListener<UISpaceNavigationManagement> {

    public void execute(Event<UISpaceNavigationManagement> event) throws Exception {
      //
      UISpaceNavigationManagement uiSNM = event.getSource();
      UISpaceNavigationNodeSelector uiNodeSelector = uiSNM.getChild(UISpaceNavigationNodeSelector.class);

      String nodeID = event.getRequestContext().getRequestParameter(OBJECTID);
      TreeNode node = uiNodeSelector.findNode(nodeID);
      uiNodeSelector.selectNode(node);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiNodeSelector.getParent());
    }
  }
  
  /**
   * This action trigger when user click on back button from UISpaceNavigationManagement
   * @author hoatle
   *
   */
  static public class BackActionListener extends EventListener<UIPageNodeForm> {

    @Override
    public void execute(Event<UIPageNodeForm> event) throws Exception {
      UIPageNodeForm uiPageNode = event.getSource();
      UserNavigation contextNavigation = uiPageNode.getContextPageNavigation();
      UISpaceNavigationManagement uiSpaceNavManagement = uiPageNode.getAncestorOfType(UISpaceNavigationManagement.class);
      UIPopupWindow uiPopup = uiSpaceNavManagement.getChild(UIPopupWindow.class);
      uiPopup.setShow(false);
    }
  }
  
  static public class ClosePopupActionListener extends EventListener<UIPopupWindow> {

    @Override
    public void execute(Event<UIPopupWindow> event) throws Exception {
      UIPopupWindow uiPopup = event.getSource();
      UISpaceNavigationManagement uiSpaceNavManagement = uiPopup.getAncestorOfType(UISpaceNavigationManagement.class);
      UISpaceNavigationNodeSelector selector = uiSpaceNavManagement.getChild(UISpaceNavigationNodeSelector.class);
      UserNavigation contextNavigation = selector.getEdittedNavigation();
      uiPopup.setShow(false);
      uiSpaceNavManagement.setOwner(contextNavigation.getKey().getName());
      uiSpaceNavManagement.setOwnerType(contextNavigation.getKey().getTypeName());
      selector.setEdittedNavigation(contextNavigation);
      selector.initTreeData();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSpaceNavManagement);
    }
  }

  /**
   * Resets value of space.
   * 
   * @param space
   */
  protected void setSpace(Space space) {
    this.space = space;
  }
  
  /**
   * Gets space.
   * 
   * @return
   */
  protected Space getSpace() {
    return this.space;
  }
}
