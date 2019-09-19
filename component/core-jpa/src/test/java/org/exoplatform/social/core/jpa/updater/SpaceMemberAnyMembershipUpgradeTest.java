package org.exoplatform.social.core.jpa.updater;

import java.util.ArrayList;
import java.util.List;


import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.jpa.test.BaseCoreTest;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.impl.StorageUtils;

public class SpaceMemberAnyMembershipUpgradeTest extends BaseCoreTest {
  private static final Log    LOG = ExoLogger.getLogger(SpaceMemberAnyMembershipUpgradeTest.class);

  private OrganizationService organizationService;

  private SpaceService        spaceService;

  private List<User>          tearDownUserList;

  private List<Space>         tearDownSpaceList;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);
    assertNotNull("spaceService is null");
    organizationService = getContainer().getComponentInstanceOfType(OrganizationService.class);
    assertNotNull("organizationService is null");
    tearDownUserList = new ArrayList<>();
    tearDownSpaceList = new ArrayList<>();
    ExoContainerContext.setCurrentContainer(getContainer());
    RequestLifeCycle.begin(getContainer());
  }

  @Override
  public void tearDown() throws Exception {
    LOG.info("Test cleanup - Delete users");
    for (User user : tearDownUserList) {
      organizationService.getUserHandler().removeUser(user.getUserName(), true);
    }
    for (Space space : tearDownSpaceList) {
      spaceService.deleteSpace(space);
    }
    super.tearDown();
    RequestLifeCycle.end();
  }

  public void testSingleSpaceUpgrade() throws Exception {
    // Create upgrade plugin for ECMS
    InitParams params = new InitParams();
    ValueParam param = new ValueParam();
    param.setName("product.group.id");
    param.setValue("org.exoplatform.social");
    params.addParameter(param);

    param = new ValueParam();
    param.setName("plugin.execution.order");
    param.setValue("1");
    params.addParameter(param);

    param = new ValueParam();
    param.setName(UpgradeProductPlugin.UPGRADE_PLUGIN_EXECUTE_ONCE_PARAMETER);
    param.setValue("true");
    params.addParameter(param);

    SpaceMemberAnyMembershipUpgradePlugin upgradeProductPlugin = new SpaceMemberAnyMembershipUpgradePlugin(getContainer(),
                                                                                                           organizationService,
                                                                                                           spaceService,
                                                                                                           params);
    upgradeProductPlugin.setName("SpaceMemberAnyMembershipUpgradePlugin");

    int randomNumber = (int) (Math.random() * 10000);
    String spaceName = "spaceName" + randomNumber;
    LOG.info("Test preparation - Create space {}", spaceName);
    Space space = createSpace(spaceName);
    tearDownSpaceList.add(space);
    String[] members = space.getMembers();
    int totalCount = 40;
    MembershipType membershipType = organizationService.getMembershipTypeHandler().createMembershipTypeInstance();
    membershipType.setName("*");
    for (int i = 0; i < totalCount; i++) {
      String remoteId = "user" + String.valueOf(randomNumber) + String.valueOf(i);
      User user = organizationService.getUserHandler().createUserInstance(remoteId);
      user.setFirstName("FirstName" + i);
      user.setLastName("LastName" + i);
      user.setEmail(remoteId + String.valueOf(randomNumber) + "@exemple.com");
      user.setPassword("testuser");

      LOG.info("Test preparation - Create user {}", remoteId);
      organizationService.getUserHandler().createUser(user, true);
      tearDownUserList.add(user);

      Group group = organizationService.getGroupHandler().findGroupById(space.getGroupId());

      organizationService.getMembershipHandler().linkMembership(user, group, membershipType, false);
    }
    Identity identity = identityManager.getOrCreateIdentity("organization", "root", false);
    identity.setDeleted(false);
    identity.setEnable(true);
    identityManager.updateIdentity(identity);
    space.setMembers(members);
    space = spaceService.updateSpace(space);
    assertEquals(members.length, space.getMembers().length);

    // Workaround of PLF-7707
    int i = forceCommitTransactions();

    RequestLifeCycle.begin(getContainer());
    LOG.info("Test start");
    upgradeProductPlugin.processUpgrade("1.0", "5.0-GA");
    LOG.info("Test end");
    RequestLifeCycle.end();

    // Reinit Transaction with the same exact
    // previous number of RequestLifeCycle.begin
    for (int j = 0; j < i; j++) {
      RequestLifeCycle.begin(getContainer());
    }

    space = spaceService.getSpaceByGroupId(space.getGroupId());

    assertEquals(totalCount + members.length, space.getMembers().length);
  }

  public void testMultiSpacesUpgrade() throws Exception{
    InitParams params = new InitParams();
    ValueParam param = new ValueParam();
    param.setName("product.group.id");
    param.setValue("org.exoplatform.social");
    params.addParameter(param);

    param = new ValueParam();
    param.setName("plugin.execution.order");
    param.setValue("1");
    params.addParameter(param);

    param = new ValueParam();
    param.setName(UpgradeProductPlugin.UPGRADE_PLUGIN_EXECUTE_ONCE_PARAMETER);
    param.setValue("true");
    params.addParameter(param);

    SpaceMemberAnyMembershipUpgradePlugin upgradeProductPlugin = new SpaceMemberAnyMembershipUpgradePlugin(getContainer(),
                                                                                                           organizationService,
                                                                                                           spaceService,
                                                                                                           params);
    int totalCount = 5;
    upgradeProductPlugin.setName("SpaceMemberAnyMembershipUpgradePlugin");
    int numberOfSpaces = 11;
    for(int counter = 0; counter < numberOfSpaces; counter++) {
      int randomNumber = (int) (Math.random() * 10000);
      String spaceName = "spaceName" + randomNumber;
      LOG.info("Test preparation - Create space {}", spaceName);
      try {
        RequestLifeCycle.begin(getContainer());
        Space space = createSpace(spaceName);
        tearDownSpaceList.add(space);
        String[] members = space.getMembers();

        MembershipType membershipType = organizationService.getMembershipTypeHandler().createMembershipTypeInstance();
        membershipType.setName("*");
        for (int i = 0; i < totalCount; i++) {
          String remoteId = "user" + String.valueOf(randomNumber) + String.valueOf(i);
          User user = organizationService.getUserHandler().createUserInstance(remoteId);
          user.setFirstName("FirstName" + i);
          user.setLastName("LastName" + i);
          user.setEmail(remoteId + String.valueOf(randomNumber) + "@exemple.com");
          user.setPassword("testuser");

          LOG.info("Test preparation - Create user {}", remoteId);
          organizationService.getUserHandler().createUser(user, true);
          tearDownUserList.add(user);

          Group group = organizationService.getGroupHandler().findGroupById(space.getGroupId());

          organizationService.getMembershipHandler().linkMembership(user, group, membershipType, false);
        }
        space.setMembers((String[]) ArrayUtils.addAll(members, space.getMembers()));
        space = spaceService.updateSpace(space);
        // We need to count the root user + new added members
        assertEquals(members.length + 1, space.getMembers().length);

        // Workaround of PLF-7707
        int i = forceCommitTransactions();

        // Reinit Transaction with the same exact
        // previous number of RequestLifeCycle.begin
        for (int j = 0; j < i; j++) {
          RequestLifeCycle.begin(getContainer());
        }
      } finally {
        RequestLifeCycle.end();
      }
    }
    RequestLifeCycle.begin(getContainer());
    LOG.info("Test Multi spaces migration start");
    upgradeProductPlugin.processUpgrade("1.0", "5.0-GA");
    LOG.info("Test Multi spaces migration end");
    RequestLifeCycle.end();
  }

  private int forceCommitTransactions() {
    int i = 0;
    try {
      do {
        RequestLifeCycle.end();
        i++;
      } while (true);
    } catch (Exception e) {
      // Expected
    }
    return i;
  }

  private Space createSpace(String spaceName) throws Exception {
    try {
      Space space = new Space();
      space.setDisplayName(spaceName);
      space.setPrettyName(spaceName);
      space.setGroupId("/spaces/" + space.getPrettyName());
      space.setRegistration(Space.OPEN);
      space.setDescription("description of space" + spaceName);
      space.setType(DefaultSpaceApplicationHandler.NAME);
      space.setVisibility(Space.PRIVATE);
      space.setRegistration(Space.OPEN);
      space.setPriority(Space.INTERMEDIATE_PRIORITY);
      String[] managers = new String[] { "root" };
      String[] members = new String[] {};
      space.setManagers(managers);
      space.setMembers(members);
      space = spaceService.createSpace(space, "root");
      return space;
    } finally {
      StorageUtils.persist();
    }
  }

}
