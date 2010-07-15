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
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceListAccess;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.model.SpaceAttachment;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.UISpaceSearch;
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
    @EventConfig(listeners = UIManageInvitationSpaces.DenyActionListener.class),
    @EventConfig(listeners = UIManageInvitationSpaces.SearchActionListener.class , phase = Phase.DECODE)
  }
)
public class UIManageInvitationSpaces extends UIContainer {
  static private final String MSG_ERROR_ACCEPT_INVITATION = "UIManageInvitationSpaces.msg.error_accept_invitation";
  static private final String MSG_ERROR_DENY_INVITATION = "UIManageInvitationSpaces.msg.error_deny_invitation";
  private static final String SPACE_DELETED_INFO = "UIManageInvitationSpaces.msg.DeletedInfo";
  private static final String INVITATION_REVOKED_INFO = "UIManageInvitationSpaces.msg.RevokedInfo";

  static public final Integer LEADER = 1, MEMBER = 2;

  private UIPageIterator iterator;
  private final Integer SPACES_PER_PAGE = 4;
  private final String ITERATOR_ID = "UIIteratorInvitationSpaces";
  private SpaceService spaceService = null;
  private String userId = null;
  private List<Space> spaces; // for search result
  private UISpaceSearch uiSpaceSearch = null;

  /**
   * Constructor for initialize UIPopupWindow for adding new space popup
   * @throws Exception
   */
  public UIManageInvitationSpaces() throws Exception {
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
   * gets spaceService
   * @return spaceService
   */
  private SpaceService getSpaceService() {
    if(spaceService == null)
      spaceService = getApplicationComponent(SpaceService.class);
    return spaceService;
  }

  /**
   * gets remote user Id
   * @return userId
   */
  private String getUserId() {
    if(userId == null)
      userId = Util.getPortalRequestContext().getRemoteUser();
    return userId;
  }

  /**
   * gets all user's spaces
   * @return space list
   * @throws Exception
   */
  public List<Space> getInvitationSpaces() throws Exception {
    SpaceService spaceService = getSpaceService();
    String userId = getUserId();
    List<Space> userSpaces = spaceService.getInvitedSpaces(userId);
    return SpaceUtils.getOrderedSpaces(userSpaces);
  }

  /**
   * gets paginated spaces in which user is member or leader
   *
   * @return paginated spaces list
   * @throws Exception
   */
  public List<Space> getInvitedSpaces() throws Exception {
    List<Space> listSpace = getSpaceList();
    uiSpaceSearch.setSpaceNameForAutoSuggest(getInvitedSpaceNames());
    return getDisplayInvitedSpace(listSpace, iterator);
  }

  /**
   * Get role of the user in a specific space for displaying in template
   *
   * @param spaceId
   * @return UIManageMySpaces.LEADER if the remote user is the space's leader <br />
   *         UIManageMySpaces.MEMBER if the remote user is the space's member
   * @throws SpaceException
   */
  public int getRole(String spaceId) throws SpaceException {
    SpaceService spaceService = getSpaceService();
    String userId = getUserId();
    if(spaceService.hasEditPermission(spaceId, userId)) {
      return LEADER;
    }
    return MEMBER;
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
  * This action is triggered when user clicks on Accept Space Invitation
  * When accepting, that user will be the member of the space
  */
  static public class AcceptActionListener extends EventListener<UIManageInvitationSpaces> {

   @Override
   public void execute(Event<UIManageInvitationSpaces> event) throws Exception {
     UIManageInvitationSpaces uiForm = event.getSource();
     SpaceService spaceService = uiForm.getSpaceService();
     WebuiRequestContext ctx = event.getRequestContext();
     UIApplication uiApp = ctx.getUIApplication();
     String spaceId = ctx.getRequestParameter(OBJECTID);
     String userId = uiForm.getUserId();

     Space space = spaceService.getSpaceById(spaceId);

     if (space == null) {
       uiApp.addMessage(new ApplicationMessage(SPACE_DELETED_INFO, null, ApplicationMessage.INFO));
       return;
     }

     if (!spaceService.isInvited(space, userId)) {
       uiApp.addMessage(new ApplicationMessage(INVITATION_REVOKED_INFO, null, ApplicationMessage.INFO));
       return;
     }

     try {
       spaceService.acceptInvitation(spaceId, userId);
     } catch(SpaceException se) {
       uiApp.addMessage(new ApplicationMessage(MSG_ERROR_ACCEPT_INVITATION, null, ApplicationMessage.ERROR));
       return;
     }

     PageNavigation spaceNavigation = SpaceUtils.getGroupNavigation(space.getGroupId());
     SpaceUtils.setNavigation(spaceNavigation);
     SpaceUtils.updateWorkingWorkSpace();
   }
  }

  /**
  * This action is triggered when user clicks on Deny Space Invitation
  * When denying, that space will remove the user from pending list
  */
  static public class DenyActionListener extends EventListener<UIManageInvitationSpaces> {

   @Override
   public void execute(Event<UIManageInvitationSpaces> event) throws Exception {
     UIManageInvitationSpaces uiForm = event.getSource();
     SpaceService spaceService = uiForm.getSpaceService();
     WebuiRequestContext ctx = event.getRequestContext();
     UIApplication uiApp = ctx.getUIApplication();
     String spaceId = ctx.getRequestParameter(OBJECTID);
     String userId = uiForm.getUserId();
     Space space = spaceService.getSpaceById(spaceId);

     if (space == null) {
       uiApp.addMessage(new ApplicationMessage(SPACE_DELETED_INFO, null, ApplicationMessage.INFO));
       return;
     }

     if (!spaceService.isInvited(space, userId)) {
       uiApp.addMessage(new ApplicationMessage(INVITATION_REVOKED_INFO, null, ApplicationMessage.INFO));
       return;
     }

     try {
       spaceService.denyInvitation(spaceId, userId);
     } catch(SpaceException se) {
       uiApp.addMessage(new ApplicationMessage(MSG_ERROR_DENY_INVITATION, null, ApplicationMessage.ERROR));
     }
   }
  }

  /**
   * triggers this action when user clicks on search button
   * @author hoatle
   *
   */
  public static class SearchActionListener extends EventListener<UIManageInvitationSpaces> {
    @Override
    public void execute(Event<UIManageInvitationSpaces> event) throws Exception {
      UIManageInvitationSpaces uiForm = event.getSource();
      UISpaceSearch uiSpaceSearch = uiForm.getChild(UISpaceSearch.class);
      List<Space> spaceList = uiSpaceSearch.getSpaceList();
      uiForm.setSpaces(spaceList);
    }
  }

  /**
   * gets image source url
   * @param space
   * @return image source url
   * @throws Exception
   */
  public String getImageSource(Space space) throws Exception {
    SpaceAttachment spaceAtt = (SpaceAttachment) space.getSpaceAttachment();
    if (spaceAtt != null) {
      return "/"+ getRestContext() + "/jcr/" + getRepository()+ "/" + spaceAtt.getWorkspace()
              + spaceAtt.getDataPath() + "/?rnd=" + System.currentTimeMillis();
    }
    return null;
  }

  /**
   * gets invited space name list
   * @return invited space name list
   * @throws Exception
   */
  private List<String> getInvitedSpaceNames() throws Exception {
    List<Space> invitedSpaces = getInvitationSpaces();
    List<String> invitedSpaceNames = new ArrayList<String>();
    for (Space space: invitedSpaces) {
      invitedSpaceNames.add(space.getName());
    }

    return invitedSpaceNames;
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
   * gets portal name
   * @return portal name
   */
  private String getPortalName() {
    return PortalContainer.getCurrentPortalContainerName();
  }

  /**
   * gets repository name
   * @return repository name
   * @throws Exception
   */
  private String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;
    return rService.getCurrentRepository().getConfiguration().getName() ;
  }

  /**
   * gets space list
   * @return space list
   * @throws Exception
   */
  private List<Space> getSpaceList() throws Exception {
    List<Space> spaceList = getSpaces();
    List<Space> allInvitationSpace = getInvitationSpaces();
    List<Space> invitedSpaces = new ArrayList<Space>();
    if (allInvitationSpace.size() == 0) return allInvitationSpace;
    if(spaceList != null) {
      Iterator<Space> spaceItr = spaceList.iterator();
      while(spaceItr.hasNext()) {
        Space space = spaceItr.next();
        for(Space invitationSpace : allInvitationSpace) {
          if(space.getName().equals(invitationSpace.getName())){
            invitedSpaces.add(invitationSpace);
            break;
          }
        }
      }

      return invitedSpaces;
    }

    return allInvitationSpace;
  }

  /**
   * gets displayed invited space list
   * @param spaces_
   * @param pageIterator_
   * @return invited space list
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private List<Space> getDisplayInvitedSpace(List<Space> spaces, UIPageIterator pageIterator_) throws Exception {
    int currentPage = pageIterator_.getCurrentPage();
    LazyPageList<Space> pageList = new LazyPageList<Space>(new SpaceListAccess(spaces), SPACES_PER_PAGE);
    pageIterator_.setPageList(pageList);
    int pageCount = pageIterator_.getAvailablePage();
    if (pageCount >= currentPage) {
      pageIterator_.setCurrentPage(currentPage);
    } else if (pageCount < currentPage) {
      pageIterator_.setCurrentPage(currentPage - 1);
    }
    return pageIterator_.getCurrentPageData();
  }
}
