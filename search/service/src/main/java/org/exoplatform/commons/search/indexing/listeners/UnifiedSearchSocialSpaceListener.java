package org.exoplatform.commons.search.indexing.listeners;

import org.exoplatform.commons.api.indexing.IndexingService;
import org.exoplatform.commons.api.indexing.data.SearchEntry;
import org.exoplatform.commons.api.indexing.data.SearchEntryId;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Indexing with :
 * - collection : "social"
 * - type : "space"
 * - name : object id
 */
public class UnifiedSearchSocialSpaceListener extends SpaceListenerPlugin {

  private static Log log = ExoLogger.getLogger(UnifiedSearchSocialSpaceListener.class);

  private final IndexingService indexingService;

  public UnifiedSearchSocialSpaceListener(IndexingService indexingService) {
    this.indexingService = indexingService;
  }

  @Override
  public void spaceCreated(SpaceLifeCycleEvent spaceLifeCycleEvent) {
    if(indexingService != null) {
      Map<String, Object> content = new HashMap<String, Object>();
      content.put("space", spaceLifeCycleEvent.getSpace());
      SearchEntry searchEntry = new SearchEntry("social", "space", spaceLifeCycleEvent.getSpace().getId(), content);
      indexingService.add(searchEntry);
    }
  }

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent spaceLifeCycleEvent) {
    if(indexingService != null) {
      SearchEntryId searchEntryId = new SearchEntryId("social", "space", spaceLifeCycleEvent.getSpace().getId());
      indexingService.delete(searchEntryId);
    }
  }

  @Override
  public void applicationAdded(SpaceLifeCycleEvent spaceLifeCycleEvent) {
    spaceUpdated(spaceLifeCycleEvent);
  }

  @Override
  public void applicationRemoved(SpaceLifeCycleEvent spaceLifeCycleEvent) {
    spaceUpdated(spaceLifeCycleEvent);
  }

  @Override
  public void applicationActivated(SpaceLifeCycleEvent spaceLifeCycleEvent) {
    spaceUpdated(spaceLifeCycleEvent);
  }

  @Override
  public void applicationDeactivated(SpaceLifeCycleEvent spaceLifeCycleEvent) {
    spaceUpdated(spaceLifeCycleEvent);
  }

  @Override
  public void joined(SpaceLifeCycleEvent spaceLifeCycleEvent) {
    spaceUpdated(spaceLifeCycleEvent);
  }

  @Override
  public void left(SpaceLifeCycleEvent spaceLifeCycleEvent) {
    spaceUpdated(spaceLifeCycleEvent);
  }

  @Override
  public void grantedLead(SpaceLifeCycleEvent spaceLifeCycleEvent) {
    spaceUpdated(spaceLifeCycleEvent);
  }

  @Override
  public void revokedLead(SpaceLifeCycleEvent spaceLifeCycleEvent) {
    spaceUpdated(spaceLifeCycleEvent);
  }

  @Override
  public void spaceRenamed(SpaceLifeCycleEvent spaceLifeCycleEvent) {
    spaceUpdated(spaceLifeCycleEvent);
  }

  @Override
  public void spaceDescriptionEdited(SpaceLifeCycleEvent spaceLifeCycleEvent) {
    spaceUpdated(spaceLifeCycleEvent);
  }

  @Override
  public void spaceAvatarEdited(SpaceLifeCycleEvent spaceLifeCycleEvent) {
    spaceUpdated(spaceLifeCycleEvent);
  }

  @Override
  public void addInvitedUser(SpaceLifeCycleEvent spaceLifeCycleEvent) {
    spaceUpdated(spaceLifeCycleEvent);
  }

  @Override
  public void addPendingUser(SpaceLifeCycleEvent spaceLifeCycleEvent) {
    spaceUpdated(spaceLifeCycleEvent);
  }

  @Override
  public void spaceBannerEdited(SpaceLifeCycleEvent event) {

  }

  @Override
  public void spaceAccessEdited(SpaceLifeCycleEvent spaceLifeCycleEvent) {
    spaceUpdated(spaceLifeCycleEvent);
  }

  protected void spaceUpdated(SpaceLifeCycleEvent spaceLifeCycleEvent) {
    if(indexingService != null) {
      Map<String, Object> content = new HashMap<String, Object>();
      content.put("space", spaceLifeCycleEvent.getSpace());
      SearchEntryId searchEntryId = new SearchEntryId("social", "space", spaceLifeCycleEvent.getSpace().getId());
      indexingService.update(searchEntryId, content);
    }
  }
}
