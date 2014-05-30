/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.webui.connections;

import java.io.Writer;
import java.util.ResourceBundle;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIUpdateRelationship extends UIContainer {

  private Identity identity = null;
  private Relationship relationship;

  public UIUpdateRelationship() {
  }

  public UIUpdateRelationship setIdentity(Identity identity) {
    this.identity = identity;
    return this;
  }

  public UIUpdateRelationship setRelationship(Relationship relationship) {
    this.relationship = relationship;
    return this;
  }
  
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    if (getTemplate() != null) {
      super.processRender(context);
      return;
    }
    Writer writer = context.getWriter();
    writer.append("<div id=\"").append(getId()).append("\">");
    if(identity != null) {
      ResourceBundle res = context.getApplicationResourceBundle();
      UIAllPeople uiAllPeople = getAncestorOfType(UIAllPeople.class);
      
      String label = res.getString("UIAllPeople.label.Ignore");
      String action = uiAllPeople.event("Ignore", identity.getId());
      String statusLabel = "", statusClass = "";
      //
      Type status = (relationship != null) ? relationship.getStatus() : null;
      //
      if (status == null) {
        label = res.getString("UIAllPeople.label.Connect");
        action = uiAllPeople.event("Connect", identity.getId());

      } else if (status.equals(Type.PENDING)) {
        label = res.getString("UIAllPeople.label.CancelRequest");
        statusLabel = res.getString("UIAllPeople.label.InvitationSent");

      } else if (status == Type.CONFIRMED) {
        label = res.getString("UIAllPeople.label.RemoveConnection");
        statusClass = "checkedBox";
      }
      //
      writer.append("<div style=\"display:none\" data-action=\"").append(action).append("\" ")
            .append("data-status=\"").append(statusLabel).append("\" ")
            .append("data-class=\"").append(statusClass).append("\">")
            .append(label).append("</div>");
      //
      context.getJavascriptManager().getRequireJS().require("SHARED/socialUtil", "utils")
             .addScripts("utils.updateRelationship('" + identity.getId() + "' );");
    }
    identity = null;
    writer.append("</div>");
  }
  
  
}