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
package org.exoplatform.social.webui.profile;

import java.util.List;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.webui.IdentityListAccess;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
/**
 * Displays information about all existing users. Manages actions
 * such as request make connection, invoke request, accept or deny invitation
 * and delete connection.<br>
 *   - Get all existing users.<br>
 *   - Check the status of each user with current user then display the list.<br>
 *   - Process actions from user: add contact, accept contact, deny contact and search.<br>
 *
 */
@ComponentConfig(
  template = "classpath:groovy/social/webui/profile/UIDisplayProfileList.gtmpl",
  events = {
    @EventConfig(listeners = UIDisplayProfileList.AddContactActionListener.class),
    @EventConfig(listeners = UIDisplayProfileList.AcceptContactActionListener.class),
    @EventConfig(listeners = UIDisplayProfileList.DenyContactActionListener.class),
    @EventConfig(listeners = UIDisplayProfileList.SearchActionListener.class, phase = Phase.DECODE)
  }
)
public class UIDisplayProfileList extends UIContainer {
  /** Label for display invoke action */
  private static final String INVITATION_REVOKED_INFO = "UIDisplayProfileList.label.RevokedInfo";

  /** Label for display established invitation */
  private static final String INVITATION_ESTABLISHED_INFO = "UIDisplayProfileList.label.InvitationEstablishedInfo";

  /** Number element per page. */
  private static final Integer PEOPLE_PER_PAGE = 10;

  /** Id of iterator. */
  private static final String ITERATOR_ID = "UIIteratorPeople";

  /** The search object variable. */
  UIProfileUserSearch uiProfileUserSearchPeople = null;

  /** Iterator object contains elements of page */
  private UIPageIterator iterator;

  /** Contains identities. */
  private List<Identity> identityList;
  
  /** The first page of profile pages. */
  private static final int FIRST_PAGE = 1;

  /**
   * Gets identities.
   *
   * @return one list of identity.
   * @throws Exception
   */
  public List<Identity> getIdentityList() throws Exception {
    if (identityList == null) {
      identityList = Utils.getIdentityManager().getIdentities(OrganizationIdentityProvider.NAME);
      if (identityList.contains(Utils.getViewerIdentity())) {
        identityList.remove(Utils.getViewerIdentity());
      }
    }
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
    return iterator;
  }

  /**
   * Initializes all components for the first time.
   *
   * @throws Exception
   */
  public UIDisplayProfileList() throws Exception {
    iterator = addChild(UIPageIterator.class, null, ITERATOR_ID);
    uiProfileUserSearchPeople = createUIComponent(UIProfileUserSearch.class, null, "UIProfileUserSearch");
    addChild(uiProfileUserSearchPeople);
  }

  /**
   * Gets all identities in the current list for display.
   * 
   * @return all identities in the current page of iterator.
   *
   * @throws Exception
   */
  public List<Identity> getList() throws Exception {
    int currentPage = iterator.getCurrentPage();
    LazyPageList<Identity> pageList = new LazyPageList<Identity>(new IdentityListAccess(getIdentityList()), PEOPLE_PER_PAGE);
    iterator.setPageList(pageList);
    if (this.uiProfileUserSearchPeople.isNewSearch()) {
      iterator.setCurrentPage(FIRST_PAGE);
    } else {
      iterator.setCurrentPage(currentPage);
    }
    this.uiProfileUserSearchPeople.setNewSearch(false);
    return iterator.getCurrentPageData();
  }

  /**
   * Listens to add action then make request to invite person to make connection.<br>
   *   - Gets information of user is invited.<br>
   *   - Checks the relationship to confirm that there have not got connection yet.<br>
   *   - Saves the new connection.<br>
   *
   */
  public static class AddContactActionListener extends EventListener<UIDisplayProfileList> {
    public void execute(Event<UIDisplayProfileList> event) throws Exception {
      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      Identity requestedIdentity = Utils.getIdentityManager().getIdentity(userId);

      Relationship relationship = Utils.getRelationshipManager().get(Utils.getViewerIdentity(), requestedIdentity);
      if (relationship != null) {
        UIApplication uiApplication = event.getRequestContext().getUIApplication();
        uiApplication.addMessage(new ApplicationMessage(INVITATION_ESTABLISHED_INFO, null, ApplicationMessage.INFO));
        return;
      }

      Utils.getRelationshipManager().invite(Utils.getViewerIdentity(), requestedIdentity);
    }
  }

  /**
   * Listens to accept actions then make connection to accepted person.<br>
   *   - Gets information of user who made request.<br>
   *   - Checks the relationship to confirm that there still got invited connection.<br>
   *   - Makes and Save the new relationship.<br>
   */
  public static class AcceptContactActionListener extends EventListener<UIDisplayProfileList> {
    public void execute(Event<UIDisplayProfileList> event) throws Exception {
      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      Identity requestedIdentity = Utils.getIdentityManager().getIdentity(userId);

      Relationship relationship = Utils.getRelationshipManager().get(Utils.getViewerIdentity(), requestedIdentity);
      if (relationship == null || relationship.getStatus() != Relationship.Type.PENDING) {
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
  public static class DenyContactActionListener extends EventListener<UIDisplayProfileList> {
    public void execute(Event<UIDisplayProfileList> event) throws Exception {
      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      Identity requestedIdentity = Utils.getIdentityManager().getIdentity(userId);

      Relationship relationship = Utils.getRelationshipManager().get(Utils.getViewerIdentity(), requestedIdentity);
      if (relationship == null || relationship.getStatus() != Relationship.Type.PENDING) {
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
  public static class SearchActionListener extends EventListener<UIDisplayProfileList> {
    @Override
    public void execute(Event<UIDisplayProfileList> event) throws Exception {
      UIDisplayProfileList uiMyRelation = event.getSource();
      UIProfileUserSearch uiProfileUserSearch = uiMyRelation.getChild(UIProfileUserSearch.class);
      List<Identity> identityList = uiProfileUserSearch.getIdentityList();
      uiMyRelation.setIdentityList(identityList);
    }
  }

  /**
   * 
   * @param identity
   * @return
   * @throws Exception
   */
  public Relationship getRelationship(Identity identity) throws Exception {
    if (identity.equals(Utils.getViewerIdentity())) {
      return null;
    }
    return Utils.getRelationshipManager().get(identity, Utils.getViewerIdentity());
  }

  /**
   * Gets path of current portal page base on url.<br>
   *
   * @return path of current portal page.
   */
  public String getPath() {
    String nodePath = Util.getPortalRequestContext().getNodePath();
    String uriPath = Util.getPortalRequestContext().getRequestURI();
    return uriPath.replaceAll(nodePath, "");
  }
}
