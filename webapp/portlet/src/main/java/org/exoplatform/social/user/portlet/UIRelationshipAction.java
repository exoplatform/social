package org.exoplatform.social.user.portlet;

import java.io.Writer;
import java.util.ResourceBundle;

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
/*
  
#
UIBasicProfile.action.label.Connect=Connect
#Display is label and when mouse-over is actions.
UIBasicProfile.label.Connected=Connected
UIBasicProfile.action.RemoveConnection=Disconnect

UIBasicProfile.action.label.RequestReceived=Request received
UIBasicProfile.action.Accept=Accept
UIBasicProfile.action.Deny=Deny

UIBasicProfile.action.label.RequestSent=Request sent
UIBasicProfile.action.CancelRequest=Cancel
#
UIBasicProfile.action.EditProfile=Edit my profile
UIBasicProfile.action.ViewAll=View All  
  
  
   String label = res.getString("UIAllPeople.label.Ignore");
      String action = uiAllPeople.event("Ignore", identity.getId());
      String statusLabel = "", statusClass = "", btClass = "";
      //
      Type status = (relationship != null) ? relationship.getStatus() : null;
      //
      if (status == null) {
        label = res.getString("UIAllPeople.label.Connect");
        action = uiAllPeople.event("Connect", identity.getId());
        btClass = "btn-primary";
      } else if (status.equals(Type.PENDING)) {
        label = res.getString("UIAllPeople.label.CancelRequest");
        statusLabel = res.getString("UIAllPeople.label.InvitationSent");
      } else if (status == Type.CONFIRMED) {
        label = res.getString("UIAllPeople.label.RemoveConnection");
        statusClass = "checkedBox";
      }
      
     <%=uicomponent.event("AcceptContact")%>;return false;" >$connectLabel</button>  
*/
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    //
    Writer writer = context.getWriter();
    writer.append("<div class=\"uiRelationshipAction\" id=\"").append(getId()).append("\">");
    //
    Relationship relationship = getRelationship();
    Type status = (relationship != null) ? relationship.getStatus() : null;
    
    if(status == null) {
      writer.append("<button class=\"btn btn-primary connect-status hide-default\" onclick=\"").append(event("Connect")).append("\">")
            .append(getLabel(context, "UIBasicProfile.action.label.Connect")).append("</button>");
    } else if(status == Type.PENDING) {//PENDING
      if(relationship.getSender().equals(Utils.getOwnerIdentity())) {
        writer.append("<button class=\"btn sent-status show-default\">").append("<i class=\"uiIconSentStatus\"></i>")
              .append(getLabel(context, "UIBasicProfile.action.label.RequestSent")).append("</button>");
        writer.append("<button class=\"btn cancel-status hide-default\" onclick=\"").append(event("Cancel")).append("\">")
              .append("<i class=\"uiIconCancelStatus\"></i>")
              .append(getLabel(context, "UIBasicProfile.action.CancelRequest")).append("</button>");          
      } else {
        writer.append("<button class=\"btn sent-status show-default\">").append("<i class=\"uiIconSentStatus\"></i>")
              .append(getLabel(context, "UIBasicProfile.action.label.RequestSent")).append("</button>");
        writer.append("<button class=\"btn cancel-status hide-default\" onclick=\"").append(event("Cancel")).append("\">")
              .append("<i class=\"uiIconCancelStatus\"></i>")
              .append(getLabel(context, "UIBasicProfile.action.CancelRequest")).append("</button>");          
      }
    }
    writer.append("</div>");
    super.processRender(context);
  }

  private String getLabel(WebuiRequestContext context, String key) {
    ResourceBundle res = context.getApplicationResourceBundle();
    try {
      return res.getString(key);
    } catch (Exception e) {
      return (key.indexOf(".") > 0) ? key.substring(key.lastIndexOf(".")) : key;
    }
  }
  
  
  /**
   * Gets relationship between current user and viewer identity.<br>
   * 
   * @return relationship.
   * 
   * @throws Exception
   */
  protected Relationship getRelationship() throws Exception {
    return Utils.getRelationshipManager().get(Utils.getOwnerIdentity(), Utils.getViewerIdentity());
  }
  
  public static abstract class AbstractActionListener extends EventListener<UIRelationshipAction> {
    protected Relationship relationship = null;
    protected String msgKey = "UIRelationshipAction.label.ConnectNotExisting";
    @Override
    public void execute(Event<UIRelationshipAction> event) throws Exception {
      UIRelationshipAction uiAction = event.getSource();
      relationship = uiAction.getRelationship();
      if (!isValid()) {
        uiAction.getAncestorOfType(UIPortletApplication.class).addMessage(new ApplicationMessage(msgKey, new String[]{}, ApplicationMessage.WARNING));
        return;
      }
      //
      doAction(event, uiAction);
    }
    protected boolean isValid() {
      return (relationship != null);
    }
    protected abstract void doAction(Event<UIRelationshipAction> event, UIRelationshipAction uiAction);
  }
  
  public static class ConnectActionListener extends AbstractActionListener {
    @Override
    protected boolean isValid() {
      msgKey = "UIRelationshipAction.label.ConnectionExisted";
      return (relationship != null);
    }
    @Override
    protected void doAction(Event<UIRelationshipAction> event, UIRelationshipAction uiAction) {
      Utils.getRelationshipManager().inviteToConnect(relationship.getReceiver(), relationship.getSender());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAction.getParent());
    }
  }

  public static class CancelActionListener extends AbstractActionListener {
    @Override
    protected void doAction(Event<UIRelationshipAction> event, UIRelationshipAction uiAction) {
      Utils.getRelationshipManager().delete(relationship);
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource().getParent());
    }
  }

  public static class AcceptActionListener extends AbstractActionListener {
    @Override
    protected boolean isValid() {
      return super.isValid() || relationship.getStatus().equals(Relationship.Type.IGNORED);
    }
    @Override
    protected void doAction(Event<UIRelationshipAction> event, UIRelationshipAction uiAction) {
      Utils.getRelationshipManager().confirm(relationship.getReceiver(), relationship.getSender());
      Utils.updateWorkingWorkSpace();
    }
  }

  public static class DenyActionListener extends AbstractActionListener {
    @Override
    protected void doAction(Event<UIRelationshipAction> event, UIRelationshipAction uiAction) {
      Utils.getRelationshipManager().deny(relationship.getReceiver(), relationship.getSender());
      Utils.updateWorkingWorkSpace();
    }
  }

  public static class DisconnectActionListener extends CancelActionListener {}
}
