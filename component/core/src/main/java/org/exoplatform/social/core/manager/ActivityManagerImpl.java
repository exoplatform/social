/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.social.core.manager;

import java.util.*;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.file.model.FileItem;
import org.exoplatform.commons.file.services.FileService;
import org.exoplatform.commons.file.services.FileStorageException;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.ActivityProcessor;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.*;
import org.exoplatform.social.core.activity.ActivitiesRealtimeListAccess.ActivityType;
import org.exoplatform.social.core.activity.model.*;
import org.exoplatform.social.core.application.SpaceActivityPublisher;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.ActivityStorageException;
import org.exoplatform.social.core.storage.api.ActivityStorage;

/**
 * Class ActivityManagerImpl implements ActivityManager without caching.
 *
 * @author <a href="mailto:vien_levan@exoplatform.com">vien_levan</a>
 * @author <a href="hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Nov 24, 2010
 * @since 1.2.0-GA
 */
public class ActivityManagerImpl implements ActivityManager {
  /** Logger */
  private static final Log            LOG                             = ExoLogger.getLogger(ActivityManagerImpl.class);

  /** The activityStorage. */
  protected ActivityStorage           activityStorage;

  /** identityManager to get identity for saving and getting activities */
  protected IdentityManager           identityManager;

  private UserACL                     userACL;

  /** spaceService */
  protected SpaceService              spaceService;

  private FileService                 fileService;

  protected ActivityLifeCycle         activityLifeCycle               = new ActivityLifeCycle();

  /**
   * Default limit for deprecated methods to get maximum number of activities.
   */
  private static final int            DEFAULT_LIMIT                   = 20;

  /**
   * The list of enabled/disabled activity types by exo properties.
   */
  private static Map<String, Boolean> activityTypesRegistry           = new HashMap<>();

  /**
   * Exo property pattern used for disable activity type
   */
  private static final String         ACTIVITY_TYPE_PROPERTY_PATTERN  = "exo\\.activity-type\\..*\\.enabled";

  /**
   * Exo property pattern prefix
   */
  private static final String         PREFIX                          = "exo.activity-type.";

  /**
   * Exo property pattern suffix
   */
  private static final String         SUFFIX                          = ".enabled";

  /**
   * exo property for editing activity permission
   */
  public static final String          ENABLE_EDIT_ACTIVITY            = "exo.edit.activity.enabled";

  public static final String          ENABLE_EDIT_COMMENT             = "exo.edit.comment.enabled";

  public static final String          ENABLE_MANAGER_EDIT_ACTIVITY    = "exo.manager.edit.activity.enabled";

  public static final String          ENABLE_MANAGER_EDIT_COMMENT     = "exo.manager.edit.comment.enabled";

  private int                         maxUploadSize                   = 10;

  private boolean                     enableEditActivity              = true;

  private boolean                     enableEditComment               = true;

  private boolean                     enableManagerEditActivity       = true;

  private boolean                     enableManagerEditComment        = true;

  public static final List<String>    AUTOMATIC_EDIT_TITLE_ACTIVITIES = Arrays.asList("has_joined",
                                                                                      "space_avatar_edited",
                                                                                      "space_description_edited",
                                                                                      "space_renamed",
                                                                                      "manager_role_revoked",
                                                                                      "manager_role_granted");

  public static final String          SEPARATOR_REGEX                 = "\\|@\\|";

  public static final String          ID                              = "id";

  public static final String          STORAGE                         = "storage";

  public static final String          FILE                            = "file";

  /**
   * Instantiates a new activity manager.
   *
   * @param activityStorage
   * @param identityManager
   */
  public ActivityManagerImpl(ActivityStorage activityStorage,
                             IdentityManager identityManager,
                             UserACL userACL,
                             FileService fileService,
                             InitParams params) {
    this.activityStorage = activityStorage;
    this.identityManager = identityManager;
    this.userACL = userACL;
    this.fileService = fileService;
    initActivityTypes();

    if (params != null) {
      if (params.containsKey("upload.limit.size")
          && StringUtils.isNotBlank(params.getValueParam("upload.limit.size").getValue())) {
        maxUploadSize = Integer.parseInt(params.getValueParam("upload.limit.size").getValue());
      }
      if (params.containsKey(ENABLE_EDIT_ACTIVITY)) {
        enableEditActivity = Boolean.parseBoolean(params.getValueParam(ENABLE_EDIT_ACTIVITY).getValue());
      }
      if (params.containsKey(ENABLE_EDIT_COMMENT)) {
        enableEditComment = Boolean.parseBoolean(params.getValueParam(ENABLE_EDIT_COMMENT).getValue());
      }
      if (params.containsKey(ENABLE_MANAGER_EDIT_ACTIVITY)) {
        enableManagerEditActivity = Boolean.parseBoolean(params.getValueParam(ENABLE_MANAGER_EDIT_ACTIVITY).getValue());
      }
      if (params.containsKey(ENABLE_MANAGER_EDIT_COMMENT)) {
        enableManagerEditComment = Boolean.parseBoolean(params.getValueParam(ENABLE_MANAGER_EDIT_COMMENT).getValue());
      }
    } else {
      String maxUploadString = System.getProperty("wcm.connector.drives.uploadLimit");
      if (StringUtils.isNotBlank(maxUploadString)) {
        maxUploadSize = Integer.parseInt(maxUploadString);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void saveActivityNoReturn(Identity streamOwner, ExoSocialActivity newActivity) {
    if (!streamOwner.isEnable()) {
      LOG.warn("Activity could not be saved. Owner has been disabled.");
      return;
    }

    if (newActivity.getType() != null && activityTypesRegistry.get(newActivity.getType()) != null
        && !activityTypesRegistry.get(newActivity.getType())) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Activity could not be saved. Activity Type {} has been disabled.", newActivity.getType());
      }
      return;
    }

    activityStorage.saveActivity(streamOwner, newActivity);
    activityLifeCycle.saveActivity(newActivity);
  }

  /**
   * {@inheritDoc}
   */
  public void saveActivityNoReturn(ExoSocialActivity newActivity) {
    Identity owner = getStreamOwner(newActivity);
    saveActivityNoReturn(owner, newActivity);
  }

  /**
   * {@inheritDoc}
   */
  public void saveActivity(Identity streamOwner, String activityType, String activityTitle) {
    if (activityType != null && activityTypesRegistry.get(activityType) != null && !activityTypesRegistry.get(activityType)) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Activity could not be saved. Activity Type {} has been disabled.", activityType);
      }
      return;
    }
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setType(activityType);
    activity.setTitle(activityTitle);
    saveActivity(streamOwner, activity);
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity getActivity(String activityId) {
    return activityStorage.getActivity(activityId);
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity getParentActivity(ExoSocialActivity comment) {
    return activityStorage.getParentActivity(comment);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ExoSocialActivity> getSubComments(ExoSocialActivity comment) {
    return activityStorage.getSubComments(comment);
  }



  /**
   * {@inheritDoc}
   */
  public void updateActivity(ExoSocialActivity existingActivity) {
    //by default, event is broadcasted
    updateActivity(existingActivity,true);
  }

  /**
   * {@inheritDoc}
   */
  public void updateActivity(ExoSocialActivity existingActivity, boolean broadcast) {
    String activityId = existingActivity.getId();

    // In order to get the added mentions in the ActivityMentionPlugin we need
    // to
    // pass the previous mentions in the activity, since there is no way to do
    // so,
    // as a solution we pass them throw the activity's template params
    String[] previousMentions = getActivity(activityId).getMentionedIds();
    activityStorage.updateActivity(existingActivity);

    if (previousMentions.length > 0) {
      String mentions = String.join(",", previousMentions);
      Map<String, String> mentionsTemplateParams = existingActivity.getTemplateParams() != null ? existingActivity.getTemplateParams() : new HashMap<>();
      mentionsTemplateParams.put("PreviousMentions", mentions);

      existingActivity.setTemplateParams(mentionsTemplateParams);
    }
    if (broadcast) {
      activityLifeCycle.updateActivity(existingActivity);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void deleteActivity(ExoSocialActivity existingActivity) {
    Validate.notNull(existingActivity.getId(), "existingActivity.getId() must not be null!");
    deleteActivity(existingActivity.getId());
  }

  /**
   * {@inheritDoc}
   */
  public void deleteActivity(String activityId) {
    activityStorage.deleteActivity(activityId);
  }

  /**
   * {@inheritDoc}
   */
  public void saveComment(ExoSocialActivity existingActivity, ExoSocialActivity newComment) {
    if (existingActivity == null) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_SAVE_COMMENT, "Activity cannot be NULL");
    }
    if (existingActivity.getId() == null) {
      LOG.debug("Comment could not be saved because activity id is null.");
      return;
    }
    String activityType = existingActivity.getType();
    String commentActivityType = newComment.getType();
    String commentId = newComment.getId();
    // If activity Type is disabled, comment's can't be added
    // If comment activity Type is disabled, comment's can't be added
    // If existingActivity.getId() == null for the new activity if it's disabled
    // comment should be added for the old created activity if it's disabled
    boolean commentActivityTypeDisabled = commentActivityType != null && activityTypesRegistry.containsKey(commentActivityType)
        && activityTypesRegistry.get(commentActivityType) == Boolean.FALSE;
    boolean activityTypeDisabled = activityType != null && activityTypesRegistry.containsKey(activityType)
        && activityTypesRegistry.get(activityType) == Boolean.FALSE;
    if (commentActivityTypeDisabled || activityTypeDisabled) {
      if (LOG.isDebugEnabled()) {
        if (activityTypeDisabled) {
          LOG.debug("Comment could not be saved. Activity Type {} is disabled.", activityType);
        }
        if (commentActivityTypeDisabled) {
          LOG.debug("Comment could not be saved. Comment activity Type {} is disabled.", commentActivityType);
        }
      }
      return;
    }

    // In order to get the added mentions in the ActivityMentionPlugin we need
    // to
    // pass the previous mentions in the activity, since there is no way to do
    // so,
    // as a solution we pass them throw the activity's template params
    String[] previousMentions = StringUtils.isEmpty(commentId) ? new String[0] : getActivity(commentId).getMentionedIds();
    activityStorage.saveComment(existingActivity, newComment);

    if (StringUtils.isEmpty(commentId)) {
      activityLifeCycle.saveComment(newComment);
    } else {
      if (previousMentions.length > 0) {
        String mentions = String.join(",", previousMentions);
        Map<String, String> mentionsTemplateParams = newComment.getTemplateParams() != null ? newComment.getTemplateParams() : new HashMap<>();
        mentionsTemplateParams.put("PreviousMentions", mentions);

        newComment.setTemplateParams(mentionsTemplateParams);
      }
      activityLifeCycle.updateComment(newComment);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public RealtimeListAccess<ExoSocialActivity> getCommentsWithListAccess(ExoSocialActivity existingActivity,
                                                                         boolean loadSubComments) {
    return new CommentsRealtimeListAccess(activityStorage, existingActivity, loadSubComments);
  }

  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getCommentsWithListAccess(ExoSocialActivity existingActivity) {
    return new CommentsRealtimeListAccess(activityStorage, existingActivity);
  }

  /**
   * {@inheritDoc}
   */
  public void deleteComment(String activityId, String commentId) {
    activityStorage.deleteComment(activityId, commentId);
  }

  /**
   * {@inheritDoc}
   */
  public void deleteComment(ExoSocialActivity existingActivity, ExoSocialActivity existingComment) {
    deleteComment(existingActivity.getId(), existingComment.getId());
  }

  /**
   * {@inheritDoc}
   */
  public void saveLike(ExoSocialActivity existingActivity, Identity identity) {
    String[] identityIds = existingActivity.getLikeIdentityIds();
    if (ArrayUtils.contains(identityIds, identity.getId())) {
      LOG.warn("activity is already liked by identity: " + identity);
      return;
    }
    identityIds = (String[]) ArrayUtils.add(identityIds, identity.getId());
    existingActivity.setLikeIdentityIds(identityIds);
    //broadcast is false : we don't want to launch update listeners for a like
    updateActivity(existingActivity, false);
    if(existingActivity.isComment()){
      activityLifeCycle.likeComment(existingActivity);
    } else {
      activityLifeCycle.likeActivity(existingActivity);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void deleteLike(ExoSocialActivity activity, Identity identity) {
    activity.setTitle(null);
    activity.setBody(null);
    activity.setTemplateParams(null);
    String[] identityIds = activity.getLikeIdentityIds();
    if (ArrayUtils.contains(identityIds, identity.getId())) {
      identityIds = (String[]) ArrayUtils.removeElement(identityIds, identity.getId());
      activity.setLikeIdentityIds(identityIds);
      //broadcast is false : we don't want to launch update listeners for a like
      updateActivity(activity, false);
    } else {
      LOG.warn("activity is not liked by identity: " + identity);
    }
  }

  @Override
  public void addActivityEventListener(ActivityListenerPlugin activityListenerPlugin) {
    registerActivityListener(activityListenerPlugin);
  }

  /**
   * {@inheritDoc}
   */
  public void registerActivityListener(ActivityListener listener) {
    activityLifeCycle.addListener(listener);
  }

  /**
   * {@inheritDoc}
   */
  public void unregisterActivityListener(ActivityListener listener) {
    activityLifeCycle.removeListener(listener);
  }

  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivitiesWithListAccess(Identity existingIdentity) {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.USER_ACTIVITIES, existingIdentity);
  }

  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivitiesWithListAccess(Identity ownerIdentity, Identity viewerIdentity) {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.VIEW_USER_ACTIVITIES, ownerIdentity, viewerIdentity);
  }

  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivitiesOfConnectionsWithListAccess(Identity existingIdentity) {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.CONNECTIONS_ACTIVITIES, existingIdentity);
  }

  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivitiesOfUserSpacesWithListAccess(Identity existingIdentity) {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.USER_SPACE_ACTIVITIES, existingIdentity);
  }

  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivitiesOfSpaceWithListAccess(Identity existingSpaceIdentity) {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.SPACE_ACTIVITIES, existingSpaceIdentity);
  }

  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivityFeedWithListAccess(Identity existingIdentity) {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.ACTIVITY_FEED, existingIdentity);
  }

  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivitiesByPoster(Identity posterIdentity) {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.POSTER_ACTIVITIES, posterIdentity);
  }

  /**
   * {@inheritDoc}
   */
  public RealtimeListAccess<ExoSocialActivity> getActivitiesByPoster(Identity posterIdentity, String... activityTypes) {
    return new ActivitiesRealtimeListAccess(activityStorage,
                                            ActivityType.POSTER_AND_TYPES_ACTIVITIES,
                                            posterIdentity,
                                            activityTypes);
  }

  /**
   * {@inheritDoc}
   */
  public void addProcessor(ActivityProcessor processor) {
    activityStorage.getActivityProcessors().add(processor);
    LOG.debug("added activity processor " + processor.getClass());
  }

  /**
   * {@inheritDoc}
   */
  public void addProcessorPlugin(BaseActivityProcessorPlugin plugin) {
    this.addProcessor(plugin);
  }

  public void initActivityTypes() {
    Properties properties = PropertyManager.getPropertiesByPattern(ACTIVITY_TYPE_PROPERTY_PATTERN);
    properties.forEach((k, v) -> {
      String value = properties.getProperty(k.toString());
      String name = k.toString().substring(PREFIX.length(), k.toString().lastIndexOf(SUFFIX));
      if (value != null && value.equalsIgnoreCase("false")) {
        LOG.info("Activity Type key:  {},  registration status: disabled", name);
        activityTypesRegistry.putIfAbsent(name, false);
      } else {
        LOG.info("Activity Type key:  {},  registration status: enabled", name);
        activityTypesRegistry.putIfAbsent(name, true);
      }
    });
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity saveActivity(Identity streamOwner, ExoSocialActivity newActivity) {
    ExoSocialActivity created = activityStorage.saveActivity(streamOwner, newActivity);
    activityLifeCycle.saveActivity(getActivity(created.getId()));
    return created;
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity saveActivity(ExoSocialActivity newActivity) {
    saveActivityNoReturn(newActivity);
    return newActivity;
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getActivities(Identity identity) {
    List<ExoSocialActivity> activityList = Collections.emptyList();
    try {
      ExoSocialActivity[] activities = getActivitiesWithListAccess(identity).load(0, DEFAULT_LIMIT);
      activityList = Arrays.asList(activities);
    } catch (Exception e) {
      LOG.warn("Failed to get activities by identity: " + identity);
    }
    return activityList;
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getActivities(Identity identity,
                                               long start,
                                               long limit) {
    return activityStorage.getUserActivities(identity, start, limit);
  }

  /**
   * {@inheritDoc} The result list is returned with 30 maximum activities.
   */
  public List<ExoSocialActivity> getActivitiesOfConnections(Identity ownerIdentity) {
    List<ExoSocialActivity> activityList = Collections.emptyList();
    try {
      ExoSocialActivity[] activities = getActivitiesOfConnectionsWithListAccess(ownerIdentity).load(0, 30);
      activityList = Arrays.asList(activities);
    } catch (Exception e) {
      LOG.warn("Failed to get activities of connections!");
    }
    return activityList;
  }

  /**
   * {@inheritDoc} By default, the activity list is composed of all spaces'
   * activities. Each activity list of the space contains maximum 20 activities
   * and are returned sorted starting from the most recent.
   */
  public List<ExoSocialActivity> getActivitiesOfUserSpaces(Identity ownerIdentity) {
    return getActivitiesOfUserSpacesWithListAccess(ownerIdentity).loadAsList(0, DEFAULT_LIMIT);
  }

  /**
   * {@inheritDoc} Return maximum number of activities: 40
   */
  public List<ExoSocialActivity> getActivityFeed(Identity identity) {
    return getActivityFeedWithListAccess(identity).loadAsList(0, DEFAULT_LIMIT * 2);
  }

  /**
   * {@inheritDoc}
   */
  public void removeLike(ExoSocialActivity existingActivity, Identity existingIdentity) {
    deleteLike(existingActivity, existingIdentity);
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> getComments(ExoSocialActivity existingActivity) {
    return getCommentsWithListAccess(existingActivity).loadAsList(0, DEFAULT_LIMIT * 2);
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity recordActivity(Identity owner, String type, String title) {
    ExoSocialActivity newActivity = new ExoSocialActivityImpl(owner.getId(), type, title);
    saveActivity(owner, newActivity);
    return newActivity;
  }

  /**
   * {@inheritDoc}
   */
  public int getActivitiesCount(Identity owner) {
    return activityStorage.getNumberOfUserActivities(owner);
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity recordActivity(Identity owner, ExoSocialActivity activity) throws Exception {
    saveActivity(owner, activity);
    return activity;
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity recordActivity(Identity owner,
                                          String type,
                                          String title,
                                          String body) {
    String userId = owner.getId();
    ExoSocialActivity activity = new ExoSocialActivityImpl(userId, type, title, body);
    saveActivity(owner, activity);
    return activity;
  }

  /**
   * Gets stream owner from identityId = newActivity.userId.
   * 
   * @param newActivity the new activity
   * @return the identity stream owner
   */
  private Identity getStreamOwner(ExoSocialActivity newActivity) {
    Validate.notNull(newActivity.getUserId(), "activity.getUserId() must not be null!");
    return identityManager.getIdentity(newActivity.getUserId(), false);
  }

  @Override
  public RealtimeListAccess<ExoSocialActivity> getAllActivitiesWithListAccess() {
    return new ActivitiesRealtimeListAccess(activityStorage, ActivityType.ALL);
  }

  @Override
  public int getMaxUploadSize() {
    return maxUploadSize;
  }

  @Override
  public List<ExoSocialActivity> getActivities(List<String> activityIdList) {
    return activityStorage.getActivities(activityIdList);
  }

  @Override
  public boolean isActivityEditable(ExoSocialActivity activity, org.exoplatform.services.security.Identity viewer) {
    if (activity != null) {
      boolean enableEdit;
      boolean enableManagerEdit;
      if (activity.isComment()) {
        enableEdit = enableEditComment;
        enableManagerEdit = enableManagerEditComment;
      } else {
        enableEdit = enableEditActivity;
        enableManagerEdit = enableManagerEditActivity;
      }
      Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, viewer.getUserId());
      if (enableEdit && identity != null && (StringUtils.equals(identity.getId(), activity.getPosterId())
          || (enableManagerEdit && viewer.getGroups().contains(userACL.getAdminGroups())))) {
        return !activity.isComment() || !isAutomaticComment(activity);
      }
    }
    return false;
  }

  @Override
  public List<String> getActivityFilesIds(ExoSocialActivity activity) {
    List<String> values = new ArrayList<>();
    if (activity != null) {
      String[] ids = getParameterValues(activity.getTemplateParams(), ID);
      String[] storages = getParameterValues(activity.getTemplateParams(), STORAGE);
      if (ids != null && ids.length > 0) {
        for (int i = 0; i < ids.length; i++) {
          if (storages != null
              && storages.length > i
              && storages[i].equals(FILE)) {
            values.add(ids[i]);
          }
        }
      }
    }
    return values;
  }

  @Override
  public ActivityFile getActivityFileById(long fileId) throws Exception {
    FileItem file = null;
    try {
      file = fileService.getFile(fileId);
    } catch (FileStorageException e) {
      LOG.error("Failed to  get the file with id : " + fileId, e);
    }
    return convertFileItemToActivityFile(file);
  }

  public boolean isAutomaticComment(ExoSocialActivity activity) {
    // Only not automatic created comments are editable
    return activity != null && (!SpaceActivityPublisher.SPACE_APP_ID.equals(activity.getType())
        || (SpaceActivityPublisher.SPACE_APP_ID.equals(activity.getType())
            && AUTOMATIC_EDIT_TITLE_ACTIVITIES.contains(activity.getTitleId())));
  }

  private String[] getParameterValues(Map<String, String> activityParams, String paramName) {
    String[] values = null;
    String value = activityParams.get(paramName);
    if (value == null) {
      value = activityParams.get(paramName.toLowerCase());
    }
    if (value != null) {
      values = value.split(SEPARATOR_REGEX);
    }
    return values;
  }

  private ActivityFile convertFileItemToActivityFile(FileItem fileItem) throws Exception {
    ActivityFile activityFile = new ActivityFile();

    activityFile.setInputStream(fileItem.getAsStream());
    activityFile.setName(fileItem.getFileInfo().getName());
    activityFile.setMimeType(fileItem.getFileInfo().getMimetype());
    long lastUpdated = fileItem.getFileInfo().getUpdatedDate() != null ? fileItem.getFileInfo().getUpdatedDate().getTime()
                                                                       : (new Date().getTime());
    activityFile.setLastModified(lastUpdated);
    return activityFile;
  }

}
