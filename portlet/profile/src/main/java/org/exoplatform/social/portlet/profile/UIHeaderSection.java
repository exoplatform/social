/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.portlet.profile;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;


@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/portal/webui/component/UIHeaderSection.gtmpl",
    events = {
        @EventConfig(listeners = UIHeaderSection.EditActionListener.class, phase = Phase.DECODE),
        @EventConfig(listeners = UIHeaderSection.SaveActionListener.class)
    }
)
public class UIHeaderSection extends UIProfileSection {
  
  public UIHeaderSection() throws Exception {
    addUIFormInput(new UIFormStringInput("profilePosition", null, null).
                   addValidator(MandatoryValidator.class).
                   addValidator(StringLengthValidator.class, 3, 30));
  }

  public static class SaveActionListener extends EventListener<UIHeaderSection> {
    public void execute(Event<UIHeaderSection> event) throws Exception {
      UIHeaderSection uiHeader = event.getSource();
      uiHeader.setEditMode(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiHeader);
      System.out.println("\n\n\n\n save");
    }
  }
  
  public static class EditActionListener extends EventListener<UIHeaderSection> {
    public void execute(Event<UIHeaderSection> event) throws Exception {
      UIHeaderSection uiHeader = event.getSource();
      uiHeader.setEditMode(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiHeader);
      System.out.println("\n\n\n\n edit");
    }
  }

}
