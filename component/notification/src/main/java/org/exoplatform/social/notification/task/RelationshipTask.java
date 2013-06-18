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
package org.exoplatform.social.notification.task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.task.NotificationTask;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.notification.context.NotificationContext;

public abstract class RelationshipTask implements NotificationTask<NotificationContext> {
  
  public enum PROVIDER_TYPE {
    NEW_USER("NewUserJoinSocialIntranet"), CONNECTION_REQUEST("ReceiceConnectionRequest");
    private final String name;

    PROVIDER_TYPE(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  };
  
  @Override
  public void initSupportProvider() {
    // TODO Auto-generated method stub

  }

  @Override
  public void start(NotificationContext ctx) {
    // TODO Auto-generated method stub

  }

  @Override
  public void end(NotificationContext ctx) {
    // TODO Auto-generated method stub

  }
  
  public static RelationshipTask NEW_USER_JOIN_INTRANET = new RelationshipTask() {
    
    @Override
    public NotificationMessage execute(NotificationContext ctx) {
      NotificationMessage message = new NotificationMessage();
      
      Profile profile = ctx.getProfile();
      
      //This type of notification need to get all users of the system
      List<String> allUsers = new ArrayList<String>();
      
      message.setProviderType(PROVIDER_TYPE.NEW_USER.getName())
             .setFrom(profile.getId())
             .setSendToUserIds(allUsers);
      
      return message;
    }
  
  };
  
  public static RelationshipTask CONNECTION_REQUEST_RECEIVED = new RelationshipTask() {
    
    @Override
    public NotificationMessage execute(NotificationContext ctx) {
      NotificationMessage message = new NotificationMessage();
      
      Relationship relation = ctx.getRelationship();
      
      message.setProviderType(PROVIDER_TYPE.CONNECTION_REQUEST.getName())
             .setFrom(relation.getSender().getProviderId())
             .setSendToUserIds(Arrays.asList(relation.getReceiver().getProviderId()))
             .addOwnerParameter("relationShipId", relation.getId());
      
      return message;
    }
  
  };
  
}
