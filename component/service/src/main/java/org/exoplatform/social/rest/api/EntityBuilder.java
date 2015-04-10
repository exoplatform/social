/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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

package org.exoplatform.social.rest.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.rest.ApplicationContext;
import org.exoplatform.services.rest.impl.ApplicationContextImpl;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.entity.ActivityEntity;
import org.exoplatform.social.rest.entity.BaseEntity;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.CommentEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.rest.entity.IdentityEntity;
import org.exoplatform.social.rest.entity.LinkEntity;
import org.exoplatform.social.rest.entity.RelationshipEntity;
import org.exoplatform.social.rest.entity.SpaceEntity;
import org.exoplatform.social.rest.entity.SpaceMembershipEntity;
import org.exoplatform.social.rest.entity.UserEntity;

public class EntityBuilder {
  public static final String USERS_TYPE              = "users";

  public static final String USERS_RELATIONSHIP_TYPE = "usersRelationships";

  public static final String USER_ACTIVITY_TYPE      = "user";

  public static final String IDENTITIES_TYPE         = "identities";

  public static final String SPACES_TYPE             = "spaces";
  
  public static final String SPACES_MEMBERSHIP_TYPE  = "spacesMemberships";

  public static final String SPACE_ACTIVITY_TYPE     = "space";

  public static final String ACTIVITIES_TYPE         = "activities";
  
  public static final String COMMENTS_TYPE           = "comments";
  
  public static final String LIKES_TYPE              = "likes";

  public static final String KEY                     = "key";

  public static final String VALUE                   = "value";
  /** Link header next relation. */
  private static final String NEXT_ACTION             = "next";
  /** Link header previous relation. */
  private static final String PREV_ACTION             = "prev";
  /** Link header first relation. */
  private static final String FIRST_ACTION            = "first";
  /** Link header last relation. */
  private static final String LAST_ACTION             = "last";
  /** Link header name. */
  private static final String LINK                    = "Link";
  /**
   * Get a IdentityEntity from an identity in order to build a json object for the rest service
   * 
   * @param identity the provided identity
   * @return a hash map
   */
  public static IdentityEntity buildEntityIdentity(Identity identity, String restPath, String expand) {
    IdentityEntity idntityEntity = new IdentityEntity(identity.getId());
    idntityEntity.setHref(RestUtils.getRestUrl(IDENTITIES_TYPE, identity.getId(), restPath));
    idntityEntity.setProviderId(identity.getProviderId());
    idntityEntity.setGlobalId(identity.getGlobalId());
    idntityEntity.setDeleted(identity.isDeleted());
    if(OrganizationIdentityProvider.NAME.equals(identity.getProviderId())) {
      idntityEntity.setProfile(buildEntityProfile(identity.getProfile(), restPath, "").getDataEntity());//
    }
    
    updateCachedEtagValue(getEtagValue(identity.getId()));
    return idntityEntity;
  }

  /**
   * @param userName
   * @param restPath
   * @param expand
   * @return
   */
  public static IdentityEntity buildEntityIdentity(String userName, String restPath, String expand) {
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName, true);
    return buildEntityIdentity(userIdentity, restPath, expand);
  }

  public static UserEntity buildEntityProfile(Profile profile, String restPath, String expand) {
    UserEntity userEntity = new UserEntity(profile.getId());
    userEntity.setHref(RestUtils.getRestUrl(USERS_TYPE, profile.getIdentity().getRemoteId(), restPath));
    userEntity.setIdentity(RestUtils.getRestUrl(IDENTITIES_TYPE, profile.getIdentity().getRemoteId(), restPath));
    userEntity.setUsername(profile.getIdentity().getRemoteId());
    userEntity.setFirstname(profile.getProperty(Profile.FIRST_NAME).toString());
    userEntity.setLastname(profile.getProperty(Profile.LAST_NAME).toString());
    userEntity.setFullname(profile.getFullName());
    userEntity.setGender(profile.getGender());
    userEntity.setPosition(profile.getPosition());
    userEntity.setEmail(profile.getEmail());
//    userEntity.setAboutMe(profile.getAboutMe());
    userEntity.setAvatar(profile.getAvatarUrl());
    userEntity.setPhones(getSubListByProperties(profile.getPhones(), getPhoneProperties()));
    userEntity.setExperiences(getSubListByProperties((List)(List<Map<String, Object>>) profile.getProperty(Profile.EXPERIENCES), getExperiencesProperties()));
    userEntity.setIms(getSubListByProperties((List<Map<String, String>>) profile.getProperty(Profile.CONTACT_IMS), getImsProperties()));
    userEntity.setUrls(getSubListByProperties((List<Map<String, String>>) profile.getProperty(Profile.CONTACT_URLS), getUrlProperties()));
    userEntity.setDeleted(profile.getIdentity().isDeleted());
    updateCachedEtagValue(getEtagValue(profile.getId()));
    return userEntity;
  }

  /**
   * @param userName
   * @param restPath
   * @param expand
   * @return
   */
  public static UserEntity buildEntityProfile(String userName, String restPath, String expand) {
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity userIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userName, true);
    return buildEntityProfile(userIdentity.getProfile(), restPath, expand);
  }
  
  /**
   * @param userNames
   * @param restPath
   * @param expand
   * @return
   */
  public static List<DataEntity> buildEntityProfiles(String[] userNames, String restPath, String expand) {
    if (userNames == null || userNames.length == 0) {
      return new ArrayList<DataEntity>();
    }
    List<DataEntity> userEntities = new ArrayList<DataEntity>();
    for (int i = 0; i < userNames.length; i++) {
      userEntities.add(buildEntityProfile(userNames[0], restPath, expand).getDataEntity());
    }
    return userEntities;
  }
  
  /**
   * Get a hash map from a space in order to build a json object for the rest service
   * 
   * @param space the provided space
   * @param userId the user's remote id
   * @return a hash map
   */
  public static SpaceEntity buildEntityFromSpace(Space space, String userId, String restPath, String expand) {
    SpaceEntity spaceEntity = new SpaceEntity(space.getId());
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    if (ArrayUtils.contains(space.getMembers(), userId) || RestUtils.isMemberOfAdminGroup()) {
      spaceEntity.setHref(RestUtils.getRestUrl(SPACES_TYPE, space.getId(), restPath));
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), true);
      LinkEntity identity;
      if(RestProperties.IDENTITY.equals(expand)) {
        identity = new LinkEntity(buildEntityIdentity(spaceIdentity, restPath, null));
      } else {
        identity = new LinkEntity(RestUtils.getRestUrl(IDENTITIES_TYPE, spaceIdentity.getId(), restPath));
      }
      spaceEntity.setIdentity(identity);
      spaceEntity.setGroupId(space.getGroupId());
      spaceEntity.setApplications(getSpaceApplications(space));
      LinkEntity managers, memebers;
      if(RestProperties.MANAGERS.equals(expand)) {
        managers = new LinkEntity(buildEntityProfiles(space.getManagers(), restPath, expand));
      } else {
        managers = new LinkEntity(getMembersSpaceRestUrl(space.getId(), true, restPath));
      }
      spaceEntity.setManagers(managers);
      if(RestProperties.MEMBERS.equals(expand)) {
        memebers = new LinkEntity(buildEntityProfiles(space.getMembers(), restPath, expand));
      } else {
        memebers = new LinkEntity(getMembersSpaceRestUrl(space.getId(), false, restPath));
      }
      spaceEntity.setMembers(memebers);
    }
    spaceEntity.setDisplayName(space.getDisplayName());
    spaceEntity.setDescription(space.getDescription());
    spaceEntity.setUrl(LinkProvider.getSpaceUri(space.getPrettyName()));
    spaceEntity.setAvatarUrl(space.getAvatarUrl());
    spaceEntity.setVisibility(space.getVisibility());
    spaceEntity.setSubscription(space.getRegistration());
    //
    updateCachedEtagValue(getEtagValue(space.getId()));
    return spaceEntity;
  }
  
  /**
   * Get a hash map from a space in order to build a json object for the rest service
   * 
   * @param space the provided space
   * @param userId the user's remote id
   * @param type membership type
   * @return a hash map
   */
  public static SpaceMembershipEntity buildEntityFromSpaceMembership(Space space, String userId, String type, String restPath, String expand) {
    //
    updateCachedEtagValue(getEtagValue(type));

    String id = space.getPrettyName() + ":" + userId + ":" + type;
    SpaceMembershipEntity spaceMembership = new SpaceMembershipEntity(id);
    spaceMembership.setHref(RestUtils.getRestUrl(SPACES_MEMBERSHIP_TYPE, id, restPath));
    LinkEntity userEntity, spaceEntity;
    if (USERS_TYPE.equals(expand)) {
      userEntity = new LinkEntity(buildEntityProfile(userId, restPath, ""));
    } else {
      userEntity = new LinkEntity(RestUtils.getRestUrl(USERS_TYPE, userId, restPath));
    }
    spaceMembership.setDataUser(userEntity);
    if (SPACES_TYPE.equals(expand)) {
      spaceEntity = new LinkEntity(buildEntityProfile(userId, restPath, ""));
    } else {
      spaceEntity = new LinkEntity(RestUtils.getRestUrl(SPACES_TYPE, space.getId(), restPath));
    }
    spaceMembership.setDataSpace(spaceEntity);
    spaceMembership.setRole(type);
    spaceMembership.setStatus("approved");

    return spaceMembership;
  }

  /**
   * Get a ActivityEntity from an activity in order to build a json object for the rest service
   * 
   * @param activity the provided activity
   * @param expand 
   * @return a hash map
   */
  public static ActivityEntity buildEntityFromActivity(ExoSocialActivity activity, String restPath, String expand) {
    Identity poster = CommonsUtils.getService(IdentityManager.class).getIdentity(activity.getPosterId(), true);
    ActivityEntity activityEntity = new ActivityEntity(activity);
    activityEntity.setHref(RestUtils.getRestUrl(ACTIVITIES_TYPE, activity.getId(), restPath));
    LinkEntity identityLink;
    if (expand != null && RestProperties.IDENTITY.equals(expand)) {
      identityLink = new LinkEntity(buildEntityIdentity(poster, restPath, null));
    } else {
      identityLink = new LinkEntity(RestUtils.getRestUrl(IDENTITIES_TYPE, activity.getPosterId(), restPath));
    }
    activityEntity.setDatIdentity(identityLink);
    activityEntity.setOwner(getActivityOwner(poster, restPath));
    activityEntity.setMentions(getActivityMentions(activity, restPath));
    activityEntity.setAttachments(new ArrayList<DataEntity>());
    
    LinkEntity commentLink;
    if (expand != null && COMMENTS_TYPE.equals(expand)) {
      List<DataEntity> commentsEntity = EntityBuilder.buildEntityFromComment(activity, restPath, "", RestUtils.DEFAULT_OFFSET, RestUtils.DEFAULT_OFFSET);
      commentLink = new LinkEntity(commentsEntity);
    } else {
      commentLink = new LinkEntity(getCommentsActivityRestUrl(activity.getId(), restPath));
    }
    activityEntity.setComments(commentLink);
    activityEntity.setLikes(new LinkEntity(RestUtils.getBaseUrl() + "/" + restPath + "/likers"));
    activityEntity.setCreateDate(RestUtils.formatISO8601(new Date(activity.getPostedTime())));
    activityEntity.setUpdateDate(RestUtils.formatISO8601(activity.getUpdated()));
    //
    updateCachedLastModifiedValue(activity.getUpdated());
    return activityEntity;
  }
  
  public static CommentEntity buildEntityFromComment(ExoSocialActivity comment, String restPath, String expand, boolean isBuildList) {
    Identity poster = CommonsUtils.getService(IdentityManager.class).getIdentity(comment.getPosterId(), true);
    CommentEntity commentEntity = new CommentEntity(comment.getId());
    commentEntity.setHref(RestUtils.getRestUrl(ACTIVITIES_TYPE, comment.getId(), restPath));
    LinkEntity identityLink;
    if (expand != null && RestProperties.IDENTITY.equals(expand)) {
      identityLink = new LinkEntity(buildEntityIdentity(poster, restPath, null));
    } else {
      identityLink = new LinkEntity(RestUtils.getRestUrl(IDENTITIES_TYPE, comment.getPosterId(), restPath));
    }
    commentEntity.setDataIdentity(identityLink);
    commentEntity.setPoster(poster.getRemoteId());
    commentEntity.setBody(comment.getTitle());
    commentEntity.setMentions(getActivityMentions(comment, restPath));
    commentEntity.setCreateDate(RestUtils.formatISO8601(new Date(comment.getPostedTime())));
    commentEntity.setUpdateDate(RestUtils.formatISO8601(comment.getUpdated()));
    //
    if(!isBuildList) {
      updateCachedLastModifiedValue(comment.getUpdated());
    }
    //
    return commentEntity;
  }

  public static List<DataEntity> buildEntityFromComment(ExoSocialActivity activity, String restPath, String expand, int offset, int limit) {
    List<DataEntity> commentsEntity = new ArrayList<DataEntity>();
    ActivityManager activityManager = CommonsUtils.getService(ActivityManager.class);
    RealtimeListAccess<ExoSocialActivity> listAccess = activityManager.getCommentsWithListAccess(activity);
    List<ExoSocialActivity> comments = listAccess.loadAsList(offset, limit);
    for (ExoSocialActivity comment : comments) {
      CommentEntity commentInfo = buildEntityFromComment(comment, restPath, expand, true);
      commentsEntity.add(commentInfo.getDataEntity());
    }
    return commentsEntity;
  }

  /**
   * Get a RelationshipEntity from a relationship in order to build a json object for the rest service
   * 
   * @param relationship the provided relationship
   * @return a RelationshipEntity
   */
  public static RelationshipEntity buildEntityRelationship(Relationship relationship, String restPath, String expand, boolean isSymetric) {
    RelationshipEntity relationshipEntity = new RelationshipEntity(relationship.getId());
    relationshipEntity.setHref(RestUtils.getRestUrl(USERS_RELATIONSHIP_TYPE, relationship.getId(), restPath));
    LinkEntity sender, receiver;
    if(RestProperties.SENDER.equals(expand)) {
      sender = new LinkEntity(buildEntityProfile(relationship.getSender().getProfile(), restPath, null));
    } else {
      sender = new LinkEntity(RestUtils.getRestUrl(USERS_TYPE, relationship.getSender().getRemoteId(), restPath));
    }
    relationshipEntity.setDataSender(sender);
    if(RestProperties.RECEIVER.equals(expand)) {
      receiver = new LinkEntity(buildEntityProfile(relationship.getReceiver().getProfile(), restPath, null));
    } else {
      receiver = new LinkEntity(RestUtils.getRestUrl(USERS_TYPE, relationship.getReceiver().getRemoteId(), restPath));
    }
    relationshipEntity.setDataReceiver(receiver);
    relationshipEntity.setStatus(relationship.getStatus().name());
    if (isSymetric) {
      relationshipEntity.setSymetric(relationship.isSymetric());
    }
    updateCachedEtagValue(getEtagValue(relationship.getId()));
    return relationshipEntity;
  }

  public static List<DataEntity> buildRelationshipEntities(List<Relationship> relationships, UriInfo uriInfo) {
    List<DataEntity> infos = new ArrayList<DataEntity>();
    for (Relationship relationship : relationships) {
      //
      infos.add(EntityBuilder.buildEntityRelationship(relationship, uriInfo.getPath(), RestUtils.getQueryParam(uriInfo, "expand"), true).getDataEntity());
    }
    return infos;
  }

  private static DataEntity getActivityOwner(Identity owner, String restPath) {
    BaseEntity mentionEntity = new BaseEntity(owner.getId());
    mentionEntity.setHref(RestUtils.getRestUrl(USERS_TYPE, owner.getRemoteId(), restPath));
    return mentionEntity.getDataEntity();
  }

  private static List<DataEntity> getActivityMentions(ExoSocialActivity activity, String restPath) {
    List<DataEntity> mentions = new ArrayList<DataEntity>();
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    for (String mentionner : activity.getMentionedIds()) {
      String mentionnerId = mentionner.split("@")[0];
      mentions.add(getActivityOwner(identityManager.getIdentity(mentionnerId, false), restPath));
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
  public static DataEntity getActivityStream(ExoSocialActivity activity, Identity authentiatedUsed) {
    DataEntity as = new DataEntity();
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    Identity owner = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, activity.getStreamOwner(), true);
    if (owner != null) { //case of user activity
      Relationship relationship = CommonsUtils.getService(RelationshipManager.class).get(authentiatedUsed, owner);
      if (! authentiatedUsed.getId().equals(activity.getPosterId()) //the viewer is not the poster
          && ! authentiatedUsed.getRemoteId().equals(activity.getStreamOwner()) //the viewer is not the owner
          && (relationship == null || ! relationship.getStatus().equals(Relationship.Type.CONFIRMED)) //the viewer has no relationship with the given user
          && ! RestUtils.isMemberOfAdminGroup()) { //current user is not an administrator  
        return null;
      }
      as.put(RestProperties.TYPE, USER_ACTIVITY_TYPE);
    } else { //case of space activity
      SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
      owner = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, activity.getStreamOwner(), true);
      Space space = spaceService.getSpaceByPrettyName(owner.getRemoteId());
      if (!spaceService.isMember(space, authentiatedUsed.getRemoteId())) { //the viewer is not member of space
        return null;
      }
      as.put(RestProperties.TYPE, SPACE_ACTIVITY_TYPE);
    }
    //
    as.put(RestProperties.ID, owner.getRemoteId());
    return as;
  }

  private static List<DataEntity> getSpaceApplications(Space space) {
    List<DataEntity> spaceApplications = new ArrayList<DataEntity>();
    String installedApps = space.getApp();
    if (installedApps != null) {
      String[] appStatuses = installedApps.split(",");
      for (String appStatus : appStatuses) {
        String[] apps = appStatus.split(":");
        BaseEntity app = new BaseEntity(apps[0]);
        app.setProperty(RestProperties.DISPLAY_NAME, apps.length > 1 ? apps[1] : "");
        spaceApplications.add(app.getDataEntity());
      }
    }
    return spaceApplications;
  }

  private static List<DataEntity> getSubListByProperties(List<Map<String, String>> sources, Map<String, String> properties) {
    List<DataEntity> results = new ArrayList<DataEntity>();
    if (sources == null || sources.size() == 0) {
      return results;
    }
    for (Map<String, String> map : sources) {
      if (map.isEmpty()) continue;
      BaseEntity result = new BaseEntity();
      for (Entry<String, String> property : properties.entrySet()) {
        result.setProperty(property.getKey(), map.get(property.getValue()));
      }
      results.add(result.getDataEntity());
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

  private static void updateCachedEtagValue(int etagValue) {
    ApplicationContext ac = ApplicationContextImpl.getCurrent();
    Map<String, String> properties = ac.getProperties();
    ConcurrentHashMap<String, String> props = new ConcurrentHashMap<String, String>(properties);

    if (props.containsKey(RestProperties.ETAG)) {
      props.remove(RestProperties.ETAG);
    }

    if (props.containsKey(RestProperties.UPDATE_DATE)) {
      props.remove(RestProperties.UPDATE_DATE);
    }

    ac.setProperty(RestProperties.ETAG, String.valueOf(etagValue));
    ApplicationContextImpl.setCurrent(ac);
  }

  private static void updateCachedLastModifiedValue(Date lastModifiedDate) {
    ApplicationContext ac = ApplicationContextImpl.getCurrent();
    Map<String, String> properties = ac.getProperties();
    ConcurrentHashMap<String, String> props = new ConcurrentHashMap<String, String>(properties);

    if (props.containsKey(RestProperties.UPDATE_DATE)) {
      props.remove(RestProperties.UPDATE_DATE);
    }

    if (props.containsKey(RestProperties.ETAG)) {
      props.remove(RestProperties.ETAG);
    }

    ac.setProperty(RestProperties.UPDATE_DATE, String.valueOf(lastModifiedDate.getTime()));
    ApplicationContextImpl.setCurrent(ac);
  }

  private static int getEtagValue(String... properties) {
    final int prime = 31;
    int result = 0;

    for (String prop : properties) {
      if (prop != null) {
        result = prime * result + prop.hashCode();
      }
    }
    return result;
  }

  /**
   * Get the rest url to load all members or managers of a space
   * 
   * @param id the id of space
   * @param returnManager return managers or members
   * @return rest url to load all members or managers of a space
   */
  public static String getMembersSpaceRestUrl(String id, boolean returnManager, String restPath) {
    StringBuffer spaceMembersRestUrl = new StringBuffer(RestUtils.getRestUrl(SPACES_TYPE, id, restPath)).append("/").append(USERS_TYPE);
    if (returnManager) {
      return spaceMembersRestUrl.append("?role=manager").toString();
    }
    return spaceMembersRestUrl.toString();
  }

  /**
   * Get the rest url in order to load all comments of an activity
   * 
   * @param activityId activity's id
   * @return
   */
  public static String getCommentsActivityRestUrl(String activityId, String restPath) {
    return new StringBuffer(RestUtils.getRestUrl(ACTIVITIES_TYPE, activityId, restPath)).append("/").append("comments").toString();
  }

  /**
   * Gets the response object constructed from the provided params.
   *
   * @param entity the identity
   * @param uriInfo the uri request info
   * @param mediaType the media type to be returned
   * @param status the status code
   * @return response the response object
   */
  public static Response getResponse(Object entity, UriInfo uriInfo, MediaType mediaType, Response.Status status) {
    if (entity instanceof BaseEntity) {
      entity = ((BaseEntity) entity).getDataEntity();
    }
    Response resp = Response.created(uriInfo.getAbsolutePath())
                   .entity(entity)
                   .type(mediaType.toString() + "; charset=utf-8")
                   .status(status)
                   .build();
    if (!hasPaging(entity)) {
      return resp;
    }
    
    ResponseBuilder responseBuilder = Response.fromResponse(resp);
    responseBuilder.header(LINK, buildLinkForHeader(entity, uriInfo.getAbsolutePath().toString()));
    return responseBuilder.build();
  }
  
  private static boolean hasPaging(Object entity) {
    if (!(entity instanceof CollectionEntity)) {
      return false;
    }
    
    CollectionEntity rc = (CollectionEntity)entity;
    int size = rc.getSize();
    int offset = rc.getOffset();
    int limit = rc.getLimit(); // items per page
    
    if (size <= 0 || limit == 0 || offset > size || size <= limit) {
      return false;
    }
    
    return true; 
  }

  /**
   * "https://localhost:8080/rest/users?offset=50&limit=25"
   * 
   * Link: <https://localhost:8080/rest/users?offset=25&limit=25>; rel="previous", 
   * <https://localhost:8080/rest/users?offset=75&limit=25>; rel="next"
   * 
   * @param entity
   * @param restPath
   * @return
   */
  public static Object buildLinkForHeader(Object entity, String requestPath) {
    CollectionEntity rc = (CollectionEntity)entity;
    int size = rc.getSize();
    int offset = rc.getOffset();
    int limit = rc.getLimit();
    
    StringBuilder linkHeader = new StringBuilder();
    
    if (hasNext(size, offset, limit)){
      int nextOS = offset + limit;
      linkHeader.append(createLinkHeader(requestPath, nextOS, limit, NEXT_ACTION));
    }
    
    if (hasPrevious(size, offset, limit)){
      int preOS = offset - limit;
      appendCommaIfNecessary(linkHeader);
      linkHeader.append(createLinkHeader(requestPath, preOS, limit,  PREV_ACTION));
    }
    
    if (hasFirst(size, offset, limit)){
      appendCommaIfNecessary(linkHeader);
      linkHeader.append(createLinkHeader(requestPath, 0, limit,  FIRST_ACTION));
    }
    
    if (hasLast(size, offset, limit)){
      int pages = (int)Math.ceil((double)size/limit);
      int lastOS = (pages - 1)*limit;
      appendCommaIfNecessary(linkHeader);
      linkHeader.append(createLinkHeader(requestPath, lastOS, limit,  LAST_ACTION));
    }
    
    return linkHeader.toString();
  }

  private static boolean hasNext(int size, int offset, int limit) {
    return size > offset + limit;
  }

  private static boolean hasPrevious(int size, int offset, int limit) {
    if (offset == 0) {
      return false;
    }
    
    return offset >= limit;
  }

  private static boolean hasFirst(int size, int offset, int limit) {
    return hasPrevious(size, offset, limit);
  }

  private static boolean hasLast(int size, int offset, int limit) {
    if (offset + limit == size) {
      return false;
    }
    
    return hasNext(size, offset, limit);
  }

  private static void appendCommaIfNecessary(final StringBuilder linkHeader) {
    if (linkHeader.length() > 0) {
      linkHeader.append(", ");
    }
  }

  private static String createLinkHeader(String uri, int offset, int limit, String rel) {
    return "<" + uri + "?offset="+ offset + "&limit="+ limit + ">; rel=\"" + rel + "\"";
  }
}
