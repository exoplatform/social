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
import org.exoplatform.commons.api.notification.NotificationDataStorage;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.RelationshipEvent;
import org.exoplatform.social.core.relationship.RelationshipListenerPlugin;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.notification.context.NotificationExecutor;
import org.exoplatform.social.notification.task.ProfileTask;
import org.exoplatform.social.notification.task.RelationshipTask;

public class RelationshipNotifictionImpl extends RelationshipListenerPlugin {

  private static final Log LOG = ExoLogger.getLogger(RelationshipNotifictionImpl.class);
  
  @Override
  public void confirmed(RelationshipEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void ignored(RelationshipEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void removed(RelationshipEvent event) {
    // TODO Auto-generated method stub

  }

  @Override
  public void requested(RelationshipEvent event) {
    Relationship relationship = event.getPayload();
    try {
      NotificationContext ctx = NotificationContextImpl.DEFAULT.append(RelationshipTask.RELATIONSHIP, relationship);
      NotificationDataStorage storage = Utils.getSocialEmailStorage();
      storage.addAll(NotificationExecutor.execute(ctx, RelationshipTask.CONNECTION_REQUEST_RECEIVED));
    } catch (Exception e) {
      LOG.warn("Failed to get invite to connect information of " + event + ": " + e.getMessage());
    }

  }

  @Override
  public void denied(RelationshipEvent event) {
    // TODO Auto-generated method stub

  }

}
