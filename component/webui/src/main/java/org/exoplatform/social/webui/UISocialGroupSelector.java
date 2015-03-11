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

package org.exoplatform.social.webui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIBreadcumbs;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.core.UIBreadcumbs.LocalPath;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;

@ComponentConfigs({
  @ComponentConfig(
    template = "war:/groovy/social/webui/space/UISocialGroupSelector.gtmpl",
    events = {
      @EventConfig(phase = Phase.DECODE, listeners = UISocialGroupSelector.ChangeNodeActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UISocialGroupSelector.SelectGroupActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UISocialGroupSelector.SelectPathActionListener.class)
    }
   ),
   @ComponentConfig(
     type = UIFilterableTree.class,
     id = "UITreeGroupSelector",
     template = "war:/groovy/social/webui/UIFilterableTree.gtmpl",
     events = @EventConfig(listeners = UIFilterableTree.ChangeNodeActionListener.class, phase = Phase.DECODE)
   ),
   @ComponentConfig(
     type = UIBreadcumbs.class,
     id = "BreadcumbGroupSelector",
     template = "system:/groovy/webui/core/UIBreadcumbs.gtmpl",
     events = @EventConfig(phase = Phase.DECODE, listeners = UIBreadcumbs.SelectPathActionListener.class))
})
public class UISocialGroupSelector extends UIContainer {
  final static public String MANAGER = "manager";
  final static public String ANY = "*";
  
  private static Log LOG = ExoLogger.getLogger(UISocialGroupSelector.class);
  private Group selectGroup_;

  @SuppressWarnings("unchecked")
  public UISocialGroupSelector() throws Exception {
    UIBreadcumbs uiBreadcumbs = addChild(UIBreadcumbs.class,
        "BreadcumbGroupSelector", "BreadcumbGroupSelector");
    UITree tree = addChild(UIFilterableTree.class, "UITreeGroupSelector",
        "TreeGroupSelector");
    OrganizationService service = getApplicationComponent(OrganizationService.class);
    Collection<?> sibblingsGroup = service.getGroupHandler().findGroups(
        null);

    tree.setSibbling((List) sibblingsGroup);
    tree.setIcon("GroupAdminIcon");
    tree.setSelectedIcon("PortalIcon");
    tree.setBeanIdField("id");
    tree.setBeanLabelField("label");
    uiBreadcumbs.setBreadcumbsStyle("UIExplorerHistoryPath");
    setupFilterableTree();
  }

  public Group getCurrentGroup() {
    return selectGroup_;
  }

  public List<String> getListGroup() throws Exception {
    OrganizationService service = getApplicationComponent(OrganizationService.class);
    List<String> listGroup = new ArrayList<String>();
    RequestContext reqCtx = RequestContext.getCurrentInstance();
    String remoteUser = reqCtx.getRemoteUser();
    if (getCurrentGroup() == null)
      return null;
    Collection<Group> groups = service.getGroupHandler().findGroups(getCurrentGroup());
    if (groups.size() > 0) {
      for (Object child : groups) {
        Group childGroup = (Group) child;
        Membership membership = getMemberShip(remoteUser, childGroup.getId());
            
        if (membership != null) {
          listGroup.add(childGroup.getId());
        }
      }
    }
    return listGroup;
  }

  public String event(String name, String beanId) throws Exception {
    UIForm uiForm = getAncestorOfType(UIForm.class);
    if (uiForm != null)
      return uiForm.event(name, getId(), beanId);
    return super.event(name, beanId);
  }

  /**
   * Reset tree into the current selected group.
   *
   * @param groupId
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private void changeGroup(String groupId) throws Exception {
    OrganizationService service = getApplicationComponent(OrganizationService.class);
    UIBreadcumbs uiBreadcumb = getChild(UIBreadcumbs.class);
    uiBreadcumb.setPath(getPath(null, groupId));

    UITree tree = getChild(UIFilterableTree.class);
    Collection<?> sibblingGroup;

    if (groupId == null) {
      sibblingGroup = service.getGroupHandler().findGroups(null);
      tree.setSibbling((List) sibblingGroup);
      tree.setChildren(null);
      tree.setSelected(null);
      selectGroup_ = null;
      return;
    }

    selectGroup_ = service.getGroupHandler().findGroupById(groupId);
    String parentGroupId = null;
    if (selectGroup_ != null)
      parentGroupId = selectGroup_.getParentId();
    Group parentGroup = null;
    if (parentGroupId != null)
      parentGroup = service.getGroupHandler()
          .findGroupById(parentGroupId);

    Collection childrenGroup = service.getGroupHandler().findGroups(
        selectGroup_);
    sibblingGroup = service.getGroupHandler().findGroups(parentGroup);

    tree.setSibbling((List) sibblingGroup);
    tree.setChildren((List) childrenGroup);
    tree.setSelected(selectGroup_);
    tree.setParentSelected(parentGroup);
  }

  /**
   * Get path of the group in tree.
   *
   * @param list
   * @param id
   * @return
   * @throws Exception
   */
  private List<LocalPath> getPath(List<LocalPath> list, String id)
      throws Exception {
    if (list == null)
      list = new ArrayList<LocalPath>(5);
    if (id == null)
      return list;
    OrganizationService service = getApplicationComponent(OrganizationService.class);
    Group group = service.getGroupHandler().findGroupById(id);
    if (group == null)
      return list;
    list.add(0, new LocalPath(group.getId(), group.getGroupName()));
    getPath(list, group.getParentId());
    return list;
  }

  /**
   * Set up tree at start up.
   *
   */
  private void setupFilterableTree() {
    UIFilterableTree.TreeNodeFilter nodeFilter = new UIFilterableTree.TreeNodeFilter() {

      public boolean filterThisNode(Object nodeObject,
          WebuiRequestContext context) {
        String remoteUser = context.getRemoteUser();
        OrganizationService service = getApplicationComponent(OrganizationService.class);

        if (remoteUser == null) {
          return true;
        }

        if (!(nodeObject instanceof Group)) {
          return true;
        }

        Group group = (Group) nodeObject;

        OrganizationService orgService = getApplicationComponent(OrganizationService.class);
        ;
        Membership membership = null;
        try {
          membership = getMemberShip(remoteUser, group.getId());
        } catch (Exception e) {
          LOG.warn("Error when finding membership for filtering tree node", e);
        }

        if (membership != null) {
          return false;
        }

        List<Group> groups = null;
        try {
          groups = (List) service.getGroupHandler().findGroups(group);
          boolean existMembership = checkMembershipOfChildren(groups,
              orgService, remoteUser);
          
          for (Group childGroup : groups) {
            if (!existMembership) {
              List<Group> childGroups = null;
              childGroups = (List) service.getGroupHandler().findGroups(childGroup);
              boolean hasMembership = checkMembershipOfChildren(childGroups, orgService, remoteUser);
              if (hasMembership) {
                return (!hasMembership);
              }
            }
          }
          
          return (!existMembership);
        } catch (Exception e) {
          LOG.warn("Error when filtering tree node", e);
        }

        return true;
      }

      private boolean checkMembershipOfChildren(List<Group> groups,
          OrganizationService orgService, String remoteUser) {
        Membership membership = null;
        for (Group group : groups) {
          try {
            membership = getMemberShip(remoteUser, group.getId());
          } catch (Exception e) {
            LOG.warn("Error when filtering tree node", e);
          }

          if (membership != null) {
            return true;
          }
        }

        return false;
      }

    };

    this.getChild(UIFilterableTree.class).setTreeNodeFilter(nodeFilter);
  }

  static public class ChangeNodeActionListener extends
      EventListener<UIFilterableTree> {
    public void execute(Event<UIFilterableTree> event) throws Exception {
      UISocialGroupSelector uiGroupSelector = event.getSource()
          .getAncestorOfType(UISocialGroupSelector.class);
      String groupId = event.getRequestContext().getRequestParameter(
          OBJECTID);
      uiGroupSelector.changeGroup(groupId);
      event.getRequestContext().addUIComponentToUpdateByAjax(
          uiGroupSelector);
    }
  }

  static public class SelectGroupActionListener extends
      EventListener<UISocialGroupSelector> {
    public void execute(Event<UISocialGroupSelector> event)
        throws Exception {
      UISocialGroupSelector uiSelector = event.getSource();
      UIComponent uiPermission = uiSelector.<UIComponent> getParent()
          .getParent();
      WebuiRequestContext pcontext = event.getRequestContext();

      UIPopupWindow uiPopup = uiSelector.getParent();
      UIForm uiForm = event.getSource().getAncestorOfType(UIForm.class);
      if (uiForm != null) {
        event.getRequestContext().addUIComponentToUpdateByAjax(
            uiForm.getParent());
      } else {
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
      }
      if (uiSelector.getCurrentGroup() == null) {
        UIApplication uiApp = pcontext.getUIApplication();
        uiApp.addMessage(new ApplicationMessage(
            "UIGroupSelector.msg.selectGroup", null));
        //pcontext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        uiPopup.setShow(true);
        return;
      }

      uiPermission.broadcast(event, event.getExecutionPhase());
      uiPopup.setShow(false);

    }
  }

  static public class SelectPathActionListener extends
      EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIBreadcumbs uiBreadcumbs = event.getSource();
      UISocialGroupSelector uiSelector = uiBreadcumbs.getParent();
      String objectId = event.getRequestContext().getRequestParameter(
          OBJECTID);
      uiBreadcumbs.setSelectPath(objectId);
      String selectGroupId = uiBreadcumbs.getSelectLocalPath().getId();
      uiSelector.changeGroup(selectGroupId);

      UIPopupWindow uiPopup = uiSelector.getParent();
      uiPopup.setShow(true);

      UIForm uiForm = event.getSource().getAncestorOfType(UIForm.class);
      if (uiForm != null) {
        event.getRequestContext().addUIComponentToUpdateByAjax(
            uiForm.getParent());
      } else {
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
      }
    }
  }
  
  private Membership getMemberShip(String remoteUser, String id) throws Exception {
    OrganizationService service = getApplicationComponent(OrganizationService.class);
    Membership membership = service.getMembershipHandler()
    .findMembershipByUserGroupAndType(remoteUser,
        id, MANAGER);
    
    if (membership == null) {
      membership = service.getMembershipHandler()
          .findMembershipByUserGroupAndType(remoteUser,
              id, ANY);
    }
    
    return membership;
  }
}
