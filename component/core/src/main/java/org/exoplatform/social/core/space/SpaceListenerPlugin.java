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
package org.exoplatform.social.core.space;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.jcr.ManagedPlugin;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleListener;

/**
 * Base class for a manageable space listener plugin.
 *
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public abstract class SpaceListenerPlugin extends ManagedPlugin implements
        SpaceLifeCycleListener {

  /**
   * The Logger.
   */
  private static final Log LOG = ExoLogger.getExoLogger(SpaceListenerPlugin.class);
  
  /**
   * {@inheritDoc}
   */
  public abstract void spaceCreated(SpaceLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void spaceRemoved(SpaceLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void applicationActivated(SpaceLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void applicationAdded(SpaceLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void applicationDeactivated(SpaceLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void applicationRemoved(SpaceLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void grantedLead(SpaceLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void joined(SpaceLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void left(SpaceLifeCycleEvent event);

  /**
   * {@inheritDoc}
   */
  public abstract void revokedLead(SpaceLifeCycleEvent event);
  
  /**
   * {@inheritDoc}
   */
  public void spaceRenamed(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String message = "The space" + event.getTarget() + " was renamed to " + space.getDisplayName();
    LOG.debug(message);
  }

}
