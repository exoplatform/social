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

package org.exoplatform.social.extras.migration.v11x_12x;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.extras.migration.MigrationTool;
import org.exoplatform.social.extras.migration.io.WriterContext;
import org.exoplatform.social.extras.migration.loading.DataLoader;
import org.exoplatform.social.extras.migration.rw.NodeReader;
import org.exoplatform.social.extras.migration.rw.NodeReader_11x_12x;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collection;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class Read11x12xTestCase extends AbstractMigrationTestCase {

  private DataLoader loader;
  private Node rootNode;
  private OrganizationService organizationService;

  @Override
  public void setUp() throws Exception {

    super.setUp();
    loader = new DataLoader("migrationdata-11x.xml", session);
    loader.load();
    rootNode = session.getRootNode();

    PortalContainer container = PortalContainer.getInstance();
    organizationService = (OrganizationService) container.getComponentInstance(OrganizationService.class);

  }

  @Override
  public void tearDown() throws Exception {

    NodeIterator it = rootNode.getNode("exo:applications").getNode("Social_Identity").getNodes();

    while(it.hasNext()) {
      String userName = ((Node) it.next()).getProperty("exo:remoteId").getString();
      organizationService.getUserHandler().removeUser(userName, true);
    }

    Group spaces = organizationService.getGroupHandler().findGroupById("/spaces");
    for (Group group : (Collection<Group>) organizationService.getGroupHandler().findGroups(spaces)) {
      organizationService.getGroupHandler().removeGroup(group, true);
    }

    rootNode.getNode("exo:applications").remove();
    session.save();

    rootNode.getNode("migration_context").remove();
    session.save();

    super.tearDown();

  }

  public void testReadIdentity() throws Exception {

    NodeReader reader = new NodeReader_11x_12x(session);

    //
    PipedOutputStream out = new PipedOutputStream();
    PipedInputStream in = new PipedInputStream(out);

    //
    reader.readIdentities(out, new WriterContext(session, "11x", "12x"));
    DataInputStream dis = new DataInputStream(in);

    checkIdentity(dis, "exo:identity", "user_idA", "organization");
    checkIdentity(dis, "exo:identity[2]", "user_idB", "organization");
    checkIdentity(dis, "exo:identity[3]", "user_idC", "organization");
    checkIdentity(dis, "exo:identity[4]", "user_idD", "organization");
    checkIdentity(dis, "exo:identity[5]", "user_idE", "organization");

    checkIdentity(dis, "user_a", "user_a", "organization");
    checkIdentity(dis, "user_b", "user_b", "organization");
    checkIdentity(dis, "user_c", "user_c", "organization");
    checkIdentity(dis, "user_d", "user_d", "organization");
    checkIdentity(dis, "user_e", "user_e", "organization");

    String uuid = rootNode.getNode("exo:applications/Social_Space/Space/exo:space").getUUID();
    checkIdentity(dis, uuid, uuid, "space");

    uuid = rootNode.getNode("exo:applications/Social_Space/Space/exo:space[2]").getUUID();
    checkIdentity(dis, uuid, uuid, "space");

    uuid = rootNode.getNode("exo:applications/Social_Space/Space/exo:space[3]").getUUID();
    checkIdentity(dis, uuid, uuid, "space");

  }

  public void testReadRelationship() throws Exception {

    NodeReader reader = new NodeReader_11x_12x(session);

    //
    PipedOutputStream out = new PipedOutputStream();
    PipedInputStream in = new PipedInputStream(out);

    //
    reader.readRelationships(out, new WriterContext(session, "11x", "12x"));
    DataInputStream dis = new DataInputStream(in);

    checkRelationship(dis, "exo:relationship", "exo:identity[3]", "exo:identity[2]", "CONFIRM");
    checkRelationship(dis, "exo:relationship[2]", "exo:identity[3]", "exo:identity[4]", "PENDING");
    checkRelationship(dis, "exo:relationship[3]", "user_a", "exo:identity[4]", "CONFIRM");
    checkRelationship(dis, "exo:relationship[4]", "user_d", "exo:identity", "CONFIRM");
    checkRelationship(dis, "exo:relationship[5]", "user_b", "user_a", "PENDING");
    checkRelationship(dis, "exo:relationship[6]", "user_c", "user_d", "CONFIRM");
    checkRelationship(dis, "ec1bbdea2e8902a901cf62bd95f0bdc8", "user_c", "user_a", "CONFIRM");

  }

  public void testReadSpaces() throws Exception {

    NodeReader reader = new NodeReader_11x_12x(session);

    //
    PipedOutputStream out = new PipedOutputStream();
    PipedInputStream in = new PipedInputStream(out);

    //
    reader.readSpaces(out, new WriterContext(session, "11x", "12x"));
    DataInputStream dis = new DataInputStream(in);

    checkSpace(dis, "exo:space", "a", new String[]{"user_a","user_b","user_d"}, null);
    checkSpace(dis, "exo:space[2]", "b", null, null);
    checkSpace(dis, "exo:space[3]", "c", null, new String[]{"user_a","user_d"});
    checkSpace(dis, "exo:space[4]", "d", null, new String[]{"user_a","user_d"});
    checkSpace(dis, "exo:space[5]", "e", new String[]{"user_c"}, new String[]{"user_a","user_d"});

  }

  public void testReadActivity() throws Exception {

    NodeReader reader = new NodeReader_11x_12x(session);

    //
    PipedOutputStream out = new PipedOutputStream();
    PipedInputStream in = new PipedInputStream(out);

    //
    reader.readActivities(out, new WriterContext(session, "11x", "12x"));
    DataInputStream dis = new DataInputStream(in);

    String like1 = rootNode.getNode("exo:applications/Social_Identity/user_a").getUUID();
    String like2 = rootNode.getNode("exo:applications/Social_Identity/user_b").getUUID();
    String replyToId = rootNode.getNode("exo:applications/Social_Activity/organization/user_a/published/ad25a8622e8902a9004557b913a2982e").getUUID();
    String spaceId = rootNode.getNode("exo:applications/Social_Space/Space/exo:space").getUUID();

    checkActivity(dis, "ad25a8622e8902a9004557b913a2982b", "organization", "user_a", "user_a", new String[]{"SENDER=user_b", "RECEIVER=user_a"}, "@user_b has invited @user_a to connect", "CONNECTION_REQUESTED", "exosocial:relationship", "1298642872377", null, null, null, null, null, null, null);
    checkActivity(dis, "ad25a8622e8902a9004557b913a2982c", "organization", "user_a", "user_a", new String[]{"SENDER=user_c", "RECEIVER=user_a"}, "@user_c has invited @user_a to connect", "CONNECTION_REQUESTED", "exosocial:relationship", "1298642872378", null, new String[]{like1, like2}, "body", "template", "url", "1", "external id");
    checkActivity(dis, "ad25a8622e8902a9004557b913a2982e", "organization", "user_a", "user_a", null, "foo", null, "exosocial:relationship", "1298642872380", "IS_COMMENT", null, null, null, null, null, null);
    checkActivity(dis, "ad25a8622e8902a9004557b913a2982d", "organization", "user_a", "user_a", new String[]{"SENDER=user_d", "RECEIVER=user_a"}, "@user_d has invited @user_a to connect", "CONNECTION_REQUESTED", "exosocial:relationship", "1298642872379", replyToId, null, null, null, null, null, null);

    checkActivity(dis, "ad25a8622e8902a9004557b913a2983b", "space", spaceId, "user_a", null, "@user_a has joined.", null, "exosocial:spaces", "1298642872387", null, null, null, null, null, null, null);
    checkActivity(dis, "ad25a8622e8902a9004557b913a2983c", "space", spaceId, "user_b", null, "@user_b has joined.", null, "exosocial:spaces", "1298642872388", null, null, null, null, null, null, null);
    checkActivity(dis, "ad25a8622e8902a9004557b913a2983d", "space", spaceId, "user_c", null, "@user_c has joined.", null, "exosocial:spaces", "1298642872389", null, null, null, null, null, null, null);

  }

  public void testReadProfiles() throws Exception {

    NodeReader reader = new NodeReader_11x_12x(session);

    //
    PipedOutputStream out = new PipedOutputStream();
    PipedInputStream in = new PipedInputStream(out);

    //
    reader.readProfiles(out, new WriterContext(session, "11x", "12x"));
    DataInputStream dis = new DataInputStream(in);

    checkProfile(dis, "exo:profile", "a");
    checkProfile(dis, "exo:profile[2]", "b");

  }

  private void checkIdentity(DataInputStream dis, String nodeName, String remoteId, String providerId) throws IOException, RepositoryException {

    String path;

    assertEquals(MigrationTool.START_NODE, dis.readInt());
    assertEquals("/exo:applications/Social_Identity/" + nodeName, path = dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("jcr:primaryType", dis.readUTF());
    assertEquals("exo:identity", dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_MULTI, dis.readInt());
    assertEquals(1, dis.readInt());
    assertEquals("jcr:mixinTypes", dis.readUTF());
    assertEquals("mix:referenceable", dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("jcr:uuid", dis.readUTF());
    assertEquals(rootNode.getNode(path.substring(1)).getUUID(), dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:providerId", dis.readUTF());
    assertEquals(providerId, dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:remoteId", dis.readUTF());
    assertEquals(remoteId, dis.readUTF());

    assertEquals(MigrationTool.END_NODE, dis.readInt());

  }

  private void checkRelationship(DataInputStream dis, String nodeName, String identitiy1, String identitiy2, String status) throws IOException, RepositoryException {

    assertEquals(MigrationTool.START_NODE, dis.readInt());
    assertEquals("/exo:applications/Social_Relationship/" + nodeName, dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("jcr:primaryType", dis.readUTF());
    assertEquals("exo:relationship", dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:identity1Id", dis.readUTF());
    assertEquals(rootNode.getNode("exo:applications/Social_Identity/" + identitiy1).getUUID(), dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:identity2Id", dis.readUTF());
    assertEquals(rootNode.getNode("exo:applications/Social_Identity/" + identitiy2).getUUID(), dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:status", dis.readUTF());
    assertEquals(status, dis.readUTF());

    assertEquals(MigrationTool.END_NODE, dis.readInt());

  }

  private void checkSpace(DataInputStream dis, String nodeName, String suffix, String[] pendingUsers, String[] invitedUsers) throws IOException, RepositoryException {

    String path;

    assertEquals(MigrationTool.START_NODE, dis.readInt());
    assertEquals("/exo:applications/Social_Space/Space/" + nodeName, path = dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("jcr:primaryType", dis.readUTF());
    assertEquals("exo:space", dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_MULTI, dis.readInt());
    assertEquals(1, dis.readInt());
    assertEquals("jcr:mixinTypes", dis.readUTF());
    assertEquals("mix:referenceable", dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("jcr:uuid", dis.readUTF());
    assertEquals(rootNode.getNode(path.substring(1)).getUUID(), dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:description", dis.readUTF());
    assertEquals("foo " + suffix, dis.readUTF());
    
    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:groupId", dis.readUTF());
    assertEquals("/spaces/name" + suffix, dis.readUTF());

    if (invitedUsers != null) {
      assertEquals(MigrationTool.PROPERTY_MULTI, dis.readInt());
      assertEquals(invitedUsers.length, dis.readInt());
      assertEquals("exo:invitedUsers", dis.readUTF());
      for (String invitedUser : invitedUsers) {
        assertEquals(invitedUser, dis.readUTF());
      }
    }

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:name", dis.readUTF());
    assertEquals("Name " + suffix, dis.readUTF());

    if (pendingUsers != null) {
      assertEquals(MigrationTool.PROPERTY_MULTI, dis.readInt());
      assertEquals(pendingUsers.length, dis.readInt());
      assertEquals("exo:pendingUsers", dis.readUTF());
      for (String pendingUser : pendingUsers) {
        assertEquals(pendingUser, dis.readUTF());
      }
    }

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:priority", dis.readUTF());
    assertEquals("2", dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:registration", dis.readUTF());
    assertEquals("validation", dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:type", dis.readUTF());
    assertEquals("classic", dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:url", dis.readUTF());
    assertEquals("name" + suffix, dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:visibility", dis.readUTF());
    assertEquals("private", dis.readUTF());

    assertEquals(MigrationTool.END_NODE, dis.readInt());

  }

  private void checkActivity(DataInputStream dis, String nodeName, String provider, String owner, String poster, String[] params, String title, String titleTemplate, String type, String timestamp, String replyToId, String[] likes, String body, String bodyId, String url, String priority, String externalId) throws IOException, RepositoryException {

    String path;
    assertEquals(MigrationTool.START_NODE, dis.readInt());
    assertEquals("/exo:applications/Social_Activity/" + provider + "/" + owner + "/published/" + nodeName, path = dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("jcr:primaryType", dis.readUTF());
    assertEquals("exo:activity", dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_MULTI, dis.readInt());
    assertEquals(1, dis.readInt());
    assertEquals("jcr:mixinTypes", dis.readUTF());
    assertEquals("mix:referenceable", dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("jcr:uuid", dis.readUTF());
    assertEquals(rootNode.getNode(path.substring(1)).getUUID(), dis.readUTF());

    if (body != null) {
      assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
      assertEquals("exo:body", dis.readUTF());
      assertEquals("body", dis.readUTF());
    }

    if (bodyId != null) {
      assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
      assertEquals("exo:bodyTemplate", dis.readUTF());
      assertEquals("template", dis.readUTF());
    }

    if (externalId != null) {
      assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
      assertEquals("exo:externalId", dis.readUTF());
      assertEquals(externalId, dis.readUTF());
    }
    
    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:hidden", dis.readUTF());
    assertEquals("false", dis.readUTF());

    if (likes != null) {
      assertEquals(MigrationTool.PROPERTY_MULTI, dis.readInt());
      assertEquals(params.length, dis.readInt());
      assertEquals("exo:likeIdentityIds", dis.readUTF());
      for (String like : likes) {
        assertEquals(like, dis.readUTF());
      }
    }

    if (params != null) {
      assertEquals(MigrationTool.PROPERTY_MULTI, dis.readInt());
      assertEquals(params.length, dis.readInt());
      assertEquals("exo:params", dis.readUTF());
      for (String param : params) {
        assertEquals(param, dis.readUTF());
      }
    }

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:postedTime", dis.readUTF());
    assertEquals(timestamp, dis.readUTF());

    if (priority != null) {
      assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
      assertEquals("exo:priority", dis.readUTF());
      assertEquals(priority, dis.readUTF());
    }

    if (replyToId != null) {
      assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
      assertEquals("exo:replyToId", dis.readUTF());
      assertEquals(replyToId, dis.readUTF());
    }
    
    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:title", dis.readUTF());
    assertEquals(title, dis.readUTF());

    if (titleTemplate != null) {
      assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
      assertEquals("exo:titleTemplate", dis.readUTF());
      assertEquals(titleTemplate, dis.readUTF());
    }

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:type", dis.readUTF());
    assertEquals(type, dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:updatedTimestamp", dis.readUTF());
    assertEquals(timestamp, dis.readUTF());
    
    if (url != null) {
      assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
      assertEquals("exo:url", dis.readUTF());
      assertEquals(url, dis.readUTF());
    }

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:userId", dis.readUTF());
    assertEquals(rootNode.getNode("exo:applications/Social_Identity/" + poster).getUUID(), dis.readUTF());

    assertEquals(MigrationTool.END_NODE, dis.readInt());

  }

  private void checkProfile(DataInputStream dis, String name, String suffix) throws IOException, RepositoryException {

    String path;
    assertEquals(MigrationTool.START_NODE, dis.readInt());
    assertEquals("/exo:applications/Social_Profile/" + name, path = dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("jcr:primaryType", dis.readUTF());
    assertEquals("exo:profile", dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_MULTI, dis.readInt());
    assertEquals(1, dis.readInt());
    assertEquals("jcr:mixinTypes", dis.readUTF());
    assertEquals("mix:referenceable", dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("jcr:uuid", dis.readUTF());
    assertEquals(rootNode.getNode(path.substring(1)).getUUID(), dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("Url", dis.readUTF());
    assertEquals("/portal/private/classic/profile/user_" + suffix, dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("firstName", dis.readUTF());
    assertEquals("User " + suffix, dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("lastName", dis.readUTF());
    assertEquals("Foobar", dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("position", dis.readUTF());
    assertEquals("My position", dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("username", dis.readUTF());
    assertEquals("user_" + suffix, dis.readUTF());

    assertEquals(MigrationTool.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:identity", dis.readUTF());
    assertEquals(rootNode.getNode("exo:applications/Social_Identity/user_" + suffix).getUUID(), dis.readUTF());

    assertEquals(MigrationTool.END_NODE, dis.readInt());

  }

}
