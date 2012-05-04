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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.jcr.JCRSessionManager;
import org.exoplatform.social.common.jcr.LockManager;
import org.exoplatform.social.common.jcr.QueryBuilder;
import org.exoplatform.social.common.jcr.SocialDataLocation;
import org.exoplatform.social.common.jcr.Util;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;

/**
 * The Class JCRStorage represents storage for activity manager.
 * {@link ActivityManager} is the access point for activity service, do not access
 * {@link ActivityStorage} directly though your application.
 *
 * @see org.exoplatform.social.core.manager.ActivityManager
 */
public class ActivityStorage {
  /** The logger */
  private static final Log LOG = ExoLogger.getLogger(ActivityStorage.class);

  /** The Constant PUBLISHED_NODE. */
  private static final String PUBLISHED_NODE = "published".intern();

  /** The Constant NT_UNSTRUCTURED. */
  private static final String NT_UNSTRUCTURED = "nt:unstructured".intern();

  /** The Constant ACTIVITY_NODETYPE. */
  private static final String ACTIVITY_NODETYPE =  "exo:activity".intern();

  /** The Constant BODY. */
  private static final String BODY =  "exo:body".intern();

  /** The Constant BODY. */
  private static final String BODY_TEMPLATE =  "exo:bodyTemplate".intern();

  /** The Constant EXTERNAL_ID. */
  private static final String EXTERNAL_ID =  "exo:externalId".intern();

  /** The Constant ID. */
  private static final String ID =  "exo:id".intern();

  /** The Constant UPDATED. */
  private static final String UPDATED_TIMESTAMP =  "exo:updatedTimestamp".intern();

  /** The Constant POSTED_TIME. */
  private static final String POSTED_TIME =  "exo:postedTime".intern();

  /** The Constant PRIORITY. */
  private static final String PRIORITY =  "exo:priority".intern();

  /** The Constant TITLE. */
  private static final String TITLE =  "exo:title".intern();

  /** The Constant TITLE_TEMPLATE. */
  private static final String TITLE_TEMPLATE =  "exo:titleTemplate".intern();

  /** The Constant URL. */
  private static final String URL =  "exo:url".intern();

  /** The Constant USER_ID. */
  private static final String USER_ID =  "exo:userId".intern();

  /** The Constant TYPE. */
  private static final String TYPE =  "exo:type".intern();

  private static final String REPLY_TO_ID = "exo:replyToId".intern();

  /** The Constant HIDDEN. */
  private static final String HIDDEN =  "exo:hidden".intern();

  /** The Constant LIKE_IDENTITY_IDS. */
  private static final String LIKE_IDENTITY_IDS = "exo:likeIdentityIds".intern();

  /** The Constant LIKE_IDENTITY_IDS. */
  private static final String PARAMS = "exo:params";

  private static final String ACTIVITY_PROPERTIES_NAME_PATTERN;
  
  private static final String SLASH_STR = "/";
  
  static {
    StringBuilder buffer = new StringBuilder(256);
    char separator = '|';
    buffer.append(BODY).append(separator);
    buffer.append(EXTERNAL_ID).append(separator);
    buffer.append(HIDDEN).append(separator);
    buffer.append(POSTED_TIME).append(separator);
    buffer.append(PRIORITY).append(separator);
    buffer.append(TITLE).append(separator);
    buffer.append(TYPE).append(separator);
    buffer.append(REPLY_TO_ID).append(separator);
    buffer.append(UPDATED_TIMESTAMP).append(separator);
    buffer.append(URL).append(separator);
    buffer.append(USER_ID).append(separator);
    buffer.append(LIKE_IDENTITY_IDS).append(separator);
    buffer.append(PARAMS).append(separator);
    buffer.append(TITLE_TEMPLATE).append(separator);
    buffer.append(BODY_TEMPLATE);
    ACTIVITY_PROPERTIES_NAME_PATTERN = buffer.toString();
  }

  /** The data location. */
  private SocialDataLocation dataLocation;

  /** The session manager. */
  private JCRSessionManager sessionManager;

  private IdentityManager identityManager;

  /** The Lock manager. */
  private final LockManager lockManager;

  /**
   * Instantiates a new JCR storage base on SocialDataLocation
   * @param dataLocation the data location.
   * @param identityManager
   * @param lockManager
   */
  public ActivityStorage(SocialDataLocation dataLocation, IdentityManager identityManager, LockManager lockManager) {
    this.lockManager = lockManager;
    this.dataLocation = dataLocation;
    this.identityManager = identityManager;
    sessionManager = dataLocation.getSessionManager();
  }

  /**
   * Saves an activity
   *
   * @param owner
   * @param activity
   * @deprecated use {@link #saveActivity(Identity, Activity)} instead.
   */
  //TODO to be removed for 1.2.x
  public void save(Identity owner, Activity activity) {
    saveActivity(owner, activity);
  }
  /**
   * Saves an activity into a stream
   *
   * Note that the field {@link Activity#setUserId(String)} should be the id of an identity {@link Identity#getId()}
   *
   * @param owner owner of the stream where this activity is bound. Usually a user or space identity
   * @param activity the activity to save
   * @return the activity
   * @since 1.1.1
   */
  //TODO hoatle: we force title is mandatory; the spec says that if title is not
  // available, titleId must be available. We haven't processed titleId yet, so leave title is mandatory
  public Activity saveActivity(Identity owner, Activity activity) {
    Validate.notNull(owner, "owner must not be null.");
    Validate.notNull(activity, "activity must not be null.");
    Validate.notNull(activity.getUpdated(), "Activity.getUpdated() must not be null.");
    Validate.notNull(activity.getPostedTime(), "Activity.getPostedTime() must not be null.");
    if (activity.getUserId() == null) {
      activity.setUserId(owner.getId());
    }
    Node activityHomeNode = getPublishedActivityServiceHome(owner);

    try {
      Node activityNode;

      Session session = sessionManager.getOrOpenSession();
      if (activity.getId() == null) {
        activityNode = activityHomeNode.addNode(IdGenerator.generate(), ACTIVITY_NODETYPE);
        activityNode.addMixin("mix:referenceable");
      } else {
        activityNode = session.getNodeByUUID(activity.getId());
      }

      setStreamInfo(activity, activityNode);

      if (activity.getTitle() != null) {
        activityNode.setProperty(TITLE, activity.getTitle());
      }
      if (activity.getTitleId() != null) {
        activityNode.setProperty(TITLE_TEMPLATE, activity.getTitleId());
      }
      activityNode.setProperty(UPDATED_TIMESTAMP, activity.getUpdatedTimestamp());
      activityNode.setProperty(POSTED_TIME, activity.getPostedTime());

      if(activity.getBody() != null)
        activityNode.setProperty(BODY, activity.getBody());
      if(activity.getExternalId() != null)
        activityNode.setProperty(EXTERNAL_ID, activity.getExternalId());

      if(activity.getPriority() != null)
        activityNode.setProperty(PRIORITY, activity.getPriority());
      if(activity.getTitle() != null)
        activityNode.setProperty(TITLE, activity.getTitle());
      if(activity.getUserId() != null)
        activityNode.setProperty(USER_ID, activity.getUserId());
      if(activity.getType() != null)
        activityNode.setProperty(TYPE, activity.getType());
      if (activity.getReplyToId() != null)
        activityNode.setProperty(REPLY_TO_ID, activity.getReplyToId());
      if(activity.getUrl() != null) {
        activityNode.setProperty(URL, activity.getUrl());
      }
      activityNode.setProperty(LIKE_IDENTITY_IDS, activity.getLikeIdentityIds());
      activityNode.setProperty(HIDDEN, activity.isHidden());
      activityNode.setProperty(TITLE_TEMPLATE, activity.getTitleId());
      activityNode.setProperty(BODY_TEMPLATE, activity.getBodyId());
      activityNode.setProperty(PARAMS, mapToArray(activity.getTemplateParams()));

      if (activity.getId() == null) {
        activityHomeNode.save();
        activity.setId(activityNode.getUUID());
      } else {
        activityNode.save();
      }
    } catch (Exception e) {
      LOG.warn("Failed to save activity", e);
      return null;
    } finally {
      sessionManager.closeSession();
    }
    return activity;
  }

  /**
   * Deletes activity by its id.
   * This will delete comments from this activity first, then delete the activity.
   *
   * @param activityId the activity id
   */
  public void deleteActivity(String activityId) {
    deleteActivityComments(activityId);
    Session session = sessionManager.getOrOpenSession();
    try {
      Node activityNode = session.getNodeByUUID(activityId);
      if (activityNode != null) {
        activityNode.remove();
        session.save();
      } else {
        LOG.warn("Failed to delete activityId: " + activityId + ": not found");
      }
    } catch(Exception ex) {
      LOG.error("Failed to delete activity", ex);
    } finally {
      sessionManager.closeSession();
    }
  }

  /**
   * Deletes a stored activity
   *
   * @param storedActivity
   * @since 1.1.1
   */
  public void deleteActivity(Activity storedActivity) {
    if (storedActivity.getId() == null) {
      LOG.warn("failed to delete this actvitiy. It is not stored in JCR yet.");
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
  public void saveComment(Activity activity, Activity comment) {
    Validate.notNull(activity, "activity must not be null.");
    Validate.notNull(comment.getUserId(), "comment.getUserId() must not be null.");
    Validate.notNull(comment.getTitle(), "comment.getTitle() must not be null.");
    if (comment.getId() != null) { // allows users to edit its comment?
      comment.setUpdatedTimestamp(System.currentTimeMillis());
    } else {
      comment.setPostedTime(System.currentTimeMillis());
      comment.setUpdatedTimestamp(System.currentTimeMillis());
    }
    comment.setReplyToId(Activity.IS_COMMENT);
    Identity ownerStream = identityManager.getIdentity(activity.getUserId());
    comment = saveActivity(ownerStream, comment);
    String rawCommentIds = activity.getReplyToId();
    if (rawCommentIds == null) {
      rawCommentIds = "";
    }
    rawCommentIds += "," + comment.getId();
    activity.setReplyToId(rawCommentIds);
    saveActivity(ownerStream, activity);
  }

  /**
   * Delete comment by its id.
   *
   * @param activityId
   * @param commentId
   */
  public void deleteComment(String activityId, String commentId) {
    Activity activity = getActivity(activityId);
    String rawCommentIds = activity.getReplyToId();
    //rawCommentIds can be: null || ,a,b,c,d
    if (rawCommentIds != null && rawCommentIds.contains(commentId)) {
      Activity comment = getActivity(commentId);
      if (comment == null) {
        LOG.warn("can not find comment with id: " + commentId);
        return;
      }
      try {
        deleteActivity(commentId);
        commentId = "," + commentId;
        rawCommentIds = rawCommentIds.replace(commentId, "");
        activity.setReplyToId(rawCommentIds);
        Identity user = identityManager.getIdentity(activity.getUserId());
        saveActivity(user, activity);
      } catch (Exception e) {
        LOG.warn("failed to delete comment with id: " + commentId);
      }
    } else {
      LOG.warn("can't not find commentId: " + commentId + " in activity with activityId: " + activityId);
    }
  }

  /**
   * Load an activity by its id.
   *
   * @param activityId the id of the activity. An UUID.
   * @return the activity
   */
  public Activity getActivity(String activityId) {
    Session session = sessionManager.getOrOpenSession();
    try {
      Node activityNode = session.getNodeByUUID(activityId);
      if (activityNode != null) {
        return load(activityNode);
      }
    } catch (Exception e) {
      LOG.warn(e.getMessage());
    } finally {
      sessionManager.closeSession();
    }
    return null;
  }

  /**
   * Gets an activity by its id
   *
   * @param activityId
   * @return stored activity
   * @deprecated use {@link #getActivity(String)}
   */
  public Activity load(String activityId) {
    return getActivity(activityId);
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
  public List<Activity> getActivities(Identity owner, long offset, long limit) {
    Node n = getPublishedActivityServiceHome(owner);
    List<Activity> activities = new ArrayList<Activity>();

    try {
      String path = n.getPath();
      Session session = sessionManager.getOrOpenSession();
      List<Node> nodes = new QueryBuilder(session)
              .select(ACTIVITY_NODETYPE, offset, limit)
              .like("jcr:path", path + "[%]/%")
              .and()
              .not().equal(REPLY_TO_ID,Activity.IS_COMMENT)
              .orderBy(POSTED_TIME, QueryBuilder.DESC).exec();

      for (Node node : nodes) {
        activities.add(load(node));
      }
    } catch (RepositoryException e) {
      LOG.warn(e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }

    return activities;
  }

  /**
   * Gets the activities for a list of identities.
   *
   * Access a activity stream of a list of identities by specifying the offset and limit.
   *
   * @param connectionList the list of connections for which we want to get 
   * the latest activities
   * @param offset
   * @param limit
   * @return the activities related to the list of connections
   * @since 1.1.3
   */
  public List<Activity> getActivitiesOfConnections(List<Identity> connectionList, int offset, int limit) {
    List<Activity> activities = new ArrayList<Activity>();

    if (connectionList.isEmpty()) {
      return activities;
    }

    // /exo:applications/Social_Activity/%providerId%/%remoteId%/published
    Node streamLocation = getStreamLocation(connectionList.get(0));
    try {
      //the path needed: /exo:applications/Social_Activity/%providerId%
      String path = streamLocation.getParent().getPath();
      Session session = sessionManager.getOrOpenSession();
      QueryBuilder queryBuilder = new QueryBuilder(session)
              .select(ACTIVITY_NODETYPE, offset, limit)
              .like("jcr:path", path + "/%")
              .and()
              .not().equal(REPLY_TO_ID, Activity.IS_COMMENT)
              .and()
              .group();
      for (int i = 0, length = connectionList.size(); i < length; i++) {
        Identity id = connectionList.get(i);
        if (i != 0) {
          queryBuilder.or();
        }
        queryBuilder.equal(USER_ID, id.getId());
      }
      queryBuilder.endGroup()
              .orderBy(POSTED_TIME, QueryBuilder.DESC);
      List<Node> nodes = queryBuilder.exec();
      for (Node node : nodes) {
        activities.add(load(node));
      }
    } catch (RepositoryException e) {
      LOG.warn(e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }

    return activities;
  }
  /**
   * Gets the activities by identity.
   *
   * @param owner the identity
   * @return the activities
   */
  public List<Activity> getActivities(Identity owner) {
    Node publishingNode = getPublishedActivityServiceHome(owner);
    //here is path of activity of john  :/exo:applications/Social_Activity/organization/john/published
    // we will query activities of owner via the way : jcr:path of activity will contains owner's remoteid and providerid(/organization/john/)
    List<Activity> activities = new ArrayList<Activity>();
    Session session = sessionManager.getOrOpenSession();
    try {
      String path = publishingNode.getPath();
      List<Node> nodes = new QueryBuilder(session)
              .select(ACTIVITY_NODETYPE)
              .like("jcr:path", path + "[%]/%")
              .and()
              .not().equal(REPLY_TO_ID, Activity.IS_COMMENT).exec();

      for (Node node : nodes) {
        activities.add(load(node));
      }
    } catch (RepositoryException e) {
      LOG.warn(e.getMessage(), e);
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
  public int getActivitiesCount(Identity owner) {
    int count = 0;

    Node publishingNode = getPublishedActivityServiceHome(owner);
    Session session = sessionManager.getOrOpenSession();
    try {
      String path = publishingNode.getPath();
      count = (int) new QueryBuilder(session)
              .select(ACTIVITY_NODETYPE)
              .like("jcr:path", path + "[%]/%")
              .and()
              .not().equal(REPLY_TO_ID, Activity.IS_COMMENT).count();
    } catch (Exception e){
      LOG.warn(e.getMessage(), e);
    } finally {
      sessionManager.closeSession();
    }

    return count;
  }

  /**
   * Gets the activity service home node which is cached and lazy-loaded.
   *
   * @param session the session
   * @return the activity service home
   */
  private Node getActivityServiceHome(Session session) {
    String path = dataLocation.getSocialActivitiesHome();
    try {
      Node rootNode = session.getRootNode();
      Util.createNodes(rootNode, path);
      return session.getRootNode().getNode(path);
    } catch (PathNotFoundException e) {
      LOG.warn(e.getMessage(), e);
    } catch (RepositoryException e) {
      LOG.warn(e.getMessage(), e);
    }
    return null;
  }

  /**
   * Gets the user activity service home node.
   *
   * @param owner the owner of the stream
   * @return the user activity service home
   */
  //TODO hoatle bug if id = uuid, username refers to the same identity
  private Node getStreamLocation(Identity owner) {
    String type = owner.getProviderId();
    String id = owner.getRemoteId();

    //If then, do not create new stream location, use existing location.
    if(type != null && id != null) {
      return getStreamsLocationByType(type, id);
    } else {
      // default location for stream without a prefix
      LOG.warn("attempting to get a stream for non prefixed owner : " + id);
      return getStreamsLocationByType("default", id);
    }
  }

  private Node getStreamsLocationByType(String type, String username) {
    Session session = sessionManager.getOrOpenSession();
    try {
      // first get or create the node for type. Ex: /activities/organization
      Node activityHomeNode = getActivityServiceHome(session);
      Node typeHome;
      if (activityHomeNode.hasNode(type)){
        typeHome = activityHomeNode.getNode(type);
      } else {
        Lock lock = lockManager.getLock("Activity", type);
        lock.lock();
        try {
          if (activityHomeNode.hasNode(type)){
            typeHome = activityHomeNode.getNode(type);
          } else {
            typeHome = activityHomeNode.addNode(type, NT_UNSTRUCTURED);
            activityHomeNode.save();
          }
        } finally {
          lock.unlock();
        }
      }

      // now get or create the node for the owner. Ex: /activities/organization/root
      if (typeHome.hasNode(username)){
        return typeHome.getNode(username);
      } else {
        Lock lock = lockManager.getLock("Activity", username);
        lock.lock();
        try {
          if (typeHome.hasNode(username)){
            return typeHome.getNode(username);
          } else {
            Node streamNode = typeHome.addNode(username, NT_UNSTRUCTURED);
            typeHome.save();
            return streamNode;
          }
        } finally {
          lock.unlock();
        }
      }
    } catch (Exception e) {
      LOG.error("failed to locate stream owner node for " +username, e);
      return null;
    } finally {
      sessionManager.closeSession();
    }
  }


  /**
   * Gets the published activity service home node.
   *
   * @param owner the owner of the stream
   * @return the published activity service home
   */
  private Node getPublishedActivityServiceHome(Identity owner) {
    try {
      Node userActivityHomeNode = getStreamLocation(owner);
      try {
        return userActivityHomeNode.getNode(PUBLISHED_NODE);
      } catch (PathNotFoundException ex) {
        Lock lock = lockManager.getLock("Activity", owner.getRemoteId());
        lock.lock();
        try {
          try {
            return userActivityHomeNode.getNode(PUBLISHED_NODE);
          } catch (PathNotFoundException ex2) {

            Node appNode = userActivityHomeNode.addNode(PUBLISHED_NODE, NT_UNSTRUCTURED);
            appNode.addMixin("mix:referenceable");
            userActivityHomeNode.save();
            return appNode;
          }
        } finally {
          lock.unlock();
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to get published activity service location for " + owner, e);
      return null;
    }
  }

  /**
   * Delete an activity's comments
   * All the comment ids are stored in an activity's replytoId
   * @param activityId
   */
  private void deleteActivityComments(String activityId) {
    Activity activity = getActivity(activityId);
    String rawCommentIds = activity.getReplyToId();
    //rawCommentIds can be: null || ,a,b,c,d
    if (rawCommentIds != null) {
      if (rawCommentIds.equals(Activity.IS_COMMENT)) return;

      String[] commentIds = rawCommentIds.split(",");
      //remove the first empty element
      commentIds = (String[]) ArrayUtils.removeElement(commentIds, "");
      for (String commentId : commentIds) {
        try {
          deleteActivity(commentId);
        } catch(Exception ex) {
          LOG.warn("Failed to delete comment actvity " + commentId + ": " + ex.getMessage());
          //TODO hoatle handles or ignores?
        }
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
  private void setStreamInfo(Activity activity, Node activityNode) throws Exception {
    try {// /activities/space/spaceID/published/activity
      activity.setStreamOwner(activityNode.getParent().getParent().getName());
      activity.setStreamId(activityNode.getParent().getUUID());
    } catch (UnsupportedRepositoryOperationException e) {
      activityNode.getParent().addMixin("mix:referenceable");
      activity.setStreamId(activityNode.getParent().getUUID());
    }
  }

  /**
   * Loads an activity object by node from jcr.
   *
   * @param activityNode the node
   * @return the activity
   * @throws Exception the exception
   */
  private Activity load(Node activityNode) {
    Activity activity = new Activity();
    try {
      activity.setId(activityNode.getUUID());
      setStreamInfo(activity, activityNode);

      PropertyIterator it = activityNode.getProperties(ACTIVITY_PROPERTIES_NAME_PATTERN);
      while (it.hasNext()) {
        Property p = it.nextProperty();
        String propertyName = p.getName();
        if (BODY.equals(propertyName)) {
          activity.setBody(p.getString());
        } else if (EXTERNAL_ID.equals(propertyName)) {
          activity.setExternalId(p.getString());
        } else if (HIDDEN.equals(propertyName)) {
          activity.setHidden(p.getBoolean());
        } else if (POSTED_TIME.equals(propertyName)) {
          activity.setPostedTime(p.getLong());
        } else if (PRIORITY.equals(propertyName)) {
          activity.setPriority((int) p.getLong());
        } else if (TITLE.equals(propertyName)) {
          activity.setTitle(p.getString());
        } else if (TYPE.equals(propertyName)) {
          activity.setType(p.getString());
        } else if (REPLY_TO_ID.equals(propertyName)) {
          activity.setReplyToId(p.getString());
        } else if (UPDATED_TIMESTAMP.equals(propertyName)) {
          activity.setUpdatedTimestamp(p.getLong());
        } else if (URL.equals(propertyName)) {
          activity.setUrl(p.getString());
        }
        // TODO: replace by a reference to the identity node
        else if (USER_ID.equals(propertyName)) {
          activity.setUserId(p.getString());
        } else if (LIKE_IDENTITY_IDS.equals(propertyName)) {
          activity.setLikeIdentityIds(convertValuesToStrings(p.getValues()));
        } else if (PARAMS.equals(propertyName)) {
          activity.setTemplateParams(convertValuesToMap(p.getValues()));
        } else if (TITLE_TEMPLATE.equals(propertyName)) {
          activity.setTitleId(p.getString());
        } else if (BODY_TEMPLATE.equals(propertyName)) {
          activity.setBodyId(p.getString());
        }
      }
    } catch (UnsupportedRepositoryOperationException e) {
      LOG.warn(e.getMessage(), e);
    } catch (RepositoryException e) {
      LOG.warn(e.getMessage(), e);
    } catch (Exception e) {
      LOG.warn(e.getMessage(), e);
    }
    return activity;
  }

  /**
   * transforms an array {@link Value} into a map of string. The values are expected to be of string type and in the form key=value
   * @param values
   * @return
   */
  private Map<String, String> convertValuesToMap(Value[] values) {
    if (values == null) {
      return null;
    }
    Map<String, String> result = new LinkedHashMap<String, String>();
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
      result[i++] = key + "=" +templateParams.get(key);
    }
    return result;
  }

  /**
   * Values to strings.
   *
   * @param values the jcr value
   * @return the string[]
   * @throws Exception the exception
   */
  private String [] convertValuesToStrings(Value[] values) throws Exception {
    if(values.length == 1) return new String[]{values[0].getString()};
    String[] Str = new String[values.length];
    for(int i = 0; i < values.length; ++i) {
      Str[i] = values[i].getString();
    }
    return Str;
  }
}