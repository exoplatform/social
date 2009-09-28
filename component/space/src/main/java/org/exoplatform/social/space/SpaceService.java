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
package org.exoplatform.social.space;

import java.util.List;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
/**
 * <pre>
 * SpaceService provides methods for working with Space 
 * 
 * Created by The eXo Platform SARL
 * 
 * Author : dang.tung
 *          tungcnw@gmail.com
 *          
 * August 29, 2008
 * </pre>     
 */
public interface SpaceService {
  
  /**
   * Gets all spaces of the portal
   * @return all spaces of portal
   * @throws SpaceException with code SpaceException.Code.ERROR_DATASTORE
   */
  List<Space> getAllSpaces() throws SpaceException;
  
  /**
   * Get a space by its id
   * @param spaceId Id of that space
   * @return space with id specified
   * @throws SpaceException with code SpaceException.Code.ERROR_DATASTORE
   */
  Space getSpaceById(String spaceId) throws SpaceException;
  
  /**
   * Get a space by its url
   * @param spaceUrl url of space
   * @return Space space with string url specified
   * @throws SpaceException
   */
  Space getSpaceByUrl(String spaceUrl) throws SpaceException;
  
  /**
   * Get spaces of a user in which user is a member
   * 
   * @param userId Id of user
   * @return all spaces of a user in which the user is a member
   * @throws SpaceException with code SpaceException.Code.ERROR_DATASTORE
   */
  List<Space> getSpaces(String userId) throws SpaceException;
  
  /**
   * Get spaces of a user which user has access permission
   * @param userId
   * @return
   * @throws SpaceException
   */
  List<Space> getAccessibleSpaces(String userId) throws SpaceException;
  
  /**
   * Get spaces of a user which user has edit permission
   * @param userId
   * @return
   * @throws SpaceException
   */
  List<Space> getEditableSpaces(String userId) throws SpaceException;
  
  /**
   * Get user's invited spaces and that user can accept or deny the request
   * @param userId
   * @return spaces list of all user's invited spaces
   * @throws SpaceException
   */
  List<Space> getInvitedSpaces(String userId) throws SpaceException;
  
  
  /**
   * Get user's public spaces and that user can request to join
   * @param userId Id of user
   * @return spaces list in which user can request to join
   * @throws SpaceException
   */
  List<Space> getPublicSpaces(String userId) throws SpaceException;
  
  /**
   * Get user's pending spaces and that user can revoke that request
   * @param userId
   * @return spaces list in which user can revode that request
   * @throws SpaceException
   */
  List<Space> getPendingSpaces(String userId) throws SpaceException;
  
  /**
   * Create new space by creating new group
   * This new group will be under /Spaces node
   * This is shorthand for calling createSpace(space, creator, null)
   * @param space
   * @param creator
   * @return
   * @throws SpaceException with possible code SpaceException.Code.SPACE_ALREADY_EXIST, UNABLE_TO_ADD_CREATOR
   */
  Space createSpace(Space space, String creator) throws SpaceException;
  
  /**
   * Create new space from an existing group
   * 
   * @param space
   * @param creator
   * @param groupId if groupId == null : create new space by creating new group
   * @return space
   * @throws SpaceException with possible code SpaceException.Code.SPACE_ALREADY_EXIST; UNABLE_TO_ADD_CREATOR
   */
  Space createSpace(Space space, String creator, String groupId) throws SpaceException;
  
  /**
   * Save new space or Update space
   * 
   * @param space space is saved
   * @param isNew true if create new space; otherwise, update existed space
   * @throws SpaceException with code: SpaceException.Code.ERROR_DATASTORE
   */
  void saveSpace(Space space, boolean isNew) throws SpaceException;
  
  /**
   * Delete space. When deleting a space, all it's page navigations and it's group will be deleted.
   * @param space
   * @throws SpaceException
   */
  void deleteSpace(Space space) throws SpaceException;
  
  /**
   * Delete space
   * @param spaceId
   * @throws SpaceException
   */
  void deleteSpace(String spaceId) throws SpaceException;
  
  /**
   * Initialize default application to space.
   * 
   * Set HomeSpacePortlet to be the root page of that space node
   * 
   * @param space Space
   * @throws SpaceException with code SpaceException.Code.UNABLE_INIT_APP
   */
  void initApp(Space space) throws SpaceException;

  /**
   * Add a user to a space, the user will get role: member
   * @param space
   * @param userId
   * @throws SpaceException
   */
  void addMember(Space space, String userId) throws SpaceException;
  
  /**
   * Add a user to a space, the user will get role: member
   * @param spaceId
   * @param userId
   * @throws SpaceException
   */
  void addMember(String spaceId, String userId) throws SpaceException;
  
  /**
   * Remove member from a space
   * If the member is the only leader from that space, member remove is not allowed and throws SpaceException
   * with Code = USER_ONLY_LEADER
   * @param space
   * @param userId
   * @throws SpaceException
   */
  void removeMember(Space space, String userId) throws SpaceException;
  
  /**
   * Remove member from a space
   * @param spaceId
   * @param userId
   * @throws SpaceException
   */
  void removeMember(String spaceId, String userId) throws SpaceException;
  
  /**
   * Add a userId to the pending list of a space
   * @param space
   * @param userId
   * @return space with new pending list
   * @throws SpaceException
   */
  
  List<String> getMembers(Space space) throws SpaceException;

  /**
   * Get all members from a space
   * @param spaceId
   * @return members list
   * @throws SpaceException
   */
  List<String> getMembers(String spaceId) throws SpaceException;
  
  /**
   * Set leader to a member of a space.
   * 
   * If isLeader == true, that user will be assigned "manager" membership and removed "member" membership
   * Otherwise, that user will be assigned "member" membership and removed "manager" membership
   * However, if that user is the only leader, that user is not allowed to be removed from manager membership.
   * @param space
   * @param userId
   * @param isLeader
   * @throws SpaceException
   */
  void setLeader(Space space, String userId, boolean isLeader) throws SpaceException;
  
  /**
   * Set leader to a member of a space.
   * <p>
   * If isLeader == true, that user will be assigned "manager" membership and removed "member" membership
   * Otherwise, that user will be assigned "member" membership and removed "manager" membership
   * </p>
   * @param spaceId
   * @param userId
   * @param isLeader
   * @throws SpaceException
   */
  void setLeader(String spaceId, String userId, boolean isLeader) throws SpaceException; 
  
  /**
   * Checking whether a user is a space's leader.
   * @param space
   * @param userId
   * @return true if that user if a leader otherwise false
   * @throws SpaceException
   */
  boolean isLeader(Space space, String userId) throws SpaceException;
  
  /**
   * Checking whether a user is a space's leader.
   * @param spaceId
   * @param userId
   * @return
   * @throws SpaceException
   */
  boolean isLeader(String spaceId, String userId) throws SpaceException;
  
  /**
   * Checking whether a user is the only leader of a space
   * @param space
   * @param userId
   * @return <tt>true</tt> if that user is the only leader of the space; otherwise, false
   * @throws SpaceException
   */
  boolean isOnlyLeader(Space space, String userId) throws SpaceException;
  
  /**
   * Checking whether a user is the only leader of a space
   * @param spaceId
   * @param userId
   * @return
   * @throws SpaceException
   */
  boolean isOnlyLeader(String spaceId, String userId) throws SpaceException;

  /**
   * Checking whether a user is a space's member.
   * @param space
   * @param userId
   * @return true if that user is a member; otherwise, false
   * @throws SpaceException
   */
  boolean isMember(Space space, String userId) throws SpaceException;
  
  /**
   * Checking whether a user is a space's member.
   * @param spaceId
   * @param userId
   * @return true if that user is a member; otherwise,false
   * @throws SpaceException
   */
  boolean isMember(String spaceId, String userId) throws SpaceException;
  
  /**
   * If user is root or user is space's member, return true
   * @param space
   * @param userId
   * @return
   * @throws SpaceException
   */
  boolean hasAccessPermission(Space space, String userId) throws SpaceException;
  
  /**
   * If user is root or user is space's member, return true
   * @param spaceId
   * @param userId
   * @return
   * @throws SpaceException
   */
  boolean hasAccessPermission(String spaceId, String userId) throws SpaceException;
  
  /**
   * If user is root or user is space's manager, return true
   * @param space
   * @param userId
   * @return
   * @throws SpaceException
   */
  boolean hasEditPermission(Space space, String userId) throws SpaceException;
  
  /**
   * If user is root or user is space's manager, return true
   * @param spaceId
   * @param userId
   * @return
   * @throws SpaceException
   */
  boolean hasEditPermission(String spaceId, String userId) throws SpaceException;
  
  /**
   * Checking whether a user is in the invited list of a space
   * @param space
   * @param userId
   * @return true if that user is in invited list; otherwise, false
   * @throws SpaceException
   */
  boolean isInvited(Space space, String userId) throws SpaceException;
  
  /**
   * Checking whether a user is in the invited list of a space
   * @param spaceId
   * @param userId
   * @return <tt>true</tt> if user is in invited list; otherwise, false
   * @throws SpaceException
   */
  boolean isInvited(String spaceId, String userId) throws SpaceException;
  
  /**
   * Checking whether a user is in the pending list of a space 
   * @param space
   * @param userId
   * @return true if that user is in pending list; otherwise, false
   * @throws SpaceException
   */
  boolean isPending(Space space, String userId) throws SpaceException;
  
  /**
   * Checking whether a user is in the pending list of a space 
   * @param spaceId
   * @param userId
   * @return true if that user is in pending list; otherwise, false
   * @throws SpaceException
   */
  boolean isPending(String spaceId, String userId) throws SpaceException;
  
  /**
   * Install an application to a space
   * @param spaceId
   * @param appId
   * @throws SpaceException with code SpaceException.Code.ERROR_DATA_STORE
   */
  void installApplication(String spaceId, String appId) throws SpaceException;
  
  /**
   * Install an application to a space
   * @param space
   * @param appId
   * @throws SpaceException with code SpaceException.Code.ERROR_DATA_STORE
   */
  void installApplication(Space space, String appId) throws SpaceException;
  
  /**
   * Activate an installed application in a space
   * @param space
   * @param appId
   * @throws SpaceException with possible code: SpaceException.Code.UNABLE_TO_ADD_APPLICATION, 
   *                                            SpaceExeption.Code.ERROR_DATA_STORE  
   */
  void activateApplication(Space space, String appId) throws SpaceException;
  
  /**
   * Activate an installed application in a space
   * @param spaceId
   * @param appId
   * @throws SpaceException with possible code: SpaceException.Code.UNABLE_TO_ADD_APPLICATION, 
   *                                            SpaceExeption.Code.ERROR_DATA_STORE  
   */
  void activateApplication(String spaceId, String appId) throws SpaceException;
  
  /**
   * Deactivate an installed application in a space
   * @param space
   * @param appId
   * @throws SpaceException
   */
  void deactivateApplication(Space space, String appId) throws SpaceException;
  
  /**
   * Deactivate an installed application in a space
   * @param spaceId
   * @param appId
   * @throws SpaceException
   */
  void deactivateApplication(String spaceId, String appId) throws SpaceException;
  
  /**
   * Remove an installed application from a space
   * @param space
   * @param appId
   * @throws SpaceException
   */
  void removeApplication(Space space, String appId) throws SpaceException;
  
  /**
   * Remove and installed application from a space
   * @param spaceId
   * @param appId
   * @throws SpaceException
   */
  void removeApplication(String spaceId, String appId) throws SpaceException;
  
  /**
   * Request to join a space, add that user to pending list
   * @param space
   * @param userId
   * @throws SpaceException
   */
  void requestJoin(Space space, String userId) throws SpaceException;
  
  /**
   * Request to join a space, add that user to pending list
   * @param spaceId
   * @param userId
   * @throws SpaceException
   */
  void requestJoin(String spaceId, String userId) throws SpaceException;
  
  /**
   * Revoke request join request after user request to join a group and is in pending status
   * @param space
   * @param userId
   * @throws SpaceException
   */
  void revokeRequestJoin(Space space, String userId) throws SpaceException;
  
  /**
   * Revoke request join request
   * @param spaceId
   * @param userId
   * @throws SpaceException
   */
  void revokeRequestJoin(String spaceId, String userId) throws SpaceException;
  
  /**
   * Invite a userId to a be member of a space
   * @param space
   * @param userId
   * @throws SpaceException
   */
  void inviteMember(Space space, String userId) throws SpaceException;
  
  /**
   * Invite a userId to a be member of a space
   * @param spaceId
   * @param userId
   * @throws SpaceException
   */
  void inviteMember(String spaceId, String userId) throws SpaceException;
  
  /**
   * Revoke invitation - undo inviteMember
   * Remove user from space's invited  member list
   * @param space
   * @param userId
   * @throws SpaceException
   */
  void revokeInvitation(Space space, String userId) throws SpaceException;
  
  /**
   * Revoke invitation - undo inviteMember
   * Remove user from space's invited  member list
   * @param spaceId
   * @param userId
   * @throws SpaceException
   */
  void revokeInvitation(String spaceId, String userId) throws SpaceException;
  
  /**
   * Accept Invitation - move user from invited list to member list
   * @param space
   * @param userId
   * @throws SpaceException
   */
  void acceptInvitation(Space space, String userId) throws SpaceException;
  
  /**
   * Accept Invitation - move user from invited list to member list
   * @param spaceId
   * @param userId
   * @throws SpaceException
   */
  void acceptInvitation(String spaceId, String userId) throws SpaceException;
  
  /**
   * Deny Invitation - remove user from invited list
   * @param space
   * @param userId
   * @throws SpaceException
   */
  void denyInvitation(Space space, String userId) throws SpaceException;
  
  /**
   * Deny Invitation - remove user from invited list
   * @param spaceId
   * @param userId
   * @throws SpaceException
   */
  void denyInvitation(String spaceId, String userId) throws SpaceException;
  
  /**
   * Validate request, move user from pending list to member list
   * @param space
   * @param userId
   * @throws SpaceException
   */
  void validateRequest(Space space, String userId) throws SpaceException;
  
  /**
   * Validate request, move user from pending list to member list
   * @param spaceId
   * @param userId
   * @throws SpaceException
   */
  void validateRequest(String spaceId, String userId) throws SpaceException;
  
  /**
   * Decline request, remove user from pending list
   * @param space
   * @param userId
   * @throws SpaceException
   */
  void declineRequest(Space space, String userId) throws SpaceException;
  
  /**
   * Decline request, remove user from pending list
   * @param spaceId
   * @param userId
   * @throws SpaceException
   */
  void declineRequest(String spaceId, String userId) throws SpaceException;
}