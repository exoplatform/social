/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.webui.notification;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.api.notification.Provider;
import org.exoplatform.commons.api.notification.UserNotificationSetting;
import org.exoplatform.commons.api.notification.UserNotificationSetting.FREQUENCY;
import org.exoplatform.commons.api.notification.service.ProviderManager;
import org.exoplatform.commons.api.notification.service.UserNotificationService;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.input.UICheckBoxInput;

@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "classpath:groovy/social/webui/notification/UINotificationSettings.gtmpl",
  events = {
    @EventConfig(listeners = UINotificationSettings.SaveActionListener.class),
    @EventConfig(listeners = UINotificationSettings.CancelActionListener.class) 
  }
)
public class UINotificationSettings extends UIForm {
  
  private final static Log         LOG         = ExoLogger.getExoLogger(UINotificationSettings.class);

  private static final FREQUENCY[] frequencies = new FREQUENCY[] { 
    FREQUENCY.INSTANTLY, FREQUENCY.DAILY_KEY, FREQUENCY.WEEKLY_KEY, FREQUENCY.MONTHLY_KEY
  };
  
  private List<Provider> listProviders;

  public UINotificationSettings() throws Exception {
    listProviders = getProviderManager().getActiveProvier(isAdmin());
    for (Provider provider : listProviders) {
      for (int i = 0; i < frequencies.length; ++i) {
        addUIFormInput(new UICheckBoxInput(provider.getType() + frequencies[i].getName(), 
                                           provider.getType() + frequencies[i].getName(),
                                           false));
      }
    }
    setActions(new String[] {"Save", "Cancel"});
  }
  
  private void init() {
    UserNotificationSetting notificationSetting = getUserNotificationService().getUserNotificationSetting(Utils.getOwnerRemoteId());
    for (Provider provider : listProviders) {
      for (int i = 0; i < frequencies.length; ++i) {
        UICheckBoxInput checkbox = getUICheckBoxInput(provider.getType() + frequencies[i].getName());
        checkbox.setChecked(isCheckAcitve(notificationSetting, frequencies[i].getName(), provider.getType()));
      }
    }
  }
  
  private boolean isCheckAcitve(UserNotificationSetting notificationSetting, String frequency, String providerId) {
    if (frequency.equals(FREQUENCY.INSTANTLY.getName())) {
      return notificationSetting.isInInstantly(providerId);
    } else if (frequency.equals(FREQUENCY.DAILY_KEY.getName())) {
      return notificationSetting.isInDaily(providerId);
    } else if (frequency.equals(FREQUENCY.WEEKLY_KEY.getName())) {
      return notificationSetting.isInWeekly(providerId);
    } else if (frequency.equals(FREQUENCY.MONTHLY_KEY.getName())) {
      return notificationSetting.isInMonthly(providerId);
    }
    return false;
  }
  
  private boolean isAdmin() {
    try {
      UserACL userACL = (UserACL) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(UserACL.class);
      List<String> list = new ArrayList<String>();
      Identity identity = ConversationState.getCurrent().getIdentity();
      String userId = identity.getUserId();
      if (userId == null || userId.equals("") || IdentityConstants.ANONIM.equals(userId)) {
        userId = Utils.getOwnerRemoteId();
        if(userId != null && ! userId.equals("")) {
          list.add(userId);
        }
      } else {
        list.addAll(identity.getGroups());
      }
      for (String str : list) {
        if (str.equals(userACL.getSuperUser()) || str.equals(userACL.getAdminGroups())){
          return true;
        }
      }
    } catch (Exception e) {
      LOG.debug("Failed to check permision for user by component UserACL", e);
    }
    return false;
  }
  
  public static class SaveActionListener extends EventListener<UINotificationSettings> {
    public void execute(Event<UINotificationSettings> event) throws Exception {
      UINotificationSettings notification = event.getSource();
      UserNotificationSetting notificationSetting = new UserNotificationSetting();
      for (Provider provider : notification.listProviders) {
        for (int i = 0; i < frequencies.length; ++i) {
          UICheckBoxInput checkbox = notification.getUICheckBoxInput(provider.getType() + frequencies[i].getName());
          if (checkbox.isChecked()) {
            notificationSetting.addProvider(provider.getType(), frequencies[i]);
          }
        }
      }
      notification.getUserNotificationService().saveUserNotificationSetting(Utils.getOwnerRemoteId(), notificationSetting);
      event.getRequestContext().addUIComponentToUpdateByAjax(notification);
    }
  }

  public static class CancelActionListener extends EventListener<UINotificationSettings> {
    public void execute(Event<UINotificationSettings> event) throws Exception {
      UINotificationSettings notifications = event.getSource();
      event.getRequestContext().addUIComponentToUpdateByAjax(notifications);
    }
  }
  
  private ProviderManager getProviderManager() {
    return getApplicationComponent(ProviderManager.class);
  }
  
  private UserNotificationService getUserNotificationService() {
    return getApplicationComponent(UserNotificationService.class);
  }
}
