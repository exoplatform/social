package org.exoplatform.social.user.portlet;

import java.io.Writer;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(
  lifecycle = UIContainerLifecycle.class,
  events = {
    @EventConfig(listeners = UIRelationshipAction.ConnectActionListener.class),
    @EventConfig(listeners = UIRelationshipAction.CancelActionListener.class),
    @EventConfig(listeners = UIRelationshipAction.AcceptActionListener.class),
    @EventConfig(listeners = UIRelationshipAction.DenyActionListener.class),
    @EventConfig(listeners = UIRelationshipAction.DisconnectActionListener.class)
  }
)
public class UIRelationshipAction extends UIContainer {
  private boolean isRenderedActions = true;

  public UIRelationshipAction() {
  }

  public UIRelationshipAction setRenderedActions(boolean isRenderedActions) {
    this.isRenderedActions = isRenderedActions;
    return this;
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    Identity viewerIdentity = Utils.getViewerIdentity();// current login user
    Identity ownerIdentity = Utils.getOwnerIdentity(); // current user viewing
    //
    Writer writer = context.getWriter();
    writer.append("<div class=\"uiRelationshipAction clearfix\" id=\"").append(getId()).append("\">");
    writer.append("<div class=\"user-actions pull-right\" data-user-action=\"").append(ownerIdentity.getRemoteId()).append("\">");
    //
    if (isRenderedActions && !ownerIdentity.equals(viewerIdentity)) {
      Relationship relationship = Utils.getRelationshipManager().get(viewerIdentity, ownerIdentity);
      Type status = (relationship != null) ? relationship.getStatus() : null;
      
      if(status == null) {
        writer.append("<button class=\"btn btn-primary connect-status\" onclick=\"").append(event("Connect")).append("\">")
              .append("<i class=\"uiIconStatusConnect\"></i>")
              .append(UserProfileHelper.getLabel(context, "UIBasicProfile.action.label.Connect")).append("</button>");
      } else if(status == Type.PENDING) {//PENDING
        if(relationship.getSender().equals(viewerIdentity)) {
          writer.append("<button class=\"btn show-default\">")
                .append("<i class=\"uiIconStatusSent\"></i> ")
                .append(UserProfileHelper.getLabel(context, "UIBasicProfile.action.label.RequestSent")).append("</button>");
          writer.append("<button class=\"btn hide-default\" onclick=\"").append(event("Cancel")).append("\">")
                .append("<i class=\"uiIconStatusCancel\"></i> ")
                .append(UserProfileHelper.getLabel(context, "UIBasicProfile.action.CancelRequest")).append("</button>");          
        } else {
          writer.append("<button class=\"btn btn-primary show-default\">")
                .append("<i class=\"uiIconStatusReceived\"></i> ")
                .append(UserProfileHelper.getLabel(context, "UIBasicProfile.action.label.RequestReceived")).append("</button>");
          
          writer.append("<button class=\"btn hide-default\" onclick=\"").append(event("Deny")).append("\">")
                .append("<i class=\"uiIconStatusDeny\"></i> ")
                .append(UserProfileHelper.getLabel(context, "UIBasicProfile.action.Deny")).append("</button>");          
          writer.append("&nbsp;<button class=\"btn btn-primary hide-default\" onclick=\"").append(event("Accept")).append("\">")
                .append("<i class=\"uiIconStatusAccept\"></i> ")
                .append(UserProfileHelper.getLabel(context, "UIBasicProfile.action.Accept")).append("</button>");          
        }
      } else if(status == Type.CONFIRMED) {
        writer.append("<button class=\"btn show-default\">")
              .append("<i class=\"uiIconStatusConnected\"></i> ")
              .append(UserProfileHelper.getLabel(context, "UIBasicProfile.label.Connected")).append("</button>");
        writer.append("<button class=\"btn hide-default\" onclick=\"").append(event("Disconnect")).append("\">")
              .append("<i class=\"uiIconStatusDisconnect\"></i> ")
              .append(UserProfileHelper.getLabel(context, "UIBasicProfile.action.RemoveConnection")).append("</button>");       
      }
      writer.append("</div>");
      writer.append("</div>");
    } else {
      super.processRender(context);
    }
  }

  public static abstract class AbstractActionListener extends EventListener<UIRelationshipAction> {
    protected Relationship relationship = null;
    protected String msgKey = "UIRelationshipAction.label.ConnectNotExisting";
    @Override
    public void execute(Event<UIRelationshipAction> event) throws Exception {
      UIRelationshipAction uiAction = event.getSource();
      relationship = Utils.getRelationshipManager().get(Utils.getOwnerIdentity(), Utils.getViewerIdentity());
      if (isValid()) {
        doAction(event);
      } else {
        uiAction.getAncestorOfType(UIPortletApplication.class).addMessage(new ApplicationMessage(msgKey, new String[]{}, ApplicationMessage.WARNING));
      }
      //
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAction.getParent());
    }
    protected boolean isValid() {
      return (relationship != null);
    }
    protected abstract void doAction(Event<UIRelationshipAction> event);
  }
  
  public static class ConnectActionListener extends AbstractActionListener {
    @Override
    protected boolean isValid() {
      msgKey = "UIRelationshipAction.label.ConnectionExisted";
      return (relationship == null);
    }
    @Override
    protected void doAction(Event<UIRelationshipAction> event) {// sender --> owner
      Utils.getRelationshipManager().inviteToConnect(Utils.getViewerIdentity(), Utils.getOwnerIdentity());
    }
  }

  public static class CancelActionListener extends AbstractActionListener {
    @Override
    protected void doAction(Event<UIRelationshipAction> event) {
      Utils.getRelationshipManager().delete(relationship);
    }
  }

  public static class AcceptActionListener extends AbstractActionListener {
    @Override
    protected boolean isValid() {
      return super.isValid() && (relationship.getStatus() != Type.IGNORED);
    }
    @Override
    protected void doAction(Event<UIRelationshipAction> event) {
      Utils.getRelationshipManager().confirm(relationship.getReceiver(), relationship.getSender());
      Utils.updateWorkingWorkSpace();
    }
  }

  public static class DenyActionListener extends AbstractActionListener {
    @Override
    protected void doAction(Event<UIRelationshipAction> event) {
      Utils.getRelationshipManager().deny(relationship.getReceiver(), relationship.getSender());
      Utils.updateWorkingWorkSpace();
    }
  }

  public static class DisconnectActionListener extends CancelActionListener {}
}
