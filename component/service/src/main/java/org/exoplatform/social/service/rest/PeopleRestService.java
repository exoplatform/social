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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shindig.social.opensocial.model.Activity;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.rest.impl.user.UserRestResourcesV1;
import org.exoplatform.social.service.rest.api.models.IdentityNameList;
import org.exoplatform.social.service.rest.api.models.IdentityNameList.Option;
import org.exoplatform.social.service.rest.api.models.PeopleInfo;
import org.exoplatform.webui.utils.TimeConvertUtils;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.core.Response.Status;
import java.util.*;

/**
 * 
 * Provides REST Services for manipulating jobs relates to people.
 *
 * @anchor PeopleRestService
 */

@Path("social/people")
public class PeopleRestService implements ResourceContainer{
  private static final String SPACE_PREFIX = "space::";
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
  /** User or space to share document with */
  private static final String SHARE_DOCUMENT = "share_document";
  /** User to mention in a comment */
  private static final String MENTION_COMMENT = "mention_comment";
  /** User mentioned in an activity stream */
  private static final String MENTION_ACTIVITY_STREAM = "mention_activity_stream";
  /** No action */
  private static final String NO_ACTION = "NoAction";
  /** No information */
  private static final String NO_INFO = "NoInfo";
  /** Number of user names is added to suggest list. */
  private static final long SUGGEST_LIMIT = 20;

  /** Number of default limit activities. */
  private static final String DEFAULT_ACTIVITY = "DEFAULT_ACTIVITY";
  private static final String LINK_ACTIVITY = "LINK_ACTIVITY";
  private static final String DOC_ACTIVITY = "DOC_ACTIVITY";
  private static final Log LOG = ExoLogger.getLogger(PeopleRestService.class);

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
   * @param activityId the Id of the activity where we want to mention a user in its comment
   * @param typeOfRelation The relationship status such as "confirmed", "pending", "incoming", "member_of_space", "mention_activity_stream", "mention_comment" or "user_to_invite"
   * @param spaceURL The URL of the related space.
   * @param format The format of the returned result, for example, JSON, or XML.
   * @return A list of users' names that match the input string.
   * @throws Exception
   * @LevelAPI Platform
   * @anchor PeopleRestService.suggestUsernames
   */
  @SuppressWarnings("deprecation")
  @RolesAllowed("users")
  @GET
  @Path("suggest.{format}")
  public Response suggestUsernames(@Context UriInfo uriInfo,
                    @QueryParam("nameToSearch") String name,
                    @QueryParam("currentUser") String currentUser,
                    @QueryParam("typeOfRelation") String typeOfRelation,
                    @QueryParam("activityId") String activityId,
                    @QueryParam("spaceURL") String spaceURL,
                    @PathParam("format") String format) throws Exception {
    String[] mediaTypes = new String[] { "json", "xml" };
    MediaType mediaType = Util.getMediaType(format, mediaTypes);

    ProfileFilter identityFilter = new ProfileFilter();

    identityFilter.setName(name);
    identityFilter.setCompany("");
    identityFilter.setPosition("");
    identityFilter.setSkills("");
    Space currentSpace = getSpaceService().getSpaceByUrl(spaceURL);
    Activity currentActivity = getActivityManager().getActivity(activityId);

    List<Identity> excludedIdentityList = identityFilter.getExcludedIdentityList();
    if (excludedIdentityList == null) {
      excludedIdentityList = new ArrayList<Identity>();
    }
    IdentityNameList nameList = new IdentityNameList();
    Identity currentIdentity = Util.getViewerIdentity(currentUser);
    identityFilter.setViewerIdentity(currentIdentity);

    Identity[] result;
    if (PENDING_STATUS.equals(typeOfRelation)) {
      ListAccess<Identity> listAccess = getRelationshipManager().getOutgoingByFilter(currentIdentity, identityFilter);
      result = listAccess.load(0, (int)SUGGEST_LIMIT);
      nameList.addToNameList(result);
    } else if (INCOMING_STATUS.equals(typeOfRelation)) {
      ListAccess<Identity> listAccess = getRelationshipManager().getIncomingByFilter(currentIdentity, identityFilter);
      result = listAccess.load(0, (int)SUGGEST_LIMIT);
      nameList.addToNameList(result);
    } else if (CONFIRMED_STATUS.equals(typeOfRelation)){
      ListAccess<Identity> listAccess = getRelationshipManager().getConnectionsByFilter(currentIdentity, identityFilter);
      result = listAccess.load(0, (int)SUGGEST_LIMIT);
      nameList.addToNameList(result);
    } else if (SPACE_MEMBER.equals(typeOfRelation)) {  // Use in search space member
      List<Identity> identities = Arrays.asList(getIdentityManager().getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, identityFilter, false).load(0, (int)SUGGEST_LIMIT));
      Space space = getSpaceService().getSpaceByUrl(spaceURL);
      addSpaceOrUserToList(identities, nameList, space, typeOfRelation, 0);
    } else if (USER_TO_INVITE.equals(typeOfRelation)) {
      Space space = getSpaceService().getSpaceByUrl(spaceURL);

      // This is for pre-loading data
      if (name != null && name.contains(",")) {
        String[] items = name.split(",");
        for (String item : items) {
          Option opt = new Option();
          if (item.startsWith(SPACE_PREFIX)) {
            Space s = getSpaceService().getSpaceByPrettyName(item.substring(7));
            opt.setType("space");
            opt.setValue(SPACE_PREFIX + s.getPrettyName());
            opt.setText(s.getDisplayName());
            opt.setAvatarUrl(s.getAvatarUrl());
            opt.setOrder(3);
          } else {
            Identity identity = getIdentityManager().getOrCreateIdentity(
                                                     OrganizationIdentityProvider.NAME, item, false);
            opt.setType("user");
            opt.setOrder(1);
            if (identity != null) {
              Profile p = identity.getProfile();
              opt.setValue((String) p.getProperty(Profile.USERNAME));
              opt.setText(p.getFullName() + " (" + (String) p.getProperty(Profile.USERNAME) + ")");
              opt.setAvatarUrl(p.getAvatarUrl());
            } else {
              opt.setValue(item);
              opt.setText(item);
              opt.setInvalid(true);
            }
          }
          nameList.addOption(opt);
        }

        return Util.getResponse(nameList, uriInfo, mediaType, Response.Status.OK);
      }

      // Search in connections first
      ListAccess<Identity> connections = getRelationshipManager().getConnectionsByFilter(currentIdentity, identityFilter);
      if (connections != null && connections.getSize() > 0) {
        int size = connections.getSize();
        Identity[] identities = connections.load(0, size < SUGGEST_LIMIT ? size : (int)SUGGEST_LIMIT);
        for (Identity id : identities) {
          addSpaceOrUserToList(Arrays.asList(id), nameList, space, typeOfRelation, 1);
          excludedIdentityList.add(id);
        }
      }

      List<Space> exclusions = new ArrayList<Space>();
      // Includes spaces the current user is member.
      long remain = SUGGEST_LIMIT - (nameList.getOptions() != null ? nameList.getOptions().size() : 0);
      if (remain > 0) {
        identityFilter.setExcludedIdentityList(excludedIdentityList);
        ListAccess<Identity> listAccess = getIdentityManager().getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, identityFilter, false);
        List<Identity> identities = Arrays.asList(listAccess.load(0, (int) remain));
        addSpaceOrUserToList(identities, nameList, space, typeOfRelation, 2);
      }

      remain = SUGGEST_LIMIT - (nameList.getOptions() != null ? nameList.getOptions().size() : 0);
      if (remain > 0) {
        SpaceFilter spaceFilter = new SpaceFilter();
        spaceFilter.setSpaceNameSearchCondition(name);
        ListAccess<Space> list = getSpaceService().getMemberSpacesByFilter(currentUser, spaceFilter);
        Space[] spaces = list.load(0, (int) remain);
        for (Space s : spaces) {
          //do not add current space
          if (s.equals(space)) {
            exclusions.add(s);
            continue;
          }
          Option opt = new Option();
          opt.setType("space");
          opt.setValue(SPACE_PREFIX + s.getPrettyName());
          opt.setText(s.getDisplayName());
          opt.setAvatarUrl(s.getAvatarUrl());
          opt.setOrder(3);
          nameList.addOption(opt);
          exclusions.add(s);
        }
      }

      // Adding all non hidden spaces.
      remain = SUGGEST_LIMIT - (nameList.getOptions() != null ? nameList.getOptions().size() : 0);
      if (remain > 0) {
        SpaceFilter spaceFilter = new SpaceFilter();
        spaceFilter.setSpaceNameSearchCondition(name);
        spaceFilter.addExclusions(exclusions);
        ListAccess<Space> list = getSpaceService().getVisibleSpacesWithListAccess(currentUser, spaceFilter);
        Space[] spaces = list.load(0, (int) remain);
        for (Space s : spaces) {
          if (s.equals(space)) {
            //do not add current space
            exclusions.add(s);
            continue;
          }
          Option opt = new Option();
          opt.setType("space");
          opt.setValue(SPACE_PREFIX + s.getPrettyName());
          opt.setText(s.getDisplayName());
          opt.setAvatarUrl(s.getAvatarUrl());
          opt.setOrder(4);
          nameList.addOption(opt);
        }
      }
    } else if (SHARE_DOCUMENT.equals(typeOfRelation)) {

      // This is for pre-loading data
      if (name != null && name.contains(",")) {
        String[] items = name.split(",");
        for (String item : items) {
          Option opt = new Option();
          if (item.startsWith(SPACE_PREFIX)) {
            Space s = getSpaceService().getSpaceByPrettyName(item.substring(7));
            opt.setType("space");
            opt.setValue(SPACE_PREFIX + s.getPrettyName());
            opt.setText(s.getDisplayName());
            opt.setAvatarUrl(s.getAvatarUrl());
            opt.setOrder(2);
          } else {
            Identity identity = getIdentityManager().getOrCreateIdentity(
                OrganizationIdentityProvider.NAME, item, false);
            opt.setType("user");
            opt.setOrder(1);
            if (identity != null) {
              Profile p = identity.getProfile();
              opt.setValue((String) p.getProperty(Profile.USERNAME));
              opt.setText(p.getFullName());
              opt.setAvatarUrl(p.getAvatarUrl());
            } else {
              opt.setValue(item);
              opt.setText(item);
              opt.setInvalid(true);
            }
          }
          nameList.addOption(opt);
        }

        return Util.getResponse(nameList, uriInfo, mediaType, Response.Status.OK);
      }

      // Search in connections first
      ListAccess<Identity> connections = getRelationshipManager().getConnectionsByFilter(currentIdentity, identityFilter);
      if (connections != null && connections.getSize() > 0) {
        int size = connections.getSize();
        Identity[] identities = connections.load(0, size < SUGGEST_LIMIT ? size : (int)SUGGEST_LIMIT);
        for (Identity id : identities) {
          Option opt = new Option();
          String fullName = id.getProfile().getFullName();
          String userName = (String) id.getProfile().getProperty(Profile.USERNAME);
          opt.setType("user");
          opt.setValue(userName);
          opt.setText(fullName + " (" + userName + ")");
          opt.setAvatarUrl(id.getProfile() == null ? null : id.getProfile().getAvatarUrl());
          excludedIdentityList.add(id);
          opt.setOrder(1);
          nameList.addOption(opt);
        }
      }

      List<Space> exclusions = new ArrayList<Space>();
      // Includes spaces the current user is member.
      long remain = SUGGEST_LIMIT - (nameList.getOptions() != null ? nameList.getOptions().size() : 0);
      if (remain > 0) {
        SpaceFilter spaceFilter = new SpaceFilter();
        spaceFilter.setSpaceNameSearchCondition(name);
        ListAccess<Space> list = getSpaceService().getMemberSpacesByFilter(currentUser, spaceFilter);
        Space[] spaces = list.load(0, (int) remain);
        for (Space s : spaces) {
          Option opt = new Option();
          opt.setType("space");
          opt.setValue(SPACE_PREFIX + s.getPrettyName());
          opt.setText(s.getDisplayName());
          opt.setAvatarUrl(s.getAvatarUrl());
          opt.setOrder(2);
          nameList.addOption(opt);
          exclusions.add(s);
        }
      }
      remain = SUGGEST_LIMIT - (nameList.getOptions() != null ? nameList.getOptions().size() : 0);
      if (remain > 0) {
        identityFilter.setExcludedIdentityList(excludedIdentityList);
        ListAccess<Identity> listAccess = getIdentityManager().getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, identityFilter, false);
        List<Identity> identities = Arrays.asList(listAccess.load(0, (int) remain));
        for (Identity id : identities) {
          Option opt = new Option();
          String fullName = id.getProfile().getFullName();
          String userName = (String) id.getProfile().getProperty(Profile.USERNAME);
          opt.setType("user");
          opt.setValue(userName);
          opt.setText(fullName);
          opt.setAvatarUrl(id.getProfile() == null ? null : id.getProfile().getAvatarUrl());
          excludedIdentityList.add(id);
          opt.setOrder(4);
          nameList.addOption(opt);
        }
      }
    } else if (MENTION_ACTIVITY_STREAM.equals(typeOfRelation)) {
      LinkedHashSet<UserInfo> userInfos = new LinkedHashSet<UserInfo>();

      // first add space members in the suggestion list when mentioning in a space Activity Stream
      if (currentSpace != null) {
        userInfos = addSpaceMembers(spaceURL, identityFilter, userInfos, currentUser);
      }
      // then add connections in the suggestions
      long remain = SUGGEST_LIMIT - (userInfos != null ? userInfos.size() : 0);
      if (remain > 0) {
        userInfos = addUserConnections(currentIdentity, identityFilter, userInfos, currentUser, remain);
      }

      // finally add others users in the suggestions
      remain = SUGGEST_LIMIT - (userInfos != null ? userInfos.size() : 0);
      if (remain > 0) {
        userInfos = addOtherUsers(identityFilter, excludedIdentityList, userInfos, currentUser, remain);
      }
      return Util.getResponse(userInfos, uriInfo, mediaType, Response.Status.OK);

    } else if (MENTION_COMMENT.equals(typeOfRelation)) {
      LinkedHashSet<UserInfo> userInfos = new LinkedHashSet<UserInfo>();
      long remain = SUGGEST_LIMIT;

      if(activityId == null) {
        LOG.warn("Mentioning in activity comment : activity id parameter is null. the activity users will not be added in the result of mentioning.");
      } else {
        ExoSocialActivity activity = getActivityManager().getActivity(activityId);

        // first add the author in the suggestion
        String authorId = activity.getPosterId();
        userInfos = addUsernameToInfosList(authorId, identityFilter, userInfos, currentUser, true);

        // then add the commented users in the suggestion list
        userInfos = addCommentedUsers(activity, identityFilter, excludedIdentityList, userInfos, currentUser);

        // add the mentioned users in the suggestion
        remain = SUGGEST_LIMIT - (userInfos != null ? userInfos.size() : 0);
        if (remain > 0) {
          userInfos = addMentionedUsers(activity, identityFilter, excludedIdentityList, userInfos, currentUser);
        }

        // add the liked users in the suggestion
        remain = SUGGEST_LIMIT - (userInfos != null ? userInfos.size() : 0);
        if (remain > 0) {
          userInfos = addLikedUsers(activity, identityFilter, excludedIdentityList, userInfos, currentUser);
        }

      }

      // add space members in the suggestion list when mentioning in a comment in a space Activity Stream
      if (currentSpace != null && currentActivity != null) {
        remain = SUGGEST_LIMIT - (userInfos != null ? userInfos.size() : 0);
        if (remain > 0) {
          userInfos = addSpaceMembers(spaceURL, identityFilter, userInfos, currentUser);
        }
      }

      // add the connections in the suggestion
      remain = SUGGEST_LIMIT - (userInfos != null ? userInfos.size() : 0);
      if (remain > 0) {
        userInfos = addUserConnections(currentIdentity, identityFilter, userInfos, currentUser, remain);
      }

      // finally add others in the suggestion
      remain = SUGGEST_LIMIT - (userInfos != null ? userInfos.size() : 0);
      if (remain > 0) {
        userInfos = addOtherUsers(identityFilter, excludedIdentityList, userInfos, currentUser, remain);
      }

      return Util.getResponse(userInfos, uriInfo, mediaType, Response.Status.OK);

    } else { // Identities that match the keywords.
      result = getIdentityManager().getIdentityStorage().getIdentitiesForMentions(OrganizationIdentityProvider.NAME, identityFilter, null, 0L, SUGGEST_LIMIT, false).toArray(new Identity[0]);
      nameList.addToNameList(result);
    }

    return Util.getResponse(nameList, uriInfo, mediaType, Response.Status.OK);
  }

  private LinkedHashSet<UserInfo> addUsersToUserInfosList(Identity[] identities, ProfileFilter identityFilter, LinkedHashSet<UserInfo> userInfos, String currentUserId,  boolean filterByName) {
    for (Identity identity : identities) {
      userInfos = addUserToInfosList(identity, identityFilter, userInfos, currentUserId, filterByName);
    }
    return userInfos;
  }

  private LinkedHashSet<UserInfo> addUserToInfosList(Identity userIdentity, ProfileFilter identityFilter, LinkedHashSet<UserInfo> userInfos, String currentUserId, boolean filterByName) {
    if (!userIdentity.getProviderId().equals(OrganizationIdentityProvider.NAME)) {
      LOG.warn("Cannot add Identity to suggestion list. Identity with id '"+ userIdentity.getRemoteId() + "' is not of type 'user'");
      return userInfos;
    }
    if (userInfos.size() == SUGGEST_LIMIT) {
      return userInfos;
    }
    if (identityFilter.getExcludedIdentityList().contains(userIdentity)) {
      return userInfos;
    }
    if(filterByName && !userIdentity.getRemoteId().toLowerCase().contains(identityFilter.getName().toLowerCase()) && !userIdentity.getProfile().getFullName().toLowerCase().contains(identityFilter.getName().toLowerCase())) {
      return userInfos;
    }
    UserInfo user = new UserInfo();
    boolean isAnonymous = IdentityConstants.ANONIM.equals(currentUserId);
    if (!isAnonymous) {
      user.setId(userIdentity.getRemoteId());
    }
    user.setName(userIdentity.getProfile() == null ? null : userIdentity.getProfile().getFullName());
    user.setAvatar(userIdentity.getProfile() == null ? null : userIdentity.getProfile().getAvatarUrl());
    user.setType("contact");
    userInfos.add(user);
    return userInfos;
  }

  private LinkedHashSet<UserInfo> addUsernameToInfosList(String userId, ProfileFilter identityFilter, LinkedHashSet<UserInfo> userInfos, String currentUserId, boolean filterByName) {
    Identity userIdentity = getIdentityManager().getIdentity(userId, false);
    if (userIdentity == null) {
      userIdentity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, false);
    }
    if (userIdentity == null) {
      LOG.warn("Cannot find user identity with username = " + userId);
      return userInfos;
    }
    return addUserToInfosList(userIdentity, identityFilter, userInfos, currentUserId, filterByName);
  }

  private LinkedHashSet<UserInfo> addUserConnections (Identity currentIdentity, ProfileFilter identityFilter, LinkedHashSet<UserInfo> userInfos, String currentUser, long remain) throws Exception {
    ListAccess<Identity> connections = getRelationshipManager().getConnectionsByFilter(currentIdentity, identityFilter);
    if (connections != null && connections.getSize() > 0) {
      Identity[] identities = connections.load(0, (int) remain);
      userInfos = addUsersToUserInfosList(identities, identityFilter, userInfos, currentUser, false);
    }
    return userInfos;
  }

  private LinkedHashSet<UserInfo> addOtherUsers (ProfileFilter identityFilter, List<Identity> excludedIdentityList, LinkedHashSet<UserInfo> userInfos, String currentUser, long remain) throws Exception {
    List<Identity> listAccess = getIdentityManager().getIdentityStorage().getIdentitiesForMentions(OrganizationIdentityProvider.NAME, identityFilter, null, 0L, remain, false);
    identityFilter.setExcludedIdentityList(excludedIdentityList);
    Identity[] identitiesList = listAccess.toArray(new Identity[0]);
    userInfos = addUsersToUserInfosList(identitiesList, identityFilter, userInfos, currentUser, false);
    return userInfos;
  }

  private LinkedHashSet<UserInfo> addSpaceMembers (String spaceURL, ProfileFilter identityFilter, LinkedHashSet<UserInfo> userInfos, String currentUser) {
    String[] spaceMembers = getSpaceService().getSpaceByUrl(spaceURL).getMembers();
    for (String spaceMember : spaceMembers) {
      userInfos = addUsernameToInfosList(spaceMember, identityFilter,userInfos, currentUser, true);
    }
    return userInfos;
  }

  private LinkedHashSet<UserInfo> addCommentedUsers (ExoSocialActivity activity, ProfileFilter identityFilter, List<Identity> excludedIdentityList, LinkedHashSet<UserInfo> userInfos, String currentUser) {
    String[] commentedUsers = activity.getCommentedIds();
    for (String commentedUser : commentedUsers) {
      identityFilter.setExcludedIdentityList(excludedIdentityList);
      userInfos = addUsernameToInfosList(commentedUser, identityFilter, userInfos, currentUser, true);
    }
    return userInfos;
  }

  private LinkedHashSet<UserInfo> addMentionedUsers(ExoSocialActivity activity, ProfileFilter identityFilter, List<Identity> excludedIdentityList, LinkedHashSet<UserInfo> userInfos, String currentUser) {
    String[] mentionedUsers = activity.getMentionedIds();
    for (String mentionedUser : mentionedUsers) {
      identityFilter.setExcludedIdentityList(excludedIdentityList);
      userInfos = addUsernameToInfosList(mentionedUser, identityFilter, userInfos, currentUser, true);
    }
    return userInfos;
  }

  private LinkedHashSet<UserInfo> addLikedUsers(ExoSocialActivity activity, ProfileFilter identityFilter, List<Identity> excludedIdentityList, LinkedHashSet<UserInfo> userInfos, String currentUser) {
    String[] likedUsers = activity.getLikeIdentityIds();
    for (String likedUser : likedUsers) {
      identityFilter.setExcludedIdentityList(excludedIdentityList);
      userInfos = addUsernameToInfosList(likedUser, identityFilter, userInfos, currentUser, true);
    }
    return userInfos;
  }

  private void addSpaceOrUserToList(List<Identity> identities, IdentityNameList options,
                                   Space space, String typeOfRelation, int order) throws SpaceException {
    SpaceService spaceSrv = getSpaceService(); 
    for (Identity identity : identities) {
      String fullName = identity.getProfile().getFullName();
      String userName = (String) identity.getProfile().getProperty(Profile.USERNAME); 
      Option opt = new Option();
      if (SPACE_MEMBER.equals(typeOfRelation) && spaceSrv.isMember(space, userName)) {
        opt.setType("user");
        opt.setValue(fullName);
        opt.setText(fullName);
        opt.setAvatarUrl(identity.getProfile() == null ? null : identity.getProfile().getAvatarUrl());
      } else if (USER_TO_INVITE.equals(typeOfRelation) && (space == null || (!spaceSrv.isInvitedUser(space, userName)
                 && !spaceSrv.isPendingUser(space, userName) && !spaceSrv.isMember(space, userName)))) {
        opt.setType("user");
        opt.setValue(userName);
        opt.setText(fullName + " (" + userName + ")");
        opt.setAvatarUrl(identity.getProfile() == null ? null : identity.getProfile().getAvatarUrl());
      } else {
        continue;
      }
      opt.setOrder(order);
      options.addOption(opt);
    }
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
   * @deprecated Deprecated from 4.3.x. Replaced by a new API {@link UserRestResourcesV1#getUsers(UriInfo, String, String, String, int, int, boolean, String)}
   * 
   */
  @GET
  @Path("getprofile/data.json")
  @RolesAllowed("users")
  public Response suggestUsernames(@Context UriInfo uriInfo,
                                   @Context SecurityContext securityContext,
                    @QueryParam("search") String query) throws Exception {
    MediaType mediaType = Util.getMediaType("json", new String[]{"json"});
    ProfileFilter filter = new ProfileFilter();
    
    filter.setName(query);
    List<Identity> identities = Arrays.asList(getIdentityManager().getIdentitiesByProfileFilter(
                                  OrganizationIdentityProvider.NAME, filter, false).load(0, (int)SUGGEST_LIMIT));
    
    List<UserInfo> userInfos = new ArrayList<PeopleRestService.UserInfo>(identities.size());
    UserInfo userInfo;
    String userType = ConversationState.getCurrent().getIdentity().getUserId();
    boolean isAnonymous = IdentityConstants.ANONIM.equals(userType) 
      || securityContext.getUserPrincipal() == null;
    
    for (Identity identity : identities) {
      userInfo = new UserInfo();
      if (!isAnonymous) {
        userInfo.setId(identity.getRemoteId());
      }
      userInfo.setName(identity.getProfile().getFullName());
      userInfo.setAvatar(identity.getProfile() == null ? null : identity.getProfile().getAvatarUrl());
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
   * @deprecated Deprecated from 4.3.x. Replaced by a new API {@link UserRestResourcesV1#getConnectionOfUser(UriInfo, String, boolean, String)}
   */
  @GET
  @Path("{portalName}/getConnections.{format}")
  @RolesAllowed("users")
  public Response searchConnection(@Context UriInfo uriInfo,
                                   @Context SecurityContext securityContext,
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
    
    Identity currentUser = Util.getIdentityManager(portalName).getOrCreateIdentity(OrganizationIdentityProvider.NAME,
                                                                                   Util.getViewerId(uriInfo), true);
    Identity[] identities;
    List<HashMap<String, Object>> entitys = new ArrayList<HashMap<String,Object>>();
    if (nameToSearch == null) { 
      // default loading, if load more then need to re-calculate offset and limit before going here via rest URL.     
      identities = getIdentityManager().getConnectionsWithListAccess(currentUser).load(offset, limit);
    } else { 
      // search
      nameToSearch = nameToSearch.trim();
      
      ProfileFilter filter = new ProfileFilter();
      filter.setName(nameToSearch);
      filter.setViewerIdentity(currentUser);
      // will be getConnectionsByProfileFilter
      identities = relationshipManager.getConnectionsByFilter(currentUser, filter).load(offset, limit);
    }
    
    String userType = ConversationState.getCurrent().getIdentity().getUserId();
    boolean isAnonymous = IdentityConstants.ANONIM.equals(userType) 
      || securityContext.getUserPrincipal() == null;
    
    for(Identity identity : identities){
      if (isAnonymous) {
        entitys.add(new ConnectionInfoRestOut(identity));
        continue;
      }
      
      HashMap<String, Object> temp = getIdentityInfo(identity, lang);
      if(temp != null){
        entitys.add(temp);
      }
    }
    
    return Util.getResponse(entitys, uriInfo, mediaType, Response.Status.OK);
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
  @RolesAllowed("users")
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
    Identity currentIdentity = getIdentityManager()
        .getOrCreateIdentity(OrganizationIdentityProvider.NAME, currentUserName, false);
    if (identity != null) {
      // public information
      peopleInfo.setFullName(identity.getProfile().getFullName());
      peopleInfo.setPosition(StringEscapeUtils.unescapeHtml(identity.getProfile().getPosition()));
      peopleInfo.setDeleted(identity.isDeleted());
      Profile userProfile = identity.getProfile();
      String avatarURL = userProfile.getAvatarUrl();
      if (avatarURL == null) {
        avatarURL = LinkProvider.PROFILE_DEFAULT_AVATAR_URL;
      }
      peopleInfo.setAvatarURL(avatarURL);
      
      
      String userType = ConversationState.getCurrent().getIdentity().getUserId();
      boolean isAnonymous = IdentityConstants.ANONIM.equals(userType) 
          || securityContext.getUserPrincipal() == null || !userType.equals(currentIdentity.getRemoteId());
      
      if (!isAnonymous) { // private information
        peopleInfo.setProfileUrl(LinkProvider.getProfileUri(identity.getRemoteId()));
        
        peopleInfo.setRelationshipType(NO_ACTION);
        
        String relationshipType = null;
        
        if(currentUserName != null && !userId.equals(currentUserName)) {
          // Set relationship type
          Relationship relationship = getRelationshipManager().get(currentIdentity, identity);
          
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

                relationship = getRelationshipManager().get(currentIdentity, identity);
              }
            }
  
            relationshipType = getRelationshipType(relationship, currentIdentity);
            peopleInfo.setRelationshipType(relationshipType);
          }
        }
        
        if (CONFIRMED_STATUS.equals(relationshipType)) {
        
          // exposed if relationship type is confirmed (has connection with current logged in user)
          String activityTitle = getLatestActivityTitle(identity, currentIdentity);
          if (activityTitle != null) {
            peopleInfo.setActivityTitle(StringEscapeUtils.unescapeHtml(activityTitle));
          }
        }
      }
    }
    
    return Util.getResponse(peopleInfo, uriInfo, mediaType, Response.Status.OK);
  }

  public static class ConnectionInfoRestOut extends HashMap<String, Object> {

    private static final long serialVersionUID = -3638967656497819786L;

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
    
    public ConnectionInfoRestOut(Identity identity) {
      this.setDisplayName(identity.getProfile().getFullName());
      this.setAvatarUrl(Util.buildAbsoluteAvatarURL(identity));
      this.setPosition(StringEscapeUtils.unescapeHtml(identity.getProfile().getPosition()));
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
      
      this.setPosition(StringEscapeUtils.unescapeHtml(identity.getProfile().getPosition()));
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
    } catch (Exception e) {
      LOG.debug("Could not get id of user from ConversationState.");  
    }
    
    if(userId == null || userId.isEmpty() || IdentityConstants.ANONIM.equals(userId)) {
      if (securityContext != null && securityContext.getUserPrincipal() != null) {
        return securityContext.getUserPrincipal().getName();
      } else if (uriInfo != null) {
        return Util.getViewerId(uriInfo);
      }
    }
    return userId;
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
    static private String AVATAR_URL = "/eXoSkin/skin/images/system/UserAvtDefault.png";

    String id;
    String name;
    String avatar;
    String type;

    public void setId(String id) {
      this.id = "@" + id;
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

    @Override
    public int hashCode() {
      return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      if(obj == null || !(obj instanceof UserInfo)) {
        return false;
      }
      return id.equals(((UserInfo)obj).getId());
    }
  }

  private String getLatestActivityTitle(Identity identity, Identity currentIdentity) {
    RealtimeListAccess<ExoSocialActivity> activitiesListAccess = getActivityManager()
        .getActivitiesByPoster(identity, DEFAULT_ACTIVITY, LINK_ACTIVITY, DOC_ACTIVITY);
    
    int totalActivities = activitiesListAccess.getSize();
    int loadedActivityNum = 0;
    while (true) {
      List<ExoSocialActivity> activities = activitiesListAccess.loadAsList(0, 20);
      
      loadedActivityNum += activities.size();
      
      for (ExoSocialActivity act : activities) {
        
        if (getIdentityManager().getOrCreateIdentity(
            OrganizationIdentityProvider.NAME, act.getStreamOwner(), false) != null) {
          return act.getTitle();
        }
        
        if (getIdentityManager().getOrCreateIdentity(
            SpaceIdentityProvider.NAME, act.getStreamOwner(), false) != null) {
          Space space = getSpaceService().getSpaceByPrettyName(act.getStreamOwner());
          if (getSpaceService().isMember(space, currentIdentity.getRemoteId())) {
            return act.getTitle();
          }
        }
      }
      
      if (loadedActivityNum >= totalActivities) {
        return null;  
      }
    }
  }
}
