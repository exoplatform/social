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

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Nov 07, 2008          
 */

@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class, 
    template = "app:/groovy/portal/webui/space/UIUserListPortlet.gtmpl"
)
public class UIUserListPortlet extends UIPortletApplication {
  
  private UIPageIterator iterator_;
  
  public UIUserListPortlet() throws Exception {
    iterator_ = createUIComponent(UIPageIterator.class, null, null);
    addChild(iterator_);
    init();
  }
  
  @SuppressWarnings("unchecked")
  public void init() throws Exception {
    int n = iterator_.getCurrentPage();
    String spaceName = getSpaceName();
    List<User> users;
    String groupId = "/spaces/" + spaceName;
    OrganizationService orgSrc = getApplicationComponent(OrganizationService.class);
    PageList usersPageList = orgSrc.getUserHandler().findUsersByGroup(groupId);
    users = usersPageList.getAll();
    PageList pageList = new ObjectPageList(users,3);
    iterator_.setPageList(pageList);
    if (n <= pageList.getAvailablePage()) iterator_.setCurrentPage(n);
  }

  public UIPageIterator getUIPageIterator() throws Exception { 
    return iterator_;
    }
  
  private String getSpaceName() {
    PortalRequestContext pcontext = Util.getPortalRequestContext();
    HttpServletRequest request = pcontext.getRequest();
    String url = request.getRequestURL().toString();
    String[] tempSplit = url.split("/");
    String spaceName = tempSplit[tempSplit.length-2];
    return spaceName;
  }
  
  @SuppressWarnings("unchecked")
  public List<User> getUsersInSpace() throws Exception{
    init();
    return iterator_.getCurrentPageData();
  }
  
  @SuppressWarnings("unchecked")
  public String getMemberships(String userName) throws Exception {
    String memberShip = null;
    OrganizationService orgService = getApplicationComponent(OrganizationService.class);
    MembershipHandler memberShipHandler = orgService.getMembershipHandler();
    Collection<Membership> memberShips= memberShipHandler.findMembershipsByUserAndGroup(userName, "/spaces/" + getSpaceName());
    for(Membership aaa : memberShips) {
      if(memberShip == null) memberShip = aaa.getMembershipType();
      else memberShip += "," + aaa.getMembershipType();
        
    }
    return memberShip;
  }
}
