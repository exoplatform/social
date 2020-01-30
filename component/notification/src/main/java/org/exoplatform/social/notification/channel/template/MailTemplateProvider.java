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

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

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

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 13, 2014  
 */
@TemplateConfigs(templates = {
    @TemplateConfig(pluginId = ActivityReplyToCommentPlugin.ID, template = "war:/notification/templates/ActivityReplyToCommentPlugin.gtmpl"),
    @TemplateConfig(pluginId = ActivityCommentPlugin.ID, template = "war:/notification/templates/ActivityCommentPlugin.gtmpl"),
    @TemplateConfig(pluginId = EditActivityPlugin.ID, template = "war:/notification/templates/EditActivityPlugin.gtmpl"),
    @TemplateConfig(pluginId = EditCommentPlugin.ID, template = "war:/notification/templates/EditCommentPlugin.gtmpl"),
    @TemplateConfig(pluginId = ActivityMentionPlugin.ID, template = "war:/notification/templates/ActivityMentionPlugin.gtmpl"),
    @TemplateConfig(pluginId = LikePlugin.ID, template = "war:/notification/templates/LikePlugin.gtmpl"),
    @TemplateConfig(pluginId = LikeCommentPlugin.ID, template = "war:/notification/templates/LikeCommentPlugin.gtmpl"),
    @TemplateConfig(pluginId = NewUserPlugin.ID, template = "war:/notification/templates/NewUserPlugin.gtmpl"),
    @TemplateConfig(pluginId = PostActivityPlugin.ID, template = "war:/notification/templates/PostActivityPlugin.gtmpl"),
    @TemplateConfig(pluginId = PostActivitySpaceStreamPlugin.ID, template = "war:/notification/templates/PostActivitySpaceStreamPlugin.gtmpl"),
    @TemplateConfig(pluginId = RelationshipReceivedRequestPlugin.ID, template = "war:/notification/templates/RelationshipReceivedRequestPlugin.gtmpl"),
    @TemplateConfig(pluginId = RequestJoinSpacePlugin.ID, template = "war:/notification/templates/RequestJoinSpacePlugin.gtmpl"),
    @TemplateConfig(pluginId = SpaceInvitationPlugin.ID, template = "war:/notification/templates/SpaceInvitationPlugin.gtmpl")})
public class MailTemplateProvider extends TemplateProvider {

  private static final Log LOG = ExoLogger.getLogger(MailTemplateProvider.class);

  /** Defines the template builder for ActivityReplyToCommentPlugin*/
  private AbstractTemplateBuilder replyToComment = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();
      NotificationInfo notification = ctx.getNotificationInfo();
      String language = getLanguage(notification);

      String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
      String commentId = notification.getValueOwnerParameter(SocialNotificationUtils.COMMENT_ID.getKey());
      ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
      ExoSocialActivity commentActivity = Utils.getActivityManager().getActivity(commentId);
      ExoSocialActivity parentCommentActivity = Utils.getActivityManager().getActivity(commentActivity.getParentCommentId());
      Identity identity = Utils.getIdentityManager().getIdentity(commentActivity.getPosterId(), true);
      
      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      templateContext.put("USER", identity.getProfile().getFullName());
      String subject = TemplateUtils.processSubject(templateContext);

      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      String imagePlaceHolder = SocialNotificationUtils.getImagePlaceHolder(language);
      String subCommentTitle = SocialNotificationUtils.processImageTitle(commentActivity.getTitle(), imagePlaceHolder);
      String commentTitle = SocialNotificationUtils.processImageTitle(parentCommentActivity.getTitle(), imagePlaceHolder);
      templateContext.put("COMMENT_REPLY", NotificationUtils.processLinkTitle(subCommentTitle));
      templateContext.put("COMMENT", NotificationUtils.processLinkTitle(commentTitle));
      templateContext.put("OPEN_URL", LinkProviderUtils.getOpenLink(activity));
      templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity_highlight_comment_reply", activity.getId() + "-" + parentCommentActivity.getId() + "-" + commentActivity.getId()));
      templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity_highlight_comment_reply", activity.getId() + "-" + parentCommentActivity.getId() + "-" + commentActivity.getId()));

      String body = SocialNotificationUtils.getBody(ctx, templateContext, parentCommentActivity);
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
        String imagePlaceHolder = SocialNotificationUtils.getImagePlaceHolder(language);
        for (NotificationInfo message : notifications) {
          String commentId = message.getValueOwnerParameter(SocialNotificationUtils.COMMENT_ID.getKey());
          ExoSocialActivity commentActivity = Utils.getActivityManager().getActivity(commentId);
          if (commentActivity == null || commentActivity.getParentCommentId() == null) {
            continue;
          }

          ExoSocialActivity parentCommentActivity = Utils.getActivityManager().getActivity(commentActivity.getParentCommentId());
          if (!parentCommentActivity.isComment()) {
            continue;
          }
          Identity identity = Utils.getIdentityManager().getIdentity(parentCommentActivity.getPosterId(), true);
          if (identity == null || StringUtils.isBlank(message.getTo()) || !message.getTo().equals(identity.getRemoteId())) {
            continue;
          }
          String poster = message.getValueOwnerParameter("poster");
          String title = SocialNotificationUtils.processImageTitle(commentActivity.getTitle(), imagePlaceHolder);
          Pair<String, String> userComment = new ImmutablePair<String, String>(poster, title);
          ExoSocialActivity parentActivity = Utils.getActivityManager().getParentActivity(commentActivity);
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
  
  /** Defines the template builder for ActivityCommentPlugin*/
  private AbstractTemplateBuilder comment = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      MessageInfo messageInfo = new MessageInfo();
      NotificationInfo notification = ctx.getNotificationInfo();
      String language = getLanguage(notification);

      String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
      String commentId = notification.getValueOwnerParameter(SocialNotificationUtils.COMMENT_ID.getKey());
      ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
      ExoSocialActivity comment = null;
      if (StringUtils.isNotBlank(commentId)) {
        comment = Utils.getActivityManager().getActivity(commentId);
      }
      if (activity == null) {
        LOG.debug("Activity with id '{}' was removed but the notification with id'{}' is remaining", activityId, notification.getId());
        return null;
      }
      if(activity.isComment()) {
        comment = Utils.getActivityManager().getParentActivity(activity);
      }
      if (comment == null) {
        LOG.debug("Comment of activity with id '{}' was removed but the notification with id'{}' is remaining", commentId, notification.getId());
        return null;
      }
      Identity identity = Utils.getIdentityManager().getIdentity(comment.getPosterId(), true);

      TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
      templateContext.put("USER", identity.getProfile().getFullName());
      String subject = TemplateUtils.processSubject(templateContext);

      SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);
      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));

      String imagePlaceHolder = SocialNotificationUtils.getImagePlaceHolder(language);
      String title = SocialNotificationUtils.processImageTitle(comment.getTitle(), imagePlaceHolder);
      templateContext.put("COMMENT", NotificationUtils.processLinkTitle(title));
      templateContext.put("OPEN_URL", LinkProviderUtils.getOpenLink(comment));
      templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity_highlight_comment", activity.getId() + "-" + comment.getId()));
      templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity_highlight_comment", activity.getId() + "-" + comment.getId()));

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
      SocialNotificationUtils.addFooterAndFirstName(first.getTo(), templateContext);

      //Store the activity id as key, and the list all identities who posted to the activity.
      Map<String, List<String>> receiverMap = new LinkedHashMap<String, List<String>>();
      Map<String, List<Pair<String, String>>> activityUserComments = new LinkedHashMap<String, List<Pair<String, String>>>();


      try {
        String imagePlaceHolder = SocialNotificationUtils.getImagePlaceHolder(language);
        for (NotificationInfo message : notifications) {
          String activityId = message.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
          ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
          if (activity == null) {
            continue;
          }
          String commentId = message.getValueOwnerParameter(SocialNotificationUtils.COMMENT_ID.getKey());
          ExoSocialActivity parentActivity = null;
          if(StringUtils.isBlank(commentId)) {
            LOG.warn("Attempt to send a mail message with id '{}' and receiver '{}' with empty parameter 'commentId' and activityId = '{}' ",
                     message.getId(),
                     message.getTo(),
                     activityId);
          } else {
            parentActivity = activity;
            activity = Utils.getActivityManager().getActivity(commentId);
            if (activity == null) {
              continue;
            }
          }

          String poster = message.getValueOwnerParameter("poster");
          if(message.getTo() != null && poster != null && poster.equals(message.getTo())) {
            continue;
          }
          String title = SocialNotificationUtils.processImageTitle(activity.getTitle(), imagePlaceHolder);
          Pair<String, String> userComment = new ImmutablePair<String, String>(poster, title);
          if (parentActivity.getStreamOwner() != null) {
            Identity spaceIdentity = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, parentActivity.getStreamOwner(), true);
            if (spaceIdentity == null) {
              if (message.getTo()!=null && !message.getTo().equals(parentActivity.getStreamOwner())) {
                continue;
              }
            } else if (parentActivity.getPosterId() != null) {
              Identity identity = Utils.getIdentityManager().getIdentity(parentActivity.getPosterId(), true);
              if (identity != null) {
                if (message.getTo() != null && !message.getTo().equals(identity.getRemoteId())) {
                  continue;
                }
              }
            }
          }
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

  /** Defines the template builder for EditCommentPlugin*/
  private AbstractTemplateBuilder editComment = new AbstractTemplateBuilder() {
    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
        MessageInfo messageInfo = new MessageInfo();
        NotificationInfo notification = ctx.getNotificationInfo();
        String language = getLanguage(notification);

        String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
        String commentId = notification.getValueOwnerParameter(SocialNotificationUtils.COMMENT_ID.getKey());
        ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
        ExoSocialActivity comment = null;
        if (StringUtils.isNotBlank(commentId)) {
            comment = Utils.getActivityManager().getActivity(commentId);
        }
        if (activity == null) {
            LOG.debug("Activity with id '{}' was removed but the notification with id'{}' is remaining", activityId, notification.getId());
            return null;
        }
        if(activity.isComment()) {
            comment = Utils.getActivityManager().getParentActivity(activity);
        }
        if (comment == null) {
            LOG.debug("Comment of activity with id '{}' was removed but the notification with id'{}' is remaining", commentId, notification.getId());
            return null;
        }
        Identity identity = Utils.getIdentityManager().getIdentity(comment.getPosterId(), true);

        TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
        templateContext.put("USER", identity.getProfile().getFullName());
        String subject = TemplateUtils.processSubject(templateContext);

        SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);
        templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));

        String imagePlaceHolder = SocialNotificationUtils.getImagePlaceHolder(language);
        String title = SocialNotificationUtils.processImageTitle(comment.getTitle(), imagePlaceHolder);
        templateContext.put("COMMENT", NotificationUtils.processLinkTitle(title));
        templateContext.put("OPEN_URL", LinkProviderUtils.getOpenLink(comment));
        templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity_highlight_comment", activity.getId() + "-" + comment.getId()));
        templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity_highlight_comment", activity.getId() + "-" + comment.getId()));

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
      SocialNotificationUtils.addFooterAndFirstName(first.getTo(), templateContext);

      //Store the activity id as key, and the list all identities who posted to the activity.
      Map<String, List<String>> receiverMap = new LinkedHashMap<String, List<String>>();
      Map<String, List<Pair<String, String>>> activityUserComments = new LinkedHashMap<String, List<Pair<String, String>>>();


      try {
        String imagePlaceHolder = SocialNotificationUtils.getImagePlaceHolder(language);
        for (NotificationInfo message : notifications) {
          String activityId = message.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
          ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
          if (activity == null) {
            continue;
          }
          String commentId = message.getValueOwnerParameter(SocialNotificationUtils.COMMENT_ID.getKey());
          ExoSocialActivity parentActivity = activity;
          if(StringUtils.isBlank(commentId)) {
            LOG.warn("Attempt to send a mail message with id '{}' and receiver '{}' with empty parameter 'commentId' and activityId = '{}' ",
                    message.getId(),
                    message.getTo(),
                    activityId);
          } else {
            parentActivity = activity;
            activity = Utils.getActivityManager().getActivity(commentId);
            if (activity == null) {
              continue;
            }
          }

          String poster = message.getValueOwnerParameter("poster");
          if(message.getTo() != null && poster != null && poster.equals(message.getTo())) {
            continue;
          }
          String title = SocialNotificationUtils.processImageTitle(activity.getTitle(), imagePlaceHolder);
          Pair<String, String> userComment = new ImmutablePair<String, String>(poster, title);
          if (parentActivity.getStreamOwner() != null) {
            Identity spaceIdentity = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, parentActivity.getStreamOwner(), true);
            if (spaceIdentity == null) {
              if (message.getTo()!=null && !message.getTo().equals(parentActivity.getStreamOwner())) {
                continue;
              }
            } else if (parentActivity.getPosterId() != null) {
              Identity identity = Utils.getIdentityManager().getIdentity(parentActivity.getPosterId(), true);
              if (identity != null) {
                if (message.getTo() != null && !message.getTo().equals(identity.getRemoteId())) {
                  continue;
                }
              }
            }
          }
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

    /** Defines the template builder for EditActivityPlugin*/
    private AbstractTemplateBuilder editActivity = new AbstractTemplateBuilder() {
        @Override
        protected MessageInfo makeMessage(NotificationContext ctx) {
            MessageInfo messageInfo = new MessageInfo();
            NotificationInfo notification = ctx.getNotificationInfo();
            String language = getLanguage(notification);

            String activityId = notification.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
            ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
            if (activity == null) {
                LOG.debug("Activity with id '{}' was removed but the notification with id'{}' is remaining", activityId, notification.getId());
                return null;
            }

            Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);

            TemplateContext templateContext = new TemplateContext(notification.getKey().getId(), language);
            templateContext.put("USER", identity.getProfile().getFullName());
            String subject = TemplateUtils.processSubject(templateContext);

            SocialNotificationUtils.addFooterAndFirstName(notification.getTo(), templateContext);
            templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));

            String imagePlaceHolder = SocialNotificationUtils.getImagePlaceHolder(language);
            String title = SocialNotificationUtils.processImageTitle(activity.getTitle(), imagePlaceHolder);
            templateContext.put("COMMENT", NotificationUtils.processLinkTitle(title));
            templateContext.put("OPEN_URL", LinkProviderUtils.getOpenLink(activity));
            templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity", activityId));
            templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activityId));

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
            SocialNotificationUtils.addFooterAndFirstName(first.getTo(), templateContext);

            //Store the activity id as key, and the list all identities who posted to the activity.
            Map<String, List<String>> receiverMap = new LinkedHashMap<String, List<String>>();
            Map<String, List<Pair<String, String>>> activityUserComments = new LinkedHashMap<String, List<Pair<String, String>>>();


            try {
                String imagePlaceHolder = SocialNotificationUtils.getImagePlaceHolder(language);
                for (NotificationInfo message : notifications) {
                    String activityId = message.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
                    ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
                    if (activity == null) {
                        continue;
                    }
                    String commentId = message.getValueOwnerParameter(SocialNotificationUtils.COMMENT_ID.getKey());
                    ExoSocialActivity parentActivity = null;
                    if(StringUtils.isBlank(commentId)) {
                        LOG.warn("Attempt to send a mail message with id '{}' and receiver '{}' with empty parameter 'commentId' and activityId = '{}' ",
                                message.getId(),
                                message.getTo(),
                                activityId);
                    } else {
                        parentActivity = activity;
                        activity = Utils.getActivityManager().getActivity(commentId);
                        if (activity == null) {
                            continue;
                        }
                    }

                    String poster = message.getValueOwnerParameter("poster");
                    if(message.getTo() != null && poster != null && poster.equals(message.getTo())) {
                        continue;
                    }
                    String title = SocialNotificationUtils.processImageTitle(activity.getTitle(), imagePlaceHolder);
                    Pair<String, String> userComment = new ImmutablePair<String, String>(poster, title);
                    if (parentActivity != null && parentActivity.getStreamOwner() != null) {
                        Identity spaceIdentity = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, parentActivity.getStreamOwner(), true);
                        if (spaceIdentity == null) {
                            if (message.getTo()!=null && !message.getTo().equals(parentActivity.getStreamOwner())) {
                                continue;
                            }
                        } else if (parentActivity.getPosterId() != null) {
                            Identity identity = Utils.getIdentityManager().getIdentity(parentActivity.getPosterId(), true);
                            if (identity != null) {
                                if (message.getTo() != null && !message.getTo().equals(identity.getRemoteId())) {
                                    continue;
                                }
                            }
                        }
                    }
                    //
                    parentActivity = parentActivity == null ? activity : parentActivity;
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
        String title = getI18N(activity,new Locale(language)).getTitle();
        String imagePlaceHolder = SocialNotificationUtils.getImagePlaceHolder(language);
        title = SocialNotificationUtils.processImageTitle(title, imagePlaceHolder);
        templateContext.put("ACTIVITY", NotificationUtils.processLinkTitle(title));
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
          List<String> mentionedUsers = new LinkedList<String>();
          for (String id : activity.getMentionedIds()) {
            if (id.contains("@")) {
              id = id.substring(0, id.length() - 2);
            }
            Identity identity = Utils.getIdentityManager().getIdentity(id, true);
            if (identity != null) {
              mentionedUsers.add(identity.getRemoteId());
            }
          }
          if (notification.getTo() != null && !mentionedUsers.contains(notification.getTo())) {
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
      String imagePlaceHolder = SocialNotificationUtils.getImagePlaceHolder(language);
      String title = SocialNotificationUtils.processImageTitle(activity.getTitle(), imagePlaceHolder);
      templateContext.put("SUBJECT", title);
      String subject = TemplateUtils.processSubject(templateContext);

      templateContext.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
      templateContext.put("OPEN_URL", LinkProviderUtils.getOpenLink(activity));
      String body;
      if(activity.isComment()) {
        ExoSocialActivity activityOfComment = Utils.getActivityManager().getParentActivity(activity);
        templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity", activityOfComment.getId()));
        templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activityOfComment.getId()));
        String commentTitle = getI18N(activity, new Locale(language)).getTitle();
        commentTitle = SocialNotificationUtils.processImageTitle(commentTitle, imagePlaceHolder);
        templateContext.put("ACTIVITY", NotificationUtils.processLinkTitle(commentTitle));
        body = TemplateUtils.processGroovy(templateContext);
      } else {
        templateContext.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity", activity.getId()));
        templateContext.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activity.getId()));
        body = SocialNotificationUtils.getBody(ctx, templateContext, activity);
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

      Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();

      try {
        for (NotificationInfo message : notifications) {
          String activityId = message.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());

          ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);

          //
          if (activity == null) {
            continue;
          }

          if (activity.getPosterId() != null) {
            Identity identity = Utils.getIdentityManager().getIdentity(activity.getPosterId(), true);
            if (identity != null) {
              if (message.getTo() != null && !message.getTo().equals(identity.getRemoteId())) {
                continue;
              }
            }
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

          Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId);
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
      String imagePlaceHolder = SocialNotificationUtils.getImagePlaceHolder(language);
      String title = SocialNotificationUtils.processImageTitle(activity.getTitle(), imagePlaceHolder);
      templateContext.put("SUBJECT", title);
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
          if (activity == null || !activity.getStreamOwner().equals(message.getTo())) {
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
      Space space = Utils.getSpaceService().getSpaceByPrettyName(spaceIdentity.getRemoteId());

      templateContext.put("USER", identity.getProfile().getFullName());
      templateContext.put("SPACE", space.getDisplayName());

      String imagePlaceHolder = SocialNotificationUtils.getImagePlaceHolder(language);
      String title = SocialNotificationUtils.processImageTitle(activity.getTitle(), imagePlaceHolder);
      templateContext.put("SUBJECT", title);
      String subject = TemplateUtils.processSubject(templateContext);

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
          String poster = message.getValueOwnerParameter(SocialNotificationUtils.POSTER.getKey());
          String activityId = message.getValueOwnerParameter(SocialNotificationUtils.ACTIVITY_ID.getKey());
          ExoSocialActivity activity = Utils.getActivityManager().getActivity(activityId);
          if (activity == null) {
            continue;
          }
          Space space = Utils.getSpaceService().getSpaceByPrettyName(activity.getStreamOwner());
          if (!Arrays.asList(space.getMembers()).contains(message.getTo())) {
            continue;
          }
          if(message.getTo() != null && poster != null && poster.equals(message.getTo())) {
            continue;
          }
          //
          SocialNotificationUtils.processInforSendTo(map, space.getId(), poster);
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
          if (relationship == null || relationship.getStatus().name().equals("PENDING") == false || !relationship.getReceiver().getRemoteId().equals(message.getTo())) {
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
          if (ArrayUtils.contains(space.getPendingUsers(), fromUser) == false || !ArrayUtils.contains(space.getManagers(), message.getTo())) {
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
          if(space == null) {
            LOG.info("Can't find space with id '{}'. Mail notification with id '{}' will not be sent", spaceId, message.getId());
            continue;
          }
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
    this.templateBuilders.put(PluginKey.key(EditCommentPlugin.ID), editComment);
    this.templateBuilders.put(PluginKey.key(EditActivityPlugin.ID), editActivity);
    this.templateBuilders.put(PluginKey.key(ActivityReplyToCommentPlugin.ID), replyToComment);
    this.templateBuilders.put(PluginKey.key(ActivityMentionPlugin.ID), mention);
    this.templateBuilders.put(PluginKey.key(LikePlugin.ID), like);
    this.templateBuilders.put(PluginKey.key(LikeCommentPlugin.ID), like);
    this.templateBuilders.put(PluginKey.key(NewUserPlugin.ID), newUser);
    this.templateBuilders.put(PluginKey.key(PostActivityPlugin.ID), postActivity);
    this.templateBuilders.put(PluginKey.key(PostActivitySpaceStreamPlugin.ID), postActivitySpace);
    this.templateBuilders.put(PluginKey.key(RelationshipReceivedRequestPlugin.ID), relationshipReceived);
    this.templateBuilders.put(PluginKey.key(RequestJoinSpacePlugin.ID), requestJoinSpace);
    this.templateBuilders.put(PluginKey.key(SpaceInvitationPlugin.ID), spaceInvitation);
  }

}
