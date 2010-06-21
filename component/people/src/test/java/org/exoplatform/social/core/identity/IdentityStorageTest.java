package org.exoplatform.social.core.identity;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.social.AbstractPeopleTest;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.jcr.JCRSessionManager;
import org.exoplatform.social.jcr.SocialDataLocation;
import org.exoplatform.social.utils.QueryBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: zun
 * Date: Jun 17, 2010
 * Time: 9:34:56 AM
 * To change this template use File | Settings | File Templates.
 */
public class IdentityStorageTest extends AbstractPeopleTest {
  private IdentityStorage identityStorage;
  private JCRSessionManager sessionManager;
  private static final String IDENTITY_NODETYPE = "exo:identity".intern();
  private static final String PROFILE_NODETYPE = "exo:profile".intern();
  private final String WORKSPACE = "portal-test";

  @Override
  protected void beforeRunBare() throws Exception {
    super.beforeRunBare();
    SocialDataLocation dataLocation = (SocialDataLocation) getContainer().getComponentInstanceOfType(SocialDataLocation.class);
    RepositoryService repositoryService = (RepositoryService) getContainer().getComponentInstanceOfType(RepositoryService.class);
    sessionManager = new JCRSessionManager(WORKSPACE, repositoryService);
    identityStorage = new IdentityStorage(dataLocation);
  }

  @Override
  protected void afterRunBare() {
    super.afterRunBare();
  }

  public void setUp() throws Exception {
    super.setUp();
    begin();
  }

  public void tearDown() throws Exception {
    end();
    Session session = sessionManager.getOrOpenSession();
    try {
      List<Node> profileNodes = new QueryBuilder(session).select(PROFILE_NODETYPE).exec();
      List<Node> idetityNodes = new QueryBuilder(session).select(IDENTITY_NODETYPE).exec();
      for (Node node : profileNodes) {
        node.remove();
      }
      for (Node node : idetityNodes) {
        node.remove();
      }
      session.save();
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testSaveIdentity(){
    String providerId = "organization";
    String remoteId = "zun";
    Identity identity = new Identity(providerId, remoteId);

    identityStorage.saveIdentity(identity);

    assertNotNull(identity);
    assertNotNull(identity.getId());
  }

  public void testFindIdentityById() throws Exception {
    String providerId = "organization";
    String remoteId = "zun";
    Identity identity = new Identity(providerId, remoteId);
    
    identityStorage.saveIdentity(identity);

    Identity identityById = identityStorage.findIdentityById(identity.getId());
    assertNotNull(identityById);
    assertEquals(identity.getId(), identityById.getId());
  }
  
  public void testFindIdentityByProviderIdAndRemoteId() throws Exception {
    String providerId = "organization";
    String remoteId = "zun";
    Identity identity = new Identity(providerId, remoteId);

    identityStorage.saveIdentity(identity);

    Identity identitybyproviderIdandremoteId = identityStorage.findIdentity(providerId, remoteId);
    assertNotNull(identitybyproviderIdandremoteId);
    assertEquals(identity.getId(), identitybyproviderIdandremoteId.getId());
  }

  public void testGetAllIdentities(){
    String providerId = "organization";
    String remoteId = "zun";

    int total = 10;
    for (int i = 0; i < total; i++) {
      Identity identity = new Identity(providerId, remoteId + i);
      identityStorage.saveIdentity(identity);
    }

    List<Identity> identities = identityStorage.getAllIdentities();

    assertEquals(total, identities.size());
  }

  public void testGetIdentityByNode() throws Exception {
    String providerId = "organization";
    String remoteId = "zun";
    Identity identity = new Identity(providerId, remoteId);

    identityStorage.saveIdentity(identity);

    final Session session = sessionManager.openSession();
    try {
      QueryBuilder queryBuilder = new QueryBuilder(session);
      final List<Node> nodes = queryBuilder
        .select(IDENTITY_NODETYPE)
        .equal(IdentityStorage.IDENTITY_PROVIDERID, providerId)
        .and()
        .equal(IdentityStorage.IDENTITY_REMOTEID, remoteId)
        .exec();

      assertEquals(1, nodes.size());

      final Node identityNode = nodes.get(0);
      final Identity identityByNode = identityStorage.getIdentity(identityNode);
      assertNotNull(identityByNode);
      assertEquals(identity.getId(), identityByNode.getId());
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testSaveProfile() throws Exception {
    String providerId = "organization";
    String remoteId = "zun";

    Identity identity = new Identity(providerId, remoteId);
    identityStorage.saveIdentity(identity);
    
    Profile profile = new Profile(identity);
    identityStorage.saveProfile(profile);

    try {
      final Session session = sessionManager.openSession();
      final List<Node> nodes = new QueryBuilder(session)
        .select(IdentityStorage.PROFILE_NODETYPE).exec();

      assertEquals(1, nodes.size());
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testLoadProfileByLazyCreating() throws Exception {
    String providerId = "organization";
    String remoteId = "zun";
    Identity identity = new Identity(providerId, remoteId);

    identityStorage.saveIdentity(identity);

    //create new profile in db without data (lazy creating)
    Profile profile = new Profile(identity);
    identityStorage.loadProfile(profile);

    //query created profile
    try {
      final Session session = sessionManager.openSession();
      final List<Node> nodes = new QueryBuilder(session)
        .select(IdentityStorage.PROFILE_NODETYPE).exec();

      assertEquals(1, nodes.size());

      final Node profileNode = nodes.get(0);
      final Node identityNode = profileNode.getProperty(IdentityStorage.PROFILE_IDENTITY).getNode();
      final Identity identityByProfile = identityStorage.getIdentity(identityNode);

      assertEquals(identity.getId(), identityByProfile.getId());
    } finally {
      sessionManager.closeSession();
    }
  }

  public void testLoadProfileByReloadCreatedProfileNode() throws Exception {
    String providerId = "organization";
    String remoteId = "zun";
    Identity identity = new Identity(providerId, remoteId);

    identityStorage.saveIdentity(identity);
    String profileId;
    //this code snippet will create profile node for test case
    {
      //create new profile in db without data (lazy creating)
      Profile profile = new Profile(identity);
      identityStorage.loadProfile(profile);
      profileId = profile.getId();
    }

    //here is the testcase
    {
      Profile profile = new Profile(identity);
      identityStorage.loadProfile(profile);
      assertEquals(profileId, profile.getId());
    }
  }
  
  public void testFindIdentityByExistName() throws Exception {
    String providerId = "organization";
    String remoteId = "zun";

    Identity identity = new Identity(providerId, remoteId);
    identityStorage.saveIdentity(identity);

    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, "zuanoc");
    identityStorage.saveProfile(profile);

    final ProfileFilter filter = new ProfileFilter();
    filter.setName("zu");
    final List<Identity> result = identityStorage.getIdentitiesFilterByAlphaBet(providerId, filter,0 ,1);
    assertEquals(1, result.size());
  }

  public void testFindManyIdentitiesByExistName() throws Exception {
    String providerId = "organization";

    int total = 10;
    for (int i = 0; i <  total; i++) {
      String remoteId = "zun" + i;
      Identity identity = new Identity(providerId, remoteId+i);
      identityStorage.saveIdentity(identity);

      Profile profile = new Profile(identity);
      profile.setProperty(Profile.FIRST_NAME, "zuanoc"+ i);
      identityStorage.saveProfile(profile);
    }

    final ProfileFilter filter = new ProfileFilter();
    filter.setName("zu");
    final List<Identity> result = identityStorage.getIdentitiesFilterByAlphaBet(providerId, filter, 0, total);
    assertEquals(total, result.size());
  }

  public void testFindIdentityByNotExistName() throws Exception {
    String providerId = "organization";
    String remoteId = "zun";

    Identity identity = new Identity(providerId, remoteId);
    identityStorage.saveIdentity(identity);

    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, "zuanoc");
    identityStorage.saveProfile(profile);

    final ProfileFilter filter = new ProfileFilter();
    filter.setName("kuku");
    final List<Identity> result = identityStorage.getIdentitiesFilterByAlphaBet(providerId, filter, 0, 1);
    assertEquals(0, result.size());
  }

  public void testFindIdentityByProfileFilter() throws Exception {
    String providerId = "organization";
    String remoteId = "zun";

    Identity identity = new Identity(providerId, remoteId);
    identityStorage.saveIdentity(identity);

    Profile profile = new Profile(identity);
    profile.setProperty(Profile.FIRST_NAME, "zuanoc");
    profile.setProperty("postition", "hanoi");
    profile.setProperty("gender", "male");
    
    identityStorage.saveProfile(profile);

    final ProfileFilter filter = new ProfileFilter();
    filter.setPosition("hanoi");
    filter.setGender("male");
    filter.setName("zu");
    final List<Identity> result = identityStorage.getIdentitiesFilterByAlphaBet(providerId, filter, 0, 1);
    assertEquals(1, result.size());
  }

  public void testFindManyIdentitiesByProfileFilter() throws Exception {
    String providerId = "organization";

    int total = 10;
    for (int i = 0; i < total; i++) {
      String remoteId = "zun" + i;
      Identity identity = new Identity(providerId, remoteId);
      identityStorage.saveIdentity(identity);

      Profile profile = new Profile(identity);
      profile.setProperty(Profile.FIRST_NAME, "zuanoc" + i);
      profile.setProperty("postition", "hanoi");
      profile.setProperty("gender", "male");

      identityStorage.saveProfile(profile);
    }

    final ProfileFilter filter = new ProfileFilter();
    filter.setPosition("hanoi");
    filter.setGender("male");
    filter.setName("zu");
    final List<Identity> result = identityStorage.getIdentitiesFilterByAlphaBet(providerId, filter, 0, total);
    assertEquals(total, result.size());
  }
}