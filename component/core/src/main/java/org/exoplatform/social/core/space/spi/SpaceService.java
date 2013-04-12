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

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.application.PortletPreferenceRequiredPlugin;
import org.exoplatform.social.core.space.SpaceApplicationConfigPlugin;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.SpaceListAccess;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.model.Space;

/**
 * SpaceService provides methods for working with Space.
 * 
 * @since Aug 29, 2008
 * 
 */
public interface SpaceService {

  /**
   * Will be removed by 4.0.x
   */
  @Deprecated
  final String SPACES_APP_ID = "exosocial:spaces";

  /**
   * Gets a space by its space display name.
   *
   * @param spaceDisplayName the space display name
   * @return the space with space display name that matches the string input.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  Space getSpaceByDisplayName(String spaceDisplayName);

  /**
   * Gets a space by its pretty name.
   *
   * @param spacePrettyName the space pretty name
   * @return the space with space pretty name that matches the string input.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  Space getSpaceByPrettyName(String spacePrettyName);

  /**
   * Gets a space by its group id.
   *
   * @param groupId the group id
   * @return the space that has group id that matches the string input.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  Space getSpaceByGroupId(String groupId);

  /**
   * Gets a space by its id.
   *
   * @param spaceId id of that space
   * @return the space with id specified
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  Space getSpaceById(String spaceId);

  /**
   * Gets a space by its url.
   *
   * @param spaceUrl url of a space
   * @return the space with the space url that matched the string input
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  Space getSpaceByUrl(String spaceUrl);

  /**
   * Gets a space list access which contains all the spaces.
   *
   * @return the space list access for all spaces
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getAllSpacesWithListAccess();


  /**
   * Gets a space list access which contains all the spaces matching the space filter.
   *
   * @param spaceFilter the space filter
   * @return the space list access for all spaces matching the space filter
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getAllSpacesByFilter(SpaceFilter spaceFilter);

  /**
   * Gets a spaces list access that contains all the spaces in which a user has the "member" role.
   *
   * @param userId the remote user id
   * @return the space list access
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getMemberSpaces(String userId);

  /**
   * Gets a space list access that contains all the spaces that a user has "member" role and matches the provided space
   * filter.
   *
   * @param userId      the remote user id
   * @param spaceFilter the space filter
   * @return the space list access
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getMemberSpacesByFilter(String userId, SpaceFilter spaceFilter);

  /**
   * Gets a spaces list access which contains all the spaces that a user has the access permission.
   *
   * @param userId the remote user id.
   * @return the space list access
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getAccessibleSpacesWithListAccess(String userId);

  /**
   * Gets a space list access which contains all the spaces that a user has the access permission and matches the
   * provided space filter.
   *
   * @param userId      the remote user id
   * @param spaceFilter the provided space filter
   * @return the space list access
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getAccessibleSpacesByFilter(String userId, SpaceFilter spaceFilter);

  /**
   * Gets a spaces list access which contains all the spaces that a user has the setting permission.
   *
   * @param userId the remote user id
   * @return the space list access
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getSettingableSpaces(String userId);

  /**
   * Gets a space list access which contains all the spaces that a user has the setting permission and matches the
   * provided space filter.
   *
   * @param userId      the remote user id
   * @param spaceFilter the provided space filter
   * @return the space list access
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getSettingabledSpacesByFilter(String userId, SpaceFilter spaceFilter);

  /**
   * Gets a space list access which contains all the spaces that a user is invited to join.
   *
   * @param userId the remote user id
   * @return the space list access
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getInvitedSpacesWithListAccess(String userId);

  /**
   * Gets a space list access which contains all the spaces that a user is invited to join and matches the provided
   * space filter.
   *
   * @param userId      the remote user id
   * @param spaceFilter the provided space filter
   * @return the space list access
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getInvitedSpacesByFilter(String userId, SpaceFilter spaceFilter);

  /**
   * Gets a space list access which contains all the spaces that a user can request to join.
   *
   * @param userId the remote user id
   * @return the space list access
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getPublicSpacesWithListAccess(String userId);

  /**
   * Gets a space list access which contains all the spaces that a user can request to join and matches the provided
   * space filter.
   *
   * @param userId      the remote user id
   * @param spaceFilter the provided space filter
   * @return the space list access
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  ListAccess<Space> getPublicSpacesByFilter(String userId, SpaceFilter spaceFilter);


  /**
   * Gets a space list access which contains all the spaces that a user sent join-request to a space.
   *
   * @param userId the remote user id
   * @return the space list access
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getPendingSpacesWithListAccess(String userId);

  /**
   * Gets a space list access which contains all the spaces that a user sent join-request to a space and matches the
   * provided space filter.
   *
   * @param userId      the remote user id
   * @param spaceFilter the provided space filter
   * @return the space list access
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  ListAccess<Space> getPendingSpacesByFilter(String userId, SpaceFilter spaceFilter);

  /**
   * Creates a new space: create a group, its group navigation with pages for installing space applications.
   *
   * @param space         the space to be created
   * @param creatorUserId the remote user id
   * @return the created space
   * @LevelAPI Platform
   */
  Space createSpace(Space space, String creatorUserId);

  /**
   * Updates a space's information
   *
   * @param existingSpace the existing space to be updated
   * @return the updated space
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  Space updateSpace(Space existingSpace);

  /**
   * Updates a space's avatar
   *
   * @param existingSpace the existing space to be updated
   * @return the updated space
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  Space updateSpaceAvatar(Space existingSpace);

  /**
   * Deletes a space. When deleting a space, all of its page navigations and its group will be deleted.
   *
   * @param space the space to be deleted
   * @LevelAPI Platform
   */
  void deleteSpace(Space space);

  /**
   * Adds a user to pending list to request to join a space.
   *
   * @param space  the exising space
   * @param userId the remote user id
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void addPendingUser(Space space, String userId);

  /**
   * Removes a user from pending list to request to join a space.
   *
   * @param space  the existing space
   * @param userId the remote user id
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void removePendingUser(Space space, String userId);

  /**
   * Checks if a user is in the pending list to request to join a space.
   *
   * @param space  the existing space
   * @param userId the remote user id
   * @return TRUE is pending Otherwise FALSE
   * @LevelAPI Platform
   * @since 1.2.0-GA
   * 
   */
  boolean isPendingUser(Space space, String userId);

  /**
   * Adds a user to invited list to join a space.
   *
   * @param space  the existing space
   * @param userId the remote user id
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void addInvitedUser(Space space, String userId);

  /**
   * Removes a user from the invited list to join a space.
   *
   * @param space  the existing space
   * @param userId the remote user id
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void removeInvitedUser(Space space, String userId);

  /**
   * Checks if a user is in the invited list to join a space.
   *
   * @param space  the existing space
   * @param userId the remote user id
   * @return TRUE for invited user; Otherwise FALSE
   * @LevelAPI Platform
   * @since 1.2.0-GA
   * 
   */
  boolean isInvitedUser(Space space, String userId);

  /**
   * Adds a user to a space, the user will get the "member" role in a space.
   *
   * @param space  the existing space
   * @param userId the remote user id
   * @LevelAPI Platform
   */
  void addMember(Space space, String userId);

  /**
   * Removes a member from a space.
   *
   * @param space  the existing space
   * @param userId the remote user id
   * @LevelAPI Platform
   */
  void removeMember(Space space, String userId);

  /**
   * Checks whether a user is a space's member or not.
   *
   * @param space  the existing space
   * @param userId the remote user id
   * @return TRUE if that user is a member; otherwise, FALSE
   * @LevelAPI Platform
   */
  boolean isMember(Space space, String userId);

  /**
   * Adds a user to have the "manager" role in a space.
   *
   * @param space     the existing space
   * @param userId    the remote user id
   * @param isManager true or false to indicate a user will get "manager" role or not. If false, that user will get
   *                  "member" role.
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void setManager(Space space, String userId, boolean isManager);

  /**
   * Checks if a user has "manager" role in a space.
   *
   * @param space  the existing space
   * @param userId the remote user id
   * @return true or false
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  boolean isManager(Space space, String userId);

  /**
   * Checks if a user is the only one who has "manager" role in a space.
   *
   * @param space  the existing space
   * @param userId the remote user id
   * @return true if that user id is the only one who has "manager" role in a space. Otherwise, return false.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  boolean isOnlyManager(Space space, String userId);

  /**
   * Checks if a user can access a space or not. If the user is root or the space's member, return true
   *
   * @param space  the existing space
   * @param userId the remote user id
   * @return true if access permission is allowed, otherwise, false.
   * @LevelAPI Platform
   * 
   */
  boolean hasAccessPermission(Space space, String userId);

  /**
   * Checks if a user can have setting permission to a space or not.
   * <p/>
   * If the user is root or the space's member, return true
   *
   * @param space  the existing space
   * @param userId the remote user id
   * @return true if setting permission is allowed, otherwise, false.
   * @LevelAPI Platform
   * @since  1.2.0-GA
   */
  boolean hasSettingPermission(Space space, String userId);

  /**
   * Registers a space listener plugin to listen to space lifecyle events: create, update, install application, etc,.
   *
   * @param spaceListenerPlugin a space listener plugin
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void registerSpaceListenerPlugin(SpaceListenerPlugin spaceListenerPlugin);

  /**
   * Unregisters an existing space listener plugin.
   *
   * @param spaceListenerPlugin
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void unregisterSpaceListenerPlugin(SpaceListenerPlugin spaceListenerPlugin);

  /**
   * Sets space application config plugin for configuring the home and space applications.
   * <p/>
   * By configuring this, space service will know how to create a new page node with title, url, and portlet to use.
   *
   * @param spaceApplicationConfigPlugin space application config plugin
   * @LevelAPI Platform
   * @since 1.2.0-GA
   */
  void setSpaceApplicationConfigPlugin(SpaceApplicationConfigPlugin spaceApplicationConfigPlugin);

  /**
   * Gets the configured space application config plugin.
   *
   * @return the configured space application config plugin
   * @LevelAPI Platform
   */
  SpaceApplicationConfigPlugin getSpaceApplicationConfigPlugin();

  /**
   * Gets all spaces in Social.
   *
   * @return list of spaces in Social
   * @throws SpaceException with code SpaceException.Code.ERROR_DATASTORE
   * @LevelAPI Provisional
   * @deprecated Use {@link #getAllSpacesWithListAccess()} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Space> getAllSpaces() throws SpaceException;

  /**
   * Gets a space by its space name.
   *
   * @param spaceName space name
   * @return the stored space
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link SpaceService#getSpaceByPrettyName(String)} instead.
   *             Will be removed at 4.0.x
   */
  public Space getSpaceByName(String spaceName) throws SpaceException;

  /**
   * Gets all spaces has the name starting with the input character.
   *
   * @return all spaces which have first character of name matched the input string.
   * @throws SpaceException with code SpaceException.Code.ERROR_DATASTORE
   * @LevelAPI Provisional
   * @deprecated Use {@link #getAllSpacesByFilter(org.exoplatform.social.core.space.SpaceFilter)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Space> getSpacesByFirstCharacterOfName(String firstCharacterOfName) throws SpaceException;

  /**
   * Gets all spaces which has name or description that match input condition.
   *
   * @param condition the input condition
   * @return a list of spaces
   * @throws Exception
   * @LevelAPI Provisional
   * @deprecated Use {@link #getAllSpacesByFilter(org.exoplatform.social.core.space.SpaceFilter)} instead.
   *             Will be removed by 4.0.x
   */
  List<Space> getSpacesBySearchCondition(String condition) throws Exception;

  /**
   * Gets spaces of a user in which that user is a member.
   *
   * @param userId Id of user
   * @return all spaces of a user in which the user is a member
   * @throws SpaceException with code SpaceException.Code.ERROR_DATASTORE
   * @LevelAPI Provisional
   * @deprecated Use {@link #getMemberSpaces(String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Space> getSpaces(String userId) throws SpaceException;

  /**
   * Gets spaces of a user which that user has the access permission.
   *
   * @param userId
   * @return list of spaces
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #getAccessibleSpacesWithListAccess(String)} instead.
   *             Will be removed by 4.0.x
   */
  List<Space> getAccessibleSpaces(String userId) throws SpaceException;

  /**
   * Gets spaces of a user which that user can see the visible spaces.
   *
   * @param userId
   * @param spaceFilter
   * @return list of spaces
   * @throws SpaceException
   * @LevelAPI Platform
   * @since 1.2.5-GA
   */
  public List<Space> getVisibleSpaces(String userId, SpaceFilter spaceFilter) throws SpaceException;
  
  /**
   * Gets spaces of a user which that user can see the visible spaces.
   * @param userId
   * @param spaceFilter
   * @return list of spaces
   * @LevelAPI Platform
   * @since 1.2.5-GA
   */
  public SpaceListAccess getVisibleSpacesWithListAccess(String userId, SpaceFilter spaceFilter);
  
  /**
   * Provides Unified Search feature to get these spaces of a user which that user can see the visible spaces.
   * @param userId
   * @param spaceFilter
   * @return list of spaces
   * @LevelAPI Platform
   * @since 4.0.0-GA
   */
  public SpaceListAccess getUnifiedSearchSpacesWithListAccess(String userId, SpaceFilter spaceFilter);
  
  /**
   * Gets spaces of a user which that user has the edit permission.
   *
   * @param userId
   * @return list of space
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #getSettingableSpaces(String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Space> getEditableSpaces(String userId) throws SpaceException;

  /**
   * Gets a user's invited spaces and that user can accept or deny the request.
   *
   * @param userId
   * @return spaces list of all user's invited spaces
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #getInvitedSpacesWithListAccess(String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Space> getInvitedSpaces(String userId) throws SpaceException;


  /**
   * Gets a user's public spaces and that user can request to join.
   *
   * @param userId Id of user
   * @return spaces list in which the user can request to join
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #getPublicSpacesWithListAccess(String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Space> getPublicSpaces(String userId) throws SpaceException;

  /**
   * Gets a user's pending spaces and that the user can revoke that request.
   *
   * @param userId
   * @return spaces list in which the user can revoke that request
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #getPendingSpacesWithListAccess(String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<Space> getPendingSpaces(String userId) throws SpaceException;

  /**
   * Creates a new space and invites all users from invitedGroupId to join this newly created space.
   *
   * @param space
   * @param creator
   * @param invitedGroupId
   * @return space
   * @throws SpaceException with possible code SpaceException.Code.SPACE_ALREADY_EXIST; UNABLE_TO_ADD_CREATOR
   * @LevelAPI Platform
   */
  Space createSpace(Space space, String creator, String invitedGroupId) throws SpaceException;

  /**
   * Saves a new space or updates a space.
   *
   * @param space space is saved
   * @param isNew true if creating a new space; otherwise, update an existing space.
   * @throws SpaceException with code: SpaceException.Code.ERROR_DATASTORE
   * @LevelAPI Provisional
   * @deprecated Use {@link #updateSpace(org.exoplatform.social.core.space.model.Space)} instead.
   *             Will be removed by 4.0.x
   */
  void saveSpace(Space space, boolean isNew) throws SpaceException;

  /**
   * Renames a space.
   * 
   * @param space the existing space
   * @param newDisplayName  new display name
   * @throws SpaceException
   * @LevelAPI Platform
   * @since 1.2.8
   */
  void renameSpace(Space space, String newDisplayName) throws SpaceException;
  
  /**
   * Renames a space by identity who has right as super admin role
   * 
   * @param remoteId who made rename space
   * @param space the existing space
   * @param newDisplayName  new display name
   * @throws SpaceException
   * @LevelAPI Platform
   * @since 4.0.0
   */
  void renameSpace(String remoteId, Space space, String newDisplayName) throws SpaceException;
  
  /**
   * Deletes a space by its id.
   *
   * @param spaceId
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #deleteSpace(org.exoplatform.social.core.space.model.Space)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void deleteSpace(String spaceId) throws SpaceException;

  /**
   * Does nothing, just for compatible.
   *
   * @param space the space
   * @throws SpaceException with code SpaceException.Code.UNABLE_INIT_APP
   * @LevelAPI Provisional
   * @deprecated Will be removed by 4.0.x
   */
  @Deprecated
  void initApp(Space space) throws SpaceException;

  /**
   * Does nothing, just for compatible.
   *
   * @param space Space
   * @throws SpaceException with code SpaceException.Code.UNABLE_INIT_APP
   * @LevelAPI Provisional
   * @deprecated Will be removed by 4.0.x
   */
  @Deprecated
  void initApps(Space space) throws SpaceException;

  /**
   * Does nothing, just for compatible.
   *
   * @param space the space
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Will be removed by 4.0.x
   */
  @Deprecated
  void deInitApps(Space space) throws SpaceException;

  /**
   * Adds a user to a space, the user will get the "member" role in a space.
   *
   * @param spaceId
   * @param userId
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #addMember(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void addMember(String spaceId, String userId) throws SpaceException;

  /**
   * Removes a member from a space.
   *
   * @param spaceId
   * @param userId
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #removeMember(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void removeMember(String spaceId, String userId) throws SpaceException;

  /**
   * Gets a list of the space members from a space.
   *
   * @param space
   * @return list of space members
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link Space#getMembers()} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<String> getMembers(Space space) throws SpaceException;

  /**
   * Gets a list of the space members from a space.
   *
   * @param spaceId
   * @return a list of the space members
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link Space#getMembers()} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  List<String> getMembers(String spaceId) throws SpaceException;

  /**
   * Sets a member of a space as a manager.
   *
   * @param space
   * @param userId
   * @param isLeader
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #setManager(org.exoplatform.social.core.space.model.Space, String, boolean)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void setLeader(Space space, String userId, boolean isLeader) throws SpaceException;

  /**
   * Sets a member of a space as a manager.
   *
   * @param spaceId
   * @param userId
   * @param isLeader
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #setManager(org.exoplatform.social.core.space.model.Space, String, boolean)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void setLeader(String spaceId, String userId, boolean isLeader) throws SpaceException;

  /**
   * Checks whether a user is a space's leader or not.
   *
   * @param space
   * @param userId
   * @return true if that the user is a leader; otherwise, false
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #isManager(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  boolean isLeader(Space space, String userId) throws SpaceException;

  /**
   * Checks whether a user is a space's leader or not.
   *
   * @param spaceId
   * @param userId
   * @return true if that user is a leader; otherwise, false
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #isManager(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  boolean isLeader(String spaceId, String userId) throws SpaceException;

  /**
   * Checks whether a user is the only leader of a space or not.
   *
   * @param space
   * @param userId
   * @return <tt>true</tt> if that user is the only leader of the space; otherwise, false
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #isOnlyManager(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  boolean isOnlyLeader(Space space, String userId) throws SpaceException;

  /**
   * Checks whether a user is the only leader of a space or not.
   *
   * @param spaceId Id of space
   * @param userId Id of user (remoteId)
   * @return True if user is the last leader of space.
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #isOnlyManager(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  boolean isOnlyLeader(String spaceId, String userId) throws SpaceException;

  /**
   * Checks whether a user is a space's member or not.
   *
   * @param spaceId Id of space
   * @param userId Id of user (remoteId)
   * @return true if that user is a member; otherwise,false
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #isMember(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  boolean isMember(String spaceId, String userId) throws SpaceException;

  /**
   * Checks if a user can access a space or not.
   * If the user is root or the space's member, return true
   * 
   * @param spaceId Id of space
   * @param userId Id of user (remoteId)
   * @return True if user has access permission and vice-versa
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #hasAccessPermission(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  boolean hasAccessPermission(String spaceId, String userId) throws SpaceException;

  /**
   * Checks if a user can have the edit permission of a space or not.
   * If user is root or the space's manager, return true.
   *
   * @param space Provided space
   * @param userId Id of user (user remoteId)
   * @return True if user has edition permission and vice-versa
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #hasSettingPermission(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  boolean hasEditPermission(Space space, String userId) throws SpaceException;

  /**
   * Checks if a user can have edit permission of a space.
   * If user is root or the space's manager, return true
   * @param spaceId Id of space
   * @param userId Id of user (remoteId)
   * @return True if user has edition permission and vice-versa
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #hasSettingPermission(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  boolean hasEditPermission(String spaceId, String userId) throws SpaceException;

  /**
   * Checks if a user is in the invited list of a space or not.
   *
   * @param space
   * @param userId
   * @return true if that user is in the invited list; otherwise, false
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #isInvitedUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  boolean isInvited(Space space, String userId) throws SpaceException;

  /**
   * Checks if a user is in the invited list of a space or not.
   *
   * @param spaceId
   * @param userId
   * @return <tt>true</tt> if user is in the invited list; otherwise, false
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #isInvitedUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  boolean isInvited(String spaceId, String userId) throws SpaceException;

  /**
   * Checks if a user is in the pending list of a space or not.
   *
   * @param space
   * @param userId
   * @return true if that user is in pending list; otherwise, false
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #isPendingUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  boolean isPending(Space space, String userId) throws SpaceException;

  /**
   * Checks if a user is in the pending list of a space.
   *
   * @param spaceId
   * @param userId
   * @return true if that user is in the pending list; otherwise, false
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #isPendingUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  boolean isPending(String spaceId, String userId) throws SpaceException;

  /**
   * Installs an application to a space.
   *
   * @param spaceId
   * @param appId
   * @throws SpaceException with code SpaceException.Code.ERROR_DATA_STORE
   * @LevelAPI Platform
   */
  void installApplication(String spaceId, String appId) throws SpaceException;

  /**
   * Installs an application to a space.
   *
   * @param space
   * @param appId
   * @throws SpaceException with code SpaceException.Code.ERROR_DATA_STORE
   * @LevelAPI Platform
   */
  void installApplication(Space space, String appId) throws SpaceException;

  /**
   * Activates an installed application in a space.
   *
   * @param space
   * @param appId
   * @throws SpaceException with possible code: SpaceException.Code.UNABLE_TO_ADD_APPLICATION,
   *                                            SpaceExeption.Code.ERROR_DATA_STORE
   * @LevelAPI Platform
   */
  void activateApplication(Space space, String appId) throws SpaceException;

  /**
   * Activates an installed application in a space.
   *
   * @param spaceId
   * @param appId
   * @throws SpaceException with possible code: SpaceException.Code.UNABLE_TO_ADD_APPLICATION,
   *                                            SpaceExeption.Code.ERROR_DATA_STORE
   * @LevelAPI Platform
   */
  void activateApplication(String spaceId, String appId) throws SpaceException;

  /**
   * Deactivates an installed application in a space.
   *
   * @param space
   * @param appId
   * @throws SpaceException
   * @LevelAPI Platform
   */
  void deactivateApplication(Space space, String appId) throws SpaceException;

  /**
   * Deactivates an installed application in a space.
   *
   * @param spaceId
   * @param appId
   * @throws SpaceException
   * @LevelAPI Platform
   */
  void deactivateApplication(String spaceId, String appId) throws SpaceException;

  /**
   * Removes an installed application from a space.
   *
   * @param space
   * @param appId
   * @throws SpaceException
   * @LevelAPI Platform
   */
  void removeApplication(Space space, String appId, String appName) throws SpaceException;

  /**
   * Removes an installed application from a space.
   *
   * @param spaceId
   * @param appId
   * @LevelAPI Platform
   */
  void removeApplication(String spaceId, String appId, String appName) throws SpaceException;
  
  /**
   * Update accessed space to top of space members list of Identity model
   *
   * @param remoteId
   * @param space
   * @LevelAPI Platform
   */
  void updateSpaceAccessed(String remoteId, Space space) throws SpaceException;
  
  /**
   * Gets list of spaces which user has been last visited.
   *
   * @param remoteId
   * @param appId
   * @param offset
   * @param limit
   * @LevelAPI Platform
   */
  List<Space> getLastAccessedSpace(String remoteId, String appId, int offset, int limit) throws SpaceException;
  

  /**
   * Requests a user to join a space, adds that user to the pending list of the space.
   *
   * @param space
   * @param userId
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #addPendingUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void requestJoin(Space space, String userId) throws SpaceException;

  /**
   * Requests a user to join a space, adds that user to the pending list of the space.
   *
   * @param spaceId
   * @param userId
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #addPendingUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void requestJoin(String spaceId, String userId) throws SpaceException;

  /**
   * Revokes a join request after users request to join a group and is in the pending status.
   *
   * @param space
   * @param userId
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #removePendingUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void revokeRequestJoin(Space space, String userId) throws SpaceException;

  /**
   * Revokes a request to join a space.
   *
   * @param spaceId
   * @param userId
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #removePendingUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void revokeRequestJoin(String spaceId, String userId) throws SpaceException;

  /**
   * Invites a userId to become a member of a space.
   *
   * @param space
   * @param userId
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #addInvitedUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void inviteMember(Space space, String userId) throws SpaceException;

  /**
   * Invites a userId to a be member of a space.
   *
   * @param spaceId
   * @param userId
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #addInvitedUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void inviteMember(String spaceId, String userId) throws SpaceException;

  /**
   * Revokes an invitation - undo inviteMember.
   * Removes a user from the invited  member list of the space.
   *
   * @param space
   * @param userId
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #removeInvitedUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void revokeInvitation(Space space, String userId) throws SpaceException;

  /**
   * Revokes invitation - undo inviteMember.
   * Removes a user from the invited  member list of the space.
   *
   * @param spaceId
   * @param userId
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #removeInvitedUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void revokeInvitation(String spaceId, String userId) throws SpaceException;

  /**
   * Accepts Invitation - move a user from the invited list to the member list.
   *
   * @param space
   * @param userId
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #addMember(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void acceptInvitation(Space space, String userId) throws SpaceException;

  /**
   * Accepts an invitation - move a user from the invited list to the member list.
   *
   * @param spaceId
   * @param userId
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #addMember(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void acceptInvitation(String spaceId, String userId) throws SpaceException;

  /**
   * Denies an invitation - removes a user from the invited list.
   *
   * @param space
   * @param userId
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #removeInvitedUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void denyInvitation(Space space, String userId) throws SpaceException;

  /**
   * Denies an invitation - removes user from the invited list.
   *
   * @param spaceId
   * @param userId
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #removeInvitedUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void denyInvitation(String spaceId, String userId) throws SpaceException;

  /**
   * Validates a request, moves a user from the pending list to the member list.
   *
   * @param space
   * @param userId
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #addMember(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void validateRequest(Space space, String userId) throws SpaceException;

  /**
   * Validates request, moves a user from pending list to member list.
   *
   * @param spaceId
   * @param userId
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #addMember(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void validateRequest(String spaceId, String userId) throws SpaceException;

  /**
   * Declines a request and removes a user from  the pending list.
   *
   * @param space
   * @param userId
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #removePendingUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void declineRequest(Space space, String userId) throws SpaceException;

  /**
   * Declines request and removes a user from the pending list.
   *
   * @param spaceId
   * @param userId
   * @throws SpaceException
   * @LevelAPI Provisional
   * @deprecated Use {@link #removePendingUser(org.exoplatform.social.core.space.model.Space, String)} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void declineRequest(String spaceId, String userId) throws SpaceException;

  /**
   * Registers a space lifecycle listener.
   *
   * @param listener
   * @LevelAPI Provisional
   * @deprecated Will be removed by 4.0.x
   */
  @Deprecated
  void registerSpaceLifeCycleListener(SpaceLifeCycleListener listener);

  /**
   * Unregisters a space lifecycle listener.
   *
   * @param listener
   * @LevelAPI Provisional
   * @deprecated Will be removed by 4.0.x
   */
  @Deprecated
  void unregisterSpaceLifeCycleListener(SpaceLifeCycleListener listener);

  /**
   * Sets the portlet preferences got from the plug-in configuration.
   *
   *
   * @param portletPrefsRequiredPlugin
   * @LevelAPI Provisional
   * @deprecated Use {@link SpaceApplicationConfigPlugin} instead.
   *             Will be removed by 4.0.x
   */
  @Deprecated
  void setPortletsPrefsRequired(PortletPreferenceRequiredPlugin portletPrefsRequiredPlugin);
  /**
   * Gets the portlet preferences required to use in creating the portlet application.
   *
   * @return Array of Portlet preferences.
   * @LevelAPI Provisional
   * @deprecated Will be removed by 4.0.x
   */
  String [] getPortletsPrefsRequired();

}
