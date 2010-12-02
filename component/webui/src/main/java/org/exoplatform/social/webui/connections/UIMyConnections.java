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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
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
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Manages relation with the current user of all existing users. Manages actions
 * such as remove relation and search action.<br>
 *   - Get all users that really have relation.<br>
 *   - Check the status of each user with current user then display the list.<br>
 *   - Listens to event: remove relation and search action.<br>
 *
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Aug 25, 2009
 */
@ComponentConfigs({
  @ComponentConfig(
    template =  "classpath:groovy/social/webui/connections/UIMyConnections.gtmpl",
    events = {
      @EventConfig(listeners = UIMyConnections.RemoveActionListener.class),
      @EventConfig(listeners = UIMyConnections.SearchActionListener.class, phase = Phase.DECODE)
    }
  )
})
public class UIMyConnections extends UIContainer {
  /** UIPageIterator ID. */
  private final String iteratorIDContact = "UIPageIteratorContact";

  /** Label for display realtion is deleted information */
  private static final String RELATION_DELETED_INFO = "UIMyRelations.label.DeletedInfo";

  /** Stores UIPageIterator instance. */
  UIPageIterator uiPageIteratorContact;

  /** Stores RelationshipManager instance. */
  private RelationshipManager relationshipManager;

  /** Stores IdentityManager instance. */
  private IdentityManager identityManager = null;

  /** Stores UIProfileUserSearch instance. */
  UIProfileUserSearch uiProfileUserSearchRelation = null;

  /** Stores identities. */
  private List<Identity> identityList;

  /**
   * Gets identities.
   *
   * @return one list of identity.
   */
  public List<Identity> getIdentityList() { return identityList; }

  /**
   * Sets list identity.
   *
   * @param identityList
   *        Identities for setting to list.
   */
  public void setIdentityList(List<Identity> identityList) { this.identityList = identityList; }

  /**
   * Gets iterator for display.
   *
   * @return an iterator contains information for display.
   */
  public UIPageIterator getUIPageIteratorContact() {
    return uiPageIteratorContact;
  }

  /**
   * Initializes components and add as child of form.<br>
   *
   * @throws Exception
   */
  public UIMyConnections() throws Exception {
    uiPageIteratorContact = createUIComponent(UIPageIterator.class, null, iteratorIDContact);
    addChild(uiPageIteratorContact);
    uiProfileUserSearchRelation = createUIComponent(UIProfileUserSearch.class, null, "UIProfileUserSearch");
    addChild(uiProfileUserSearchRelation);
  }

  /**
   * Gets all contact that really has relation.<br>
   *
   * @return all contact that has relation.
   *
   * @throws Exception
   */
  public List<Relationship> getMyRelation() throws Exception {
    List<Relationship> listContacts = getMyContacts();
    List<Relationship> contactLists = getDisplayRelationList(listContacts, uiPageIteratorContact);
    return contactLists;
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
      return im.getOrCreateIdentity(OrganizationIdentityProvider.NAME, getCurrentVieweredUserName());
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
    Identity identity = im.getOrCreateIdentity(OrganizationIdentityProvider.NAME, getCurrentVieweredUserName());
    if (identity == null) identity = im.getOrCreateIdentity(OrganizationIdentityProvider.NAME, getCurrentUserName());
    return identity;
  }

  /**
   * Listens to remove action then delete the relation.<br>
   *   - Gets information of user is removed.<br>
   *   - Checks the relation to confirm that still got relation.<br>
   *   - Removes the current relation.<br>
   *
   */
  static public class RemoveActionListener extends EventListener<UIMyConnections> {
    @Override
    public void execute(Event<UIMyConnections> event) throws Exception {
      UIMyConnections uiMyRelation = event.getSource();
      String identityId = event.getRequestContext().getRequestParameter(OBJECTID);
      String currUserId = uiMyRelation.getCurrentUserName();

      IdentityManager im = uiMyRelation.getIdentityManager();
      Identity currIdentity = im.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                       currUserId);

      Identity requestedIdentity = im.getIdentity(identityId);

      RelationshipManager rm = uiMyRelation.getRelationshipManager();

      UIApplication uiApplication = event.getRequestContext().getUIApplication();
      Relationship.Type relationStatus = uiMyRelation.getContactStatus(requestedIdentity);
      if (relationStatus != Relationship.Type.CONFIRM) {
        uiApplication.addMessage(new ApplicationMessage(RELATION_DELETED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      Relationship rel = rm.getRelationship(currIdentity, requestedIdentity);
      if (rel != null)
        rm.remove(rel);
    }
  }

  /**
   * Listens to search action that broadcasted from search form then set to current form.<br>
   *   - Gets search result from search form.<br>
   *   - Sets the search result to the current form that added search form as child.<br>
   */
  public static class SearchActionListener extends EventListener<UIMyConnections> {
    @Override
    public void execute(Event<UIMyConnections> event) throws Exception {
      UIMyConnections uiMyRelation = event.getSource();
      UIProfileUserSearch uiProfileUserSearch = uiMyRelation.getChild(UIProfileUserSearch.class);
      List<Identity> identityList = uiProfileUserSearch.getIdentityList();
      uiMyRelation.setIdentityList(identityList);
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
   * Returns list of relation of current page in iterator.<br>
   *
   * @param listContacts
   *        All invited contact.
   *
   * @param uiPageIterator
   *        Page iterator for paging.
   *
   * @return list of relation in current page.
   *
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private List<Relationship> getDisplayRelationList(List<Relationship> listContacts, UIPageIterator uiPageIterator) throws Exception {
    int curPage = uiPageIterator.getCurrentPage();
    LazyPageList<Relationship> pageListContact = new LazyPageList<Relationship>(new RelationshipListAccess(listContacts), 5);
    uiPageIterator.setPageList(pageListContact) ;
    int availablePageCount = uiPageIterator.getAvailablePage();
    if(availablePageCount >= curPage){
      uiPageIterator.setCurrentPage(curPage);
    }else if(availablePageCount < curPage){
      uiPageIterator.setCurrentPage(curPage-1);
    }

    return uiPageIterator.getCurrentPageData();
  }

  /**
   * Gets contacts from searched result list.
   *
   * @return Relationship list.
   *
   * @throws Exception
   */
  private List<Relationship> getMyContacts() throws Exception {
    RelationshipManager relm = getRelationshipManager();
    Identity currentIdentity = getCurrentViewerIdentity();

    List<Identity> matchIdentities = getIdentityList();

    if (matchIdentities == null) {
      return relm.getContacts(currentIdentity);
    }

    return relm.getContacts(currentIdentity, matchIdentities);
  }

  /**
   * Gets identity manager object.<br>
   *
   * @return identity manager object.
   */
  private IdentityManager getIdentityManager() {
    if(identityManager == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      identityManager =  (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    }
    return identityManager;
  }

  /**
   * Gets currents name of user that is viewed by another.<br>
   *
   * @return name of user who is viewed.
   */
  private String getCurrentVieweredUserName() {
    String username = URLUtils.getCurrentUser();
    if(username != null)
      return username;

    PortalRequestContext portalRequest = Util.getPortalRequestContext();
    return portalRequest.getRemoteUser();
  }

  /**
   * Gets name of current login user.
   *
   * @return name of current login user.
   */
  private String getCurrentUserName() {
    PortalRequestContext portalRequest = Util.getPortalRequestContext();
    return portalRequest.getRemoteUser();
  }

  /**
   * Gets relationship manager object.<br>
   *
   * @return an object that is instance of relationship manager.
   */
  private RelationshipManager getRelationshipManager() {
    if(relationshipManager == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      relationshipManager = (RelationshipManager) container.getComponentInstanceOfType(RelationshipManager.class);
    }
    return relationshipManager;
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
    if (identity.getId().equals(getCurrentIdentity().getId())) {
      return Relationship.Type.SELF;
    }
    RelationshipManager rm = getRelationshipManager();
    Relationship rl = rm.getRelationship(identity, getCurrentIdentity());
    return rm.getRelationshipStatus(rl, getCurrentIdentity());
  }
}
