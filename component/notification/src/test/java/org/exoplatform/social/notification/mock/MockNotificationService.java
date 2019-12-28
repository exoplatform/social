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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.notification.service.storage.NotificationService;
import org.exoplatform.commons.notification.channel.WebChannel;
import org.exoplatform.commons.utils.CommonsUtils;

public class MockNotificationService implements NotificationService {

  private Map<String, List<NotificationInfo>> storedDigest = new HashMap<>();
  private Map<String, List<NotificationInfo>> storeInstantly = new HashMap<>();
  private Map<String, List<NotificationInfo>> storeWebNotifs = new HashMap<>();

  public int sizeOfInstantly() {
    return this.storeInstantly.values().stream().mapToInt(List::size).sum();
  }

  public int sizeOfWebNotifs() {
    return this.storeWebNotifs.values().stream().mapToInt(List::size).sum();
  }
  
  public int sizeOfStoredDigest() {
    return this.storedDigest.values().stream().mapToInt(List::size).sum();
  }

  public int sizeOfStoredDigest(String username) {
    return this.storedDigest.containsKey(username) ? this.storedDigest.get(username).size() : 0;
  }
  
  public void clearAll() {
    clearOfStoredDigest();
    clearOfInstantly();
    clearOfWebNotifs();
  }
  
  public void clearOfStoredDigest() {
    this.storedDigest.clear();
  }
  
  public void clearOfInstantly() {
    this.storeInstantly.clear();
  }
  
  public void clearOfWebNotifs() {
    this.storeWebNotifs.clear();
  }

  public List<NotificationInfo> storeDigest(String username) {
    return this.storedDigest.containsKey(username) ? this.storedDigest.get(username) : Collections.emptyList();
  }
  
  public List<NotificationInfo> storeInstantly(String username) {
    return this.storeInstantly.containsKey(username) ? this.storeInstantly.get(username) : Collections.emptyList();
  }
  
  public List<NotificationInfo> storeWebNotifs(String username) {
    return this.storeWebNotifs.containsKey(username) ? this.storeWebNotifs.get(username) : Collections.emptyList();
  }

  @Override
  public void process(NotificationInfo notification) throws Exception {
    String pluginId = notification.getKey().getId();
    
    // if the provider is not active, do nothing
    PluginSettingService settingService = CommonsUtils.getService(PluginSettingService.class);
    if (settingService.isActive(UserSetting.EMAIL_CHANNEL, pluginId) == false)
      return;
    
    List<String> userIds = notification.getSendToUserIds();
    MockUserSettingServiceImpl userSettingService = (MockUserSettingServiceImpl) CommonsUtils.getService(UserSettingService.class);
    //
    if (notification.isSendAll()) {
      userIds = userSettingService.getUserHasSettingPlugin(UserSetting.EMAIL_CHANNEL, pluginId);
    }
    
    for (String userId : userIds) {
      UserSetting userSetting =  userSettingService.get(userId);
      
      if (userSetting == null) {
        userSetting = userSettingService.getDefaultSettings();
        userSetting.setUserId(userId);
      }
      
      if (userSetting.isActive(UserSetting.EMAIL_CHANNEL, pluginId)) {
        if(!this.storeInstantly.containsKey(userId)) {
          this.storeInstantly.put(userId, new ArrayList<>());
        }
        this.storeInstantly.get(userId).add(notification);
      }
      
      if (userSetting.isActive(WebChannel.ID, pluginId)) {
        if(!this.storeWebNotifs.containsKey(userId)) {
          this.storeWebNotifs.put(userId, new ArrayList<>());
        }
        this.storeWebNotifs.get(userId).add(notification);
      }
      
      if (userSetting.isInDaily(pluginId) || userSetting.isInWeekly(pluginId)) {
        if(!this.storedDigest.containsKey(userId)) {
          this.storedDigest.put(userId, new ArrayList<>());
        }
        this.storedDigest.get(userId).add(notification);
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
