package org.exoplatform.social.core.activitystream;

import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.NodeIterator;
import java.util.List;

import com.google.common.collect.Lists;


public class JCRStorage {
  final private static String ACTIVITY_APP = "Social_Activity".intern();
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

  private NodeHierarchyCreator nodeHierarchyCreator;


  public JCRStorage(NodeHierarchyCreator nodeHierarchyCreator) {
    this.nodeHierarchyCreator = nodeHierarchyCreator;
  }


  private Node getActivityServiceHome() throws Exception {
    SessionProvider sProvider = SessionProvider.createSystemProvider();

    Node appsNode = nodeHierarchyCreator.getPublicApplicationNode(sProvider);

    try {
      return appsNode.getNode(ACTIVITY_APP);
    } catch (PathNotFoundException ex) {
      Node appNode = appsNode.addNode(ACTIVITY_APP, NT_UNSTRUCTURED);
      appsNode.save();
      return appNode;
    }
  }

  private Node getUserActivityServiceHome(String username) throws Exception {
    Node activityHomeNode = getActivityServiceHome();
    try {
      return activityHomeNode.getNode(username);
    } catch (PathNotFoundException ex) {
      Node appNode = activityHomeNode.addNode(username, NT_UNSTRUCTURED);
      activityHomeNode.save();
      return appNode;
    }
  }

  private Node getPublishedActivityServiceHome(String username) throws Exception {
    Node userActivityHomeNode = getUserActivityServiceHome(username);
    try {
      return userActivityHomeNode.getNode(PUBLISHED_NODE);
    } catch (PathNotFoundException ex) {
      Node appNode = userActivityHomeNode.addNode(PUBLISHED_NODE, NT_UNSTRUCTURED);
      userActivityHomeNode.save();
      return appNode;
    }
  }


  public Activity save(String user, Activity activity) throws Exception {
    Node activityNode;
    Node activityHomeNode = getPublishedActivityServiceHome(user);

    if (activity.getId() == null) {
      activityNode = activityHomeNode.addNode(ACTIVITY_NODETYPE, ACTIVITY_NODETYPE);
      activityNode.addMixin("mix:referenceable");
    } else {
      activityNode = activityHomeNode.getSession().getNodeByUUID(activity.getId());
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
    activityNode.setProperty(HIDDEN, activity.isHidden());

    if (activity.getId() == null) {
      activityHomeNode.save();
      activity.setId(activityNode.getUUID());
    } else {
      activityNode.save();
    }
    return activity;
  }

  public Activity load(String id) throws Exception {
    Node activityHomeNode = getActivityServiceHome();
    Node activityNode = activityHomeNode.getSession().getNodeByUUID(id);
    if (activityNode != null)
      return load(activityNode);
    return null;
  }

  private Activity load(Node n) throws RepositoryException {
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
}
