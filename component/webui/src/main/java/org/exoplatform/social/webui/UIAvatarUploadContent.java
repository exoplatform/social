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
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import org.exoplatform.download.DownloadResource;
import org.exoplatform.download.DownloadService;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.image.ImageUtils;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.space.UISpaceInfo;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(
  template = "war:/groovy/social/webui/UIAvatarUploadContent.gtmpl",
  events = {
    @EventConfig(listeners = UIAvatarUploadContent.SaveActionListener.class),
    @EventConfig(listeners = UIAvatarUploadContent.CancelActionListener.class)
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
   * Accepts and saves the uploaded image to profile.
   * Closes the popup window, refreshes UIProfile.
   *
   */
  public static class SaveActionListener extends EventListener<UIAvatarUploadContent> {
    @Override
    public void execute(Event<UIAvatarUploadContent> event) throws Exception {
      UIAvatarUploadContent uiAvatarUploadContent = event.getSource();
      saveAvatar(uiAvatarUploadContent);
      UIPopupWindow uiPopup = uiAvatarUploadContent.getParent();
      uiPopup.setShow(false);
      uiPopup.setUIComponent(null);
      uiPopup.setRendered(false);
      UISpaceInfo spaceInfo = uiPopup.getAncestorOfType(UISpaceInfo.class);
      if(spaceInfo == null) {
        UIContainer container = uiPopup.getAncestorOfType(UIPortletApplication.class).findComponentById("Avatar");
        if (container != null) {
          event.getRequestContext().addUIComponentToUpdateByAjax(container);
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup.getParent());
      } else {
        Utils.updateWorkingWorkSpace();
      }
    }

    private void saveAvatar(UIAvatarUploadContent uiAvatarUploadContent) throws Exception {
      UISpaceInfo spaceInfo = uiAvatarUploadContent.getAncestorOfType(UISpaceInfo.class);
      if(spaceInfo != null) {
        SpaceService spaceService = spaceInfo.getSpaceService();
        String id = spaceInfo.getUIStringInput(UISpaceInfo.SPACE_ID).getValue();
        Space space = spaceService.getSpaceById(id);
        if (space != null) {
          spaceInfo.saveAvatar(uiAvatarUploadContent, space);
        }
      } else {
        // Save user avatar
        uiAvatarUploadContent.saveUserAvatar(uiAvatarUploadContent);
      }
    }
  }

  /**
   * Saves avatar of users.
   * 
   * @param uiAvatarUploadContent
   * @throws Exception
   * @since 1.2.2
   */
  public void saveUserAvatar(UIAvatarUploadContent uiAvatarUploadContent) throws Exception {
    AvatarAttachment attacthment = uiAvatarUploadContent.getAvatarAttachment();
    
    Profile p = Utils.getOwnerIdentity().getProfile();
    p.setProperty(Profile.AVATAR, attacthment);
    p.setListUpdateTypes(Arrays.asList(Profile.UpdateType.AVATAR));
    Map<String, Object> props = p.getProperties();

    // Removes avatar url and resized avatar
    for (String key : props.keySet()) {
      if (key.startsWith(Profile.AVATAR + ImageUtils.KEY_SEPARATOR)) {
        p.removeProperty(key);
      }
    }

    Utils.getIdentityManager().updateProfile(p);
  }
  
  /**
   * Cancels, close the popup window.
   *
   */
  public static class CancelActionListener extends EventListener<UIAvatarUploadContent> {
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
  
  private class InputStreamDownloadResource extends DownloadResource {
    private InputStream is_;
    public InputStreamDownloadResource(ByteArrayInputStream byteImage, String resourceMimeType) {
      super(resourceMimeType);
      this.is_ = byteImage;
    }
    public InputStreamDownloadResource(String resourceMimeType) {
      super(resourceMimeType);
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return is_;
    }
  }
  
}
