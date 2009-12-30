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

import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.space.JCRSessionManager;
import org.exoplatform.social.space.impl.SocialDataLocation;

import com.google.common.collect.Lists;


public class JCRStorage {
  final private static String PUBLISHED_NODE = "published".intern();
  final private static String NT_UNSTRUCTURED = "nt:unstructured".intern();
  private static final String ACTIVITY_NODETYPE =  "exo:activity".intern();

  private static final String BODY =  "exo:body".intern();
  private static final String EXTERNAL_ID =  "exo:externalId".intern();
  private static final String ID =  "exo:id".intern();
  private static final String UPDATED =  "exo:updated".intern();
  private static final String POSTED_TIME =  "exo:postedTime".intern();
  private static final String PRIORITY =  "exo:priority".intern();
  private static final String TITLE =  "exo:title".intern();
  private static final String URL =  "exo:url".intern();
  private static final String USER_ID =  "exo:userId".intern();
  private static final String TYPE =  "exo:type".intern();
  private static final String HIDDEN =  "exo:hidden".intern();
  private static final String LIKE_IDENTITIES_ID = "exo:like".intern();
  
  //new change
  private SocialDataLocation dataLocation;
  private JCRSessionManager sessionManager;

  public JCRStorage(SocialDataLocation dataLocation) {
    this.dataLocation = dataLocation;
    this.sessionManager = dataLocation.getSessionManager();
  }

  private Node getActivityServiceHome(Session session) throws Exception {
    String path = dataLocation.getSocialActivitiesHome();
    return session.getRootNode().getNode(path);
  }

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
      e.printStackTrace();
      return null;
    }
   
  }

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
      if(activity.getUpdated() != null)
        activityNode.setProperty(UPDATED, activity.getUpdated());
      if(activity.getPostedTime() != null)
        activityNode.setProperty(POSTED_TIME, activity.getPostedTime());
      if(activity.getPriority() != null)
        activityNode.setProperty(PRIORITY, activity.getPriority());
      if(activity.getTitle() != null)
        activityNode.setProperty(TITLE, activity.getTitle());
      if(activity.getUpdated() != null)
        activityNode.setProperty(UPDATED, activity.getUpdated());
      if(activity.getUserId() != null)
        activityNode.setProperty(USER_ID, activity.getUserId());
      if(activity.getType() != null)
        activityNode.setProperty(TYPE, activity.getType());
      if(activity.getUrl() != null) {
        activityNode.setProperty(URL, activity.getUrl());  
      }
      //if(activity.getLikeIdentitiesId() != null) {
        activityNode.setProperty(LIKE_IDENTITIES_ID, activity.getLikeIdentitiesId());  
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
    } finally {
      sessionManager.closeSession();
    }
    
    return activity;
  }
  
  /**
   * delete activity by its id
   * @param activityId
   * @throws Exception
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
      ex.printStackTrace();
    } finally {
      sessionManager.closeSession();
    }
    
  }

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
    if (n.hasProperty(LIKE_IDENTITIES_ID))
      activity.setLikeIdentitiesId(ValuesToStrings(n.getProperty(LIKE_IDENTITIES_ID).getValues()));
    return activity;
  }

  public List<Activity> getActivities(Identity identity) throws Exception {
    List<Activity> activities = Lists.newArrayList();
    Node n = getPublishedActivityServiceHome(identity.getId());
    NodeIterator nodes = n.getNodes();
    while (nodes.hasNext()) {
      Node node = nodes.nextNode();
      activities.add(load(node));
    }
    return activities;
  }
  
  private String [] ValuesToStrings(Value[] Val) throws Exception {
    if(Val.length == 1) return new String[]{Val[0].getString()};
    String[] Str = new String[Val.length];
    for(int i = 0; i < Val.length; ++i) {
      Str[i] = Val[i].getString();
    }
    return Str;
  }
}
