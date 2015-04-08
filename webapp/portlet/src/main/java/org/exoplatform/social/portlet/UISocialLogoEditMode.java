/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.social.portlet;

import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.URLValidator;

@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl",
  events = {
    @EventConfig(listeners = UISocialLogoEditMode.SaveActionListener.class)
  }
)
public class UISocialLogoEditMode extends UIForm {

  final static private String FIELD_URL = "logoUrl";

  public UISocialLogoEditMode() throws Exception {
    PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    PortletPreferences pref = pcontext.getRequest().getPreferences();
    UIFormStringInput fieldUrl = new UIFormStringInput(FIELD_URL, FIELD_URL, pref.getValue("url", ""));
    fieldUrl.setHTMLAttribute("title", FIELD_URL);
    addUIFormInput(fieldUrl);
  }

  static public class SaveActionListener extends EventListener<UISocialLogoEditMode> {
    public void execute(Event<UISocialLogoEditMode> event) throws Exception {
      UISocialLogoEditMode uiForm = event.getSource();
      String url = uiForm.getUIStringInput(FIELD_URL).getValue();
      if ((url == null || url.trim().length() == 0)
          || (!url.trim().matches(URLValidator.URL_REGEX))) {
        UISocialLogoPortlet uiPortlet = uiForm.getParent();
        uiForm.getUIStringInput(FIELD_URL).setValue(uiPortlet.getURL());
        Object[] args = { FIELD_URL, "URL" };
        throw new MessageException(new ApplicationMessage("ExpressionValidator.msg.value-invalid",
                                                          args));
      }
      PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
      PortletPreferences pref = pcontext.getRequest().getPreferences();
      pref.setValue("url", uiForm.getUIStringInput(FIELD_URL).getValue());
      pref.store();

      UIPortalApplication portalApp = Util.getUIPortalApplication();
      if (portalApp.getModeState() == UIPortalApplication.NORMAL_MODE) {
        pcontext.setApplicationMode(PortletMode.VIEW);
      }
    }
  }
}
