package org.exoplatform.social.core.jpa.updater;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class SpaceMemberAnyMembershipUpgradePlugin extends UpgradeProductPlugin {
  private static final Log    LOG             = ExoLogger.getLogger(SpaceMemberAnyMembershipUpgradePlugin.class);

  private final int           BUFFER          = 50;

  private final int           THREAD          = 10;

  private PortalContainer     portalContainer;

  private SpaceService        spaceService;

  private OrganizationService organizationService;

  private ExecutorService     executorService = Executors.newCachedThreadPool();

  public SpaceMemberAnyMembershipUpgradePlugin(PortalContainer portalContainer,
                                               OrganizationService organizationService,
                                               SpaceService spaceService,
                                               InitParams initParams) {
    super(initParams);
    this.portalContainer = portalContainer;
    this.organizationService = organizationService;
    this.spaceService = spaceService;
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    // start the migration
    LOG.info("=== Start IDM Membership '*'  to Space Entity migration");
    long startTime = System.currentTimeMillis();

    MigrationResult migrationResult = new MigrationResult();
    RequestLifeCycle.begin(portalContainer);
    try {
      boolean migrationSuccessfullyProceeded = migrate(migrationResult);
      if (!migrationSuccessfullyProceeded || migrationResult.getErrorCount() > 0) {
        throw new IllegalStateException("=== Error while checking the consistency of spaces members. "
            + migrationResult.getSuccessCount() + "/" + migrationResult.getTotalEntities() + " memberships has been fixed in "
            + (System.currentTimeMillis() - startTime) + "ms. Errors count of Memberships migration = "
            + migrationResult.getErrorCount() + ". Total scanned spaces count = " + migrationResult.getSpacesCount());
      }
      if (migrationResult.getTotalEntities() == 0) {
        LOG.info("=== End of consistency check of space members in {}ms. No inconsistency detected. Total scanned spaces count = {}",
                 System.currentTimeMillis() - startTime,
                 migrationResult.getSpacesCount());
      } else {
        LOG.info("=== End of consistency check of space members in {}ms. {}/{} memberships has been fixed. Total scanned spaces count = {}",
                 System.currentTimeMillis() - startTime,
                 migrationResult.getSuccessCount(),
                 migrationResult.getTotalEntities(),
                 migrationResult.getSpacesCount());
      }
    } catch (Exception ex) {
      LOG.error("=== Error while checking the consistency of spaces members. " + migrationResult.getSuccessCount() + "/"
          + migrationResult.getTotalEntities() + " memberships has been fixed in " + (System.currentTimeMillis() - startTime)
          + "ms. Error count of Memberships migration = " + migrationResult.getErrorCount(), ex);
      throw new RuntimeException("Error while checking the consistency of spaces members", ex);
    } finally {
      RequestLifeCycle.end();
      executorService.shutdown();
    }
  }

  private boolean migrate(MigrationResult migrationResult) throws Exception {
    ListAccess<Space> allSpacesListAccess = spaceService.getAllSpacesWithListAccess();
    int spacesCount = allSpacesListAccess.getSize();
    if (spacesCount == 0) {
      return true;
    }

    // Load all spaces instead of using offset to split treatment between Thread
    // Because this upgrade is made asynchronously, thus a new space can be
    // added
    // while the upgrade is in progress, so some spaces could be ignored by the
    // upgrade
    List<Space> allSpaces = getAllSpaces(allSpacesListAccess, spacesCount);
    spacesCount = allSpaces.size();

    int numberOfThreads = spacesCount >= THREAD ? THREAD : spacesCount;
    int numberOfSpacesPerThreads = (int) Math.ceil((double) spacesCount / (double) numberOfThreads);

    migrationResult.setSpacesCount(spacesCount);

    List<Future<Boolean>> futures = new ArrayList<>();
    for (int i = 0; i < numberOfThreads; i++) {
      int fromIndex = numberOfSpacesPerThreads * i;
      if(fromIndex > spacesCount){
        break;
      }
      int toIndex = fromIndex + numberOfSpacesPerThreads;
      toIndex = toIndex > spacesCount ? spacesCount : toIndex;
      List<Space> spaces = allSpaces.subList(fromIndex, toIndex);
      Future<Boolean> future = executorService.submit(new SpaceMigrationCallable(spaces, migrationResult));
      futures.add(future);
    }

    boolean migrationCompletedSuccessfully = true;
    for (Future<Boolean> future : futures) {
      migrationCompletedSuccessfully = migrationCompletedSuccessfully && future.get();
    }
    return migrationCompletedSuccessfully;
  }

  private List<Space> getAllSpaces(ListAccess<Space> allSpacesListAccess, int spacesCount) throws Exception {
    List<Space> allSpaces = new ArrayList<>();
    int offset = 0;
    Space[] spaces = null;
    do {
      spaces = allSpacesListAccess.load(offset, BUFFER);
      if (spaces == null) {
        break;
      }
      offset += spaces.length;
      for (Space space : spaces) {
        if (space == null || StringUtils.isBlank(space.getGroupId())) {
          continue;
        }
        allSpaces.add(space);
      }
    } while (offset < spacesCount || spaces.length == 0);
    return allSpaces;
  }

  private void migrateSpace(Space space, MigrationResult migrationResult) throws Exception {
    LOG.info("Checking members of space '{}'", space.getDisplayName());

    ListAccess<Membership> memberships = getMembershipByGroup(space.getGroupId());
    int membershipsSize = memberships.getSize();
    //
    int fromId = 0;
    int pageSize = membershipsSize > BUFFER ? BUFFER : membershipsSize;
    int totalMigratedMemberships = 0;
    int errorMembershipsMigration = 0;

    while (pageSize > 0) {
      RequestLifeCycle.begin(portalContainer);
      try {
        for (Membership m : memberships.load(fromId, pageSize)) {
          if (m == null) {
            continue;
          }
          if (MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.equalsIgnoreCase(m.getMembershipType())) {
            migrationResult.incrementTotalEntities();
            LOG.info("Start migrating {}", m.toString());

            String username = m.getUserName();
            boolean isManager = spaceService.isManager(space, username);
            boolean isMember = spaceService.isMember(space, username);
            if (isManager && isMember) {
              LOG.debug("User '{}' belongs to space's group using '*' membershipType but he's already a space member and manager of the space '{}'. No migration is needed",
                        username,
                        space.getDisplayName());
              continue;
            }

            try {
              if (!isMember) {
                addMember(space, username);
              }
            } catch (Exception e) {
              LOG.error("Error while setting user " + username + " as member of space " + space.getDisplayName(), e);
              migrationResult.incrementErrorCount();
              errorMembershipsMigration++;
              continue;
            }

            try {
              if (!isManager) {
                setManager(space, username);
              }
            } catch (Exception e) {
              LOG.error("Error while setting user " + username + " as manager of space " + space.getDisplayName(), e);
              migrationResult.incrementErrorCount();
              errorMembershipsMigration++;
              continue;
            }

            migrationResult.incrementSuccessCount();
            totalMigratedMemberships++;
          }
        }
      } catch (Exception e) {
        migrationResult.incrementErrorCount();
        errorMembershipsMigration++;
        LOG.error("error during migrate membership at index {}, page {}", fromId, pageSize);
        throw e;
      } finally {
        RequestLifeCycle.end();
      }

      LOG.info("Check space members is in progress, percentage = {}%",
               ((int) ((totalMigratedMemberships * 100) / membershipsSize)));

      fromId += pageSize;
      pageSize = membershipsSize - fromId;
      pageSize = pageSize > BUFFER ? BUFFER : pageSize;
    }
    if (totalMigratedMemberships == 0) {
      if (errorMembershipsMigration > 0) {
        LOG.warn("No inconsistencies detected for space '{}'. Total checked memberships of space = {}. Memberships checks error = {} ",
                 space.getDisplayName(),
                 membershipsSize,
                 errorMembershipsMigration);
      } else {
        LOG.info("No inconsistencies detected for space '{}'. Total checked memberships of space = {}.",
                 space.getDisplayName(),
                 membershipsSize);
      }
    } else {
      if (errorMembershipsMigration > 0) {
        LOG.warn("{} inconsistencies has been fixed for space '{}'. Total checked memberships of space = {}. Memberships checks error = {}",
                 totalMigratedMemberships,
                 space.getDisplayName(),
                 membershipsSize,
                 errorMembershipsMigration);
      } else {
        LOG.info("{} inconsistencies has been fixed for space '{}'. Total checked memberships of space = {}",
                 totalMigratedMemberships,
                 space.getDisplayName(),
                 membershipsSize);
      }
    }
  }

  private void setManager(Space space, String userId) {
    String[] managers = space.getManagers();
    if (!ArrayUtils.contains(managers, userId)) {
      managers = (String[]) ArrayUtils.add(managers, userId);
      space.setManagers(managers);
      spaceService.updateSpace(space);
      SpaceUtils.addUserToGroupWithManagerMembership(userId, space.getGroupId());
    }
  }

  private void addMember(Space space, String userId) {
    String[] members = space.getMembers();
    space = removeInvited(space, userId);
    space = removePending(space, userId);
    if (!ArrayUtils.contains(members, userId)) {
      members = (String[]) ArrayUtils.add(members, userId);
      space.setMembers(members);
      spaceService.updateSpace(space);
      SpaceUtils.addUserToGroupWithMemberMembership(userId, space.getGroupId());
    }
  }

  private Space removePending(Space space, String userId) {
    String[] pendingUsers = space.getPendingUsers();
    if (ArrayUtils.contains(pendingUsers, userId)) {
      pendingUsers = (String[]) ArrayUtils.removeElement(pendingUsers, userId);
      space.setPendingUsers(pendingUsers);
    }
    return space;
  }

  private Space removeInvited(Space space, String userId) {
    String[] invitedUsers = space.getInvitedUsers();
    if (ArrayUtils.contains(invitedUsers, userId)) {
      invitedUsers = (String[]) ArrayUtils.removeElement(invitedUsers, userId);
      space.setInvitedUsers(invitedUsers);
    }
    return space;
  }

  private ListAccess<Membership> getMembershipByGroup(String groupId) throws Exception {
    Group group = organizationService.getGroupHandler().findGroupById(groupId);
    return organizationService.getMembershipHandler().findAllMembershipsByGroup(group);
  }

  public final class SpaceMigrationCallable implements Callable<Boolean> {
    List<Space>     spaces          = null;

    MigrationResult migrationResult = null;

    public SpaceMigrationCallable(List<Space> spaces, MigrationResult migrationResult) {
      this.spaces = spaces;
      this.migrationResult = migrationResult;
    }

    @Override
    public Boolean call() throws Exception {
      ExoContainerContext.setCurrentContainer(portalContainer);
      boolean migrationProceededWithoutErrors = true;
      for (Space space : spaces) {
        if (space == null || StringUtils.isBlank(space.getGroupId())) {
          continue;
        }
        RequestLifeCycle.begin(portalContainer);
        try {
          //
          migrateSpace(space, migrationResult);
        } catch (Exception ex) {
          LOG.error("Error during migrate memberships of group " + space.getGroupId(), ex);
          migrationProceededWithoutErrors = false;
        } finally {
          RequestLifeCycle.end();
        }
      }
      return migrationProceededWithoutErrors;
    }
  }

  public static class MigrationResult {
    int errorCount    = 0;

    int successCount  = 0;

    int totalEntities = 0;

    int spacesCount = 0;

    public void incrementErrorCount() {
      this.errorCount++;
    }

    public int getErrorCount() {
      return errorCount;
    }

    public int getSuccessCount() {
      return successCount;
    }

    public void incrementSuccessCount() {
      this.successCount++;
    }

    public void setTotalEntities(int totalEntities) {
      this.totalEntities = totalEntities;
    }

    public void incrementTotalEntities() {
      this.totalEntities++;
    }

    public int getTotalEntities() {
      return totalEntities;
    }

    public int getSpacesCount() {
      return spacesCount;
    }

    public void setSpacesCount(int spacesCount) {
      this.spacesCount = spacesCount;
    }

  }
}
