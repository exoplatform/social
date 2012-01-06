/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.application;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.core.space.spi.SpaceService;

/**
 * This listener is responsible for initializing and notifying activity stream for the space. We create a special
 * opensocial user (with a group provider) ready to receive new activities.
 *
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class SpaceActivityPublisher extends SpaceListenerPlugin {

  /**
   * The Logger.
   */
  private static final Log LOG = ExoLogger.getExoLogger(SpaceActivityPublisher.class);

  /**
   * activity manager for posting activities.
   */
  private ActivityManager activityManager;

  /**
   * identity manager for getting identities.
   */
  private IdentityManager identityManager;

  /**
   * Constructor.
   *
   * @param params the initial params
   * @param activityManager the activity manager
   * @param identityManager the identity manager
   */
  public SpaceActivityPublisher(final InitParams params,
                                final ActivityManager activityManager,
                                final IdentityManager identityManager) {
    this.activityManager = activityManager;
    this.identityManager = identityManager;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    final String activityMessage = space.getDisplayName() + " was created by @" + event.getTarget() + " .";
    recordActivity(event, activityMessage);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {
    LOG.debug("space " + event.getSpace().getDisplayName() + " was removed!");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void applicationActivated(SpaceLifeCycleEvent event) {
    LOG.debug("application <strong>" + event.getTarget() + "</strong> was activated in space "
            + event.getSpace().getDisplayName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void applicationAdded(SpaceLifeCycleEvent event) {
    LOG.debug("application <strong>" + event.getTarget() + "</strong> was added in space "
            + event.getSpace().getDisplayName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void applicationDeactivated(SpaceLifeCycleEvent event) {
    LOG.debug("application " + event.getTarget() + " was deactivated in space "
            + event.getSpace().getDisplayName());

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void applicationRemoved(SpaceLifeCycleEvent event) {
    LOG.debug("application " + event.getTarget() + " was removed in space "
            + event.getSpace().getDisplayName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void grantedLead(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    final String activityMessage = "@" + event.getTarget() + " was granted manager role.";
    recordActivity(event, activityMessage);
    LOG.debug("user " + event.getTarget() + " was granted manager role of space " + space.getDisplayName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void joined(SpaceLifeCycleEvent event) {
    final String activityMessage = "@" + event.getTarget() + " has joined the space.";
    recordActivity(event, activityMessage);
    LOG.debug("user " + event.getTarget() + " joined space " + event.getSpace().getDisplayName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void left(SpaceLifeCycleEvent event) {
    final String activityMessage = "@" + event.getTarget() + " has left the space.";
    recordActivity(event, activityMessage);
    LOG.debug("user " + event.getTarget() + " has left of space " + event.getSpace().getDisplayName());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void revokedLead(SpaceLifeCycleEvent event) {
    LOG.debug("user " + event.getTarget() + " was revoked lead privileges of space "
            + event.getSpace().getDisplayName());
  }

  /**
   * Records an activity based on space lifecycle event and the activity message.
   *
   * @param event the space lifecycle event
   * @param activityMessage the activity message
   */
  private void recordActivity(SpaceLifeCycleEvent event, String activityMessage) {
    Space space = event.getSpace();
    try {
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,
              space.getPrettyName(),
              false);
      activityManager.recordActivity(spaceIdentity, SpaceService.SPACES_APP_ID, activityMessage);
    } catch (Exception e) {
      LOG.error("Failed to record activity: " + activityMessage, e);
    }
  }

}
