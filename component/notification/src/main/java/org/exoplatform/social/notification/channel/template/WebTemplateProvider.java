/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.social.notification.channel.template;

import java.io.Writer;
import java.util.Calendar;
import java.util.Locale;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.NotificationPluginUtils;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.notification.plugin.ActivityCommentPlugin;
import org.exoplatform.social.notification.plugin.ActivityMentionPlugin;
import org.exoplatform.social.notification.plugin.LikePlugin;
import org.exoplatform.social.notification.plugin.NewUserPlugin;
import org.exoplatform.social.notification.plugin.PostActivityPlugin;
import org.exoplatform.social.notification.plugin.PostActivitySpaceStreamPlugin;
import org.exoplatform.social.notification.plugin.RelationshipReceivedRequestPlugin;
import org.exoplatform.social.notification.plugin.RequestJoinSpacePlugin;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;
import org.exoplatform.social.notification.plugin.SpaceInvitationPlugin;
import org.exoplatform.webui.utils.TimeConvertUtils;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 14, 2014  
 */
public class WebTemplateProvider extends TemplateProvider {
  private static final String ACCEPT_INVITATION_TO_CONNECT = "social/intranet-notification/confirmInvitationToConnect";
  private static final String REFUSE_INVITATION_TO_CONNECT = "social/intranet-notification/ignoreInvitationToConnect";
  private static final String VALIDATE_SPACE_REQUEST = "social/intranet-notification/validateRequestToJoinSpace";
  private static final String REFUSE_SPACE_REQUEST = "social/intranet-notification/refuseRequestToJoinSpace";
  private static final String ACCEPT_SPACE_INVITATION = "social/intranet-notification/acceptInvitationToJoinSpace";
  private static final String REFUSE_SPACE_INVITATION = "social/intranet-notification/ignoreInvitationToJoinSpace";
  
  /** Defines the template builder for ActivityCommentPlugin*/
  private AbstractTemplateBuilder comment = new AbstractTemplateBuilder() {

    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      NotificationInfo notification = ctx.getNotificationInfo();
      String language = getLanguage(notification);

      String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
      ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
      ExoSocialActivity parentActivity = Utils.getActivityManager().getParentActivity(activity);
      Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);
      
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      templateContext.put("isIntranet", "true");
        Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(notification.getLastModifiedDate());
      templateContext.put("READ", Boolean.valueOf(notification.getValueOwnerParameter(AbstractService.NTF_READ)) ? "read" : "unread");
      templateContext.put("NOTIFICATION_ID", notification.getId());
      templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgo(cal.getTime(), "EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));
      templateContext.put("USER", identity.getProfile().getFullName());
      templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(identity.getProfile()));
      templateContext.put("ACTIVITY", NotificationUtils.removeLinkTitle(parentActivity.getTitle()));
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity_highlight_comment", parentActivity.getId() + "-" + activity.getId()));
      String body = TemplateUtils.processGroovy(templateContext);
      
      MessageInfo messageInfo = new MessageInfo();
      return messageInfo.body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }
    
  };
  
  /** Defines the template builder for ActivityMentionPlugin*/
  private AbstractTemplateBuilder mention = new AbstractTemplateBuilder() {

    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      NotificationInfo notification = ctx.getNotificationInfo();
      String language = getLanguage(notification);

      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      
      String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
      ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
      Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);
      templateContext.put("isIntranet", "true");
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(notification.getLastModifiedDate());
      templateContext.put("READ", Boolean.valueOf(notification.getValueOwnerParameter(AbstractService.NTF_READ)) ? "read" : "unread");
      templateContext.put("NOTIFICATION_ID", notification.getId());
      templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgo(cal.getTime(), "EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));
      templateContext.put("USER", identity.getProfile().getFullName());
      templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(identity.getProfile()));
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("ACTIVITY", NotificationUtils.removeLinkTitle(activity.getTitle()));
      
      // In case of mention on a comment, we need provide the id of the activity, not of the comment
      if (activity.isComment()) {
        ExoSocialActivity parentActivity = Utils.getActivityManager().getParentActivity(activity);
        activityId = parentActivity.getId();
        templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity_highlight_comment", activityId + "-" + activity.getId()));
      } else {
        templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activityId));
      }
      //
      String body = TemplateUtils.processGroovy(templateContext);
      MessageInfo messageInfo = new MessageInfo();
      return messageInfo.body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }
    
  };
  
  /** Defines the template builder for LikePlugin*/
  private AbstractTemplateBuilder like = new AbstractTemplateBuilder() {

    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      NotificationInfo notification = ctx.getNotificationInfo();
      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      
      String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
      ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
      Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, notification.getValueOwnerParameter("likersId"), true);
      templateContext.put("isIntranet", "true");
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(notification.getLastModifiedDate());
      templateContext.put("READ", Boolean.valueOf(notification.getValueOwnerParameter(AbstractService.NTF_READ)) ? "read" : "unread");
      templateContext.put("NOTIFICATION_ID", notification.getId());
      templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgo(cal.getTime(), "EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));
      templateContext.put("USER", identity.getProfile().getFullName());
      templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(identity.getProfile()));
      templateContext.put("ACTIVITY", NotificationUtils.removeLinkTitle(activity.getTitle()));
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activity.getId()));
      //
      String body = TemplateUtils.processGroovy(templateContext);
      MessageInfo messageInfo = new MessageInfo();
      return messageInfo.body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }
   
  };
  
  /** Defines the template builder for NewUserPlugin*/
  private AbstractTemplateBuilder newUser = new AbstractTemplateBuilder() {

    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      NotificationInfo notification = ctx.getNotificationInfo();
      
      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);

      String remoteId = notification.getValueOwnerParameter(SocialNotificationUtils.REMOTE_ID.getKey());
      Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId, true);
      Profile userProfile = identity.getProfile();
      templateContext.put("isIntranet", "true");
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(notification.getLastModifiedDate());
      templateContext.put("READ", Boolean.valueOf(notification.getValueOwnerParameter(AbstractService.NTF_READ)) ? "read" : "unread");
      templateContext.put("NOTIFICATION_ID", notification.getId());
      templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgo(TimeConvertUtils.getGreenwichMeanTime().getTime(), "EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));
      templateContext.put("USER", userProfile.getFullName());
      templateContext.put("PORTAL_NAME", NotificationPluginUtils.getBrandingPortalName());
      //templateContext.put("PORTAL_HOME", NotificationUtils.getPortalHome(NotificationPluginUtils.getBrandingPortalName()));
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(userProfile));
      //
      String body = TemplateUtils.processGroovy(templateContext);
      MessageInfo messageInfo = new MessageInfo();
      return messageInfo.body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }
    
    
  };
  
  /** Defines the template builder for PostActivityPlugin*/
  private AbstractTemplateBuilder postActivity = new AbstractTemplateBuilder() {

    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      NotificationInfo notification = ctx.getNotificationInfo();
      
      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);

      String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
      ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
      Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);
      templateContext.put("isIntranet", "true");
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(notification.getLastModifiedDate());
      templateContext.put("READ", Boolean.valueOf(notification.getValueOwnerParameter(AbstractService.NTF_READ)) ? "read" : "unread");
      templateContext.put("NOTIFICATION_ID", notification.getId());
      templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgo(cal.getTime(), "EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));
      templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(identity.getProfile()));
      templateContext.put("ACTIVITY", NotificationUtils.removeLinkTitle(activity.getTitle()));
      templateContext.put("USER", identity.getProfile().getFullName());
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activity.getId()));
      //
      String body = TemplateUtils.processGroovy(templateContext);
      MessageInfo messageInfo = new MessageInfo();
      return messageInfo.body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }
    
    
  };
  
  /** Defines the template builder for PostActivitySpaceStreamPlugin*/
  private AbstractTemplateBuilder postActivitySpace = new AbstractTemplateBuilder() {

    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      NotificationInfo notification = ctx.getNotificationInfo();
      
      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);

      String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
      ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
      Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);
      
      Identity spaceIdentity = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, activity.getStreamOwner(), true);
      Space space = Utils.getSpaceService().getSpaceByPrettyName(spaceIdentity.getRemoteId());
      templateContext.put("isIntranet", "true");
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(notification.getLastModifiedDate());
      templateContext.put("READ", Boolean.valueOf(notification.getValueOwnerParameter(AbstractService.NTF_READ)) ? "read" : "unread");
      templateContext.put("NOTIFICATION_ID", notification.getId());
      templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgo(cal.getTime(), "EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));
      templateContext.put("USER", identity.getProfile().getFullName());
      templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(identity.getProfile()));
      templateContext.put("ACTIVITY", NotificationUtils.removeLinkTitle(activity.getTitle()));
      templateContext.put("SPACE", spaceIdentity.getProfile().getFullName());
      templateContext.put("SPACE_URL", LinkProviderUtils.getRedirectUrl("space", space.getId()));
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activity.getId()));
      //
      String body = TemplateUtils.processGroovy(templateContext);
      MessageInfo messageInfo = new MessageInfo();
      return messageInfo.body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }
    
    
  };
  
  /** Defines the template builder for RelationshipReceivedRequestPlugin*/
  private AbstractTemplateBuilder relationshipReceived = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      NotificationInfo notification = ctx.getNotificationInfo();
      
      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);

      String sender = notification.getValueOwnerParameter("sender");
      String status = notification.getValueOwnerParameter("status");
      String toUser = notification.getTo();
      Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, sender, true);
      Profile userProfile = identity.getProfile();
      templateContext.put("isIntranet", "true");
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(notification.getLastModifiedDate());
      templateContext.put("READ", Boolean.valueOf(notification.getValueOwnerParameter(AbstractService.NTF_READ)) ? "read" : "unread");
      templateContext.put("NOTIFICATION_ID", notification.getId());
      templateContext.put("STATUS", status != null && status.equals("accepted") ? "ACCEPTED" : "PENDING");
      templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgo(cal.getTime(), "EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));
      templateContext.put("USER", userProfile.getFullName());
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(userProfile));
      templateContext.put("ACCEPT_CONNECTION_REQUEST_ACTION_URL", LinkProviderUtils.getRestUrl(ACCEPT_INVITATION_TO_CONNECT, sender, toUser));
      templateContext.put("REFUSE_CONNECTION_REQUEST_ACTION_URL", LinkProviderUtils.getRestUrl(REFUSE_INVITATION_TO_CONNECT, sender, toUser));
      //
      String body = TemplateUtils.processGroovy(templateContext);
      MessageInfo messageInfo = new MessageInfo();
      return messageInfo.body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }
    
  };
  
  /** Defines the template builder for RequestJoinSpacePlugin*/
  private AbstractTemplateBuilder requestJoinSpace = new AbstractTemplateBuilder() {

    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      NotificationInfo notification = ctx.getNotificationInfo();
      
      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);

      String status = notification.getValueOwnerParameter("status");
      String spaceId = notification.getValueOwnerParameter(SocialNotificationUtils.SPACE_ID.getKey());
      Space space = Utils.getSpaceService().getSpaceById(spaceId);
      Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, notification.getValueOwnerParameter("request_from"), true);
      Profile userProfile = identity.getProfile();
      templateContext.put("isIntranet", "true");
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(notification.getLastModifiedDate());
      templateContext.put("READ", Boolean.valueOf(notification.getValueOwnerParameter(AbstractService.NTF_READ)) ? "read" : "unread");
      templateContext.put("STATUS", status != null && status.equals("accepted") ? "ACCEPTED" : "PENDING");
      templateContext.put("NOTIFICATION_ID", notification.getId());
      templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgo(cal.getTime(), "EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));
      templateContext.put("SPACE", space.getDisplayName());
      templateContext.put("USER", userProfile.getFullName());
      templateContext.put("SPACE_URL", LinkProviderUtils.getRedirectUrl("space_members", space.getId()));
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(userProfile));
      templateContext.put("VALIDATE_SPACE_REQUEST_ACTION_URL", LinkProviderUtils.getRestUrl(VALIDATE_SPACE_REQUEST, space.getId(), identity.getRemoteId()));
      templateContext.put("REFUSE_SPACE_REQUEST_ACTION_URL", LinkProviderUtils.getRestUrl(REFUSE_SPACE_REQUEST, space.getId(), identity.getRemoteId()));
      //
      String body = TemplateUtils.processGroovy(templateContext);
      MessageInfo messageInfo = new MessageInfo();
      return messageInfo.body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }
    
    
  };
  
  /** Defines the template builder for SpaceInvitationPlugin*/
  private AbstractTemplateBuilder spaceInvitation = new AbstractTemplateBuilder() {

    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      NotificationInfo notification = ctx.getNotificationInfo();
      
      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);

      String status = notification.getValueOwnerParameter("status");
      String spaceId = notification.getValueOwnerParameter(SocialNotificationUtils.SPACE_ID.getKey());
      Space space = Utils.getSpaceService().getSpaceById(spaceId);
      templateContext.put("isIntranet", "true");
      Calendar cal = Calendar.getInstance();
      cal.setTimeInMillis(notification.getLastModifiedDate());
      templateContext.put("READ", Boolean.valueOf(notification.getValueOwnerParameter(AbstractService.NTF_READ)) ? "read" : "unread");
      templateContext.put("STATUS", status != null && status.equals("accepted") ? "ACCEPTED" : "PENDING");
      templateContext.put("NOTIFICATION_ID", notification.getId());
      templateContext.put("LAST_UPDATED_TIME", TimeConvertUtils.convertXTimeAgo(cal.getTime(), "EE, dd yyyy", new Locale(language), TimeConvertUtils.YEAR));
      templateContext.put("SPACE", space.getDisplayName());
      templateContext.put("SPACE_URL", LinkProviderUtils.getRedirectUrl("space", space.getId()));
      templateContext.put("SPACE_AVATAR", LinkProviderUtils.getSpaceAvatarUrl(space));
      templateContext.put("ACCEPT_SPACE_INVITATION_ACTION_URL", LinkProviderUtils.getRestUrl(ACCEPT_SPACE_INVITATION, space.getId(), notification.getTo()));
      templateContext.put("REFUSE_SPACE_INVITATION_ACTION_URL", LinkProviderUtils.getRestUrl(REFUSE_SPACE_INVITATION, space.getId(), notification.getTo()));
      //
      String body = TemplateUtils.processGroovy(templateContext);
      MessageInfo messageInfo = new MessageInfo();
      return messageInfo.body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      return false;
    }
    
    
  };
  
  public WebTemplateProvider(InitParams initParams) {
    super(initParams);
    this.templateBuilders.put(PluginKey.key(ActivityCommentPlugin.ID), comment);
    this.templateBuilders.put(PluginKey.key(ActivityMentionPlugin.ID), mention);
    this.templateBuilders.put(PluginKey.key(LikePlugin.ID), like);
    this.templateBuilders.put(PluginKey.key(NewUserPlugin.ID), newUser);
    this.templateBuilders.put(PluginKey.key(PostActivityPlugin.ID), postActivity);
    this.templateBuilders.put(PluginKey.key(PostActivitySpaceStreamPlugin.ID), postActivitySpace);
    this.templateBuilders.put(PluginKey.key(RelationshipReceivedRequestPlugin.ID), relationshipReceived);
    this.templateBuilders.put(PluginKey.key(RequestJoinSpacePlugin.ID), requestJoinSpace);
    this.templateBuilders.put(PluginKey.key(SpaceInvitationPlugin.ID), spaceInvitation);
  }

}
