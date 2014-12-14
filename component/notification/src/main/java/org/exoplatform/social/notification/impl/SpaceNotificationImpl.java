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

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.notification.plugin.RequestJoinSpacePlugin;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;
import org.exoplatform.social.notification.plugin.SpaceInvitationPlugin;

public class SpaceNotificationImpl extends SpaceListenerPlugin {

  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {}

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {}

  @Override
  public void applicationAdded(SpaceLifeCycleEvent event) {}

  @Override
  public void applicationRemoved(SpaceLifeCycleEvent event) {}

  @Override
  public void applicationActivated(SpaceLifeCycleEvent event) {}

  @Override
  public void applicationDeactivated(SpaceLifeCycleEvent event) {}

  @Override
  public void joined(SpaceLifeCycleEvent event) {}

  @Override
  public void left(SpaceLifeCycleEvent event) {}

  @Override
  public void grantedLead(SpaceLifeCycleEvent event) {}

  @Override
  public void revokedLead(SpaceLifeCycleEvent event) {}

  @Override
  public void spaceRenamed(SpaceLifeCycleEvent event) {}

  @Override
  public void spaceDescriptionEdited(SpaceLifeCycleEvent event) {}

  @Override
  public void spaceAvatarEdited(SpaceLifeCycleEvent event) {}

  @Override
  public void spaceAccessEdited(SpaceLifeCycleEvent event) {}
  
  @Override
  public void addInvitedUser(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String userId = event.getTarget();
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance().append(SocialNotificationUtils.REMOTE_ID, userId)
                                                             .append(SocialNotificationUtils.SPACE, space);
    
    ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(SpaceInvitationPlugin.ID))).execute(ctx);
  }
  
  @Override
  public void addPendingUser(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String userId = event.getTarget();
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance().append(SocialNotificationUtils.REMOTE_ID, userId)
                                                             .append(SocialNotificationUtils.SPACE, space);
    
    ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(RequestJoinSpacePlugin.ID))).execute(ctx);
    
  }
}
