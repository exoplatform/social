/*
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
package org.exoplatform.social.portlet;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Provides links for People Directory and User's connections<br>
 *
 */
@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template = "app:/groovy/social/portlet/UIPeopleToolBarPortlet.gtmpl"
)
public class UIPeopleToolBarPortlet extends UIPortletApplication {

  /**
   * Constructor
   * @throws Exception
   */
  public UIPeopleToolBarPortlet() throws Exception {

  }
}
