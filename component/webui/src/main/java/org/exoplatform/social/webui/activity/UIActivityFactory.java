package org.exoplatform.social.webui.activity;

import java.util.Hashtable;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.webui.activity.default_.UIDefaultActivity;
import org.exoplatform.webui.ext.UIExtension;
import org.exoplatform.webui.ext.UIExtensionManager;

/**
 * Created by IntelliJ IDEA.
 * User: zun
 * Date: Jun 22, 2010
 * Time: 10:54:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class UIActivityFactory extends BaseComponentPlugin {
  private static final Log LOG = ExoLogger.getLogger(UIActivityFactory.class);
  private Hashtable<String, BaseUIActivityBuilder> builders = new Hashtable<String, BaseUIActivityBuilder>();

  public UIActivityFactory() {
  }

  public BaseUIActivity addChild(Activity activity, UIContainer parent) throws Exception {
    final String type = activity.getType();
    if(type!=null){
      return buildActivity(activity, parent, type);
    } else {
      return buildActivity(activity, parent, UIDefaultActivity.ACTIVITY_TYPE);
    }
  }

  private BaseUIActivity buildActivity(Activity activity, UIContainer parent, String type) throws Exception {
    UIExtensionManager extensionManager = (UIExtensionManager) PortalContainer.getInstance().getComponentInstanceOfType(UIExtensionManager.class);
    UIExtension activityExtension = extensionManager.getUIExtension(BaseUIActivity.class.getName(), type);
    BaseUIActivity uiActivity = (BaseUIActivity) extensionManager.addUIExtension(activityExtension, null, parent);

    uiActivity.setId(uiActivity.getId()+"_"+uiActivity.hashCode());

    //populate data for this uiActivity
    BaseUIActivityBuilder builder = getBuilder(type);
    return builder.populateData(uiActivity, activity);
  }

  private BaseUIActivityBuilder getBuilder(String activityType) {
    BaseUIActivityBuilder builder = builders.get(activityType);
    if(builder == null){
      throw new IllegalArgumentException("No builder is registered for type :" +activityType);
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