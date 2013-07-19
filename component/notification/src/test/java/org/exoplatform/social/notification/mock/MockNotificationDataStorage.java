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
package org.exoplatform.social.notification.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.service.storage.NotificationDataStorage;

public class MockNotificationDataStorage implements NotificationDataStorage {
  Queue<NotificationMessage>       queue = new ConcurrentLinkedQueue<NotificationMessage>();

  public MockNotificationDataStorage() {
  }
  
  public MockNotificationDataStorage add(NotificationMessage notificationMessage) {
    queue.add(notificationMessage);
    return this;
  }

  public MockNotificationDataStorage addAll(Collection<NotificationMessage> notificationMessages) {
    queue.addAll(notificationMessages);
    return this;
  }
  
  public int size() {
    return queue.size();
  }
  
  public Collection<NotificationMessage> emails() {
    Collection<NotificationMessage> messages = new ArrayList<NotificationMessage>(queue);
    queue.clear();
    return messages;
  }
}
