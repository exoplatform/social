package org.exoplatform.social.portlet.breadcrumb;

import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfig;

@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class
)
public class UIBreadcrumbPortlet  extends UIPortletApplication {

  public UIBreadcrumbPortlet() throws Exception {
    super();
    addChild(UIBreadcrumb.class, null, null);
  }
}
