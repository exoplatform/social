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

import org.exoplatform.social.common.lifecycle.AbstractLifeCycle;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleListener;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent.Type;


/**
 * Implementation of the lifecycle of spaces. <br/>
 * Events are dispatched asynchronously but sequentially to their listeners
 * according to their type.<br/>
 * Listeners may fail, this is safe for the lifecycle, subsequent listeners will
 * still be called.
 *
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class SpaceLifecycle extends AbstractLifeCycle<SpaceLifeCycleListener, SpaceLifeCycleEvent> {

  @Override
  protected void dispatchEvent(SpaceLifeCycleListener listener, SpaceLifeCycleEvent event) {
    switch (event.getType()) {
    case SPACE_CREATED:
      listener.spaceCreated(event);
      break;
    case SPACE_REMOVED:
      listener.spaceRemoved(event);
      break;
    case APP_ACTIVATED:
      listener.applicationActivated(event);
      break;
    case APP_DEACTIVATED:
      listener.applicationDeactivated(event);
      break;
    case APP_ADDED:
      listener.applicationAdded(event);
      break;
    case APP_REMOVED:
      listener.applicationRemoved(event);
      break;
    case JOINED:
      listener.joined(event);
      break;
    case LEFT:
      listener.left(event);
      break;
    case GRANTED_LEAD:
      listener.grantedLead(event);
      break;
    case REVOKED_LEAD:
      listener.revokedLead(event);
      break;
    case SPACE_RENAMED:
      listener.spaceRenamed(event);
      break;
    case SPACE_DESCRIPTION_EDITED:
      listener.spaceDescriptionEdited(event);
      break;
    case SPACE_AVATAR_EDITED:
      listener.spaceAvatarEdited(event);
      break;
    case SPACE_HIDDEN:
      listener.spaceAccessEdited(event);
      break;
    case ADD_INVITED_USER:
      listener.addInvitedUser(event);
      break;
    case ADD_PENDING_USER:
      listener.addPendingUser(event);
      break;
    default:
      break;
    }
  }

  public void spaceCreated(Space space, String creator) {
    broadcast(new SpaceLifeCycleEvent(space, creator, Type.SPACE_CREATED));
  }

  public void spaceRemoved(Space space, String remover) {
    broadcast(new SpaceLifeCycleEvent(space, remover, Type.SPACE_REMOVED));
  }

  public void addApplication(Space space, String appId) {
    SpaceLifeCycleEvent event = new SpaceLifeCycleEvent(space, appId, Type.APP_ADDED);
    event.getSource();
    broadcast(event);
  }

  public void deactivateApplication(Space space, String appId) {
    SpaceLifeCycleEvent event = new SpaceLifeCycleEvent(space, appId, Type.APP_DEACTIVATED);
    broadcast(event);
  }

  public void activateApplication(Space space, String appId) {
    SpaceLifeCycleEvent event = new SpaceLifeCycleEvent(space, appId, Type.APP_ACTIVATED);
    broadcast(event);
  }

  public void removeApplication(Space space, String appId) {
    SpaceLifeCycleEvent event = new SpaceLifeCycleEvent(space, appId, Type.APP_REMOVED);
    broadcast(event);
  }

  public void memberJoined(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.JOINED));
  }

  public void memberLeft(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.LEFT));
  }

  public void grantedLead(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.GRANTED_LEAD));
  }

  public void revokedLead(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.REVOKED_LEAD));
  }
  
  public void spaceRenamed(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.SPACE_RENAMED));
  }
  
  public void spaceDescriptionEdited(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.SPACE_DESCRIPTION_EDITED));
  }
  
  public void spaceAvatarEdited(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.SPACE_AVATAR_EDITED));
  }
  
  public void spaceAccessEdited(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.SPACE_HIDDEN));
  }

  public void addInvitedUser(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.ADD_INVITED_USER));
  }
  
  public void addPendingUser(Space space, String userId) {
    broadcast(new SpaceLifeCycleEvent(space, userId, Type.ADD_PENDING_USER));
  }
}
