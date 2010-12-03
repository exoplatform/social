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
package org.exoplatform.social.webui.profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.image.ImageUtils;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.webui.UIAvatarUploadContent;
import org.exoplatform.social.webui.UIAvatarUploader;
import org.exoplatform.social.webui.URLUtils;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Component is used for profile managing, it is the
 * place put all other profile management components.<br>
 *
 */

@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "classpath:groovy/social/webui/profile/UIProfile.gtmpl",
  events = {
    @EventConfig(listeners=UIProfile.ChangeAvatarActionListener.class)
  }
)
public class UIProfile extends UIContainer {

  private final String POPUP_AVATAR_UPLOADER = "UIPopupAvatarUploader";
  private Profile profile;
  private static IdentityManager im;
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
   * Gets current uri base on url of current page.<br>
   *
   * @return uri of current page.
   */
  public String getCurrentUriObj() {
    PortalRequestContext pcontext = Util.getPortalRequestContext();
    String requestUrl = pcontext.getRequestURI();
    String portalUrl = pcontext.getPortalURI();
    String uriObj = requestUrl.replace(portalUrl, "");
    if (uriObj.contains("/"))
      uriObj = uriObj.split("/")[0] + "/" + uriObj.split("/")[1];
    return uriObj;
  }


  /**
   * gets profile with forceReload.
   * @param forceReload
   * @return
   * @throws Exception
   */
  public Profile getProfile(boolean forceReload) throws Exception {
    if (forceReload == true || profile == null) {
      Identity id = getIdentity();
      profile = id.getProfile();
    }
    return profile;
  }

  /**
   * Checks the current user is right edit permission.<br>
   *
   * @return true if current user has permission.
   */
  public boolean isEditable() {
    RequestContext context = RequestContext.getCurrentInstance();
    String rUser = context.getRemoteUser();

    if(rUser == null)
      return false;

    return getCurrentProfileID().equals(rUser);
  }

  /**
   * Gets the source of image.
   *
   * @return imageSource link
   */
  public String getImageSource() throws Exception {
    return LinkProvider.getAvatarImageSource(getProfile(true));
  }

  /**
   * @param uiAvatarUploadContent
   * @throws Exception
   */
  public void saveAvatar(UIAvatarUploadContent uiAvatarUploadContent) throws Exception {
    AvatarAttachment attacthment = uiAvatarUploadContent.getAvatarAttachment();
    if (im == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    }

    Profile p = getProfile(true);
    p.setProperty(Profile.AVATAR, attacthment);
    Map<String, Object> props = p.getProperties();

    // Removes avatar url and resized avatar
    if (p.contains(Profile.AVATAR_URL)) {
      p.removeProperty(Profile.AVATAR_URL);
    }
    for (String key : props.keySet()) {
      if (key.startsWith(Profile.AVATAR + ImageUtils.KEY_SEPARATOR)) {
        p.removeProperty(key);
      }
    }

    im.updateAvatar(p);
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

  /**
   * Gets current profile Id.<br>
   *
   * @return id of current profile.
   */
  private String getCurrentProfileID() {
    String username = URLUtils.getCurrentUser();
    if(username != null)
      return username;

    // if we are not on the page of a user, we display the profile of the current user
    RequestContext context = RequestContext.getCurrentInstance();
    return context.getRemoteUser();
  }

  /**
   * Gets current identity of login user.<br>
   *
   * @return current identity.
   *
   * @throws Exception
   */
  private Identity getIdentity() throws Exception {
    if (im == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    }
    return im.getOrCreateIdentity(OrganizationIdentityProvider.NAME, getCurrentProfileID());
  }

 /**
  *
  * @return the list of sections ordered by display order
  */
  @SuppressWarnings("unchecked")
  private List getSections() {
    List sects = new ArrayList();
    sects.add(UIHeaderSection.class);
    sects.add(UIBasicInfoSection.class);
    sects.add(UIContactSection.class);
    sects.add(UIExperienceSection.class);
    return sects;
  }

public void setProfile(Profile profile) {
  this.profile = profile;
}

public Profile getProfile() {
  return profile;
}

}
