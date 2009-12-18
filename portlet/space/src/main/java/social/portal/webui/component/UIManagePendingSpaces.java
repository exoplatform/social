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
import java.util.Iterator;
import java.util.List;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceAttachment;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceListAccess;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceUtils;
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
 * UIManagePendingSpaces: list all pending spaces which user can revoke pending.
 * Created by The eXo Platform SAS
 * Author : hoatle
 *          hoatlevan@gmail.com
 * Jun 23, 2009  
 */
@ComponentConfig(
  template = "app:/groovy/portal/webui/component/UIManagePendingSpaces.gtmpl",
  events = {
      @EventConfig(listeners = UIManagePendingSpaces.RevokePendingActionListener.class),
      @EventConfig(listeners = UIManagePendingSpaces.SearchActionListener.class , phase = Phase.DECODE)
  }
)
public class UIManagePendingSpaces extends UIContainer {
  static private final String MSG_ERROR_REVOKE_PENDING = "UIManagePendingSpaces.msg.error_revoke_pending";
  SpaceService spaceService = null;
  String userId = null;
  private UIPageIterator iterator;
  private final String ITERATOR_ID = "UIIteratorPendingSpaces";
  private final Integer SPACES_PER_PAGE = 4;
  private List<Space> spaces_; // for search result
  /**
   * Constructor to initialize iterator
   * @throws Exception
   */
  public UIManagePendingSpaces() throws Exception {
    addChild(UISpaceSearch.class, null, "UIPendingSpaceSearch");
    iterator = addChild(UIPageIterator.class, null, ITERATOR_ID);
  }
  
  /**
   * get {@SpaceService}
   * @return spaceService
   */
  private SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = getApplicationComponent(SpaceService.class);
    }
    return spaceService;
  }
  
  /**
   * Get remote user
   * @return userId
   */
  private String getUserId() {
    if (userId == null) {
      userId = Util.getPortalRequestContext().getRemoteUser();
    }
    return userId;
  }
  
  /**
   * Get UIPageIterator
   * @return
   */
  public UIPageIterator getUIPageIterator() {
    return iterator;
  }
  
  /**
   * Get all pending spaces of a user
   * @return
   * @throws SpaceException
   */
  public List<Space> getAllPendingSpaces() throws SpaceException {
    SpaceService spaceService = getSpaceService();
    String userId = getUserId();
    List<Space> userSpaces = spaceService.getPendingSpaces(userId);
    return SpaceUtils.getOrderedSpaces(userSpaces);
  }
  
  private List<Space> getSpaceList() throws Exception {
    List<Space> spaceList = getSpaces_();
    List<Space> allPendingSpace = getAllPendingSpaces();
    if (allPendingSpace.size() == 0) return allPendingSpace;
    List<Space> pendingSpaces = new ArrayList<Space>();
    if(spaceList != null) {
      Iterator<Space> spaceItr = spaceList.iterator();
      while(spaceItr.hasNext()) {
        Space space = spaceItr.next();
        for(Space pendingSpace : allPendingSpace) {
          if(space.getName().equals(pendingSpace.getName())){
            pendingSpaces.add(space);
            break;
          }
        }
      }
    
      return pendingSpaces;
    }
    
    return allPendingSpace;
  }
  
  public List<Space> getPendingSpaces() throws Exception {
    List<Space> listSpace = getSpaceList();
    return getDisplayPendingSpaces(listSpace, iterator);
  }
  
  public String getImageSource(Space space) throws Exception {
    SpaceAttachment spaceAtt = (SpaceAttachment) space.getSpaceAttachment();
    if (spaceAtt != null) {
      return "/" + getPortalName()+"/rest/jcr/" + getRepository()+ "/" + spaceAtt.getWorkspace()
              + spaceAtt.getDataPath() + "/?rnd=" + System.currentTimeMillis();
    }
    return null;
  }
  
  private String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance() ;
    return pcontainer.getPortalContainerInfo().getContainerName() ;  
  }
  
  private String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;    
    return rService.getCurrentRepository().getConfiguration().getName() ;
  }
  
  /**
   * Get paginated pending spaces so that the user can revoke pending
   * @return
   * @throws Exception 
   */
  @SuppressWarnings("unchecked")
  private List<Space> getDisplayPendingSpaces(List<Space> spaces, UIPageIterator iterator) throws Exception {
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
   * This action is triggered when user clicks on RevokePending action
   */
  static public class RevokePendingActionListener extends EventListener<UIManagePendingSpaces> {
    @Override
    public void execute(Event<UIManagePendingSpaces> event) throws Exception {
      UIManagePendingSpaces uiPendingSpaces = event.getSource();
      SpaceService spaceService = uiPendingSpaces.getSpaceService();
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApp = ctx.getUIApplication();
      String spaceId = ctx.getRequestParameter(OBJECTID);
      String userId = uiPendingSpaces.getUserId();
      try {
        spaceService.revokeRequestJoin(spaceId, userId);
      } catch(SpaceException se) {
        uiApp.addMessage(new ApplicationMessage(MSG_ERROR_REVOKE_PENDING, null, ApplicationMessage.ERROR));
        return;
      }
    }
  }
  public static class SearchActionListener extends EventListener<UIManagePendingSpaces> {
    @Override
    public void execute(Event<UIManagePendingSpaces> event) throws Exception {
      UIManagePendingSpaces uiForm = event.getSource();
      UISpaceSearch uiSpaceSearch = uiForm.getChild(UISpaceSearch.class);
      List<Space> spaceList = uiSpaceSearch.getSpaceList();
      uiForm.setSpaces_(spaceList);
    }
  }
  
  public void setSpaces_(List<Space> spaces_) {
    this.spaces_ = spaces_;
  }
  public List<Space> getSpaces_() {
    return spaces_;
  }
}