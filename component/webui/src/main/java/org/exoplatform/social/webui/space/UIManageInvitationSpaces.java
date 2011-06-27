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
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.Utils;
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
 * UIManageInvitationSpaces.java used for managing invitation spaces. <br />
 * Created by The eXo Platform SAS
 * @author tung.dang <tungcnw at gmail dot com>
 * @since Nov 02, 2009
 */

@ComponentConfig(
  template="classpath:groovy/social/webui/space/UIManageInvitationSpaces.gtmpl",
  events = {
    @EventConfig(listeners = UIManageInvitationSpaces.AcceptActionListener.class),
    @EventConfig(listeners = UIManageInvitationSpaces.DenyActionListener.class)
  }
)
public class UIManageInvitationSpaces extends UIContainer {
  private static final String MSG_ERROR_ACCEPT_INVITATION = "UIManageInvitationSpaces.msg.error_accept_invitation";
  private static final String MSG_ERROR_DENY_INVITATION = "UIManageInvitationSpaces.msg.error_deny_invitation";
  private static final String SPACE_DELETED_INFO = "UIManageInvitationSpaces.msg.DeletedInfo";
  private static final String INVITATION_REVOKED_INFO = "UIManageInvitationSpaces.msg.RevokedInfo";
  private static final String INCOMING_STATUS = "incoming";

  /**
   * SEARCH ALL.
   */
  private static final String SEARCH_ALL = "All";
  
  /**
   * The first page
   */
  private static final int FIRST_PAGE = 1;

  static public final Integer LEADER = 1, MEMBER = 2;

  private UIPageIterator iterator;
  private final Integer SPACES_PER_PAGE = 4;
  private final String ITERATOR_ID = "UIIteratorInvitationSpaces";
  private List<Space> spaces; // for search result
  private UISpaceSearch uiSpaceSearch = null;

  /**
   * Constructor for initialize UIPopupWindow for adding new space popup.
   *
   * @throws Exception
   */
  public UIManageInvitationSpaces() throws Exception {
    uiSpaceSearch = createUIComponent(UISpaceSearch.class, null, "UISpaceSearch");
    uiSpaceSearch.setTypeOfRelation(INCOMING_STATUS);
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
   * Gets all user's spaces.
   *
   * @return space list
   * @throws Exception
   */
  public List<Space> getInvitationSpaces() throws Exception {
    List<Space> userSpaces = Utils.getSpaceService().getInvitedSpaces(Utils.getViewerRemoteId());
    return SpaceUtils.getOrderedSpaces(userSpaces);
  }

  /**
   * Gets paginated spaces in which user is member or leader.
   *
   * @return paginated spaces list
   * @throws Exception
   */
  public List<Space> getInvitedSpaces() throws Exception {
    uiSpaceSearch.setSpaceNameForAutoSuggest(getInvitedSpaceNames());
    return getDisplayInvitedSpace(iterator);
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
   * This action is triggered when user clicks on Accept Space Invitation. When accepting, that user
   * will be the member of the space.
   */
  public static class AcceptActionListener extends EventListener<UIManageInvitationSpaces> {

    @Override
    public void execute(Event<UIManageInvitationSpaces> event) throws Exception {
      SpaceService spaceService = Utils.getSpaceService();
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApp = ctx.getUIApplication();
      String spaceId = ctx.getRequestParameter(OBJECTID);
      String userId = Utils.getViewerRemoteId();

      Space space = spaceService.getSpaceById(spaceId);
     
      if (space == null) {
        uiApp.addMessage(new ApplicationMessage(SPACE_DELETED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      if (!spaceService.isInvitedUser(space, userId)) {
        uiApp.addMessage(new ApplicationMessage(INVITATION_REVOKED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      spaceService.addMember(space, userId);
      SpaceUtils.updateWorkingWorkSpace();
    }
  }

  /**
   * This action is triggered when user clicks on Deny Space Invitation. When denying, that space
   * will remove the user from pending list.
   */
  public static class DenyActionListener extends EventListener<UIManageInvitationSpaces> {

    @Override
    public void execute(Event<UIManageInvitationSpaces> event) throws Exception {
      SpaceService spaceService = Utils.getSpaceService();
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApp = ctx.getUIApplication();
      String spaceId = ctx.getRequestParameter(OBJECTID);
      String userId = Utils.getViewerRemoteId();
      Space space = spaceService.getSpaceById(spaceId);
      
      if (space == null) {
        uiApp.addMessage(new ApplicationMessage(SPACE_DELETED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      if (!spaceService.isInvitedUser(space, userId)) {
        uiApp.addMessage(new ApplicationMessage(INVITATION_REVOKED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      spaceService.removeInvitedUser(space, userId);
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
   * Gets invited space name list.
   *
   * @return invited space name list
   * @throws Exception
   */
  private List<String> getInvitedSpaceNames() throws Exception {
    List<Space> invitedSpaces = getInvitationSpaces();
    List<String> invitedSpaceNames = new ArrayList<String>();
    for (Space space : invitedSpaces) {
      invitedSpaceNames.add(space.getDisplayName());
    }

    return invitedSpaceNames;
  }

  /**
   * Gets displayed invited space list.
   *
   * @param spaces
   * @param pageIterator_
   * @return invited space list
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private List<Space> getDisplayInvitedSpace(UIPageIterator pageIterator_) throws Exception {
    int currentPage = pageIterator_.getCurrentPage();
    SpaceService spaceService = Utils.getSpaceService();
    String userId = Utils.getViewerRemoteId();
    String selectedChar = this.uiSpaceSearch.getSelectedChar();
    String spaceNameSearch = this.uiSpaceSearch.getSpaceNameSearch();
    LazyPageList<Space> pageList = null;
    
    if ((selectedChar == null && spaceNameSearch == null) || (selectedChar != null && selectedChar.equals(SEARCH_ALL))) {
      pageList = new LazyPageList<Space>(spaceService.getInvitedSpacesWithListAccess(userId), SPACES_PER_PAGE);
    } else {
      SpaceFilter spaceFilter = null;
      if (selectedChar != null) {
        spaceFilter = new SpaceFilter(selectedChar.charAt(0));
      } else {
        spaceFilter = new SpaceFilter(spaceNameSearch);
      }
      pageList = new LazyPageList<Space>(spaceService.getInvitedSpacesByFilter(userId, spaceFilter), SPACES_PER_PAGE);
    }
    
    pageIterator_.setPageList(pageList);
    int availablePage = pageIterator_.getAvailablePage();
    if (this.uiSpaceSearch.isNewSearch()) {
      pageIterator_.setCurrentPage(FIRST_PAGE);
    } else if (currentPage > availablePage) {
      pageIterator_.setCurrentPage(availablePage);
    } else {
      pageIterator_.setCurrentPage(currentPage);
    }
    this.uiSpaceSearch.setNewSearch(false);
    return pageIterator_.getCurrentPageData();
  }
}