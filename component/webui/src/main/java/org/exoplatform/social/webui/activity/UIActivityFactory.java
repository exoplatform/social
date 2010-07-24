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

import java.util.Hashtable;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.webui.ext.UIExtension;
import org.exoplatform.webui.ext.UIExtensionManager;

/**
 * UIActivityFactory.java
 *
 * @author    zun
 * @since     Jul 22, 2010
 * @copyright eXo Platform SAS
 */
public class UIActivityFactory extends BaseComponentPlugin {
  private static final Log LOG = ExoLogger.getLogger(UIActivityFactory.class);
  private Hashtable<String, BaseUIActivityBuilder> builders = new Hashtable<String, BaseUIActivityBuilder>();

  public UIActivityFactory() {
  }

  public BaseUIActivity addChild(Activity activity, UIContainer parent) throws Exception {
    final String type = activity.getType();
    if(type != null) {
      return buildActivity(activity, parent, type);
    } else {
      return buildActivity(activity, parent, UIDefaultActivity.ACTIVITY_TYPE);
    }
  }

  private BaseUIActivity buildActivity(Activity activity, UIContainer parent, String type) throws Exception {
    UIExtensionManager extensionManager = (UIExtensionManager) PortalContainer.getInstance().getComponentInstanceOfType(UIExtensionManager.class);
    UIExtension activityExtension = extensionManager.getUIExtension(BaseUIActivity.class.getName(), type);
    if (activityExtension == null) {
      activityExtension = extensionManager.getUIExtension(BaseUIActivity.class.getName(), UIDefaultActivity.ACTIVITY_TYPE);
    }
    BaseUIActivity uiActivity = (BaseUIActivity) extensionManager.addUIExtension(activityExtension, null, parent);

    uiActivity.setId(uiActivity.getId() + "_" + uiActivity.hashCode());

    //populate data for this uiActivity
    BaseUIActivityBuilder builder = getBuilder(type);
    return builder.populateData(uiActivity, activity);
  }

  private BaseUIActivityBuilder getBuilder(String activityType) {
    BaseUIActivityBuilder builder = builders.get(activityType);
    if(builder == null){
      builder = builders.get(UIDefaultActivity.ACTIVITY_TYPE);
      //throw new IllegalArgumentException("No builder is registered for type :" +activityType);
    }
    return builder;
  }

  public void registerBuilder(ActivityBuilderPlugin activityBuilderPlugin){
    String activityType = activityBuilderPlugin.getActivityType();

    if(builders.contains(activityType)){
      builders.remove(activityType);
    }

    builders.put(activityType, activityBuilderPlugin.getActivityBuilder());
  }
}