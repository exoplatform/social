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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.Value;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.jcr.JCRSessionManager;
import org.exoplatform.social.common.jcr.QueryBuilder;
import org.exoplatform.social.common.jcr.SocialDataLocation;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;

import com.google.common.collect.Lists;

/**
 * The Class JCRStorage represents storage for activity manager
 * @see org.exoplatform.social.core.activitystream.ActivityManager
 */
public class ActivityStorage {

  private static final Log LOG = ExoLogger.getLogger(ActivityStorage.class);

  /** The Constant PUBLISHED_NODE. */
  final private static String PUBLISHED_NODE = "published".intern();

  /** The Constant NT_UNSTRUCTURED. */
  final private static String NT_UNSTRUCTURED = "nt:unstructured".intern();

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

  //new change
  /** The data location. */
  private SocialDataLocation dataLocation;

  /** The session manager. */
  private JCRSessionManager sessionManager;

  /**
   * Instantiates a new JCR storage base on SocialDataLocation
   * @param dataLocation the data location.
   */
  public ActivityStorage(SocialDataLocation dataLocation) {
    this.dataLocation = dataLocation;
    this.sessionManager = dataLocation.getSessionManager();
  }

  /**
   * Gets the activity service home node.
   *
   * @param session the session
   * @return the activity service home
   * @throws Exception the exception
   */
  private Node getActivityServiceHome(Session session) throws Exception {
    String path = dataLocation.getSocialActivitiesHome();
    return session.getRootNode().getNode(path);
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
    }
    else {
      // default location for stream without a prefix
      LOG.warn("attempting to get a stream for non prefixed owner : " + id);
      return getStreamsLocationByType("default", id);
    }
  }

  private Node getStreamsLocationByType(String type, String username) {
    Session session = sessionManager.openSession();
    try {
      // first get or create the node for type. Ex: /activities/organization
      Node activityHomeNode = getActivityServiceHome(session);
      Node typeHome;
      if (activityHomeNode.hasNode(type)){
        typeHome = activityHomeNode.getNode(type);
      } else {
        typeHome = activityHomeNode.addNode(type, NT_UNSTRUCTURED);
        activityHomeNode.save();
      }

      // now get or create the node for the owner. Ex: /activities/organization/root
      if (typeHome.hasNode(username)){
        return typeHome.getNode(username);
      } else {
        Node streamNode = typeHome.addNode(username, NT_UNSTRUCTURED);
        typeHome.save();
        return streamNode;
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
        Node appNode = userActivityHomeNode.addNode(PUBLISHED_NODE, NT_UNSTRUCTURED);
        appNode.addMixin("mix:referenceable");
        userActivityHomeNode.save();
        return appNode;
      }
    } catch (Exception e) {
      LOG.error("Failed to get published activity service location for " + owner, e);
      return null;
    }
  }

  /**
   * Saves an activity into a stream
   *
   * @param owner owner of the stream where this activity is bound. Ususally a user or space identity
   * @param activity the activity to save
   * @return the activity
   * @throws Exception the exception
   */
  //TODO hoatle: we force title is mandatory; the spec says that if title is not
  // available, titleId must be available. We haven't processed titleId yet, so leave title is mandatory
  public Activity save(Identity owner, Activity activity) throws Exception {

    LOG.info("storing activity for owner " + owner + " by " + activity.getUserId());
    Validate.notNull(activity.getUpdated(), "Activity.getUpdated() must not be null.");
    Validate.notNull(activity.getPostedTime(), "Activity.getPostedTime() must not be null.");
    Validate.notNull(activity.getTitle(), "Activity.getTitle() must not be null.");

    Node activityHomeNode = getPublishedActivityServiceHome(owner);

    try {
      Node activityNode;

      Session session = sessionManager.openSession();
      if (activity.getId() == null) {
        activityNode = activityHomeNode.addNode(ACTIVITY_NODETYPE, ACTIVITY_NODETYPE);
        activityNode.addMixin("mix:referenceable");
      } else {
        activityNode = session.getNodeByUUID(activity.getId());
      }

      setStreamInfo(activity, activityNode);

      activityNode.setProperty(TITLE, activity.getTitle());
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
      // TODO: handle exception
      LOG.error("Failed to save activity", e);
    } finally {
      sessionManager.closeSession();
    }
    return activity;
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
   * transforms a map into a string array where values are in the form key=value
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
   * Deletes activity by its id.
   *
   * @param activityId the activity id
   * @throws Exception the exception
   */
  public void deleteActivity(String activityId) throws Exception {
    deleteActivityComments(activityId);

    Session session = sessionManager.openSession();
    try {
      Node activityNode = session.getNodeByUUID(activityId);
      if (activityNode != null) {
        activityNode.remove();
        session.save();
      }
    } catch(Exception ex) {
      LOG.error("Failed to delete activity", ex);
    } finally {
      sessionManager.closeSession();
    }
  }

  /**
   * Load activity by its id.
   *
   * @param activityId the id of the activity. An UUID.
   * @return the activity
   */
  public Activity load(String activityId) {
    Session session = sessionManager.openSession();
    try {
      Node activityNode = session.getNodeByUUID(activityId);
      if (activityNode != null)
        return load(activityNode);
    } catch (Exception e) {
      return null;
    } finally {
      sessionManager.closeSession();
    }
    return null;
  }

  /**
   * Loads an activity object by node from jcr.
   *
   * @param activityNode the node
   * @return the activity
   * @throws Exception the exception
   */
  private Activity load(Node activityNode) throws Exception {
    Activity activity = new Activity();
    activity.setId(activityNode.getUUID());

    activity.setStreamId(activityNode.getParent().getUUID());
    activity.setStreamOwner(activityNode.getParent().getParent().getName());

    if (activityNode.hasProperty(BODY))
      activity.setBody(activityNode.getProperty(BODY).getString());
    if (activityNode.hasProperty(EXTERNAL_ID))
      activity.setExternalId(activityNode.getProperty(EXTERNAL_ID).getString());
    if (activityNode.hasProperty(HIDDEN))
      activity.setHidden(activityNode.getProperty(HIDDEN).getBoolean());
    if (activityNode.hasProperty(POSTED_TIME))
      activity.setPostedTime(activityNode.getProperty(POSTED_TIME).getLong());
    if (activityNode.hasProperty(PRIORITY))
      activity.setPriority((int) activityNode.getProperty(PRIORITY).getLong());
    if (activityNode.hasProperty(TITLE))
      activity.setTitle(activityNode.getProperty(TITLE).getString());
    if (activityNode.hasProperty(TYPE))
      activity.setType(activityNode.getProperty(TYPE).getString());
    if (activityNode.hasProperty(REPLY_TO_ID))
      activity.setReplyToId(activityNode.getProperty(REPLY_TO_ID).getString());
    if (activityNode.hasProperty(UPDATED_TIMESTAMP))
      activity.setUpdatedTimestamp(activityNode.getProperty(UPDATED_TIMESTAMP).getLong());
    if (activityNode.hasProperty(URL))
      activity.setUrl(activityNode.getProperty(URL).getString());
    //TODO: replace by a reference to the identity node
    if (activityNode.hasProperty(USER_ID))
      activity.setUserId(activityNode.getProperty(USER_ID).getString());
    if (activityNode.hasProperty(LIKE_IDENTITY_IDS))
      activity.setLikeIdentityIds(ValuesToStrings(activityNode.getProperty(LIKE_IDENTITY_IDS).getValues()));
    if(activityNode.hasProperty(PARAMS)) {
      activity.setTemplateParams(valuesToMap(activityNode.getProperty(PARAMS).getValues()));
    }
    if(activityNode.hasProperty(TITLE_TEMPLATE)) {
      activity.setTitleId(activityNode.getProperty(TITLE_TEMPLATE).getString());
    }
    if(activityNode.hasProperty(BODY_TEMPLATE)) {
      activity.setBodyId(activityNode.getProperty(BODY_TEMPLATE).getString());
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
        if (val.indexOf("=") > 0) {
          result.put(val.split("=")[0], val.split("=")[1]);
        }
      } catch (Exception e) {
        ;// ignore
      }
    }
    return result;
  }

  /**
   * Gets the activities by identity.
   *
   * @param owner the identity
   * @return the activities
   * @throws Exception the exception
   */
  public List<Activity> getActivities(Identity owner, long offset, long limit) throws Exception {
    Node n = getPublishedActivityServiceHome(owner);
    String path = n.getPath();

    List<Activity> activities = Lists.newArrayList();
    try {
      Session session = sessionManager.getOrOpenSession();
      List<Node> nodes = new QueryBuilder(session)
        .select(ACTIVITY_NODETYPE, offset, limit)
        .like("jcr:path", path + "[%]/exo:activity[%]")
        .and()
        .not().equal(REPLY_TO_ID,Activity.IS_COMMENT)
        .orderBy("exo:updatedTimestamp", QueryBuilder.DESC).exec();

      for (Node node : nodes) {
        activities.add(load(node));
      }
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
   * @throws Exception the exception
   */
  public List<Activity> getActivities(Identity owner) throws Exception {
    Node publishingNode = getPublishedActivityServiceHome(owner);
    //here is path of activity of john  :/exo:applications/Social_Activity/organization/john/published
    // we will query activities of owner via the way : jcr:path of activity will contains owner's remoteid and providerid(/organization/john/)
    List<Activity> activities = Lists.newArrayList();
    Session session = sessionManager.getOrOpenSession();
    try {
      String path = publishingNode.getPath();
      List<Node> nodes = new QueryBuilder(session)
        .select(ACTIVITY_NODETYPE)
        .like("jcr:path", path + "[%]/exo:activity[%]")
        .and()
        .not().equal(REPLY_TO_ID, Activity.IS_COMMENT).exec();

      for (Node node : nodes) {
        activities.add(load(node));
      }
    } finally {
      sessionManager.closeSession();
    }

    return activities;
  }


  /**
   * Delete an activity's comments
   * All the comment ids are stored in an activity's replytoId
   * @param activityId
   */
  private void deleteActivityComments(String activityId) {
    Activity activity = load(activityId);
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