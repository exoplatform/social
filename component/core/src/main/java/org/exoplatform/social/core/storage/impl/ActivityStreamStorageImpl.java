/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.chromattic.api.query.Query;
import org.chromattic.api.query.QueryBuilder;
import org.chromattic.api.query.QueryResult;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.filter.ActivityFilter;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.chromattic.entity.ActivityEntity;
import org.exoplatform.social.core.chromattic.entity.ActivityRef;
import org.exoplatform.social.core.chromattic.entity.ActivityRefListEntity;
import org.exoplatform.social.core.chromattic.entity.IdentityEntity;
import org.exoplatform.social.core.chromattic.filter.JCRFilterLiteral;
import org.exoplatform.social.core.chromattic.utils.ActivityRefList;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.ActivityStreamStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.exception.NodeNotFoundException;

public class ActivityStreamStorageImpl extends AbstractStorage implements ActivityStreamStorage {
  
  /**
   * The identity storage
   */
  private final IdentityStorageImpl identityStorage;
  
  /**
   * The space storage
   */
  private final SpaceStorage spaceStorage;
  

  /**
   * The relationship storage
   */
  private final RelationshipStorage relationshipStorage;
  
  /**
   * The activity storage
   */
  private ActivityStorage activityStorage;
  
  /** Logger */
  private static final Log LOG = ExoLogger.getLogger(ActivityStreamStorageImpl.class);
  
  public ActivityStreamStorageImpl(IdentityStorageImpl identityStorage, RelationshipStorage relationshipStorage, SpaceStorage spaceStorage) {
    this.identityStorage = identityStorage;
    this.relationshipStorage = relationshipStorage;
    this.spaceStorage = spaceStorage;
  }
  
  private ActivityStorage getStorage() {
    if (activityStorage == null) {
      activityStorage = (ActivityStorage) PortalContainer.getInstance().getComponentInstanceOfType(ActivityStorage.class);
    }
    
    return activityStorage;
  }

  @Override
  public void save(Identity owner, ExoSocialActivity activity) {
    try {
      //
      ActivityEntity activityEntity = _findById(ActivityEntity.class, activity.getId());     
      if (OrganizationIdentityProvider.NAME.equals(owner.getProviderId())) {
        user(owner, activityEntity);
      } else if (SpaceIdentityProvider.NAME.equals(owner.getProviderId())) {
        space(owner, activityEntity);
      }
    } catch (NodeNotFoundException e) {
      LOG.warn("Failed to add Activity references.");
    }
  }
  
  private void user(Identity owner, ActivityEntity activityEntity) throws NodeNotFoundException {
    createOwnerRefs(owner, activityEntity);

    //
    List<Identity> got = relationshipStorage.getConnections(owner);
    if (got.size() > 0) {
      createConnectionsRefs(got, activityEntity);
    }
  }

  private void space(Identity owner, ActivityEntity activityEntity) throws NodeNotFoundException {
    
    //Space space = this.spaceStorage.getSpaceByGroupId(SpaceUtils.SPACE_GROUP + "/" + owner.getRemoteId());
    Space space = this.spaceStorage.getSpaceByPrettyName(owner.getRemoteId());
    
    if (space == null) return;
    
    List<Identity> identities = getMemberIdentities(space);
    createSpaceRefs(identities, activityEntity);
    
  }

  
  private List<Identity> getMemberIdentities(Space space) {
    List<Identity> identities = new ArrayList<Identity>();
    for(String remoteId : space.getMembers()) {
      identities.add(identityStorage.findIdentity(OrganizationIdentityProvider.NAME, remoteId));
    }
    
    return identities;
  }
  
  @Override
  public void addSpaceMember(Space space, Identity owner) {
    if (space == null) return; 
    
  }
  
  @Override
  public void removeSpaceMember(Space space, Identity member) {
    if (space == null) return; 
    
  }
  
  @Override
  public void delete(String activityId) {
    
    try {
      //
      ActivityEntity activityEntity = _findById(ActivityEntity.class, activityId);
      
      IdentityEntity identityEntity = activityEntity.getPosterIdentity();
      Identity owner = identityStorage.findIdentityById(identityEntity.getId());
      
      removeOwnerRefs(owner, activityEntity);
      
      //
      List<Identity> got = relationshipStorage.getConnections(owner);
      if (got.size() > 0) {
        removeConnectionsRefs(got, activityEntity);
      }
    } catch (NodeNotFoundException e) {
      LOG.warn("Failed to delete Activity references.");
    }
  }

  @Override
  public void update(Identity owner) {
    
  }

  @Override
  public List<ExoSocialActivity> getFeed(Identity owner, int offset, int limit) {
    return getActivities(ActivityRefType.FEED, owner, offset, limit);
  }

  @Override
  public int getNumberOfFeed(Identity owner) {
    return getNumberOfActivities(ActivityRefType.FEED, owner);
  }

  @Override
  public List<ExoSocialActivity> getConnections(Identity owner, int offset, int limit) {
    return getActivities(ActivityRefType.CONNECTION, owner, offset, limit);
  }

  @Override
  public int getNumberOfConnections(Identity owner) {
    return getNumberOfActivities(ActivityRefType.CONNECTION, owner);
  }

  @Override
  public List<ExoSocialActivity> getSpaces(Identity owner, int offset, int limit) {
    return getActivities(ActivityRefType.MY_SPACES, owner, offset, limit);
  }

  @Override
  public int getNumberOfSpaces(Identity owner) {
    return getNumberOfActivities(ActivityRefType.MY_SPACES, owner);
  }

  @Override
  public List<ExoSocialActivity> getMyActivities(Identity owner, int offset, int limit) {
    return getActivities(ActivityRefType.MY_ACTIVITIES, owner, offset, limit);
  }

  @Override
  public int getNumberOfMyActivities(Identity owner) {
    return getNumberOfActivities(ActivityRefType.MY_ACTIVITIES, owner);
  }

  @Override
  public void connect(Identity sender, Identity receiver) {
    
    try {
      //
      List<ExoSocialActivity> activities = getActivitiesOfConnections(sender);
      for (ExoSocialActivity activity : activities) {
        
        ActivityEntity entity = _findById(ActivityEntity.class, activity.getId());
        
        createConnectionsRefs(receiver, entity);
      }
      
      //
      activities = getActivitiesOfConnections(receiver);
      for (ExoSocialActivity activity : activities) {
        
        ActivityEntity entity = _findById(ActivityEntity.class, activity.getId());
        
        createConnectionsRefs(sender, entity);
      }
    } catch (NodeNotFoundException e) {
      LOG.warn("Failed to add Activity references when create relationship.");
    }
  }
  
  @Override
  public void deleteConnect(Identity sender, Identity receiver) {
    
    try {
      //
      List<ExoSocialActivity> activities = getActivitiesOfConnections(sender);
      for (ExoSocialActivity activity : activities) {
        
        ActivityEntity entity = _findById(ActivityEntity.class, activity.getId());
        
        removeRelationshipRefs(receiver, entity);
      }
      
      //
      activities = getActivitiesOfConnections(receiver);
      for (ExoSocialActivity activity : activities) {
        
        ActivityEntity entity = _findById(ActivityEntity.class, activity.getId());
        
        removeRelationshipRefs(sender, entity);
      }
    } catch (NodeNotFoundException e) {
      LOG.warn("Failed to delete Activity references when delete relationship.");
    }
  }
  
  /**
   * The reference types.
   */
  public enum ActivityRefType {
    FEED() {
      @Override
      public ActivityRefListEntity refsOf(IdentityEntity identityEntity) {
        return identityEntity.getAllStream();
      }
    },
    CONNECTION() {
      @Override
      public ActivityRefListEntity refsOf(IdentityEntity identityEntity) {
        return identityEntity.getConnectionStream();
      }
    },
    MY_SPACES() {
      @Override
      public ActivityRefListEntity refsOf(IdentityEntity identityEntity) {
        return identityEntity.getSpaceStream();
      }
    },
    MY_ACTIVITIES() {
      @Override
      public ActivityRefListEntity refsOf(IdentityEntity identityEntity) {
        return identityEntity.getMyStream();
      }
    };

    public abstract ActivityRefListEntity refsOf(IdentityEntity identityEntity);
  }
  
  private List<ExoSocialActivity> getActivities(ActivityRefType type, Identity owner, int offset, int limit) {
    List<ExoSocialActivity> got = new LinkedList<ExoSocialActivity>();
    try {
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, owner.getRemoteId());
      ActivityRefListEntity refList = type.refsOf(identityEntity);
      ActivityRefList list = new ActivityRefList(refList);
      
      int nb = 0;
      
      Iterator<ActivityRef> it = list.iterator();

      _skip(it, offset);

      while (it.hasNext()) {
        ActivityRef current = it.next();

        //take care in the case, current.getActivityEntity() = null the same SpaceRef, need to remove it out
        if (current.getActivityEntity() == null) {
          current.getDay().getActivityRefs().remove(current.getName());
          continue;
        }
        
        //
        got.add(getStorage().getActivity(current.getActivityEntity().getId()));

        if (++nb == limit) {
          break;
        }

      }
      
    } catch (NodeNotFoundException e) {
      LOG.warn("Failed to getActivities()");
    }
    
    return got;
  }
  
  private int getNumberOfActivities(ActivityRefType type, Identity owner) {
    try {
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, owner.getRemoteId());
      ActivityRefListEntity refList = type.refsOf(identityEntity);
      
      return refList.getNumber().intValue();
    } catch (NodeNotFoundException e) {
      LOG.warn("Failed to getNumberOfActivities()");
    }
    
    return 0;
  }
  
  /**
   * {@inheritDoc}
   */
  private List<ExoSocialActivity> getActivitiesOfConnections(Identity ownerIdentity) {

    List<Identity> connections = new ArrayList<Identity>();
    
    if (ownerIdentity == null ) {
      return Collections.emptyList();
    }
    
    connections.add(ownerIdentity);
    
    //
    ActivityFilter filter = new ActivityFilter(){};

    //
    return getActivitiesOfIdentities(ActivityBuilderWhere.simple().owners(connections), filter, 0, -1);
  }
  
  /**
   * {@inheritDoc}
   */
  private List<ExoSocialActivity> getActivitiesOfIdentities(ActivityBuilderWhere where, ActivityFilter filter,
                                                           long offset, long limit) throws ActivityStorageException {

    QueryResult<ActivityEntity> results = getActivitiesOfIdentitiesQuery(where, filter).objects(offset, limit);

    List<ExoSocialActivity> activities =  new ArrayList<ExoSocialActivity>();

    while(results.hasNext()) {
      activities.add(getStorage().getActivity(results.next().getId()));
    }

    return activities;
  }
  
  
  private Query<ActivityEntity> getActivitiesOfIdentitiesQuery(ActivityBuilderWhere whereBuilder,
                                                               JCRFilterLiteral filter) throws ActivityStorageException {

    QueryBuilder<ActivityEntity> builder = getSession().createQueryBuilder(ActivityEntity.class);

    builder.where(whereBuilder.build(filter));
    whereBuilder.orderBy(builder, filter);

    return builder.get();
  }
  
  private void createOwnerRefs(Identity owner, ActivityEntity activityEntity) throws NodeNotFoundException {
    manageRefList(new UpdateContext(owner, null), activityEntity, ActivityRefType.FEED);
    
    if (OrganizationIdentityProvider.NAME.equals(owner.getProviderId())) {
      manageRefList(new UpdateContext(owner, null), activityEntity, ActivityRefType.MY_ACTIVITIES);
    } else if (SpaceIdentityProvider.NAME.equals(owner.getProviderId())) {
      manageRefList(new UpdateContext(owner, null), activityEntity, ActivityRefType.MY_SPACES);
    }
    
  }
  
  private void createConnectionsRefs(List<Identity> identities, ActivityEntity activityEntity) throws NodeNotFoundException {
    manageRefList(new UpdateContext(identities, null), activityEntity, ActivityRefType.FEED);
    manageRefList(new UpdateContext(identities, null), activityEntity, ActivityRefType.CONNECTION);
  }
  
  private void createConnectionsRefs(Identity identity, ActivityEntity activityEntity) throws NodeNotFoundException {
    manageRefList(new UpdateContext(identity, null), activityEntity, ActivityRefType.FEED);
    manageRefList(new UpdateContext(identity, null), activityEntity, ActivityRefType.CONNECTION);
  }
  
  private void removeRelationshipRefs(Identity identity, ActivityEntity activityEntity) throws NodeNotFoundException {
    manageRefList(new UpdateContext(null, identity), activityEntity, ActivityRefType.FEED);
    manageRefList(new UpdateContext(null, identity), activityEntity, ActivityRefType.CONNECTION);
  }
  
  private void removeOwnerRefs(Identity owner, ActivityEntity activityEntity) throws NodeNotFoundException {
    manageRefList(new UpdateContext(null, owner), activityEntity, ActivityRefType.FEED);
    
    //
    if (OrganizationIdentityProvider.NAME.equals(owner.getProviderId())) {
      manageRefList(new UpdateContext(null, owner), activityEntity, ActivityRefType.MY_ACTIVITIES);
    } else if (SpaceIdentityProvider.NAME.equals(owner.getProviderId())) {
      manageRefList(new UpdateContext(null, owner), activityEntity, ActivityRefType.MY_SPACES);
    }
  }
  
  private void removeConnectionsRefs(List<Identity> identities, ActivityEntity activityEntity) throws NodeNotFoundException {
    manageRefList(new UpdateContext(null, identities), activityEntity, ActivityRefType.FEED);
    manageRefList(new UpdateContext(null, identities), activityEntity, ActivityRefType.CONNECTION);
  }
  
  private void createSpaceRefs(List<Identity> identities, ActivityEntity activityEntity) throws NodeNotFoundException {
    manageRefList(new UpdateContext(identities, null), activityEntity, ActivityRefType.MY_SPACES);
  }
  
  private void manageRefList(UpdateContext context, ActivityEntity activityEntity, ActivityRefType type) throws NodeNotFoundException {

    if (context.getAdded() != null) {
      for (Identity identity : context.getAdded()) {
        IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, identity.getRemoteId());

        ActivityRefListEntity listRef = type.refsOf(identityEntity);
        ActivityRef ref = listRef.get(activityEntity);
        if (!ref.getName().equals(activityEntity.getName())) {
          ref.setName(activityEntity.getName());
        }

        if (ref.getLastUpdated() == null || ref.getLastUpdated().longValue() != activityEntity.getLastUpdated().longValue()) {
          ref.setLastUpdated(activityEntity.getLastUpdated());
        }

        ref.setActivityEntity(activityEntity);

      }
    }
    
    if (context.getRemoved() != null) {

      for (Identity identity : context.getRemoved()) {
        try {
          IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, identity.getRemoteId());
          
          ActivityRefListEntity listRef = type.refsOf(identityEntity);
          listRef.remove(activityEntity);
        }
        catch (NodeNotFoundException e) {
          LOG.warn(e.getMessage(), e);
        }
      }
    }
  }
  
  private class UpdateContext {
    private List<Identity> added;
    private List<Identity> removed;

    private UpdateContext(List<Identity> added, List<Identity> removed) {
      this.added = added;
      this.removed = removed;
    }
    
    private UpdateContext(Identity added, Identity removed) {
      if (added != null) {
        this.added = new CopyOnWriteArrayList<Identity>();
        this.added.add(added);
      }
      
      //
      if (removed != null) {
        this.removed = new CopyOnWriteArrayList<Identity>();
        this.removed.add(removed);
      }
    }

    public List<Identity> getAdded() {
      return added == null ? new ArrayList<Identity>() : added;
    }

    public List<Identity> getRemoved() {
      return removed == null ? new ArrayList<Identity>() : removed;
    }
  }

}
