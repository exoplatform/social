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
package org.exoplatform.social.core.relationship.storage;

import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.*;
import org.exoplatform.social.space.JCRSessionManager;
import org.exoplatform.social.space.impl.SocialDataLocation;

import javax.jcr.*;
import java.util.List;
import java.util.ArrayList;


public class JCRStorage {

  private IdentityManager identityManager;
  private SocialDataLocation dataLocation;
  private JCRSessionManager sessionManager;

  final private static String RELATION_NODETYPE = "exo:relationship".intern();

  //final private static String IDENTITY_ID = "exo:id".intern();
  final private static String PROPERTY_ISSYMETRIC = "exo:isSymetric".intern();
  final private static String PROPERTY_STATUS = "exo:status".intern();
  final private static String PROPERTY_NAME = "exo:name".intern();
  final private static String PROPERTY_INITIATOR = "exo:initiator".intern();
  final private static String PROPERTY_NODETYPE = "exo:relationshipProperty".intern();


  final private static String RELATION_IDENTITY1 = "exo:identity1Id".intern();
  final private static String RELATION_IDENTITY2 = "exo:identity2Id".intern();

  public JCRStorage(SocialDataLocation dataLocation, IdentityManager identityManager) {
    this.dataLocation = dataLocation;
    this.identityManager = identityManager;
    this.sessionManager = dataLocation.getSessionManager();
  }

  public void saveRelationship(Relationship relationship) throws Exception {
    Session session = sessionManager.openSession();
    Node relationshipNode = null;
    try {
      Node relationshipHomeNode = getRelationshipServiceHome(session);

      if (relationship.getId() == null) {
        relationshipNode = relationshipHomeNode.addNode(RELATION_NODETYPE, RELATION_NODETYPE);
        relationshipNode.addMixin("mix:referenceable");
        //relationshipHomeNode.save();
      } else {
        relationshipNode = session.getNodeByUUID(relationship.getId());
      }
      Node id1Node = session.getNodeByUUID(relationship.getIdentity1().getId());
      Node id2Node = session.getNodeByUUID(relationship.getIdentity2().getId());


      relationshipNode.setProperty(RELATION_IDENTITY1, id1Node);
      relationshipNode.setProperty(RELATION_IDENTITY2, id2Node);
      relationshipNode.setProperty(PROPERTY_STATUS, relationship.getStatus().toString());

      updateProperties(relationship, relationshipNode, session);

      if (relationship.getId() == null) {
        relationshipHomeNode.save();
        relationship.setId(relationshipNode.getUUID());
      }
      else {
        relationshipNode.save();
      }
    } catch (Exception e) {
    } finally {
      sessionManager.closeSession();
    }
      loadProperties(relationship, relationshipNode);  
  }
  
  public void removeRelationship(Relationship relationship) {
    Session session = sessionManager.openSession();
    try {
      Node relationshipHomeNode = getRelationshipServiceHome(session);
      Node relationshipNode = session.getNodeByUUID(relationship.getId());
      relationshipNode.remove();
      relationshipHomeNode.save();
    } catch (Exception e) {
      // TODO: handle exception
    } finally {
      sessionManager.closeSession();
    }
  }

  public Relationship getRelationship(String uuid) throws Exception {
    Session session = sessionManager.openSession();

    Node relationshipNode;
    try {
      relationshipNode = session.getNodeByUUID(uuid);
    }
    catch (ItemNotFoundException e) {
      return null;
    } finally {
      sessionManager.closeSession();
    }

    Relationship relationship = new Relationship(relationshipNode.getUUID());

    Node idNode = relationshipNode.getProperty(RELATION_IDENTITY1).getNode();
    Identity id = identityManager.getIdentityById(idNode.getUUID());
    relationship.setIdentity1(id);

    idNode = relationshipNode.getProperty(RELATION_IDENTITY2).getNode();
    id = identityManager.getIdentityById(idNode.getUUID());
    relationship.setIdentity2(id);

    relationship.setStatus(Relationship.Type.valueOf(relationshipNode.getProperty(PROPERTY_STATUS).getString()));

    loadProperties(relationship, relationshipNode);

    return relationship;
  }

  public List<Relationship> getRelationshipByIdentity(Identity identity) throws Exception {
    if (identity.getId() == null)
      return null;

    return getRelationshipByIdentityId(identity.getId());
  }

  public List<Relationship> getRelationshipByIdentityId(String identityId) throws Exception {
    Session session = sessionManager.openSession();
    List<Relationship> results = new ArrayList<Relationship>();
    PropertyIterator refNodes = null;
    try {
      if (identityId == null) {
        return null;
      }

      Node identityNode = session.getNodeByUUID(identityId);
      refNodes = identityNode.getReferences();
    } catch (Exception e) {
      // TODO: handle exception
      return null;
    } finally {
      sessionManager.closeSession();
    }
    
    while (refNodes.hasNext()) {
      javax.jcr.Property property = (javax.jcr.Property) refNodes.next();
      Node node = property.getParent();
      if (node.isNodeType(RELATION_NODETYPE)) {
        results.add(getRelationship(node.getUUID()));
      }
    }
    
    return results;
  }

  public List<Identity> getRelationshipIdentitiesByIdentity(Identity identity) throws Exception {
    Session session = sessionManager.openSession();
    List<Identity> results = new ArrayList<Identity>();
    PropertyIterator refNodes = null;
    try {
      if (identity.getId() == null) {
        return null;
      }

      Node identityNode = session.getNodeByUUID(identity.getId());
      refNodes = identityNode.getReferences();
    } catch (Exception e) {
      // TODO: handle exception
      return null;
    } finally {
      sessionManager.closeSession();
    }
    
    while (refNodes.hasNext()) {
        javax.jcr.Property property = (javax.jcr.Property) refNodes.next();
        Node node = property.getParent();
        if (node.isNodeType(RELATION_NODETYPE)) {
            Node relationshipNode;
            try {
              relationshipNode = session.getNodeByUUID(node.getUUID());
            }
            catch (ItemNotFoundException e) {
                continue;
            }

            Node idNode = relationshipNode.getProperty(RELATION_IDENTITY1).getNode();
            String sId = idNode.getUUID();

            if (!sId.equals(identity.getId()))
                results.add(identityManager.getIdentityById(idNode.getUUID()));
            else {

                idNode = relationshipNode.getProperty(RELATION_IDENTITY2).getNode();
                results.add(identityManager.getIdentityById(idNode.getUUID()));
            }

        }
    }
      
    return results;
  }

  private Node getRelationshipServiceHome(Session session) throws Exception {
    String path = dataLocation.getSocialRelationshipHome();
    return session.getRootNode().getNode(path);
  }
  
  /**
   * Load all the properties and add them to the relationship
   *
   * @param relationship
   * @param relationshipNode
   * @throws Exception
   */
  private void loadProperties(Relationship relationship, Node relationshipNode) throws Exception {
    NodeIterator nodes = relationshipNode.getNodes(PROPERTY_NODETYPE);

    List<org.exoplatform.social.core.relationship.Property> props = new ArrayList<org.exoplatform.social.core.relationship.Property>();

    while (nodes.hasNext()) {
      Node node = (Node) nodes.next();

      org.exoplatform.social.core.relationship.Property property = new org.exoplatform.social.core.relationship.Property();
      property.setId(node.getUUID());
      property.setName(node.getProperty(PROPERTY_NAME).getString());
      property.setSymetric(node.getProperty(PROPERTY_ISSYMETRIC).getBoolean());
      property.setStatus(Relationship.Type.valueOf(node.getProperty(PROPERTY_STATUS).getString()));

      if (node.hasProperty(PROPERTY_INITIATOR)) {
        Node idNode = node.getProperty(PROPERTY_INITIATOR).getNode();
          
        Identity id = identityManager.getIdentityById(idNode.getUUID());
        property.setInitiator(id);
      }

      props.add(property);
    }
    relationship.setProperties(props);
  }

  private void updateProperties(Relationship relationship, Node relationshipNode, Session session) throws Exception {
    List<org.exoplatform.social.core.relationship.Property> properties = relationship.getProperties();

    NodeIterator oldNodes = relationshipNode.getNodes(PROPERTY_NODETYPE);

    //remove the nodes that need to be removed
    while (oldNodes.hasNext()) {
      Node node = (Node) oldNodes.next();
      String uuid = node.getUUID();
      Boolean toRemove = true;
      for (org.exoplatform.social.core.relationship.Property property : properties) {
        if (uuid.equals(property.getId())) {
          toRemove = false;
          break;
        }
      }
      if (toRemove) {
        node.remove();
      }
    }

    //add the new properties and update the exisiting one
    for (org.exoplatform.social.core.relationship.Property property : properties) {
      Node propertyNode;
      if (property.getId() == null) {
        propertyNode = relationshipNode.addNode(PROPERTY_NODETYPE, PROPERTY_NODETYPE);
        propertyNode.addMixin("mix:referenceable");
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
}