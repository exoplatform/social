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

import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.Relationship;
import org.exoplatform.social.core.relationship.RelationshipManager;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Aug 25, 2009  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/portal/webui/component/UIMyRelation.gtmpl",
    events = {
        @EventConfig(listeners = UIMyRelation.RemoveActionListener.class),
        @EventConfig(listeners = UIMyRelation.AcceptActionListener.class),
        @EventConfig(listeners = UIMyRelation.DenyActionListener.class)
      }
)
public class UIMyRelation extends UIForm {
  
  private RelationshipManager relationshipManager;
  private Identity currIdentity = null;
  private IdentityManager identityManager = null;
  
  public List<Relationship> getMyRelation() throws Exception {
    RelationshipManager relationshipManager = getRelationshipManager();
    Identity currId = getCurrentIdentity();
    return relationshipManager.getContacts(currId);
  }
  
  public List<Relationship> getInvitedRelation() throws Exception {
    RelationshipManager relationshipManager = getRelationshipManager();
    Identity currId = getCurrentIdentity();
    return relationshipManager.getPending(currId, false);
  }
  
  private RelationshipManager getRelationshipManager() {
    if(relationshipManager == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      relationshipManager = (RelationshipManager) container.getComponentInstanceOfType(RelationshipManager.class);
    }
    return relationshipManager;
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
  
  private IdentityManager getIdentityManager() {
    if(identityManager == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      identityManager =  (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    }
    return identityManager;
  }
  
  private String getCurrentUserName() {
    PortalRequestContext portalRequest = Util.getPortalRequestContext();
    return portalRequest.getRemoteUser();
  }
 
}
