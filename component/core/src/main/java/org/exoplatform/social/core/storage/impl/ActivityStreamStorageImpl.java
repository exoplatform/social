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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.ArrayUtils;
import org.chromattic.api.query.Ordering;
import org.chromattic.api.query.Query;
import org.chromattic.api.query.QueryBuilder;
import org.chromattic.api.query.QueryResult;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.service.ProcessContext;
import org.exoplatform.social.common.service.utils.ObjectHelper;
import org.exoplatform.social.core.activity.filter.ActivityFilter;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.chromattic.entity.ActivityEntity;
import org.exoplatform.social.core.chromattic.entity.ActivityRef;
import org.exoplatform.social.core.chromattic.entity.ActivityRefListEntity;
import org.exoplatform.social.core.chromattic.entity.HidableEntity;
import org.exoplatform.social.core.chromattic.entity.IdentityEntity;
import org.exoplatform.social.core.chromattic.entity.StreamsEntity;
import org.exoplatform.social.core.chromattic.filter.JCRFilterLiteral;
import org.exoplatform.social.core.chromattic.utils.ActivityRefIterator;
import org.exoplatform.social.core.chromattic.utils.ActivityRefList;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.ActivityStreamStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.exception.NodeNotFoundException;
import org.exoplatform.social.core.storage.query.JCRProperties;
import org.exoplatform.social.core.storage.query.WhereExpression;
import org.exoplatform.social.core.storage.streams.StreamProcessContext;

public class ActivityStreamStorageImpl extends AbstractStorage implements ActivityStreamStorage {
  
  /**
   * The identity storage
   */
  private final IdentityStorageImpl identityStorage;
  
  /**
   * The space storage
   */
  private SpaceStorage spaceStorage;
  

  /**
   * The relationship storage
   */
  private RelationshipStorage relationshipStorage;
  
  /**
   * The activity storage
   */
  private ActivityStorage activityStorage;
  
  /** Logger */
  private static final Log LOG = ExoLogger.getLogger(ActivityStreamStorageImpl.class);
  
  public ActivityStreamStorageImpl(IdentityStorageImpl identityStorage) {
    this.identityStorage = identityStorage;
  }
  
  private ActivityStorage getStorage() {
    if (activityStorage == null) {
      activityStorage = (ActivityStorage) PortalContainer.getInstance().getComponentInstanceOfType(ActivityStorage.class);
    }
    
    return activityStorage;
  }
  
  private SpaceStorage getSpaceStorage() {
    if (spaceStorage == null) {
      spaceStorage = (SpaceStorage) PortalContainer.getInstance().getComponentInstanceOfType(SpaceStorage.class);
    }
    
    return this.spaceStorage;
  }
  
  private RelationshipStorage getRelationshipStorage() {
    if (relationshipStorage == null) {
      relationshipStorage = (RelationshipStorage) PortalContainer.getInstance().getComponentInstanceOfType(RelationshipStorage.class);
    }
    
    return this.relationshipStorage;
  }

  @Override
  public void save(ProcessContext ctx) {
    //must call with asynchronous
    try {
      StreamProcessContext streamCtx = ObjectHelper.cast(StreamProcessContext.class, ctx);
      Identity owner = streamCtx.getIdentity();
      //
      ActivityEntity activityEntity = _findById(ActivityEntity.class, streamCtx.getActivity().getId());     
      if (OrganizationIdentityProvider.NAME.equals(owner.getProviderId())) {
        user(owner, activityEntity);
        //mention case
        addMentioner(streamCtx.getMentioners(), activityEntity);
      } else if (SpaceIdentityProvider.NAME.equals(owner.getProviderId())) {
        //records to Space Streams for SpaceIdentity
        space(owner, activityEntity);
        //mention case
        addMentioner(streamCtx.getMentioners(), activityEntity);
      }
    } catch (NodeNotFoundException e) {
      ctx.setException(e);
      LOG.warn("Failed to add Activity Relations references.", e);
    }
  }
  
  @Override
  public void savePoster(ProcessContext ctx) {
    //call synchronous
    try {
      StreamProcessContext streamCtx = ObjectHelper.cast(StreamProcessContext.class, ctx);
      Identity owner = streamCtx.getIdentity();
      //
      ActivityEntity activityEntity = _findById(ActivityEntity.class, streamCtx.getActivity().getId());     
      if (OrganizationIdentityProvider.NAME.equals(owner.getProviderId())) {
        createOwnerRefs(owner, activityEntity);
      } else if (SpaceIdentityProvider.NAME.equals(owner.getProviderId())) {
        //
        manageRefList(new UpdateContext(owner, null), activityEntity, ActivityRefType.SPACE_STREAM);
        //
        Identity ownerPosterOnSpace = identityStorage.findIdentityById(activityEntity.getPosterIdentity().getId());
        ownerSpaceMembersRefs(ownerPosterOnSpace, activityEntity);
      }
    } catch (NodeNotFoundException e) {
      ctx.setException(e);
      LOG.warn("Failed to add Activity references.", e);
    }
  }
  
  private void user(Identity owner, ActivityEntity activityEntity) throws NodeNotFoundException {
    //
    List<Identity> got = getRelationshipStorage().getConnections(owner);
    if (got.size() > 0) {
      createConnectionsRefs(got, activityEntity);
    }
  }
  
  private void removeMentioner(String[] identityIds, ActivityEntity activityEntity) throws NodeNotFoundException {
   if (identityIds != null && identityIds.length > 0) {
     for(String identityId : identityIds) {
       Identity identity = identityStorage.findIdentityById(identityId);
       removeOwnerRefs(identity, activityEntity);
     }
   }
  }
  
  private void removeCommenter(String[] identityIds, ActivityEntity activityEntity) throws NodeNotFoundException {
    if (identityIds != null && identityIds.length > 0) {
      for(String identityId : identityIds) {
        Identity identity = identityStorage.findIdentityById(identityId);
        manageRefList(new UpdateContext(null, identity), activityEntity, ActivityRefType.MY_ACTIVITIES);
      }
    }
  }
  
  private void addMentioner(String[] identityIds, ActivityEntity activityEntity) throws NodeNotFoundException {
    if (identityIds != null && identityIds.length > 0) {
      for(String identityId : identityIds) {
        Identity identity = identityStorage.findIdentityById(identityId);
        createOwnerRefs(identity, activityEntity);
      }
    }
   }
   
   private void addCommenter(String[] identityIds, ActivityEntity activityEntity) throws NodeNotFoundException {
     if (identityIds != null && identityIds.length > 0) {
       for(String identityId : identityIds) {
         Identity identity = identityStorage.findIdentityById(identityId);
         createOwnerRefs(identity, activityEntity);
       }
     }
   }

  private void space(Identity owner, ActivityEntity activityEntity) throws NodeNotFoundException {
    Space space = getSpaceStorage().getSpaceByPrettyName(owner.getRemoteId());
    
    if (space == null) return;
    //Don't create ActivityRef on space stream for given SpaceIdentity
    List<Identity> identities = getMemberIdentities(space);
    createSpaceMembersRefs(identities, activityEntity);
  }

  private List<Identity> getMemberIdentities(Space space) {
    List<Identity> identities = new ArrayList<Identity>();
    for(String remoteId : space.getMembers()) {
      identities.add(identityStorage.findIdentity(OrganizationIdentityProvider.NAME, remoteId));
    }
    
    return identities;
  }
  
  @Override
  public void delete(String activityId) {
    try {
      //
      ActivityEntity activityEntity = _findById(ActivityEntity.class, activityId);
      
      Collection<ActivityRef> references = activityEntity.getActivityRefs();
      
      List<ActivityRefListEntity> refList = new ArrayList<ActivityRefListEntity>(); 
      //
      for(ActivityRef ref : references) {
        
        //
        refList.add(ref.getDay().getMonth().getYear().getList());
      }
      
      for(ActivityRefListEntity list : refList) {
        list.remove(activityEntity.getLastUpdated());
      }
      
      
    } catch (NodeNotFoundException e) {
      LOG.warn("Failed to delete Activities references.", e);
    }
  }
  
  @Override
  public void like(Identity liker, ExoSocialActivity activity) {
    try {
      //
      ActivityEntity entity = _findById(ActivityEntity.class, activity.getId());
      
      manageRefList(new UpdateContext(liker, null), entity, ActivityRefType.FEED);
      manageRefList(new UpdateContext(liker, null), entity, ActivityRefType.MY_ACTIVITIES);
      
    } catch (NodeNotFoundException e) {
      LOG.warn("Failed to make Activity References for like case.");
    }
    
  }
  
  @Override
  public void unLike(Identity removedLike, ExoSocialActivity activity) {
    try {
      //
      ActivityEntity entity = _findById(ActivityEntity.class, activity.getId());
      
      //manageRefList(new UpdateContext(null, removedLike), entity, ActivityRefType.FEED);
      boolean notDelete = ArrayUtils.contains(activity.getCommentedIds(), removedLike.getId());
      
      notDelete |= hasMentioned(removedLike, activity);
      
      if (notDelete) return;
      
      manageRefList(new UpdateContext(null, removedLike), entity, ActivityRefType.FEED);
      manageRefList(new UpdateContext(null, removedLike), entity, ActivityRefType.MY_ACTIVITIES);
      
    } catch (NodeNotFoundException e) {
      LOG.warn("Failed to delete Activity References for unlike case.");
    }
  }
  
  private boolean hasMentioned(Identity removedLike, ExoSocialActivity activity) {
    for(String id : activity.getMentionedIds()) {
      if (id.indexOf(removedLike.getId()) > -1) {
        return true;
      }
    }
    return false;
  }
  
  @Override
  public void updateCommenter(ProcessContext ctx) {
    
    try {
      StreamProcessContext streamCtx = ObjectHelper.cast(StreamProcessContext.class, ctx);
      ExoSocialActivity activity = streamCtx.getActivity();
      Identity commenter = streamCtx.getIdentity();
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(commenter.getProviderId(), commenter.getRemoteId());
      ActivityEntity activityEntity = _findById(ActivityEntity.class, activity.getId());
      
      QueryResult<ActivityRef> got = getActivityRefs(identityEntity, activityEntity);
      ActivityRef activityRef = null;
      while(got.hasNext()) {
        activityRef = got.next();
        activityRef.setName("" + activity.getUpdated().getTime());
        activityRef.setLastUpdated(activity.getUpdated().getTime());
      }
      
      long oldUpdated = streamCtx.getOldLastUpdated();  
      //activity's poster != comment's poster
      //don't have on My Activity stream
      boolean has = hasActivityRefs(identityEntity, activityEntity, ActivityRefType.MY_ACTIVITIES, oldUpdated);
      if (has == false) {
        manageRefList(new UpdateContext(commenter, null), activityEntity, ActivityRefType.MY_ACTIVITIES);
      }
      //post comment also put the activity on feed if have not any
      has = hasActivityRefs(identityEntity, activityEntity, ActivityRefType.FEED, oldUpdated);
      if (has == false) {
        manageRefList(new UpdateContext(commenter, null), activityEntity, ActivityRefType.FEED);
      }
      //create activityref for owner's activity
      createRefForPoster(activityEntity, oldUpdated);
    } catch (NodeNotFoundException e) {
      LOG.warn("Failed to updateCommenter Activity references.");
    }
  }

  private void createRefForPoster(ActivityEntity activityEntity, long oldUpdated) throws NodeNotFoundException {
    boolean has;
    //poster if not migration
    IdentityEntity posterIdentity = activityEntity.getIdentity();
    has = hasActivityRefs(posterIdentity, activityEntity, ActivityRefType.MY_ACTIVITIES, oldUpdated);
    if (has == false) {
      addRefList(posterIdentity, activityEntity, ActivityRefType.MY_ACTIVITIES, false);
      LOG.debug("createRefForPoster::MyActivities stream :" + posterIdentity.getRemoteId());
    }
    //post comment also put the activity on feed if have not any
    has = hasActivityRefs(posterIdentity, activityEntity, ActivityRefType.FEED, oldUpdated);
    if (has == false) {
      addRefList(posterIdentity, activityEntity, ActivityRefType.FEED, false);
      LOG.debug("createRefForPoster::Feed stream :" + posterIdentity.getRemoteId());
    }
  }
  
  @Override
  public void update(ProcessContext ctx) {
    
    try {
      StreamProcessContext streamCtx = ObjectHelper.cast(StreamProcessContext.class, ctx);
      ExoSocialActivity activity = streamCtx.getActivity();

      ActivityEntity activityEntity = _findById(ActivityEntity.class, activity.getId());
      Collection<ActivityRef> references = activityEntity.getActivityRefs();
      Set<String> ids = new HashSet<String>();
        
      for (ActivityRef ref : references) {
        ids.add(ref.getId());
      }
      
      if (ids.size() > 0) {
        for(String id : ids) {
          ActivityRef old =_findById(ActivityRef.class, id);
          LOG.debug("ActivityRef will be deleted: " + old.toString());
          ActivityRefListEntity refList = old.getDay().getMonth().getYear().getList();
          //
          if (refList.isOnlyUpdate(old, activity.getUpdated().getTime())) {
            old.setName("" + activity.getUpdated().getTime());
            old.setLastUpdated(activity.getUpdated().getTime());
            //handle in the case injection, there are a lot of updating activity short time.
            getSession().save();
          } else {
            ActivityRef newRef = refList.getOrCreated(activity.getUpdated().getTime());
            newRef.setLastUpdated(activity.getUpdated().getTime());
            newRef.setActivityEntity(activityEntity);
            getSession().remove(old);
          }
          
        }
      }
      //mentioners
      addMentioner(streamCtx.getMentioners(), activityEntity);
    } catch (Exception e) {
      LOG.warn("Failed to update Activity references.", e);
    }
  }
  
  @Override
  public void updateHidable(ProcessContext ctx) {
    
    try {
      StreamProcessContext streamCtx = ObjectHelper.cast(StreamProcessContext.class, ctx);
      ExoSocialActivity activity = streamCtx.getActivity();

      ActivityEntity activityEntity = _findById(ActivityEntity.class, activity.getId());
      Collection<ActivityRef> references = activityEntity.getActivityRefs();
      
      //Case of update hidden activity after migration
      if (references == null || references.size() == 0) {
        savePoster(ctx);
        save(ctx);
      }
        
      HidableEntity hidableActivity = _getMixin(activityEntity, HidableEntity.class, true);
      hidableActivity.setHidden(activity.isHidden());
      for (ActivityRef ref : references) {
        if (hidableActivity.getHidden() == false) {
          ref.getDay().inc();
        } else {
          ref.getDay().desc();
        }
      }
      
    } catch (Exception e) {
      LOG.warn("Failed to update Activity references when change the visibility of activity.", e);
    }
  }

  /**
  @Override
  public void update(ProcessContext ctx) {
    
    try {
      StreamProcessContext streamCtx = ObjectHelper.cast(StreamProcessContext.class, ctx);
      ExoSocialActivity activity = streamCtx.getActivity();

      ActivityEntity activityEntity = _findById(ActivityEntity.class, activity.getId());
      Collection<ActivityRef> references = activityEntity.getActivityRefs();

      for (ActivityRef ref : references) {
        if (_hasMixin(ref, HidableEntity.class) == false) {
          _getMixin(ref, HidableEntity.class, true);
        }
        
        ref.setName("" + activity.getUpdated().getTime());
        ref.setLastUpdated(activity.getUpdated().getTime());

      }
      //mentioners
      addMentioner(streamCtx.getMentioners(), activityEntity);
    } catch (NodeNotFoundException e) {
      LOG.warn("Failed to update Activity references.");
    }
  }*/
  
  @Override
  public void deleteComment(ProcessContext ctx) {
    
    try {
      StreamProcessContext streamCtx = ObjectHelper.cast(StreamProcessContext.class, ctx);
      ExoSocialActivity activity = streamCtx.getActivity();

      //
      ActivityEntity activityEntity = _findById(ActivityEntity.class, activity.getId());
      
      //mentioners
      removeMentioner(streamCtx.getMentioners(), activityEntity);
      //commenter
      removeCommenter(streamCtx.getCommenters(), activityEntity);
    } catch (NodeNotFoundException e) {
      LOG.warn("Failed to delete Activity references for mentioner and commenter.");
    }
  }
  
  @Override
  public void addSpaceMember(ProcessContext ctx) {
    try {
      
      StreamProcessContext streamCtx = ObjectHelper.cast(StreamProcessContext.class, ctx);
      createSpaceMemberRefs(streamCtx.getIdentity(), streamCtx.getSpaceIdentity());
      
    } catch (NodeNotFoundException e) {
      LOG.warn("Failed to addSpaceMember Activity references.");
    }
    
  }
  
  @Override
  public void removeSpaceMember(ProcessContext ctx) {
    try {
      StreamProcessContext streamCtx = ObjectHelper.cast(StreamProcessContext.class, ctx);
      removeSpaceMemberRefs(streamCtx.getIdentity(), streamCtx.getSpaceIdentity());
    } catch (NodeNotFoundException e) {
      LOG.warn("Failed to removeSpaceMember Activity references.");
    }
    
  }

  

  @Override
  public List<ExoSocialActivity> getFeed(Identity owner, int offset, int limit) {
    return getActivitiesNotQuery(ActivityRefType.FEED, owner, offset, limit);
  }

  @Override
  public int getNumberOfFeed(Identity owner) {
    return getNumberOfActivities(ActivityRefType.FEED, owner);
  }

  @Override
  public List<ExoSocialActivity> getConnections(Identity owner, int offset, int limit) {
    
    return getActivitiesNotQuery(ActivityRefType.CONNECTION, owner, offset, limit);
  }

  @Override
  public int getNumberOfConnections(Identity owner) {
    return getNumberOfActivities(ActivityRefType.CONNECTION, owner);
  }

  @Override
  public List<ExoSocialActivity> getMySpaces(Identity owner, int offset, int limit) {
    return getActivitiesNotQuery(ActivityRefType.MY_SPACES, owner, offset, limit);
  }

  @Override
  public int getNumberOfMySpaces(Identity owner) {
    return getNumberOfActivities(ActivityRefType.MY_SPACES, owner);
  }
  
  @Override
  public List<ExoSocialActivity> getSpaceStream(Identity owner, int offset, int limit) {
    return getActivities(ActivityRefType.SPACE_STREAM, owner, offset, limit);
  }

  @Override
  public int getNumberOfSpaceStream(Identity owner) {
    return getNumberOfActivities(ActivityRefType.SPACE_STREAM, owner);
  }

  @Override
  public List<ExoSocialActivity> getMyActivities(Identity owner, int offset, int limit) {
    return getActivitiesNotQuery(ActivityRefType.MY_ACTIVITIES, owner, offset, limit);
  }

  @Override
  public int getNumberOfMyActivities(Identity owner) {
    return getNumberOfActivities(ActivityRefType.MY_ACTIVITIES, owner);
  }

  @Override
  public void connect(Identity sender, Identity receiver) {
    try {
      //
      QueryResult<ActivityEntity> activities = getActivitiesOfConnections(sender);
      
      
      IdentityEntity receiverEntity = identityStorage._findIdentityEntity(receiver.getProviderId(), receiver.getRemoteId());
      
      if (activities != null) {
        while(activities.hasNext()) {
          ActivityEntity entity = activities.next();
          
          //has on sender stream
          if (isExistingActivityRef(receiverEntity, entity)) continue;
          
          //
          createConnectionsRefs(receiver, entity);
        }
      }
      
      //
      IdentityEntity senderEntity = identityStorage._findIdentityEntity(sender.getProviderId(), sender.getRemoteId());
      activities = getActivitiesOfConnections(receiver);
      if (activities != null) {
        while(activities.hasNext()) {
          ActivityEntity entity = activities.next();

          //has on receiver stream
          if (isExistingActivityRef(senderEntity, entity)) continue;
          
          //
          createConnectionsRefs(sender, entity);
        }
      }
      
    } catch (NodeNotFoundException e) {
      LOG.warn("Failed to add Activity references when create relationship.");
    }
  }
  
  @Override
  public void deleteConnect(Identity sender, Identity receiver) {
    
    try {
      //
      QueryResult<ActivityEntity> activities = getActivitiesOfConnections(sender);
      
      if (activities != null) {
        while(activities.hasNext()) {
          ActivityEntity entity = activities.next();
          removeRelationshipRefs(receiver, entity);
        }
      }
      
      //
      activities = getActivitiesOfConnections(receiver);
      if (activities != null) {
        while(activities.hasNext()) {
          ActivityEntity entity = activities.next();
          removeRelationshipRefs(sender, entity);
        }
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
        if (identityEntity.getStreams() == null) return create(identityEntity);
        if (identityEntity.getStreams().getOwner() == null) return create(identityEntity);

        return identityEntity.getStreams().getAll();
      }
      
      @Override
      public ActivityRefListEntity create(IdentityEntity identityEntity) {
        StreamsEntity streams = identityEntity.getStreams();
        
        if (streams == null) {
          streams = identityEntity.createStreams();
          identityEntity.setStreams(streams);
          
        }
        
        ActivityRefListEntity refList = streams.getAll() == null ?  streams.createAllStream() : streams.getAll();
        return refList;
      }

    },
    CONNECTION() {
      @Override
      public ActivityRefListEntity refsOf(IdentityEntity identityEntity) {
        if (identityEntity.getStreams() == null) return create(identityEntity);
        if (identityEntity.getStreams().getOwner() == null) return create(identityEntity);
        
        return identityEntity.getStreams().getConnections();
      }
      
      @Override
      public ActivityRefListEntity create(IdentityEntity identityEntity) {
        StreamsEntity streams = identityEntity.getStreams();
        
        if (streams == null) {
          streams = identityEntity.createStreams();
          identityEntity.setStreams(streams);
          
        }
        
        ActivityRefListEntity refList = streams.getConnections() == null ?  streams.createConnectionsStream() : streams.getConnections();
        return refList;
      }
    },
    MY_SPACES() {
      @Override
      public ActivityRefListEntity refsOf(IdentityEntity identityEntity) {
        if (identityEntity.getStreams() == null) return create(identityEntity);
        if (identityEntity.getStreams().getOwner() == null) return create(identityEntity);
        
        return identityEntity.getStreams().getMySpaces();
      }
      
      @Override
      public ActivityRefListEntity create(IdentityEntity identityEntity) {
        StreamsEntity streams = identityEntity.getStreams();
        
        if (streams == null) {
          streams = identityEntity.createStreams();
          identityEntity.setStreams(streams);
          
        }
        
        ActivityRefListEntity refList = streams.getMySpaces() == null ?  streams.createMySpacesStream() : streams.getMySpaces();
        return refList;
      }
    },
    SPACE_STREAM() {
      @Override
      public ActivityRefListEntity refsOf(IdentityEntity identityEntity) {
        if (identityEntity.getStreams() == null) return create(identityEntity);
        if (identityEntity.getStreams().getOwner() == null) return create(identityEntity);
        
        return identityEntity.getStreams().getSpace();
      }
      
      @Override
      public ActivityRefListEntity create(IdentityEntity identityEntity) {
        StreamsEntity streams = identityEntity.getStreams();
        
        if (streams == null) {
          streams = identityEntity.createStreams();
          identityEntity.setStreams(streams);
          
        }
        
        ActivityRefListEntity refList = streams.getSpace() == null ?  streams.createSpaceStream() : streams.getSpace();
        return refList;
      }
    },
    MY_ACTIVITIES() {
      @Override
      public ActivityRefListEntity refsOf(IdentityEntity identityEntity) {
        if (identityEntity.getStreams() == null) return create(identityEntity);
        
        if (identityEntity.getStreams().getOwner() == null) return create(identityEntity);
          
        return identityEntity.getStreams().getOwner();
      }

      @Override
      public ActivityRefListEntity create(IdentityEntity identityEntity) {
        StreamsEntity streams = identityEntity.getStreams();
        
        if (streams == null) {
          streams = identityEntity.createStreams();
          identityEntity.setStreams(streams);
          
        }
        
        ActivityRefListEntity refList = streams.getOwner() == null ?  streams.createOwnerStream() : streams.getOwner();
        return refList;
      }
    };

    public abstract ActivityRefListEntity refsOf(IdentityEntity identityEntity);
    
    public abstract ActivityRefListEntity create(IdentityEntity identityEntity);
  }
  
  private List<ExoSocialActivity> getActivities(ActivityRefType type, Identity owner, int offset, int limit) {
    List<ExoSocialActivity> got = new LinkedList<ExoSocialActivity>();
    try {
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(owner.getProviderId(), owner.getRemoteId());
      
      Iterator<ActivityRef> it = getActivityRefs(identityEntity, type, offset, limit);

      while (it.hasNext()) {
        ActivityRef current = it.next();

        //take care in the case, current.getActivityEntity() = null the same ActivityRef, need to remove it out
        if (current.getActivityEntity() == null) {
          current.getDay().getActivityRefs().remove(current.getName());
          continue;
        }
        
        //
        ExoSocialActivity a = getStorage().getActivity(current.getActivityEntity().getId());
        if (a.isHidden() == true) {
          continue;
        }
        got.add(a);

      }
      
    } catch (NodeNotFoundException e) {
      LOG.warn("Failed to getActivities()");
    }
    
    return got;
  }
  
  private List<ExoSocialActivity> getActivitiesNotQuery(ActivityRefType type, Identity owner, int offset, int limit) {
    List<ExoSocialActivity> got = new LinkedList<ExoSocialActivity>();
    try {
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(owner.getProviderId(), owner.getRemoteId());
      
      ActivityRefListEntity refList = type.refsOf(identityEntity);
      ActivityRefList list = new ActivityRefList(refList);

      int nb = 0;
      ActivityRefIterator it = list.iterator();
      _skip(it, offset);
      while (it.hasNext()) {
        ActivityRef current = it.next();
        // take care in the case, current.getActivityEntity() = null the same
        // SpaceRef, need to remove it out
        if (current.getActivityEntity() == null) {
          current.getDay().getActivityRefs().remove(current.getName());
          continue;
        }
        ExoSocialActivity activity = getStorage().getActivity(current.getActivityEntity().getId());
        if (activity.isHidden() == true) {
          continue;
        }
        got.add(activity);
        if (++nb == limit) {
          break;
        }
      }
    } catch (NodeNotFoundException e) {
      LOG.warn("Failed to activities!");
    }
    return got;
  }
  
  
  private int getNumberOfActivities(ActivityRefType type, Identity owner) {
    try {
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(owner.getProviderId(), owner.getRemoteId());
      ActivityRefListEntity refList = type.refsOf(identityEntity);
      
      if (refList == null) return 0;
      
      return refList.getNumber().intValue();
    } catch (NodeNotFoundException e) {
      LOG.warn("Failed to getNumberOfActivities()");
    }
    
    return 0;
  }
  
  
  private QueryResult<ActivityEntity> getActivitiesOfConnections(Identity ownerIdentity) {

    List<Identity> connections = new ArrayList<Identity>();
    
    if (ownerIdentity == null ) {
      return null;
    }
    
    connections.add(ownerIdentity);
    
    //
    ActivityFilter filter = ActivityFilter.newer();

    //
    return getActivitiesOfIdentities(ActivityBuilderWhere.simple().owners(connections), filter, 0, -1);
  }
  
  private QueryResult<ActivityEntity> getActivitiesOfSpace(Identity spaceIdentity) {

    if (spaceIdentity == null) {
      return null;
    }

    //
    ActivityFilter filter = ActivityFilter.space();

    //
    return getActivitiesOfIdentities(ActivityBuilderWhere.space().owners(spaceIdentity), filter, 0, -1);
  }
  
  /**
   * {@inheritDoc}
   */
  private QueryResult<ActivityEntity> getActivitiesOfIdentities(ActivityBuilderWhere where, ActivityFilter filter,
                                                           long offset, long limit) throws ActivityStorageException {
    return getActivitiesOfIdentitiesQuery(where, filter).objects(offset, limit);
  }
  
  
  private Query<ActivityEntity> getActivitiesOfIdentitiesQuery(ActivityBuilderWhere whereBuilder,
                                                               JCRFilterLiteral filter) throws ActivityStorageException {

    QueryBuilder<ActivityEntity> builder = getSession().createQueryBuilder(ActivityEntity.class);

    builder.where(whereBuilder.build(filter));
    whereBuilder.orderBy(builder, filter);

    return builder.get();
  }
  
  private boolean isExistingActivityRef(IdentityEntity identityEntity, ActivityEntity activityEntity) throws NodeNotFoundException {
    return getActivityRefs(identityEntity, activityEntity).size() > 0;
  }
  
  private QueryResult<ActivityRef> getActivityRefs(IdentityEntity identityEntity, ActivityEntity activityEntity) throws NodeNotFoundException {

    QueryBuilder<ActivityRef> builder = getSession().createQueryBuilder(ActivityRef.class);

    WhereExpression whereExpression = new WhereExpression();
    whereExpression.like(JCRProperties.path, identityEntity.getPath() + "/%");
    whereExpression.and().equals(ActivityRef.target, activityEntity.getId());

    builder.where(whereExpression.toString());
    
    
    return builder.get().objects();
  }
  
  private boolean hasActivityRefs(IdentityEntity identityEntity, ActivityEntity activityEntity, ActivityRefType type, long oldUpdated) throws NodeNotFoundException {
    ActivityRefListEntity refList = type.refsOf(identityEntity);
    ActivityRef ref = refList.get(oldUpdated);
    return ref != null && ref.getActivityEntity().getId() == activityEntity.getId();
  }
  
  private QueryResult<ActivityRef> getActivityRefs(IdentityEntity identityEntity, ActivityRefType type, long offset, long limit) throws NodeNotFoundException {

    QueryBuilder<ActivityRef> builder = getSession().createQueryBuilder(ActivityRef.class);

    WhereExpression whereExpression = new WhereExpression();
    ActivityRefListEntity refList = type.refsOf(identityEntity);
    whereExpression.like(JCRProperties.path, refList.getPath() + "/%");

    builder.where(whereExpression.toString());
    builder.orderBy(ActivityRef.lastUpdated.getName(), Ordering.DESC);
    builder.orderBy(JCRProperties.name.getName(), Ordering.DESC);
    return builder.get().objects(offset, limit);
  }
  
  private void createOwnerRefs(Identity owner, ActivityEntity activityEntity) throws NodeNotFoundException {
    manageRefList(new UpdateContext(owner, null), activityEntity, ActivityRefType.FEED);
    manageRefList(new UpdateContext(owner, null), activityEntity, ActivityRefType.MY_ACTIVITIES);
  }
  
  private void removeOwnerRefs(Identity owner, ActivityEntity activityEntity) throws NodeNotFoundException {
    manageRefList(new UpdateContext(null, owner), activityEntity, ActivityRefType.FEED);
    manageRefList(new UpdateContext(null, owner), activityEntity, ActivityRefType.MY_ACTIVITIES);
  }
  
  private void createConnectionsRefs(List<Identity> identities, ActivityEntity activityEntity) throws NodeNotFoundException {
    manageRefList(new UpdateContext(identities, null), activityEntity, ActivityRefType.FEED, true);
    manageRefList(new UpdateContext(identities, null), activityEntity, ActivityRefType.CONNECTION, true);
  }
  
  private void createConnectionsRefs(Identity identity, ActivityEntity activityEntity) throws NodeNotFoundException {
    manageRefList(new UpdateContext(identity, null), activityEntity, ActivityRefType.FEED, true);
    manageRefList(new UpdateContext(identity, null), activityEntity, ActivityRefType.CONNECTION, true);
  }
  
  private void removeRelationshipRefs(Identity identity, ActivityEntity activityEntity) throws NodeNotFoundException {
    manageRefList(new UpdateContext(null, identity), activityEntity, ActivityRefType.FEED);
    manageRefList(new UpdateContext(null, identity), activityEntity, ActivityRefType.CONNECTION);
  }

  private void createSpaceMembersRefs(List<Identity> identities, ActivityEntity activityEntity) throws NodeNotFoundException {
    manageRefList(new UpdateContext(identities, null), activityEntity, ActivityRefType.FEED);
    manageRefList(new UpdateContext(identities, null), activityEntity, ActivityRefType.MY_SPACES);
    //manageRefList(new UpdateContext(identities, null), activityEntity, ActivityRefType.SPACE_STREAM);
  }
  
  private void ownerSpaceMembersRefs(Identity identity, ActivityEntity activityEntity) throws NodeNotFoundException {
    manageRefList(new UpdateContext(identity, null), activityEntity, ActivityRefType.MY_ACTIVITIES);
  }
  
  private void createSpaceMembersRefs(Identity identity, ActivityEntity activityEntity) throws NodeNotFoundException {
    manageRefList(new UpdateContext(identity, null), activityEntity, ActivityRefType.FEED);
    manageRefList(new UpdateContext(identity, null), activityEntity, ActivityRefType.MY_SPACES);
  }
  
  private void createSpaceMemberRefs(Identity member, Identity space) throws NodeNotFoundException {
    QueryResult<ActivityEntity> spaceActivities = getActivitiesOfSpace(space);
    if (spaceActivities != null) {
      while(spaceActivities.hasNext()) {
        createSpaceMembersRefs(member, spaceActivities.next());
      }
    }
    
  }
  
  private void removeSpaceMemberRefs(Identity removedMember, Identity space) throws NodeNotFoundException {
    QueryResult<ActivityEntity> spaceActivities = getActivitiesOfSpace(space);
    if (spaceActivities != null) {
      while(spaceActivities.hasNext()) {
        
        ActivityEntity entity = spaceActivities.next();
        manageRefList(new UpdateContext(null, removedMember), entity, ActivityRefType.FEED);
        manageRefList(new UpdateContext(null, removedMember), entity, ActivityRefType.MY_SPACES);
      }
    }
    
  }
  
  private void manageRefList(UpdateContext context, ActivityEntity activityEntity, ActivityRefType type) throws NodeNotFoundException {
    manageRefList(context, activityEntity, type, false);
  }
  
  private void manageRefList(UpdateContext context, ActivityEntity activityEntity, ActivityRefType type, boolean mustCheck) throws NodeNotFoundException {

    AtomicBoolean newYearMonthday = new AtomicBoolean(false);
    if (context.getAdded() != null) {
      for (Identity identity : context.getAdded()) {
        IdentityEntity identityEntity = identityStorage._findIdentityEntity(identity.getProviderId(), identity.getRemoteId());

        //
        if (mustCheck) {
          //to avoid add back activity to given stream what has already existing
          if (isExistingActivityRef(identityEntity, activityEntity)) continue;
        }
        
        
        ActivityRefListEntity listRef = type.refsOf(identityEntity);
        //keep number
        Integer oldNumberOfStream = listRef.getNumber();
        
        newYearMonthday.set(false);
        ActivityRef ref = listRef.getOrCreated(activityEntity, newYearMonthday);
        
        //Take care the YearMonthDay path don't throw ADD_PROPERTY exception.
        if (newYearMonthday.get()) {
          StorageUtils.persist();
        }
        
        //LOG.info("manageRefList()::BEFORE");
        //printDebug(listRef, activityEntity.getLastUpdated());
        if (ref.getName() == null) {
          ref.setName(activityEntity.getName());
        }

        if (ref.getLastUpdated() == null) {
          ref.setLastUpdated(activityEntity.getLastUpdated());
        }

        ref.setActivityEntity(activityEntity);
        
        Integer newNumberOfStream = listRef.getNumber();
        //If activity is hidden, we must decrease the number of activity references
        HidableEntity hidableActivity = _getMixin(activityEntity, HidableEntity.class, true);
        if (hidableActivity.getHidden() && (newNumberOfStream > oldNumberOfStream)) {
          ref.getDay().desc();
        }

        //LOG.info("manageRefList()::AFTER");
        //printDebug(listRef, activityEntity.getLastUpdated());
      }
    }
    
    if (context.getRemoved() != null) {

      for (Identity identity : context.getRemoved()) {
        IdentityEntity identityEntity = identityStorage._findIdentityEntity(identity.getProviderId(), identity.getRemoteId());
          
        ActivityRefListEntity listRef = type.refsOf(identityEntity);
        listRef.remove(activityEntity);
      }
    }
  }
  
  private void addRefList(IdentityEntity identityEntity,
                          ActivityEntity activityEntity,
                          ActivityRefType type,
                          boolean mustCheck) throws NodeNotFoundException {

    AtomicBoolean newYearMonthday = new AtomicBoolean(false);
    //
    if (mustCheck) {
      // to avoid add back activity to given stream what has already existing
      if (isExistingActivityRef(identityEntity, activityEntity))
        return;
    }

    ActivityRefListEntity listRef = type.refsOf(identityEntity);

    newYearMonthday.set(false);
    ActivityRef ref = listRef.getOrCreated(activityEntity, newYearMonthday);

    // Take care the YearMonthDay path don't throw ADD_PROPERTY exception.
    if (newYearMonthday.get()) {
      StorageUtils.persist();
    }

    if (ref.getName() == null) {
      ref.setName(activityEntity.getName());
    }

    if (ref.getLastUpdated() == null) {
      ref.setLastUpdated(activityEntity.getLastUpdated());
    }

    ref.setActivityEntity(activityEntity);
  }
  /**
  private void printDebug(ActivityRefListEntity list, long oldUpdated) {
    LOG.info("printDebug::OLD Date = " + oldUpdated);
    LOG.info("printDebug::SIZE = " + list.refs(oldUpdated).size());
    LOG.info("printDebug::path = " + list.getPath());
    
    Map<String, ActivityRef> refs = list.refs(oldUpdated);
    for(Entry<String, ActivityRef> entry : refs.entrySet()) {
      if (entry.getValue() != null && entry.getValue().getActivityEntity() != null )
      LOG.info(String.format("printDebug::KEY = %s| %s", entry.getKey(), entry.getValue().toString()));
    }
  }*/
  
  @Override
  public void createFeedActivityRef(Identity owner,
                                List<ExoSocialActivity> activities) {
    createActivityRef(owner, activities, ActivityRefType.FEED);
  }
  
  @Override
  public void createConnectionsActivityRef(Identity owner,
                                    List<ExoSocialActivity> activities) {
    createActivityRef(owner, activities, ActivityRefType.CONNECTION);
       
  }
  
  @Override
  public void createMySpacesActivityRef(Identity owner,
                                           List<ExoSocialActivity> activities) {
    createActivityRef(owner, activities, ActivityRefType.MY_SPACES);
  }
  
  @Override
  public void createSpaceActivityRef(Identity owner,
                                           List<ExoSocialActivity> activities) {
    createActivityRef(owner, activities, ActivityRefType.SPACE_STREAM);
  }
  
  @Override
  public void createMyActivitiesActivityRef(Identity owner,
                                           List<ExoSocialActivity> activities) {
    createActivityRef(owner, activities, ActivityRefType.MY_ACTIVITIES);
  }

  @Override
  public void createActivityRef(Identity owner,
                                List<ExoSocialActivity> activities,
                                ActivityRefType type) {
    
    if (activities == null || activities.size() == 0) return;

    try {
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(owner.getProviderId(),
                                                                          owner.getRemoteId());
      ActivityRefListEntity listRef = type.create(identityEntity);
      // keep last migration
      ExoSocialActivity entity = activities.get(activities.size() - 1);
      Long value = entity.getUpdated() != null ? entity.getUpdated().getTime() : entity.getPostedTime();
      Long oldLastMigration = listRef.getLastMigration();
      listRef.setLastMigration(value.longValue());
      //don't increase with lazy migration.
      Integer numberOfStream = listRef.getNumber();
      
      //
      for (ExoSocialActivity a : activities) {
        ActivityEntity activityEntity = getSession().findById(ActivityEntity.class, a.getId());

        // migration 3.5.x => 4.x, lastUpdated of Activity is NULL, then use
        // createdDate for replacement
        ActivityRef ref = listRef.getOrCreated(activityEntity);
        ref.setActivityEntity(activityEntity);
      }
      
      //StorageUtils.getNode(identityEntity).save();
      //getSession().save();
      //don't increase with lazy migration if has any migration before
      if (oldLastMigration != null && oldLastMigration.longValue() > 0) {
        listRef.setNumber(numberOfStream);
      }
      
      StorageUtils.persist();
      
    } catch (NodeNotFoundException e) {
      LOG.warn("Failed to create Activity references.");
    }
  }
  
  @Override
  public void migrateStreamSize(Identity owner, int size, ActivityRefType type) {
    
    try {
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(owner.getProviderId(), owner.getRemoteId());
      ActivityRefListEntity listRef = type.create(identityEntity);
      listRef.setNumber(size);
    } catch (NodeNotFoundException e) {
      LOG.warn("Failed to migrateStreamSize.");
    } finally {
      StorageUtils.persist();
    }
  }

  @Override
  public boolean hasSizeOfFeed(Identity owner) {
    return hasSizeOfActivities(ActivityRefType.FEED, owner);
  }

  @Override
  public boolean hasSizeOfMySpaces(Identity owner) {
    return hasSizeOfActivities(ActivityRefType.MY_SPACES, owner);
  }

  @Override
  public boolean hasSizeOfSpaceStream(Identity owner) {
    return hasSizeOfActivities(ActivityRefType.SPACE_STREAM, owner);
  }

  @Override
  public boolean hasSizeOfMyActivities(Identity owner) {
    return hasSizeOfActivities(ActivityRefType.MY_ACTIVITIES, owner);
  }
  
  @Override
  public boolean hasSizeOfConnections(Identity owner) {
    return hasSizeOfActivities(ActivityRefType.CONNECTION, owner);
  }
  
  private boolean hasSizeOfActivities(ActivityRefType type, Identity owner) {
    try {
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(owner.getProviderId(), owner.getRemoteId());
      ActivityRefListEntity refList = type.refsOf(identityEntity);
      if (refList == null) return false;
      return refList.getNumber().intValue() > 0;
      //using this code
      //return refList.getSize().intValue() > 0;
    } catch (NodeNotFoundException e) {
      LOG.warn("Failed to hasSizeOfActivities()");
    }
    
    return false;
  }

  
}
