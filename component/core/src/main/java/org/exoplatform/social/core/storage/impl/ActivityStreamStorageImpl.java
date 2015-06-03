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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.ArrayUtils;
import org.chromattic.api.ChromatticException;
import org.chromattic.api.query.Ordering;
import org.chromattic.api.query.Query;
import org.chromattic.api.query.QueryBuilder;
import org.chromattic.api.query.QueryResult;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.service.ProcessContext;
import org.exoplatform.social.common.service.utils.ObjectHelper;
import org.exoplatform.social.common.service.utils.TraceElement;
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
import org.exoplatform.social.core.identity.model.ActiveIdentityFilter;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.ActivityStreamStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.exception.NodeNotFoundException;
import org.exoplatform.social.core.storage.query.ChromatticNameEncode;
import org.exoplatform.social.core.storage.query.JCRProperties;
import org.exoplatform.social.core.storage.query.WhereExpression;
import org.exoplatform.social.core.storage.streams.StreamConfig;
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
      activityStorage = CommonsUtils.getService(ActivityStorage.class);
    }
    
    return activityStorage;
  }
  
  private SpaceStorage getSpaceStorage() {
    if (spaceStorage == null) {
      spaceStorage = CommonsUtils.getService(SpaceStorage.class);
    }
    
    return this.spaceStorage;
  }
  
  private RelationshipStorage getRelationshipStorage() {
    if (relationshipStorage == null) {
      relationshipStorage = CommonsUtils.getService(RelationshipStorage.class);
    }
    
    return this.relationshipStorage;
  }

  @Override
  public void save(ProcessContext ctx) {
    //must call with asynchronous
    try {
      StreamProcessContext streamCtx = ObjectHelper.cast(StreamProcessContext.class, ctx);
      Identity owner = streamCtx.getIdentity();
      //It has been invoked by Activity Service with the multi-threading.
      //so that, gets Entity from JCR, prevent Session.logout exception.
      ActivityEntity activityEntity = null;
      try {
        activityEntity = _findById(ActivityEntity.class, streamCtx.getActivityEntity().getId());
      } catch (Exception e) {
        activityEntity = streamCtx.getActivityEntity();
      }
      
      if (OrganizationIdentityProvider.NAME.equals(owner.getProviderId())) {
        Identity poster = identityStorage.findIdentityById(activityEntity.getPosterIdentity().getId());
        user(poster, activityEntity);
        //mention case
        addMentioner(streamCtx.getMentioners(), activityEntity);
      } else if (SpaceIdentityProvider.NAME.equals(owner.getProviderId())) {
        //records to Space Streams for SpaceIdentity
        space(owner, activityEntity);
        //mention case
        addMentioner(streamCtx.getMentioners(), activityEntity);
      }
      
    } catch (Exception e) {
      ctx.setException(e);
      LOG.warn("Failed to add Activity references.", e);
      LOG.debug("Failed to add Activity references.", e);
    }
  }
  
  @Override
  public void savePoster(ProcessContext ctx) {
    //call synchronous
    try {
      StreamProcessContext streamCtx = ObjectHelper.cast(StreamProcessContext.class, ctx);
      Identity owner = streamCtx.getIdentity();
      //It has been invoked by Activity Service with the same thread.
      //so that, retrieves Entity directly from Stream context, don't spend time to get from JCR => impact performance.
      ActivityEntity activityEntity = streamCtx.getActivityEntity();
      if (OrganizationIdentityProvider.NAME.equals(owner.getProviderId())) {
        //fixed for SOC-4494 post on viewer stream
        Identity poster = identityStorage.findIdentityById(activityEntity.getPosterIdentity().getId());
        manageRefList(new UpdateContext(owner, null), activityEntity, ActivityRefType.MY_ACTIVITIES);
        createOwnerRefs(poster, activityEntity);
      } else if (SpaceIdentityProvider.NAME.equals(owner.getProviderId())) {
        //
        manageRefList(new UpdateContext(owner, null), activityEntity, ActivityRefType.SPACE_STREAM);
        //
        Identity ownerPosterOnSpace = identityStorage.findIdentityById(activityEntity.getPosterIdentity().getId());
        ownerSpaceMembersRefs(ownerPosterOnSpace, activityEntity);
      }
    } catch (NodeNotFoundException e) {
      ctx.setException(e);
      LOG.warn("Failed to add Activity references.");
      LOG.debug("Failed to add Activity references.", e);
    }
  }

  /**
  * Making the Activity Reference for the user's connections
  * 
  * @param owner
  * @param activityEntity
  * @throws NodeNotFoundException
  */
 private void user(Identity poster, ActivityEntity activityEntity) throws NodeNotFoundException, RepositoryException {
   //
   TraceElement trace = TraceElement.getInstance("creating ref-" + poster.getRemoteId() + "-" + activityEntity.getTitle());
   trace.start();
   //
   StreamConfig streamConfig = CommonsUtils.getService(StreamConfig.class);
   //get multiple user groups separates by comma. Using StringTokenizer to split
   String userGroups = streamConfig.getActiveUserGroups();
   ActiveIdentityFilter filer = new ActiveIdentityFilter(userGroups);
   Set<String> activeGroups = identityStorage.getActiveUsers(filer);
   //
   int days = streamConfig.getLastLoginAroundDays();
   filer = new ActiveIdentityFilter(days);
   int i = createRefForActiveUsers(poster, activityEntity, filer, activeGroups);
   trace.end();
   if (i > 0) {
     LOG.info("loop times = " + i + trace.toString());
   }
 }
 /**
  * Creates the activity ref for active users
  * 
  * @param owner
  * @param activityEntity
  * @param filer the days filter
  * @param activeGroups
  * @return
  * @throws NodeNotFoundException
  */
 private int createRefForActiveUsers(Identity owner,
                               ActivityEntity activityEntity,
                               ActiveIdentityFilter filer, Set<String> activeGroups) throws NodeNotFoundException, RepositoryException {
   Set<String> activeUsers = identityStorage.getActiveUsers(filer);
   //just for testing
   /**
   List<Identity> relationships = getRelationshipStorage().getConnections(owner, 0, 500);
   Set<String> activeUsers = new HashSet<String>();
   for(Identity identity : relationships) {
     activeUsers.add(identity.getRemoteId());
   }*/
   
   if (activeUsers == null) {
     activeUsers = new HashSet<String>();
   }
   //eliminate duplicate "active" user between N days list and group list 
   if (activeGroups.size() > 0) {
     activeUsers.addAll(activeGroups);
   }
   int i = activeUsers.size() > 0 ? createRefWithActiveUser(owner, activityEntity, activeUsers) : createRefWithoutActiveUser(owner, activityEntity);
   return i;
 }

 /**
  * Creates the activity ref for connections in the case.
  * Don't found any active users around days.
  *  
  * @param owner
  * @param activityEntity
  * @return
  * @throws NodeNotFoundException
  */
 private int createRefWithoutActiveUser(Identity owner,
                                        ActivityEntity activityEntity) throws NodeNotFoundException {
   StreamConfig streamConfig = CommonsUtils.getService(StreamConfig.class);
   int limitLoading = streamConfig.getLimitThresholdLoading();
   int connectionsThreshold = streamConfig.getConnectionsThreshold();

   int timesLoop = connectionsThreshold / limitLoading;
   LOG.info("Identity:" + owner.getRemoteId());
   int offset = 0;
   int i = 0;
   for (i = 0; i < timesLoop; i++) {
     List<Identity> got = getRelationshipStorage().getConnections(owner, offset, limitLoading);
     if (got.size() > 0) {
       createConnectionsRefs(got, activityEntity);
     } else {
       break;
     }
     // increase offset
     offset += limitLoading;
     StorageUtils.persist();
   }
   return i;
 }

  /**
  * Creates the activity ref for connections in the case.
  * Don't found any active users around days.
  * 
  * @param owner
  * @param activityEntity
  * @return
  * @throws NodeNotFoundException
  */
 private int createRefWithActiveUser(Identity owner, ActivityEntity activityEntity, Set<String> activeUsers) throws NodeNotFoundException, RepositoryException {
   StreamConfig streamConfig = CommonsUtils.getService(StreamConfig.class);
   int connectionsThreshold = streamConfig.getConnectionsThreshold();
   int limitLoading = streamConfig.getLimitThresholdLoading();

   LOG.debug("active users: " + (activeUsers.size() -1));
   int offset = 0;
   
   IdentityEntity ownerEntity = _findById(IdentityEntity.class, owner.getId());
   String nodePath = ownerEntity.getPath();
   StringBuilder relationshipPath = new StringBuilder();
   int batchIndex = 0;
   
   List<Identity> inputIdentities = new ArrayList<Identity>();
   
   for(String userName : activeUsers) {
     //userName is the same owner, ignore.
     if (owner.getRemoteId().equals(userName)) continue;
     //reset StringBuilder with delete(0, length)
     relationshipPath.delete(0, relationshipPath.length());
     //
     relationshipPath.append(nodePath).append("/").append(JCRProperties.RELATIONSHIP_NODE_TYPE).append("/").append("soc:").append(ChromatticNameEncode.encodeNodeName(userName));
     
     Identity identity2 = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, userName);
     
     boolean hasRelationship = getRelationshipStorage().hasRelationship(owner, identity2, relationshipPath.toString());
     
     if(hasRelationship) {
       LOG.debug("creates activity ref: " + userName);
       
       if (identity2 != null) {
         inputIdentities.add(identity2);
         batchIndex++;
         
         //handle loading limit
         if (batchIndex == limitLoading) {
           createConnectionsRefs(inputIdentities, activityEntity);
           batchIndex = 0;
           inputIdentities.clear();
           LOG.debug("start - persist to storage...");
           StorageUtils.persist();
           LOG.debug("end - persist to storage...");
         }
       }
       offset++;
       //handle connections threshold
       if (offset == connectionsThreshold) {
         break;
       }
     }//end if(relationshipNode != null)
   }
   
   if (batchIndex > 0 && inputIdentities.size() > 0) {
     createConnectionsRefs(inputIdentities, activityEntity);
   }

   return offset;
 }
  
  /**
   * Remove activity reference from "my activity stream" of an user and if he is not connected with the
   * activity's owner, remove also this reference from his "feed activity stream" 
   * 
   * @param identityIds
   * @param activityEntity
   * @throws NodeNotFoundException
   */
  private void removeActivityRefs(String[] identityIds, ActivityEntity activityEntity) throws NodeNotFoundException {
    if (identityIds != null && identityIds.length > 0) {
      Identity owner = identityStorage.findIdentityById(activityEntity.getIdentity().getId());
      for(String identityId : identityIds) {
        if (identityId.equals(owner.getId()) || identityId.equals(activityEntity.getPosterIdentity().getId())) {
          continue;
        }
        Identity identity = identityStorage.findIdentityById(identityId);
        manageRefList(new UpdateContext(null, identity), activityEntity, ActivityRefType.MY_ACTIVITIES);
        Relationship relationship = relationshipStorage.getRelationship(owner, identity);
        if (relationship == null || ! relationship.getStatus().equals(Relationship.Type.CONFIRMED)) {
          manageRefList(new UpdateContext(null, identity), activityEntity, ActivityRefType.FEED);
        }
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
      //improves performance here just load indentity data without profile
      identities.add(identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, remoteId, false));
    }
    
    return identities;
  }
  
  @Override
  public void delete(String activityId) {
    try {
      //
      ActivityEntity activityEntity = _findById(ActivityEntity.class, activityId);
      HidableEntity hidableActivity = _getMixin(activityEntity, HidableEntity.class, false);
      
      Collection<ActivityRef> references = activityEntity.getActivityRefs();
      
      List<ActivityRefListEntity> refList = new ArrayList<ActivityRefListEntity>(); 
      //
      for(ActivityRef ref : references) {
        
        //
        refList.add(ref.getDay().getMonth().getYear().getList());
      }
      
      for(ActivityRefListEntity list : refList) {
        list.remove(activityEntity, hidableActivity.getHidden(), null);
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
      Identity commenter = streamCtx.getIdentity();
      //It has been invoked by Activity Service with the same thread.
      //so that, retrieves Entity directly from Stream context, don't spend time to get from JCR => impact performance.
      ActivityEntity activityEntity = streamCtx.getActivityEntity();
      //
      long oldUpdated = streamCtx.getOldLastUpdated();  
      //activity's poster != comment's poster
      //don't have on My Activity stream
      updateCommenterActivityRefs(commenter, activityEntity, ActivityRefType.MY_ACTIVITIES, oldUpdated);
      
      //post comment also put the activity on feed if have not any
      updateCommenterActivityRefs(commenter, activityEntity, ActivityRefType.FEED, oldUpdated);
      //create activityref for owner's activity for 3.5.x
      createRefForPoster(activityEntity, oldUpdated);
    } catch (NodeNotFoundException ex) {
      LOG.warn("Probably was updated activity reference by another session");
      LOG.debug(ex.getMessage(), ex);
    } catch (ChromatticException ex) {
      LOG.warn("Probably was updated activity reference by another session");
      LOG.debug(ex.getMessage(), ex);
    }
  }
  

  private void updateCommenterActivityRefs(Identity identity, ActivityEntity activityEntity, ActivityRefType type, long oldUpdated) throws NodeNotFoundException {
    IdentityEntity identityEntity = identityStorage._findIdentityEntity(identity.getProviderId(), identity.getRemoteId());
    ActivityRefListEntity refList = type.refsOf(identityEntity);
    ActivityRef ref = refList.get(activityEntity, oldUpdated);
    HidableEntity hidableActivity = _getMixin(activityEntity, HidableEntity.class, false);
    if (ref != null) {
      LOG.trace("remove activityRefId " +  ref.getId() +" for commenter: " + identityEntity.getRemoteId());
      refList.remove(activityEntity, hidableActivity.getHidden(), oldUpdated);
    }
    
    refList.getOrCreated(activityEntity, hidableActivity.getHidden() );
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
      //It has been invoked by Activity Service with the multi-threading.
      //so that, gets Entity from JCR, prevent Session.logout exception when retrieves its references
      ActivityEntity activityEntity = _findById(ActivityEntity.class, streamCtx.getActivity().getId());
      HidableEntity hidableActivity = _getMixin(activityEntity, HidableEntity.class, false);
      //ActivityEntity activityEntity = streamCtx.getActivity();
      Collection<ActivityRef> references = activityEntity.getActivityRefs();
      long oldUpdated = streamCtx.getOldLastUpdated();
      ActivityRef newRef = null;
      for (ActivityRef old : references) {
        ActivityRefListEntity refList = old.getDay().getMonth().getYear().getList();
        //ActivityRef.getName equals ActivityId or not
        if (old.getName().equalsIgnoreCase(activityEntity.getId())) {
          refList.update(activityEntity, old, oldUpdated, hidableActivity.getHidden());
        } else {
          newRef = refList.getOrCreated(activityEntity, hidableActivity.getHidden());
          newRef.setLastUpdated(activityEntity.getLastUpdated());
          newRef.setActivityEntity(activityEntity);
          refList.remove(activityEntity, hidableActivity.getHidden(), oldUpdated);
        }
      }
    } catch (NodeNotFoundException ex) {
      LOG.warn("Probably was updated activity reference by another session");
      LOG.debug(ex.getMessage(), ex);
      //turnOnLock to avoid next exception
    } catch (ChromatticException ex) {
        LOG.warn("Probably was updated activity reference by another session");
        LOG.debug(ex.getMessage(), ex);
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
      removeActivityRefs(streamCtx.getMentioners(), activityEntity);
      //commenter
      removeActivityRefs(streamCtx.getCommenters(), activityEntity);
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
    return getActivitiesNotQuery(ActivityRefType.SPACE_STREAM, owner, offset, limit);
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
  public List<ExoSocialActivity> getViewerActivities(Identity owner, int offset, int limit) {
    return getOwnerActivitiesNotQuery(ActivityRefType.MY_ACTIVITIES, owner, offset, limit);
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
          if (isExistingActivityRef(receiverEntity, entity, ActivityRefType.CONNECTION)) continue;
          
          //exclude activity of space
          // for SOC-4525
          if (entity.getPath().contains(SPACE_NODETYPE_PATH)) {
            continue;
          }
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
          if (isExistingActivityRef(senderEntity, entity, ActivityRefType.CONNECTION)) continue;
          
          // for SOC-4525
          if (entity.getPath().contains(SPACE_NODETYPE_PATH)) {
            continue;
          }
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
  
  
  private List<ExoSocialActivity> getActivitiesNotQuery(ActivityRefType type, Identity owner, int offset, int limit) {
    List<ExoSocialActivity> got = new LinkedList<ExoSocialActivity>();
    try {
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(owner.getProviderId(), owner.getRemoteId());
      
      ActivityRefListEntity refList = type.refsOf(identityEntity);
      ActivityRefList list = new ActivityRefList(refList);

      int nb = 0;
      ActivityRefIterator it = list.iterator();
      _skip(it, offset);
      int size = refList.getNumber()>0? refList.getNumber(): 0;
      boolean sizeIsZero = (size==0)?true:false;
      while (it.hasNext()) {
        if(sizeIsZero) size ++;
        try {
          ActivityRef current = it.next();
          // take care in the case, current.getActivityEntity() = null the same
          // SpaceRef, need to remove it out
          if (current.getActivityEntity() == null) {
            current.getDay().getActivityRefs().remove(current.getName());
            continue;
          }

          ExoSocialActivity a = getStorage().getActivity(current.getActivityEntity().getId());

          //SOC-4525 : exclude all space activities that owner is not member
          if (SpaceIdentityProvider.NAME.equals(a.getActivityStream().getType().toString())) {
            Space space = getSpaceStorage().getSpaceByPrettyName(a.getStreamOwner());
            if(null == space){
              IdentityEntity spaceIdentity = current.getActivityEntity().getIdentity();
              LOG.info("SPACE PATH:" + spaceIdentity.getPath());
              space = getSpaceStorage().getSpaceByPrettyName(spaceIdentity.getName());
              if(space!=null){
                LOG.info("SPACE was renamed before: " + space.getPrettyName());
              }
            }
            if (ActivityRefType.CONNECTION.equals(type) || ActivityRefType.FEED.equals(type)) {
              if (space != null && !ArrayUtils.contains(space.getMembers(), owner.getRemoteId())) {
                LOG.info("Cleanup leak activities " + current.getName() + " of space: " + space.getPrettyName());
                current.getDay().getActivityRefs().remove(current.getName());
                getSession().save();
                size--;
                continue;
              }
            }
          }

          if (!got.contains(a)) {
            if (!a.isHidden()) {
              got.add(a);
              if (++nb == limit) {
                break;
              }
            }
          } else {
            //remove if we have duplicate activity on stream.
            //some of cases on PLF 3.5.x migration has duplicated Activity
            current.getDay().getActivityRefs().remove(current.getName());
          }
        } catch (Exception e) {
          LOG.warn("Exception while loading activities for user: " + owner.getRemoteId());
        }
      }

      //re-update size
      refList.setNumber(size);

    } catch (NodeNotFoundException e) {
      LOG.warn("Failed to activities!");
    }
    return got;
  }
  
  private List<ExoSocialActivity> getOwnerActivitiesNotQuery(ActivityRefType type, Identity owner, int offset, int limit) {
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

        ExoSocialActivity a = getStorage().getActivity(current.getActivityEntity().getId());
        if (!got.contains(a)) {
          //only take these user's activities and ower is poster
          if (!a.isHidden() && a.getStreamOwner().equals(owner.getRemoteId())) {
            got.add(a);
            if (++nb == limit) {
              break;
            }
          }
        } else {
          //remove if we have duplicate activity on stream.
          //some of cases on PLF 3.5.x migration has duplicated Activity
          current.getDay().getActivityRefs().remove(current.getName());
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
      
      if (refList == null || refList.getNumber() < 0) return 0;
      
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
    return getActivitiesOfIdentities(ActivityBuilderWhere.owner().owners(connections), filter, 0, -1);
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
  
  private boolean isExistingActivityRef(IdentityEntity identityEntity, ActivityEntity activityEntity, ActivityRefType type) throws NodeNotFoundException {
    ActivityRefListEntity refList = type.refsOf(identityEntity);
    return refList.get(activityEntity, null) != null;
  }
  
  private boolean hasActivityRefs(IdentityEntity identityEntity, ActivityEntity activityEntity, ActivityRefType type, long oldUpdated) throws NodeNotFoundException {
    ActivityRefListEntity refList = type.refsOf(identityEntity);
    ActivityRef ref = refList.get(activityEntity, oldUpdated);
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
    HidableEntity hidableActivity = _getMixin(activityEntity, HidableEntity.class, true);
    if (context.getAdded() != null) {
      for (Identity identity : context.getAdded()) {
        if (!identity.isEnable()) {
          continue;
        }
        IdentityEntity identityEntity = identityStorage._findIdentityEntity(identity.getProviderId(), identity.getRemoteId());
        
        //keep the latest activity posted time
        if (type.equals(ActivityRefType.CONNECTION)) {
          identityEntity.setLatestActivityCreatedTime(activityEntity.getLastUpdated());
        }
        //
        if (mustCheck) {
          //to avoid add back activity to given stream what has already existing
          if (isExistingActivityRef(identityEntity, activityEntity, type)) continue;
        }
        
        
        ActivityRefListEntity listRef = type.refsOf(identityEntity);
        listRef.getOrCreated(activityEntity,  hidableActivity.getHidden());
      }
    }
    
    if (context.getRemoved() != null) {

      for (Identity identity : context.getRemoved()) {
        if (!identity.isEnable()) {
          continue;
        }
        IdentityEntity identityEntity = identityStorage._findIdentityEntity(identity.getProviderId(), identity.getRemoteId());
          
        ActivityRefListEntity listRef = type.refsOf(identityEntity);
        listRef.remove(activityEntity, hidableActivity.getHidden(), null);
      }
    }
  }
  
  private void addRefList(IdentityEntity identityEntity,
                          ActivityEntity activityEntity,
                          ActivityRefType type,
                          boolean mustCheck) throws NodeNotFoundException {

    //
    if (mustCheck) {
      // to avoid add back activity to given stream what has already existing
      if (isExistingActivityRef(identityEntity, activityEntity, type))
        return;
    }
    
    HidableEntity hidableActivity = _getMixin(activityEntity, HidableEntity.class, false);

    ActivityRefListEntity listRef = type.refsOf(identityEntity);
    ActivityRef ref = listRef.getOrCreated(activityEntity, hidableActivity.getHidden());

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
        HidableEntity hidableActivity = _getMixin(activityEntity, HidableEntity.class, true);
        

        // migration 3.5.x => 4.x, lastUpdated of Activity is NULL, then use
        // createdDate for replacement
        ActivityRef ref = listRef.getOrCreated(activityEntity, hidableActivity.getHidden());
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
  
  @Override
  public void updateHidable(ProcessContext ctx) {
    try {
      StreamProcessContext streamCtx = ObjectHelper.cast(StreamProcessContext.class, ctx);
      ExoSocialActivity activity = streamCtx.getActivity();

      ActivityEntity activityEntity = _findById(ActivityEntity.class, activity.getId());
      Collection<ActivityRef> references = activityEntity.getActivityRefs();
      
      //Case of update hidden activity after migration
      if (references == null || references.size() == 0) {
        streamCtx.activityEntity(activityEntity);
        savePoster(streamCtx);
        save(streamCtx);
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
      //turnOffLock to get increase perf
      //turnOnUpdateLock = false;
    } 
  }

  @Override
  public void addMentioners(ProcessContext ctx) {
    try {
      StreamProcessContext streamCtx = ObjectHelper.cast(StreamProcessContext.class, ctx);
      //
      if (streamCtx.getMentioners() == null || streamCtx.getMentioners().length == 0) {
        return;
      }
      ActivityEntity activityEntity = _findById(ActivityEntity.class, streamCtx.getActivity().getId());
      // mentioners
      addMentioner(streamCtx.getMentioners(), activityEntity);
    } catch (NodeNotFoundException ex) {
      LOG.warn("Probably was updated activity reference by another session");
      LOG.debug(ex.getMessage(), ex);
    } 
    
  }
  
}
