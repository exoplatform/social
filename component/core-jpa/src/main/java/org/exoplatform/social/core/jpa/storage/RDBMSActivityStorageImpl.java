/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.jpa.storage;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.LockModeType;

import org.apache.commons.lang.ArrayUtils;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.EntityManagerHolder;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.jpa.storage.dao.ActivityDAO;
import org.exoplatform.social.core.jpa.storage.dao.ConnectionDAO;
import org.exoplatform.social.core.jpa.storage.dao.StreamItemDAO;
import org.exoplatform.social.core.jpa.storage.entity.ActivityEntity;
import org.exoplatform.social.core.jpa.storage.entity.StreamItemEntity;
import org.exoplatform.social.core.jpa.storage.entity.StreamType;
import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.activity.filter.ActivityFilter;
import org.exoplatform.social.core.activity.filter.ActivityUpdateFilter;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ActivityStreamImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.ActivityStorageException.Type;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.impl.ActivityBuilderWhere;
import org.exoplatform.social.core.storage.impl.ActivityStorageImpl;

public class RDBMSActivityStorageImpl extends ActivityStorageImpl {

  private static final Log LOG = ExoLogger.getLogger(RDBMSActivityStorageImpl.class);
  private final ActivityDAO activityDAO;
  private final StreamItemDAO streamItemDAO;
  private IdentityStorage identityStorage;
  private final SpaceStorage spaceStorage;
  private final SortedSet<ActivityProcessor> activityProcessors;
  private static final Pattern MENTION_PATTERN = Pattern.compile("@([^\\s]+)|@([^\\s]+)$");
  public final static String COMMENT_PREFIX = "comment";

  public RDBMSActivityStorageImpl(RelationshipStorage relationshipStorage, 
                                      IdentityStorage identityStorage, 
                                      SpaceStorage spaceStorage,
                                      ActivityDAO activityDAO,
                                      ConnectionDAO connectionDAO,
                                      StreamItemDAO streamItemDAO) {
    
    super(relationshipStorage, identityStorage, spaceStorage);
    //
    
    this.identityStorage = identityStorage;
    this.activityProcessors = new TreeSet<ActivityProcessor>(processorComparator());
    this.activityDAO = activityDAO;
    this.streamItemDAO = streamItemDAO;
    this.spaceStorage = spaceStorage;
  }
  
  private static Comparator<ActivityProcessor> processorComparator() {
    return new Comparator<ActivityProcessor>() {

      public int compare(ActivityProcessor p1, ActivityProcessor p2) {
        if (p1 == null || p2 == null) {
          throw new IllegalArgumentException("Cannot compare null ActivityProcessor");
        }
        return p1.getPriority() - p2.getPriority();
      }
    };
  }

  @Override
  public void addPlugin(BaseComponentPlugin baseComponent) {
    super.addPlugin(baseComponent);
  }

  @Override
  public void setInjectStreams(boolean mustInject) {
    super.setInjectStreams(mustInject);
  }

  private ExoSocialActivity fillActivityFromEntity(ActivityEntity activityEntity, ExoSocialActivity activity) {
    if (activity == null) {
      activity = new ExoSocialActivityImpl(activityEntity.getPosterId(), activityEntity.getType(),
              activityEntity.getTitle(), activityEntity.getBody(), false);
    } else {
      activity.setPosterId(activityEntity.getPosterId());
      activity.setType(activityEntity.getType());
      activity.setTitle(activity.getTitle());
      activity.setBody(activity.getBody());
      activity.isComment(false);
    }

    activity.setId(String.valueOf(activityEntity.getId()));
    activity.setLikeIdentityIds(activityEntity.getLikerIds().toArray(new String[]{}));
    activity.setTemplateParams(activityEntity.getTemplateParams() != null ? new LinkedHashMap<String, String>(activityEntity.getTemplateParams())
            : new HashMap<String, String>());

    String ownerIdentityId = activityEntity.getOwnerId();
    ActivityStream stream = new ActivityStreamImpl();
    Identity owner = identityStorage.findIdentityById(ownerIdentityId);
    stream.setType(owner.getProviderId());
    stream.setPrettyId(owner.getRemoteId());
    stream.setId(owner.getId());
    //
    activity.setActivityStream(stream);
    activity.setStreamOwner(owner.getRemoteId());
    activity.setPosterId(activityEntity.getPosterId());
    //
    activity.isLocked(activityEntity.getLocked());
    activity.isHidden(activityEntity.getHidden());
    activity.setTitleId(activityEntity.getTitleId());
    activity.setPostedTime(activityEntity.getPosted() != null ? activityEntity.getPosted().getTime() : 0);
    activity.setUpdated(activityEntity.getUpdatedDate().getTime());
    //
    List<String> commentIds = new ArrayList<String>();
    List<String> replyToIds = new ArrayList<String>();
    List<ActivityEntity> comments = activityEntity.getComments() != null ? activityEntity.getComments() : new ArrayList<ActivityEntity>();
    for (ActivityEntity comment : comments) {
      if (!commentIds.contains(comment.getPosterId())) {
        commentIds.add(comment.getPosterId());
      }
      replyToIds.add(getExoCommentID(comment.getId()));
    }
    activity.setCommentedIds(commentIds.toArray(new String[commentIds.size()]));
    activity.setReplyToId(replyToIds.toArray(new String[replyToIds.size()]));
    activity.setMentionedIds(activityEntity.getMentionerIds().toArray(new String[activityEntity.getMentionerIds().size()]));

    return activity;
  }

  private ExoSocialActivity convertActivityEntityToActivity(ActivityEntity activityEntity) {
    if(activityEntity == null) return null;
    //
    ExoSocialActivity activity = fillActivityFromEntity(activityEntity, null);
    //
    
    //
    processActivity(activity);
    
    return activity;
  }

  private ActivityEntity convertActivityToActivityEntity(ExoSocialActivity activity, String ownerId) {
    ActivityEntity activityEntity  =  new ActivityEntity();
    if (activity.getId() != null) {
      activityEntity = activityDAO.find(Long.valueOf(activity.getId()));
    }
    activityEntity.setTitle(activity.getTitle());
    activityEntity.setTitleId(activity.getTitleId());
    activityEntity.setType(activity.getType());
    activityEntity.setBody(activity.getBody());
    if (ownerId != null) {
      activityEntity.setPosterId(activity.getUserId() != null ? activity.getUserId() : ownerId);
    }
    if(activity.getLikeIdentityIds() != null) {
      activityEntity.setLikerIds(new HashSet<String>(Arrays.asList(activity.getLikeIdentityIds())));
    }
    Map<String, String> params = activity.getTemplateParams();
    if (params != null) {
      for (Entry<String, String> param : params.entrySet()) {
        String key = param.getKey();
        String value = param.getValue();
      }
      
      activityEntity.setTemplateParams(params);
    }
    
    //
    if (activity.getPostedTime() == null || activity.getPostedTime() <= 0) {
      activity.setPostedTime(System.currentTimeMillis());
    }
    activityEntity.setPosted(new Date(activity.getPostedTime()));
    activityEntity.setLocked(activity.isLocked());
    activityEntity.setHidden(activity.isHidden());
    activityEntity.setUpdatedDate(activity.getUpdated());
    activityEntity.setMentionerIds(new HashSet<String>(Arrays.asList(processMentions(activity.getTitle()))));
    //
    return activityEntity;
  }
  
  private ExoSocialActivity convertCommentEntityToComment(ActivityEntity comment) {
    ExoSocialActivity exoComment = new ExoSocialActivityImpl(comment.getPosterId(), null,
        comment.getTitle(), comment.getBody(), false);
    exoComment.setId(getExoCommentID(comment.getId()));
    exoComment.setTitle(comment.getTitle());
    exoComment.setType(comment.getType());
    exoComment.setTitleId(comment.getTitleId());
    exoComment.setBody(comment.getBody());
    exoComment.setTemplateParams(comment.getTemplateParams() != null ? new LinkedHashMap<String, String>(comment.getTemplateParams())
                                                                    : new HashMap<String, String>());
    exoComment.setPosterId(comment.getPosterId());
    exoComment.isComment(true);
    //
    exoComment.isLocked(comment.getLocked().booleanValue());
    exoComment.isHidden(comment.getHidden().booleanValue());
    exoComment.setUpdated(comment.getUpdatedDate().getTime());
    //
    exoComment.setParentId(String.valueOf(comment.getParent().getId()));
    //
    exoComment.setPostedTime(comment.getPosted() != null ? comment.getPosted().getTime() : 0);
    exoComment.setUpdated(comment.getUpdatedDate().getTime());
    //
    Set<String> mentioned = comment.getMentionerIds();
    if (mentioned != null && !mentioned.isEmpty()) {
      exoComment.setMentionedIds(comment.getMentionerIds().toArray(new String[mentioned.size()]));
    }

    processActivity(exoComment);
    
    return exoComment;
  }

  private List<ExoSocialActivity> convertCommentEntitiesToComments(List<ActivityEntity> comments) {
    List<ExoSocialActivity> exoSocialActivities = new ArrayList<ExoSocialActivity>();
    if (comments == null) return exoSocialActivities;
    for (ActivityEntity comment : comments) {
      exoSocialActivities.add(convertCommentEntityToComment(comment));
    }
    return exoSocialActivities;
  }
  
  private ActivityEntity convertCommentToCommentEntity(ExoSocialActivity comment) {
    ActivityEntity commentEntity = new ActivityEntity();
    if (comment.getId() != null) {
      commentEntity = activityDAO.find(getCommentID(comment.getId()));
    }
    commentEntity.setComment(true);
    commentEntity.setTitle(comment.getTitle());
    commentEntity.setTitleId(comment.getTitleId());
    commentEntity.setType(comment.getType());
    commentEntity.setBody(comment.getBody());
    commentEntity.setPosterId(comment.getPosterId() != null ? comment.getPosterId() : comment.getUserId());
    if (comment.getTemplateParams() != null) {
      commentEntity.setTemplateParams(comment.getTemplateParams());
    }
    //
    commentEntity.setLocked(comment.isLocked());
    commentEntity.setHidden(comment.isHidden());
    //
    Date commentTime = (comment.getPostedTime() != null ? new Date(comment.getPostedTime()) : new Date());
    commentEntity.setPosted(commentTime);
    commentEntity.setUpdatedDate(commentTime);
    commentEntity.setMentionerIds(new HashSet<>(Arrays.asList(processMentions(comment.getTitle()))));
    //
    return commentEntity;
  }
  
  private List<ExoSocialActivity> convertActivityEntitiesToActivities(List<ActivityEntity> activities) {
    List<ExoSocialActivity> exoSocialActivities = new ArrayList<ExoSocialActivity>();
    if (activities == null) return exoSocialActivities;
    for (ActivityEntity activity : activities) {
      exoSocialActivities.add(convertActivityEntityToActivity(activity));
    }
    return exoSocialActivities;
  }
  
  @Override
  @ExoTransactional
  public ExoSocialActivity getActivity(String activityId) throws ActivityStorageException {
    if (activityId == null || activityId.isEmpty()) {
      return null;
    }
    if (activityId != null && activityId.startsWith(COMMENT_PREFIX)) {
      return getComment(activityId);
    }
    try {
      ActivityEntity entity = activityDAO.find(Long.valueOf(activityId));
      return convertActivityEntityToActivity(entity);
    } catch (Exception e) {
      if (PropertyManager.isDevelopping()) {
        throw new ActivityStorageException(Type.FAILED_TO_GET_ACTIVITY, e.getMessage(), e);
      }
      return null;
    }
  }

  public ExoSocialActivity getComment(String commentId) throws ActivityStorageException {
    try {
      ActivityEntity entity = activityDAO.find(getCommentID(commentId));
      return convertCommentEntityToComment(entity);
    } catch (Exception e) {
      if (PropertyManager.isDevelopping()) {
        throw new ActivityStorageException(Type.FAILED_TO_GET_ACTIVITY, e.getMessage(), e);
      }
      return null;
    }
  }

  @Override
  public List<ExoSocialActivity> getUserActivities(Identity owner) throws ActivityStorageException {
    return getUserActivities(owner, 0, -1);
  }

  @Override
  public List<ExoSocialActivity> getUserActivities(Identity owner, long offset, long limit) throws ActivityStorageException {
    return getUserActivitiesForUpgrade(owner, offset, limit);
  }
  
  @Override
  public List<String> getUserSpacesActivityIds(Identity ownerIdentity, int offset, int limit) {
    return activityDAO.getUserSpacesActivityIds(ownerIdentity, offset, limit, memberOfSpaceIds(ownerIdentity));
  }
  
  @Override
  public List<String> getUserIdsActivities(Identity owner, long offset, long limit) throws ActivityStorageException {
    return activityDAO.getUserIdsActivities(owner, offset, limit);
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getUserActivitiesForUpgrade(Identity owner, long offset, long limit) throws ActivityStorageException {
    return convertActivityEntitiesToActivities(activityDAO.getUserActivities(owner, offset, limit));
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getActivities(Identity owner, Identity viewer, long offset, long limit) throws ActivityStorageException {
    return convertActivityEntitiesToActivities(activityDAO.getActivities(owner, viewer, offset, limit));
  }

  @Override
  @ExoTransactional
  public void saveComment(ExoSocialActivity activity, ExoSocialActivity eXoComment) throws ActivityStorageException {
    ActivityEntity activityEntity = activityDAO.find(Long.valueOf(activity.getId()));
    try {
      EntityManagerHolder.get().lock(activityEntity, LockModeType.PESSIMISTIC_WRITE);
      
      ActivityEntity commentEntity = convertCommentToCommentEntity(eXoComment);
      commentEntity.setParent(activityEntity);
      //
      Identity commenter = identityStorage.findIdentityById(commentEntity.getPosterId());
      saveStreamItemForCommenter(commenter, activityEntity);
      mention(commenter, activityEntity, processMentions(eXoComment.getTitle()));
      //
      activityEntity.addComment(commentEntity);
      commentEntity = activityDAO.create(commentEntity);
      eXoComment.setId(getExoCommentID(commentEntity.getId()));
      Set<String> mentioned = commentEntity.getMentionerIds();
      if (mentioned != null && !mentioned.isEmpty()) {
        eXoComment.setMentionedIds(mentioned.toArray(new String[mentioned.size()]));
      }
      //
      activityEntity.setMentionerIds(processMentionOfComment(activityEntity, commentEntity, activity.getMentionedIds(), processMentions(eXoComment.getTitle()), true));
      if (eXoComment.getUpdated() != null) {
        activityEntity.setUpdatedDate(eXoComment.getUpdated());
      } else {
        activityEntity.setUpdatedDate(new Date());
      }
      activityDAO.update(activityEntity);
      //
      updateLastUpdatedForStreamItem(activityEntity);

      //
      //activity = convertActivityEntityToActivity(activityEntity);
    } finally {
      EntityManagerHolder.get().lock(activityEntity, LockModeType.NONE);
    }

  }

  /**
   * Creates the StreamItem for commenter
   * @param commenter
   * @param activityEntity
   */
  private void saveStreamItemForCommenter(Identity commenter, ActivityEntity activityEntity) {
    Identity ownerActivity = identityStorage.findIdentityById(activityEntity.getOwnerId());
    if (! SpaceIdentityProvider.NAME.equals(ownerActivity.getProviderId())) {
      createStreamItem(StreamType.COMMENTER, activityEntity, Long.parseLong(commenter.getId()));
    }
  }

  private void updateLastUpdatedForStreamItem(ActivityEntity activity) {
    List<StreamItemEntity> items = streamItemDAO.findStreamItemByActivityId(activity.getId());
    for (StreamItemEntity item : items) {
      item.setUpdatedDate(activity.getUpdatedDate());
      streamItemDAO.update(item);
    }
  }

  private Set<String> processMentionOfComment(ActivityEntity activityEntity, ActivityEntity commentEntity, String[] activityMentioners, String[] commentMentioners, boolean isAdded) {
    Set<String> mentioners = new HashSet<String>(Arrays.asList(activityMentioners));
    if (commentMentioners.length == 0) return mentioners;
    //
    for (String mentioner : commentMentioners) {
      if (!mentioners.contains(mentioner) && isAdded) {
        mentioners.add(mentioner);
      }
      if (mentioners.contains(mentioner) && !isAdded) {
        if (isAllowedToRemove(activityEntity, commentEntity, mentioner)) {
          mentioners.remove(mentioner);
          //remove stream item
          StreamItemEntity item = new StreamItemEntity(StreamType.MENTIONER);
          item.setOwnerId(Long.parseLong(mentioner));
          activityEntity.removeStreamItem(item);
        }
      }
    }
    return mentioners;
  }
  
  private boolean isAllowedToRemove(ActivityEntity activity, ActivityEntity comment, String mentioner) {
    if (ArrayUtils.contains(processMentions(activity.getTitle()), mentioner)) {
      return false;
    }
    List<ActivityEntity> comments = activity.getComments();
    comments.remove(comment);
    for (ActivityEntity cmt : comments) {
      if (ArrayUtils.contains(processMentions(cmt.getTitle()), mentioner)) {
        return false;
      }
    }
    return true;
  }
  
  @Override
  public ExoSocialActivity saveActivity(Identity owner, ExoSocialActivity activity) throws ActivityStorageException {
    boolean isNew = (activity.getId() == null);
    ActivityEntity entity = convertActivityToActivityEntity(activity, owner.getId());
    //
    entity.setOwnerId(owner.getId());
    entity.setProviderId(owner.getProviderId());
    saveStreamItem(owner, entity);
    //
    entity = activityDAO.create(entity);
    activity.setId(Long.toString(entity.getId()));
    //

    if (isNew) {
      fillActivityFromEntity(entity, activity);
    }
    
    return activity;
  }
  /**
   * Creates the StreamIteam for Space Member
   * @param spaceOwner
   * @param activity
   */
  private void spaceMembers(Identity spaceOwner, ActivityEntity activity) {
    createStreamItem(StreamType.SPACE, activity, Long.parseLong(spaceOwner.getId()));
    createStreamItem(StreamType.SPACE, activity, Long.parseLong(activity.getPosterId()));
  }
  
  private void saveStreamItem(Identity owner, ActivityEntity activity) {
    //create StreamItem    
    if (OrganizationIdentityProvider.NAME.equals(owner.getProviderId())) {
      //poster
      poster(owner, activity);
      
    } else {
      //for SPACE
      spaceMembers(owner, activity);
    }
    //mention
    mention(owner, activity, processMentions(activity.getTitle()));
  }
  
  /**
   * Creates the StreamItem for poster and stream owner
   * @param owner
   * @param activity
   */
  private void poster(Identity owner, ActivityEntity activity) {
    createStreamItem(StreamType.POSTER, activity, Long.parseLong(activity.getPosterId()));
    //User A posts a new activity on user B stream (A connected B)
    if (!owner.getId().equals(activity.getPosterId())) {
      createStreamItem(StreamType.POSTER, activity, Long.parseLong(owner.getId()));
    }
  }
  
  /**
   * Creates StreamItem for each user who has mentioned on the activity
   * 
   * @param owner
   * @param activity
   */
  private void mention(Identity owner, ActivityEntity activity, String [] mentions) {
    for (String mentioner : mentions) {
      Identity identity = identityStorage.findIdentityById(mentioner);
      if(identity != null) {
        createStreamItem(StreamType.MENTIONER, activity, Long.parseLong(identity.getId()));
      }
    }
  }
  
  private void createStreamItem(StreamType streamType, ActivityEntity activity, Long ownerId){
    StreamItemEntity streamItem = new StreamItemEntity(streamType);
    streamItem.setOwnerId(ownerId);
    streamItem.setUpdatedDate(activity.getUpdatedDate());
    boolean isExist = false;
    if (activity.getId() != null) {
      //TODO need to improve it
      for (StreamItemEntity item : activity.getStreamItems()) {
        if (item.getOwnerId().equals(ownerId) && streamType.equals(item.getStreamType())) {
          isExist = true;
          break;
        }
      }
    }
    if (!isExist) {
      activity.addStreamItem(streamItem);
    }
  }
  

  /**
   * Processes Mentioners who has been mentioned via the Activity.
   * 
   * @param title
   */
  private String[] processMentions(String title) {
    if (title == null || title.length() == 0) {
      return ArrayUtils.EMPTY_STRING_ARRAY;
    }

    Set<String> mentions = new HashSet<>();
    Matcher matcher = MENTION_PATTERN.matcher(title);
    while (matcher.find()) {
      String remoteId = matcher.group().substring(1);
      if (!USER_NAME_VALIDATOR_REGEX.matcher(remoteId).matches()) {
        continue;
      }
      Identity identity = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, remoteId);
      // if not the right mention then ignore
      if (identity != null && !mentions.contains(identity.getId())) {
        mentions.add(identity.getId());
      }
    }
    return mentions.toArray(new String[mentions.size()]);
  }
  
  @Override
  @ExoTransactional
  public ExoSocialActivity getParentActivity(ExoSocialActivity comment) throws ActivityStorageException {
    try {
      Long commentId = getCommentID(comment.getId());
      return convertActivityEntityToActivity(activityDAO.getParentActivity(commentId));
    } catch (NumberFormatException e) {
      LOG.warn("The input ExoSocialActivity is not comment, it is Activity");
      return null;
    }
  }

  @Override
  public void deleteActivity(String activityId) throws ActivityStorageException {
    ActivityEntity a = activityDAO.find(Long.valueOf(activityId));
    if (a != null) {
      activityDAO.delete(a);
    } else {
      LOG.warn("The activity's " + activityId + " is not found!" );
    }
  }

  @Override
  @ExoTransactional
  public void deleteComment(String activityId, String commentId) throws ActivityStorageException {
    ActivityEntity comment = activityDAO.find(getCommentID(commentId));
    activityDAO.delete(comment);
    //
    ActivityEntity activity = activityDAO.find(Long.valueOf(activityId));
    activity.getComments().remove(comment);
    //
    activity.setMentionerIds(processMentionOfComment(activity, comment, activity.getMentionerIds().toArray(new String[activity.getMentionerIds().size()]), processMentions(comment.getTitle()), false));
    //
    if (!hasOtherComment(activity, comment.getPosterId())) {
      StreamItemEntity item = new StreamItemEntity(StreamType.COMMENTER);
      item.setOwnerId(Long.parseLong(comment.getPosterId()));
      activity.removeStreamItem(item);
    }
    //
    activityDAO.update(activity);
  }
  
  private boolean hasOtherComment(ActivityEntity activity, String poster) {
    for (ActivityEntity comment : activity.getComments()) {
      if (poster.equals(comment.getPosterId())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public List<ExoSocialActivity> getActivitiesOfIdentities(List<Identity> connectionList, long offset, long limit) throws ActivityStorageException {
    return null;
  }

  @Override
  public List<ExoSocialActivity> getActivitiesOfIdentities(List<Identity> connectionList, TimestampType type, long offset, long limit) throws ActivityStorageException {
    return null;
  }

  @Override
  public int getNumberOfUserActivities(Identity owner) throws ActivityStorageException {
    return getNumberOfUserActivitiesForUpgrade(owner);
  }

  @Override
  public int getNumberOfUserActivitiesForUpgrade(Identity owner) throws ActivityStorageException {
    return activityDAO.getNumberOfUserActivities(owner);
  }

  @Override
  public int getNumberOfNewerOnUserActivities(Identity ownerIdentity, ExoSocialActivity baseActivity) {
    return getNumberOfNewerOnUserActivities(ownerIdentity, baseActivity.getUpdated().getTime());
  }

  @Override
  public List<ExoSocialActivity> getNewerOnUserActivities(Identity ownerIdentity, ExoSocialActivity baseActivity, int limit) {
    return getNewerUserActivities(ownerIdentity, baseActivity.getUpdated().getTime(), limit);
  }

  @Override
  public int getNumberOfOlderOnUserActivities(Identity ownerIdentity, ExoSocialActivity baseActivity) {
    return getNumberOfOlderOnUserActivities(ownerIdentity, baseActivity.getUpdated().getTime());
  }

  @Override
  public List<ExoSocialActivity> getOlderOnUserActivities(Identity ownerIdentity, ExoSocialActivity baseActivity, int limit) {
    return getOlderUserActivities(ownerIdentity, baseActivity.getUpdated().getTime(), limit);
  }

  @Override
  public List<ExoSocialActivity> getActivityFeed(Identity ownerIdentity, int offset, int limit) {
    return getActivityFeedForUpgrade(ownerIdentity, offset, limit);
  }
  
  @Override
  public List<String> getActivityIdsFeed(Identity ownerIdentity, int offset, int limit) {
    return activityDAO.getActivityIdsFeed(ownerIdentity, offset, limit, memberOfSpaceIds(ownerIdentity));
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getActivityFeedForUpgrade(Identity ownerIdentity, int offset, int limit) {
    return convertActivityEntitiesToActivities(activityDAO.getActivityFeed(ownerIdentity, offset, limit, memberOfSpaceIds(ownerIdentity)));
  }

  @Override
  public int getNumberOfActivitesOnActivityFeed(Identity ownerIdentity) {
    return getNumberOfActivitesOnActivityFeedForUpgrade(ownerIdentity);
  }

  @Override
  public int getNumberOfActivitesOnActivityFeedForUpgrade(Identity ownerIdentity) {
    return activityDAO.getNumberOfActivitesOnActivityFeed(ownerIdentity, memberOfSpaceIds(ownerIdentity));
  }

  @Override
  public int getNumberOfNewerOnActivityFeed(Identity ownerIdentity, ExoSocialActivity baseActivity) {
    return getNumberOfNewerOnActivityFeed(ownerIdentity, baseActivity.getUpdated().getTime());
  }

  @Override
  public List<ExoSocialActivity> getNewerOnActivityFeed(Identity ownerIdentity, ExoSocialActivity baseActivity, int limit) {
    return getNewerFeedActivities(ownerIdentity, baseActivity.getUpdated().getTime(), limit);
  }

  @Override
  public int getNumberOfOlderOnActivityFeed(Identity ownerIdentity, ExoSocialActivity baseActivity) {
    return getNumberOfOlderOnActivityFeed(ownerIdentity, baseActivity.getUpdated().getTime());
  }

  @Override
  public List<ExoSocialActivity> getOlderOnActivityFeed(Identity ownerIdentity, ExoSocialActivity baseActivity, int limit) {
    return getOlderFeedActivities(ownerIdentity, baseActivity.getUpdated().getTime(), limit);
  }

  @Override
  public List<ExoSocialActivity> getActivitiesOfConnections(Identity ownerIdentity, int offset, int limit) {
    return getActivitiesOfConnectionsForUpgrade(ownerIdentity, offset, limit);
  }
  
  @Override
  public List<String> getActivityIdsOfConnections(Identity ownerIdentity, int offset, int limit) {
    return activityDAO.getActivityIdsOfConnections(ownerIdentity, offset, limit);
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getActivitiesOfConnectionsForUpgrade(Identity ownerIdentity, int offset, int limit) {
    return convertActivityEntitiesToActivities(activityDAO.getActivitiesOfConnections(ownerIdentity, offset, limit));
  }

  @Override
  public int getNumberOfActivitiesOfConnections(Identity ownerIdentity) {
    return getNumberOfActivitiesOfConnectionsForUpgrade(ownerIdentity);
  }

  @Override
  public int getNumberOfActivitiesOfConnectionsForUpgrade(Identity ownerIdentity) {
    return activityDAO.getNumberOfActivitiesOfConnections(ownerIdentity);
  }

  @Override
  public List<ExoSocialActivity> getActivitiesOfIdentity(Identity ownerIdentity, long offset, long limit) {
    return getUserActivities(ownerIdentity, offset, limit);
  }

  @Override
  public int getNumberOfNewerOnActivitiesOfConnections(Identity ownerIdentity, ExoSocialActivity baseActivity) {
    return getNumberOfNewerOnActivitiesOfConnections(ownerIdentity, baseActivity.getUpdated().getTime());
  }

  @Override
  public List<ExoSocialActivity> getNewerOnActivitiesOfConnections(Identity ownerIdentity, ExoSocialActivity baseActivity, long limit) {
    return getNewerActivitiesOfConnections(ownerIdentity, baseActivity.getUpdated().getTime(), (int) limit);
  }

  @Override
  public int getNumberOfOlderOnActivitiesOfConnections(Identity ownerIdentity, ExoSocialActivity baseActivity) {
    return getNumberOfOlderOnActivitiesOfConnections(ownerIdentity, baseActivity.getUpdated().getTime());
  }

  @Override
  public List<ExoSocialActivity> getOlderOnActivitiesOfConnections(Identity ownerIdentity, ExoSocialActivity baseActivity, int limit) {
    return getOlderActivitiesOfConnections(ownerIdentity, baseActivity.getUpdated().getTime(), limit);
  }

  @Override
  public List<ExoSocialActivity> getUserSpacesActivities(Identity ownerIdentity, int offset, int limit) {
    return getUserSpacesActivitiesForUpgrade(ownerIdentity, offset, limit);
  }
  
  @Override
  public List<String> getSpaceActivityIds(Identity spaceIdentity, int offset, int limit) {
    return activityDAO.getSpaceActivityIds(spaceIdentity, offset, limit);
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getUserSpacesActivitiesForUpgrade(Identity ownerIdentity, int offset, int limit) {
    return convertActivityEntitiesToActivities(activityDAO.getUserSpacesActivities(ownerIdentity, offset, limit, memberOfSpaceIds(ownerIdentity)));
  }

  @Override
  public int getNumberOfUserSpacesActivities(Identity ownerIdentity) {
    return getNumberOfUserSpacesActivitiesForUpgrade(ownerIdentity);
  }

  @Override
  public int getNumberOfUserSpacesActivitiesForUpgrade(Identity ownerIdentity) {
    return activityDAO.getNumberOfUserSpacesActivities(ownerIdentity, memberOfSpaceIds(ownerIdentity));
  }

  @Override
  public int getNumberOfNewerOnUserSpacesActivities(Identity ownerIdentity, ExoSocialActivity baseActivity) {
    return getNumberOfNewerOnUserSpacesActivities(ownerIdentity, baseActivity.getUpdated().getTime());
  }

  @Override
  public List<ExoSocialActivity> getNewerOnUserSpacesActivities(Identity ownerIdentity, ExoSocialActivity baseActivity, int limit) {
    return getNewerUserSpacesActivities(ownerIdentity, baseActivity.getUpdated().getTime(), limit);
  }

  @Override
  public int getNumberOfOlderOnUserSpacesActivities(Identity ownerIdentity, ExoSocialActivity baseActivity) {
    return getNumberOfOlderOnUserSpacesActivities(ownerIdentity, baseActivity.getUpdated().getTime());
  }

  @Override
  public List<ExoSocialActivity> getOlderOnUserSpacesActivities(Identity ownerIdentity, ExoSocialActivity baseActivity, int limit) {
    return getOlderUserSpacesActivities(ownerIdentity, baseActivity.getUpdated().getTime(), limit);
  }

  @Override
  public List<ExoSocialActivity> getComments(ExoSocialActivity existingActivity, int offset, int limit) {
    long activityId = 0;
    try {
      activityId = Long.parseLong(existingActivity.getId());
    } catch (NumberFormatException ex) {
      activityId = 0;
    }

    List<ActivityEntity> comments;
    if (activityId > 0) {
      comments = activityDAO.getComments(activityId, offset, limit);
    } else {
      comments = null;
    }
    
    return convertCommentEntitiesToComments(comments);
  }

  @Override
  public int getNumberOfComments(ExoSocialActivity existingActivity) {
    return (int)activityDAO.getNumberOfComments(Long.valueOf(existingActivity.getId()));
  }

  @Override
  public int getNumberOfNewerComments(ExoSocialActivity existingActivity, ExoSocialActivity baseComment) {
    return getNewerComments(existingActivity, baseComment, 0).size();
  }

  @Override
  public List<ExoSocialActivity> getNewerComments(ExoSocialActivity existingActivity, ExoSocialActivity baseComment, int limit) {
    return getNewerComments(existingActivity, baseComment.getPostedTime(), limit);
  }

  @Override
  public int getNumberOfOlderComments(ExoSocialActivity existingActivity, ExoSocialActivity baseComment) {
    return getOlderComments(existingActivity, baseComment, 0).size();
  }

  @Override
  public List<ExoSocialActivity> getOlderComments(ExoSocialActivity existingActivity, ExoSocialActivity baseComment, int limit) {
    return getOlderComments(existingActivity, baseComment.getPostedTime(), limit);
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getNewerComments(ExoSocialActivity existingActivity, Long sinceTime, int limit) {
    List<ActivityEntity> comments = activityDAO.getNewerComments(Long.valueOf(existingActivity.getId()), sinceTime > 0 ? new Date(sinceTime) : null, 0, limit);
    //
    return convertCommentEntitiesToComments(comments);
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getOlderComments(ExoSocialActivity existingActivity, Long sinceTime, int limit) {
    List<ActivityEntity> comments = activityDAO.getOlderComments(Long.valueOf(existingActivity.getId()), sinceTime > 0 ? new Date(sinceTime) : null, 0, limit);
    return convertCommentEntitiesToComments(comments);
  }

  @Override
  public int getNumberOfNewerComments(ExoSocialActivity existingActivity, Long sinceTime) {
    return getNewerComments(existingActivity, sinceTime, 0).size();
  }

  @Override
  public int getNumberOfOlderComments(ExoSocialActivity existingActivity, Long sinceTime) {
    return getOlderComments(existingActivity, sinceTime, 0).size();
  }

  @Override
  public SortedSet<ActivityProcessor> getActivityProcessors() {
    return activityProcessors;
  }

  @Override
  public void updateActivity(ExoSocialActivity existingActivity) throws ActivityStorageException {
    if(existingActivity == null) {
      throw new IllegalArgumentException("Activity to update cannot be null");
    }

    ActivityEntity updatedActivity = activityDAO.find(Long.valueOf(existingActivity.getId()));

    if(updatedActivity != null) {
      if (existingActivity.getId().startsWith(COMMENT_PREFIX)) {
        // update comment
        updatedActivity.setUpdatedDate(new Date());
      } else {
        //create or remove liker if exist
        processLikerActivityInStreams(new HashSet<>(Arrays.asList(existingActivity.getLikeIdentityIds())), new HashSet<>(updatedActivity.getLikerIds()), updatedActivity);
      }

      if (existingActivity.getTitleId() != null) updatedActivity.setTitleId(existingActivity.getTitleId());
      if (existingActivity.getTitle() != null) updatedActivity.setTitle(existingActivity.getTitle());
      if (existingActivity.getBody() != null) updatedActivity.setBody(existingActivity.getBody());
      if (existingActivity.getLikeIdentityIds() != null) updatedActivity.setLikerIds(new HashSet<>(Arrays.asList(existingActivity.getLikeIdentityIds())));
      if (existingActivity.getMentionedIds() != null) updatedActivity.setMentionerIds(new HashSet<>(Arrays.asList(existingActivity.getMentionedIds())));
      if (existingActivity.getPermaLink() != null) updatedActivity.setPermaLink(existingActivity.getPermaLink());
      if (existingActivity.getTemplateParams() != null) updatedActivity.setTemplateParams(existingActivity.getTemplateParams());
      updatedActivity.setHidden(existingActivity.isHidden());
      updatedActivity.setComment(existingActivity.isComment());
      updatedActivity.setLocked(existingActivity.isLocked());

      activityDAO.update(updatedActivity);
    } else {
      throw new ActivityStorageException(Type.FAILED_TO_UPDATE_ACTIVITY, "Cannot find activity with id=" + existingActivity.getId());
    }
  }
  
  private void processLikerActivityInStreams(Set<String> newLikerList, Set<String> oldLikerList, ActivityEntity activity) {
    for (String id : newLikerList) {
      if (!oldLikerList.contains(id)) {//new like ==> create stream item
        createStreamItem(StreamType.LIKER, activity, Long.parseLong(id));
      } else {
        oldLikerList.remove(id);
      }
    }
    if (oldLikerList.size() > 0) {//unlike ==> remove stream item
      for (String id : oldLikerList) {
        StreamItemEntity item = new StreamItemEntity(StreamType.LIKER);
        item.setOwnerId(Long.parseLong(id));
        activity.removeStreamItem(item);
      }
    }
  }

  @Override
  public int getNumberOfNewerOnActivityFeed(Identity ownerIdentity, Long sinceTime) {
    return activityDAO.getNumberOfNewerOnActivityFeed(ownerIdentity, sinceTime, memberOfSpaceIds(ownerIdentity));
  }

  @Override
  public int getNumberOfNewerOnUserActivities(Identity ownerIdentity, Long sinceTime) {
    return activityDAO.getNumberOfNewerOnUserActivities(ownerIdentity, sinceTime);
  }

  @Override
  public int getNumberOfNewerOnActivitiesOfConnections(Identity ownerIdentity, Long sinceTime) {
    return activityDAO.getNumberOfNewerOnActivitiesOfConnections(ownerIdentity, sinceTime);
  }

  @Override
  public int getNumberOfNewerOnUserSpacesActivities(Identity ownerIdentity, Long sinceTime) {
    return activityDAO.getNumberOfNewerOnUserSpacesActivities(ownerIdentity, sinceTime, memberOfSpaceIds(ownerIdentity));
  }

  @Override
  public List<ExoSocialActivity> getActivitiesOfIdentities(ActivityBuilderWhere where, ActivityFilter filter, long offset, long limit) throws ActivityStorageException {
    return null;
  }

  @Override
  public int getNumberOfSpaceActivities(Identity spaceIdentity) {
    return getNumberOfSpaceActivitiesForUpgrade(spaceIdentity);
  }

  @Override
  public int getNumberOfSpaceActivitiesForUpgrade(Identity spaceIdentity) {
    return activityDAO.getNumberOfSpaceActivities(spaceIdentity);
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getSpaceActivities(Identity spaceIdentity, int offset, int limit) {
    return convertActivityEntitiesToActivities(activityDAO.getSpaceActivities(spaceIdentity, offset, limit));
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getSpaceActivitiesForUpgrade(Identity spaceIdentity, int offset, int limit) {
    return convertActivityEntitiesToActivities(activityDAO.getSpaceActivities(spaceIdentity, offset, limit));
  }

  @Override
  public List<ExoSocialActivity> getActivitiesByPoster(Identity posterIdentity, int offset, int limit) {
    return getActivitiesByPoster(posterIdentity, offset, limit, new String[]{});
  }

  @Override
  public List<ExoSocialActivity> getActivitiesByPoster(Identity posterIdentity, int offset, int limit, String... activityTypes) {
    return convertActivityEntitiesToActivities(activityDAO.getActivitiesByPoster(posterIdentity, offset, limit, activityTypes));
  }

  @Override
  public int getNumberOfActivitiesByPoster(Identity posterIdentity) {
    return activityDAO.getNumberOfActivitiesByPoster(posterIdentity, new String[]{});
  }

  @Override
  public int getNumberOfActivitiesByPoster(Identity ownerIdentity, Identity viewerIdentity) {
    return 0;
  }

  @Override
  public List<ExoSocialActivity> getNewerOnSpaceActivities(Identity spaceIdentity, ExoSocialActivity baseActivity, int limit) {
    return getNewerSpaceActivities(spaceIdentity, baseActivity.getUpdated().getTime(), limit);
  }

  @Override
  public int getNumberOfNewerOnSpaceActivities(Identity spaceIdentity, ExoSocialActivity baseActivity) {
    return getNumberOfNewerOnSpaceActivities(spaceIdentity, baseActivity.getUpdated().getTime());
  }

  @Override
  public List<ExoSocialActivity> getOlderOnSpaceActivities(Identity spaceIdentity, ExoSocialActivity baseActivity, int limit) {
    return getOlderSpaceActivities(spaceIdentity, baseActivity.getUpdated().getTime(), limit);
  }

  @Override
  public int getNumberOfOlderOnSpaceActivities(Identity spaceIdentity, ExoSocialActivity baseActivity) {
    return getNumberOfOlderOnSpaceActivities(spaceIdentity, baseActivity.getUpdated().getTime());
  }

  @Override
  public int getNumberOfNewerOnSpaceActivities(Identity spaceIdentity, Long sinceTime) {
    return activityDAO.getNumberOfNewerOnSpaceActivities(spaceIdentity, sinceTime);
  }

  @Override
  public int getNumberOfUpdatedOnActivityFeed(Identity owner, ActivityUpdateFilter filter) {
    return 0;
  }

  @Override
  public int getNumberOfUpdatedOnUserActivities(Identity owner, ActivityUpdateFilter filter) {
    return 0;
  }

  @Override
  public int getNumberOfUpdatedOnActivitiesOfConnections(Identity owner, ActivityUpdateFilter filter) {
    return 0;
  }

  @Override
  public int getNumberOfUpdatedOnUserSpacesActivities(Identity owner, ActivityUpdateFilter filter) {
    return 0;
  }

  @Override
  public int getNumberOfUpdatedOnSpaceActivities(Identity owner, ActivityUpdateFilter filter) {
    return 0;
  }

  @Override
  public int getNumberOfMultiUpdated(Identity owner, Map<String, Long> sinceTimes) {
    return 0;
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getNewerFeedActivities(Identity owner, Long sinceTime, int limit) {
    return convertActivityEntitiesToActivities(activityDAO.getNewerOnActivityFeed(owner, sinceTime, limit, memberOfSpaceIds(owner)));
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getNewerUserActivities(Identity owner, Long sinceTime, int limit) {
    return convertActivityEntitiesToActivities(activityDAO.getNewerOnUserActivities(owner, sinceTime, limit));
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getNewerUserSpacesActivities(Identity owner, Long sinceTime, int limit) {
    return convertActivityEntitiesToActivities(activityDAO.getNewerOnUserSpacesActivities(owner, sinceTime, limit, memberOfSpaceIds(owner)));
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getNewerActivitiesOfConnections(Identity owner, Long sinceTime, int limit) {
    return convertActivityEntitiesToActivities(activityDAO.getNewerOnActivitiesOfConnections(owner, sinceTime, limit));
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getNewerSpaceActivities(Identity owner, Long sinceTime, int limit) {
    return convertActivityEntitiesToActivities(activityDAO.getNewerOnSpaceActivities(owner, sinceTime, limit));
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getOlderFeedActivities(Identity owner, Long sinceTime, int limit) {
    return convertActivityEntitiesToActivities(activityDAO.getOlderOnActivityFeed(owner, sinceTime, limit, memberOfSpaceIds(owner)));
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getOlderUserActivities(Identity owner, Long sinceTime, int limit) {
    return convertActivityEntitiesToActivities(activityDAO.getOlderOnUserActivities(owner, sinceTime, limit));
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getOlderUserSpacesActivities(Identity owner, Long sinceTime, int limit) {
    return convertActivityEntitiesToActivities(activityDAO.getOlderOnUserSpacesActivities(owner, sinceTime, limit, memberOfSpaceIds(owner)));
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getOlderActivitiesOfConnections(Identity owner, Long sinceTime, int limit) {
    return convertActivityEntitiesToActivities(activityDAO.getOlderOnActivitiesOfConnections(owner, sinceTime, limit));
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getOlderSpaceActivities(Identity owner, Long sinceTime, int limit) {
    return convertActivityEntitiesToActivities(activityDAO.getOlderOnSpaceActivities(owner, sinceTime, limit));
  }

  @Override
  public int getNumberOfOlderOnActivityFeed(Identity ownerIdentity, Long sinceTime) {
    return activityDAO.getNumberOfOlderOnActivityFeed(ownerIdentity, sinceTime, memberOfSpaceIds(ownerIdentity));
  }

  @Override
  public int getNumberOfOlderOnUserActivities(Identity ownerIdentity, Long sinceTime) {
    return activityDAO.getNumberOfOlderOnUserActivities(ownerIdentity, sinceTime);
  }

  @Override
  public int getNumberOfOlderOnActivitiesOfConnections(Identity ownerIdentity, Long sinceTime) {
    return activityDAO.getNumberOfOlderOnActivitiesOfConnections(ownerIdentity, sinceTime);
  }

  @Override
  public int getNumberOfOlderOnUserSpacesActivities(Identity ownerIdentity, Long sinceTime) {
    return activityDAO.getNumberOfOlderOnUserSpacesActivities(ownerIdentity, sinceTime, memberOfSpaceIds(ownerIdentity));
  }

  @Override
  public int getNumberOfOlderOnSpaceActivities(Identity ownerIdentity, Long sinceTime) {
    return activityDAO.getNumberOfOlderOnSpaceActivities(ownerIdentity, sinceTime);
  }

  private Long getCommentID(String commentId) {
    return (commentId == null || commentId.trim().isEmpty()) ? null : Long.valueOf(commentId.replace(COMMENT_PREFIX, ""));
  }

  private String getExoCommentID(Long commentId) {
    return String.valueOf(COMMENT_PREFIX + commentId);
  }

  private void processActivity(ExoSocialActivity existingActivity) {
    Iterator<ActivityProcessor> it = activityProcessors.iterator();
    while (it.hasNext()) {
      try {
        it.next().processActivity(existingActivity);
      } catch (Exception e) {
        LOG.debug("activity processing failed ");
      }
    }
  }
  
  /**
   * Gets the list of spaceIds what the given identify is member
   * 
   * @param ownerIdentity
   * @return
   */
  private List<String> memberOfSpaceIds(Identity ownerIdentity) {
    return spaceStorage.getMemberSpaceIds(ownerIdentity.getId(), 0, -1);
 
   }


  public void setIdentityStorage(IdentityStorage identityStorage) {
    this.identityStorage = identityStorage;
  }
}
