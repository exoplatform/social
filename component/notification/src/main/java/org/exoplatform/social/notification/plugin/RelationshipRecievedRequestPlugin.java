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

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.commons.api.notification.MessageInfo;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;

public class RelationshipRecievedRequestPlugin extends AbstractNotificationPlugin {
  public final String ID = "RelationshipRecievedRequest";

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public NotificationMessage makeNotification(NotificationContext ctx) {
    Relationship relation = ctx.value(SocialNotificationUtils.RELATIONSHIP);
    return NotificationMessage.instance()
                              .key(getId())
                              .to(relation.getReceiver().getRemoteId())
                              .with("sender", relation.getSender().getRemoteId())
                              .with(SocialNotificationUtils.RELATIONSHIP_ID.getKey(), relation.getId())
                              .end();
  }

  @Override
  public MessageInfo makeMessage(NotificationContext ctx) {
    MessageInfo messageInfo = new MessageInfo();
    Map<String, String> templateContext = new HashMap<String, String>();
    
    NotificationMessage notification = ctx.getNotificationMessage();
    
    String language = getLanguage(notification);
    
    String sender = notification.getValueOwnerParameter("sender");
    String toUser = notification.getSendToUserIds().iterator().next();
    Identity identity = Utils.getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, sender, true);
    Profile userProfile = identity.getProfile();
    
    templateContext.put("USER", userProfile.getFullName());
    String subject = Utils.getTemplateGenerator().processSubjectIntoString(notification.getKey().getId(), templateContext, language);
    
    templateContext.put("AVATAR", LinkProviderUtils.getUserAvatarUrl(userProfile));
    templateContext.put("ACCEPT_CONNECTION_REQUEST_ACTION_URL", LinkProviderUtils.getConfirmInvitationToConnectUrl(sender, toUser));
    templateContext.put("REFUSE_CONNECTION_REQUEST_ACTION_URL", LinkProviderUtils.getIgnoreInvitationToConnectUrl(sender, toUser));
    String body = Utils.getTemplateGenerator().processTemplateIntoString(notification.getKey().getId(), templateContext, language);

    return messageInfo.subject(subject).body(body).end();
  }

  @Override
  public boolean makeDigest(NotificationContext ctx, Writer writer) {
    return true;
  }

}
