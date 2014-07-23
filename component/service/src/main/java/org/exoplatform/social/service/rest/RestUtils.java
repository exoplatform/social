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
package org.exoplatform.social.service.rest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class RestUtils {

  public static final int    DEFAULT_LIMIT           = 20;

  public static final int    DEFAULT_OFFSET          = 0;
  
  public static final int    HARD_LIMIT              = 50;

  public static final String USERS_TYPE              = "users";

  public static final String USERS_RELATIONSHIP_TYPE = "usersRelationships";

  public static final String USER_ACTIVITY_TYPE      = "user";

  public static final String IDENTITIES_TYPE         = "identities";

  public static final String SPACES_TYPE             = "spaces";
  
  public static final String SPACES_MEMBERSHIP_TYPE  = "spacesMemberships";

  public static final String SPACE_ACTIVITY_TYPE     = "space";

  public static final String ACTIVITIES_TYPE         = "activities";

  public static final String KEY                     = "key";

  public static final String VALUE                   = "value";

  public static final String SUPPORT_TYPE            = "json";

  public static final String ADMIN_GROUP             = "/platform/administrators";
  
  private static final Log   LOG                     = ExoLogger.getLogger(RestUtils.class);
  
  /**
   * Get a hash map from an identity in order to build a json object for the rest service
   * 
   * @param identity the provided identity
   * @return a hash map
   */
  public static Map<String, Object> buildEntityFromIdentity(Identity identity) {
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    Profile profile = identity.getProfile();
    map.put(RestProperties.ID, identity.getId());
    map.put(RestProperties.USER_NAME, identity.getRemoteId());
    if (OrganizationIdentityProvider.NAME.equals(identity.getProviderId())) {
      map.put(RestProperties.FIRST_NAME, profile.getProperty(Profile.FIRST_NAME).toString());
      map.put(RestProperties.LAST_NAME, profile.getProperty(Profile.LAST_NAME).toString());
      map.put(RestProperties.GENDER, profile.getGender());
      map.put(RestProperties.POSITION, profile.getPosition());
      map.put(RestProperties.FULL_NAME, profile.getFullName());
      map.put(RestProperties.EMAIL, profile.getEmail());
      map.put(RestProperties.HREF, Util.getRestUrl(USERS_TYPE, identity.getRemoteId()));
      map.put(RestProperties.PHONES, getSubListByProperties(profile.getPhones(), getPhoneProperties()));
      map.put(RestProperties.EXPERIENCES, getSubListByProperties((List)(List<Map<String, Object>>)profile.getProperty(Profile.EXPERIENCES), getExperiencesProperties()));
      map.put(RestProperties.IMS, getSubListByProperties((List<Map<String, String>>) profile.getProperty(Profile.CONTACT_IMS), getImsProperties()));
      map.put(RestProperties.URLS, getSubListByProperties((List<Map<String, String>>) profile.getProperty(Profile.CONTACT_URLS), getUrlProperties()));
      map.put(RestProperties.AVATAR, profile.getAvatarUrl());
    }
    map.put(RestProperties.DELETED, identity.isDeleted());
    map.put(RestProperties.IDENTITY, Util.getRestUrl(IDENTITIES_TYPE, identity.getId()));
    return map;
  }
  
  /**
   * Get a hash map from a space in order to build a json object for the rest service
   * 
   * @param space the provided space
   * @param userId the user's remote id
   * @return a hash map
   */
  public static Map<String, Object> buildEntityFromSpace(Space space, String userId) {
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    if (ArrayUtils.contains(space.getMembers(), userId) || isMemberOfAdminGroup()) {
      map.put(RestProperties.ID, space.getId());
      map.put(RestProperties.HREF, Util.getRestUrl(SPACES_TYPE, space.getId()));
      Identity spaceIdentity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), true);
      map.put(RestProperties.IDENTITY, Util.getRestUrl(IDENTITIES_TYPE, spaceIdentity.getId()));
      map.put(RestProperties.GROUP_ID, space.getGroupId());
      map.put(RestProperties.AVATAR_URL, space.getAvatarUrl());
      map.put(RestProperties.APPLICATIONS, getSpaceApplications(space));
      map.put(RestProperties.MANAGERS, Util.getMembersSpaceRestUrl(space.getId(), true));
      map.put(RestProperties.MEMBERS, Util.getMembersSpaceRestUrl(space.getId(), false));
    }
    map.put(RestProperties.DISPLAY_NAME, space.getDisplayName());
    map.put(RestProperties.URL, LinkProvider.getSpaceUri(space.getPrettyName()));
    map.put(RestProperties.VISIBILITY, space.getVisibility());
    map.put(RestProperties.SUBSCRIPTION, space.getRegistration());
    return map;
  }
  
  /**
   * Get a hash map from a space in order to build a json object for the rest service
   * 
   * @param space the provided space
   * @param userId the user's remote id
   * @param type membership type
   * @return a hash map
   */
  public static Map<String, String> buildEntityFromSpaceMembership(Space space, String userId, String type) {
    Map<String, String> map = new LinkedHashMap<String, String>();
    OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
    MembershipHandler handler = organizationService.getMembershipHandler();
    try {
      Membership membership = handler.findMembershipByUserGroupAndType(userId, space.getGroupId(), type);
      map.put(RestProperties.ID, membership.getId());
      map.put(RestProperties.HREF, Util.getRestUrl(SPACES_MEMBERSHIP_TYPE, membership.getId()));
    } catch (Exception e) {
      LOG.debug("Failed to find the membership");
      return map;
    }
    map.put(RestProperties.USERS, Util.getRestUrl(USERS_TYPE, userId));
    map.put(RestProperties.SPACES, Util.getRestUrl(SPACES_TYPE, space.getId()));
    map.put(RestProperties.ROLE, type);
    map.put(RestProperties.STATUS, "approved");
    return map;
  }
  
  /**
   * Get a hash map from an activity in order to build a json object for the rest service
   * 
   * @param activity the provided activity
   * @return a hash map
   */
  public static Map<String, Object> buildEntityFromActivity(ExoSocialActivity activity) {
    Identity poster = CommonsUtils.getService(IdentityManager.class).getIdentity(activity.getPosterId(), true);
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put(RestProperties.ID, activity.getId());
    map.put(RestProperties.HREF, Util.getRestUrl(ACTIVITIES_TYPE, activity.getId()));
    map.put(RestProperties.IDENTITY, Util.getRestUrl(IDENTITIES_TYPE, activity.getPosterId()));
    map.put(RestProperties.TITLE, activity.getTitle());
    map.put(RestProperties.BODY, activity.getBody());
    map.put(RestProperties.OWNER, getActivityOwner(poster));
    map.put(RestProperties.LINK, activity.getPermaLink());
    map.put(RestProperties.ATTACHMENTS, new ArrayList<String>());
    map.put(RestProperties.TYPE, activity.getType());
    map.put(RestProperties.CREATE_DATE, formatDateToISO8601(new Date(activity.getPostedTime())));
    map.put(RestProperties.UPDATE_DATE, formatDateToISO8601(activity.getUpdated()));
    map.put(RestProperties.MENTIONS, getActivityMentions(activity));
    map.put(RestProperties.LIKES, Util.getLikesActivityRestUrl(activity.getId()));
    map.put(RestProperties.COMMENTS, Util.getCommentsActivityRestUrl(activity.getId()));
    return map;
  }
  
  /**
   * Get a hash map from a relationship in order to build a json object for the rest service
   * 
   * @param relationship the provided relationship
   * @return a hash map
   */
  public static Map<String, String> buildEntityFromRelationship(Relationship relationship) {
    Map<String, String> map = new LinkedHashMap<String, String>();
    map.put(RestProperties.ID, relationship.getId());
    map.put(RestProperties.HREF, Util.getRestUrl(USERS_RELATIONSHIP_TYPE, relationship.getId()));
    map.put(RestProperties.STATUS, relationship.getStatus().name());
    map.put(RestProperties.SENDER, Util.getRestUrl(USERS_TYPE, relationship.getSender().getRemoteId()));
    map.put(RestProperties.RECEIVER, Util.getRestUrl(USERS_TYPE, relationship.getReceiver().getRemoteId()));
    return map;
  }
  
  private static Map<String, String> getActivityOwner(Identity owner) {
    Map<String, String> mention = new LinkedHashMap<String, String>();
    mention.put(RestProperties.ID, owner.getId());
    mention.put(RestProperties.HREF, Util.getRestUrl(USERS_TYPE, owner.getRemoteId()));
    return mention;
  }
  
  private static List<Map<String, String>> getActivityMentions(ExoSocialActivity activity) {
    List<Map<String, String>> mentions = new ArrayList<Map<String, String>>();
    for (String mentionner : activity.getMentionedIds()) {
      String mentionnerId = mentionner.split("@")[0];
      Identity userIdentity = CommonsUtils.getService(IdentityManager.class).getIdentity(mentionnerId, false);
      mentions.add(getActivityOwner(userIdentity));
    }
    return mentions;
  }
  
  /**
   * Get the activityStream's information related to the activity.
   * 
   * @param authentiatedUsed the viewer
   * @param activity
   * @param target the owner of the stream that we want to display
   * @return activityStream object, null if the viewer has no permission to view activity
   */
  public static Map<String, String> getActivityStream(String authentiatedUsed, ExoSocialActivity activity, Identity target) {
    Identity viewerIdentity = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, authentiatedUsed, true);
    Map<String, String> as = new LinkedHashMap<String, String>();
    Identity owner = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(OrganizationIdentityProvider.NAME, activity.getStreamOwner(), true);
    if (owner != null) { //case of user activity
      Relationship relationship = CommonsUtils.getService(RelationshipManager.class).get(viewerIdentity, target);
      if (! viewerIdentity.getId().equals(activity.getPosterId()) //the viewer is not the poster
          && ! viewerIdentity.getRemoteId().equals(activity.getStreamOwner()) //the viewer is not the owner
          && (relationship == null || ! relationship.getStatus().equals(Relationship.Type.CONFIRMED))) { //the viewer has no relationship with the given user 
        return null;
      }
      as.put(RestProperties.TYPE, USER_ACTIVITY_TYPE);
    } else { //case of space activity
      owner = CommonsUtils.getService(IdentityManager.class).getOrCreateIdentity(SpaceIdentityProvider.NAME, activity.getStreamOwner(), true);
      Space space = CommonsUtils.getService(SpaceService.class).getSpaceByPrettyName(owner.getRemoteId());
      if (! CommonsUtils.getService(SpaceService.class).isMember(space, authentiatedUsed)) { //the viewer is not member of space
        return null;
      }
      as.put(RestProperties.TYPE, SPACE_ACTIVITY_TYPE);
    }
    as.put(RestProperties.ID, owner.getRemoteId());
    return as;
  }
  
  private static List<Map<String, String>> getSpaceApplications(Space space) {
    List<Map<String, String>> spaceApplications = new ArrayList<Map<String, String>>();
    String installedApps = space.getApp();
    if (installedApps != null) {
      String[] appStatuses = installedApps.split(",");
      for (String appStatus : appStatuses) {
        Map<String, String> app = new LinkedHashMap<String, String>();
        String[] apps = appStatus.split(":");
        app.put(RestProperties.ID, apps[0]);
        app.put(RestProperties.DISPLAY_NAME, apps.length > 1 ? apps[1] : "");
        spaceApplications.add(app);
      }
    }
    return spaceApplications;
  }
  
  private static List<Map<String, Object>> getSubListByProperties(List<Map<String, String>> sources, Map<String, String> properties) {
    List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
    if (sources == null || sources.size() == 0) {
      return results;
    }
    for (Map<String, String> map : sources) {
      if (map.isEmpty()) continue;
      Map<String, Object> result = new LinkedHashMap<String, Object>();
      for (Entry<String, String> property : properties.entrySet()) {
        result.put(property.getKey(), map.get(property.getValue()));
      }
      results.add(result);
    }
    
    return results;
  }
  
  private static Map<String, String> getPhoneProperties() {
    Map<String, String> properties = new LinkedHashMap<String, String>();
    properties.put(RestProperties.PHONE_TYPE, KEY);
    properties.put(RestProperties.PHONE_NUMBER, VALUE);
    return properties;
  }
  
  private static Map<String, String> getImsProperties() {
    Map<String, String> properties = new LinkedHashMap<String, String>();
    properties.put(RestProperties.IM_TYPE, KEY);
    properties.put(RestProperties.IM_ID, VALUE);
    return properties;
  }
  
  private static Map<String, String> getUrlProperties() {
    Map<String, String> properties = new LinkedHashMap<String, String>();
    properties.put(RestProperties.URL, VALUE);
    return properties;
  }
  
  private static Map<String, String> getExperiencesProperties() {
    Map<String, String> properties = new LinkedHashMap<String, String>();
    properties.put(RestProperties.COMPANY, Profile.EXPERIENCES_COMPANY);
    properties.put(RestProperties.DESCRIPTION, Profile.EXPERIENCES_DESCRIPTION);
    properties.put(RestProperties.POSITION, Profile.EXPERIENCES_POSITION);
    properties.put(RestProperties.SKILLS, Profile.EXPERIENCES_SKILLS);
    properties.put(RestProperties.IS_CURRENT, Profile.EXPERIENCES_IS_CURRENT);
    properties.put(RestProperties.START_DATE, Profile.EXPERIENCES_START_DATE);
    properties.put(RestProperties.END_DATE, Profile.EXPERIENCES_END_DATE);
    return properties;
  }
  
  private static String formatDateToISO8601(Date date) {
    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    df.setTimeZone(tz);
    return df.format(date);
  }
  
  /**
   * Gets the json media type
   * 
   * @return a media type
   */
  public static MediaType getJsonMediaType() {
    return Util.getMediaType(SUPPORT_TYPE, new String[]{SUPPORT_TYPE});
  }
  
  /**
   * Check if the authenticated user is a member of the admin group
   * 
   * @return
   */
  public static boolean isMemberOfAdminGroup() {
    return ConversationState.getCurrent().getIdentity().isMemberOf(ADMIN_GROUP);
  }
}
