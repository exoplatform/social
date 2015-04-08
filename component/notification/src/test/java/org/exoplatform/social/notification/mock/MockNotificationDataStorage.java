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

import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.storage.NotificationDataStorage;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;

public class MockNotificationDataStorage implements NotificationDataStorage {
  NotificationService notificationService;
  public MockNotificationDataStorage(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @Override
  public void save(NotificationInfo notification) throws Exception {
  }

  @Override
  public void removeMessageAfterSent() throws Exception {
    
  }

  @Override
  public Map<PluginKey, List<NotificationInfo>> getByUser(NotificationContext context,
                                                                UserSetting userSetting) {
    return null;
  }


}
