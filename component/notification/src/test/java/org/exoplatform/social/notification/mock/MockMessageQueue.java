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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.notification.mock;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.api.notification.model.NotificationInfo;

public class MockMessageQueue {
  
  private final static List<NotificationInfo> messageQueue = new ArrayList<NotificationInfo>();
  
  public static void add(NotificationInfo message) {
    messageQueue.add(message);
  }

  public static NotificationInfo get() {
    
    if (messageQueue.size() == 0) return null;
    
    List<NotificationInfo> result = new ArrayList<NotificationInfo>(messageQueue);
    messageQueue.clear();
    return result.get(0);
  }

}
