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
package org.exoplatform.social.core.space.spi;

import org.exoplatform.social.common.lifecycle.LifeCycleListener;


/**
 * A listener to follow the liecycle of a space.
 *
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public interface SpaceLifeCycleListener extends LifeCycleListener<SpaceLifeCycleEvent> {


  /**
   * Invokes this method when a space is created.
   *
   * @param event the space lifecycle event
   */
  void spaceCreated(SpaceLifeCycleEvent event);

  /**
   * Invokes this method when a space is removed.
   *
   * @param event the space lifecyle event
   */
  void spaceRemoved(SpaceLifeCycleEvent event);

  /**
   * Invokes this method when an application is added to a space.
   *
   * @param event the space lifecycle event
   */
  void applicationAdded(SpaceLifeCycleEvent event);


  /**
   * Invokes this method when an application is removed from a space.
   *
   * @param event the space lifecycle event.
   */
  void applicationRemoved(SpaceLifeCycleEvent event);


  /**
   * Invokes this method when an application is activated.
   *
   * @param event the space lifecyle event
   */
  void applicationActivated(SpaceLifeCycleEvent event);


  /**
   * Invokes this method when an application is deactivated from a space.
   *
   * @param event the space lifecycle event
   */
  void applicationDeactivated(SpaceLifeCycleEvent event);

  /**
   * Invokes this method when a user joins a space.
   *
   * @param event the space lifecycle event
   */
  void joined(SpaceLifeCycleEvent event);

  /**
   * Invokes this method when a user leaves a space.
   *
   * @param event the space lifecycle event
   */
  void left(SpaceLifeCycleEvent event);

  /**
   * Invokes this method when a user is granted lead role of a space.
   *
   * @param event the space lifecycle event
   */
  void grantedLead(SpaceLifeCycleEvent event);

  /**
   * Invokes this method when a user is revoked lead role of a space.
   *
   * @param event the space lifecycle event
   */
  void revokedLead(SpaceLifeCycleEvent event);
  
  /**
   * Invokes this method when a user rename a space.
   *
   * @param event the space lifecycle event
   */
  void spaceRenamed(SpaceLifeCycleEvent event);
  
  /**
   * Invokes this method when a user change the description of a space.
   *
   * @param event the space lifecycle event
   */
  void spaceDescriptionEdited(SpaceLifeCycleEvent event);
  
  /**
   * Invokes this method when a user change the avatar of a space.
   *
   * @param event the space lifecycle event
   */
  void spaceAvatarEdited(SpaceLifeCycleEvent event);
  
  /**
   * Invokes this method when a user update the space access.
   *
   * @param event the space lifecycle event
   */
  void spaceAccessEdited(SpaceLifeCycleEvent event);
  
  /**
   * Invokes this method when a user is invited to join a space.
   *
   * @param event the space lifecycle event
   */
  void addInvitedUser(SpaceLifeCycleEvent event);
  
  /**
   * Invokes this method when a user request to join a space.
   *
   * @param event the space lifecycle event
   */
  void addPendingUser(SpaceLifeCycleEvent event);

}
