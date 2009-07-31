/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

import java.util.List;

import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceUtils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.WebuiConfiguration;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * UIManageMySpaces
 * Manage all user's spaces, user can edit, delete, leave space.
 * User can create new space here.
 * 
 * Created by The eXo Platform SAS
 * Author : hoatle
 *          hoatlevan@gmail.com
 * Jun 29, 2009  
 */
@ComponentConfig(
  template="app:/groovy/portal/webui/component/UIManageMySpaces.gtmpl",
  events = {
      @EventConfig(listeners = UIManageMySpaces.EditSpaceActionListener.class), 
      @EventConfig(listeners = UIManageMySpaces.EditSpaceNavigationActionListener.class),
      @EventConfig(listeners = UIManageMySpaces.DeleteSpaceActionListener.class, confirm = "UIManageMySpace.msg.confirm-space-delete"),
      @EventConfig(listeners = UIManageMySpaces.LeaveSpaceActionListener.class),
      @EventConfig(listeners = UIManageMySpaces.AddSpaceActionListener.class),
      @EventConfig(listeners = UIManageMySpaces.AcceptActionListener.class),
      @EventConfig(listeners = UIManageMySpaces.DenyActionListener.class)
  }
)
public class UIManageMySpaces extends UIContainer {
  //Message Bundle
  static public final String LBL_INVITED_SPACES = "UIManageMySpaces.label.invited-spaces";
  static public final String LBL_MY_SPACES = "UIManageMySpaces.label.my-spaces";
  static public final String LBL_ACTION_ACCEPT = "UIManageMySpaces.label.action-accept";
  static public final String LBL_ACTION_DENY = "UIManageMySpaces.label.action-deny";
  static public final String LBL_ACTION_EDIT_SPACE = "UIManageMySpaces.label.action-edit-space";
  static public final String LBL_ACTION_EDIT_SPACE_NAVIGATION = "UIManageMySpaces.label.action-edit-space-navigation";
  static public final String LBL_ACTION_DELETE_SPACE = "UIManageMySpaces.label.action-delete-space";
  static public final String LBL_ACTION_LEAVE_SPACE = "UIManageMySpaces.label.action-leave";
  static public final String LBL_ACTION_ADD_SPACE = "UIManageMySpaces.label.action-add-space";
  
  static private final String MSG_WARNING_LEAVE_SPACE = "UIManageMySpaces.msg.warning-leave-space";
  static private final String MSG_ERROR_LEAVE_SPACE = "UIManageMySpaces.msg.error-leave-space";
  static private final String MSG_LEAVE_SPACE_SUCCESS = "UIManageMySpaces.msg.leave-space-success";
  
  static private final String MSG_ERROR_ACCEPT_INVITATION = "UIManageMySpaces.msg.error-accept-invited";
  static private final String MSG_ACCEPT_INVITATION_SUCCESS = "UIManageMySpaces.msg.accept-invited-success";
  
  static private final String MSG_ERROR_DENY_INVITATION = "UIManageMySpaces.msg.error-deny-invitation";
  static private final String MSG_DENY_INVITATION_SUCCESS = "UIManageMySpaces.msg.deny-invitation-success";
  
  static public final Integer LEADER = 1, MEMBER = 2;
  
  
  private final String POPUP_ADD_SPACE = "UIPopupAddSpace";
  
  private SpaceService spaceService = null;
  private String userId = null;
  
  /**
   * Constructor for initialize UIPopupWindow for adding new space popup
   * @throws Exception
   */
  public UIManageMySpaces() throws Exception {
    // Add UIPopup for displaying UIAddSpaceForm
    UIPopupWindow uiPopup = createUIComponent(UIPopupWindow.class, null, POPUP_ADD_SPACE);
    uiPopup.setShow(false);
    uiPopup.setWindowSize(500, 0);
    addChild(uiPopup);
  }
  
  /**
   * Get SpaceService
   * @return spaceService
   */
  private SpaceService getSpaceService() {
    if(spaceService == null)
      spaceService = getApplicationComponent(SpaceService.class);
    return spaceService;
  }
  
  /**
   * Get remote user Id
   * @return userId
   */
  private String getUserId() {
    if(userId == null) 
      userId = Util.getPortalRequestContext().getRemoteUser();
    return userId;
  }
  
  /**
   * Get spaces in which user is member or leader
   * 
   * @return userSpaces List<Space>
   * @throws Exception
   */
  public List<Space> getUserSpaces() throws Exception {
    SpaceService spaceService = getSpaceService();
    String userId = getUserId();
    List<Space> userSpaces = spaceService.getSpaces(userId);
    return SpaceUtils.getOrderedSpaces(userSpaces);
  }
  
  /**
   * Get spaces which user is invited to join
   * 
   * @return invitedSpaces List<Space>
   * @throws Exception
   */
  public List<Space> getInvitedSpaces() throws Exception {
    SpaceService spaceService = getSpaceService();
    String userId = getUserId();
    List<Space> invitedSpaces = spaceService.getInvitedSpaces(userId);
    return SpaceUtils.getOrderedSpaces(invitedSpaces);
  }
  
  /**
   * Get role of the user in a specific space for displaying in template
   * 
   * @param spaceId
   * @return UIManageMySpaces.LEADER if the remote user is the space's leader
   *         UIManageMySpaces.MEMBER if the remote user is the space's member
   * @throws SpaceException 
   */
  public int getRole(String spaceId) throws SpaceException {
    SpaceService spaceService = getSpaceService();
    String userId = getUserId();
    Space space = spaceService.getSpaceById(spaceId);
    
    if(spaceService.isMember(space, userId)) {
      if(spaceService.isLeader(space, userId)) return LEADER;
      return MEMBER;
    }
    return 0;
  }
  
  /**
   * This action is triggered when user click on EditSpace
   * 
   * When user click on editSpace, the user is redirected to SpaceSettingPortlet
   *
   */
  static public class EditSpaceActionListener extends EventListener<UIManageMySpaces> {

    @Override
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      // TODO hoatle EditSpaceActionListener
    }
  }
  
  /**
   * This action is triggered when user click on EditSpaceNavigation
   * 
   * A Navigation popup for user to edit space navigation.
   *
   */
  static public class EditSpaceNavigationActionListener extends EventListener<UIManageMySpaces> {
    @Override
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      // TODO hoatle EditSpaceNavigationActionListener
      
    }
    
  }
  
  /**
   * This action is triggered when user click on DeleteSpace
   * a prompt popup is display for confirmation, if yes delete that space; otherwise, do nothing.
   *
   */
  static public class DeleteSpaceActionListener extends EventListener<UIManageMySpaces> {

    @Override
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      // TODO DeleteSpaceActionListener
    }
    
  }
  
  /**
   * This action is triggered when user click on LeaveSpace
   * The leaving space will remove that user in the space.
   * If that user is the only leader -> can't not leave that space
   */
  static public class LeaveSpaceActionListener extends EventListener<UIManageMySpaces> {
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      UIManageMySpaces uiMySpaces = event.getSource();
      SpaceService spaceService = uiMySpaces.getSpaceService();
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApp = ctx.getUIApplication();
      String spaceId = ctx.getRequestParameter(OBJECTID);
      String userId = ctx.getRemoteUser();
      String msg = "";
      if (spaceService.isOnlyLeader(spaceId, userId)) {
        msg = MSG_WARNING_LEAVE_SPACE;
        uiApp.addMessage(new ApplicationMessage(msg, null, ApplicationMessage.WARNING));
        return;
      }
      try {
        spaceService.removeMember(spaceId, userId);
      } catch(SpaceException se) {
        msg = MSG_ERROR_LEAVE_SPACE;
        uiApp.addMessage(new ApplicationMessage(msg, null, ApplicationMessage.ERROR));
        return;
      }
      
      msg = MSG_LEAVE_SPACE_SUCCESS;
      uiApp.addMessage(new ApplicationMessage(msg, null, ApplicationMessage.INFO));
      ctx.addUIComponentToUpdateByAjax(uiMySpaces);
    }
  }
  
  /**
   * This action is triggered when user clicks on AddSpace
   * 
   * UIAddSpaceForm will be displayed in a popup window
   */
  static public class AddSpaceActionListener extends EventListener<UIManageMySpaces> {

    @Override
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      UIManageMySpaces uiManageMySpaces = event.getSource();
      UIPopupWindow uiPopup = uiManageMySpaces.getChild(UIPopupWindow.class);
      UISpaceAddForm uiAddSpaceForm = uiManageMySpaces.createUIComponent(UISpaceAddForm.class,
                                                                         null,
                                                                         null);
      uiPopup.setUIComponent(uiAddSpaceForm);
      uiPopup.setShow(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManageMySpaces);
    }
    
  }
  
  /**
   * This action is triggered when user clicks on Accept Space Invitation
   * When accepting, that user will be the member of the space
   */
  static public class AcceptActionListener extends EventListener<UIManageMySpaces> {

    @Override
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      UIManageMySpaces uiMySpaces = event.getSource();
      SpaceService spaceService = uiMySpaces.getSpaceService();
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApp = ctx.getUIApplication();
      String spaceId = ctx.getRequestParameter(OBJECTID);
      String userId = ctx.getRemoteUser();
      String msg = "";
      try {
        spaceService.acceptInvitation(spaceId, userId);
      } catch(SpaceException se) {
        msg = MSG_ERROR_ACCEPT_INVITATION;
        uiApp.addMessage(new ApplicationMessage(msg, null, ApplicationMessage.ERROR));
        return;
      }
      msg = MSG_ACCEPT_INVITATION_SUCCESS;
      uiApp.addMessage(new ApplicationMessage(msg, null, ApplicationMessage.INFO));
      ctx.addUIComponentToUpdateByAjax(uiMySpaces);
    }
  }
  
  /**
   * This action is triggered when user clicks on Deny Space Invitation
   * When denying, that space will remove the user from pending
   */
  static public class DenyActionListener extends EventListener<UIManageMySpaces> {

    @Override
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      UIManageMySpaces uiMySpaces = event.getSource();
      SpaceService spaceService = uiMySpaces.getSpaceService();
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApp = ctx.getUIApplication();
      String spaceId = ctx.getRequestParameter(OBJECTID);
      String userId = ctx.getRemoteUser();
      String msg = "";
      try {
        spaceService.denyInvitation(spaceId, userId);
      } catch(SpaceException se) {
        msg = MSG_ERROR_DENY_INVITATION;
        uiApp.addMessage(new ApplicationMessage(msg, null, ApplicationMessage.ERROR));
      }
      msg = MSG_DENY_INVITATION_SUCCESS;
      uiApp.addMessage(new ApplicationMessage(msg, null, ApplicationMessage.INFO));
      ctx.addUIComponentToUpdateByAjax(uiMySpaces);      
    }    
  }
}
