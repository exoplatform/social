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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.social.core.image.ImageUtils;
import org.exoplatform.social.core.model.AvatarAttachment;
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


@ComponentConfigs ({
  @ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "war:/groovy/social/webui/UIAvatarUploader.gtmpl",
    events = {
      @EventConfig(listeners = UIAvatarUploader.ConfirmActionListener.class),
      @EventConfig(listeners = UIAvatarUploader.CancelActionListener.class)
    }
  )
})
public class UIAvatarUploader extends UIForm {
  /** Message alert that mimetype is not accepted. */
  private static final String MSG_MIMETYPE_NOT_ACCEPTED = "UIAvatarUploader.msg.mimetype_not_accepted";
  /** Message alert that image is not loaded. */
  private static final String MSG_IMAGE_NOT_LOADED = "UIAvatarUploader.msg.img_not_loaded";
/*  *//** Message alert that the file name is too long *//*
  *//** The number of characters allowed to rename *//*

  /** FIELD Uploader. */
  private static final String FIELD_UPLOADER = "Uploader";

  /** The limit size for upload image. */
  private static final int uploadLimit = 2; //MB

  /** List of accepted mimetype. */
  private static final String[] ACCEPTED_MIME_TYPES = new String[] {"image/jpeg", "image/jpg", "image/png", "image/x-png", "image/pjpeg"};

  /** Contains pair of IE Mimetype and Standard Mimetype. */
  private final Map<String, String> IE_MIME_TYPES = new HashMap<String, String>() {
    private static final long serialVersionUID = 1L;
    {
      put("image/pjpeg", "image/jpeg");
      put("image/x-png", "image/png");
    }
  };
  
  /**
   * Initializes upload form.<br>\
   *
   */
  public UIAvatarUploader() throws Exception {
    UIUploadInput uiAvatarUploadInput = new UIUploadInput(FIELD_UPLOADER, FIELD_UPLOADER, 1, uploadLimit);
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
    for (String acceptedMimeType : ACCEPTED_MIME_TYPES) {
      if (mimeType.equals(acceptedMimeType)) return true;
    }
    return false;
  }

  /**
   * Gets standard mimetype from IE mimetype appropriately.
   * 
   * @param mimeType
   * @return standard mimetype.
   * @since 1.1.3
   */
  private String getStandardMimeType(String mimeType) {
    return IE_MIME_TYPES.get(mimeType);  
  }
  
  /**
   * Changes and displays avatar on the profile if upload successful, else
   * inform user to upload image.
   */
  public static class ConfirmActionListener extends EventListener<UIAvatarUploader> {
    // The width of resized avatar fix 200px like facebook avatar
    private static final int WIDTH = 200;

    @Override
    public void execute(Event<UIAvatarUploader> event) throws Exception {
      WebuiRequestContext ctx = event.getRequestContext();
      UIAvatarUploader uiAvatarUploader = event.getSource();
      UIUploadInput uiAvatarUploadInput = uiAvatarUploader.getChild(UIUploadInput.class);
      if (uiAvatarUploadInput.getUploadResources().length < 1) {
        ctx.getUIApplication().addMessage(new ApplicationMessage(MSG_IMAGE_NOT_LOADED, null, ApplicationMessage.ERROR));
        ctx.addUIComponentToUpdateByAjax(uiAvatarUploader);
      }
      
      for (UploadResource uploadResource : uiAvatarUploadInput.getUploadResources()) {
        if (uploadResource == null) {
          continue;
        }
        try {
          String fileName = uploadResource.getFileName();
          if (fileName == null || fileName.length() == 0) {
            continue;
          }
          String mimeType = uploadResource.getMimeType();
          
          if (!uiAvatarUploader.isAcceptedMimeType(mimeType)) {
            ctx.getUIApplication().addMessage(new ApplicationMessage(MSG_MIMETYPE_NOT_ACCEPTED, null, ApplicationMessage.ERROR));
            ctx.addUIComponentToUpdateByAjax(uiAvatarUploader);
          } else {
            InputStream uploadedStream = new FileInputStream(new File(uploadResource.getStoreLocation()));
            MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
            
            // @since 1.1.3
            String extension = mimeTypeResolver.getExtension(mimeType);
            if ("".equals(extension)) {
              mimeType = uiAvatarUploader.getStandardMimeType(mimeType);
            }

            AvatarAttachment avatarAttachment = ImageUtils.createResizedAvatarAttachment(uploadedStream, WIDTH, 0, null,
                                                                                         fileName, mimeType, null);
            if (avatarAttachment == null) {
              avatarAttachment = new AvatarAttachment(null, fileName, mimeType, uploadedStream, null, System.currentTimeMillis());
            }
            //
            UIAvatarUploadContent uiAvatarUploadContent = uiAvatarUploader.createUIComponent(UIAvatarUploadContent.class, null, null);
            uiAvatarUploadContent.setAvatarAttachment(avatarAttachment);
            //
            UIPopupWindow uiPopup = uiAvatarUploader.getParent();
            uiPopup.setUIComponent(uiAvatarUploadContent);
            ctx.addUIComponentToUpdateByAjax(uiPopup);
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
  public static class CancelActionListener extends EventListener<UIAvatarUploader> {
    @Override
    public void execute(Event<UIAvatarUploader> event) throws Exception {
      UIAvatarUploader uiAvatarUploader = event.getSource();
      UIPopupWindow uiPopup = uiAvatarUploader.getParent();
      uiPopup.setShow(false);
      uiPopup.setRendered(false);
      uiPopup.setUIComponent(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup.getParent());
    }
  }
}
