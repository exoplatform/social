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

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.Value;

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
  
  /** The Constant EXTERNAL_ID. */
  private static final String EXTERNAL_ID =  "exo:externalId".intern();
  
  /** The Constant ID. */
  private static final String ID =  "exo:id".intern();
  
  /** The Constant UPDATED. */
  private static final String UPDATED =  "exo:updated".intern();
  
  /** The Constant POSTED_TIME. */
  private static final String POSTED_TIME =  "exo:postedTime".intern();
  
  /** The Constant PRIORITY. */
  private static final String PRIORITY =  "exo:priority".intern();
  
  /** The Constant TITLE. */
  private static final String TITLE =  "exo:title".intern();
  
  /** The Constant URL. */
  private static final String URL =  "exo:url".intern();
  
  /** The Constant USER_ID. */
  private static final String USER_ID =  "exo:userId".intern();
  
  /** The Constant TYPE. */
  private static final String TYPE =  "exo:type".intern();
  
  /** The Constant HIDDEN. */
  private static final String HIDDEN =  "exo:hidden".intern();
  
  /** The Constant LIKE_IDENTITY_IDS. */
  private static final String LIKE_IDENTITY_IDS = "exo:like".intern();
  
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
   * Save activity base on user id and activity
   * 
   * @param user the user id
   * @param activity the activity
   * @return the activity
   * @throws Exception the exception
   */
  public Activity save(String user, Activity activity) throws Exception {
    Node activityNode;
    Node activityHomeNode = getPublishedActivityServiceHome(user);
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
        activityNode.setProperty(UPDATED, activity.getUpdatedTimestamp());
      if(activity.getUserId() != null)
        activityNode.setProperty(USER_ID, activity.getUserId());
      if(activity.getType() != null)
        activityNode.setProperty(TYPE, activity.getType());
      if(activity.getUrl() != null) {
        activityNode.setProperty(URL, activity.getUrl());  
      }
      //if(activity.getLikeIdentitiesId() != null) {
        activityNode.setProperty(LIKE_IDENTITY_IDS, activity.getLikeIdentityIds());  
      //}
      activityNode.setProperty(HIDDEN, activity.isHidden());
      
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
   * delete activity by its id.
   * 
   * @param activityId the activity id
   * @throws Exception the exception
   */
  public void deleteActivity(String activityId) throws Exception {
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
    if (n.hasProperty(UPDATED))
      activity.setUpdated(n.getProperty(UPDATED).getLong());
    if (n.hasProperty(URL))
      activity.setUrl(n.getProperty(URL).getString());
    //TODO: replace by a reference to the identity node
    if (n.hasProperty(USER_ID))
      activity.setUserId(n.getProperty(USER_ID).getString());
    if (n.hasProperty(LIKE_IDENTITY_IDS))
      activity.setLikeIdentityIds(ValuesToStrings(n.getProperty(LIKE_IDENTITY_IDS).getValues()));
    return activity;
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
    String externalId;
    while (nodes.hasNext()) {
      Node node = nodes.nextNode();
      if (node.hasProperty(EXTERNAL_ID)) {
        externalId = node.getProperty(EXTERNAL_ID).getString();
        if (!externalId.equals(Activity.IS_COMMENT)) {
          activities.add(load(node));
        }
      } else {
        activities.add(load(node));
      }
    }
    return activities;
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
