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
package org.exoplatform.social.core.jpa.updater;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jun 30, 2015  
 */
public final class MigrationContext {
  public static final String SOC_RDBMS_MIGRATION_STATUS_KEY = "SOC_RDBMS_MIGRATION_DONE";
  public static final String SOC_RDBMS_ACTIVITY_MIGRATION_KEY = "SOC_RDBMS_ACTIVITY_MIGRATION_DONE";
  public static final String SOC_RDBMS_ACTIVITY_CLEANUP_KEY = "SOC_RDBMS_ACTIVITY_CLEANUP_DONE";
  public static final String SOC_RDBMS_CONNECTION_MIGRATION_KEY = "SOC_RDBMS_CONNECTION_MIGRATION_DONE";
  public static final String SOC_RDBMS_CONNECTION_CLEANUP_KEY = "SOC_RDBMS_CONNECTION_CLEANUP_DONE";
  public static final String SOC_RDBMS_SPACE_MIGRATION_KEY = "SOC_RDBMS_SPACE_MIGRATION_DONE";
  public static final String SOC_RDBMS_SPACE_CLEANUP_KEY = "SOC_RDBMS_SPACE_CLEANUP_DONE";
  public static final String SOC_RDBMS_IDENTITY_MIGRATION_KEY = "SOC_RDBMS_IDENTITY_MIGRATION_DONE";
  public static final String SOC_RDBMS_IDENTITY_CLEANUP_KEY = "SOC_RDBMS_IDENTITY_CLEANUP_DONE";

  public static final String KEY_MIGRATE_CONNECTION = "connection_migrated";
  public static final String KEY_MIGRATE_ACTIVITIES = "activity_migrated";
  public static final String TRUE_STRING = "true";

  
  //
  private static boolean isDone = false;
  private static boolean isActivityDone = false;
  private static boolean isActivityCleanupDone = false;
  private static boolean isConnectionDone = false;
  private static boolean isConnectionCleanupDone = false;
  private static boolean isSpaceDone = false;
  private static boolean isSpaceCleanupDone = false;
  private static boolean isIdentityDone = false;
  private static boolean isIdentityCleanupDone = false;

  private static boolean forceCleanup = false;

  private static Set<String> spaceMigrateFailed = new HashSet<>();
  private static Set<String> identitiesMigrateFailed = new HashSet<>();
  private static Set<String> identitiesMigrateConnectionFailed = new HashSet<>();
  private static Set<String> identitiesMigrateActivityFailed = new HashSet<>();

  private static Set<String> identitiesCleanupConnectionFailed = new HashSet<>();
  private static Set<String> identitiesCleanupActivityFailed = new HashSet<>();
  private static Set<String> identitiesCleanupFailed = new HashSet<>();
  private static Set<String> spaceCleanupFailed = new HashSet<>();


  public static boolean isDone() {
    return isDone;
  }

  public static void setDone(boolean isDoneArg) {
    isDone = isDoneArg;
  }

  public static boolean isActivityDone() {
    return isActivityDone;
  }

  public static void setActivityDone(boolean isActivityDoneArg) {
    isActivityDone = isActivityDoneArg;
  }

  public static boolean isConnectionDone() {
    return isConnectionDone;
  }

  public static void setConnectionDone(boolean isConnectionDoneArg) {
    isConnectionDone = isConnectionDoneArg;
  }

  public static boolean isActivityCleanupDone() {
    return isActivityCleanupDone;
  }

  public static void setActivityCleanupDone(boolean isActivityCleanupDone) {
    MigrationContext.isActivityCleanupDone = isActivityCleanupDone;
  }

  public static boolean isConnectionCleanupDone() {
    return isConnectionCleanupDone;
  }

  public static void setConnectionCleanupDone(boolean isConnectionCleanupDone) {
    MigrationContext.isConnectionCleanupDone = isConnectionCleanupDone;
  }

  public static boolean isSpaceDone() {
    return isSpaceDone;
  }

  public static void setSpaceDone(boolean isSpaceDone) {
    MigrationContext.isSpaceDone = isSpaceDone;
  }

  public static boolean isSpaceCleanupDone() {
    return isSpaceCleanupDone;
  }

  public static void setSpaceCleanupDone(boolean isSpaceCleanupDone) {
    MigrationContext.isSpaceCleanupDone = isSpaceCleanupDone;
  }

  public static boolean isIdentityDone() {
    return MigrationContext.isIdentityDone;
  }
  public static void setIdentityDone(boolean isIdentityDone) {
    MigrationContext.isIdentityDone = isIdentityDone;
  }

  public static boolean isIdentityCleanupDone() {
    return MigrationContext.isIdentityCleanupDone;
  }

  public static void setIdentityCleanupDone(boolean isIdentityCleanupDone) {
    MigrationContext.isIdentityCleanupDone = isIdentityCleanupDone;
  }

  public static Set<String> getSpaceMigrateFailed() {
    return spaceMigrateFailed;
  }

  public static void setSpaceMigrateFailed(Set<String> spaceMigrateFailed) {
    MigrationContext.spaceMigrateFailed = spaceMigrateFailed;
  }

  public static Set<String> getIdentitiesMigrateFailed() {
    return identitiesMigrateFailed;
  }

  public static void setIdentitiesMigrateFailed(Set<String> identitiesMigrateFailed) {
    MigrationContext.identitiesMigrateFailed = identitiesMigrateFailed;
  }

  public static Set<String> getIdentitiesMigrateConnectionFailed() {
    return identitiesMigrateConnectionFailed;
  }

  public static void setIdentitiesMigrateConnectionFailed(Set<String> identitiesMigrateConnectionFailed) {
    MigrationContext.identitiesMigrateConnectionFailed = identitiesMigrateConnectionFailed;
  }

  public static Set<String> getIdentitiesMigrateActivityFailed() {
    return identitiesMigrateActivityFailed;
  }

  public static void setIdentitiesMigrateActivityFailed(Set<String> identitiesMigrateActivityFailed) {
    MigrationContext.identitiesMigrateActivityFailed = identitiesMigrateActivityFailed;
  }

  public static Set<String> getIdentitiesCleanupConnectionFailed() {
    return identitiesCleanupConnectionFailed;
  }

  public static void setIdentitiesCleanupConnectionFailed(Set<String> identitiesCleanupConnectionFailed) {
    MigrationContext.identitiesCleanupConnectionFailed = identitiesCleanupConnectionFailed;
  }

  public static Set<String> getIdentitiesCleanupActivityFailed() {
    return identitiesCleanupActivityFailed;
  }

  public static void setIdentitiesCleanupActivityFailed(Set<String> identitiesCleanupActivityFailed) {
    MigrationContext.identitiesCleanupActivityFailed = identitiesCleanupActivityFailed;
  }

  public static Set<String> getIdentitiesCleanupFailed() {
    return identitiesCleanupFailed;
  }

  public static void setIdentitiesCleanupFailed(Set<String> identitiesCleanupFailed) {
    MigrationContext.identitiesCleanupFailed = identitiesCleanupFailed;
  }

  public static Set<String> getSpaceCleanupFailed() {
    return spaceCleanupFailed;
  }

  public static void setSpaceCleanupFailed(Set<String> spaceCleanupFailed) {
    MigrationContext.spaceCleanupFailed = spaceCleanupFailed;
  }

  public static boolean isForceCleanup() {
    return forceCleanup;
  }

  public static void setForceCleanup(boolean forceCleanup) {
    MigrationContext.forceCleanup = forceCleanup;
  }
}
