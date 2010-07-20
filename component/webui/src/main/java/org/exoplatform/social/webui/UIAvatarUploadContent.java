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
package org.exoplatform.social.webui;

import java.io.ByteArrayInputStream;

import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.social.core.identity.model.AvatarAttachment;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.profile.UIProfile;
import org.exoplatform.social.webui.space.UISpaceInfo;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Displays uploaded content from UIAvatarUploader.<br>
 *
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Nov 4, 2009
 */
@ComponentConfig(
  template = "classpath:groovy/social/webui/UIAvatarUploadContent.gtmpl",
  events = {
    @EventConfig(listeners = UIAvatarUploadContent.SaveActionListener.class),
    @EventConfig(listeners = UIAvatarUploadContent.AbortActionListener.class)
  }
)
public class UIAvatarUploadContent extends UIContainer {

  /** AvatarAttachment instance. */
  private AvatarAttachment avatarAttachment;

  /** Stores information of image storage. */
  private String imageSource;

  /**
   * Default constructor.<br>
   *
   */
  public UIAvatarUploadContent() {

  }

  /**
   * Initializes object at the first run time.<br>
   *
   * @param AvatarAttachment
   *        Information about attachment.
   *
   * @throws Exception
   */
  public UIAvatarUploadContent(AvatarAttachment avatarAttachment) throws Exception {
    this.avatarAttachment = avatarAttachment;
    setImageSource(avatarAttachment.getImageBytes());
  }


  /**
   * Gets information of AvatarAttachment.<br>
   *
   * @return AvatarAttachment
   */
  public AvatarAttachment getAvatarAttachment() {
    return avatarAttachment;
  }

  /**
   * Sets information of AvatarAttachment.<br>
   *
   * @param AvatarAttachment
   *
   * @throws Exception
   */
  public void setAvatarAttachment(AvatarAttachment avatarAttachment) throws Exception {
    this.avatarAttachment = avatarAttachment;
    setImageSource(avatarAttachment.getImageBytes());
  }

  /**
   * Gets the source of image.
   *
   * @return imageSource link
   */
  public String getImageSource() {
    return imageSource;
  }

  /**
   * Updates working work space.<br>
   *
   */
  static public void updateWorkingWorkSpace() {
    UIPortalApplication uiPortalApplication = Util.getUIPortalApplication();
    UIWorkingWorkspace uiWorkingWS = uiPortalApplication.getChildById(UIPortalApplication.UI_WORKING_WS_ID);
    PortalRequestContext pContext = Util.getPortalRequestContext();
    pContext.addUIComponentToUpdateByAjax(uiWorkingWS);
    pContext.setFullRender(true);
  }

  /**
   * Accepts and saves the uploaded image to profile.
   * Closes the popup window, refreshes UIProfile.
   *
   */
  static public class SaveActionListener extends EventListener<UIAvatarUploadContent> {
    @Override
    public void execute(Event<UIAvatarUploadContent> event) throws Exception {
      UIAvatarUploadContent uiAvatarUploadContent = event.getSource();
      saveAvatar(uiAvatarUploadContent);
      UIPopupWindow uiPopup = uiAvatarUploadContent.getParent();
      uiPopup.setShow(false);
      updateWorkingWorkSpace();
    }
    
    private void saveAvatar(UIAvatarUploadContent uiAvatarUploadContent) throws Exception {
      UIComponent parent =uiAvatarUploadContent.getParent();
      while (parent != null) {
         if (UIProfile.class.isInstance(parent)) {
           ((UIProfile)parent).saveAvatar(uiAvatarUploadContent);
         } else if (UISpaceInfo.class.isInstance(parent)) {
           UISpaceInfo uiSpaceInfo = ((UISpaceInfo)parent);
           SpaceService spaceService = uiSpaceInfo.getSpaceService();
           String id = uiSpaceInfo.getUIStringInput("id").getValue();
           Space space = spaceService.getSpaceById(id);
           uiSpaceInfo.saveAvatar(uiAvatarUploadContent, space);
         }
         parent = parent.getParent();
      }
    }
  }

  /**
   * Aborts, close the popup window.
   *
   */
  static public class AbortActionListener extends EventListener<UIAvatarUploadContent> {
    @Override
    public void execute(Event<UIAvatarUploadContent> event) throws Exception {
      UIAvatarUploadContent uiAvatarUploadContent = event.getSource();
      UIPopupWindow uiPopup = uiAvatarUploadContent.getParent();
      uiPopup.setShow(false);
    }

  }

  /**
   * Sets information of image storage.<br>
   *
   * @param imageBytes
   *        Image information in byte type for storing.
   * @throws Exception
   */
  private void setImageSource(byte[] imageBytes) throws Exception {
    if (imageBytes == null || imageBytes.length == 0) return;
    ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes);
    DownloadService downloadService = getApplicationComponent(DownloadService.class);
    InputStreamDownloadResource downloadResource = new InputStreamDownloadResource(byteImage, "image");
    downloadResource.setDownloadName(avatarAttachment.getFileName());
    imageSource = downloadService.getDownloadLink(downloadService.addDownloadResource(downloadResource));
  }
}
