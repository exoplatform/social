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
package org.exoplatform.social.portlet.profilelist;

import java.util.Iterator;
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
import org.exoplatform.social.relation.IdentityListAccess;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

@ComponentConfig(
    template = "app:/groovy/portal/webui/component/UIDisplayProfileList.gtmpl",
    events = {
            @EventConfig(listeners = UIDisplayProfileList.AddContactActionListener.class),
            @EventConfig(listeners = UIDisplayProfileList.AcceptContactActionListener.class),
            @EventConfig(listeners = UIDisplayProfileList.DenyContactActionListener.class),
            @EventConfig(listeners = UIDisplayProfileList.SearchActionListener.class, phase = Phase.DECODE)
    }
)
            
public class UIDisplayProfileList extends UIContainer {
  private IdentityManager     identityManager_ = null;
  UIProfileUserSearch uiProfileUserSearchPeople = null;
  private UIPageIterator iterator;
  private final Integer PEOPLE_PER_PAGE = 10;
  private final String ITERATOR_ID = "UIIteratorPeople";
  private List<Identity> identityList;
  
  public List<Identity> getIdentityList() { return identityList; }

  public void setIdentityList(List<Identity> identityList) { this.identityList = identityList; }
  
  /**
   * Get UIPageIterator
   * @return
   */
  public UIPageIterator getUIPageIterator() {
    return iterator;
  }
  
  
  public UIDisplayProfileList() throws Exception {
    iterator = addChild(UIPageIterator.class, null, ITERATOR_ID);
    uiProfileUserSearchPeople = createUIComponent(UIProfileUserSearch.class, null, "UIPeopleSearch");
    addChild(uiProfileUserSearchPeople);
  }
  
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

  public static class AddContactActionListener extends EventListener<UIDisplayProfileList> {
    public void execute(Event<UIDisplayProfileList> event) throws Exception {
      UIDisplayProfileList portlet = event.getSource();
      
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
      } else {
        rel.setStatus(Relationship.Type.CONFIRM);
        rm.save(rel);
      }
    }
  }

  public static class AcceptContactActionListener extends EventListener<UIDisplayProfileList> {
    public void execute(Event<UIDisplayProfileList> event) throws Exception {
      UIDisplayProfileList portlet = event.getSource();

      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      String currUserId = portlet.getCurrentUserName();

      IdentityManager im = portlet.getIdentityManager();
      Identity currIdentity = im.getIdentityByRemoteId(OrganizationIdentityProvider.NAME,
                                                       currUserId);

      Identity requestedIdentity = im.getIdentityById(userId);

      RelationshipManager rm = portlet.getRelationshipManager();

      Relationship rel = rm.getRelationship(currIdentity, requestedIdentity);

      rel.setStatus(Relationship.Type.CONFIRM);
      rm.save(rel);
    }
  }

  public static class DenyContactActionListener extends EventListener<UIDisplayProfileList> {
    public void execute(Event<UIDisplayProfileList> event) throws Exception {
      UIDisplayProfileList portlet = event.getSource();

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

  public static class SearchActionListener extends EventListener<UIDisplayProfileList> {
    @Override
    public void execute(Event<UIDisplayProfileList> event) throws Exception {
      UIDisplayProfileList uiMyRelation = event.getSource();
      UIProfileUserSearch uiProfileUserSearch = uiMyRelation.getChild(UIProfileUserSearch.class);
      List<Identity> identityList = uiProfileUserSearch.getIdentityList();
      uiMyRelation.setIdentityList(identityList);
    }
  }
  
  private IdentityManager getIdentityManager() {
    if (identityManager_ == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      identityManager_ = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    }
    return identityManager_;
  }

  private List<Identity> getProfiles() throws Exception {
    List<Identity> matchIdentities = getIdentityList();
    
    if (matchIdentities == null) {
      return loadAllProfiles();
    }
    
    return matchIdentities;
  }
  
  public Identity getCurrentViewerIdentity() throws Exception {
    IdentityManager im = getIdentityManager();
    return im.getIdentityByRemoteId("organization", getCurrentViewerUserName());
  }
  
  private String getCurrentViewerUserName() {
    String username = URLUtils.getCurrentUser();
    if(username != null)
      return username;
    
    PortalRequestContext portalRequest = Util.getPortalRequestContext();
    
    return portalRequest.getRemoteUser();
  }
  private List<Identity> loadAllProfiles() throws Exception {
    IdentityManager im = getIdentityManager();
    List<Identity> ids = im.getIdentities("organization");
    Iterator<Identity> itr = ids.iterator();
    while(itr.hasNext()) {
      Identity id = itr.next();
      if(id.getId() == getCurrentIdentity().getId()){
        itr.remove();
      }
    }
    return ids;
  }
  
  public Relationship.Type getContactStatus(Identity identity) throws Exception {
    if (identity.getId().equals(getCurrentIdentity().getId()))
      return Relationship.Type.SELF;
    RelationshipManager rm = getRelationshipManager();
    Relationship rl = rm.getRelationship(identity, getCurrentIdentity());
    return rm.getRelationshipStatus(rl, getCurrentIdentity());
  }

  private RelationshipManager getRelationshipManager() {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      return (RelationshipManager) container.getComponentInstanceOfType(RelationshipManager.class);
  }

  public String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance();
    return pcontainer.getPortalContainerInfo().getContainerName();  
  }
  
  public String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;    
    return rService.getCurrentRepository().getConfiguration().getName() ;
  }
  
  public String getCurrentUserName() {
    RequestContext context = RequestContext.getCurrentInstance();
    return context.getRemoteUser();
  }

  public Identity getCurrentIdentity() throws Exception {
      IdentityManager im = getIdentityManager();
      return im.getIdentityByRemoteId("organization", getCurrentUserName());
  }
  
  public String getPath() {
    String nodePath = Util.getPortalRequestContext().getNodePath();
    String uriPath = Util.getPortalRequestContext().getRequestURI();
    return uriPath.replaceAll(nodePath, "");
  }
}
