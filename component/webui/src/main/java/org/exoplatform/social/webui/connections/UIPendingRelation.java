/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.webui.connections;

import java.util.List;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.webui.RelationshipListAccess;
import org.exoplatform.social.webui.URLUtils;
import org.exoplatform.social.webui.profile.UIProfileUserSearch;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Manages pending relation of all existing users. Manages actions
 * such as accept or deny invitation and search action.<br>
 *   - Get all users that have pending relation.<br>
 *   - Check the status of each user with current user then display the list.<br>
 *   - Listens to event: deny contact and search action.<br>
 *
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Aug 25, 2009
 */
@ComponentConfig(
  template =  "classpath:groovy/social/webui/connections/UIPendingRelation.gtmpl",
  events = {
    @EventConfig(listeners = UIPendingRelation.DenyContactActionListener.class),
    @EventConfig(listeners = UIPendingRelation.SearchActionListener.class, phase = Phase.DECODE)
  }
)
public class UIPendingRelation extends UIContainer {
  /** UIPageIterator ID. */
  private final String iteratorID_ = "UIPageIteratorPendingRelation";

  /** Label for display invitation is revoked information */
  private static final String INVITATION_REVOKED_INFO = "UIPendingRelation.label.RevokedInfo";

  /** Stores UIPageIterator instance. */
  UIPageIterator uiPageIterator_;

  /** Stores current identity. */
  Identity            currIdentity = null;

  /** RelationshipManager */
  RelationshipManager rm           = null;

  /** Stores IdentityManager instance. */
  IdentityManager     im           = null;

  /** Stores UIProfileUserSearch instance. */
  UIProfileUserSearch uiProfileUserSearchPending = null;

  /** Stores identities. */
  private List<Identity> identityList;
  
  /** The first page. */
  private static final int FIRST_PAGE = 1;

  /**
   * Gets identities.
   *
   * @return one list of identity.
   */
  public List<Identity> getIdentityList() {
    return identityList;
  }

  /**
   * Sets list identity.
   *
   * @param identityList
   *        Identities for setting to list.
   */
  public void setIdentityList(List<Identity> identityList) {
    this.identityList = identityList;
  }

  /**
   * Gets iterator for display.
   *
   * @return an iterator contains information for display.
   */
  public UIPageIterator getUIPageIterator() {
    return uiPageIterator_;
  }

  /**
   * Initializes components and add as child of form.<br>
   *
   * @throws Exception
   */
  public UIPendingRelation() throws Exception {
    uiPageIterator_ = createUIComponent(UIPageIterator.class, null, iteratorID_);
    addChild(uiPageIterator_);
    uiProfileUserSearchPending = createUIComponent(UIProfileUserSearch.class, null, "UIProfileUserSearch");
    addChild(uiProfileUserSearchPending);
  }

  /**
   * Get list of relationship that has status is PENDING.
   *
   * @return
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<Relationship> getPendingRelationList() throws Exception {
    List<Relationship> listRelationShip = getPendingRelationships();
    if (listRelationShip == null)
      return null;
    int currentPage = uiPageIterator_.getCurrentPage();
    LazyPageList<Relationship> pageList = new LazyPageList<Relationship>(new RelationshipListAccess(listRelationShip), 5);
    uiPageIterator_.setPageList(pageList) ;
    if (this.uiProfileUserSearchPending.isNewSearch()) {
      uiPageIterator_.setCurrentPage(FIRST_PAGE);
    } else {
      uiPageIterator_.setCurrentPage(currentPage);
    }
    this.uiProfileUserSearchPending.setNewSearch(false);
    return uiPageIterator_.getCurrentPageData();
  }

  /**
   * Gets the current portal name.<br>
   *
   * @return name of current portal.
   *
   */
  public String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance();
    return pcontainer.getPortalContainerInfo().getContainerName();
  }

  /**
   * Gets the current repository.<br>
   *
   * @return current repository through repository service.
   *
   * @throws Exception
   */
  public String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;
    return rService.getCurrentRepository().getConfiguration().getName() ;
  }

  /**
   * Gets current identity.<br>
   *
   * @return identity of current login user.
   *
   * @throws Exception
   */
  public Identity getCurrentIdentity() throws Exception {
      IdentityManager im = getIdentityManager();
      return im.getOrCreateIdentity("organization", getCurrentUserName());
  }

  /**
   * Gets the identity of current user is viewed by another.<br>
   *
   * @return identity of current user who is viewed.
   *
   * @throws Exception
   */
  public Identity getCurrentViewerIdentity() throws Exception {
    IdentityManager im = getIdentityManager();
    Identity identity = im.getOrCreateIdentity("organization", getCurrentViewerUserName());
    if (identity == null) identity = im.getOrCreateIdentity("organization", getCurrentUserName());
    return identity;
  }

  /**
   * Gets name of current login user.
   *
   * @return name of current login user.
   */
  public String getCurrentUserName() {
    RequestContext context = RequestContext.getCurrentInstance();
    return context.getRemoteUser();
  }

  /**
   * Gets currents name of user that is viewed by another.<br>
   *
   * @return name of user who is viewed.
   */
  public String getCurrentViewerUserName() {
    String username = URLUtils.getCurrentUser();
    if(username != null)
      return username;

    RequestContext context = RequestContext.getCurrentInstance();
    return context.getRemoteUser();
  }

  /**
   * Listens to deny action then delete the invitation.<br>
   *   - Gets information of user is invited or made request.<br>
   *   - Checks the relation to confirm that have got pending relation.<br>
   *   - Removes the current pending relation and save the new relation.<br>
   *
   */
  public static class DenyContactActionListener extends EventListener<UIPendingRelation> {
    public void execute(Event<UIPendingRelation> event) throws Exception {
      UIPendingRelation portlet = event.getSource();

      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      String currUserId = portlet.getCurrentUserName();

      IdentityManager im = portlet.getIdentityManager();
      Identity currIdentity = im.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                       currUserId);

      Identity requestedIdentity = im.getIdentity(userId);

      // TODO Check if relation is deleted by another user
      UIApplication uiApplication = event.getRequestContext().getUIApplication();
      Relationship.Type relationStatus = portlet.getContactStatus(requestedIdentity);
      if (relationStatus != Relationship.Type.PENDING) {
        uiApplication.addMessage(new ApplicationMessage(INVITATION_REVOKED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      RelationshipManager rm = portlet.getRelationshipManager();
      Relationship rel = rm.get(currIdentity, requestedIdentity);
      if (rel != null)
        rm.remove(rel);
    }
  }

  /**
   * Listens to search action that broadcasted from search form then set to current form.<br>
   *   - Gets search result from search form.<br>
   *   - Sets the search result to the current form that added search form as child.<br>
   */
  public static class SearchActionListener extends EventListener<UIPendingRelation> {
    @Override
    public void execute(Event<UIPendingRelation> event) throws Exception {
      UIPendingRelation uiPending = event.getSource();
      UIProfileUserSearch uiProfileUserSearch = uiPending.getChild(UIProfileUserSearch.class);
      List<Identity> identityList = uiProfileUserSearch.getIdentityList();
      uiPending.setIdentityList(identityList);
    }
  }

  /**
   * Return true to accept user is viewing can edit.
   *
   * @return true if current user is current login user.
   */
  public boolean isEditable () {
    RequestContext context = RequestContext.getCurrentInstance();
    String currentUserName = context.getRemoteUser();
    String currentViewer = URLUtils.getCurrentUser();

    return currentUserName.equals(currentViewer);
  }

  /**
   * Gets pending relationships from searched result identities.
   *
   * @return Relationship list.
   * @throws Exception
   */
  private List<Relationship> getPendingRelationships() throws Exception {
    RelationshipManager relm = getRelationshipManager();
    Identity currentIdentity = getCurrentViewerIdentity();
    List<Identity> matchIdentities = getIdentityList();

    if (matchIdentities == null) {
      return relm.getPending(currentIdentity);
    }

    return relm.getPending(currentIdentity, matchIdentities);
  }

  /**
   * Gets relationship manager object.<br>
   *
   * @return an object that is instance of relationship manager.
   */
  private RelationshipManager getRelationshipManager() {
    if (rm == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      rm = (RelationshipManager) container.getComponentInstanceOfType(RelationshipManager.class);
    }
    return rm;
  }

  /**
   * Gets identity manager object.<br>
   *
   * @return identity manager object.
   */
  private IdentityManager getIdentityManager() {
    if (im == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    }
    return im;
  }

  /**
   * Gets contact status between current user and identity that is checked.<br>
   *
   * @param identity
   *        Object is checked status with current user.
   *
   * @return type of relation status that equivalent the relation.
   *
   * @throws Exception
   */
  private Relationship.Type getContactStatus(Identity identity) throws Exception {
    if (identity.getId().equals(getCurrentIdentity().getId()))
      return null;
    return getRelationshipManager().getStatus(identity, getCurrentIdentity());
  }
}
