package org.exoplatform.social.core.jpa.updater;

import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.DatabaseException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipTypeHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.idm.PicketLinkIDMService;
import org.exoplatform.services.organization.idm.PicketLinkIDMServiceImpl;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;
import org.picketlink.idm.spi.configuration.metadata.*;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.concurrent.*;

public class AnyMembershipMigration implements CustomTaskChange {
  private static final Log LOG = ExoLogger.getLogger(AnyMembershipMigration.class);
  private static final String GROUP_QUERY = "select i.ID, i.NAME from jbid_io i inner join jbid_realm r on i.REALM = r.ID inner join jbid_io_type t on i.IDENTITY_TYPE = t.ID where t.NAME = ? AND r.NAME = ?";
  private static final String GROUP_ID = "ID";
  private static final String GROUP_NAME = "NAME";
  private static final String USER_NAME = "NAME";
  private static final String SPACE_ID = "SPACE_ID";
  private static final String TYPE_SPACE = ".spaces";
  private static final String INSERT_SPACE_MEMBER = "insert into SOC_SPACES_MEMBERS(SPACE_ID, USER_ID, STATUS, LAST_ACCESS, VISITED) VALUES(?,?,?,?,?)";
  private static final String USER_IN_GROUP_QUERY = "select i.NAME from jbid_io i INNER JOIN jbid_io_rel r on i.ID = r.TO_IDENTITY INNER JOIN jbid_io_rel_name n on r.NAME = n.ID where n.NAME = '*' AND r.FROM_IDENTITY = ?";
  private static final String SPACE_QUERY = "select SPACE_ID from SOC_SPACES where GROUP_ID = ?";

  private PreparedStatement userStm;
  private PreparedStatement insertStm;
  private PreparedStatement spaceStm;

  private final int BUFFER = 50;

  private final int THREAD = 10;

  private ExecutorService pool = Executors.newFixedThreadPool(THREAD);

  @Override
  public void execute(Database database) throws CustomChangeException {
    // start the migration
    LOG.info("=== Start Social Membership * migration");
    long startTime = System.currentTimeMillis();

    try {
      boolean isHibernate = checkIDM();

      int[] result;
      if (isHibernate) {
        result = migrateJDBC(database);
      } else {
        Future<int[]> future = pool.submit(migrate());
        result = future.get();
      }

      LOG.info("=== End Social Membership * {} memberships migrated successfully, {} errors, in {} miliseconds",
          result[0], result[1], System.currentTimeMillis() - startTime);
    } catch (Exception ex) {
      LOG.error("error during migrate social membership *", ex);
      throw new CustomChangeException(ex);
    }
  }

  private int[] migrateJDBC(Database database) throws Exception {
    RequestLifeCycle.begin(PortalContainer.getInstance());
    JdbcConnection dbConn = (JdbcConnection) database.getConnection();
    boolean autoCommit = dbConn.getAutoCommit();

    try {
      Session idmConn = getIDMConn();
      return idmConn.doReturningWork(new ReturningWork<int[]>() {
        @Override
        public int[] execute(Connection idmConn) throws SQLException {
          int count = 0;
          int error = 0;
          int buffer = 0;
          ResultSet groupSet = null;

          try {
            dbConn.setAutoCommit(false);
            insertStm = dbConn.prepareStatement(INSERT_SPACE_MEMBER);
            userStm = idmConn.prepareStatement(USER_IN_GROUP_QUERY);
            spaceStm = dbConn.prepareStatement(SPACE_QUERY);

            groupSet = getGroupSet(idmConn);
            while (groupSet.next()) {
              long groupId = groupSet.getLong(GROUP_ID);
              String groupName = groupSet.getString(GROUP_NAME);

              Long spaceId = getSpaceId("/spaces/" + groupName);
              if (spaceId == null) {
                LOG.warn("Not found space for group {}", groupName);
                continue;
              }
              //
              LOG.info("Migrating for space {}", "/spaces/" + groupName);

              Set<String> usernames = getUserInGroup(groupId);
              //
              LOG.info("There are {} user with membership * in group", usernames.size());
              for (String username : usernames) {
                insertStm.clearParameters();
                insertStm.setLong(1, spaceId);
                insertStm.setString(2, username);
                insertStm.setInt(3, 0);
                insertStm.setDate(4, new Date(86400000L));
                insertStm.setBoolean(5, false);
                try {
                  insertStm.executeUpdate();
                  insertStm.setInt(3, 1);
                  insertStm.executeUpdate();
                  count++;
                  if (++buffer == BUFFER) {
                    LOG.info("commiting...");
                    dbConn.commit();
                    buffer = 0;
                  }
                } catch (Exception ex) {
                  error++;
                  LOG.error("Error during migrating for user {}", username, ex);
                  throw ex;
                }
              }
            }
          } catch (Exception ex) {
            throw new SQLException("Error during migrate jdbc", ex);
          } finally {
            try {
              groupSet.close();
            } catch (SQLException e) {
              LOG.warn("Error during close ResultSet  - Cause : " + e.getMessage(), e);
            }

            try {
              if (buffer > 0) {
                LOG.info("commiting...remain");
                dbConn.commit();
              }
              dbConn.setAutoCommit(autoCommit);
            } catch (DatabaseException e) {
              LOG.warn("Error during set AutoCommit  - Cause : " + e.getMessage(), e);
            }
          }

          return new int[] {count, error};
        }
      });
    } finally {
      RequestLifeCycle.end();
    }
  }

  private Long getSpaceId(String groupId) throws Exception {
    ResultSet spaceSet = null;
    try {
      spaceStm.clearParameters();
      spaceStm.setString(1, groupId);

      spaceSet = spaceStm.executeQuery();
      if (spaceSet.next()) {
        return spaceSet.getLong(SPACE_ID);
      }
    } finally {
      try {
        spaceSet.close();
      } catch (SQLException e) {
        LOG.error("Error during close ResultSet  - Cause : " + e.getMessage(), e);
      }
    }
    return -1L;
  }

  private Set<String> getUserInGroup(long groupId) throws Exception {
    Set<String> usernames = new HashSet<>();
    ResultSet userSet = null;
    try {
      userStm.clearParameters();
      userStm.setLong(1, groupId);

      userSet = userStm.executeQuery();
      while (userSet.next()) {
        usernames.add(userSet.getString(USER_NAME));
      }
    } finally {
      try {
        userSet.close();
      } catch (SQLException e) {
        LOG.error("Error during close ResultSet  - Cause : " + e.getMessage(), e);
      }
    }
    return usernames;
  }

  private ResultSet getGroupSet(Connection idmConn) throws Exception {
    PicketLinkIDMService idmService = CommonsUtils.getService(PicketLinkIDMService.class);
    String realmName = ((PicketLinkIDMServiceImpl) idmService).getRealmName();

    PreparedStatement groupQueryStm = idmConn.prepareStatement(GROUP_QUERY);
    groupQueryStm.setString(1, TYPE_SPACE);
    groupQueryStm.setString(2, realmName);
    return groupQueryStm.executeQuery();
  }

  private boolean checkIDM() throws Exception {
    PicketLinkIDMService idmService = CommonsUtils.getService(PicketLinkIDMService.class);
    if (idmService instanceof PicketLinkIDMServiceImpl) {
      IdentityConfigurationMetaData config = ((PicketLinkIDMServiceImpl)idmService).getConfigMD();

      IdentityRepositoryConfigurationMetaData repo = getRepo(config);
      if (repo != null) {
        List<IdentityStoreConfigurationMetaData> stores = config.getIdentityStores();

        String dfStoreName = repo.getDefaultIdentityStoreId();
        IdentityStoreConfigurationMetaData dfStore = getStore(dfStoreName, stores);
        if (dfStore != null) {
          if (checkClass(dfStore.getClassName(),
              Arrays.asList("org.picketlink.idm.impl.store.hibernate.PatchedHibernateIdentityStoreImpl",
                  "org.picketlink.idm.impl.store.hibernate.HibernateIdentityStoreImpl"))) {

            List<IdentityStoreMappingMetaData> storeMappings = repo.getIdentityStoreToIdentityObjectTypeMappings();
            if (storeMappings == null || storeMappings.size() == 0) {
              return true;
            } else {
              boolean spaceMap = false;
              for (IdentityStoreMappingMetaData storeMap : storeMappings) {
                for (IdentityStoreConfigurationMetaData store : stores) {
                  if (store.getId().equals(storeMap.getIdentityStoreId())) {
                    if (checkClass(store.getClassName(),
                        Arrays.asList("org.picketlink.idm.impl.store.ldap.LDAPIdentityStoreImpl"))) {
                      List<IdentityObjectTypeMetaData> identityTypes = store.getSupportedIdentityTypes();
                      for (IdentityObjectTypeMetaData type : identityTypes) {
                        for (String typeMap : storeMap.getIdentityObjectTypeMappings()) {
                          if (type.getName().equals(typeMap)) {
                            List<String> options = type.getOption("ctxDNs");
                            if (options != null) {
                              for (String option : options) {
                                if (option.toLowerCase().contains("spaces")) {
                                  spaceMap = true;
                                  break;
                                }
                              }
                            }
                          }
                        }
                      }
                    } else {
                      return false;
                    }
                  }
                }
              }
              return !spaceMap;
            }
          }
        }
      }
    }
    return false;
  }

  private IdentityStoreConfigurationMetaData getStore(String dfStoreName, List<IdentityStoreConfigurationMetaData> stores) {
    for (IdentityStoreConfigurationMetaData store : stores) {
      if (store.getId().equals(dfStoreName)) {
        return store;
      }
    }
    return null;
  }

  private IdentityRepositoryConfigurationMetaData getRepo(IdentityConfigurationMetaData config) {
    PicketLinkIDMService idmService = CommonsUtils.getService(PicketLinkIDMService.class);
    if (idmService instanceof PicketLinkIDMServiceImpl) {
      String realmName = ((PicketLinkIDMServiceImpl) idmService).getRealmName();

      List<RealmConfigurationMetaData> realms = config.getRealms();
      for (RealmConfigurationMetaData realm : realms) {
        if (realmName.startsWith(realm.getId())) {
          String repoName = realm.getIdentityRepositoryIdRef();
          //
          List<IdentityRepositoryConfigurationMetaData> repos = config.getRepositories();
          for (IdentityRepositoryConfigurationMetaData repo : repos) {
            if (repo.getId().equals(repoName)) {
              return repo;
            }
          }
        }
      }
    }
    return null;
  }

  private boolean checkClass(String className, List<String> list) throws Exception {
    Class<?> clazz = Class.forName(className);
    do {
      String name = clazz.getName();
      if (list.contains(name)) {
        return true;
      }
      clazz = clazz.getSuperclass();
    } while (clazz != null);

    return false;
  }

  private Callable<int[]> migrate() {
    return new Callable<int[]>() {
      @Override
      public int[] call() throws Exception {
        try {
          int count = 0;
          int error = 0;
          Collection<Group> groups = getAllGroup();

          List<Future<int[]>> results = new ArrayList<>();
          for (Group group : groups) {
            if (group.getId().startsWith(SpaceUtils.SPACE_GROUP)) {
              //
              results.add(pool.submit(migrateGroup(group)));
            }
          }

          for (Future<int[]> result : results) {
            int[] r = result.get(10, TimeUnit.MINUTES);
            count += r[0];
            error += r[1];
          }

          return new int[]{count, error};
        } catch (Exception ex) {
          LOG.error("error during migrate memberships", ex);
          throw new CustomChangeException(ex);
        }
      }
    };
  }

  private Callable<int[]> migrateGroup(Group group) {
    return new Callable<int[]>() {
      @Override
      public int[] call() throws Exception {
        int count = 0;
        int error = 0;
        SpaceService spaceService = CommonsUtils.getService(SpaceService.class);

        Space space = getSpaceByGroupId(group.getId());
        if (space == null) {
          LOG.info("ignore {} group, no space found", group.getId());
          return new int[] {0, 0};
        }

        //
        LOG.info("Migrating group {}", group.getId());

        ListAccess<Membership> memberships = getMembershipByGroup(group);
        int size = memberships.getSize();
        //
        int fromId = 0;
        int pageSize = size > BUFFER ? BUFFER : size;

        while (pageSize > 0) {
          try {
            RequestLifeCycle.begin(PortalContainer.getInstance());

            for (Membership m : memberships.load(fromId, pageSize)) {
              if (MembershipTypeHandler.ANY_MEMBERSHIP_TYPE.equalsIgnoreCase(m.getMembershipType())) {
                LOG.info("Start migrating {}", m.toString());
                String username = m.getUserName();

                if (!spaceService.isManager(space, username)) {
                  if (!spaceService.isMember(space, username)) {
                    spaceService.addMember(space, username);
                  }
                  spaceService.setManager(space, username, true);
                  count++;
                }
              }
            }
          } catch (Exception e) {
            error++;
            LOG.error("error during migrate membership at index {}, page {}", fromId, pageSize);
            throw e;
          } finally {
            RequestLifeCycle.end();
            LOG.info("finish migrating in thread {}", Thread.currentThread().getId());
          }

          fromId += pageSize;
          pageSize = size - fromId;
          pageSize = pageSize > BUFFER ? BUFFER : pageSize;
        }

        return new int[] {count, error};
      }
    };
  }

  private ListAccess<Membership> getMembershipByGroup(Group group) throws Exception {
    try {
      RequestLifeCycle.begin(PortalContainer.getInstance());
      //
      OrganizationService orgService = CommonsUtils.getService(OrganizationService.class);

      ListAccess<Membership> memberships = orgService.getMembershipHandler().findAllMembershipsByGroup(group);
      LOG.info("There are {} memberships", memberships.getSize());

      return memberships;
    } finally {
      RequestLifeCycle.end();
    }
  }

  private Space getSpaceByGroupId(String groupId) {
    try {
      RequestLifeCycle.begin(PortalContainer.getInstance());
      //
      SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
      Space space = spaceService.getSpaceByGroupId(groupId);
      return space;
    } finally {
      RequestLifeCycle.end();
    }
  }

  private Collection<Group> getAllGroup() throws Exception {
    try {
      RequestLifeCycle.begin(PortalContainer.getInstance());
      //
      OrganizationService orgService = CommonsUtils.getService(OrganizationService.class);
      Collection<Group> groups = orgService.getGroupHandler().getAllGroups();
      LOG.info("There are {} groups", groups.size());
      return groups;
    } finally {
      RequestLifeCycle.end();
    }
  }

  @Override
  public String getConfirmationMessage() {
    return "ANY memberships migrated as social space managers";
  }

  @Override
  public void setUp() throws SetupException {

  }

  @Override
  public void setFileOpener(ResourceAccessor resourceAccessor) {

  }

  @Override
  public ValidationErrors validate(Database database) {
    return null;
  }

  public Session getIDMConn() {
    PicketLinkIDMService idmService = CommonsUtils.getService(PicketLinkIDMService.class);
    if (idmService instanceof PicketLinkIDMServiceImpl) {
      return ((PicketLinkIDMServiceImpl)idmService).getHibernateService().openSession();
    } else {
      return null;
    }
  }
}
