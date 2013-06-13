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
package org.exoplatform.commons.api.notification;

import java.util.ArrayList;
import java.util.List;

public class EmailMessage {
  private String       providerId;

  private String       from;

  private String       body;

  private String       messageType;

  private List<String> sendToUserIds = new ArrayList<String>();

  public EmailMessage() {
  }

  /**
   * @return the providerId
   */
  public String getProviderId() {
    return providerId;
  }

  /**
   * @param providerId the providerId to set
   */
  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }

  /**
   * @return the from
   */
  public String getFrom() {
    return from;
  }

  /**
   * @param from the from to set
   */
  public void setFrom(String from) {
    this.from = from;
  }

  /**
   * @return the body
   */
  public String getBody() {
    return body;
  }

  /**
   * @param body the body to set
   */
  public void setBody(String body) {
    this.body = body;
  }

  /**
   * @return the messageType
   */
  public String getMessageType() {
    return messageType;
  }

  /**
   * @param messageType the messageType to set
   */
  public void setMessageType(String messageType) {
    this.messageType = messageType;
  }

  /**
   * @return the sendToUserIds
   */
  public List<String> getSendToUserIds() {
    return sendToUserIds;
  }

  /**
   * @param sendToUserIds the sendToUserIds to set
   */
  public void setSendToUserIds(List<String> sendToUserIds) {
    this.sendToUserIds = sendToUserIds;
  }

}
