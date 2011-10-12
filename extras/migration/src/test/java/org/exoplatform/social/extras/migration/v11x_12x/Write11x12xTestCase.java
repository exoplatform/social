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
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.impl.ActivityStorageImpl;
import org.exoplatform.social.core.storage.impl.IdentityStorageImpl;
import org.exoplatform.social.core.storage.impl.RelationshipStorageImpl;
import org.exoplatform.social.core.storage.impl.SpaceStorageImpl;
import org.exoplatform.social.extras.migration.io.WriterContext;
import org.exoplatform.social.extras.migration.loading.DataLoader;
import org.exoplatform.social.extras.migration.rw.NodeReader;
import org.exoplatform.social.extras.migration.rw.NodeReader_11x_12x;
import org.exoplatform.social.extras.migration.rw.NodeWriter;
import org.exoplatform.social.extras.migration.rw.NodeWriter_11x_12x;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Collection;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class Write11x12xTestCase extends AbstractMigrationTestCase {

  private IdentityStorage identityStorage;
  private RelationshipStorage relationshipStorage;
  private SpaceStorage spaceStorage;
  private ActivityStorage activityStorage;
  private OrganizationService organizationService;

  private Node rootNode;

  @Override
  public void setUp() throws Exception {

    super.setUp();

    PortalContainer container = PortalContainer.getInstance();

    identityStorage = (IdentityStorage) container.getComponentInstanceOfType(IdentityStorageImpl.class);
    relationshipStorage = (RelationshipStorage) container.getComponentInstanceOfType(RelationshipStorageImpl.class);
    spaceStorage = (SpaceStorage) container.getComponentInstanceOfType(SpaceStorageImpl.class);
    activityStorage = (ActivityStorage) container.getComponentInstanceOfType(ActivityStorageImpl.class);
    organizationService = (OrganizationService) container.getComponentInstance(OrganizationService.class);

    rootNode = session.getRootNode();
    DataLoader loader = new DataLoader("migrationdata-11x.xml", session);
    loader.load();

  }

  @Override
  public void tearDown() throws Exception {

    NodeIterator it = rootNode.getNode("exo:applications/Social_Identity").getNodes();

    while(it.hasNext()) {
      String userName = ((Node) it.next()).getProperty("exo:remoteId").getString();
      organizationService.getUserHandler().removeUser(userName, true);
    }

    Group spaces = organizationService.getGroupHandler().findGroupById("/spaces");
    for (Group group : (Collection<Group>) organizationService.getGroupHandler().findGroups(spaces)) {
      organizationService.getGroupHandler().removeGroup(group, false);
    }

    rootNode.getNode("exo:applications").remove();
    session.save();

    NodeIterator providers = rootNode.getNode("production/soc:providers").getNodes();
    while(providers.hasNext()) {
      providers.nextNode().remove();
    }
    session.save();

    rootNode.getNode("migration_context").remove();
    session.save();

    super.tearDown();

  }

  public void testWriteIdentities() throws Exception {

    PipedOutputStream osIdentity = new PipedOutputStream();
    PipedInputStream isIdentity = new PipedInputStream(osIdentity);

    NodeReader reader = new NodeReader_11x_12x(session);
    NodeWriter writer = new NodeWriter_11x_12x(identityStorage, relationshipStorage, spaceStorage, activityStorage, organizationService, session);
    WriterContext ctx = new WriterContext(session, "11x", "12x");

    reader.readIdentities(osIdentity, ctx);
    writer.writeIdentities(isIdentity, ctx);

    checkIdentity("organization", "user_idA");
    checkIdentity("organization", "user_idB");
    checkIdentity("organization", "user_idC");
    checkIdentity("organization", "user_idD");
    checkIdentity("organization", "user_idE");

    checkIdentity("organization", "user_a");
    checkIdentity("organization", "user_b");
    checkIdentity("organization", "user_c");
    checkIdentity("organization", "user_d");
    checkIdentity("organization", "user_e");

    try {
      rootNode.getNode("production/soc:providers/soc:space");
      fail();
    }
    catch (PathNotFoundException e) {
      // ok
    }

  }

  public void testWriteRelationships() throws Exception {

    PipedOutputStream osIdentity = new PipedOutputStream();
    PipedInputStream isIdentity = new PipedInputStream(osIdentity);

    PipedOutputStream osRelationship = new PipedOutputStream();
    PipedInputStream isRelationship = new PipedInputStream(osRelationship);

    NodeReader reader = new NodeReader_11x_12x(session);
    NodeWriter writer = new NodeWriter_11x_12x(identityStorage, relationshipStorage, spaceStorage, activityStorage, organizationService, session);
    WriterContext ctx = new WriterContext(session, "11x", "12x");

    reader.readIdentities(osIdentity, ctx);
    writer.writeIdentities(isIdentity, ctx);

    reader.readRelationships(osRelationship, ctx);
    writer.writeRelationships(isRelationship, ctx);

    checkRelationship("organization", "user_idC", "user_idB", "relationship");
    checkRelationship("organization", "user_idB", "user_idC", "relationship");

    checkRelationship("organization", "user_idC", "user_idD", "sender");
    checkRelationship("organization", "user_idD", "user_idC", "receiver");

    checkRelationship("organization", "user_a", "user_idD", "relationship");
    checkRelationship("organization", "user_idD", "user_a", "relationship");

    checkRelationship("organization", "user_d", "user_idA", "relationship");
    checkRelationship("organization", "user_idA", "user_d", "relationship");

    checkRelationship("organization", "user_b", "user_a", "sender");
    checkRelationship("organization", "user_a", "user_b", "receiver");

    checkRelationship("organization", "user_c", "user_d", "relationship");
    checkRelationship("organization", "user_d", "user_c", "relationship");

  }

  public void testWriteSpaces() throws Exception {

    PipedOutputStream osIdentity = new PipedOutputStream();
    PipedInputStream isIdentity = new PipedInputStream(osIdentity);

    PipedOutputStream osSpace = new PipedOutputStream();
    PipedInputStream isSpace = new PipedInputStream(osSpace);

    NodeReader reader = new NodeReader_11x_12x(session);
    NodeWriter writer = new NodeWriter_11x_12x(identityStorage, relationshipStorage, spaceStorage, activityStorage, organizationService, session);
    WriterContext ctx = new WriterContext(session, "11x", "12x");

    reader.readIdentities(osIdentity, ctx);
    writer.writeIdentities(isIdentity, ctx);

    reader.readSpaces(osSpace, ctx);
    writer.writeSpaces(isSpace, ctx);

    checkSpace("a", null, null, new String[]{"user_a", "user_b", "user_d"}, null);
    checkSpace("b", new String[]{"user_a", "user_b"}, new String[]{"user_c"}, null, null);
    checkSpace("c", null, null, null, new String[]{"user_a", "user_d"});
    checkSpace("d", null, null, null, new String[]{"user_a", "user_d"});
    checkSpace("e", null, null, new String[]{"user_c"}, new String[]{"user_a", "user_d"});

  }

  public void testWriteActivities() throws Exception {

    PipedOutputStream osIdentity = new PipedOutputStream();
    PipedInputStream isIdentity = new PipedInputStream(osIdentity);

    PipedOutputStream osSpace = new PipedOutputStream();
    PipedInputStream isSpace = new PipedInputStream(osSpace);

    PipedOutputStream osActivity = new PipedOutputStream();
    PipedInputStream isActivity = new PipedInputStream(osActivity);

    NodeReader reader = new NodeReader_11x_12x(session);
    NodeWriter writer = new NodeWriter_11x_12x(identityStorage, relationshipStorage, spaceStorage, activityStorage, organizationService, session);
    WriterContext ctx = new WriterContext(session, "11x", "12x");

    reader.readIdentities(osIdentity, ctx);
    writer.writeIdentities(isIdentity, ctx);

    reader.readSpaces(osSpace, ctx);
    writer.writeSpaces(isSpace, ctx);

    reader.readActivities(osActivity, ctx);
    writer.writeActivities(isActivity, ctx);

    String user_aId = rootNode.getNode("production/soc:providers/soc:organization/soc:user_a").getUUID();
    String user_bId = rootNode.getNode("production/soc:providers/soc:organization/soc:user_b").getUUID();

    checkActivity("organization", "user_a", "1298642872377", null, "@user_b has invited @user_a to connect", "CONNECTION_REQUESTED", null);
    checkActivity("organization", "user_a", "1298642872378", null, "@user_c has invited @user_a to connect", "CONNECTION_REQUESTED", new String[]{user_aId, user_bId});
    checkActivity("organization", "user_a", "1298642872379", null, "@user_d has invited @user_a to connect", "CONNECTION_REQUESTED", null);

    try {
      checkActivity("organization", "user_a", "1298642872380", null, null, null, null);
      fail();
    }
    catch (PathNotFoundException e) {
      // ok
    }

    checkActivity("space", "name_a", "1298642872387", null, "@user_a has joined.", null, null);
    checkActivity("space", "name_a", "1298642872388", null, "@user_b has joined.", null, null);
    checkActivity("space", "name_a", "1298642872389", null, "@user_c has joined.", null, null);

    checkActivity("organization", "user_a", "1298642872379", "1298642872380", "foo", null, null);

  }

  public void testWriteProfile() throws Exception {

    PipedOutputStream osIdentity = new PipedOutputStream();
    PipedInputStream isIdentity = new PipedInputStream(osIdentity);
    
    PipedOutputStream osProfile = new PipedOutputStream();
    PipedInputStream isProfile = new PipedInputStream(osProfile);

    NodeReader reader = new NodeReader_11x_12x(session);
    NodeWriter writer = new NodeWriter_11x_12x(identityStorage, relationshipStorage, spaceStorage, activityStorage, organizationService, session);
    WriterContext ctx = new WriterContext(session, "11x", "12x");

    reader.readIdentities(osIdentity, ctx);
    writer.writeIdentities(isIdentity, ctx);

    reader.readProfiles(osProfile, ctx);
    writer.writeProfiles(isProfile, ctx);

    checkProfile("a");
    checkProfile("b");

  }

  private void checkIdentity(String providerId, String remoteId) throws RepositoryException {

    Node identityNode = rootNode.getNode("production/soc:providers/soc:" + providerId + "/soc:" + remoteId);
    assertEquals(providerId, identityNode.getProperty("soc:providerId").getString());
    assertEquals(remoteId, identityNode.getProperty("soc:remoteId").getString());

  }

  private void checkRelationship(String providerId, String remoteId, String contactId, String type) throws RepositoryException {

    Node relationshipNode = rootNode.getNode("production/soc:providers/soc:" + providerId + "/soc:" + remoteId + "/soc:" + type + "/soc:" + contactId);
    if ("relationship".equals(type)) {
      assertEquals("CONFIRMED", relationshipNode.getProperty("soc:status").getString());
    }
    else {
      assertEquals("PENDING", relationshipNode.getProperty("soc:status").getString());
    }

  }

  private void checkSpace(String suffix, String[] members, String[] managers, String[] pending, String[] invited) throws RepositoryException {

    checkIdentity("space", "name_" + suffix);
    Node spaceNode = rootNode.getNode("production/soc:spaces/soc:name_" + suffix);

    assertEquals("foo " + suffix, spaceNode.getProperty("soc:description").getString());
    assertEquals("foo " + suffix, spaceNode.getProperty("soc:app").getString());
    assertEquals("name_" + suffix, spaceNode.getProperty("soc:name").getString());
    assertEquals("Name " + suffix, spaceNode.getProperty("soc:displayName").getString());
    assertEquals("/spaces/name" + suffix, spaceNode.getProperty("soc:groupId").getString());
    assertEquals("2", spaceNode.getProperty("soc:priority").getString());
    assertEquals("validation", spaceNode.getProperty("soc:registration").getString());
    assertEquals("classic", spaceNode.getProperty("soc:type").getString());
    assertEquals("private", spaceNode.getProperty("soc:visibility").getString());
    assertEquals("validation", spaceNode.getProperty("soc:registration").getString());

    checkMembership(spaceNode, "soc:membersId", members);
    checkMembership(spaceNode, "soc:managerMembersId", managers);
    checkMembership(spaceNode, "soc:pendingMembersId", pending);
    checkMembership(spaceNode, "soc:invitedMembersId", invited);

  }

  private void checkMembership(Node spaceNode, String propertyName, String[] values) throws RepositoryException {

    if (values != null) {
      Value[] propertyValues = spaceNode.getProperty(propertyName).getValues();
      for (int i = 0; i < values.length; ++i) {
        assertEquals(values[i], propertyValues[i].getString());
      }
    }
    else {
      try {
        spaceNode.getProperty(propertyName);
        fail();
      }
      catch (PathNotFoundException e) {
        // ok
      }
    }

  }

  private void checkActivity(String providerId, String remoteId, String activityTimestamp, String commentTimestamp, String title, String titleId, String[] likes) throws RepositoryException {

    String path = "production/soc:providers/soc:" + providerId + "/soc:" + remoteId + "/soc:activities/soc:2011/soc:February/soc:25/soc:" + activityTimestamp;

    if (commentTimestamp != null) {
      path += "/soc:" + commentTimestamp;
    }

    Node activityNode = rootNode.getNode(path);

    if (likes != null) assertEquals(likes.length, activityNode.getProperty("soc:likes").getValues().length);
    assertEquals(title, activityNode.getProperty("soc:title").getString());
    if (titleId != null) assertEquals(titleId, activityNode.getProperty("soc:titleId").getString());

  }

  private void checkProfile(String suffix) throws RepositoryException {

    String parentId = rootNode.getNode("production/soc:providers/soc:organization/soc:user_" + suffix).getUUID();

    Node profileNode = rootNode.getNode("production/soc:providers/soc:organization/soc:user_" + suffix + "/soc:profile");
    assertEquals("User " + suffix, profileNode.getProperty("void-firstName").getValues()[0].getString());
    assertEquals("user_" + suffix + "@mail.com", profileNode.getProperty("void-email").getValues()[0].getString());
    assertEquals("Foobar", profileNode.getProperty("void-lastName").getValues()[0].getString());
    assertEquals("My position", profileNode.getProperty("void-position").getValues()[0].getString());
    assertEquals("user_" + suffix, profileNode.getProperty("void-username").getValues()[0].getString());
    assertEquals(parentId, profileNode.getProperty("soc:parentId").getString());

  }

}
