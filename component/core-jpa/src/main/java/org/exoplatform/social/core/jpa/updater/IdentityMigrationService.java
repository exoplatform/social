/*
 * Copyright (C) 2019 eXo Platform SAS.
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

package org.exoplatform.social.core.jpa.updater;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.Session;

import org.chromattic.ext.ntdef.NTFile;
import org.chromattic.ext.ntdef.Resource;

import org.exoplatform.commons.api.event.EventManager;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.jpa.storage.RDBMSIdentityStorageImpl;
import org.exoplatform.social.core.chromattic.entity.DisabledEntity;
import org.exoplatform.social.core.chromattic.entity.IdentityEntity;
import org.exoplatform.social.core.chromattic.entity.ProfileEntity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.storage.RDBMSSpaceStorageImpl;
import org.exoplatform.social.core.jpa.updater.utils.IdentityUtil;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.exception.NodeNotFoundException;
import org.exoplatform.social.core.storage.impl.IdentityStorageImpl;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
@Managed
@ManagedDescription("Social migration Identities from JCR to RDBMS.")
@NameTemplate({@Property(key = "service", value = "social"), @Property(key = "view", value = "migration-identities") })
public class IdentityMigrationService extends AbstractMigrationService<Identity> {

  public static final String EVENT_LISTENER_KEY = "SOC_IDENTITY_MIGRATION";

  protected final static String REMOVE_LIMIT_THRESHOLD_KEY = "REMOVE_LIMIT_THRESHOLD";

  private int REMOVE_LIMIT_THRESHOLD = 20;

  private long totalNumberIdentites = 0;

  private final RDBMSIdentityStorageImpl identityStorage;
  private final IdentityStorageImpl jcrIdentityStorage;

  private SpaceStorage spaceStorage;

  private String identityQuery;

  private Set<String> identitiesMigrateFailed = new HashSet<>();
  private Set<String> identitiesCleanupFailed = new HashSet<>();

  public IdentityMigrationService(InitParams initParams,
                                  RDBMSIdentityStorageImpl identityStorage,
                                  IdentityStorageImpl jcrIdentityStorage,
                                  RDBMSSpaceStorageImpl spaceStorage,
                                  EventManager<Identity, String> eventManager, EntityManagerService entityManagerService) {
    super(initParams, jcrIdentityStorage, eventManager, entityManagerService);
    this.LIMIT_THRESHOLD = getInteger(initParams, LIMIT_THRESHOLD_KEY, 200);
    this.REMOVE_LIMIT_THRESHOLD = getInteger(initParams, REMOVE_LIMIT_THRESHOLD_KEY, 20);
    this.identityStorage = identityStorage;
    this.jcrIdentityStorage = jcrIdentityStorage;
    this.spaceStorage = spaceStorage;
  }

  @Override
  protected void beforeMigration() throws Exception {
    MigrationContext.setIdentityDone(false);
    identitiesMigrateFailed = new HashSet<>();
  }

  @Override
  @Managed
  @ManagedDescription("Manual to start run migration data of identities from JCR to RDBMS.")
  public void doMigration() throws Exception {
    long t = System.currentTimeMillis();

    long totalIdentities = getTotalNumberIdentities();

    //endTx(begunTx);

    LOG.info("|\\ START::Identity migration ---------------------------------");

    RequestLifeCycle.end();

    long offset = 0;
    boolean cont = true;
    boolean begunTx = false;
    List<String> transactionList = new ArrayList<>();

    long numberSuccessful = 0;
    long batchSize = 0;

    while(cont && !forceStop) {
      try {

        try {

          RequestLifeCycle.begin(PortalContainer.getInstance());
          begunTx = startTx();
          transactionList = new ArrayList<>();
          NodeIterator nodeIter = getIdentityNodes(offset, LIMIT_THRESHOLD);

          if (nodeIter == null || nodeIter.getSize() == 0) {
            cont = false;

          } else {
            batchSize = nodeIter.getSize();
            while (nodeIter.hasNext() && !forceStop) {
              offset++;
              Node identityNode = nodeIter.nextNode();
              String identityName = identityNode.getName();
              transactionList.add(identityName);

              LOG.info(String.format("|  \\ START::identity number: %s/%s (%s identity)", offset, totalIdentities, identityName));
              long t1 = System.currentTimeMillis();

              String jcrid = identityNode.getUUID();
              if((identityNode.hasProperty("soc:providerId") && identityNode.getProperty("soc:providerId").getString().equals(OrganizationIdentityProvider.NAME)) ||
                !identityNode.hasProperty("soc:isDeleted") || !identityNode.getProperty("soc:isDeleted").getBoolean()) {
                //SOC-5828 : if identity is user, we migrated it even if the user is deleted
                //if identity is space and deleted, we don't migrated it
                try {
                  Identity identity = migrateIdentity(identityNode, jcrid);

                  if (identity != null) {
                    String newId = identity.getId();
                    identity.setId(jcrid);
                    broadcastListener(identity, newId);
                  }
                  numberSuccessful++;
                } catch (Exception ex) {
                  identitiesMigrateFailed.add(identityName);
                  LOG.error("Error while migrate identity " + identityName, ex);
                }
              } else {
                LOG.info("Ignore migration of identity with id {} since it's marked as deleted", identityName);
              }
              LOG.info(String.format("|  / END::identity number %s (%s identity) consumed %s(ms)", offset, identityNode.getName(), System.currentTimeMillis() - t1));
            }
          }

        } finally {
          try {
            endTx(begunTx);
          } catch (Exception ex) {
            // If commit was failed, all identities are failed also
            identitiesMigrateFailed.addAll(transactionList);
            numberSuccessful -= batchSize;
          }
          RequestLifeCycle.end();
        }
      } catch (Exception ex) {
        LOG.error("Exception while migrating identity ", ex);
      }
    }

    if (identitiesMigrateFailed.size() > 0) {
      LOG.info(String.format("| / END::Identity migration failed for (%s) identity(s)", identitiesMigrateFailed.size()));
    }
    LOG.info(String.format("|// END::Identity migration for (%s) identity(s) consumed %s(ms)", numberSuccessful, System.currentTimeMillis() - t));

    LOG.info("| \\ START::Re-indexing identity(s) ---------------------------------");
    IndexingService indexingService = CommonsUtils.getService(IndexingService.class);
    indexingService.reindexAll(ProfileIndexingServiceConnector.TYPE);
    LOG.info("| / END::Re-indexing identity(s) ---------------------------------");

    RequestLifeCycle.begin(PortalContainer.getInstance());
  }

  @Override
  protected void afterMigration() throws Exception {
    MigrationContext.setIdentitiesMigrateFailed(identitiesMigrateFailed);
    if (!forceStop && identitiesMigrateFailed.isEmpty()) {
      MigrationContext.setIdentityDone(true);
    }
  }

  @Override
  public void doRemove() throws Exception {
    identitiesCleanupFailed = new HashSet<>();

    long totalIdentities = getTotalNumberIdentities();

    LOG.info("| \\ START::cleanup Identities ---------------------------------");
    long t = System.currentTimeMillis();
    long timePerIdentity = System.currentTimeMillis();
    int offset = 0;
    long failed = 0;
    List<String> transactionList = new ArrayList<>();

    try {
      boolean cont = true;
      while (cont) {
        try {

          RequestLifeCycle.begin(PortalContainer.getInstance());
          failed = identitiesCleanupFailed.size();
          transactionList = new ArrayList<>();

          NodeIterator nodeIter  = getIdentityNodes(failed, REMOVE_LIMIT_THRESHOLD);
          if(nodeIter == null || nodeIter.getSize() == 0) {
            cont = false;

          } else {

            if (nodeIter.getSize() < REMOVE_LIMIT_THRESHOLD) {
              cont = false;
            }
            while (nodeIter.hasNext()) {
              offset++;

              if (offset > totalIdentities) {
                cont = false;
              }

              Node node = nodeIter.nextNode();
              timePerIdentity = System.currentTimeMillis();
              LOG.info(String.format("|  \\ START::cleanup Identity number: %s/%s (%s identity)", offset, totalIdentities, node.getName()));

              String name = node.getName();
              if (!MigrationContext.isForceCleanup() && (MigrationContext.getIdentitiesCleanupConnectionFailed().contains(name)
                      || MigrationContext.getIdentitiesCleanupActivityFailed().contains(name))) {
                identitiesCleanupFailed.add(name);
                LOG.warn("Will not remove this identity because the cleanup connection or activities were failed for it");
                continue;
              }

              IdentityEntity identityEntity = _findById(IdentityEntity.class, node.getUUID());
              String provider = identityEntity.getProviderId();
              String activityMigrated = identityEntity.getProperty(MigrationContext.KEY_MIGRATE_ACTIVITIES);
              String connectionMigrated = identityEntity.getProperty(MigrationContext.KEY_MIGRATE_CONNECTION);
              if (!MigrationContext.TRUE_STRING.equalsIgnoreCase(activityMigrated)
                      || (OrganizationIdentityProvider.NAME.equals(provider) && !MigrationContext.TRUE_STRING.equalsIgnoreCase(connectionMigrated))) {
                LOG.warn("Can not remove identity " + name + " due to migration was not successful for activities and connections");
                identitiesCleanupFailed.add(name);
                continue;
              }

              //transactionList.add(name);

              try {
                PropertyIterator pit = node.getReferences();
                if (pit != null && pit.getSize() > 0) {
                  int num = 0;
                  while (pit.hasNext()) {
                    num++;
                    pit.nextProperty().remove();
                    if (num % REMOVE_LIMIT_THRESHOLD == 0) {
                      getSession().save();
                    }
                  }
                  getSession().save();
                }
                node.remove();
                getSession().save();
              } catch (Exception ex) {
                LOG.error("Error when cleanup the identity: " + name, ex);
                identitiesCleanupFailed.add(name);
                // Discard all change if there is any error
                getSession().getJCRSession().refresh(false);
              }

              LOG.info(String.format("|  / END::cleanup (%s identity) consumed time %s(ms)", node.getName(), System.currentTimeMillis() - timePerIdentity));
            }
          }

        } finally {
          RequestLifeCycle.end();
        }
      }

    } finally {
      MigrationContext.setIdentitiesCleanupFailed(identitiesCleanupFailed);
      if (identitiesCleanupFailed.size() > 0) {
        LOG.warn("Cleanup failed for " + identitiesCleanupFailed.size() + " identities");
      }
      LOG.info(String.format("| / END::cleanup Identities migration for (%s) identity consumed %s(ms)", offset, System.currentTimeMillis() - t));
    }
  }

  private Identity migrateIdentity(Node node, String jcrId) throws Exception {
    String providerId = node.getProperty("soc:providerId").getString();
    // The node name is the identity id.
    // Node name is soc:<name>, only the <name> is relevant
    // Node name should equals to remoteId on all identities
    String name = IdentityUtil.getIdentityName(node.getName());
    String remoteId = node.getProperty("soc:remoteId").getString();

    if (!name.equals(remoteId)) {
      LOG.info(String.format("Node name(%s) does not equals to remoteId(%s), need to adjust and make them equally before migrate", name, remoteId));
      boolean needUpdateRemoteId = true;

      if (SpaceIdentityProvider.NAME.equals(providerId)) {
        Space space = spaceStorage.getSpaceByPrettyName(name);
        if (space == null) {
          space = spaceStorage.getSpaceByPrettyName(remoteId);
          // If we can not find the space for this identity, we could ignore to migrate
          if (space == null) {
            LOG.warn("The space with prettyName=" + remoteId + " does not exists, this identity will not be migrated.");
            return null;
          } else {
            needUpdateRemoteId = false;
            name = remoteId;
          }
        }
      }

      if (needUpdateRemoteId) {
        // Node name is more accurate in case user identity. Will update the remoteId and make it the same
        LOG.info("Update remoteId to " + name + " to make it equals to node name");
        node.setProperty("soc:remoteId", name);
        node.getSession().save();

      } else {
        // RemoteId is more accurate in case space identity and we found a space for this remoteName, we have to update node name to remoteId
        LOG.info("Update node name to soc:" + name + " to make it equals to remoteId because we found a space with prettyName=" + name);
        Session session = node.getSession();
        Node parent = node.getParent();

        String parentId = parent.getUUID();
        String path = node.getPath();
        String parentPath = parent.getPath();
        session.move(path, parentPath + "/soc:" + name);
        session.save();

        node = session.getNodeByUUID(parentId).getNode("soc:" + name);
      }
    }

    Identity identity = identityStorage.findIdentity(providerId, name);
    if (identity != null) {
      LOG.info("Identity with providerId = " + identity.getProviderId() + " and remoteId=" + identity.getRemoteId() + " has already been migrated.");
      return identity;
    }

    identity = new Identity(providerId, name);
    identity.setDeleted(node.getProperty("soc:isDeleted").getBoolean());

    if (node.isNodeType("soc:isDisabled")) {
      identity.setEnable(false);
    }

    identityStorage.saveIdentity(identity);

    //
    String id = identity.getId();
    identity.setId(jcrId);

    // Migrate profile
    //TODO: please check the way to load profile data from JCR
    Profile profile = new Profile(identity);
    jcrIdentityStorage.loadProfile(profile);
    String oldProfileId = profile.getId();
    profile.setId("0");
    identity.setId(id);

    // Process profile
    ProfileEntity entity = _findById(ProfileEntity.class, oldProfileId);
    NTFile avatar = entity.getAvatar();
    if (avatar != null) {
      Resource resource = avatar.getContentResource();
      AvatarAttachment attachment = new AvatarAttachment();
      attachment.setMimeType(resource.getMimeType());
      attachment.setInputStream(new ByteArrayInputStream(resource.getData()));

      profile.setProperty(Profile.AVATAR, attachment);
    }


    identityStorage.saveProfile(profile);

    identity.setProfile(profile);

    return identity;
  }

  public Identity migrateIdentity(String oldId) {
    boolean begun = false;
    try {
      RequestLifeCycle.begin(PortalContainer.getInstance());
      begun = startTx();
      IdentityEntity jcrEntity = _findById(IdentityEntity.class, oldId);

      String providerId = jcrEntity.getProviderId();
      String remoteId = jcrEntity.getRemoteId();

      Identity identity = identityStorage.findIdentity(providerId, remoteId);

      if (identity == null) {
        identity = new Identity(providerId, remoteId);
        identity.setDeleted(jcrEntity.isDeleted());
        identity.setEnable(_getMixin(jcrEntity, DisabledEntity.class, false) == null);

        identityStorage.saveIdentity(identity);

        //
        String id = identity.getId();
        identity.setId(oldId);

        // Migrate profile
        Profile profile = new Profile(identity);
        jcrIdentityStorage.loadProfile(profile);
        String oldProfileId = profile.getId();
        profile.setId("0");
        identity.setId(id);

        // Process profile
        ProfileEntity entity = _findById(ProfileEntity.class, oldProfileId);
        NTFile avatar = entity.getAvatar();
        if (avatar != null) {
          Resource resource = avatar.getContentResource();
          AvatarAttachment attachment = new AvatarAttachment();
          attachment.setMimeType(resource.getMimeType());
          attachment.setInputStream(new ByteArrayInputStream(resource.getData()));

          profile.setProperty(Profile.AVATAR, attachment);
        }


        identityStorage.saveProfile(profile);

        identity.setProfile(profile);

      }

      if (identity != null) {
        String newId = identity.getId();
        identity.setId(oldId);
        broadcastListener(identity, newId);
      }

      return identity;
    } catch (NodeNotFoundException ex) {
      LOG.error("Can not find indentity with oldId: " + oldId, ex);
      return null;
    } catch (Exception ex) {
      LOG.error("Exception while migrate identity with oldId: " + oldId, ex);
      return null;
    } finally {
      try {
        endTx(begun);
      } catch (Exception ex) {
        LOG.error("Error while commit transaction", ex);
      }
      RequestLifeCycle.end();
    }
  }

  private NodeIterator getIdentityNodes(long offset, int limit) {
    if(identityQuery == null) {
      identityQuery = new StringBuilder().append("SELECT * FROM soc:identitydefinition").toString();
    }
    return nodes(identityQuery, offset, limit);
  }

  private long getTotalNumberIdentities() {
    if (this.totalNumberIdentites == 0) {
      if(identityQuery == null) {
        identityQuery = new StringBuilder().append("SELECT * FROM soc:identitydefinition").toString();
      }
      this.totalNumberIdentites = nodes(identityQuery).getSize();
    }
    return this.totalNumberIdentites;
  }


  @Override
  @Managed
  @ManagedDescription("Manual to stop run migration data of identities from JCR to RDBMS.")
  public void stop() {
    super.stop();
  }

  @Override
  protected String getListenerKey() {
    return EVENT_LISTENER_KEY;
  }
}
