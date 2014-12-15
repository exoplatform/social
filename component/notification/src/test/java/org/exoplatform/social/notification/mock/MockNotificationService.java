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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.utils.CommonsUtils;

public class MockNotificationService implements NotificationService {

  private List<NotificationInfo> storeDigestJCR = new CopyOnWriteArrayList<NotificationInfo>();
  private List<NotificationInfo> storeInstantly = new CopyOnWriteArrayList<NotificationInfo>();
  
  public int sizeOfDigestJCR() {
    return this.storeDigestJCR.size();
  }
  
  public void clearAll() {
    clearOfDigestJCR();
    clearOfInstantly();
  }
  
  public void clearOfDigestJCR() {
    this.storeDigestJCR.clear();
  }
  
  public void clearOfInstantly() {
    this.storeInstantly.clear();
  }

  public List<NotificationInfo> storeDigestJCR() {
    return this.storeDigestJCR;
  }
  
  public List<NotificationInfo> storeInstantly() {
    return this.storeInstantly;
  }

  @Override
  public void process(NotificationInfo notification) throws Exception {
    String pluginId = notification.getKey().getId();
    
    // if the provider is not active, do nothing
    PluginSettingService settingService = CommonsUtils.getService(PluginSettingService.class);
    if (settingService.isActive(UserSetting.EMAIL_CHANNEL, pluginId) == false)
      return;
    
    List<String> userIds = notification.getSendToUserIds();
    UserSettingService userSettingService = CommonsUtils.getService(UserSettingService.class);
    //
    if (notification.isSendAll()) {
      userIds = userSettingService.getUserHasSettingPlugin(UserSetting.EMAIL_CHANNEL, pluginId);
    }
    
    for (String userId : userIds) {
      UserSetting userSetting =  userSettingService.get(userId);
      
      if (userSetting == null) {
        userSetting = UserSetting.getDefaultInstance();
        userSetting.setUserId(userId);
      }
      
      if (userSetting.isActive(UserSetting.EMAIL_CHANNEL, pluginId)) {
        this.storeInstantly.add(notification);
      }
      
      if (userSetting.isInDaily(pluginId) || userSetting.isInWeekly(pluginId)) {
        storeDigestJCR.add(notification);
      }
      
    }
    
  }


  @Override
  public void process(Collection<NotificationInfo> messages) throws Exception {
    for (NotificationInfo message : messages) {
      process(message);
    }
  }

  @Override
  public void digest(NotificationContext context) throws Exception {
    // TODO Auto-generated method stub
  }
}
