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
package org.exoplatform.social.space.spi;

import org.exoplatform.social.lifecycle.LifeCycleListener;

/**
 * A listener to follow the liecycle of a space.
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public interface SpaceLifeCycleListener extends LifeCycleListener<SpaceLifeCycleEvent> {


  /**
   * Invoked when the space is added
   * @param event
   */
  void spaceCreated(SpaceLifeCycleEvent event);

  /**
   * Invoked when the space is removed
   * @param event
   */
  void spaceRemoved(SpaceLifeCycleEvent event);


  /**
   * Invoked when an application is added to a space
   * @param event
   */
  void applicationAdded(SpaceLifeCycleEvent event);


  /**
   * Invoked when an application is removed from a space
   * @param event
   */
  void applicationRemoved(SpaceLifeCycleEvent event);



  /**
   * Invoked when an application is activated
   * @param event
   */
  void applicationActivated(SpaceLifeCycleEvent event);


  /**
   * Invoked when an application is deactivated from the space
   * @param event
   */
  void applicationDeactivated(SpaceLifeCycleEvent event);

  /**
   * when a user joins a space
   * @param event
   */
  void joined(SpaceLifeCycleEvent event);

  /**
   * When a user leaves a space
   * @param event
   */
  void left(SpaceLifeCycleEvent event);

  /**
   * When a user is granted lead of a space
   * @param event
   */
  void grantedLead(SpaceLifeCycleEvent event);

  /**
   * When a user is revoked lead of a space
   * @param event
   */
  void revokedLead(SpaceLifeCycleEvent event);


}
