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
package social.portal.webui.component;

import java.io.InputStream;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.space.SpaceAttachment;
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
 * UIAvatarUploader.java
 * Upload images to set space's avatar.
 * Created by The eXo Platform SAS
 * Author : hanhvq
 *          hanhvq@gmail.com
 * Nov 10, 2009  
 */
@ComponentConfigs ({
  @ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/portal/webui/uiform/UIAvatarUploader.gtmpl",
    events = {
      @EventConfig(listeners = UIAvatarUploader.ConfirmActionListener.class),
      @EventConfig(listeners = UIAvatarUploader.CancelActionListener.class)
    }
  )
})
public class UIAvatarUploader extends UIForm {
  private String FIELD_NAME = "Name";
  private String FIELD_Uploader = "Uploader";
  private UIFormUploadInput uiAvatarUploadInput;
  private int uploadLimit = 2; //MB
  private String[] acceptedMimeTypes = new String[] {"image/gif", "image/jpeg", "image/jpg", "image/png", "image/x-png", "image/pjpeg"};
  static final String MSG_IMG_NOT_UPLOADED = "UIAvatarUploader.msg.img_not_loaded";
  static final String MSG_MIMETYPE_NOT_ACCEPTED = "UIAvatarUploader.msg.mimetype_not_accepted";
  /**
   * Constructor: Add UIFormUploadInput
   */
  public UIAvatarUploader() throws Exception {
    addUIFormInput(new UIFormStringInput(FIELD_NAME, null));
    uiAvatarUploadInput = new UIFormUploadInput(FIELD_Uploader, null, uploadLimit);
    uiAvatarUploadInput.setAutoUpload(true);
    addUIFormInput(uiAvatarUploadInput);
    setActions(new String[]{"Confirm", "Cancel"});
  }
  
  /**
   * checks if the provided mimeType matches acceptedMimeTypes
   * @param mimeType String
   * @return boolean
   */
  private boolean isAcceptedMimeType(String mimeType) {
    for (String acceptedMimeType : acceptedMimeTypes) {
      if (mimeType.equals(acceptedMimeType)) return true;
    }
    return false;
  }
  
  /**
   * gets mime extension from mimetype
   * eg: image/gif => gif; image/jpg => jpg
   * @param mimeType
   * @return file extension
   */
  private String getMimeExtension(String mimeType) {
    int slashIndex = mimeType.lastIndexOf('/');
    return mimeType.substring(slashIndex + 1);
  }
  

  /**
   * This action will be triggered when user click on change avatar button.
   * If there is uploaded image, change and display avatar on the space.
   * if no, inform user to upload image.
   * @author hoatle
   */
  static public class ConfirmActionListener extends EventListener<UIAvatarUploader> {

    @Override
    public void execute(Event<UIAvatarUploader> event) throws Exception {
      UIAvatarUploader uiAvatarUploader = event.getSource();
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApplication = ctx.getUIApplication();
      UIFormUploadInput uiAvatarUploadInput = uiAvatarUploader.getChild(UIFormUploadInput.class);
      UIFormStringInput uiName = uiAvatarUploader.getChild(UIFormStringInput.class);
      String newName = uiName.getValue();
      UIPopupWindow uiPopup = uiAvatarUploader.getParent();
      InputStream input = uiAvatarUploadInput.getUploadDataAsStream();
      if (input == null) {
        uiApplication.addMessage(new ApplicationMessage(MSG_IMG_NOT_UPLOADED, null, ApplicationMessage.ERROR));
        ctx.addUIComponentToUpdateByAjax(uiAvatarUploader);
        return;
      }
      UploadResource uploadResource = uiAvatarUploadInput.getUploadResource();
      if (!uiAvatarUploader.isAcceptedMimeType(uploadResource.getMimeType())) {
        UploadService uploadService = (UploadService)PortalContainer.getComponent(UploadService.class);
        uploadService.removeUpload(uiAvatarUploadInput.getUploadId());
        uiApplication.addMessage(new ApplicationMessage(MSG_MIMETYPE_NOT_ACCEPTED, null, ApplicationMessage.ERROR));
        ctx.addUIComponentToUpdateByAjax(uiAvatarUploader);
      } else {
        SpaceAttachment spaceAtt = new SpaceAttachment();
        
        spaceAtt.setInputStream(uiAvatarUploadInput.getUploadDataAsStream());
        spaceAtt.setMimeType(uploadResource.getMimeType());
        if (newName == null) {
          newName = uploadResource.getFileName();
        } else {
          newName = newName + "." + uiAvatarUploader.getMimeExtension(uploadResource.getMimeType());
        }
        spaceAtt.setFileName(newName);
        spaceAtt.setLastModified(System.currentTimeMillis());
        
        UploadService uploadService = (UploadService)PortalContainer.getComponent(UploadService.class);
        uploadService.removeUpload(uiAvatarUploadInput.getUploadId());
        
        UIAvatarUploadContent uiAvatarUploadContent = uiAvatarUploader.createUIComponent(UIAvatarUploadContent.class, null, null);
        uiAvatarUploadContent.setSpaceAttachment(spaceAtt);
        uiPopup.setUIComponent(uiAvatarUploadContent);
        ctx.addUIComponentToUpdateByAjax(uiAvatarUploader.getParent());
      }
      }
  }
  
  /**
   * This action will be triggered when user click on cancel button.
   * This action is something like Close button in the parent popup window.
   * Clean perform can be done in this action.
   * @author hoatle
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