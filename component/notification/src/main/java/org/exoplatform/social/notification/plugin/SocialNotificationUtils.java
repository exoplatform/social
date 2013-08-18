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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;

public class SocialNotificationUtils {

  public final static ArgumentLiteral<String> ACTIVITY_ID = new ArgumentLiteral<String>(String.class, "activityId");
  public final static ArgumentLiteral<String> POSTER = new ArgumentLiteral<String>(String.class, "poster");
  public final static ArgumentLiteral<ExoSocialActivity> ACTIVITY = new ArgumentLiteral<ExoSocialActivity>(ExoSocialActivity.class, "activity");
  public final static ArgumentLiteral<Profile> PROFILE = new ArgumentLiteral<Profile>(Profile.class, "profile");
  public final static ArgumentLiteral<Space> SPACE = new ArgumentLiteral<Space>(Space.class, "space");
  public final static ArgumentLiteral<String> REMOTE_ID = new ArgumentLiteral<String>(String.class, "remoteId");
  public final static ArgumentLiteral<String> SPACE_ID = new ArgumentLiteral<String>(String.class, "spaceId");
  public final static ArgumentLiteral<String> PRETTY_NAME = new ArgumentLiteral<String>(String.class, "prettyName");
  public final static ArgumentLiteral<Relationship> RELATIONSHIP = new ArgumentLiteral<Relationship>(Relationship.class, "relationship");
  
  public final static ArgumentLiteral<String> RELATIONSHIP_ID = new ArgumentLiteral<String>(String.class, "relationshipId");

  
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
  
  public static List<String> getDestinataires(String[] users, String poster) {
    List<String> destinataires = new ArrayList<String>();
    for (String user : users) {
      user = user.split("@")[0];
      String userName = getUserId(user);
      if (! destinataires.contains(userName) && ! user.equals(poster)) {
        destinataires.add(userName);
      }
    }
    return destinataires;
  }
  
  public static List<String> getDestinataires(ExoSocialActivity activity, Space space) {
    List<String> destinataires = Arrays.asList(space.getMembers());
    destinataires.remove(getUserId(activity.getPosterId()));
    return destinataires;
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
    sb.append("<ul style=\"margin: 0 0  40px; padding-left: 0; list-style-position: outside;\">");
    for (Entry<String, List<String>> entry : receiversMap.entrySet()) {
      sb.append("<li style=\"margin: 0 0 13px 14px; font-size: 13px; list-style: disc; line-height: 18px; font-family: HelveticaNeue, Helvetica, Arial, sans-serif;\">");
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
        templateContext.put("ACTIVITY", SocialNotificationUtils.buildRedirecUrl(typeActivityDisplay, activity.getId(), Utils.processMentions(activity.getTitle())));
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
          templateContext.put("COUNT", SocialNotificationUtils.buildRedirecUrl(typeActivityDisplay, activity.getId(), String.valueOf((count - 3))));
        } else {
          templateContext.put("COUNT", SocialNotificationUtils.buildRedirecUrl(typeSpaceDisplay, space.getId(), String.valueOf((count - 3))));
        }
      }

      String digester = TemplateUtils.processDigest(templateContext.digestType(count));
      sb.append(digester);
      sb.append("</li>");
    }
    
    sb.append("</ul>");
    
    return sb.toString();
  }
  
  public static void processInforSendTo(Map<String, List<String>> map, String key, String value) {
    Set<String> set = new HashSet<String>();
    if (map.containsKey(key)) {
      set.addAll(map.get(key));
    }
    set.add(value);
    map.put(key, new ArrayList<String>(set));
  }
  
  public static String buildRedirecUrl(String type, String id, String name) {
    String link = LinkProviderUtils.getRedirectUrl(type, id);
    return "<a target=\"_blank\" style=\"text-decoration: none; font-weight: bold; color: #2f5e92; font-family: 'HelveticaNeue Bold', Helvetica, Arial, sans-serif; font-size: 13px; line-height: 18px;\" href=\""+ link + "\">" + name + "</a>";
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
  
}
