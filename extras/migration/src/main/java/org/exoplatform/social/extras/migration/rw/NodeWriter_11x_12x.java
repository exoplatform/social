/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.social.extras.migration.rw;

import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.extras.migration.io.NodeData;
import org.exoplatform.social.extras.migration.io.NodeStreamHandler;
import org.exoplatform.social.extras.migration.io.WriterContext;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class NodeWriter_11x_12x implements NodeWriter {

  //
  private final IdentityStorage identityStorage;
  private final RelationshipStorage relationshipStorage;
  private final SpaceStorage spaceStorage;
  private final ActivityStorage activityStorage;
  private final OrganizationService organizationService;

  //
  private final Session session;

  //
  private final String NT_IDENTITY = "exo:identity";
  private final String NT_PROFILE = "exo:profile";
  private final String NT_PROFILE_DETAIL = "exo:profileKeyValue";
  private final String NT_PROFILE_XP = "exo:profileExperience";
  private final String NT_PROFILE_EDU = "exo:profileEducation";
  private final String NT_PROFILE_ADDR = "exo:profileAddress";
  private final String NT_REL_PROP = "exo:relationshipProperty";
  private final String NT_REL = "exo:relationship";
  private final String NT_ACTIVITY = "exo:activity";
  private final String NT_SPACE = "exo:space";

  //
  private final String PATH_EXO_APPLICATION = "exo:applications";
  private final String PATH_SOC_ACTIVITY = "Social_Activity";
  private final String PATH_SOC_RELATIONSHIP = "Social_Relationship";
  private final String PATH_SOC_PROFILE = "Social_Profile";
  private final String PATH_SOC_SPACE = "Social_Space/Space";
  private final String PATH_SOC_IDENTITY = "Social_Identity";

  // Identity
  private final String PROP_PROVIDER_ID = "exo:providerId";
  private final String PROP_REMOTE_ID = "exo:remoteId";

  // Space
  private final String PROP_NAME = "exo:name";
  private final String PROP_APP = "exo:app";
  private final String PROP_DESC = "exo:description";
  private final String PROP_GROUP_ID = "exo:groupId";
  private final String PROP_PRIORITY = "exo:priority";
  private final String PROP_REGISTRATION = "exo:registration";
  private final String PROP_TYPE = "exo:type";
  private final String PROP_EURL = "exo:url";
  private final String PROP_VISIBILITY = "exo:visibility";
  private final String PROP_USER_PENDING = "exo:pendingUsers";
  private final String PROP_USER_INVITED = "exo:invitedUsers";
  private final String PROP_IDENTITY_REF = "exo:identity";

  // Relationship
  private final String PROP_IDENTITY1_REF = "exo:identity1Id";
  private final String PROP_IDENTITY2_REF = "exo:identity2Id";
  private final String PROP_RECIPROCAL_REF = "soc:reciprocal";
  private final String PROP_STATUS = "exo:status";
  private final String REL_STATUS_CONFIRMED = "CONFIRM";
  private final String REL_STATUS_PENDING = "PENDING";

  // Profile
  private final String PROP_KEY = "key";
  private final String PROP_VALUE = "value";
  private final String PROP_IMS = "ims";
  private final String PROP_PHONES = "phones";
  private final String PROP_URLS = "urls";
  private final String PROP_EMAILS = "emails";
  private final String PROP_POSITION = "position";
  private final String PROP_DEPARTMENT = "department";
  private final String PROP_COMPANY = "company";
  private final String PROP_START_DATE = "startDate";
  private final String PROP_END_DATE = "endDate";
  private final String PROP_IS_CURRENT = "isCurrent";
  private final String PROP_FIRST_NAME = "firstName";
  private final String PROP_LAST_NAME = "lastName";
  private final String PROP_USERNAME = "username";
  private final String PROP_GENDER = "gender";
  private final String PROP_URL = "Url";

  // Activity
  private final String PROP_TITLE = "exo:title";
  private final String PROP_TITLE_TEMPLATE = "exo:titleTemplate";
  private final String PROP_BODY = "exo:body";
  private final String PROP_BODY_TEMPLATE = "exo:bodyTemplate";
  private final String PROP_USER_ID = "exo:userId";
  private final String PROP_POSTED_TIME = "exo:postedTime";
  private final String PROP_UPDATED_TIME = "exo:updatedTimestamp";
  private final String PROP_REPLY = "exo:replyToId";
  private final String PROP_EXTERNAL_ID = "exo:externalId";
  private final String PROP_PARAMS = "exo:params";
  private final String PROP_LIKE = "exo:likeIdentityIds";

  //
  private final String PATH_ACTIVITIES = "/exo:applications/Social_Activity";

  // Organization membership
  private final String ORGA_MEMBER = "member";
  private final String ORGA_MANAGER = "manager";

  //
  private final String PROVIDER_SPACE = "space";
  private final String PROVIDER_ORGANIZATION = "organization";

  //
  private final String JCR_PRIMARYTYPE = "jcr:primaryType";
  private final String JCR_UUID = "jcr:uuid";
  private final String JCR_MIME_TYPE = "jcr:mimeType";
  private final String JCR_DATA = "jcr:data";

  //
  private final String CTX_UUID = "id";
  private final String CTX_REMOTE_ID = "remoteId";

  private static final Log LOG = ExoLogger.getLogger(NodeWriter_11x_12x.class);

  public NodeWriter_11x_12x(
      final IdentityStorage identityStorage,
      final RelationshipStorage relationshipStorage,
      final SpaceStorage spaceStorage,
      final ActivityStorage activityStorage,
      final OrganizationService organizationService,
      final Session session) {

    this.identityStorage = identityStorage;
    this.relationshipStorage = relationshipStorage;
    this.spaceStorage = spaceStorage;
    this.activityStorage = activityStorage;
    this.organizationService = organizationService;
    this.session = session;

  }

  /**
   * {@inheritDoc}
   */
  public void writeIdentities(final InputStream is, final WriterContext ctx) {

    //
    NodeStreamHandler handler = new NodeStreamHandler();
    NodeData currentData;
    while ((currentData = handler.readNode(is)) != null) {

      //
      String provider = (String) currentData.get(PROP_PROVIDER_ID);
      String uuid = (String) currentData.get(JCR_UUID);
      String remote = (String) currentData.get(PROP_REMOTE_ID);

      // Add space to context
      if (PROVIDER_SPACE.equals(provider)) {
        ctx.put(uuid + "-" + CTX_REMOTE_ID, remote);
        continue;
      }

      // Handle identities
      Identity identity = new Identity(provider, remote);
      try {
        identityStorage.saveIdentity(identity);
        LOG.info("Write identity " + provider + "/" + remote);
        ctx.incDone(WriterContext.DataType.IDENTITIES);
      }
      catch (Exception e) {
        LOG.error(e.getMessage());
      }

      if (uuid != null) {
        ctx.put(uuid + "-" + CTX_REMOTE_ID, (String) currentData.get(PROP_REMOTE_ID));
        ctx.put(uuid + "-" + CTX_UUID, identity.getId());
      }

    }

    ctx.setCompleted(WriterContext.DataType.IDENTITIES);

  }

  /**
   * {@inheritDoc}
   */
  public void writeSpaces(final InputStream is, final WriterContext ctx) {

    //
    NodeStreamHandler handler = new NodeStreamHandler();
    NodeData currentData;
    while ((currentData = handler.readNode(is)) != null) {

      //
      String name = (String) currentData.get(PROP_NAME);
      String app = (String) currentData.get(PROP_APP);
      String description = (String) currentData.get(PROP_DESC);
      String groupId = (String) currentData.get(PROP_GROUP_ID);
      String priority = (String) currentData.get(PROP_PRIORITY);
      String registration = (String) currentData.get(PROP_REGISTRATION);
      String type = (String) currentData.get(PROP_TYPE);
      String url = (String) currentData.get(PROP_EURL);
      String visibility = (String) currentData.get(PROP_VISIBILITY);

      //
      String[] pendingUsers = (String[]) currentData.get(PROP_USER_PENDING);
      String[] invitedUsers = (String[]) currentData.get(PROP_USER_INVITED);
      String[] members = null;
      String[] managers = null;

      try {

        //
        Group group = organizationService.getGroupHandler().findGroupById(groupId);
        Collection<Membership> memberships = organizationService.getMembershipHandler().findMembershipsByGroup(group);

        //
        List<String> membersList = new ArrayList<String>();
        List<String> managersList = new ArrayList<String>();
        for (Membership membership : memberships) {

          if (ORGA_MEMBER.equals(membership.getMembershipType())) {
            membersList.add(membership.getUserName());
          }
          else if (ORGA_MANAGER.equals(membership.getMembershipType())) {
            managersList.add(membership.getUserName());
          }

        }

        //
        if (membersList.size() > 0) {
          members = membersList.toArray(new String[]{});
        }

        //
        if (managersList.size() > 0) {
          managers = managersList.toArray(new String[]{});
        }

      }
      catch (Exception e) {
        LOG.error(e.getMessage());
      }

      //
      Space space = new Space();
      space.setDisplayName(name);
      space.setApp(app);
      space.setDescription(description);
      space.setGroupId(groupId);
      space.setPriority(priority);
      space.setRegistration(registration);
      space.setType(type);
      space.setUrl(url);
      space.setVisibility(visibility);

      //
      space.setPendingUsers(checkUser(pendingUsers));
      space.setInvitedUsers(checkUser(invitedUsers));
      space.setMembers(members);
      space.setManagers(managers);

      Identity identity = new Identity(PROVIDER_SPACE, space.getPrettyName());

      try {

        identityStorage.saveIdentity(identity);
        LOG.info("Write space identity " + identity.getProviderId() + "/" + identity.getRemoteId());

        spaceStorage.saveSpace(space, true);
        LOG.info("Write space " + space.getGroupId());

        ctx.incDone(WriterContext.DataType.SPACES);

      }
      catch (Exception e) {
        LOG.error(e.getMessage());
      }

      //
      ctx.put(currentData.get(JCR_UUID) + "-" + CTX_REMOTE_ID, space.getPrettyName());
      ctx.put(currentData.get(JCR_UUID) + "-" + CTX_UUID, identity.getId());
      
    }

    ctx.setCompleted(WriterContext.DataType.SPACES);

  }

  /**
   * {@inheritDoc}
   */
  public void writeProfiles(final InputStream is, final WriterContext ctx) {

    Identity currentIdentity = null;

    NodeStreamHandler handler = new NodeStreamHandler();
    NodeData currentData;
    while ((currentData = handler.readNode(is)) != null) {

      if (NT_PROFILE.equals(currentData.get(JCR_PRIMARYTYPE))) {
        currentIdentity = handleProfileBasic(currentData, ctx);
      }

      else if (NT_PROFILE_DETAIL.equals(currentData.get(JCR_PRIMARYTYPE))) {
        handleProfileContact(currentData, currentIdentity);
      }

      else if (NT_PROFILE_XP.equals(currentData.get(JCR_PRIMARYTYPE))) {
        handleProfileXp(currentData, currentIdentity);
      }

    }

    ctx.setCompleted(WriterContext.DataType.PROFILES);

  }

  /**
   * {@inheritDoc}
   */
  public void writeActivities(final InputStream is, final WriterContext ctx) {

    //
    NodeStreamHandler handler = new NodeStreamHandler();
    NodeData currentData;
    while ((currentData = handler.readNode(is)) != null) {
      
      String replyToId = (String) currentData.get(PROP_REPLY);

      // Don't handle directly comments.
      if ("IS_COMMENT".equals(replyToId)) {
        continue;
      }

      String ownerId = extractOwner(currentData);
      Identity owner;

      // Resolve owner
      if (isOrganizationActivity(currentData)) {
        owner = identityStorage.findIdentity(PROVIDER_ORGANIZATION, ownerId);
      }
      else if (isSpaceActivity(currentData)) {
        String spaceName = ctx.get(ownerId + "-" + CTX_REMOTE_ID);
        owner = identityStorage.findIdentity(PROVIDER_SPACE, spaceName);
      }
      else {
        continue;
      }

      //
      ExoSocialActivity activity = new ExoSocialActivityImpl();

      //
      String title = (String) currentData.get(PROP_TITLE);
      String titleTemplate = (String) currentData.get(PROP_TITLE_TEMPLATE);
      String type = (String) currentData.get(PROP_TYPE);
      String userId = (String) currentData.get(PROP_USER_ID);
      String postedTime = (String) currentData.get(PROP_POSTED_TIME);
      String updatedTimestamp = (String) currentData.get(PROP_UPDATED_TIME);
      String body = (String) currentData.get(PROP_BODY);
      String bodyTemplate = (String) currentData.get(PROP_BODY_TEMPLATE);
      String url = (String) currentData.get(PROP_EURL);
      String priority = (String) currentData.get(PROP_PRIORITY);
      String externalId = (String) currentData.get(PROP_EXTERNAL_ID);
      String[] params = (String[]) currentData.get(PROP_PARAMS);
      String[] likes = (String[]) currentData.get(PROP_LIKE);

      //
      Map<String, String> paramMap = readParams(params);
      if (paramMap != null) {
        activity.setTemplateParams(paramMap);
      }

      //
      activity.setTitle(title);
      activity.setTitleId(titleTemplate);
      activity.setBody(body);
      activity.setBodyId(bodyTemplate);
      activity.setUrl(url);
      activity.setExternalId(externalId);
      activity.setType(type);
      activity.setPostedTime(Long.parseLong(postedTime));
      activity.setUpdated(new Date(Long.parseLong(updatedTimestamp)));

      // Get likes
      if (likes != null) {
        String[] newLikes = new String[likes.length];
        for (int i = 0; i < likes.length; ++i) {
          newLikes[i] = ctx.get(likes[i] + "-" + CTX_UUID);
        }
        activity.setLikeIdentityIds(newLikes);
      }

      //
      if (priority != null) {
        activity.setPriority(Float.parseFloat(priority));
      }

      //
      if (userId == null) {
        activity.setUserId(owner.getId());
      }
      else { // Find poster

        //
        String userName = ctx.get(userId + "-" + CTX_REMOTE_ID);
        Identity i = identityStorage.findIdentity(PROVIDER_ORGANIZATION, userName);

        if (i != null) {
          activity.setUserId(i.getId());
        }
        else {
          // Find space poster
          try {
            Node oldSpaceIdentity = session.getNodeByUUID(userId);
            String oldSpaceId = oldSpaceIdentity.getProperty(PROP_REMOTE_ID).getString();
            String spaceName = ctx.get(oldSpaceId + "-" + CTX_REMOTE_ID);
            Identity spaceIdentity = identityStorage.findIdentity(PROVIDER_SPACE, spaceName);
            if (spaceIdentity != null) {
              activity.setUserId(spaceIdentity.getId());
            }
          }
          catch (RepositoryException e1) {
            // No poster found, the poster must be removed.
            LOG.info("Ignore activity : " + activity.getPostedTime());
          }
        }
        
      }

      try {

        activityStorage.saveActivity(owner, activity);
        LOG.info("Write activity " + owner.getRemoteId() + " : " + activity.getPostedTime());

        ctx.incDone(WriterContext.DataType.ACTIVITIES);

      }
      catch (Exception e) {
        LOG.error(e.getMessage());
      }


      // Handle comment
      if (replyToId != null) {

        //
        String[] ids = replyToId.split(",");

        for (String id : ids) {

          //
          if ("".equals(id)) {
            continue;
          }

          try {

            //
            Node node = session.getNodeByUUID(id);
            ExoSocialActivity comment = buildActivityFromNode(node, ctx);

            //
            if (comment.getUserId() == null) {
              comment.setUserId(activity.getUserId());
            }

            try {

              activityStorage.saveComment(activity, comment);
              LOG.info("Write comment " + owner.getRemoteId() + " : " + activity.getPostedTime() + "/" + comment.getPostedTime());

            }
            catch (Exception e) {
              LOG.error(e.getMessage());
            }
          }
          catch (RepositoryException e) {
            LOG.error(e);
          }
        }
      }

    }

    ctx.setCompleted(WriterContext.DataType.ACTIVITIES);

  }

  /**
   * {@inheritDoc}
   */
  public void writeRelationships(final InputStream is, final WriterContext ctx) {

    //
    NodeStreamHandler handler = new NodeStreamHandler();
    NodeData currentData;
    while ((currentData = handler.readNode(is)) != null) {

      //
      String id1 = (String) currentData.get(PROP_IDENTITY1_REF);
      String id2 = (String) currentData.get(PROP_IDENTITY2_REF);
      String status = (String) currentData.get(PROP_STATUS);

      //
      String remoteId1 = ctx.get(id1 + "-" + CTX_REMOTE_ID);
      String remoteId2 = ctx.get(id2 + "-" + CTX_REMOTE_ID);

      //
      Identity i1 = identityStorage.findIdentity(PROVIDER_ORGANIZATION, remoteId1);
      Identity i2 = identityStorage.findIdentity(PROVIDER_ORGANIZATION, remoteId2);

      // Handle relationship type
      Relationship.Type type = null;
      if (REL_STATUS_CONFIRMED.equals(status)) {
        type = Relationship.Type.CONFIRMED;
      }
      else if (REL_STATUS_PENDING.equals(status)) {
        type = Relationship.Type.PENDING;
      }

      //
      Relationship relationship = new Relationship(i1, i2, type);
      
      try {
        relationshipStorage.saveRelationship(relationship);
        LOG.info("Write relationship " + i1.getRemoteId() + " -> " + i2.getRemoteId() + " : " + type);
        ctx.incDone(WriterContext.DataType.RELATIONSHIPS);
      }
      catch (Exception e) {
        LOG.error(e.getMessage());
      }

    }

    ctx.setCompleted(WriterContext.DataType.RELATIONSHIPS);

  }

  /**
   * {@inheritDoc}
   */
  public void rollback(final WriterContext ctx) throws RepositoryException {

    NodeIterator itUserActivity = session.getRootNode().getNode("production/soc:providers/soc:organization").getNodes();
    while (itUserActivity.hasNext()) {
      NodeIterator itActivities = itUserActivity.nextNode().getNode("soc:activities").getNodes();
      while(itActivities.hasNext()) {
        Node activity = itActivities.nextNode();
        LOG.info("Removing activity " + activity.getPath());
        activity.remove();

        session.save();
      }
    }

    NodeIterator itSpaceActivity = session.getRootNode().getNode("production/soc:providers/soc:space").getNodes();
    while (itSpaceActivity.hasNext()) {
      NodeIterator itActivities = itSpaceActivity.nextNode().getNode("soc:activities").getNodes();
      while(itActivities.hasNext()) {
        Node activity = itActivities.nextNode();
        LOG.info("Removing activity " + activity.getPath());
        activity.remove();

        session.save();
      }
    }

    NodeIterator it = session.getRootNode().getNode("production/soc:providers/soc:organization").getNodes();
    while (it.hasNext()) {
      Node current = it.nextNode();

      removeRelationNode(current, "soc:relationship");
      removeRelationNode(current, "soc:sender");
      removeRelationNode(current, "soc:receiver");
      
      LOG.info("Removing relationship for " + current.getPath());

      session.save();
    }

    NodeIterator itOrganization = session.getRootNode().getNode("production/soc:providers/soc:organization/").getNodes();
    while (itOrganization.hasNext()) {
      Node node = itOrganization.nextNode();
      LOG.info("Removing identity " + node.getPath());
      node.remove();

      session.save();
    }

    NodeIterator itSpaceIdentitiy = session.getRootNode().getNode("production/soc:providers/soc:space/").getNodes();
    while (itSpaceIdentitiy.hasNext()) {
      Node node = itSpaceIdentitiy.nextNode();
      LOG.info("Removing space identity " + node.getPath());
      node.remove();

      session.save();
    }

    NodeIterator itSpaces = session.getRootNode().getNode("production/soc:spaces/").getNodes();
    while (itSpaces.hasNext()) {
      Node node = itSpaces.nextNode();
      LOG.info("Removing space " + node.getPath());
      node.remove();

      session.save();

    }

    //
    ctx.cleanup();

  }

  /**
   * {@inheritDoc}
   */
  public void commit(final WriterContext ctx) throws RepositoryException {

    NodeIterator itActivity = session.getRootNode().getNode(PATH_EXO_APPLICATION + "/" + PATH_SOC_ACTIVITY).getNodes();
    while (itActivity.hasNext()) {
      NodeIterator itActivityProvider = itActivity.nextNode().getNodes();
      while (itActivityProvider.hasNext()) {
        NodeIterator itActivityUser = itActivityProvider.nextNode().getNodes();
        while (itActivityUser.hasNext()) {

          //
          Node publishedNode = itActivityUser.nextNode();
          LOG.info("Removing activities " + publishedNode.getPath());
          publishedNode.remove();

          //
          session.save();

        }

      }
    }

    //
    NodeIterator itRelationship = session.getRootNode().getNode(PATH_EXO_APPLICATION + "/" + PATH_SOC_RELATIONSHIP).getNodes();
    while (itRelationship.hasNext()) {

      Node relationshipNode = itRelationship.nextNode();
      LOG.info("Removing relationship " + relationshipNode.getPath());
      relationshipNode.remove();
      session.save();

    }

    //
    NodeIterator itProfile = session.getRootNode().getNode(PATH_EXO_APPLICATION + "/" + PATH_SOC_PROFILE).getNodes();
    while (itProfile.hasNext()) {

      Node profileNode = itProfile.nextNode();
      LOG.info("Removing profile " + profileNode.getPath());
      profileNode.remove();
      session.save();

    }

    //
    NodeIterator itSpace = session.getRootNode().getNode(PATH_EXO_APPLICATION + "/" + PATH_SOC_SPACE).getNodes();
    while (itSpace.hasNext()) {

      Node spaceNode = itSpace.nextNode();
      LOG.info("Removing space " + spaceNode.getPath());
      spaceNode.remove();
      session.save();

    }

    //
    NodeIterator itIdentity = session.getRootNode().getNode(PATH_EXO_APPLICATION + "/" + PATH_SOC_IDENTITY).getNodes();
    while (itIdentity.hasNext()) {

      Node identityNode = itIdentity.nextNode();
      LOG.info("Removing identity " + identityNode.getPath());
      identityNode.remove();
      session.save();

    }

    //
    session.getRootNode().getNode(PATH_EXO_APPLICATION).remove();
    session.save();

    //
    ctx.cleanup();

    //
    ExtendedNodeTypeManager nodeTypeManager = (ExtendedNodeTypeManager) session.getWorkspace().getNodeTypeManager();
    nodeTypeManager.unregisterNodeTypes(
        new String[]{
            NT_IDENTITY,
            NT_PROFILE,
            NT_PROFILE_DETAIL,
            NT_PROFILE_XP,
            NT_PROFILE_EDU,
            NT_PROFILE_ADDR,
            NT_REL_PROP,
            NT_REL,
            NT_ACTIVITY,
            NT_SPACE,
        }
    );
    session.save();

  }

  private void removeRelationNode(Node current, String nodeName) {

    try {
      NodeIterator relationships = current.getNode(nodeName).getNodes();
      while (relationships.hasNext()) {
        removeRelationship(relationships.nextNode());
      }
    }
    catch (RepositoryException e) {
      LOG.error(e);
    }

  }

  private void removeRelationship(Node relationship) {

    try {
      relationship.getProperty(PROP_RECIPROCAL_REF).getNode().remove();
      relationship.remove();
    }
    catch (RepositoryException e) {
      LOG.error(e);
    }

  }

  private boolean isOrganizationActivity(NodeData data) {
    return data.getPath().startsWith(PATH_ACTIVITIES + "/" + PROVIDER_ORGANIZATION);
  }

  private boolean isSpaceActivity(NodeData data) {
    return data.getPath().startsWith(PATH_ACTIVITIES + "/" + PROVIDER_SPACE);
  }

  private String extractOwner(NodeData data) {
    return data.getPath().split("/")[4];
  }

  private Map<String, String> readParams(String[] params) {

    if (params != null) {

      //
      Map<String, String> paramMap = new HashMap<String, String>();
      for(String param : params) {

        String[] keyValue = param.split("=");
        if (keyValue.length < 2) {
          paramMap.put(keyValue[0], null);
        }
        else {
          paramMap.put(keyValue[0], keyValue[1]);
        }

      }

      //
      if (paramMap.size() > 0) {
        return paramMap;
      }

    }

    //
    return null;

  }

  private ExoSocialActivity buildActivityFromNode(Node node, WriterContext ctx) {

    //
    ExoSocialActivity comment = new ExoSocialActivityImpl();

    //
    String title = getPropertyValue(node, PROP_TITLE);
    String titleTemplate = getPropertyValue(node, PROP_TITLE_TEMPLATE);
    String type = getPropertyValue(node, PROP_TYPE);
    String userId = getPropertyValue(node, PROP_USER_ID);
    String postedTime = getPropertyValue(node, PROP_POSTED_TIME);
    String updatedTimestamp = getPropertyValue(node, PROP_UPDATED_TIME);

    //
    comment.setTitle(title);
    comment.setTitleId(titleTemplate);
    comment.setType(type);
    comment.setPostedTime(Long.parseLong(postedTime));
    comment.setUpdated(new Date(Long.parseLong(updatedTimestamp)));

    //
    String userName = ctx.get(userId + "-" + CTX_REMOTE_ID);
    Identity newUser = identityStorage.findIdentity(PROVIDER_ORGANIZATION, userName);

    if (newUser != null) {
      comment.setUserId(newUser.getId());
    }

    return comment;
  }

  private String getPropertyValue(Node node, String propertyName) {

    try {
      return node.getProperty(propertyName).getString();
    }
    catch (RepositoryException e) {
      return null;
    }

  }

  private String[] checkUser(String[] users) {

    //
    if (users == null) {
      return null;
    }

    List<String> checked = new ArrayList<String>();
    for (String user : users) {

        Identity i = identityStorage.findIdentity(PROVIDER_ORGANIZATION, user);
        if (i != null) {
          checked.add(user);
        }

      }

    //
    return checked.toArray(new String[]{});

  }

  private Identity handleProfileBasic(NodeData currentData, WriterContext ctx) {

    //
    Identity currentIdentity = null;
    Node avatarContent = null;

    //
    String url = (String) currentData.get(PROP_URL);
    String firstName = (String) currentData.get(PROP_FIRST_NAME);
    String lastName = (String) currentData.get(PROP_LAST_NAME);
    String position = (String) currentData.get(PROP_POSITION);
    String username = (String) currentData.get(PROP_USERNAME);
    String gender = (String) currentData.get(PROP_GENDER);
    String identityOld = (String) currentData.get(PROP_IDENTITY_REF);

    // Is identity
    String identityId = ctx.get(identityOld + "-" + CTX_UUID);
    if (identityId != null) {
      currentIdentity = identityStorage.findIdentityById(identityId);
      username = currentIdentity.getRemoteId();
      try {
        avatarContent = session.getRootNode().getNode(currentData.getPath().substring(1) + "/avatar/jcr:content");
      }
      catch (RepositoryException e) {
        LOG.error(e.getMessage());
      }
    }

    // Is space
    else {
      String spaceId = ctx.get(identityOld + "-" + CTX_REMOTE_ID);
      if (spaceId != null) {
        try {

          //
          Node node = session.getNodeByUUID(spaceId);
          String groupId = node.getProperty(PROP_GROUP_ID).getString();
          int lastSlash = groupId.lastIndexOf("/");
          String groupName = groupId.substring(lastSlash + 1);
          currentIdentity = identityStorage.findIdentity(PROVIDER_SPACE, groupName);
          avatarContent = node.getNode("image/jcr:content");

          //
          Space space = spaceStorage.getSpaceByPrettyName(groupName);
          if (space != null) {
            space.setAvatarLastUpdated(System.currentTimeMillis());
            spaceStorage.saveSpace(space, false);
          }

        }
        catch (RepositoryException e) {
          LOG.error(e.getMessage());
        }
      }
      else {
        return null;
      }
    }

    //
    Profile profile = new Profile(currentIdentity);
    profile.setProperty(Profile.URL, url);
    profile.setProperty(Profile.FIRST_NAME, firstName);
    profile.setProperty(Profile.LAST_NAME, lastName);
    profile.setProperty(Profile.FULL_NAME, firstName + " " + lastName);
    profile.setProperty(Profile.POSITION, position);
    profile.setProperty(Profile.USERNAME, username);
    profile.setProperty(Profile.GENDER, gender);
    profile.setProperty(Profile.CONTACT_IMS, new ArrayList<Map<String, String>>());
    profile.setProperty(Profile.CONTACT_PHONES, new ArrayList<Map<String, String>>());
    profile.setProperty(Profile.CONTACT_URLS, new ArrayList<Map<String, String>>());

    //
    try {
      String email = organizationService.getUserHandler().findUserByName(username).getEmail();
      profile.setProperty(Profile.EMAIL, email);
    }
    catch (Exception e) {
      LOG.error(e.getMessage());
    }

    //
    try {

      //

      String mime = avatarContent.getProperty(JCR_MIME_TYPE).getString();
      InputStream contentStream = avatarContent.getProperty(JCR_DATA).getStream();

      //
      AvatarAttachment avatarAttachment = new AvatarAttachment();
      avatarAttachment.setMimeType(mime);
      avatarAttachment.setInputStream(contentStream);
      profile.setProperty(Profile.AVATAR, avatarAttachment);

      //
      currentIdentity.setProfile(profile);

    }
    catch (Exception e) {
      LOG.error(e.getMessage());
    }

    try {

      identityStorage.saveProfile(profile);
      LOG.info("Write profile " + currentIdentity.getProviderId() + "/" + currentIdentity.getRemoteId());

      ctx.incDone(WriterContext.DataType.PROFILES);

    }
    catch (Exception e) {
      LOG.error(e.getMessage());
    }

    return currentIdentity;
  }

  private void handleProfileContact(NodeData currentData, Identity identity) {

    //
    if (identity == null) {
      return;
    }

    //
    Profile profile = identity.getProfile();
    profile = identityStorage.loadProfile(profile);

    //
    String path = currentData.getPath();
    String key = (String) currentData.get(PROP_KEY);
    String value = (String) currentData.get(PROP_VALUE);

    //
    String contactType = null;
    if (path.endsWith(PROP_IMS)) {
      contactType = Profile.CONTACT_IMS;
    }
    else if (path.endsWith(PROP_PHONES)) {
      contactType = Profile.CONTACT_PHONES;
    }
    else if (path.endsWith(PROP_URLS)) {
      contactType = Profile.CONTACT_URLS;
    }
    if (contactType == null) {
      return;
    }

    //
    List<Map<String, String>> data = (List<Map<String, String>>) profile.getProperty(contactType);
    if (data == null) {
      data = new ArrayList<Map<String, String>>();
    }

    //
    Map<String, String> info = new HashMap<String, String>();
    info.put(PROP_KEY, key);
    info.put(PROP_VALUE, value);
    data.add(info);

    //
    profile.setProperty(contactType, data);
    identityStorage.saveProfile(profile);

  }

  private void handleProfileXp(NodeData currentData, Identity identity) {

    //
    if (identity == null) {
      return;
    }

    //
    Profile profile = identity.getProfile();
    profile = identityStorage.loadProfile(profile);

    String position = (String) currentData.get(PROP_POSITION);
    String department = (String) currentData.get(PROP_DEPARTMENT);
    String company = (String) currentData.get(PROP_COMPANY);
    String startDate = (String) currentData.get(PROP_START_DATE);
    String endDate = (String) currentData.get(PROP_END_DATE);
    String isCurrent = (String) currentData.get(PROP_IS_CURRENT);
    String description = (String) currentData.get(PROP_DESC);

    //
    List<Map<String, Object>> xps = (List<Map<String, Object>>) profile.getProperty(Profile.EXPERIENCES);
    if (xps == null) {
      xps = new ArrayList<Map<String, Object>>();
    }

    //
    Map<String, Object> xp = new HashMap<String, Object>();
    xp.put(Profile.EXPERIENCES_SKILLS, department);
    xp.put(Profile.EXPERIENCES_POSITION, position);
    xp.put(Profile.EXPERIENCES_COMPANY, company);
    xp.put(Profile.EXPERIENCES_DESCRIPTION, description);
    xp.put(Profile.EXPERIENCES_START_DATE, startDate);
    xp.put(Profile.EXPERIENCES_END_DATE, endDate);
    xp.put(Profile.EXPERIENCES_IS_CURRENT, Boolean.valueOf(isCurrent));

    //
    xps.add(xp);

    //
    profile.setProperty(Profile.EXPERIENCES, xps);
    identityStorage.saveProfile(profile);

  }

}
