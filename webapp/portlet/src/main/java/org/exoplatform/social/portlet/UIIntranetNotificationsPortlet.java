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
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.json.JSONObject;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Nov 14, 2014  
 */
@ComponentConfig(
 lifecycle = UIApplicationLifecycle.class,
 template = "app:/groovy/social/portlet/UIIntranetNotificationsPortlet.gtmpl"
)
public class UIIntranetNotificationsPortlet extends UIPortletApplication {
  private static final Log LOG = ExoLogger.getLogger(UIIntranetNotificationsPortlet.class);
  private final WebNotificationService webNotifService;
  private static final String LOAD_MORE_KEY = "loadMoreNotif";
  private static final String REMOVE_ITEM_KEY = "removeNotif";
  private static final String ADD_ITEM_KEY = "addNotif";
  private static final int ITEMS_LOADED_NUM = 21;
  private static final int ITEMS_PER_PAGE = 20;
  private String currentUser = "";
  private int offset = 0;
  private boolean hasMore = false;
  
  public UIIntranetNotificationsPortlet() throws Exception {
    webNotifService = getApplicationComponent(WebNotificationService.class);
  }
  
  @Override
  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    this.currentUser = context.getRemoteUser();
    this.offset = 0;
    this.hasMore = false;
    //
    super.processRender(app, context);
  }
  
  @Override
  public void serveResource(WebuiRequestContext context) throws Exception {
    super.serveResource(context);
    ResourceRequest req = context.getRequest();
    String resourceId = req.getResourceID();
    //
    if (LOAD_MORE_KEY.equals(resourceId) && hasMore) {
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
      object.put("hasMore", String.valueOf(hasMore));
      //
      res.getWriter().write(object.toString());
    }
    if (REMOVE_ITEM_KEY.equals(resourceId) && (offset > 0)) {
      --offset;
    }
    if (ADD_ITEM_KEY.equals(resourceId)) {
      ++offset;
    }
  }

  protected String getLoadMoreURL() {
    return buildResourceURL(LOAD_MORE_KEY);
  }

  protected String buildResourceURL(String key) {
    try {
      WebuiRequestContext ctx = WebuiRequestContext.getCurrentInstance();
      MimeResponse res = ctx.getResponse();
      ResourceURL rsURL = res.createResourceURL();
      rsURL.setResourceID(key);
      return rsURL.toString();
    } catch (Exception e) {
      return "";
    }
  }

  protected List<String> getNotifications() throws Exception {
    WebNotificationFilter filter = new WebNotificationFilter(currentUser);
    if (hasMore) {
      offset += ITEMS_PER_PAGE;
    }
    LOG.debug("Current offset of page: " + offset);
    List<String> notificationContents = webNotifService.get(filter, offset, ITEMS_LOADED_NUM);
    hasMore = (notificationContents.size() > ITEMS_PER_PAGE);
    //
    return (hasMore) ? notificationContents.subList(0, ITEMS_PER_PAGE) : notificationContents;
  }
  
  protected String getUserNotificationSettingUrl() {
    return LinkProvider.getUserNotificationSettingUri(currentUser);
  }

  protected List<String> getActions() {
    return Arrays.asList(ADD_ITEM_KEY, REMOVE_ITEM_KEY);
  }
}
