/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

import java.io.ByteArrayInputStream;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.ProfileAttachment;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Displays uploaded content from UIAvatarUploader
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Nov 4, 2009  
 */
@ComponentConfig(
  template = "app:/groovy/portal/webui/component/UIAvatarUploadContent.gtmpl",
  events = {
      @EventConfig(listeners = UIAvatarUploadContent.SaveActionListener.class),
      @EventConfig(listeners = UIAvatarUploadContent.AbortActionListener.class)
  }
)
public class UIAvatarUploadContent extends UIContainer {
  static public final String AVARTAR_PROPERTY = "avatar";
  private ProfileAttachment profileAttachment;
  private String imageSource;
  
  /**
   * constructor
   */
  public UIAvatarUploadContent() {
  
  }
  
  /**
   * constructor
   * @param profileAttachment
   * @throws Exception 
   */
  public UIAvatarUploadContent(ProfileAttachment profileAttachment) throws Exception {
    this.profileAttachment = profileAttachment;
    setImageSource(profileAttachment.getImageBytes());
  }
  
  
  /**
   * gets profileAttachment
   * @return profileAttachment
   */
  public ProfileAttachment getProfileAttachment() {
    return profileAttachment;
  }
  
  /**
   * sets profileAttachment
   * @param profileAttachment
   * @throws Exception 
   */
  public void setProfileAttachment(ProfileAttachment profileAttachment) throws Exception {
    this.profileAttachment = profileAttachment;
    setImageSource(profileAttachment.getImageBytes());
  }
  
  private void setImageSource(byte[] imageBytes) throws Exception {
    if (imageBytes == null || imageBytes.length == 0) return;
    ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes);
    DownloadService downloadService = getApplicationComponent(DownloadService.class);
    InputStreamDownloadResource downloadResource = new InputStreamDownloadResource(byteImage, "image");
    downloadResource.setDownloadName(profileAttachment.getFileName());
    imageSource = downloadService.getDownloadLink(downloadService.addDownloadResource(downloadResource));
  }
  
  /**
   * gets imageSource link
   * @return
   */
  public String getImageSource() {
    return imageSource;
  }
  
  /**
   * accepts and saves the uploaded image to profile
   * closes the popup window, refreshes UIProfile
   * @author hoatle
   */
  static public class SaveActionListener extends EventListener<UIAvatarUploadContent> {
    @Override
    public void execute(Event<UIAvatarUploadContent> event) throws Exception {
      WebuiRequestContext ctx = event.getRequestContext();
      UIAvatarUploadContent uiAvatarUploadContent = event.getSource();
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      IdentityManager im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
      UIProfile uiProfile = uiAvatarUploadContent.getAncestorOfType(UIProfile.class);
      Profile p = uiProfile.getProfile();
      p.setProperty(AVARTAR_PROPERTY, uiAvatarUploadContent.getProfileAttachment());
      im.saveProfile(p);
      UIPopupWindow uiPopup = uiAvatarUploadContent.getParent();
      uiPopup.setShow(false);
      if (uiPopup.getParent() != null) {
        ctx.addUIComponentToUpdateByAjax(uiPopup.getParent());
      }
    } 
  }
  
  /**
   * aborts, close the popup window
   * @author hoatle
   */
  static public class AbortActionListener extends EventListener<UIAvatarUploadContent> {
    @Override
    public void execute(Event<UIAvatarUploadContent> event) throws Exception {
      UIAvatarUploadContent uiAvatarUploadContent = event.getSource();
      UIPopupWindow uiPopup = uiAvatarUploadContent.getParent();
      uiPopup.setShow(false);
    }
    
  }

}
