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
package org.exoplatform.social.webui.activity;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * UIActivitiesContainer.java
 *
 * @author    <a href="http://hoatle.net">hoatle</a>
 * @since 	  Apr 12, 2010
 * @copyright eXo Platform SAS
 */
@ComponentConfig(
  template = "classpath:groovy/social/webui/UIActivitiesContainer.gtmpl"
)
public class UIActivitiesContainer extends UIContainer {
  private List<Activity> activityList;
  /**
   * constructor
   */
  public UIActivitiesContainer() {
  }

  public UIActivitiesContainer setActivityList(List<Activity> activityList) throws Exception {
    this.activityList = activityList;
    init();
    return this;
  }

  /**
   * initializes ui component child
   * @throws Exception
   */
  private void init() throws Exception {
    if (activityList == null) {
      activityList = new ArrayList<Activity>();
    }
    
    PortalContainer portalContainer = PortalContainer.getInstance();
    UIActivityFactory factory = (UIActivityFactory) portalContainer.getComponentInstanceOfType(UIActivityFactory.class);
    for (Activity activity : activityList) {      
      factory.addChild(activity, this);
    }
  }
}