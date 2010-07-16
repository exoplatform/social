package org.exoplatform.social.webui.activity;

import org.exoplatform.social.core.activity.model.Activity;

/**
 * Created by IntelliJ IDEA.
 * User: zun
 * Date: Jun 22, 2010
 * Time: 11:46:12 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class BaseUIActivityBuilder {
  public BaseUIActivity populateData(BaseUIActivity uiActivity, Activity activity){
    initBaseUIActivity(uiActivity, activity);
    extendUIActivity(uiActivity, activity);
    return uiActivity;
  }

  private void initBaseUIActivity(BaseUIActivity uiActivity, Activity activity) {
    uiActivity.setActivity(activity);
  }

  protected abstract void extendUIActivity(BaseUIActivity uiActivity, Activity activity);
}