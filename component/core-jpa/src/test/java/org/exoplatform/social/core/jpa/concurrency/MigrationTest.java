/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.social.core.jpa.concurrency;

import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.jpa.rest.IdentityAvatarRestService;
import org.exoplatform.social.core.jpa.storage.RDBMSActivityStorageImpl;
import org.exoplatform.social.core.jpa.storage.RDBMSIdentityStorageImpl;
import org.exoplatform.social.core.jpa.storage.RDBMSSpaceStorageImpl;
import org.exoplatform.social.core.jpa.test.BaseCoreTest;
import org.exoplatform.social.core.jpa.test.QueryNumberTest;
import org.exoplatform.social.core.jpa.updater.ActivityMigrationService;
import org.exoplatform.social.core.jpa.updater.IdentityMigrationService;
import org.exoplatform.social.core.jpa.updater.RDBMSMigrationManager;
import org.exoplatform.social.core.jpa.updater.*;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.updater.RelationshipMigrationService;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.IdentityManagerImpl;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.impl.ActivityStorageImpl;
import org.exoplatform.social.core.storage.impl.IdentityStorageImpl;
import org.exoplatform.social.core.storage.impl.RelationshipStorageImpl;
import org.exoplatform.social.core.storage.impl.SpaceStorageImpl;
import org.jboss.byteman.contrib.bmunit.BMUnit;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
@ConfiguredBy({
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.core.test.configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/component.search.configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.test.jcr-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.test.portal-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.migrate.test.configuration.xml")
})
public class MigrationTest extends BaseCoreTest {
  protected final Log LOG = ExoLogger.getLogger(AbstractAsynMigrationTest.class);
  private ActivityStorageImpl activityJCRStorage;
  private IdentityStorageImpl identityJCRStorage;
  private RelationshipStorageImpl relationshipJCRStorage;
  private SpaceStorageImpl spaceJCRStorage;

  private RDBMSIdentityStorageImpl identityJPAStorage;

  protected ActivityStorage activityStorage;
  private SpaceStorage spaceStorage;

  private IdentityMigrationService identityMigrationService;
  private ActivityMigrationService activityMigration;
  private RelationshipMigrationService relationshipMigration;
  private RDBMSMigrationManager rdbmsMigrationManager;
  private SpaceMigrationService spaceMigrationService;

  private List<ExoSocialActivity> activitiesToDelete = new ArrayList<>();

  @Override
  public void setUp() throws Exception {
    begin();
    // If is query number test, init byteman
    hasByteMan = getClass().isAnnotationPresent(QueryNumberTest.class);
    if (hasByteMan) {
      count = 0;
      maxQuery = 0;
      BMUnit.loadScriptFile(getClass(), "queryBaseCount", "src/test/resources");
    }

    identityJPAStorage = getService(RDBMSIdentityStorageImpl.class);

    activityStorage = getService(ActivityStorage.class);
    spaceStorage = getService(SpaceStorage.class);

    identityJCRStorage = getService(IdentityStorageImpl.class);
    activityJCRStorage = getService(ActivityStorageImpl.class);
    relationshipJCRStorage = getService(RelationshipStorageImpl.class);
    spaceJCRStorage = getService(SpaceStorageImpl.class);


    identityManager = getService(IdentityManager.class);
    activityManager =  getService(ActivityManager.class);
    relationshipManager = getService(RelationshipManager.class);

    spaceService = getService(SpaceService.class);

    entityManagerService = getService(EntityManagerService.class);

    //


    identityMigrationService = getService(IdentityMigrationService.class);
    activityMigration = getService(ActivityMigrationService.class);
    relationshipMigration = getService(RelationshipMigrationService.class);
    spaceMigrationService = getService(SpaceMigrationService.class);

    activitiesToDelete = new ArrayList<>();
  }

  @Override
  public void tearDown() throws Exception {
    //super.tearDown();

    for (ExoSocialActivity activity : activitiesToDelete) {
      activityStorage.deleteActivity(activity.getId());
    }

    end();
  }

  public void testMigrateIdentityWithAvatar() throws Exception {
    // create jcr data
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
    Profile rootProfile = rootIdentity.getProfile();

    InputStream inputStream = getClass().getResourceAsStream("/eXo-Social.png");
    AvatarAttachment avatarAttachment = new AvatarAttachment(null, "avatar", "png", inputStream, null, System.currentTimeMillis());
    rootProfile.setProperty(Profile.AVATAR, avatarAttachment);

    identityManager.updateProfile(rootProfile);

    end();
    begin();
    switchToUseJPAStorage();

    identityMigrationService.start();

    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
    rootProfile = rootIdentity.getProfile();

    assertNotNull(rootProfile.getAvatarUrl());
    assertEquals(IdentityAvatarRestService.buildAvatarURL(OrganizationIdentityProvider.NAME, "root"), rootProfile.getAvatarUrl());
  }


  public void testMigrateActivityWithMention() throws Exception {
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john", false);
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo", false);
    maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary", false);

    Map<String, String> params = new HashMap<String, String>();
    params.put("MESSAGE", "activity message for here");
    final ExoSocialActivity activity = ActivityBuilder.getInstance()
            .posterId(rootIdentity.getId())
            .title("activity title @root @mary")
            .body("activity body")
            .titleId("titleId")
            .isComment(false)
            .take();

    final ExoSocialActivity activity1 = ActivityBuilder.getInstance()
            .posterId(rootIdentity.getId())
            .title("activity title\nsecond line")
            .body("activity body\nsecond line")
            .titleId("titleId1")
            .isComment(false)
            .take();

    activityJCRStorage.setInjectStreams(false);
    activityJCRStorage.saveActivity(johnIdentity, activity);
    activityJCRStorage.saveActivity(maryIdentity, activity1);
    activityJCRStorage.setInjectStreams(true);

    end();



    activityMigration.addMigrationListener(new Listener<ExoSocialActivity, String>() {
      @Override
      public void onEvent(Event<ExoSocialActivity, String> event) throws Exception {
        String newId = event.getData();
        if (event.getSource().getId().equals(activity.getId())) {
          activity.setId(newId);
        }
        if (event.getSource().getId().equals(activity1.getId())) {
          activity1.setId(newId);
        }
      }
    });

    switchToUseJPAStorage();
    identityMigrationService.start();
    activityMigration.start();


    begin();

    ExoSocialActivity migrated = activityStorage.getActivity(activity.getId());
    activitiesToDelete.add(migrated);
    assertNotNull(migrated);
    assertEquals(2, migrated.getMentionedIds().length);

    ExoSocialActivity migrated1 = activityStorage.getActivity(activity1.getId());
    activitiesToDelete.add(migrated1);
    assertNotNull(migrated1);
    assertEquals("activity title<br />second line", migrated1.getTitle());
    assertEquals("activity body\nsecond line", migrated1.getBody());
  }

  public void testMigrateSpace() throws Exception {
    Space space = new Space();
    space.setApp("app1:appName1:true:active,app2:appName2:false:deactive");
    space.setDisplayName("my space");
    space.setPrettyName("my_space");
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space");
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/spaces/space1");
    String[] managers = new String[] { "demo"};
    String[] members = new String[] {};
    String[] invitedUsers = new String[] {};
    String[] pendingUsers = new String[] {};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);
    space.setUrl(space.getPrettyName());

    identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo", true).getProfile();

    spaceJCRStorage.saveSpace(space, true);

    Identity spaceIdentity = new Identity(SpaceIdentityProvider.NAME, space.getPrettyName());
    identityJCRStorage.saveIdentity(spaceIdentity);
    Profile spaceProfile = new Profile(spaceIdentity);
    identityJCRStorage.saveProfile(spaceProfile);

    end();
    begin();
    spaceMigrationService.start();
    switchToUseJPAStorage();

    Space s1 = spaceStorage.getSpaceByPrettyName(space.getPrettyName());
    assertNotNull(s1);

    identityJCRStorage.deleteIdentity(spaceIdentity);
    spaceStorage.deleteSpace(s1.getId());
  }

  protected void switchToUseJPAStorage() {
    // Swith to use RDBMSIdentityStorage
    ((IdentityManagerImpl)identityManager).setIdentityStorage(identityJPAStorage);
    if (spaceStorage instanceof RDBMSSpaceStorageImpl) {
      ((RDBMSSpaceStorageImpl)spaceStorage).setIdentityStorage(identityJPAStorage);
    }
    if (activityStorage instanceof RDBMSActivityStorageImpl) {
      ((RDBMSActivityStorageImpl)activityStorage).setIdentityStorage(identityJPAStorage);
    }
  }
}
