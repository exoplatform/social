/**
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
package org.exoplatform.social.portlet.profile;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.portlet.URLUtils;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;


@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template = "app:/groovy/portal/webui/component/UIProfile.gtmpl"
)
public class UIProfile extends UIContainer {

  public UIProfile() throws Exception {
    List sections = getSections();
    java.util.Iterator it = sections.iterator();
    while (it.hasNext()) {
      Class sect = (Class) it.next();
      addChild(sect, null, null);
    }
  }


  /**
   *
   * @return the list of sections ordered by display order
   */
  private List getSections() {
    List sects = new ArrayList();
    sects.add(UIHeaderSection.class);
    sects.add(UIBasicInfoSection.class);
    sects.add(UIContactSection.class);
    sects.add(UIExperienceSection.class);
    return sects;
  }

  public String getCurrentUriObj() {
    PortalRequestContext pcontext = Util.getPortalRequestContext();
    String requestUrl = pcontext.getRequestURI();
    String portalUrl = pcontext.getPortalURI();
    String uriObj = requestUrl.replace(portalUrl, "");
    if (uriObj.contains("/"))
      uriObj = uriObj.split("/")[0];
    return uriObj;
  }

  public Profile getProfile() throws Exception {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    IdentityManager im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    Identity id = im.getIdentityByRemoteId("organization", getCurrentProfileID());

    if(id == null)
      return null;
    else
      return id.getProfile();
  }

  public boolean isEditable() {
    RequestContext context = RequestContext.getCurrentInstance();
    String rUser = context.getRemoteUser();

    if(rUser == null)
      return false;

    return getCurrentProfileID().equals(rUser);
  }

  private String getCurrentProfileID() {
    String username = URLUtils.getCurrentUser();
    if(username != null)
      return username;

    // if we are not on the page of a user, we display the profile of the current user
    RequestContext context = RequestContext.getCurrentInstance();
    return context.getRemoteUser();
  }
}
