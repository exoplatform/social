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
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.NotificationKey;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;

public class MockNotificationService implements NotificationService {

  private Queue<NotificationInfo> jcrMock = new ConcurrentLinkedQueue<NotificationInfo>();
  
  @Override
  public Map<NotificationKey, List<NotificationInfo>> getByUser(UserSetting userSetting) {
    return null;
  }
  
  public int size() {
    return this.jcrMock.size();
  }
  
  public void clear() {
    jcrMock.clear();
  }

  public List<NotificationInfo> emails() {
    List<NotificationInfo> list = new ArrayList<NotificationInfo>(jcrMock);
    clear();
    return list;
  }

  @Override
  public void process(NotificationInfo message) throws Exception {
    jcrMock.add(message);
  }

  @Override
  public void processDaily() throws Exception {
  }

  @Override
  public void process(Collection<NotificationInfo> messages) throws Exception {
    for (NotificationInfo message : messages) {
      process(message);
    }
  }

  @Override
  public Map<String, NotificationInfo> getNotificationMessagesByProviderId(String pluginId,
                                                                              boolean isWeekend) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void removeNotificationMessages(String pluginId) {
    // TODO Auto-generated method stub
    
  }
 
}
