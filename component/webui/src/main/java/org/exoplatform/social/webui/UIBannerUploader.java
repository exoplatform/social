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

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.MimeTypeResolver;
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
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.input.UIUploadInput;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@ComponentConfigs ({
  @ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "war:/groovy/social/webui/UIBannerUploader.gtmpl",
    events = {
      @EventConfig(listeners = UIBannerUploader.ConfirmActionListener.class),
      @EventConfig(listeners = UIBannerUploader.CancelActionListener.class)
    }
  )
})
public class UIBannerUploader extends UIForm {
  /** Message alert that mimetype is not accepted. */
  private static final String MSG_MIMETYPE_NOT_ACCEPTED = "UIBannerUploader.msg.mimetype_not_accepted";
  /** Message alert that image is not loaded. */
  private static final String MSG_IMAGE_NOT_LOADED = "UIBannerUploader.msg.img_not_loaded";
/*  *//** Message alert that the file name is too long *//*
  *//** The number of characters allowed to rename *//*

  /** FIELD Uploader. */
  private static final String FIELD_UPLOADER = "BannerUploader";

  /** The limit size for upload image. */
  private static final int uploadLimit = 2; //MB

  /** List of accepted mimetype. */
  private static final String[] ACCEPTED_MIME_TYPES = new String[] {"image/gif", "image/jpeg", "image/jpg", "image/png", "image/x-png", "image/pjpeg"};

  /**
   * Initializes upload form.<br>\
   *
   */
  public UIBannerUploader() throws Exception {
    UIUploadInput uiBannerUploadInput = new UIUploadInput(FIELD_UPLOADER, FIELD_UPLOADER, 1, uploadLimit);
    addUIFormInput(uiBannerUploadInput);
    setActions(new String[]{"Confirm", "Cancel"});
  }

  /**
   * Checks if the provided mimeType matches acceptedMimeTypes.<br>
   *
   * @param mimeType String
   *
   * @return boolean
   */
  private boolean isAcceptedMimeType(String mimeType) {
    for (String acceptedMimeType : ACCEPTED_MIME_TYPES) {
      if (mimeType.equals(acceptedMimeType)) return true;
    }
    return false;
  }

  public void saveSpaceBanner(BannerAttachment banner) throws Exception {
    SpaceService spaceService = getApplicationComponent(SpaceService.class);
    String spaceUrl = Utils.getSpaceUrlByContext();
    Space space = spaceService.getSpaceByUrl(spaceUrl);

    space.setBannerAttachment(banner);
    spaceService.updateSpace(space);
    space.setEditor(Utils.getViewerRemoteId());
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

  public static class ConfirmActionListener extends EventListener<UIBannerUploader> {

    @Override
    public void execute(Event<UIBannerUploader> event) throws Exception {
      WebuiRequestContext ctx = event.getRequestContext();
      UIBannerUploader uiBannerUploader = event.getSource();
      UIUploadInput uiBannerUploadInput = uiBannerUploader.getChild(UIUploadInput.class);
      if (uiBannerUploadInput.getUploadResources().length < 1) {
        ctx.getUIApplication().addMessage(new ApplicationMessage(MSG_IMAGE_NOT_LOADED, null, ApplicationMessage.ERROR));
        ctx.addUIComponentToUpdateByAjax(uiBannerUploader);
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
            MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();

            String extension = mimeTypeResolver.getExtension(mimeType);
            BannerAttachment avatarAttachment = new BannerAttachment(null, fileName, mimeType,
                    uploadedStream, null, System.currentTimeMillis());

            if (uiBannerUploader.getAncestorOfType(UISpaceMenu.class) != null) {
              uiBannerUploader.saveSpaceBanner(avatarAttachment);
            } else {
              uiBannerUploader.saveUserBanner(avatarAttachment);
            }

            UIPopupWindow uiPopup = uiBannerUploader.getParent();
            uiPopup.setShow(false);
            uiPopup.setRendered(false);
            uiPopup.setUIComponent(null);
            ctx.addUIComponentToUpdateByAjax(uiPopup.getParent().getParent());
          }
          return;
        } finally {
          UploadService uploadService = CommonsUtils.getService(UploadService.class);
          uploadService.removeUploadResource(uploadResource.getUploadId());
        }
      }  
    }
  }

  /**
   * Cancels the upload image.<br>
   *
   */
  public static class CancelActionListener extends EventListener<UIBannerUploader> {
    @Override
    public void execute(Event<UIBannerUploader> event) throws Exception {
      UIBannerUploader uiBannerUploader = event.getSource();
      UIPopupWindow uiPopup = uiBannerUploader.getParent();
      uiPopup.setShow(false);
      uiPopup.setRendered(false);
      uiPopup.setUIComponent(null);

      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
    }
  }
}
