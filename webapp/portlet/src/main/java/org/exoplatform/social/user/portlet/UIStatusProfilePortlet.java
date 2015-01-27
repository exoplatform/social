/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.social.user.portlet;

import java.util.ResourceBundle;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.user.UserStateService;
import org.exoplatform.social.user.portlet.UserProfileHelper.StatusIconCss;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/social/portlet/user/UIStatusProfilePortlet.gtmpl"
)
public class UIStatusProfilePortlet extends UIAbstractUserPortlet {
  public static final String OFFLINE_STATUS        = "offline";
  public static final String OFFLINE_TITLE         = "UIStatusProfile.title.offline";
  public static final String USER_STATUS_TITLE     = "UIStatusProfile.title.";

  public UIStatusProfilePortlet() throws Exception {
  }

  protected StatusInfo getStatusInfo() {
    StatusInfo si = new StatusInfo();
    ResourceBundle rb = PortalRequestContext.getCurrentInstance().getApplicationResourceBundle();
    UserStateService stateService = getApplicationComponent(UserStateService.class);
    boolean isOnline = stateService.isOnline(currentProfile.getIdentity().getRemoteId());
    if (isOnline) {
      String status = stateService.getUserState(currentProfile.getIdentity().getRemoteId()).getStatus();
      si.setCssName(StatusIconCss.getIconCss(status));   
      si.setTitle(rb.getString(USER_STATUS_TITLE + status));
    } else {
      si.setCssName(StatusIconCss.getIconCss(OFFLINE_STATUS));
      si.setTitle(rb.getString(OFFLINE_TITLE));
    }
    
    return si;
  }
  
  class StatusInfo {
    private String title;
    private String cssName;
    public String getTitle() {
      return title;
    }
    public void setTitle(String title) {
      this.title = title;
    }
    public String getCssName() {
      return cssName;
    }
    public void setCssName(String cssName) {
      this.cssName = cssName;
    }
  }
}
