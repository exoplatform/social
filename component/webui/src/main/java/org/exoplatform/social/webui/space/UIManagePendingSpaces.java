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
import java.util.List;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceFilter;
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
      @EventConfig(listeners = UIManagePendingSpaces.RevokePendingActionListener.class)
  }
)
public class UIManagePendingSpaces extends UIContainer {
  private static final String MSG_ERROR_REVOKE_PENDING = "UIManagePendingSpaces.msg.error_revoke_pending";
  private static final String SPACE_DELETED_INFO = "UIManagePendingSpaces.msg.DeletedInfo";
  private static final String PENDING_STATUS = "pending";
  
  /**
   * SEARCH ALL.
   */
  private static final String SEARCH_ALL = "All";
  
  /** The first page. */
  private static final int FIRST_PAGE = 1;

  SpaceService spaceService = null;
  String userId = null;
  private UIPageIterator iterator;
  private final String ITERATOR_ID = "UIIteratorPendingSpaces";
  private final Integer SPACES_PER_PAGE = 4;
  private List<Space> spaces; // for search result
  private UISpaceSearch uiSpaceSearch = null;
  
  /**
   * Constructor to initialize iterator.
   * 
   * @throws Exception
   */
  public UIManagePendingSpaces() throws Exception {
    uiSpaceSearch = createUIComponent(UISpaceSearch.class, null, "UISpaceSearch");
    uiSpaceSearch.setTypeOfRelation(PENDING_STATUS);
    addChild(uiSpaceSearch);
    iterator = addChild(UIPageIterator.class, null, ITERATOR_ID);
  }

  /**
   * Gets uiPageIterator.
   * 
   * @return uiPageIterator
   */
  public UIPageIterator getUIPageIterator() {
    return iterator;
  }

  /**
   * Gets all pending spaces of a user.
   * 
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
   * Gets pending spaces by page iterator.
   * 
   * @return pending spaces
   * @throws Exception
   */
  public List<Space> getPendingSpaces() throws Exception {
    uiSpaceSearch.setSpaceNameForAutoSuggest(getPendingSpaceNames());
    return getDisplayPendingSpaces(iterator);
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
   * This action is triggered when user clicks on RevokePending action.
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

      spaceService.removePendingUser(space, userId);
    }
  }

  /**
   * Sets space list.
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
   * Gets spaceService.
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
   * Gets remote user.
   * 
   * @return userId
   */
  private String getUserId() {
    if (userId == null) {
      userId = Util.getPortalRequestContext().getRemoteUser();
    }
    return userId;
  }

  /**
   * Gets pending space names.
   * 
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
   * Gets paginated pending spaces so that the user can revoke pending.
   * 
   * @return paginated pending spaces
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private List<Space> getDisplayPendingSpaces(UIPageIterator iterator) throws Exception {
    int currentPage = iterator.getCurrentPage();
    String selectedChar = this.uiSpaceSearch.getSelectedChar();
    String spaceNameSearch = this.uiSpaceSearch.getSpaceNameSearch();
    LazyPageList<Space> pageList = null;
    
    if ((selectedChar == null && spaceNameSearch == null) || (selectedChar != null && selectedChar.equals(SEARCH_ALL))) {
      pageList = new LazyPageList<Space>(spaceService.getPendingSpacesWithListAccess(userId), SPACES_PER_PAGE);
    } else {
      SpaceFilter spaceFilter = null;
      if (selectedChar != null) {
        spaceFilter = new SpaceFilter(selectedChar.charAt(0));
      } else {
        spaceFilter = new SpaceFilter(spaceNameSearch);
      }
      pageList = new LazyPageList<Space>(spaceService.getPendingSpacesByFilter(userId, spaceFilter), SPACES_PER_PAGE);
    }
    
    iterator.setPageList(pageList);
    int availablePage = iterator.getAvailablePage();
    if (this.uiSpaceSearch.isNewSearch()) {
      iterator.setCurrentPage(FIRST_PAGE);
    } else if (currentPage > availablePage) {
      iterator.setCurrentPage(availablePage);
    } else {
      iterator.setCurrentPage(currentPage);
    }
    this.uiSpaceSearch.setNewSearch(false);
    return iterator.getCurrentPageData();
  }
}