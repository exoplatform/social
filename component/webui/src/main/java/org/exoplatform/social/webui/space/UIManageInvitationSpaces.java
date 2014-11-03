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
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(
  template="war:/groovy/social/webui/space/UIManageInvitationSpaces.gtmpl",
  events = {
    @EventConfig(listeners = UIManageInvitationSpaces.AcceptActionListener.class),
    @EventConfig(listeners = UIManageInvitationSpaces.IgnoreActionListener.class),
    @EventConfig(listeners = UIManageInvitationSpaces.SearchActionListener.class),
    @EventConfig(listeners = UIManageInvitationSpaces.LoadMoreSpaceActionListener.class)
  }
)
public class UIManageInvitationSpaces extends UIContainer {
  private static final Log LOG = ExoLogger.getLogger(UIManageInvitationSpaces.class);
  private static final String SPACE_DELETED_INFO = "UIManageInvitationSpaces.msg.DeletedInfo";
  private static final String INVITATION_REVOKED_INFO = "UIManageInvitationSpaces.msg.RevokedInfo";
  private static final String INCOMING_STATUS = "incoming";

  /**
   * SEARCH ALL.
   */
  private static final String SEARCH_ALL = "All";
  private static final String SPACE_SEARCH = "SpaceSearch";
  static public final Integer LEADER = 1, MEMBER = 2;

  private final Integer SPACES_PER_PAGE = 20;
  private List<Space> spaces; // for search result
  private UISpaceSearch uiSpaceSearch = null;
  private SpaceService spaceService = null;
  private String userId = null;
  private boolean loadAtEnd = false;
  private boolean hasUpdatedSpace = false;
  private int currentLoadIndex;
  private boolean enableLoadNext;
  private int loadingCapacity;
  private String spaceNameSearch;
  private List<Space> invitedSpacesList;
  private ListAccess<Space> invitedSpacesListAccess;
  private int invitedSpacesNum;
  private String selectedChar = null;
  
  /**
   * Constructor for initialize UIPopupWindow for adding new space popup.
   *
   * @throws Exception
   */
  public UIManageInvitationSpaces() throws Exception {
    uiSpaceSearch = createUIComponent(UISpaceSearch.class, null, "UISpaceSearch");
    uiSpaceSearch.setTypeOfRelation(INCOMING_STATUS);
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
      enableLoadNext = false;
      currentLoadIndex = 0;
      loadingCapacity = SPACES_PER_PAGE;
      invitedSpacesList = new ArrayList<Space>();
      setInvitedSpacesList(loadInvitedSpaces(currentLoadIndex, loadingCapacity));
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
   * @param enableLoadNext
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
   * @param loadAtEnd
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
   * @param hasUpdatedSpace
   */
  public void setHasUpdatedSpace(boolean hasUpdatedSpace) {
    this.hasUpdatedSpace = hasUpdatedSpace;
  }

  /**
   * Gets list of invited space.
   * 
   * @return the invitedSpacesList
   * @throws Exception 
   * @since 1.2.2
   */
  public List<Space> getInvitedSpacesList() throws Exception {
    this.invitedSpacesList = loadInvitedSpaces(0, this.invitedSpacesList.size());
    int realInvitedSpacesListSize = this.invitedSpacesList.size();
    
    if (isHasUpdatedSpace()) {
      setHasUpdatedSpace(false);
    }
    
    setEnableLoadNext((realInvitedSpacesListSize >= SPACES_PER_PAGE)
            && (realInvitedSpacesListSize < getInvitedSpacesNum()));
    
    return this.invitedSpacesList;
  }

  /**
   * Sets list of invited space.
   * 
   * @param invitedSpacesList the invitedSpacesList to set
   */
  public void setInvitedSpacesList(List<Space> invitedSpacesList) {
    this.invitedSpacesList = invitedSpacesList;
  }
  
  /**
   * Gets number of invited space.
   * 
   * @return the invitedSpacesNum
   */
  public int getInvitedSpacesNum() {
    return invitedSpacesNum;
  }

  /**
   * Sets number of invited space.
   * 
   * @param invitedSpacesNum the invitedSpacesNum to set
   */
  public void setInvitedSpacesNum(int invitedSpacesNum) {
    this.invitedSpacesNum = invitedSpacesNum;
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
   * @return the invitedSpacesListAccess
   */
  public ListAccess<Space> getInvitedSpacesListAccess() {
    return invitedSpacesListAccess;
  }

  /**
   * Sets spaces with ListAccess type.
   * 
   * @param invitedSpacesListAccess the invitedSpacesListAccess to set
   */
  public void setInvitedSpacesListAccess(ListAccess<Space> invitedSpacesListAccess) {
    this.invitedSpacesListAccess = invitedSpacesListAccess;
  }

  /**
   * Loads more space.
   * @throws Exception
   * @since 1.2.2
   */
  public void loadNext() throws Exception {
    currentLoadIndex += loadingCapacity;
    if (currentLoadIndex <= getInvitedSpacesNum()) {
      this.invitedSpacesList.addAll(new ArrayList<Space>(Arrays.asList(getInvitedSpacesListAccess()
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
    setInvitedSpacesList(loadInvitedSpaces(currentLoadIndex, loadingCapacity));
  }

  private List<Space> loadInvitedSpaces(int index, int length) throws Exception {
    String charSearch = getSelectedChar();
    String searchCondition = uiSpaceSearch.getSpaceNameSearch();
    
    if (SEARCH_ALL.equals(charSearch) || (charSearch == null && searchCondition == null)) {
      setInvitedSpacesListAccess(getSpaceService().getInvitedSpacesWithListAccess(getUserId()));
    } else if (searchCondition != null) {
      setInvitedSpacesListAccess(getSpaceService().getInvitedSpacesByFilter(getUserId(), new SpaceFilter(searchCondition)));
    } else if(charSearch != null) {
      setInvitedSpacesListAccess(getSpaceService().getInvitedSpacesByFilter(getUserId(), new SpaceFilter(charSearch.charAt(0))));
    }
    
    setInvitedSpacesNum(getInvitedSpacesListAccess().getSize());
    uiSpaceSearch.setSpaceNum(getInvitedSpacesNum());
    Space[] spaces = getInvitedSpacesListAccess().load(index, length);
    
    return new ArrayList<Space>(Arrays.asList(spaces));
  }
  
  /**
   * Listeners loading more space action.
   * @author <a href="mailto:hanhvq@exoplatform.com">Hanh Vi Quoc</a>
   * @since Aug 18, 2011
   * @since 1.2.2
   */
  static public class LoadMoreSpaceActionListener extends EventListener<UIManageInvitationSpaces> {
    public void execute(Event<UIManageInvitationSpaces> event) throws Exception {
      UIManageInvitationSpaces uiManageInvitedSpaces = event.getSource();
      uiManageInvitedSpaces.loadNext();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManageInvitedSpaces);
    }
  }
  
  /**
   * Listens event that broadcast from UISpaceSearch.
   * 
   * @author <a href="mailto:hanhvq@exoplatform.com">Hanh Vi Quoc</a>
   * @since Aug 19, 2011
   */
  static public class SearchActionListener extends EventListener<UIManageInvitationSpaces> {
    @Override
    public void execute(Event<UIManageInvitationSpaces> event) throws Exception {
      UIManageInvitationSpaces uiManageInvitedSpaces = event.getSource();
      WebuiRequestContext ctx = event.getRequestContext();
      String charSearch = ctx.getRequestParameter(OBJECTID);
      
      if (charSearch == null) {
        uiManageInvitedSpaces.setSelectedChar(null);
      } else {
        uiManageInvitedSpaces.uiSpaceSearch.getUIStringInput(SPACE_SEARCH).setValue("");
        uiManageInvitedSpaces.uiSpaceSearch.setSpaceNameSearch(null);
        uiManageInvitedSpaces.setSelectedChar(charSearch);
      }
      
      uiManageInvitedSpaces.loadSearch();
      uiManageInvitedSpaces.setLoadAtEnd(false);
      ctx.addUIComponentToUpdateByAjax(uiManageInvitedSpaces);
    }
  }

  /**
   * This action is triggered when user clicks on Accept Space Invitation. When accepting, that user
   * will be the member of the space.
   */
  public static class AcceptActionListener extends EventListener<UIManageInvitationSpaces> {

    @Override
    public void execute(Event<UIManageInvitationSpaces> event) throws Exception {
      UIManageInvitationSpaces uiManageInvitationSpaces = event.getSource();
      SpaceService spaceService = Utils.getSpaceService();
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApp = ctx.getUIApplication();
      String spaceId = ctx.getRequestParameter(OBJECTID);
      String userId = Utils.getViewerRemoteId();

      Space space = spaceService.getSpaceById(spaceId);
      uiManageInvitationSpaces.setLoadAtEnd(false);
      
      if (space == null) {
        uiApp.addMessage(new ApplicationMessage(SPACE_DELETED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      if (!spaceService.isInvitedUser(space, userId)) {
        uiApp.addMessage(new ApplicationMessage(INVITATION_REVOKED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      spaceService.addMember(space, userId);
      uiManageInvitationSpaces.setHasUpdatedSpace(true);
      SpaceUtils.updateWorkingWorkSpace();
      
      JavascriptManager jsManager = ctx.getJavascriptManager();
      jsManager.addJavascript("try { window.location.href='" + Utils.getSpaceHomeURL(space) + "' } catch(e) {" +
          "window.location.href('" + Utils.getSpaceHomeURL(space) + "') }");
    }
  }

  /**
   * This action is triggered when user clicks on Ignore Space Invitation. When denying, that space
   * will remove the user from pending list.
   */
  public static class IgnoreActionListener extends EventListener<UIManageInvitationSpaces> {

    @Override
    public void execute(Event<UIManageInvitationSpaces> event) throws Exception {
      UIManageInvitationSpaces uiManageInvitationSpaces = event.getSource();
      SpaceService spaceService = Utils.getSpaceService();
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApp = ctx.getUIApplication();
      String spaceId = ctx.getRequestParameter(OBJECTID);
      String userId = Utils.getViewerRemoteId();
      Space space = spaceService.getSpaceById(spaceId);
      uiManageInvitationSpaces.setLoadAtEnd(false);
      
      if (space == null) {
        uiApp.addMessage(new ApplicationMessage(SPACE_DELETED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      if (!spaceService.isInvitedUser(space, userId)) {
        uiApp.addMessage(new ApplicationMessage(INVITATION_REVOKED_INFO, null, ApplicationMessage.INFO));
        return;
      }
      uiManageInvitationSpaces.setHasUpdatedSpace(true);
      spaceService.removeInvitedUser(space, userId);
   }
  }
  
  /**
   * Gets role of the user in a specific space for displaying in template.
   *
   * @param spaceId
   * @return UIManageMySpaces.LEADER if the remote user is the space's leader <br />
   *         UIManageMySpaces.MEMBER if the remote user is the space's member
   * @throws SpaceException
   */
  public int getRole(String spaceId) throws SpaceException {
    SpaceService spaceService = Utils.getSpaceService();
    if (spaceService.hasSettingPermission(spaceService.getSpaceById(spaceId), Utils.getViewerRemoteId())) {
      return LEADER;
    }
    return MEMBER;
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
   * Gets remote user Id.
   *
   * @return remote userId
   */
  private String getUserId() {
    if (userId == null) {
      userId = Util.getPortalRequestContext().getRemoteUser();
    }
    return userId;
  }
}
