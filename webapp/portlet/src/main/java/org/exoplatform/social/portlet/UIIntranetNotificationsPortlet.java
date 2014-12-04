/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.social.portlet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.NotificationKey;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.notification.service.storage.IntranetNotificationDataStorage;
import org.exoplatform.commons.api.notification.service.storage.NotificationDataStorage;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Nov 14, 2014  
 */
@ComponentConfig(
 lifecycle = UIApplicationLifecycle.class,
 template = "app:/groovy/social/portlet/UIIntranetNotificationsPortlet.gtmpl",
 events = {
     @EventConfig(listeners = UIIntranetNotificationsPortlet.MarkReadActionListener.class),
     @EventConfig(listeners = UIIntranetNotificationsPortlet.RemoveActionListener.class),
     @EventConfig(listeners = UIIntranetNotificationsPortlet.LoadMoreActionListener.class)
   }
)
public class UIIntranetNotificationsPortlet extends UIPortletApplication {
  
  private IntranetNotificationDataStorage dataStorage;
  
  private NotificationDataStorage storage;
  
  private UserSettingService userSettingService;
  
  private String currentUser = "";
  
  private static final Log LOG = ExoLogger.getLogger(UIIntranetNotificationsPortlet.class);

  public UIIntranetNotificationsPortlet() throws Exception {
    currentUser = WebuiRequestContext.getCurrentInstance().getRemoteUser();
    dataStorage = getApplicationComponent(IntranetNotificationDataStorage.class);
    userSettingService = getApplicationComponent(UserSettingService.class);
    storage = getApplicationComponent(NotificationDataStorage.class);
  }
  
  @Override
  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    this.currentUser = context.getRemoteUser();
    super.processRender(app, context);
  }
  
  protected List<String> getNotifications() throws Exception {
    //return dataStorage.getNotificationContent(currentUser, true);
    return createList();
  }
  
  private List<String> createList() {
    List<String> result = new ArrayList<>();
    
    ArgumentLiteral<Boolean> JOB_DAILY = new ArgumentLiteral<Boolean>(Boolean.class, "jobDaily");
    ArgumentLiteral<String> DAY_OF_JOB = new ArgumentLiteral<String>(String.class, "dayOfJob");
    ArgumentLiteral<Boolean> JOB_WEEKLY = new ArgumentLiteral<Boolean>(Boolean.class, "jobWeekly");
    
    NotificationContext context = NotificationContextImpl.cloneInstance();
    context.append(JOB_DAILY, false);
    String dayName = String.valueOf(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
    context.append(DAY_OF_JOB, dayName);
    context.append(JOB_WEEKLY, true);
    UserSetting userSetting = userSettingService.get(currentUser);
    
    Map<NotificationKey, List<NotificationInfo>> notificationMessageMap = storage.getByUser(context, userSetting);
    for (Entry<NotificationKey, List<NotificationInfo>> entry : notificationMessageMap.entrySet()) {
      for (NotificationInfo info : entry.getValue()) {
        String s = buildNotificationMessage(info);
        if (s.length() > 0)
          result.add(s);
      }
    }
    return result;
  }

  private String buildNotificationMessage(NotificationInfo notification) {
    NotificationContext nCtx = NotificationContextImpl.cloneInstance();
    AbstractNotificationPlugin plugin = nCtx.getPluginContainer().getPlugin(notification.getKey());
    try {
      notification.setLastModifiedDate(Calendar.getInstance());
      nCtx.setNotificationInfo(notification);
      return plugin.buildUIMessage(nCtx);
    } catch (Exception e) {
      
    }
    return "";
  }
  
  protected List<String> getActions() {
    return Arrays.asList("MarkRead", "Remove");
  }
  
  protected String getActionUrl(String actionName) throws Exception {
    return event(actionName).replace("javascript:ajaxGet('", "").replace("')", "&" + OBJECTID + "=");
  }
  
  public static class MarkReadActionListener extends EventListener<UIIntranetNotificationsPortlet> {
    public void execute(Event<UIIntranetNotificationsPortlet> event) throws Exception {
      String notificationId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIIntranetNotificationsPortlet portlet = event.getSource();
      LOG.info("Run action MarkReadActionListener");
      portlet.dataStorage.saveRead(portlet.currentUser, notificationId);
      // Ignore reload portlet
      ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
    }
  }
  
  public static class RemoveActionListener extends EventListener<UIIntranetNotificationsPortlet> {
    public void execute(Event<UIIntranetNotificationsPortlet> event) throws Exception {
      String notificationId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIIntranetNotificationsPortlet portlet = event.getSource();
      LOG.info("Run action RemoveActionListener: " + notificationId);
      portlet.dataStorage.remove(portlet.currentUser, notificationId);
      // Ignore reload portlet
      ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
    }
  }
  
  public static class LoadMoreActionListener extends EventListener<UIIntranetNotificationsPortlet> {
    public void execute(Event<UIIntranetNotificationsPortlet> event) throws Exception {
      String notificationId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIIntranetNotificationsPortlet portlet = event.getSource();
      LOG.info("Run action LoadMoreActionListener");
      // Ignore reload portlet
      ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
    }
  }
}
