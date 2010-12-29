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
package org.exoplatform.social.webui.space;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.StringListAccess;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.ExpressionValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.organization.account.UIUserSelector;

/**
 * {@link UISpaceMember} is used for managing space member: inviting, validating or declining user's
 * request to join; set leader... <br />
 *
 * Created by The eXo Platform SARL <br />
 * @author <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 * @since Sep 12, 2008
 */

@ComponentConfigs ( {
  @ComponentConfig(
     lifecycle = UIFormLifecycle.class,
     template =  "classpath:groovy/social/webui/space/UISpaceMember.gtmpl",
     events = {
       @EventConfig(listeners = UISpaceMember.InviteActionListener.class),
       @EventConfig(listeners = UISpaceMember.SearchUserActionListener.class, phase=Phase.DECODE),
       @EventConfig(listeners = UISpaceMember.RevokeInvitedUserActionListener.class, phase=Phase.DECODE),
       @EventConfig(listeners = UISpaceMember.DeclineUserActionListener.class, phase=Phase.DECODE),
       @EventConfig(listeners = UISpaceMember.ValidateUserActionListener.class, phase=Phase.DECODE),
       @EventConfig(listeners = UISpaceMember.RemoveUserActionListener.class, phase=Phase.DECODE),
       @EventConfig(listeners = UISpaceMember.RemoveLeaderActionListener.class, phase=Phase.DECODE),
       @EventConfig(listeners = UISpaceMember.MakeLeaderActionListener.class, phase=Phase.DECODE)
       }
 ),
  @ComponentConfig(
    type = UIPopupWindow.class,
    id = "SearchUser",
    template =  "system:/groovy/webui/core/UIPopupWindow.gtmpl",
    events = {
      @EventConfig(listeners = UIPopupWindow.CloseActionListener.class, name = "ClosePopup")  ,
      @EventConfig(listeners = UISpaceMember.CloseActionListener.class, name = "Close", phase = Phase.DECODE)  ,
      @EventConfig(listeners = UISpaceMember.AddActionListener.class, name = "Add", phase = Phase.DECODE)
    }
 )
})

public class UISpaceMember extends UIForm {

  static private final String MSG_ERROR_REMOVE_MEMBER  = "UISpaceMember.msg.error_remove_member";
  static private final String MSG_ERROR_REMOVE_LEADER  = "UISpaceMember.msg.error_remove_leader";
  static private final String MSG_ERROR_SELF_REMOVE_LEADER  = "UISpaceMember.msg.error_self_remove_leader";
  static private final String MSG_ERROR_REVOKE_INVITED = "UISpaceMember.msg.error_revoke_invited";
  static private final String MSG_ERROR_DECLINE_USER   = "UISpaceMember.msg.error_decline_user";
  static private final String MSG_ERROR_VALIDATE_USER  = "UISpaceMember.msg.error_validate_user";
  static private final String MSG_ERROR_MAKE_LEADER    = "UISpaceMember.msg.error_make_leader";
  /** The first page. */
  private static final int FIRST_PAGE = 1;

  private String spaceId;
  private SpaceService spaceService = null;
  private final static String USER = "user";
  private UIPageIterator iteratorPendingUsers;
  private UIPageIterator iteratorInvitedUsers;
  private UIPageIterator iteratorExistingUsers;
  private final String iteratorPendingID = "UIIteratorPendding";
  private final String iteratorInvitedID = "UIIteratorInvited";
  private final String iteratorExistingID = "UIIteratorExisting";
  private final Integer ITEMS_PER_PAGE = 5;
  private static final String USER_TO_INVITE = "user_to_invite";
  String typeOfRelation = null;
  String spaceURL = null;
  
  /** The flag notifies a new search when clicks search icon or presses enter. */
  private boolean isNewSearch;
  
  /**
   * constructor
   * @throws Exception
   */
  public UISpaceMember() throws Exception {
    addUIFormInput(new UIFormStringInput(USER,null,null)
                    .addValidator(MandatoryValidator.class)
                    .addValidator(ExpressionValidator.class, "^\\p{L}[\\p{L}\\d\\s._,]+$", "UISpaceMember.msg.Invalid-char"));
    UIPopupWindow searchUserPopup = addChild(UIPopupWindow.class, "SearchUser", "SearchUser");
    searchUserPopup.setWindowSize(640, 0);
    iteratorPendingUsers = createUIComponent(UIPageIterator.class, null, iteratorPendingID);
    iteratorInvitedUsers = createUIComponent(UIPageIterator.class, null, iteratorInvitedID);
    iteratorExistingUsers = createUIComponent(UIPageIterator.class, null, iteratorExistingID);
    addChild(iteratorPendingUsers);
    addChild(iteratorInvitedUsers);
    addChild(iteratorExistingUsers);
    setTypeOfRelation(USER_TO_INVITE);
  }

  /**
   * gets uiPageIteratorPendingUsers
   * @return uiPageIterator
   */
  public UIPageIterator getUIPageIteratorPendingUsers() { return iteratorPendingUsers;}

  /**
   * gets uiPageIteratorInvitedUsers to display; revoke
   * @return uiPageIteraatorInvitedUsers
   */
  public UIPageIterator getUIPageIteratorInvitedUsers() { return iteratorInvitedUsers;}

  /**
   * gets uiPageIteratorExistingUsers to display; remove or set/remove leader.
   * @return uiPageiteratorExistingUsers
   */
  public UIPageIterator getUIPageIteratorExistingUsers() { return iteratorExistingUsers;}

  /**
   * sets spaceId to this for getting current space
   * @param spaceId
   * @throws Exception
   */
  public void setValue(String spaceId) throws Exception {
    this.spaceId = spaceId;
  }


  /**
   * Gets type of relation with current user.
   *
   */
  public String getTypeOfRelation() {
    return typeOfRelation;
  }

  /**
   * Sets type of relation with current user to variable.
   *
   * @param typeOfRelation <code>char</code>
   */
  public void setTypeOfRelation(String typeOfRelation) {
    this.typeOfRelation = typeOfRelation;
  }

  /**
   * Gets space url.
   *
   */
  public String getSpaceURL() {
    return spaceURL;
  }

  /**
   * Sets space url.
   *
   * @param spaceURL <code>char</code>
   */
  public void setSpaceURL(String spaceURL) {
    this.spaceURL = spaceURL;
  }
  
  /**
   * Get current user name.
   *
   * @return
   */
  public String getCurrentUserName() {
    RequestContext context = RequestContext.getCurrentInstance();
    return context.getRemoteUser();
  }

  /**
   * Get current rest context name.
   *
   * @return
   */
  protected String getRestContextName() {
    return PortalContainer.getCurrentRestContextName();
  }
  
  /**
   * Get portal name.
   *
   * @return
   */
  protected String getPortalName() {
    return PortalContainer.getCurrentPortalContainerName();
  }

  /**
   * gets list of pending users in a space
   * @return list of pending users
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<String> getPendingUsers() throws Exception {
    List<String> pendingUsersList = new ArrayList<String>();
    SpaceService spaceService = getSpaceService();
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null) {
      return new ArrayList<String>(0);
    }
    String[] pendingUsers = space.getPendingUsers();
    if(pendingUsers != null) {
      pendingUsersList.addAll(Arrays.asList(pendingUsers));
    }

    int currentPage = iteratorPendingUsers.getCurrentPage();
    LazyPageList<String> pageList = new LazyPageList<String>(new StringListAccess(pendingUsersList), ITEMS_PER_PAGE);
    iteratorPendingUsers.setPageList(pageList);
    int pageCount = iteratorPendingUsers.getAvailablePage();
    if (pageCount >= currentPage) {
      iteratorPendingUsers.setCurrentPage(currentPage);
    } else if (pageCount < currentPage) {
      iteratorPendingUsers.setCurrentPage(currentPage - 1);
    }
    return iteratorPendingUsers.getCurrentPageData();
  }

  /**
   * gets list of invited users in a space
   * @return lists of invited users
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<String> getInvitedUsers() throws Exception {
    List<String> invitedUsersList = new ArrayList<String>();
    SpaceService spaceService = getSpaceService();
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null) {
      return new ArrayList<String>(0);
    }
    String[] invitedUsers = space.getInvitedUsers();
    if(invitedUsers != null) {
      invitedUsersList.addAll(Arrays.asList(invitedUsers));
    }

    int currentPage = iteratorInvitedUsers.getCurrentPage();
    LazyPageList<String> pageList = new LazyPageList<String>(new StringListAccess(invitedUsersList), ITEMS_PER_PAGE);
    iteratorInvitedUsers.setPageList(pageList);
    int pageCount = iteratorInvitedUsers.getAvailablePage();
    if (pageCount >= currentPage) {
      iteratorInvitedUsers.setCurrentPage(currentPage);
    } else if (pageCount < currentPage) {
      iteratorInvitedUsers.setCurrentPage(currentPage - 1);
    }

    return iteratorInvitedUsers.getCurrentPageData();
  }

  /**
   * gets list of existing users in a space
   * @return list of existing users
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<String> getExistingUsers() throws Exception {
    SpaceService spaceService = getSpaceService();
    Space space  = spaceService.getSpaceById(spaceId);
    if (space == null) {
      return new ArrayList<String>(0);
    }
    int currentPage = iteratorExistingUsers.getCurrentPage();
    LazyPageList<String> pageList = new LazyPageList<String>(new StringListAccess(spaceService.getMembers(space)), ITEMS_PER_PAGE);
    iteratorExistingUsers.setPageList(pageList);
    if (this.isNewSearch()) {
      iteratorExistingUsers.setCurrentPage(FIRST_PAGE);
    } else {
      iteratorExistingUsers.setCurrentPage(currentPage);
    }
    this.setNewSearch(false);
    return iteratorExistingUsers.getCurrentPageData();
  }

  /**
   * sets users name to the input string
   * @param userName string of users name
   */
  public void setUsersName(String userName) {
    getUIStringInput(USER).setValue(userName);
  }

  /**
   * gets string of user names input: a, b, c
   * @return string of user names input
   */
  public String getUsersName() {
    return getUIStringInput(USER).getValue();
  }

  /**
   * checks whether the remote user is super user
   * @return true or false
   * @throws Exception
   */
  public boolean isSuperUser() throws Exception {
    return getRemoteUser().equals(getUserACL().getSuperUser());
  }

  /**
   * gets spaceUrl
   * @return string homespace url
   * @throws Exception
   */
  public String getHomeSpaceUrl() throws Exception {
   Space space = getSpaceService().getSpaceById(spaceId);
   return Util.getPortalRequestContext().getPortalURI() + space.getUrl();
  }

  /**
   * gets Manage Spaces Url (UIManageMySpaces)
   * @return manage spaces url
   * @throws Exception
   */
  public String getManageSpacesUrl() throws Exception {
    //TODO hoatle: Hard-coded
    return Util.getPortalRequestContext().getPortalURI() + "spaces";
  }

  /**
   * checks if a user is a leader of a space.
   * @param userName logged-in user
   * @return true or false
   * @throws Exception
   */
  public boolean isLeader(String userName) throws Exception {
    SpaceService spaceService = getSpaceService();
    Space space = spaceService.getSpaceById(spaceId);
    return spaceService.isLeader(space, userName);
  }

  public boolean isCurrentUser(String userName) throws Exception {
	return (getRemoteUser().equals(userName));
  }

  /**
   * triggers this action when user click on "invite" button.
   * @author hoatle
   *
   */
  static public class InviteActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIApplication uiApp = requestContext.getUIApplication();
      SpaceService spaceService = uiSpaceMember.getSpaceService();
      uiSpaceMember.validateInvitedUser();
      Space space = spaceService.getSpaceById(uiSpaceMember.spaceId);
      String usersInput = uiSpaceMember.getUsersName();
      String[] invitedUsers = null;
      String name = null;
      if (usersInput != null) {
    	  invitedUsers = uiSpaceMember.getUsersName().split(",");

		  List<String> usersForInviting = new ArrayList<String>();
		  if (invitedUsers != null)
		  {
			  for (int idx = 0; idx < invitedUsers.length; idx++) {
				  name = invitedUsers[idx].trim();
				  if ((name.length() > 0) && !usersForInviting.contains(name)) usersForInviting.add(name);
			  }
		  }

		  for (String userName : usersForInviting) {
			spaceService.inviteMember(space, userName);
		  }

		  uiSpaceMember.setUsersName(null);
      }
      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
      requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
    }
  }


  /**
   * triggers this action when user clicks on "search users" button.
   * @author hoatle
   *
   */
  static public class SearchUserActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      UIPopupWindow searchUserPopup = uiSpaceMember.getChild(UIPopupWindow.class);
      UIUserSelector userSelector = uiSpaceMember.createUIComponent(UIUserSelector.class, null, null);
      uiSpaceMember.setNewSearch(true);
      userSelector.setShowSearchGroup(false);
      searchUserPopup.setUIComponent(userSelector);
      searchUserPopup.setShow(true);
    }
  }

  /**
   * triggers this action when user clicks on "revoke invited" button.
   * @author hoatle
   *
   */
  static public class RevokeInvitedUserActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIApplication uiApp = requestContext.getUIApplication();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);

      SpaceService spaceService = uiSpaceMember.getSpaceService();
      try {
        spaceService.revokeInvitation(uiSpaceMember.spaceId, userName);
      } catch (SpaceException se) {
        uiApp.addMessage(new ApplicationMessage(MSG_ERROR_REVOKE_INVITED, null, ApplicationMessage.WARNING));
      }
      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }

  /**
   * triggers this action when user clicks on "decline user's request" button.
   * @author hoatle
   *
   */
  static public class DeclineUserActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIApplication uiApp = requestContext.getUIApplication();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);

      SpaceService spaceService = uiSpaceMember.getSpaceService();
      try {
        spaceService.declineRequest(uiSpaceMember.spaceId, userName);
      } catch (SpaceException se) {
        uiApp.addMessage(new ApplicationMessage(MSG_ERROR_DECLINE_USER, null, ApplicationMessage.WARNING));
      }
      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }

  /**
   * triggers this action when user clicks on "remove user" button.
   * @author hoatle
   *
   */
  static public class RemoveUserActionListener extends EventListener<UISpaceMember> {

    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      PortalRequestContext prContext = Util.getPortalRequestContext();
      boolean useAjax = prContext.useAjax();
      UIApplication uiApp = requestContext.getUIApplication();
      SpaceService spaceService = uiSpaceMember.getSpaceService();
      Space space = spaceService.getSpaceById(uiSpaceMember.spaceId);
      String currentUser = requestContext.getRemoteUser();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);
      if (!useAjax) userName = currentUser;

      if (spaceService.isOnlyLeader(space, userName)) {
        uiApp.addMessage(new ApplicationMessage(MSG_ERROR_SELF_REMOVE_LEADER, null, ApplicationMessage.WARNING));
        return;
      }
      
      try {
          spaceService.removeMember(space, userName);
      } catch(SpaceException se) {
          uiApp.addMessage(new ApplicationMessage(MSG_ERROR_REMOVE_MEMBER, null, ApplicationMessage.WARNING));
          return;
      }

      if (!useAjax) { // self remove.
        prContext = Util.getPortalRequestContext();
        prContext.setResponseComplete(true);
        StringBuffer url = new StringBuffer(Util.getPortalRequestContext().getPortalURI());
        if(uiSpaceMember.isSuperUser()) {
          url.append(space.getUrl()).append("/SpaceSettingPortlet");
        } else {
          url.append("spaces");
        }
        prContext.getResponse().sendRedirect(url.toString());
        return;
      } else { // remove other and use ajax to update.
        requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
      }
    }
  }

  /**
   * triggers this action when user click on "validate user's request" button
   * @author hoatle
   *
   */
  static public class ValidateUserActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIApplication uiApp = requestContext.getUIApplication();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);

      SpaceService spaceService = uiSpaceMember.getSpaceService();
      try {
        spaceService.validateRequest(uiSpaceMember.spaceId, userName);
      } catch (SpaceException se) {
        uiApp.addMessage(new ApplicationMessage(MSG_ERROR_VALIDATE_USER, null, ApplicationMessage.WARNING));
      }
      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }

  /**
   * triggers this action when user click on "remove leader" button
   * @author hoatle
   *
   */
  static public class RemoveLeaderActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIApplication uiApp = requestContext.getUIApplication();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);
      PortalRequestContext prContext = Util.getPortalRequestContext();
      String currentUser = requestContext.getRemoteUser();
      boolean useAjax = prContext.useAjax();
      SpaceService spaceService = uiSpaceMember.getSpaceService();
      Space space = spaceService.getSpaceById(uiSpaceMember.spaceId);
      
      if (!useAjax) userName = currentUser;
      
      if (spaceService.isOnlyLeader(space, userName)) {
        uiApp.addMessage(new ApplicationMessage(MSG_ERROR_SELF_REMOVE_LEADER, null, ApplicationMessage.WARNING));
        return;
      }
      
      try {
        spaceService.setLeader(space, userName, false);
      } catch(SpaceException se) {
        uiApp.addMessage(new ApplicationMessage(MSG_ERROR_REMOVE_LEADER, null, ApplicationMessage.WARNING));
      }
      if (!useAjax) { // self remove.
        prContext = Util.getPortalRequestContext();
        prContext.setResponseComplete(true);
        StringBuffer url = new StringBuffer(Util.getPortalRequestContext().getPortalURI());
        if(uiSpaceMember.isSuperUser()) {
          url.append(space.getUrl()).append("/SpaceSettingPortlet");
        } else {
          url.append(space.getUrl());
        }
        prContext.getResponse().sendRedirect(url.toString());
        return;
      } else {
        requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
      }
    }
  }

  /**
   * triggers this action when user clicks on "set leader" button
   * @author hoatle
   *
   */
  static public class MakeLeaderActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIApplication uiApp = requestContext.getUIApplication();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);

      SpaceService spaceService = uiSpaceMember.getSpaceService();
      Space space = spaceService.getSpaceById(uiSpaceMember.spaceId);
      try {
        spaceService.setLeader(space, userName, true);
      } catch (SpaceException se) {
        uiApp.addMessage(new ApplicationMessage(MSG_ERROR_MAKE_LEADER, null, ApplicationMessage.WARNING));
      }
      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }

  /**
   * triggers this action when user click on "add" button.
   * @author hoatle
   *
   */
  static public class AddActionListener extends EventListener<UIUserSelector> {
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiForm = event.getSource();
      UISpaceMember uiSpaceMember = uiForm.getAncestorOfType(UISpaceMember.class);
      String userNamesSelected = uiForm.getSelectedUsers();
      String userNamesInputted = uiSpaceMember.getUsersName();
      String userNameForInvite = null;
      if ((userNamesInputted == null) || (userNamesInputted.length() == 0)) {
    	  userNameForInvite = userNamesSelected;
      } else {
       	  userNameForInvite = userNamesInputted.trim() + ", " + userNamesSelected;
      }

      uiSpaceMember.setUsersName(userNameForInvite);
      UIPopupWindow uiPopup = uiSpaceMember.getChild(UIPopupWindow.class);
      uiPopup.setUIComponent(null);
      uiPopup.setShow(false);
      uiSpaceMember.validateInvitedUser();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }

  /**
   * triggers this action when user clicks on popup's close button.
   * @author hoatle
   *
   */
  static public class CloseActionListener extends EventListener<UIUserSelector> {
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiForm = event.getSource();
      UISpaceMember uiSpaceMember = uiForm.getAncestorOfType(UISpaceMember.class);
      UIPopupWindow uiPopup = uiSpaceMember.getChild(UIPopupWindow.class);
      uiPopup.setUIComponent(null);
      uiPopup.setShow(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }

  /**
   * validates invited users for checking if any error happens.
   * @throws Exception
   */
  private void validateInvitedUser() throws Exception {
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    UIApplication uiApp = requestContext.getUIApplication();
    String[] invitedUserList = getUsersName().split(",");
    String usersNotExist = null;
    String usersIsInvited = null;
    String usersIsMember = null;
    String newMemberForInvite = null;
    SpaceService spaceService = getSpaceService();
    Space space = spaceService.getSpaceById(spaceId);
    String invitedUser = null;
    for(String userStr : invitedUserList){
      invitedUser = userStr.trim();

      if (invitedUser.length() == 0) continue;

      try {
        if (invitedUser.equals(getUserACL().getSuperUser())) {
          //BUG #SOC-539
          if (spaceService.isMember(space, invitedUser)) {
            throw new SpaceException(SpaceException.Code.USER_ALREADY_MEMBER);
          } else {
            spaceService.addMember(space, invitedUser);
          }
        } else {
        	checkInvitedUser(space, invitedUser);
        	if (newMemberForInvite == null) {
        		newMemberForInvite = invitedUser;
        	} else {
        		newMemberForInvite += ", " + invitedUser;
        	}
        }
      } catch (SpaceException e) {
        if(e.getCode() == SpaceException.Code.USER_NOT_EXIST) {
          if(usersNotExist == null) usersNotExist = invitedUser;
          else usersNotExist += "," +invitedUser;
        } else if (e.getCode() == SpaceException.Code.USER_ALREADY_INVITED) {
          if(usersIsInvited == null) usersIsInvited = invitedUser;
          else usersIsInvited += "," +invitedUser;
        } else if(e.getCode() == SpaceException.Code.USER_ALREADY_MEMBER) {
          if(usersIsMember == null) usersIsMember = invitedUser;
          else usersIsMember += "," +invitedUser;
        }
      }
    }
    setUsersName(newMemberForInvite);
    String remainUsers = null;
    if(usersNotExist != null){
      remainUsers = usersNotExist;
      uiApp.addMessage(new ApplicationMessage("UISpaceMember.msg.user-not-exist", new String[] {usersNotExist},ApplicationMessage.WARNING));
    }
    if(usersIsInvited != null){
      if(remainUsers == null) remainUsers = usersIsInvited;
      else remainUsers += "," + usersIsInvited;
      uiApp.addMessage(new ApplicationMessage("UISpaceMember.msg.user-is-invited",new String[] {usersIsInvited},ApplicationMessage.WARNING));
    }
    if(usersIsMember != null){
      if(remainUsers == null) remainUsers = usersIsMember;
      else remainUsers += "," + usersIsMember;
      uiApp.addMessage(new ApplicationMessage("UISpaceMember.msg.user-is-member",new String[] {usersIsMember},ApplicationMessage.WARNING));
    }
//    setUsersName(remainUsers);
  }

  private void checkInvitedUser(Space space, String userId) throws Exception{
	  OrganizationService orgService = getApplicationComponent(OrganizationService.class);
	  SpaceService spaceService = getSpaceService();
	    try {
	      User user = orgService.getUserHandler().findUserByName(userId);
	      if(user == null) {
	        throw new SpaceException(SpaceException.Code.USER_NOT_EXIST);
	      }
	    } catch (Exception e) {
	      if(e instanceof SpaceException)
	        throw (SpaceException)e;
	      throw new SpaceException(SpaceException.Code.ERROR_RETRIEVING_USER, e);
	    }

	    if(spaceService.isInvited(space, userId)) {
	      throw new SpaceException(SpaceException.Code.USER_ALREADY_INVITED);
	    } else if (spaceService.isMember(space, userId) && !userId.equals(getUserACL().getSuperUser())) {
	      throw new SpaceException(SpaceException.Code.USER_ALREADY_MEMBER);
	    }
  }

  /**
   * gets spaceService
   * @return spaceService
   * @see SpaceService
   */
  private SpaceService getSpaceService() {
    if(spaceService == null) {
      spaceService = getApplicationComponent(SpaceService.class);
    }
    return spaceService;
  }

  /**
   * gets userACL
   * @return userACL
   * @throws Exception
   */
  private UserACL getUserACL() throws Exception {
    return getApplicationComponent(UserACL.class);
  }

  /**
   * gets remoteUser ~ currently logged-in user
   * @return remoteUser
   * @throws Exception
   */
  private String getRemoteUser() throws Exception {
    return Util.getPortalRequestContext().getRemoteUser();
  }

  public boolean isNewSearch() {
    return isNewSearch;
  }

  public void setNewSearch(boolean isNewSearch) {
    this.isNewSearch = isNewSearch;
  }
}
