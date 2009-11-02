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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceListAccess;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

import social.portal.webui.component.space.UISpaceSearch;

/**
 * UIManagePublicSpaces: list all spaces where user can request to join. 
 * Created by The eXo Platform SAS
 * Author : hoatle
 *          hoatlevan@gmail.com
 *          hoat.le@exoplatform.com
 * Jun 23, 2009  
 */
@ComponentConfig(
  template = "app:/groovy/portal/webui/component/UIManagePublicSpaces.gtmpl",
  events = {
      @EventConfig(listeners = UIManagePublicSpaces.RequestJoinActionListener.class),
      @EventConfig(listeners = UIManagePublicSpaces.SearchActionListener.class , phase = Phase.DECODE)
  }
)
public class UIManagePublicSpaces extends UIContainer {
  static private final String MSG_ERROR_REQUEST_JOIN = "UIManagePublicSpaces.msg.error_request_join"; 
  private SpaceService spaceService = null;
  private String userId = null;
  private UIPageIterator iterator;
  private final String ITERATOR_ID = "UIIteratorPublicSpaces";
  private final Integer SPACES_PER_PAGE = 4;
  private List<Space> spaceList; // for search result
  /**
   * Constructor to initialize iterator
   * @throws Exception
   */
  public UIManagePublicSpaces() throws Exception {
	addChild(UISpaceSearch.class, null, "UIPublicSpaceSearch");
    iterator = addChild(UIPageIterator.class, null, ITERATOR_ID);
  }
  
  private String getUserId() {
    if(userId == null) userId = Util.getPortalRequestContext().getRemoteUser();
    return userId;
  }
  
  private SpaceService getSpaceService() {
    if(spaceService == null) spaceService = getApplicationComponent(SpaceService.class);
    return spaceService;
  }
  /**
   * Get UIPageIterator
   * @return
   */
  public UIPageIterator getUIPageIterator() {
    return iterator;
  }
  
  /**
   * sets spaceList
   * @param spaceList
   */
  public void setSpaceList(List<Space> spaceList) {
	  this.spaceList = spaceList;
  }
  
  /**
   * gets spaceList
   * @return
   * @throws Exception 
   */
  public List<Space> getSpaceList() throws Exception {
	  if (spaceList == null) spaceList = getAllPublicSpaces();
	  return spaceList;
  }
  
  /**
   * Get all public spaces of a user
   * @return
   * @throws Exception 
   */
  private List<Space> getAllPublicSpaces() throws Exception {
    SpaceService spaceService = getSpaceService();
    String userId = getUserId();
    UserACL userACL = getApplicationComponent(UserACL.class);
    if (userId.equals(userACL.getSuperUser())) return new ArrayList<Space>();
    return spaceService.getPublicSpaces(userId);
  }
  
  /**
   * Checking if remote user has edit permission  of a space
   * @param space
   * @return
   */
  public boolean hasEditPermission(Space space) throws Exception {
    SpaceService spaceService = getSpaceService();
    return spaceService.hasEditPermission(space, getUserId());
  }
  
  /**
   * Get paginated public spaces so that user can request to join
   * @return
   * @throws Exception 
   */
  public List<Space> getPublicSpaces() throws Exception {
	  List<Space> spaceList = getSpaceList();
	  return getDisplayPublicSpaces(spaceList, iterator);
  }
 /**
  * gets paginated public spaces so that the user can request to join
  * @param spaces
  * @param iterator
  * @return
  * @throws Exception
  */
 @SuppressWarnings("unchecked")
 private List<Space> getDisplayPublicSpaces(List<Space> spaces, UIPageIterator iterator) throws Exception {
	 int currentPage = iterator.getCurrentPage();
	 LazyPageList<Space> pageList = new LazyPageList<Space>(new SpaceListAccess(spaces), SPACES_PER_PAGE);
	 iterator.setPageList(pageList);
	 int pageCount = iterator.getAvailablePage();
	 if (pageCount >= currentPage) {
	   iterator.setCurrentPage(currentPage);
	 } else if (pageCount < currentPage) {
	   iterator.setCurrentPage(currentPage - 1);
	 }
	 return iterator.getCurrentPageData();
 }
  
  /**
   * Class listener for request to join space action
   */
  static public class RequestJoinActionListener extends EventListener<UIManagePublicSpaces> {
    public void execute(Event<UIManagePublicSpaces> event) throws Exception {
     UIManagePublicSpaces uiPublicSpaces = event.getSource();
     WebuiRequestContext ctx = event.getRequestContext();
     UIApplication uiApp = ctx.getUIApplication();
     SpaceService spaceService = uiPublicSpaces.getSpaceService();
     String spaceId = ctx.getRequestParameter(OBJECTID);
     String userId = uiPublicSpaces.getUserId();
     Space space = spaceService.getSpaceById(spaceId);
     try {
       spaceService.requestJoin(space, userId);
     } catch(SpaceException se) {
       uiApp.addMessage(new ApplicationMessage(MSG_ERROR_REQUEST_JOIN, null, ApplicationMessage.ERROR));
       return;
     }
    }
  }
  
  /**
   * listener for SpaceSearch's broadcasting
   * @author hoatle
   *
   */
  static public class SearchActionListener extends EventListener<UIManagePublicSpaces> {
	@Override
	public void execute(Event<UIManagePublicSpaces> event) throws Exception {
      UIManagePublicSpaces uiForm = event.getSource();
      UISpaceSearch uiSpaceSearch = uiForm.getChild(UISpaceSearch.class);
      List<Space> spaceList = uiSpaceSearch.getSpaceList();
      uiForm.setSpaceList(spaceList);
	}
	  
  }
}  