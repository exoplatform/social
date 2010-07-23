package org.exoplatform.social.webui.activity;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;

/**
 * Created by IntelliJ IDEA.
 * User: zun
 * Date: Jul 21, 2010
 * Time: 11:20:58 AM
 * To change this template use File | Settings | File Templates.
 */
public class ActivityBuilderPlugin extends BaseComponentPlugin {
  private String activityType;
  private BaseUIActivityBuilder activityBuilder;
  private static final String ACTIVITY_TYPE = "ACTIVITY_TYPE";
  private static final String ACTIVITY_BUILDER = "ACTIVITY_BUILDER";

  public ActivityBuilderPlugin(InitParams params) {
    activityType = params.getValueParam(ACTIVITY_TYPE).getValue();    
    activityBuilder = (BaseUIActivityBuilder) params.getObjectParam(ACTIVITY_BUILDER).getObject();
  }

  public BaseUIActivityBuilder getActivityBuilder() {
    return activityBuilder;
  }

  public String getActivityType() {
    return activityType;
  }
}
