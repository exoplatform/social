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

import java.io.ByteArrayInputStream;
import java.util.*;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.Value;

import org.chromattic.ext.ntdef.NTFile;

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
import org.exoplatform.social.core.jpa.search.SpaceIndexingServiceConnector;
import org.exoplatform.social.core.chromattic.entity.IdentityEntity;
import org.exoplatform.social.core.chromattic.entity.ProviderEntity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.exception.NodeNotFoundException;
import org.exoplatform.social.core.storage.impl.IdentityStorageImpl;

@Managed
@ManagedDescription("Social migration Spaces from JCR to RDBMS.")
@NameTemplate({@Property(key = "service", value = "social"), @Property(key = "view", value = "migration-spaces") })
public class SpaceMigrationService extends AbstractMigrationService<Space> {
  public static final String EVENT_LISTENER_KEY = "SOC_SPACES_MIGRATION";

  protected final static String REMOVE_LIMIT_THRESHOLD_KEY = "REMOVE_LIMIT_THRESHOLD";
  private int REMOVE_LIMIT_THRESHOLD = 20;

  private long spaceNumber = 0;

  private String spaceQuery;
  private SpaceStorage spaceStorage;

  private Set<String> spaceMigrateFailed = new HashSet<>();
  private Set<String> spaceCleanupFailed = new HashSet<>();

  public SpaceMigrationService(InitParams initParams, SpaceStorage spaceStorage,
                               IdentityStorageImpl identityStorage,
                               EventManager<Space, String> eventManager,
                               EntityManagerService entityManagerService) {

    super(initParams, identityStorage, eventManager, entityManagerService);
    this.LIMIT_THRESHOLD = getInteger(initParams, LIMIT_THRESHOLD_KEY, 200);
    this.REMOVE_LIMIT_THRESHOLD = getInteger(initParams, REMOVE_LIMIT_THRESHOLD_KEY, 20);
    this.spaceStorage = spaceStorage;
  }

  @Override
  protected void beforeMigration() throws Exception {
    MigrationContext.setSpaceDone(false);
    spaceMigrateFailed = new HashSet<>();
  }

  @Override
  @Managed
  @ManagedDescription("Manual to start run migration data of spaces from JCR to RDBMS.")
  public void doMigration() throws Exception {
    RequestLifeCycle.end();
    long totalSpace = getNumberSpaces();
    LOG.info("| \\ START::Spaces migration ---------------------------------");

    long t = System.currentTimeMillis();
    long offset = 0;
    long numberSuccessful = 0;
    boolean cont = true;

    while (cont && !forceStop) {
      long batchSize = 0;
      RequestLifeCycle.begin(PortalContainer.getInstance());
      boolean begunTx = startTx();
      List<String> spaceInTransaction = new ArrayList<>();
      try {
        NodeIterator nodeIter  = getSpaceNodes(offset, LIMIT_THRESHOLD);
        batchSize = nodeIter == null ? 0 : nodeIter.getSize();
        if(batchSize == 0) {
          cont = false;

        } else {
          Node spaceNode = null;
          while (nodeIter.hasNext()) {
            if(forceStop) {
              break;
            }

            offset++;
            spaceNode = nodeIter.nextNode();
            String spaceName = spaceNode.getName();

            spaceInTransaction.add(spaceName);

            LOG.info(String.format("|  \\ START::space number: %s/%s (%s space)", offset, totalSpace, spaceName));
            long t1 = System.currentTimeMillis();

            try {
              Space space = migrateSpace(spaceNode);
              broadcastListener(space, space.getId());
              numberSuccessful++;
            } catch (Exception ex) {
              LOG.error("Error while migrate the space " + spaceName, ex);
              spaceMigrateFailed.add(spaceName);
            }
            LOG.info(String.format("|  / END::space number %s (%s space) consumed %s(ms)", offset, spaceNode.getName(), System.currentTimeMillis() - t1));
          }
        }

      } finally {
        try {
          endTx(begunTx);
        } catch (Exception ex) {
          // Commit transaction failed
          spaceMigrateFailed.addAll(spaceInTransaction);
        }
        RequestLifeCycle.end();
      }
    }

    RequestLifeCycle.begin(PortalContainer.getInstance());

    if (numberFailed > 0) {
      LOG.info(String.format("|   Space migration failed for (%s) space(s)", numberFailed));
    }
    LOG.info(String.format("| / END::Space migration for (%s) space(s) consumed %s(ms)", numberSuccessful, System.currentTimeMillis() - t));

    LOG.info("| \\ START::Re-indexing space(s) ---------------------------------");
    //To be sure all of the space will be indexed in ES after migrated
    IndexingService indexingService = CommonsUtils.getService(IndexingService.class);
    indexingService.reindexAll(SpaceIndexingServiceConnector.TYPE);
    LOG.info("| / END::Re-indexing space(s) ---------------------------------");
  }
  
  private Space migrateSpace(Node spaceNode) throws Exception {
    String prettyName = this.getProperty(spaceNode, "soc:name");
    Space existing = spaceStorage.getSpaceByPrettyName(prettyName);
    if (existing != null) {
      return existing;
    }

    Space space = new Space();
    space.setApp(this.getProperty(spaceNode, "soc:app"));

    IdentityEntity identity = findIdentityEntity(SpaceIdentityProvider.NAME, this.getProperty(spaceNode, "soc:name"));
    if (identity != null) {
      NTFile avatar = identity.getProfile().getAvatar();
      if (avatar != null) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(avatar.getContentResource().getData());
        AvatarAttachment attach = new AvatarAttachment(avatar.getName(), avatar.getName(), 
                                                       avatar.getContentResource().getMimeType(), 
                                                       inputStream, null, avatar.getLastModified().getTime());
        space.setAvatarAttachment(attach);
        space.setAvatarLastUpdated(avatar.getLastModified().getTime());
        space.setAvatarUrl(getSession().getPath(avatar));
      }
    }

    if (spaceNode.hasProperty("soc:createdTime")) {
      space.setCreatedTime(spaceNode.getProperty("soc:createdTime").getLong());
    } else {
      space.setCreatedTime(System.currentTimeMillis());
    }
    space.setDescription(this.getProperty(spaceNode, "soc:description"));
    space.setDisplayName(this.getProperty(spaceNode, "soc:displayName"));
    space.setGroupId(this.getProperty(spaceNode, "soc:groupId"));
    space.setInvitedUsers(this.getProperties(spaceNode, "soc:invitedMembersId"));
    space.setManagers(this.getProperties(spaceNode, "soc:managerMembersId"));
    space.setMembers(this.getProperties(spaceNode, "soc:membersId"));
    space.setPendingUsers(this.getProperties(spaceNode, "soc:pendingMembersId"));
    space.setPrettyName(this.getProperty(spaceNode, "soc:name"));
    space.setPriority(this.getProperty(spaceNode, "soc:priority"));
    space.setRegistration(this.getProperty(spaceNode, "soc:registration"));
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setUrl(this.getProperty(spaceNode, "soc:url"));
    space.setVisibility(this.getProperty(spaceNode, "soc:visibility"));    
    
    spaceStorage.saveSpace(space, true);
    return space;
  }

  @Override
  protected void afterMigration() throws Exception {
    MigrationContext.setSpaceMigrateFailed(spaceMigrateFailed);

    if (!forceStop && spaceMigrateFailed.size() == 0) {
      MigrationContext.setSpaceDone(true);
    }
  }

  public void doRemove() throws Exception {
    spaceCleanupFailed = new HashSet<>();
    long totalSpace = getNumberSpaces();

    LOG.info("| \\ START::cleanup Spaces ---------------------------------");
    long t = System.currentTimeMillis();
    long timePerSpace = System.currentTimeMillis();
    RequestLifeCycle.begin(PortalContainer.getInstance());
    int offset = 0;
    long failed = 0;
    List<String> transactionList = new ArrayList<>();
    try {
      NodeIterator nodeIter  = getSpaceNodes(failed, REMOVE_LIMIT_THRESHOLD);
      transactionList = new ArrayList<>();
      if(nodeIter == null || nodeIter.getSize() == 0) {
        return;
      }

      while (nodeIter.hasNext()) {
        offset++;
        Node node = nodeIter.nextNode();
        String name = node.getName();

        if (!MigrationContext.isForceCleanup() &&  (MigrationContext.getSpaceMigrateFailed().contains(name)
                || MigrationContext.getIdentitiesCleanupFailed().contains(name))) {
          spaceCleanupFailed.add(name);
          LOG.warn("Will not remove this space because the migration or cleanup identity failed");
          continue;
        }

        // Validate space migrated
        String prettyName = this.getProperty(node, "soc:name");
        Space sp = spaceStorage.getSpaceByPrettyName(prettyName);
        if (sp == null) {
          LOG.warn("Will not remove this space because the migration or cleanup identity failed");
          spaceCleanupFailed.add(name);
          continue;
        }

        transactionList.add(name);

        LOG.info(String.format("|  \\ START::cleanup Space number: %s/%s (%s space)", offset, totalSpace, node.getName()));

        try {

          // Do remove all reference first
          PropertyIterator pit = node.getReferences();
          if (pit != null && pit.getSize() > 0) {
            int num = 0;
            while (pit.hasNext()) {
              pit.nextProperty().remove();
              num++;
              if (num % REMOVE_LIMIT_THRESHOLD == 0) {
                getSession().save();
              }
            }
            getSession().save();
          }

          node.remove();
        } catch (Exception ex) {
          spaceCleanupFailed.add(name);
          LOG.error("Error while remove the space " + name, ex);
        }

        LOG.info(String.format("|  / END::cleanup (%s space) consumed time %s(ms)", node.getName(), System.currentTimeMillis() - timePerSpace));
        
        timePerSpace = System.currentTimeMillis();
        if(offset % REMOVE_LIMIT_THRESHOLD == 0) {
          try {
            getSession().save();
          } catch (Exception ex) {
            LOG.error("Exception while cleanup spaces", ex);
            spaceCleanupFailed.addAll(transactionList);
          }
          RequestLifeCycle.end();
          RequestLifeCycle.begin(PortalContainer.getInstance());
          failed = spaceCleanupFailed.size();
          nodeIter = getSpaceNodes(failed, REMOVE_LIMIT_THRESHOLD);
          transactionList = new ArrayList<>();
        }
      }
      LOG.info(String.format("| / END::cleanup Spaces migration for (%s) space consumed %s(ms)", offset, System.currentTimeMillis() - t));
    } finally {
      try {
        getSession().save();
      } catch (Exception ex) {
        LOG.error("Exception while cleanup spaces", ex);
        spaceCleanupFailed.addAll(transactionList);
        getSession().getJCRSession().refresh(false);
      }
      RequestLifeCycle.end();

      MigrationContext.setSpaceCleanupFailed(spaceCleanupFailed);
    }
  }

  @Override
  @Managed
  @ManagedDescription("Manual to stop run miguration data of spaces from JCR to RDBMS.")
  public void stop() {
    super.stop();
  }

  protected String getListenerKey() {
    return EVENT_LISTENER_KEY;
  }
  
  private IdentityEntity findIdentityEntity(final String providerId, final String remoteId) throws NodeNotFoundException {
    ProviderEntity providerEntity;
    try {
      providerEntity = getProviderRoot().getProviders().get(providerId);
    } catch (Exception ex) {
      lifeCycle.getProviderRoot().set(null);
      providerEntity = getProviderRoot().getProviders().get(providerId);
    }

    if (providerEntity == null) {
      throw new NodeNotFoundException("The node " + providerId + " doesn't exist");
    }

    IdentityEntity identityEntity = providerEntity.getIdentities().get(remoteId);

    if (identityEntity == null) {
      throw new NodeNotFoundException("The node " + providerId + "/" + remoteId + " doesn't exist");
    }

    return identityEntity;

  }
  
  private NodeIterator getSpaceNodes(long offset, int LIMIT_THRESHOLD) {
    if(spaceQuery == null) {
      spaceQuery = new StringBuffer().append("SELECT * FROM soc:spacedefinition").toString();
    }
    return nodes(spaceQuery, offset, LIMIT_THRESHOLD);
  }

  private long getNumberSpaces() {
    if (spaceNumber <= 0) {
      if (spaceQuery == null) {
        spaceQuery = new StringBuffer().append("SELECT * FROM soc:spacedefinition").toString();
      }
      spaceNumber = nodes(spaceQuery).getSize();
    }
    return spaceNumber;
  }
  
  private String getProperty(Node spaceNode, String propName) throws Exception {
    try {
      return spaceNode.getProperty(propName).getString();
    } catch (Exception ex) {
      return null;
    }
  }
  
  private String[] getProperties(Node spaceNode, String propName) throws Exception {
    List<String> values = new LinkedList<>();
    try {
      for (Value val : spaceNode.getProperty(propName).getValues()) {
        values.add(val.getString());
      }
      return values.toArray(new String[values.size()]);
    } catch (Exception ex) {
      return null;
    }
  }
}
