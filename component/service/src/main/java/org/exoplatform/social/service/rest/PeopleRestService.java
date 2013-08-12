/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.service.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shindig.social.opensocial.model.Activity;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.webui.utils.TimeConvertUtils;

/**
 * 
 * Provides REST Services for manipulating jobs relates to people.
 * 
 * @anchor PeopleRestService
 */

@Path("social/people")
public class PeopleRestService implements ResourceContainer{
  /** Confirmed Status information */
  private static final String CONFIRMED_STATUS = "confirmed";
  /** Pending Status information */
  private static final String PENDING_STATUS = "pending";
  /** Incoming Status information */
  private static final String INCOMING_STATUS = "incoming";
  /** Ignored Status information */
  private static final String IGNORED_STATUS = "ignored";
  /** Waiting Status information */
  private static final String WAITING_STATUS = "waiting";
  /** Alien Status information */
  private static final String ALIEN_STATUS = "alien";
  /** Invite action */
  private static final String INVITE_ACTION = "Invite";
  /** Accept action */
  private static final String ACCEPT_ACTION = "Accept";
  /** Deny action */
  private static final String DENY_ACTION = "Deny";
  /** Revoke action */
  private static final String REVOKE_ACTION = "Revoke";
  /** Remove action */
  private static final String REMOVE_ACTION = "Disconnect";
  /** Member of space Status information */
  private static final String SPACE_MEMBER = "member_of_space";
  /** User to invite to join the space Status information */
  private static final String USER_TO_INVITE = "user_to_invite";
  /** No action */
  private static final String NO_ACTION = "NoAction";
  /** No information */
  private static final String NO_INFO = "NoInfo";
  /** Number of user names is added to suggest list. */
  private static final long SUGGEST_LIMIT = 20;
  
  /** Number of default limit activities. */
  private static final int DEFAULT_LIMIT = 20;
  
  private IdentityManager identityManager;
  private ActivityManager activityManager;
  private RelationshipManager relationshipManager;
  private SpaceService spaceService;
  
  public PeopleRestService() {
  }

  /**
   * Gets users' names that match the input string for suggestion.
   * 
   * @param uriInfo The requested URI information.
   * @param name The provided characters to be searched.
   * @param currentUser The user who sends request.
   * @param typeOfRelation The relationship status such as "confirmed", "pending", "incoming", "member_of_space" or "user_to_invite"
   * @param spaceURL The URL of the related space.
   * @param format The format of the returned result, for example, JSON, or XML.
   * @return A list of users' names that match the input string.
   * @throws Exception
   * @LevelAPI Platform
   * @anchor PeopleRestService.suggestUsernames
   */
  @GET
  @Path("suggest.{format}")
  public Response suggestUsernames(@Context UriInfo uriInfo,
                    @QueryParam("nameToSearch") String name,
                    @QueryParam("currentUser") String currentUser,
                    @QueryParam("typeOfRelation") String typeOfRelation,
                    @QueryParam("spaceURL") String spaceURL,
                    @PathParam("format") String format) throws Exception {
    MediaType mediaType = Util.getMediaType(format);
    List<Identity> excludedIdentityList = new ArrayList<Identity>();
    excludedIdentityList.add(Util.getViewerIdentity(currentUser));
    UserNameList nameList = new UserNameList();
    ProfileFilter filter = new ProfileFilter();
    
    filter.setName(name);
    filter.setCompany("");
    filter.setPosition("");
    filter.setSkills("");
    filter.setExcludedIdentityList(excludedIdentityList);
    List<Identity> identities = Arrays.asList(getIdentityManager().getIdentitiesByProfileFilter(
                                  OrganizationIdentityProvider.NAME, filter, false).load(0, (int)SUGGEST_LIMIT));

    Identity currentIdentity = getIdentityManager().getOrCreateIdentity(
                                 OrganizationIdentityProvider.NAME, currentUser, false);

    Space space = getSpaceService().getSpaceByUrl(spaceURL);
    if (PENDING_STATUS.equals(typeOfRelation)) {
      addToNameList(currentIdentity, getRelationshipManager().getPending(currentIdentity, identities), nameList);
    } else if (INCOMING_STATUS.equals(typeOfRelation)) {
      addToNameList(currentIdentity, getRelationshipManager().getIncoming(currentIdentity, identities), nameList);
    } else if (CONFIRMED_STATUS.equals(typeOfRelation)){
      addToNameList(currentIdentity, getRelationshipManager().getConfirmed(currentIdentity, identities), nameList);
    } else if (SPACE_MEMBER.equals(typeOfRelation)) {  // Use in search space member
      addSpaceUserToList (identities, nameList, space, typeOfRelation);
    } else if (USER_TO_INVITE.equals(typeOfRelation)) { 
      addSpaceUserToList (identities, nameList, space, typeOfRelation);
    } else { // Identities that match the keywords.
      for (Identity identity : identities) {
        nameList.addName(identity.getProfile().getFullName());
      }
    }
    
    return Util.getResponse(nameList, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * Gets users' information that matches the input string.
   * 
   * @param uriInfo The request URI information.
   * @param query The name of the user to filter.
   * @return Users' information that matches the input string.
   * @throws Exception
   * @LevelAPI Platform
   * @anchor PeopleRestService.suggestUsernames
   */
  @GET
  @Path("getprofile/data.json")
  public Response suggestUsernames(@Context UriInfo uriInfo,
                    @QueryParam("search") String query) throws Exception {
    MediaType mediaType = Util.getMediaType("json", new String[]{"json"});
    List<Identity> excludedIdentityList = new ArrayList<Identity>();
    ProfileFilter filter = new ProfileFilter();
    
    filter.setName(query);
    filter.setCompany("");
    filter.setPosition("");
    filter.setSkills("");
    filter.setExcludedIdentityList(excludedIdentityList);
    List<Identity> identities = Arrays.asList(getIdentityManager().getIdentitiesByProfileFilter(
                                  OrganizationIdentityProvider.NAME, filter, false).load(0, (int)SUGGEST_LIMIT));
    
    List<UserInfo> userInfos = new ArrayList<PeopleRestService.UserInfo>(identities.size());
    UserInfo userInfo;
    for (Identity identity : identities) {
      userInfo = new UserInfo(identity.getRemoteId());
      userInfo.setName(identity.getProfile().getFullName());
      userInfo.setAvatar(identity.getProfile().getAvatarUrl());
      userInfo.setType("contact"); //hardcode for test
      userInfos.add(userInfo);
    }
    
    return Util.getResponse(userInfos, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * Gets the information of people who have had connection with the current user.
   * 
   * @param uriInfo The requested URI information.
   * @param format The format of the returned result, for example, JSON, or XML.
   * @param portalName The name of current portal.
   * @param nameToSearch The name of user who wants to get.
   * @param offset The starting point of the returned result.
   * @param limit The ending point of the returned result.
   * @param lang The locale type.
   * @return The information of people who have had connection with the current user and the information must match with
   * the input condition (name to search).
   * @throws Exception
   * @LevelAPI Platform
   * @anchor PeopleRestService.searchConnection
   */
  @GET
  @Path("{portalName}/getConnections.{format}")
  public Response searchConnection(@Context UriInfo uriInfo,
                    @PathParam("portalName") String portalName,
                    @QueryParam("nameToSearch") String nameToSearch,
                    @QueryParam("offset") int offset,
                    @QueryParam("limit") int limit,
                    @QueryParam("lang") String lang,
                    @PathParam("format") String format) throws Exception {
    String[] supportedMediaType = { "json" };
    MediaType mediaType = Util.getMediaType(format,supportedMediaType);

    activityManager = Util.getActivityManager(portalName);
    relationshipManager = Util.getRelationshipManager(portalName);
    identityManager = Util.getIdentityManager(portalName);
    
    List<Identity> excludedIdentityList = new ArrayList<Identity>();
    Identity currentUser = Util.getIdentityManager(portalName).getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                                   Util.getViewerId(uriInfo), true);
    
    excludedIdentityList.add(currentUser);

    Identity[] identities;
    List<HashMap<String, Object>> entitys = new ArrayList<HashMap<String,Object>>();
    if (nameToSearch == null) { 
      // default loading, if load more then need to re-calculate offset and limit before going here via rest URL.     
      identities = identityManager.getConnectionsWithListAccess(currentUser).load(offset, limit);
    } else { 
      // search
      nameToSearch = nameToSearch.trim();
      
      ProfileFilter filter = new ProfileFilter();
      filter.setName(nameToSearch);
      filter.setExcludedIdentityList(excludedIdentityList);
      // will be getConnectionsByProfileFilter
      identities = relationshipManager.getConnectionsByFilter(currentUser, filter).load(offset, limit);
    }
    
    for(Identity identity : identities){
      HashMap<String, Object> temp = getIdentityInfo(identity, lang);
      if(temp != null){
        entitys.add(temp);
      }
    }
    
    return Util.getResponse(entitys, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * Gets the detailed information of a user on the pop-up, based on his/her username.
   * 
   * @param uriInfo The requested URI information.
   * @param format The format of the returned result, for example, JSON, or XML.
   * @param portalName The name of the current portal container.
   * @param currentUserName The current user name who sends request.
   * @param userId The specific user Id.
   * @param updatedType The type of connection action shown on the pop-up.
   * @return The detailed information of a user.
   * @throws Exception
   * @LevelAPI Provisional
   * @deprecated Will be removed in eXo Platform 4.0.x
   * @anchor PeopleRestService.getPeopleInfo
   */
  @GET
  @Path("{portalName}/{currentUserName}/getPeopleInfo/{userId}.{format}")
  public Response getPeopleInfo(@Context UriInfo uriInfo,
                                @PathParam("portalName") String portalName,
                                @PathParam("currentUserName") String currentUserName,
                                @PathParam("userId") String userId,
                                @PathParam("format") String format,
                                @QueryParam("updatedType") String updatedType) throws Exception {
    PeopleInfo peopleInfo = new PeopleInfo();
    MediaType mediaType = Util.getMediaType(format);
    Identity identity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                   userId, false);

    Identity currentIdentity = getIdentityManager().
            getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUserName, false);
    
    if (updatedType != null) {
      Relationship rel = getRelationshipManager().get(currentIdentity, identity);
      if (ACCEPT_ACTION.equals(updatedType)) { // Accept or Deny
        getRelationshipManager().confirm(rel);
      } else if (DENY_ACTION.equals(updatedType)) {
        getRelationshipManager().deny(rel);
      } else if (REVOKE_ACTION.equals(updatedType)) {
        getRelationshipManager().deny(rel);
      } else if (INVITE_ACTION.equals(updatedType)) {
        getRelationshipManager().invite(currentIdentity, identity);
      } else if (REMOVE_ACTION.equals(updatedType)) {
        getRelationshipManager().remove(rel);
      }
    }
    
    Relationship relationship = getRelationshipManager().get(currentIdentity, identity);
    
    peopleInfo.setRelationshipType(getRelationshipType(relationship, currentIdentity));
    
    RealtimeListAccess<ExoSocialActivity> activitiesListAccess = getActivityManager().getActivitiesWithListAccess(identity);
    
    List<ExoSocialActivity> activities = activitiesListAccess.loadAsList(0, DEFAULT_LIMIT);
    if (activities.size() > 0) {
      peopleInfo.setActivityTitle(activities.get(0).getTitle());
    } else { // Default title of activity
      peopleInfo.setActivityTitle("No updates have been posted yet.");
    }
    
    peopleInfo.setAvatarURL((String) identity.getProfile().getProperty(Profile.AVATAR_URL));
    
    return Util.getResponse(peopleInfo, uriInfo, mediaType, Response.Status.OK);
  }
  
  /**
   * Gets a set of information of the target user. The returned information of the user includes full name, position
   * avatar, link to profile and relationship status with the current user who sends request.
   * 
   * @param uriInfo The requested URI information.
   * @param securityContext The security context of the system.
   * @param userId The Id of a specific user.
   * @param format The format of the returned result, for example, JSON, or XML.
   * @param currentUserName The current user name who sends request.
   * @param updatedType The type of connection action shown on the pop-up.
   * @return The detailed information of a user.
   * @throws Exception
   * @LevelAPI Platform
   * @anchor PeopleRestService.getPeopleInfo
   */
  @GET
  @Path("/getPeopleInfo/{userId}.{format}")
  public Response getPeopleInfo(@Context UriInfo uriInfo,
                                @Context SecurityContext securityContext,
                                @PathParam("userId") String userId,
                                @PathParam("format") String format,
                                @QueryParam("currentUserName") String currentUserName,
                                @QueryParam("updatedType") String updatedType) throws Exception {
    //
    
    if (format.indexOf('.') > 0) {
      userId = new StringBuffer(userId).append(".").append(format.substring(0, format.lastIndexOf('.'))).toString();
      format = format.substring(format.lastIndexOf('.') + 1);
    }
    
    String[] mediaTypes = new String[] { "json", "xml" };
    format = ArrayUtils.contains(mediaTypes, format) ? format : mediaTypes[0];
    
    if(currentUserName == null || currentUserName.trim().isEmpty()) {
      currentUserName = getUserId(securityContext, uriInfo);
    }
    
    //
    MediaType mediaType = Util.getMediaType(format, mediaTypes);
    
    PeopleInfo peopleInfo = new PeopleInfo(NO_INFO);
    Identity identity = getIdentityManager()
        .getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false);
    if (identity != null) {
      peopleInfo.setRelationshipType(NO_ACTION);
      if(currentUserName != null && !userId.equals(currentUserName)){
        Identity currentIdentity = getIdentityManager()
            .getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUserName, false);

        if(currentIdentity != null) {
          // Process action
          if (updatedType != null) {
            if (currentIdentity != null) {
              if (ACCEPT_ACTION.equals(updatedType)) { // Accept or Deny
                getRelationshipManager().confirm(currentIdentity, identity);
              } else if (DENY_ACTION.equals(updatedType)) {
                getRelationshipManager().deny(currentIdentity, identity);
              } else if (REVOKE_ACTION.equals(updatedType)) {
                getRelationshipManager().deny(currentIdentity, identity);
              } else if (INVITE_ACTION.equals(updatedType)) {
                getRelationshipManager().inviteToConnect(currentIdentity, identity);
              } else if (REMOVE_ACTION.equals(updatedType)) {
                getRelationshipManager().delete(getRelationshipManager().get(currentIdentity, identity));
              }
            }
          }

          // Set relationship type
          Relationship relationship = getRelationshipManager().get(currentIdentity, identity);
          peopleInfo.setRelationshipType(getRelationshipType(relationship, currentIdentity));
        }
      }

      RealtimeListAccess<ExoSocialActivity> activitiesListAccess = getActivityManager().getActivitiesByPoster(identity);
      
      List<ExoSocialActivity> activities = activitiesListAccess.loadAsList(0, 1);
      if (activities.size() > 0) {
        peopleInfo.setActivityTitle(StringEscapeUtils.unescapeHtml(activities.get(0).getTitle()));
      }
      
      Profile userProfile = identity.getProfile();
      
      String avatarURL = userProfile.getAvatarUrl();
      if (avatarURL == null) {
        avatarURL = LinkProvider.PROFILE_DEFAULT_AVATAR_URL;
      }
      
      peopleInfo.setAvatarURL(avatarURL);

      peopleInfo.setProfileUrl(LinkProvider.getUserActivityUri(identity.getRemoteId()));
      
      peopleInfo.setFullName(identity.getProfile().getFullName());
      peopleInfo.setPosition(identity.getProfile().getPosition());
    }
    return Util.getResponse(peopleInfo, uriInfo, mediaType, Response.Status.OK);
  }
  
  public static class ConnectionInfoRestOut extends HashMap<String, Object> {
    public static enum Field {
      /**
       * User Displayname
       */
      DISPLAY_NAME("displayName"),
      /**
       * full url of avatar
       */
      AVATAR_URL("avatarURL"),
      /**
       * full url of profile
       */
      PROFILE_URL("profileURL"),
      /**
       * activity text
       */
      ACTIVITY_TITLE("activityTitle"),
      /**
       * activity text
       */
      ACTIVITY_ID("activityId"),
      /**
       * activity pretty posted time ( ago style )
       */
      PRETTY_POSTED_TIME("prettyPostedTime"),
      /** 
       * Identity's Position 
      */
      POSITION("position");
      
      
     /**
      * String type.
      */
      private final String fieldName;

     /**
      * private constructor.
      *
      * @param string string type
      */
      private Field(final String string) {
        fieldName = string;
      }
      
      public String toString() {
        return fieldName;
      }
    }
    /**
     * Default constructor, used by JAX-RS.
     */
    public ConnectionInfoRestOut() {
      initialize();
    }
    
    public ConnectionInfoRestOut(Identity identity, Activity lastestActivity, String lang){
      this.setDisplayName(identity.getProfile().getFullName());
      this.setAvatarUrl(Util.buildAbsoluteAvatarURL(identity));
      this.setProfileUrl(identity.getProfile().getUrl());
      this.setActivityTitle(lastestActivity.getTitle());
      
      Calendar calendar = Calendar.getInstance();
      calendar.setLenient(false);
      int gmtoffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
      calendar.setTimeInMillis(lastestActivity.getPostedTime() - gmtoffset);
      this.setPrettyPostedTime(TimeConvertUtils.convertXTimeAgo(calendar.getTime(), "EEE,MMM dd,yyyy", new Locale(lang),
                                                                TimeConvertUtils.MONTH));
      
      this.setPosition(identity.getProfile().getPosition());
      this.setActivityId(lastestActivity.getId());
    }
    
    public String getDisplayName() {
      return (String) this.get(Field.DISPLAY_NAME.toString());
    }

    public void setDisplayName(final String displayName) {
      if(displayName != null){
        this.put(Field.DISPLAY_NAME.toString(), displayName);
      } else {
        this.put(Field.DISPLAY_NAME.toString(), "");
      }
    }
    
    public String getAvatarUrl() {
      return (String) this.get(Field.AVATAR_URL.toString());
    }

    public void setAvatarUrl(final String avatarUrl) {
      if(avatarUrl != null){
        this.put(Field.AVATAR_URL.toString(), avatarUrl);
      } else {
        this.put(Field.AVATAR_URL.toString(), "");
      }
    }
    
    
    public String getProfileUrl() {
      return (String) this.get(Field.PROFILE_URL.toString());
    }

    public void setProfileUrl(final String profileUrl) {
      if(profileUrl != null){
        this.put(Field.PROFILE_URL.toString(), Util.getBaseUrl() + profileUrl);
      } else {
        this.put(Field.PROFILE_URL.toString(), "");
      }
    }
    
    public String getActivityTitle() {
      return (String) this.get(Field.ACTIVITY_TITLE.toString());
    }

    public void setActivityTitle(final String activityTitle) {
      if(activityTitle != null){
        this.put(Field.ACTIVITY_TITLE.toString(), activityTitle);
      } else {
        this.put(Field.ACTIVITY_TITLE.toString(), "");
      }
    }
    
    public String getPrettyPostedTime() {
      return  (String) this.get(Field.PRETTY_POSTED_TIME);
    }

    public void setPrettyPostedTime(final String postedTime) {
      if(postedTime != null){
        this.put(Field.PRETTY_POSTED_TIME.toString(), postedTime);
      } else {
        this.put(Field.PRETTY_POSTED_TIME.toString(), new Long(0));
      }
    }
    
    public String getPosition() {
      return  (String) this.get(Field.POSITION);
    }

    public void setPosition(final String position) {
      if(position != null){
        this.put(Field.POSITION.toString(), position);
      } else {
        this.put(Field.POSITION.toString(), "");
      }
    }
    
    public String getActivityId() {
      return  (String) this.get(Field.ACTIVITY_ID);
    }

    public void setActivityId(final String activityId) {
      if(activityId != null){
        this.put(Field.ACTIVITY_ID.toString(), activityId);
      } else {
        this.put(Field.ACTIVITY_ID.toString(), "");
      }
    }    
    private void initialize(){
      this.setActivityTitle("");
      this.setAvatarUrl("");
      this.setDisplayName("");
      this.setProfileUrl("");
      this.setActivityId("");
      this.setPosition("");
      this.setPrettyPostedTime("");
    }
  }
  
  private String getUserId(SecurityContext securityContext, UriInfo uriInfo) {
    String userId = StringUtils.EMPTY;
    try {
      userId = ConversationState.getCurrent().getIdentity().getUserId();
    } catch (Exception e) {}
    if(userId == null || userId.isEmpty() || IdentityConstants.ANONIM.equals(userId)) {
      if (securityContext != null && securityContext.getUserPrincipal() != null) {
        return securityContext.getUserPrincipal().getName();
      } else if (uriInfo != null) {
        return Util.getViewerId(uriInfo);
      }
    }
    return userId;
  }
  
  private void addToNameList(Identity currentIdentity, List<Relationship> identitiesHasRelation, UserNameList nameList) {
    for (Relationship relationship : identitiesHasRelation) {
      Identity identity = relationship.getPartner(currentIdentity);
      String fullName = identity.getProfile().getFullName();
      nameList.addName(fullName);
    }
  }
  
  private void addSpaceUserToList (List<Identity> identities, UserNameList nameList,
                                   Space space, String typeOfRelation) throws SpaceException {
    SpaceService spaceSrv = getSpaceService(); 
    for (Identity identity : identities) {
      String fullName = identity.getProfile().getFullName();
      String userName = (String) identity.getProfile().getProperty(Profile.USERNAME); 
      if (SPACE_MEMBER.equals(typeOfRelation) && spaceSrv.isMember(space, userName)) {
        nameList.addName(fullName);
        continue;
      } else if (USER_TO_INVITE.equals(typeOfRelation) && !spaceSrv.isInvited(space, userName)
                 && !spaceSrv.isPending(space, userName) && !spaceSrv.isMember(space, userName)) {
        nameList.addName(userName);
      }
    }
  }
  
  private HashMap<String, Object> getIdentityInfo(Identity existingIdentity, String lang){
    RealtimeListAccess<ExoSocialActivity>  activityRealtimeListAccess = 
                                            activityManager.getActivitiesWithListAccess(existingIdentity);
    if(activityRealtimeListAccess.getSize() == 0 ){
      return null;
    }
    Activity lastestActivity =  activityRealtimeListAccess.load(0, 1)[0];
    return new ConnectionInfoRestOut(existingIdentity, lastestActivity, lang);
  }
  
  /**
   * Gets type of relationship appropriate to each specific relationship.
   * 
   * @param relationship Relationship of current user and selected user.
   * @param identity Current identity
   * @return Relationship Type.
   */
  private String getRelationshipType(Relationship relationship, Identity identity) {
    if (relationship == null) return ALIEN_STATUS;
    if (relationship.getStatus() == Relationship.Type.PENDING) {
      if (relationship.getSender().equals(identity)) {
        return WAITING_STATUS;  
      }
      return PENDING_STATUS;
    } else if (relationship.getStatus() == Relationship.Type.CONFIRMED) {
      return CONFIRMED_STATUS;
    } else if (relationship.getStatus() == Relationship.Type.IGNORED) {
      return IGNORED_STATUS;
    }
    
    return ALIEN_STATUS;
  }
  
  public SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = (SpaceService) getPortalContainer().getComponentInstanceOfType(SpaceService.class);
    }
    return spaceService;
  }

  /**
   * Gets identityManager
   * @return
   */
  private IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = (IdentityManager) getPortalContainer().getComponentInstanceOfType(IdentityManager.class);
    }
    return identityManager;
  }
  
  /**
   * Gets activity Manager instance.
   * @return activityManager
   * @see ActivityManager
   */
  private ActivityManager getActivityManager() {
    if (activityManager == null) {
      activityManager = (ActivityManager) getPortalContainer().getComponentInstanceOfType(ActivityManager.class);
    }
    return activityManager;
  }
  
  /**
   * Gets identityManager
   * @return
   */
  private RelationshipManager getRelationshipManager() {
    if (relationshipManager == null) {
      relationshipManager = (RelationshipManager) getPortalContainer().getComponentInstanceOfType(RelationshipManager.class);
    }
    return relationshipManager;
  }
  
  /**
   * Gets Portal Container instance.
   * @return portalContainer
   * @see PortalContainer
   */
  private ExoContainer getPortalContainer() {
    ExoContainer exoContainer = ExoContainerContext.getCurrentContainer();
    if (exoContainer == null) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    return exoContainer;
  }
  
  static public class UserInfo {
    static private String AVATAR_URL = "/social-resources/skin/ShareImages/Avatar.gif";

    String id;
    String name;
    String avatar;
    String type;

    public UserInfo(String name) {
      this.name = name;
      this.id = "@" + name;
    }

    public String getId() {
      return id;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public void setAvatar(String url) {
      this.avatar = url;
    }

    public String getAvatar() {
      if (avatar == null || avatar.length() == 0) return AVATAR_URL;
      return avatar;
    }

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }
  }
  
  /**
   * UserNameList class. < /br>
   * 
   * Contains list of user's name that match the input string.
   *
   */
  @XmlRootElement
  static public class UserNameList {
    private List<String> _names;
    /**
     * Sets user name list
     * @param user name list
     */
    public void setNames(List<String> names) {
      this._names = names; 
    }
    
    /**
     * Gets user name list
     * @return user name list
     */
    public List<String> getNames() { 
      return _names; 
    }
    
    /**
     * Add name to user name list
     * @param user name
     */
    public void addName(String name) {
      if (_names == null) {
        _names = new ArrayList<String>();
      }
      _names.add(name);
    }
  }

  /**
   * PeopleInfo class. < /br>
   * 
   * Contains people's information that relate to specific user.
   *
   */
  @XmlRootElement
  static public class PeopleInfo {
    private String id;
    private String profileUrl;
    private String avatarURL;
    private String activityTitle;
    private String relationshipType;
    private String fullName;
    private String position;

    
    public PeopleInfo() {
    }
    
    public PeopleInfo(String relationshipType) {
      this.relationshipType = relationshipType;
    }

    public String getFullName() {
      return fullName;
    }

    public void setFullName(String fullName) {
      this.fullName = fullName;
    }

    public String getActivityTitle() {
      return activityTitle;
    }
    
    public void setActivityTitle(String activityTitle) {
      this.activityTitle = activityTitle;
    }
    
    public String getAvatarURL() {
      return avatarURL;
    }
    
    public void setAvatarURL(String avatarURL) {
      this.avatarURL = avatarURL;
    }

    public String getRelationshipType() {
      return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
      this.relationshipType = relationshipType;
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getProfileUrl() {
      return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
      this.profileUrl = profileUrl;
    }

    public String getPosition() {
      return position;
    }

    public void setPosition(String position) {
      this.position = position;
    }
  }
}
