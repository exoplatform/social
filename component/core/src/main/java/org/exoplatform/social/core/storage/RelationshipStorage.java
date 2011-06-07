/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.social.core.storage;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.chromattic.entity.IdentityEntity;
import org.exoplatform.social.core.chromattic.entity.ProfileEntity;
import org.exoplatform.social.core.chromattic.entity.RelationshipEntity;
import org.exoplatform.social.core.chromattic.entity.RelationshipListEntity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.exception.NodeNotFoundException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class RelationshipStorage extends AbstractStorage {

  /** Logger */
  private static final Log LOG = ExoLogger.getLogger(RelationshipStorage.class);

  private void putRelationshipToList(List<Relationship> relationships, RelationshipListEntity list) {
    if (list != null) {
      for (Map.Entry<String, RelationshipEntity> entry : list.getRelationships().entrySet()) {
        Relationship relationship = new Relationship(entry.getValue().getId());

        RelationshipEntity relationshipEntity = entry.getValue();
        IdentityEntity senderEntity = relationshipEntity.getFrom();
        IdentityEntity receiverEntity = relationshipEntity.getTo();

        Identity sender = new Identity(senderEntity.getId());
        sender.setRemoteId(senderEntity.getRemoteId());
        sender.setProviderId(senderEntity.getProviderId());
        ProfileEntity senderProfileEntity = senderEntity.getProfile();

        if (senderProfileEntity != null) {
          Profile senderProfile = new Profile(sender);

          senderProfile.setProperty(
              Profile.FIRST_NAME,
              senderProfileEntity.getPropertyFirst(IdentityStorage.PropNs.VOID.nameOf(Profile.FIRST_NAME)));

          senderProfile.setProperty(
              Profile.LAST_NAME,
              senderProfileEntity.getPropertyFirst(IdentityStorage.PropNs.VOID.nameOf(Profile.LAST_NAME)));

          senderProfile.setProperty(
              Profile.URL,
              senderProfileEntity.getPropertyFirst(IdentityStorage.PropNs.VOID.nameOf(Profile.URL)));

          senderProfile.setProperty(
              Profile.POSITION,
              senderProfileEntity.getPropertyFirst(IdentityStorage.PropNs.VOID.nameOf(Profile.POSITION)));

          senderProfile.setId(senderProfileEntity.getId());
          sender.setProfile(senderProfile);
        }

        Identity receiver = new Identity(receiverEntity.getId());
        receiver.setRemoteId(receiverEntity.getRemoteId());
        receiver.setProviderId(receiverEntity.getProviderId());
        ProfileEntity receiverProfileEntity = receiverEntity.getProfile();

        if (receiverProfileEntity != null) {
          Profile receiverProfile = new Profile(receiver);

          receiverProfile.setProperty(
              Profile.FIRST_NAME,
              receiverProfileEntity.getPropertyFirst(IdentityStorage.PropNs.VOID.nameOf(Profile.FIRST_NAME)));

          receiverProfile.setProperty(
              Profile.LAST_NAME,
              receiverProfileEntity.getPropertyFirst(IdentityStorage.PropNs.VOID.nameOf(Profile.LAST_NAME)));

          receiverProfile.setProperty(
              Profile.URL,
              receiverProfileEntity.getPropertyFirst(IdentityStorage.PropNs.VOID.nameOf(Profile.URL)));
          
          receiverProfile.setProperty(
              Profile.POSITION,
              receiverProfileEntity.getPropertyFirst(IdentityStorage.PropNs.VOID.nameOf(Profile.POSITION)));

          receiverProfile.setId(receiverProfileEntity.getId());
          receiver.setProfile(receiverProfile);
        }

        if (relationshipEntity.isSender()) {
          relationship.setSender(sender);
          relationship.setReceiver(receiver);
        }
        else {
          relationship.setSender(receiver);
          relationship.setReceiver(sender);
        }

        if (SENDER.equals(entry.getValue().getParent().getName()) ||
            RECEIVER.equals(entry.getValue().getParent().getName())) {
          relationship.setStatus(Relationship.Type.PENDING);
        }
        else {
          relationship.setStatus(Relationship.Type.CONFIRMED);
        }

        // TODO : IGNORED

        relationships.add(relationship);
      }
    }
  }

  /*
   * Internal
   */

  protected RelationshipEntity _createRelationship(final Relationship relationship) throws NodeNotFoundException {
    String identityId1 = relationship.getSender().getId();
    String identityId2 = relationship.getReceiver().getId();

    IdentityEntity identity1 = _findById(IdentityEntity.class, identityId1);
    IdentityEntity identity2 = _findById(IdentityEntity.class, identityId2);

    RelationshipEntity createdRelationship = identity1.createRelationship();
    RelationshipEntity symmetricalRelationship = identity1.createRelationship();

    switch (relationship.getStatus()) {

      case PENDING:
        identity1.getSender().getRelationships().put(identity2.getRemoteId(), createdRelationship);
        identity2.getReceiver().getRelationships().put(identity1.getRemoteId(), symmetricalRelationship);
        break;

      case CONFIRMED:
        identity1.getRelationship().getRelationships().put(identity2.getRemoteId(), createdRelationship);
        identity2.getRelationship().getRelationships().put(identity1.getRemoteId(), symmetricalRelationship);
        break;

      // TODO : IGNORED

    }

    createdRelationship.setFrom(identity1);
    createdRelationship.setTo(identity2);
    createdRelationship.setReciprocal(symmetricalRelationship);
    createdRelationship.setStatus(relationship.getStatus().toString());
    
    symmetricalRelationship.setFrom(identity2);
    symmetricalRelationship.setTo(identity1);
    symmetricalRelationship.setReciprocal(createdRelationship);
    symmetricalRelationship.setStatus(relationship.getStatus().toString());

    relationship.setId(createdRelationship.getId());

    getSession().save();

    //
    LOG.debug(String.format(
        "Relationship from %s:%s to %s:%s created (%s)",
        createdRelationship.getFrom().getProviderId(),
        createdRelationship.getFrom().getRemoteId(),
        createdRelationship.getTo().getProviderId(),
        createdRelationship.getTo().getRemoteId(),
        createdRelationship.getPath()
    ));

    //
    LOG.debug(String.format(
        "Symmetrical relationship from %s:%s to %s:%s created (%s)",
        symmetricalRelationship.getFrom().getProviderId(),
        symmetricalRelationship.getFrom().getRemoteId(),
        symmetricalRelationship.getTo().getProviderId(),
        symmetricalRelationship.getTo().getRemoteId(),
        symmetricalRelationship.getPath()
    ));

    return createdRelationship;
  }

  protected RelationshipEntity _saveRelationship(final Relationship relationship) throws NodeNotFoundException {

    RelationshipEntity savedRelationship = _findById(RelationshipEntity.class, relationship.getId());
    RelationshipEntity symmetricalRelationship = savedRelationship.getReciprocal();

    savedRelationship.setStatus(relationship.getStatus().toString());
    symmetricalRelationship.setStatus(relationship.getStatus().toString());

    switch (relationship.getStatus()) {
      case PENDING:

        // Move to sender / receiver
        savedRelationship.getParent().getParent().getSender().getRelationships()
            .put(savedRelationship.getName(), savedRelationship);

        symmetricalRelationship.getParent().getParent().getReceiver().getRelationships()
            .put(symmetricalRelationship.getName(), symmetricalRelationship);
        
        break;
      case CONFIRMED:

        // Move to relationship
        savedRelationship.getParent().getParent().getRelationship().getRelationships()
            .put(savedRelationship.getName(), savedRelationship);

        symmetricalRelationship.getParent().getParent().getRelationship().getRelationships()
            .put(symmetricalRelationship.getName(), symmetricalRelationship);
        
        break;
      
      // TODO : IGNORED
    }

    getSession().save();

    //
    LOG.debug(String.format(
        "Relationship from %s:%s to %s:%s saved (%s)",
        savedRelationship.getFrom().getProviderId(),
        savedRelationship.getFrom().getRemoteId(),
        savedRelationship.getTo().getProviderId(),
        savedRelationship.getTo().getRemoteId(),
        savedRelationship.getPath()
    ));

    //
    LOG.debug(String.format(
        "Symmetrical relationship from %s:%s to %s:%s saved (%s)",
        symmetricalRelationship.getFrom().getProviderId(),
        symmetricalRelationship.getFrom().getRemoteId(),
        symmetricalRelationship.getTo().getProviderId(),
        symmetricalRelationship.getTo().getRemoteId(),
        symmetricalRelationship.getPath()
    ));

    return savedRelationship;
  }

  protected List<Relationship> _getSenderRelationships(
      final Identity sender, final Relationship.Type type, final List<Identity> listCheckIdentity)
      throws NodeNotFoundException {

    // TODO : listCheckIdentity ?

    List<Relationship> relationships = new ArrayList<Relationship>();

    //
    IdentityEntity senderEntity = _findById(IdentityEntity.class, sender.getId());

    if (type == null) {
      putRelationshipToList(relationships, senderEntity.getRelationship());
      putRelationshipToList(relationships, senderEntity.getSender());
    }
    else {
      switch (type) {

        case CONFIRMED:
          putRelationshipToList(relationships, senderEntity.getRelationship());
          break;

        case PENDING:
          putRelationshipToList(relationships, senderEntity.getSender());
          break;

        // TODO : IGNORED

      }
    }

    return relationships;
  }

  protected List<Relationship> _getReceiverRelationships(
      final Identity receiver, final Relationship.Type type, final List<Identity> listCheckIdentity)
      throws NodeNotFoundException {

    List<Relationship> relationships = new ArrayList<Relationship>();

    //
    IdentityEntity receiverEntity = _findById(IdentityEntity.class, receiver.getId());

    if (type == null) {
      putRelationshipToList(relationships, receiverEntity.getRelationship());
      putRelationshipToList(relationships, receiverEntity.getReceiver());
    }
    else {
      switch (type) {

        case CONFIRMED:
          putRelationshipToList(relationships, receiverEntity.getRelationship());
          break;

        case PENDING:
          putRelationshipToList(relationships, receiverEntity.getReceiver());
          break;

        // TODO : IGNORED

      }
    }

    return relationships;
  }

  protected Relationship _getRelationship(String uuid) throws NodeNotFoundException {

    RelationshipEntity relationshipEntity = _findById(RelationshipEntity.class, uuid);

    IdentityEntity receiverEntity = relationshipEntity.getTo();
    IdentityEntity senderEntity = relationshipEntity.getFrom();

    Identity receiver = new Identity(receiverEntity.getId());
    Identity sender = new Identity(senderEntity.getId());

    Relationship relationship = new Relationship(uuid);
    if (relationshipEntity.isSender()) {
      relationship.setSender(sender);
      relationship.setReceiver(receiver);
    }
    else {
      relationship.setSender(receiver);
      relationship.setReceiver(sender);
    }

    if (SENDER.equals(relationshipEntity.getParent().getName()) ||
        RECEIVER.equals(relationshipEntity.getParent().getName())) {
      relationship.setStatus(Relationship.Type.PENDING);
    }
    else {
      relationship.setStatus(Relationship.Type.CONFIRMED);
    }

    // TODO : IGNORED

    return relationship;
  }

  protected Relationship _getRelationship(final Identity identity1, final Identity identity2)
      throws RelationshipStorageException, NodeNotFoundException {

    IdentityEntity identityEntity1 = _findById(IdentityEntity.class, identity1.getId());
    IdentityEntity identityEntity2 = _findById(IdentityEntity.class, identity2.getId());

    RelationshipEntity got = identityEntity1.getRelationship().getRelationships().get(identity2.getRemoteId());

    if (got == null) {
      got = identityEntity1.getSender().getRelationships().get(identity2.getRemoteId());
    }
    if (got == null) {
      got = identityEntity2.getSender().getRelationships().get(identity1.getRemoteId());
    }

    if (got == null) {
      throw new NodeNotFoundException();
    }

    Relationship relationship = new Relationship(got.getId());

    //
    IdentityEntity senderEntity = got.getFrom();
    IdentityEntity receiverEntity = got.getTo();

    Identity sender = new Identity(senderEntity.getId());
    sender.setRemoteId(senderEntity.getRemoteId());
    sender.setProviderId(senderEntity.getProviderId());

    Identity receiver = new Identity(receiverEntity.getId());
    receiver.setRemoteId(receiverEntity.getRemoteId());
    receiver.setProviderId(receiverEntity.getProviderId());

    relationship.setSender(sender);
    relationship.setReceiver(receiver);

    relationship.setStatus(Relationship.Type.valueOf(got.getStatus()));
    return relationship;
  }

  /*
   * Public
   */

  /**
   * Saves relationship.
   *
   * @param relationship the relationship
   * @throws RelationshipStorageException
   */
  public Relationship saveRelationship(final Relationship relationship) throws RelationshipStorageException {
    try {
      if (relationship.getId() == null) {
        _createRelationship(relationship);
      }
      else {
        _saveRelationship(relationship);
      }
    }
    catch (NodeNotFoundException e) {
      throw new RelationshipStorageException(
          RelationshipStorageException.Type.ILLEGAL_ARGUMENTS,
          new String[] { Relationship.class.getSimpleName() });
    }

    return relationship;
  }

  /**
   * Removes the relationship.
   *
   * @param relationship the relationship
   * @throws RelationshipStorageException
   */
  public void removeRelationship(Relationship relationship) throws RelationshipStorageException {

    try {
      RelationshipEntity toDeleteRelationship = _findById(RelationshipEntity.class, relationship.getId());
      RelationshipEntity symmetricalRelationship = toDeleteRelationship.getReciprocal();

      IdentityEntity from = toDeleteRelationship.getFrom();
      IdentityEntity to = toDeleteRelationship.getFrom();

      _removeById(RelationshipEntity.class, symmetricalRelationship.getId());
      _removeById(RelationshipEntity.class, relationship.getId());
      
      getSession().save();

      //
      LOG.debug(String.format(
          "Symmetrical relationship from %s:%s to %s:%s removed",
          to.getProviderId(),
          to.getRemoteId(),
          from.getProviderId(),
          from.getRemoteId()
      ));

      //
      LOG.debug(String.format(
          "Relationship from %s:%s to %s:%s removed",
          from.getProviderId(),
          from.getRemoteId(),
          to.getProviderId(),
          to.getRemoteId()
      ));
    }
    catch (NodeNotFoundException e) {
      throw new RelationshipStorageException(RelationshipStorageException.Type.FAILED_TO_GET_RELATIONSHIP);
    }
  }

  /**
   * Gets the relationship.
   *
   * @param uuid the uuid
   * @return the relationship
   * @throws RelationshipStorageException
   */
  public Relationship getRelationship(String uuid) throws RelationshipStorageException {

    try {
      return _getRelationship(uuid);
    }
    catch (NodeNotFoundException e) {
      return null;
    }
  }

  /**
   * Gets the list of relationship by identity id matching with checking
   * identity ids
   *
   * @param sender the identity
   * @param type
   * @param listCheckIdentity identity the checking identities
   * @return the relationship
   * @throws RelationshipStorageException
   */
  public List<Relationship> getSenderRelationships(
      final Identity sender, final Relationship.Type type, final List<Identity> listCheckIdentity)
      throws RelationshipStorageException {

    try {
      return _getSenderRelationships(sender, type, listCheckIdentity);
    }
    catch (NodeNotFoundException e) {
      throw new RelationshipStorageException(
          RelationshipStorageException.Type.FAILED_TO_GET_RELATIONSHIP, null, e, sender.getId(), type.toString());
    }
  }

  /**
   * Gets the list of relationship by identity id matching with checking
   * identity ids
   *
   * @param receiver the identity id
   * @param type
   * @param listCheckIdentity identityId the checking identity ids
   * @return the relationship
   * @throws RelationshipStorageException
   */
  public List<Relationship> getReceiverRelationships(
      final Identity receiver, final Relationship.Type type, final List<Identity> listCheckIdentity)
      throws RelationshipStorageException {

    try {
      return _getReceiverRelationships(receiver, type, listCheckIdentity);
    }
    catch (NodeNotFoundException e) {
      throw new RelationshipStorageException(
          RelationshipStorageException.Type.FAILED_TO_GET_RELATIONSHIP, null, e, receiver.getId(), type.toString());
    }
  }

  /**
   * Gets the relationship of 2 identities.
   *
   * @param identity1 the identity1
   * @param identity2 the identity2
   * @return the relationship
   * @throws RelationshipStorageException
   */
  public Relationship getRelationship(final Identity identity1, final Identity identity2)
      throws RelationshipStorageException {

    try {
      return _getRelationship(identity1, identity2);
    }
    catch (NodeNotFoundException e) {
      return null;
    }
  }

  /**
   * Gets the list of relationship by identity id matching with checking
   * identity ids
   *
   * @param identity the identity
   * @param type
   * @param listCheckIdentity identity the checking identities
   * @return the relationship
   * @throws RelationshipStorageException
   */
  public List<Relationship> getRelationships(
      final Identity identity, final Relationship.Type type, final List<Identity> listCheckIdentity)
      throws RelationshipStorageException {
    
    try {
      List<Relationship> relationships = new ArrayList<Relationship>();

      //
      IdentityEntity receiverEntity = _findById(IdentityEntity.class, identity.getId());

      if (type == null) {
        putRelationshipToList(relationships, receiverEntity.getRelationship());
        putRelationshipToList(relationships, receiverEntity.getReceiver());
        putRelationshipToList(relationships, receiverEntity.getSender());
      }
      else {
        switch (type) {

          case CONFIRMED:
            putRelationshipToList(relationships, receiverEntity.getRelationship());
            break;

          case PENDING:
            putRelationshipToList(relationships, receiverEntity.getReceiver());
            putRelationshipToList(relationships, receiverEntity.getSender());
            break;

          // TODO : IGNORED

        }
      }
      
      return relationships;
    }
    catch (NodeNotFoundException e) {
      return new ArrayList<Relationship>();
    }
  }

  /**
   * Gets connections with the identity.
   *
   * @param identity
   * @param offset
   * @param limit
   * @return number of connections belong to limitation of offset and limit.
   * @throws RelationshipStorageException
   * @since 1.2.0-GA
   */
  public List<Identity> getConnections(Identity identity, int offset, int limit) throws RelationshipStorageException {

    //
    List<Identity> identities = new ArrayList<Identity>();
    int i = 0;

    try {
      IdentityEntity identityEntity = _findById(IdentityEntity.class, identity.getId());

      Iterator<RelationshipEntity> it = identityEntity.getRelationship().getRelationships().values().iterator();
      _skip(it, offset);

      while (it.hasNext()) {

        RelationshipEntity relationshipEntity = it.next();

        IdentityEntity targetIdentity = relationshipEntity.getTo();
        Identity gotIdentity = new Identity(targetIdentity.getId());
        gotIdentity.setRemoteId(targetIdentity.getRemoteId());
        gotIdentity.setProviderId(targetIdentity.getProviderId());
        identities.add(gotIdentity);
        
        if (limit > 0 && ++i >= limit) {
          break;
        }
      }
      
      return identities;
    }
    catch (NodeNotFoundException e) {
      throw new RelationshipStorageException(RelationshipStorageException.Type.ILLEGAL_ARGUMENTS);
    }
  }

  /**
   * Gets count of connection with the identity.
   *
   * @param identity
   * @return
   * @throws RelationshipStorageException
   * @since 1.2.0-GA
   */
  public int getConnectionsCount(Identity identity) throws RelationshipStorageException {

    try {

      // TODO : use property to improve the perfs

      IdentityEntity identityEntity = _findById(IdentityEntity.class, identity.getId());
      return identityEntity.getRelationship().getRelationships().size();
    }
    catch (NodeNotFoundException e) {
      throw new RelationshipStorageException(RelationshipStorageException.Type.ILLEGAL_ARGUMENTS);
    }
  }
}