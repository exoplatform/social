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
package org.exoplatform.social.core.storage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.jcr.JCRSessionManager;
import org.exoplatform.social.common.jcr.NodeProperty;
import org.exoplatform.social.common.jcr.NodeType;
import org.exoplatform.social.common.jcr.QueryBuilder;
import org.exoplatform.social.common.jcr.SocialDataLocation;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;

/**
 * The Class JCRStorage represents storage for activity manager.
 * {@link org.exoplatform.social.core.manager.ActivityManager} is the access point for activity service, do not access
 * {@link ActivityStorage} directly though your application.
 *
 * @see org.exoplatform.social.core.manager.ActivityManager
 */
public class ActivityStorage {
  /** The logger */
  private static final Log LOG = ExoLogger.getLogger(ActivityStorage.class);

  /** The Constant PUBLISHED_NODE. */
  private static final String PUBLISHED_NODE = "published".intern();

  private static final String COMMENT_IDS_DELIMITER = ",";

  /** The data location. */
  private SocialDataLocation dataLocation;

  /** The session manager. */
  private JCRSessionManager sessionManager;

  /** The activityServiceHome node */
  private Node activityServiceHome;

  private IdentityManager identityManager;

  /**
   * Hack for unit test to work
   */
  private String portalOwner = "classic";

  /**
   * Instantiates a new JCR storage base on SocialDataLocation
   * @param dataLocation the data location.
   * @param identityManager the identity manager instance
   */
  public ActivityStorage(SocialDataLocation dataLocation, IdentityManager identityManager) {
    this.dataLocation = dataLocation;
    this.identityManager = identityManager;
    sessionManager = dataLocation.getSessionManager();
  }

  /**
   * Saves an activity into a stream.
   * Note that the field {@link org.exoplatform.social.core.activity.model.ExoSocialActivity#setUserId(String)}
   * should be the id of an identity {@link Identity#getId()}
   * @param owner owner of the stream where this activity is bound.
   *              Usually a user or space identity
   * @param activity the activity to save
   * @return stored activity
   * @throws ActivityStorageException activity storage exception with type: ActivityStorageException.Type.FAILED_TO_SAVE_ACTIVITY
   * @since 1.1.1
   */
  //TODO hoatle: we force title is mandatory; the spec says that if title is not
  // available, titleId must be available. We haven't processed titleId yet, so leave title is mandatory
  public ExoSocialActivity saveActivity(Identity owner, ExoSocialActivity activity) throws ActivityStorageException {
    try {
      Validate.notNull(owner, "owner must not be null.");
      Validate.notNull(activity, "activity must not be null.");
      Validate.notNull(activity.getUpdated(), "Activity.getUpdated() must not be null.");
      Validate.notNull(activity.getPostedTime(), "Activity.getPostedTime() must not be null.");
      Validate.notNull(activity.getTitle(), "Activity.getTitle() must not be null.");
    } catch (IllegalArgumentException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.ILLEGAL_ARGUMENTS, e.getMessage(), e);
    }

    if (activity.getUserId() == null) {
      activity.setUserId(owner.getId());
    }

    try {
      Node activityHomeNode = getPublishedActivityServiceHome(owner);
      Node activityNode;

      Session session = sessionManager.getOrOpenSession();
      if (activity.getId() == null) {
        activityNode = activityHomeNode.addNode(NodeType.NT_ACTIVITY, NodeType.NT_ACTIVITY);
        activityNode.addMixin(NodeType.MIX_REFERENCEABLE);
      } else {
        activityNode = session.getNodeByUUID(activity.getId());
      }

      setStreamInfo(activity, activityNode);

      setActivityNodeFromActivity(activityNode, activity);

      if (activity.getId() == null) {
        activityHomeNode.save();
        activity.setId(activityNode.getUUID());
      } else {
        activityNode.save();
      }
    } catch (Exception e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_SAVE_ACTIVITY, e.getMessage(), e);
    } finally {
      sessionManager.closeSession(true);
    }
    return activity;
  }

  /**
   * Deletes activity by its id.
   * This will delete comments from this activity first, then delete the activity.
   *
   * @param activityId the activity id
   */
  public void deleteActivity(String activityId) throws ActivityStorageException {
    try {
      //TODO check if this is a comment
      deleteActivityComments(activityId);
      Session session = sessionManager.getOrOpenSession();
      Node activityNode = session.getNodeByUUID(activityId);
      if (activityNode != null) {
        activityNode.remove();
      } else {
        throw new Exception("Failed to delete activityId: " + activityId + ": not found");
      }
    } catch(Exception e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_DELETE_ACTIVITY, e.getMessage(), e);
    } finally {
      sessionManager.closeSession(true);
    }
  }

  /**
   * Deletes a stored activity
   *
   * @param storedActivity
   * @since 1.1.1
   */
  public void deleteActivity(ExoSocialActivity storedActivity) throws ActivityStorageException {
    if (storedActivity.getId() == null) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_DELETE_ACTIVITY, "Failed to delete this activity. It is not stored in JCR yet.");
    }
    deleteActivity(storedActivity.getId());
  }

  /**
   * Save comment to an activity.
   * activity's ownerstream has to be the same as ownerStream param here.
   *
   * @param activity
   * @param comment
   * @since 1.1.1
   */
  public void saveComment(ExoSocialActivity activity, ExoSocialActivity comment) throws ActivityStorageException {
    try {
      Validate.notNull(activity, "activity must not be null.");
      Validate.notNull(comment.getUserId(), "comment.getUserId() must not be null.");
      Validate.notNull(comment.getTitle(), "comment.getTitle() must not be null.");
    } catch (IllegalArgumentException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.ILLEGAL_ARGUMENTS, e.getMessage(), e);
    }
    long currentTimeMillis = System.currentTimeMillis();
    if (comment.getId() != null) { // allows users to edit its comment?
      comment.setUpdated(new Date(currentTimeMillis));
    } else {
      comment.setPostedTime(currentTimeMillis);
      comment.setUpdated(new Date(currentTimeMillis));
    }
    comment.setReplyToId(ExoSocialActivity.IS_COMMENT);
    Identity ownerStream = identityManager.getIdentity(activity.getUserId());
    try {
      comment = saveActivity(ownerStream, comment);
      String rawCommentIds = activity.getReplyToId();
      if (rawCommentIds == null) {
        rawCommentIds = "";
      }
      rawCommentIds += COMMENT_IDS_DELIMITER + comment.getId();
      activity.setReplyToId(rawCommentIds);
      saveActivity(ownerStream, activity);
    } catch (ActivityStorageException e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_SAVE_COMMENT, e.getMessage(), e);
    }

  }

  /**
   * Delete comment by its id.
   *
   * @param activityId
   * @param commentId
   */
  public void deleteComment(String activityId, String commentId) throws ActivityStorageException {
    ExoSocialActivity activity = null;
    try {
      activity = getActivity(activityId);
      String rawCommentIds = activity.getReplyToId();
      //rawCommentIds can be: null || ,a,b,c,d
      if (rawCommentIds != null && rawCommentIds.contains(commentId)) {
        ExoSocialActivity comment = getActivity(commentId);
        if (comment == null) {
          throw new Exception("Failed to find comment with id: " + commentId);
        }
        try {
          deleteActivity(commentId);
          commentId = COMMENT_IDS_DELIMITER + commentId;
          rawCommentIds = rawCommentIds.replace(commentId, "");
          activity.setReplyToId(rawCommentIds);
          Identity user = identityManager.getIdentity(activity.getUserId());
          saveActivity(user, activity);
        } catch (Exception e) {
          throw new Exception("Failed to delete comment with id: " + commentId, e);
        }
      } else {
        throw new Exception("Failed to find commentId: " + commentId + " in activity with activityId: " + activityId);
      }
    } catch (Exception e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_DELETE_COMMENT, e.getMessage(), e);
    }

  }

  /**
   * Load an activity by its id.
   *
   * @param activityId the id of the activity. An UUID.
   * @return the activity
   */
  public ExoSocialActivity getActivity(String activityId) throws ActivityStorageException {
    Session session = sessionManager.getOrOpenSession();
    try {
      Node activityNode = session.getNodeByUUID(activityId);
      if (activityNode != null) {
        return getActivityFromActivityNode(activityNode);
      }
    } catch (Exception e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_GET_ACTIVITY, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
    return null;
  }


  /**
   * Gets the activities by identity.
   *
   * Access a user's activity stream by specifying the offset and limit.
   *
   * @param owner the identity
   * @param offset
   * @param limit
   * @return the activities
   */
  public List<ExoSocialActivity> getActivities(Identity owner, long offset, long limit) throws ActivityStorageException {
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();

    try {
      Node n = getPublishedActivityServiceHome(owner);
      String path = n.getPath();
      Session session = sessionManager.getOrOpenSession();
      List<Node> nodes = new QueryBuilder(session)
        .select(NodeType.NT_ACTIVITY, offset, limit)
        //TODO hard-coded
        .like("jcr:path", path + "[%]/exo:activity[%]")
        .and()
        .not().equal(NodeProperty.ACTIVITY_REPLY_TO_ID, ExoSocialActivity.IS_COMMENT)
        .orderBy(NodeProperty.ACTIVITY_UPDATED, QueryBuilder.DESC).exec();

      for (Node node : nodes) {
        activities.add(getActivityFromActivityNode(node));
      }
    } catch (Exception e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_GET_ACTIVITIES, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }

    return activities;
  }

  /**
   * Gets all the activities by identity.
   *
   * @param owner the identity
   * @return the activities
   */
  public List<ExoSocialActivity> getActivities(Identity owner) throws ActivityStorageException {
    //here is path of activity of john  :/exo:applications/Social_Activity/organization/john/published
    // we will query activities of owner via the way : jcr:path of activity will contains owner's remoteid and providerid(/organization/john/)
    List<ExoSocialActivity> activities = new ArrayList<ExoSocialActivity>();
    try {
      Node publishingNode = getPublishedActivityServiceHome(owner);
      Session session = sessionManager.getOrOpenSession();
      String path = publishingNode.getPath();
      List<Node> nodes = new QueryBuilder(session)
        .select(NodeType.NT_ACTIVITY)
        //TODO hard-coded
        .like("jcr:path", path + "[%]/exo:activity[%]")
        .and()
        .not().equal(NodeProperty.ACTIVITY_REPLY_TO_ID, ExoSocialActivity.IS_COMMENT).exec();

      for (Node node : nodes) {
        activities.add(getActivityFromActivityNode(node));
      }
    } catch (Exception e) {
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_GET_ACTIVITIES, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }
    return activities;
  }

  /**
   * Count the number of activities from an ownerIdentity
   *
   * @param owner
   * @return the number of activities
   */
  public int getActivitiesCount(Identity owner) throws ActivityStorageException {
    int count = 0;

    try {
      Node publishingNode = getPublishedActivityServiceHome(owner);
      Session session = sessionManager.getOrOpenSession();
      String path = publishingNode.getPath();
      count = (int) new QueryBuilder(session)
        .select(NodeType.NT_ACTIVITY)
        //TODO hard-coded
        .like("jcr:path", path + "[%]/exo:activity[%]")
        .and()
        .not().equal(NodeProperty.ACTIVITY_REPLY_TO_ID, ExoSocialActivity.IS_COMMENT).count();
    } catch (Exception e){
      throw new ActivityStorageException(ActivityStorageException.Type.FAILED_TO_GET_ACTIVITIES_COUNT, e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }

    return count;
  }

  public String getPortalOwner() {
    try {
      // This always works well on UI
      //TODO This can have problem with REST API, need to check
      PortalRequestContext context = Util.getPortalRequestContext();
      portalOwner = context.getPortalOwner();
    } catch (Exception e) {
      //Ignore
    }
    return portalOwner;
  }

  /**
   * Sets portal owner for getStream permaLink
   * @param portalOwner
   */
  public void setPortalOwner(String portalOwner) {
    this.portalOwner = portalOwner;
  }

  /**
   * Gets the activity service home node which is cached and lazy-loaded.
   *
   * @param session the session
   * @return the activity service home
   */
  private Node getActivityServiceHome(Session session) throws Exception {
    if (activityServiceHome == null) {
      String path = dataLocation.getSocialActivitiesHome();
      activityServiceHome = session.getRootNode().getNode(path);
    }
    return activityServiceHome;
  }

  /**
   * Gets the user activity service home node.
   *
   * @param owner the owner of the stream
   * @return the user activity service home
   */
  //TODO hoatle bug if id = uuid, username refers to the same identity
  private Node getStreamLocation(Identity owner) throws Exception {
    String type = owner.getProviderId();
    String id = owner.getRemoteId();

    //If then, do not create new stream location, use existing location.
    if(type != null && id != null) {
      return getStreamsLocationByType(type, id);
    } else {
      // default location for stream without a prefix
      LOG.warn("attempting to get a stream for non prefixed owner : " + id);
      //TODO hard-coded
      return getStreamsLocationByType("default", id);
    }
  }

  private Node getStreamsLocationByType(String type, String username) throws Exception {
    Session session = sessionManager.getOrOpenSession();
    try {
      // first get or create the node for type. Ex: /activities/organization
      Node activityHomeNode = getActivityServiceHome(session);
      Node typeHome;
      if (activityHomeNode.hasNode(type)){
        typeHome = activityHomeNode.getNode(type);
      } else {
        typeHome = activityHomeNode.addNode(type, NodeType.NT_UNSTRUCTURED);
        activityHomeNode.save();
      }

      // now get or create the node for the owner. Ex: /activities/organization/root
      if (typeHome.hasNode(username)){
        return typeHome.getNode(username);
      } else {
        Node streamNode = typeHome.addNode(username, NodeType.NT_UNSTRUCTURED);
        typeHome.save();
        return streamNode;
      }
    } catch (Exception e) {
      throw new Exception("Failed to locate stream owner node for " + username +"; " + e.getMessage(), e);
    } finally {
      sessionManager.closeSession(true);
    }
  }


  /**
   * Gets the published activity service home node.
   *
   * @param owner the owner of the stream
   * @return the published activity service home
   */
  private Node getPublishedActivityServiceHome(Identity owner) throws Exception {
    Node userActivityHomeNode = getStreamLocation(owner);
    try {
      return userActivityHomeNode.getNode(PUBLISHED_NODE);
    } catch (PathNotFoundException ex) {
      Node appNode = userActivityHomeNode.addNode(PUBLISHED_NODE, NodeType.NT_UNSTRUCTURED);
      appNode.addMixin(NodeType.MIX_REFERENCEABLE);
      userActivityHomeNode.save();
      return appNode;
    }
  }

  /**
   * Delete an activity's comments
   * All the comment ids are stored in an activity's replytoId
   * 
   * @param activityId
   */
  private void deleteActivityComments(String activityId) throws Exception {
    ExoSocialActivity activity = getActivity(activityId);
    String rawCommentIds = activity.getReplyToId();
    //rawCommentIds can be: null || ,a,b,c,d
    if (rawCommentIds != null) {
      if (rawCommentIds.equals(ExoSocialActivity.IS_COMMENT)) return;

      String[] commentIds = rawCommentIds.split(COMMENT_IDS_DELIMITER);
      //remove the first empty element
      commentIds = (String[]) ArrayUtils.removeElement(commentIds, "");
      for (String commentId : commentIds) {
        deleteActivity(commentId);
      }
    }
  }

  /**
   * set stream owner and id in he activity object.
   * the stream id is the UUID of the parent (ex UUID of 'published').
   * The stream owner is the name of the 2nd ancestor node (parent of published)
   * @param activity
   * @param activityNode
   * @throws Exception
   */
  private void setStreamInfo(ExoSocialActivity activity, Node activityNode) throws Exception {
    ActivityStream activityStream = activity.getActivityStream();
    try {// /activities/space/spaceID/published/activity
      final String providerName = activityNode.getParent().getParent().getParent().getName();
      final String streamNodeId = activityNode.getParent().getUUID(); //published
      final String streamName = activityNode.getParent().getParent().getName();
      activityStream.setId(streamNodeId);
      activityStream.setPrettyId(streamName);
      activityStream.setType(providerName);
      //TODO hard-coded
      activityStream.setTitle("Activity Stream of " + streamName);
      //TODO use absolute url here
      activityStream.setPermaLink(LinkProvider.getActivityUri(providerName, streamName, getPortalOwner()));

    } catch (UnsupportedRepositoryOperationException e) {
      activityNode.getParent().addMixin(NodeType.MIX_REFERENCEABLE);
      //TODO handle this case: activityStream is not fully set here
      activityStream.setId(activityNode.getParent().getUUID());
    }
  }

  private void setActivityNodeFromActivity(Node activityNode, ExoSocialActivity activity) throws RepositoryException {
    activityNode.setProperty(NodeProperty.ACTIVITY_TITLE, activity.getTitle());
    activityNode.setProperty(NodeProperty.ACTIVITY_USER_ID, activity.getUserId());
    //TODO Change "exo:updated" property to time/ string instead of long
    activityNode.setProperty(NodeProperty.ACTIVITY_UPDATED, activity.getUpdated().getTime());
    activityNode.setProperty(NodeProperty.ACTIVITY_POSTED_TIME, activity.getPostedTime());

    if (activity.getLikeIdentityIds() != null) {
      activityNode.setProperty(NodeProperty.ACTIVITY_LIKE_IDENTITY_IDS, activity.getLikeIdentityIds());
    }
    if (activity.isHidden()) {
      activityNode.setProperty(NodeProperty.ACTIVITY_HIDDEN, activity.isHidden());
    }
    //TODO clarify this: bodyTemplate vs bodyId
    activityNode.setProperty(NodeProperty.ACTIVITY_BODY_TEMPLATE, activity.getBodyId());
    activityNode.setProperty(NodeProperty.ACTIVITY_TEMPLATE_PARAMS, mapToArray(activity.getTemplateParams()));

    if (activity.getTitleId() != null) {
      activityNode.setProperty(NodeProperty.ACTIVITY_TITLE_TEMPLATE, activity.getTitleId());
    }

    if(activity.getBody() != null) {
      activityNode.setProperty(NodeProperty.ACTIVITY_BODY, activity.getBody());
    }
    if(activity.getExternalId() != null) {
      activityNode.setProperty(NodeProperty.ACTIVITY_EXTERNAL_ID, activity.getExternalId());
    }

    if(activity.getPriority() != null) {
      activityNode.setProperty(NodeProperty.ACTIVITY_PRIORITY, activity.getPriority());
    }

    if(activity.getType() != null) {
      activityNode.setProperty(NodeProperty.ACTIVITY_TYPE, activity.getType());
    }

    if (activity.getReplyToId() != null) {
      activityNode.setProperty(NodeProperty.ACTIVITY_REPLY_TO_ID, activity.getReplyToId());
    }

    if(activity.getUrl() != null) {
      activityNode.setProperty(NodeProperty.ACTIVITY_URL, activity.getUrl());
    }
  }

  /**
   * Loads an activity object by node from jcr.
   *
   * @param activityNode the node
   * @return the activity
   * @throws Exception the exception
   */
  private ExoSocialActivity getActivityFromActivityNode(Node activityNode) throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setId(activityNode.getUUID());
    setStreamInfo(activity, activityNode);

    if (activityNode.hasProperty(NodeProperty.ACTIVITY_BODY)) {
      activity.setBody(activityNode.getProperty(NodeProperty.ACTIVITY_BODY).getString());
    }
    if (activityNode.hasProperty(NodeProperty.ACTIVITY_EXTERNAL_ID)) {
      activity.setExternalId(activityNode.getProperty(NodeProperty.ACTIVITY_EXTERNAL_ID).getString());
    }
    if (activityNode.hasProperty(NodeProperty.ACTIVITY_HIDDEN)) {
      activity.isHidden(activityNode.getProperty(NodeProperty.ACTIVITY_HIDDEN).getBoolean());
    }
    if (activityNode.hasProperty(NodeProperty.ACTIVITY_POSTED_TIME)) {
      activity.setPostedTime(activityNode.getProperty(NodeProperty.ACTIVITY_POSTED_TIME).getLong());
    }
    if (activityNode.hasProperty(NodeProperty.ACTIVITY_PRIORITY)) {
      activity.setPriority((float)activityNode.getProperty(NodeProperty.ACTIVITY_PRIORITY).getLong());
    }
    if (activityNode.hasProperty(NodeProperty.ACTIVITY_TITLE)) {
      activity.setTitle(activityNode.getProperty(NodeProperty.ACTIVITY_TITLE).getString());
    }
    if (activityNode.hasProperty(NodeProperty.ACTIVITY_TYPE)) {
      activity.setType(activityNode.getProperty(NodeProperty.ACTIVITY_TYPE).getString());
    }
    if (activityNode.hasProperty(NodeProperty.ACTIVITY_REPLY_TO_ID)) {
      activity.setReplyToId(activityNode.getProperty(NodeProperty.ACTIVITY_REPLY_TO_ID).getString());
    }
    if (activityNode.hasProperty(NodeProperty.ACTIVITY_UPDATED)) {
      activity.setUpdated(new Date(activityNode.getProperty(NodeProperty.ACTIVITY_UPDATED).getLong()));
    }
    if (activityNode.hasProperty(NodeProperty.ACTIVITY_URL)) {
      activity.setUrl(activityNode.getProperty(NodeProperty.ACTIVITY_URL).getString());
    }
    //TODO: replace by a reference to the identity node
    if (activityNode.hasProperty(NodeProperty.ACTIVITY_USER_ID)) {
      activity.setUserId(activityNode.getProperty(NodeProperty.ACTIVITY_USER_ID).getString());
    }
    if (activityNode.hasProperty(NodeProperty.ACTIVITY_LIKE_IDENTITY_IDS)) {
      activity.setLikeIdentityIds(ValuesToStrings(activityNode.getProperty(NodeProperty.ACTIVITY_LIKE_IDENTITY_IDS).getValues()));
    }
    if(activityNode.hasProperty(NodeProperty.ACTIVITY_TEMPLATE_PARAMS)) {
      activity.setTemplateParams(valuesToMap(activityNode.getProperty(NodeProperty.ACTIVITY_TEMPLATE_PARAMS).getValues()));
    }
    if(activityNode.hasProperty(NodeProperty.ACTIVITY_TITLE_TEMPLATE)) {
      activity.setTitleId(activityNode.getProperty(NodeProperty.ACTIVITY_TITLE_TEMPLATE).getString());
    }
    if(activityNode.hasProperty(NodeProperty.ACTIVITY_BODY_TEMPLATE)) {
      activity.setBodyId(activityNode.getProperty(NodeProperty.ACTIVITY_BODY_TEMPLATE).getString());
    }
    return activity;
  }

  /**
   * transforms an array {@link Value} into a map of string. The values are expected to be of string type and in the form key=value
   * @param values
   * @return
   */
  private Map<String, String> valuesToMap(Value[] values) {
    if (values == null) {
      return null;
    }
    Map<String, String> result = new HashMap<String, String>();
    for (Value value : values) {
      try {
        String val = value.getString();
        int equalIndex = val.indexOf("=");
        if (equalIndex > 0) {
          result.put(val.split("=")[0], val.substring(equalIndex + 1));
        }
      } catch (Exception e) {
        ;// ignore
      }
    }
    return result;
  }

  /**
   * transforms a map into a string array where values are in the form key=value
   *
   * @param templateParams
   * @return
   */
  private String[] mapToArray(Map<String, String> templateParams) {
    if (templateParams == null) {
      return null;
    }
    Set<String> keys = templateParams.keySet();
    String [] result = new String[keys.size()];
    int i = 0;
    for (String key : keys) {
      result[i++] = key + "=" + templateParams.get(key);
    }
    return result;
  }

  /**
   * Values to strings.
   *
   * @param Val the jcr value
   * @return the string[]
   * @throws Exception the exception
   */
  private String [] ValuesToStrings(Value[] Val) throws Exception {
    if(Val.length == 1) return new String[]{Val[0].getString()};
    String[] Str = new String[Val.length];
    for(int i = 0; i < Val.length; ++i) {
      Str[i] = Val[i].getString();
    }
    return Str;
  }
}