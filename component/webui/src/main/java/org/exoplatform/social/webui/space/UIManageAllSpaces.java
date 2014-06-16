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
import java.util.ResourceBundle;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
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
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * UI component to list all spaces that is associated with current logged-in user: public spaces to join,
 * invitation spaces to accept or deny to join, his spaces in which he is a member or manager to access or manage.
 * @author <a href="mailto:hanhvq@exoplatform.com">Hanh Vi Quoc</a>
 * @since Aug 18, 2011
 * @since 1.2.2
 */
@ComponentConfig(
  template = "war:/groovy/social/webui/space/UIManageAllSpaces.gtmpl",
  events = {
    @EventConfig(listeners = UIManageAllSpaces.RequestToJoinActionListener.class),
    @EventConfig(listeners = UIManageAllSpaces.CancelInvitationActionListener.class),
    @EventConfig(listeners = UIManageAllSpaces.AcceptInvitationActionListener.class),
    @EventConfig(listeners = UIManageAllSpaces.IgnoreInvitationActionListener.class),
    @EventConfig(listeners = UIManageAllSpaces.LeaveSpaceActionListener.class),
    @EventConfig(listeners = UIManageAllSpaces.DeleteSpaceActionListener.class),
    @EventConfig(listeners = UIManageAllSpaces.SearchActionListener.class),
    @EventConfig(listeners = UIManageAllSpaces.LoadMoreSpaceActionListener.class)
  }
)
public class UIManageAllSpaces extends UIContainer {
  public static final String SEARCH_ALL = "All";
  private static final String SPACE_SEARCH = "SpaceSearch";
  private static final Log LOG = ExoLogger.getLogger(UIManageAllSpaces.class);
  
  private static final String SPACE_DELETED_INFO = "UIManageAllSpaces.msg.DeletedInfo";
  private static final String MEMBERSHIP_REMOVED_INFO = "UIManageAllSpaces.msg.MemberShipRemovedInfo";
  private static final String MSG_WARNING_LEAVE_SPACE = "UIManageAllSpaces.msg.warning_leave_space";
  private static final String INVITATION_REVOKED_INFO = "UIManageAllSpaces.msg.RevokedInfo";

  private SpaceService spaceService = null;
  private String userId = null;
  private final Integer SPACES_PER_PAGE = 20;
  private static final String ALL_SPACES_STATUS = "all_spaces";
  private List<Space> spaces; // for search result
  private UISpaceSearch uiSpaceSearch = null;

  private boolean loadAtEnd = false;
  private boolean hasUpdatedSpace = false;
  private int currentLoadIndex;
  private boolean enableLoadNext;
  private int loadingCapacity;
  private String spaceNameSearch;
  private List<Space> spacesList;
  private ListAccess<Space> spacesListAccess;
  private int spacesNum;
  private String selectedChar = null;
  
  public enum TypeOfSpace {
    INVITED,
    SENT,
    NONE,
    MEMBER,
    MANAGER
  }
  
  /**
   * Constructor to initialize iterator.
   *
   * @throws Exception
   */
  public UIManageAllSpaces() throws Exception {
    uiSpaceSearch = createUIComponent(UISpaceSearch.class, null, "UISpaceSearch");
    uiSpaceSearch.setTypeOfRelation(ALL_SPACES_STATUS);
    addChild(uiSpaceSearch);
    init();
  }
  
  /**
   * Inits at the first loading.
   */
  public void init() {
    try {
      setHasUpdatedSpace(false);
      setLoadAtEnd(false);
      enableLoadNext = false;
      currentLoadIndex = 0;
      loadingCapacity = SPACES_PER_PAGE;
      spacesList = new ArrayList<Space>();
      setSpacesList(loadSpaces(currentLoadIndex, loadingCapacity));
      setSelectedChar(SEARCH_ALL);
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
   * @param enableLoadNext the enableLoadNext to set
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
   * @param loadAtEnd the loadAtEnd to set
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
   * @param hasUpdatedSpace the hasUpdatedSpace to set
   */
  public void setHasUpdatedSpace(boolean hasUpdatedSpace) {
    this.hasUpdatedSpace = hasUpdatedSpace;
  }

  /**
   * Gets list of all type of space.
   * 
   * @return the spacesList
   * @throws Exception 
   */
  public List<Space> getSpacesList() throws Exception {
    if (isHasUpdatedSpace()) {
      setHasUpdatedSpace(false);
      setSpacesList(loadSpaces(0, this.spacesList.size()));
    }
    
    setEnableLoadNext((this.spacesList.size() >= SPACES_PER_PAGE)
            && (this.spacesList.size() < getSpacesNum()));
    
    return this.spacesList;
  }

  /**
   * Sets list of all type of space.
   * 
   * @param spacesList the spacesList to set
   */
  public void setSpacesList(List<Space> spacesList) {
    this.spacesList = spacesList;
  }
  
  /**
   * Gets number of spaces for displaying.
   * 
   * @return the spacesNum
   */
  public int getSpacesNum() {
    return spacesNum;
  }

  /**
   * Sets number of spaces for displaying.
   * @param spacesNum the spacesNum to set
   */
  public void setSpacesNum(int spacesNum) {
    this.spacesNum = spacesNum;
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
   * @return the spacesListAccess
   */
  public ListAccess<Space> getSpacesListAccess() {
    return spacesListAccess;
  }

  /**
   * Sets spaces with ListAccess type.
   * 
   * @param spacesListAccess the spacesListAccess to set
   */
  public void setSpacesListAccess(ListAccess<Space> spacesListAccess) {
    this.spacesListAccess = spacesListAccess;
  }

  /**
   * Loads more space.
   * @throws Exception
   */
  public void loadNext() throws Exception {
    currentLoadIndex += loadingCapacity;
    if (currentLoadIndex <= getSpacesNum()) {
      this.spacesList.addAll(new ArrayList<Space>(Arrays.asList(getSpacesListAccess()
                                                 .load(currentLoadIndex, loadingCapacity))));
    }
  }
  
  /**
   * Loads space when searching.
   * @throws Exception
   */
  public void loadSearch() throws Exception {
    currentLoadIndex = 0;
    setSpacesList(loadSpaces(currentLoadIndex, loadingCapacity));
  }
  
  /**
   * Gets type of one given space of current user.
   * 
   * @param space
   * @return
   */
  protected static String getTypeOfSpace(Space space) {
    String currentUserId = Utils.getOwnerIdentity().getRemoteId();
    SpaceService spaceService = Utils.getSpaceService();
    
    if (spaceService.isInvitedUser(space, currentUserId)) { // Received
      return TypeOfSpace.INVITED.toString();
    } else if (spaceService.isPendingUser(space, currentUserId)) { // Sent
      return TypeOfSpace.SENT.toString();
    } else if (spaceService.isMember(space, currentUserId)) { // Member
      if (spaceService.isManager(space, currentUserId)) {
        return TypeOfSpace.MANAGER.toString(); // Manager
      }
      return TypeOfSpace.MEMBER.toString(); // Is member
    } 
    
    return TypeOfSpace.NONE.toString(); // No relationship with this space.
  }
  
  protected boolean isSuperUser(Space space) {
    String currentUserId = Utils.getOwnerIdentity().getRemoteId();
    SpaceService spaceService = Utils.getSpaceService();
    
    return spaceService.hasSettingPermission(space, currentUserId);
  }
  
  private List<Space> loadSpaces(int index, int length) throws Exception {
    String charSearch = getSelectedChar();
    String searchCondition = uiSpaceSearch.getSpaceNameSearch();
    String userId = Util.getPortalRequestContext().getRemoteUser();
    
    if (SEARCH_ALL.equals(charSearch) || (charSearch == null && searchCondition == null)) {
      setSpacesListAccess(getSpaceService().getVisibleSpacesWithListAccess(userId, null));
    } else if (searchCondition != null) {
      setSpacesListAccess(getSpaceService().getVisibleSpacesWithListAccess(userId, new SpaceFilter(searchCondition)));
    } else if(charSearch != null) {
      setSpacesListAccess(getSpaceService().getVisibleSpacesWithListAccess(userId, new SpaceFilter(charSearch.charAt(0))));
    }
    
    setSpacesNum(getSpacesListAccess().getSize());
    uiSpaceSearch.setSpaceNum(getSpacesNum());
    Space[] spaces = getSpacesListAccess().load(index, length);
    
    return new ArrayList<Space>(Arrays.asList(spaces));
  }
  
  /**
   * Listeners loading more space action.
   * 
   * @author <a href="mailto:hanhvq@exoplatform.com">Hanh Vi Quoc</a>
   * @since Aug 18, 2011
   */
  static public class LoadMoreSpaceActionListener extends EventListener<UIManageAllSpaces> {
    public void execute(Event<UIManageAllSpaces> event) throws Exception {
      UIManageAllSpaces uiManageAllSpaces = event.getSource();
      if (uiManageAllSpaces.currentLoadIndex > uiManageAllSpaces.spacesNum) {
        return;
      }
      uiManageAllSpaces.loadNext();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManageAllSpaces);
    }
  }
  
  /**
   * Listens event that broadcast from UISpaceSearch.
   * 
   * @author <a href="mailto:hanhvq@exoplatform.com">Hanh Vi Quoc</a>
   * @since Aug 19, 2011
   */
  static public class SearchActionListener extends EventListener<UIManageAllSpaces> {
    @Override
    public void execute(Event<UIManageAllSpaces> event) throws Exception {
      UIManageAllSpaces uiManageAllSpaces = event.getSource();
      WebuiRequestContext ctx = event.getRequestContext();
      String charSearch = ctx.getRequestParameter(OBJECTID);
      
      if (charSearch == null) {
        uiManageAllSpaces.setSelectedChar(null);
      } else {
        ResourceBundle resApp = ctx.getApplicationResourceBundle();
        String defaultSpaceNameAndDesc = resApp.getString(uiManageAllSpaces.getId() + ".label.DefaultSpaceNameAndDesc");
        ((UIFormStringInput) uiManageAllSpaces.uiSpaceSearch.getUIStringInput(SPACE_SEARCH)).setValue(defaultSpaceNameAndDesc);
        uiManageAllSpaces.setSelectedChar(charSearch);
        uiManageAllSpaces.uiSpaceSearch.setSpaceNameSearch(null);
      }
      
      uiManageAllSpaces.loadSearch();
      uiManageAllSpaces.setLoadAtEnd(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiManageAllSpaces);
    }
  }
  
  /**
   * Checks if the remote user has edit permission  of a space.
   *
   * @param space
   * @return true or false
   * @throws Exception
   */
  public boolean hasEditPermission(Space space) throws Exception {
    return spaceService.hasSettingPermission(space, getUserId());
  }

  /**
   * Check if the remote user has access permission.
   * 
   * @param space
   * @return
   * @throws Exception
   */
  protected boolean hasAccessPermission(Space space) throws Exception {
    return spaceService.hasAccessPermission(space, getUserId());
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
  static public class RequestToJoinActionListener extends EventListener<UIManageAllSpaces> {
    public void execute(Event<UIManageAllSpaces> event) throws Exception {
      UIManageAllSpaces uiManageAllSpaces = event.getSource();
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApp = ctx.getUIApplication();
      SpaceService spaceService = uiManageAllSpaces.getSpaceService();
      String spaceId = ctx.getRequestParameter(OBJECTID);
      String userId = uiManageAllSpaces.getUserId();
      Space space = spaceService.getSpaceById(spaceId);
      uiManageAllSpaces.setLoadAtEnd(false);
      
      if (space == null) {
        uiApp.addMessage(new ApplicationMessage(SPACE_DELETED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      spaceService.addPendingUser(space, userId);
      uiManageAllSpaces.setHasUpdatedSpace(true);
      ctx.addUIComponentToUpdateByAjax(uiManageAllSpaces);
    }
  }

  /**
   * This action is triggered when user click on DeleteSpace a prompt popup is display for
   * confirmation, if yes delete that space; otherwise, do nothing.
   */
  static public class DeleteSpaceActionListener extends EventListener<UIManageAllSpaces> {

    @Override
    public void execute(Event<UIManageAllSpaces> event) throws Exception {
      UIManageAllSpaces uiManageAllSpaces = event.getSource();
      SpaceService spaceService = uiManageAllSpaces.getSpaceService();
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApp = ctx.getUIApplication();
      String spaceId = ctx.getRequestParameter(OBJECTID);
      Space space = spaceService.getSpaceById(spaceId);
      String userId = uiManageAllSpaces.getUserId();
      uiManageAllSpaces.setLoadAtEnd(false);
      
      if (space == null) {
        uiApp.addMessage(new ApplicationMessage(SPACE_DELETED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      if (!spaceService.isMember(space, userId) && !spaceService.hasSettingPermission(space, userId)) {
        uiApp.addMessage(new ApplicationMessage(MEMBERSHIP_REMOVED_INFO, null, ApplicationMessage.INFO));
        return;
      }
      uiManageAllSpaces.setHasUpdatedSpace(true);
      spaceService.deleteSpace(space);
      SpaceUtils.updateWorkingWorkSpace();
    }

  }

  /**
   * This action is triggered when user click on LeaveSpace <br /> The leaving space will remove
   * that user in the space. <br /> If that user is the only leader -> can't not leave that space
   * <br />
   */
  static public class LeaveSpaceActionListener extends EventListener<UIManageAllSpaces> {
    public void execute(Event<UIManageAllSpaces> event) throws Exception {
      UIManageAllSpaces uiManageAllSpaces = event.getSource();
      SpaceService spaceService = uiManageAllSpaces.getSpaceService();
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApp = ctx.getUIApplication();
      String spaceId = ctx.getRequestParameter(OBJECTID);
      String userId = uiManageAllSpaces.getUserId();
      Space space = spaceService.getSpaceById(spaceId);
      uiManageAllSpaces.setLoadAtEnd(false);

      if (space == null) {
        uiApp.addMessage(new ApplicationMessage(SPACE_DELETED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      if (!spaceService.isMember(space, userId) && !spaceService.hasSettingPermission(space, userId)) {
        uiApp.addMessage(new ApplicationMessage(MEMBERSHIP_REMOVED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      if (spaceService.isOnlyManager(space, userId)) {
        uiApp.addMessage(new ApplicationMessage(MSG_WARNING_LEAVE_SPACE, null, ApplicationMessage.WARNING));
        return;
      }

      spaceService.removeMember(space, userId);
      uiManageAllSpaces.setHasUpdatedSpace(true);
      spaceService.setManager(space, userId, false);
      SpaceUtils.updateWorkingWorkSpace();
    }
  }
  
  /**
   * This action is triggered when user clicks on RevokePending action.
   */
  static public class CancelInvitationActionListener extends EventListener<UIManageAllSpaces> {
    @Override
    public void execute(Event<UIManageAllSpaces> event) throws Exception {
      UIManageAllSpaces uiManageAllSpaces = event.getSource();
      SpaceService spaceService = uiManageAllSpaces.getSpaceService();
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApp = ctx.getUIApplication();
      String spaceId = ctx.getRequestParameter(OBJECTID);
      String userId = uiManageAllSpaces.getUserId();

      Space space = spaceService.getSpaceById(spaceId);
      uiManageAllSpaces.setLoadAtEnd(false);
      
      if (space == null) {
        uiApp.addMessage(new ApplicationMessage(SPACE_DELETED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      spaceService.removePendingUser(space, userId);
      uiManageAllSpaces.setHasUpdatedSpace(true);
      ctx.addUIComponentToUpdateByAjax(uiManageAllSpaces);
    }
  }
  
  /**
   * This action is triggered when user clicks on Accept Space Invitation. When accepting, that user
   * will be the member of the space.
   */
  public static class AcceptInvitationActionListener extends EventListener<UIManageAllSpaces> {

    @Override
    public void execute(Event<UIManageAllSpaces> event) throws Exception {
      UIManageAllSpaces uiManageAllSpaces = event.getSource();
      SpaceService spaceService = Utils.getSpaceService();
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApp = ctx.getUIApplication();
      String spaceId = ctx.getRequestParameter(OBJECTID);
      String userId = Utils.getViewerRemoteId();

      Space space = spaceService.getSpaceById(spaceId);
      uiManageAllSpaces.setLoadAtEnd(false);
      
      if (space == null) {
        uiApp.addMessage(new ApplicationMessage(SPACE_DELETED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      if (!spaceService.isInvitedUser(space, userId)) {
        uiApp.addMessage(new ApplicationMessage(INVITATION_REVOKED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      spaceService.addMember(space, userId);
      uiManageAllSpaces.setHasUpdatedSpace(true);
      SpaceUtils.updateWorkingWorkSpace();
      
      JavascriptManager jsManager = ctx.getJavascriptManager();
      jsManager.addJavascript("try { window.location.href='" + Utils.getSpaceHomeURL(space) + "' } catch(e) {" +
          "window.location.href('" + Utils.getSpaceHomeURL(space) + "') }");
    }
  }

  /**
   * This action is triggered when user clicks on Ignore Space Invitation. When ignore, that space
   * will remove the user from pending list.
   */
  public static class IgnoreInvitationActionListener extends EventListener<UIManageAllSpaces> {

    @Override
    public void execute(Event<UIManageAllSpaces> event) throws Exception {
      UIManageAllSpaces uiManageAllSpaces = event.getSource();
      SpaceService spaceService = Utils.getSpaceService();
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApp = ctx.getUIApplication();
      String spaceId = ctx.getRequestParameter(OBJECTID);
      String userId = Utils.getViewerRemoteId();
      Space space = spaceService.getSpaceById(spaceId);
      uiManageAllSpaces.setLoadAtEnd(false);
      
      if (space == null) {
        uiApp.addMessage(new ApplicationMessage(SPACE_DELETED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      if (!spaceService.isInvitedUser(space, userId)) {
        uiApp.addMessage(new ApplicationMessage(INVITATION_REVOKED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      spaceService.removeInvitedUser(space, userId);
      uiManageAllSpaces.setHasUpdatedSpace(true);
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
    if (userId == null) {
      userId = Util.getPortalRequestContext().getRemoteUser();
    }
    return userId;
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
}
