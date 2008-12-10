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


@ComponentConfig(
    template =  "app:/groovy/portal/webui/component/UIExperienceSection.gtmpl",
    events = {
        @EventConfig(listeners = UIProfileSection.EditActionListener.class),
        @EventConfig(listeners = UIProfileSection.SaveActionListener.class),
        @EventConfig(listeners = UIProfileSection.CancelActionListener.class)
    }
)
public class UIExperienceSection extends UIProfileSection {

  public UIExperienceSection() throws Exception {
    addChild(UITitleBar.class, null, null);
    addChild(UIAddButton.class, null, null);
  }

}