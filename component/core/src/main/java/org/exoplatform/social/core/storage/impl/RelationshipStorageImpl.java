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

package org.exoplatform.social.core.storage.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.apache.commons.lang.ArrayUtils;
import org.chromattic.api.query.Ordering;
import org.chromattic.api.query.QueryBuilder;
import org.chromattic.api.query.QueryResult;
import org.chromattic.core.query.QueryImpl;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.chromattic.entity.IdentityEntity;
import org.exoplatform.social.core.chromattic.entity.ProfileEntity;
import org.exoplatform.social.core.chromattic.entity.RelationshipEntity;
import org.exoplatform.social.core.chromattic.entity.RelationshipListEntity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.RelationshipStorageException;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.cache.CachedActivityStorage;
import org.exoplatform.social.core.storage.exception.NodeNotFoundException;
import org.exoplatform.social.core.storage.query.JCRProperties;
import org.exoplatform.social.core.storage.query.WhereExpression;
import org.exoplatform.social.core.storage.thread.SocialExecutorService;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class RelationshipStorageImpl extends AbstractStorage implements RelationshipStorage {

  /** Logger */
  private static final Log LOG = ExoLogger.getLogger(RelationshipStorage.class);

  private final IdentityStorage identityStorage;
  private RelationshipStorage relationshipStorage;
  private CachedActivityStorage cachedActivityStorage;
  private Object lock = new Object();
  private SocialExecutorService<String[]> executorService = new SocialExecutorService<String[]>(10);

  public RelationshipStorageImpl(IdentityStorage identityStorage) {
   this.identityStorage = identityStorage;
  }

  private enum Origin { FROM, TO }
  
  private CachedActivityStorage getCachedActivityStorage() {
    
    if (this.cachedActivityStorage == null) {
      PortalContainer container = PortalContainer.getInstance();
      this.cachedActivityStorage  = (CachedActivityStorage) container.getComponentInstanceOfType(ActivityStorage.class);
    }
    
    return this.cachedActivityStorage;
  }
  
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
          loadProfile(sender);
        }

        Identity receiver = new Identity(receiverEntity.getId());
        receiver.setRemoteId(receiverEntity.getRemoteId());
        receiver.setProviderId(receiverEntity.getProviderId());
        ProfileEntity receiverProfileEntity = receiverEntity.getProfile();

        if (receiverProfileEntity != null) {
          loadProfile(receiver);
        }

        relationship.setSender(sender);
        relationship.setReceiver(receiver);

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

  private void loadProfile(Identity identity) {
    Profile profile = new Profile(identity);
    profile = identityStorage.loadProfile(profile);
    identity.setProfile(profile);
  }

  private List<Identity> getIdentitiesFromRelationship(Iterator<RelationshipEntity> it, Origin origin, long offset, long limit) {

    //
    List<Identity> identities = new ArrayList<Identity>();
    int i = 0;

    _skip(it, offset);

    Identity identity = null;
    while (it.hasNext()) {

      RelationshipEntity relationshipEntity = it.next();

      switch (origin) {

        case FROM:
          identity = createIdentityFromEntity(relationshipEntity.getFrom());
          
          //remove duplicated
          if (identities.indexOf(identity) == -1) {
            identities.add(identity);
          }
          break;

        case TO:
          identity = createIdentityFromEntity(relationshipEntity.getTo());
          //remove duplicated
          if (identities.indexOf(identity) == -1) {
            identities.add(identity);
          }

          break;
      }

      if (limit != -1 && limit > 0 && ++i >= limit) {
        break;
      }

    }

    return identities;
  }

  private Identity createIdentityFromEntity(IdentityEntity entity) {

    Identity identity = identityStorage.findIdentityById(entity.getId());
    loadProfile(identity);

    return identity;

  }

  private List<Identity> getIdentitiesRelationsByFilter(final List<Identity> relations, final ProfileFilter filter,
                                                        final long offset, final long limit) {
    
    if (relations.isEmpty()) return new ArrayList<Identity>();
    
    //
    List<Identity> found = new ArrayList<Identity>();
    if(relations.isEmpty()) return found ;
    QueryBuilder<ProfileEntity> builder = getSession().createQueryBuilder(ProfileEntity.class);
    WhereExpression whereExpression = new WhereExpression();
    StorageUtils.applyWhereFromIdentity(whereExpression, relations);

    //
    StorageUtils.applyFilter(whereExpression, filter);

    //
    builder.where(whereExpression.toString()).orderBy(ProfileEntity.fullName.getName(), Ordering.ASC);
    
    QueryImpl<ProfileEntity> queryImpl = (QueryImpl<ProfileEntity>) builder.get();
    ((org.exoplatform.services.jcr.impl.core.query.QueryImpl) queryImpl.getNativeQuery()).setCaseInsensitiveOrder(true);
    
    QueryResult<ProfileEntity> result = queryImpl.objects(offset, limit);
    
    while(result.hasNext()) {
      IdentityEntity current = result.next().getIdentity();
      Identity i = new Identity(current.getProviderId(), current.getRemoteId());
      i.setId(current.getId());
      found.add(i);
    }

    //
    return found;

  }

  private int getIdentitiesRelationsByFilterCount(final List<Identity> relations, final ProfileFilter filter) {

    if (relations.size() == 0) {
      return 0;
    }

    //
    QueryBuilder<ProfileEntity> builder = getSession().createQueryBuilder(ProfileEntity.class);

    //
    WhereExpression whereExpression = new WhereExpression();
    StorageUtils.applyWhereFromIdentity(whereExpression, relations);

    //
    StorageUtils.applyFilter(whereExpression, filter);

    //
    return builder.where(whereExpression.toString()).get().objects().size();

  }

  private RelationshipStorage getStorage() {
    return (relationshipStorage != null ? relationshipStorage : this);
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
    RelationshipEntity symmetricalRelationship = identity2.createRelationship();
    
    switch (relationship.getStatus()) {

      case PENDING:
        identity1.getSender().getRelationships().put(identity2.getRemoteId(), createdRelationship);
        identity2.getReceiver().getRelationships().put(identity1.getRemoteId(), symmetricalRelationship);
        
        createdRelationship.setFrom(identity1);
        createdRelationship.setTo(identity2);
        
        symmetricalRelationship.setFrom(identity1);
        symmetricalRelationship.setTo(identity2);
        
        break;

      case CONFIRMED:
        identity1.getRelationship().getRelationships().put(identity2.getRemoteId(), createdRelationship);
        identity2.getRelationship().getRelationships().put(identity1.getRemoteId(), symmetricalRelationship);
        
        createdRelationship.setFrom(identity1);
        createdRelationship.setTo(identity2);
        
        symmetricalRelationship.setFrom(identity2);
        symmetricalRelationship.setTo(identity1);
        
        break;

      case IGNORED:
        identity1.getIgnore().getRelationships().put(identity2.getRemoteId(), createdRelationship);
        identity2.getIgnored().getRelationships().put(identity1.getRemoteId(), symmetricalRelationship);
        
        createdRelationship.setFrom(identity1);
        createdRelationship.setTo(identity2);
        
        symmetricalRelationship.setFrom(identity1);
        symmetricalRelationship.setTo(identity2);
        
        break;

    }

    long createdTimeStamp = System.currentTimeMillis();
    createdRelationship.setReciprocal(symmetricalRelationship);
    createdRelationship.setStatus(relationship.getStatus().toString());
    createdRelationship.setCreatedTime(createdTimeStamp);
    
    symmetricalRelationship.setReciprocal(createdRelationship);
    symmetricalRelationship.setStatus(relationship.getStatus().toString());
    symmetricalRelationship.setCreatedTime(createdTimeStamp);

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
    
    IdentityEntity sender = _findById(IdentityEntity.class, relationship.getSender().getId());
    IdentityEntity receiver = _findById(IdentityEntity.class, relationship.getReceiver().getId());

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
        
        //measure the relationship is two ways when relationship is confirmed
        savedRelationship.setFrom(sender);
        savedRelationship.setTo(receiver);
        
        symmetricalRelationship.setFrom(receiver);
        symmetricalRelationship.setTo(sender);

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

    Identity receiver = identityStorage.findIdentityById(receiverEntity.getId());
    Identity sender = identityStorage.findIdentityById(senderEntity.getId());

    Relationship relationship = new Relationship(uuid);
    if (relationshipEntity.isReceiver()) {
      relationship.setSender(receiver);
      relationship.setReceiver(sender);
    }
    else {
      relationship.setSender(sender);
      relationship.setReceiver(receiver);
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

    // CONFIRMED
    RelationshipEntity got = identityEntity1.getRelationship().getRelationships().get(identityEntity2.getName());    

    // PENDING
    if (got == null) {
      got = identityEntity1.getSender().getRelationships().get(identity2.getRemoteId());
    }
    if (got == null) {
      got = identityEntity2.getSender().getRelationships().get(identity1.getRemoteId());
    }

    // NOT FOUND
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
   * {@inheritDoc}
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
   * {@inheritDoc}
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
      
      getCachedActivityStorage().clearCache();

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
   * {@inheritDoc}
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
   * {@inheritDoc}
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
   * {@inheritDoc}
   */
  public List<Relationship> getSenderRelationships(
      final String senderId, final Relationship.Type type, final List<Identity> listCheckIdentity)
      throws RelationshipStorageException {

    return getSenderRelationships(new Identity(senderId), type, listCheckIdentity);

  }

  /**
   * {@inheritDoc}
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
   * {@inheritDoc}
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
   * {@inheritDoc}
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

          case IGNORED:
            putRelationshipToList(relationships, receiverEntity.getIgnored());
            break;

        }
      }
      
      return relationships;
    }
    catch (NodeNotFoundException e) {
      return new ArrayList<Relationship>();
    }
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getRelationships(final Identity identity, long offset, long limit)
      throws RelationshipStorageException {

    List<Identity> identities = new ArrayList<Identity>();

    try {

      IdentityEntity identityEntity = _findById(IdentityEntity.class, identity.getId());

      QueryBuilder<RelationshipEntity> builder = getSession().createQueryBuilder(RelationshipEntity.class);

      WhereExpression whereExpression = new WhereExpression();
      whereExpression.like(JCRProperties.path, identityEntity.getPath() + StorageUtils.SLASH_STR + StorageUtils.PERCENT_STR);

      builder.where(whereExpression.toString());
      builder.orderBy(RelationshipEntity.createdTime.getName(), Ordering.DESC);

      QueryResult<RelationshipEntity> results = builder.get().objects(offset, limit);

      while (results.hasNext()) {

        RelationshipEntity currentRelationshipEntity = results.next();
        IdentityEntity gotIdentityEntity;
        if (currentRelationshipEntity.isReceiver()) {
          gotIdentityEntity = currentRelationshipEntity.getFrom();
        }
        else {
          gotIdentityEntity = currentRelationshipEntity.getTo();
        }

        Identity newIdentity = new Identity(gotIdentityEntity.getId());
        newIdentity.setProviderId(gotIdentityEntity.getProviderId());
        newIdentity.setRemoteId(gotIdentityEntity.getRemoteId());

        identities.add(newIdentity);
      }

    }
    catch (NodeNotFoundException e) {
      throw new RelationshipStorageException(
           RelationshipStorageException.Type.FAILED_TO_GET_RELATIONSHIP,
           e.getMessage());
    }

    return identities;
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIncomingRelationships(Identity receiver,
                                                 long offset, long limit) throws RelationshipStorageException {

    try {
      
      IdentityEntity receiverEntity = _findById(IdentityEntity.class, receiver.getId());

      Iterator<RelationshipEntity> it = receiverEntity.getReceiver().getRelationships().values().iterator();
      return getIdentitiesFromRelationship(it, Origin.FROM, offset, limit);

    }
    catch (NodeNotFoundException e) {
      throw new RelationshipStorageException(
           RelationshipStorageException.Type.FAILED_TO_GET_RELATIONSHIP,
           e.getMessage());
    }

  }

  /**
   * {@inheritDoc}
   */
   public int getIncomingRelationshipsCount(Identity receiver) throws RelationshipStorageException {

     try {

       IdentityEntity receiverEntity = _findById(IdentityEntity.class, receiver.getId());
       return receiverEntity.getReceiver().getRelationships().size();
       
     }
     catch (NodeNotFoundException e) {
       throw new RelationshipStorageException(
           RelationshipStorageException.Type.FAILED_TO_GET_RELATIONSHIP,
           e.getMessage());
     }
   }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getOutgoingRelationships(Identity sender,
                                                 long offset, long limit) throws RelationshipStorageException {

    try {

      IdentityEntity senderEntity = _findById(IdentityEntity.class, sender.getId());

      Iterator<RelationshipEntity> it = senderEntity.getSender().getRelationships().values().iterator();
      return getIdentitiesFromRelationship(it, Origin.TO, offset, limit);

    }
    catch (NodeNotFoundException e) {
      throw new RelationshipStorageException(
           RelationshipStorageException.Type.FAILED_TO_GET_RELATIONSHIP,
           e.getMessage());
    }

  }

  /**
   * {@inheritDoc}
   */
  public int getOutgoingRelationshipsCount(Identity sender) throws RelationshipStorageException {

    try {

       IdentityEntity receiverEntity = _findById(IdentityEntity.class, sender.getId());
       return receiverEntity.getSender().getRelationships().size();

     }
     catch (NodeNotFoundException e) {
       throw new RelationshipStorageException(
           RelationshipStorageException.Type.FAILED_TO_GET_RELATIONSHIP,
           e.getMessage());
     }

  }

  /**
   * {@inheritDoc}
   */
   public int getRelationshipsCount(Identity identity) throws RelationshipStorageException {

     int nb = 0;

     //
     try {

       IdentityEntity identityEntity = _findById(IdentityEntity.class, identity.getId());
       nb += identityEntity.getRelationship().getRelationships().size();
       nb += identityEntity.getSender().getRelationships().size();
       nb += identityEntity.getReceiver().getRelationships().size();
       nb += identityEntity.getIgnore().getRelationships().size();

       return nb;
       
     }
     catch (NodeNotFoundException e) {

       throw new RelationshipStorageException(
           RelationshipStorageException.Type.FAILED_TO_GET_RELATIONSHIP,
           e.getMessage());

     }
   }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getConnections(Identity identity, long offset, long limit) throws RelationshipStorageException {

    try {
      IdentityEntity identityEntity = _findById(IdentityEntity.class, identity.getId());

      Iterator<RelationshipEntity> it = identityEntity.getRelationship().getRelationships().values().iterator();
      return getIdentitiesFromRelationship(it, Origin.TO, offset, limit);

    }
    catch (NodeNotFoundException e) {
      throw new RelationshipStorageException(RelationshipStorageException.Type.ILLEGAL_ARGUMENTS);
    }
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getConnections(Identity identity) throws RelationshipStorageException {

    return getConnections(identity, 0, -1);

  }

  /**
   * {@inheritDoc}
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

  /**
   * {@inheritDoc}
   */
  public List<Identity> getConnectionsByFilter(
      final Identity existingIdentity, final ProfileFilter profileFilter, final long offset, final long limit)
      throws RelationshipStorageException {

    List<Identity> identities = getStorage().getConnections(existingIdentity);
    return getIdentitiesRelationsByFilter(identities, profileFilter, offset, limit);
    
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIncomingByFilter(
      final Identity existingIdentity, final ProfileFilter profileFilter, final long offset, final long limit)
      throws RelationshipStorageException {

    List<Identity> identities = getStorage().getIncomingRelationships(existingIdentity, 0, -1);
    return getIdentitiesRelationsByFilter(identities, profileFilter, offset, limit);

  }
  
  /**
   * {@inheritDoc}
   */
  public List<Identity> getOutgoingByFilter(
      final Identity existingIdentity, final ProfileFilter profileFilter, final long offset, final long limit)
      throws RelationshipStorageException {

    List<Identity> identities = getStorage().getOutgoingRelationships(existingIdentity, 0, -1);
    return getIdentitiesRelationsByFilter(identities, profileFilter, offset, limit);

  }
  /**
   * {@inheritDoc}
   */
  public int getIncomingCountByFilter(
      final Identity existingIdentity, final ProfileFilter profileFilter) throws RelationshipStorageException {

    List<Identity> identities = getStorage().getIncomingRelationships(existingIdentity, 0, -1);
    return getIdentitiesRelationsByFilterCount(identities, profileFilter);

  }
  
  /**
   * {@inheritDoc}
   */
  public int getConnectionsCountByFilter(
      final Identity existingIdentity, final ProfileFilter profileFilter) throws RelationshipStorageException {

    List<Identity> identities = getStorage().getConnections(existingIdentity);
    return getIdentitiesRelationsByFilterCount(identities, profileFilter);

  }
  
  /**
   * {@inheritDoc}
   */
  public int getOutgoingCountByFilter(
      final Identity existingIdentity, final ProfileFilter profileFilter) throws RelationshipStorageException {

    List<Identity> identities = getStorage().getOutgoingRelationships(existingIdentity, 0, -1);
    return getIdentitiesRelationsByFilterCount(identities, profileFilter);

  }
  
  private String[] getRelationships(String id) {
    ThreadLocal<String[]> identityIdsLocal = new ThreadLocal<String[]>();

    String[] relationshipIds = new String[0];
    identityIdsLocal.set(relationshipIds);

    try {

      IdentityEntity identityEntity = _findById(IdentityEntity.class, id);

      StringBuffer sb = new StringBuffer().append("SELECT * FROM soc:relationshipdefinition WHERE ");
      sb.append(JCRProperties.path.getName())
        .append(" LIKE '")
        .append(identityEntity.getPath() + StorageUtils.SLASH_STR + StorageUtils.PERCENT_STR)
        .append("'");
      //
      sb.append(" ORDER BY ").append(RelationshipEntity.createdTime.getName()).append(" DESC ");

      synchronized (lock) {

        NodeIterator it = nodes(sb.toString());

        while (it.hasNext()) {
          Node node = (Node) it.next();

          RelationshipEntity currentRelationshipEntity = _findById(RelationshipEntity.class,
                                                                   node.getUUID());

          IdentityEntity gotIdentityEntity;
          if (currentRelationshipEntity.isReceiver()) {
            gotIdentityEntity = currentRelationshipEntity.getFrom();
          } else {
            gotIdentityEntity = currentRelationshipEntity.getTo();
          }
          identityIdsLocal.set((String[]) ArrayUtils.add(identityIdsLocal.get(),
                                                         gotIdentityEntity.getId()));
        }

      }
    } catch (Exception e) {
      throw new RelationshipStorageException(RelationshipStorageException.Type.FAILED_TO_GET_RELATIONSHIP,
                                             e.getMessage());
    }

    return identityIdsLocal.get();
  }
  
  private String[] getSuggestion(List<String> relationshipIds) {
    String[] suggestions = new String[0];
    
    try {
      int startIndex = 0;
      int toIndex = 100;
      //
      List<String> excludeIdentities = StorageUtils.subList(relationshipIds, startIndex, toIndex); 
      while (excludeIdentities.size() > 0) {
        
        StringBuffer sb = new StringBuffer().append("SELECT * FROM soc:identitydefinition WHERE ");
        sb.append(JCRProperties.path.getName()).append(" LIKE '").append(getProviderRoot().getProviders().get(OrganizationIdentityProvider.NAME).getPath() + StorageUtils.SLASH_STR + StorageUtils.PERCENT_STR).append("'");
        
        for(String id : excludeIdentities) {
          sb.append(" AND NOT ").append(JCRProperties.id.getName()).append(" = '").append(id).append("'");
        }
        
        NodeIterator nodeIter = nodes(sb.toString()); 
        while(nodeIter.hasNext()) {
          Node node = (Node)nodeIter.next();
          String id = node.getUUID();
          //String remoteId = node.getProperty(IdentityEntity.remoteId.getName()).getString();
          if (relationshipIds.contains(id) == false
              && ArrayUtils.contains(suggestions, id) == false) {
            suggestions = (String[]) ArrayUtils.add(suggestions, id);
          }
        }
        
        //
        startIndex = toIndex;
        toIndex += 100;
        
        excludeIdentities = StorageUtils.subList(relationshipIds, startIndex, toIndex); 
      }//end filter identities
      
    } catch (Exception e) {
      throw new RelationshipStorageException(RelationshipStorageException.Type.FAILED_TO_GET_RELATIONSHIP,
                                             e.getMessage());
    }
    
    return suggestions;
  }
  /**
   * Gets Suggestion list with relationship for each identity.
   * @param suggestions
   * @return
   */
  private Map<String, String[]> getSuggestionRelationships(final String[] suggestions) {
    for (final String id : suggestions) {
      //
      Callable<String[]> task = new  Callable<String[]>() {
        @Override
        public String[] call() throws Exception {
          return getRelationships(id);
        }
      };
      executorService.submit(task, id);
    }
    
    //Wait multiple threads finish work and push relationships to map
    Map<String, String[]> suggestRelationships = new HashMap<String, String[]>();
    copyFutureToMap(suggestRelationships, executorService.getFutureCollections());
    
    return suggestRelationships;
  }
  /**
   * Gets result from future task
   * @param map
   * @param futures
   */
  private void copyFutureToMap(Map<String, String[]> map, Map<String, Future<String[]>> futures) {
    try {
      for (Entry<String, Future<String[]>> future : futures.entrySet()) {
        map.put(future.getKey(), future.getValue().get());
      }
    } catch (Exception e) {
      LOG.warn("Failed to get result from Future." + e);
    }
  }
  /**
   * Calculate and statistic suggestion.
   * 
   * @param relationshipIds
   * @param suggestRelationships
   * @return
   */
  private Map<String, Integer> statisticSuggesstion(List<String> relationshipIds, Map<String, String[]> suggestRelationships) {
    //Calculate the common users of current Identity with each suggestion.
    Map<String, Integer> suggestionIdMap = new HashMap<String, Integer>();
    for (Entry<String, String[]> friendOfIdentity : suggestRelationships.entrySet()) {
      //
      String identityId = friendOfIdentity.getKey();
      suggestionIdMap.put(identityId, StorageUtils.getCommonItemNumber(relationshipIds, Arrays.asList(friendOfIdentity.getValue())));
    }
    
    return suggestionIdMap;
  }
  
  /**
   * {@inheritDoc}
   */
  public Map<Identity, Integer> getSuggestions(Identity identity, int offset, int limit) throws RelationshipStorageException {
    //get identities who have relationship
    List<String> relationshipIds = new ArrayList<String>(); 
    relationshipIds.add(identity.getId());
    relationshipIds.addAll(Arrays.asList(getRelationships(identity.getId())));
    
    //store all identities who is not relationship with provided identity
    String[] suggestions = getSuggestion(relationshipIds);

    //Find relationships of the identities are filtered
    //Wait multiple threads finish work and push relationships to map
    Map<String, String[]> suggestRelationships = getSuggestionRelationships(suggestions);

    //statistic suggestion
    Map<String, Integer> suggestionIdMap = statisticSuggesstion(relationshipIds, suggestRelationships);
    //
    if (offset > suggestionIdMap.size()) return Collections.emptyMap();
    return buildSuggestions(StorageUtils.sortMapByValue(suggestionIdMap, false), offset, limit);
  }
  
  private Map<Identity, Integer> buildSuggestions(Map<String, Integer> mapIds,
                                                  final int offset,
                                                  final int limit) {

    Map<Identity, Integer> suggestions = new LinkedHashMap<Identity, Integer>();
    //
    int i = 0;
    for (Entry<String, Integer> id : mapIds.entrySet()) {
      // skip to offset
      if (i < offset) {
        i++;
        continue;
      }

      // limit the return result
      if (i > (offset + limit))
        break;

      try {
        IdentityEntity identityEntity = _findById(IdentityEntity.class, id.getKey());
        Identity _identity = new Identity(id.getKey());
        _identity.setDeleted(identityEntity.isDeleted());
        _identity.setRemoteId(identityEntity.getRemoteId());
        _identity.setProviderId(identityEntity.getProviderId());
        loadProfile(_identity);
        // put to suggestion result
        suggestions.put(_identity, id.getValue());

      } catch (NodeNotFoundException e) {
        LOG.warn("Could not found identity with id = " + id.getKey());
      }
      i++;
    }
    return suggestions;
  }

  public void setStorage(RelationshipStorage storage) {
    this.relationshipStorage = storage;
  }

}
