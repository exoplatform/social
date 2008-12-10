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

import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.core.UIComponent;


@ComponentConfig(
    template =  "app:/groovy/portal/webui/component/UIBasicInfoSection.gtmpl",
    events = {
        @EventConfig(listeners = UIProfileSection.EditActionListener.class),
        @EventConfig(listeners = UIBasicInfoSection.SaveActionListener.class),
        @EventConfig(listeners = UIProfileSection.CancelActionListener.class)
    }
)
public class UIBasicInfoSection extends UIProfileSection {

  public UIBasicInfoSection() throws Exception {
    addChild(UITitleBar.class, null, null);
  }

  public static class SaveActionListener extends UIProfileSection.SaveActionListener {

    public void execute(Event<UIProfileSection> event) throws Exception {
      System.out.println("execute SaveActionListener of UIBasicInfoSection");
      super.execute(event);
      UIProfileSection sect = event.getSource();
              
      //we try to get the UIHeaderSection to refresh it since it contains also
      //the firstname and lastname
      UIComponent parent = sect.getParent();
      UIProfileSection header = parent.findFirstComponentOfType(UIHeaderSection.class);
      if(header != null) {
        event.getRequestContext().addUIComponentToUpdateByAjax(header);
      }
    }
  }

}
