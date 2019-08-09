/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
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

package org.exoplatform.social.core.jpa.updater;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;

import org.exoplatform.commons.file.services.NameSpaceService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.social.core.chromattic.entity.ProviderRootEntity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.storage.RDBMSIdentityStorageImpl;
import org.exoplatform.social.core.manager.IdentityManagerImpl;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.impl.IdentityStorageImpl;
import org.picocontainer.Startable;

import org.exoplatform.commons.api.persistence.DataInitializer;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.jcr.impl.core.SessionImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class RDBMSMigrationManager implements Startable {
  private static final Log LOG = ExoLogger.getLogger(RDBMSMigrationManager.class);
  
  public static final String MIGRATION_SETTING_GLOBAL_KEY = "MIGRATION_SETTING_GLOBAL";
  public static final String MIGRATION_RUNNING_NODE_KEY = "NODE_RUNNING_MIGRATION";

  private Thread migrationThread;
  
  private final CountDownLatch migrater;

  private final RepositoryService repositoryService;

  private final ChromatticManager chromatticManager;

  private RelationshipMigrationService relationshipMigration;

  private ActivityMigrationService activityMigration;
  
  private SpaceMigrationService spaceMigration;

  private IdentityMigrationService identityMigration;
  
  private SettingService settingService;

  private boolean forceRemoveJCR = false;
  private boolean clusterMode = false;
  private String  nodeName = null;

//  public RDBMSMigrationManager(RelationshipMigrationService relationshipMigration,
//                               ActivityMigrationService activityMigration, 
//                               SettingService settingService) {
//    this.relationshipMigration = relationshipMigration;
//    this.activityMigration = activityMigration;
//    this.settingService = settingService;
//    migrater = new CountDownLatch(1);
//    //
//  }
  public RDBMSMigrationManager(InitParams initParams, NameSpaceService nameSpaceService, RepositoryService repoService, ChromatticManager chromatticManager) {
    CommonsUtils.getService(DataInitializer.class);
    this.repositoryService = repoService;
    this.chromatticManager = chromatticManager;
    migrater = new CountDownLatch(1);
    //
    if (initParams != null) {
      ValueParam param = initParams.getValueParam("forceDeleteJCRData");
      if (param != null) {
        forceRemoveJCR = "true".equalsIgnoreCase(param.getValue());
      }

      param = initParams.getValueParam("clusterMode");
      if (param != null) {
        clusterMode = "true".equalsIgnoreCase(param.getValue());
      }
      param = initParams.getValueParam("nodeName");
      if (param != null) {
        nodeName = param.getValue();
      }
    }
  }
  
  

  /**
   * Gets the relationship service
   * @return RelationshipMigrationService
   */
  public RelationshipMigrationService getRelationshipMigration() {
    return relationshipMigration == null ? CommonsUtils.getService(RelationshipMigrationService.class) : relationshipMigration;
  }

  public IdentityMigrationService getIdentityMigrationService() {
    if (identityMigration == null) {
      identityMigration = CommonsUtils.getService(IdentityMigrationService.class);
    }
    return identityMigration;
  }

  public SpaceMigrationService getSpaceMigrationService() {
    if (spaceMigration == null) {
      spaceMigration = CommonsUtils.getService(SpaceMigrationService.class);
    }
    return spaceMigration;
  }

  @Override
  public void start() {
    initMigrationSetting();
    Runnable migrateTask = new Runnable() {
      @Override
      public void run() {
        boolean start = false;

        RequestLifeCycle.begin(PortalContainer.getInstance());
        try {
          start = checkCanStartMigration();
          if (!start || MigrationContext.isDone()) {
            migrater.countDown();
            return;
          }

          // Check JCR data is existing or not
          ProviderRootEntity providerRoot = getRelationshipMigration().getProviderRoot();
          if (providerRoot == null || (providerRoot != null && providerRoot.getProviders().get(SpaceIdentityProvider.NAME) == null &&
                  providerRoot.getProviders().get(OrganizationIdentityProvider.NAME) == null)) {
            LOG.info("No Social data to migrate from JCR to RDBMS ");
            updateMigrationSettings(start);
            migrater.countDown();
            return;
          }
        } catch (Exception ex) {
          LOG.warn("Error checking JCR data to migrate", ex);
          updateMigrationSettings(start);
          migrater.countDown();
          return;
        } finally {
          RequestLifeCycle.end();
        }


        long timeToMigrateSpaces = 0;
        long timeToMigrateIdentities = 0;
        long timeToMigrateActivities = 0;
        long timeToMigrateConnections = 0;

        long totalCleanupTime = 0;
        long timeToCleanupConnections = 0;
        long timeToCleanupActivities = 0;
        long timeToCleanupIdentities = 0;
        long timeToCleanupSpaces = 0;

        long startTime = System.currentTimeMillis();

        //
        Field field =  null;
        RequestLifeCycle.begin(ExoContainerContext.getCurrentContainer());
        try {

          // Set FORCE_USE_GET_NODES_LAZILY in JCR
          field =  SessionImpl.class.getDeclaredField("FORCE_USE_GET_NODES_LAZILY");
          if (field != null) {
            field.setAccessible(true);
            field.set(null, true);
          }

          if (!MigrationContext.isDone()) {

            boolean useMigrationIdentityStorage = false;
            IdentityManagerImpl identityManager = CommonsUtils.getService(IdentityManagerImpl.class);
            IdentityStorage identityStorage = identityManager.getIdentityStorage();
            if (!MigrationContext.isIdentityDone()) {
              IdentityStorageImpl jcrIdentityStorage = CommonsUtils.getService(IdentityStorageImpl.class);
              RDBMSIdentityStorageImpl jpaIdentityStorage = CommonsUtils.getService(RDBMSIdentityStorageImpl.class);
              MigrationIdentityStorage storage = new MigrationIdentityStorage(jcrIdentityStorage, jpaIdentityStorage, getIdentityMigrationService());
              identityManager.setIdentityStorage(storage);
              useMigrationIdentityStorage = true;
            }

            //
            LOG.info("START ASYNC MIGRATION---------------------------------------------------");
            //
            if (!MigrationContext.isDone()) {
              if (!MigrationContext.isDone() && !MigrationContext.isSpaceDone()) {
                long t = System.currentTimeMillis();
                getSpaceMigrationService().start();
                updateSettingValue(MigrationContext.SOC_RDBMS_SPACE_MIGRATION_KEY, MigrationContext.isSpaceDone());
                timeToMigrateSpaces = System.currentTimeMillis() - t;
              }

              // We could start to migrate identities if there is some spaces migrated failure
              if (!MigrationContext.isDone() && !MigrationContext.isIdentityDone()) {
                long t = System.currentTimeMillis();
                getIdentityMigrationService().start();
                updateSettingValue(MigrationContext.SOC_RDBMS_IDENTITY_MIGRATION_KEY, MigrationContext.isIdentityDone());

                if (useMigrationIdentityStorage && MigrationContext.isIdentityDone()) {
                  identityManager.setIdentityStorage(identityStorage);
                }
                timeToMigrateIdentities = System.currentTimeMillis() - t;
              }

              // We could not start to migrate connections and activities if there are identities which migrated failure
              if (!MigrationContext.isDone() && MigrationContext.isIdentityDone() && !MigrationContext.isActivityDone()) {
                long t = System.currentTimeMillis();
                getActivityMigrationService().start();
                updateSettingValue(MigrationContext.SOC_RDBMS_ACTIVITY_MIGRATION_KEY, MigrationContext.isActivityDone());
                timeToMigrateActivities = System.currentTimeMillis() - t;
              }

              if (!MigrationContext.isDone() && MigrationContext.isIdentityDone() && !MigrationContext.isConnectionDone()) {
                long t = System.currentTimeMillis();
                getRelationshipMigration().start();
                updateSettingValue(MigrationContext.SOC_RDBMS_CONNECTION_MIGRATION_KEY, MigrationContext.isConnectionDone());
                timeToMigrateConnections = System.currentTimeMillis() - t;
              }
            }

            // We only try to start cleanup when all identities are migrated successfully
            // Because if there is identity migrate failed, we could not start to migrate connection and activities
            if (!MigrationContext.isDone() && MigrationContext.isIdentityDone()) {

              // cleanup Connections
              if (!MigrationContext.isConnectionCleanupDone()) {
                long t = System.currentTimeMillis();
                getRelationshipMigration().doRemove();

                if (MigrationContext.getIdentitiesCleanupConnectionFailed().isEmpty()) {
                  updateSettingValue(MigrationContext.SOC_RDBMS_CONNECTION_CLEANUP_KEY, Boolean.TRUE);
                  MigrationContext.setConnectionCleanupDone(true);
                }
                timeToCleanupConnections = System.currentTimeMillis() - t;
              }

              // cleanup activities
              if (!MigrationContext.isActivityCleanupDone()) {
                long t = System.currentTimeMillis();
                getActivityMigrationService().doRemove();

                if (MigrationContext.getIdentitiesCleanupActivityFailed().isEmpty()) {
                  updateSettingValue(MigrationContext.SOC_RDBMS_ACTIVITY_CLEANUP_KEY, Boolean.TRUE);
                  MigrationContext.setActivityCleanupDone(true);
                }
                timeToCleanupActivities = System.currentTimeMillis() - t;
              }

              // Cleanup identity
              if (!MigrationContext.isIdentityCleanupDone()) {
                long t = System.currentTimeMillis();
                getIdentityMigrationService().doRemove();

                if (MigrationContext.getIdentitiesCleanupFailed().isEmpty()) {
                  updateSettingValue(MigrationContext.SOC_RDBMS_IDENTITY_CLEANUP_KEY, Boolean.TRUE);
                  MigrationContext.setIdentityCleanupDone(true);
                }
                timeToCleanupIdentities = System.currentTimeMillis() - t;
              }

              // cleanup spaces
              if (!MigrationContext.isSpaceCleanupDone()) {
                long t = System.currentTimeMillis();
                getSpaceMigrationService().doRemove();

                if (MigrationContext.getSpaceCleanupFailed().isEmpty()) {
                  updateSettingValue(MigrationContext.SOC_RDBMS_SPACE_CLEANUP_KEY, Boolean.TRUE);
                  MigrationContext.setSpaceCleanupDone(true);
                }
                timeToCleanupSpaces = System.currentTimeMillis() - t;
              }

              if (MigrationContext.isIdentityCleanupDone()&& MigrationContext.isSpaceCleanupDone() || forceRemoveJCR){
                updateSettingValue(MigrationContext.SOC_RDBMS_MIGRATION_STATUS_KEY, Boolean.TRUE);
                MigrationContext.setDone(true);
              }
            }
            
            //
            LOG.info("END ASYNC MIGRATION-----------------------------------------------------");
          }

        } catch (Exception e) {
          LOG.error("Failed to running Migration data from JCR to RDBMS", e);
        } finally {

          removeRunningNodeIfPresent(start);

          if (field != null) {
            try {
              field.set(null, false);
            } catch (IllegalArgumentException e) {
              LOG.warn(e.getMessage(), e);
            } catch (IllegalAccessException e) {
              LOG.warn(e.getMessage(), e);
            }
          }

          LOG.info(String.format("Migration job has done, total time is %s (ms)", (System.currentTimeMillis() - startTime)));
          LOG.info(String.format("Migration space in %s (ms)", timeToMigrateSpaces));
          LOG.info(String.format("- Number space failed: %s", MigrationContext.getSpaceMigrateFailed().size()));
          if (!MigrationContext.getSpaceMigrateFailed().isEmpty()) {
            LOG.warn("- space failed: " + MigrationContext.getSpaceMigrateFailed());
          }

          LOG.info(String.format("Migration identities in %s (ms)", timeToMigrateIdentities));
          LOG.info(String.format("- Number identities failed: %s", MigrationContext.getIdentitiesMigrateFailed().size()));
          if (!MigrationContext.getIdentitiesMigrateFailed().isEmpty()) {
            LOG.warn("- identities failed: " + MigrationContext.getIdentitiesMigrateFailed());
          }

          if (!MigrationContext.getIdentitiesMigrateFailed().isEmpty()) {
            LOG.info("We could not continue migration job because the identities migration was failed");
          } else {
            LOG.info(String.format("Migration relationships in %s (ms)", timeToMigrateConnections));
            LOG.info(String.format("- migrate failed for %s user(s)", MigrationContext.getIdentitiesMigrateConnectionFailed().size()));
            if (!MigrationContext.getIdentitiesMigrateConnectionFailed().isEmpty()) {
              LOG.warn("- identities failed: " + MigrationContext.getIdentitiesMigrateConnectionFailed());
            }

            LOG.info(String.format("Migration activities in %s (ms)", timeToMigrateActivities));
            LOG.info(String.format("- migrate failed for %s user(s)", MigrationContext.getIdentitiesMigrateActivityFailed().size()));
            if (!MigrationContext.getIdentitiesMigrateActivityFailed().isEmpty()) {
              LOG.warn("- identities failed: " + MigrationContext.getIdentitiesMigrateActivityFailed());
            }

            // Cleanup
            LOG.info(String.format("Cleanup relationship in %s (ms)", timeToCleanupConnections));
            LOG.info(String.format("- cleanup connection failed for %s user(s)", MigrationContext.getIdentitiesCleanupConnectionFailed().size()));
            if (!MigrationContext.getIdentitiesCleanupConnectionFailed().isEmpty()) {
              LOG.warn("- identities cleanup failed: " + MigrationContext.getIdentitiesCleanupConnectionFailed());
            }

            LOG.info(String.format("Cleanup activities in %s (ms)", timeToCleanupActivities));
            LOG.info(String.format("- cleanup activities failed for %s identity(s)", MigrationContext.getIdentitiesCleanupActivityFailed().size()));
            if (!MigrationContext.getIdentitiesCleanupActivityFailed().isEmpty()) {
              LOG.warn("- identities cleanup failed: " + MigrationContext.getIdentitiesCleanupActivityFailed());
            }

            LOG.info(String.format("Cleanup identities in %s (ms)", timeToCleanupIdentities));
            LOG.info(String.format("- cleanup failed for %s identity(s)", MigrationContext.getIdentitiesCleanupFailed().size()));
            if (!MigrationContext.getIdentitiesCleanupFailed().isEmpty()) {
              LOG.warn("- identities cleanup failed: " + MigrationContext.getIdentitiesCleanupFailed());
            }

            LOG.info(String.format("Cleanup spaces in %s (ms)", timeToCleanupSpaces));
            LOG.info(String.format("- cleanup failed for %s space(s)", MigrationContext.getSpaceCleanupFailed().size()));
            if (!MigrationContext.getSpaceCleanupFailed().isEmpty()) {
              LOG.warn("- space cleanup failed: " + MigrationContext.getSpaceCleanupFailed());
            }
          }



          migrater.countDown();

          RequestLifeCycle.end();
        }
      }
    };
    this.migrationThread = new Thread(migrateTask);
    this.migrationThread.setPriority(Thread.NORM_PRIORITY);
    this.migrationThread.setName("SOC-MIGRATION-RDBMS");
    this.migrationThread.start();
  }
  
  private void initMigrationSetting() {
    settingService = CommonsUtils.getService(SettingService.class);
    MigrationContext.setDone(getOrCreateSettingValue(MigrationContext.SOC_RDBMS_MIGRATION_STATUS_KEY));
    //
    MigrationContext.setConnectionDone(getOrCreateSettingValue(MigrationContext.SOC_RDBMS_CONNECTION_MIGRATION_KEY));
    MigrationContext.setConnectionCleanupDone(getOrCreateSettingValue(MigrationContext.SOC_RDBMS_CONNECTION_CLEANUP_KEY));
    //
    MigrationContext.setActivityDone(getOrCreateSettingValue(MigrationContext.SOC_RDBMS_ACTIVITY_MIGRATION_KEY));
    MigrationContext.setActivityCleanupDone(getOrCreateSettingValue(MigrationContext.SOC_RDBMS_ACTIVITY_CLEANUP_KEY));

    MigrationContext.setSpaceDone(getOrCreateSettingValue(MigrationContext.SOC_RDBMS_SPACE_MIGRATION_KEY));
    MigrationContext.setSpaceCleanupDone(getOrCreateSettingValue(MigrationContext.SOC_RDBMS_SPACE_CLEANUP_KEY));

    MigrationContext.setIdentityDone(getOrCreateSettingValue(MigrationContext.SOC_RDBMS_IDENTITY_MIGRATION_KEY));
    MigrationContext.setIdentityCleanupDone(getOrCreateSettingValue(MigrationContext.SOC_RDBMS_IDENTITY_CLEANUP_KEY));

    MigrationContext.setForceCleanup(forceRemoveJCR);
  }

  private boolean getOrCreateSettingValue(String key) {
    try {
      SettingValue<?> migrationValue =  settingService.get(Context.GLOBAL, Scope.GLOBAL.id(MIGRATION_SETTING_GLOBAL_KEY), key);
      if (migrationValue != null) {
        return Boolean.parseBoolean(migrationValue.getValue().toString());
      } else {
        updateSettingValue(key, Boolean.FALSE);
        return false;
      }
    } finally {
      Scope.GLOBAL.id(null);
    }
  }

  private void updateSettingValue(String key, Boolean status) {
    try {
      RequestLifeCycle.begin(ExoContainerContext.getCurrentContainer());
      settingService.set(Context.GLOBAL, Scope.GLOBAL.id(MIGRATION_SETTING_GLOBAL_KEY), key, SettingValue.create(status));
    } finally {
      Scope.GLOBAL.id(null);
      RequestLifeCycle.end();
    }
  }

  private boolean checkCanStartMigration() {
    if (!clusterMode) {
      return true;
    }
    try {
      SettingValue<String> migrationValue =  (SettingValue<String>)settingService.get(Context.GLOBAL, Scope.GLOBAL.id(MIGRATION_SETTING_GLOBAL_KEY), MIGRATION_RUNNING_NODE_KEY);

      if (migrationValue == null || migrationValue.getValue() == null) {
        settingService.set(Context.GLOBAL, Scope.GLOBAL.id(MIGRATION_SETTING_GLOBAL_KEY), MIGRATION_RUNNING_NODE_KEY, SettingValue.create(nodeName));
        migrationValue =  (SettingValue<String>)settingService.get(Context.GLOBAL, Scope.GLOBAL.id(MIGRATION_SETTING_GLOBAL_KEY), MIGRATION_RUNNING_NODE_KEY);
      }

      if (migrationValue != null && migrationValue.getValue() != null
              && migrationValue.getValue().equals(nodeName)) {
        return true;
      } else {
        return false;
      }
    } finally {
      Scope.GLOBAL.id(null);
    }
  }

  private void updateMigrationSettings(boolean remove){
    // Update and mark that migrate was done
    updateSettingValue(MigrationContext.SOC_RDBMS_CONNECTION_MIGRATION_KEY, Boolean.TRUE);
    updateSettingValue(MigrationContext.SOC_RDBMS_ACTIVITY_MIGRATION_KEY, Boolean.TRUE);
    updateSettingValue(MigrationContext.SOC_RDBMS_SPACE_MIGRATION_KEY, Boolean.TRUE);
    updateSettingValue(MigrationContext.SOC_RDBMS_IDENTITY_MIGRATION_KEY, Boolean.TRUE);

    updateSettingValue(MigrationContext.SOC_RDBMS_CONNECTION_CLEANUP_KEY, Boolean.TRUE);
    updateSettingValue(MigrationContext.SOC_RDBMS_ACTIVITY_CLEANUP_KEY, Boolean.TRUE);
    updateSettingValue(MigrationContext.SOC_RDBMS_SPACE_CLEANUP_KEY, Boolean.TRUE);
    updateSettingValue(MigrationContext.SOC_RDBMS_IDENTITY_CLEANUP_KEY, Boolean.TRUE);

    updateSettingValue(MigrationContext.SOC_RDBMS_MIGRATION_STATUS_KEY, Boolean.TRUE);
    MigrationContext.setDone(true);

    removeRunningNodeIfPresent(remove);
  }

  private void removeRunningNodeIfPresent(boolean remove) {
    if (!clusterMode || !remove) return;
    try {
      settingService.remove(Context.GLOBAL, Scope.GLOBAL.id(MIGRATION_SETTING_GLOBAL_KEY), MIGRATION_RUNNING_NODE_KEY);
    } finally {
      Scope.GLOBAL.id(null);
    }
  }

  private ActivityMigrationService getActivityMigrationService() {
    if (activityMigration == null) {
      activityMigration = CommonsUtils.getService(ActivityMigrationService.class);
    }
    return activityMigration;
  }

  @Override
  public void stop() {
    relationshipMigration.stop();
    getActivityMigrationService().stop();
    try {
      this.migrationThread.join();
    } catch (InterruptedException e) {
      LOG.error(e);
    }
  }

  public CountDownLatch getMigrater() {
    return migrater;
  }
  
  
}