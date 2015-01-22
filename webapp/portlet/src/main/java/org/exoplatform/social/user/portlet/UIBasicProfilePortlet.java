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

import java.util.Map;

import org.exoplatform.web.application.RequireJS;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/social/portlet/user/UIBasicProfilePortlet.gtmpl"
)
public class UIBasicProfilePortlet extends UIAbstractUserPortlet {
  final private static String URL_KEY = "url";
  
  public UIBasicProfilePortlet() throws Exception {
  }

  @Override
  public void beforeProcessRender(WebuiRequestContext context) {
    super.beforeProcessRender(context);
    //
    RequireJS requireJs = context.getJavascriptManager().getRequireJS();
    requireJs.require("SHARED/edit-user-profile", "profile").addScripts("profile.init('" + getId() + "');");
  }

  protected Map<String, Object> getProfileInfo() {
    return UserProfileHelper.getDisplayProfileInfo(currentProfile);
  }

  protected boolean isString(Object s) {
    return s instanceof String;
  }

  protected boolean isURL(String key) {
    if (key == null) return false;
    return key.startsWith(URL_KEY);  
  }
}



















