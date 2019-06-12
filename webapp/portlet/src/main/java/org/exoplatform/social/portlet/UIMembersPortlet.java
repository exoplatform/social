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
package org.exoplatform.social.portlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.SpaceMemberFilterListAccess.Type;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.UIUserInvitation;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.profile.UIProfileUserSearch;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;


@ComponentConfigs ({
  @ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template = "app:/groovy/social/portlet/UIMembersPortlet.gtmpl",
    events = {
      @EventConfig(listeners = UIMembersPortlet.ConnectActionListener.class),
      @EventConfig(listeners = UIMembersPortlet.ConfirmActionListener.class),
      @EventConfig(listeners = UIMembersPortlet.IgnoreActionListener.class),
      @EventConfig(listeners = UIMembersPortlet.SearchActionListener.class),
      @EventConfig(listeners = UIMembersPortlet.LoadMoreManagerActionListener.class),
      @EventConfig(listeners = UIMembersPortlet.LoadMoreMemberActionListener.class)
    }
  )
})
public class UIMembersPortlet extends UIPortletApplication {
  private static final Log LOG = ExoLogger.getLogger(UIMembersPortlet.class);

  private ListAccess<Identity> memberListAccess;
  private ListAccess<Identity> managerListAccess;
  private List<Identity> memberList;
  private List<Identity> managerList;

  private int memberNum;
  private int managerNum;
  
  private ProfileFilter memberProfileFilter;
  private ProfileFilter managerProfileFilter;

  /**
   * Members to display per page = 3*4 to display items on lines correctly
   * Else we can see lines with empty places added into the end and just after
   * we have th show more button 
   */
  private final int MEMBER_PER_PAGE = 48;
  private final int MANAGER_PER_PAGE = 12;
  private static final String SPACE_MEMBER = "member_of_space";
  private static final String ALL_FILTER = "All";
  public static final String SEARCH = "Search";
  private static final char EMPTY_CHARACTER = '\u0000';
  private static final String INVITATION_REVOKED_INFO = "UIMembersPortlet.label.RevokedInfo";
  private static final String INVITATION_ESTABLISHED_INFO = "UIMembersPortlet.label.InvitationEstablishedInfo";

  private int currentLoadIndex = 0;
  private int currentLoadManagerIndex = 0;
  private IdentityManager identityManager_ = null;
  private UIProfileUserSearch uiSearchMemberOfSpace = null;

  boolean enableLoadNext;
  boolean enableLoadManagerNext;
  private boolean loadAtEnd;
  private String selectedChar = null;

  private String msg;
  
//  private static final int FIRST_PAGE = 1;

  /**
   * constructor
   *
   * @throws Exception
   */
  public UIMembersPortlet() throws Exception {    
    uiSearchMemberOfSpace = createUIComponent(UIProfileUserSearch.class, null, "UIProfileUserSearch");
    uiSearchMemberOfSpace.setTypeOfRelation(SPACE_MEMBER);
    uiSearchMemberOfSpace.setSpaceURL(getSpace().getUrl());
    uiSearchMemberOfSpace.setHasPeopleTab(false);
    addChild(uiSearchMemberOfSpace);

    initMember();
    initManager();

    boolean isAdmin = false;
    String currentUser = Utils.getViewerRemoteId();
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    Space space = getSpace();

    isAdmin = spaceService.isSuperManager(currentUser) ||
            spaceService.isManager(space, currentUser);
    if (isAdmin) {
      addChild(UIUserInvitation.class, null, null);
    }
  }

  public void setMemberListAccess(ListAccess<Identity> memberListAccess){
    this.memberListAccess = memberListAccess;
  }
  
  public ListAccess<Identity> getMemberListAccess(){
    return this.memberListAccess;
  }
  
  public void setManagerListAccess(ListAccess<Identity> managerListAccess){
    this.managerListAccess = managerListAccess;
  }
  
  public ListAccess<Identity> getManagerListAccess(){
    return this.managerListAccess;
  }
  
  public int getMemberNum() {
    return memberNum;
  }

  public void setMemberNum(int memberNum) {
    this.memberNum = memberNum;
  }

  public int getManagerNum() {
    return managerNum;
  }

  public void setManagerNum(int managerNum) {
    this.managerNum = managerNum;
  }
  
  /**
   * Gets selected character when search by alphabet.
   *
   * @return The selected character.
   */
  public final String getSelectedChar() {
    return selectedChar;
  }

  /**
   * Sets selected character to variable.
   *
   * @param selectedChar <code>char</code>
   */
  public final void setSelectedChar(final String selectedChar) {
    this.selectedChar = selectedChar;
  }

  private List<Identity> loadPeople(int index, int length, Type type) throws Exception {
    Identity[] result = null;
    Space space = getSpace();
    if(Type.MEMBER.equals(type)){
      ProfileFilter filter = uiSearchMemberOfSpace.getProfileFilter();
      setMemberListAccess(Utils.getIdentityManager().getSpaceIdentityByProfileFilter(space, filter, type, true));

      setMemberNum(getMemberListAccess().getSize());
      uiSearchMemberOfSpace.setPeopleNum(getMemberNum());
      result  = getMemberListAccess().load(index, length);
    } else if(Type.MANAGER.equals(type)){
      ProfileFilter filter = managerProfileFilter;
      setManagerListAccess(Utils.getIdentityManager().getSpaceIdentityByProfileFilter(space, filter, type, true));
      setManagerNum(getManagerListAccess().getSize());
      result  = getManagerListAccess().load(index, length);
    }
    return Arrays.asList(result);
  }

  
  /**
   * set identity list
   *
   * @param identityList
   */
  public void setIdentityList(ListAccess<Identity> identityList) {
    this.memberListAccess = identityList;
  }

  /**
   * sets member list
   *
   * @param memberList
   */
  public void setMemberList(List<Identity> memberList) {
    this.memberList = memberList;
  }

  /**
   * gets member list
   *
   * @return leader list
   * @throws Exception
   */
  public List<Identity> getMemberList() throws Exception {
    int elementsToDisplay = currentLoadIndex + MEMBER_PER_PAGE;
    setMemberList(loadPeople(0, elementsToDisplay, Type.MEMBER));
    setEnableLoadNext(elementsToDisplay < memberNum);
    return memberList;
  }
  
  /**
   * sets leader list
   *
   * @param managerList
   */
  public void setManagerList(List<Identity> managerList) {
    this.managerList = managerList;
  }

  /**
   * gets leader list
   *
   * @return leader list
   * @throws Exception
   */
  public List<Identity> getManagerList() throws Exception {
    int elementsToDisplay = currentLoadManagerIndex + MANAGER_PER_PAGE;
    setManagerList(loadPeople(0, elementsToDisplay, Type.MANAGER));
    setEnableLoadManagerNext(elementsToDisplay < managerNum);
    return managerList;
  }
  

  /**
   * initialize members, called from {@link #getMemberList()}}
   *
   * @throws Exception
   */
  public void initMember() throws Exception {
    try{ 
      setLoadAtEnd(false);
      enableLoadNext = false;
      currentLoadIndex = 0;
      setSelectedChar(ALL_FILTER);
      memberProfileFilter = new ProfileFilter();
      memberProfileFilter.setViewerIdentity(Utils.getViewerIdentity());
      uiSearchMemberOfSpace.setProfileFilter(memberProfileFilter);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }

  /**
   * initialize leaders, called from {@link #getManagerList()}}
   *
   * @throws Exception
   */
  public void initManager() throws Exception {
    managerProfileFilter = new ProfileFilter();
    currentLoadManagerIndex = 0;
    enableLoadManagerNext = false;
  }


  /**
   * gets space, space identified by the url.
   *
   * @return space
   * @throws SpaceException
   */
  public Space getSpace() throws SpaceException {
    String spaceUrl = Utils.getSpaceUrlByContext();
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    return spaceService.getSpaceByUrl(spaceUrl);
  }


  /**
   * gets current path
   *
   * @return current path
   */
  public String getPath() {
    String nodePath = Util.getPortalRequestContext().getNodePath();
    String uriPath = Util.getPortalRequestContext().getRequestURI();
    return uriPath.replaceAll(nodePath, "");
  }

  /**
   * gets memberships of a user in a space.
   *
   * @param userName
   * @return string of membership name
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public String getMemberships(String userName) throws Exception {
    String memberShip = null;
    OrganizationService orgService = getApplicationComponent(OrganizationService.class);
    MembershipHandler memberShipHandler = orgService.getMembershipHandler();
    Collection<Membership> memberShips = memberShipHandler.findMembershipsByUserAndGroup(userName, getSpace().getGroupId());
    for (Membership aaa : memberShips) {
      if (memberShip == null) {
        memberShip = aaa.getMembershipType();
      } else {
        memberShip += "," + aaa.getMembershipType();
      }
    }
    return memberShip;
  }

  /**
   * Listens to add action then make request to invite person to make connection.<br> - Gets
   * information of user is invited.<br> - Checks the relationship to confirm that there have not
   * got connection yet.<br> - Saves the new connection.<br>
   */
  public static class ConnectActionListener extends EventListener<UIMembersPortlet> {
    public void execute(Event<UIMembersPortlet> event) throws Exception {
      UIMembersPortlet uiAllPeople = event.getSource();
      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      Identity invitedIdentity = Utils.getIdentityManager().getIdentity(userId, true);
      Identity invitingIdentity = Utils.getViewerIdentity();

      Relationship relationship = Utils.getRelationshipManager().get(invitingIdentity, invitedIdentity);
      uiAllPeople.setLoadAtEnd(false);
      
      if (relationship != null) {
        UIApplication uiApplication = event.getRequestContext().getUIApplication();
        uiApplication.addMessage(new ApplicationMessage(INVITATION_ESTABLISHED_INFO, null, ApplicationMessage.INFO));
        return;
      }
      
      Utils.getRelationshipManager().inviteToConnect(invitingIdentity, invitedIdentity);
      Utils.clearCacheOnUserPopup();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAllPeople);
    }
  }

  /**
   * Listens to accept actions then make connection to accepted person.<br> - Gets information of
   * user who made request.<br> - Checks the relationship to confirm that there still got invited
   * connection.<br> - Makes and Save the new relationship.<br>
   */
  public static class ConfirmActionListener extends EventListener<UIMembersPortlet> {
    public void execute(Event<UIMembersPortlet> event) throws Exception {
      UIMembersPortlet uiAllPeople = event.getSource();
      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      Identity invitedIdentity = Utils.getIdentityManager().getIdentity(userId, true);
      Identity invitingIdentity = Utils.getViewerIdentity();

      Relationship relationship = Utils.getRelationshipManager().get(invitingIdentity, invitedIdentity);
      uiAllPeople.setLoadAtEnd(false);
      
      if (relationship == null || relationship.getStatus() != Relationship.Type.PENDING) {
        UIApplication uiApplication = event.getRequestContext().getUIApplication();
        uiApplication.addMessage(new ApplicationMessage(INVITATION_REVOKED_INFO, null, ApplicationMessage.INFO));
        return;
      }
      
      Utils.clearCacheOnUserPopup();
      Utils.getRelationshipManager().confirm(invitedIdentity, invitingIdentity);
    }
  }

  /**
   * Listens to deny action then delete the invitation.<br> - Gets information of user is invited or
   * made request.<br> - Checks the relation to confirm that there have not got relation yet.<br> -
   * Removes the current relation and save the new relation.<br>
   */
  public static class IgnoreActionListener extends EventListener<UIMembersPortlet> {
    public void execute(Event<UIMembersPortlet> event) throws Exception {
      UIMembersPortlet   uiAllPeople = event.getSource();
      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      Identity inviIdentityIdentity = Utils.getIdentityManager().getIdentity(userId, true);
      Identity invitingIdentity = Utils.getViewerIdentity();

      Relationship relationship = Utils.getRelationshipManager().get(invitingIdentity, inviIdentityIdentity);
      
      uiAllPeople.setLoadAtEnd(false);
      if (relationship != null && relationship.getStatus() == Relationship.Type.CONFIRMED) {
        Utils.getRelationshipManager().delete(relationship);
        return;
      }
      
      if (relationship == null) {
        UIApplication uiApplication = event.getRequestContext().getUIApplication();
        uiApplication.addMessage(new ApplicationMessage(INVITATION_REVOKED_INFO, null, ApplicationMessage.INFO));
        return;
      }
      
      Utils.clearCacheOnUserPopup();
      Utils.getRelationshipManager().deny(inviIdentityIdentity, invitingIdentity);
    }
  }
  
  /**
   * triggers this action when user clicks on search button
   */
  public static class SearchActionListener extends EventListener<UIMembersPortlet> {
    @Override
    public void execute(Event<UIMembersPortlet> event) throws Exception {
      WebuiRequestContext ctx = event.getRequestContext();
      UIMembersPortlet uiMembersPortlet = event.getSource();
      
      UIProfileUserSearch uiSearch = uiMembersPortlet.uiSearchMemberOfSpace;
      
      String charSearch = ctx.getRequestParameter(OBJECTID);

      ProfileFilter filter = uiSearch.getProfileFilter();
      
      try {
        uiMembersPortlet.setSelectedChar(charSearch);
        if (charSearch != null) { // search by alphabet
          filter.setName("");
          filter.setPosition("");
          filter.setSkills("");
          filter.setFirstCharacterOfName(charSearch.toCharArray()[0]);
          if (ALL_FILTER.equals(charSearch)) {
            filter.setFirstCharacterOfName(EMPTY_CHARACTER);
          }
          uiSearch.setRawSearchConditional("");
        } 
        
        uiSearch.setProfileFilter(filter);
        uiSearch.setNewSearch(true);
      } catch (Exception e) {
        uiSearch.setIdentityList(new ArrayList<Identity>());
      }
      
      uiMembersPortlet.loadSearch();
      uiMembersPortlet.setLoadAtEnd(false);
    }
  }

  /**
   * Action when user clicks on loadMoreMember
   * @author phuonglm
   *
   */
  static public class LoadMoreMemberActionListener extends EventListener<UIMembersPortlet> {
    public void execute(Event<UIMembersPortlet> event) throws Exception {
      UIMembersPortlet uiMembersPortlet = event.getSource();
      if (uiMembersPortlet.currentLoadIndex < uiMembersPortlet.memberNum) {
        uiMembersPortlet.increaseOffset();
      } else {
        uiMembersPortlet.setEnableLoadNext(false);
      }
    }
  }

  static public class LoadMoreManagerActionListener extends EventListener<UIMembersPortlet> {
    public void execute(Event<UIMembersPortlet> event) throws Exception {
      UIMembersPortlet uiMembersPortlet = event.getSource();
      if (uiMembersPortlet.currentLoadManagerIndex < uiMembersPortlet.managerNum) {
        uiMembersPortlet.increaseOffsetManager();
      } else {
        uiMembersPortlet.setEnableLoadManagerNext(false);
      }
    }
  }
  
  /**
   * Loads people when searching.
   * @throws Exception
   */
  public void loadSearch() throws Exception {
    currentLoadIndex = 0;
    setMemberList(loadPeople(currentLoadIndex, MEMBER_PER_PAGE, Type.MEMBER));
  }
  
  
  /**
   * Sets flags to clarify that load at the last element or not.
   * 
   * @param loadAtEnd the loadAtEnd to set
   */
  public void setLoadAtEnd(boolean loadAtEnd) {
    this.loadAtEnd = loadAtEnd;
  }
  
  /**
   * get identityManager
   * @return identityManager
   * @see IdentityManager
   */
  private IdentityManager getIdentityManager() {
    if (identityManager_ == null) {
      PortalContainer pcontainer = PortalContainer.getInstance();
      identityManager_ = (IdentityManager) pcontainer.getComponentInstanceOfType(IdentityManager.class);
    }
    return identityManager_;
  }
 
  /**
   * increase offset.
   * @throws Exception
   */
  public void increaseOffset() throws Exception {
    currentLoadIndex += MEMBER_PER_PAGE;
  }

  public void increaseOffsetManager() throws Exception {
    currentLoadManagerIndex += MANAGER_PER_PAGE;
  }
 
  /**
   * Load next member on UIUserSearch
   * @throws Exception
   */
  @Deprecated
  public void loadNextMember() throws Exception {
    currentLoadIndex += MEMBER_PER_PAGE;
    if (currentLoadIndex <= getMemberNum()) {
      List<Identity> currentPeopleList = new ArrayList<Identity>(this.memberList);
      List<Identity> loadedPeople = new ArrayList<Identity>(Arrays.asList(getMemberListAccess()
                    .load(currentLoadIndex, MEMBER_PER_PAGE)));
      currentPeopleList.addAll(loadedPeople);
      setMemberList(currentPeopleList);
    }
  }

  /**
   * Gets flag to display LoadNext button or not.
   * 
   * @return the enableLoadNext
   */
  public boolean isEnableLoadNext() {
    return enableLoadNext;
  }

  /**
   * Sets flag to display LoadNext button or not.
   * 
   * @param enableLoadNext the enableLoadNext to set
   */
  public void setEnableLoadNext(boolean enableLoadNext) {
    this.enableLoadNext = enableLoadNext;
  }

  public void setEnableLoadManagerNext(boolean enableLoadNext) {
    this.enableLoadManagerNext = enableLoadNext;
  }
}
