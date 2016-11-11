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

import java.util.*;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

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
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.jpa.storage.dao.ConnectionDAO;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.jpa.storage.entity.ConnectionEntity;
import org.exoplatform.social.core.chromattic.entity.IdentityEntity;
import org.exoplatform.social.core.chromattic.entity.RelationshipEntity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.impl.IdentityStorageImpl;

@Managed
@ManagedDescription("Social migration relationships from JCR to RDBMS.")
@NameTemplate({@Property(key = "service", value = "social"), @Property(key = "view", value = "migration-relationships") })
public class RelationshipMigrationService extends AbstractMigrationService<Relationship> {
  public static final String EVENT_LISTENER_KEY = "SOC_RELATIONSHIP_MIGRATION";
  private final ConnectionDAO connectionDAO;
  private final IdentityDAO identityDAO;
  private static final int LIMIT_REMOVED_THRESHOLD = 10;

  private Set<String> identitiesMigrateFailed = new HashSet<>();
  private Set<String> identitiesCleanupFailed = new HashSet<>();

  public RelationshipMigrationService(InitParams initParams,
                                      IdentityStorageImpl identityStorage,
                                      ConnectionDAO connectionDAO,
                                      IdentityDAO identityDAO,
                                      EventManager<Relationship, String> eventManager,
                                      EntityManagerService entityManagerService) {

    super(initParams, identityStorage, eventManager, entityManagerService);
    this.connectionDAO = connectionDAO;
    this.identityDAO = identityDAO;
    this.LIMIT_THRESHOLD = getInteger(initParams, LIMIT_THRESHOLD_KEY, 200);
  }

  @Override
  protected void beforeMigration() throws Exception {
    MigrationContext.setConnectionDone(false);
    identitiesMigrateFailed = new HashSet<>();
  }

  @Override
  @Managed
  @ManagedDescription("Manual to start run migration data of relationships from JCR to RDBMS.")
  public void doMigration() throws Exception {
    RequestLifeCycle.end();

    long totalIdentities = getNumberUserIdentities();

    boolean cont = true;
    long offset = 0;
    int total = 0;
    long t = System.currentTimeMillis();

    while(cont && !forceStop) {
      RequestLifeCycle.begin(PortalContainer.getInstance());
      boolean begunTx = startTx();
      List<String> transactionList = new ArrayList<>();

      try {
        LOG.info("| \\ START::Relationships migration ---------------------------------");
        NodeIterator nodeIter = getIdentityNodes(offset, LIMIT_THRESHOLD);
        if (nodeIter == null || nodeIter.getSize() == 0) {
          cont = false;
        } else {

          int relationshipNo;
          Node identityNode;
          while (nodeIter.hasNext()) {
            if (forceStop) {
              break;
            }
            relationshipNo = 0;
            offset++;
            identityNode = nodeIter.nextNode();

            String identityName = identityNode.getName();
            transactionList.add(identityName);

            LOG.info(String.format("|  \\ START::user number: %s/%s (%s user)", offset, totalIdentities, identityNode.getName()));
            long t1 = System.currentTimeMillis();

            try {
              Node relationshipNode = identityNode.getNode("soc:relationship");
              if (relationshipNode != null) {
                NodeIterator rIt = relationshipNode.getNodes();
                long size = rIt.getSize();
                LOG.info("|     - CONFIRMED:: size = " + size);
                if (size > 0) {
                  relationshipNo += migrateRelationshipEntity(rIt, identityNode.getName(), false, Relationship.Type.CONFIRMED);
                }
              }

              relationshipNode = identityNode.getNode("soc:sender");
              if (relationshipNode != null) {
                NodeIterator rIt = relationshipNode.getNodes();
                long size = rIt.getSize();
                LOG.info("|     - SENDER:: size = " + size);
                if (size > 0) {
                  relationshipNo += migrateRelationshipEntity(rIt, identityNode.getName(), false, Relationship.Type.OUTGOING);
                }
              }

              relationshipNode = identityNode.getNode("soc:receiver");
              if (relationshipNode != null) {
                NodeIterator rIt = relationshipNode.getNodes();
                long size = rIt.getSize();
                LOG.info("|     - RECEIVER:: size = " + size);
                if (size > 0) {
                  relationshipNo += migrateRelationshipEntity(rIt, identityNode.getName(), true, Relationship.Type.INCOMING);
                }
              }

              IdentityEntity identityEntity = _findById(IdentityEntity.class, identityNode.getUUID());
              identityEntity.setProperty(MigrationContext.KEY_MIGRATE_CONNECTION, MigrationContext.TRUE_STRING);
            } catch (Exception ex) {
              LOG.error("Exception while migrate relationship for " + identityName, ex);
              identitiesMigrateFailed.add(identityName);
            }

            //
            total += relationshipNo;
            if (offset % LIMIT_THRESHOLD == 0) {
              try {
                endTx(begunTx);
              } catch (Exception ex) {
                identitiesMigrateFailed.addAll(transactionList);
              }
              RequestLifeCycle.end();
              RequestLifeCycle.begin(PortalContainer.getInstance());
              begunTx = startTx();
              transactionList = new ArrayList<>();
              nodeIter = getIdentityNodes(offset, LIMIT_THRESHOLD);
            }

            LOG.info(String.format("|  / END::user number %s (%s user) with %s relationship(s) user consumed %s(ms)", relationshipNo, identityNode.getName(), relationshipNo, System.currentTimeMillis() - t1));
          }
        }

      } finally {
        try {
          endTx(begunTx);
        } catch (Exception ex) {
          identitiesMigrateFailed.addAll(transactionList);
        }

        RequestLifeCycle.end();
      }
    }

    LOG.info(String.format("| / END::Relationships migration for (%s) user(s) with %s relationship(s) consumed %s(ms)", offset, total, System.currentTimeMillis() - t));
    RequestLifeCycle.begin(PortalContainer.getInstance());

    LOG.info("| \\ START::Re-indexing identity(s) ---------------------------------");
    IndexingService indexingService = CommonsUtils.getService(IndexingService.class);
    indexingService.reindexAll(ProfileIndexingServiceConnector.TYPE);
    LOG.info("| / END::Re-indexing identity(s) ---------------------------------");
  }
  
  private int migrateRelationshipEntity(NodeIterator it, String userName, boolean isIncoming, Relationship.Type status) throws RepositoryException {
    int doneConnectionNo = 0;
    startTx();
    while (it.hasNext()) {
      Node relationshipNode = it.nextNode();
      String receiverId = relationshipNode.getProperty("soc:to").getString();
      LOG.debug("|     - FROM ID = " + receiverId);
      String senderId = relationshipNode.getProperty("soc:from").getString();
      LOG.debug("|     - TO ID = " + senderId);
      long lastUpdated = System.currentTimeMillis();
      if (relationshipNode.hasProperty("exo:lastModifiedDate")) {
        lastUpdated = relationshipNode.getProperty("exo:lastModifiedDate").getDate().getTimeInMillis();
      }
      LOG.debug("|     - LAST UPDATED = " + lastUpdated);
      //handle the duplicate connection key by catch exception
      try {
        //check the sender
        //Identity sender = new Identity(isIncoming ? senderId : receiverId);
        //Identity receiver = new Identity(isIncoming ? receiverId : senderId);

        IdentityEntity senderIdentity = _findById(IdentityEntity.class, senderId);
        IdentityEntity receiverIdentity = _findById(IdentityEntity.class, receiverId);

        org.exoplatform.social.core.jpa.storage.entity.IdentityEntity sender = identityDAO.findByProviderAndRemoteId(OrganizationIdentityProvider.NAME, senderIdentity.getRemoteId());
        org.exoplatform.social.core.jpa.storage.entity.IdentityEntity receiver = identityDAO.findByProviderAndRemoteId(OrganizationIdentityProvider.NAME, receiverIdentity.getRemoteId());

        ConnectionEntity exist = connectionDAO.getConnection(sender.getId(), receiver.getId());
        if (exist == null) {
          exist = connectionDAO.getConnection(receiver.getId(), sender.getId());
        }
        if (exist == null) {
          ConnectionEntity entity = new ConnectionEntity(sender, receiver);
          entity.setStatus(status);
          entity.setUpdatedDate(new Date(lastUpdated));
          //
          connectionDAO.create(entity);
          ++doneConnectionNo;
        }       
      } catch(Exception e) {
        LOG.warn(e.getMessage());
        continue;
      }
      
      if(doneConnectionNo % LIMIT_THRESHOLD == 0) {
        LOG.info(String.format("|     - BATCH MIGRATION::relationship number: %s (%s user)", doneConnectionNo,  userName));
        endTx(true);
        entityManagerService.endRequest(PortalContainer.getInstance());
        entityManagerService.startRequest(PortalContainer.getInstance());
        startTx();
      }
    }
    return doneConnectionNo;
  }

  @Override
  protected void afterMigration() throws Exception {
    MigrationContext.setIdentitiesMigrateConnectionFailed(identitiesMigrateFailed);
    if(!forceStop && identitiesMigrateFailed.isEmpty()) {
      MigrationContext.setConnectionDone(true);
    }
  }

  public void doRemove() throws Exception {
    identitiesCleanupFailed = new HashSet<>();

    long totalIdentities = getNumberUserIdentities();

    LOG.info("| \\ START::cleanup Relationships ---------------------------------");
    long t = System.currentTimeMillis();
    long timePerUser = System.currentTimeMillis();
    RequestLifeCycle.begin(PortalContainer.getInstance());
    int offset = 0;
    List<String> transactionList = new ArrayList<>();
    try {
      NodeIterator nodeIter  = getIdentityNodes(offset, LIMIT_THRESHOLD);
      transactionList = new ArrayList<>();
      if(nodeIter == null || nodeIter.getSize() == 0) {
        return;
      }
      Node node = null;
      
      while (nodeIter.hasNext()) {
        node = nodeIter.nextNode();
        String name = node.getName();

        // Do not cleanup if migrate failed
        if (!MigrationContext.isForceCleanup() && MigrationContext.getIdentitiesMigrateConnectionFailed().contains(name)) {
          identitiesCleanupFailed.add(name);
          continue;
        }

        transactionList.add(node.getName());
        offset++;

        LOG.info(String.format("|  \\ START::cleanup Relationship of user number: %s/%s (%s user)", offset, totalIdentities, node.getName()));
        IdentityEntity identityEntity = _findById(IdentityEntity.class, node.getUUID());

        String migrated = identityEntity.getProperty(MigrationContext.KEY_MIGRATE_CONNECTION);
        if (!MigrationContext.TRUE_STRING.equalsIgnoreCase(migrated)) {
          identitiesCleanupFailed.add(name);
          LOG.warn("Can not clean connection for " + name + " due to migration was not successful");
          continue;
        }
        
        Collection<RelationshipEntity> entities = identityEntity.getRelationship().getRelationships().values();
        removeRelationshipEntity(entities);
        // 
        entities = identityEntity.getSender().getRelationships().values();
        removeRelationshipEntity(entities);
        //
        entities = identityEntity.getReceiver().getRelationships().values();
        removeRelationshipEntity(entities);
        
        LOG.info(String.format("|  / END::cleanup (%s user) consumed time %s(ms)", node.getName(), System.currentTimeMillis() - timePerUser));
        
        timePerUser = System.currentTimeMillis();
        if(offset % LIMIT_THRESHOLD == 0) {
          try {
            getSession().save();
          } catch (Exception ex) {
            LOG.error("Failed when commit the cleanup connections", ex);
            identitiesCleanupFailed.addAll(transactionList);
          }
          RequestLifeCycle.end();
          RequestLifeCycle.begin(PortalContainer.getInstance());
          nodeIter = getIdentityNodes(offset, LIMIT_THRESHOLD);
          transactionList = new ArrayList<>();
        }
      }
      LOG.info(String.format("| / END::cleanup Relationships migration for (%s) user consumed %s(ms)", offset, System.currentTimeMillis() - t));
    } finally {
      try {
        getSession().save();
      } catch (Exception ex) {
        LOG.error("Failed when commit the cleanup connections", ex);
        identitiesCleanupFailed.addAll(transactionList);
      }
      RequestLifeCycle.end();
      MigrationContext.setIdentitiesCleanupConnectionFailed(identitiesCleanupFailed);
    }
  }
  
  
  private void removeRelationshipEntity(Collection<RelationshipEntity> entities) {
    try {
      int offset = 0;
      Iterator<RelationshipEntity> it = entities.iterator();
      while (it.hasNext()) {
        RelationshipEntity relationshipEntity = it.next();
        getSession().remove(relationshipEntity);
        ++offset;
        if (offset % LIMIT_REMOVED_THRESHOLD == 0) {
          LOG.info(String.format("|     - BATCH CLEANUP::relationship number: %s", offset));
          getSession().save();
        }
      }
    } finally {
      getSession().save();
    }
  }

  

  @Override
  @Managed
  @ManagedDescription("Manual to stop run miguration data of relationships from JCR to RDBMS.")
  public void stop() {
    super.stop();
  }

  protected String getListenerKey() {
    return EVENT_LISTENER_KEY;
  }
}
