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
package org.exoplatform.social.core.space.impl;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class SocialGroupEventListenerImpl extends GroupEventListener {

  private SpaceService spaceService;

  /**
   * Constructor.
   */
  public SocialGroupEventListenerImpl() {
  }

  /**
   * This method is called before the group is persisted to the database.
   *
   * @param group The group to be saved
   * @param isNew if the group is a new record in the database or not
   * @throws Exception The developer can decide to throw an exception or not. If
   *           the listener throw an exception, the organization service should
   *           not save/update the group to the database
   */
  public void preSave(Group group, boolean isNew) throws Exception {
  }

  /**
   * This method is called after the group has been saved but not commited yet
   *
   * @param group The group has been saved.
   * @param isNew if the group is a new record in the database or not
   * @throws Exception The developer can decide to throw the exception or not.
   *           If the method throw an exception. The organization service should
   *           role back the data to the state before the method
   *           GroupHandler.addChild(..) or GroupHandler.saveGroup(..) is
   *           called.
   */
  public void postSave(Group group, boolean isNew) throws Exception {
  }

  /**
   * This method is called before a group should be deleted
   *
   * @param group the group to be delete
   * @throws Exception The developer can decide to throw the exception or not.
   *           If the method throw an exception. The organization service should
   *           not remove the group record from the database.
   */
  public void preDelete(Group group) throws Exception {
    SpaceService spaceSrv = getSpaceService();
    String groupId = group.getId();
    Space space = spaceSrv.getSpaceByGroupId(groupId);
    if (space != null) {
      spaceSrv.deleteSpace(space);
    }
  }

  /**
   * This method should be called after the group has been removed from the
   * database but not commited yet.
   *
   * @param group The group has been removed.
   * @throws Exception The developer can decide to throw the exception or not.
   *           If the method throw the exception, the organization service
   *           should role back the database to the state before the method
   *           GroupHandler.removeGroup(..) is called.
   */
  public void postDelete(Group group) throws Exception {
  }

  private SpaceService getSpaceService() {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    if (spaceService == null) {
      spaceService = (SpaceService) container.getComponentInstance(SpaceService.class);
    }

    return spaceService;
  }
}
