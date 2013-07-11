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

import org.exoplatform.commons.api.notification.ArgumentLiteral;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.task.NotificationTask;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.relationship.model.Relationship;

public abstract class RelationshipTask implements NotificationTask<NotificationContext> {
  
  public final static ArgumentLiteral<Relationship> RELATIONSHIP = new ArgumentLiteral<Relationship>(Relationship.class, "relationship");
  
  @Override
  public void start(NotificationContext ctx) {}

  @Override
  public void end(NotificationContext ctx) {}
  
  public static RelationshipTask NEW_USER_JOIN_INTRANET = new RelationshipTask() {
    private final String PROVIDER_TYPE = "NewUserJoinSocialIntranet";

    @Override
    public String getId() {
      return PROVIDER_TYPE;
    }
    
    @Override
    public NotificationMessage execute(NotificationContext ctx) {
      NotificationMessage message = new NotificationMessage();
      
      Profile profile = ctx.value(ProfileTask.PROFILE);
      
      //This type of notification need to get all users of the system
      List<String> allUsers = new ArrayList<String>();
      
      message.setProviderType(PROVIDER_TYPE)
             .setSendToUserIds(allUsers);
      
      return message;
    }
    
    @Override
    public boolean isValid(NotificationContext ctx) {
      return true;
    }
  
  };
  
  public static RelationshipTask CONNECTION_REQUEST_RECEIVED = new RelationshipTask() {
    private final String PROVIDER_TYPE = "ReceiceConnectionRequest";

    @Override
    public String getId() {
      return PROVIDER_TYPE;
    }
    
    @Override
    public NotificationMessage execute(NotificationContext ctx) {
      NotificationMessage message = new NotificationMessage();
      
      Relationship relation = ctx.value(RELATIONSHIP);
      
      message.setProviderType(PROVIDER_TYPE)
             .setSendToUserIds(Arrays.asList(relation.getReceiver().getRemoteId()))
             .addOwnerParameter("relationShipId", relation.getId());
      
      return message;
    }
    
    @Override
    public boolean isValid(NotificationContext ctx) {
      return true;
    }
  
  };
  
}
