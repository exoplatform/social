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
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.webui.RelationshipListAccess;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.profile.UIProfileUserSearch;
import org.exoplatform.web.application.ApplicationMessage;
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
  private static final String iteratorIDContact = "UIPageIteratorContact";

  /** Label for display realtion is deleted information */
  private static final String RELATION_DELETED_INFO = "UIMyConnections.label.DeletedInfo";

  /** Stores UIPageIterator instance. */
  UIPageIterator uiPageIteratorContact;

  /** Stores UIProfileUserSearch instance. */
  UIProfileUserSearch uiProfileUserSearchRelation = null;

  /** Stores identities. */
  private List<Identity> identityList;

  /** The first page. */
  private static final int FIRST_PAGE = 1;

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
    if (listContacts == null)
      return null;
    List<Relationship> contactLists = getDisplayRelationList(listContacts, uiPageIteratorContact);
    return contactLists;
  }

  /**
   * Listens to remove action then delete the relation.<br>
   *   - Gets information of user is removed.<br>
   *   - Checks the relation to confirm that still got relation.<br>
   *   - Removes the current relation.<br>
   *
   */
  public static class RemoveActionListener extends EventListener<UIMyConnections> {
    @Override
    public void execute(Event<UIMyConnections> event) throws Exception {
      String identityId = event.getRequestContext().getRequestParameter(OBJECTID);
      Identity requestedIdentity = Utils.getIdentityManager().getIdentity(identityId);
      Relationship relationship = Utils.getRelationshipManager().get(Utils.getOwnerIdentity(), requestedIdentity);
      if (relationship == null || relationship.getStatus() != Relationship.Type.CONFIRMED) {
        UIApplication uiApplication = event.getRequestContext().getUIApplication();
        uiApplication.addMessage(new ApplicationMessage(RELATION_DELETED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      Utils.getRelationshipManager().remove(relationship);
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
    return Utils.isOwner();
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
    if (this.uiProfileUserSearchRelation.isNewSearch()) {
      uiPageIterator.setCurrentPage(FIRST_PAGE);
    } else {
      uiPageIterator.setCurrentPage(curPage);
    }
    this.uiProfileUserSearchRelation.setNewSearch(false);
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
    List<Identity> matchIdentities = getIdentityList();
    return Utils.getRelationshipManager().getConfirmed(Utils.getOwnerIdentity(), matchIdentities);
  }
}
