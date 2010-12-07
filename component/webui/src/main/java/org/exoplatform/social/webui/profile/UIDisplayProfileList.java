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

import java.util.Iterator;
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
import org.exoplatform.social.webui.IdentityListAccess;
import org.exoplatform.social.webui.URLUtils;
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
  private final Integer PEOPLE_PER_PAGE = 10;

  /** Id of iterator. */
  private final String ITERATOR_ID = "UIIteratorPeople";

  /** Stores IdentityManager instance. */
  private IdentityManager     identityManager_ = null;

  /** The search object variable. */
  UIProfileUserSearch uiProfileUserSearchPeople = null;

  /** Iterator object contains elements of page */
  private UIPageIterator iterator;

  /** Contains identities. */
  private List<Identity> identityList;

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
    List<Identity> peopleList = getProfiles();
    LazyPageList<Identity> pageList = new LazyPageList<Identity>(new IdentityListAccess(peopleList), PEOPLE_PER_PAGE);
    iterator.setPageList(pageList);
    int pageCount = iterator.getAvailablePage();
    if (pageCount >= currentPage) {
      iterator.setCurrentPage(currentPage);
    } else if (pageCount < currentPage) {
      iterator.setCurrentPage(currentPage - 1);
    }
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
      UIDisplayProfileList portlet = event.getSource();
      IdentityManager im = portlet.getIdentityManager();
      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      String currUserId = portlet.getCurrentUserName();
      Identity currIdentity = im.getOrCreateIdentity(OrganizationIdentityProvider.NAME, currUserId);
      Identity requestedIdentity = im.getIdentity(userId);
      RelationshipManager rm = portlet.getRelationshipManager();
      // Check if invitation is established by another user
      UIApplication uiApplication = event.getRequestContext().getUIApplication();
      Relationship.Type relationStatus = portlet.getContactStatus(requestedIdentity);
      if (relationStatus != null) {
        uiApplication.addMessage(new ApplicationMessage(INVITATION_ESTABLISHED_INFO, null, ApplicationMessage.INFO));
        return;
      }
      rm.invite(currIdentity, requestedIdentity);
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
      UIDisplayProfileList portlet = event.getSource();
      UIApplication uiApplication = event.getRequestContext().getUIApplication();

      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      String currUserId = portlet.getCurrentUserName();

      IdentityManager im = portlet.getIdentityManager();
      Identity currIdentity = im.getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                       currUserId);
      Identity requestedIdentity = im.getIdentity(userId);
      RelationshipManager rm = portlet.getRelationshipManager();
      // Check if invitation is revoked or deleted by another user
      Relationship rel = rm.get(currIdentity, requestedIdentity);
      Relationship.Type relationStatus = portlet.getContactStatus(requestedIdentity);
      if (relationStatus == Relationship.Type.IGNORED) {
        uiApplication.addMessage(new ApplicationMessage(INVITATION_REVOKED_INFO, null, ApplicationMessage.INFO));
        return;
      }
      rm.confirm(rel);
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
      UIDisplayProfileList portlet = event.getSource();
      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      String currUserId = portlet.getCurrentUserName();
      IdentityManager im = portlet.getIdentityManager();
      Identity currIdentity = im.getOrCreateIdentity(OrganizationIdentityProvider.NAME, currUserId);
      Identity requestedIdentity = im.getIdentity(userId);
      RelationshipManager rm = portlet.getRelationshipManager();
      // Check if invitation is revoked or deleted by another user
      UIApplication uiApplication = event.getRequestContext().getUIApplication();
      Relationship.Type relationStatus = portlet.getContactStatus(requestedIdentity);
      if (relationStatus == Relationship.Type.IGNORED) {
        uiApplication.addMessage(new ApplicationMessage(INVITATION_REVOKED_INFO, null, ApplicationMessage.INFO));
        return;
      }
      Relationship rel = rm.get(currIdentity, requestedIdentity);
      if (rel != null) {
        rm.deny(rel);
      }
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
   * Gets the identity of current user is viewed by another.<br>
   *
   * @return identity of current user who is viewed.
   *
   * @throws Exception
   */
  public Identity getCurrentViewerIdentity() throws Exception {
    IdentityManager im = getIdentityManager();
    return im.getOrCreateIdentity(OrganizationIdentityProvider.NAME, getCurrentViewerUserName());
  }

  /**
   * Gets contact status between current user and identity that is checked.<br>
   *
   * @param identity
   *        Object is checked status with current user.
   *
   * @return type of relationship status that equivalent the relationship.
   *
   * @throws Exception
   */
  private Relationship.Type getContactStatus(Identity identity) throws Exception {
    if (identity.getId().equals(getCurrentIdentity().getId()))
      return null;
    return getRelationshipManager().getStatus(identity, getCurrentIdentity());
  }

  /**
   * 
   * @param identity
   * @return
   * @throws Exception
   */
  public Relationship getRelationship(Identity identity) throws Exception {
    if (identity.getId().equals(getCurrentIdentity().getId()))
      return null;
    return getRelationshipManager().get(identity, getCurrentIdentity());
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
   * Gets name of current user.
   *
   * @return name of current login user.
   */
  public String getCurrentUserName() {
    RequestContext context = RequestContext.getCurrentInstance();
    return context.getRemoteUser();
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
      return im.getOrCreateIdentity(OrganizationIdentityProvider.NAME, getCurrentUserName());
  }

  /**
   * Gets the rest context.
   *
   * @return the rest context
   */
	public String getRestContext() {
	  return PortalContainer.getInstance().getRestContextName();
	}

  /**
   * Gets identity manager object.<br>
   *
   * @return identity manager object.
   */
  private IdentityManager getIdentityManager() {
    if (identityManager_ == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      identityManager_ = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    }
    return identityManager_;
  }

  /**
   * Gets all profiles exclude profile of current identity.<br>
   *
   * @return all profiles exclude current identity's profile.
   *
   * @throws Exception
   */
  private List<Identity> getProfiles() throws Exception {
    List<Identity> matchIdentities = getIdentityList();

    if (matchIdentities == null) {
      return loadAllProfiles();
    }

    Iterator<Identity> itr = matchIdentities.iterator();
    while(itr.hasNext()) {
      Identity id = itr.next();
      if(id.equals(getCurrentIdentity())) {
        itr.remove();
      }
    }

    return matchIdentities;
  }

  /**
   * Gets currents name of user that is viewed by another.<br>
   *
   * @return name of user who is viewed.
   */
  private String getCurrentViewerUserName() {
    String username = URLUtils.getCurrentUser();
    if(username != null)
      return username;

    PortalRequestContext portalRequest = Util.getPortalRequestContext();

    return portalRequest.getRemoteUser();
  }

  /**
   * Loads all existing user profiles.<br>
   *
   * @return all existing profiles.
   *
   * @throws Exception
   */
  private List<Identity> loadAllProfiles() throws Exception {
    IdentityManager im = getIdentityManager();
    List<Identity> ids = im.getIdentities(OrganizationIdentityProvider.NAME);
    Iterator<Identity> itr = ids.iterator();
    while(itr.hasNext()) {
      Identity id = itr.next();
      if(id.equals(getCurrentIdentity())){
        itr.remove();
      }
    }

    return ids;
  }

  /**
   * Gets relationship manager object.<br>
   *
   * @return an object that is instance of relationship manager.
   */
  private RelationshipManager getRelationshipManager() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    return (RelationshipManager) container.getComponentInstanceOfType(RelationshipManager.class);
  }

}
