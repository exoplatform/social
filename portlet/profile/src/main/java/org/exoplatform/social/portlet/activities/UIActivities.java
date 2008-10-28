package org.exoplatform.social.portlet.activities;

import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.portlet.URLUtils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;

import java.util.List;

@ComponentConfig(
    template =  "app:/groovy/portal/webui/component/UIActivities.gtmpl"
)
public class UIActivities  extends UIContainer {



  public List<Activity> getActivities() throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    ActivityManager am = (ActivityManager) container.getComponentInstanceOfType(ActivityManager.class);
    IdentityManager im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);

    Identity id = im.getIdentityByRemoteId("organization", URLUtils.getCurrentUser());

    return am.getActivities(id);
  }
}
