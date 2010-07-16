package org.exoplatform.social.webui.composer;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by IntelliJ IDEA.
 * User: zun
 * Date: Jun 29, 2010
 * Time: 10:36:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class UIActivityComposerManager extends BaseComponentPlugin {
  private static final Log LOG = ExoLogger.getLogger(UIActivityComposerManager.class);

  private static final String DEFAULT_ACTIVITY_COMPOSER = "DEFAULT_ACTIVITY_COMPOSER";
  private static final String OTHER_ACTIVITY_COMPOSER = "OTHER_ACTIVITY_COMPOSER";
  
  private List<UIActivityComposer> activityComposers = new ArrayList<UIActivityComposer>();
  private UIActivityComposer currentActivityComposer = null;
  private UIActivityComposer defaultActivityComposer = null;

  public UIActivityComposerManager(InitParams initParams) {
    try {
      loadDefaultActivityComposer(initParams);
//      loadOtherActivityComposers(initParams);
    } catch (Exception e) {
      LOG.error(e);
      e.printStackTrace();
    }
  }

  private void loadDefaultActivityComposer(InitParams initParams) throws Exception{
    final ValueParam valueParam = initParams.getValueParam(DEFAULT_ACTIVITY_COMPOSER);
    final String defaultComposerClassStr = valueParam.getValue();

    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    final Class<UIActivityComposer> defaultComposerClass = (Class<UIActivityComposer>) classLoader.loadClass(defaultComposerClassStr);
    
    defaultActivityComposer = defaultComposerClass.newInstance();
    setCurrentActivityComposer(defaultActivityComposer);
  }

//  private void loadOtherActivityComposers(InitParams initParams) throws Exception{
//    final ValuesParam valuesParam = initParams.getValuesParam(OTHER_ACTIVITY_COMPOSER);
//    final ArrayList<String> composerList = valuesParam.getValues();
//
//    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
//    UIForm uiComponentCreator = new UIForm();
//    for (int i = 0; i < composerList.size(); i++) {
//      //get java class
//      final String composerClassStr = composerList.get(i);
//      final Class<UIActivityComposer> composerClass = (Class<UIActivityComposer>) classLoader.loadClass(composerClassStr);
//
//      //create activity composer
//      final UIActivityComposer uiActivityComposer = uiComponentCreator.addChild(composerClass,null,null);
//      uiActivityComposer.setParent(null);
//
//      //load its config
//      final ValuesParam config = initParams.getValuesParam(composerClassStr);
//      if(config != null){
//        uiActivityComposer.loadConfig(config);
//      }
//
//      //register composer
//      registerActivityComposer(uiActivityComposer);
//    }
//  }

  public void registerActivityComposer(UIActivityComposerPlugin activityComposerPlugin) throws Exception {
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    final Class<UIActivityComposer> composerClass = (Class<UIActivityComposer>) classLoader.loadClass(activityComposerPlugin.getClazz());

    //create activity composer
    UIForm uiComponentCreator = new UIForm();
    final UIActivityComposer uiActivityComposer = uiComponentCreator.addChild(composerClass,null,null);
    uiActivityComposer.setParent(null);

    //load its config
    final ValuesParam config = activityComposerPlugin.getConfigs();
    if(config != null){
      uiActivityComposer.loadConfig(config);
    }

    //register composer
    registerActivityComposer(uiActivityComposer);
  }
  
  public void setDefaultActivityComposer(){
    currentActivityComposer = defaultActivityComposer;
  }

  public UIActivityComposer getCurrentActivityComposer() {
    return currentActivityComposer;
  }

  public void setCurrentActivityComposer(UIActivityComposer currentActivityComposer) {
    this.currentActivityComposer = currentActivityComposer;
  }

  public void registerActivityComposer(UIActivityComposer activityComposer){
    activityComposers.add(activityComposer);
  }

  public void removeActivityComposer(UIActivityComposer activityComposer){
    activityComposers.remove(activityComposer);
  }

  public List<UIActivityComposer> getAllComposers(){
    return activityComposers;
  }
}