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

package org.exoplatform.social.webui.space;

import java.util.List;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.page.UIPageNodeForm;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

@ComponentConfigs({
@ComponentConfig(
   template = "classpath:groovy/social/webui/space/UISpaceNavigationManagement.gtmpl",
   events = {
       @EventConfig(listeners = UISpaceNavigationManagement.SaveActionListener.class),
       @EventConfig(listeners = UISpaceNavigationManagement.AddRootNodeActionListener.class)
   }
),
@ComponentConfig(
       type = UIPageNodeForm.class,
       lifecycle = UIFormLifecycle.class,
       template = "system:/groovy/webui/form/UIFormTabPane.gtmpl",
       events = {
         @EventConfig(listeners = UIPageNodeForm.SaveActionListener.class),
         @EventConfig(listeners = UISpaceNavigationManagement.BackActionListener.class, phase = Phase.DECODE),
         @EventConfig(listeners = UIPageNodeForm.SwitchPublicationDateActionListener.class, phase = Phase.DECODE),
         @EventConfig(listeners = UIPageNodeForm.ClearPageActionListener.class, phase = Phase.DECODE),
         @EventConfig(listeners = UIPageNodeForm.CreatePageActionListener.class, phase = Phase.DECODE)
       }
     ),
@ComponentConfig(
   type = UIPopupWindow.class,
   id = "AddNode",
   template =  "system:/groovy/webui/core/UIPopupWindow.gtmpl",
   events = @EventConfig(listeners = UISpaceNavigationManagement.ClosePopupActionListener.class, name = "ClosePopup")
)
})

public class UISpaceNavigationManagement extends UIContainer {

   private String owner;

   private String ownerType;

   @SuppressWarnings("unused")
   public UISpaceNavigationManagement() throws Exception {
     // add config for popup
      UIPopupWindow uiPopup = createUIComponent(UIPopupWindow.class, "AddNode", null);
      uiPopup.setWindowSize(800, 500);
      uiPopup.setShow(false);
      addChild(uiPopup);
      SpaceService spaceService = getApplicationComponent(SpaceService.class);
      String spaceUrl = SpaceUtils.getSpaceUrl();
      Space space = spaceService.getSpaceByUrl(spaceUrl);

      PageNavigation groupNav = SpaceUtils.getGroupNavigation(space.getGroupId());

      setOwner(groupNav.getOwnerId());
      setOwnerType(groupNav.getOwnerType());

      UISpaceNavigationNodeSelector selector = createUIComponent(UISpaceNavigationNodeSelector.class, null, "UISpaceNavigationNodeSelector");
      selector.setEdittedNavigation(groupNav);
      selector.initTreeData();
      addChild(selector);
   }

   public void setOwner(String owner) {
      this.owner = owner;
   }

   public String getOwner() {
      return this.owner;
   }
   
   public String getDisplayName() {
     return "/spaces/" + SpaceUtils.getSpaceUrl();
   }

   public <T extends UIComponent> T setRendered(boolean b) {
      return super.<T> setRendered(b);
   }

   public void loadView(Event<? extends UIComponent> event) throws Exception {
      UISpaceNavigationNodeSelector uiNodeSelector = getChild(UISpaceNavigationNodeSelector.class);
      UITree uiTree = uiNodeSelector.getChild(UITree.class);
      uiTree.createEvent("ChangeNode", event.getExecutionPhase(), event.getRequestContext()).broadcast();
   }

   public void setOwnerType(String ownerType) {
      this.ownerType = ownerType;
   }

   public String getOwnerType() {
      return this.ownerType;
   }

   static public class SaveActionListener extends EventListener<UISpaceNavigationManagement> {

      public void execute(Event<UISpaceNavigationManagement> event) throws Exception {
         PortalRequestContext prContext = Util.getPortalRequestContext();
         UISpaceNavigationManagement uiManagement = event.getSource();
         UISpaceNavigationNodeSelector uiNodeSelector = uiManagement.getChild(UISpaceNavigationNodeSelector.class);
         DataStorage dataService = uiManagement.getApplicationComponent(DataStorage.class);
         UserPortalConfigService portalConfigService = uiManagement.getApplicationComponent(UserPortalConfigService.class);
         
         PageNavigation navigation = uiNodeSelector.getEdittedNavigation();
         String editedOwnerType = navigation.getOwnerType();
         String editedOwnerId = navigation.getOwnerId();
         // Check existed
         PageNavigation persistNavigation =  dataService.getPageNavigation(editedOwnerType, editedOwnerId);
         if (persistNavigation == null) {
            UIApplication uiApp = Util.getPortalRequestContext().getUIApplication();
            uiApp.addMessage(new ApplicationMessage("UISpaceNavigationManagement.msg.NavigationNotExistAnymore", null));
            UIPopupWindow uiPopup = uiManagement.getChild(UIPopupWindow.class);
            uiPopup.setShow(false);
            UIPortalApplication uiPortalApp = (UIPortalApplication)prContext.getUIApplication();
            UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
            prContext.addUIComponentToUpdateByAjax(uiWorkingWS);
            prContext.setFullRender(true);
            return;
         }
         
         if(PortalConfig.PORTAL_TYPE.equals(navigation.getOwnerType())) {
            UserPortalConfig portalConfig = portalConfigService.getUserPortalConfig(navigation.getOwnerId(), prContext.getRemoteUser());
            if(portalConfig != null) {
               dataService.save(navigation);
            } else {
               UIApplication uiApp = Util.getPortalRequestContext().getUIApplication();
               uiApp.addMessage(new ApplicationMessage("UIPortalForm.msg.notExistAnymore", null));
               UIPopupWindow uiPopup = uiManagement.getChild(UIPopupWindow.class);
               uiPopup.setShow(false);
               UIPortalApplication uiPortalApp = (UIPortalApplication)prContext.getUIApplication();
               UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
               prContext.addUIComponentToUpdateByAjax(uiWorkingWS);
               prContext.setFullRender(true);
               return;
            }
         } else {
            dataService.save(navigation);
         }

         // Reload navigation here as some navigation could exist in the back end such as system navigations
         // that would not be in the current edited UI navigation
         navigation = dataService.getPageNavigation(navigation.getOwnerType(), navigation.getOwnerId());

         UIPortalApplication uiPortalApp = Util.getUIPortalApplication();
         setNavigation(uiPortalApp.getNavigations(), navigation);

         // Need to relocalize as it was loaded from storage
         uiPortalApp.localizeNavigations();
         
         //Update UIPortal corredponding to edited navigation
         UIPortal targetedUIPortal = uiPortalApp.getCachedUIPortal(editedOwnerType, editedOwnerId);
         if(targetedUIPortal != null) {
            targetedUIPortal.setNavigation(navigation);
         }
         
         UIPopupWindow uiPopup = uiManagement.getChild(UIPopupWindow.class);
         uiPopup.setShow(false);
         UIWorkingWorkspace uiWorkingWS = uiPortalApp.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
         prContext.addUIComponentToUpdateByAjax(uiWorkingWS);
         prContext.setFullRender(true);
      }

      private void setNavigation(List<PageNavigation> navs, PageNavigation nav) {
         for (int i = 0; i < navs.size(); i++) {
            if (navs.get(i).getId() == nav.getId()) {
               navs.set(i, nav);
               return;
            }
         }
      }

   }

   static public class AddRootNodeActionListener extends EventListener<UISpaceNavigationManagement> {

      @Override
      public void execute(Event<UISpaceNavigationManagement> event) throws Exception {
        UISpaceNavigationManagement uiManagement = event.getSource();
        UISpaceNavigationNodeSelector uiNodeSelector = uiManagement.getChild(UISpaceNavigationNodeSelector.class);
        UIPopupWindow uiManagementPopup = uiManagement.getChild(UIPopupWindow.class);
        UIPageNodeForm uiNodeForm = uiManagementPopup.createUIComponent(UIPageNodeForm.class, null, null);
        uiNodeForm.setValues(null);
        uiManagementPopup.setUIComponent(uiNodeForm);
        PageNavigation nav = uiNodeSelector.getEdittedNavigation();
        uiNodeForm.setSelectedParent(nav);

        uiNodeForm.setContextPageNavigation(nav);

        uiManagementPopup.setWindowSize(800, 500);
        uiManagementPopup.setShow(true);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiManagementPopup);
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
       PageNavigation contextNavigation = uiPageNode.getContextPageNavigation();
       UISpaceNavigationManagement uiSpaceNavManagement = uiPageNode.getAncestorOfType(UISpaceNavigationManagement.class);
       UIPopupWindow uiPopup = uiSpaceNavManagement.getChild(UIPopupWindow.class);
       uiPopup.setShow(false);
       uiSpaceNavManagement.setOwner(contextNavigation.getOwnerId());
       uiSpaceNavManagement.setOwnerType(contextNavigation.getOwnerType());
       UISpaceNavigationNodeSelector selector = uiSpaceNavManagement.getChild(UISpaceNavigationNodeSelector.class);
       selector.setEdittedNavigation(contextNavigation);
       selector.initTreeData();
       event.getRequestContext().addUIComponentToUpdateByAjax(uiSpaceNavManagement);
     }
   }
   
   static public class ClosePopupActionListener extends EventListener<UIPopupWindow> {

     @Override
     public void execute(Event<UIPopupWindow> event) throws Exception {
       UIPopupWindow uiPopup = event.getSource();
       UISpaceNavigationManagement uiSpaceNavManagement = uiPopup.getAncestorOfType(UISpaceNavigationManagement.class);
       UISpaceNavigationNodeSelector selector = uiSpaceNavManagement.getChild(UISpaceNavigationNodeSelector.class);
       PageNavigation contextNavigation = selector.getEdittedNavigation();
       uiPopup.setShow(false);
       uiSpaceNavManagement.setOwner(contextNavigation.getOwnerId());
       uiSpaceNavManagement.setOwnerType(contextNavigation.getOwnerType());
       selector.setEdittedNavigation(contextNavigation);
       selector.initTreeData();
       event.getRequestContext().addUIComponentToUpdateByAjax(uiSpaceNavManagement);
     }
   }
}