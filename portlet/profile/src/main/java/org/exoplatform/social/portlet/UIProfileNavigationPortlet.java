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
import org.exoplatform.social.webui.URLUtils;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Manages the navigation of relation.<br>
 *   - Decides which node is current selected.<br>
 *   - Checked is view by current user or by another.<br>
 */

@ComponentConfig(
 lifecycle = UIApplicationLifecycle.class,
 template = "app:/groovy/portal/webui/component/UIProfileNavigationPortlet.gtmpl"
)
public class UIProfileNavigationPortlet extends UIPortletApplication {

  /**
   * Default Constructor.<br>
   * 
   * @throws Exception
   */
  public UIProfileNavigationPortlet() throws Exception { }
  
  /**
   * Returns the current selected node.<br>
   * 
   * @return selected node.
   */
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
  
  /**
   * Checked is view by current user or by another.<br>
   * 
   * @return true if it is viewed by current login user.
   */
  public boolean isMe () {
    RequestContext context = RequestContext.getCurrentInstance();
    String currentUserName = context.getRemoteUser();
    String currentViewer = URLUtils.getCurrentUser();
    
    if (currentViewer == null) {
      return true;
    }
    
    return currentUserName.equals(currentViewer);
  }
  
  /**
   * Gets information about source that avatar image is stored.<br>
   *  
   * @return image source address.
   * 
   * @throws Exception
   */
  protected String getImageSource() throws Exception {
    Identity currIdentity = Utils.getCurrentIdentity();
    Profile p = currIdentity.getProfile();
    ProfileAttachment att = (ProfileAttachment) p.getProperty(Profile.AVATAR);
    if (att != null) {
      return "/"+ getRestContext() + "/jcr/" + getRepository()+ "/" + att.getWorkspace()
              + att.getDataPath() + "/?rnd=" + System.currentTimeMillis();
    }
    return null;
  }
  
  /**
   * Gets the current portal name.<br>
   * 
   * @return name of current portal.
   * 
   */
//  private String getPortalName() {
//    PortalContainer pcontainer =  PortalContainer.getInstance() ;
//    return pcontainer.getPortalContainerInfo().getContainerName() ;  
//  }
  
  /**
   * Gets the current repository.<br>
   * 
   * @return current repository through repository service.
   * 
   * @throws Exception
   */
  private String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;    
    return rService.getCurrentRepository().getConfiguration().getName() ;
  }
  
  /**
   * Gets the rest context.
   * 
   * @return the rest context
   */
	private String getRestContext() {
	  return PortalContainer.getInstance().getRestContextName();
	}
}
