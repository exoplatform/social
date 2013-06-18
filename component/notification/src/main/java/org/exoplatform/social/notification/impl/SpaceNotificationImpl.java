/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.notification.impl;

import org.exoplatform.commons.api.notification.NotificationDataStorage;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.notification.context.NotificationContext;
import org.exoplatform.social.notification.context.NotificationExecutor;
import org.exoplatform.social.notification.task.SpaceTask;

public class SpaceNotificationImpl extends SpaceListenerPlugin {

  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void applicationAdded(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void applicationRemoved(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void applicationActivated(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void applicationDeactivated(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void joined(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void left(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void grantedLead(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void revokedLead(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void spaceRenamed(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void spaceDescriptionEdited(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void spaceAvatarEdited(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void spaceAccessEdited(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub

  }
  
  @Override
  public void addInvitedUser(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String userId = event.getTarget();
    
    NotificationContext ctx = NotificationContext.makeSpaceNofification(space, userId);
    NotificationDataStorage storage = Utils.getSocialEmailStorage();
    
    storage.addAll(NotificationExecutor.execute(ctx, SpaceTask.SPACE_INVITATION));
  }
  
  @Override
  public void addPendingUser(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String userId = event.getTarget();
    
    NotificationContext ctx = NotificationContext.makeSpaceNofification(space, userId);
    NotificationDataStorage storage = Utils.getSocialEmailStorage();
    
    storage.addAll(NotificationExecutor.execute(ctx, SpaceTask.SPACE_JOIN_REQUEST));
  }
}
