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

import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.ResourceBundleUtil;
import org.exoplatform.social.core.application.RelationshipPublisher.TitleId;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.social.webui.activity.UIActivitiesContainer;
import org.exoplatform.social.webui.profile.UIUserActivitiesDisplay;
import org.exoplatform.social.webui.profile.UIUserActivitiesDisplay.DisplayMode;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.lifecycle.WebuiBindingContext;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 *
 * @author    <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since     Aug 31, 2010
 * @copyright eXo SAS
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "classpath:groovy/social/webui/activity/plugin/UIRelationshipActivity.gtmpl",
  events = {
    @EventConfig(listeners = BaseUIActivity.ToggleDisplayLikesActionListener.class),
    @EventConfig(listeners = BaseUIActivity.ToggleDisplayCommentFormActionListener.class),
    @EventConfig(listeners = BaseUIActivity.LikeActivityActionListener.class),
    @EventConfig(listeners = BaseUIActivity.SetCommentListStatusActionListener.class),
    @EventConfig(listeners = BaseUIActivity.PostCommentActionListener.class),
    @EventConfig(listeners = BaseUIActivity.DeleteActivityActionListener.class, confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Activity"),
    @EventConfig(listeners = BaseUIActivity.DeleteCommentActionListener.class, confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Comment"),
    @EventConfig(listeners = UIRelationshipActivity.AcceptActionListener.class),
    @EventConfig(listeners = UIRelationshipActivity.RefuseActionListener.class),
    @EventConfig(listeners = UIRelationshipActivity.RevokeActionListener.class)
  }
)
public class UIRelationshipActivity extends BaseUIActivity {
  private static final Log LOG = ExoLogger.getLogger(UIRelationshipActivity.class);

  private TitleId titleId;

  private String senderName;

  private String receiverName;

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

  public String getSenderProfileLink() {
    return getProfileLink(senderName);
  }

  public String getReceiverProfileLink() {
    return getProfileLink(receiverName);
  }

  public boolean isActivityStreamOwner() {
    UIActivitiesContainer uiActivititesContainer = getAncestorOfType(UIActivitiesContainer.class);
    String remoteUser = Util.getPortalRequestContext().getRemoteUser();
    return remoteUser.equals(uiActivititesContainer.getOwnerName());
  }

  public String getActivityTitle(WebuiBindingContext ctx) throws Exception {
    UIUserActivitiesDisplay uiUserActivitiesDisplay = getAncestorOfType(UIUserActivitiesDisplay.class);
    DisplayMode displayMode = uiUserActivitiesDisplay.getSelectedDisplayMode();
    if (titleId == TitleId.CONNECTION_CONFIRMED) {
      if (isActivityStreamOwner() && (displayMode == DisplayMode.MY_STATUS)) {
        return ResourceBundleUtil.replaceArguments(
                                 ctx.appRes("UIRelationshipActivity.msg.You_Are_Now_Connected_With_UserName"),
                                 new String[]{getReceiverProfileLink()});
      } else {
        return ResourceBundleUtil.replaceArguments(
                                 ctx.appRes("UIRelationshipActivity.msg.UserName_Are_Now_Connected_With_UserName"),
                                 new String[]{getReceiverProfileLink(), getSenderProfileLink()});
      }
    } else if (titleId == TitleId.CONNECTION_REQUESTED) {
        if (isActivityStreamOwner() && ((displayMode == DisplayMode.MY_STATUS))) {
          return ResourceBundleUtil.replaceArguments(
                                   ctx.appRes("UIRelationshipActivity.msg.UserName_Invited_You_To_Connect"),
                                   new String[]{getSenderProfileLink()});
      } else {
        return ResourceBundleUtil.replaceArguments(
                                   ctx.appRes("UIRelationshipActivity.msg.UserName_Invited_UserName_To_Connect"),
                                   new String[]{getSenderProfileLink(), getReceiverProfileLink()});
      }
    }
    return "";
  }

  public static class AcceptActionListener extends EventListener<UIRelationshipActivity> {

    @Override
    public void execute(Event<UIRelationshipActivity> event) throws Exception {
      UIRelationshipActivity uiRelationshipActivity = event.getSource();
      IdentityManager identityManager = uiRelationshipActivity.getApplicationComponent(IdentityManager.class);
      Identity senderIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, uiRelationshipActivity.getSenderName(), false);
      Identity receiverIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, uiRelationshipActivity.getReceiverName(), false);

      RelationshipManager relationshipManager = uiRelationshipActivity.getApplicationComponent(RelationshipManager.class);
      Relationship relationship = relationshipManager.getRelationship(receiverIdentity, senderIdentity);
      Type status = relationshipManager.getRelationshipStatus(relationship, receiverIdentity);
      if (status == Type.REQUIRE_VALIDATION) {
        relationshipManager.confirm(relationship);
      }
      //delete this activity
      Event<UIComponent> deleteActivityEvent = uiRelationshipActivity.createEvent("DeleteActivity", Phase.PROCESS, event.getRequestContext());
      if (deleteActivityEvent != null) {
        deleteActivityEvent.broadcast();
      }
    }

  }

  public static class RefuseActionListener extends EventListener<UIRelationshipActivity> {

    @Override
    public void execute(Event<UIRelationshipActivity> event) throws Exception {
      UIRelationshipActivity uiRelationshipActivity = event.getSource();
      IdentityManager identityManager = uiRelationshipActivity.getApplicationComponent(IdentityManager.class);
      Identity senderIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, uiRelationshipActivity.getSenderName(), false);
      Identity receiverIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, uiRelationshipActivity.getReceiverName(), false);

      RelationshipManager relationshipManager = uiRelationshipActivity.getApplicationComponent(RelationshipManager.class);
      Relationship relationship = relationshipManager.getRelationship(receiverIdentity, senderIdentity);
      Type status = relationshipManager.getRelationshipStatus(relationship, receiverIdentity);
      if (status == Type.REQUIRE_VALIDATION) {
        relationshipManager.deny(relationship);
      }
      //delete this activity
      Event<UIComponent> deleteActivityEvent = uiRelationshipActivity.createEvent("DeleteActivity", Phase.PROCESS, event.getRequestContext());
      if (deleteActivityEvent != null) {
        deleteActivityEvent.broadcast();
      }
    }

  }

  public static class RevokeActionListener extends EventListener<UIRelationshipActivity> {

    @Override
    public void execute(Event<UIRelationshipActivity> event) throws Exception {
      // TODO Auto-generated method stub
      LOG.info("Revoke");
      System.out.println("Revoke");
    }

  }

  private String getProfileLink(String username) {
    return "<a href=" + LinkProvider.getProfileUri(username) + ">" + username +"</a>";
  }
}
