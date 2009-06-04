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
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.Relationship;
import org.exoplatform.social.core.relationship.RelationshipManager;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(template = "app:/groovy/portal/webui/component/UIProfileList.gtmpl", events = { @EventConfig(listeners = UIProfileList.ChangeViewActionListener.class) })
public class UIProfileList extends UIContainer {
  List                tempList     = null;

  Identity            currIdentity = null;

  Type                currType     = null;

  Type                displayType  = null;

  RelationshipManager rm           = null;

  IdentityManager     im           = null;

  public enum Type {
    ALL, CONTACTS, PENDING;
  }

  public UIProfileList() throws Exception {
    addChild(UIDisplayProfileList.class, null, null);
  }

  public List getList() {
    return tempList;
  }

  public Type getCurrentType() {
    return currType;
  }

  public List load(Type type) throws Exception {
    if (type.equals(Type.ALL)) {
      tempList = loadAllProfiles();
    } else if (type.equals(Type.PENDING)) {
      tempList = loadPendingList();
    } else if (type.equals(Type.CONTACTS)) {
      tempList = loadContactList();
    }

    currType = type;
    return tempList;
  }

  public void unloadTemporaryVar() {
    tempList = null;
    currType = null;
  }

  public Relationship.Type getContactStatus(Identity identity) throws Exception {
    if (identity.getId().equals(getCurrentIdentity().getId()))
      return Relationship.Type.SELF;
    RelationshipManager rm = getRelationshipManager();
    Relationship rl = rm.getRelationship(identity, getCurrentIdentity());
    return rm.getRelationshipStatus(rl, getCurrentIdentity());
  }

  private RelationshipManager getRelationshipManager() {
    if (rm == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      rm = (RelationshipManager) container.getComponentInstanceOfType(RelationshipManager.class);
    }
    return rm;
  }

  private IdentityManager getIdentityManager() {
    if (im == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    }
    return im;
  }

  public void processRender(WebuiRequestContext context) throws Exception {
    try {
      super.processRender(context);
    } finally {
      // make sure we don't keep in memory the list of profile and relations
      unloadTemporaryVar();
    }
  }

  private List<Identity> loadAllProfiles() throws Exception {
    IdentityManager im = getIdentityManager();
    List<Identity> ids = im.getIdentities("organization");
    return ids;
  }

  private List<Relationship> loadPendingList() throws Exception {
    RelationshipManager rm = getRelationshipManager();
    Identity currId = getCurrentIdentity();
    return rm.getPending(currId, true);

  }

  private List<Relationship> loadContactList() throws Exception {
    RelationshipManager rm = getRelationshipManager();
    Identity currId = getCurrentIdentity();
    return rm.getContacts(currId);
  }

  public String getCurrentUserName() {
    // if we are not on the page of a user, we display the profile of the
    // current user
    RequestContext context = RequestContext.getCurrentInstance();
    return context.getRemoteUser();
  }

  public Identity getCurrentIdentity() throws Exception {
    if (currIdentity == null) {
      IdentityManager im = getIdentityManager();
      currIdentity = im.getIdentityByRemoteId("organization", getCurrentUserName());
    }
    return currIdentity;
  }

  public static class ChangeViewActionListener extends EventListener<UIProfileList> {

    public void execute(Event<UIProfileList> event) throws Exception {
      UIProfileList pl = event.getSource();
      String type = event.getRequestContext().getRequestParameter(OBJECTID);
      pl.setDisplayType(UIProfileList.Type.valueOf(type));

      event.getRequestContext().addUIComponentToUpdateByAjax(pl);
    }
  }

  private void setDisplayType(Type type) {
    this.displayType = type;
  }

  public Type getDisplayType() {
    return displayType;
  }
}
