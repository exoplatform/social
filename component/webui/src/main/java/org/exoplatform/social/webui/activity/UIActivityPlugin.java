package org.exoplatform.social.webui.activity;

import java.util.ArrayList;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;

/**
 * Created by IntelliJ IDEA.
 * User: zun
 * Date: Jul 16, 2010
 * Time: 1:32:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class UIActivityPlugin extends BaseComponentPlugin {
  private String activityType;
  private String activityClass;
  private String builderClass;

  public UIActivityPlugin(InitParams params) {
    final ValuesParam valuesParam = params.getValuesParamIterator().next();
    final ArrayList<String> values = valuesParam.getValues();
    final String[] strValues = values.toArray(new String[values.size()]);

    //get string configs
    activityType = strValues[0];
    activityClass= strValues[1];
    builderClass = strValues[2];
  }

  public String getActivityType() {
    return activityType;
  }

  public String getActivityClass() {
    return activityClass;
  }

  public String getBuilderClass() {
    return builderClass;
  }
}
