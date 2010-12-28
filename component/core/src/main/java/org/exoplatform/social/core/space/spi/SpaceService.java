/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.core.space.spi;

import java.util.List;

import org.exoplatform.social.core.application.PortletPreferenceRequiredPlugin;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.model.Space;

/**
 * SpaceService provides methods for working with Space.
 *
 * @author <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 * @since Aug 29, 2008
 */
public interface SpaceService {

  static final String SPACES_APP_ID = "exosocial:spaces";

  /**
   * Gets all spaces in Social.
   *
   * @return list of spaces in Social
   * @throws SpaceException with code SpaceException.Code.ERROR_DATASTORE
   */
  List<Space> getAllSpaces() throws SpaceException;

  /**
   * Get a space by its space display name.
   *
   * @param spaceDisplayName
   * @return
   * @throws SpaceException
   * @since  1.2.0-GA
   */
  Space getSpaceByDisplayName(String spaceDisplayName) throws SpaceException;

  /**
   * Gets a space by its space name.
   *
   * @param spaceName space name
   * @return the stored space
   * @throws SpaceException
   * @deprecated Use {@link SpaceService#getSpaceByPrettyName(String)} instead.
   *             Will be removed at 1.3.x
   */
  public Space getSpaceByName(String spaceName) throws SpaceException;

  /**
   * Gets a space by its space name.
   *
   * @param spaceName space name
   * @return the stored space
   * @throws SpaceException
   */
  Space getSpaceByPrettyName(String spaceName) throws SpaceException;

  /**
   * Gets all spaces has the name starting with the input character.
   *
   * @return all spaces which have first character of name matched the input string.
   * @throws SpaceException with code SpaceException.Code.ERROR_DATASTORE
   */
  List<Space> getSpacesByFirstCharacterOfName(String firstCharacterOfName) throws SpaceException;

  /**
   * Gets all spaces which has name or description that match input condition.
   *
   * @param condition the input condition
   * @return a list of spaces
   * @throws Exception
   */
  List<Space> getSpacesBySearchCondition(String condition) throws Exception;

  /**
   * Gets a space by its id.
   *
   * @param spaceId Id of that space
   * @return space with id specified
   * @throws SpaceException with code SpaceException.Code.ERROR_DATASTORE
   */
  Space getSpaceById(String spaceId) throws SpaceException;

  /**
   * Gets a space by its url.
   *
   * @param spaceUrl url of space
   * @return the space with string url specified
   * @throws SpaceException
   */
  Space getSpaceByUrl(String spaceUrl) throws SpaceException;

  /**
   * Gets spaces of a user in which that user is a member.
   *
   * @param userId Id of user
   * @return all spaces of a user in which the user is a member
   * @throws SpaceException with code SpaceException.Code.ERROR_DATASTORE
   */
  List<Space> getSpaces(String userId) throws SpaceException;

  /**
   * Gets spaces of a user which that user has the access permission.
   *
   * @param userId
   * @return list of spaces
   * @throws SpaceException
   */
  List<Space> getAccessibleSpaces(String userId) throws SpaceException;

  /**
   * Gets spaces of a user which that user has the edit permission.
   *
   * @param userId
   * @return list of space
   * @throws SpaceException
   */
  List<Space> getEditableSpaces(String userId) throws SpaceException;

  /**
   * Gets a user's invited spaces and that user can accept or deny the request.
   *
   * @param userId
   * @return spaces list of all user's invited spaces
   * @throws SpaceException
   */
  List<Space> getInvitedSpaces(String userId) throws SpaceException;


  /**
   * Gets a user's public spaces and that user can request to join.
   *
   * @param userId Id of user
   * @return spaces list in which the user can request to join
   * @throws SpaceException
   */
  List<Space> getPublicSpaces(String userId) throws SpaceException;

  /**
   * Gets a user's pending spaces and that the user can revoke that request.
   *
   * @param userId
   * @return spaces list in which the user can revoke that request
   * @throws SpaceException
   */
  List<Space> getPendingSpaces(String userId) throws SpaceException;

  /**
   * Creates a new space by creating a new group.
   * This new group will be under /Spaces node.
   * This is shorthand for calling createSpace(space, creator, null).
   *
   * @param space
   * @param creator
   * @return the created space
   * @throws SpaceException with possible code SpaceException.Code.SPACE_ALREADY_EXIST, UNABLE_TO_ADD_CREATOR
   */
  Space createSpace(Space space, String creator) throws SpaceException;

  /**
   * Creates a new space from an existing group.
   *
   * @param space
   * @param creator
   * @param groupId if groupId == null : create new space by creating new group
   * @return space
   * @throws SpaceException with possible code SpaceException.Code.SPACE_ALREADY_EXIST; UNABLE_TO_ADD_CREATOR
   */
  Space createSpace(Space space, String creator, String groupId) throws SpaceException;

  /**
   * Saves a new space or updates a space.
   *
   * @param space space is saved
   * @param isNew true if creating a new space; otherwise, update an existing space.
   * @throws SpaceException with code: SpaceException.Code.ERROR_DATASTORE
   */
  void saveSpace(Space space, boolean isNew) throws SpaceException;

  /**
   * Deletes a space. When deleting a space, all of its page navigations and its group will be deleted.
   *
   * @param space the space to be deleted
   * @throws SpaceException
   */
  void deleteSpace(Space space) throws SpaceException;

  /**
   * Deletes a space by its id.
   *
   * @param spaceId
   * @throws SpaceException
   */
  void deleteSpace(String spaceId) throws SpaceException;

  /**
   * Initializes default applications in a space.
   *
   * @param space the space
   * @throws SpaceException with code SpaceException.Code.UNABLE_INIT_APP
   * @deprecated Use {@link #initApps(Space)} instead
   */
  void initApp(Space space) throws SpaceException;

  /**
   * Initialize default applications in a space.
   * Set <tt>space.homeNodeApp</tt> from configuration file to be the root page of that space node.
   * When removing a space, make sure to call {@link #deInitApps(Space)} and then {@link #deleteSpace(Space)}
   * or {@link #deleteSpace(String)}
   *
   *
   * @param space Space
   * @throws SpaceException with code SpaceException.Code.UNABLE_INIT_APP
   */
  void initApps(Space space) throws SpaceException;

  /**
   * De-initializes the applications of a space.
   * Make sure to call this method before {@link #deleteSpace(Space)} or {@link #deleteSpace(String)},
   * Otherwise, the space is deleted but its pages and navigation still exists.
   * @param space the space
   * @throws SpaceException
   */
  void deInitApps(Space space) throws SpaceException;

  /**
   * Adds a user to a space, the user will get the "member" role in a space.
   *
   * @param space
   * @param userId
   * @throws SpaceException
   */
  void addMember(Space space, String userId) throws SpaceException;

  /**
   * Adds a user to a space, the user will get the "member" role in a space.
   *
   * @param spaceId
   * @param userId
   * @throws SpaceException
   */
  void addMember(String spaceId, String userId) throws SpaceException;

  /**
   * Removes a member from a space.
   * If the member is the only leader of that space, the member removed is not allowed and throws SpaceException
   * with Code = USER_ONLY_LEADER
   *
   * @param space
   * @param userId
   * @throws SpaceException
   */
  void removeMember(Space space, String userId) throws SpaceException;

  /**
   * Removes a member from a space.
   *
   * @param spaceId
   * @param userId
   * @throws SpaceException
   */
  void removeMember(String spaceId, String userId) throws SpaceException;

  /**
   * Gets a list of the space members from a space.
   *
   * @param space
   * @return list of space members
   * @throws SpaceException
   */

  List<String> getMembers(Space space) throws SpaceException;

  /**
   * Gets a list of the space members from a space.
   *
   * @param spaceId
   * @return a list of the space members
   * @throws SpaceException
   */
  List<String> getMembers(String spaceId) throws SpaceException;

  /**
   * Sets a member of a space as a manager.
   *
   * @param space
   * @param userId
   * @param isLeader
   * @throws SpaceException
   */
  void setLeader(Space space, String userId, boolean isLeader) throws SpaceException;

  /**
   * Sets a member of a space as a manager.
   *
   * @param spaceId
   * @param userId
   * @param isLeader
   * @throws SpaceException
   */
  void setLeader(String spaceId, String userId, boolean isLeader) throws SpaceException;

  /**
   * Checks whether a user is a space's leader or not.
   *
   * @param space
   * @param userId
   * @return true if that the user is a leader; otherwise, false
   * @throws SpaceException
   */
  boolean isLeader(Space space, String userId) throws SpaceException;

  /**
   * Checks whether a user is a space's leader or not.
   *
   * @param spaceId
   * @param userId
   * @return true if that user is a leader; otherwise, false
   * @throws SpaceException
   */
  boolean isLeader(String spaceId, String userId) throws SpaceException;

  /**
   * Checks whether a user is the only leader of a space or not.
   *
   * @param space
   * @param userId
   * @return <tt>true</tt> if that user is the only leader of the space; otherwise, false
   * @throws SpaceException
   */
  boolean isOnlyLeader(Space space, String userId) throws SpaceException;

  /**
   * Checks whether a user is the only leader of a space or not.
   *
   * @param spaceId
   * @param userId
   * @return
   * @throws SpaceException
   */
  boolean isOnlyLeader(String spaceId, String userId) throws SpaceException;

  /**
   * Checks whether a user is a space's member or not.
   *
   * @param space
   * @param userId
   * @return true if that user is a member; otherwise, false
   * @throws SpaceException
   */
  boolean isMember(Space space, String userId) throws SpaceException;

  /**
   * Checks whether a user is a space's member or not.
   *
   * @param spaceId
   * @param userId
   * @return true if that user is a member; otherwise,false
   * @throws SpaceException
   */
  boolean isMember(String spaceId, String userId) throws SpaceException;

  /**
   * Checks if a user can access a space or not.
   * returns true If the user is root or the space's member.
   *
   * @param space
   * @param userId
   * @return
   * @throws SpaceException
   */
  boolean hasAccessPermission(Space space, String userId) throws SpaceException;

  /**
   * Checks if a user can access a space or not.
   * If the user is root or the space's member, return true
   * @param spaceId
   * @param userId
   * @return
   * @throws SpaceException
   */
  boolean hasAccessPermission(String spaceId, String userId) throws SpaceException;

  /**
   * Checks if a user can have the edit permission of a space or not.
   * If user is root or the space's manager, return true.
   *
   * @param space
   * @param userId
   * @return
   * @throws SpaceException
   */
  boolean hasEditPermission(Space space, String userId) throws SpaceException;

  /**
   * Checks if a user can have edit permission of a space.
   * If user is root or the space's manager, return true
   * @param spaceId
   * @param userId
   * @return
   * @throws SpaceException
   */
  boolean hasEditPermission(String spaceId, String userId) throws SpaceException;

  /**
   * Checks if a user is in the invited list of a space or not.
   *
   * @param space
   * @param userId
   * @return true if that user is in the invited list; otherwise, false
   * @throws SpaceException
   */
  boolean isInvited(Space space, String userId) throws SpaceException;

  /**
   * Checks if a user is in the invited list of a space or not.
   *
   * @param spaceId
   * @param userId
   * @return <tt>true</tt> if user is in the invited list; otherwise, false
   * @throws SpaceException
   */
  boolean isInvited(String spaceId, String userId) throws SpaceException;

  /**
   * Checks if a user is in the pending list of a space or not.
   *
   * @param space
   * @param userId
   * @return true if that user is in pending list; otherwise, false
   * @throws SpaceException
   */
  boolean isPending(Space space, String userId) throws SpaceException;

  /**
   * Checks if a user is in the pending list of a space.
   *
   * @param spaceId
   * @param userId
   * @return true if that user is in the pending list; otherwise, false
   * @throws SpaceException
   */
  boolean isPending(String spaceId, String userId) throws SpaceException;

  /**
   * Installs an application to a space.
   *
   * @param spaceId
   * @param appId
   * @throws SpaceException with code SpaceException.Code.ERROR_DATA_STORE
   */
  void installApplication(String spaceId, String appId) throws SpaceException;

  /**
   * Installs an application to a space.
   *
   * @param space
   * @param appId
   * @throws SpaceException with code SpaceException.Code.ERROR_DATA_STORE
   */
  void installApplication(Space space, String appId) throws SpaceException;

  /**
   * Activates an installed application in a space.
   *
   * @param space
   * @param appId
   * @throws SpaceException with possible code: SpaceException.Code.UNABLE_TO_ADD_APPLICATION,
   *                                            SpaceExeption.Code.ERROR_DATA_STORE
   */
  void activateApplication(Space space, String appId) throws SpaceException;

  /**
   * Activates an installed application in a space.
   *
   * @param spaceId
   * @param appId
   * @throws SpaceException with possible code: SpaceException.Code.UNABLE_TO_ADD_APPLICATION,
   *                                            SpaceExeption.Code.ERROR_DATA_STORE
   */
  void activateApplication(String spaceId, String appId) throws SpaceException;

  /**
   * Deactivates an installed application in a space.
   *
   * @param space
   * @param appId
   * @throws SpaceException
   */
  void deactivateApplication(Space space, String appId) throws SpaceException;

  /**
   * Deactivates an installed application in a space.
   *
   * @param spaceId
   * @param appId
   * @throws SpaceException
   */
  void deactivateApplication(String spaceId, String appId) throws SpaceException;

  /**
   * Removes an installed application from a space.
   *
   * @param space
   * @param appId
   * @throws SpaceException
   */
  void removeApplication(Space space, String appId, String appName) throws SpaceException;

  /**
   * Removes an installed application from a space.
   *
   * @param spaceId
   * @param appId
   * @throws SpaceException
   */
  void removeApplication(String spaceId, String appId, String appName) throws SpaceException;

  /**
   * Requests a user to join a space, adds that user to the pending list of the space.
   *
   * @param space
   * @param userId
   * @throws SpaceException
   */
  void requestJoin(Space space, String userId) throws SpaceException;

  /**
   * Requests a user to join a space, adds that user to the pending list of the space.
   *
   * @param spaceId
   * @param userId
   * @throws SpaceException
   */
  void requestJoin(String spaceId, String userId) throws SpaceException;

  /**
   * Revokes a join request after users request to join a group and is in the pending status.
   *
   * @param space
   * @param userId
   * @throws SpaceException
   */
  void revokeRequestJoin(Space space, String userId) throws SpaceException;

  /**
   * Revokes a request to join a space.
   *
   * @param spaceId
   * @param userId
   * @throws SpaceException
   */
  void revokeRequestJoin(String spaceId, String userId) throws SpaceException;

  /**
   * Invites a userId to become a member of a space.
   *
   * @param space
   * @param userId
   * @throws SpaceException
   */
  void inviteMember(Space space, String userId) throws SpaceException;

  /**
   * Invites a userId to a be member of a space.
   *
   * @param spaceId
   * @param userId
   * @throws SpaceException
   */
  void inviteMember(String spaceId, String userId) throws SpaceException;

  /**
   * Revokes an invitation - undo inviteMember.
   * Removes a user from the invited  member list of the space.
   *
   * @param space
   * @param userId
   * @throws SpaceException
   */
  void revokeInvitation(Space space, String userId) throws SpaceException;

  /**
   * Revokes invitation - undo inviteMember.
   * Removes a user from the invited  member list of the space.
   *
   * @param spaceId
   * @param userId
   * @throws SpaceException
   */
  void revokeInvitation(String spaceId, String userId) throws SpaceException;

  /**
   * Accepts Invitation - move a user from the invited list to the member list.
   *
   * @param space
   * @param userId
   * @throws SpaceException
   */
  void acceptInvitation(Space space, String userId) throws SpaceException;

  /**
   * Accepts an invitation - move a user from the invited list to the member list.
   *
   * @param spaceId
   * @param userId
   * @throws SpaceException
   */
  void acceptInvitation(String spaceId, String userId) throws SpaceException;

  /**
   * Denies an invitation - removes a user from the invited list.
   *
   * @param space
   * @param userId
   * @throws SpaceException
   */
  void denyInvitation(Space space, String userId) throws SpaceException;

  /**
   * Denies an invitation - removes user from the invited list.
   *
   * @param spaceId
   * @param userId
   * @throws SpaceException
   */
  void denyInvitation(String spaceId, String userId) throws SpaceException;

  /**
   * Validates a request, moves a user from the pending list to the member list.
   *
   * @param space
   * @param userId
   * @throws SpaceException
   */
  void validateRequest(Space space, String userId) throws SpaceException;

  /**
   * Validates request, moves a user from pending list to member list.
   *
   * @param spaceId
   * @param userId
   * @throws SpaceException
   */
  void validateRequest(String spaceId, String userId) throws SpaceException;

  /**
   * Declines a request and removes a user from  the pending list.
   *
   * @param space
   * @param userId
   * @throws SpaceException
   */
  void declineRequest(Space space, String userId) throws SpaceException;

  /**
   * Declines request and removes a user from the pending list.
   *
   * @param spaceId
   * @param userId
   * @throws SpaceException
   */
  void declineRequest(String spaceId, String userId) throws SpaceException;

  /**
   * Registers a space lifecycle listener.
   *
   * @param listener
   */
  void registerSpaceLifeCycleListener(SpaceLifeCycleListener listener);

  /**
   * Unregisters a space lifecycle listener.
   *
   * @param listener
   */
  void unregisterSpaceLifeCycleListener(SpaceLifeCycleListener listener);

  /**
   * Sets the portlet preferences got from the plug-in configuration.
   *
   * @param portletPrefsRequiredPlugin
   */
  void setPortletsPrefsRequired(PortletPreferenceRequiredPlugin portletPrefsRequiredPlugin);
  /**
   * Gets the portlet preferences required to use in creating the portlet application.
   *
   * @return
   */
  String[] getPortletsPrefsRequired();

}
