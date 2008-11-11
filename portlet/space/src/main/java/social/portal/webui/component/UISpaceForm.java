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
package social.portal.webui.component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceUtils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.NameValidator;
/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Sep 07, 2008          
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/portal/webui/uiform/UISpaceForm.gtmpl",
    events = {
        @EventConfig(listeners = UISpaceForm.CreateSpaceActionListener.class )
      }
)
public class UISpaceForm extends UIForm implements UIPopupComponent{
  final static public String SPACE_PARENT = "/spaces";
  final static private String SPACE_NAME = "spaceName";
  public UISpaceForm() throws Exception {
    addUIFormInput(new UIFormStringInput(SPACE_NAME,SPACE_NAME,null).addValidator(MandatoryValidator.class));
  }
  
  static public class CreateSpaceActionListener extends EventListener<UISpaceForm> {
    public void execute(Event<UISpaceForm> event) throws Exception {
      UISpaceForm uiForm = event.getSource();
      UIManageSpacesPortlet uiPorlet = uiForm.getAncestorOfType(UIManageSpacesPortlet.class);
      OrganizationService orgService = uiForm.getApplicationComponent(OrganizationService.class);
      GroupHandler groupHandler = orgService.getGroupHandler();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIApplication uiApp = requestContext.getUIApplication();
      Group groupParrent = groupHandler.findGroupById(SPACE_PARENT);
      
      //Create new group
      Group newGroup = groupHandler.createGroupInstance();
      String spaceName = ((UIFormStringInput)uiForm.getChildById(SPACE_NAME)).getValue();
      String spaceNameCleaned = SpaceUtils.cleanString(spaceName);
      String groupId = groupParrent.getId() + "/" + spaceNameCleaned;
      if(groupHandler.findGroupById(groupId) != null) {
        uiApp.addMessage(new ApplicationMessage("UISpaceForm.msg.space-exist", null));
        requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }
      newGroup.setGroupName(spaceNameCleaned);
      newGroup.setLabel(spaceName);
      groupHandler.addChild(groupParrent,newGroup,true);
      
      // add user as creator (manager)
      String userName = requestContext.getRemoteUser();
      User user = orgService.getUserHandler().findUserByName(userName);
      MembershipType mbShipType = orgService.getMembershipTypeHandler().findMembershipType("manager");
      orgService.getMembershipHandler().linkMembership(user, newGroup, mbShipType, true);
      
      // Store space to database
      SpaceService spaceSrc = uiForm.getApplicationComponent(SpaceService.class);
      Space space = new Space();
      space.setName(spaceName);
      space.setGroupId(groupId);
      space.setType(Space.CLASSIC);
      //for testing
      space.setDescription("edit this description to explain what your space is about");
      space.setTag("");
      //--end---
      spaceSrc.saveSpace(space, true);
      uiForm.getAncestorOfType(UIPopupContainer.class).deActivate();
      
      // create the new page and node to new group
      // the template page id
      String tempPageId= "group::platform/user::dashboard";
      
      //create the name and uri of the new pages
      String newPageName = spaceNameCleaned;
      
      // create new space navigation
      UserPortalConfigService dataService = uiForm.getApplicationComponent(UserPortalConfigService.class);
      
      PageNavigation spaceNav = new PageNavigation();
      spaceNav.setOwnerType(PortalConfig.GROUP_TYPE);
      spaceNav.setOwnerId(newGroup.getId().substring(1));
      spaceNav.setModifiable(true);
      dataService.create(spaceNav);
      UIPortal uiPortal = Util.getUIPortal();
      List<PageNavigation> pnavigations = uiPortal.getNavigations();
      setNavigation(pnavigations, spaceNav);
      pnavigations.add(spaceNav) ;
      PageNode node = dataService.createNodeFromPageTemplate(newPageName, newPageName, tempPageId, PortalConfig.GROUP_TYPE, spaceNameCleaned,null) ;
      node.setUri(spaceNameCleaned) ;
      spaceNav.addNode(node) ;
      dataService.update(spaceNav) ;
      setNavigation(uiPortal.getNavigations(), spaceNav) ;
      requestContext.addUIComponentToUpdateByAjax(uiPorlet);
    }
  }
  
  private static void setNavigation(List<PageNavigation> navs, PageNavigation nav) {
    for(int i = 0; i < navs.size(); i++) {
      if(navs.get(i).getId() == nav.getId()) {
        navs.set(i, nav);
        return;
      }
    }
  }

  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
    
}
