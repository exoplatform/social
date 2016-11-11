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
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.social.core.jpa.search.ProfileIndexingServiceConnector;
import org.exoplatform.social.core.jpa.storage.dao.ConnectionDAO;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.relationship.model.Relationship;

import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Oct 1, 2015  
 */
public class UserESListenerImpl extends UserEventListener {
  private static final Log LOG = ExoLogger.getLogger(UserESListenerImpl.class);

  @Override
  public void preDelete(final User user) throws Exception {
    RequestLifeCycle.begin(PortalContainer.getInstance());
    try{
      IdentityManager idm = CommonsUtils.getService(IdentityManager.class);
      Identity identity = idm.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user.getUserName(), false);

      LOG.info("Notifying indexing service for user deletion id={}", identity.getId());

      CommonsUtils.getService(IndexingService.class).unindex(ProfileIndexingServiceConnector.TYPE, identity.getId());
      reIndexAllConnector(identity);
    } finally {
      RequestLifeCycle.end();
    }
  }
  
  @Override
  public void postSetEnabled(User user) throws Exception {
    RequestLifeCycle.begin(PortalContainer.getInstance());
    try {
      IdentityManager idm = CommonsUtils.getService(IdentityManager.class);
      Identity identity = idm.getOrCreateIdentity(OrganizationIdentityProvider.NAME, user.getUserName(), false);

      LOG.info("Notifying indexing service for user enable status change id={}", identity.getId());

      if (! user.isEnabled()) {
        CommonsUtils.getService(IndexingService.class).unindex(ProfileIndexingServiceConnector.TYPE, identity.getId());
      } else {
        CommonsUtils.getService(IndexingService.class).reindex(ProfileIndexingServiceConnector.TYPE, identity.getId());
      }
    } finally {
      RequestLifeCycle.end();
    }
  }

  private void reIndexAllConnector(Identity identity) {
    ConnectionDAO connectionDAO = CommonsUtils.getService(ConnectionDAO.class);
    IndexingService indexingService = CommonsUtils.getService(IndexingService.class);
    long identityId = Long.parseLong(identity.getId());

    final int limit = 500;
    List<Long> connections = null;

    // Sender
    int start = 0;
    do {
      connections = connectionDAO.getSenderIds(identityId, Relationship.Type.ALL, start, limit);
      for (Long id : connections) {
        indexingService.reindex(ProfileIndexingServiceConnector.TYPE, String.valueOf(id));
      }
      start += limit;
    } while (connections.size() >= limit);

    // Receiver
    start = 0;
    do {
      connections = connectionDAO.getReceiverIds(identityId, Relationship.Type.ALL, start, limit);
      for (Long id : connections) {
        indexingService.reindex(ProfileIndexingServiceConnector.TYPE, String.valueOf(id));
      }
      start += limit;
    } while (connections.size() >= limit);
  }

}
