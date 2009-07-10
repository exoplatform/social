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
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
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

  private final String POPUP_ADD_SPACE = "popupAddSpace";
  
  private SpaceService spaceService = null;
  private String userId = null;
  
  public UIManageMySpaces() throws Exception {
    // Add UIPopup for displaying UIAddSpaceForm
    UIPopupWindow uiPopup = createUIComponent(UIPopupWindow.class, null, POPUP_ADD_SPACE);
    uiPopup.setShow(false);
    uiPopup.setWindowSize(500, 0);
    addChild(uiPopup);
  }
  
  private SpaceService getSpaceService() {
    if(spaceService == null)
      spaceService = getApplicationComponent(SpaceService.class);
    return spaceService;
  }
  
  private String getUserId() {
    if(userId == null) 
      userId = Util.getPortalRequestContext().getRemoteUser();
    return userId;
  }
  
  /*
   * Get spaces in which user is member or leader
   */
  public List<Space> getUserSpaces() throws Exception {
    SpaceService spaceService = getSpaceService();
    String userId = getUserId();
    List<Space> userSpaces = spaceService.getSpaces(userId);
    return userSpaces;
  }
  
  /*
   * Get spaces which user is invited to join
   */
  public List<Space> getInvitedSpaces() throws Exception {
    SpaceService spaceService = getSpaceService();
    String userId = getUserId();
    List<Space> invitedSpaces = spaceService.getInvitedSpaces(userId);
    return invitedSpaces;
  }
  
  /*
   * Get role of the user in a specific group for displaying in template
   * @return 1 - leader
   *         2 - member 
   */
  public int getRole(String spaceId) throws SpaceException {
    //1: leader, 2: member
    SpaceService spaceService = getSpaceService();
    String userId = getUserId();
    Space space = spaceService.getSpaceById(spaceId);
    
    if(spaceService.isMember(space, userId)) {
      if(spaceService.isLeader(space, userId)) return 1;
      return 2;
    }
    return 0;
  }
  
  /*
   * This action is triggered when user click on EditSpace
   */
  static public class EditSpaceActionListener extends EventListener<UIManageMySpaces> {

    @Override
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      // TODO Auto-generated method stub
    }
  }
  
  /*
   * This action is triggered when user click on EditSpaceNavigation
   */
  static public class EditSpaceNavigationActionListener extends EventListener<UIManageMySpaces> {

    @Override
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      // TODO Auto-generated method stub
      
    }
    
  }
  
  /*
   * This action is triggered when user click on DeleteSpace
   */
  static public class DeleteSpaceActionListener extends EventListener<UIManageMySpaces> {

    @Override
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      // TODO Auto-generated method stub      
    }
    
  }
  
  /*
   * This action is triggered when user click on LeaveSpace
   */
  static public class LeaveSpaceActionListener extends EventListener<UIManageMySpaces> {
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      // TODO Auto-generated method stub
    }
  }
  
  /*
   * This action is triggered when user clicks on AddSpace
   * 
   * UIAddSpaceForm will be displayed in a popup window
   */
  static public class AddSpaceActionListener extends EventListener<UIManageMySpaces> {

    @Override
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      // TODO Auto-generated method stub
      UIManageMySpaces uiManageMySpaces = event.getSource();
      UIPopupWindow uiPopup = uiManageMySpaces.getChild(UIPopupWindow.class);
      System.out.println("\n\n\n uipopadd" + uiPopup.getId());
      UISpaceAddForm uiAddSpaceForm = uiManageMySpaces.createUIComponent(UISpaceAddForm.class, null, null);
      uiPopup.setUIComponent(uiAddSpaceForm);
      uiPopup.setShow(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManageMySpaces);
    }
    
  }
  
  /*
   * This action is triggered when user clicks on Accept Space Invitation
   */
  static public class AcceptActionListener extends EventListener<UIManageMySpaces> {

    @Override
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      // TODO Auto-generated method stub      
    }
  }
  
  /*
   * This action is triggered when user clicks on Deny Space Invitation
   */
  static public class DenyActionListener extends EventListener<UIManageMySpaces> {

    @Override
    public void execute(Event<UIManageMySpaces> event) throws Exception {
      // TODO Auto-generated method stub      
    }    
  }
}
