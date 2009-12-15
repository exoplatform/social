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
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.ProfileAttachment;
import org.exoplatform.social.portlet.URLUtils;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;


@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template = "app:/groovy/portal/webui/component/UIProfile.gtmpl",
    events = {
      @EventConfig(listeners=UIProfile.ChangeAvatarActionListener.class)
    }
)
public class UIProfile extends UIContainer {
  
  private final String POPUP_AVATAR_UPLOADER = "UIPopupAvatarUploader";
  
  /**
   * Constructor to initialize UIAvatarUploader popup and info sections
   * @throws Exception
   */
  public UIProfile() throws Exception {
    
    List sections = getSections();
    java.util.Iterator it = sections.iterator();
    while (it.hasNext()) {
      Class sect = (Class) it.next();
      addChild(sect, null, null);
    }
    UIPopupWindow uiPopup = createUIComponent(UIPopupWindow.class, null, POPUP_AVATAR_UPLOADER);
    uiPopup.setWindowSize(500, 0);
    addChild(uiPopup);
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
      uriObj = uriObj.split("/")[0] + "/" + uriObj.split("/")[1];
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
  
  protected String getImageSource() throws Exception {
    Profile p = getProfile();
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
  
  /**
   * Action trigger for editting avatar. An UIAvatarUploader popup should be displayed.
   * @author hoatle
   *
   */
  static public class ChangeAvatarActionListener extends EventListener<UIProfile> {

    @Override
    public void execute(Event<UIProfile> event) throws Exception {
      UIProfile uiProfile = event.getSource();
      UIPopupWindow uiPopup = uiProfile.getChild(UIPopupWindow.class);
      UIAvatarUploader uiAvatarUploader = uiProfile.createUIComponent(UIAvatarUploader.class, null, null);
      uiPopup.setUIComponent(uiAvatarUploader);
      uiPopup.setShow(true);
    }
    
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
