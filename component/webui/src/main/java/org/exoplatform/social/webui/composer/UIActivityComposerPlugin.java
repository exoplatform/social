package org.exoplatform.social.webui.composer;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;

/**
 * Created by IntelliJ IDEA.
 * User: zun
 * Date: Jul 16, 2010
 * Time: 1:27:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class UIActivityComposerPlugin extends BaseComponentPlugin{
  private String clazz;
  private ValuesParam configs;

  public UIActivityComposerPlugin(InitParams initParams) {
    clazz = initParams.getValueParam("ComposerClass").getValue();
    configs = initParams.getValuesParam("ComposerConfig");
  }

  public String getClazz() {
    return clazz;
  }

  public ValuesParam getConfigs() {
    return configs;
  }
}

