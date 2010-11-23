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
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceListAccess;
import org.exoplatform.social.core.space.SpaceUtils;
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
 * UIManagePendingSpaces: list all pending spaces which user can revoke pending. <br />
 * Created by The eXo Platform SAS
 * @author hoatle <hoatlevan at gmail dot com>
 * @since Jun 23, 2009
 */
@ComponentConfig(
  template = "classpath:groovy/social/webui/space/UIManagePendingSpaces.gtmpl",
  events = {
      @EventConfig(listeners = UIManagePendingSpaces.RevokePendingActionListener.class),
      @EventConfig(listeners = UIManagePendingSpaces.SearchActionListener.class , phase = Phase.DECODE)
  }
)
public class UIManagePendingSpaces extends UIContainer {
  static private final String MSG_ERROR_REVOKE_PENDING = "UIManagePendingSpaces.msg.error_revoke_pending";
  static private final String SPACE_DELETED_INFO = "UIManagePendingSpaces.msg.DeletedInfo";

  SpaceService spaceService = null;
  String userId = null;
  private UIPageIterator iterator;
  private final String ITERATOR_ID = "UIIteratorPendingSpaces";
  private final Integer SPACES_PER_PAGE = 4;
  private List<Space> spaces; // for search result
  private UISpaceSearch uiSpaceSearch = null;

  /**
   * constructor to initialize iterator
   * @throws Exception
   */
  public UIManagePendingSpaces() throws Exception {
    uiSpaceSearch = createUIComponent(UISpaceSearch.class, null, "UISpaceSearch");
    addChild(uiSpaceSearch);
    iterator = addChild(UIPageIterator.class, null, ITERATOR_ID);
  }

  /**
   * gets uiPageIterator
   * @return uiPageIterator
   */
  public UIPageIterator getUIPageIterator() {
    return iterator;
  }

  /**
   * gets all pending spaces of a user
   * @return all pending spaces
   * @throws SpaceException
   */
  public List<Space> getAllPendingSpaces() throws SpaceException {
    SpaceService spaceService = getSpaceService();
    String userId = getUserId();
    List<Space> userSpaces = spaceService.getPendingSpaces(userId);
    return SpaceUtils.getOrderedSpaces(userSpaces);
  }


  /**
   * gets pending spaces by page iterator
   * @return pending spaces
   * @throws Exception
   */
  public List<Space> getPendingSpaces() throws Exception {
    List<Space> listSpace = getSpaceList();
    uiSpaceSearch.setSpaceNameForAutoSuggest(getPendingSpaceNames());
    return getDisplayPendingSpaces(listSpace, iterator);
  }

  /**
   * gets image source url
   * @param space
   * @return image source url
   * @throws Exception
   */
  public String getImageSource(Space space) throws Exception {
    return LinkProvider.getAvatarImageSource(space.getAvatarAttachment());
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

      Space space = spaceService.getSpaceById(spaceId);

      if (space == null) {
        uiApp.addMessage(new ApplicationMessage(SPACE_DELETED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      try {
        spaceService.revokeRequestJoin(spaceId, userId);
      } catch(SpaceException se) {
        uiApp.addMessage(new ApplicationMessage(MSG_ERROR_REVOKE_PENDING, null, ApplicationMessage.ERROR));
        return;
      }
    }
  }

  /**
   * triggers this action when user click on search button
   * @author hoatle
   *
   */
  public static class SearchActionListener extends EventListener<UIManagePendingSpaces> {
    @Override
    public void execute(Event<UIManagePendingSpaces> event) throws Exception {
      UIManagePendingSpaces uiForm = event.getSource();
      UISpaceSearch uiSpaceSearch = uiForm.getChild(UISpaceSearch.class);
      List<Space> spaceList = uiSpaceSearch.getSpaceList();
      uiForm.setSpaces(spaceList);
    }
  }

  /**
   * sets space list
   * @param spaces
   */
  public void setSpaces(List<Space> spaces) {
    this.spaces = spaces;
  }
  /**
   * gets space list
   * @return space list
   */
  public List<Space> getSpaces() {
    return spaces;
  }

  /**
   * gets spaceService
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
   * gets remote user
   * @return userId
   */
  private String getUserId() {
    if (userId == null) {
      userId = Util.getPortalRequestContext().getRemoteUser();
    }
    return userId;
  }
  /**
   * gets space list
   * @return space list
   * @throws Exception
   */
  private List<Space> getSpaceList() throws Exception {
    List<Space> spaceList = getSpaces();
    List<Space> allPendingSpace = getAllPendingSpaces();
    if (allPendingSpace.size() == 0) return allPendingSpace;
    List<Space> pendingSpaces = new ArrayList<Space>();
    if(spaceList != null) {
      Iterator<Space> spaceItr = spaceList.iterator();
      while(spaceItr.hasNext()) {
        Space space = spaceItr.next();
        for(Space pendingSpace : allPendingSpace) {
          if(space.getDisplayName().equals(pendingSpace.getDisplayName())){
            pendingSpaces.add(pendingSpace);
            break;
          }
        }
      }

      return pendingSpaces;
    }
    return allPendingSpace;
  }

  /**
   * gets pending space names
   * @return pending space names
   * @throws SpaceException
   */
  private List<String> getPendingSpaceNames() throws SpaceException {
    List<Space> pendingSpaces = getAllPendingSpaces();
    List<String> pendingSpaceNames = new ArrayList<String>();
    for (Space space : pendingSpaces) {
      pendingSpaceNames.add(space.getDisplayName());
    }

    return pendingSpaceNames;
  }

  /**
   * gets current portal name
   * @return current portal name
   */
  private String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance();
    return pcontainer.getPortalContainerInfo().getContainerName();
  }

  /**
   * gets repository name
   * @return repository name
   * @throws Exception
   */
  private String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class);
    return rService.getCurrentRepository().getConfiguration().getName();
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
   * gets paginated pending spaces so that the user can revoke pending
   * @return paginated pending spaces
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
}