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
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormPageIterator;

/**
 * Created by The eXo Platform SAS
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Aug 25, 2009  
 */
@ComponentConfig(
    template =  "app:/groovy/portal/webui/component/UIPendingRelation.gtmpl",
    events = { 
        @EventConfig(listeners = UIPendingRelation.DenyContactActionListener.class),
        @EventConfig(listeners = UIPendingRelation.SearchActionListener.class, phase = Phase.DECODE)
      }
)
public class UIPendingRelation extends UIContainer {
  /** UIFormPageIterator */
  UIFormPageIterator uiFormPageIterator_;
  /** UIFormPageIterator ID. */
  private final String iteratorID_ = "UIFormPageIteratorPendingRelation";
  private static final String INVITATION_REVOKED_INFO = "UIPendingRelation.label.RevokedInfo";
  /** Current identity. */
  Identity            currIdentity = null;
  /** RelationshipManager */
  RelationshipManager rm           = null;
  /** IdentityManager */
  IdentityManager     im           = null;
  UIProfileUserSearch uiProfileUserSearchPending = null;
  private List<Identity> identityList;
  
  public List<Identity> getIdentityList() {
    return identityList;
  }

  public void setIdentityList(List<Identity> identityList) {
    this.identityList = identityList;
  }

  /**
   * Get UIFormPageIterator.
   * @return
   */
  public UIFormPageIterator getUiFormPageIterator() {
    return uiFormPageIterator_;
  }

  /**
   * Constructor.
   * @throws Exception 
   */
  public UIPendingRelation() throws Exception {
    uiFormPageIterator_ = createUIComponent(UIFormPageIterator.class, null, iteratorID_);
    addChild(uiFormPageIterator_);
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
    int currentPage = uiFormPageIterator_.getCurrentPage();
    LazyPageList<Relationship> pageList = new LazyPageList<Relationship>(new RelationshipListAccess(listRelationShip), 5);
    
    uiProfileUserSearchPending.setAllUserContactName(getAllPendingUserNames()); // set identitite names for suggestion
    uiFormPageIterator_.setPageList(pageList) ;  
    int pageCount = uiFormPageIterator_.getAvailablePage();
    if(pageCount >= currentPage){
      uiFormPageIterator_.setCurrentPage(currentPage);
    }else if(pageCount < currentPage){
      uiFormPageIterator_.setCurrentPage(currentPage-1);
    }
    List<Relationship> lists;
    lists = uiFormPageIterator_.getCurrentPageData();
    
    return lists;
  }
    
  public String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance();
    return pcontainer.getPortalContainerInfo().getContainerName();  
  }
  
  public String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;    
    return rService.getCurrentRepository().getConfiguration().getName() ;
  }
  
  /**
   * Get current identity.
   * 
   * @return
   * @throws Exception
   */
  public Identity getCurrentIdentity() throws Exception {
      IdentityManager im = getIdentityManager();
      return im.getIdentityByRemoteId("organization", getCurrentUserName());
  }
  
  public Identity getCurrentViewerIdentity() throws Exception {
    IdentityManager im = getIdentityManager();
    Identity identity = im.getIdentityByRemoteId("organization", getCurrentViewerUserName());
    if (identity == null) identity = im.getIdentityByRemoteId("organization", getCurrentUserName());
    return identity;
  }
  
  /**
   * Get current user name.
   * 
   * @return
   */
  public String getCurrentUserName() {
    RequestContext context = RequestContext.getCurrentInstance();
    return context.getRemoteUser();
  }

  public String getCurrentViewerUserName() {
    String username = URLUtils.getCurrentUser();
    if(username != null)
      return username;
    
    RequestContext context = RequestContext.getCurrentInstance();
    return context.getRemoteUser();
  }
  /**
   *  Revoke the request with relation that has status is PENDING. 
   */
  public static class DenyContactActionListener extends EventListener<UIPendingRelation> {
    public void execute(Event<UIPendingRelation> event) throws Exception {
      UIPendingRelation portlet = event.getSource();
  
      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      String currUserId = portlet.getCurrentUserName();
  
      IdentityManager im = portlet.getIdentityManager();
      Identity currIdentity = im.getIdentityByRemoteId(OrganizationIdentityProvider.NAME,
                                                       currUserId);
  
      Identity requestedIdentity = im.getIdentityById(userId);
  
      // TODO Check if relation is deleted by another user
      UIApplication uiApplication = event.getRequestContext().getUIApplication();
      Relationship.Type relationStatus = portlet.getContactStatus(requestedIdentity);
      if (relationStatus == Relationship.Type.ALIEN) {
        uiApplication.addMessage(new ApplicationMessage(INVITATION_REVOKED_INFO, null, ApplicationMessage.INFO));
        return;
      }
      
      RelationshipManager rm = portlet.getRelationshipManager();
      Relationship rel = rm.getRelationship(currIdentity, requestedIdentity);
      if (rel != null)
        rm.remove(rel);
    }
  }
  
  public static class SearchActionListener extends EventListener<UIPendingRelation> {
    @Override
    public void execute(Event<UIPendingRelation> event) throws Exception {
      UIPendingRelation uiPending = event.getSource();
      UIProfileUserSearch uiProfileUserSearch = uiPending.getChild(UIProfileUserSearch.class);
      List<Identity> identityList = uiProfileUserSearch.getIdentityList();
      uiPending.setIdentityList(identityList);
    }
  }
  
  public boolean isEditable () {
    RequestContext context = RequestContext.getCurrentInstance();
    String currentUserName = context.getRemoteUser();
    String currentViewer = URLUtils.getCurrentUser();
    
    return currentUserName.equals(currentViewer);
  }
  
  /**
   * Get all pending identities for searching suggestion.
   * 
   * @return Relationship list.
   * @throws Exception
   */
  private List<String> getAllPendingUserNames() throws Exception {
    RelationshipManager relm = getRelationshipManager();
    Identity currentIdentity = getCurrentViewerIdentity();
    List<Identity> allPendingIdentities = new ArrayList<Identity>();
    List<Relationship> allPendings = relm.getPending(currentIdentity, true);
    Identity id = null;
    Identity currIdentity = getCurrentIdentity();
    for(Relationship rel : allPendings) {
      id = (currIdentity.getId() == (rel.getIdentity1()).getId()) ? rel.getIdentity2() : rel.getIdentity1();
      allPendingIdentities.add(id);
    }
    
    List<String> allUserContactName = new ArrayList<String>();
    
    for (Identity identity : allPendingIdentities) {
      allUserContactName.add((identity.getProfile()).getFullName());
    }
    
    return allUserContactName;
  }
  
  /**
   * Get pending relationships from searched result identities. 
   * 
   * @return Relationship list.
   * @throws Exception
   */
  private List<Relationship> getPendingRelationships() throws Exception {
    RelationshipManager relm = getRelationshipManager();
    Identity currentIdentity = getCurrentViewerIdentity();
    List<Identity> matchIdentities = getIdentityList();
    
    if (matchIdentities == null) {
      return relm.getPending(currentIdentity, true);
    }
    
    return relm.getPending(currentIdentity, matchIdentities, true);
  }
  
  /**
   * Get Relationship manager.
   * 
   * @return
   */
  private RelationshipManager getRelationshipManager() {
    if (rm == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      rm = (RelationshipManager) container.getComponentInstanceOfType(RelationshipManager.class);
    }
    return rm;
  }
  
  /**
   * Get identity manager.
   * 
   * @return
   */
  private IdentityManager getIdentityManager() {
    if (im == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    }
    return im;
  }
  
  private Relationship.Type getContactStatus(Identity identity) throws Exception {
    if (identity.getId().equals(getCurrentIdentity().getId()))
      return Relationship.Type.SELF;
    RelationshipManager rm = getRelationshipManager();
    Relationship rl = rm.getRelationship(identity, getCurrentIdentity());
    return rm.getRelationshipStatus(rl, getCurrentIdentity());
  }
}
