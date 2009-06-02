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
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * August 29, 2008          
 */
public interface SpaceService {
  
  /**
   * Gets all spaces
   * @return get All space of Portal
   * @throws Exception
   */
  List<Space> getAllSpaces() throws SpaceException;
  
  /**
   * Gets all spaces
   * @return get All space of Portal base on the User
   *         if space is hidden and User isn't member of that space, space will be removed. 
   * @throws Exception
   */
  List<Space> getAllSpaces(String userId) throws SpaceException;
  
  /**
   * Gets all ordered spaces
   * @return get all ordered spaces of Portal base on the User Id
   *         if space is hidden and User isn't member of that space, space will be removed. 
   * @throws SpaceException
   */
  List<Space> getAllOrderedSpaces(String userId) throws SpaceException;
  
  /**
   * Gets a space by its id
   * @param id Id of space
   * @return space with id specified
   * @throws Exception
   */
  Space getSpaceById(String id) throws SpaceException;
  
  
  /**
   * Get all ordered spaces: pending -> leader -> member
   * @param userId Id of user
   * @return all ordered spaces of a user
   *          that the user is member or pending member
   * @throws SpaceException
   */
  List<Space> getUserOrderedSpaces(String userId) throws SpaceException;
  
  
  /**
   * Gets a space by its url
   * @param url Url of space
   * @return space with url specified
   * @throws Exception
   */
  Space getSpaceByUrl(String url) throws SpaceException;
 
  /**
   * Creates new space or saves when edit it
   * @param space space is saved
   * @param isNew is true if create new space, false if edit existed space
   * @throws Exception
   */
  void saveSpace(Space space, boolean isNew) throws SpaceException;


  Space createSpace(Space space, String creator) throws SpaceException;

  void leave(Space space, String userId) throws SpaceException;
  void leave(String spaceId, String userId) throws SpaceException;

  void addMember(Space space, String userId) throws SpaceException;

  void invite(Space space, String userId) throws SpaceException;

  void removeMember(Space space, String userId) throws SpaceException;

  void revokeInvitation(String spaceId, String userId) throws SpaceException;
  void revokeInvitation(Space space, String userId) throws SpaceException;

  void acceptInvitation(Space space, String userId) throws SpaceException;
  void acceptInvitation(String spaceId, String userId) throws SpaceException;

  void denyInvitation(String spaceId, String userId) throws SpaceException;
  void denyInvitation(Space space, String userId) throws SpaceException;
  
  void validateRequest(Space space, String userId) throws SpaceException;
  void validateRequest(String spaceId, String userId) throws SpaceException;
  
  void declineRequest(Space space, String userId) throws SpaceException;
  void declineRequest(String spaceId, String userId) throws SpaceException;

  void requestJoin(String spaceId, String userId) throws SpaceException;
  void requestJoin(Space space, String userId) throws SpaceException;

  void setLeader(Space space, String userId, boolean status) throws SpaceException;

  void installApplication(String spaceId, String appId) throws SpaceException;
  void installApplication(Space space, String appId) throws SpaceException;

  void deactiveApplication(Space space, String appId) throws SpaceException;

  void activateApplication(Space space, String appId) throws SpaceException;

  void removeApplication(Space space, String appId) throws SpaceException;
  void removeApplication(String spaceId, String appId) throws SpaceException;

  void activateApplication(String spaceId, String appId) throws SpaceException;

  List<String> getMembers(Space space) throws SpaceException;
  boolean isLeader(Space space, String userId) throws SpaceException;
  boolean isMember(Space space, String userId) throws SpaceException;
  boolean isInvited(Space space, String userId);
  boolean isPending(Space space, String userId);
}