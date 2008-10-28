package org.exoplatform.social.portlet.dashboard;

import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupMessages;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.dashboard.webui.component.*;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.config.model.Container;

import javax.portlet.PortletPreferences;


@ComponentConfig(
    lifecycle = UIContainerLifecycle.class
)
public class UISocialDashboard extends UIContainer implements DashboardParent{



    public UISocialDashboard() throws Exception {
      addChild(UIDashboard.class, null, null);

    }

    public boolean canEdit() {
      PortletRequestContext context = (PortletRequestContext) WebuiRequestContext
          .getCurrentInstance();
      return getDashboardOwner().equals(context.getRemoteUser());
    }

    public void initNewDashboard(Container root) {
      
    }

    public String getDashboardOwner() {
      PortalRequestContext request = Util.getPortalRequestContext() ;
      String uri = request.getNodePath();

      if (uri.endsWith("/dashboard") && uri.startsWith("/people/")) {
        return uri.substring(8, uri.length() - 10);
      } else {
        PortletRequestContext context = (PortletRequestContext) WebuiRequestContext
          .getCurrentInstance();
        return context.getRemoteUser();
      }
    }


    public UIPopupMessages getUIPopupMessages() {
      return getAncestorOfType(UIApplication.class).getUIPopupMessages();
    }



}
