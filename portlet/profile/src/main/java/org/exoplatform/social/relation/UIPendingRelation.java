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
import org.exoplatform.social.portlet.profile.Utils;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/portal/webui/component/UIPendingRelation.gtmpl",
    events = { 
        @EventConfig(listeners = UIPendingRelation.DenyContactActionListener.class),
        @EventConfig(listeners = UIPendingRelation.SearchActionListener.class, phase = Phase.DECODE)
      }
)
public class UIPendingRelation extends UIForm {
  /** UIFormPageIterator */
  UIFormPageIterator uiFormPageIterator_;
  /** UIFormPageIterator ID. */
  private final String iteratorID_ = "UIFormPageIteratorPendingRelation";
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
    uiProfileUserSearchPending = createUIComponent(UIProfileUserSearch.class, null, "UIPendingRelationSearch");
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
      List<Identity> identityList = uiProfileUserSearch.getidentityList();
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
  
}
