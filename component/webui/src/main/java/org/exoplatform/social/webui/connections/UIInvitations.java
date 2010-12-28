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
 * Manages connection invitations of all existing users. Manages actions
 * such as accept or deny invitation and search action.<br>
 *   - Get all users that have invited connection.<br>
 *   - Check the status of each user with current user then display the list.<br>
 *   - Listens to event: accept, deny contact and search action.<br>
 *
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Aug 25, 2009
 */
@ComponentConfigs({
  @ComponentConfig(
    template =  "classpath:groovy/social/webui/connections/UIInvitations.gtmpl",
    events = {
      @EventConfig(listeners = UIInvitations.AcceptActionListener.class),
      @EventConfig(listeners = UIInvitations.DenyActionListener.class),
      @EventConfig(listeners = UIInvitations.SearchActionListener.class, phase = Phase.DECODE)
    }
  )
})
public class UIInvitations extends UIContainer {
  /** UIPageIterator ID. */
  private static final String iteratorIDInvitation = "UIPageIteratorInvitation";

  /** Label displays revoked information. */
  private static final String INVITATION_REVOKED_INFO = "UIInvitations.label.RevokedInfo";

  /** Stores UIPageIterator object. */
  private UIPageIterator uiPageIteratorInvitation;

  /** Stores UIProfileUserSearch object. */
  UIProfileUserSearch uiProfileUserSearchRelation = null;

  /** Stores identities. */
  private List<Identity> identityList;
  
  /** The first page. */
  private static final int FIRST_PAGE = 1;
  
  /**
   * Gets identities.<br>
   *
   * @return list of identity.
   */
  public List<Identity> getIdentityList() { return identityList; }

 /** Sets to identity list. */
  public void setIdentityList(List<Identity> identityList) { this.identityList = identityList; }

  /**
   * Gets page iterator.<br>
   *
   * @return an iterator.
   */
  public UIPageIterator getUIPageIteratorInvitation() {
    return uiPageIteratorInvitation;
  }

  /**
   * Initializes components and add as child of form.<br>
   *
   * @throws Exception
   */
  public UIInvitations() throws Exception {
    uiPageIteratorInvitation = createUIComponent(UIPageIterator.class, null, iteratorIDInvitation);
    addChild(uiPageIteratorInvitation);
    uiProfileUserSearchRelation = createUIComponent(UIProfileUserSearch.class, null, "UIProfileUserSearch");
    addChild(uiProfileUserSearchRelation);
  }

  /**
   * Gets all relation that has invited relation.<br>
   *
   * @return all invited relation.
   *
   * @throws Exception
   */
  public List<Relationship> getInvitation() throws Exception {
    List<Relationship> invitationList = getInvitedRelations();
    if (invitationList == null)
      return null;
    List<Relationship> contactLists = getDisplayRelationList(invitationList, uiPageIteratorInvitation);
    return contactLists;
  }

  /**
   * Listens to accept actions then make relation to accepted person.<br>
   *   - Gets information of user who made request.<br>
   *   - Checks the relation to confirm that there still got invited relation.<br>
   *   - Makes and Save the new relation.<br>
   */
  public static class AcceptActionListener extends EventListener<UIInvitations> {
    @Override
    public void execute(Event<UIInvitations> event) throws Exception {
      String identityId = event.getRequestContext().getRequestParameter(OBJECTID);
      Identity requestedIdentity = Utils.getIdentityManager().getIdentity(identityId);

      Relationship relationship = Utils.getRelationshipManager().get(Utils.getOwnerIdentity(), requestedIdentity);
      if (relationship == null ||relationship.getStatus() != Relationship.Type.PENDING) {
        UIApplication uiApplication = event.getRequestContext().getUIApplication();
        uiApplication.addMessage(new ApplicationMessage(INVITATION_REVOKED_INFO, null, ApplicationMessage.INFO));
        return;
      }
      Utils.getRelationshipManager().confirm(relationship);
    }
  }

  /**
   * Listens to deny action then delete the invitation.<br>
   *   - Gets information of user is invited or made request.<br>
   *   - Checks the relation to confirm that there have not got relation yet.<br>
   *   - Removes the current relation and save the new relation.<br>
   *
   */
  public static class DenyActionListener extends EventListener<UIInvitations> {
    @Override
    public void execute(Event<UIInvitations> event) throws Exception {
      String identityId = event.getRequestContext().getRequestParameter(OBJECTID);
      Identity requestedIdentity = Utils.getIdentityManager().getIdentity(identityId);

      Relationship relationship = Utils.getRelationshipManager().get(Utils.getViewerIdentity(), requestedIdentity);
      if (relationship == null ||relationship.getStatus() != Relationship.Type.PENDING) {
        UIApplication uiApplication = event.getRequestContext().getUIApplication();
        uiApplication.addMessage(new ApplicationMessage(INVITATION_REVOKED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      Utils.getRelationshipManager().deny(relationship);
    }
  }

  /**
   * Listens to search action that broadcasted from search form then set to current form.<br>
   *   - Gets search result from search form.<br>
   *   - Sets the search result to the current form that added search form as child.<br>
   */
  public static class SearchActionListener extends EventListener<UIInvitations> {
    @Override
    public void execute(Event<UIInvitations> event) throws Exception {
      UIInvitations uiMyRelation = event.getSource();
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
    uiPageIterator.setPageList(pageListContact);
    if (this.uiProfileUserSearchRelation.isNewSearch()) {
      uiPageIterator.setCurrentPage(FIRST_PAGE);
    } else {
      uiPageIterator.setCurrentPage(curPage);
    }
    this.uiProfileUserSearchRelation.setNewSearch(false);
    return uiPageIterator.getCurrentPageData();
  }

  /**
   * Gets contact relationships from searched result identities.
   *
   * @return Relationship list.
   *
   * @throws Exception
   */
  private List<Relationship> getInvitedRelations() throws Exception {
    return Utils.getRelationshipManager().getIncoming(Utils.getOwnerIdentity(), getIdentityList());
  }
}
