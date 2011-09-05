/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

import org.exoplatform.social.webui.space.UIManageAllSpaces;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Displays all type of spaces.
 * 
 * @author <a href="mailto:hanhvq@exoplatform.com">Hanh Vi Quoc</a>
 * @since Aug 18, 2011
 * @since 1.2.2
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/social/portlet/UIAllSpacesPortlet.gtmpl"
)
public class UIAllSpacesPortlet extends UIPortletApplication {
  /**
   * constructor
   * @throws Exception
   */
  public UIAllSpacesPortlet() throws Exception {
    addChild(UIManageAllSpaces.class, null, null);
  }
}