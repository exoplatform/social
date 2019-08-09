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

import org.exoplatform.social.core.chromattic.entity.ProviderEntity;
import org.exoplatform.social.core.chromattic.entity.ProviderRootEntity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.storage.RDBMSActivityStorageImpl;
import org.exoplatform.social.core.jpa.storage.RDBMSSpaceStorageImpl;
import org.exoplatform.social.core.jpa.test.MaxQueryNumber;
import org.exoplatform.social.core.jpa.updater.MigrationContext;
import org.exoplatform.social.core.manager.IdentityManagerImpl;
import org.exoplatform.social.core.relationship.model.Relationship;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class AsynMigrateActivitiesTest extends AbstractAsynMigrationTest {
  @MaxQueryNumber(36990)
  public void testMigrationActivities() throws Exception {
    // create jcr data
    LOG.info("Create connection for root,john,mary and demo");
    begin();
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john", false);
    maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary", false);
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo", false);

    //John invites Demo
    Relationship johnToDemo = new Relationship(johnIdentity, demoIdentity, Relationship.Type.PENDING);
    relationshipJCRStorage.saveRelationship(johnToDemo);

    //John invites Mary
    Relationship johnToMary = new Relationship(johnIdentity, maryIdentity, Relationship.Type.PENDING);
    relationshipJCRStorage.saveRelationship(johnToMary);

    //John invites Root
    Relationship johnToRoot = new Relationship(johnIdentity, rootIdentity, Relationship.Type.PENDING);
    relationshipJCRStorage.saveRelationship(johnToRoot);

    //Root invites Mary
    Relationship rootToMary = new Relationship(rootIdentity, maryIdentity, Relationship.Type.PENDING);
    relationshipJCRStorage.saveRelationship(rootToMary);

    //Demo invites Mary
    Relationship demoToMary = new Relationship(demoIdentity, maryIdentity, Relationship.Type.PENDING);
    relationshipJCRStorage.saveRelationship(demoToMary);

    //Demo invites Root
    Relationship demoToRoot = new Relationship(demoIdentity, rootIdentity, Relationship.Type.PENDING);
    relationshipJCRStorage.saveRelationship(demoToRoot);


    //confirmed john and demo
    johnToDemo.setStatus(Relationship.Type.CONFIRMED);
    relationshipJCRStorage.saveRelationship(johnToDemo);

    //confirmed john and demo
    johnToMary.setStatus(Relationship.Type.CONFIRMED);
    relationshipJCRStorage.saveRelationship(johnToMary);

    //confirmed john and root
    johnToRoot.setStatus(Relationship.Type.CONFIRMED);
    relationshipJCRStorage.saveRelationship(johnToRoot);

    //confirmed root and mary
    rootToMary.setStatus(Relationship.Type.CONFIRMED);
    relationshipJCRStorage.saveRelationship(rootToMary);

    //confirmed demo and mary
    demoToMary.setStatus(Relationship.Type.CONFIRMED);
    relationshipJCRStorage.saveRelationship(demoToMary);

    //confirmed demo and root
    demoToRoot.setStatus(Relationship.Type.CONFIRMED);
    relationshipJCRStorage.saveRelationship(demoToRoot);

    //
    LOG.info("Create the activities storage on JCR ....");
    activityJCRStorage.setInjectStreams(false);
    createActivityToOtherIdentity(rootIdentity, johnIdentity, 5);
    createActivityToOtherIdentity(demoIdentity, maryIdentity, 5);
    createActivityToOtherIdentity(johnIdentity, demoIdentity, 5);
    createActivityToOtherIdentity(maryIdentity, rootIdentity, 5);
    createActivityEmoji(rootIdentity, rootIdentity);
    activityJCRStorage.setInjectStreams(true);
    LOG.info("Done created the activities storage on JCR.");
    end();

    ProviderRootEntity providerEntity = identityJCRStorage.getProviderRoot();
    ProviderEntity organization = providerEntity.getProvider(OrganizationIdentityProvider.NAME);
    assertTrue(0 < organization.getIdentities().size());

    // Swith to use RDBMSIdentityStorage
    ((IdentityManagerImpl)identityManager).setIdentityStorage(identityJPAStorage);
    if (spaceStorage instanceof RDBMSSpaceStorageImpl) {
      ((RDBMSSpaceStorageImpl)spaceStorage).setIdentityStorage(identityJPAStorage);
    }
    if (activityStorage instanceof RDBMSActivityStorageImpl) {
      ((RDBMSActivityStorageImpl)activityStorage).setIdentityStorage(identityJPAStorage);
    }

    //
    rdbmsMigrationManager.start();
    //
    rdbmsMigrationManager.getMigrater().await();

    begin();

    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john", false);
    maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary", false);
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo", false);

    //
    assertTrue(getOrCreateSettingValue(MigrationContext.SOC_RDBMS_CONNECTION_MIGRATION_KEY));
    assertTrue(getOrCreateSettingValue(MigrationContext.SOC_RDBMS_ACTIVITY_MIGRATION_KEY));
    assertTrue(getOrCreateSettingValue(MigrationContext.SOC_RDBMS_MIGRATION_STATUS_KEY));

    assertTrue(getOrCreateSettingValue(MigrationContext.SOC_RDBMS_ACTIVITY_CLEANUP_KEY));
    assertTrue(getOrCreateSettingValue(MigrationContext.SOC_RDBMS_CONNECTION_CLEANUP_KEY));

    assertTrue(getOrCreateSettingValue(MigrationContext.SOC_RDBMS_IDENTITY_MIGRATION_KEY));
    assertTrue(getOrCreateSettingValue(MigrationContext.SOC_RDBMS_IDENTITY_CLEANUP_KEY));

    assertEquals(21, activityStorage.getActivityFeed(rootIdentity, 0, 100).size());
    assertEquals(21, activityStorage.getActivityFeed(maryIdentity, 0, 100).size());
    assertEquals(21, activityStorage.getActivityFeed(johnIdentity, 0, 100).size());
    assertEquals(21, activityStorage.getActivityFeed(demoIdentity, 0, 100).size());
  }
}
