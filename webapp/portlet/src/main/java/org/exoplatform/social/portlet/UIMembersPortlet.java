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
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.SpaceMemberFilterListAccess.Type;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.profile.UIProfileUserSearch;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * {@link UIMembersPortlet} used as a portlet displaying space members.<br />
 * <p/>
 * Created by The eXo Platform SARL
 *
 * @author <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 * @since Nov 07, 200
 */

@ComponentConfigs ({
  @ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template = "app:/groovy/social/portlet/UIMembersPortlet.gtmpl",
    events = {
      @EventConfig(listeners = UIMembersPortlet.SearchActionListener.class, phase = Phase.DECODE), 
      @EventConfig(listeners = UIMembersPortlet.LoadMoreMemberActionListener.class)
    }
  )
})
public class UIMembersPortlet extends UIPortletApplication {
  private ListAccess<Identity> memberListAccess;
  private ListAccess<Identity> managerListAccess;
  private List<Identity> memberList;
  private List<Identity> managerList;

  private int memberNum;
  private int managerNum;
  
  private ProfileFilter memberProfileFilter;
  private ProfileFilter managerProfileFilter;

  
  private final int MEMBER_PER_PAGE = 45;
  private static final String SPACE_MEMBER = "member_of_space";
  private int currentLoadIndex;
  private IdentityManager identityManager_ = null;
  private UIProfileUserSearch uiSearchMemberOfSpace = null;
  
  
  boolean enableLoadNext;
  private boolean loadAtEnd;

//  private static final int FIRST_PAGE = 1;

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
      ProfileFilter filter = uiSearchMemberOfSpace.getProfileFilter();
      setManagerListAccess(Utils.getIdentityManager().getSpaceIdentityByProfileFilter(space, filter, type, true));
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
    int realMemberListSize = memberList.size();
    setEnableLoadNext((realMemberListSize >= MEMBER_PER_PAGE) 
        && (realMemberListSize < getMemberNum()));
    return memberList;
  }
  
  /**
   * sets leader list
   *
   * @param leaderList
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
    return managerList;
  }
  

  /**
   * initialize members, called from {@link #getMembers()}
   *
   * @throws Exception
   */
  public void initMember() throws Exception {
    memberProfileFilter = new ProfileFilter();
    memberProfileFilter.getExcludedIdentityList().add(Utils.getViewerIdentity());
    uiSearchMemberOfSpace.setProfileFilter(memberProfileFilter);
    loadSearch();
  }

  /**
   * initialize leaders, called from {@link #getLeaderList()}
   *
   * @throws Exception
   */
  public void initManager() throws Exception {
    Space space = getSpace();    
    managerProfileFilter = new ProfileFilter();
    
    ListAccess<Identity> managerListAccess = getIdentityManager().
                                              getSpaceIdentityByProfileFilter(space, managerProfileFilter, Type.MANAGER, true);
    
    managerList = (Arrays.asList(managerListAccess.load(0, managerListAccess.getSize())));
  }


  /**
   * gets space, space identified by the url.
   *
   * @return space
   * @throws SpaceException
   */
  public Space getSpace() throws SpaceException {
    String spaceUrl = SpaceUtils.getSpaceUrl();
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
   * triggers this action when user clicks on search button
   */
  public static class SearchActionListener extends EventListener<UIMembersPortlet> {
    @Override
    public void execute(Event<UIMembersPortlet> event) throws Exception {
      UIMembersPortlet uiMembersPortlet = event.getSource();
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
        uiMembersPortlet.loadNextMember();
      } else {
      uiMembersPortlet.setEnableLoadNext(false);
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
   * Load next member on UIUserSearch
   * @throws Exception
   */
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
}