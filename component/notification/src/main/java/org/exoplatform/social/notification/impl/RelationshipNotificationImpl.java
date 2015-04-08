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
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.relationship.RelationshipEvent;
import org.exoplatform.social.core.relationship.RelationshipListenerPlugin;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.notification.plugin.RelationshipReceivedRequestPlugin;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;

public class RelationshipNotificationImpl extends RelationshipListenerPlugin {

  private static final Log LOG = ExoLogger.getLogger(RelationshipNotificationImpl.class);
  
  @Override
  public void confirmed(RelationshipEvent event) {

  }

  @Override
  public void ignored(RelationshipEvent event) {

  }

  @Override
  public void removed(RelationshipEvent event) {

  }

  @Override
  public void requested(RelationshipEvent event) {
    Relationship relationship = event.getPayload();
    try {
      NotificationContext ctx = NotificationContextImpl.cloneInstance().append(SocialNotificationUtils.RELATIONSHIP, relationship);
      ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(RelationshipReceivedRequestPlugin.ID)))
                                   .execute(ctx);
    } catch (Exception e) {
      LOG.warn("Failed to get invite to connect information of " + event + ": " + e.getMessage());
    }
  }

  @Override
  public void denied(RelationshipEvent event) {
    
  }

}
