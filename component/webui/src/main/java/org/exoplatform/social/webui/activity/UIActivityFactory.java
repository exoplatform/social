package org.exoplatform.social.webui.activity;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.form.UIForm;

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
  private Hashtable<String, Class<?>> classes = new Hashtable<String, Class<?>>();
  private UIForm uiElementCreator = new UIForm();

  public UIActivityFactory(InitParams params) {
    try {
        getConfigs(params);
    } catch (Exception e) {
      LOG.error(e);
    }
  }

  private void getConfigs(InitParams initParams) throws Exception {
    final Iterator<ValuesParam> iterator = initParams.getValuesParamIterator();
    while(iterator.hasNext()){

      //get configs from xml
      final ValuesParam valuesParam = iterator.next();
      final ArrayList<String> values = valuesParam.getValues();
      final String[] strValues = values.toArray(new String[values.size()]);
      
      //get string configs
      String activityType = strValues[0];
      String uiActivityClazzStr = strValues[1];
      String uiActivityBuilderClazzStr = strValues[2];

      //get class instances
      final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      final Class<BaseUIActivityBuilder> uiActivityBuilderClazz = (Class<BaseUIActivityBuilder>) classLoader.loadClass(uiActivityBuilderClazzStr);
      final Class<UIComponent> uiActivityClazz = (Class<UIComponent>) classLoader.loadClass(uiActivityClazzStr);

      //registering
      registerBuilder(activityType, uiActivityBuilderClazz.newInstance());
      registerClass(activityType, uiActivityClazz);
    }
  }

  public BaseUIActivity create(Activity activity) throws Exception {
    final Class<UIComponent> uiClazz = (Class<UIComponent>) getClass(activity.getType());

    //create uiActivity via form and its class for fully created instance
    final BaseUIActivity uiActivity = (BaseUIActivity) uiElementCreator.addChild(uiClazz, null, uiClazz.getSimpleName() + activity.getId());

    //remove uiActivity from the creator immediately     (MUST-HAVE)
    uiElementCreator.removeChildById(uiActivity.getId());

    //populate data for this uiActivity
    final String type = activity.getType();
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

  public void registerBuilder(String activityType, BaseUIActivityBuilder builder){
    if(builders.contains(activityType)){
      builders.remove(activityType);
    }

    builders.put(activityType, builder);
  }

  public void registerClass(String activityType, Class<?> clazz){
    if(classes.contains(activityType)){
      classes.remove(activityType);
    }

    classes.put(activityType, clazz);
  }

  private Class<?> getClass(String activityType) {
    Class<?> clazz = classes.get(activityType);
    if(clazz == null){
      throw new IllegalArgumentException("No builder is registered for type :" +activityType);
    }
    return clazz;
  }
}