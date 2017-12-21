/*
 * Copyright (C) 2003-2017 eXo Platform SAS.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.image.ImageUtils;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.space.UISpaceMenu;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.input.UIUploadInput;

@ComponentConfigs ({
  @ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "war:/groovy/social/webui/UIBannerAvatarUploader.gtmpl",
    events = {
      @EventConfig(listeners = UIBannerAvatarUploader.ConfirmActionListener.class)
    }
  )
})
public class UIBannerAvatarUploader extends UIBannerUploader {
  /** FIELD Uploader. */
  protected static final String FIELD_UPLOADER = "BannerAvatarUploader";

  private boolean renderUpload = false;

  public UIBannerAvatarUploader() {
    super(FIELD_UPLOADER);
  }

  public void setRenderUpload(boolean renderUpload) {
    this.renderUpload = renderUpload;
  }

  public boolean isRenderUpload() {
    return renderUpload;
  }

  public void saveSpaceAvatar(Space space, AvatarAttachment avatarAttachment) throws Exception {
    SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
    space.setAvatarAttachment(avatarAttachment);
    space.setEditor(Utils.getViewerRemoteId());

    spaceService.updateSpace(space);
    spaceService.updateSpaceAvatar(space);
  }

  public void saveUserAvatar(AvatarAttachment avatarAttachment) throws Exception {
    Profile p = Utils.getOwnerIdentity().getProfile();
    p.setProperty(Profile.AVATAR, avatarAttachment);
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

  public static class ConfirmActionListener extends EventListener<UIBannerAvatarUploader> {
    @Override
    public void execute(Event<UIBannerAvatarUploader> event) throws Exception {
      WebuiRequestContext ctx = event.getRequestContext();
      UIBannerAvatarUploader uiBannerAvatarUploader = event.getSource();
      UIUploadInput uiBannerUploadInput = uiBannerAvatarUploader.getChild(UIUploadInput.class);
      if (uiBannerUploadInput.getUploadResources().length < 1) {
        ctx.getUIApplication().addMessage(new ApplicationMessage(MSG_IMAGE_NOT_LOADED, null, ApplicationMessage.ERROR));
        ctx.addUIComponentToUpdateByAjax(uiBannerAvatarUploader);
      }

      for (UploadResource uploadResource : uiBannerUploadInput.getUploadResources()) {
        if (uploadResource == null) {
          continue;
        }
        try {
          String fileName = uploadResource.getFileName();
          if (fileName == null || fileName.length() == 0) {
            continue;
          }
          String mimeType = uploadResource.getMimeType();
          InputStream uploadedStream = new FileInputStream(new File(uploadResource.getStoreLocation()));
          AvatarAttachment avatarAttachment = ImageUtils.createResizedAvatarAttachment(uploadedStream, UIAvatarUploader.WIDTH, UIAvatarUploader.HEIGHT, null,
                                                                                       fileName, mimeType, null);
          if (avatarAttachment == null) {
            avatarAttachment = new AvatarAttachment(null, fileName, mimeType, uploadedStream, null, System.currentTimeMillis());
          }

          if (!uiBannerAvatarUploader.isAcceptedMimeType(mimeType)) {
            ctx.getUIApplication().addMessage(new ApplicationMessage(MSG_MIMETYPE_NOT_ACCEPTED, null, ApplicationMessage.ERROR));
            ctx.addUIComponentToUpdateByAjax(uiBannerAvatarUploader);
          } else {
            if (uiBannerAvatarUploader.getAncestorOfType(UISpaceMenu.class) != null) {
              String spaceUrl = Utils.getSpaceUrlByContext();
              SpaceService spaceService = CommonsUtils.getService(SpaceService.class);
              Space space = spaceService.getSpaceByUrl(spaceUrl);
              if (space != null) {
                uiBannerAvatarUploader.saveSpaceAvatar(space, avatarAttachment);
              }
            } else {
              uiBannerAvatarUploader.saveUserAvatar(avatarAttachment);
            }
            ctx.addUIComponentToUpdateByAjax(uiBannerAvatarUploader.getParent());
          }
          return;
        } finally {
          UploadService uploadService = CommonsUtils.getService(UploadService.class);
          uploadService.removeUploadResource(uploadResource.getUploadId());
          uiBannerUploadInput.addNewUploadId();
        }
      }  
    }
  }
}
