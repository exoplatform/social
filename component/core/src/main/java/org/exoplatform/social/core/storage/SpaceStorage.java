/**
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
package org.exoplatform.social.core.storage;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.jcr.JCRSessionManager;
import org.exoplatform.social.common.jcr.NodeProperties;
import org.exoplatform.social.common.jcr.NodeTypes;
import org.exoplatform.social.common.jcr.QueryBuilder;
import org.exoplatform.social.common.jcr.SocialDataLocation;
import org.exoplatform.social.common.jcr.Util;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;

/**
 * SpaceStorage layer to work directly with JCR.
 *
 * @author <a href="hoatle.net">hoatle</a>
 */
public class SpaceStorage {
  /**
   * The Logger.
   */
  private static final Log LOG = ExoLogger.getLogger(SpaceStorage.class);
  
  /**
   * SPACE_PROPERTIES_NAME_PATTERN for loading all properties of one node at once.
   */
  private static final String SPACE_PROPERTIES_NAME_PATTERN = Util.getPropertiesNamePattern(
          new String[]{
                  NodeProperties.SPACE_APP,
                  NodeProperties.SPACE_AVATAR_URL,
                  NodeProperties.SPACE_DESCRIPTION,
                  NodeProperties.SPACE_DISPLAY_NAME,
                  NodeProperties.SPACE_GROUP_ID,
                  NodeProperties.SPACE_INVITED_USERS,
                  NodeProperties.SPACE_PARENT,
                  NodeProperties.SPACE_PENDING_USERS,
                  NodeProperties.SPACE_PRETTY_NAME,
                  NodeProperties.SPACE_PRIORITY,
                  NodeProperties.SPACE_REGISTRATION,
                  NodeProperties.SPACE_TAG,
                  NodeProperties.SPACE_TYPE,
                  NodeProperties.SPACE_URL,
                  NodeProperties.SPACE_VISIBILITY,
                  NodeProperties.SPACE_CREATOR,
                  NodeProperties.SPACE_MANAGERS,
                  NodeProperties.SPACE_MEMBERS,
          }
  );
  private SocialDataLocation dataLocation;
  private JCRSessionManager sessionManager;
  private static final String IMAGE_PATH = "image";
  private Node spaceHomeNode;
  
  private static final String PERCENT_STRING = "%";
  private static final String ASTERISK_STRING = "*";
  private static final char ASTERISK_CHARACTER = '*';
  
  /**
   * The cache for the spaces. The cache key is spaceId.
   */
  private final ExoCache<String, Space> spaceCacheById;

  /**
   * Constructor.
   *
   * @param dataLocation
   * @param cacheService
   */
  public SpaceStorage(SocialDataLocation dataLocation, CacheService cacheService) {
    this.dataLocation = dataLocation;
    this.sessionManager = dataLocation.getSessionManager();
    this.spaceCacheById = cacheService.getCacheInstance("exo.social.SpaceStorage.SpaceCacheById");
  }
  
  /**
   * Gets all the spaces. By the default get the all spaces with OFFSET = 0, LIMIT = 200;
   * 
   * @throws SpaceStorageException
   * @return the list of all spaces
   */
  public List<Space> getAllSpaces() throws SpaceStorageException {
    List<Space> spaces = new ArrayList<Space>();
    try {
      Session session = sessionManager.getOrOpenSession();
      List<Node> spacesNode = new QueryBuilder(session)
                                  .select(NodeTypes.EXO_SPACE)
                                  .orderBy(NodeProperties.SPACE_PRETTY_NAME, QueryBuilder.ASC)
                                  .exec();
      for (Node node : spacesNode) {
        spaces.add(getSpaceFromNode(node, session));
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_ALL_SPACES, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
    return spaces;
  }
  
  /**
   * Gets the spaces with offset, limit.
   * 
   * @param offset
   * @param limit
   * @return the list of the spaces with offset, limit
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getSpaces(long offset, long limit) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      List<Node> spaceNodes = null;
      Session session = sessionManager.getOrOpenSession();
      spaceNodes = new QueryBuilder(session)
                      .select(NodeTypes.EXO_SPACE, offset, limit)
                      .orderBy(NodeProperties.SPACE_PRETTY_NAME, QueryBuilder.ASC)
                      .exec();
      for (Node spaceNode : spaceNodes) {
        spaceList.add(getSpaceFromNode(spaceNode, session));
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_SPACES_WITH_OFFSET, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the count of the spaces.
   * 
   * @return the count of all spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getAllSpacesCount() throws SpaceStorageException {
    try {
      Session session = sessionManager.getOrOpenSession();
      return (int) new QueryBuilder(session)
                        .select(NodeTypes.EXO_SPACE)
                        .count();
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_SPACES_COUNT, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
  }
  
  /**
   * Gets the spaces by space filter with offset, limit.
   * 
   * @param spaceFilter
   * @param offset
   * @param limit
   * @return
   * @throws SpaceStorageException
   */
  public List<Space> getSpacesByFilter(SpaceFilter spaceFilter, long offset, long limit) {
    String spaceNameSearchCondition = spaceFilter.getSpaceNameSearchCondition();
    if (spaceNameSearchCondition != null && spaceNameSearchCondition.length() != 0) {
      return this.getSpacesBySearchCondition(spaceNameSearchCondition, offset, limit);
    } 
    return this.getSpacesByFirstCharacterOfName(spaceFilter.getFirstCharacterOfSpaceName(), offset, limit);
  }
  
  /**
   * Gets the count of the spaces which are searched by space filter.
   * 
   * @param spaceFilter
   * @return
   * @since 1.2.0-GA
   */
  public int getAllSpacesByFilterCount(SpaceFilter spaceFilter) {
    String spaceNameSearchCondition = spaceFilter.getSpaceNameSearchCondition();
    if (spaceNameSearchCondition != null && spaceNameSearchCondition.length() != 0) {
      return this.getSpacesBySearchConditionCount(spaceNameSearchCondition);
    } 
    return this.getSpacesByFirstCharacterOfNameCount(spaceFilter.getFirstCharacterOfSpaceName());
  }
  
  /**
   * Gets the spaces by search condition with offset, limit.
   * 
   * @param searchCondition
   * @param offset
   * @param limit
   * @return a list of the spaces with offset, limit.
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getSpacesBySearchCondition(String searchCondition, long offset, long limit) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      if (this.isValidInput(searchCondition)) {
        List<Node> spaceNodes = null;
        Session session = sessionManager.getOrOpenSession();
        searchCondition = this.processSearchCondition(searchCondition);
        if (searchCondition.indexOf(PERCENT_STRING) >= 0) {
          spaceNodes = new QueryBuilder(session)
                          .select(NodeTypes.EXO_SPACE, offset, limit)
                            .like(NodeProperties.SPACE_DISPLAY_NAME, searchCondition)
                            .or()
                            .like(NodeProperties.SPACE_DESCRIPTION, searchCondition)
                          .exec();
        } else {
          spaceNodes = new QueryBuilder(session)
                          .select(NodeTypes.EXO_SPACE, offset, limit)
                            .contains(NodeProperties.SPACE_DISPLAY_NAME, searchCondition)
                            .or()
                            .contains(NodeProperties.SPACE_DESCRIPTION, searchCondition)
                          .exec();
        }
        for (Node spaceNode : spaceNodes) {
          spaceList.add(getSpaceFromNode(spaceNode, session));
        }
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_SPACES_BY_SEARCH_CONDITION_WITH_OFFSET,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the count of the spaces which are matched by condition.
   * 
   * @param searchCondition
   * @return the count of the spaces.
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getSpacesBySearchConditionCount(String searchCondition) throws SpaceStorageException {
    int count = 0;
    try {
      if (this.isValidInput(searchCondition)) {
        searchCondition = this.processSearchCondition(searchCondition);
        Session session = sessionManager.getOrOpenSession();
        if (searchCondition.indexOf(PERCENT_STRING) >= 0) {
          count = (int) new QueryBuilder(session)
                            .select(NodeTypes.EXO_SPACE)
                              .like(NodeProperties.SPACE_DISPLAY_NAME, searchCondition)
                              .or()
                              .like(NodeProperties.SPACE_DESCRIPTION, searchCondition)
                            .count();
        } else {
          count = (int) new QueryBuilder(session)
                            .select(NodeTypes.EXO_SPACE)
                              .contains(NodeProperties.SPACE_DISPLAY_NAME, searchCondition)
                              .or()
                              .contains(NodeProperties.SPACE_DESCRIPTION, searchCondition)
                            .count();
        }
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_SPACES_BY_SEARCH_CONDITION_COUNT, 
                                      e.getMessage(), 
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return count;
  }
  
  /**
   * Gets the spaces by first character of name space with offset, limit.
   * 
   * @param firstCharacterOfName
   * @param offset
   * @param limit
   * @return a list of the spaces with offset, limit
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getSpacesByFirstCharacterOfName(char firstCharacterOfName,
                                                     long offset,
                                                     long limit) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      if (!Character.isDigit(firstCharacterOfName)) {
        String firstCharacterOfNameString = Character.toString(firstCharacterOfName);
        String firstCharacterOfNameLowerCase = firstCharacterOfNameString.toLowerCase()
            + PERCENT_STRING;
        List<Node> spaceNodes = null;
        Session session = sessionManager.getOrOpenSession();

        QueryBuilder query = new QueryBuilder(session);
        spaceNodes = query.select(NodeTypes.EXO_SPACE, offset, limit)
                          .like(query.lower(NodeProperties.SPACE_DISPLAY_NAME),
                                firstCharacterOfNameLowerCase)
                          .exec();
        for (Node spaceNode : spaceNodes) {
          spaceList.add(getSpaceFromNode(spaceNode, session));
        }
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_SPACES_BY_FIRST_CHARACTER_WITH_OFFSET,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the count of the spaces which are matched by first character of name space.
   * 
   * @param firstCharacterOfName
   * @return the count of the spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getSpacesByFirstCharacterOfNameCount(char firstCharacterOfName) throws SpaceStorageException {
    int count = 0;
    try {
      if (!Character.isDigit(firstCharacterOfName)) {
        String firstCharacterOfNameString = Character.toString(firstCharacterOfName);
        String firstCharacterOfNameLowerCase = firstCharacterOfNameString.toLowerCase() + PERCENT_STRING;
        Session session = sessionManager.getOrOpenSession();
        QueryBuilder query = new QueryBuilder(session);
        count = (int) query.select(NodeTypes.EXO_SPACE)
                             .like(query.lower(NodeProperties.SPACE_DISPLAY_NAME), firstCharacterOfNameLowerCase )
                           .count();
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_SPACES_BY_FIRST_CHARACTER_COUNT, 
                                      e.getMessage(), 
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return count;
  }
  
  /**
   * Gets the accessible spaces of the user by filter with offset, limit.
   * 
   * @param userId
   * @param spaceFilter
   * @param offset
   * @param limit
   * @return
   * @since 1.2.0-GA
   */
  public List<Space> getAccessibleSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    String spaceNameSearchCondition = spaceFilter.getSpaceNameSearchCondition();
    if (spaceNameSearchCondition != null && spaceNameSearchCondition.length() != 0) {
      return this.getAccessibleSpacesBySearchCondition(userId, spaceNameSearchCondition, offset, limit);
    } 
    return this.getAccessibleSpacesByFirstCharacterOfName(userId, spaceFilter.getFirstCharacterOfSpaceName(), offset, limit);
  }
  
  /**
   * Gets the count of the accessible spaces of the user by filter.
   * 
   * @param userId
   * @param spaceFilter
   * @return
   * @since 1.2.0-GA
   */
  public int getAccessibleSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    String spaceNameSearchCondition = spaceFilter.getSpaceNameSearchCondition();
    if (spaceNameSearchCondition != null && spaceNameSearchCondition.length() != 0) {
      return this.getAccessibleSpacesBySearchConditionCount(userId, spaceNameSearchCondition);
    } 
    return this.getAccessibleSpacesByFirstCharacterOfNameCount(userId, spaceFilter.getFirstCharacterOfSpaceName());
  }
  
  /**
   * Gets the spaces of a user which that user has the "member" role or edit permission.
   * 
   * @param userId the userId
   * @return a list of the accessible spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getAccessibleSpaces(String userId) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    List<Node> spaceNodes = null;
    try {
      Session session = sessionManager.getOrOpenSession();
      spaceNodes = new QueryBuilder(session)
                        .select(NodeTypes.EXO_SPACE)
                          .equal(NodeProperties.SPACE_MANAGERS, userId)
                          .or()
                          .equal(NodeProperties.SPACE_MEMBERS, userId)
                        .exec();
      for (Node spaceNode : spaceNodes) {
        spaceList.add(getSpaceFromNode(spaceNode, session));
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_ACCESSIBLE_SPACES, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the spaces of a user which that user has "member" role or edit permission with offset, limit.
   * 
   * @param userId the userId
   * @param offset
   * @param limit
   * @return a list of the accessible space with offset, limit
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getAccessibleSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      List<Node> spaceNodes = null;
      Session session = sessionManager.getOrOpenSession();
      spaceNodes = new QueryBuilder(session)
                        .select(NodeTypes.EXO_SPACE, offset, limit)
                          .equal(NodeProperties.SPACE_MANAGERS, userId)
                          .or()
                          .equal(NodeProperties.SPACE_MEMBERS, userId)
                        .exec();
      for (Node spaceNode : spaceNodes) {
        spaceList.add(getSpaceFromNode(spaceNode, session));
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_ACCESSIBLE_SPACES_WITH_OFFSET,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the count of the accessible spaces of the userId.
   * 
   * @param userId
   * @return the count of the accessible spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getAccessibleSpacesCount(String userId) throws SpaceStorageException {
    int count = 0;
    try {
      Session session = sessionManager.getOrOpenSession();
      count = (int) new QueryBuilder(session)
                        .select(NodeTypes.EXO_SPACE)
                          .equal(NodeProperties.SPACE_MANAGERS, userId)
                          .or()
                          .equal(NodeProperties.SPACE_MEMBERS, userId)
                        .count();
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_ACCESSIBLE_SPACES_COUNT, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
    return count;
  }
  
  /**
   * Gets the spaces of a user which that user has the "member" role or edit permission by searchCondition with offset, limit.
   * 
   * @param userId the userId
   * @param searchCondition
   * @param offset
   * @param limit
   * @return a list of the spaces with offset, limit
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getAccessibleSpacesBySearchCondition(String userId, 
                                                          String searchCondition, 
                                                          long offset, 
                                                          long limit) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      if (this.isValidInput(searchCondition)) {
        searchCondition = this.processSearchCondition(searchCondition);
        List<Node> spaceNodes = null;
        Session session = sessionManager.getOrOpenSession();
        if (searchCondition.indexOf(PERCENT_STRING) >= 0) {
          spaceNodes = new QueryBuilder(session)
                          .select(NodeTypes.EXO_SPACE, offset, limit)
                            .group()
                              .equal(NodeProperties.SPACE_MANAGERS, userId)
                              .or()
                              .equal(NodeProperties.SPACE_MEMBERS, userId)
                            .endGroup()
                            .and()
                            .group()
                              .like(NodeProperties.SPACE_DISPLAY_NAME, searchCondition)
                              .or()
                              .like(NodeProperties.SPACE_DESCRIPTION, searchCondition)
                            .endGroup()
                          .exec();
        } else {
          spaceNodes = new QueryBuilder(session)
                          .select(NodeTypes.EXO_SPACE, offset, limit)
                            .group()
                              .equal(NodeProperties.SPACE_MANAGERS, userId)
                              .or()
                              .equal(NodeProperties.SPACE_MEMBERS, userId)
                            .endGroup()
                            .and()
                            .group()
                              .contains(NodeProperties.SPACE_DISPLAY_NAME, searchCondition)
                              .or()
                              .contains(NodeProperties.SPACE_DESCRIPTION, searchCondition)
                            .endGroup()
                          .exec();
        }
        for (Node spaceNode : spaceNodes) {
          spaceList.add(getSpaceFromNode(spaceNode, session));
        }
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_ACCESSIBLE_SPACES_BY_SEARCH_CONDITION,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the count of the spaces of a user which that user has the "member" role or edit permission by searchCondition.
   * 
   * @param userId the userId
   * @param searchCondition
   * @return the count of the spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getAccessibleSpacesBySearchConditionCount(String userId, String searchCondition) throws SpaceStorageException {
    int count = 0;
    try {
      if (this.isValidInput(searchCondition)) {
        searchCondition = this.processSearchCondition(searchCondition);
        Session session = sessionManager.getOrOpenSession();
        if (searchCondition.indexOf(PERCENT_STRING) >= 0) {
          count = (int) new QueryBuilder(session)
                            .select(NodeTypes.EXO_SPACE)
                              .group()
                                .equal(NodeProperties.SPACE_MANAGERS, userId)
                                .or()
                                .equal(NodeProperties.SPACE_MEMBERS, userId)
                              .endGroup()
                              .and()
                              .group()
                                .like(NodeProperties.SPACE_DISPLAY_NAME, searchCondition)
                                .or()
                                .like(NodeProperties.SPACE_DESCRIPTION, searchCondition)
                              .endGroup()
                            .count();
        } else {
          count = (int) new QueryBuilder(session)
                        .select(NodeTypes.EXO_SPACE)
                          .group()
                            .equal(NodeProperties.SPACE_MANAGERS, userId)
                            .or()
                            .equal(NodeProperties.SPACE_MEMBERS, userId)
                          .endGroup()
                          .and()
                          .group()
                            .contains(NodeProperties.SPACE_DISPLAY_NAME, searchCondition)
                            .or()
                            .contains(NodeProperties.SPACE_DESCRIPTION, searchCondition)
                          .endGroup()
                        .count();
        }
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_ACCESSIBLE_SPACES_BY_SEARCH_CONDITION_COUNT,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return count;
  }
  
  /**
   * Gets the spaces of a user which that user has the "member" role or edit permission 
   * by first character of name space with offset, limit.
   * 
   * @param userId the userId
   * @param firstCharacterOfName
   * @param offset
   * @param limit
   * @return a list of the spaces with offset, limit
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getAccessibleSpacesByFirstCharacterOfName(String userId,
                                                               char firstCharacterOfName,
                                                               long offset,
                                                               long limit) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      if (!Character.isDigit(firstCharacterOfName)) {
        List<Node> spaceNodes = null;
        String firstCharacterOfNameString = Character.toString(firstCharacterOfName);
        String firstCharacterOfNameLowerCaseQuery = firstCharacterOfNameString.toLowerCase() + PERCENT_STRING;
        Session session = sessionManager.getOrOpenSession();
        QueryBuilder query = new QueryBuilder(session);
        spaceNodes = query.select(NodeTypes.EXO_SPACE, offset, limit)
                            .group()
                              .equal(NodeProperties.SPACE_MANAGERS, userId)
                              .or()
                              .equal(NodeProperties.SPACE_MEMBERS, userId)
                            .endGroup()
                            .and()
                              .like(query.lower(NodeProperties.SPACE_DISPLAY_NAME), firstCharacterOfNameLowerCaseQuery)
                          .exec();
        for (Node spaceNode : spaceNodes) {
          spaceList.add(getSpaceFromNode(spaceNode, session));
        }
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_ACCESSIBLE_SPACES_BY_FIRST_CHARACTER,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the count of the spaces of a user which that user has the "member" role or edit permission 
   * by first character of name space.
   * 
   * @param userId the userId
   * @param firstCharacterOfName
   * @return the count of the spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getAccessibleSpacesByFirstCharacterOfNameCount(String userId,
                                                            char firstCharacterOfName) throws SpaceStorageException {
    int count = 0;
    try {
      if (!Character.isDigit(firstCharacterOfName)) {
        Session session = sessionManager.getOrOpenSession();
        String firstCharacterOfNameString = Character.toString(firstCharacterOfName);
        String firstCharacterOfNameLowerCaseQuery = firstCharacterOfNameString.toLowerCase() + PERCENT_STRING;
        QueryBuilder query = new QueryBuilder(session);
        count = (int) query.select(NodeTypes.EXO_SPACE)
                              .group()
                                .equal(NodeProperties.SPACE_MANAGERS, userId)
                                .or()
                                .equal(NodeProperties.SPACE_MEMBERS, userId)
                              .endGroup()
                              .and()
                              .like(query.lower(NodeProperties.SPACE_DISPLAY_NAME), firstCharacterOfNameLowerCaseQuery)
                            .count();
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_ACCESSIBLE_SPACES_BY_FIRST_CHARACTER_COUNT, 
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return count;
  }
  
  /**
   * Gets the editable spaces of the user by filter with offset, limit.
   * 
   * @param userId
   * @param spaceFilter
   * @param offset
   * @param limit
   * @return
   * @since 1.2.0-GA
   */
  public List<Space> getEditableSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    String spaceNameSearchCondition = spaceFilter.getSpaceNameSearchCondition();
    if (spaceNameSearchCondition != null && spaceNameSearchCondition.length() != 0) {
      return this.getEditableSpacesBySearchCondition(userId, spaceNameSearchCondition, offset, limit);
    } 
    return this.getEditableSpacesByFirstCharacterOfSpaceName(userId, spaceFilter.getFirstCharacterOfSpaceName(), offset, limit);
  }
  
  /**
   * Gets the count of the editable spaces of the user by filter.
   * 
   * @param userId
   * @param spaceFilter
   * @return
   * @since 1.2.0-GA
   */
  public int getEditableSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    String spaceNameSearchCondition = spaceFilter.getSpaceNameSearchCondition();
    if (spaceNameSearchCondition != null && spaceNameSearchCondition.length() != 0) {
      return this.getEditableSpacesBySearchConditionCount(userId, spaceNameSearchCondition);
    } 
    return this.getEditableSpacesByFirstCharacterOfSpaceNameCount(userId, spaceFilter.getFirstCharacterOfSpaceName());
  }
  
  /**
   * Gets the spaces of a user which that user has the edit permission.
   * 
   * @param userId
   * @return a list of the editable spaces.
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getEditableSpaces(String userId) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      List<Node> spaceNodes = null;
      Session session = sessionManager.getOrOpenSession();
      spaceNodes = new QueryBuilder(session)
                        .select(NodeTypes.EXO_SPACE)
                          .equal(NodeProperties.SPACE_MANAGERS, userId)
                        .exec();
      for (Node spaceNode : spaceNodes) {
        spaceList.add(getSpaceFromNode(spaceNode, session));
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_EDITABLE_SPACES, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the spaces of a user which that user has the edit permission with offset, limit.
   * 
   * @param userId
   * @param offset
   * @param limit
   * @return a list of the spaces with offset, limit
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getEditableSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      List<Node> spaceNodes = null;
      Session session = sessionManager.getOrOpenSession();
      spaceNodes = new QueryBuilder(session)
                        .select(NodeTypes.EXO_SPACE, offset, limit)
                          .equal(NodeProperties.SPACE_MANAGERS, userId)
                        .exec();
      for (Node spaceNode : spaceNodes) {
        spaceList.add(getSpaceFromNode(spaceNode, session));
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_EDITABLE_SPACES_WITH_OFFSET,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the count of the spaces of a user which that user has the edit permission.
   * 
   * @param userId
   * @return the count of the editable spaces.
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getEditableSpacesCount(String userId) throws SpaceStorageException {
    try {
      Session session = sessionManager.getOrOpenSession();
      return (int) new QueryBuilder(session)
                        .select(NodeTypes.EXO_SPACE)
                          .equal(NodeProperties.SPACE_MANAGERS, userId)
                        .count();
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_EDITABLE_SPACES_COUNT, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
  }
  
  /**
   * Gets the editable spaces of the user by space name search condition with offset, limit.
   * 
   * @param userId
   * @param spaceNameSearchCondition
   * @param offset
   * @param limit
   * @return
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getEditableSpacesBySearchCondition(String userId,
                                                        String spaceNameSearchCondition,
                                                        long offset,
                                                        long limit) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      if (this.isValidInput(spaceNameSearchCondition)) {
        spaceNameSearchCondition = this.processSearchCondition(spaceNameSearchCondition);
        List<Node> spaceNodes = null;
        Session session = sessionManager.getOrOpenSession();
        if (spaceNameSearchCondition.indexOf(PERCENT_STRING) >= 0) {
          spaceNodes = new QueryBuilder(session)
                          .select(NodeTypes.EXO_SPACE, offset, limit)
                            .equal(NodeProperties.SPACE_MANAGERS, userId)
                            .and()
                            .group()
                              .like(NodeProperties.SPACE_DISPLAY_NAME, spaceNameSearchCondition)
                              .or()
                              .like(NodeProperties.SPACE_DESCRIPTION, spaceNameSearchCondition)
                            .endGroup()
                          .exec();
        } else {
          spaceNodes = new QueryBuilder(session)
                          .select(NodeTypes.EXO_SPACE, offset, limit)
                            .equal(NodeProperties.SPACE_MANAGERS, userId)
                            .and()
                            .group()
                            .contains(NodeProperties.SPACE_DISPLAY_NAME, spaceNameSearchCondition)
                            .or()
                              .contains(NodeProperties.SPACE_DESCRIPTION, spaceNameSearchCondition)
                            .endGroup()
                          .exec();
        }
        for (Node spaceNode : spaceNodes) {
          spaceList.add(getSpaceFromNode(spaceNode, session));
        }
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_EDITABLE_SPACES_BY_SEARCH_CONDITION,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the count of the editable spaces of the user by space name search condition.
   * 
   * @param userId
   * @param spaceNameSearchCondition
   * @return
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getEditableSpacesBySearchConditionCount(String userId,
                                                     String spaceNameSearchCondition) throws SpaceStorageException {
    int count = 0;
    try {
      if (this.isValidInput(spaceNameSearchCondition)) {
        spaceNameSearchCondition = this.processSearchCondition(spaceNameSearchCondition);
        Session session = sessionManager.getOrOpenSession();
        if (spaceNameSearchCondition.indexOf(PERCENT_STRING) >= 0) {
          count = (int) new QueryBuilder(session)
                          .select(NodeTypes.EXO_SPACE)
                            .equal(NodeProperties.SPACE_MANAGERS, userId)
                            .and()
                            .group()
                              .like(NodeProperties.SPACE_DISPLAY_NAME, spaceNameSearchCondition)
                              .or()
                              .like(NodeProperties.SPACE_DESCRIPTION, spaceNameSearchCondition)
                            .endGroup()
                          .count();
        } else {
          count = (int) new QueryBuilder(session)
                          .select(NodeTypes.EXO_SPACE)
                            .equal(NodeProperties.SPACE_MANAGERS, userId)
                            .and()
                            .group()
                              .contains(NodeProperties.SPACE_DISPLAY_NAME, spaceNameSearchCondition)
                              .or()
                              .contains(NodeProperties.SPACE_DESCRIPTION, spaceNameSearchCondition)
                            .endGroup()
                          .count();
        }
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_EDITABLE_SPACES_BY_SEARCH_CONDITION_COUNT,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return count;
  }
  
  /**
   * Gets the editable spaces of the user by first character of space name with offset, limit.
   * 
   * @param userId
   * @param firstCharacterOfSpaceName
   * @param offset
   * @param limit
   * @return
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getEditableSpacesByFirstCharacterOfSpaceName(String userId,
                                                                  char firstCharacterOfSpaceName,
                                                                  long offset,
                                                                  long limit) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      if (!Character.isDigit(firstCharacterOfSpaceName)) {
        List<Node> spaceNodes = null;
        String firstCharacterOfNameString = Character.toString(firstCharacterOfSpaceName);
        String firstCharacterOfNameLowerCaseQuery = firstCharacterOfNameString.toLowerCase() + PERCENT_STRING;
        Session session = sessionManager.getOrOpenSession();
        QueryBuilder query = new QueryBuilder(session);
        spaceNodes = new QueryBuilder(session)
                        .select(NodeTypes.EXO_SPACE, offset, limit)
                          .equal(NodeProperties.SPACE_MANAGERS, userId)
                          .and()
                          .like(query.lower(NodeProperties.SPACE_DISPLAY_NAME), firstCharacterOfNameLowerCaseQuery)
                        .exec();
        for (Node spaceNode : spaceNodes) {
          spaceList.add(getSpaceFromNode(spaceNode, session));
        }
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_EDITABLE_SPACES_BY_FIRST_CHARACTER,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the count of the editable spaces of the user by first character of space name.
   * 
   * @param userId
   * @param firstCharacterOfSpaceName
   * @return
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getEditableSpacesByFirstCharacterOfSpaceNameCount(String userId,
                                                               char firstCharacterOfSpaceName) throws SpaceStorageException {
    int count = 0;
    try {
      if (!Character.isDigit(firstCharacterOfSpaceName)) {
        String firstCharacterOfNameString = Character.toString(firstCharacterOfSpaceName);
        String firstCharacterOfNameLowerCaseQuery = firstCharacterOfNameString.toLowerCase() + PERCENT_STRING;
        Session session = sessionManager.getOrOpenSession();
        QueryBuilder query = new QueryBuilder(session);
        count = (int) new QueryBuilder(session)
                          .select(NodeTypes.EXO_SPACE)
                            .equal(NodeProperties.SPACE_MANAGERS, userId)
                            .and()
                            .like(query.lower(NodeProperties.SPACE_DISPLAY_NAME), firstCharacterOfNameLowerCaseQuery)
                          .count();
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_EDITABLE_SPACES_BY_FIRST_CHARACTER_COUNT,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return count;
  }
  
  /**
   * Gets the invited spaces of the user by space filter with offset, limit.
   * 
   * @param userId
   * @param spaceFilter
   * @param offset
   * @param limit
   * @return
   * @since 1.2.0-GA
   */
  public List<Space> getInvitedSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    String spaceNameSearchCondition = spaceFilter.getSpaceNameSearchCondition();
    if (spaceNameSearchCondition != null && spaceNameSearchCondition.length() != 0) {
      return this.getInvitedSpacesBySearchCondition(userId, spaceNameSearchCondition, offset, limit);
    } 
    return this.getInvitedSpacesByFirstCharacterOfName(userId, spaceFilter.getFirstCharacterOfSpaceName(), offset, limit);
  }
  
  /**
   * Gets the count of the invited spaces of the user by filter.
   * 
   * @param userId
   * @param spaceFilter
   * @return
   * @since 1.2.0-GA
   */
  public int getInvitedSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    String spaceNameSearchCondition = spaceFilter.getSpaceNameSearchCondition();
    if (spaceNameSearchCondition != null && spaceNameSearchCondition.length() != 0) {
      return this.getInvitedSpacesBySearchConditionCount(userId, spaceNameSearchCondition);
    } 
    return this.getInvitedSpacesByFirstCharacterNameCount(userId, spaceFilter.getFirstCharacterOfSpaceName());
  }
  
  /**
   * Gets a user's invited spaces and that user can accept or deny the request.
   * 
   * @param userId
   * @return a list of the invited spaces.
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getInvitedSpaces(String userId) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      List<Node> spaceNodes = null;
      Session session = sessionManager.getOrOpenSession();
      spaceNodes = new QueryBuilder(session)
                      .select(NodeTypes.EXO_SPACE)
                        .equal(NodeProperties.SPACE_INVITED_USERS, userId)
                      .exec();
      for (Node spaceNode : spaceNodes) {
        spaceList.add(getSpaceFromNode(spaceNode, session));
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_INVITED_SPACES, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets a user's invited spaces and that user can accept or deny the request with offset, limit.
   * 
   * @param userId
   * @param offset
   * @param limit
   * @return a list of the invited spaces with offset, limit
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getInvitedSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      List<Node> spaceNodes = null;
      Session session = sessionManager.getOrOpenSession();
      spaceNodes = new QueryBuilder(session)
                      .select(NodeTypes.EXO_SPACE, offset, limit)
                        .equal(NodeProperties.SPACE_INVITED_USERS, userId)
                      .exec();
      for (Node spaceNode : spaceNodes) {
        spaceList.add(getSpaceFromNode(spaceNode, session));
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_INVITED_SPACES_WITH_OFFSET,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the count of the invited spaces of the userId.
   * 
   * @param userId
   * @return the count of the invited spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getInvitedSpacesCount(String userId) throws SpaceStorageException {
    try {
      Session session = sessionManager.getOrOpenSession();
      return (int) new QueryBuilder(session)
                        .select(NodeTypes.EXO_SPACE)
                          .equal(NodeProperties.SPACE_INVITED_USERS, userId)
                        .count();
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_INVITED_SPACES_COUNT, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
  }
  
  /**
   * Gets a user's invited spaces and that user can accept or deny the request by search condition with offset, limit.
   * 
   * @param userId
   * @param searchCondition
   * @param offset
   * @param limit
   * @return a list of the spaces with offset, limit
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getInvitedSpacesBySearchCondition(String userId,
                                                       String searchCondition,
                                                       long offset,
                                                       long limit) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      if (this.isValidInput(searchCondition)) {
        searchCondition = this.processSearchCondition(searchCondition);
        List<Node> spaceNodes = null;
        Session session = sessionManager.getOrOpenSession();
        if(searchCondition.indexOf(PERCENT_STRING) >= 0) {
          spaceNodes = new QueryBuilder(session)
                          .select(NodeTypes.EXO_SPACE, offset, limit)
                            .equal(NodeProperties.SPACE_INVITED_USERS, userId)
                            .and()
                            .group()
                              .like(NodeProperties.SPACE_DISPLAY_NAME, searchCondition)
                              .or()
                              .like(NodeProperties.SPACE_DESCRIPTION, searchCondition)
                            .endGroup()
                          .exec();
        } else {
          spaceNodes = new QueryBuilder(session)
                          .select(NodeTypes.EXO_SPACE, offset, limit)
                            .equal(NodeProperties.SPACE_INVITED_USERS, userId)
                            .and()
                            .group()
                              .contains(NodeProperties.SPACE_DISPLAY_NAME, searchCondition)
                              .or()
                              .contains(NodeProperties.SPACE_DESCRIPTION, searchCondition)
                            .endGroup()
                          .exec();
        }
        for (Node spaceNode : spaceNodes) {
          spaceList.add(getSpaceFromNode(spaceNode, session));
        }
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_INVITED_SPACES_BY_SEARCH_CONDITION,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the count of a user's invited spaces and that user can accept or deny the request by search condition.
   * 
   * @param userId
   * @param searchCondition
   * @return the count of the spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getInvitedSpacesBySearchConditionCount(String userId, String searchCondition) throws SpaceStorageException {
    int count = 0;
    try {
      if (this.isValidInput(searchCondition)) {
        searchCondition = this.processSearchCondition(searchCondition);
        Session session = sessionManager.getOrOpenSession();
        if (searchCondition.indexOf(PERCENT_STRING) >= 0) {
          count = (int) new QueryBuilder(session)
                            .select(NodeTypes.EXO_SPACE)
                              .equal(NodeProperties.SPACE_INVITED_USERS, userId)
                              .and()
                              .group()
                                .like(NodeProperties.SPACE_DISPLAY_NAME, searchCondition)
                                .or()
                                .like(NodeProperties.SPACE_DESCRIPTION, searchCondition)
                              .endGroup()
                            .count();
        } else {
          count = (int) new QueryBuilder(session)
                            .select(NodeTypes.EXO_SPACE)
                              .equal(NodeProperties.SPACE_INVITED_USERS, userId)
                              .and()
                              .group()
                                .contains(NodeProperties.SPACE_DISPLAY_NAME, searchCondition)
                                .or()
                                .contains(NodeProperties.SPACE_DESCRIPTION, searchCondition)
                              .endGroup()
                            .count();
        }
      }
    } catch (Exception e) {
      throw  new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_INVITED_SPACES_BY_SEARCH_CONDITION_COUNT,
                                       e.getMessage(),
                                       e);
    } finally {
      sessionManager.closeSession();
    }
    return count;
  }
  
  /**
   * Gets a user's invited spaces and that user can accept or deny the request 
   * by first character of name space with offset, limit.
   * 
   * @param userId
   * @param firstCharacterName
   * @param offset
   * @param limit
   * @return a list of the spaces with offset, limit
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getInvitedSpacesByFirstCharacterOfName(String userId,
                                                            char firstCharacterOfName,
                                                            long offset,
                                                            long limit) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      if (!Character.isDigit(firstCharacterOfName)) {
        Session session = sessionManager.getOrOpenSession();
        List<Node> spaceNodes = null;
        String firstCharacterOfNameString = Character.toString(firstCharacterOfName);
        String firstCharacterOfNameLowerCaseQuery = firstCharacterOfNameString.toLowerCase() + PERCENT_STRING;
        QueryBuilder query = new QueryBuilder(session);
        spaceNodes = query.select(NodeTypes.EXO_SPACE, offset, limit)
                            .equal(NodeProperties.SPACE_INVITED_USERS, userId)
                            .and()
                            .like(query.lower(NodeProperties.SPACE_DISPLAY_NAME), firstCharacterOfNameLowerCaseQuery)
                          .exec();
        for (Node spaceNode : spaceNodes) {
          spaceList.add(getSpaceFromNode(spaceNode, session));
        }
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_INVITED_SPACES_BY_FIRST_CHARACTER,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the count of a user's invited spaces and that user can accept or deny the request by first character of name space.
   * 
   * @param userId
   * @param firstCharacterName
   * @return the count of the spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getInvitedSpacesByFirstCharacterNameCount(String userId, char firstCharacterOfName) throws SpaceStorageException {
    int count = 0;
    try {
      if (!Character.isDigit(firstCharacterOfName)) {
        Session session = sessionManager.getOrOpenSession();
        String firstCharacterOfNameString = Character.toString(firstCharacterOfName);
        String firstCharacterOfNameLowerCaseQuery = firstCharacterOfNameString.toLowerCase() + PERCENT_STRING;
        QueryBuilder query = new QueryBuilder(session);
        count = (int) query.select(NodeTypes.EXO_SPACE)
                             .equal(NodeProperties.SPACE_INVITED_USERS, userId)
                             .and()
                             .like(query.lower(NodeProperties.SPACE_DISPLAY_NAME), firstCharacterOfNameLowerCaseQuery)
                           .count();
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_INVITED_SPACES_BY_FIRST_CHARACTER_COUNT,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return count;
  }
  
  /**
   * Gets the pending spaces of the user by space filter with offset, limit.
   * 
   * @param userId
   * @param spaceFilter
   * @param offset
   * @param limit
   * @return
   * @since 1.2.0-GA
   */
  public List<Space> getPendingSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    String spaceNameSearchCondition = spaceFilter.getSpaceNameSearchCondition();
    if (spaceNameSearchCondition != null && spaceNameSearchCondition.length() != 0) {
      return this.getPendingSpacesBySearchCondition(userId, spaceNameSearchCondition, offset, limit);
    } 
    return this.getPendingSpacesByFirstCharacterOfName(userId, spaceFilter.getFirstCharacterOfSpaceName(), offset, limit);
  }
  
  /**
   * Gets the count of the pending spaces of the user by space filter.
   * 
   * @param userId
   * @param spaceFilter
   * @return
   * @since 1.2.0-GA
   */
  public int getPendingSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    String spaceNameSearchCondition = spaceFilter.getSpaceNameSearchCondition();
    if (spaceNameSearchCondition != null && spaceNameSearchCondition.length() != 0) {
      return this.getPendingSpacesBySearchConditionCount(userId, spaceNameSearchCondition);
    } 
    return this.getPendingSpacesByFirstCharacterOfNameCount(userId, spaceFilter.getFirstCharacterOfSpaceName());
  }
  
  /**
   * Gets a user's pending spaces and that the user can revoke that request.
   * 
   * @param userId
   * @return a list of the pending spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getPendingSpaces(String userId) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      List<Node> spaceNodes = null;
      Session session = sessionManager.getOrOpenSession();
      spaceNodes = new QueryBuilder(session)
                        .select(NodeTypes.EXO_SPACE)
                          .equal(NodeProperties.SPACE_PENDING_USERS, userId)
                        .exec();
      for (Node spaceNode : spaceNodes) {
        spaceList.add(getSpaceFromNode(spaceNode, session));
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_PENDING_SPACES, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets a user's pending spaces and that the user can revoke that request with offset, limit.
   * 
   * @param userId
   * @param offset
   * @param limit
   * @return a list of the pending spaces with offset, limit
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getPendingSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      List<Node> spaceNodes = null;
      Session session = sessionManager.getOrOpenSession();
      spaceNodes = new QueryBuilder(session)
                        .select(NodeTypes.EXO_SPACE, offset, limit)
                          .equal(NodeProperties.SPACE_PENDING_USERS, userId)
                        .exec();
      for (Node spaceNode : spaceNodes) {
        spaceList.add(getSpaceFromNode(spaceNode, session));
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_PENDING_SPACES_WITH_OFFSET,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the count of the pending spaces of the userId.
   * 
   * @param userId
   * @return the count of the pending spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getPendingSpacesCount(String userId) throws SpaceStorageException {
    try {
      Session session = sessionManager.getOrOpenSession();
      return (int) new QueryBuilder(session)
                        .select(NodeTypes.EXO_SPACE)
                          .equal(NodeProperties.SPACE_PENDING_USERS, userId)
                        .count();
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_PENDING_SPACES_COUNT, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
  }
  
  /**
   * Gets a user's pending spaces and that the user can revoke that request by search condition with offset, limit.
   * 
   * @param userId
   * @param searchCondition
   * @param offset
   * @param limit
   * @return a list of the spaces with offset, limit
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getPendingSpacesBySearchCondition(String userId,
                                                       String searchCondition,
                                                       long offset,
                                                       long limit) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      if (this.isValidInput(searchCondition)) {
        searchCondition = this.processSearchCondition(searchCondition);
        List<Node> spaceNodes = null;
        Session session = sessionManager.getOrOpenSession();
        if (searchCondition.indexOf(PERCENT_STRING) >= 0) {
          spaceNodes = new QueryBuilder(session)
                          .select(NodeTypes.EXO_SPACE, offset, limit)
                            .equal(NodeProperties.SPACE_PENDING_USERS, userId)
                            .and()
                            .group()
                              .like(NodeProperties.SPACE_DISPLAY_NAME, searchCondition)
                              .or()
                              .like(NodeProperties.SPACE_DESCRIPTION, searchCondition)
                            .endGroup()
                          .exec();
        } else {
          spaceNodes = new QueryBuilder(session)
                          .select(NodeTypes.EXO_SPACE, offset, limit)
                            .equal(NodeProperties.SPACE_PENDING_USERS, userId)
                            .and()
                            .group()
                              .contains(NodeProperties.SPACE_DISPLAY_NAME, searchCondition)
                              .or()
                              .contains(NodeProperties.SPACE_DESCRIPTION, searchCondition)
                            .endGroup()
                          .exec();
        }
        for (Node spaceNode : spaceNodes) {
          spaceList.add(getSpaceFromNode(spaceNode, session));
        }
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_PENDING_SPACES_BY_SEARCH_CONDITION,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the count of a user's pending spaces and that the user can revoke that request by search condition.
   * 
   * @param userId
   * @param searchCondition
   * @return the count of the spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getPendingSpacesBySearchConditionCount(String userId, String searchCondition) throws SpaceStorageException {
    int count = 0;
    try {
      if (this.isValidInput(searchCondition)) {
        searchCondition = this.processSearchCondition(searchCondition);
        Session session = sessionManager.getOrOpenSession();
        if (searchCondition.indexOf(PERCENT_STRING) >= 0) {
          count = (int) new QueryBuilder(session)
                            .select(NodeTypes.EXO_SPACE)
                              .equal(NodeProperties.SPACE_PENDING_USERS, userId)
                              .and()
                              .group()
                                .like(NodeProperties.SPACE_DISPLAY_NAME, searchCondition)
                                .or()
                                .like(NodeProperties.SPACE_DESCRIPTION, searchCondition)
                              .endGroup()
                            .count();
        } else {
          count = (int) new QueryBuilder(session)
                            .select(NodeTypes.EXO_SPACE)
                              .equal(NodeProperties.SPACE_PENDING_USERS, userId)
                              .and()
                              .group()
                                .contains(NodeProperties.SPACE_DISPLAY_NAME, searchCondition)
                                .or()
                                .contains(NodeProperties.SPACE_DESCRIPTION, searchCondition)
                              .endGroup()
                            .count();
        }
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_PENDING_SPACES_BY_SEARCH_CONDITION_COUNT,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return count;
  }
  
  /**
   * Gets a user's pending spaces and that the user can revoke that request by first character of name space with offset, limit.
   * 
   * @param userId
   * @param firstCharacterOfName
   * @param offset
   * @param limit
   * @return a list of the spaces with offset, limit
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getPendingSpacesByFirstCharacterOfName(String userId,
                                                            char firstCharacterOfName,
                                                            long offset,
                                                            long limit) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      if (!Character.isDigit(firstCharacterOfName)) {
        List<Node> spaceNodes = null;
        String firstCharacterOfNameString = Character.toString(firstCharacterOfName);
        String firstCharacterOfNameLowerCaseQuery = firstCharacterOfNameString.toLowerCase() + PERCENT_STRING;
        Session session = sessionManager.getOrOpenSession();
        QueryBuilder query = new QueryBuilder(session);
        spaceNodes = query.select(NodeTypes.EXO_SPACE, offset, limit)
                            .equal(NodeProperties.SPACE_PENDING_USERS, userId)
                            .and()
                            .like(query.lower(NodeProperties.SPACE_DISPLAY_NAME), firstCharacterOfNameLowerCaseQuery)
                          .exec();
        for (Node spaceNode : spaceNodes) {
          spaceList.add(getSpaceFromNode(spaceNode, session));
        }
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_PENDING_SPACES_BY_FIRST_CHARACTER,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the count of a user's pending spaces and that the user can revoke that request with first character of name space.
   * 
   * @param userId
   * @param firstCharacterOfName
   * @return the count of the spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getPendingSpacesByFirstCharacterOfNameCount(String userId, char firstCharacterOfName) throws SpaceStorageException {
    int count = 0;
    try {
      if (!Character.isDigit(firstCharacterOfName)) {
        String firstCharacterOfNameString = Character.toString(firstCharacterOfName);
        String firstCharacterOfNameLowerCaseQuery = firstCharacterOfNameString.toLowerCase() + PERCENT_STRING;
        Session session = sessionManager.getOrOpenSession();
        QueryBuilder query = new QueryBuilder(session);
        count = (int) query.select(NodeTypes.EXO_SPACE)
                            .equal(NodeProperties.SPACE_PENDING_USERS, userId)
                            .and()
                            .like(query.lower(NodeProperties.SPACE_DISPLAY_NAME), firstCharacterOfNameLowerCaseQuery)
                           .count();
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_PENDING_SPACES_BY_FIRST_CHARACTER_COUNT,
                                      e.getMessage(),
                                      e); 
    } finally {
      sessionManager.closeSession();
    }
    return count;
  }
  
  /**
   * Gets the public spaces of the user by filter with offset, limit.
   * 
   * @param userId
   * @param spaceFilter
   * @param offset
   * @param limit
   * @return
   * @since 1.2.0-GA
   */
  public List<Space> getPublicSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    String spaceNameSearchCondition = spaceFilter.getSpaceNameSearchCondition();
    if (spaceNameSearchCondition != null && spaceNameSearchCondition.length() != 0) {
      return this.getPublicSpacesBySearchCondition(userId, spaceNameSearchCondition, offset, limit);
    } 
    return this.getPublicSpacesByFirstCharacterOfName(userId, spaceFilter.getFirstCharacterOfSpaceName(), offset, limit);
  }
  
  
  /**
   * Gets the count of the public spaces of the user by space filter.
   * 
   * @param userId
   * @param spaceFilter
   * @return
   * @since 1.2.0-GA
   */
  public int getPublicSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    String spaceNameSearchCondition = spaceFilter.getSpaceNameSearchCondition();
    if (spaceNameSearchCondition != null && spaceNameSearchCondition.length() != 0) {
      return this.getPublicSpacesBySearchConditionCount(userId, spaceNameSearchCondition);
    } 
    return this.getPublicSpacesByFirstCharacterOfNameCount(userId, spaceFilter.getFirstCharacterOfSpaceName());
  }
  
  /**
   * Gets a user's public spaces and that user can request to join.
   * 
   * @param userId
   * @return spaces list in which the user can request to join.
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getPublicSpaces(String userId) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      List<Node> spaceNodes = null;
      Session session = sessionManager.getOrOpenSession();
      QueryBuilder query = new QueryBuilder(session);
      query.select(NodeTypes.EXO_SPACE);
      this.processPublicSpacesQuery(query, userId);
      spaceNodes = query.exec();
      for (Node spaceNode : spaceNodes) {
        spaceList.add(getSpaceFromNode(spaceNode, session));
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_PUBLIC_SPACES, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }

  
  /**
   * Gets a user's public spaces and that user can request to join with offset, limit.
   * 
   * @param userId
   * @param offset
   * @param limit
   * @return spaces list in which the user can request to join with offset, limit
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getPublicSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      List<Node> spaceNodes = null;
      Session session = sessionManager.getOrOpenSession();
      QueryBuilder query = new QueryBuilder(session);
      query.select(NodeTypes.EXO_SPACE, offset, limit);
      this.processPublicSpacesQuery(query, userId);
      spaceNodes = query.exec();
      for (Node spaceNode : spaceNodes) {
        spaceList.add(getSpaceFromNode(spaceNode, session));
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_PUBLIC_SPACES_WITH_OFFSET,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the count of the public spaces of the userId.
   * 
   * @param userId
   * @return the count of the spaces in which the user can request to join
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getPublicSpacesCount(String userId) throws SpaceStorageException {
    try {
      Session session = sessionManager.getOrOpenSession();
      QueryBuilder query = new QueryBuilder(session);
      query.select(NodeTypes.EXO_SPACE);
      this.processPublicSpacesQuery(query, userId);
      return (int) query.count();
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_PUBLIC_SPACES_COUNT, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
  }

  /**
   * Processes the query of public spaces.
   * 
   * @param query
   * @param userId
   * @since 1.2.0-GA
   */
  private void processPublicSpacesQuery(QueryBuilder query, String userId) {
    query.not()
            .equal(NodeProperties.SPACE_REGISTRATION, Space.PRIVATE)
          .and().not()
            .equal(NodeProperties.SPACE_VISIBILITY, Space.HIDDEN)
          .and().not()
            .equal(NodeProperties.SPACE_INVITED_USERS, userId)
          .and().not()
            .equal(NodeProperties.SPACE_MEMBERS, userId)
          .and().not()
            .equal(NodeProperties.SPACE_PENDING_USERS, userId)
          .and().not()
            .equal(NodeProperties.SPACE_MANAGERS, userId);
  }
  
  /**
   * Gets the public spaces that have name or description match input search condition with offset, limit.
   *
   * @param userId
   * @param searchCondition
   * @param offset
   * @param limit
   * @return a list of the spaces with offset, limit
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getPublicSpacesBySearchCondition(String userId,
                                                      String searchCondition,
                                                      long offset,
                                                      long limit) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      if (this.isValidInput(searchCondition)) {
        searchCondition = this.processSearchCondition(searchCondition);
        Session session = sessionManager.getOrOpenSession();
        List<Node> spaceNodes = null;
        QueryBuilder query = new QueryBuilder(session);
        query.select(NodeTypes.EXO_SPACE, offset, limit);
        this.processPublicSpacesBySearchConditionQuery(query, userId, searchCondition);
        spaceNodes = query.exec();
        for (Node spaceNode : spaceNodes) {
          spaceList.add(getSpaceFromNode(spaceNode, session));
        }
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_PUBLIC_SPACES_BY_SEARCH_CONDITION,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the count of the public spaces that have name or description match input search condition.
   *
   * @param searchCondition
   * @param offset
   * @param limit
   * @return the count of the spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getPublicSpacesBySearchConditionCount(String userId, String searchCondition) throws SpaceStorageException {
    int count = 0;
    try {
      if (this.isValidInput(searchCondition)) {
        searchCondition = this.processSearchCondition(searchCondition);
        Session session = sessionManager.getOrOpenSession();
        QueryBuilder query = new QueryBuilder(session);
        query.select(NodeTypes.EXO_SPACE);
        this.processPublicSpacesBySearchConditionQuery(query, userId, searchCondition);
        count = (int) query.count();
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_PUBLIC_SPACES_BY_SEARCH_CONDITION_COUNT,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return count;
  }
  
  /**
   * Processes the query of public spaces by search condition.
   * 
   * @param query
   * @param userId
   * @param searchCondition
   * @since 1.2.0-GA
   */
  private void processPublicSpacesBySearchConditionQuery(QueryBuilder query, String userId, String searchCondition) {
    if (searchCondition.indexOf(PERCENT_STRING) >= 0) {
      this.processPublicSpacesQuery(query, userId);
      query
        .and()
        .group()
          .like(NodeProperties.SPACE_DISPLAY_NAME, searchCondition)
          .or()
          .like(NodeProperties.SPACE_DESCRIPTION, searchCondition)
        .endGroup();
    } else {
      this.processPublicSpacesQuery(query, userId);
      query
        .and()
        .group()
          .contains(NodeProperties.SPACE_DISPLAY_NAME, searchCondition)
          .or()
          .contains(NodeProperties.SPACE_DESCRIPTION, searchCondition)
        .endGroup();
    }
  }
  
  /**
   * Gets the public spaces has the name starting with the input character with offset, limit.
   * 
   * @param userId
   * @param firstCharacterOfName
   * @param offset
   * @param limit
   * @return a list of the spaces with offset, limit
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getPublicSpacesByFirstCharacterOfName(String userId,
                                                           char firstCharacterOfName,
                                                           long offset,
                                                           long limit) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      if (!Character.isDigit(firstCharacterOfName)) {
        String firstCharacterOfNameString = Character.toString(firstCharacterOfName);
        String firstCharacterOfNameLowerCaseQuery = firstCharacterOfNameString.toLowerCase() + PERCENT_STRING;
        List<Node> spaceNodes = null;
        Session session = sessionManager.getOrOpenSession();
        QueryBuilder query = new QueryBuilder(session);
        query.select(NodeTypes.EXO_SPACE, offset, limit);
        this.processPublicSpacesByFirstCharacterOfNameQuery(query, userId, firstCharacterOfNameLowerCaseQuery);
        spaceNodes = query.exec();
        for (Node spaceNode : spaceNodes) {
          spaceList.add(getSpaceFromNode(spaceNode, session));
        }
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_PUBLIC_SPACES_BY_FIRST_CHARACTER,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the count of the public spaces has the name starting with the input character.
   * 
   * @param userId
   * @param firstCharacterOfName
   * @return the count of the spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getPublicSpacesByFirstCharacterOfNameCount(String userId, char firstCharacterOfName) throws SpaceStorageException {
    int count = 0;
    try {
      if (!Character.isDigit(firstCharacterOfName)) {
        String firstCharacterOfNameString = Character.toString(firstCharacterOfName);
        String firstCharacterOfNameLowerCaseQuery = firstCharacterOfNameString.toLowerCase() + PERCENT_STRING;
        Session session = sessionManager.getOrOpenSession();
        QueryBuilder query = new QueryBuilder(session);
        query.select(NodeTypes.EXO_SPACE);
        this.processPublicSpacesByFirstCharacterOfNameQuery(query, userId, firstCharacterOfNameLowerCaseQuery);
        count = (int) query.count();
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_PUBLIC_SPACES_BY_FIRST_CHARACTER_COUNT,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return count;
  }
  
  /**
   * Processes the query of public spaces by first character of name.
   * 
   * @param query
   * @param userId
   * @param firstCharacterOfNameLowerCaseQuery
   * @since 1.2.0-GA
   */
  private void processPublicSpacesByFirstCharacterOfNameQuery(QueryBuilder query, String userId,
                                                              String firstCharacterOfNameLowerCaseQuery) {
    this.processPublicSpacesQuery(query, userId);
    query.and()
        .like(query.lower(NodeProperties.SPACE_DISPLAY_NAME), firstCharacterOfNameLowerCaseQuery);
  }
  
  /**
   * Gets the member spaces of the user id by the filter with offset, limit.
   * 
   * @param userId
   * @param spaceFilter
   * @param offset
   * @param limit
   * @return
   * @since 1.2.0-GA
   */
  public List<Space> getMemberSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {
    String spaceNameSearchCondition = spaceFilter.getSpaceNameSearchCondition();
    if (spaceNameSearchCondition != null && !spaceNameSearchCondition.equals("")) {
      return this.getMemberSpacesBySpaceNameSearchCondition(userId, spaceNameSearchCondition, offset, limit);
    } 
    return this.getMemberSpacesByFirstCharacterOfSpaceName(userId, spaceFilter.getFirstCharacterOfSpaceName(), offset, limit);
  }
  
  /**
   * Gets the count of the spaces which user has "member" role by filter.
   * 
   * @param userId
   * @param spaceFilter
   * @return
   * @since 1.2.0-GA
   */
  public int getMemberSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    String spaceNameSearchCondition = spaceFilter.getSpaceNameSearchCondition();
    if (spaceNameSearchCondition != null && !spaceNameSearchCondition.equals("")) {
      return this.getMemberSpacesBySpaceNameSearchConditionCount(userId, spaceNameSearchCondition);
    } 
    return this.getMemberSpacesByFirstCharacterOfSpaceNameCount(userId, spaceFilter.getFirstCharacterOfSpaceName());
  }
  
  /**
   * Gets the spaces which user has "member" role by the space name search condition with offset, limit.
   * 
   * @param userId
   * @param spaceNameSearchCondition
   * @param offset
   * @param limit
   * @return
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getMemberSpacesBySpaceNameSearchCondition(String userId,
                                                               String spaceNameSearchCondition,
                                                               long offset,
                                                               long limit) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      if (this.isValidInput(spaceNameSearchCondition)) {
        spaceNameSearchCondition = this.processSearchCondition(spaceNameSearchCondition);
        Session session = sessionManager.getOrOpenSession();
        List<Node> spaceNodes = null;
        if (spaceNameSearchCondition.indexOf(PERCENT_STRING) >= 0) {
          spaceNodes = new QueryBuilder(session)
                            .select(NodeTypes.EXO_SPACE, offset, limit)
                              .equal(NodeProperties.SPACE_MEMBERS, userId)
                              .and()
                              .group()
                                .like(NodeProperties.SPACE_DISPLAY_NAME, spaceNameSearchCondition)
                                .or()
                                .like(NodeProperties.SPACE_DESCRIPTION, spaceNameSearchCondition)
                              .endGroup()
                            .exec();
        } else {
          spaceNodes = new QueryBuilder(session)
                            .select(NodeTypes.EXO_SPACE, offset, limit)
                              .equal(NodeProperties.SPACE_MEMBERS, userId)
                              .and()
                              .group()
                                .contains(NodeProperties.SPACE_DISPLAY_NAME, spaceNameSearchCondition)
                                .or()
                                .contains(NodeProperties.SPACE_DESCRIPTION, spaceNameSearchCondition)
                              .endGroup()
                            .exec();
        }
        for (Node spaceNode : spaceNodes) {
          spaceList.add(getSpaceFromNode(spaceNode, session));
        }
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_MEMBER_SPACES_BY_SEARCH_CONDITION,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the count of the spaces which user has "member" role by space name search condition.
   * 
   * @param userId
   * @param spaceNameSearchCondition
   * @return
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getMemberSpacesBySpaceNameSearchConditionCount(String userId,
                                                            String spaceNameSearchCondition) throws SpaceStorageException {
    int count = 0;
    try {
      if (this.isValidInput(spaceNameSearchCondition)) {
        spaceNameSearchCondition = this.processSearchCondition(spaceNameSearchCondition);
        Session session = sessionManager.getOrOpenSession();
        if (spaceNameSearchCondition.indexOf(PERCENT_STRING) >= 0) {
          count = (int) new QueryBuilder(session)
                            .select(NodeTypes.EXO_SPACE)
                              .equal(NodeProperties.SPACE_MEMBERS, userId)
                              .and()
                              .group()
                                .like(NodeProperties.SPACE_DISPLAY_NAME, spaceNameSearchCondition)
                                .or()
                                .like(NodeProperties.SPACE_DESCRIPTION, spaceNameSearchCondition)
                              .endGroup()
                            .count();
        } else {
          count = (int) new QueryBuilder(session)
                            .select(NodeTypes.EXO_SPACE)
                              .equal(NodeProperties.SPACE_MEMBERS, userId)
                              .and()
                              .group()
                                .contains(NodeProperties.SPACE_DISPLAY_NAME, spaceNameSearchCondition)
                                .or()
                                .contains(NodeProperties.SPACE_DESCRIPTION, spaceNameSearchCondition)
                              .endGroup()
                            .count();
        }
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_MEMBER_SPACES_BY_SEARCH_CONDITION_COUNT,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return count;
  }
  
  /**
   * Gets the spaces which user has "member" role by first character of space name with offset, limit.
   * 
   * @param userId
   * @param firstCharacterOfSpaceName
   * @param offset
   * @param limit
   * @return
   * @since 1.2.0-GA
   */
  public List<Space> getMemberSpacesByFirstCharacterOfSpaceName(String userId,
                                                                char firstCharacterOfSpaceName,
                                                                long offset,
                                                                long limit) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      if (!Character.isDigit(firstCharacterOfSpaceName)) {
        List<Node> spaceNodes = null;
        String firstCharacterOfSpaceNameString = Character.toString(firstCharacterOfSpaceName);
        String firstCharacterOfSpaceNameQuery = firstCharacterOfSpaceNameString.toLowerCase() + PERCENT_STRING;
        Session session = sessionManager.getOrOpenSession();
        QueryBuilder query = new QueryBuilder(session);
        spaceNodes = query.select(NodeTypes.EXO_SPACE, offset, limit)
                            .equal(NodeProperties.SPACE_MEMBERS, userId)
                            .and()
                            .like(query.lower(NodeProperties.SPACE_DISPLAY_NAME), firstCharacterOfSpaceNameQuery)
                          .exec();
        for (Node spaceNode : spaceNodes) {
          spaceList.add(getSpaceFromNode(spaceNode, session));
        }
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_MEMBER_SPACES_BY_FIRST_CHARACTER,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the count of the spaces which user has "member" role by first character of space name.
   * 
   * @param userId
   * @param firstCharacterOfSpaceName
   * @return
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getMemberSpacesByFirstCharacterOfSpaceNameCount(String userId,
                                                             char firstCharacterOfSpaceName) throws SpaceStorageException {
    int count = 0;
    try {
      if (!Character.isDigit(firstCharacterOfSpaceName)) {
        String firstCharacterOfSpaceNameString = Character.toString(firstCharacterOfSpaceName);
        String firstCharacterOfSpaceNameQuery = firstCharacterOfSpaceNameString.toLowerCase() + PERCENT_STRING;
        Session session = sessionManager.getOrOpenSession();
        QueryBuilder query = new QueryBuilder(session);
        count = (int) query.select(NodeTypes.EXO_SPACE)
                            .equal(NodeProperties.SPACE_MEMBERS, userId)
                            .and()
                            .like(query.lower(NodeProperties.SPACE_DISPLAY_NAME), firstCharacterOfSpaceNameQuery)
                           .count();
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_MEMBER_SPACES_BY_FIRST_CHARACTER_COUNT,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return count;
  }
  
  /**
   * Gets the spaces that a user has the "member" role.
   * 
   * @param userId
   * @return a list of the member spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getMemberSpaces(String userId) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      List<Node> spaceNodes = null;
      Session session = sessionManager.getOrOpenSession();
      spaceNodes = new QueryBuilder(session)
                      .select(NodeTypes.EXO_SPACE)
                        .equal(NodeProperties.SPACE_MEMBERS, userId)
                      .exec();
      for (Node spaceNode : spaceNodes) {
        spaceList.add(getSpaceFromNode(spaceNode, session));
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_MEMBER_SPACES, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the spaces that a user has the "member" role with offset, limit.
   * 
   * @param userId
   * @param offset
   * @param limit
   * @return a list of the member spaces with offset, limit
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getMemberSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      List<Node> spaceNodes = null;
      Session session = sessionManager.getOrOpenSession();
      spaceNodes = new QueryBuilder(session)
                      .select(NodeTypes.EXO_SPACE, offset, limit)
                        .equal(NodeProperties.SPACE_MEMBERS, userId)
                      .exec();
      for (Node spaceNode : spaceNodes) {
        spaceList.add(getSpaceFromNode(spaceNode, session));
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_MEMBER_SPACES_WITH_OFFSET,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the count of the spaces that a user has the "member" role.
   * 
   * @param userId
   * @return the count of the member spaces
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public int getMemberSpacesCount(String userId) throws SpaceStorageException {
    try {
      Session session = sessionManager.getOrOpenSession();
      return (int) new QueryBuilder(session)
                        .select(NodeTypes.EXO_SPACE)
                          .equal(NodeProperties.SPACE_MEMBERS, userId)
                        .count();
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_MEMBER_SPACES_COUNT, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
  }
  
  /**
   * Gets a space by its associated group id.
   *
   * @param  groupId
   * @return the space that has group id matching the groupId string input.
   * @throws SpaceStorageException
   * @since  1.2.0-GA
   */
  public Space getSpaceByGroupId(String groupId) throws SpaceStorageException {
    Space space = null;
    try {
      Session session = sessionManager.getOrOpenSession();
      Node foundNode = new QueryBuilder(session)
              .select(NodeTypes.EXO_SPACE)
              .equal(NodeProperties.SPACE_GROUP_ID, groupId).findNode();
      if (foundNode != null) {
        space = getSpaceFromNode(foundNode, session);
      } else {
        LOG.warn("Not found node with groupId: " + groupId);
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_SPACE_BY_GROUP_ID, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
    return space;
  }

  /**
   * Gets a space by its space id.
   *
   * @param id
   * @return space with id specified
   * @throws SpaceStorageException
   */
  public Space getSpaceById(String id) throws SpaceStorageException {
    Space cachedSpace = spaceCacheById.get(id);
    if (cachedSpace != null) {
      return cachedSpace;
    }
    try {
      Session session = sessionManager.getOrOpenSession();
      Node spaceNode = session.getNodeByUUID(id);
      if (spaceNode != null) {
        return getSpaceFromNode(spaceNode, session);
      } else {
        LOG.warn("No node found for space: " + id);
        return null;
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_SPACE_BY_ID, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
  }
  
  /**
   * Gets the spaces that have name or description match input search condition.
   * 
   * @param searchCondition
   * @return a list of the spaces that match the search condition
   * @throws SpaceStorageException the exception
   */
  public List<Space> getSpacesBySearchCondition(String searchCondition) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      if (this.isValidInput(searchCondition)) {
        List<Node> spaceNodes = null;
        Session session = sessionManager.getOrOpenSession();
        searchCondition = this.processSearchCondition(searchCondition);
        if (searchCondition.indexOf(PERCENT_STRING) >= 0) {
          spaceNodes = new QueryBuilder(session)
                          .select(NodeTypes.EXO_SPACE)
                            .like(NodeProperties.SPACE_DISPLAY_NAME, searchCondition)
                            .or()
                            .like(NodeProperties.SPACE_DESCRIPTION, searchCondition)
                          .exec();
        } else {
          spaceNodes = new QueryBuilder(session)
                          .select(NodeTypes.EXO_SPACE)
                            .contains(NodeProperties.SPACE_DISPLAY_NAME, searchCondition)
                            .or()
                            .contains(NodeProperties.SPACE_DESCRIPTION, searchCondition)
                          .exec();
        }
        for (Node spaceNode : spaceNodes) {
          spaceList.add(getSpaceFromNode(spaceNode, session));
        }
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_SPACES_BY_SEARCH_CONDITION, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets the spaces has the name starting with the input character.
   * 
   * @param firstCharacterOfName
   * @return the spaces which have first character of name that matches the input string.
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public List<Space> getSpacesByFirstCharacterOfName(String firstCharacterOfName) throws SpaceStorageException {
    List<Space> spaceList = new ArrayList<Space>();
    try {
      if (!Character.isDigit(firstCharacterOfName.charAt(0))) {
        List<Node> spaceNodes = null;
        String firstCharacterOfNameLowerCaseQuery = firstCharacterOfName.toLowerCase() + PERCENT_STRING;
        Session session = sessionManager.getOrOpenSession();
        QueryBuilder query = new QueryBuilder(session);
        spaceNodes = query.select(NodeTypes.EXO_SPACE)
                            .like(query.lower(NodeProperties.SPACE_DISPLAY_NAME), firstCharacterOfNameLowerCaseQuery)
                          .exec();
        for (Node spaceNode : spaceNodes) {
          spaceList.add(getSpaceFromNode(spaceNode, session));
        }
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_SPACES_BY_FIRST_CHARACTER,
                                      e.getMessage(),
                                      e);
    } finally {
      sessionManager.closeSession();
    }
    return spaceList;
  }
  
  /**
   * Gets a space by its display name.
   *
   * @param spaceDisplayName
   * @return the space with spaceDisplayName that matches the spaceDisplayName input.
   * @since  1.2.0-GA
   * @throws SpaceStorageException
   */
  public Space getSpaceByDisplayName(String spaceDisplayName) throws SpaceStorageException {
    Space space = null;
    try {
      Session session = sessionManager.getOrOpenSession();
      Node foundNode = new QueryBuilder(session)
              .select(NodeTypes.EXO_SPACE)
              .equal(NodeProperties.SPACE_DISPLAY_NAME, spaceDisplayName).findNode();
      if (foundNode != null) {
        space = this.getSpaceFromNode(foundNode, session);
      } else {
        LOG.warn("Not found node: " + spaceDisplayName);
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_SPACE_BY_DISPLAY_NAME, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
    return space;
  }

  /**
   * Gets a space by its space name.
   *
   * @param spaceName
   * @return the stored space
   * @throws SpaceStorageException
   * @deprecated Use {@link SpaceStorage#getSpaceByPrettyName(String)} instead.
   *             Will be removed at 1.3.x
   */
  public Space getSpaceByName(String spaceName) throws SpaceStorageException {
    try {
      return getSpaceByPrettyName(spaceName);
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_SPACE_BY_NAME, e.getMessage(), e);
    }
  }

  /**
   * Gets a space by its pretty name.
   *
   * @param spacePrettyName
   * @return the space with spacePrettyName that matches spacePrettyName input.
   * @throws SpaceStorageException
   * @since 1.2.0-GA
   */
  public Space getSpaceByPrettyName(String spacePrettyName) throws SpaceStorageException {
    Space space = null;
    try {
      Session session = sessionManager.getOrOpenSession();
      Node foundNodeSpace = new QueryBuilder(session)
              .select(NodeTypes.EXO_SPACE)
              .equal(NodeProperties.SPACE_PRETTY_NAME, spacePrettyName)
              .findNode();
      if (foundNodeSpace != null) {
        space = this.getSpaceFromNode(foundNodeSpace, session);
      } else {
        LOG.warn("Node not found for spacePrettyName:  " + spacePrettyName);
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_SPACE_BY_PRETTY_NAME, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
    return space;
  }

  /**
   * Gets a space by its url.
   *
   * @param url
   * @return the space with string url specified
   * @throws SpaceStorageException
   */
  public Space getSpaceByUrl(String url) throws SpaceStorageException {
    Space space = null;
    try {
      Session session = sessionManager.getOrOpenSession();
      Node foundNode = new QueryBuilder(session)
              .select(NodeTypes.EXO_SPACE)
              .equal(NodeProperties.SPACE_URL, url).findNode();
      if (foundNode != null) {
        space = this.getSpaceFromNode(foundNode, session);
      } else {
        LOG.warn("No node found for url: " + url);
      }
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_SPACE_BY_URL, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
    return space;
  }
  
  /**
   * Deletes a space by space id.
   *
   * @param id
   * @throws SpaceStorageException
   */
  public void deleteSpace(String id) throws SpaceStorageException {
    try {
      Session session = sessionManager.getOrOpenSession();
      Node spaceNode = session.getNodeByUUID(id);
      if (spaceNode != null) {
        spaceNode.remove();
        session.save();
      } else {
        LOG.warn("Failed to find a spaceNode by its id: " + id);
      }
      spaceCacheById.remove(id);
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_DELETE_SPACE, e.getMessage(), e);
    } finally {
      sessionManager.closeSession(true);
    }
  }

  /**
   * Saves a space. If isNew is true, creates new space. If not only updates space
   * an saves it.
   *
   * @param space
   * @param isNew
   * @throws SpaceStorageException
   */
  public void saveSpace(Space space, boolean isNew) throws SpaceStorageException {
    try {
      Session session = sessionManager.getOrOpenSession();
      spaceHomeNode = getSpaceHomeNode(session);
      saveSpace(spaceHomeNode, space, isNew, session);
    } catch (Exception e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_SAVE_SPACE, e.getMessage(), e);
    } finally {
      sessionManager.closeSession(true);
    }
  }

  /**
   * Creates a new space. If isNew is true, creates a new space. If not only
   * updates space an saves it.
   *
   * @param space
   * @param isNew
   */
  private void saveSpace(Node spaceHomeNode, Space space, boolean isNew, Session session) {
    Node spaceNode;
    try {
      if (isNew) {
        spaceNode = spaceHomeNode.addNode(NodeTypes.EXO_SPACE, NodeTypes.EXO_SPACE);
        spaceNode.addMixin(NodeTypes.MIX_REFERENCEABLE);
      } else {
        spaceNode = session.getNodeByUUID(space.getId());
      }
      if (space.getId() == null) {
        space.setId(spaceNode.getUUID());
      }
      setNodeFromSpace(space, spaceNode);
      //  save image to contact
      AvatarAttachment attachment = space.getAvatarAttachment();
      if (attachment != null) {
        // fix load image on IE6 UI
        ExtendedNode extNode = (ExtendedNode) spaceNode;
        if (extNode.canAddMixin(NodeTypes.EXO_PRIVILEGEABLE)) {
          extNode.addMixin(NodeTypes.EXO_PRIVILEGEABLE);
        }
        String[] arrayPers = {PermissionType.READ, PermissionType.ADD_NODE, PermissionType.SET_PROPERTY, PermissionType.REMOVE};
        extNode.setPermission(SystemIdentity.ANY, arrayPers);
        List<AccessControlEntry> permsList = extNode.getACL().getPermissionEntries();
        for (AccessControlEntry accessControlEntry : permsList) {
          extNode.setPermission(accessControlEntry.getIdentity(), arrayPers);
        }

        if (attachment.getFileName() != null) {
          Node nodeFile = null;
          try {
            nodeFile = spaceNode.getNode(IMAGE_PATH);
          } catch (PathNotFoundException e) {
            nodeFile = spaceNode.addNode(IMAGE_PATH, NodeTypes.NT_FILE);
          }
          Node nodeContent = null;
          try {
            nodeContent = nodeFile.getNode(NodeProperties.JCR_CONTENT);
          } catch (PathNotFoundException e) {
            nodeContent = nodeFile.addNode(NodeProperties.JCR_CONTENT, NodeTypes.NT_RESOURCE);
          }
          long lastModified = attachment.getLastModified();
          long lastSaveTime = 0;
          if (nodeContent.hasProperty(NodeProperties.JCR_LAST_MODIFIED)) {
            lastSaveTime = nodeContent.getProperty(NodeProperties.JCR_LAST_MODIFIED).getLong();
          }
          if ((lastModified != 0) && (lastModified != lastSaveTime)) {
            nodeContent.setProperty(NodeProperties.JCR_MIME_TYPE, attachment.getMimeType());
            nodeContent.setProperty(NodeProperties.JCR_DATA, attachment.getInputStream(session));
            nodeContent.setProperty(NodeProperties.JCR_LAST_MODIFIED, attachment.getLastModified());
          }
        }
      } else {
        if (spaceNode.hasNode(IMAGE_PATH)) {
          spaceNode.getNode(IMAGE_PATH).remove();
          // add 12DEC
          session.save();
        }
      }
      if (isNew) {
        spaceHomeNode.save();
      } else {
        spaceNode.save();
        spaceCacheById.remove(spaceNode.getUUID());
      }
    } catch (Exception e) {
      LOG.error("Failed to save space", e);
    } finally {
      sessionManager.closeSession();
    }
  }
  
  private void setNodeFromSpace(Space space, Node spaceNode) throws RepositoryException {
    spaceNode.setProperty(NodeProperties.SPACE_DISPLAY_NAME, space.getDisplayName());
    spaceNode.setProperty(NodeProperties.SPACE_GROUP_ID, space.getGroupId());
    spaceNode.setProperty(NodeProperties.SPACE_APP, space.getApp());
    spaceNode.setProperty(NodeProperties.SPACE_PARENT, space.getParent());
    spaceNode.setProperty(NodeProperties.SPACE_DESCRIPTION, space.getDescription());
    spaceNode.setProperty(NodeProperties.SPACE_TAG, space.getTag());
    spaceNode.setProperty(NodeProperties.SPACE_PENDING_USERS, space.getPendingUsers());
    spaceNode.setProperty(NodeProperties.SPACE_INVITED_USERS, space.getInvitedUsers());
    spaceNode.setProperty(NodeProperties.SPACE_TYPE, space.getType());
    spaceNode.setProperty(NodeProperties.SPACE_URL, space.getUrl());
    spaceNode.setProperty(NodeProperties.SPACE_VISIBILITY, space.getVisibility());
    spaceNode.setProperty(NodeProperties.SPACE_REGISTRATION, space.getRegistration());
    spaceNode.setProperty(NodeProperties.SPACE_PRIORITY, space.getPriority());
    spaceNode.setProperty(NodeProperties.SPACE_PRETTY_NAME, space.getPrettyName());
    spaceNode.setProperty(NodeProperties.SPACE_AVATAR_URL, space.getAvatarUrl());
    spaceNode.setProperty(NodeProperties.SPACE_CREATOR, space.getCreator());
    spaceNode.setProperty(NodeProperties.SPACE_MANAGERS, space.getManagers());
    spaceNode.setProperty(NodeProperties.SPACE_MEMBERS, space.getMembers());
  }
  
  /**
   * Gets the space from space node.
   *
   * @param spaceNode
   * @param session
   * @return the space
   * @throws SpaceException
   */
  private Space getSpaceFromNode(Node spaceNode, Session session) throws SpaceException {
    Space space = null;
    try {
      String id = spaceNode.getUUID();

      space = spaceCacheById.get(id);
      if (space != null) {
        return space;
      }
      space = new Space();
      space.setId(spaceNode.getUUID());
      PropertyIterator itr = spaceNode.getProperties(SPACE_PROPERTIES_NAME_PATTERN);
      while (itr.hasNext()) {
        Property p = itr.nextProperty();
        String propertyName = p.getName();
        if (NodeProperties.SPACE_DISPLAY_NAME.equals(propertyName)) {
          space.setDisplayName(p.getString());
        } else if (NodeProperties.SPACE_GROUP_ID.equals(propertyName)) {
          space.setGroupId(p.getString());
        } else if (NodeProperties.SPACE_APP.equals(propertyName)) {
          space.setApp(p.getString());
        } else if (NodeProperties.SPACE_PARENT.equals(propertyName)) {
          space.setParent(p.getString());
        } else if (NodeProperties.SPACE_DESCRIPTION.equals(propertyName)) {
          space.setDescription(p.getString());
        } else if (NodeProperties.SPACE_TAG.equals(propertyName)) {
          space.setTag(p.getString());
        } else if (NodeProperties.SPACE_PENDING_USERS.equals(propertyName)) {
            space.setPendingUsers(Util.convertValuesToStrings(p.getValues()));
        } else if (NodeProperties.SPACE_INVITED_USERS.equals(propertyName)) {
          space.setInvitedUsers(Util.convertValuesToStrings(p.getValues()));
        } else if (NodeProperties.SPACE_TYPE.equals(propertyName)) {
          space.setType(p.getString());
        } else if (NodeProperties.SPACE_URL.equals(propertyName)) {
          space.setUrl(p.getString());
        } else if (NodeProperties.SPACE_VISIBILITY.equals(propertyName)) {
          space.setVisibility(p.getString());
        } else if (NodeProperties.SPACE_REGISTRATION.equals(propertyName)) {
          space.setRegistration(p.getString());
        } else if (NodeProperties.SPACE_PRIORITY.equals(propertyName)) {
          space.setPriority(p.getString());
        } else if (NodeProperties.SPACE_PRETTY_NAME.equals(propertyName)) {
          space.setPrettyName(p.getString());
        } else if (NodeProperties.SPACE_AVATAR_URL.equals(propertyName)) {
          space.setAvatarUrl(p.getString());
        }  else if(NodeProperties.SPACE_CREATOR.equals(propertyName)) {
          space.setCreator(p.getString());
        } else if (NodeProperties.SPACE_MANAGERS.equals(propertyName)) {
          space.setManagers(Util.convertValuesToStrings(p.getValues()));
        } else if (NodeProperties.SPACE_MEMBERS.equals(propertyName)) {
          space.setMembers(Util.convertValuesToStrings(p.getValues()));
        }
      }
      if (spaceNode.hasNode(IMAGE_PATH)) {
        Node image = spaceNode.getNode(IMAGE_PATH);
        if (image.isNodeType(NodeTypes.NT_FILE)) {
          AvatarAttachment file = new AvatarAttachment();
          file.setId(image.getPath());
          file.setMimeType(image.getNode(NodeProperties.JCR_CONTENT).getProperty(NodeProperties.JCR_MIME_TYPE).getString());
          try {
            file.setInputStream(image.getNode(NodeProperties.JCR_CONTENT)
                                     .getProperty(NodeProperties.JCR_DATA)
                                     .getValue().getStream());
          } catch (Exception e) {
            LOG.warn("Failed to setInputStream in space", e);
          }
          file.setFileName(image.getName());
          file.setLastModified(image.getNode(NodeProperties.JCR_CONTENT).getProperty(NodeProperties.JCR_LAST_MODIFIED).getLong());
          file.setWorkspace(session.getWorkspace().getName());
          space.setAvatarAttachment(file);
        }
      }
      spaceCacheById.put(id, space);
    } catch (Exception e) {
      LOG.warn("Failed to getSpaceFromNode", e);
    }
    return space;
  }

  /**
   * Gets the space home node.
   * 
   * @param session
   * @return
   * @throws SpaceException
   */
  private Node getSpaceHomeNode(Session session) throws SpaceException {
    try {
      if (spaceHomeNode == null) {
        String path = dataLocation.getSocialSpaceHome();
        Util.createNodes(session.getRootNode(), path);
        spaceHomeNode = session.getRootNode().getNode(path);
      }
    } catch (Exception e) {
      LOG.warn("Failed to getSpaceHomeNode", e);
    }
    return spaceHomeNode;
  }
  
  /**
   * Processes the search condition.
   * 
   * @param searchCondition
   * @return
   * @since 1.2.0-GA
   */
  private String processSearchCondition(String searchCondition) {
    StringBuffer searchConditionBuffer = new StringBuffer();
    if (searchCondition.indexOf(ASTERISK_STRING) < 0 && searchCondition.indexOf(PERCENT_STRING) < 0) {
      if (searchCondition.charAt(0) != ASTERISK_CHARACTER) {
        searchConditionBuffer.append(ASTERISK_STRING).append(searchCondition);
      }
      if (searchCondition.charAt(searchCondition.length() - 1) != ASTERISK_CHARACTER) {
        searchConditionBuffer.append(ASTERISK_STRING);
      }
    } else {
      searchCondition = searchCondition.replace(ASTERISK_STRING, PERCENT_STRING);
      searchConditionBuffer.append(PERCENT_STRING).append(searchCondition).append(PERCENT_STRING);
    }
    return searchConditionBuffer.toString();
  }
  
  /**
   * Validate the input string.
   *
   * @param input
   *        A {@code String}
   *
   * @return true if user input a right string for space searching else return false.
   */
  private boolean isValidInput(String input) {
    if (input == null) {
      return false;
    }
    String cleanString = input.replaceAll("\\*", "");
    cleanString = cleanString.replaceAll("\\%", "");
    if (cleanString.length() > 0 && Character.isDigit(cleanString.charAt(0))) {
       return false;
    } else if (cleanString.length() == 0) {
      return false;
    }
    return true; 
  }
}