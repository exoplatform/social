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
package org.exoplatform.social.relation;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.Relationship;
import org.exoplatform.social.core.relationship.RelationshipManager;
import org.exoplatform.social.webui.UIProfileUserSearch;
import org.exoplatform.social.webui.URLUtils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormPageIterator;

/**
 * Manages invitation relation of all existing users. Manages actions 
 * such as accept or deny invitation and search action.<br>
 *   - Get all users that have invited relation.<br>
 *   - Check the status of each user with current user then display the list.<br>
 *   - Listens to event: accept, deny contact and search action.<br>
 *   
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Aug 25, 2009  
 */
@ComponentConfigs( {
    @ComponentConfig(
        template =  "app:/groovy/portal/webui/component/UIInvitationRelation.gtmpl",
        events = {
            @EventConfig(listeners = UIInvitationRelation.AcceptActionListener.class),
            @EventConfig(listeners = UIInvitationRelation.DenyActionListener.class),
            @EventConfig(listeners = UIInvitationRelation.SearchActionListener.class, phase = Phase.DECODE)
        }
    )
  }
)
public class UIInvitationRelation extends UIContainer {
  /** UIFormPageIterator ID. */
  private final String iteratorIDInvitation = "UIFormPageIteratorInvitation";
  
  /** Label displays revoked information. */
  private static final String INVITATION_REVOKED_INFO = "UIInvitationRelation.label.RevokedInfo";
  
  /** Stores UIFormPageIterator object. */
  UIFormPageIterator uiFormPageIteratorInvitation;
  
  /** Stores relationship manager object. */
  private RelationshipManager relationshipManager;
  
  /** Stores IdentityManager object. */
  private IdentityManager identityManager = null;
  
  /** Stores UIProfileUserSearch object. */
  UIProfileUserSearch uiProfileUserSearchRelation = null;
  
  /** Stores identities. */
  private List<Identity> identityList;
  
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
  public UIFormPageIterator getUiFormPageIteratorInvitation() {
    return uiFormPageIteratorInvitation;
  }
  
  /**
   * Initializes components and add as child of form.<br>
   * 
   * @throws Exception 
   */
  public UIInvitationRelation() throws Exception {
    uiFormPageIteratorInvitation = createUIComponent(UIFormPageIterator.class, null, iteratorIDInvitation);
    addChild(uiFormPageIteratorInvitation);
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
    List<Relationship> contactLists = getDisplayRelationList(invitationList, uiFormPageIteratorInvitation);
    uiProfileUserSearchRelation.setAllUserContactName(getAllInvitedUserNames()); // set identitite names for suggestion
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
      return im.getIdentityByRemoteId("organization", getCurrentUserName());
  }
  
  
  /**
   * Gets current identity of user who is viewed.<br>
   * 
   * @return identity of current user that is viewed.
   * 
   * @throws Exception
   */
  public Identity getCurrentViewerIdentity() throws Exception {
    IdentityManager im = getIdentityManager();
    Identity identity = im.getIdentityByRemoteId("organization", getCurrentViewerUserName());
    
    // portlet is added into space application
    if (identity == null) 
      identity = im.getIdentityByRemoteId("organization", getCurrentUserName());
    
    return identity;
  }
  
  /**
   * Listens to accept actions then make relation to accepted person.<br>
   *   - Gets information of user who made request.<br>
   *   - Checks the relation to confirm that there still got invited relation.<br>
   *   - Makes and Save the new relation.<br>
   */
  static public class AcceptActionListener extends EventListener<UIInvitationRelation> {
    @Override
    public void execute(Event<UIInvitationRelation> event) throws Exception {
      UIInvitationRelation uiMyRelation = event.getSource();
      String identityId = event.getRequestContext().getRequestParameter(OBJECTID);
      String currUserId = uiMyRelation.getCurrentUserName();
      IdentityManager im = uiMyRelation.getIdentityManager();
      Identity currIdentity = im.getIdentityByRemoteId(OrganizationIdentityProvider.NAME,
                                                       currUserId);

      Identity requestedIdentity = im.getIdentityById(identityId);

      RelationshipManager rm = uiMyRelation.getRelationshipManager();
      
      // TODO Check if invitation is revoked or deleted by another user
      UIApplication uiApplication = event.getRequestContext().getUIApplication();
      Relationship.Type relationStatus = uiMyRelation.getContactStatus(requestedIdentity);
      if (relationStatus == Relationship.Type.ALIEN) {
        uiApplication.addMessage(new ApplicationMessage(INVITATION_REVOKED_INFO, null, ApplicationMessage.INFO));
        return;
      }
      
      Relationship rel = rm.getRelationship(currIdentity, requestedIdentity);
      rel.setStatus(Relationship.Type.CONFIRM);
      rm.save(rel);  
    }
  }
  
  /**
   * Listens to deny action then delete the invitation.<br>
   *   - Gets information of user is invited or made request.<br>
   *   - Checks the relation to confirm that there have not got relation yet.<br>
   *   - Removes the current relation and save the new relation.<br> 
   *
   */
  static public class DenyActionListener extends EventListener<UIInvitationRelation> {
    @Override
    public void execute(Event<UIInvitationRelation> event) throws Exception {
      UIInvitationRelation uiMyRelation = event.getSource();
      String identityId = event.getRequestContext().getRequestParameter(OBJECTID);
      String currUserId = uiMyRelation.getCurrentUserName();

      IdentityManager im = uiMyRelation.getIdentityManager();
      Identity currIdentity = im.getIdentityByRemoteId(OrganizationIdentityProvider.NAME,
                                                       currUserId);

      Identity requestedIdentity = im.getIdentityById(identityId);
      
      // TODO Check if invitation is revoked or deleted by another user
      UIApplication uiApplication = event.getRequestContext().getUIApplication();
      Relationship.Type relationStatus = uiMyRelation.getContactStatus(requestedIdentity);
      if (relationStatus == Relationship.Type.ALIEN) {
        uiApplication.addMessage(new ApplicationMessage(INVITATION_REVOKED_INFO, null, ApplicationMessage.INFO));
        return;
      }
      
      RelationshipManager rm = uiMyRelation.getRelationshipManager();
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
  public static class SearchActionListener extends EventListener<UIInvitationRelation> {
    @Override
    public void execute(Event<UIInvitationRelation> event) throws Exception {
      UIInvitationRelation uiMyRelation = event.getSource();
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
   * @param uiFormPageIterator
   *        Page iterator for paging.
   *        
   * @return list of relation in current page.
   * 
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private List<Relationship> getDisplayRelationList(List<Relationship> listContacts, UIFormPageIterator uiFormPageIterator) throws Exception {
    int curPage = uiFormPageIterator.getCurrentPage();
    LazyPageList<Relationship> pageListContact = new LazyPageList<Relationship>(new RelationshipListAccess(listContacts), 5);
    uiFormPageIterator.setPageList(pageListContact) ;  
    int availablePageCount = uiFormPageIterator.getAvailablePage();
    if(availablePageCount >= curPage){
      uiFormPageIterator.setCurrentPage(curPage);
    }else if(availablePageCount < curPage){
      uiFormPageIterator.setCurrentPage(curPage-1);
    }
    List<Relationship> contactLists;
    contactLists = uiFormPageIterator.getCurrentPageData();
    return contactLists;
  }
  
  /**
   * Gets all invited  identities for searching suggestion.
   * 
   * @return Relationship list.
   * 
   * @throws Exception
   */
  private List<String> getAllInvitedUserNames() throws Exception {
    RelationshipManager relm = getRelationshipManager();
    Identity currentIdentity = getCurrentViewerIdentity();
    List<Identity> allInvitedIdentities = new ArrayList<Identity>();
    List<Relationship> allInviteds = relm.getPending(currentIdentity, false);
    Identity id = null;
    Identity currIdentity = getCurrentIdentity();
    for(Relationship rel : allInviteds) {
      id = (currIdentity.getId() == (rel.getIdentity1()).getId()) ? rel.getIdentity2() : rel.getIdentity1();
      allInvitedIdentities.add(id);
    }
    
    List<String> allUserContactName = new ArrayList<String>();
    
    for (Identity identity : allInvitedIdentities) {
      allUserContactName.add((identity.getProfile()).getFullName());
    }
    
    return allUserContactName;
  }
  
  /**
   * Gets contact relationships from searched result identities. 
   * 
   * @return Relationship list.
   * 
   * @throws Exception
   */
  private List<Relationship> getInvitedRelations() throws Exception {
    RelationshipManager relm = getRelationshipManager();
    Identity currentIdentity = getCurrentViewerIdentity();
    
    List<Identity> matchIdentities = getIdentityList();
    
    if (matchIdentities == null) {
      return relm.getPending(currentIdentity, false);
    }
    
    return relm.getPending(currentIdentity, matchIdentities, false);
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
  private String getCurrentViewerUserName() {
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
    if (identity.getId().equals(getCurrentIdentity().getId()))
      return Relationship.Type.SELF;
    RelationshipManager rm = getRelationshipManager();
    Relationship rl = rm.getRelationship(identity, getCurrentIdentity());
    return rm.getRelationshipStatus(rl, getCurrentIdentity());
  }
}
