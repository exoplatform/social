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
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.shindig.social.opensocial.model.Activity;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.resource.ResourceContainer;
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
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.webui.utils.TimeConvertUtils;

/**
 * PeopleRestService.java < /br>
 * 
 * Provides REST Services for manipulating jobs realtes to people.
 * 
 * @author hanhvq@gmail.com
 * @since Nov 22, 2010  
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
  private static final String REMOVE_ACTION = "Remove";
  /** Member of space Status information */
  private static final String SPACE_MEMBER = "member_of_space";
  /** User to invite to join the space Status information */
  private static final String USER_TO_INVITE = "user_to_invite";
  /** Number of user names is added to suggest list. */
  private static final long SUGGEST_LIMIT = 20;
  
  /** Number of default limit activities. */
  private static final int DEFAULT_LIMIT = 20;
  
  private String portalName_;
  private IdentityManager identityManager;
  private ActivityManager activityManager;
  private RelationshipManager relationshipManager;
  private SpaceService spaceService;
  
  public PeopleRestService() {
  }

  /**
   * Gets and returns list of user's name that match the input string for suggesting.
   * 
   * @param uriInfo
   * @param name
   * @param format
   * @return list of user's name match the input string.
   * @throws Exception
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
   * Gets and returns list of user's name that match the input string for suggesting.
   * 
   * @param uriInfo
   * @param name
   * @param format
   * @return list of user's name match the input string.
   * @throws Exception
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
   * Gets and returns list people's information that have had connection with current viewer.
   * 
   * @param uriInfo
   * @param portalName
   * @param nameToSearch
   * @param offset
   * @param limit
   * @param lang
   * @param format
   * 
   * @return list people's information.
   * @throws Exception
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
  
  private HashMap<String, Object> getIdentityInfo(Identity existingIdentity, String lang){
    RealtimeListAccess<ExoSocialActivity>  activityRealtimeListAccess = 
                                            activityManager.getActivitiesWithListAccess(existingIdentity);
    if(activityRealtimeListAccess.getSize() == 0 ){
      return null;
    }
    Activity lastestActivity =  activityRealtimeListAccess.load(0, 1)[0];
    return new ConnectionInfoRestOut(existingIdentity, lastestActivity, lang);
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
  
  
  /**
   * Gets and returns information of people that are displayed as detail user's information on popup.
   * @param uriInfo
   * @param portalName name of current portal container.
   * @param currentUserName Name of current user.
   * @param userId Id of user is specified.
   * @param format
   * @param update
   * @return Information of people appropriate focus user.
   * @throws Exception
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
    portalName_ = portalName;
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
      PortalContainer portalContainer = (PortalContainer) ExoContainerContext.getCurrentContainer();
      spaceService = (SpaceService) portalContainer.getComponentInstanceOfType(SpaceService.class);
    }
    return spaceService;
  }

  /**
   * Gets identityManager
   * @return
   */
  private IdentityManager getIdentityManager() {
    if (identityManager == null) {
      PortalContainer portalContainer = (PortalContainer) ExoContainerContext.getCurrentContainer();
      identityManager = (IdentityManager) portalContainer.getComponentInstanceOfType(IdentityManager.class);
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
      PortalContainer portalContainer = (PortalContainer) ExoContainerContext.getCurrentContainer();
      relationshipManager = (RelationshipManager) portalContainer.getComponentInstanceOfType(RelationshipManager.class);
    }
    return relationshipManager;
  }
  
  /**
   * Gets Portal Container instance.
   * @return portalContainer
   * @see PortalContainer
   */
  private PortalContainer getPortalContainer() {
    PortalContainer portalContainer = (PortalContainer) ExoContainerContext.getContainerByName(portalName_);
    if (portalContainer == null) {
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }
    return portalContainer;
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
    private String avatarURL;
    private String activityTitle;
    private String relationshipType;
    
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
  }
}
