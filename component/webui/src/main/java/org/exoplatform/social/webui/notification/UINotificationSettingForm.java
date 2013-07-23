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
import java.util.ResourceBundle;

import org.exoplatform.commons.api.notification.GroupProvider;
import org.exoplatform.commons.api.notification.UserNotificationSetting;
import org.exoplatform.commons.api.notification.UserNotificationSetting.FREQUENCY;
import org.exoplatform.commons.api.notification.service.setting.ProviderSettingService;
import org.exoplatform.commons.api.notification.service.setting.UserNotificationService;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIConfirmation;
import org.exoplatform.webui.core.UIConfirmation.ActionConfirm;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.input.UICheckBoxInput;

@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "classpath:groovy/social/webui/notification/UINotificationSettingForm.gtmpl",
  events = {
    @EventConfig(listeners = UINotificationSettingForm.SaveActionListener.class),
    @EventConfig(listeners = UINotificationSettingForm.ResetActionListener.class),
    @EventConfig(listeners = UINotificationSettingForm.DeactivateActionListener.class),
    @EventConfig(listeners = UINotificationSettingForm.ClickActionListener.class) 
  }
)
public class UINotificationSettingForm extends UIForm {

  private static final Log         LOG                  = ExoLogger.getLogger(UINotificationSettingForm.class);

  private static final String      CHECK_BOX_DEACTIVATE     = "checkBoxDeactivate";

  private static final String      DAILY                = "Daily";

  private static final String      WEEKLY               = "Weekly";

  private static final String      NEVER                = "Never";

  private boolean                  isActiveNotification  = true;

  private List<String>             activeProviders        = new ArrayList<String>();
  
  private UserNotificationService notificationService;
  
  private ProviderSettingService settingService;

  private boolean isRenderConfirm = false;

  public UINotificationSettingForm() throws Exception {
    notificationService = CommonsUtils.getService(UserNotificationService.class);
    settingService = CommonsUtils.getService(ProviderSettingService.class);
    setActions(new String[] { "Save", "Reset" });
  }
  
  protected boolean isActiveNotification() {
    return this.isActiveNotification;
  }

  protected boolean isRenderConfirm() {
    return this.isRenderConfirm;
  }
  
  protected List<GroupProvider> getGroupProviders() {
    return settingService.getGroupProviders();
  }

  protected List<String> getActiveProviders() {
    return new ArrayList<String>(activeProviders);
  }
  
  private boolean isInInstantly(UserNotificationSetting setting, String providerId) {
    return (setting.isInInstantly(providerId)) ? true : false;
  }

  private String getDigestValue(UserNotificationSetting setting, String providerId) {
    if (setting.isInWeekly(providerId)) {
      return WEEKLY;
    } else if (setting.isInDaily(providerId)) {
      return DAILY;
    } else {
      return NEVER;
    }
  }
  
  @Override
  public String getLabel(String key) {
    try {
      return super.getLabel(key);
    } catch (Exception e) {
      return key;
    }
  }
  
  private String makeSelectBoxId(String providerId) {
    return new StringBuffer(providerId).append("SelectBox").toString();
  }
  
  private List<SelectItemOption<String>> getDigestOptions() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    options.add(new SelectItemOption<String>(getLabel(NEVER), NEVER));
    options.add(new SelectItemOption<String>(getLabel(DAILY), DAILY));
    options.add(new SelectItemOption<String>(getLabel(WEEKLY), WEEKLY));
    return options;
  }

  public void initSettingForm() {
    UserNotificationSetting setting = notificationService.getUserNotificationSetting(Utils.getOwnerRemoteId());
    activeProviders = settingService.getActiveProviderIds();
    for (String providerId : activeProviders) {
      addUIFormInput(new UICheckBoxInput(providerId, providerId, isInInstantly(setting, providerId)));
      addUIFormInput(new UIFormSelectBox(makeSelectBoxId(providerId), makeSelectBoxId(providerId), getDigestOptions()).setValue(getDigestValue(setting, providerId)));
    }
    //
    isActiveNotification = setting.isActive();
    UICheckBoxInput boxInput = new UICheckBoxInput(CHECK_BOX_DEACTIVATE, CHECK_BOX_DEACTIVATE, (isActiveNotification == false));
    boxInput.setOnChange("Save");
    addUIFormInput(boxInput);
  }

  private void resetSettingForm(UserNotificationSetting setting) {
    for (String providerId : activeProviders) {
        getUICheckBoxInput(providerId).setChecked(isInInstantly(setting, providerId));
        getUIFormSelectBox(makeSelectBoxId(providerId)).setValue(getDigestValue(setting, providerId));
    }
    isActiveNotification = setting.isActive();
    getUICheckBoxInput(CHECK_BOX_DEACTIVATE).setChecked((isActiveNotification == false));
  }
  
  
  public static class SaveActionListener extends EventListener<UINotificationSettingForm> {
    public void execute(Event<UINotificationSettingForm> event) throws Exception {
      UINotificationSettingForm uiForm = event.getSource();
      UserNotificationSetting notificationSetting = new UserNotificationSetting();
      for (String providerId : uiForm.activeProviders) {
        if(uiForm.getUICheckBoxInput(providerId).isChecked() == true) {
          notificationSetting.addProvider(providerId, FREQUENCY.INSTANTLY);
        }
        //
        String selected = uiForm.getUIFormSelectBox(uiForm.makeSelectBoxId(providerId)).getValue();
        if(WEEKLY.equals(selected)) {
          notificationSetting.addProvider(providerId, FREQUENCY.WEEKLY_KEY);
        }
        if(DAILY.equals(selected)) {
          notificationSetting.addProvider(providerId, FREQUENCY.DAILY_KEY);
        }
      }
      notificationSetting.setActive(uiForm.getUICheckBoxInput(CHECK_BOX_DEACTIVATE).isChecked() == false);
      //
      uiForm.notificationService.saveUserNotificationSetting(notificationSetting.setUserId(Utils.getOwnerRemoteId()));
      uiForm.resetSettingForm(notificationSetting);
      //
      WebuiRequestContext context = event.getRequestContext();
      String objectId = context.getRequestParameter(OBJECTID);
      if(objectId != null && objectId.equals("true")) {
        context.getUIApplication().addMessage(
          new ApplicationMessage("UINotificationSettingForm.msg.SaveOKNotification", 
                                  new Object[]{}, ApplicationMessage.INFO));
      }
      context.addUIComponentToUpdateByAjax(uiForm);
    }
  }

  public static class ResetActionListener extends EventListener<UINotificationSettingForm> {
    public void execute(Event<UINotificationSettingForm> event) throws Exception {
      UINotificationSettingForm notifications = event.getSource();
      ResourceBundle res = event.getRequestContext().getApplicationResourceBundle();
      
      UIConfirmation confirmation = notifications.getChild(UIConfirmation.class);
      if(confirmation == null) {
        confirmation = notifications.addChild(UIConfirmation.class, null, null);
        confirmation.setShowMask(true);
        confirmation.setCaller(notifications);
        List<ActionConfirm> actions_ = new ArrayList<UIConfirmation.ActionConfirm>();
        actions_.add(new ActionConfirm("Click", res.getString("UINotificationSettingForm.action.Confirm")));
        actions_.add(new ActionConfirm("Close", res.getString("UINotificationSettingForm.action.Cancel")));
        confirmation.setActions(actions_);
      }
      confirmation.setMessage(res.getString("UINotificationSettingForm.msg.ResetNotificationSetting"));
      confirmation.setShow(true);
      confirmation.setRendered(true);
      notifications.isRenderConfirm  = true;
      event.getRequestContext().addUIComponentToUpdateByAjax(notifications);
    }
  }

  public static class DeactivateActionListener extends EventListener<UINotificationSettingForm> {
    public void execute(Event<UINotificationSettingForm> event) throws Exception {
      UINotificationSettingForm notifications = event.getSource();
      notifications.isActiveNotification = !notifications.isActiveNotification;
      notifications.getUICheckBoxInput(CHECK_BOX_DEACTIVATE).setChecked((notifications.isActiveNotification == false));
      event.getRequestContext().addUIComponentToUpdateByAjax(notifications);
    }
  }

  public static class ClickActionListener extends EventListener<UINotificationSettingForm> {
    public void execute(Event<UINotificationSettingForm> event) throws Exception {
      UINotificationSettingForm notifications = event.getSource();
      UserNotificationSetting notificationSetting = UserNotificationSetting.getDefaultInstance();
      notifications.resetSettingForm(notificationSetting);
      notifications.notificationService.saveUserNotificationSetting(notificationSetting.setUserId(Utils.getOwnerRemoteId()));
      event.getRequestContext().addUIComponentToUpdateByAjax(notifications);
    }
  }
}
