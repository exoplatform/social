/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.portlet;

import javax.portlet.PortletRequest;

import org.exoplatform.dashboard.webui.component.DashboardParent;
import org.exoplatform.dashboard.webui.component.UIDashboard;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.space.UISpaceAddForm;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
/**
 * {@link UIHomeSpacePortlet} used for default space's home page.
 * Created by The eXo Platform SARL
 * @author <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 * @since Oct 23, 2008
 */

@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template = "app:/groovy/social/portlet/UIHomeSpacePortlet.gtmpl"
)
public class UIHomeSpacePortlet extends UIPortletApplication implements DashboardParent {

  private static final Log LOG = ExoLogger.getLogger(UIHomeSpacePortlet.class);
  
  private String DEFAULT_TEMPLATE = "home-spaces";
  private String DEFAULT_RSSFETCH_ID = "rssFetch";
  /**
   * constructor
   * @throws Exception
   */
  public UIHomeSpacePortlet() throws Exception {
    UIDashboard uiDashboard = addChild(UIDashboard.class, null, null);
    PortletRequestContext context = (PortletRequestContext)  WebuiRequestContext.getCurrentInstance() ;
    PortletRequest prequest = context.getRequest() ;
    String template =  prequest.getPreferences().getValue("template", DEFAULT_TEMPLATE) ;
    String rssFetchId = prequest.getPreferences().getValue("aggregatorId", DEFAULT_RSSFETCH_ID) ;
    uiDashboard.setContainerTemplate(template);
    uiDashboard.setAggregatorId(rssFetchId);
  }

  public boolean canEdit() {
    String spaceUrl = SpaceUtils.getSpaceUrl();
    Space space = null;
    try {
      space = Utils.getSpaceService().getSpaceByUrl(spaceUrl);
      return Utils.getSpaceService().hasEditPermission(space, Utils.getViewerRemoteId());
    } catch (Exception e) {
      LOG.warn("The current user can not allow to edit permission with space::" + space.getDisplayName());
    }
    return false;
  }

  public String getDashboardOwner() {
    return Utils.getViewerRemoteId();
  }
}
