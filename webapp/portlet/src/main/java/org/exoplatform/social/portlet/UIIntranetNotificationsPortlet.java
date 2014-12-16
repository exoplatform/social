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

import java.util.Arrays;
import java.util.List;

import org.exoplatform.commons.api.notification.service.WebNotificationService;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.service.LinkProvider;
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
  
  private WebNotificationService webNotifService;
  
  private String currentUser = "";
  
  private static final Log LOG = ExoLogger.getLogger(UIIntranetNotificationsPortlet.class);

  public UIIntranetNotificationsPortlet() throws Exception {
    currentUser = WebuiRequestContext.getCurrentInstance().getRemoteUser();
    webNotifService = getApplicationComponent(WebNotificationService.class);
  }
  
  @Override
  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    this.currentUser = context.getRemoteUser();
    super.processRender(app, context);
  }
  
  protected List<String> getNotifications() throws Exception {
    //TODO Implement this process on the WebNotificationService
    //return dataStorage.getNotificationContent(currentUser, false);
    return null;
  }
  
  
  protected String getUserNotificationSettingUrl() {
    return LinkProvider.getUserNotificationSettingUri(currentUser);
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
      portlet.webNotifService.markRead(notificationId);
      // Ignore reload portlet
      ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
    }
  }
  
  public static class RemoveActionListener extends EventListener<UIIntranetNotificationsPortlet> {
    public void execute(Event<UIIntranetNotificationsPortlet> event) throws Exception {
      String notificationId = event.getRequestContext().getRequestParameter(OBJECTID);
      UIIntranetNotificationsPortlet portlet = event.getSource();
      LOG.info("Run action RemoveActionListener: " + notificationId);
      portlet.webNotifService.remove(portlet.currentUser, notificationId);
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
