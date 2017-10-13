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
package org.exoplatform.social.core.jpa.search.listener;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.profile.ProfileLifeCycleEvent;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 29, 2015  
 */
public class ProfileESListenerImpl extends ProfileListenerPlugin {
  
  private static final Log LOG = ExoLogger.getLogger(ProfileESListenerImpl.class);

  @Override
  public void avatarUpdated(ProfileLifeCycleEvent event) {
    IndexingService indexingService = CommonsUtils.getService(IndexingService.class);
    String id = event.getProfile().getIdentity().getId();

    LOG.info("Notifying indexing service for profile avatar update id={}", id);

    indexingService.reindex(ProfileIndexingServiceConnector.TYPE, id);
  }

  @Override
  public void bannerUpdated(ProfileLifeCycleEvent event) {
    LOG.debug("Profile banner of user {} has been updated", event.getProfile().getIdentity().getId());
  }

  @Override
  public void contactSectionUpdated(ProfileLifeCycleEvent event) {
    IndexingService indexingService = CommonsUtils.getService(IndexingService.class);
    String id = event.getProfile().getIdentity().getId();

    LOG.info("Notifying indexing service for the profile contact update id={}", id);

    indexingService.reindex(ProfileIndexingServiceConnector.TYPE, id);
  }

  @Override
  public void aboutMeUpdated(ProfileLifeCycleEvent event) {
    String id = event.getProfile().getIdentity().getId();

    LOG.info("Notifying indexing service for the profile aboutme update id={}", id);

    CommonsUtils.getService(IndexingService.class).reindex(ProfileIndexingServiceConnector.TYPE, id);
  }

  @Override
  public void experienceSectionUpdated(ProfileLifeCycleEvent event) {
    String id = event.getProfile().getIdentity().getId();

    LOG.info("Notifying indexing service for the profile experience update id={}", id);

    CommonsUtils.getService(IndexingService.class).reindex(ProfileIndexingServiceConnector.TYPE, id);
  }

  @Override
  public void createProfile(ProfileLifeCycleEvent event) {
    IndexingService indexingService = CommonsUtils.getService(IndexingService.class);
    String id = event.getProfile().getIdentity().getId();

    LOG.info("Notifying indexing service for the profile creation id={}", id);

    indexingService.index(ProfileIndexingServiceConnector.TYPE, id);
  }

}
