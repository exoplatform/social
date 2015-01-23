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

import org.exoplatform.social.user.form.UIEditUserProfileForm;
import org.exoplatform.social.webui.composer.PopupContainer;
import org.exoplatform.web.application.RequireJS;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/social/portlet/user/UIEditUserProfilePortlet.gtmpl"
)
public class UIEditUserProfilePortlet extends UIAbstractUserPortlet {

  public UIEditUserProfilePortlet() throws Exception {
    addChild(UIEditUserProfileForm.class, null, null);
    addChild(PopupContainer.class, null, "AvatarPopupContainer");
  }

  @Override
  public void beforeProcessRender(WebuiRequestContext context) {
    super.beforeProcessRender(context);
    //
    RequireJS requireJs = context.getJavascriptManager().getRequireJS();
    requireJs.require("SHARED/user-profile", "userprofile").addScripts("userprofile.init('" + getId() + "');");
  }
  
}
