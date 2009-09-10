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
import java.io.InputStream;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Profile;
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
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormUploadInput;

/**
 * Created by The eXo Platform SAS
 * Author : hoatle
 *          hoatlevan@gmail.com
 * Sep 8, 2009  
 */
@ComponentConfigs ({
  @ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    //template = "app:/groovy/portal/webui/component/UIAvatarUploader.gtmpl",
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIAvatarUploader.ChangeActionListener.class),
      @EventConfig(listeners = UIAvatarUploader.CancelActionListener.class)
    }
  )
})
public class UIAvatarUploader extends UIForm {
  private UIFormUploadInput uiAvatarUploadInput;
  final public static String AVARTAR = "avatar";
  private byte[] imageBytes = null;
  //private final Integer avatarWidth = 156;
  //private final Integer avatarHeight = 197;
  static final String MSG_IMG_NOT_UPLOADED = UIAvatarUploader.class.getSimpleName()+".msg.img_not_loaded";
  /**
   * Constructor: Add UIFormUploadInput
   */
  public UIAvatarUploader() throws Exception {
    uiAvatarUploadInput = new UIFormUploadInput("UIAvatarUploader", null);
    addUIFormInput(uiAvatarUploadInput);
    setActions(new String[]{"Change", "Cancel"});
  }
  
  protected void setImageBytes(InputStream input) throws Exception {
    if (input != null) {
      imageBytes = new byte[input.available()];
      input.read(imageBytes);
    } else {
      imageBytes = null;
    }
  }
  
  protected byte[] getImageBytes() {
    return imageBytes;
  }
  
  protected String getImageSource() throws Exception {
    if (imageBytes == null || imageBytes.length == 0) return null;
    ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes);
    DownloadService downloadService = getApplicationComponent(DownloadService.class);
    InputStreamDownloadResource downloadResource = new InputStreamDownloadResource(byteImage, "image");
    downloadResource.setDownloadName("image");
    return downloadService.getDownloadLink(downloadService.addDownloadResource(downloadResource));
  }
  
  /**
   * This action will be triggered when user click on change avatar button.
   * If there is uploaded image, change and display avatar on the profile.
   * if no, inform user to upload image.
   * @author hoatle
   */
  static public class ChangeActionListener extends EventListener<UIAvatarUploader> {

    @Override
    public void execute(Event<UIAvatarUploader> event) throws Exception {
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApp = ctx.getUIApplication();
      UIAvatarUploader uiAvatarUploader = event.getSource();
      UIFormUploadInput uiAvatarUploadInput = uiAvatarUploader.getChild(UIFormUploadInput.class);
      UIPopupWindow uiPopup = uiAvatarUploader.getParent();
      InputStream input = uiAvatarUploadInput.getUploadDataAsStream();
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      IdentityManager im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
      UIProfile uiProfile = uiAvatarUploader.getAncestorOfType(UIProfile.class);
      if (input == null) {
        uiApp.addMessage(new ApplicationMessage(MSG_IMG_NOT_UPLOADED, null, ApplicationMessage.WARNING));
      } else {
        uiPopup.setShow(false);
        uiAvatarUploader.setImageBytes(input);
        Profile p = uiProfile.getProfile();
        p.setProperty(AVARTAR, uiAvatarUploader.getImageSource());
        im.saveProfile(p);
      }
      //TODO Save to database
      //Update UIProfile
      ctx.addUIComponentToUpdateByAjax(uiPopup.getParent());
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
      WebuiRequestContext ctx = event.getRequestContext();
      UIAvatarUploader uiAvatarUploader = event.getSource();
      UIPopupWindow uiPopup = uiAvatarUploader.getParent();
      uiPopup.setShow(false);
    }
    
  }
}
