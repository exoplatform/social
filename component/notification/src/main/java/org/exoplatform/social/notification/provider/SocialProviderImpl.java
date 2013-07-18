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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.exoplatform.social.notification.Utils;


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
    messageInfo.from(getFrom(message)).to(getTo(message));

    //
    ProviderData provider = providerService.getProvider(message.getKey().getId());
    String language = getLanguage(message);
    Map<String, String> valueables = new HashMap<String, String>();
    String body = "";
    String subject = "";
    Identity receiver = identityManager.getIdentity(message.getTo(), true);
    if (receiver == null) {
      receiver = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, message.getTo(), true);
    }
    valueables.put("FIRSTNAME", getFirstName(receiver.getRemoteId()));
    valueables.put("FOOTER_LINK", LinkProviderUtils.getRedirectUrl("settings", receiver.getRemoteId()));

    PROVIDER_TYPE type = PROVIDER_TYPE.valueOf(message.getKey().getId());
    try {
      switch (type) {
        case ActivityMentionProvider: {
          String activityId = message.getValueOwnerParameter(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);

          valueables.put("USER", identity.getProfile().getFullName());
          subject = templateGenerator.processSubjectIntoString(provider.getType(), valueables, language);
          
          valueables.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
          valueables.put("ACTIVITY", activity.getTitle());
          valueables.put("REPLY_ACTION_URL", LinkProviderUtils.getReplyActivityUrl(activity.getId(), message.getSendToUserIds().get(0)));
          valueables.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getViewFullDiscussionUrl(activity.getId(), message.getSendToUserIds().get(0)));
          body = templateGenerator.processTemplate(provider.getType(), valueables, language);
         
          messageInfo.subject(subject).body(body);
          break;
        }
        case ActivityCommentProvider: {
          String activityId = message.getValueOwnerParameter(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          ExoSocialActivity parentActivity = activityManager.getParentActivity(activity);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);
          
          valueables.put("USER", identity.getProfile().getFullName());
          subject = templateGenerator.processSubjectIntoString(provider.getType(), valueables, language);
          
          valueables.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
          valueables.put("COMMENT", activity.getTitle());
          valueables.put("ACTIVITY", parentActivity.getTitle());
          valueables.put("REPLY_ACTION_URL", LinkProviderUtils.getReplyActivityUrl(activity.getId(), message.getSendToUserIds().get(0)));
          valueables.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getViewFullDiscussionUrl(activity.getId(), message.getSendToUserIds().get(0)));
          body = templateGenerator.processTemplate(provider.getType(), valueables, language);
          
          messageInfo.subject(subject).body(body);
          break;
        }
        case ActivityLikeProvider: {
          String activityId = message.getValueOwnerParameter(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(message.getValueOwnerParameter("likersId"), true);
          
          valueables.put("USER", identity.getProfile().getFullName());
          subject = templateGenerator.processSubjectIntoString(provider.getType(), valueables, language);

          valueables.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
          valueables.put("ACTIVITY", activity.getTitle());
          valueables.put("REPLY_ACTION_URL", LinkProviderUtils.getReplyActivityUrl(activity.getId(), message.getSendToUserIds().get(0)));
          valueables.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getViewFullDiscussionUrl(activity.getId(), message.getSendToUserIds().get(0)));
          body = templateGenerator.processTemplate(provider.getType(), valueables, language);
          
          messageInfo.subject(subject).body(body);
          break;
        }
        case ActivityPostProvider: {
          String activityId = message.getValueOwnerParameter(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);
          
          valueables.put("USER", identity.getProfile().getFullName());
          subject = templateGenerator.processSubjectIntoString(provider.getType(), valueables, language);
          
          valueables.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
          valueables.put("ACTIVITY", activity.getTitle());
          valueables.put("REPLY_ACTION_URL", LinkProviderUtils.getReplyActivityUrl(activity.getId(), message.getSendToUserIds().get(0)));
          valueables.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getViewFullDiscussionUrl(activity.getId(), message.getSendToUserIds().get(0)));
          body = templateGenerator.processTemplate(provider.getType(), valueables, language);
          
          messageInfo.subject(subject).body(body);
          break;
        }
        case ActivityPostSpaceProvider: {
          String activityId = message.getValueOwnerParameter(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);
          Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, activity.getStreamOwner(), true);
          Space space = spaceService.getSpaceByPrettyName(spaceIdentity.getRemoteId());
          
          valueables.put("USER", identity.getProfile().getFullName());
          valueables.put("SPACE", spaceIdentity.getProfile().getFullName());
          subject = templateGenerator.processSubjectIntoString(provider.getType(), valueables, language);
          
          valueables.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
          valueables.put("SPACE_URL", LinkProviderUtils.getRedirectUrl("space", space.getId()));
          valueables.put("ACTIVITY", activity.getTitle());
          valueables.put("REPLY_ACTION_URL", LinkProviderUtils.getReplyActivityUrl(activity.getId(), message.getSendToUserIds().get(0)));
          valueables.put("VIEW_FULL_DISCUSSION_ACTION_URL", LinkProviderUtils.getViewFullDiscussionUrl(activity.getId(), message.getSendToUserIds().get(0)));
          body = templateGenerator.processTemplate(provider.getType(), valueables, language);
          
          messageInfo.subject(subject).body(body);
          break;
        }
        case InvitedJoinSpace: {
          String spaceId = message.getValueOwnerParameter(SPACE_ID);
          Space space = spaceService.getSpaceById(spaceId);
          
          valueables.put("SPACE", space.getPrettyName());
          subject = templateGenerator.processSubjectIntoString(provider.getType(), valueables, language);
          
          valueables.put("SPACE_URL", LinkProviderUtils.getRedirectUrl("space", space.getId()));
          valueables.put("SPACE_AVATAR", LinkProviderUtils.getSpaceAvatarUrl(space));
          valueables.put("ACCEPT_SPACE_INVITATION_ACTION_URL", LinkProviderUtils.getAcceptInvitationToJoinSpaceUrl(space.getId(), message.getSendToUserIds().get(0)));
          valueables.put("REFUSE_SPACE_INVITATION_ACTION_URL", LinkProviderUtils.getIgnoreInvitationToJoinSpaceUrl(space.getId(), message.getSendToUserIds().get(0)));
          body = templateGenerator.processTemplate(provider.getType(), valueables, language);
          
          messageInfo.subject(subject).body(body);
          break;
        }
        case RequestJoinSpace: {
          String spaceId = message.getValueOwnerParameter(SPACE_ID);
          Space space = spaceService.getSpaceById(spaceId);
          Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, message.getValueOwnerParameter("request_from"), true);
          Profile userProfile = identity.getProfile();
          
          valueables.put("SPACE", space.getPrettyName());
          valueables.put("USER", userProfile.getFullName());
          subject = templateGenerator.processSubjectIntoString(provider.getType(), valueables, language);
          
          valueables.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
          valueables.put("SPACE_URL", LinkProviderUtils.getRedirectUrl("space", space.getId()));
          valueables.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(userProfile));
          valueables.put("VALIDATE_SPACE_REQUEST_ACTION_URL", LinkProviderUtils.getValidateRequestToJoinSpaceUrl(space.getId(), identity.getRemoteId()));
          valueables.put("REFUSE_SPACE_REQUEST_ACTION_URL", LinkProviderUtils.getRefuseRequestToJoinSpaceUrl(space.getId(), identity.getRemoteId()));
          body = templateGenerator.processTemplate(provider.getType(), valueables, language);
          
          messageInfo.subject(subject).body(body);
          break;
        }
        case NewUserJoinSocialIntranet: {
          
          break;
        }
        case ReceiceConnectionRequest: {
          String sender = message.getValueOwnerParameter("sender");
          String toUser = message.getSendToUserIds().iterator().next();
          Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, sender, true);
          Profile userProfile = identity.getProfile();
          
          valueables.put("USER", userProfile.getFullName());
          subject = templateGenerator.processSubjectIntoString(provider.getType(), valueables, language);
          
          valueables.put("PROFILE_URL", LinkProviderUtils.getRedirectUrl("user", identity.getRemoteId()));
          valueables.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(userProfile));
          valueables.put("ACCEPT_CONNECTION_REQUEST_ACTION_URL", LinkProviderUtils.getConfirmInvitationToConnectUrl(sender, toUser));
          valueables.put("REFUSE_CONNECTION_REQUEST_ACTION_URL", LinkProviderUtils.getIgnoreInvitationToConnectUrl(sender, toUser));
          body = templateGenerator.processTemplate(provider.getType(), valueables, language);

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
          String activityId = message.getValueOwnerParameter(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);

          valueables.put("USER", identity.getProfile().getFullName());
          valueables.put("ACTIVITY", activity.getTitle());
          String digester = templateGenerator.processDigestIntoString(providerId, valueables, language, 0);
          sb.append(digester).append("</br>");

          valueables.clear();
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
        sb.append(getMessageByIds(map, providerData, language));
        break;
      }
      case ActivityLikeProvider: {
        for (NotificationMessage message : messages) {
          String activityId = message.getValueOwnerParameter(ACTIVITY_ID);
          //
          processInforSendTo(map, activityId, message.getValueOwnerParameter("likersId"));
        }
        sb.append(getMessageByIds(map, providerData, language));
        break;
      }
      case ActivityPostProvider: {
        Map<String, String> valueables = new HashMap<String, String>();
        for (NotificationMessage message : messages) {
          String activityId = message.getValueOwnerParameter(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);
          
          
          valueables.put("USER", identity.getProfile().getFullName());
          valueables.put("ACTIVITY", activity.getTitle());
          String digester = templateGenerator.processDigestIntoString(providerId, valueables, language, 0);
          sb.append(digester).append("</br>");

          valueables.clear();
        }
        break;
      }
      case ActivityPostSpaceProvider: {
        Map<String, String> valueables = new HashMap<String, String>();
        for (NotificationMessage message : messages) {
          String activityId = message.getValueOwnerParameter(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);
          Space space = Utils.getSpaceService().getSpaceByPrettyName(activity.getStreamOwner());
          
          valueables.put("USER", identity.getProfile().getFullName());
          valueables.put("ACTIVITY", activity.getTitle());
          valueables.put("SPACE", space.getPrettyName());
          String digester = templateGenerator.processDigestIntoString(providerId, valueables, language, 0);
          sb.append(digester).append("</br>");

          valueables.clear();
        }
        break;
      }
      case InvitedJoinSpace: {
        Map<String, String> valueables = new HashMap<String, String>();
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
          value.append(space.getPrettyName());
          if (count > (i + 1) && i < 2) {
            value.append(", ");
          }
        }
        valueables.put(key, value.toString());
        if(count > 3) {
          valueables.put("COUNT", String.valueOf((count - 3)));
        }

        String digester = templateGenerator.processDigestIntoString(providerId, valueables, language, count);
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
        sb.append(getMessageByIds(map, providerData, language));
        break;
      }
      case NewUserJoinSocialIntranet: {
        
        break;
      }
      case ReceiceConnectionRequest: {
        Map<String, String> valueables = new HashMap<String, String>();
        int count = messages.size();
        String[] keys = {"USER", "USER_LIST", "LAST3_USERS"};
        String key = "";
        StringBuilder value = new StringBuilder();

        for (int i = 0; i < count && i < 3; i++) {
          Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, messages.get(i).getValueOwnerParameter("sender"), true);
          Profile userProfile = identity.getProfile();
          if (i > 1 && count == 3) {
            key = keys[i - 1];
          } else {
            key = keys[i];
          }
          value.append(userProfile.getFullName());
          if (count > (i + 1) && i < 2) {
            value.append(", ");
          }
        }
        valueables.put(key, value.toString());
        if(count > 3) {
          valueables.put("COUNT", String.valueOf((count - 3)));
        }

        String digester = templateGenerator.processDigestIntoString(providerId, valueables, language, count);
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
        valueables.put("ACTIVITY", activity.getTitle());
      } else {
        valueables.put("SPACE", space.getPrettyName());
      }
      
      String[] keys = {"USER", "USER_LIST", "LAST3_USERS"};
      String key = "";
      StringBuilder value = new StringBuilder();
      
      for (int i = 0; i < count && i <= 3; i++) {
        Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, values.get(i), true);
        Profile userProfile = identity.getProfile();
        if (i > 1 && count == 3) {
          key = keys[i - 1];
        } else {
          key = keys[i];
        }
        value.append(userProfile.getFullName());
        if (count > (i + 1) && i < 2) {
          value.append(", ");
        }
      }
      valueables.put(key, value.toString());
      if(count > 3) {
        valueables.put("COUNT", String.valueOf((count - 3)));
      }

      String digester = templateGenerator.processDigestIntoString(providerData.getType(), valueables, language, count);
      sb.append(digester).append("</br>");
    }
    
    return sb.toString();
  }
  
}
