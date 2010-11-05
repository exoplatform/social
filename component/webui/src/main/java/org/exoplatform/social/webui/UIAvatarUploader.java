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

import java.io.InputStream;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.image.ImageUtils;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormUploadInput;


/**
 * Uploads image to set user's avatar.<br>
 *
 * Author : hoatle
 *          hoatlevan@gmail.com
 * Sep 8, 2009
 */
@ComponentConfigs ({
  @ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "classpath:groovy/social/webui/UIAvatarUploader.gtmpl",
    events = {
      @EventConfig(listeners = UIAvatarUploader.ConfirmActionListener.class),
      @EventConfig(listeners = UIAvatarUploader.CancelActionListener.class)
    }
  )
})
public class UIAvatarUploader extends UIForm {
  /** Message alert that image is not uploaded successfully. */
  static private final String MSG_IMG_NOT_UPLOADED = "UIAvatarUploader.msg.img_not_loaded";

  /** Message alert that mimetype is not accepted. */
  static private final String MSG_MIMETYPE_NOT_ACCEPTED = "UIAvatarUploader.msg.mimetype_not_accepted";
/*  *//** Message alert that the file name is too long *//*
  static private final String MSG_CHARACTERS_TOO_LONG = "UIAvatarUploader.msg.characters_too_long";
  *//** The number of characters allowed to rename *//*
  static private final int ALLOWED_CHARACTERS_LONG = 50;*/
  /** FIELD NAME. */
  private final String FIELD_NAME = "Name";

  /** FIELD Uploader. */
  private final String FIELD_UPLOADER = "Uploader";

  /** The limit size for upload image. */
  private final int uploadLimit = 2; //MB

  /** List of accepted mimetype. */
  private final String[] acceptedMimeTypes = new String[] {"image/jpeg", "image/jpg", "image/png", "image/x-png", "image/pjpeg"};

  /** Stores UIFormUploadInput instance. */
  private final UIFormUploadInput uiAvatarUploadInput;

  /**
   * Initializes upload form.<br>\
   *
   */
  public UIAvatarUploader() throws Exception {
    addUIFormInput(new UIFormStringInput(FIELD_NAME, null));
    uiAvatarUploadInput = new UIFormUploadInput(FIELD_UPLOADER, null, uploadLimit);
    uiAvatarUploadInput.setAutoUpload(true);
    addUIFormInput(uiAvatarUploadInput);
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
    for (String acceptedMimeType : acceptedMimeTypes) {
      if (mimeType.equals(acceptedMimeType)) return true;
    }
    return false;
  }

  /**
   * Changes and displays avatar on the profile if upload successful, else
   * inform user to upload image.
   */
  static public class ConfirmActionListener extends EventListener<UIAvatarUploader> {
    // The width of resized avatar fix 200px like facebook avatar
    private static final int WIDTH = 200;

    @Override
    public void execute(Event<UIAvatarUploader> event) throws Exception {
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApplication = ctx.getUIApplication();
      UIAvatarUploader uiAvatarUploader = event.getSource();
      UIFormUploadInput uiAvatarUploadInput = uiAvatarUploader.getChild(UIFormUploadInput.class);
      UIFormStringInput uiName = uiAvatarUploader.getChild(UIFormStringInput.class);
      UIPopupWindow uiPopup = uiAvatarUploader.getParent();
      InputStream uploadedStream = uiAvatarUploadInput.getUploadDataAsStream();

      if (uploadedStream == null) {
        uiApplication.addMessage(new ApplicationMessage(MSG_IMG_NOT_UPLOADED,
                                                        null,
                                                        ApplicationMessage.ERROR));
        ctx.addUIComponentToUpdateByAjax(uiAvatarUploader);
        return;
      }
      UploadResource uploadResource = uiAvatarUploadInput.getUploadResource();

      String mimeType = uploadResource.getMimeType();
      String uploadId = uiAvatarUploadInput.getUploadId();
      if (!uiAvatarUploader.isAcceptedMimeType(mimeType)) {
        UploadService uploadService = (UploadService) PortalContainer.getComponent(UploadService.class);
        uploadService.removeUpload(uploadId);
        uiApplication.addMessage(new ApplicationMessage(MSG_MIMETYPE_NOT_ACCEPTED,
                                                        null,
                                                        ApplicationMessage.ERROR));
        ctx.addUIComponentToUpdateByAjax(uiAvatarUploader);
      } else {
        MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
        String fileName = uiName.getValue();
        if (fileName == null)
          fileName = uploadResource.getFileName();
        else
          fileName = fileName + "." + mimeTypeResolver.getExtension(mimeType);

        // Resize avatar to fixed width if can't(avatarAttachment == null) keep
        // origin avatar
        AvatarAttachment avatarAttachment = ImageUtils.createResizedAvatarAttachment(uploadedStream,
                                                                                    WIDTH,
                                                                                    0,
                                                                                    null,
                                                                                    fileName,
                                                                                    mimeType,
                                                                                    null);
        if (avatarAttachment == null) {
          avatarAttachment = new AvatarAttachment(null,
                                                  fileName,
                                                  mimeType,
                                                  uploadedStream,
                                                  null,
                                                  System.currentTimeMillis());
        }

        UploadService uploadService = (UploadService) PortalContainer.getComponent(UploadService.class);
        uploadService.removeUpload(uploadId);
        UIAvatarUploadContent uiAvatarUploadContent = uiAvatarUploader.createUIComponent(UIAvatarUploadContent.class,
                                                                                         null,
                                                                                         null);
        uiAvatarUploadContent.setAvatarAttachment(avatarAttachment);
        uiPopup.setUIComponent(uiAvatarUploadContent);
        ctx.addUIComponentToUpdateByAjax(uiAvatarUploader.getParent());
      }
    }
  }

  /**
   * Cancels the upload image.<br>
   *
   */
  static public class CancelActionListener extends EventListener<UIAvatarUploader> {

    @Override
    public void execute(Event<UIAvatarUploader> event) throws Exception {
      UIAvatarUploader uiAvatarUploader = event.getSource();
      UIPopupWindow uiPopup = uiAvatarUploader.getParent();
      uiPopup.setShow(false);
    }

  }
}