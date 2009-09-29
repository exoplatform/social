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
    template =  "app:/groovy/portal/webui/component/UIPublicRelation.gtmpl",
    events = { 
      @EventConfig(listeners = UIPublicRelation.AddContactActionListener.class),
      @EventConfig(listeners = UIPublicRelation.SearchActionListener.class, phase = Phase.DECODE)
    }
)
public class UIPublicRelation extends UIForm {
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
  public UIPublicRelation() throws Exception {
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
    List<Identity> listIdentity = getIdentityList();
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
    if (currIdentity == null) {
      IdentityManager im = getIdentityManager();
      currIdentity = im.getIdentityByRemoteId("organization", getCurrentUserName());
    }
    return currIdentity;
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
  public static class AddContactActionListener extends EventListener<UIPublicRelation> {
    @Override
    public void execute(Event<UIPublicRelation> event) throws Exception {
      UIPublicRelation portlet = event.getSource();

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
        portlet.setIdentityList(portlet.removeIdentity(requestedIdentity));
      }
    }
  }
  
  public static class SearchActionListener extends EventListener<UIPublicRelation> {
    @Override
    public void execute(Event<UIPublicRelation> event) throws Exception {
      UIPublicRelation uiPub = event.getSource();
      UIProfileUserSearch uiProfileUserSearch = uiPub.getChild(UIProfileUserSearch.class);
      List<Identity> identityList = uiProfileUserSearch.getidentityList();
      uiPub.setIdentityList(identityList);
    }
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
  
  private List<Identity> removeIdentity(Identity reqIdentity) throws Exception {
    List<Identity> rtnIdentityList = new ArrayList<Identity>();
    String reqRemoteUser = reqIdentity.getRemoteId();
    String remoteUser = null;
    for (Identity id : identityList) {
      remoteUser = id.getRemoteId();
      if (!reqRemoteUser.equals(remoteUser)) {
        rtnIdentityList.add(id);
      }
    }
    
    return rtnIdentityList;
  }
  
  private List<Identity> getIdentityList() throws Exception {
    RelationshipManager relm = getRelationshipManager();
    Identity currentIdentity = getCurrentIdentity();
    if (identityList == null) {
      return relm.getPublicRelation(currentIdentity);
    }
    
    return identityList;
  }
}
