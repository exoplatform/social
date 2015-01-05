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

import javax.portlet.MimeResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceURL;

import org.exoplatform.commons.api.notification.model.WebNotificationFilter;
import org.exoplatform.commons.api.notification.service.WebNotificationService;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.json.JSONObject;

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
     @EventConfig(listeners = UIIntranetNotificationsPortlet.RemoveActionListener.class)
   }
)
public class UIIntranetNotificationsPortlet extends UIPortletApplication {
  private final WebNotificationService webNotifService;
  private static final String LOAD_MORE_KEY = "loadMoreNotif";
  private static final int ITEMS_LOADED_NUM = 21;
  private static final int ITEMS_PER_PAGE = 20;
  private String currentUser = "";
  private int offset = 0;
  private int currentPage = 0;
  private boolean hasMore = false;
  
  public UIIntranetNotificationsPortlet() throws Exception {
    webNotifService = getApplicationComponent(WebNotificationService.class);
  }
  
  @Override
  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    this.currentUser = context.getRemoteUser();
    super.processRender(app, context);
  }
  
  @Override
  public void serveResource(WebuiRequestContext context) throws Exception {
    super.serveResource(context);
    ResourceRequest req = context.getRequest();
    String resourceId = req.getResourceID();
    //
    if (LOAD_MORE_KEY.equals(resourceId) && isHasMore()) {
      ++currentPage;
      //
      List<String> moreNotifications = getNotifications();
      //
      StringBuffer sb = new StringBuffer();
      for (String notif : moreNotifications) {
        sb.append(notif);  
      }
      //
      MimeResponse res = context.getResponse();
      res.setContentType("application/json");
      //
      JSONObject object = new JSONObject();
      object.put("context", sb.toString());
      object.put("currentPage", String.valueOf(currentPage));
      object.put("hasMore", String.valueOf(isHasMore()));
      //
      res.getWriter().write(object.toString());
    }
  }
  
  protected String buildResourceURL() {
    try {
      WebuiRequestContext ctx = WebuiRequestContext.getCurrentInstance();
      MimeResponse res = ctx.getResponse();
      ResourceURL rsURL = res.createResourceURL();
      rsURL.setResourceID(LOAD_MORE_KEY);
      return rsURL.toString();
    } catch (Exception e) {
      return "";
    }
  }

  protected List<String> getNotifications() throws Exception {
    WebNotificationFilter filter = new WebNotificationFilter(currentUser);
    
    if (hasMore) {
      offset = ITEMS_PER_PAGE;
    } else {
      offset = 0;
      currentPage = 0;
    }
    
    List<String> notificationContents = webNotifService.get(filter, offset * currentPage, ITEMS_LOADED_NUM);
    if (notificationContents.size() > ITEMS_PER_PAGE) {
      hasMore = true;
      return notificationContents.subList(0, ITEMS_PER_PAGE);
    } else {
      hasMore = false;
    }
    return notificationContents;
  }
  
  public boolean isHasMore() {
    return hasMore;
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
      String id = event.getRequestContext().getRequestParameter(OBJECTID);
      UIIntranetNotificationsPortlet portlet = event.getSource();
      portlet.webNotifService.markRead(id);
      // Ignore reload portlet
      ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
    }
  }
  
  public static class RemoveActionListener extends EventListener<UIIntranetNotificationsPortlet> {
    public void execute(Event<UIIntranetNotificationsPortlet> event) throws Exception {
      String id = event.getRequestContext().getRequestParameter(OBJECTID);
      UIIntranetNotificationsPortlet portlet = event.getSource();
      portlet.webNotifService.remove(id);
      // Ignore reload portlet
      ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);
    }
  }
}
