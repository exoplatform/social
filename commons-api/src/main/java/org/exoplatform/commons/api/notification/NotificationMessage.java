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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationMessage {
  private Date                createData;

  private String              providerType; //

  private String              from;

  private Map<String, String> ownerParameter = new HashMap<String, String>();

  private String              messageType;

  private List<String>        sendToUserIds  = new ArrayList<String>();

  public NotificationMessage() {
  }

  /**
   * @return the createData
   */
  public Date getCreateData() {
    return createData;
  }

  /**
   * @param createData the createData to set
   */
  public NotificationMessage setCreateData(Date createData) {
    this.createData = createData;
    return this;
  }

  public String getProviderType() {
    return providerType;
  }

  public NotificationMessage setProviderType(String providerType) {
    this.providerType = providerType;
    return this;
  }

  public String getFrom() {
    return from;
  }

  public NotificationMessage setFrom(String from) {
    this.from = from;
    return this;
  }

  public String getMessageType() {
    return messageType;
  }

  public NotificationMessage setMessageType(String messageType) {
    this.messageType = messageType;
    return this;
  }

  public List<String> getSendToUserIds() {
    return sendToUserIds;
  }

  public NotificationMessage setSendToUserIds(List<String> sendToUserIds) {
    this.sendToUserIds = sendToUserIds;
    return this;
  }

  public NotificationMessage addSendToUserId(String sendToUserId) {
    this.sendToUserIds.add(sendToUserId);
    return this;
  }

  /**
   * @return the ownerParameter
   */
  public Map<String, String> getOwnerParameter() {
    return ownerParameter;
  }

  /**
   * @param ownerParameter the ownerParameter to set
   */
  public NotificationMessage setOwnerParameter(Map<String, String> ownerParameter) {
    this.ownerParameter = ownerParameter;
    return this;
  }

  /**
   * @param ownerParameter the ownerParameter to set
   */
  public NotificationMessage addOwnerParameter(String key, String value) {
    this.ownerParameter.put(key, value);
    return this;
  }
}
