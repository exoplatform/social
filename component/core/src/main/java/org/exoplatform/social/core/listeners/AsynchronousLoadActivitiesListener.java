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
package org.exoplatform.social.core.listeners;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.streams.StreamInvocationHelper;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 23, 2013  
 */
public class AsynchronousLoadActivitiesListener extends Listener<ConversationRegistry, ConversationState> {

  private ExoContainerContext context;

  public AsynchronousLoadActivitiesListener(ExoContainerContext context) throws Exception {
    this.context = context;
  }

  @Override
  public void onEvent(Event<ConversationRegistry, ConversationState> event) throws Exception {
    String remoteId = event.getData().getIdentity().getUserId();
    
    String name = context.getPortalContainerName();
    ExoContainer container = ExoContainerContext.getContainerByName(name);

    IdentityStorage storage = (IdentityStorage) container.getComponentInstanceOfType(IdentityStorage.class);
    Identity owner = storage.findIdentity(OrganizationIdentityProvider.NAME, remoteId);
    StreamInvocationHelper.loadFeed(owner);
    
  }
}
