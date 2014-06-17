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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.exoplatform.commons.api.notification.service.storage.NotificationDataStorage;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class Utils {
  
  private static final Pattern MENTION_PATTERN = Pattern.compile("<a href=\"([\\w|/]+|)/profile/([\\w]+)\">([\\w|\\s]+|)</a>");
  
  private static final int MAX_LENGTH = 150;
  
  private static final Pattern LINK_PATTERN = Pattern.compile("<a ([^>]+)>([^<]+)</a>");
  
  private static final Pattern HREF_PATTERN = Pattern.compile("href=\"(.*?)\"");
  
  private static final String SLASH_STR = "/";
  
  private static final Pattern URL_PATTERN = Pattern
      .compile("^(?i)" +
      "(" +
        "((?:(?:ht)tp(?:s?)\\:\\/\\/)?" +                                                       // protolcol
        "(?:\\w+:\\w+@)?" +                                                                       // username password
        "(((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\." +  // IPAddress
        "(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))|" +     // IPAddress
        "(?:(?:[-\\p{L}\\p{Digit}\\+\\$\\-\\*\\=]+\\.)+" +
        "(?:com|org|net|edu|gov|mil|biz|info|mobi|name|aero|jobs|museum|travel|asia|cat|coop|int|pro|tel|xxx|[a-z]{2}))))|" + //Domain
        "(?:(?:(?:ht)tp(?:s?)\\:\\/\\/)(?:\\w+:\\w+@)?(?:[-\\p{L}\\p{Digit}\\+\\$\\-\\*\\=]+))" + // Protocol with hostname
      ")" +
      "(?::[\\d]{1,5})?" +                                                                        // port
      "(?:[\\/|\\?|\\#].*)?$");                                                               // path and query
  
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
  
  public static void sendToCommeters(Set<String> receivers, String[] commenters, String poster) {
    receivers.addAll(getDestinataires(commenters, poster));
  }
  
  /**
   * Checks if a notification message is sent to a stream owner or not.
   * @param receivers The list of users receiving the notification message.
   * @param streamOwner The owner of activity stream.
   * @param posteId Id of the user who has posted the activity.
   */
  public static void sendToStreamOwner(Set<String> receivers, String streamOwner, String posteId) {
    //Don't send to the stream owner when it's a space
    Identity id = getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, streamOwner, false);
    if (id != null) 
      return;
    
    String postRemoteId = Utils.getUserId(posteId);
    if (streamOwner.equals(postRemoteId) == false) {
      receivers.add(streamOwner);
    }
  }
  
  /**
   * Checks if a notification message is sent to an activity poster when a new comment is created.
   * @param receivers The list of users receiving the notification message.
   * @param activityPosterId Id of the activity poster.
   * @param posteId Id of the user who has commented.
   */
  public static void sendToActivityPoster(Set<String> receivers, String activityPosterId, String posteId) {
    String activityPosterRemoteId = Utils.getUserId(activityPosterId);
    if (activityPosterId.equals(posteId) == false) {
      receivers.add(activityPosterRemoteId);
    }
  }
  
  public static void sendToMentioners(Set<String> receivers, String[] mentioners, String poster) {
    receivers.addAll(getDestinataires(mentioners, poster));
  }
  
  /**
   * Gets remote Ids of all users who receive a notification message.
   * 
   * @param users The list of all users related to the activity.
   * @param poster The user who has posted the activity or comment.
   * @return The remote Ids.
   */
  private static Set<String> getDestinataires(String[] users, String poster) {
    Set<String> destinataires = new HashSet<String>();
    for (String user : users) {
      user = user.split("@")[0];
      String userName = getUserId(user);
      if (! user.equals(poster)) {
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
    String domain = CommonsUtils.getCurrentDomain();
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
   * Gets the list of mentioners in a message that is not the poster
   * 
   * @param title the activity title
   * @param posterId id of the poster
   * @return list of mentioners
   */
  public static Set<String> getMentioners(String title, String posterId) {
    String posterRemoteId = getUserId(posterId);
    Set<String> mentioners = new HashSet<String>();
    Matcher matcher = MENTION_PATTERN.matcher(title);
    while (matcher.find()) {
      String remoteId = matcher.group(2);
      Identity identity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId, false);
      if (identity != null && posterRemoteId.equals(remoteId) == false) { 
        mentioners.add(remoteId);
      }
    }
    return mentioners;
  }
  
  public static String processLinkTitle(String title) {
    Matcher matcher = LINK_PATTERN.matcher(title);
    String domain = CommonsUtils.getCurrentDomain();
    while (matcher.find()) {
      String result = matcher.group(1);
      title = title.replace(result, result + styleCSS);
      Matcher m = HREF_PATTERN.matcher(result);
      if (m.find()) {
        String url = m.group(1);
        if (url != null && !isValidUrl(url)) {
          String newUrl = url.startsWith(SLASH_STR) ? domain + url : domain + SLASH_STR + url;
          if (isValidUrl(newUrl)) {
            title = title.replace(url, newUrl);
          }
        }
      }
    }
    
    return title;
  }

  /**
   * Validates URL.
   * 
   * @param url string to validate
   * @return true if url is valid (url started with http/https/www/ftp ...)
   */
  public static boolean isValidUrl(String url) {
    return URL_PATTERN.matcher(url).matches();  
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
  
  /**
   * Get 150 first characters of a string
   * 
   * @param content
   * @return
   */
  public static String formatContent(String content) {
    if (content.length() > MAX_LENGTH) {
      content = content.substring(0, MAX_LENGTH) + " ... ";
    }
    return content;
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

  public static RelationshipManager getRelationshipManager() {
    return getService(RelationshipManager.class);
  }
}
