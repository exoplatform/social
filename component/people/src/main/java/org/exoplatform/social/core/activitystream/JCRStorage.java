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
package org.exoplatform.social.core.activitystream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.space.JCRSessionManager;
import org.exoplatform.social.space.impl.SocialDataLocation;

import com.google.common.collect.Lists;


/**
 * The Class JCRStorage represents storage for activity manager
 * @see org.exoplatform.social.core.activitystream.ActivityManager
 */
public class JCRStorage {
  
  private static final Log LOG = ExoLogger.getLogger(JCRStorage.class);
  
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
   * 
   * @param dataLocation the data location.
   * @see 	org.exoplatform.social.space.impl.SoscialDataLocation.
   */
  public JCRStorage(SocialDataLocation dataLocation) {
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
   * @param username the username
   * @return the user activity service home
   */
  private Node getUserActivityServiceHome(String username) {
    Session session = sessionManager.openSession();
    try {
      Node activityHomeNode = getActivityServiceHome(session);
      if (activityHomeNode.hasNode(username)){
        return activityHomeNode.getNode(username);
      } else {
        Node appNode = activityHomeNode.addNode(username, NT_UNSTRUCTURED);
        activityHomeNode.save();
        return appNode;
      }
    } catch (Exception e) {
      return null;
    } finally {
      sessionManager.closeSession();
    }
    
  }

  /**
   * Gets the published activity service home node.
   * 
   * @param username the username
   * @return the published activity service home
   */
  private Node getPublishedActivityServiceHome(String username) {
    try {
      Node userActivityHomeNode = getUserActivityServiceHome(username);
      try {
        return userActivityHomeNode.getNode(PUBLISHED_NODE);
      } catch (PathNotFoundException ex) {
        Node appNode = userActivityHomeNode.addNode(PUBLISHED_NODE, NT_UNSTRUCTURED);
        userActivityHomeNode.save();
        return appNode;
      }
    } catch (Exception e) {
      LOG.error("Failed to get published activity service location for " + username, e);
      return null;
    }
   
  }

  /**
   * Saves activity base on userId and activity
   * 
   * @param userId the user id
   * @param activity the activity
   * @return the activity
   * @throws Exception the exception
   */
  public Activity save(String userId, Activity activity) throws Exception {
    Node activityNode;
    Node activityHomeNode = getPublishedActivityServiceHome(userId);
    try {
      Session session = sessionManager.openSession();
      if (activity.getId() == null) {
        activityNode = activityHomeNode.addNode(ACTIVITY_NODETYPE, ACTIVITY_NODETYPE);
        activityNode.addMixin("mix:referenceable");
      } else {
        activityNode = session.getNodeByUUID(activity.getId());
      }
      
      if(activity.getBody() != null)
        activityNode.setProperty(BODY, activity.getBody());
      if(activity.getExternalId() != null)
        activityNode.setProperty(EXTERNAL_ID, activity.getExternalId());
      if(activity.getPostedTime() != null)
        activityNode.setProperty(POSTED_TIME, activity.getPostedTime());
      if(activity.getPriority() != null)
        activityNode.setProperty(PRIORITY, activity.getPriority());
      if(activity.getTitle() != null)
        activityNode.setProperty(TITLE, activity.getTitle());
      if(activity.getUpdated() != null)
        activityNode.setProperty(UPDATED_TIMESTAMP, activity.getUpdatedTimestamp());
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
    Node activityNode = null;
    try {
      activityNode = session.getNodeByUUID(activityId);
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
   * load activity by its id
   * 
   * @param id the id
   * @return the activity
   */
  public Activity load(String id) {
    Session session = sessionManager.openSession();
    try {
      Node activityNode = session.getNodeByUUID(id);
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
   * Load activity by node from jcr.
   * 
   * @param n the node
   * @return the activity
   * @throws Exception the exception
   */
  private Activity load(Node n) throws Exception {
    Activity activity = new Activity();
    activity.setId(n.getUUID());

    if (n.hasProperty(BODY))
      activity.setBody(n.getProperty(BODY).getString());
    if (n.hasProperty(EXTERNAL_ID))
      activity.setExternalId(n.getProperty(EXTERNAL_ID).getString());
    if (n.hasProperty(HIDDEN))
      activity.setHidden(n.getProperty(HIDDEN).getBoolean());
    if (n.hasProperty(POSTED_TIME))
      activity.setPostedTime(n.getProperty(POSTED_TIME).getLong());
    if (n.hasProperty(PRIORITY))
      activity.setPriority((int) n.getProperty(PRIORITY).getLong());
    if (n.hasProperty(TITLE))
      activity.setTitle(n.getProperty(TITLE).getString());
    if (n.hasProperty(TYPE))
      activity.setType(n.getProperty(TYPE).getString());
    if (n.hasProperty(REPLY_TO_ID))
      activity.setReplyToId(n.getProperty(REPLY_TO_ID).getString());
    if (n.hasProperty(UPDATED_TIMESTAMP))
      activity.setUpdatedTimestamp(n.getProperty(UPDATED_TIMESTAMP).getLong());
    if (n.hasProperty(URL))
      activity.setUrl(n.getProperty(URL).getString());
    //TODO: replace by a reference to the identity node
    if (n.hasProperty(USER_ID))
      activity.setUserId(n.getProperty(USER_ID).getString());
    if (n.hasProperty(LIKE_IDENTITY_IDS))
      activity.setLikeIdentityIds(ValuesToStrings(n.getProperty(LIKE_IDENTITY_IDS).getValues()));
    if(n.hasProperty(PARAMS)) {
      activity.setTemplateParams(valuesToMap(n.getProperty(PARAMS).getValues()));
    }
    if(n.hasProperty(TITLE_TEMPLATE)) {
      activity.setTitleId(n.getProperty(TITLE_TEMPLATE).getString());
    }
    if(n.hasProperty(BODY_TEMPLATE)) {
      activity.setBodyId(n.getProperty(BODY_TEMPLATE).getString());
    }
    return activity;
  }

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
   * @param identity the identity
   * @return the activities
   * @throws Exception the exception
   */
  public List<Activity> getActivities(String user) throws Exception {
    List<Activity> activities = Lists.newArrayList();
    Node n = getPublishedActivityServiceHome(user);
    NodeIterator nodes = n.getNodes();
    String replyToId;
    while (nodes.hasNext()) {
      Node node = nodes.nextNode();
      if (node.hasProperty(REPLY_TO_ID)) {
        replyToId = node.getProperty(REPLY_TO_ID).getString();
        if (!replyToId.equals(Activity.IS_COMMENT)) {
          activities.add(load(node));
        }
      } else {
        activities.add(load(node));
      }
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
          ex.printStackTrace();
          //TODO hoatle LOG
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
