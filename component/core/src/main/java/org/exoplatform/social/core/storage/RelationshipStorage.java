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
package org.exoplatform.social.core.storage;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.jcr.JCRSessionManager;
import org.exoplatform.social.common.jcr.QueryBuilder;
import org.exoplatform.social.common.jcr.SocialDataLocation;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.relationship.model.Relationship;

/**
 * The Class JCRStorage.
 */
public class RelationshipStorage {
  private static final Log LOG = ExoLogger.getLogger(RelationshipStorage.class);

  /** The identity manager. */
  private IdentityManager identityManager;

  /** The data location. */
  private SocialDataLocation dataLocation;

  /** The session manager. */
  private JCRSessionManager sessionManager;

  /** The Constant RELATION_NODETYPE. */
  final public static String RELATION_NODETYPE = "exo:relationship".intern();

  /** The Constant PROPERTY_ISSYMETRIC. */
  final public static String PROPERTY_ISSYMETRIC = "exo:isSymetric".intern();

  /** The Constant PROPERTY_STATUS. */
  final public static String PROPERTY_STATUS = "exo:status".intern();

  /** The Constant PROPERTY_NAME. */
  final public static String PROPERTY_NAME = "exo:name".intern();

  /** The Constant PROPERTY_INITIATOR. */
  final public static String PROPERTY_INITIATOR = "exo:initiator".intern();

  /** The Constant PROPERTY_NODETYPE. */
  final public static String PROPERTY_NODETYPE = "exo:relationshipProperty".intern();

  /** The Constant RELATION_SENDER. */
  final public static String RELATION_SENDER = "exo:identity1Id".intern();

  /** The Constant RELATION_RECEIVER. */
  final public static String RELATION_RECEIVER = "exo:identity2Id".intern();

  /** The Constant REFERENCEABLE_TYPE */
  public final static String REFERENCEABLE_TYPE = "mix:referenceable";

  private Node relationshipServiceHome;
  /**
   * Instantiates a new jCR storage.
   *
   * @param dataLocation the data location
   */
  public RelationshipStorage(SocialDataLocation dataLocation) {
    this.dataLocation = dataLocation;
    this.sessionManager = dataLocation.getSessionManager();
  }

  /**
   * Save relationship.
   *
   * @param relationship the relationship
   */
  public void saveRelationship(Relationship relationship) {
    Session session = sessionManager.getOrOpenSession();
    Node relationshipNode = null;

    try {
      Node relationshipHomeNode = getRelationshipServiceHome(session);

      if (relationship.getId() == null) {
        relationshipNode = relationshipHomeNode.addNode(RELATION_NODETYPE, RELATION_NODETYPE);
        relationshipNode.addMixin(REFERENCEABLE_TYPE);
      } else {
        relationshipNode = session.getNodeByUUID(relationship.getId());
      }
      Node id1Node = session.getNodeByUUID(relationship.getSender().getId());
      Node id2Node = session.getNodeByUUID(relationship.getReceiver().getId());

      relationshipNode.setProperty(RELATION_SENDER, id1Node);
      relationshipNode.setProperty(RELATION_RECEIVER, id2Node);
      relationshipNode.setProperty(PROPERTY_STATUS, relationship.getStatus().toString());

      updateProperties(relationship, relationshipNode, session);

      if (relationship.getId() == null) {
        relationshipHomeNode.save();
        relationship.setId(relationshipNode.getUUID());
        LOG.debug("relationship " + relationship + " stored");
      } else {
        relationshipNode.save();
        LOG.debug("relationship " + relationship + " updated");
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }

    try {
      loadProperties(relationship, relationshipNode);
    } catch (Exception e) {
      LOG.warn("can not loadProperties()", e);
    }
  }

  /**
   * Removes the relationship.
   *
   * @param relationship the relationship
   */
  public void removeRelationship(Relationship relationship) {
    Session session = sessionManager.openSession();
    try {
      Node relationshipNode = session.getNodeByUUID(relationship.getId());
      relationshipNode.remove();
      session.save();
      LOG.debug("relationship: " + relationship + " deleted");
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
  }

  /**
   * Gets the relationship.
   *
   * @param uuid the uuid
   * @return the relationship
   */
  public Relationship getRelationship(String uuid) {
    Session session = sessionManager.getOrOpenSession();

    Node relationshipNode;
    Relationship relationship = null;
    try {
      relationshipNode = session.getNodeByUUID(uuid);
      relationship = new Relationship(relationshipNode.getUUID());

      Node idNode = relationshipNode.getProperty(RELATION_SENDER).getNode();
      identityManager = getIdentityManager();
      Identity id = identityManager.getIdentity(idNode.getUUID());
      relationship.setSender(id);

      idNode = relationshipNode.getProperty(RELATION_RECEIVER).getNode();
      id = identityManager.getIdentity(idNode.getUUID());
      relationship.setReceiver(id);

      relationship.setStatus(Relationship.Type.valueOf(relationshipNode.getProperty(PROPERTY_STATUS).getString()));
      loadProperties(relationship, relationshipNode);
    } catch (ItemNotFoundException e) {
      LOG.warn(e.getMessage());
      return null;
    } catch (RepositoryException e) {
      LOG.error(e.getMessage(), e);
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }

    return relationship;
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> findRelationships(String senderIdentityId, String relationshipTypeName) throws Exception {
    Session session = sessionManager.getOrOpenSession();
    relationshipServiceHome = getRelationshipServiceHome(session);
    List<Identity> results = new ArrayList<Identity>();
    QueryBuilder queryBuilder = new QueryBuilder(session);
    try {
      queryBuilder.select("exo:relationship", "exo:identity2Id")
                  .like("jcr:path", relationshipServiceHome.getPath()+"/%")
                  .and()
                  .equal("exo:status", relationshipTypeName)
                  .and()
                  .equal("exo:identity1Id", senderIdentityId);
      List<Value> values = queryBuilder.execProperty();
      for (Value value : values) {
        String uuid = value.getString();
        Identity identity = getIdentityManager().getIdentity(uuid, false);
        if (identity != null) {
          results.add(identity);
        } else {
          LOG.warn(senderIdentityId + " has a  relationship of type: " + relationshipTypeName
                  + " with" + uuid + " which does not exist in storage");
        }
      }
    } catch (Exception e) {
      LOG.warn("Failed to findRelationships:" + e.getMessage(), e);
    } finally {
     sessionManager.closeSession();
    }
    return results;
  }

  /**
   * Gets the relationship by identity.
   *
   * @param identity the identity
   * @return the relationship by identity
   * @throws Exception the exception
   */
  public List<Relationship> getRelationshipByIdentity(Identity identity) throws Exception {
    if (identity.getId() == null) {
      return null;
    }
    return getRelationshipByIdentityId(identity.getId());
  }

  /**
   * Gets the relationship by identity id.
   *
   * @param identityId the identity id
   * @return the relationship by identity id
   * @throws Exception the exception
   */
  public List<Relationship> getRelationshipByIdentityId(String identityId) throws Exception {
    Session session = sessionManager.getOrOpenSession();
    List<Relationship> results = new ArrayList<Relationship>();
    PropertyIterator refNodes = null;
    try {
      if (identityId == null) {
        return null;
      }

      Node identityNode = session.getNodeByUUID(identityId);
      refNodes = identityNode.getReferences();
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
      return null;
    } finally {
      sessionManager.closeSession();
    }

    while (refNodes.hasNext()) {
      Property property = (Property) refNodes.next();
      Node node = property.getParent();
      if (node.isNodeType(RELATION_NODETYPE)) {
        results.add(getRelationship(node.getUUID()));
      }
    }

    return results;
  }

  /**
   * Gets the relationship identities by identity.
   *
   * @param identity the identity
   * @return the relationship identities by identity
   * @throws Exception the exception
   */
  public List<Identity> getRelationshipIdentitiesByIdentity(Identity identity) throws Exception {
    Session session = sessionManager.getOrOpenSession();
    List<Identity> results = new ArrayList<Identity>();
    PropertyIterator refNodes = null;
    try {
      if (identity.getId() == null) {
        return null;
      }

      Node identityNode = session.getNodeByUUID(identity.getId());
      refNodes = identityNode.getReferences();
    } catch (Exception e) {
      LOG.warn(e.getMessage(), e);
      return null;
    } finally {
      sessionManager.closeSession();
    }
    identityManager = getIdentityManager();
    while (refNodes.hasNext()) {
      Property property = (Property) refNodes.next();
      Node node = property.getParent();
      if (node.isNodeType(RELATION_NODETYPE)) {
        Node relationshipNode;
        try {
          relationshipNode = session.getNodeByUUID(node.getUUID());
        }
        catch (ItemNotFoundException e) {
          continue;
        }

        Node idNode = relationshipNode.getProperty(RELATION_SENDER).getNode();
        String sId = idNode.getUUID();

        if (!sId.equals(identity.getId()))
          results.add(identityManager.getIdentity(idNode.getUUID()));
        else {
          idNode = relationshipNode.getProperty(RELATION_RECEIVER).getNode();
          results.add(identityManager.getIdentity(idNode.getUUID()));
        }
      }
    }
    return results;
  }

  /**
   * Gets the relationship service home.
   *
   * @param session the session
   * @return the relationship service home
   * @throws Exception the exception
   */
  private Node getRelationshipServiceHome(Session session) {
    if (relationshipServiceHome == null) {
      String path = dataLocation.getSocialRelationshipHome();
      try {
        relationshipServiceHome = session.getRootNode().getNode(path);
      } catch (PathNotFoundException e) {
        LOG.warn(e.getMessage(), e);
      } catch (RepositoryException e) {
        LOG.warn(e.getMessage(), e);
      }
    }
    return relationshipServiceHome;
  }

  /**
   * Load all the properties and add them to the relationship.
   *
   * @param relationship the relationship
   * @param relationshipNode the relationship node
   * @throws Exception the exception
   */
  private void loadProperties(Relationship relationship, Node relationshipNode) throws Exception {
    NodeIterator nodes = relationshipNode.getNodes(PROPERTY_NODETYPE);

    List<org.exoplatform.social.core.relationship.model.Property> props = new ArrayList<org.exoplatform.social.core.relationship.model.Property>();

    while (nodes.hasNext()) {
      Node node = (Node) nodes.next();

      org.exoplatform.social.core.relationship.model.Property property = new org.exoplatform.social.core.relationship.model.Property();
      property.setId(node.getUUID());
      property.setName(node.getProperty(PROPERTY_NAME).getString());
      property.setSymetric(node.getProperty(PROPERTY_ISSYMETRIC).getBoolean());
      property.setStatus(Relationship.Type.valueOf(node.getProperty(PROPERTY_STATUS).getString()));

      if (node.hasProperty(PROPERTY_INITIATOR)) {
        Node idNode = node.getProperty(PROPERTY_INITIATOR).getNode();

        Identity id = identityManager.getIdentity(idNode.getUUID());
        property.setInitiator(id);
      }

      props.add(property);
    }
    relationship.setProperties(props);
  }

  /**
   * Update properties.
   *
   * @param relationship the relationship
   * @param relationshipNode the relationship node
   * @param session the session
   * @throws Exception the exception
   */
  private void updateProperties(Relationship relationship, Node relationshipNode, Session session) throws Exception {
    List<org.exoplatform.social.core.relationship.model.Property> properties = relationship.getProperties();

    NodeIterator oldNodes = relationshipNode.getNodes(PROPERTY_NODETYPE);

    //remove the nodes that need to be removed
    while (oldNodes.hasNext()) {
      Node node = (Node) oldNodes.next();
      String uuid = node.getUUID();
      Boolean toRemove = true;
      for (org.exoplatform.social.core.relationship.model.Property property : properties) {
        if (uuid.equals(property.getId())) {
          toRemove = false;
          break;
        }
      }
      if (toRemove) {
        node.remove();
      }
    }

    //add the new properties and update the existing one
    for (org.exoplatform.social.core.relationship.model.Property property : properties) {
      Node propertyNode;
      if (property.getId() == null) {
        propertyNode = relationshipNode.addNode(PROPERTY_NODETYPE, PROPERTY_NODETYPE);
        propertyNode.addMixin(REFERENCEABLE_TYPE);
        //relationshipNode.save();
        //property.setId(propertyNode.getUUID());
      } else {
        propertyNode = session.getNodeByUUID(relationship.getId());
      }
      propertyNode.setProperty(PROPERTY_NAME, property.getName());
      propertyNode.setProperty(PROPERTY_ISSYMETRIC, property.isSymetric());
      propertyNode.setProperty(PROPERTY_STATUS, property.getStatus().toString());

      if (property.getInitiator() != null) {
        Node initiatorNode = session.getNodeByUUID(property.getInitiator().getId());
        propertyNode.setProperty(PROPERTY_INITIATOR, initiatorNode);
      }
    }
  }

  /**
   * Gets identityManager
   * @return identityManager
   */
  private IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = (IdentityManager) PortalContainer.getInstance().getComponentInstanceOfType(IdentityManager.class);
    }
    return identityManager;
  }
}