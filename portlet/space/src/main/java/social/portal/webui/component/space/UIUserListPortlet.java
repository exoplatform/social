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
import java.util.List;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.PortalContainer;
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
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

import social.portal.webui.component.UISpaceUserSearch;
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
      @EventConfig(listeners=UIUserListPortlet.SearchActionListener.class, phase=Phase.DECODE)
    }
  )
})
public class UIUserListPortlet extends UIPortletApplication {
  
  private UIPageIterator iterator_;
  private List<User> userList;
  private final Integer ITEMS_PER_PAGE = 3;
  private IdentityManager identityManager_ = null;
  
  public UIUserListPortlet() throws Exception {
    iterator_ = createUIComponent(UIPageIterator.class, null, null);
    addChild(iterator_);
    
    addChild(UISpaceUserSearch.class,null , null);
    init();
  }
  
  public void setUserList(List<User> userList) {
    this.userList = userList;
  }
  
  public List<User> getUserList() {
    return userList;
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
  
  @SuppressWarnings("unchecked")
  public void init() throws Exception {
    Space space = getSpace();
    userList = new ArrayList<User>();
    OrganizationService orgSrc = getApplicationComponent(OrganizationService.class);
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    UserHandler userHandler = orgSrc.getUserHandler();
    List<String> userNames = spaceService.getMembers(space);
    for (String name : userNames) {
      userList.add(userHandler.findUserByName(name));
    }

  }
  
  /**
   * This method is called by template file
   * @throws Exception
   */
  private void update() throws Exception {
    int n = iterator_.getCurrentPage();
    PageList pageList = new ObjectPageList(userList, ITEMS_PER_PAGE);
    iterator_.setPageList(pageList);
    if (n <= pageList.getAvailablePage()) iterator_.setCurrentPage(n);
  }
  

  public UIPageIterator getUIPageIterator() throws Exception { 
    return iterator_;
    }
  
  private Space getSpace() throws SpaceException {
    String spaceUrl = SpaceUtils.getSpaceUrl();
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    return spaceService.getSpaceByUrl(spaceUrl);
  }
  
  @SuppressWarnings("unchecked")
  public List<User> getUsersInSpace() throws Exception{
    update();
    return iterator_.getCurrentPageData();
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
  static public class SearchActionListener extends EventListener<UIUserListPortlet> {
    @Override
    public void execute(Event<UIUserListPortlet> event) throws Exception {
      UIUserListPortlet uiUserListPortlet = event.getSource();
      UISpaceUserSearch uiUserSearch = uiUserListPortlet.getChild(UISpaceUserSearch.class);
      uiUserListPortlet.setUserList(uiUserSearch.getUserList());
    }
    
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
