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

import java.io.ByteArrayInputStream;

import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceAttachment;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceUtils;
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
  template = "app:/groovy/portal/webui/uiform/UIAvatarUploadContent.gtmpl",
  events = {
      @EventConfig(listeners = UIAvatarUploadContent.SaveActionListener.class),
      @EventConfig(listeners = UIAvatarUploadContent.AbortActionListener.class)
  }
)
public class UIAvatarUploadContent extends UIContainer {
  static public final String AVARTAR_PROPERTY = "avatar";
  private SpaceAttachment spaceAttachment;
  private String imageSource;
  
  /**
   * constructor
   */
  public UIAvatarUploadContent() {
  
  }
  
  /**
   * constructor
   * @param SpaceAttachment
   * @throws Exception 
   */
  public UIAvatarUploadContent(SpaceAttachment spaceAttachment) throws Exception {
    this.spaceAttachment = spaceAttachment;
    setImageSource(spaceAttachment.getImageBytes());
  }
  
  
  /**
   * gets SpaceAttachment
   * @return SpaceAttachment
   */
  public SpaceAttachment getSpaceAttachment() {
    return spaceAttachment;
  }
  
  /**
   * sets SpaceAttachment
   * @param SpaceAttachment
   * @throws Exception 
   */
  public void setSpaceAttachment(SpaceAttachment spaceAttachment) throws Exception {
    this.spaceAttachment = spaceAttachment;
    setImageSource(spaceAttachment.getImageBytes());
  }
  
  private void setImageSource(byte[] imageBytes) throws Exception {
    if (imageBytes == null || imageBytes.length == 0) return;
    ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes);
    DownloadService downloadService = getApplicationComponent(DownloadService.class);
    InputStreamDownloadResource downloadResource = new InputStreamDownloadResource(byteImage, "image");
    downloadResource.setDownloadName(spaceAttachment.getFileName());
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
   * accepts and saves the uploaded image to Space
   * closes the popup window, refreshes UISpace
   * @author hoatle
   */
  static public class SaveActionListener extends EventListener<UIAvatarUploadContent> {
    @Override
    public void execute(Event<UIAvatarUploadContent> event) throws Exception {
      WebuiRequestContext ctx = event.getRequestContext();
      UIAvatarUploadContent uiAvatarUploadContent = event.getSource();
      UISpaceInfo uiSpaceInfo = uiAvatarUploadContent.getAncestorOfType(UISpaceInfo.class);
      SpaceService spaceService = uiSpaceInfo.getSpaceService();
      
      String id = uiSpaceInfo.getUIStringInput("id").getValue();
      Space space = spaceService.getSpaceById(id);
      space.setSpaceAttachment(uiAvatarUploadContent.getSpaceAttachment());
      spaceService.saveSpace(space, false);
      
      UIPopupWindow uiPopup = uiAvatarUploadContent.getParent();
      uiPopup.setShow(false);
      SpaceUtils.updateWorkingWorkSpace();
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
