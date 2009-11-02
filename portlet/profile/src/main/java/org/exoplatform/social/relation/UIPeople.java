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
import org.exoplatform.social.portlet.URLUtils;
import org.exoplatform.social.portlet.profile.UIProfileUserSearch;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormPageIterator;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Aug 25, 2009  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/portal/webui/component/UIPeople.gtmpl",
    events = { 
      @EventConfig(listeners = UIPeople.AddContactActionListener.class),
      @EventConfig(listeners = UIPeople.RemoveActionListener.class),
      @EventConfig(listeners = UIPeople.AcceptActionListener.class),
      @EventConfig(listeners = UIPeople.DenyActionListener.class),
      @EventConfig(listeners = UIPeople.SearchActionListener.class, phase = Phase.DECODE)
    }
)
public class UIPeople extends UIForm {
  /** UIFormPageIterator */
  UIFormPageIterator uiFormPageIteratorPublic;
  /** UIFormPageIterator ID. */
  private final String iteratorIDPublic = "UIFormPageIteratorPublicRelation";
  /** Current identity. */
  Identity            currIdentity = null;
  /** RelationshipManager */
  RelationshipManager rm           = null;
  /** IdentityManager */
  IdentityManager     im           = null;
  
  UIProfileUserSearch uiProfileUserSearch = null;
  private List<Identity> identityList;
  
  /**
   * Constructor.
   * @throws Exception 
   */
  public UIPeople() throws Exception {
    addUIFormInput(new UIFormStringInput("search", null));
    uiProfileUserSearch = createUIComponent(UIProfileUserSearch.class, null, "UIPublicRelationSearch");
    addChild(uiProfileUserSearch);
    uiFormPageIteratorPublic = createUIComponent(UIFormPageIterator.class, null, iteratorIDPublic);
    addChild(uiFormPageIteratorPublic);
  }
  
  /**
   * Get UIFormPageIterator.
   * @return
   */
  public UIFormPageIterator getUiFormPageIterator() {
    return uiFormPageIteratorPublic;
  }
  
  /**
   * Get list of identity that have not got any relation with current identity (status is Alien).
   *  
   * @return
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<Identity> getPublicRelationList() throws Exception {
    List<Identity> listIdentity = getPublicIdentities();
    int currentPage = uiFormPageIteratorPublic.getCurrentPage();
    LazyPageList<Identity> pageList = new LazyPageList<Identity>(new IdentityListAccess(listIdentity), 5);
    uiFormPageIteratorPublic.setPageList(pageList) ;  
    int pageCount = uiFormPageIteratorPublic.getAvailablePage();
    if(pageCount >= currentPage){
      uiFormPageIteratorPublic.setCurrentPage(currentPage);
    }else if(pageCount < currentPage){
      uiFormPageIteratorPublic.setCurrentPage(currentPage-1);
    }
    List<Identity> lists;
    lists = uiFormPageIteratorPublic.getCurrentPageData();
    return lists;
  }
  
  /**
   * Get list of identity have relation with current identity.
   *  
   * @return
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<Identity> getMyRelationList() throws Exception {
    List<Identity> listIdentity = getPublicIdentities();
    int currentPage = uiFormPageIteratorPublic.getCurrentPage();
    LazyPageList<Identity> pageList = new LazyPageList<Identity>(new IdentityListAccess(listIdentity), 5);
    uiFormPageIteratorPublic.setPageList(pageList) ;  
    int pageCount = uiFormPageIteratorPublic.getAvailablePage();
    if(pageCount >= currentPage){
      uiFormPageIteratorPublic.setCurrentPage(currentPage);
    }else if(pageCount < currentPage){
      uiFormPageIteratorPublic.setCurrentPage(currentPage-1);
    }
    List<Identity> lists;
    lists = uiFormPageIteratorPublic.getCurrentPageData();
    return lists;
  }
  
  /**
   * Get list of identity that have relation with current identity (invited relation).
   *  
   * @return
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<Identity> getInvitedRelationList() throws Exception {
    List<Identity> listIdentity = getPublicIdentities();
    int currentPage = uiFormPageIteratorPublic.getCurrentPage();
    LazyPageList<Identity> pageList = new LazyPageList<Identity>(new IdentityListAccess(listIdentity), 5);
    uiFormPageIteratorPublic.setPageList(pageList) ;  
    int pageCount = uiFormPageIteratorPublic.getAvailablePage();
    if(pageCount >= currentPage){
      uiFormPageIteratorPublic.setCurrentPage(currentPage);
    }else if(pageCount < currentPage){
      uiFormPageIteratorPublic.setCurrentPage(currentPage-1);
    }
    List<Identity> lists;
    lists = uiFormPageIteratorPublic.getCurrentPageData();
    return lists;
  }
  
  /**
   * Get list of identity that have relation with current identity (pending relation).
   *  
   * @return
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public List<Identity> getPendingRelationList() throws Exception {
    List<Identity> listIdentity = getPublicIdentities();
    int currentPage = uiFormPageIteratorPublic.getCurrentPage();
    LazyPageList<Identity> pageList = new LazyPageList<Identity>(new IdentityListAccess(listIdentity), 5);
    uiFormPageIteratorPublic.setPageList(pageList) ;  
    int pageCount = uiFormPageIteratorPublic.getAvailablePage();
    if(pageCount >= currentPage){
      uiFormPageIteratorPublic.setCurrentPage(currentPage);
    }else if(pageCount < currentPage){
      uiFormPageIteratorPublic.setCurrentPage(currentPage-1);
    }
    List<Identity> lists;
    lists = uiFormPageIteratorPublic.getCurrentPageData();
    return lists;
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
    return im.getIdentityByRemoteId("organization", getCurrentViewerUserName());
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
  
  public String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance();
    return pcontainer.getPortalContainerInfo().getContainerName();  
  }
  
  public String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;    
    return rService.getCurrentRepository().getConfiguration().getName() ;
  }
  
  /**
   * Add identity to contact list.
   * 
   */
  public static class AddContactActionListener extends EventListener<UIPeople> {
    @Override
    public void execute(Event<UIPeople> event) throws Exception {
      UIPeople portlet = event.getSource();

      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      String currUserId = portlet.getCurrentUserName();

      IdentityManager im = portlet.getIdentityManager();
      Identity currIdentity = im.getIdentityByRemoteId(OrganizationIdentityProvider.NAME,
                                                       currUserId);

      Identity requestedIdentity = im.getIdentityById(userId);

      RelationshipManager rm = portlet.getRelationshipManager();

      Relationship rel = rm.getRelationship(currIdentity, requestedIdentity);

      if (rel == null) {
        rel = rm.create(currIdentity, requestedIdentity);
        rel.setStatus(Relationship.Type.PENDING);
        rm.save(rel);
      }
    }
  }
  
  static public class RemoveActionListener extends EventListener<UIPeople> {
    @Override
    public void execute(Event<UIPeople> event) throws Exception {
      UIPeople uiMyRelation = event.getSource();
      String identityId = event.getRequestContext().getRequestParameter(OBJECTID);
      String currUserId = uiMyRelation.getCurrentUserName();

      IdentityManager im = uiMyRelation.getIdentityManager();
      Identity currIdentity = im.getIdentityByRemoteId(OrganizationIdentityProvider.NAME,
                                                       currUserId);

      Identity requestedIdentity = im.getIdentityById(identityId);

      RelationshipManager rm = uiMyRelation.getRelationshipManager();

      Relationship rel = rm.getRelationship(currIdentity, requestedIdentity);
      if (rel != null)
        rm.remove(rel);
    }
  }
  
  static public class AcceptActionListener extends EventListener<UIPeople> {
    @Override
    public void execute(Event<UIPeople> event) throws Exception {
      UIPeople uiMyRelation = event.getSource();
      String identityId = event.getRequestContext().getRequestParameter(OBJECTID);
      String currUserId = uiMyRelation.getCurrentUserName();
      IdentityManager im = uiMyRelation.getIdentityManager();
      Identity currIdentity = im.getIdentityByRemoteId(OrganizationIdentityProvider.NAME,
                                                       currUserId);

      Identity requestedIdentity = im.getIdentityById(identityId);

      RelationshipManager rm = uiMyRelation.getRelationshipManager();

      Relationship rel = rm.getRelationship(currIdentity, requestedIdentity);

      rel.setStatus(Relationship.Type.CONFIRM);
      rm.save(rel);  
    }
  }
  
  static public class DenyActionListener extends EventListener<UIPeople> {
    @Override
    public void execute(Event<UIPeople> event) throws Exception {
      UIPeople uiMyRelation = event.getSource();
      String identityId = event.getRequestContext().getRequestParameter(OBJECTID);
      String currUserId = uiMyRelation.getCurrentUserName();

      IdentityManager im = uiMyRelation.getIdentityManager();
      Identity currIdentity = im.getIdentityByRemoteId(OrganizationIdentityProvider.NAME,
                                                       currUserId);

      Identity requestedIdentity = im.getIdentityById(identityId);

      RelationshipManager rm = uiMyRelation.getRelationshipManager();

      Relationship rel = rm.getRelationship(currIdentity, requestedIdentity);
      if (rel != null)
        rm.remove(rel);
    }
  }
  
  public static class SearchActionListener extends EventListener<UIPeople> {
    @Override
    public void execute(Event<UIPeople> event) throws Exception {
      UIPeople uiPub = event.getSource();
      UIProfileUserSearch uiProfileUserSearch = uiPub.getChild(UIProfileUserSearch.class);
      List<Identity> identityList = uiProfileUserSearch.getidentityList();
      uiPub.setIdentityList(identityList);
    }
  }
    
  public boolean isEditable () {
    RequestContext context = RequestContext.getCurrentInstance();
    String currentUserName = context.getRemoteUser();
    String currentViewer = URLUtils.getCurrentUser();
    
    return currentUserName.equals(currentViewer);
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
  
  private void setIdentityList(List<Identity> identities) {
    identityList = identities;
  }
  
  private List<Identity> getIdentityList() throws Exception {
    return identityList;
  }
  
  private List<Identity> getPublicIdentities() throws Exception {
    RelationshipManager relManager = getRelationshipManager();
    Identity currentIdentity = getCurrentViewerIdentity();
    List<Identity> matchIdentities = getIdentityList();
    List<Identity> identities = new ArrayList<Identity>();
    if (matchIdentities == null) {
      return relManager.getIdentities(currentIdentity);
    }
    
    for (Identity id : matchIdentities) {
      if (!(id.getId().equals(currentIdentity.getId())) && (relManager.getRelationship(currentIdentity, id) == null)) {
        identities.add(id);
      }
    }
    
    return identities;
  }
  
  /**
   * Get invited relationships from searched result identities. 
   * 
   * @return Relationship list.
   * @throws Exception
   */
  private List<Relationship> getMyRelationIdentities() throws Exception {
    RelationshipManager relm = getRelationshipManager();
    Identity currentIdentity = getCurrentViewerIdentity();
    
    List<Identity> matchIdentities = getIdentityList();
    
    if (matchIdentities == null) {
      return relm.getContacts(currentIdentity);
    }
    
    return relm.getContacts(currentIdentity, matchIdentities);
  }
  
  /**
   * Get contact relationships from searched result identities. 
   * 
   * @return Relationship list.
   * @throws Exception
   */
  private List<Relationship> getInvitedRelationIdentities() throws Exception {
    RelationshipManager relm = getRelationshipManager();
    Identity currentIdentity = getCurrentViewerIdentity();
    
    List<Identity> matchIdentities = getIdentityList();
    
    if (matchIdentities == null) {
      return relm.getPending(currentIdentity, false);
    }
    
    return relm.getPending(currentIdentity, matchIdentities, false);
  }
  
  /**
   * Get pending relationships from searched result identities. 
   * 
   * @return Relationship list.
   * @throws Exception
   */
  private List<Relationship> getPendingRelationIdentities() throws Exception {
    RelationshipManager relm = getRelationshipManager();
    Identity currentIdentity = getCurrentViewerIdentity();
    List<Identity> matchIdentities = getIdentityList();
    
    if (matchIdentities == null) {
      return relm.getPending(currentIdentity, true);
    }
    
    return relm.getPending(currentIdentity, matchIdentities, true);
  }
}
