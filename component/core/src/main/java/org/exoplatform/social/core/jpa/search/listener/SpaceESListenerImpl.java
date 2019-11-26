package org.exoplatform.social.core.jpa.search.listener;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.jpa.search.SpaceIndexingServiceConnector;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;

public class SpaceESListenerImpl extends SpaceListenerPlugin {

  private static final Log LOG = ExoLogger.getExoLogger(SpaceESListenerImpl.class);

  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {
    IndexingService indexingService = CommonsUtils.getService(IndexingService.class);
    String id = event.getSpace().getId();

    LOG.info("Notifying indexing service for space creation id={}", id);

    indexingService.index(SpaceIndexingServiceConnector.TYPE, id);
  }

  @Override
  public void spaceDescriptionEdited(SpaceLifeCycleEvent event) {
    reindex(event, "space description");
  }

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {
    IndexingService indexingService = CommonsUtils.getService(IndexingService.class);
    String id = event.getSpace().getId();

    LOG.debug("Notifying indexing service for space removal id={}", id);

    indexingService.unindex(SpaceIndexingServiceConnector.TYPE, id);
  }

  @Override
  public void spaceRenamed(SpaceLifeCycleEvent event) {
    reindex(event, "space renaming");
  }

  private void reindex(SpaceLifeCycleEvent event, String cause) {
    IndexingService indexingService = CommonsUtils.getService(IndexingService.class);
    String id = event.getSpace().getId();

    LOG.info("Notifying indexing service for {} id={}", cause, id);

    indexingService.reindex(SpaceIndexingServiceConnector.TYPE, id);
  }

  @Override
  public void spaceAccessEdited(SpaceLifeCycleEvent event) {
    reindex(event, "space access edited");
  }

  @Override
  public void spaceRegistrationEdited(SpaceLifeCycleEvent event) {
    reindex(event, "space registration edited");
  }

  @Override
  public void spaceBannerEdited(SpaceLifeCycleEvent event) {
  }

  @Override
  public void applicationActivated(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void applicationAdded(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void applicationDeactivated(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void applicationRemoved(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void grantedLead(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void joined(SpaceLifeCycleEvent event) {
    reindex(event, "space joined");
  }

  @Override
  public void left(SpaceLifeCycleEvent event) {
    reindex(event, "space left");
  }

  @Override
  public void revokedLead(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void spaceAvatarEdited(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addInvitedUser(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addPendingUser(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }
}
