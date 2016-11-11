/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.social.core.jpa.search.listener;

import org.exoplatform.commons.search.index.IndexingService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.relationship.RelationshipEvent;
import org.exoplatform.social.core.relationship.RelationshipListenerPlugin;
import org.exoplatform.social.core.relationship.model.Relationship;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 29, 2015  
 */
public class ConnectionESListenerImpl extends RelationshipListenerPlugin {
  private static final Log LOG = ExoLogger.getExoLogger(ConnectionESListenerImpl.class);

  @Override
  public void requested(RelationshipEvent event) {
    reindexRelationship(event.getPayload(), "requested");
  }

  @Override
  public void denied(RelationshipEvent event) {
    reindexRelationship(event.getPayload(), "denied");
  }

  @Override
  public void confirmed(RelationshipEvent event) {
    reindexRelationship(event.getPayload(), "confirmed");
  }

  @Override
  public void ignored(RelationshipEvent event) {
    reindexRelationship(event.getPayload(), "ignored");
  }

  @Override
  public void removed(RelationshipEvent event) {
    reindexRelationship(event.getPayload(), "removed");
  }
  
  private void reindexRelationship(Relationship relationship, String cause) {
    IndexingService indexingService  = CommonsUtils.getService(IndexingService.class);
    String receiverId = relationship.getReceiver().getId();
    String senderId = relationship.getSender().getId();

    LOG.info("Notifying indexing service for connection {} sender_id={} receiver_id={}", cause, senderId, receiverId);

    indexingService.reindex(ProfileIndexingServiceConnector.TYPE, receiverId);
    indexingService.reindex(ProfileIndexingServiceConnector.TYPE, senderId);
  }
  
}
