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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.persistence.LockModeType;

import org.apache.commons.lang.ArrayUtils;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.EntityManagerHolder;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.filter.ActivityFilter;
import org.exoplatform.social.core.activity.filter.ActivityUpdateFilter;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ActivityStreamImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.dao.ActivityDAO;
import org.exoplatform.social.core.jpa.storage.dao.ConnectionDAO;
import org.exoplatform.social.core.jpa.storage.dao.StreamItemDAO;
import org.exoplatform.social.core.jpa.storage.entity.ActivityEntity;
import org.exoplatform.social.core.jpa.storage.entity.StreamItemEntity;
import org.exoplatform.social.core.jpa.storage.entity.StreamType;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.ActivityStorageException.Type;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.impl.ActivityBuilderWhere;

public class RDBMSActivityStorageImpl implements ActivityStorage {

  private static final Log LOG = ExoLogger.getLogger(RDBMSActivityStorageImpl.class);
  private final ActivityDAO activityDAO;
  private IdentityStorage identityStorage;
  private final SpaceStorage spaceStorage;
  private final SortedSet<ActivityProcessor> activityProcessors;
  private static final Pattern MENTION_PATTERN = Pattern.compile("@([^\\s]+)|@([^\\s]+)$");
  public final static String COMMENT_PREFIX = "comment";
  private ActivityStorage activityStorage;

  public RDBMSActivityStorageImpl(RelationshipStorage relationshipStorage, 
                                      IdentityStorage identityStorage, 
                                      SpaceStorage spaceStorage,
                                      ActivityDAO activityDAO,
                                      ConnectionDAO connectionDAO,
                                      StreamItemDAO streamItemDAO) {
    this.identityStorage = identityStorage;
    this.activityProcessors = new TreeSet<ActivityProcessor>(processorComparator());
    this.activityDAO = activityDAO;
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
    if(owner != null) {
      stream.setType(owner.getProviderId());
      stream.setPrettyId(owner.getRemoteId());
      stream.setId(owner.getId());
      activity.setStreamOwner(owner.getRemoteId());
    } else {
      LOG.warn("Cannot find stream of activity " + activityEntity.getId() + " since identity " + ownerIdentityId + " does not exist");
    }
    //
    activity.setActivityStream(stream);
    activity.setPosterId(activityEntity.getPosterId());
    //
    activity.isLocked(activityEntity.getLocked());
    activity.isHidden(activityEntity.getHidden());
    activity.setTitleId(activityEntity.getTitleId());
    activity.setPostedTime(activityEntity.getPosted() != null ? activityEntity.getPosted().getTime() : 0);
    activity.setUpdated(activityEntity.getUpdatedDate().getTime());
    //
    List<String> commentPosterIds = new ArrayList<String>();
    List<String> replyToIds = new ArrayList<String>();
    List<ActivityEntity> comments = activityEntity.getComments() != null ? activityEntity.getComments() : new ArrayList<ActivityEntity>();
    fillCommentsIdsAndPosters(comments, commentPosterIds, replyToIds, false);
    activity.setCommentedIds(commentPosterIds.toArray(new String[commentPosterIds.size()]));
    activity.setReplyToId(replyToIds.toArray(new String[replyToIds.size()]));
    activity.setMentionedIds(activityEntity.getMentionerIds().toArray(new String[activityEntity.getMentionerIds().size()]));

    return activity;
  }

  private void fillCommentsIdsAndPosters(List<ActivityEntity> comments,
                                         List<String> commentPosterIds,
                                         List<String> replyToIds,
                                         boolean isSubComment) {
    if (comments == null || comments.isEmpty()) {
      return;
    }
    List<Long> commentIds = new ArrayList<>();
    for (ActivityEntity comment : comments) {
      if (!commentPosterIds.contains(comment.getPosterId())) {
        commentPosterIds.add(comment.getPosterId());
      }
      replyToIds.add(getExoCommentID(comment.getId()));
      commentIds.add(comment.getId());
    }
    if (!isSubComment) {
      List<ActivityEntity> subComments = activityDAO.findCommentsOfActivities(commentIds);
      fillCommentsIdsAndPosters(subComments, commentPosterIds, replyToIds, true);
    }
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
    activityEntity.setMentionerIds(new HashSet<String>(Arrays.asList(processMentions(activity.getTitle(), activity.getTemplateParams()))));
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
    exoComment.isLocked(comment.getLocked() != null ? comment.getLocked().booleanValue() : false);
    exoComment.isHidden(comment.getHidden() != null ? comment.getHidden().booleanValue() : false);
    exoComment.setUpdated(comment.getUpdatedDate() != null ? comment.getUpdatedDate().getTime() : null);
    //
    ActivityEntity parentActivity = getTopParentActivity(comment);
    exoComment.setParentId(parentActivity == null ? null : String.valueOf(parentActivity.getId()));
    exoComment.setParentCommentId(isSubComment(comment) ? getExoCommentID(comment.getParent().getId()) : null);
    //
    exoComment.setPostedTime(comment.getPosted() != null ? comment.getPosted().getTime() : 0);
    exoComment.setUpdated(comment.getUpdatedDate() != null ? comment.getUpdatedDate().getTime() : null);
    //
    Set<String> mentioned = comment.getMentionerIds();
    if (mentioned != null && !mentioned.isEmpty()) {
      exoComment.setMentionedIds(comment.getMentionerIds().toArray(new String[mentioned.size()]));
    }

    //
    Set<String> likers = comment.getLikerIds();
    if (likers != null && !likers.isEmpty() ) {
      exoComment.setLikeIdentityIds(comment.getLikerIds().toArray(new String[likers.size()]));
    }
    processActivity(exoComment);
    
    return exoComment;
  }

  private ActivityEntity getTopParentActivity(ActivityEntity activity) {
    ActivityEntity parent = activity.getParent();
    if (parent == null || (parent.getId() == activity.getId())) {
      return activity;
    } else {
      return getTopParentActivity(parent);
    }
  }

  private boolean isSubComment(ActivityEntity comment) {
    return comment.getParent() != null && comment.getParent().isComment();
  }

  private List<ExoSocialActivity> convertCommentEntitiesToComments(List<ActivityEntity> comments) {
    return convertCommentEntitiesToComments(comments, false);
  }

  private List<ExoSocialActivity> convertCommentEntitiesToComments(List<ActivityEntity> comments, boolean loadSubComments) {
    if (comments == null || comments.isEmpty()) return Collections.emptyList();
    if(loadSubComments) {
      // Add subComments to the list
      List<Long> ids = new ArrayList<>();
      for (ActivityEntity activityEntity : comments) {
        ids.add(activityEntity.getId());
      }
      List<ActivityEntity> subComments = activityDAO.findCommentsOfActivities(ids);

      if(subComments != null && !subComments.isEmpty()) {
        comments.addAll(subComments);
      }

      // Sort sub comments just after the comment
      Collections.sort(comments, new CommentComparator());
    }

    return comments.stream().map(comment -> convertCommentEntityToComment(comment)).collect(Collectors.toList());
  }
  
  private ActivityEntity convertCommentToCommentEntity(ActivityEntity activityEntity, ExoSocialActivity comment) {
    ActivityEntity commentEntity = new ActivityEntity();
    if (comment.getId() != null) {
      commentEntity = activityDAO.find(getCommentID(comment.getId()));
    }
    if (comment.getParentCommentId() != null) {
      ActivityEntity parentCommentEntity = activityDAO.find(getCommentID(comment.getParentCommentId()));
      parentCommentEntity.addComment(commentEntity);
    } else {
      activityEntity.addComment(commentEntity);
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
    Date today = new Date();
    Date commentTime = (comment.getPostedTime() != null ? new Date(comment.getPostedTime()) : today);
    commentEntity.setPosted(commentTime);
    //update time may be different from post time
    Date updateCommentTime = (comment.getUpdated() != null ? comment.getUpdated() : today);
    commentEntity.setUpdatedDate(updateCommentTime);
    commentEntity.setMentionerIds(new HashSet<>(Arrays.asList(processMentions(comment.getTitle(), comment.getTemplateParams()))));
    //
    return commentEntity;
  }

  /**
   * Help to update mentionIds of activity
   * Help to update corresponding StreamItem
   * This method must be called before updating title and template param of activity entity
   *
   * activity's mentionIds include :
   * - mentioned ids processed from its title and template param
   * - mentioned ids from its comments
   *
   * @param activityEntity
   * @param activity
   */
  private void updateActivityMentions(ActivityEntity activityEntity, ExoSocialActivity activity) {
    Set<String> commentMentions = new HashSet<>();
    if (activityEntity.getComments() != null) {
      activityEntity.getComments().forEach(comment -> {
        String[] mentions = processMentions(comment.getTitle(), comment.getTemplateParams());
        commentMentions.addAll(Arrays.asList(mentions));
      });
    }
    Set<String> mentionsToRemove = new HashSet<>(Arrays.asList(processMentions(activityEntity.getTitle(), activityEntity.getTemplateParams())));
    Set<String> mentionToAdd = new HashSet<>(Arrays.asList(processMentions(activity.getTitle(), activity.getTemplateParams())));

    mentionsToRemove.forEach(mentionedId -> {
      if (!commentMentions.contains(mentionedId) && !mentionToAdd.contains(mentionedId)) {
        StreamItemEntity item = new StreamItemEntity(StreamType.MENTIONER);
        item.setOwnerId(Long.parseLong(mentionedId));
        activityEntity.removeStreamItem(item);
      }
    });

    mentionToAdd.forEach(mentionedId -> {
      if (!commentMentions.contains(mentionedId) && !mentionsToRemove.contains(mentionedId)) {
        mention(null, activityEntity, new String[] {mentionedId});
      }
    });

    mentionToAdd.addAll(commentMentions);
    activityEntity.setMentionerIds(mentionToAdd);
  }

  private List<ExoSocialActivity> convertActivityIdsToActivities(List<Long> activityIds) {
    if (activityIds == null)
      return Collections.emptyList();
    // Use getActivityStorage to benifit from Cached Storage
    return activityIds.stream().map(activityId -> getActivityStorage().getActivity(String.valueOf(activityId))).collect(Collectors.toList());
  }

  private List<ExoSocialActivity> convertActivityEntitiesToActivities(List<ActivityEntity> activities) {
    if (activities == null)
      return Collections.emptyList();
    return activities.stream().map(activity -> convertActivityEntityToActivity(activity)).collect(Collectors.toList());
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
      if (entity != null && entity.isComment()) {
        return convertCommentEntityToComment(entity);
      } else {
        return null;
      }
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
    return convertActivityIdsToActivities(activityDAO.getUserActivities(owner, offset, limit));
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getActivities(Identity owner, Identity viewer, long offset, long limit) throws ActivityStorageException {
    return convertActivityIdsToActivities(activityDAO.getActivities(owner, viewer, offset, limit));
  }

  @Override
  public List<ExoSocialActivity> getAllActivities(int index, int limit) {
    return convertActivityEntitiesToActivities(activityDAO.getAllActivities());
  }

  @Override
  @ExoTransactional
  public void saveComment(ExoSocialActivity activity, ExoSocialActivity eXoComment) throws ActivityStorageException {
    ActivityEntity activityEntity = activityDAO.find(Long.valueOf(activity.getId()));
    EntityManagerHolder.get().lock(activityEntity, LockModeType.PESSIMISTIC_WRITE);
    try {
      ActivityEntity commentEntity = convertCommentToCommentEntity(activityEntity, eXoComment);
      commentEntity = activityDAO.create(commentEntity);

      eXoComment.setId(getExoCommentID(commentEntity.getId()));
      eXoComment.setPosterId(commentEntity.getPosterId());
      eXoComment.setParentId(String.valueOf(activity.getId()));
      eXoComment.isComment(true);

      Set<String> mentioned = commentEntity.getMentionerIds();
      if (mentioned != null && !mentioned.isEmpty()) {
        eXoComment.setMentionedIds(mentioned.toArray(new String[mentioned.size()]));
      }

        //
      Identity commenter = identityStorage.findIdentityById(commentEntity.getPosterId());
      saveStreamItemForCommenter(commenter, activityEntity);

      //
      String[] mentioners = processMentions(eXoComment.getTitle(), eXoComment.getTemplateParams());
      if (mentioners != null && mentioners.length > 0) {
        mention(commenter, activityEntity, mentioners);
        activityEntity.setMentionerIds(processMentionOfComment(activityEntity, commentEntity, activityEntity.getMentionerIds().toArray(new String[activityEntity.getMentionerIds().size()]), mentioners, true));
      }

      //
      processActivityStreamUpdatedTime(activityEntity);
      activityDAO.update(activityEntity);

    } finally {
      if (EntityManagerHolder.get().isOpen() && EntityManagerHolder.get().getLockMode(activityEntity) != null
          && EntityManagerHolder.get().getLockMode(activityEntity) != LockModeType.NONE) {
        EntityManagerHolder.get().lock(activityEntity, LockModeType.NONE);
      }
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
    if (ArrayUtils.contains(processMentions(activity.getTitle(), activity.getTemplateParams()), mentioner)) {
      return false;
    }
    List<ActivityEntity> comments = activity.getComments();
    comments.remove(comment);
    for (ActivityEntity cmt : comments) {
      if (ArrayUtils.contains(processMentions(cmt.getTitle(), cmt.getTemplateParams()), mentioner)) {
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
    mention(owner, activity, processMentions(activity.getTitle(), activity.getTemplateParams()));
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
    if (streamType == StreamType.POSTER || streamType == StreamType.SPACE || streamType == StreamType.MENTIONER || streamType == StreamType.COMMENTER) {
      streamItem.setUpdatedDate(activity.getUpdatedDate());
    } else {
      streamItem.setUpdatedDate(null);
    }
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
  private String[] processMentions(String title, Map<String, String> templateParams) {
    Set<String> mentions = new HashSet<>();
    mentions.addAll(parseMention(title));

    getTemplateParamToProcess(templateParams).forEach(
            param -> mentions.addAll(parseMention(param)));

    return mentions.toArray(new String[mentions.size()]);
  }

  private Set<String> parseMention(String str) {
    if (str == null || str.length() == 0) {
      return Collections.emptySet();
    }

    Set<String> mentions = new HashSet<>();
    Matcher matcher = MENTION_PATTERN.matcher(str);
    while (matcher.find()) {
      String remoteId = matcher.group().substring(1);
      Identity identity = identityStorage.findIdentity(OrganizationIdentityProvider.NAME, remoteId);
      // if not the right mention then ignore
      if (identity != null && !mentions.contains(identity.getId())) {
        mentions.add(identity.getId());
      }
    }
    return mentions;
  }


  private List<String> getTemplateParamToProcess(Map<String, String> templateParams){
    List<String> params = new ArrayList<String>();

    if(templateParams != null && templateParams.containsKey(BaseActivityProcessorPlugin.TEMPLATE_PARAM_TO_PROCESS)){
      String[] templateParamKeys = templateParams
              .get(BaseActivityProcessorPlugin.TEMPLATE_PARAM_TO_PROCESS)
              .split(BaseActivityProcessorPlugin.TEMPLATE_PARAM_LIST_DELIM);
      for(String key : templateParamKeys){
        if(templateParams.containsKey(key)){
          params.add(templateParams.get(key));
        }
      }
    }
    return params;
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
    activity.setMentionerIds(processMentionOfComment(activity, comment, activity.getMentionerIds().toArray(new String[activity.getMentionerIds().size()]), processMentions(comment.getTitle(), comment.getTemplateParams()), false));
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
    return convertActivityIdsToActivities(activityDAO.getActivityFeed(ownerIdentity, offset, limit, memberOfSpaceIds(ownerIdentity)));
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
    return convertActivityIdsToActivities(activityDAO.getActivitiesOfConnections(ownerIdentity, offset, limit));
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
    return convertActivityIdsToActivities(activityDAO.getUserSpacesActivities(ownerIdentity, offset, limit, memberOfSpaceIds(ownerIdentity)));
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
  public List<ExoSocialActivity> getComments(ExoSocialActivity existingActivity, boolean loadSubComments, int offset, int limit) {
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
    
    return convertCommentEntitiesToComments(comments, loadSubComments);
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
    ActivityEntity parentActivity = null;
    ActivityEntity updatedActivity = null;
    boolean isComment = existingActivity.getId().startsWith(COMMENT_PREFIX);
    if (isComment) {
      long id = getCommentID(existingActivity.getId());
      updatedActivity = activityDAO.find(id);
      parentActivity = getTopParentActivity(updatedActivity);
    } else {
      parentActivity = updatedActivity = activityDAO.find(Long.valueOf(existingActivity.getId()));
    }

    if(updatedActivity != null) {
      if(isComment) {
        // update comment
        updatedActivity.setUpdatedDate(new Date());
      }
      // only raise the activity in stream when activity date updated
      if (existingActivity.getUpdated() != null && updatedActivity.getUpdatedDate() != null
          && existingActivity.getUpdated().getTime() != updatedActivity.getUpdatedDate().getTime()) {
        processActivityStreamUpdatedTime(updatedActivity);
      }
      //create or remove liker if exist
      processLikerActivityInStreams(new HashSet<>(Arrays.asList(existingActivity.getLikeIdentityIds())), new HashSet<>(updatedActivity.getLikerIds()), parentActivity, isComment);
      //this method must be called before updating title and template params
      updateActivityMentions(updatedActivity, existingActivity);

      if (existingActivity.getTitleId() != null) updatedActivity.setTitleId(existingActivity.getTitleId());
      if (existingActivity.getTitle() != null) updatedActivity.setTitle(existingActivity.getTitle());
      if (existingActivity.getBody() != null) updatedActivity.setBody(existingActivity.getBody());
      if (existingActivity.getUpdated() != null) updatedActivity.setUpdatedDate(existingActivity.getUpdated());
      if (existingActivity.getLikeIdentityIds() != null) updatedActivity.setLikerIds(new HashSet<>(Arrays.asList(existingActivity.getLikeIdentityIds())));
      if (existingActivity.getPermaLink() != null) updatedActivity.setPermaLink(existingActivity.getPermaLink());
      if (existingActivity.getTemplateParams() != null) updatedActivity.setTemplateParams(existingActivity.getTemplateParams());
      updatedActivity.setHidden(existingActivity.isHidden());
      updatedActivity.setComment(existingActivity.isComment());
      updatedActivity.setLocked(existingActivity.isLocked());
      processActivity(existingActivity);

      activityDAO.update(updatedActivity);
    } else {
      throw new ActivityStorageException(Type.FAILED_TO_UPDATE_ACTIVITY, "Cannot find activity with id=" + existingActivity.getId());
    }
  }

  private void processLikerActivityInStreams(Set<String> newLikerList, Set<String> oldLikerList, ActivityEntity activity, boolean commentLike) {
    for (String id : newLikerList) {
      if (!oldLikerList.contains(id)) {//new like ==> create stream item
        createStreamItem(commentLike ? StreamType.COMMENT_LIKER : StreamType.LIKER, activity, Long.parseLong(id));
      } else {
        oldLikerList.remove(id);
      }
    }
    if (oldLikerList.size() > 0) {//unlike ==> remove stream item
      for (String id : oldLikerList) {
        StreamItemEntity item = new StreamItemEntity(StreamType.LIKER);
        item.setOwnerId(Long.parseLong(id));
        activity.removeStreamItem(item);
        item.setStreamType(StreamType.COMMENT_LIKER);
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
    return convertActivityIdsToActivities(activityDAO.getSpaceActivities(spaceIdentity, offset, limit));
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getSpaceActivitiesForUpgrade(Identity spaceIdentity, int offset, int limit) {
    return convertActivityIdsToActivities(activityDAO.getSpaceActivities(spaceIdentity, offset, limit));
  }

  @Override
  public List<ExoSocialActivity> getActivitiesByPoster(Identity posterIdentity, int offset, int limit) {
    return getActivitiesByPoster(posterIdentity, offset, limit, new String[]{});
  }

  @Override
  public List<ExoSocialActivity> getActivitiesByPoster(Identity posterIdentity, int offset, int limit, String... activityTypes) {
    return convertActivityIdsToActivities(activityDAO.getActivitiesByPoster(posterIdentity, offset, limit, activityTypes));
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
    return convertActivityIdsToActivities(activityDAO.getNewerOnActivityFeed(owner, sinceTime, limit, memberOfSpaceIds(owner)));
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getNewerUserActivities(Identity owner, Long sinceTime, int limit) {
    return convertActivityIdsToActivities(activityDAO.getNewerOnUserActivities(owner, sinceTime, limit));
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getNewerUserSpacesActivities(Identity owner, Long sinceTime, int limit) {
    return convertActivityIdsToActivities(activityDAO.getNewerOnUserSpacesActivities(owner, sinceTime, limit, memberOfSpaceIds(owner)));
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getNewerActivitiesOfConnections(Identity owner, Long sinceTime, int limit) {
    return convertActivityIdsToActivities(activityDAO.getNewerOnActivitiesOfConnections(owner, sinceTime, limit));
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getNewerSpaceActivities(Identity owner, Long sinceTime, int limit) {
    return convertActivityIdsToActivities(activityDAO.getNewerOnSpaceActivities(owner, sinceTime, limit));
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getOlderFeedActivities(Identity owner, Long sinceTime, int limit) {
    return convertActivityIdsToActivities(activityDAO.getOlderOnActivityFeed(owner, sinceTime, limit, memberOfSpaceIds(owner)));
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getOlderUserActivities(Identity owner, Long sinceTime, int limit) {
    return convertActivityIdsToActivities(activityDAO.getOlderOnUserActivities(owner, sinceTime, limit));
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getOlderUserSpacesActivities(Identity owner, Long sinceTime, int limit) {
    return convertActivityIdsToActivities(activityDAO.getOlderOnUserSpacesActivities(owner, sinceTime, limit, memberOfSpaceIds(owner)));
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getOlderActivitiesOfConnections(Identity owner, Long sinceTime, int limit) {
    return convertActivityIdsToActivities(activityDAO.getOlderOnActivitiesOfConnections(owner, sinceTime, limit));
  }

  @Override
  @ExoTransactional
  public List<ExoSocialActivity> getOlderSpaceActivities(Identity owner, Long sinceTime, int limit) {
    return convertActivityIdsToActivities(activityDAO.getOlderOnSpaceActivities(owner, sinceTime, limit));
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

  @Override
  public List<ExoSocialActivity> getSubComments(ExoSocialActivity comment) {
    long commentId = getCommentID(comment.getId());
    List<ActivityEntity> subComments = activityDAO.getComments(commentId, 0, -1);
    return convertCommentEntitiesToComments(subComments, false);
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
   * This method will update StreamItem updatedDate field,
   * which is used to sort activies in Activity Stream
   * We only update this in 3 cases:
   * 1. edit activity message
   * 2. edit activity's comment message
   * 3. add new comment
   * @param activityEntity
   * @return
   */
  private ActivityEntity processActivityStreamUpdatedTime(ActivityEntity activityEntity) {
    if (activityEntity.getStreamItems() != null) {
      List<StreamItemEntity> items = activityEntity.getStreamItems().stream()
              .filter(item -> item.getStreamType() == StreamType.POSTER || item.getStreamType() == StreamType.SPACE)
              .collect(Collectors.toList());
      if (!items.isEmpty()) {
        Date now = new Date();
        items.stream().forEach(item -> item.setUpdatedDate(now));
      }
    }
    return activityEntity;
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

  public static final class CommentComparator implements Comparator<ActivityEntity> {
    public int compare(ActivityEntity o1, ActivityEntity o2) {
      ActivityEntity parent1 = o1.getParent();
      ActivityEntity parent2 = o2.getParent();

      boolean isParentActivity1 = parent1 == null || !parent1.isComment();
      boolean isParentActivity2 = parent2 == null || !parent2.isComment();

      if (isParentActivity1 && isParentActivity2) {
        return o1.getPosted().compareTo(o2.getPosted());
      } else if (isParentActivity1) {
        return compare(o1, parent2);
      } else if (isParentActivity2) {
        return compare(parent1, o2);
      } else if (parent1.getId() == parent2.getId()) {
        return o1.getPosted().compareTo(o2.getPosted());
      } else {
        return compare(parent1, parent2);
      }
    }
  }

  public ActivityStorage getActivityStorage() {
    if (activityStorage == null) {
      activityStorage = CommonsUtils.getService(ActivityStorage.class);
      // This may happen in Test context
      if (activityStorage == null) {
        activityStorage = this;
      }
    }
    return activityStorage;
  }

  @Override
  public List<ExoSocialActivity> getActivities(List<String> activityIdList) {
    if (activityIdList == null || activityIdList.isEmpty()) {
      return Collections.emptyList();
    }
    List<Long> activityIds = new ArrayList<>();
    for (String activityId : activityIdList) {
      if (activityId == null || activityId.isEmpty()) {
        continue;
      }

      if (activityId != null && activityId.startsWith(COMMENT_PREFIX)) {
        activityIds.add(getCommentID(activityId));
      } else {
        activityIds.add(Long.valueOf(activityId));
      }
    }
    List<ActivityEntity> activityEntities = activityDAO.findActivities(activityIds);
    if (activityEntities == null || activityEntities.isEmpty()) {
      return Collections.emptyList();
    }
    List<ExoSocialActivity> activityDTOs = new ArrayList<>();
    for (ActivityEntity activityEntity : activityEntities) {
      activityDTOs.add(convertActivityEntityToActivity(activityEntity));
    }
    return activityDTOs;
  }
}
