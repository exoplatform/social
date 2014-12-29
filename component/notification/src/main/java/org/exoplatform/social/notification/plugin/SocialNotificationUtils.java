/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.social.notification.plugin;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationChildPlugin;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.api.notification.service.setting.PluginContainer;
import org.exoplatform.commons.api.notification.service.storage.WebNotificationStorage;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.notification.plugin.child.DefaultActivityChildPlugin;

public class SocialNotificationUtils {

  public final static ArgumentLiteral<String> ACTIVITY_ID = new ArgumentLiteral<String>(String.class, "activityId");
  public final static ArgumentLiteral<String> COMMENT_ID = new ArgumentLiteral<String>(String.class, "commentId");
  public final static ArgumentLiteral<String> PARENT_ACTIVITY_ID = new ArgumentLiteral<String>(String.class, "parentActivityId");
  public final static ArgumentLiteral<String> POSTER = new ArgumentLiteral<String>(String.class, "poster");
  public final static ArgumentLiteral<String> SENDER = new ArgumentLiteral<String>(String.class, "sender");
  public final static ArgumentLiteral<ExoSocialActivity> ACTIVITY = new ArgumentLiteral<ExoSocialActivity>(ExoSocialActivity.class, "activity");
  public final static ArgumentLiteral<Profile> PROFILE = new ArgumentLiteral<Profile>(Profile.class, "profile");
  public final static ArgumentLiteral<Space> SPACE = new ArgumentLiteral<Space>(Space.class, "space");
  public final static ArgumentLiteral<String> REMOTE_ID = new ArgumentLiteral<String>(String.class, "remoteId");
  public final static ArgumentLiteral<String> SPACE_ID = new ArgumentLiteral<String>(String.class, "spaceId");
  public final static ArgumentLiteral<String> PRETTY_NAME = new ArgumentLiteral<String>(String.class, "prettyName");
  public final static ArgumentLiteral<Relationship> RELATIONSHIP = new ArgumentLiteral<Relationship>(Relationship.class, "relationship");
  
  public final static ArgumentLiteral<String> RELATIONSHIP_ID = new ArgumentLiteral<String>(String.class, "relationshipId");
  
  public static final String EMPTY_STR = "";

  public static final String SPACE_STR     = " ";
  
  public static final String DOT_STRING       = ".";
  public static final String A_HREF_TAG_REGEX = "</?a[^>]*>";
  
  
  public static String getUserId(String identityId) {
    return Utils.getIdentityManager().getIdentity(identityId, false).getRemoteId();
  }
  
  public static List<String> toListUserIds(String... userIds) {
    List<String> ids = new ArrayList<String>();

    for (String userId : userIds) {
      ids.add(userId);
    }
    
    return ids;
  }
  
  public static boolean isSpaceActivity(ExoSocialActivity activity) {
    Identity id = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, activity.getStreamOwner(), false);
    return (id != null);
  }
  
  /**
   * 
   * @param receiversMap
   * @param templateContext
   * @return
   */
  public static String getMessageByIds(Map<String, List<String>> receiversMap, TemplateContext templateContext) {
    StringBuilder sb = new StringBuilder();
    ExoSocialActivity activity = null;
    Space space = null;
    for (Entry<String, List<String>> entry : receiversMap.entrySet()) {
      sb.append("<li style=\"margin: 0 0 13px 14px; font-size: 13px; line-height: 18px; font-family: HelveticaNeue, Helvetica, Arial, sans-serif;\">");
      String id = entry.getKey();
      try {
        activity = Utils.getActivityManager().getActivity(id);
        space = null;
      } catch (Exception e) {
        space = Utils.getSpaceService().getSpaceById(id);
        activity = null;
      }
      List<String> values = entry.getValue();
      int count = values.size();

      String typeActivityDisplay = (templateContext.getPluginId().equals("LikePlugin")) ? "view_likers_activity" : "view_full_activity";
      String typeSpaceDisplay = (templateContext.getPluginId().equals("PostActivitySpaceStreamPlugin")) ? "space" : "space_members";
      if (activity != null) {
        String title = activity.getTitle();
        // removes a href link from title. Just for digest building case.
        title = title.replaceAll(A_HREF_TAG_REGEX, ""); 
        if (!title.endsWith(DOT_STRING)) {
          title = title + DOT_STRING; 
        }
        templateContext.put("ACTIVITY", SocialNotificationUtils.buildRedirecActivityUrl(typeActivityDisplay, activity.getId(), title));
      } else {
        templateContext.put("SPACE", SocialNotificationUtils.buildRedirecUrl(typeSpaceDisplay, space.getId(), space.getDisplayName()));
      }
      
      String[] keys = {"USER", "USER_LIST", "LAST3_USERS"};
      String key = "";
      StringBuilder value = new StringBuilder();
      
      for (int i = 0; i < count && i < 3; i++) {
        Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, values.get(i), true);
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
      templateContext.put(key, value.toString());
      if(count > 3) {
        if (activity != null) {
          templateContext.put("COUNT", SocialNotificationUtils.buildRedirecActivityUrl(typeActivityDisplay, activity.getId(), String.valueOf((count - 3))));
        } else {
          templateContext.put("COUNT", SocialNotificationUtils.buildRedirecUrl(typeSpaceDisplay, space.getId(), String.valueOf((count - 3))));
        }
      }

      String digester = TemplateUtils.processDigest(templateContext.digestType(count));
      sb.append(digester);
      sb.append("</li>");
    }
    
    return sb.toString();
  }

  /**
   * 
   * @param receiversMap
   * @param templateContext
   * @return
   */
  public static String getMessageByIds(Map<String, List<String>> receiversMap, TemplateContext templateContext, String type) {
    StringBuilder sb = new StringBuilder();
    for (Entry<String, List<String>> entry : receiversMap.entrySet()) {
      sb.append("<li style=\"margin: 0 0 13px 14px; font-size: 13px; line-height: 18px; font-family: HelveticaNeue, Helvetica, Arial, sans-serif;\">");
      String targetId = entry.getKey();
      List<String> values = entry.getValue();
      int count = values.size();

      String[] keys = new String[]{"USER", "USER_LIST", "LAST3_USERS"};
      if ("space".equals(type)) {
        keys = new String[]{"SPACE", "SPACE_LIST", "LAST3_SPACES"};
      }
      String key = "";
      StringBuilder value = new StringBuilder();

      for (int i = 0; i < count && i < 3; i++) {
        String name = "";
        if ("new_user".equals(type) || "user".equals(type) || "connections_request".equals(type)) {
          Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, values.get(i), true);
          name = identity.getProfile().getFullName();
        } else {
          Space space = Utils.getSpaceService().getSpaceById(values.get(i));
          name = space.getDisplayName();
        }
        //
        if (i > 1 && count == 3) {
          key = keys[i - 1];
        } else {
          key = keys[i];
        }
        value.append(SocialNotificationUtils.buildRedirecUrl("new_user".equals(type) ? "user" : type, values.get(i), name));
        if (count > (i + 1) && i < 2) {
          value.append(", ");
        }
      }
      templateContext.put(key, value.toString());
      if(count > 3) {
        if ("user".equals(type)) {
          templateContext.put("COUNT", SocialNotificationUtils.buildRedirecUrl("user_activity_stream", targetId, String.valueOf((count - 3))));
          templateContext.put("ACTIVITY_STREAM", LinkProviderUtils.getRedirectUrl("user_activity_stream", targetId));
        } else if ("space".equals(type)) {
          templateContext.put("COUNT", SocialNotificationUtils.buildRedirecUrl("space_invitation", targetId, String.valueOf((count - 3))));
        } else if ("new_user".equals(type)) {
          templateContext.put("COUNT", SocialNotificationUtils.buildRedirecUrl("connections", "all", String.valueOf((count - 3))));
        } else {
          templateContext.put("COUNT", SocialNotificationUtils.buildRedirecUrl("connections_request", targetId, String.valueOf((count - 3))));
        }
      } else {
        if ("user".equals(type)) {
          templateContext.put("ACTIVITY_STREAM", LinkProviderUtils.getRedirectUrl("user_activity_stream", targetId));
        }
      }

      String digester = TemplateUtils.processDigest(templateContext.digestType(count));
      sb.append(digester);
      sb.append("</li>");
    }
    
    return sb.toString();
  }
  
  public static void processInforSendTo(Map<String, List<String>> map, String key, String value) {
    List<String> list = new LinkedList<String>();
    if (map.containsKey(key)) {
      list.addAll(map.get(key));
    }
    if (list.contains(value) == false) {
      list.add(value);
    }
    map.put(key, new ArrayList<String>(list));
  }
  
  public static String buildRedirecUrl(String type, String id, String name) {
    String link = LinkProviderUtils.getRedirectUrl(type, id);
    return "<a target=\"_blank\" style=\"text-decoration: none; font-weight: bold; color: #2f5e92; font-family: 'HelveticaNeue Bold', Helvetica, Arial, sans-serif; font-size: 13px; line-height: 18px;\" href=\""+ link + "\">" + name + "</a>";
  }
  
  public static String buildRedirecActivityUrl(String type, String id, String activityTitle) {
    String link = LinkProviderUtils.getRedirectUrl(type, id);
    return "<a target=\"_blank\" style=\"text-decoration: none; color: #2f5e92; font-family: 'HelveticaNeue Bold', Helvetica, Arial, sans-serif; font-size: 13px; line-height: 18px;\" href=\""+ link + "\">" + activityTitle + "</a>";
  }
  
  public static void addFooterAndFirstName(String remoteId, TemplateContext templateContext) {
    try {
      Identity receiver = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId, true);
      templateContext.put("FIRSTNAME", (String) receiver.getProfile().getProperty(Profile.FIRST_NAME));
      templateContext.put("FOOTER_LINK", LinkProviderUtils.getRedirectUrl("notification_settings", receiver.getRemoteId()));
    } catch (Exception e) {
      return;
    }
  }
  
  public static String getBody(NotificationContext ctx, TemplateContext context, ExoSocialActivity activity) {
    PluginKey childKey = new PluginKey(activity.getType());
    PluginContainer pluginContainer = CommonsUtils.getService(PluginContainer.class);
    BaseNotificationPlugin child = pluginContainer.getPlugin(childKey);
    if (child == null || (child instanceof AbstractNotificationChildPlugin) == false) {
      child = pluginContainer.getPlugin(new PluginKey(DefaultActivityChildPlugin.ID));
    }
    context.put("ACTIVITY", ((AbstractNotificationChildPlugin) child).makeContent(ctx));

    return TemplateUtils.processGroovy(context);
  }
  
  public static List<String> mergeUsers(NotificationContext ctx, TemplateContext context, String propertyName, String activityId, String userId) {
    NotificationInfo notification = ctx.getNotificationInfo();
    List<String> users = null;
    if (ctx.isWritingProcess()) {
      WebNotificationStorage storage = CommonsUtils.getService(WebNotificationStorage.class); 
      NotificationInfo previousNotification = storage.getUnreadNotification(notification.getKey().getId(), activityId, notification.getTo());
      if (previousNotification != null) {
        users = NotificationUtils.stringToList(previousNotification.getValueOwnerParameter(propertyName));
        Identity userIdentity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, true);
        if (users.contains(userIdentity.getRemoteId())) {
          users.remove(userIdentity.getRemoteId());
        }
        users.add(userIdentity.getRemoteId());
        previousNotification.with(propertyName, NotificationUtils.listToString(users));
        previousNotification.setLastModifiedDate(Calendar.getInstance());
        context.put("NOTIFICATION_ID", previousNotification.getId());
        ctx.setNotificationInfo(previousNotification);
      } else {
        users = NotificationUtils.stringToList(notification.getValueOwnerParameter(propertyName));
      }
      ctx.setWritingProcess(false);
    } else {
      users = NotificationUtils.stringToList(notification.getValueOwnerParameter(propertyName));
    }
    
    return users;
  }
}
