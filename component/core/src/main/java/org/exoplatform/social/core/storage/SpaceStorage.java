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
                  NodeProperties.SPACE_VISIBILITY
          }
  );
  private SocialDataLocation dataLocation;
  private JCRSessionManager sessionManager;
  private static final String IMAGE_PATH = "image";
  private Node spaceHomeNode;
  /**
   * The cache for the spaces; cache key is spaceId.
   */
  private final ExoCache<String, Space> spaceCache;

  /**
   * Constructor.
   *
   * @param dataLocation
   */
  public SpaceStorage(SocialDataLocation dataLocation, CacheService cacheService) {
    this.dataLocation = dataLocation;
    this.sessionManager = dataLocation.getSessionManager();
    this.spaceCache = cacheService.getCacheInstance(getClass().getName() + "spaceCache");
  }

  /**
   * Gets all the spaces.
   *
   * @return the all spaces
   */
  public List<Space> getAllSpaces() {
    List<Space> spaces = new ArrayList<Space>();
    try {
      Session session = sessionManager.getOrOpenSession();
      List<Node> spacesNode = new QueryBuilder(session)
              .select(NodeTypes.EXO_SPACE)
              .exec();
      for (Node node : spacesNode) {
        Space space = this.getSpaceFromNode(node, session);
        spaces.add(space);
      }
      return spaces;
    } catch (Exception e) {
      LOG.warn("Failed to getAllSpaces()", e);
      return spaces;
    } finally {
      sessionManager.closeSession();
    }
  }

  /**
   * Gets a space by its associated group id.
   *
   * @param  groupId
   * @return
   * @throws SpaceException
   * @since  1.2.0-GA
   */
  public Space getSpaceByGroupId(String groupId) throws SpaceException {
    Session session = sessionManager.getOrOpenSession();
    Space space = null;
    try {
      Node foundNode = new QueryBuilder(session)
              .select(NodeTypes.EXO_SPACE)
              .equal(NodeProperties.SPACE_GROUP_ID, groupId).findNode();
      if (foundNode != null) {
        space = getSpaceFromNode(foundNode, session);
      } else {
        LOG.warn("Not found node with groupId: " + groupId);
      }
    } catch (Exception e) {
      LOG.warn("Failed to getSpaceByGroupId with groupId: " + groupId, e);
    } finally {
      sessionManager.closeSession();
    }
    return space;
  }

  /**
   * Gets a space by its space id.
   *
   * @param id
   * @return
   */
  public Space getSpaceById(String id) {
    Space cachedSpace = spaceCache.get(id);
    if (cachedSpace != null) {
      return cachedSpace;
    }

    Session session = sessionManager.getOrOpenSession();
    try {
      Node spaceNode = session.getNodeByUUID(id);
      if (spaceNode != null) {
        return getSpaceFromNode(spaceNode, session);
      }
    } catch (Exception e) {
      LOG.warn("Failed to getSpaceById: " + id, e);
      return null;
    } finally {
      sessionManager.closeSession();
    }
    LOG.warn("No node found for space: " + id);
    return null;
  }

  /**
   * Gets all spaces that have name or description match input condition.
   *
   * @param condition
   * @return the identities by profile filter
   * @throws Exception the exception
   */
  public List<Space> getSpacesBySearchCondition(String condition) throws Exception {
    Session session = sessionManager.getOrOpenSession();
    List<Space> spaceList = new ArrayList<Space>();
    List<Node> spaceNodes = null;
    try {
      if (condition.length() != 0) {
        if (condition.indexOf("*") < 0) {
          if (condition.charAt(0) != '*') {
            condition = "*" + condition;
          }
          if (condition.charAt(condition.length() - 1) != '*') {
            condition += "*";
          }
        }
        spaceNodes = new QueryBuilder(session)
                .select(NodeTypes.EXO_SPACE)
                .contains(NodeProperties.SPACE_DISPLAY_NAME, condition)
                .or()
                .contains(NodeProperties.SPACE_DESCRIPTION, condition)
                .exec();
      } else {
        //TODO if no condition defined, it's better to return an empty list
        spaceList = getAllSpaces();
      }
    } catch (Exception e) {
      LOG.warn("Failed to getSpacesBySearchCondition with condition: " + condition, e);
      return spaceList;
    } finally {
      sessionManager.closeSession();
    }
    for (Node spaceNode : spaceNodes) {
      Space space = getSpaceFromNode(spaceNode, session);
      if (space != null) {
        spaceList.add(space);
      }
    }
    return spaceList;
  }

  /**
   * Gets a space by its display name.
   *
   * @param spaceDisplayName
   * @return the space
   * @since  1.2.0-GA
   */
  public Space getSpaceByDisplayName(String spaceDisplayName) {
    Session session = sessionManager.getOrOpenSession();
    Space space = null;
    try {
      Node foundNode = new QueryBuilder(session)
              .select(NodeTypes.EXO_SPACE)
              .equal(NodeProperties.SPACE_DISPLAY_NAME, spaceDisplayName).findNode();
      if (foundNode != null) {
        space = this.getSpaceFromNode(foundNode, session);
      } else {
        LOG.warn("Not found node: " + spaceDisplayName);
      }
    } catch (Exception e) {
      LOG.warn("Failed to getSpaceByDisplayName with displayName: " + spaceDisplayName, e);
    } finally {
      sessionManager.closeSession();
    }
    return space;
  }

  /**
   * Gets a space by its space name.
   *
   * @param spaceName
   * @return
   * @throws Exception
   * @deprecated Use {@link SpaceStorage#getSpaceByPrettyName(String)} instead.
   *             Will be removed at 1.3.x
   */
  public Space getSpaceByName(String spaceName) throws Exception {
    return getSpaceByPrettyName(spaceName);
  }

  /**
   * Gets a space by its pretty name.
   *
   * @param spacePrettyName
   * @return
   * @since 1.2.0-GA
   */
  public Space getSpaceByPrettyName(String spacePrettyName) throws Exception {
    Session session = sessionManager.getOrOpenSession();
    Space space = null;
    try {
      Node foundNodeSpace = new QueryBuilder(session)
              .select(NodeTypes.EXO_SPACE)
              .equal(NodeProperties.SPACE_PRETTY_NAME, spacePrettyName)
              .findNode();
      if (foundNodeSpace != null) {
        space = this.getSpaceFromNode(foundNodeSpace, session);
      } else {
        LOG.warn("Node not found for spacePrettyName:  " + spacePrettyName);
      }
    } catch (Exception ex) {
      LOG.warn("Failed to getSpaceByName: " + ex.getMessage(), ex);
    } finally {
      sessionManager.closeSession();
    }
    return space;
  }

  /**
   * Gets a space by its url.
   *
   * @param url
   * @return the space
   */
  public Space getSpaceByUrl(String url) {
    Session session = sessionManager.getOrOpenSession();
    Space space = null;

    try {
      Node foundNode = new QueryBuilder(session)
              .select(NodeTypes.EXO_SPACE)
              .equal(NodeProperties.SPACE_URL, url).findNode();
      if (foundNode != null) {
        space = this.getSpaceFromNode(foundNode, session);
      } else {
        LOG.warn("No node found for url: " + url);
      }
    } catch (Exception e) {
      LOG.warn("Failed to getSpaceByUrl: " + url, e);
    } finally {
      sessionManager.closeSession();
    }
    return space;
  }

  /**
   * Deletes a space by space id.
   *
   * @param id
   */
  public void deleteSpace(String id) {
    Session session = sessionManager.getOrOpenSession();
    try {
      Node spaceNode = session.getNodeByUUID(id);
      if (spaceNode != null) {
        spaceNode.remove();
      } else {
        LOG.warn("Failed to find a spaceNode by its id: " + id);
      }
    } catch (Exception e) {
      LOG.error("Failed to delete a space by its id: " + id, e);
    } finally {
      sessionManager.closeSession(true);
    }
  }

  /**
   * Saves a space. If isNew is true, creat new space. If not only updates space
   * an saves it.
   *
   * @param space
   * @param isNew
   */
  public void saveSpace(Space space, boolean isNew) {
    try {
      Session session = sessionManager.getOrOpenSession();
      spaceHomeNode = getSpaceHomeNode(session);
      saveSpace(spaceHomeNode, space, isNew, session);
    } catch (Exception e) {
      LOG.warn("Failed to saveSpace", e);
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
          } catch (PathNotFoundException ex) {
            nodeFile = spaceNode.addNode(IMAGE_PATH, NodeTypes.NT_FILE);
          }
          Node nodeContent = null;
          try {
            nodeContent = nodeFile.getNode(NodeProperties.JCR_CONTENT);
          } catch (PathNotFoundException ex) {
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
        spaceCache.remove(spaceNode.getUUID());
      }
    } catch (Exception e) {
      LOG.error("failed to save space", e);
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
  }

  /**
   * Gets the space from space node.
   *
   * @param spaceNode
   * @param session
   * @return the space
   * @throws Exception
   */
  private Space getSpaceFromNode(Node spaceNode, Session session) throws Exception {
    String id = spaceNode.getUUID();

    Space space = spaceCache.get(id);
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
      }
    }
    if (spaceNode.hasNode(IMAGE_PATH)) {
      Node image = spaceNode.getNode(IMAGE_PATH);
      if (image.isNodeType(NodeTypes.NT_FILE)) {
        AvatarAttachment file = new AvatarAttachment();
        file.setId(image.getPath());
        file.setMimeType(image.getNode(NodeProperties.JCR_CONTENT).getProperty(NodeProperties.JCR_MIME_TYPE).getString());
        try {
          file.setInputStream(image.getNode(NodeProperties.JCR_CONTENT).getProperty(NodeProperties.JCR_DATA).getValue().getStream());
        } catch (Exception ex) {
          LOG.warn("Failed to setInputStream in space", ex);
        }
        file.setFileName(image.getName());
        file.setLastModified(image.getNode(NodeProperties.JCR_CONTENT).getProperty(NodeProperties.JCR_LAST_MODIFIED).getLong());
        file.setWorkspace(session.getWorkspace().getName());
        space.setAvatarAttachment(file);
      }
    }
    spaceCache.put(id, space);
    return space;
  }

  private Node getSpaceHomeNode(Session session) throws Exception {
    if (spaceHomeNode == null) {
      String path = dataLocation.getSocialSpaceHome();
      spaceHomeNode = session.getRootNode().getNode(path);
    }
    return spaceHomeNode;
  }
}