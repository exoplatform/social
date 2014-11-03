/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.webui.space;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(
  template = "war:/groovy/social/webui/space/UIManagePendingSpaces.gtmpl",
  events = {
      @EventConfig(listeners = UIManagePendingSpaces.RevokePendingActionListener.class),
      @EventConfig(listeners = UIManagePendingSpaces.SearchActionListener.class),
      @EventConfig(listeners = UIManagePendingSpaces.LoadMoreSpaceActionListener.class)
  }
)
public class UIManagePendingSpaces extends UIContainer {
  private static final Log LOG = ExoLogger.getLogger(UIManagePendingSpaces.class);
  private static final String SPACE_DELETED_INFO = "UIManagePendingSpaces.msg.DeletedInfo";
  private static final String PENDING_STATUS = "pending";
  
  /**
   * SEARCH ALL.
   */
  private static final String SEARCH_ALL = "All";
  private static final String SPACE_SEARCH = "SpaceSearch";
  
  SpaceService spaceService = null;
  String userId = null;
  private final Integer SPACES_PER_PAGE = 20;
  private List<Space> spaces; // for search result
  private UISpaceSearch uiSpaceSearch = null;
  
  private boolean loadAtEnd = false;
  private boolean hasUpdatedSpace = false;
  private int currentLoadIndex;
  private boolean enableLoadNext;
  private int loadingCapacity;
  private String spaceNameSearch;
  private List<Space> pendingSpacesList;
  private ListAccess<Space> pendingSpacesListAccess;
  private int pendingSpacesNum;
  private String selectedChar = null;
  
  /**
   * Constructor to initialize iterator.
   * 
   * @throws Exception
   */
  public UIManagePendingSpaces() throws Exception {
    uiSpaceSearch = createUIComponent(UISpaceSearch.class, null, "UISpaceSearch");
    uiSpaceSearch.setTypeOfRelation(PENDING_STATUS);
    addChild(uiSpaceSearch);
    init();
  }

  /**
   * Inits at the first loading.
   * @since 1.2.2
   */
  public void init() {
    try {
      setHasUpdatedSpace(false);
      setLoadAtEnd(false);
      enableLoadNext = true;
      currentLoadIndex = 0;
      loadingCapacity = SPACES_PER_PAGE;
      pendingSpacesList = new ArrayList<Space>();
      setPendingSpacesList(loadPendingSpaces(currentLoadIndex, loadingCapacity));
      if (this.selectedChar != null){
        setSelectedChar(this.selectedChar);
      } else {
        setSelectedChar(SEARCH_ALL);
      }   
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
  }
  
  /**
   * Sets loading capacity.
   * 
   * @param loadingCapacity
   */
  public void setLoadingCapacity(int loadingCapacity) {
    this.loadingCapacity = loadingCapacity;
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
   * @return the enableLoadNext
   */
  public void setEnableLoadNext(boolean enableLoadNext) {
    this.enableLoadNext = enableLoadNext;
  }

  /**
   * Gets flags to clarify that load at the last space or not. 
   * 
   * @return the loadAtEnd
   */
  public boolean isLoadAtEnd() {
    return loadAtEnd;
  }

  /**
   * Sets flags to clarify that load at the last space or not. 
   * 
   * @return the loadAtEnd
   */
  public void setLoadAtEnd(boolean loadAtEnd) {
    this.loadAtEnd = loadAtEnd;
  }

  /**
   * Gets information that clarify one space is updated or not.
   * 
   * @return the hasUpdatedSpace
   */
  public boolean isHasUpdatedSpace() {
    return hasUpdatedSpace;
  }

  /**
   * Sets information that clarify one space is updated or not.
   * 
   * @return the hasUpdatedSpace
   */
  public void setHasUpdatedSpace(boolean hasUpdatedSpace) {
    this.hasUpdatedSpace = hasUpdatedSpace;
  }

  /**
   * Gets list of sent invitation space.
   * 
   * @return the pendingSpacesList
   * @throws Exception 
   * @since 1.2.2
   */
  public List<Space> getPendingSpacesList() throws Exception {
    this.pendingSpacesList = loadPendingSpaces(0, this.pendingSpacesList.size());
    int realPendingSpacesListSize = this.pendingSpacesList.size();
    
    if (isHasUpdatedSpace()) {
      setHasUpdatedSpace(false); 
    }
        
    setEnableLoadNext((realPendingSpacesListSize >= SPACES_PER_PAGE)
            && (realPendingSpacesListSize < getPendingSpacesNum()));
    
    return this.pendingSpacesList;
  }

  /**
   * Sets list of sent invitation space.
   * 
   * @param pendingSpacesList the pendingSpacesList to set
   */
  public void setPendingSpacesList(List<Space> pendingSpacesList) {
    this.pendingSpacesList = pendingSpacesList;
  }
  
  /**
   * Gets number of pending space.
   * 
   * @return the pendingSpacesNum
   */
  public int getPendingSpacesNum() {
    return pendingSpacesNum;
  }

  /**
   * Sets number of pending space.
   * 
   * @param pendingSpacesNum the pendingSpacesNum to set
   */
  public void setPendingSpacesNum(int pendingSpacesNum) {
    this.pendingSpacesNum = pendingSpacesNum;
  }

  /**
   * Gets selected character.
   *
   * @return Character is selected.
   */
  public String getSelectedChar() {
    return selectedChar;
  }

  /**
   * Sets selected character.
   *
   * @param selectedChar A {@code String}
   */
  public void setSelectedChar(String selectedChar) {
    this.selectedChar = selectedChar;
  }
  
  /**
   * Gets name of searched space.
   * 
   * @return the spaceNameSearch
   */
  public String getSpaceNameSearch() {
    return spaceNameSearch;
  }

  /**
   * Sets name of searched space.
   * 
   * @param spaceNameSearch the spaceNameSearch to set
   */
  public void setSpaceNameSearch(String spaceNameSearch) {
    this.spaceNameSearch = spaceNameSearch;
  }
  
  /**
   * Gets spaces with ListAccess type.
   * 
   * @return the pendingSpacesListAccess
   */
  public ListAccess<Space> getPendingSpacesListAccess() {
    return pendingSpacesListAccess;
  }

  /**
   * Sets spaces with ListAccess type.
   * 
   * @param pendingSpacesListAccess the pendingSpacesListAccess to set
   */
  public void setPendingSpacesListAccess(ListAccess<Space> pendingSpacesListAccess) {
    this.pendingSpacesListAccess = pendingSpacesListAccess;
  }

  /**
   * Loads more space.
   * @throws Exception
   * @since 1.2.2
   */
  public void loadNext() throws Exception {
    currentLoadIndex += loadingCapacity;
    if (currentLoadIndex <= getPendingSpacesNum()) {
      this.pendingSpacesList.addAll(new ArrayList<Space>(Arrays.asList(getPendingSpacesListAccess()
                                                              .load(currentLoadIndex, loadingCapacity))));
    }
  }
  
  /**
   * Loads space when searching.
   * @throws Exception
   * @since 1.2.2
   */
  public void loadSearch() throws Exception {
    currentLoadIndex = 0;
    setPendingSpacesList(loadPendingSpaces(currentLoadIndex, loadingCapacity));
  }

  private List<Space> loadPendingSpaces(int index, int length) throws Exception {
    String charSearch = getSelectedChar();
    String searchCondition = uiSpaceSearch.getSpaceNameSearch();
    
    if (SEARCH_ALL.equals(charSearch) || (charSearch == null && searchCondition == null)) {
      setPendingSpacesListAccess(getSpaceService().getPendingSpacesWithListAccess(getUserId()));
    } else if (searchCondition != null) {
      setPendingSpacesListAccess(getSpaceService().getPendingSpacesByFilter(getUserId(), new SpaceFilter(searchCondition)));
    } else if(charSearch != null) {
      setPendingSpacesListAccess(getSpaceService().getPendingSpacesByFilter(getUserId(), new SpaceFilter(charSearch.charAt(0))));
    }
    
    setPendingSpacesNum(getPendingSpacesListAccess().getSize());
    uiSpaceSearch.setSpaceNum(getPendingSpacesNum());
    Space[] spaces = getPendingSpacesListAccess().load(index, length);
    
    return new ArrayList<Space>(Arrays.asList(spaces));
  }
  
  /**
   * Listeners loading more space action.
   * @author <a href="mailto:hanhvq@exoplatform.com">Hanh Vi Quoc</a>
   * @since Aug 18, 2011
   * @since 1.2.2
   */
  static public class LoadMoreSpaceActionListener extends EventListener<UIManagePendingSpaces> {
    public void execute(Event<UIManagePendingSpaces> event) throws Exception {
      UIManagePendingSpaces uiManagePendingSpaces = event.getSource();
      uiManagePendingSpaces.loadNext();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManagePendingSpaces);
    }
  }
  
  /**
   * Listens event that broadcast from UISpaceSearch.
   * 
   * @author <a href="mailto:hanhvq@exoplatform.com">Hanh Vi Quoc</a>
   * @since Aug 19, 2011
   */
  static public class SearchActionListener extends EventListener<UIManagePendingSpaces> {
    @Override
    public void execute(Event<UIManagePendingSpaces> event) throws Exception {
      UIManagePendingSpaces uiManagePendingSpaces = event.getSource();
      WebuiRequestContext ctx = event.getRequestContext();
      String charSearch = ctx.getRequestParameter(OBJECTID);
      
      if (charSearch == null) {
        uiManagePendingSpaces.setSelectedChar(null);
      } else {
        uiManagePendingSpaces.uiSpaceSearch.getUIStringInput(SPACE_SEARCH).setValue("");
        uiManagePendingSpaces.uiSpaceSearch.setSpaceNameSearch(null);
        uiManagePendingSpaces.setSelectedChar(charSearch);
      }
      
      uiManagePendingSpaces.loadSearch();
      uiManagePendingSpaces.setLoadAtEnd(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManagePendingSpaces);
    }
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
      uiPendingSpaces.setLoadAtEnd(false);
      
      if (space == null) {
        uiApp.addMessage(new ApplicationMessage(SPACE_DELETED_INFO, null, ApplicationMessage.INFO));
        return;
      }
      uiPendingSpaces.setHasUpdatedSpace(true);
      spaceService.removePendingUser(space, userId);
    }
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

}
