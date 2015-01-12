/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.portlet.actionProfile;


import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.inject.Inject;

import juzu.Path;
import juzu.Resource;
import juzu.Response;
import juzu.View;
import juzu.impl.common.JSON;
import juzu.io.AppendableStream;
import juzu.request.RenderContext;
import juzu.template.Template;

import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.juzu.ajax.Ajax;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.webui.Utils;


public class RelationshipAction {
  private static final Log LOG = ExoLogger.getLogger(RelationshipAction.class);

  @Inject
  @Path("index.gtmpl") Template index;
  
  @Inject
  ResourceBundle bundle;  
  
  @Inject
  PluginSettingService providerSettingService;

  private Locale locale = Locale.ENGLISH;

  @View
  public void index(RenderContext renderContext) {
    if (renderContext != null) {
      locale = renderContext.getUserContext().getLocale();
    }
    if (bundle == null) {
      bundle = renderContext.getApplicationContext().resolveBundle(locale);
    }
    Context context = new Context(bundle);
    index.render(parameters(context));
  }
  
  @Ajax
  @Resource
  public Response saveSetting(String params) {
    JSON data = new JSON();
    try {
      data.set("ok", "true");
    } catch (Exception e) {
      data.set("ok", "false");
      data.set("status", e.toString());
    }
    return Response.ok(data.toString()).withMimeType("application/json");
  }
  

  private static Map<String, Object> parameters(Context context) {

    Map<String, Object> parameters = new HashMap<String, Object>();
    Identity viewerIdentity = Utils.getViewerIdentity();// current login user
    Identity ownerIdentity = Utils.getOwnerIdentity(); // current user viewing

    StringBuilder writer = new StringBuilder();
    writer.append("<div class=\"uiRelationshipAction clearfix\" id=\"RelationshipAction_\">");
    writer.append("<div class=\"user-actions pull-right\" data-user-action=\"").append(ownerIdentity.getRemoteId()).append("\">");
    Relationship relationship = Utils.getRelationshipManager().get(viewerIdentity, ownerIdentity);
    Type status = (relationship != null) ? relationship.getStatus() : null;
    
    if (!ownerIdentity.equals(viewerIdentity)) {
      if(status == null) {
        writer.append("<button class=\"btn btn-primary connect-status\" data-action=\"").append(event("Connect")).append("\">")
              .append("<i class=\"uiIconStatusConnect\"></i>")
              .append(context.appRes("UIBasicProfile.action.label.Connect")).append("</button>");
      } else if(status == Type.PENDING) {//PENDING
        if(relationship.getSender().equals(viewerIdentity)) {
          writer.append("<button class=\"btn show-default\">")
                .append("<i class=\"uiIconStatusSent\"></i> ")
                .append(context.appRes("UIBasicProfile.action.label.RequestSent")).append("</button>");
          writer.append("<button class=\"btn hide-default\" data-action=\"").append(event("Cancel")).append("\">")
                .append("<i class=\"uiIconStatusCancel\"></i> ")
                .append(context.appRes("UIBasicProfile.action.CancelRequest")).append("</button>");          
        } else {
          writer.append("<button class=\"btn btn-primary show-default\">")
                .append("<i class=\"uiIconStatusReceived\"></i> ")
                .append(context.appRes("UIBasicProfile.action.label.RequestReceived")).append("</button>");
          writer.append("<button class=\"btn hide-default\" data-action=\"").append(event("Deny")).append("\">")
                .append("<i class=\"uiIconStatusDeny\"></i> ")
                .append(context.appRes("UIBasicProfile.action.Deny")).append("</button>");          
          writer.append("&nbsp;<button class=\"btn btn-primary hide-default\" data-action=\"").append(event("Accept")).append("\">")
                .append("<i class=\"uiIconStatusAccept\"></i> ")
                .append(context.appRes("UIBasicProfile.action.Accept")).append("</button>");          
        }
      } else if(status == Type.CONFIRMED) {
        writer.append("<button class=\"btn show-default\">")
              .append("<i class=\"uiIconStatusConnected\"></i> ")
              .append(context.appRes("UIBasicProfile.label.Connected")).append("</button>");
        writer.append("<button class=\"btn hide-default\" data-action=\"").append(event("Disconnect")).append("\">")
              .append("<i class=\"uiIconStatusDisconnect\"></i> ")
              .append(context.appRes("UIBasicProfile.action.RemoveConnection")).append("</button>");       
      }
    }
    writer.append("</div>");
    writer.append("</div>");
    //
    parameters.put("_ctx", context);
    parameters.put("content", writer.toString());

    return parameters;
  }
  
  private static String event(String name) {
    return name;
  }

  @Ajax
  @Resource
  public Response doAction(String actionName) throws Exception {
    ActionListener action = ActionListener.getActionListener(actionName);
    if (action != null) {
      Context context = new Context(bundle);
      return action.execute(context, index);
    }
    JSON data = new JSON();
    data.set("ok", "false");
    data.set("message", "Action not existing.");
    return Response.ok(data.toString()).withMimeType("application/json");
  }

  public class Context {
    ResourceBundle rs;

    public Context(ResourceBundle rs) {
      this.rs = rs;
    }
    public String appRes(String key) {
      try {
        return rs.getString(key).replaceAll("'", "&#39;").replaceAll("\"", "&#34;");
      } catch (java.util.MissingResourceException e) {
        LOG.warn("Can't find resource for bundle key " + key);
      } catch (Exception e) {
        LOG.debug("Error when get resource bundle key " + key, e);
      }
      return key;
    }
    public String appRes(String key, String... args) {
      return MessageFormat.format(appRes(key), args);
    }
  }
  
  public enum ActionListener {
    Connect() {
      @Override
      protected boolean isValid() {
        msgKey = "UIRelationshipAction.label.ConnectionExisted";
        return (relationship == null);
      }
      @Override
      protected void doAction() {// sender --> owner
        Utils.getRelationshipManager().inviteToConnect(Utils.getViewerIdentity(), Utils.getOwnerIdentity());
      }
    },
    Cancel() {
      @Override
      protected void doAction() {
        Utils.getRelationshipManager().delete(relationship);
      }
    },
    Accept {
      @Override
      protected boolean isValid() {
        return super.isValid() && (relationship.getStatus() != Type.IGNORED);
      }
      @Override
      protected void doAction() {
        Utils.getRelationshipManager().confirm(relationship.getReceiver(), relationship.getSender());
        Utils.updateWorkingWorkSpace();
      }
    },
    Deny {
      @Override
      protected void doAction() {
        Utils.getRelationshipManager().deny(relationship.getReceiver(), relationship.getSender());
        Utils.updateWorkingWorkSpace();
      }
    },
    Disconnect {
      @Override
      protected void doAction() {
        Utils.getRelationshipManager().delete(relationship);
      }
    };
    Relationship relationship = null;
    String msgKey = "UIRelationshipAction.label.ConnectNotExisting";

    public Response execute(Context context, Template template) throws Exception {
      relationship = Utils.getRelationshipManager().get(Utils.getOwnerIdentity(), Utils.getViewerIdentity());
      JSON data = new JSON();
      if (isValid()) {
        doAction();
        //
        data.set("ok", "true");
        StringBuilder buffer = new StringBuilder();
        template.renderTo(new AppendableStream(buffer), parameters(context));
        data.set("content", buffer.toString());
        return Response.ok(data.toString()).withMimeType("application/json");
      } else {
        data.set("ok", "false");
        data.set("message", context.appRes(msgKey));
        return Response.ok(data.toString()).withMimeType("application/json");
      }
    }
    protected boolean isValid() {
      return (relationship != null);
    }

    public static ActionListener getActionListener(String name) {
      for (ActionListener action : values()) {
        if (action.name().equals(name)) {
          return action;
        }
      }
      return null;
    }
    protected abstract void doAction();
  }
     
}
