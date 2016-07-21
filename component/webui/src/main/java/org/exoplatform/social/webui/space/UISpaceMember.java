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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.StringListAccess;
import org.exoplatform.social.webui.UIUserInvitation;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.organization.account.UIUserSelector;


@ComponentConfigs ({
  @ComponentConfig(
     template =  "war:/groovy/social/webui/space/UISpaceMember.gtmpl",
     events = {
       @EventConfig(listeners = UISpaceMember.RevokeInvitedUserActionListener.class),
       @EventConfig(listeners = UISpaceMember.DeclineUserActionListener.class),
       @EventConfig(listeners = UISpaceMember.ValidateUserActionListener.class),
       @EventConfig(listeners = UISpaceMember.RemoveUserActionListener.class),
       @EventConfig(listeners = UISpaceMember.ToggleLeadershipActionListener.class)
       }
 )
})

public class UISpaceMember extends UIContainer {

  private static final String MSG_ERROR_SELF_REMOVE_LEADER = "UISpaceMember.msg.error_self_remove_leader";
  private static final String MSG_ERROR_SELF_REMOVE_LEADER_YOU = "UISpaceMember.msg.error_self_remove_leader_you";
  private static final String MSG_ERROR_SELF_REMOVE_LEADER_ARE = "UISpaceMember.msg.error_self_remove_leader_are";
  private static final String MSG_ERROR_SELF_REMOVE_LEADER_IS = "UISpaceMember.msg.error_self_remove_leader_is";
  private static final String MSG_ERROR_SELF_REMOVE_LEADER_LEAVING_IT = "UISpaceMember.msg.error_self_remove_leader_leaving_it";
  private static final String MSG_ERROR_SELF_REMOVE_LEADER_REMOVING_THE_RIGHTS = "UISpaceMember.msg.error_self_remove_leader_removing_the_rights";
  /**
   * The first page.
   */
  private static final int FIRST_PAGE = 1;

  private String spaceId;
  private SpaceService spaceService = null;
  private UIPageIterator iteratorPendingUsers;
  private UIPageIterator iteratorInvitedUsers;
  private UIPageIterator iteratorExistingUsers;
  private final String iteratorPendingID = "UIIteratorPendding";
  private final String iteratorInvitedID = "UIIteratorInvited";
  private final String iteratorExistingID = "UIIteratorExisting";
  private final Integer PENDING_PER_PAGE = 10;
  private final Integer INVITATION_PER_PAGE = 10;
  private final Integer MEMBERS_PER_PAGE = 20;
  private static final String USER_TO_INVITE = "user_to_invite";
  String typeOfRelation = null;
  String spaceURL = null;
  private boolean hasErr = false;
  
  /**
   * The flag notifies a new search when clicks search icon or presses enter.
   */
  private boolean isNewSearch;

  /**
   * Constructor.
   *
   * @throws Exception
   */
  public UISpaceMember() throws Exception {
    addChild(UIUserInvitation.class, null, null);
    iteratorPendingUsers = createUIComponent(UIPageIterator.class, null, iteratorPendingID);
    iteratorInvitedUsers = createUIComponent(UIPageIterator.class, null, iteratorInvitedID);
    iteratorExistingUsers = createUIComponent(UIPageIterator.class, null, iteratorExistingID);
    addChild(iteratorPendingUsers);
    addChild(iteratorInvitedUsers);
    addChild(iteratorExistingUsers);
    setTypeOfRelation(USER_TO_INVITE);
  }

  /**
   * Gets uiPageIteratorPendingUsers
   *
   * @return uiPageIterator
   */
  public UIPageIterator getUIPageIteratorPendingUsers() {
    return iteratorPendingUsers;
  }

  /**
   * Gets uiPageIteratorInvitedUsers to display; revoke
   *
   * @return uiPageIteraatorInvitedUsers
   */
  public UIPageIterator getUIPageIteratorInvitedUsers() {
    return iteratorInvitedUsers;
  }

  /**
   * Gets uiPageIteratorExistingUsers to display; remove or set/remove leader.
   *
   * @return uiPageiteratorExistingUsers
   */
  public UIPageIterator getUIPageIteratorExistingUsers() {
    return iteratorExistingUsers;
  }

  /**
   * Sets spaceId to this for getting current space
   *
   * @param spaceId
   * @throws Exception
   */
  public void setValue(String spaceId) throws Exception {
    this.spaceId = spaceId;
  }


  /**
   * Gets type of relation with current user.
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
   * @return the hasErr
   */
  public boolean isHasErr() {
    return hasErr;
  }

  /**
   * @param hasErr the hasErr to set
   */
  public void setHasErr(boolean hasErr) {
    this.hasErr = hasErr;
  }

  /**
   * Gets current user name.
   *
   * @return
   */
  public String getCurrentUserName() {
    RequestContext context = RequestContext.getCurrentInstance();
    return context.getRemoteUser();
  }

  /**
   * Gets current rest context name.
   *
   * @return
   */
  protected String getRestContextName() {
    return PortalContainer.getCurrentRestContextName();
  }

  /**
   * Gets portal name.
   *
   * @return
   */
  protected String getPortalName() {
    return PortalContainer.getCurrentPortalContainerName();
  }

  /**
   * Gets list of pending users in a space
   *
   * @return list of pending users
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<String> getPendingUsers() throws Exception {
    SpaceService spaceService = getSpaceService();
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null) {
      return new ArrayList<String>(0);
    }
    
    String[] pendingUsers = space.getPendingUsers();
    if (pendingUsers == null || pendingUsers.length == 0) {
      return new ArrayList<String>();
    }
    
    int currentPage = iteratorPendingUsers.getCurrentPage();
    LazyPageList<String> pageList = new LazyPageList<String>(
                                      new StringListAccess(Arrays.asList(pendingUsers)),
                                      PENDING_PER_PAGE);
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
   * Gets list of invited users in a space
   *
   * @return lists of invited users
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<String> getInvitedUsers() throws Exception {
    SpaceService spaceService = getSpaceService();
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null) {
      return new ArrayList<String>(0);
    }
    
    String[] invitedUsers = space.getInvitedUsers();
    if (invitedUsers == null || invitedUsers.length == 0) {
      return new ArrayList<String>();
    }
    
    int currentPage = iteratorInvitedUsers.getCurrentPage();
    LazyPageList<String> pageList = new LazyPageList<String>(
                                      new StringListAccess(Arrays.asList(invitedUsers)),
                                      INVITATION_PER_PAGE);
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
   * Gets list of existing users in a space
   *
   * @return list of existing users
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<String> getExistingUsers() throws Exception {
    SpaceService spaceService = getSpaceService();
    Space space = spaceService.getSpaceById(spaceId);
    if (space == null) {
      return new ArrayList<String>(0);
    }
    
    String[] memberUsers = space.getMembers();
    if (memberUsers == null || memberUsers.length == 0) {
      return new ArrayList<String>();
    }
    
    int currentPage = iteratorExistingUsers.getCurrentPage();
    Set<String> users = new HashSet<String>(Arrays.asList(memberUsers));
    users.addAll(SpaceUtils.findMembershipUsersByGroupAndTypes(space.getGroupId(), MembershipTypeHandler.ANY_MEMBERSHIP_TYPE));
    
    LazyPageList<String> pageList = new LazyPageList<String>(new StringListAccess(new ArrayList<String>(users)), MEMBERS_PER_PAGE);
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
   * Sets users name to the input string
   *
   * @param userName string of users name
   */
  public void setUsersName(String userName) {
//    getUIStringInput(USER).setValue(userName);
  }

  /**
   * Gets string of user names input: a, b, c
   *
   * @return string of user names input
   */
  public String getUsersName() {
//    String value = getUIStringInput(USER).getValue();
//    return value != null ? value : "";
    return null;
  }

  /**
   * Checks whether the remote user is super user
   *
   * @return true or false
   * @throws Exception
   */
  public boolean isSuperUser() throws Exception {
    return getRemoteUser().equals(getUserACL().getSuperUser());
  }

  /**
   * Gets spaceUrl
   *
   * @return string homespace url
   * @throws Exception
   */
  public String getHomeSpaceUrl() throws Exception {
    Space space = getSpaceService().getSpaceById(spaceId);
    return Utils.getSpaceHomeURL(space);
  }

  /**
   * Gets Manage Spaces Url (UIManageMySpaces)
   *
   * @return manage spaces url
   * @throws Exception
   */
  public String getManageSpacesUrl() throws Exception {
    //TODO hoatle: Hard-coded
    return Utils.getURI("spaces");
  }

  /**
   * Checks if a user is a leader of a space.
   *
   * @param userName logged-in user
   * @return true or false
   * @throws Exception
   */
  public boolean isLeader(String userName) throws Exception {
    SpaceService spaceService = getSpaceService();
    Space space = spaceService.getSpaceById(spaceId);
    return spaceService.isManager(space, userName);
  }

  /**
   * Checks if user has wild card membership.
   * 
   * @param userId target user to check.
   * @return true if user has wild card membership in space.
   */
  protected boolean hasWildCardMembership(String userId) {
    SpaceService spaceService = getSpaceService();
    Space space = spaceService.getSpaceById(spaceId);
    return SpaceUtils.isUserHasMembershipTypesInGroup(userId, space.getGroupId(), MembershipTypeHandler.ANY_MEMBERSHIP_TYPE);
  }
  
  public boolean isCurrentUser(String userName) throws Exception {
    return (getRemoteUser().equals(userName));
  }

  /**
   * Triggers this action when user clicks on "revoke invited" button.
   *
   * @author hoatle
   */
  static public class RevokeInvitedUserActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);

      SpaceService spaceService = uiSpaceMember.getSpaceService();
      spaceService.removeInvitedUser(spaceService.getSpaceById(uiSpaceMember.spaceId), userName);
      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }

  /**
   * Triggers this action when user clicks on "decline user's request" button.
   *
   * @author hoatle
   */
  static public class DeclineUserActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);

      SpaceService spaceService = uiSpaceMember.getSpaceService();
      spaceService.removePendingUser(spaceService.getSpaceById(uiSpaceMember.spaceId), userName);
      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }

  /**
   * Triggers this action when user clicks on "remove user" button.
   *
   * @author hoatle
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
      if (!useAjax) {
        userName = currentUser;
      }

      if (spaceService.isOnlyManager(space, userName)) {
        uiApp.addMessage(new ApplicationMessage(MSG_ERROR_SELF_REMOVE_LEADER,
                                                uiSpaceMember.makeParamSelfRemoveLeaderErrorMessage(userName, currentUser),
                                                ApplicationMessage.WARNING));
        return;
      }

      spaceService.removeMember(space, userName);
      spaceService.setManager(spaceService.getSpaceById(space.getId()), userName, false);

      if (!useAjax) { // self remove.
        prContext = Util.getPortalRequestContext();
        prContext.setResponseComplete(true);
        StringBuffer url = new StringBuffer();
        if (uiSpaceMember.isSuperUser()) {
          url.append(Utils.getSpaceHomeURL(space)).append("/SpaceSettingPortlet");
        } else {
          url.append(Utils.getURI("spaces"));
        }
        prContext.getResponse().sendRedirect(url.toString());
        return;
      } else { // remove other and use ajax to update.
        requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
      }
    }
  }

  /**
   * Triggers this action when user click on "validate user's request" button
   *
   * @author hoatle
   */
  static public class ValidateUserActionListener extends EventListener<UISpaceMember> {
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      String userName = event.getRequestContext().getRequestParameter(OBJECTID);

      SpaceService spaceService = uiSpaceMember.getSpaceService();
      spaceService.addMember(spaceService.getSpaceById(uiSpaceMember.spaceId), userName);
      requestContext.addUIComponentToUpdateByAjax(uiSpaceMember);
    }
  }

  static public class ToggleLeadershipActionListener extends EventListener<UISpaceMember> {
    @Override
    public void execute(Event<UISpaceMember> event) throws Exception {
      UISpaceMember uiSpaceMember = event.getSource();
      WebuiRequestContext rcontext = event.getRequestContext();
      String targetUser = rcontext.getRequestParameter(OBJECTID);
      SpaceService spaceService = uiSpaceMember.getSpaceService();
      Space space = spaceService.getSpaceById(uiSpaceMember.spaceId);
      space.setEditor(Utils.getViewerRemoteId());
      
      boolean success = false;
      if (spaceService.isManager(space, targetUser)) {
        if (spaceService.isOnlyManager(space, targetUser)) {
          UIApplication uiApp = rcontext.getUIApplication();
          String currentUser = rcontext.getRemoteUser();
          
          //
          uiApp.addMessage(new ApplicationMessage(MSG_ERROR_SELF_REMOVE_LEADER, uiSpaceMember.makeParamSelfRemoveLeaderErrorMessage(targetUser, currentUser), ApplicationMessage.WARNING));
          return;
        }
        spaceService.setManager(space, targetUser, false);
        success = true;
      } else if (spaceService.isMember(space, targetUser)) {
        spaceService.setManager(space, targetUser, true);
        success = true;
      }

      if (success) {
        ((WebuiRequestContext) rcontext.getParentAppRequestContext()).setResponseComplete(true);
      }
    }
  }
  
  /**
   * Triggers this action when user clicks on popup's close button.
   *
   * @author hoatle
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

  protected boolean isMember(String userId) {
    SpaceService spaceService = getSpaceService();
    Space space = spaceService.getSpaceById(spaceId);
    try {
      if (ArrayUtils.contains(space.getMembers(), userId)) {
        return true;
      }
    } catch (Exception e) {
      return false;
    }
    return false;
  }

  /**
   * Gets spaceService
   *
   * @return spaceService
   * @see SpaceService
   */
  private SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = getApplicationComponent(SpaceService.class);
    }
    return spaceService;
  }

  /**
   * Gets userACL
   *
   * @return userACL
   * @throws Exception
   */
  private UserACL getUserACL() throws Exception {
    return getApplicationComponent(UserACL.class);
  }

  /**
   * Gets remoteUser ~ currently logged-in user
   *
   * @return remoteUser
   * @throws Exception
   */
  private String getRemoteUser() throws Exception {
    return Util.getPortalRequestContext().getRemoteUser();
  }
  
  /**
   * Get full name from userId.
   * 
   * @param userId
   * @return Full name
   */
  public String getFullName(String userId) {
    
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    IdentityManager idm = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    Identity identity = idm.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, true);
    
    if (identity == null) return StringUtils.EMPTY;
    
    return identity.getProfile().getFullName();
  }

  public boolean isNewSearch() {
    return isNewSearch;
  }

  public void setNewSearch(boolean isNewSearch) {
    this.isNewSearch = isNewSearch;
  }
  
  private String[] makeParamSelfRemoveLeaderErrorMessage(String userName, String currentUser) {
    if (userName == null || currentUser == null) {
      return null;
    }
    if (currentUser.equals(userName)) {
      return new String[] {MSG_ERROR_SELF_REMOVE_LEADER_YOU, MSG_ERROR_SELF_REMOVE_LEADER_ARE, MSG_ERROR_SELF_REMOVE_LEADER_LEAVING_IT};
    }
    return new String[] {getFullName(userName), MSG_ERROR_SELF_REMOVE_LEADER_IS, MSG_ERROR_SELF_REMOVE_LEADER_REMOVING_THE_RIGHTS};
  }
}
