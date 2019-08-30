/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.social.core.jpa.concurrency;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.commons.file.services.NameSpaceService;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.RDBMSIdentityStorageImpl;
import org.exoplatform.social.core.jpa.storage.dao.ActivityDAO;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.jpa.storage.entity.ActivityEntity;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity;
import org.exoplatform.social.core.jpa.test.BaseCoreTest;
import org.exoplatform.social.core.jpa.test.QueryNumberTest;
import org.exoplatform.social.core.jpa.updater.ActivityMigrationService;
import org.exoplatform.social.core.jpa.updater.MigrationContext;
import org.exoplatform.social.core.jpa.updater.RDBMSMigrationManager;
import org.exoplatform.social.core.jpa.updater.RelationshipMigrationService;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.IdentityManagerImpl;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.impl.*;

import org.jboss.byteman.contrib.bmunit.BMUnit;
import org.junit.FixMethodOrder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jun 19, 2015  
 */
@QueryNumberTest
@FixMethodOrder
@ConfiguredBy({
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.core.test.configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.common.test.configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.test.jcr-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/component.search.configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.test.jcr-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.test.portal-configuration.xml"),
        @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/standalone/exo.social.component.migrate.test.configuration.xml")
})
public abstract class AbstractAsynMigrationTest extends BaseCoreTest {
  protected final Log LOG = ExoLogger.getLogger(AbstractAsynMigrationTest.class);
  protected ActivityStorageImpl activityJCRStorage;
  protected IdentityStorageImpl identityJCRStorage;
  protected RelationshipStorageImpl relationshipJCRStorage;
  protected ActivityMigrationService activityMigration;
  protected RelationshipMigrationService relationshipMigration;
  protected SettingService settingService;
  protected RDBMSMigrationManager rdbmsMigrationManager;
  protected RDBMSIdentityStorageImpl identityJPAStorage;
  protected SpaceStorage spaceStorage;
  protected NameSpaceService nameSpaceService;

  protected RepositoryService repoService;

  @Override
  public void setUp() throws Exception {
    // If is query number test, init byteman
    hasByteMan = getClass().isAnnotationPresent(QueryNumberTest.class);
    if (hasByteMan) {
      count = 0;
      maxQuery = 0;
      BMUnit.loadScriptFile(getClass(), "queryBaseCount", "src/test/resources");
    }

    repoService = getService(RepositoryService.class);
    nameSpaceService = getService(NameSpaceService.class);

    spaceStorage = getService(SpaceStorage.class);
    identityJCRStorage = getService(IdentityStorageImpl.class);
    identityJPAStorage = getService(RDBMSIdentityStorageImpl.class);
    identityManager = getService(IdentityManager.class);
    activityManager =  getService(ActivityManager.class);
    activityStorage = getService(ActivityStorage.class);
    relationshipManager = getService(RelationshipManager.class);
    spaceService = getService(SpaceService.class);
    entityManagerService = getService(EntityManagerService.class);
    //
    activityJCRStorage = getService(ActivityStorageImpl.class);
    relationshipJCRStorage = getService(RelationshipStorageImpl.class);
    activityMigration = getService(ActivityMigrationService.class);
    relationshipMigration = getService(RelationshipMigrationService.class);
    settingService = getService(SettingService.class);
    rdbmsMigrationManager = new RDBMSMigrationManager(null, nameSpaceService, getService(RepositoryService.class), getService(ChromatticManager.class));

    // Switch to use JCRIdentityStorage
    ((IdentityManagerImpl)identityManager).setIdentityStorage(identityJCRStorage);
    begin();
  }

  @Override
  public void tearDown() throws Exception {
    end();
    begin();
    ActivityDAO dao = getService(ActivityDAO.class);
    //
    List<ActivityEntity> items = dao.getAllActivities();
    for (ActivityEntity item : items) {
      dao.delete(item);
    }

    for (Space space : spaceService.getAllSpaces()) {
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
      if (spaceIdentity != null) {
        identityManager.deleteIdentity(spaceIdentity);
      }
      spaceService.deleteSpace(space);
    }

    IdentityDAO identityDAO = getService(IdentityDAO.class);
    Arrays.asList("root", "john", "mary", "demo").stream().forEach(userId -> {
      Identity identityJCRToDelete = identityJCRStorage.findIdentity(OrganizationIdentityProvider.NAME, userId);
      if(identityJCRToDelete != null) {
        identityJCRStorage.deleteIdentity(identityJCRToDelete);
      }
      IdentityEntity identityEntity = identityDAO.findByProviderAndRemoteId(OrganizationIdentityProvider.NAME, userId);
      if (identityEntity != null) {
        identityDAO.delete(identityEntity);
      }
    });


    // Reset value of settings
    updateSettingValue(MigrationContext.SOC_RDBMS_CONNECTION_MIGRATION_KEY, Boolean.FALSE);
    updateSettingValue(MigrationContext.SOC_RDBMS_ACTIVITY_MIGRATION_KEY, Boolean.FALSE);

    updateSettingValue(MigrationContext.SOC_RDBMS_ACTIVITY_CLEANUP_KEY, Boolean.FALSE);
    updateSettingValue(MigrationContext.SOC_RDBMS_CONNECTION_CLEANUP_KEY, Boolean.FALSE);
    updateSettingValue(MigrationContext.SOC_RDBMS_MIGRATION_STATUS_KEY, Boolean.FALSE);

    updateSettingValue(MigrationContext.SOC_RDBMS_SPACE_MIGRATION_KEY, Boolean.FALSE);
    updateSettingValue(MigrationContext.SOC_RDBMS_SPACE_CLEANUP_KEY, Boolean.FALSE);

    updateSettingValue(MigrationContext.SOC_RDBMS_IDENTITY_MIGRATION_KEY, Boolean.FALSE);
    updateSettingValue(MigrationContext.SOC_RDBMS_IDENTITY_CLEANUP_KEY, Boolean.FALSE);

    Scope.GLOBAL.id(null);
    //
    //super.tearDown();
    end();
  }


  protected boolean getOrCreateSettingValue(String key) {
    SettingValue<?> migrationValue =  settingService.get(Context.GLOBAL, Scope.GLOBAL.id(RDBMSMigrationManager.MIGRATION_SETTING_GLOBAL_KEY), key);
    if (migrationValue != null) {
      return Boolean.parseBoolean(migrationValue.getValue().toString());
    } else {
      return false;
    }
  }

  protected void updateSettingValue(String key, Boolean value) {
    settingService.set(Context.GLOBAL, Scope.GLOBAL.id(RDBMSMigrationManager.MIGRATION_SETTING_GLOBAL_KEY), key, new SettingValue<Boolean>(value));
  }

  protected void createActivityToOtherIdentity(Identity posterIdentity, Identity targetIdentity, int number) {
    List<ExoSocialActivity> activities = listOf(number, targetIdentity, posterIdentity, false, false);
    for (ExoSocialActivity activity : activities) {
      try {
        activity = activityJCRStorage.saveActivity(targetIdentity, activity);
        //
        Map<String, String> params = new HashMap<String, String>();
        params.put("MESSAGE",
                   "                                CRaSH is the open source shell for the JVM. The shell can be accessed by various ways, remotely using network protocols such as SSH, locally by attaching a shell to a running virtual machine or via a web interface. Commands are written Groovy and can be developed live making the extensibility of the shell easy with quick development cycles. Since the version 1.3, the REPL also speaks the Groovy language, allowing Groovy combination of command using pipes.  CRaSH comes with commands such as thread management, log management, database access and JMX. The session will begin with an introduction to the shell. The main part of the session will focus on showing CRaSH commands development with few examples, showing how easy and powerful the development is.  The audience will learn how to use CRaSH for their own needs: it can be a simple usage or more advanced like developing a command or embedding the shell in their own runtime like a web application or a Grails application.");
        List<ExoSocialActivity> comments = listOf(3, targetIdentity, posterIdentity, true, false);
        for (ExoSocialActivity comment : comments) {
          comment.setTitle("comment of " + posterIdentity.getId());
          comment.setTemplateParams(params);
          //
          activityJCRStorage.saveComment(activity, comment);
        }
      } catch (Exception e) {
        LOG.error("can not save activity.", e);
      }
    }
    StorageUtils.persist();
  }

  protected void createActivityEmoji(Identity posterIdentity, Identity targetIdentity) {
    ExoSocialActivity activity = oneOfActivity("Thats a nice joke 😆😆😆 😛",
                                               posterIdentity,
                                               false,
                                               false);
    try {
      activity = activityJCRStorage.saveActivity(targetIdentity, activity);
      //
      Map<String, String> params = new HashMap<String, String>();
      params.put("MESSAGE",
                 "                                CRaSH is the open source shell for the JVM. The shell can be accessed by various ways, remotely using network protocols such as SSH, locally by attaching a shell to a running virtual machine or via a web interface. Commands are written Groovy and can be developed live making the extensibility of the shell easy with quick development cycles. Since the version 1.3, the REPL also speaks the Groovy language, allowing Groovy combination of command using pipes.  CRaSH comes with commands such as thread management, log management, database access and JMX. The session will begin with an introduction to the shell. The main part of the session will focus on showing CRaSH commands development with few examples, showing how easy and powerful the development is.  The audience will learn how to use CRaSH for their own needs: it can be a simple usage or more advanced like developing a command or embedding the shell in their own runtime like a web application or a Grails application.");
      List<ExoSocialActivity> comments = listOf(3, targetIdentity, posterIdentity, true, false);
      for (ExoSocialActivity comment : comments) {
        comment.setTitle("comment of " + posterIdentity.getId());
        comment.setTemplateParams(params);
        //
        activityJCRStorage.saveComment(activity, comment);
      }
    } catch (Exception e) {
      LOG.error("can not save activity.", e);
    }
    StorageUtils.persist();
  }
}