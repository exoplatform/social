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

import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.relationship.Relationship;
import org.exoplatform.social.core.relationship.RelationshipManager;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(template = "app:/groovy/portal/webui/component/UIDisplayProfileList.gtmpl", events = {
    @EventConfig(listeners = UIDisplayProfileList.AddContactActionListener.class),
    @EventConfig(listeners = UIDisplayProfileList.AcceptContactActionListener.class),
    @EventConfig(listeners = UIDisplayProfileList.DenyContactActionListener.class) })
public class UIDisplayProfileList extends UIComponent {
  private IdentityManager     identityManager_ = null;

  private RelationshipManager relationManager_ = null;

  public List<Profile> getList() throws Exception {
    return ((UIProfileList) this.getParent()).getList();
  }

  public boolean isRelationshipList() {
    UIProfileList.Type type = ((UIProfileList) this.getParent()).getCurrentType();
    return type.equals(UIProfileList.Type.PENDING) || type.equals(UIProfileList.Type.CONTACTS);
  }

  public UIProfileList.Type getCurrentType() {
    return ((UIProfileList) this.getParent()).getCurrentType();
  }

  public Identity getCurrentIdentity() throws Exception {
    return ((UIProfileList) this.getParent()).getCurrentIdentity();
  }

  public Relationship.Type getContactStatus(Identity identity) throws Exception {
    return ((UIProfileList) this.getParent()).getContactStatus(identity);
  }

  public static class AddContactActionListener extends EventListener<UIDisplayProfileList> {
    public void execute(Event<UIDisplayProfileList> event) throws Exception {
      UIDisplayProfileList portlet = event.getSource();

      String userId = event.getRequestContext().getRequestParameter(OBJECTID);
      String currUserId = ((UIProfileList) portlet.getParent()).getCurrentUserName();

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
      String currUserId = ((UIProfileList) portlet.getParent()).getCurrentUserName();

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
      String currUserId = ((UIProfileList) portlet.getParent()).getCurrentUserName();

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

  private IdentityManager getIdentityManager() {
    if (identityManager_ == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      identityManager_ = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    }
    return identityManager_;
  }

  private RelationshipManager getRelationshipManager() {
    if (relationManager_ == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      relationManager_ = (RelationshipManager) container.getComponentInstanceOfType(RelationshipManager.class);
    }
    return relationManager_;
  }
}
