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
package org.exoplatform.social.notification.connector;

import java.util.Collection;

import org.exoplatform.commons.api.notification.EmailMessage;
import org.exoplatform.commons.api.notification.MailConnector;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.notification.SocialEmailUtils;
import org.exoplatform.social.notification.SocialEmailStorage.CONNECTOR_TYPE;

public class RelationshipConnector extends MailConnector {

  public RelationshipConnector(InitParams initParams) {
    super(initParams);
  }

  @Override
  public Collection<EmailMessage> emails() {
    return SocialEmailUtils.getSocialEmailStorage().getEmailNotification(CONNECTOR_TYPE.RELATIONSHIP);
  }

}
