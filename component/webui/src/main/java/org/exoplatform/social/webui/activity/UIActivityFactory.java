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
import java.util.Map;

import javax.inject.Provider;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
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
  
  private Map<String, Provider<BaseUIActivityBuilder>> builders = new Hashtable<String, Provider<BaseUIActivityBuilder>>();
  
  private UIExtensionManager extensionManager;
  
  public UIActivityFactory() {
    extensionManager = (UIExtensionManager) PortalContainer.getInstance().getComponentInstanceOfType(UIExtensionManager.class);
  }

  /**
   * Find BaseUIActivity which compatible with Activity's type, and then add UIComponent the parent UI.
   * 
   * @param activity
   * @param parent
   * @return
   * @throws Exception
   */
  public BaseUIActivity addChild(ExoSocialActivity activity, UIContainer parent) throws Exception {
    return buildActivity(activity, parent, activity.getType() != null ? activity.getType() : UIDefaultActivity.ACTIVITY_TYPE);
  }

  private BaseUIActivity buildActivity(ExoSocialActivity activity, UIContainer parent, String type) throws Exception {
    
    UIExtension activityExtension = extensionManager.getUIExtension(BaseUIActivity.class.getName(), type);
    if (activityExtension == null) {
      activityExtension = extensionManager.getUIExtension(BaseUIActivity.class.getName(), UIDefaultActivity.ACTIVITY_TYPE);
    }
    BaseUIActivity uiActivity = (BaseUIActivity) extensionManager.addUIExtension(activityExtension, null, parent);
    uiActivity.setId(uiActivity.getId().replace(":","_") + "_" + uiActivity.hashCode());

    //populate data for this uiActivity
    registerBuilder((UIActivityExtension) activityExtension);
    BaseUIActivityBuilder builder = getBuilder(type);
    return builder.populateData(uiActivity, activity);
  }

  private BaseUIActivityBuilder getBuilder(String activityType) {
    Provider<BaseUIActivityBuilder> providerBuilder = builders.get(activityType);
    if(providerBuilder == null) {
      providerBuilder = builders.get(UIDefaultActivity.ACTIVITY_TYPE);
    }
    return providerBuilder.get();
  }

  private void registerBuilder(final UIActivityExtension activityExtension) throws Exception {
    String activityType = activityExtension.getName();
    if (builders.containsKey(activityType) == false) {
      builders.put(activityType, new BuilderProvider<BaseUIActivityBuilder>(activityExtension));
    }
    
  }
  
  /**
   * Lazy creating the BaseUIActivityBuilder
   * @author thanh_vucong
   *
   * @param <T>
   */
  static class BuilderProvider<T extends BaseUIActivityBuilder> implements Provider<T> {

    final UIActivityExtension activityExtension;
    T instance = null;
    public BuilderProvider(UIActivityExtension activityExtension) {
      this.activityExtension = activityExtension;
    }

    public T get() {
      if (instance != null) {
        return instance;
      }
      
      try {
        Class<T> builderClass = (Class<T>) Thread.currentThread().getContextClassLoader()
                                            .loadClass(this.activityExtension.getActivityBuiderClass());
        instance = (T) builderClass.newInstance();
      } catch (Exception e) {
        LOG.error(e);
      }

      return instance;
    }
    
  }
}
