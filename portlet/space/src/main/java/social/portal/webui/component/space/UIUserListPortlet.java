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
package social.portal.webui.component.space;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.ProfileAttachment;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceUtils;
import org.exoplatform.social.webui.UIProfileUserSearch;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

import social.portal.webui.component.UserListAccess;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Nov 07, 2008          
 */

@ComponentConfigs ({
  @ComponentConfig(
    lifecycle = UIApplicationLifecycle.class, 
    template = "app:/groovy/portal/webui/space/UIUserListPortlet.gtmpl",
    events = {
      @EventConfig(
         listeners=UIUserListPortlet.SearchActionListener.class, phase=Phase.DECODE)
    }
  )
})
public class UIUserListPortlet extends UIPortletApplication {
  
  private UIPageIterator iterator_;
  private List<User> memberList;
  private List<User> leaderList;
  private UIPageIterator iteratorLeaders;
  private UIPageIterator iteratorMembers;
  private final String iteratorLeaderID = "UIIteratorLeader";
  private final String iteratorMemberID = "UIIteratorMember";
  private final Integer ITEMS_PER_PAGE = 5;
  private IdentityManager identityManager_ = null;
  private UIProfileUserSearch uiSearchMemberOfSpace = null;
  private List<Identity> identityList;
  
  
  public List<Identity> getIdentityList() { return identityList; }

  public void setIdentityList(List<Identity> identityList) { this.identityList = identityList; }
  
  public UIUserListPortlet() throws Exception {
    initMember();
    initLeader();
    iterator_ = createUIComponent(UIPageIterator.class, null, null);
    addChild(iterator_);
    iteratorLeaders = createUIComponent(UIPageIterator.class, null, iteratorLeaderID);
    iteratorMembers = createUIComponent(UIPageIterator.class, null, iteratorMemberID);
    addChild(iteratorLeaders);
    addChild(iteratorMembers);
    uiSearchMemberOfSpace = createUIComponent(UIProfileUserSearch.class, null, "UIMemberUserSpaceSearch");
    addChild(uiSearchMemberOfSpace);
//    addChild(UISpaceUserSearch.class,null , null);
  }
  
  public void setMemberList(List<User> memberList) { this.memberList = memberList;}
  
  public void setLeaderList(List<User> leaderList) { this.leaderList = leaderList;}
  
  public List<User> getLeaderList() throws Exception { 
    initLeader();
    return leaderList;
  }
  
  public UIPageIterator getIteratorLeaders() { return iteratorLeaders; }

  public UIPageIterator getIteratorMembers() { return iteratorMembers; }
  
  public List<User> getLeaders() throws Exception {
    initLeader();
    int currentPage = iteratorLeaders.getCurrentPage();
    LazyPageList<User> pageList = new LazyPageList<User>(new UserListAccess(leaderList), ITEMS_PER_PAGE);
    iteratorLeaders.setPageList(pageList);
    int pageCount = iteratorLeaders.getAvailablePage();
    if (pageCount >= currentPage) {
      iteratorLeaders.setCurrentPage(currentPage);
    } else if (pageCount < currentPage) {
      iteratorLeaders.setCurrentPage(currentPage - 1);
    }
    
    return iteratorLeaders.getCurrentPageData();
  }

  public List<User> getMembers() throws Exception {
    initMember();
    UIProfileUserSearch uiSearchMemberOfSpace1 = getChild(UIProfileUserSearch.class);
    uiSearchMemberOfSpace1.setAllUserContactName(getAllMemberNames()); // set identitites for suggestion
    
    int currentPage = iteratorMembers.getCurrentPage();
    LazyPageList<User> pageList = new LazyPageList<User>(new UserListAccess(memberList), ITEMS_PER_PAGE);
    iteratorMembers.setPageList(pageList);
    int pageCount = iteratorMembers.getAvailablePage();
    if (pageCount >= currentPage) {
      iteratorMembers.setCurrentPage(currentPage);
    } else if (pageCount < currentPage) {
      iteratorMembers.setCurrentPage(currentPage - 1);
    }
    
    return iteratorMembers.getCurrentPageData();
  }
  
  public String getUserAvatar(String userId) throws Exception {
    Identity identity = getIdentityManager().getIdentityByRemoteId("organization", userId);
    Profile profile = identity.getProfile();
    ProfileAttachment attach = (ProfileAttachment) profile.getProperty("avatar");
    if (attach != null) {
      return "/" + getPortalName()+"/rest/jcr/" + getRepository()+ "/" + attach.getWorkspace()
              + attach.getDataPath() + "/?rnd=" + System.currentTimeMillis();
    }
    return null;
  }
  
  public Identity getIdentity(String userId) throws Exception {
    Identity identity = getIdentityManager().getIdentityByRemoteId("organization", userId);
    if (identity != null) {
      return identity;
    }
    return null;
  }
  
  @SuppressWarnings("unchecked")
  public void initMember() throws Exception {
    Space space = getSpace();
    memberList = new ArrayList<User>();
    OrganizationService orgSrc = getApplicationComponent(OrganizationService.class);
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    UserHandler userHandler = orgSrc.getUserHandler();
    List<String> userNames = spaceService.getMembers(space);
    Iterator<String> itr = userNames.iterator();
    while(itr.hasNext()) {
      String userName = itr.next();
      if(spaceService.isLeader(space, userName)){
        itr.remove();
      }
    }
    
    List<Identity> matchIdentities = getIdentityList();
    if (matchIdentities != null) {
      List<String> searchResultUserNames = new ArrayList<String>();
      String userName = null;
      for (Identity id : matchIdentities) {
        userName = id.getRemoteId();
        if (userNames.contains(userName)) searchResultUserNames.add(userName);
      }
      userNames = searchResultUserNames;
    }
    
    for (String name : userNames) {
      memberList.add(userHandler.findUserByName(name));
    }

  }
  
  @SuppressWarnings("unchecked")
  public void initLeader() throws Exception {
    Space space = getSpace();
    leaderList = new ArrayList<User>();
    OrganizationService orgSrc = getApplicationComponent(OrganizationService.class);
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    UserHandler userHandler = orgSrc.getUserHandler();
    List<String> userNames = spaceService.getMembers(space);
    Iterator<String> itr = userNames.iterator();
    while(itr.hasNext()) {
      String userName = itr.next();
      if(!spaceService.isLeader(space, userName)){
        itr.remove();
      }
    }
    for (String name : userNames) {
      leaderList.add(userHandler.findUserByName(name));
    }

  }
  
  /**
   * This method is called by template file
   * @throws Exception
   */
  private void update() throws Exception {
    int n = iterator_.getCurrentPage();
    //// Check
    PageList pageList = new ObjectPageList(memberList, ITEMS_PER_PAGE);
    iterator_.setPageList(pageList);
    if (n <= pageList.getAvailablePage()) iterator_.setCurrentPage(n);
  }
  

  public UIPageIterator getUIPageIterator() throws Exception { 
    return iterator_;
  }
  
  public Space getSpace() throws SpaceException {
    String spaceUrl = SpaceUtils.getSpaceUrl();
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    return spaceService.getSpaceByUrl(spaceUrl);
  }
  
  @SuppressWarnings("unchecked")
  public List<User> getUsersInSpace() throws Exception{
    update();
    return iterator_.getCurrentPageData();
  }
  
  public String getPath() {
    String nodePath = Util.getPortalRequestContext().getNodePath();
    String uriPath = Util.getPortalRequestContext().getRequestURI();
    return uriPath.replaceAll(nodePath, "");
  }
  
  @SuppressWarnings("unchecked")
  public String getMemberships(String userName) throws Exception {
    String memberShip = null;
    OrganizationService orgService = getApplicationComponent(OrganizationService.class);
    MembershipHandler memberShipHandler = orgService.getMembershipHandler();
    Collection<Membership> memberShips= memberShipHandler.findMembershipsByUserAndGroup(userName, getSpace().getGroupId());
    for(Membership aaa : memberShips) {
      if(memberShip == null) memberShip = aaa.getMembershipType();
      else memberShip += "," + aaa.getMembershipType();
    }
    return memberShip;
  }
  
  /**
   * Get the userList from UISpaceUserSearch
   * and then update UIUserListPortlet
   */
//  static public class SearchActionListener extends EventListener<UIUserListPortlet> {
//    @Override
//    public void execute(Event<UIUserListPortlet> event) throws Exception {
//      UIUserListPortlet uiUserListPortlet = event.getSource();
//      UISpaceUserSearch uiUserSearch = uiUserListPortlet.getChild(UISpaceUserSearch.class);
//      uiUserListPortlet.setMemberList(uiUserSearch.getUserList());
//      /////// Check .....
//      uiUserListPortlet.setLeaderList(uiUserSearch.getUserList());
//    }
//    
//  }
  
  public static class SearchActionListener extends EventListener<UIUserListPortlet> {
    @Override
    public void execute(Event<UIUserListPortlet> event) throws Exception {
      UIUserListPortlet uiUserListPortlet = event.getSource();
      UIProfileUserSearch uiProfileUserSearch = uiUserListPortlet.getChild(UIProfileUserSearch.class);
      List<Identity> identityList = uiProfileUserSearch.getIdentityList();
      uiUserListPortlet.setIdentityList(identityList);
    }
  }
  
  /**
   * Get all member names for suggesting.
   * 
   * @return
   * @throws   
   */
  private List<String> getAllMemberNames() throws Exception {
    List<String> allMemberNames = new ArrayList<String>();
    for (User user : memberList) {
      allMemberNames.add(user.getFirstName() + " " + user.getLastName());
    }
    return allMemberNames;
  }
  
  private IdentityManager getIdentityManager() {
    if(identityManager_ == null) {
      PortalContainer pcontainer =  PortalContainer.getInstance();
      identityManager_ = (IdentityManager) pcontainer.getComponentInstanceOfType(IdentityManager.class);
    }
    return identityManager_;
  }
  
  private String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance();
    return pcontainer.getPortalContainerInfo().getContainerName();  
  }
  private String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class);    
    return rService.getCurrentRepository().getConfiguration().getName();
  }
}
