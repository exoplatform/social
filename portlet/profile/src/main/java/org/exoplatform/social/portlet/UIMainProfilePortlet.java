package org.exoplatform.social.portlet;

import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.social.portlet.profilelist.UIProfileList;
import org.exoplatform.social.portlet.profile.UIProfile;
import org.exoplatform.social.portlet.dashboard.UISocialDashboard;
import org.exoplatform.social.portlet.activities.UIActivities;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.application.PortalRequestContext;

@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template = "app:/groovy/portal/webui/component/UIMainProfilePortlet.gtmpl"
)
public class UIMainProfilePortlet extends UIPortletApplication {

  public UIMainProfilePortlet() throws Exception {
    addChild(UIProfileList.class, null, null);
    addChild(UIProfile.class, null, null);
    addChild(UISocialDashboard.class, null, null);
    addChild(UIActivities.class, null, null);
  }
}
