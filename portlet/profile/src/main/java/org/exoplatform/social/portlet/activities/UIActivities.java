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
package org.exoplatform.social.portlet.activities;

import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.portlet.URLUtils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;

import java.util.List;

@ComponentConfig(
    template =  "app:/groovy/portal/webui/component/UIActivities.gtmpl"
)
public class UIActivities  extends UIContainer {



  public List<Activity> getActivities() throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    ActivityManager am = (ActivityManager) container.getComponentInstanceOfType(ActivityManager.class);
    IdentityManager im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);

    Identity id = im.getIdentityByRemoteId("organization", URLUtils.getCurrentUser());

    return am.getActivities(id);
  }
}
