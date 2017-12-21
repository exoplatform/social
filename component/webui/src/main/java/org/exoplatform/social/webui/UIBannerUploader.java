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
import org.exoplatform.social.core.model.BannerAttachment;
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
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.input.UIUploadInput;

@ComponentConfigs ({
  @ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "war:/groovy/social/webui/UIBannerUploader.gtmpl",
    events = {
      @EventConfig(listeners = UIBannerUploader.ConfirmActionListener.class)
    }
  )
})
public class UIBannerUploader extends UIForm {
  /** Message alert that mimetype is not accepted. */
  protected static final String MSG_MIMETYPE_NOT_ACCEPTED = "UIBannerUploader.msg.mimetype_not_accepted";
  /** Message alert that image is not loaded. */
  protected static final String MSG_IMAGE_NOT_LOADED = "UIBannerUploader.msg.img_not_loaded";
  /** FIELD Uploader. */
  protected static final String FIELD_UPLOADER = "BannerUploader";
  /** The limit size for upload image. */
  protected static final int uploadLimit = 2; //MB
  /** List of accepted mimetype. */
  protected static final String[] ACCEPTED_MIME_TYPES = new String[] {"image/gif", "image/jpeg", "image/jpg", "image/png", "image/x-png", "image/pjpeg"};

  private UIUploadInput uiBannerUploadInput;

  /**
   * Initializes upload form.<br>\
   *
   */
  public UIBannerUploader() {
    this(FIELD_UPLOADER);
  }

  /**
   * Initializes upload form.<br>\
   *
   */
  public UIBannerUploader(String uploadFieldId) {
    uiBannerUploadInput = new UIUploadInput(uploadFieldId, uploadFieldId, 1, uploadLimit);
    addUIFormInput(uiBannerUploadInput);
    setActions(new String[]{"Confirm"});
  }

  /**
   * Checks if the provided mimeType matches acceptedMimeTypes.<br>
   *
   * @param mimeType String
   *
   * @return boolean
   */
  protected boolean isAcceptedMimeType(String mimeType) {
    for (String acceptedMimeType : ACCEPTED_MIME_TYPES) {
      if (mimeType.equals(acceptedMimeType)) return true;
    }
    return false;
  }

  public void saveSpaceBanner(BannerAttachment banner) throws Exception {
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    String spaceUrl = Utils.getSpaceUrlByContext();
    Space space = spaceService.getSpaceByUrl(spaceUrl);
    space.setEditor(Utils.getViewerRemoteId());

    space.setBannerAttachment(banner);
    spaceService.updateSpace(space);
    spaceService.updateSpaceBanner(space);
  }

  public void saveUserBanner(BannerAttachment banner) throws Exception {
    Profile p = Utils.getOwnerIdentity().getProfile();
    p.setProperty(Profile.BANNER, banner);
    p.setListUpdateTypes(Arrays.asList(Profile.UpdateType.BANNER));
    Map<String, Object> props = p.getProperties();

    for (String key : props.keySet()) {
      if (key.startsWith(Profile.BANNER + ImageUtils.KEY_SEPARATOR)) {
        p.removeProperty(key);
      }
    }

    Utils.getIdentityManager().updateProfile(p);
  }

  public UIUploadInput getUiBannerUploadInput() {
    return uiBannerUploadInput;
  }

  public static class ConfirmActionListener extends EventListener<UIBannerUploader> {

    @Override
    public void execute(Event<UIBannerUploader> event) throws Exception {
      WebuiRequestContext ctx = event.getRequestContext();
      UIBannerUploader uiBannerUploader = event.getSource();
      UIUploadInput uiBannerUploadInput = uiBannerUploader.getChild(UIUploadInput.class);
      if (uiBannerUploadInput.getUploadResources().length < 1) {
        ctx.getUIApplication().addMessage(new ApplicationMessage(MSG_IMAGE_NOT_LOADED, null, ApplicationMessage.ERROR));
        ctx.addUIComponentToUpdateByAjax(uiBannerUploader);
        return;
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
          
          if (!uiBannerUploader.isAcceptedMimeType(mimeType)) {
            ctx.getUIApplication().addMessage(new ApplicationMessage(MSG_MIMETYPE_NOT_ACCEPTED, null, ApplicationMessage.ERROR));
            ctx.addUIComponentToUpdateByAjax(uiBannerUploader);
          } else {
            InputStream uploadedStream = new FileInputStream(new File(uploadResource.getStoreLocation()));
            BannerAttachment avatarAttachment = new BannerAttachment(null, fileName, mimeType,
                    uploadedStream, null, System.currentTimeMillis());

            if (uiBannerUploader.getAncestorOfType(UISpaceMenu.class) != null) {
              uiBannerUploader.saveSpaceBanner(avatarAttachment);
            } else {
              uiBannerUploader.saveUserBanner(avatarAttachment);
            }

            ctx.addUIComponentToUpdateByAjax(uiBannerUploader.getParent());
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
