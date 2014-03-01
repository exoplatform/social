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
import org.exoplatform.social.webui.UIAvatarUploadContent;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Component is used for profile managing, it is the
 * place put all other profile management components.<br>
 *
 */

@ComponentConfig(
  template = "war:/groovy/social/webui/profile/UIProfile.gtmpl"
)
public class UIProfile extends UIContainer {

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

    Utils.getIdentityManager().updateProfile(p);
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
