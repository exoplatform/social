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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.ProfileAttachment;
import org.exoplatform.social.portlet.profile.Utils;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SAS
 */

@ComponentConfig(
 lifecycle = UIApplicationLifecycle.class,
 template = "app:/groovy/portal/webui/component/UIProfileNavigationPortlet.gtmpl"
)
public class UIProfileNavigationPortlet extends UIPortletApplication {

  public UIProfileNavigationPortlet() throws Exception {
  }
  
  public String getSelectedNode() {
    PortalRequestContext pcontext = Util.getPortalRequestContext();
    String requestUrl = pcontext.getRequestURI();
    String[] split = requestUrl.split("/");
    if (split.length == 6) {
      return split[split.length - 2];
    } else if (split.length == 7) {
      return split[split.length - 3];
    }
    return split[split.length-1];
  }
  
  public boolean isMe () {
    RequestContext context = RequestContext.getCurrentInstance();
    String currentUserName = context.getRemoteUser();
    String currentViewer = URLUtils.getCurrentUser();
    
    if (currentViewer == null) {
      return true;
    }
    
    return currentUserName.equals(currentViewer);
  }
  
  protected String getImageSource() throws Exception {
    Identity currIdentity = Utils.getCurrentIdentity();
    Profile p = currIdentity.getProfile();
    ProfileAttachment att = (ProfileAttachment) p.getProperty("avatar");
    if (att != null) {
      return "/" + getPortalName()+"/rest/jcr/" + getRepository()+ "/" + att.getWorkspace()
              + att.getDataPath() + "/?rnd=" + System.currentTimeMillis();
    }
    return null;
  }
  
  private String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance() ;
    return pcontainer.getPortalContainerInfo().getContainerName() ;  
  }
  
  private String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;    
    return rService.getCurrentRepository().getConfiguration().getName() ;
  }
}
