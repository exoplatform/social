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
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlRootElement;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.resource.ResourceContainer;
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
    UserNameList nameList = new UserNameList();
    ProfileFilter filter = new ProfileFilter();
    
    filter.setName(name);
    filter.setCompany("");
    filter.setGender("");
    filter.setPosition("");
    filter.setSkills("");
    List<Identity> identities = getIdentityManager()
        .getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, filter, 0, SUGGEST_LIMIT);

    Identity currentIdentity = getIdentityManager().getIdentity(OrganizationIdentityProvider.NAME, currentUser, false);
    
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
        String fullName = identity.getProfile().getFullName();
        String userName = (String) identity.getProfile().getProperty(Profile.USERNAME);
        
        if (currentUser.equals(userName)) {
          continue;
        }
        
        nameList.addName(fullName);
      }
    }
    
    return Util.getResponse(nameList, uriInfo, mediaType, Response.Status.OK);
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
    Identity identity = getIdentityManager().getIdentity(OrganizationIdentityProvider.NAME, userId, false);
    Identity currentIdentity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUserName, false);
    
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
    
    List<ExoSocialActivity> activities = getActivityManager().getActivities(identity);
    if (activities.size() > 0) {
      peopleInfo.setActivityTitle(activities.get(0).getTitle());
    } else { // Default title of activity
      peopleInfo.setActivityTitle("Not any updates posted yet.");
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
