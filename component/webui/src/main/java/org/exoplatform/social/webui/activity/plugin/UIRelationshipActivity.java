/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.social.webui.activity.plugin;

import org.exoplatform.social.common.ResourceBundleUtil;
import org.exoplatform.social.core.application.RelationshipPublisher.TitleId;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.social.webui.activity.UIActivitiesContainer;
import org.exoplatform.social.webui.profile.UIUserActivitiesDisplay;
import org.exoplatform.social.webui.profile.UIUserActivitiesDisplay.DisplayMode;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.lifecycle.WebuiBindingContext;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 *
 * @author    <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since     Aug 31, 2010
 * @copyright eXo SAS
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "war:/groovy/social/webui/activity/plugin/UIRelationshipActivity.gtmpl",
  events = {
    @EventConfig(listeners = BaseUIActivity.LoadLikesActionListener.class),
    @EventConfig(listeners = BaseUIActivity.ToggleDisplayCommentFormActionListener.class),
    @EventConfig(listeners = BaseUIActivity.LikeActivityActionListener.class),
    @EventConfig(listeners = BaseUIActivity.SetCommentListStatusActionListener.class),
    @EventConfig(listeners = BaseUIActivity.PostCommentActionListener.class),
    @EventConfig(listeners = BaseUIActivity.DeleteActivityActionListener.class),
    @EventConfig(listeners = BaseUIActivity.DeleteCommentActionListener.class),
    @EventConfig(listeners = UIRelationshipActivity.AcceptActionListener.class),
    @EventConfig(listeners = UIRelationshipActivity.RefuseActionListener.class),
    @EventConfig(listeners = UIRelationshipActivity.RevokeActionListener.class)
 }
)
public class UIRelationshipActivity extends BaseUIActivity {
  private TitleId titleId;

  private String senderName;

  private String receiverName;

  private String relationshipUUID;

  private Relationship relationship;

  private Identity sender;

  private Identity receiver;

  private Type status;

  public UIRelationshipActivity() {

  }

  public void setTitleId(TitleId titleId) {
    this.titleId = titleId;
  }

  public TitleId getTitleId() {
    return titleId;
  }

  public void setSenderName(String senderName) {
    this.senderName = senderName;
  }

  public String getSenderName() {
    return senderName;
  }

  public void setReceiverName(String receiverName) {
    this.receiverName = receiverName;
  }

  public String getReceiverName() {
    return receiverName;
  }

  public void setSender(Identity sender) {
    this.sender = sender;
  }

  public Identity getSender() {
    if (sender == null) {
      sender = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, senderName, false);
    }
    return sender;
  }

  public void setReceiver(Identity receiver) {
    this.receiver = receiver;
  }

  public Identity getReceiver() {
    if (receiver == null) {
      receiver = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, receiverName, false);
    }
    return receiver;
  }

  public void setStatus(Relationship.Type status) {
    this.status = status;
  }

  public Type getStatus() throws Exception {
    if (status == null) {
      status = Utils.getRelationshipManager().getStatus(getSender(), getReceiver());
    }
    return status;
  }

  public void setRelationship(Relationship relationship) {
    this.relationship = relationship;
  }

  public Relationship getRelationship() throws Exception {
    if (relationship == null) {
      relationship = Utils.getRelationshipManager().get(relationshipUUID);
    }
    return relationship;
  }

  public boolean isActivityStreamOwner() {
    UIActivitiesContainer uiActivititesContainer = getAncestorOfType(UIActivitiesContainer.class);
    return Utils.getViewerRemoteId().equals(uiActivititesContainer.getOwnerName());
  }

  public boolean isSender() throws Exception {
    return Utils.getViewerRemoteId().equals(senderName);
  }

  public boolean isReceiver() throws Exception {
    return Utils.getViewerRemoteId().equals(receiverName);
  }

  public String getActivityTitle(WebuiBindingContext ctx) throws Exception {
    UIUserActivitiesDisplay uiUserActivitiesDisplay = getAncestorOfType(UIUserActivitiesDisplay.class);
    if (uiUserActivitiesDisplay == null) {
      return null;
    }
    DisplayMode displayMode = uiUserActivitiesDisplay.getSelectedDisplayMode();
    String senderLink = LinkProvider.getProfileLink(senderName);
    String receiverLink = LinkProvider.getProfileLink(receiverName);

    if (titleId == TitleId.CONNECTION_CONFIRMED) {
      if ((isActivityStreamOwner() && (displayMode == DisplayMode.MY_ACTIVITIES))
          || Utils.getViewerRemoteId().equals(getOwnerIdentity().getRemoteId())) {
        if(isSender()) {
          return ResourceBundleUtil.
                  replaceArguments(ctx.appRes("UIRelationshipActivity.msg.You_Are_Now_Connected_With_UserName"),
                                                     new String[] { receiverLink });
        } else {
          return ResourceBundleUtil.
                  replaceArguments(ctx.appRes("UIRelationshipActivity.msg.You_Are_Now_Connected_With_UserName"),
                                                     new String[] { senderLink });
        }
      } else {
        return ResourceBundleUtil.
              replaceArguments(ctx.appRes("UIRelationshipActivity.msg.UserName_Are_Now_Connected_With_UserName"),
                                           new String[] { receiverLink, senderLink });
      }
    } else if (titleId == TitleId.CONNECTION_REQUESTED) {
      if (isActivityStreamOwner() && ((displayMode == DisplayMode.MY_ACTIVITIES))) {
        if(isSender()) {
          return ResourceBundleUtil.
                  replaceArguments(ctx.appRes("UIRelationshipActivity.msg.You_Invited_UserName_To_Connect"),
                                                     new String[] { receiverLink });
        } else {
          return ResourceBundleUtil.
                  replaceArguments(ctx.appRes("UIRelationshipActivity.msg.UserName_Invited_You_To_Connect"),
                                             new String[] { senderLink });
        }
      } else {
          if (Utils.getViewerRemoteId().equals(getOwnerIdentity().getRemoteId())) {
            if(isSender()) {
              return ResourceBundleUtil.
                      replaceArguments(ctx.appRes("UIRelationshipActivity.msg.You_Invited_UserName_To_Connect"),
                                                         new String[] { receiverLink });
            } else {  
              return ResourceBundleUtil.
                  replaceArguments(ctx.appRes("UIRelationshipActivity.msg.UserName_Invited_You_To_Connect"),
                                         new String[] { senderLink });
            }
          } else {
            if (isReceiver()) {
              return ResourceBundleUtil.
                  replaceArguments(ctx.appRes("UIRelationshipActivity.msg.UserName_Invited_UserName_To_Connect"),
                                                 new String[] { senderLink, receiverLink });
            } else {
              return ResourceBundleUtil.
                  replaceArguments(ctx.appRes("UIRelationshipActivity.msg.UserName_Invited_UserName_To_Connect"),
                                                 new String[] { senderLink, receiverLink });
          }
        } 
      }
    }
    return "";
  }

  public void setRelationshipUUID(String relationshipUUID) {
    this.relationshipUUID = relationshipUUID;
  }

  public String getRelationshipUUID() {
    return relationshipUUID;
  }

  public static class AcceptActionListener extends EventListener<UIRelationshipActivity> {

    @Override
    public void execute(Event<UIRelationshipActivity> event) throws Exception {
      UIRelationshipActivity uiRelationshipActivity = event.getSource();

      Relationship relationship = uiRelationshipActivity.getRelationship();
      if (relationship != null && relationship.getStatus() == Type.PENDING) {
        Utils.getRelationshipManager().confirm(relationship);
        Utils.updateWorkingWorkSpace();
      }
    }
  }

  public static class RefuseActionListener extends EventListener<UIRelationshipActivity> {

    @Override
    public void execute(Event<UIRelationshipActivity> event) throws Exception {
      UIRelationshipActivity uiRelationshipActivity = event.getSource();
      Relationship relationship = uiRelationshipActivity.getRelationship();
      if (relationship != null && relationship.getStatus() == Type.PENDING) {
        Utils.getRelationshipManager().deny(relationship);
        Utils.updateWorkingWorkSpace();
      }
    }

  }

  public static class RevokeActionListener extends EventListener<UIRelationshipActivity> {

    @Override
    public void execute(Event<UIRelationshipActivity> event) throws Exception {
    }
  }
}
