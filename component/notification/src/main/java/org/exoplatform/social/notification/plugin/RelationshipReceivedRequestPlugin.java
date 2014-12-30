/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.notification.plugin;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.relationship.model.Relationship;

public class RelationshipReceivedRequestPlugin extends BaseNotificationPlugin {
  
  public RelationshipReceivedRequestPlugin(InitParams initParams) {
    super(initParams);
  }

  public static final String ID = "RelationshipReceivedRequestPlugin";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public NotificationInfo makeNotification(NotificationContext ctx) {
    Relationship relation = ctx.value(SocialNotificationUtils.RELATIONSHIP);
    return NotificationInfo.instance()
                              .key(getId())
                              .to(relation.getReceiver().getRemoteId())
                              .with(SocialNotificationUtils.SENDER.getKey(), relation.getSender().getRemoteId())
                              .with(SocialNotificationUtils.RELATIONSHIP_ID.getKey(), relation.getId())
                              .end();
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    return true;
  }

}
