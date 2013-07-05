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
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.exoplatform.commons.api.notification.MessageInfo;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.ProviderData;
import org.exoplatform.commons.api.notification.ProviderData.DIGEST_TYPE;
import org.exoplatform.commons.api.notification.service.AbstractNotificationProvider;
import org.exoplatform.commons.api.notification.service.ProviderService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
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
                         SpaceService spaceService, ProviderService providerService, OrganizationService organizationService) {
    this.providerService = providerService;
    this.activityManager = activityManager;
    this.identityManager = identityManager;
    this.spaceService = spaceService;
    this.organizationService = organizationService;
  }
  
  @Override
  public MessageInfo buildMessageInfo(NotificationMessage message) {
    MessageInfo messageInfo = new MessageInfo();

    //
    messageInfo.setFrom(getFrom(message)).setTo(getTo(message));

    //
    ProviderData provider = providerService.getProvider(message.getProviderType());
    String language = getLanguage(message);
    String body = getTemplate(provider, language);
    String subject = getSubject(provider, language);

    PROVIDER_TYPE type = PROVIDER_TYPE.valueOf(message.getProviderType());
    try {
      switch (type) {
        case ActivityMentionProvider: {
          String activityId = message.getOwnerParameter().get(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);
          messageInfo.setSubject(subject.replace("$user-who-mentionned", identity.getProfile().getFullName()))
                     .setBody(body.replace("$user-who-mentionned", identity.getProfile().getFullName())
                                  .replace("$post", activity.getTitle())
                                  .replace("$replyAction", LinkProviderUtils.getReplyActivityUrl(activity.getId(), message.getSendToUserIds().get(0)))
                                  .replace("$viewAction", LinkProviderUtils.getViewFullDiscussionUrl(activity.getId(), message.getSendToUserIds().get(0))));
          break;
        }
        case ActivityCommentProvider: {
          String activityId = message.getOwnerParameter().get(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          ExoSocialActivity parentActivity = activityManager.getParentActivity(activity);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);
          messageInfo.setSubject(subject.replace("$other_user_name", identity.getProfile().getFullName()))
                     .setBody(body.replace("$other_user_name", identity.getProfile().getFullName())
                                  .replace("$activity_comment", activity.getTitle())
                                  .replace("$original_activity_message", parentActivity.getTitle())
                                  .replace("$replyAction", LinkProviderUtils.getReplyActivityUrl(activity.getId(), message.getSendToUserIds().get(0)))
                                  .replace("$viewAction", LinkProviderUtils.getViewFullDiscussionUrl(activity.getId(), message.getSendToUserIds().get(0))));
          break;
        }
        case ActivityLikeProvider: {
          String activityId = message.getOwnerParameter().get(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(message.getFrom(), true);
          messageInfo.setSubject(subject.replace("$other_user_name", identity.getProfile().getFullName()))
                     .setBody(body.replace("$other_user_name", identity.getProfile().getFullName())
                                  .replace("$activity", activity.getTitle())
                                  .replace("$replyAction", LinkProviderUtils.getReplyActivityUrl(activity.getId(), message.getSendToUserIds().get(0)))
                                  .replace("$viewAction", LinkProviderUtils.getViewFullDiscussionUrl(activity.getId(), message.getSendToUserIds().get(0))));
          break;
        }
        case ActivityPostProvider: {
          String activityId = message.getOwnerParameter().get(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);
          messageInfo.setSubject(subject.replace("$other_user_name", identity.getProfile().getFullName()))
                     .setBody(body.replace("$other_user_name", identity.getProfile().getFullName())
                                  .replace("$activity_message", activity.getTitle())
                                  .replace("$replyAction", LinkProviderUtils.getReplyActivityUrl(activity.getId(), message.getSendToUserIds().get(0)))
                                  .replace("$viewAction", LinkProviderUtils.getViewFullDiscussionUrl(activity.getId(), message.getSendToUserIds().get(0))));
          break;
        }
        case ActivityPostSpaceProvider: {
          String activityId = message.getOwnerParameter().get(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);
          Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, activity.getStreamOwner(), true);
          messageInfo.setSubject(subject.replace("$other_user_name", identity.getProfile().getFullName()).replace("$space-name", spaceIdentity.getProfile().getFullName()))
                     .setBody(body.replace("$other_user_name", identity.getProfile().getFullName())
                                  .replace("$activity_message", activity.getTitle())
                                  .replace("$space-name", spaceIdentity.getProfile().getFullName())
                                  .replace("$replyAction", LinkProviderUtils.getReplyActivityUrl(activity.getId(), message.getSendToUserIds().get(0)))
                                  .replace("$viewAction", LinkProviderUtils.getViewFullDiscussionUrl(activity.getId(), message.getSendToUserIds().get(0))));
          break;
        }
        case InvitedJoinSpace: {
          String spaceId = message.getOwnerParameter().get(SPACE_ID);
          Space space = spaceService.getSpaceById(spaceId);
          messageInfo.setSubject(subject.replace("$space-name", space.getPrettyName()))
                     .setBody(body.replace("$space-name", space.getPrettyName())
                                  .replace("$space-avatar-url", space.getAvatarUrl())
                                  .replace("$acceptAction", LinkProviderUtils.getAcceptInvitationToJoinSpaceUrl(space.getId(), message.getSendToUserIds().get(0)))
                                  .replace("$ignoreAction", LinkProviderUtils.getIgnoreInvitationToJoinSpaceUrl(space.getId(), message.getSendToUserIds().get(0))));
          break;
        }
        case RequestJoinSpace: {
          String spaceId = message.getOwnerParameter().get(SPACE_ID);
          Space space = spaceService.getSpaceById(spaceId);
          Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, message.getFrom(), true);
          Profile userProfile = identity.getProfile();
          messageInfo.setSubject(subject.replace("$space-name", space.getPrettyName()).replace("$user-name", userProfile.getFullName()))
                     .setBody(body.replace("$space-name", space.getPrettyName())
                                  .replace("$user-name", userProfile.getFullName())
                                  .replace("$user-avatar-url", userProfile.getAvatarUrl())
                                  .replace("$validateAction", LinkProviderUtils.getValidateRequestToJoinSpaceUrl(space.getId(), identity.getRemoteId())));
          break;
        }
        case NewUserJoinSocialIntranet: {
          
          break;
        }
        case ReceiceConnectionRequest: {
          Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, message.getFrom(), true);
          Profile userProfile = identity.getProfile();
          messageInfo.setSubject(subject.replace("$user-name", userProfile.getFullName()))
                     .setBody(body.replace("$user-name", userProfile.getFullName())
                                  .replace("$user-avatar-url", userProfile.getAvatarUrl())
                                  .replace("$confirmAction", LinkProviderUtils.getConfirmInvitationToConnectUrl(message.getFrom(), message.getSendToUserIds().get(0)))
                                  .replace("$ignoreAction", LinkProviderUtils.getIgnoreInvitationToConnectUrl(message.getFrom(), message.getSendToUserIds().get(0))));
          break;
        }
      }
    } catch (Exception e) {
      LOG.error("Can not build template of SocialProviderImpl by Provider " + provider.getType(), e );
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
    String language = getLanguage(notificationMessage);
    PROVIDER_TYPE type = PROVIDER_TYPE.valueOf(providerData.getType());
    Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
    
    switch (type) {
      case ActivityMentionProvider: {
        for (NotificationMessage message : messages) {
          String digester = providerData.getDigester(language);
          String activityId = message.getOwnerParameter().get(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);
          sb.append(digester.replace("$user1-fullname", identity.getProfile().getFullName())
                            .replace("$activity", activity.getTitle()))
            .append("</br>");
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
            map.put(parentActivityId, Arrays.asList(message.getFrom()));
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
            map.put(activityId, Arrays.asList(message.getFrom()));
          }
        }
        sb.append(getMessageByIds(map, providerData, language));
        break;
      }
      case ActivityPostProvider: {
        for (NotificationMessage message : messages) {
          String digester = providerData.getDigester(language);
          String activityId = message.getOwnerParameter().get(ACTIVITY_ID);
          ExoSocialActivity activity = activityManager.getActivity(activityId);
          Identity identity = identityManager.getIdentity(activity.getPosterId(), true);
          sb.append(digester.replace("$user1-fullname", identity.getProfile().getFullName())
                            .replace("$activity", activity.getTitle()))
            .append("</br>");
        }
        break;
      }
      case ActivityPostSpaceProvider: {
        break;
      }
      case InvitedJoinSpace: {
        int count = messages.size();
        String digester = "";
        switch (count) {
          case 1: {
            digester = providerData.getDigester(language, DIGEST_TYPE.ONE);
            String spaceId = messages.get(0).getOwnerParameter().get(SPACE_ID);
            Space space = spaceService.getSpaceById(spaceId);
            sb.append(digester.replace("@space-name", space.getPrettyName())).append("</br>");
            break;
          }
          case 2: {
            digester = providerData.getDigester(language, DIGEST_TYPE.THREE);
            String space1Id = messages.get(0).getOwnerParameter().get(SPACE_ID);
            Space space1 = spaceService.getSpaceById(space1Id);
            String space2Id = messages.get(1).getOwnerParameter().get(SPACE_ID);
            Space space2 = spaceService.getSpaceById(space2Id);
            sb.append(digester.replace("@space1-name", space1.getPrettyName())
                              .replace(", @space1-name", "")
                              .replace("@space3-name", space2.getPrettyName())).append("</br>");
            break;
          }
          case 3: {
            digester = providerData.getDigester(language, DIGEST_TYPE.THREE);
            String space1Id = messages.get(0).getOwnerParameter().get(SPACE_ID);
            Space space1 = spaceService.getSpaceById(space1Id);
            String space2Id = messages.get(1).getOwnerParameter().get(SPACE_ID);
            Space space2 = spaceService.getSpaceById(space2Id);
            String space3Id = messages.get(2).getOwnerParameter().get(SPACE_ID);
            Space space3 = spaceService.getSpaceById(space3Id);
            sb.append(digester.replace("@space1-name", space1.getPrettyName())
                              .replace("@space2-name", space2.getPrettyName())
                              .replace("@space3-name", space3.getPrettyName())).append("</br>");
            break;
          }
          default: {
            digester = providerData.getDigester(language, DIGEST_TYPE.MORE);
            String space1Id = messages.get(0).getOwnerParameter().get(SPACE_ID);
            Space space1 = spaceService.getSpaceById(space1Id);
            String space2Id = messages.get(1).getOwnerParameter().get(SPACE_ID);
            Space space2 = spaceService.getSpaceById(space2Id);
            String space3Id = messages.get(2).getOwnerParameter().get(SPACE_ID);
            Space space3 = spaceService.getSpaceById(space3Id);
            sb.append(digester.replace("@space1-name", space1.getPrettyName())
                              .replace("@space2-name", space2.getPrettyName())
                              .replace("@space3-name", space3.getPrettyName())
                              .replace("$number-others", "" + (count - 3))).append("</br>");
            break;
          }
        }
      }
      case RequestJoinSpace: {
        for (NotificationMessage message : messages) {
          String spaceId = messages.get(0).getOwnerParameter().get(SPACE_ID);
          if (map.containsKey(spaceId)) {
            List<String> values = map.get(spaceId);
            values.add(message.getFrom());
            map.put(spaceId, values);
          } else {
            map.put(spaceId, Arrays.asList(message.getFrom()));
          }
        }
        sb.append(getMessageByIds(map, providerData, language));
        break;
      }
      case NewUserJoinSocialIntranet: {
        
        break;
      }
      case ReceiceConnectionRequest: {
        int count = messages.size();
        String digester = "";
        switch (count) {
          case 1: {
            digester = providerData.getDigester(language, DIGEST_TYPE.ONE);
            Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, messages.get(0).getFrom(), true);
            Profile userProfile = identity.getProfile();
            sb.append(digester.replace("$user1-fullname", userProfile.getFullName())).append("</br>");
            break;
          }
          case 2: {
            digester = providerData.getDigester(language, DIGEST_TYPE.THREE);
            Identity identity1 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, messages.get(0).getFrom(), true);
            Profile user1 = identity1.getProfile();
            Identity identity2 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, messages.get(1).getFrom(), true);
            Profile user2 = identity2.getProfile();
            sb.append(digester.replace("$user1-fullname", user1.getFullName())
                              .replace(", $user2-fullname", "")
                              .replace("$user3-fullname", user2.getFullName())).append("</br>");
            break;
          }
          case 3: {
            digester = providerData.getDigester(language, DIGEST_TYPE.THREE);
            Identity identity1 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, messages.get(0).getFrom(), true);
            Profile user1 = identity1.getProfile();
            Identity identity2 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, messages.get(1).getFrom(), true);
            Profile user2 = identity2.getProfile();
            Identity identity3 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, messages.get(2).getFrom(), true);
            Profile user3 = identity3.getProfile();
            sb.append(digester.replace("$user1-fullname", user1.getFullName())
                              .replace("$user2-fullname", user2.getFullName())
                              .replace("$user3-fullname", user3.getFullName())).append("</br>");
            break;
          }
          default: {
            digester = providerData.getDigester(language, DIGEST_TYPE.MORE);
            Identity identity1 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, messages.get(0).getFrom(), true);
            Profile user1 = identity1.getProfile();
            Identity identity2 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, messages.get(1).getFrom(), true);
            Profile user2 = identity2.getProfile();
            Identity identity3 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, messages.get(2).getFrom(), true);
            Profile user3 = identity3.getProfile();
            sb.append(digester.replace("$user1-fullname", user1.getFullName())
                              .replace("$user2-fullname", user2.getFullName())
                              .replace("$user3-fullname", user3.getFullName())
                              .replace("$number-others", "" + (count - 3))).append("</br>");
            break;
          }
        }
      }
    }
    
    return sb.toString();
  }

  private String getMessageByIds(Map<String, List<String>> map, ProviderData providerData, String language) {
    StringBuilder sb = new StringBuilder();
    for (Entry<String, List<String>> entry : map.entrySet()) {
      String id = entry.getKey();
      ExoSocialActivity activity = activityManager.getActivity(id);
      Space space = spaceService.getSpaceById(id);
      List<String> values = entry.getValue();
      int count = values.size();
      String digester = "";
      switch (count) {
        case 1: {
          digester = providerData.getDigester(language, DIGEST_TYPE.ONE);
          Identity identity1 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, values.get(0), true);
          Profile user1 = identity1.getProfile();
          if (activity != null) {
            sb.append(digester.replace("$activity", activity.getTitle()));
          } else {
            sb.append(digester.replace("$space-name", space.getPrettyName()));
          }
          sb.append(digester.replace("$user1-fullname", user1.getFullName())).append("</br>");
          break;
        }
        case 2: {
          digester = providerData.getDigester(language, DIGEST_TYPE.THREE);
          if (activity != null) {
            sb.append(digester.replace("$activity", activity.getTitle()));
          } else {
            sb.append(digester.replace("$space-name", space.getPrettyName()));
          }
          Identity identity1 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, values.get(0), true);
          Profile user1 = identity1.getProfile();
          Identity identity2 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, values.get(1), true);
          Profile user2 = identity2.getProfile();
          sb.append(digester.replace("$user1-fullname", user1.getFullName())
                            .replace(", $user2-fullname", "")
                            .replace("$user3-fullname", user2.getFullName())).append("</br>");
          break;
        }
        case 3: {
          digester = providerData.getDigester(language, DIGEST_TYPE.THREE);
          if (activity != null) {
            sb.append(digester.replace("$activity", activity.getTitle()));
          } else {
            sb.append(digester.replace("$space-name", space.getPrettyName()));
          }
          Identity identity1 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, values.get(0), true);
          Profile user1 = identity1.getProfile();
          Identity identity2 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, values.get(1), true);
          Profile user2 = identity2.getProfile();
          Identity identity3 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, values.get(2), true);
          Profile user3 = identity3.getProfile();
          sb.append(digester.replace("$user1-fullname", user1.getFullName())
                            .replace("$user2-fullname", user2.getFullName())
                            .replace("$user3-fullname", user3.getFullName())).append("</br>");
          break;
        }
        default: {
          digester = providerData.getDigester(language, DIGEST_TYPE.MORE);
          if (activity != null) {
            sb.append(digester.replace("$activity", activity.getTitle()));
          } else {
            sb.append(digester.replace("$space-name", space.getPrettyName()));
          }
          Identity identity1 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, values.get(0), true);
          Profile user1 = identity1.getProfile();
          Identity identity2 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, values.get(1), true);
          Profile user2 = identity2.getProfile();
          Identity identity3 = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, values.get(2), true);
          Profile user3 = identity3.getProfile();
          sb.append(digester.replace("$user1-fullname", user1.getFullName())
                            .replace("$user2-fullname", user2.getFullName())
                            .replace("$user3-fullname", user3.getFullName())
                            .replace("$number-others", "" + (count - 3))).append("</br>");
          break;
        }
      }
    }
    
    return sb.toString();
  }
}
