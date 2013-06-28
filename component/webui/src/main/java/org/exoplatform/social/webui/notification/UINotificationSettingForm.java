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

import org.exoplatform.commons.api.notification.UserNotificationSetting;
import org.exoplatform.commons.api.notification.UserNotificationSetting.FREQUENCY;
import org.exoplatform.commons.api.notification.service.ProviderSettingService;
import org.exoplatform.commons.api.notification.service.UserNotificationService;
import org.exoplatform.commons.utils.CommonsUtils;
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
  template = "classpath:groovy/social/webui/notification/UINotificationSettingForm.gtmpl",
  events = {
    @EventConfig(listeners = UINotificationSettingForm.SaveActionListener.class),
    @EventConfig(listeners = UINotificationSettingForm.CancelActionListener.class) 
  }
)
public class UINotificationSettingForm extends UIForm {

  private static final Log         LOG             = ExoLogger.getLogger(UINotificationSettingForm.class);

  private static final FREQUENCY[] frequencies     = FREQUENCY.values();

  private List<String>             activeProviders  = new ArrayList<String>();

  public UINotificationSettingForm() throws Exception {
    setActions(new String[] { "Save", "Cancel" });
  }
  
  private String getCheckBoxId(String providerId, String frequencyType) {
    return new StringBuffer(providerId).append(frequencyType).toString();
  }
  
  public void initSettingForm() {
    activeProviders = CommonsUtils.getService(ProviderSettingService.class).getActiveProviderIds(isAdmin());
    String checkBoxId;
    for (String providerId : activeProviders) {
      for (int i = 0; i < frequencies.length; ++i) {
        checkBoxId = getCheckBoxId(providerId, frequencies[i].getName());
        addUIFormInput(new UICheckBoxInput(checkBoxId, checkBoxId, false));
      }
    }
  }

  private void resetSettingForm() {
    UserNotificationSetting notificationSetting = getUserNotificationService().getUserNotificationSetting(Utils.getOwnerRemoteId());
    for (String providerId : activeProviders) {
      for (int i = 0; i < frequencies.length; ++i) {
        getUICheckBoxInput(getCheckBoxId(providerId, frequencies[i].getName()))
        .setChecked(isCheckAcitve(notificationSetting, frequencies[i].getName(), providerId));
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
  
  private UserNotificationService getUserNotificationService() {
    return getApplicationComponent(UserNotificationService.class);
  }
  
  public static class SaveActionListener extends EventListener<UINotificationSettingForm> {
    public void execute(Event<UINotificationSettingForm> event) throws Exception {
      UINotificationSettingForm uiForm = event.getSource();
      UserNotificationSetting notificationSetting = new UserNotificationSetting();
      String checkBoxId;
      for (String providerId : uiForm.activeProviders) {
        for (int i = 0; i < frequencies.length; ++i) {
          checkBoxId = uiForm.getCheckBoxId(providerId, frequencies[i].getName());
          if (uiForm.getUICheckBoxInput(checkBoxId).isChecked()) {
            notificationSetting.addProvider(providerId, frequencies[i]);
          }
        }
      }
      uiForm.getUserNotificationService().saveUserNotificationSetting(Utils.getOwnerRemoteId(), notificationSetting);
      uiForm.resetSettingForm();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
    }
  }

  public static class CancelActionListener extends EventListener<UINotificationSettingForm> {
    public void execute(Event<UINotificationSettingForm> event) throws Exception {
      UINotificationSettingForm notifications = event.getSource();
      notifications.resetSettingForm();
      event.getRequestContext().addUIComponentToUpdateByAjax(notifications);
    }
  }
}
