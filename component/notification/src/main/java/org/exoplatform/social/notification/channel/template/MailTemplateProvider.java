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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang.ArrayUtils;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.NotificationPluginUtils;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.processor.I18NActivityProcessor;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.notification.plugin.*;

import java.io.IOException;
import java.io.Writer;
import java.util.*;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 13, 2014  
 */
@TemplateConfigs(templates = {
    @TemplateConfig(pluginId = ActivityCommentPlugin.ID, template = "war:/notification/templates/ActivityCommentPlugin.gtmpl"),
    @TemplateConfig(pluginId = ActivityMentionPlugin.ID, template = "war:/notification/templates/ActivityMentionPlugin.gtmpl"),
    @TemplateConfig(pluginId = LikePlugin.ID, template = "war:/notification/templates/LikePlugin.gtmpl"),
    @TemplateConfig(pluginId = NewUserPlugin.ID, template = "war:/notification/templates/NewUserPlugin.gtmpl"),
    @TemplateConfig(pluginId = PostActivityPlugin.ID, template = "war:/notification/templates/PostActivityPlugin.gtmpl"),
    @TemplateConfig(pluginId = PostActivitySpaceStreamPlugin.ID, template = "war:/notification/templates/PostActivitySpaceStreamPlugin.gtmpl"),
    @TemplateConfig(pluginId = RelationshipReceivedRequestPlugin.ID, template = "war:/notification/templates/RelationshipReceivedRequestPlugin.gtmpl"),
    @TemplateConfig(pluginId = RequestJoinSpacePlugin.ID, template = "war:/notification/templates/RequestJoinSpacePlugin.gtmpl"),
    @TemplateConfig(pluginId = SpaceInvitationPlugin.ID, template = "war:/notification/templates/SpaceInvitationPlugin.gtmpl")})
public class MailTemplateProvider extends TemplateProvider {

  private static final Log LOG = ExoLogger.getLogger(MailTemplateProvider.class);

  /** Defines the template builder for ActivityCommentPlugin*/
  private AbstractTemplateBuilder comment = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();
      NotificationInfo notification = ctx.getNotificationInfo();
      String language = getLanguage(notification);

      String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
      ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
      ExoSocialActivity parentActivity = Utils.getActivityManager().getParentActivity(activity);
      Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);
      
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      templateContext.put("USER", identity.getProfile().getFullName());
      String subject = TemplateUtils.processSubject(templateContext);

      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("COMMENT", NotificationUtils.processLinkTitle(activity.getTitle()));
      templateContext.put("OPEN_URL", LinkProviderUtils.getOpenLink(activity));
      templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity_highlight_comment", parentActivity.getId() + "-" + activity.getId()));
      templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity_highlight_comment", parentActivity.getId() + "-" + activity.getId()));

      String body = SocialNotificationUtils.getBody(ctx, templateContext, parentActivity);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      return messageInfo.subject(subject).body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      List<NotificationInfo> notifications = ctx.getNotificationInfos();
      NotificationInfo first = notifications.get(0);

      String language = getLanguage(first);
      TemplateContext templateContext = new TemplateContext(first.getKey().getId(), language);
      SocialNotificationUtils.addFooterAndFirstName(first.getTo(), templateContext);
      
      //Store the activity id as key, and the list all identities who posted to the activity.
      Map<String, List<String>> receiverMap = new LinkedHashMap<String, List<String>>();
      Map<String, List<Pair<String, String>>> activityUserComments = new LinkedHashMap<String, List<Pair<String, String>>>();
      
      
      try {
        for (NotificationInfo message : notifications) {
          String activityId = message.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
          ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
          if (activity == null) {
            continue;
          }

          String poster = message.getValueOwnerParameter("poster");
          Pair<String, String> userComment = new ImmutablePair<String, String>(poster, activity.getTitle());
          ExoSocialActivity parentActivity = Utils.getActivityManager().getParentActivity(activity);
          //
          SocialNotificationUtils.processInforSendTo(receiverMap, parentActivity.getId(), poster);
          SocialNotificationUtils.processInforUserComments(activityUserComments, parentActivity.getId(), userComment);
        }
        writer.append(SocialNotificationUtils.getMessageByIds(receiverMap, activityUserComments, templateContext));
      } catch (IOException e) {
        ctx.setException(e);
        return false;
      }
      
      return true;
    }
    
  };
  
  /** Defines the template builder for ActivityMentionPlugin*/
  private AbstractTemplateBuilder mention = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();
      
      NotificationInfo notification = ctx.getNotificationInfo();
      String language = getLanguage(notification);

      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);
      
      String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
      ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
      Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);

      templateContext.put("USER", identity.getProfile().getFullName());
      String subject = TemplateUtils.processSubject(templateContext);
      
      templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(identity.getProfile()));
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("OPEN_URL", LinkProviderUtils.getOpenLink(activity));
      String body = "";
      // In case of mention on a comment, we need provide the id of the activity, not of the comment
      if (activity.isComment()) {
        ExoSocialActivity parentActivity = Utils.getActivityManager().getParentActivity(activity);
        activityId = parentActivity.getId();
        templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity_highlight_comment", activityId + "-" + activity.getId()));
        templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity_highlight_comment", activityId + "-" + activity.getId()));
        templateContext.put("ACTIVITY", NotificationUtils.processLinkTitle(getI18N(activity,new Locale(language)).getTitle()));
        body = TemplateUtils.processGroovy(templateContext);
      } else {
        templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity", activityId));
        templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activityId));
        body = SocialNotificationUtils.getBody(ctx, templateContext, getI18N(activity,new Locale(language)));
      }

      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      return messageInfo.subject(subject).body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      List<NotificationInfo> notifications = ctx.getNotificationInfos();
      NotificationInfo first = notifications.get(0);

      String language = getLanguage(first);
      TemplateContext templateContext = new TemplateContext(first.getKey().getId(), language);
      
      Map<String, List<String>> receiverMap = new LinkedHashMap<String, List<String>>();
      try {
        for (NotificationInfo notification : notifications) {
          String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
          ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
          if (activity == null) {
            continue;
          }
          Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);
          
          if (activity.isComment()) {
            activity = Utils.getActivityManager().getParentActivity(activity);
          }

          //make the list receivers who will send mail to them.
          SocialNotificationUtils.processInforSendTo(receiverMap, activity.getId(), identity.getRemoteId());
        }
        writer.append(SocialNotificationUtils.getMessageByIds(receiverMap, templateContext));
      } catch (IOException e) {
        ctx.setException(e);
        return false;
      }
      
      return true;
    }
    
  };
  
  /** Defines the template builder for LikePlugin*/
  private AbstractTemplateBuilder like = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();
      
      NotificationInfo notification = ctx.getNotificationInfo();
      
      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);
      
      String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
      ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
      Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, notification.getValueOwnerParameter("likersId"), true);
      
      templateContext.put("USER", identity.getProfile().getFullName());
      templateContext.put("SUBJECT", activity.getTitle());
      String subject = TemplateUtils.processSubject(templateContext);

      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("OPEN_URL", LinkProviderUtils.getOpenLink(activity));
      templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity", activity.getId()));
      templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activity.getId()));

      String body = SocialNotificationUtils.getBody(ctx, templateContext, activity);
      
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      return messageInfo.subject(subject).body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      List<NotificationInfo> notifications = ctx.getNotificationInfos();
      NotificationInfo first = notifications.get(0);

      String language = getLanguage(first);
      TemplateContext templateContext = new TemplateContext(first.getKey().getId(), language);
      
      Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();

      try {
        for (NotificationInfo message : notifications) {
          String activityId = message.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
          
          ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);

          //
          if (activity == null) {
            continue;
          }

          //
          String fromUser = message.getValueOwnerParameter("likersId");

          Identity identityFrom = Utils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, fromUser, false);
          if (identityFrom == null || !Arrays.asList(activity.getLikeIdentityIds()).contains(identityFrom.getId())) {
            continue;
          }
          //
          SocialNotificationUtils.processInforSendTo(map, activityId, message.getValueOwnerParameter("likersId"));
        }
        writer.append(SocialNotificationUtils.getMessageByIds(map, templateContext));
      } catch (IOException e) {
        ctx.setException(e);
        return false;
      }
      
      
      return true;
    }
    
  };
  
  /** Defines the template builder for NewUserPlugin*/
  private AbstractTemplateBuilder newUser = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();
      
      NotificationInfo notification = ctx.getNotificationInfo();
      
      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);

      String remoteId = notification.getValueOwnerParameter(SocialNotificationUtils.REMOTE_ID.getKey());
      Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId, true);
      Profile userProfile = identity.getProfile();
      
      templateContext.put("USER", userProfile.getFullName());
      templateContext.put("PORTAL_NAME", NotificationPluginUtils.getBrandingPortalName());
      templateContext.put("PORTAL_HOME", NotificationUtils.getPortalHome(NotificationPluginUtils.getBrandingPortalName()));
      String subject = TemplateUtils.processSubject(templateContext);
      
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(userProfile));
      templateContext.put("CONNECT_ACTION_URL", LinkProviderUtils.getInviteToConnectUrl(identity.getRemoteId(), notification.getTo()));
      String body = TemplateUtils.processGroovy(templateContext);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      
      return messageInfo.subject(subject).body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      List<NotificationInfo> notifications = ctx.getNotificationInfos();
      NotificationInfo first = notifications.get(0);

      String language = getLanguage(first);
      TemplateContext templateContext = new TemplateContext(first.getKey().getId(), language);
      
      Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
      try {
        for (NotificationInfo message : notifications) {
          String remoteId = message.getValueOwnerParameter(SocialNotificationUtils.REMOTE_ID.getKey());

          Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId, true);
          //
          if (identity.isDeleted() == true) {
            continue;
          }
          
          SocialNotificationUtils.processInforSendTo(map, first.getKey().getId(), remoteId);
        }
        
        String portalName = System.getProperty("exo.notifications.portalname", "eXo");
        
        templateContext.put("PORTAL_NAME", portalName);
        templateContext.put("PORTAL_HOME", SocialNotificationUtils.buildRedirecUrl("portal_home", portalName, portalName));

        writer.append(SocialNotificationUtils.getMessageByIds(map, templateContext, "new_user"));
      } catch (IOException e) {
        ctx.setException(e);
        return false;
      }
      
      return true;
    }
    
  };
  
  /** Defines the template builder for PostActivityPlugin*/
  private AbstractTemplateBuilder postActivity = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();
      
      NotificationInfo notification = ctx.getNotificationInfo();
      
      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);

      String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
      ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
      Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);
      
      
      templateContext.put("USER", identity.getProfile().getFullName());
      templateContext.put("SUBJECT", activity.getTitle());
      String subject = TemplateUtils.processSubject(templateContext);
      
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("OPEN_URL", LinkProviderUtils.getOpenLink(activity));
      templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity", activity.getId()));
      templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activity.getId()));
      
      String body = SocialNotificationUtils.getBody(ctx, templateContext, activity);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      
      return messageInfo.subject(subject).body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      List<NotificationInfo> notifications = ctx.getNotificationInfos();
      NotificationInfo first = notifications.get(0);
      String sendToUser = first.getTo();
      String language = getLanguage(first);
      
      TemplateContext templateContext = new TemplateContext(first.getKey().getId(), language);
      Map<String, List<String>> receiverMap = new LinkedHashMap<String, List<String>>();
      
      try {
        for (NotificationInfo message : notifications) {
          ExoSocialActivity activity = Utils.getActivityManager().getActivity(message.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey()));
          
          //Case of activity was deleted, ignore this notification
          if (activity == null) {
            continue;
          }
          SocialNotificationUtils.processInforSendTo(receiverMap, sendToUser, message.getValueOwnerParameter(SocialNotificationUtils.POSTER.getKey()));
        }
        writer.append(SocialNotificationUtils.getMessageByIds(receiverMap, templateContext, "user"));
      } catch (IOException e) {
        ctx.setException(e);
        return false;
      }
      return true;
    }
    
  };
  
  /** Defines the template builder for PostActivitySpaceStreamPlugin*/
  private AbstractTemplateBuilder postActivitySpace = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();
      
      NotificationInfo notification = ctx.getNotificationInfo();
      
      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);

      String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
      ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
      Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);
      
      Identity spaceIdentity = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, activity.getStreamOwner(), true);
      
      templateContext.put("USER", identity.getProfile().getFullName());
      templateContext.put("SPACE", spaceIdentity.getProfile().getFullName());
      templateContext.put("SUBJECT", activity.getTitle());
      String subject = TemplateUtils.processSubject(templateContext);
      
      Space space = Utils.getSpaceService().getSpaceByPrettyName(spaceIdentity.getRemoteId());
      templateContext.put("SPACE_URL", LinkProviderUtils.getRedirectUrl("space", space.getId()));
      templateContext.put("OPEN_URL", LinkProviderUtils.getOpenLink(activity));
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity", activity.getId()));
      templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activity.getId()));

      String body = SocialNotificationUtils.getBody(ctx, templateContext, activity);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      
      return messageInfo.subject(subject).body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      List<NotificationInfo> notifications = ctx.getNotificationInfos();
      NotificationInfo first = notifications.get(0);

      String language = getLanguage(first);
      TemplateContext templateContext = new TemplateContext(first.getKey().getId(), language);
      
      Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
      
      try {
        for (NotificationInfo message : notifications) {
          String activityId = message.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
          ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
          if (activity == null) {
            continue;
          }
          Space space = Utils.getSpaceService().getSpaceByPrettyName(activity.getStreamOwner());
          //
          SocialNotificationUtils.processInforSendTo(map, space.getId(), message.getValueOwnerParameter(SocialNotificationUtils.POSTER.getKey()));
        }
        writer.append(SocialNotificationUtils.getMessageInSpace(map, templateContext));
      } catch (IOException e) {
        ctx.setException(e);
        return false;
      }
      return true;
    }
    
  };



  /** Defines the template builder for RelationshipReceivedRequestPlugin*/
  private AbstractTemplateBuilder relationshipReceived = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();
      
      NotificationInfo notification = ctx.getNotificationInfo();
      
      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);

      String sender = notification.getValueOwnerParameter("sender");
      String toUser = notification.getTo();
      SocialNotificationUtils.addFooterAndFirstName(toUser, templateContext);
      Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, sender, true);
      Profile userProfile = identity.getProfile();
      
      templateContext.put("PORTAL_NAME", System.getProperty("exo.notifications.portalname", "eXo"));
      templateContext.put("USER", userProfile.getFullName());
      String subject = TemplateUtils.processSubject(templateContext);
      
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(userProfile));
      templateContext.put("ACCEPT_CONNECTION_REQUEST_ACTION_URL", LinkProviderUtils.getConfirmInvitationToConnectUrl(sender, toUser));
      templateContext.put("REFUSE_CONNECTION_REQUEST_ACTION_URL", LinkProviderUtils.getIgnoreInvitationToConnectUrl(sender, toUser));
      String body = TemplateUtils.processGroovy(templateContext);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      return messageInfo.subject(subject).body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      List<NotificationInfo> notifications = ctx.getNotificationInfos();
      NotificationInfo first = notifications.get(0);
      String language = getLanguage(first);
      
      TemplateContext templateContext = new TemplateContext(first.getKey().getId(), language);
      Map<String, List<String>> receiverMap = new LinkedHashMap<String, List<String>>();

      try {
        for (NotificationInfo message : notifications) {
          Relationship relationship = Utils.getRelationshipManager().get(message.getValueOwnerParameter(SocialNotificationUtils.RELATIONSHIP_ID.getKey()));
          if (relationship == null || relationship.getStatus().name().equals("PENDING") == false) {
            continue;
          }
          SocialNotificationUtils.processInforSendTo(receiverMap, first.getTo(), message.getValueOwnerParameter(SocialNotificationUtils.SENDER.getKey()));
        }
        writer.append(SocialNotificationUtils.getMessageByIds(receiverMap, templateContext, "connections_request"));
      } catch (IOException e) {
        ctx.setException(e);
        return false;
      }
      return true;
    }
    
  };
  
  /** Defines the template builder for RequestJoinSpacePlugin*/
  private AbstractTemplateBuilder requestJoinSpace = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();
      
      NotificationInfo notification = ctx.getNotificationInfo();
      
      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);

      String spaceId = notification.getValueOwnerParameter(SocialNotificationUtils.SPACE_ID.getKey());
      Space space = Utils.getSpaceService().getSpaceById(spaceId);
      Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, notification.getValueOwnerParameter("request_from"), true);
      Profile userProfile = identity.getProfile();
      
      templateContext.put("SPACE", space.getDisplayName());
      templateContext.put("USER", userProfile.getFullName());
      String subject = TemplateUtils.processSubject(templateContext);
      
      templateContext.put("SPACE_URL", LinkProviderUtils.getRedirectUrl("space_members", space.getId()));
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(userProfile));
      templateContext.put("VALIDATE_SPACE_REQUEST_ACTION_URL", LinkProviderUtils.getValidateRequestToJoinSpaceUrl(space.getId(), identity.getRemoteId()));
      templateContext.put("REFUSE_SPACE_REQUEST_ACTION_URL", LinkProviderUtils.getRefuseRequestToJoinSpaceUrl(space.getId(), identity.getRemoteId()));
      String body = TemplateUtils.processGroovy(templateContext);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      
      return messageInfo.subject(subject).body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      List<NotificationInfo> notifications = ctx.getNotificationInfos();
      NotificationInfo first = notifications.get(0);

      String language = getLanguage(first);
      TemplateContext templateContext = new TemplateContext(first.getKey().getId(), language);
      
      Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();

      try {
        for (NotificationInfo message : notifications) {
          String spaceId = message.getValueOwnerParameter(SocialNotificationUtils.SPACE_ID.getKey());
          String fromUser = message.getValueOwnerParameter("request_from");
          Space space = Utils.getSpaceService().getSpaceById(spaceId);
          if (ArrayUtils.contains(space.getPendingUsers(), fromUser) == false) {
            continue;
          }
          //
          SocialNotificationUtils.processInforSendTo(map, spaceId, fromUser);
        }
        writer.append(SocialNotificationUtils.getMessageInSpace(map, templateContext));
      } catch (IOException e) {
        ctx.setException(e);
        return false;
      }
      return true;
    }
    
  };
  
  /** Defines the template builder for SpaceInvitationPlugin*/
  private AbstractTemplateBuilder spaceInvitation = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();
      
      NotificationInfo notification = ctx.getNotificationInfo();
      
      String language = getLanguage(notification);
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);

      String spaceId = notification.getValueOwnerParameter(SocialNotificationUtils.SPACE_ID.getKey());
      Space space = Utils.getSpaceService().getSpaceById(spaceId);
      
      templateContext.put("SPACE", space.getDisplayName());
      templateContext.put("SPACE_URL", LinkProviderUtils.getRedirectUrl("space", space.getId()));
      String subject = TemplateUtils.processSubject(templateContext);
      
      templateContext.put("SPACE_AVATAR", LinkProviderUtils.getSpaceAvatarUrl(space));
      templateContext.put("ACCEPT_SPACE_INVITATION_ACTION_URL", LinkProviderUtils.getAcceptInvitationToJoinSpaceUrl(space.getId(), notification.getTo()));
      templateContext.put("REFUSE_SPACE_INVITATION_ACTION_URL", LinkProviderUtils.getIgnoreInvitationToJoinSpaceUrl(space.getId(), notification.getTo()));
      String body = TemplateUtils.processGroovy(templateContext);
      //binding the exception throws by processing template
      ctx.setException(templateContext.getException());
      
      return messageInfo.subject(subject).body(body).end();
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      List<NotificationInfo> notifications = ctx.getNotificationInfos();
      NotificationInfo first = notifications.get(0);
      String language = getLanguage(first);
      
      TemplateContext templateContext = new TemplateContext(first.getKey().getId(), language);
      Map<String, List<String>> receiverMap = new LinkedHashMap<String, List<String>>();
      
      try {
        for (NotificationInfo message : notifications) {
          String spaceId = message.getValueOwnerParameter(SocialNotificationUtils.SPACE_ID.getKey());
          Space space = Utils.getSpaceService().getSpaceById(spaceId);
          if (ArrayUtils.contains(space.getInvitedUsers(), first.getTo()) == false) {
            continue;
          }

          SocialNotificationUtils.processInforSendTo(receiverMap, first.getTo(), spaceId);
        }
        writer.append(SocialNotificationUtils.getMessageByIds(receiverMap, templateContext, "space"));
      } catch (IOException e) {
        ctx.setException(e);
        return false;
      }
      
      return true;
    }
    
  };

  protected ExoSocialActivity getI18N(ExoSocialActivity activity,Locale locale) {

    I18NActivityProcessor i18NActivityProcessor =(I18NActivityProcessor) PortalContainer.getInstance().getComponentInstanceOfType(I18NActivityProcessor.class);
    if (activity.getTitleId() != null) {
      activity = i18NActivityProcessor.process(activity, locale);
    }
    return activity;
  }
  
  public MailTemplateProvider(InitParams initParams) {
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
