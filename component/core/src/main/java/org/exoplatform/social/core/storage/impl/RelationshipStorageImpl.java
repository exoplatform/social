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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import javax.jcr.Node;
import javax.jcr.NodeIterator;

import org.chromattic.api.query.Ordering;
import org.chromattic.api.query.QueryBuilder;
import org.chromattic.api.query.QueryResult;
import org.chromattic.core.query.QueryImpl;
import org.exoplatform.commons.notification.impl.AbstractService;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.chromattic.entity.DisabledEntity;
import org.exoplatform.social.core.chromattic.entity.IdentityEntity;
import org.exoplatform.social.core.chromattic.entity.ProfileEntity;
import org.exoplatform.social.core.chromattic.entity.RelationshipEntity;
import org.exoplatform.social.core.chromattic.entity.RelationshipListEntity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.profile.ProfileLoader;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.IdentityStorageException;
import org.exoplatform.social.core.storage.RelationshipStorageException;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.ActivityStreamStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.cache.CachedActivityStorage;
import org.exoplatform.social.core.storage.cache.CachedActivityStreamStorage;
import org.exoplatform.social.core.storage.exception.NodeNotFoundException;
import org.exoplatform.social.core.storage.query.JCRProperties;
import org.exoplatform.social.core.storage.query.WhereExpression;
import org.exoplatform.social.core.storage.streams.StreamInvocationHelper;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class RelationshipStorageImpl extends AbstractStorage implements RelationshipStorage {

  /** Logger */
  private static final Log LOG = ExoLogger.getLogger(RelationshipStorage.class);

  private final IdentityStorage identityStorage;
  private RelationshipManager relationshipManager;
  private RelationshipStorage relationshipStorage;
  private CachedActivityStorage cachedActivityStorage;
  private CachedActivityStreamStorage streamStorage;

  public RelationshipStorageImpl(IdentityStorage identityStorage) {
   this.identityStorage = identityStorage;
  }

  private enum Origin { FROM, TO }

  private RelationshipManager getRelationshipManager() {
    
    if (relationshipManager == null) {
      PortalContainer container = PortalContainer.getInstance();
      this.relationshipManager  = (RelationshipManager) container.getComponentInstanceOfType(RelationshipManager.class);
    }
    return relationshipManager;
  }

  private CachedActivityStorage getCachedActivityStorage() {
    
    if (this.cachedActivityStorage == null) {
      PortalContainer container = PortalContainer.getInstance();
      this.cachedActivityStorage  = (CachedActivityStorage) container.getComponentInstanceOfType(ActivityStorage.class);
    }
    
    return this.cachedActivityStorage;
  }
  
  private CachedActivityStreamStorage getCachedActivityStreamStorage() {
    
    if (this.streamStorage == null) {
      PortalContainer container = PortalContainer.getInstance();
      this.streamStorage  = (CachedActivityStreamStorage) container.getComponentInstanceOfType(ActivityStreamStorage.class);
    }
    
    return this.streamStorage;
  }
  
  private void putRelationshipToList(List<Relationship> relationships, RelationshipListEntity list) {
    if (list != null) {
      for (Map.Entry<String, RelationshipEntity> entry : list.getRelationships().entrySet()) {
        Relationship relationship = new Relationship(entry.getValue().getId());

        RelationshipEntity relationshipEntity = entry.getValue();
        IdentityEntity senderEntity = relationshipEntity.getFrom();
        IdentityEntity receiverEntity = relationshipEntity.getTo();
        //
        if (_getMixin(senderEntity, DisabledEntity.class, false) != null ||
            _getMixin(receiverEntity, DisabledEntity.class, false) != null) {
          continue;
        }
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

  private void putReceiverRelationshipToList(List<Relationship> relationships, RelationshipListEntity list, Identity receiver) {
    if (list != null) {
      for (Map.Entry<String, RelationshipEntity> entry : list.getRelationships().entrySet()) {
        Relationship relationship = new Relationship(entry.getValue().getId());

        RelationshipEntity relationshipEntity = entry.getValue();
        IdentityEntity senderEntity = relationshipEntity.getFrom();
        if (senderEntity.getId().equals(receiver.getId())) {
          senderEntity = relationshipEntity.getTo();
        }

        Identity sender = new Identity(senderEntity.getId());
        sender.setRemoteId(senderEntity.getRemoteId());
        sender.setProviderId(senderEntity.getProviderId());
        ProfileEntity senderProfileEntity = senderEntity.getProfile();

        if (senderProfileEntity != null) {
          loadProfile(sender);
        }

        if (receiver.getProfile() != null) {
          loadProfile(receiver);
        }

        relationship.setSender(sender);
        relationship.setReceiver(receiver);
        relationship.setStatus(Relationship.Type.PENDING);

       relationships.add(relationship);
      }
    }
  }
  
  private void loadProfile(final Identity identity) {
    ProfileLoader loader = new ProfileLoader() {
      public Profile load() throws IdentityStorageException {
        Profile profile = new Profile(identity);
        return identityStorage.loadProfile(profile);
      }
    };
    identity.setProfileLoader(loader);
  }

  private List<Identity> getIdentitiesFromRelationship(Iterator<RelationshipEntity> it, Origin origin, long offset, long limit) {

    //
    Set<Identity> identities = new LinkedHashSet<Identity>();
    int i = 0;

    _skip(it, offset);

    Identity identity = null;
    while (it.hasNext()) {

      RelationshipEntity relationshipEntity = it.next();
      IdentityEntity identityEntity;

      switch (origin) {

        case FROM:
          identityEntity = relationshipEntity.getFrom();
          identity = createIdentityFromEntity(identityEntity);
          
          if (identity.isEnable()) {
            identities.add(identity);
          }
          break;

        case TO:
          identityEntity = relationshipEntity.getTo();
          identity = createIdentityFromEntity(identityEntity);

          if (identity.isEnable()) {
            identities.add(identity);
          }
          break;
      }

      if (limit != -1 && limit > 0 && ++i >= limit) {
        break;
      }

    }

    return new ArrayList<Identity>(identities);
  }

  private List<Identity> getIdentitiesFromRelationship(Iterator<RelationshipEntity> it, Identity current, long offset, long limit) {
    //
    Set<Identity> identities = new LinkedHashSet<Identity>();
    int i = 0;

    _skip(it, offset);

    Identity identity = null;
    while (it.hasNext()) {
      RelationshipEntity relationshipEntity = it.next();
      
      IdentityEntity entity = relationshipEntity.getFrom();
      if (entity.getId().equals(current.getId())) {
        entity = relationshipEntity.getTo();
      }
      
      identity = createIdentityFromEntity(entity);
      if (identity.isEnable()) {
        identities.add(identity);
        if (limit != -1 && limit > 0 && ++i >= limit) {
          break;
        }
      }

    }

    return new ArrayList<Identity>(identities);
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
      if (_getMixin(current, DisabledEntity.class, false) != null) {
        continue;
      }
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
    QueryResult<ProfileEntity> result = builder.where(whereExpression.toString()).get().objects();
    int number = 0;
    while (result.hasNext()) {
      IdentityEntity current = result.next().getIdentity();
      if (_getMixin(current, DisabledEntity.class, false) == null) {
        ++number;
      }
    }
    //
    return number;
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
        
        updateRelationshipStatistic(sender, true);
        updateRelationshipStatistic(receiver, true);
        
        StreamInvocationHelper.connect(relationship.getSender(), relationship.getReceiver());
        
        break;
      
      // TODO : IGNORED
    }

    //getSession().save();

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
  
  /**
   * Updates the relationship statistic for the given user. 
   * 
   * @param identityEntity the identity
   * @param isIncreaseCount determines the increase or decrease
   * @throws NodeNotFoundException
   */
  private void updateRelationshipStatistic(IdentityEntity identityEntity, boolean isIncreaseCount) {
    int newValue = 0;
    if (identityEntity.hasProperty(IdentityEntity.RELATIONSHIP_NUMBER_PARAM)) {
      String value = identityEntity.getProperties().get(IdentityEntity.RELATIONSHIP_NUMBER_PARAM);
      newValue = Integer.valueOf(value);
      if (isIncreaseCount) {
        newValue++;
      } else {
        newValue--;
      }
    } else {
      if (isIncreaseCount) {
        newValue = 1;
      }
    }
    
    identityEntity.setProperty(IdentityEntity.RELATIONSHIP_NUMBER_PARAM, String.valueOf(newValue));

    
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
          //SOC-4283 : to work around the problem of wrong data with receiver relationship (sender and receiver value are exchanged)
          //so we need a specific method to treat the problem
          putReceiverRelationshipToList(relationships, receiverEntity.getReceiver(), receiver);
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
        //
        StorageUtils.persist();
        
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
      IdentityEntity to = toDeleteRelationship.getTo();
      
      if(Relationship.Type.CONFIRMED.equals(relationship.getStatus())) {
        updateRelationshipStatistic(from, false);
        updateRelationshipStatistic(to, false);
      }

      _removeById(RelationshipEntity.class, symmetricalRelationship.getId());
      _removeById(RelationshipEntity.class, relationship.getId());
      
      //getSession().save();
      StorageUtils.persist();
      
      //getCachedActivityStreamStorage().deleteConnect(relationship.getSender(), relationship.getReceiver());
      StreamInvocationHelper.deleteConnect(relationship.getSender(), relationship.getReceiver());
      
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
  
  @Override
  public boolean hasRelationship(Identity identity1, Identity identity2, String relationshipPath) throws RelationshipStorageException {
    //it implemented on CachedRelationshipStorage
    throw new RelationshipStorageException(RelationshipStorageException.Type.FAILED_TO_GET_RELATIONSHIP_OF_THEM, "hasRelationship() unsupported!"); 
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
  public List<Identity> getLastConnections(Identity identity, int limit) throws RelationshipStorageException {
    //check the limit parameter
    if (limit <= 0) {
      return new ArrayList<Identity>();
    }
    //
    List<Identity> identities = new ArrayList<Identity>();
    try {
      IdentityEntity identityEntity = _findById(IdentityEntity.class, identity.getId());
      String relationshipNodePath = identityEntity.getPath() + StorageUtils.SLASH_STR + StorageUtils.SOC_RELATIONSHIP;
      Node node = (Node) getSession().getJCRSession().getItem(relationshipNodePath);
      NodeIterator iterator = AbstractService.getNodeIteratorOrderDESC(node);
      while (iterator.hasNext() && limit > 0) {
        Node relNode = iterator.nextNode();
        if (relNode.getName().contains(StorageUtils.COLON_STR)) {
          String remoteId = relNode.getName().split(StorageUtils.COLON_STR)[1];
          Identity newIdentity = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, remoteId);
          identities.add(newIdentity);
          limit--;
        }
      }
    }
    catch (Exception e) {
      throw new RelationshipStorageException(
           RelationshipStorageException.Type.FAILED_TO_GET_RELATIONSHIP,
           e.getMessage());
    }

    return identities;
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
        //
        if (_getMixin(gotIdentityEntity, DisabledEntity.class, false) != null) {
          continue;
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
      //SOC-4283 : to work around the problem of wrong data with receiver relationship (sender and receiver value are exchanged)
      //so we need a specific method to treat the problem
      return getIdentitiesFromRelationship(it, receiver, offset, limit);

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
    //
    return getIncomingRelationships(receiver, 0, -1).size();
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
    return getOutgoingRelationships(sender, 0, -1).size();
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
      if (identityEntity.hasProperty(IdentityEntity.RELATIONSHIP_NUMBER_PARAM)) {
        String value = identityEntity.getProperties().get(IdentityEntity.RELATIONSHIP_NUMBER_PARAM);
        return Integer.valueOf(value);
      } else {
        //
        int totalSize = identityEntity.getRelationship().getRelationships().size();
        identityEntity.setProperty(IdentityEntity.RELATIONSHIP_NUMBER_PARAM, String.valueOf(totalSize));
        getSession().save();
        return totalSize;
      }
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
    //
    if (profileFilter.isEmpty()) {
      return StorageUtils.sortIdentitiesByFullName(getIncomingRelationships(existingIdentity, offset, limit), true);
    }

    List<Identity> identities = getStorage().getIncomingRelationships(existingIdentity, 0, -1);
    return getIdentitiesRelationsByFilter(identities, profileFilter, offset, limit);

  }
  
  /**
   * {@inheritDoc}
   */
  public List<Identity> getOutgoingByFilter(
      final Identity existingIdentity, final ProfileFilter profileFilter, final long offset, final long limit)
      throws RelationshipStorageException {

    if (profileFilter.isEmpty()) {
      return StorageUtils.sortIdentitiesByFullName(getOutgoingRelationships(existingIdentity, offset, limit), true);
    }
    
    List<Identity> identities = getStorage().getOutgoingRelationships(existingIdentity, 0, -1);
    return getIdentitiesRelationsByFilter(identities, profileFilter, offset, limit);

  }
  /**
   * {@inheritDoc}
   */
  public int getIncomingCountByFilter(
      final Identity existingIdentity, final ProfileFilter profileFilter) throws RelationshipStorageException {
    
    if (profileFilter.isEmpty()) {
      return getIncomingRelationshipsCount(existingIdentity);
    }

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
    
    if (profileFilter.isEmpty()) {
      return getOutgoingRelationshipsCount(existingIdentity);
    }

    List<Identity> identities = getStorage().getOutgoingRelationships(existingIdentity, 0, -1);
    return getIdentitiesRelationsByFilterCount(identities, profileFilter);

  }

  /**
   * {@inheritDoc}
   */
  public Map<Identity, Integer> getSuggestions(Identity currentIdentity, int maxConnections, 
                                                int maxConnectionsToLoad, 
                                                int maxSuggestions) throws RelationshipStorageException {
    try {
      return _getSuggestions(currentIdentity, maxConnections, maxConnectionsToLoad, maxSuggestions);
    } catch (Exception e) {
      throw new RelationshipStorageException(RelationshipStorageException.Type.FAILED_TO_GET_SUGGESTION, e);
    }
  }

  public Map<Identity, Integer> _getSuggestions(Identity currentIdentity, int maxConnections, 
                                                int maxConnectionsToLoad, 
                                                int maxSuggestions) throws Exception {
    if (maxConnectionsToLoad > 0 && maxConnections > maxConnectionsToLoad)
       maxConnectionsToLoad = maxConnections;
     // Get identities level 1
    Set<Identity> relationIdLevel1 = new HashSet<Identity>();
    RelationshipManager relationshipManager = getRelationshipManager();
    ListAccess<Identity> allConnections = relationshipManager.getConnections(currentIdentity);
    int size = allConnections.getSize();
    // The ideal limit of connection to treat however we could need to go beyond this limit
    // if we cannot reach the expected amount of suggestions
    int endIndex;
    Random random = new Random();
    Identity[] connections;
    if (size > maxConnectionsToLoad && maxConnectionsToLoad > 0 && maxConnections > 0) {
      // The total amount of connections is bigger than the maximum allowed
      // We will then load only a random sample to reduce the best we can the 
      // required time for this task 
      int startIndex = random.nextInt(size - maxConnectionsToLoad);
      endIndex = maxConnections;
      connections= allConnections.load(startIndex, maxConnectionsToLoad);
    } else {
      // The total amount of connections is less than the maximum allowed
      // We call load everything
      endIndex = size;
      connections= allConnections.load(0, size);
    }
    // we need to load all the connections
    for (int i = 0; i < connections.length; i++) {
      Identity id = connections[i];
      relationIdLevel1.add(id);
    }
    relationIdLevel1.remove(currentIdentity);

    // Get identities level 2 (suggested Identities)
    Map<Identity, Integer> suggestedIdentities = new HashMap<Identity, Integer>();
    Iterator<Identity> it = relationIdLevel1.iterator();
    for (int j = 0; j < size && it.hasNext(); j++) {
      Identity id = it.next();
      // We check if we reach the limit of connections to treat and if we have enough suggestions
      if (j >= endIndex && suggestedIdentities.size() > maxSuggestions && maxSuggestions > 0)
        break;
      ListAccess<Identity> allConns = relationshipManager.getConnections(id);
      int allConnSize = allConns.getSize();
      int allConnStartIndex = 0;
      if (allConnSize > maxConnections && maxConnections > 0) {
        // The current identity has more connections that the allowed amount so we will treat a sample
        allConnStartIndex = random.nextInt(allConnSize - maxConnections);
        connections = allConns.load(allConnStartIndex, maxConnections);
      } else {
        // The current identity doesn't have more connections that the allowed amount so we will 
        // treat all of them
        connections = allConns.load(0, allConnSize);
      }
      for (int i = 0; i < connections.length; i++) {
        Identity ids = connections[i];
        // We check if the current connection is not already part of the connections of the identity
        // for which we seek some suggestions
        if (!relationIdLevel1.contains(ids) && !ids.equals(currentIdentity) && !ids.isDeleted()
             && relationshipManager.get(ids, currentIdentity) == null) {
          Integer commonIdentities = suggestedIdentities.get(ids);
          if (commonIdentities == null) {
            commonIdentities = new Integer(1);
          } else {
            commonIdentities = new Integer(commonIdentities.intValue() + 1);
          }
          suggestedIdentities.put(ids, commonIdentities);
        }
      }
    }
    NavigableMap<Integer, List<Identity>> groupByCommonConnections = new TreeMap<Integer, List<Identity>>();
    // This for loop allows to group the suggestions by total amount of common connections
    for (Identity identity : suggestedIdentities.keySet()) {
      Integer commonIdentities = suggestedIdentities.get(identity);
      List<Identity> ids = groupByCommonConnections.get(commonIdentities);
      if (ids == null) {
        ids = new ArrayList<Identity>();
        groupByCommonConnections.put(commonIdentities, ids);
      }
      ids.add(identity);
    }
    Map<Identity, Integer> suggestions = new LinkedHashMap<Identity, Integer>();
    int suggestionLeft = maxSuggestions;
    // We iterate over the suggestions starting from the suggestions with the highest amount of common
    // connections
    main: for (Integer key : groupByCommonConnections.descendingKeySet()) {
      List<Identity> ids = groupByCommonConnections.get(key);
      for (Identity identity : ids) {
        suggestions.put(identity, key);
        // We stop once we have enough suggestions
        if (maxSuggestions > 0 && --suggestionLeft == 0)
          break main;
      }
    }
    return suggestions;
  }

  public void setStorage(RelationshipStorage storage) {
    this.relationshipStorage = storage;
  }
}
