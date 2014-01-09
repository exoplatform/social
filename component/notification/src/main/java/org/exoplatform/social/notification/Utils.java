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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.notification;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.exoplatform.commons.api.notification.service.storage.NotificationDataStorage;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class Utils {
  
  private static final Pattern MENTION_PATTERN = Pattern.compile("<a href=\"([\\w|/]+|)/profile/([\\w]+)\">([\\w|\\s]+|)</a>");
  
  private static final Pattern LINK_PATTERN = Pattern.compile("<a ([^>]+)>([^<]+)</a>");
  
  private static final String styleCSS = " style=\"color: #2f5e92; text-decoration: none;\"";
  
  @SuppressWarnings("unchecked")
  public static <T> T getService(Class<T> clazz) {
    return (T) PortalContainer.getInstance().getComponentInstanceOfType(clazz);
  }
  
  public static NotificationDataStorage getSocialEmailStorage() {
    return getService(NotificationDataStorage.class);
  }

  /**
   * Gets a remote Id from a user's identity Id.
   * 
   * @param identityId The user's identity Id.
   * @return The remote Id.
   */
  public static String getUserId(String identityId) {
    return getIdentityManager().getIdentity(identityId, false).getRemoteId();
  }
  
  /**
   * Converts an array of remote user Ids into a list.
   * 
   * @param userIds The remote user Ids.
   * @return The list of remote Ids.
   */
  public static List<String> toListUserIds(String... userIds) {
    List<String> ids = new ArrayList<String>();

    for (String userId : userIds) {
      ids.add(userId);
    }
    
    return ids;
  }
  
  /**
   * Checks if an activity is created in a space or not.
   * 
   * @param activity The activity to be checked.
   * @return The returned value is "true" if the activity is created, or "false" if the activity is not created.
   */
  public static boolean isSpaceActivity(ExoSocialActivity activity) {
    Identity id = getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, activity.getStreamOwner(), false);
    return (id != null);
  }
  
  public static void sendToCommeters(List<String> receivers, String[] commenters, String poster) {
    receivers.addAll(getDestinataires(commenters, poster));
  }
  
  /**
   * Checks if a notification message is sent to a stream owner or not.
   * @param receivers The list of users receiving the notification message.
   * @param streamOwner The owner of activity stream.
   * @param posteId Id of the user who has posted the activity.
   */
  public static void sendToStreamOwner(List<String> receivers, String streamOwner, String posteId) {
    //Don't send to the stream owner when it's a space
    Identity id = getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, streamOwner, false);
    if (id != null) 
      return;
    
    String postRemoteId = Utils.getUserId(posteId);
    if (streamOwner.equals(postRemoteId) == false) {
      if (receivers.contains(streamOwner) == false) {
        receivers.add(streamOwner);
      }
        
    }
  }
  
  /**
   * Checks if a notification message is sent to an activity poster when a new comment is created.
   * @param receivers The list of users receiving the notification message.
   * @param activityPosterId Id of the activity poster.
   * @param posteId Id of the user who has commented.
   */
  public static void sendToActivityPoster(List<String> receivers, String activityPosterId, String posteId) {
    String activityPosterRemoteId = Utils.getUserId(activityPosterId);
    if (activityPosterId.equals(posteId) == false) {
      if (receivers.contains(activityPosterRemoteId) == false) {
        receivers.add(activityPosterRemoteId);
      }
        
    }
  }
  
  public static void sendToMentioners(List<String> receivers, String[] mentioners, String poster) {
    receivers.addAll(getDestinataires(mentioners, poster));
  }
  
  /**
   * Gets remote Ids of all users who receive a notification message.
   * 
   * @param users The list of all users related to the activity.
   * @param poster The user who has posted the activity or comment.
   * @return The remote Ids.
   */
  private static List<String> getDestinataires(String[] users, String poster) {
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
  
  /**
   * Finds all mention prefixes from an activity title, then adds a domain name to ensure the link to a user profile is correct.
   * 
   * @param title The activity title.
   * @return The new title which contains the correct link to the user profile. 
   */
  public static String processMentions(String title) {
    Matcher matcher = MENTION_PATTERN.matcher(title);
    String domain = System.getProperty("gatein.email.domain.url", "http://localhost:8080");
    while (matcher.find()) {
      String remoteId = matcher.group(2);
      Identity identity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId, false);
      // if not the right mention then ignore
      if (identity != null) { 
        String result = matcher.group();
        String host = matcher.group(1);
        title = title.replace(result, result.replace(host, domain + host));
      }
    }
    return title;
  }
  
  /**
   * Add the style css for a link in the activity title to display a link without underline
   * 
   * @param title activity title
   * @return activity title after process all link
   */
  public static String processLinkTitle(String title) {
    Matcher matcher = LINK_PATTERN.matcher(title);
    while (matcher.find()) {
      String result = matcher.group(1);
      title = title.replace(result, result + styleCSS);
    }
    return title;
  }
  
  /**
   * Gets remote Ids of all users who receive a notification message when an activity is posted in a space.
   * 
   * @param activity The created activity.
   * @param space The space which contains the activity.
   * @return The remote Ids.
   */
  public static List<String> getDestinataires(ExoSocialActivity activity, Space space) {
    List<String> destinataires = new ArrayList<String>();
    String poster = getUserId(activity.getPosterId());
    for (String member : space.getMembers()) {
      if (! member.equals(poster))
        destinataires.add(member);
    }
    return destinataires;
  }
  
  public static IdentityManager getIdentityManager() {
    return getService(IdentityManager.class);
  }
  
  public static SpaceService getSpaceService() {
    return getService(SpaceService.class);
  }
  
  public static ActivityManager getActivityManager() {
    return getService(ActivityManager.class);
  }
  
}
