package org.exoplatform.social.user.portlet;

import javax.portlet.PortletMode;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.core.UIPortletApplication;

public abstract class UIAbstractUserPortlet extends UIPortletApplication {
  protected Profile currentProfile;

  public UIAbstractUserPortlet() throws Exception {
    super();
  }

  @Override
  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    PortletRequestContext portletReqContext = (PortletRequestContext) context;
    PortletMode portletMode = portletReqContext.getApplicationMode();
    if (portletMode == PortletMode.VIEW) {
      Identity ownerIdentity = Utils.getOwnerIdentity(false);
      currentProfile = ownerIdentity.getProfile();
    }
    //
    super.processRender(app, context);
  }
}
