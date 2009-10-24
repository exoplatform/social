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
import org.exoplatform.social.portlet.URLUtils;
import org.exoplatform.social.portlet.profile.UIProfileUserSearch;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIRepeater;
import org.exoplatform.webui.core.UIVirtualList;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormPageIterator;

/**
 * Created by The eXo Platform SAS
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Aug 25, 2009  
 */
@ComponentConfigs( {
    @ComponentConfig(
        lifecycle = UIFormLifecycle.class,
        template =  "app:/groovy/portal/webui/component/UIMyRelation.gtmpl",
        events = {
            @EventConfig(listeners = UIMyRelation.RemoveActionListener.class),
            @EventConfig(listeners = UIMyRelation.AcceptActionListener.class),
            @EventConfig(listeners = UIMyRelation.DenyActionListener.class),
            @EventConfig(listeners = UIMyRelation.SearchActionListener.class, phase = Phase.DECODE)
        }
    ),
    @ComponentConfig(
        id = "UIInvited",
        type = UIRepeater.class,
        template = "app:/groovy/portal/webui/component/UIInvited.gtmpl") 
  }
)
public class UIMyRelation extends UIForm {
  /** UIFormPageIterator */
  UIFormPageIterator uiFormPageIteratorContact;
  /** UIFormPageIterator ID. */
  private final String iteratorIDContact = "UIFormPageIteratorContact";
  private RelationshipManager relationshipManager;
  private Identity currIdentity = null;
  private IdentityManager identityManager = null;
  UIProfileUserSearch uiProfileUserSearchRelation = null;
  private List<Identity> identityList;
  
  
  public List<Identity> getIdentityList() { return identityList; }

  public void setIdentityList(List<Identity> identityList) { this.identityList = identityList; }

  /**
   * Get UIFormPageIterator.
   * @return
   */
  public UIFormPageIterator getUiFormPageIteratorContact() {
    return uiFormPageIteratorContact;
  }
  
  /**
   * Constructor.
   * @throws Exception 
   */
  public UIMyRelation() throws Exception {
    UIVirtualList virtualList = addChild(UIVirtualList.class, null, null);      
    virtualList.setPageSize(2);
    UIRepeater uiRepeater = createUIComponent(UIRepeater.class, "UIInvited", null);
    virtualList.setUIComponent(uiRepeater);
    
    uiFormPageIteratorContact = createUIComponent(UIFormPageIterator.class, null, iteratorIDContact);
    addChild(uiFormPageIteratorContact);
    uiProfileUserSearchRelation = createUIComponent(UIProfileUserSearch.class, null, "UIMyRelationSearch");
    addChild(uiProfileUserSearchRelation);
  }
  
  public int loadInvited() throws Exception {
    List<Relationship> listRelationShip = getInvitedRelations();
    if (listRelationShip == null) listRelationShip = new ArrayList<Relationship>();
    UIVirtualList virtualList = getChild(UIVirtualList.class);
    LazyPageList<Relationship> pageList = new LazyPageList<Relationship>(new RelationshipListAccess(listRelationShip), 2);
    virtualList.dataBind(pageList);
    return listRelationShip.size();
  }
  
  public List<Relationship> getMyRelation() throws Exception {
    List<Relationship> listContacts = getMyContacts();
    List<Relationship> contactLists = getDisplayRelationList(listContacts, uiFormPageIteratorContact);
    return contactLists;
  }

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
  
  public Identity getCurrentIdentity() throws Exception {
    if (currIdentity == null) {
      IdentityManager im = getIdentityManager();
      currIdentity = im.getIdentityByRemoteId("organization", getCurrentUserName());
    }
    return currIdentity;
  }
  
  static public class RemoveActionListener extends EventListener<UIMyRelation> {
    @Override
    public void execute(Event<UIMyRelation> event) throws Exception {
      UIMyRelation uiMyRelation = event.getSource();
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
  
  static public class AcceptActionListener extends EventListener<UIMyRelation> {
    @Override
    public void execute(Event<UIMyRelation> event) throws Exception {
      UIMyRelation uiMyRelation = event.getSource();
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
  
  static public class DenyActionListener extends EventListener<UIMyRelation> {
    @Override
    public void execute(Event<UIMyRelation> event) throws Exception {
      UIMyRelation uiMyRelation = event.getSource();
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
  
  public static class SearchActionListener extends EventListener<UIMyRelation> {
    @Override
    public void execute(Event<UIMyRelation> event) throws Exception {
      UIMyRelation uiMyRelation = event.getSource();
      UIProfileUserSearch uiProfileUserSearch = uiMyRelation.getChild(UIProfileUserSearch.class);
      List<Identity> identityList = uiProfileUserSearch.getidentityList();
      uiMyRelation.setIdentityList(identityList);
    }
  }
  
  /**
   * Get contact relationships from searched result identities. 
   * 
   * @return Relationship list.
   * @throws Exception
   */
  private List<Relationship> getInvitedRelations() throws Exception {
    RelationshipManager relm = getRelationshipManager();
    Identity currentIdentity = getCurrentIdentity();
    
    List<Identity> matchIdentities = getIdentityList();
    
    if (matchIdentities == null) {
      return relm.getPending(currentIdentity, false);
    }
    
    return relm.getPending(currIdentity, matchIdentities, false);
  }
  
  /**
   * Get invited relationships from searched result identities. 
   * 
   * @return Relationship list.
   * @throws Exception
   */
  private List<Relationship> getMyContacts() throws Exception {
    RelationshipManager relm = getRelationshipManager();
    Identity currentIdentity = getCurrentIdentity();
    
    List<Identity> matchIdentities = getIdentityList();
    
    if (matchIdentities == null) {
      return relm.getContacts(currentIdentity);
    }
    
    return relm.getContacts(currIdentity, matchIdentities);
  }
  
  private IdentityManager getIdentityManager() {
    if(identityManager == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      identityManager =  (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    }
    return identityManager;
  }
  
  private String getCurrentUserName() {
    String username = URLUtils.getCurrentUser();
    if(username != null)
      return username;
    
    PortalRequestContext portalRequest = Util.getPortalRequestContext();
    return portalRequest.getRemoteUser();
  }
 
  public String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance();
    return pcontainer.getPortalContainerInfo().getContainerName();  
  }
  
  public String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;    
    return rService.getCurrentRepository().getConfiguration().getName() ;
  }
  
  private RelationshipManager getRelationshipManager() {
    if(relationshipManager == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      relationshipManager = (RelationshipManager) container.getComponentInstanceOfType(RelationshipManager.class);
    }
    return relationshipManager;
  }
}
