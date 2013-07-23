/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.social.notification.provider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.exoplatform.commons.api.notification.MessageInfo;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.ProviderData;
import org.exoplatform.commons.api.notification.TemplateContext;
import org.exoplatform.commons.api.notification.service.AbstractNotificationProvider;
import org.exoplatform.commons.api.notification.service.TemplateGenerator;
import org.exoplatform.commons.api.notification.service.storage.ProviderService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;


public class SocialProviderImpl extends AbstractNotificationProvider {
  private static final Log LOG = ExoLogger.getLogger(SocialProviderImpl.class);
  
  public static final String ACTIVITY_ID = "activityId";

  public static final String SPACE_ID    = "spaceId";

  public static final String IDENTITY_ID = "remoteId";
  
  ProviderService providerService;

  ActivityManager activityManager;
  IdentityManager identityManager;
  SpaceService spaceService;
  
  TemplateGenerator templateGenerator;
  
  public enum PROVIDER_TYPE {
    ActivityMentionProvider, ActivityCommentProvider,
    ActivityPostProvider, ActivityPostSpaceProvider,
    InvitedJoinSpace, RequestJoinSpace,
    NewUserJoinSocialIntranet, ReceiceConnectionRequest,
    ActivityLikeProvider;
    public static List<String> toValues() {
      List<String> list = new ArrayList<String>();
      for (PROVIDER_TYPE elm : PROVIDER_TYPE.values()) {
        list.add(elm.name());
      }
      return list;
    }
  }
  
  public SocialProviderImpl(ActivityManager activityManager, IdentityManager identityManager,
                         SpaceService spaceService, ProviderService providerService, TemplateGenerator templateGenerator) {
    this.providerService = providerService;
    this.activityManager = activityManager;
    this.identityManager = identityManager;
    this.spaceService = spaceService;
    this.templateGenerator = templateGenerator;
  }
  
  @Override
  public MessageInfo buildMessageInfo(NotificationMessage message) {
    MessageInfo messageInfo = new MessageInfo();

    //
    messageInfo.from(getFrom(message)).to(getTo(message));

    //
    ProviderData provider = providerService.getProvider(message.getKey().getId());
    String language = getLanguage(message);
    TemplateContext ctx = new TemplateContext(provider.getType(), language);
    String body = "";
    String subject = "";
    Identity receiver = identityManager.getIdentity(message.getTo(), true);
    if (receiver == null) {
      receiver = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, message.getTo(), true);
    }
    ctx.put("FIRSTNAME", getFirstName(receiver.getRemoteId()));
    ctx.put("FOOTER_LINK", LinkProviderUtils.getRedirectUrl("settings", receiver.getRemoteId()));

    PROVIDER_TYPE type = PROVIDER_TYPE.valueOf(message.getKey().getId());
    try {
      switch (type) {
        case ActivityMentionProvider: {
          String activityId = message.getValueOwnerParameter(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);

          ctx.put("USER", identity.getProfile().getFullName());
          subject = templateGenerator.processSubject(ctx);
          
          ctx.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
          ctx.put("ACTIVITY", activity.getTitle());
          ctx.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity", activity.getId()));
          ctx.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activity.getId()));
          body = templateGenerator.processTemplate(ctx);
         
          messageInfo.subject(subject).body(body);
          break;
        }
        case ActivityCommentProvider: {
          String activityId = message.getValueOwnerParameter(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          ExoSocialActivity parentActivity = activityManager.getParentActivity(activity);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);
          
          ctx.put("USER", identity.getProfile().getFullName());
          subject = templateGenerator.processSubject(ctx);
          
          ctx.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
          ctx.put("COMMENT", activity.getTitle());
          ctx.put("ACTIVITY", parentActivity.getTitle());
          ctx.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity", activity.getId()));
          ctx.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activity.getId()));
          body = templateGenerator.processTemplate(ctx);
          
          messageInfo.subject(subject).body(body);
          break;
        }
        case ActivityLikeProvider: {
          String activityId = message.getValueOwnerParameter(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(message.getValueOwnerParameter("likersId"), true);
          
          ctx.put("USER", identity.getProfile().getFullName());
          subject = templateGenerator.processSubject(ctx);

          ctx.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
          ctx.put("ACTIVITY", activity.getTitle());
          ctx.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity", activity.getId()));
          ctx.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activity.getId()));
          body = templateGenerator.processTemplate(ctx);
          
          messageInfo.subject(subject).body(body);
          break;
        }
        case ActivityPostProvider: {
          String activityId = message.getValueOwnerParameter(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);
          
          ctx.put("USER", identity.getProfile().getFullName());
          subject = templateGenerator.processSubject(ctx);
          
          ctx.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
          ctx.put("ACTIVITY", activity.getTitle());
          ctx.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity", activity.getId()));
          ctx.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activity.getId()));
          body = templateGenerator.processTemplate(ctx);
          
          messageInfo.subject(subject).body(body);
          break;
        }
        case ActivityPostSpaceProvider: {
          String activityId = message.getValueOwnerParameter(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);
          Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, activity.getStreamOwner(), true);
          Space space = spaceService.getSpaceByPrettyName(spaceIdentity.getRemoteId());
          
          ctx.put("USER", identity.getProfile().getFullName());
          ctx.put("SPACE", spaceIdentity.getProfile().getFullName());
          subject = templateGenerator.processSubject(ctx);
          
          ctx.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
          ctx.put("SPACE_URL", LinkProviderUtils.getRedirectUrl("space", space.getId()));
          ctx.put("ACTIVITY", activity.getTitle());
          ctx.put("REPLY_ACTION_URL", LinkProviderUtils.getRedirectUrl("reply_activity", activity.getId()));
          ctx.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getRedirectUrl("view_full_activity", activity.getId()));
          body = templateGenerator.processTemplate(ctx);
          
          messageInfo.subject(subject).body(body);
          break;
        }
        case InvitedJoinSpace: {
          String spaceId = message.getValueOwnerParameter(SPACE_ID);
          Space space = spaceService.getSpaceById(spaceId);
          
          ctx.put("SPACE", space.getPrettyName());
          subject = templateGenerator.processSubject(ctx);
          
          ctx.put("SPACE_URL", LinkProviderUtils.getRedirectUrl("space", space.getId()));
          ctx.put("SPACE_AVATAR", LinkProviderUtils.getSpaceAvatarUrl(space));
          ctx.put("ACCEPT_SPACE_INVITATION_ACTION_URL", LinkProviderUtils.getAcceptInvitationToJoinSpaceUrl(space.getId(), message.getTo()));
          ctx.put("REFUSE_SPACE_INVITATION_ACTION_URL", LinkProviderUtils.getIgnoreInvitationToJoinSpaceUrl(space.getId(), message.getTo()));
          body = templateGenerator.processTemplate(ctx);
          
          messageInfo.subject(subject).body(body);
          break;
        }
        case RequestJoinSpace: {
          String spaceId = message.getValueOwnerParameter(SPACE_ID);
          Space space = spaceService.getSpaceById(spaceId);
          Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, message.getValueOwnerParameter("request_from"), true);
          Profile userProfile = identity.getProfile();
          
          ctx.put("SPACE", space.getPrettyName());
          ctx.put("USER", userProfile.getFullName());
          subject = templateGenerator.processSubject(ctx);
          
          ctx.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
          ctx.put("SPACE_URL", LinkProviderUtils.getRedirectUrl("space", space.getId()));
          ctx.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(userProfile));
          ctx.put("VALIDATE_SPACE_REQUEST_ACTION_URL", LinkProviderUtils.getValidateRequestToJoinSpaceUrl(space.getId(), identity.getRemoteId()));
          ctx.put("REFUSE_SPACE_REQUEST_ACTION_URL", LinkProviderUtils.getRefuseRequestToJoinSpaceUrl(space.getId(), identity.getRemoteId()));
          body = templateGenerator.processTemplate(ctx);
          
          messageInfo.subject(subject).body(body);
          break;
        }
        case NewUserJoinSocialIntranet: {
          String remoteId = message.getValueOwnerParameter(IDENTITY_ID);
          Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId, true);
          Profile userProfile = identity.getProfile();
          
          ctx.put("USER", userProfile.getFullName());
          ctx.put("PORTAL_NAME", System.getProperty("exo.notifications.portalname", "eXo"));
          subject = templateGenerator.processSubject(ctx);
          
          ctx.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(userProfile));
          ctx.put("CONNECT_ACTION_URL", LinkProviderUtils.getInviteToConnectUrl(identity.getRemoteId()));
          body = templateGenerator.processTemplate(ctx);
          
          messageInfo.subject(subject).body(body);
          break;
        }
        case ReceiceConnectionRequest: {
          String sender = message.getValueOwnerParameter("sender");
          String toUser = message.getTo();
          Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, sender, true);
          Profile userProfile = identity.getProfile();
          
          ctx.put("PORTAL_NAME", System.getProperty("exo.notifications.portalname", "eXo"));
          ctx.put("USER", userProfile.getFullName());
          subject = templateGenerator.processSubject(ctx);
          
          ctx.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
          ctx.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(userProfile));
          ctx.put("ACCEPT_CONNECTION_REQUEST_ACTION_URL", LinkProviderUtils.getConfirmInvitationToConnectUrl(sender, toUser));
          ctx.put("REFUSE_CONNECTION_REQUEST_ACTION_URL", LinkProviderUtils.getIgnoreInvitationToConnectUrl(sender, toUser));
          body = templateGenerator.processTemplate(ctx);

          messageInfo.subject(subject).body(body);
          break;
        }
      }
    } catch (Exception e) {
      LOG.error("Can not build template of SocialProviderImpl by Provider " + provider.getType(), e );
      return null;
    }
    

    return messageInfo;
  }
  
  @Override
  public List<String> getSupportType() {
    return PROVIDER_TYPE.toValues();
  }
  
  public String getActivityId(NotificationMessage message) {
    return message.getValueOwnerParameter(ACTIVITY_ID);
  }

  public String getSpaceId(NotificationMessage message) {
    return message.getValueOwnerParameter(SPACE_ID);
  }

  public String getIdentityId(NotificationMessage message) {
    return message.getValueOwnerParameter(IDENTITY_ID);
  }

  @Override
  public String buildDigestMessageInfo(List<NotificationMessage> messages) {
    
    
    StringBuilder sb = new StringBuilder();
    NotificationMessage notificationMessage = messages.get(0);
    String providerId = notificationMessage.getKey().getId();
    ProviderData provider = providerService.getProvider(providerId);

    LOG.info("Start building DigestMessageInfo by Provider " + providerId);
    long startTime = System.currentTimeMillis();
    
    String language = getLanguage(notificationMessage);
    if (language == null) {
      language = DEFAULT_LANGUAGE;
    }
    Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
    
    TemplateContext ctx = new TemplateContext(providerId, language);

    PROVIDER_TYPE type = PROVIDER_TYPE.valueOf(provider.getType());
    switch (type) {
      case ActivityMentionProvider: {
        for (NotificationMessage message : messages) {
          String activityId = message.getValueOwnerParameter(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);

          ctx.put("USER", SocialNotificationUtils.buildRedirecUrl("user", identity.getRemoteId(), identity.getProfile().getFullName()));
          ctx.put("ACTIVITY", SocialNotificationUtils.buildRedirecUrl("activity", activity.getId(), activity.getTitle()));
          String digester = templateGenerator.processDigest(ctx.digestType(0).end());
          sb.append(digester).append("</br>");

          ctx.clear();
        }
        break;
      }
      case ActivityCommentProvider: {
        for (NotificationMessage message : messages) {
          String activityId = message.getValueOwnerParameter(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          ExoSocialActivity parentActivity = activityManager.getParentActivity(activity);
          //
          processInforSendTo(map, parentActivity.getId(), message.getValueOwnerParameter("poster"));
        }
        sb.append(getMessageByIds(map, ctx));
        break;
      }
      case ActivityLikeProvider: {
        for (NotificationMessage message : messages) {
          String activityId = message.getValueOwnerParameter(ACTIVITY_ID);
          //
          processInforSendTo(map, activityId, message.getValueOwnerParameter("likersId"));
        }
        sb.append(getMessageByIds(map, ctx));
        break;
      }
      case ActivityPostProvider: {
        for (NotificationMessage message : messages) {
          String activityId = message.getValueOwnerParameter(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);
          
          ctx.put("USER", SocialNotificationUtils.buildRedirecUrl("user", identity.getRemoteId(), identity.getProfile().getFullName()));
          ctx.put("ACTIVITY", SocialNotificationUtils.buildRedirecUrl("activity", activity.getId(), activity.getTitle()));
          String digester = templateGenerator.processDigest(ctx.digestType(0).end());
          sb.append(digester).append("</br>");
        }
        break;
      }
      case ActivityPostSpaceProvider: {
        for (NotificationMessage message : messages) {
          String activityId = message.getValueOwnerParameter(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);
          Space space = Utils.getSpaceService().getSpaceByPrettyName(activity.getStreamOwner());
          
          ctx.put("USER", SocialNotificationUtils.buildRedirecUrl("user", identity.getRemoteId(), identity.getProfile().getFullName()));
          ctx.put("ACTIVITY", SocialNotificationUtils.buildRedirecUrl("activity", activity.getId(), activity.getTitle()));
          ctx.put("SPACE", SocialNotificationUtils.buildRedirecUrl("space", space.getId(), space.getDisplayName()));
          String digester = templateGenerator.processDigest(ctx.digestType(0).end());
          sb.append(digester).append("</br>");

          ctx.clear();
        }
        break;
      }
      case InvitedJoinSpace: {
        int count = messages.size();
        String[] keys = {"SPACE", "SPACE_LIST", "LAST3_SPACES"};
        String key = "";
        StringBuilder value = new StringBuilder();

        for (int i = 0; i < count && i < 3; i++) {
          String spaceId = messages.get(i).getValueOwnerParameter(SPACE_ID);
          Space space = spaceService.getSpaceById(spaceId);
          if (i > 1 && count == 3) {
            key = keys[i - 1];
          } else {
            key = keys[i];
          }
          value.append(SocialNotificationUtils.buildRedirecUrl("space", space.getId(), space.getDisplayName()));
          if (count > (i + 1) && i < 2) {
            value.append(", ");
          }
        }
        ctx.put(key, value.toString());
        if(count > 3) {
          ctx.put("COUNT", SocialNotificationUtils.buildRedirecUrl("space_invitation", null, String.valueOf((count - 3))));
        }

        String digester = templateGenerator.processDigest(ctx.digestType(count).end());
        sb.append(digester).append("</br>");
        break;
      }
      case RequestJoinSpace: {
        for (NotificationMessage message : messages) {
          String spaceId = message.getValueOwnerParameter(SPACE_ID);
          String fromUser = message.getValueOwnerParameter("request_from");
          //
          processInforSendTo(map, spaceId, fromUser);
        }
        sb.append(getMessageByIds(map, ctx));
        break;
      }
      case NewUserJoinSocialIntranet: {
        int count = messages.size();
        String[] keys = {"USER", "USER_LIST", "LAST3_USERS"};
        String key = "";
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < count && i < 3; i++) {
          String remoteId = messages.get(i).getValueOwnerParameter(IDENTITY_ID);
          Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId, true);
          //
          if (i > 1 && count == 3) {
            key = keys[i - 1];
          } else {
            key = keys[i];
          }
          value.append(SocialNotificationUtils.buildRedirecUrl("user", identity.getRemoteId(), identity.getProfile().getFullName()));
          if (count > (i + 1) && i < 2) {
            value.append(", ");
          }
        }
        ctx.put(key, value.toString());
        if(count > 3) {
          ctx.put("COUNT", SocialNotificationUtils.buildRedirecUrl("connections", null, String.valueOf((count - 3))));
        }
        ctx.put("PORTAL_NAME", System.getProperty("exo.notifications.portalname", "eXo"));

        String digester = templateGenerator.processDigest(ctx.digestType(count).end());
        sb.append(digester).append("</br>");
        
        break;
      }
      case ReceiceConnectionRequest: {
        int count = messages.size();
        String[] keys = {"USER", "USER_LIST", "LAST3_USERS"};
        String key = "";
        StringBuilder value = new StringBuilder();

        for (int i = 0; i < count && i < 3; i++) {
          Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, messages.get(i).getValueOwnerParameter("sender"), true);
          if (i > 1 && count == 3) {
            key = keys[i - 1];
          } else {
            key = keys[i];
          }
          value.append(SocialNotificationUtils.buildRedirecUrl("user", identity.getRemoteId(), identity.getProfile().getFullName()));
          if (count > (i + 1) && i < 2) {
            value.append(", ");
          }
        }
        ctx.put(key, value.toString());
        if(count > 3) {
          ctx.put("COUNT", SocialNotificationUtils.buildRedirecUrl("connections_request", null, String.valueOf((count - 3))));
        }

        String digester = templateGenerator.processDigest(ctx.digestType(count).end());
        sb.append(digester).append("</br>");
        
        break;
      }
    }
    
    LOG.info("End build DigestMessageInfo by Provider " + providerId + (System.currentTimeMillis() - startTime) + " ms.");
    
    return sb.toString();
  }

  private void processInforSendTo(Map<String, List<String>> map, String key, String value) {
    Set<String> set = new HashSet<String>();
    if (map.containsKey(key)) {
      set.addAll(map.get(key));
    }
    set.add(value);
    map.put(key, new ArrayList<String>(set));
  }

  private String getMessageByIds(Map<String, List<String>> map, TemplateContext ctx) {
    StringBuilder sb = new StringBuilder();
    ExoSocialActivity activity = null;
    Space space = null;
    for (Entry<String, List<String>> entry : map.entrySet()) {
      String id = entry.getKey();
      try {
        activity = activityManager.getActivity(id);
        space = null;
      } catch (Exception e) {
        space = spaceService.getSpaceById(id);
        activity = null;
      }
      List<String> values = entry.getValue();
      int count = values.size();

      if (activity != null) {
        ctx.put("ACTIVITY", SocialNotificationUtils.buildRedirecUrl("activity", activity.getId(), activity.getTitle()));
      } else {
        ctx.put("SPACE", SocialNotificationUtils.buildRedirecUrl("space", space.getId(), space.getDisplayName()));
      }
      
      String[] keys = {"USER", "USER_LIST", "LAST3_USERS"};
      String key = "";
      StringBuilder value = new StringBuilder();
      
      for (int i = 0; i < count && i <= 3; i++) {
        Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, values.get(i), true);
        if (i > 1 && count == 3) {
          key = keys[i - 1];
        } else {
          key = keys[i];
        }
        value.append(SocialNotificationUtils.buildRedirecUrl("user", identity.getRemoteId(), identity.getProfile().getFullName()));
        if (count > (i + 1) && i < 2) {
          value.append(", ");
        }
      }
      ctx.put(key, value.toString());
      if(count > 3) {
        if (activity != null) {
          ctx.put("COUNT", SocialNotificationUtils.buildRedirecUrl("activity", activity.getId(), String.valueOf((count - 3))));
        } else {
          ctx.put("COUNT", SocialNotificationUtils.buildRedirecUrl("space", space.getId(), String.valueOf((count - 3))));
        }
      }

      String digester = templateGenerator.processDigest(ctx.digestType(count).end());
      sb.append(digester).append("</br>");
    }
    
    return sb.toString();
  }
  
}
