package org.exoplatform.social.user;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.user.portlet.UIRecentActivitiesPortlet;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
@ComponentConfig(
   template = "war:/groovy/social/webui/profile/UIRecentActivity.gtmpl"
)
public class UIRecentActivity extends UIContainer {
  public static String COMPONENT_ID = "Activity";
  private ExoSocialActivity activity = null;

  public UIRecentActivity() {
  }

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    super.processRender(context);
    ((UIRecentActivitiesPortlet) getParent()).initProfilePopup();
  }

  public static String buildComponentId(String activityId) {
    return COMPONENT_ID + activityId;
  }

  protected ExoSocialActivity getActivity() {
    return this.activity;
  }

  public void setActivity(ExoSocialActivity activity) {
    this.activity = activity;
  }
}
