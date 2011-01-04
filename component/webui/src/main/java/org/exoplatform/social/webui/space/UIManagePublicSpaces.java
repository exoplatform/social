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
package org.exoplatform.social.webui.space;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceListAccess;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;

/**
 * UIManagePublicSpaces: list all spaces where user can request to join. <br />
 * Created by The eXo Platform SAS
 * @author hoatle <hoatlevan at gmail dot com>
 * @since Jun 23, 2009
 */
@ComponentConfig(
  template = "classpath:groovy/social/webui/space/UIManagePublicSpaces.gtmpl",
  events = {
    @EventConfig(listeners = UIManagePublicSpaces.RequestJoinActionListener.class),
    @EventConfig(listeners = UIManagePublicSpaces.SearchActionListener.class , phase = Phase.DECODE)
  }
)
public class UIManagePublicSpaces extends UIContainer {
  private static final String SPACE_DELETED_INFO = "UIPublicSpacePortlet.msg.DeletedInfo";
  static private final String MSG_ERROR_REQUEST_JOIN = "UIManagePublicSpaces.msg.error_request_join";
  
  /** The first page. */
  private static final int FIRST_PAGE = 1;
  
  private SpaceService spaceService = null;
  private String userId = null;
  private UIPageIterator iterator;
  private final String ITERATOR_ID = "UIIteratorPublicSpaces";
  private final Integer SPACES_PER_PAGE = 4;
  private static final String PUBLIC_STATUS = "public";
  private List<Space> spaces; // for search result
  private UISpaceSearch uiSpaceSearch = null;

  /**
   * Constructor to initialize iterator.
   * 
   * @throws Exception
   */
  public UIManagePublicSpaces() throws Exception {
    uiSpaceSearch = createUIComponent(UISpaceSearch.class, null, "UISpaceSearch");
    uiSpaceSearch.setTypeOfRelation(PUBLIC_STATUS);
    addChild(uiSpaceSearch);
    iterator = addChild(UIPageIterator.class, null, ITERATOR_ID);
  }


  /**
   * Gets the uiPageIterator.
   * 
   * @return uiPageIterator
   */
  public UIPageIterator getUIPageIterator() {
    return iterator;
  }

  /**
   * Gets all public spaces of a user.
   * 
   * @return public spaces list
   * @throws Exception
   */
  public List<Space> getAllPublicSpaces() throws Exception {
    SpaceService spaceService = getSpaceService();
    String userId = getUserId();
    UserACL userACL = getApplicationComponent(UserACL.class);
    if (userId.equals(userACL.getSuperUser())) return new ArrayList<Space>();
    return spaceService.getPublicSpaces(userId);
  }

  /**
   * Checks if the remote user has edit permission  of a space.
   * 
   * @param space
   * @return true or false
   * @throws Exception
   */
  public boolean hasEditPermission(Space space) throws Exception {
    SpaceService spaceService = getSpaceService();
    return spaceService.hasEditPermission(space, getUserId());
  }

  /**
   * Gets paginated public spaces so that user can request to join.
   * 
   * @return paginated public spaces list
   * @throws Exception
   */
  public List<Space> getPublicSpaces() throws Exception {
	  List<Space> spaceList = getSpaceList();
	  uiSpaceSearch.setSpaceNameForAutoSuggest(getPublicSpaceNames());
	  return getDisplayPublicSpaces(spaceList, iterator);
  }

  /**
   * Gets image source url.
   * 
   * @param space
   * @return image source url
   * @throws Exception
   */
  public String getImageSource(Space space) throws Exception {
    return space.getAvatarUrl();
  }
  /**
   * Listener for request to join space action.
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

     if (space == null) {
       uiApp.addMessage(new ApplicationMessage(SPACE_DELETED_INFO, null, ApplicationMessage.INFO));
       return;
     }

     try {
       spaceService.requestJoin(space, userId);
     } catch(SpaceException se) {
       uiApp.addMessage(new ApplicationMessage(MSG_ERROR_REQUEST_JOIN, null, ApplicationMessage.ERROR));
       return;
     }

     UIWorkingWorkspace uiWorkingWS = Util.getUIPortalApplication().getChild(UIWorkingWorkspace.class);

     uiWorkingWS.updatePortletsByName("SpacesToolbarPortlet");
     // portal
     uiWorkingWS.updatePortletsByName("SocialUserToolBarGroupPortlet");
    }
  }

  /**
   * Listener for SpaceSearch's broadcasting.
   * 
   * @author hoatle
   *
   */
  static public class SearchActionListener extends EventListener<UIManagePublicSpaces> {
	@Override
	public void execute(Event<UIManagePublicSpaces> event) throws Exception {
      UIManagePublicSpaces uiForm = event.getSource();
      UISpaceSearch uiSpaceSearch = uiForm.getChild(UISpaceSearch.class);
      List<Space> spaceList = uiSpaceSearch.getSpaceList();
      uiForm.setSpaces(spaceList);
	}

  }
  /**
   * Sets space lists.
   * 
   * @param spaces
   */
  public void setSpaces(List<Space> spaces) {
    this.spaces = spaces;
  }
  /**
   * Gets space list.
   * 
   * @return space list
   */
  public List<Space> getSpaces() {
    return spaces;
  }
  /**
   * Gets current remote user.
   * 
   * @return remote user
   */
  private String getUserId() {
    if(userId == null) userId = Util.getPortalRequestContext().getRemoteUser();
    return userId;
  }

  /**
   * Gets spaceService.
   * 
   * @return spaceService
   * @see SpaceService
   */
  private SpaceService getSpaceService() {
    if(spaceService == null) spaceService = getApplicationComponent(SpaceService.class);
    return spaceService;
  }

  /**
   * Gets spaceList.
   * 
   * @return
   * @throws Exception
   */
  private List<Space> getSpaceList() throws Exception {
    List<Space> spaceList = getSpaces();
    List<Space> allPublicSpace = getAllPublicSpaces();
    if (allPublicSpace.size() == 0) return allPublicSpace;
    List<Space> publicSpaces = new ArrayList<Space>();
    if(spaceList != null) {
      Iterator<Space> spaceItr = spaceList.iterator();
      while(spaceItr.hasNext()) {
        Space space = spaceItr.next();
        for(Space publicSpace : allPublicSpace) {
          if(space.getDisplayName().equals(publicSpace.getDisplayName())){
            publicSpaces.add(publicSpace);
            break;
          }
        }
      }
      return publicSpaces;
    }
    return allPublicSpace;
 }

  /**
   * Gets public space names.
   * 
   * @return public space names
   * @throws Exception
   */
  private List<String> getPublicSpaceNames() throws Exception {
    List<Space> publicSpaces = getAllPublicSpaces();
    List<String> publicSpaceNames = new ArrayList<String>();
    for (Space space : publicSpaces) {
      publicSpaceNames.add(space.getDisplayName());
    }

    return publicSpaceNames;
  }

  /**
   * Gets current portal name.
   * 
   * @return current portal name
   */
  private String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance() ;
    return pcontainer.getPortalContainerInfo().getContainerName() ;
  }

  /**
   * Gets current repository name.
   * 
   * @return current repository name
   * @throws Exception
   */
  private String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;
    return rService.getCurrentRepository().getConfiguration().getName() ;
  }

  /**
   * Gets the rest context.
   *
   * @return the rest context
   */
   private String getRestContext() {
     return PortalContainer.getInstance().getRestContextName();
   }

 /**
  * Gets paginated public spaces so that the user can request to join.
  * 
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
   if (this.uiSpaceSearch.isNewSearch()) {
     iterator.setCurrentPage(FIRST_PAGE);
   } else {
     iterator.setCurrentPage(currentPage);
   }
   this.uiSpaceSearch.setNewSearch(false);
   return iterator.getCurrentPageData();
 }
}