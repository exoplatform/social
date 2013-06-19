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
package org.exoplatform.commons.api.notification.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationMessage;
import org.exoplatform.commons.api.notification.UserNotificationService;
import org.exoplatform.commons.api.notification.service.NotificationContext;
import org.exoplatform.commons.api.notification.service.NotificationService;
import org.exoplatform.commons.api.notification.service.NotificationServiceListener;
import org.exoplatform.commons.api.notification.user.UserNotificationSetting;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.container.PortalContainer;

public class NotificationServiceImpl implements NotificationService {

  NotificationServiceListener<NotificationMessage> messageListener;

  NotificationServiceListener<NotificationContext> contextListener;

  public NotificationServiceImpl(NotificationServiceListener<NotificationMessage> messageListener) {
    this.contextListener = new NotificationServiceListenerImpl();
    this.messageListener = messageListener;
  }

  @Override
  public void addNotificationServiceListener(NotificationContext ctx) {
    contextListener.processListener(ctx);
  }

  @Override
  public void addSendNotificationListener(NotificationMessage message) {
    messageListener.processListener(message);
  }

  private UserNotificationService getUserNotificationService() {
    return (UserNotificationService) PortalContainer.getInstance().getComponentInstanceOfType(UserNotificationService.class);
  }

  private SettingService getSettingService() {
    return (SettingService) PortalContainer.getInstance().getComponentInstanceOfType(SettingService.class);
  }

  @Override
  public void processNotificationMessage(NotificationMessage message) {
    UserNotificationService notificationService = getUserNotificationService();
    List<String> userIds = message.getSendToUserIds();
    List<String> userIdPeddings = new ArrayList<String>();

    for (String userId : userIds) {
      UserNotificationSetting userNotificationSetting = notificationService.getUserNotificationSetting(userId);
      if (userNotificationSetting.isImmediately()) {
        message.setSendToUserIds(Arrays.asList(userId));
        addSendNotificationListener(message);
      } else {
        userIdPeddings.add(userId);
      }
    }

    if (userIdPeddings.size() > 0) {
      message.setSendToUserIds(userIdPeddings);
      saveNotificationMessage(message);
    }
  }

  public void processNotificationMessages(Collection<NotificationMessage> messages) {
    for (NotificationMessage message : messages) {
      processNotificationMessage(message);
    }
  }

  @Override
  public void saveNotificationMessage(NotificationMessage message) {
    SettingService settingService = getSettingService();
    
    
    // TODO Auto-generated method stub

  }

  @Override
  public NotificationMessage getNotificationMessageByProviderType(String providerType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<NotificationMessage> getNotificationMessagesByUser(String userId) {
    // TODO Auto-generated method stub
    return null;
  }

}
