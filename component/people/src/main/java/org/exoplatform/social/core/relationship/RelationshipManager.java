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
package org.exoplatform.social.core.relationship;

import java.util.ArrayList;
import java.util.List;

import org.chromattic.api.RelationshipType;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.storage.JCRStorage;

public class RelationshipManager {
  private JCRStorage storage;

  public RelationshipManager(NodeHierarchyCreator nodeHierarchyCreator, IdentityManager im) throws Exception {
    this.storage = new JCRStorage(nodeHierarchyCreator, im);
  }

  public Relationship getById(String id) throws Exception {
    return this.storage.getRelationship(id);
  }

  /**
   * mark a relationship as confirmed
   * 
   * @param relationship
   */
  public void confirm(Relationship relationship) throws Exception {
    relationship.setStatus(Relationship.Type.CONFIRM);
    for (Property prop : relationship.getProperties()) {
      prop.setStatus(Relationship.Type.CONFIRM);
    }
    save(relationship);

  }

  /**
   * remove a relationship
   * 
   * @param relationship
   */
  public void remove(Relationship relationship) throws Exception {
    storage.removeRelationship(relationship);
  }

  /**
   * mark a relationship as ignored
   * 
   * @param relationship
   */
  public void ignore(Relationship relationship) throws Exception {
    relationship.setStatus(Relationship.Type.IGNORE);
    for (Property prop : relationship.getProperties()) {
      prop.setStatus(Relationship.Type.IGNORE);
    }
    save(relationship);
  }

  /**
   * return all the public relationship
   * 
   * @param identity
   * @return
   */
  public List<Identity> getPublicRelation(Identity identity) throws Exception {
    List<Identity> ids = new ArrayList<Identity>();
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    IdentityManager im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    List<Identity> allIds = im.getIdentities("organization");
    for (Identity id : allIds) {
      if (!(id.getId().equals(identity.getId())) && (getRelationship(identity, id) == null)) {
        ids.add(id);
      }
    }
    
    return ids;
  }
  
  /**
   * return all the pending relationship: sent and received
   * 
   * @param identity
   * @return
   */
  public List<Relationship> getPending(Identity identity) throws Exception {
    List<Relationship> rels = get(identity);
    List<Relationship> pendingRel = new ArrayList<Relationship>();
    for (Relationship rel : rels) {
      if (rel.getStatus() == Relationship.Type.PENDING) {
        pendingRel.add(rel);
      } else {
        if (rel.getProperties(Relationship.Type.PENDING).size() > 0)
          pendingRel.add(rel);
      }
    }
    return pendingRel;
  }

  /**
   * if toConfirm is true, it return list of pending relationship received not
   * confirmed if toConfirm is false, it return list of relationship sent not
   * confirmed yet
   * 
   * @param identity
   * @param toConfirm
   * @return
   */
  public List<Relationship> getPending(Identity identity, boolean toConfirm) throws Exception {
    List<Relationship> rels = get(identity);
    List<Relationship> pendingRel = new ArrayList<Relationship>();
    if(toConfirm) {
     for(Relationship rel : rels) {
       if(getRelationshipStatus(rel, identity).equals(Relationship.Type.PENDING))
         pendingRel.add(rel);
     }
     return pendingRel;
    }
    for (Relationship relationship : rels) {
      if(getRelationshipStatus(relationship, identity).equals(Relationship.Type.REQUIRE_VALIDATION))
        pendingRel.add(relationship);
    }
    return pendingRel;
//    List<Relationship> rels = get(identity);
//    List<Relationship> pendingRel = new ArrayList<Relationship>();
//    for (Relationship rel : rels) {
//      if (rel.getStatus() == Relationship.Type.PENDING && !toConfirm) {
//        pendingRel.add(rel);
//      } else if (rel.getStatus() == Relationship.Type.PENDING && toConfirm
//          && !identity.getId().equals(rel.getIdentity1().getId())) {
//        pendingRel.add(rel);
//      } else {
//        List<Property> props = rel.getProperties(Relationship.Type.PENDING);
//        for (Property prop : props) {
//          if (toConfirm == prop.getInitiator().getId().equals(identity.getId())) {
//            pendingRel.add(rel);
//            break;
//          }
//        }
//      }
//    }
//    return pendingRel;
  }

  /**
   * Get pending relations in 2 case:
   *  - if toConfirm is true, it return list of pending relationship received not confirmed 
   *  - if toConfirm is false, it return list of relationship sent not confirmed yet
   *  
   * @param currIdentity
   * @param identities
   * @param toConfirm
   * @return
   * @throws Exception
   */
  public List<Relationship> getPending(Identity currIdentity, List<Identity> identities, boolean toConfirm) throws Exception {
    List<Relationship> pendingRels = getPending(currIdentity, true);
    List<Relationship> invitedRels = getPending(currIdentity, false);
    List<Relationship> pendingRel = new ArrayList<Relationship>();
    if (toConfirm) {
      for (Identity id : identities) {
        for (Relationship rel : pendingRels) {
          if (rel.getIdentity2().getRemoteId().equals(id.getRemoteId())) 
            pendingRel.add(rel);
        }
      }
      
      return pendingRel;
    }
    
    for (Identity id : identities) {
      for (Relationship rel : invitedRels) {
        if (rel.getIdentity1().getRemoteId().equals(id.getRemoteId())) 
          pendingRel.add(rel);
      }
    }
    
    return pendingRel;
  }
  
  /**
   * Get contacts that match the search result.
   * 
   * @param currIdentity
   * @param identities
   * @return
   * @throws Exception
   */
  public List<Relationship> getContacts(Identity currIdentity, List<Identity> identities) throws Exception {
    List<Relationship> contacts = getContacts(currIdentity);
    List<Relationship> relations = new ArrayList<Relationship>();
    for (Identity id : identities) {
      for (Relationship contact : contacts) {
        if (contact.getIdentity1().getRemoteId().equals(id.getRemoteId())) 
          relations.add(contact);
      }
    }
    
    return relations;
  }
  
  public List<Relationship> getContacts(Identity identity) throws Exception {
    List<Relationship> rels = get(identity);
    List<Relationship> contacts = new ArrayList<Relationship>();
    for (Relationship rel : rels) {
      if (rel.getStatus() == Relationship.Type.CONFIRM) {
        contacts.add(rel);
      }
    }
    return contacts;
  }

  /**
   * return all the relationship associated with a given identity
   * 
   * @param id
   * @return
   * @throws Exception
   */
  public List<Relationship> get(Identity id) throws Exception {
    return this.storage.getRelationshipByIdentity(id);
  }

  /**
   * return all the relationship associated with a given identityId
   * 
   * @param id
   * @return
   * @throws Exception
   */
  public List<Relationship> getByIdentityId(String id) throws Exception {
    return this.storage.getRelationshipByIdentityId(id);
  }

  /**
   * return all the identity associated with a given identity TODO check if the
   * relation has been validated
   * 
   * @param id
   * @return
   * @throws Exception
   */
  public List<Identity> getIdentities(Identity id) throws Exception {
    return this.storage.getRelationshipIdentitiesByIdentity(id);
  }

  public Relationship create(Identity id1, Identity id2) {
    return new Relationship(id1, id2);
  }

  public void save(Relationship rel) throws Exception {
    if (rel.getIdentity1().getId().equals(rel.getIdentity2().getId()))
      throw new Exception("the two identity are the same");
    for (Property prop : rel.getProperties()) {

      // if the initator ID is not in the member of the relationship, we throw
      // an exception
      if (!(prop.getInitiator().getId().equals(rel.getIdentity1().getId()) || prop.getInitiator()
                                                                                  .getId()
                                                                                  .equals(rel.getIdentity2()
                                                                                             .getId()))) {

        throw new Exception("the property initiator is not member of the relationship");
      }
    }
    this.storage.saveRelationship(rel);
  }

  public List findRoute(Identity id1, Identity id2) {
    return null;
  }

  public Relationship getRelationship(Identity id1, Identity id2) throws Exception {
    List<Relationship> rels = get(id1);
    String sId2 = id2.getId();
    for (Relationship rel : rels) {
      if (rel.getIdentity1().getId().equals(sId2) || rel.getIdentity2().getId().equals(sId2)) {
        return rel;
      }
    }
    return null;
  }

  // TODO: dang.tung - get relation ship status of one identity in one relation
  // ship.
  public Relationship.Type getRelationshipStatus(Relationship rel, Identity id) {
    if (rel == null)
      return Relationship.Type.ALIEN;
    Identity identity1 = rel.getIdentity1();
    if (rel.getStatus().equals(Relationship.Type.PENDING)) {
      if (identity1.getId().equals(id.getId()))
        return Relationship.Type.PENDING;
      else
        return Relationship.Type.REQUIRE_VALIDATION;
    } else if (rel.getStatus().equals(Relationship.Type.IGNORE)) {
      // TODO need to change in future
      return Relationship.Type.ALIEN;
    }
    return Relationship.Type.CONFIRM;
  }
}
