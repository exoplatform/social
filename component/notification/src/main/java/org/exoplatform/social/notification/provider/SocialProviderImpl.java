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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.exoplatform.commons.api.notification.MessageInfo;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.ProviderData;
import org.exoplatform.commons.api.notification.service.AbstractNotificationProvider;
import org.exoplatform.commons.api.notification.service.ProviderService;
import org.exoplatform.commons.api.notification.service.TemplateGenerator;
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


public class SocialProviderImpl extends AbstractNotificationProvider {
  private static final Log LOG = ExoLogger.getLogger(SocialProviderImpl.class);
  
  public static final String ACTIVITY_ID = "activityId";

  public static final String SPACE_ID    = "spaceId";

  public static final String IDENTITY_ID = "identityId";
  
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
    messageInfo.setFrom(getFrom(message)).setTo(getTo(message));

    //
    ProviderData provider = providerService.getProvider(message.getProviderType());
    String language = getLanguage(message);
    Map<String, String> valueables = new HashMap<String, String>();
    String body = "";
    String subject = "";

    PROVIDER_TYPE type = PROVIDER_TYPE.valueOf(message.getProviderType());
    try {
      switch (type) {
        case ActivityMentionProvider: {
          String activityId = message.getOwnerParameter().get(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);

          valueables.put("$user-who-mentionned", identity.getProfile().getFullName());
          subject = templateGenerator.processSubjectIntoString(provider.getType(), valueables, language);
          
          valueables.put("$post", activity.getTitle());
          valueables.put("$replyAction", LinkProviderUtils.getReplyActivityUrl(activity.getId(), message.getSendToUserIds().get(0)));
          valueables.put("$viewAction", LinkProviderUtils.getViewFullDiscussionUrl(activity.getId(), message.getSendToUserIds().get(0)));
          body = templateGenerator.processTemplateIntoString(provider.getType(), valueables, language);
         
          messageInfo.setSubject(subject).setBody(body);
          break;
        }
        case ActivityCommentProvider: {
          String activityId = message.getOwnerParameter().get(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          ExoSocialActivity parentActivity = activityManager.getParentActivity(activity);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);
          
          valueables.put("$other_user_name", identity.getProfile().getFullName());
          subject = templateGenerator.processSubjectIntoString(provider.getType(), valueables, language);
          
          valueables.put("$activity_comment", activity.getTitle());
          valueables.put("$original_activity_message", parentActivity.getTitle());
          valueables.put("$replyAction", LinkProviderUtils.getReplyActivityUrl(activity.getId(), message.getSendToUserIds().get(0)));
          valueables.put("$viewAction", LinkProviderUtils.getViewFullDiscussionUrl(activity.getId(), message.getSendToUserIds().get(0)));
          body = templateGenerator.processTemplateIntoString(provider.getType(), valueables, language);
          
          messageInfo.setSubject(subject).setBody(body);
          break;
        }
        case ActivityLikeProvider: {
          String activityId = message.getOwnerParameter().get(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(message.getFrom(), true);
          
          valueables.put("$other_user_name", identity.getProfile().getFullName());
          subject = templateGenerator.processSubjectIntoString(provider.getType(), valueables, language);

          valueables.put("$activity", activity.getTitle());
          valueables.put("$replyAction", LinkProviderUtils.getReplyActivityUrl(activity.getId(), message.getSendToUserIds().get(0)));
          valueables.put("$viewAction", LinkProviderUtils.getViewFullDiscussionUrl(activity.getId(), message.getSendToUserIds().get(0)));
          body = templateGenerator.processTemplateIntoString(provider.getType(), valueables, language);
          
          messageInfo.setSubject(subject).setBody(body);
          break;
        }
        case ActivityPostProvider: {
          String activityId = message.getOwnerParameter().get(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);
          
          valueables.put("$other_user_name", identity.getProfile().getFullName());
          subject = templateGenerator.processSubjectIntoString(provider.getType(), valueables, language);
          
          valueables.put("$activity_message", activity.getTitle());
          valueables.put("$replyAction", LinkProviderUtils.getReplyActivityUrl(activity.getId(), message.getSendToUserIds().get(0)));
          valueables.put("$viewAction", LinkProviderUtils.getViewFullDiscussionUrl(activity.getId(), message.getSendToUserIds().get(0)));
          body = templateGenerator.processTemplateIntoString(provider.getType(), valueables, language);
          
          messageInfo.setSubject(subject).setBody(body);
          break;
        }
        case ActivityPostSpaceProvider: {
          String activityId = message.getOwnerParameter().get(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);
          Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, activity.getStreamOwner(), true);
          
          valueables.put("$other_user_name", identity.getProfile().getFullName());
          valueables.put("$space-name", spaceIdentity.getProfile().getFullName());
          subject = templateGenerator.processSubjectIntoString(provider.getType(), valueables, language);
          
          valueables.put("$activity_message", activity.getTitle());
          valueables.put("$replyAction", LinkProviderUtils.getReplyActivityUrl(activity.getId(), message.getSendToUserIds().get(0)));
          valueables.put("$viewAction", LinkProviderUtils.getViewFullDiscussionUrl(activity.getId(), message.getSendToUserIds().get(0)));
          body = templateGenerator.processTemplateIntoString(provider.getType(), valueables, language);
          
          messageInfo.setSubject(subject).setBody(body);
          break;
        }
        case InvitedJoinSpace: {
          String spaceId = message.getOwnerParameter().get(SPACE_ID);
          Space space = spaceService.getSpaceById(spaceId);
          
          valueables.put("$space-name", space.getPrettyName());
          subject = templateGenerator.processSubjectIntoString(provider.getType(), valueables, language);
          
          valueables.put("$space-avatar-url", LinkProviderUtils.getSpaceAvatarUrl(space));
          valueables.put("$acceptAction", LinkProviderUtils.getAcceptInvitationToJoinSpaceUrl(space.getId(), message.getSendToUserIds().get(0)));
          valueables.put("$ignoreAction", LinkProviderUtils.getIgnoreInvitationToJoinSpaceUrl(space.getId(), message.getSendToUserIds().get(0)));
          body = templateGenerator.processTemplateIntoString(provider.getType(), valueables, language);
          
          messageInfo.setSubject(subject).setBody(body);
          break;
        }
        case RequestJoinSpace: {
          String spaceId = message.getOwnerParameter().get(SPACE_ID);
          Space space = spaceService.getSpaceById(spaceId);
          Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, message.getFrom(), true);
          Profile userProfile = identity.getProfile();
          
          valueables.put("$space-name", space.getPrettyName());
          valueables.put("$user-name", userProfile.getFullName());
          subject = templateGenerator.processSubjectIntoString(provider.getType(), valueables, language);
          
          valueables.put("$user-avatar-url", LinkProviderUtils.getUserAvatarUrl(userProfile));
          valueables.put("$validateAction", LinkProviderUtils.getValidateRequestToJoinSpaceUrl(space.getId(), identity.getRemoteId()));
          body = templateGenerator.processTemplateIntoString(provider.getType(), valueables, language);
          
          messageInfo.setSubject(subject).setBody(body);
          break;
        }
        case NewUserJoinSocialIntranet: {
          
          break;
        }
        case ReceiceConnectionRequest: {
          Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, message.getFrom(), true);
          Profile userProfile = identity.getProfile();
          
          valueables.put("$user-name", userProfile.getFullName());
          subject = templateGenerator.processSubjectIntoString(provider.getType(), valueables, language);
          
          valueables.put("$user-avatar-url", LinkProviderUtils.getUserAvatarUrl(userProfile));
          valueables.put("$confirmAction", LinkProviderUtils.getConfirmInvitationToConnectUrl(message.getFrom(), message.getSendToUserIds().get(0)));
          valueables.put("$ignoreAction", LinkProviderUtils.getIgnoreInvitationToConnectUrl(message.getFrom(), message.getSendToUserIds().get(0)));
          body = templateGenerator.processTemplateIntoString(provider.getType(), valueables, language);

          messageInfo.setSubject(subject).setBody(body);
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
    return message.getOwnerParameter().get(ACTIVITY_ID);
  }

  public String getSpaceId(NotificationMessage message) {
    return message.getOwnerParameter().get(SPACE_ID);
  }

  public String getIdentityId(NotificationMessage message) {
    return message.getOwnerParameter().get(IDENTITY_ID);
  }

  @Override
  public String buildDigestMessageInfo(List<NotificationMessage> messages) {
    
    
    StringBuilder sb = new StringBuilder();
    NotificationMessage notificationMessage = messages.get(0);
    String providerId = notificationMessage.getProviderType();
    ProviderData providerData = providerService.getProvider(providerId);

    LOG.info("Start building DigestMessageInfo by Provider " + providerId);
    long startTime = System.currentTimeMillis();
    
    String language = getLanguage(notificationMessage);
    if (language == null) {
      language = DEFAULT_LANGUAGE;
    }
    PROVIDER_TYPE type = PROVIDER_TYPE.valueOf(providerData.getType());
    Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
    
    switch (type) {
      case ActivityMentionProvider: {
        Map<String, String> valueables = new HashMap<String, String>();
        for (NotificationMessage message : messages) {
          String activityId = message.getOwnerParameter().get(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);

          valueables.put("$user1-fullname", identity.getProfile().getFullName());
          valueables.put("$activity", activity.getTitle());
          String digester = templateGenerator.processDigestIntoString(providerId, valueables, language, 0);
          sb.append(digester).append("</br>");

          valueables.clear();
        }
        break;
      }
      case ActivityCommentProvider: {
        for (NotificationMessage message : messages) {
          String activityId = message.getOwnerParameter().get(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          ExoSocialActivity parentActivity = activityManager.getParentActivity(activity);
          String parentActivityId = parentActivity.getId();
          if (map.containsKey(parentActivityId)) {
            List<String> values = map.get(parentActivityId);
            values.add(message.getFrom());
            map.put(parentActivityId, values);
          } else {
            List<String> values = new ArrayList<String>();
            values.add(message.getFrom());
            map.put(activityId, values);
          }
        }
        sb.append(getMessageByIds(map, providerData, language));
        break;
      }
      case ActivityLikeProvider: {
        for (NotificationMessage message : messages) {
          String activityId = message.getOwnerParameter().get(ACTIVITY_ID);
          if (map.containsKey(activityId)) {
            List<String> values = map.get(activityId);
            values.add(message.getFrom());
            map.put(activityId, values);
          } else {
            List<String> values = new ArrayList<String>();
            values.add(message.getFrom());
            map.put(activityId, values);
          }
        }
        sb.append(getMessageByIds(map, providerData, language));
        break;
      }
      case ActivityPostProvider: {
        Map<String, String> valueables = new HashMap<String, String>();
        for (NotificationMessage message : messages) {
          String activityId = message.getOwnerParameter().get(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);
          
          
          valueables.put("$user1-fullname", identity.getProfile().getFullName());
          valueables.put("$activity", activity.getTitle());
          String digester = templateGenerator.processDigestIntoString(providerId, valueables, language, 0);
          sb.append(digester).append("</br>");

          valueables.clear();
          
          sb.append(digester).append("</br>");
        }
        break;
      }
      case ActivityPostSpaceProvider: {
        break;
      }
      case InvitedJoinSpace: {
        Map<String, String> valueables = new HashMap<String, String>();
        int count = messages.size();

        for (int i = 0; i < count && i <= 3; i++) {
          String spaceId = messages.get(i).getOwnerParameter().get(SPACE_ID);
          Space space = spaceService.getSpaceById(spaceId);
          valueables.put("$space"+i+"-name", space.getPrettyName());
          
          if(i == 2) {
            valueables.put(", $space2-name", "");
          }
          if(i == 3 && count > 3) {
            valueables.put("$number-others", String.valueOf((count - 3)));
          }
        }

        String digester = templateGenerator.processDigestIntoString(providerId, valueables, language, count);
        sb.append(digester).append("</br>");
        break;
      }
      case RequestJoinSpace: {
        for (NotificationMessage message : messages) {
          String spaceId = messages.get(0).getOwnerParameter().get(SPACE_ID);
          if (map.containsKey(spaceId)) {
            List<String> values = map.get(spaceId);
            values.add(message.getFrom());
            map.put(spaceId, values);
          } else {
            List<String> values = new ArrayList<String>();
            values.add(message.getFrom());
            map.put(spaceId, values);
          }
        }
        sb.append(getMessageByIds(map, providerData, language));
        break;
      }
      case NewUserJoinSocialIntranet: {
        
        break;
      }
      case ReceiceConnectionRequest: {
        Map<String, String> valueables = new HashMap<String, String>();
        int count = messages.size();

        for (int i = 0; i < count && i <= 3; i++) {
          Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, messages.get(i).getFrom(), true);
          Profile userProfile = identity.getProfile();
          
          valueables.put("$user"+i+"-fullname", userProfile.getFullName());
          
          if(i == 2) {
            valueables.put(", $user2-fullname", "");
          }
          if(i == 3 && count > 3) {
            valueables.put("$number-others", String.valueOf((count - 3)));
          }
        }

        String digester = templateGenerator.processDigestIntoString(providerId, valueables, language, count);
        sb.append(digester).append("</br>");
        
        break;
      }
    }
    
    LOG.info("End build DigestMessageInfo by Provider " + providerId + (System.currentTimeMillis() - startTime) + " ms.");
    
    return sb.toString();
  }

  private String getMessageByIds(Map<String, List<String>> map, ProviderData providerData, String language) {
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

      Map<String, String> valueables = new HashMap<String, String>();
      
      if (activity != null) {
        valueables.put("$activity", activity.getTitle());
      } else {
        valueables.put("$space-name", space.getPrettyName());
      }
      
      for (int i = 0; i < count && i <= 3; i++) {
        Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, values.get(i), true);
        Profile userProfile = identity.getProfile();
        
        valueables.put("$user"+i+"-fullname", userProfile.getFullName());
        
        if(i == 2) {
          valueables.put(", $user2-fullname", "");
        }
        if(i == 3 && count > 3) {
          valueables.put("$number-others", String.valueOf((count - 3)));
        }
      }

      String digester = templateGenerator.processDigestIntoString(providerData.getType(), valueables, language, count);
      sb.append(digester).append("</br>");
    }
    
    return sb.toString();
  }
  
}
