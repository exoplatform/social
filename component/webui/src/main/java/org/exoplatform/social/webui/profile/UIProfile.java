/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.webui.profile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.image.ImageUtils;
import org.exoplatform.social.core.model.AvatarAttachment;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.webui.UIAvatarUploadContent;
import org.exoplatform.social.webui.UIAvatarUploader;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Component is used for profile managing, it is the
 * place put all other profile management components.<br>
 *
 */

@ComponentConfig(
  template = "classpath:groovy/social/webui/profile/UIProfile.gtmpl",
  events = {
    @EventConfig(listeners=UIProfile.ChangeAvatarActionListener.class)
  }
)
public class UIProfile extends UIContainer {

  private final String POPUP_AVATAR_UPLOADER = "UIPopupAvatarUploader";

  /**
   * Constructor to initialize UIAvatarUploader popup and info sections
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public UIProfile() throws Exception {

    List sections = getSections();
    java.util.Iterator it = sections.iterator();
    while (it.hasNext()) {
      Class sect = (Class)it.next();
      addChild(sect, null, null);
    }
    UIPopupWindow uiPopup = createUIComponent(UIPopupWindow.class, null, POPUP_AVATAR_UPLOADER);
    uiPopup.setWindowSize(500, 0);
    addChild(uiPopup);
  }

  /**
   * Gets profile with forceReload.
   * @return
   * @throws Exception
   */
  public Profile getProfile() throws Exception {
    return Utils.getOwnerIdentity(true).getProfile();
  }

  /**
   * Checks the current user is right edit permission.<br>
   *
   * @return true if current user has permission.
   */
  public boolean isEditable() {
    return Utils.isOwner();
  }

  /**
   * @param uiAvatarUploadContent
   * @throws Exception
   */
  public void saveAvatar(UIAvatarUploadContent uiAvatarUploadContent) throws Exception {
    AvatarAttachment attacthment = uiAvatarUploadContent.getAvatarAttachment();

    Profile p = getProfile();
    p.setProperty(Profile.AVATAR, attacthment);
    Map<String, Object> props = p.getProperties();

    // Removes avatar url and resized avatar
    for (String key : props.keySet()) {
      if (key.startsWith(Profile.AVATAR + ImageUtils.KEY_SEPARATOR)) {
        p.removeProperty(key);
      }
    }

    Utils.getIdentityManager().updateAvatar(p);
    p = getProfile();
    attacthment = (AvatarAttachment) p.getProperty(Profile.AVATAR);
    p.setProperty(Profile.AVATAR_URL, LinkProvider.buildAvatarImageUri(attacthment));
    Utils.getIdentityManager().saveProfile(p);
  }

  /**
   * Action trigger for editting avatar. An UIAvatarUploader popup should be displayed.
   * @author hoatle
   *
   */
  public static class ChangeAvatarActionListener extends EventListener<UIProfile> {

    @Override
    public void execute(Event<UIProfile> event) throws Exception {
      UIProfile uiProfile = event.getSource();
      UIPopupWindow uiPopup = uiProfile.getChild(UIPopupWindow.class);
      UIAvatarUploader uiAvatarUploader = uiProfile.createUIComponent(UIAvatarUploader.class, null, null);
      uiPopup.setUIComponent(uiAvatarUploader);
      uiPopup.setShow(true);
    }

  }

 /**
  *
  * @return the list of sections ordered by display order
  */
  @SuppressWarnings("unchecked")
  private List getSections() {
    List sects = new ArrayList();
    sects.add(UIHeaderSection.class);
    sects.add(UIBasicInfoSection.class);
    sects.add(UIContactSection.class);
    sects.add(UIExperienceSection.class);
    return sects;
  }
}
