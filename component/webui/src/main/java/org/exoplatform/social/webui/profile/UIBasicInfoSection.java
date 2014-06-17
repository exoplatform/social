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

import java.util.List;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.EmailAddressValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.PersonalNameValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;

/**
 * Component manages basic information. This is one part of profile management
 * beside contact and experience.<br>
 * Modified : dang.tung tungcnw@gmail.com Aug 11, 2009
 */

@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "war:/groovy/social/webui/profile/UIBasicInfoSection.gtmpl",
  events = {
    @EventConfig(listeners = UIBasicInfoSection.EditActionListener.class, phase = Phase.DECODE),
    @EventConfig(listeners = UIBasicInfoSection.SaveActionListener.class, csrfCheck = true),
    @EventConfig(listeners = UIBasicInfoSection.CancelActionListener.class, phase = Phase.DECODE)
  }
)
public class UIBasicInfoSection extends UIProfileSection {
  public String lastloadUser;

 
  public UIBasicInfoSection() throws Exception {
    addChild(UITitleBar.class, null, null);

    UIFormStringInput userName = new UIFormStringInput(Profile.USERNAME, Profile.USERNAME, null);
    userName.setEditable(false);
    addUIFormInput(userName.addValidator(MandatoryValidator.class));
    
    addUIFormInput(new UIFormStringInput(Profile.FIRST_NAME,
                                         Profile.FIRST_NAME,
                                         null).
                   addValidator(MandatoryValidator.class).addValidator(PersonalNameValidator.class).
                   addValidator(StringLengthValidator.class, 1, 45));

    addUIFormInput(new UIFormStringInput(Profile.LAST_NAME,
                                         Profile.LAST_NAME,
                                         null).
                   addValidator(MandatoryValidator.class).addValidator(PersonalNameValidator.class).
                   addValidator(StringLengthValidator.class, 1, 45));

    addUIFormInput(new UIFormStringInput(Profile.EMAIL, Profile.EMAIL, null).
                   addValidator(MandatoryValidator.class).
                   addValidator(EmailAddressValidator.class));
  }

  /**
   * Reloads basic info in each request call
   */
  public void reloadBasicInfo() {
    if (isFirstLoad() == false) {
      Identity ownerIdentity = Utils.getOwnerIdentity(false);
      Profile profile = ownerIdentity.getProfile();
      this.getUIStringInput(Profile.USERNAME).setValue((String) profile.getProperty(Profile.USERNAME));
      this.getUIStringInput(Profile.FIRST_NAME).setValue((String) profile.getProperty(Profile.FIRST_NAME));
      this.getUIStringInput(Profile.LAST_NAME).setValue((String) profile.getProperty(Profile.LAST_NAME));
      this.getUIStringInput(Profile.EMAIL).setValue((String) profile.getProperty(Profile.EMAIL));
      if (isEditMode())
        setFirstLoad(true);
    }
  }

  /**
   * Gets and sort all uicomponents.<br>
   *
   * @return All children in order.
   */
  public List<UIComponent> getChilds() {
    return getChildren();
  }

  /**
   * Changes form into edit mode when user click edit button.<br>
   */
  public static class EditActionListener extends UIProfileSection.EditActionListener {

    @Override
    public void execute(Event<UIProfileSection> event) throws Exception {
      super.execute(event);
      UIProfileSection sect = event.getSource();
      UIBasicInfoSection uiForm = (UIBasicInfoSection) sect;
      WebuiRequestContext requestContext = event.getRequestContext();
      requestContext.addUIComponentToUpdateByAjax(uiForm);
      requestContext.addUIComponentToUpdateByAjax(sect);
      sect.setFirstLoad(false);
    }
  }

  /**
   * Stores profile information into database when form is submitted.<br>
   */
  public static class SaveActionListener extends UIProfileSection.SaveActionListener {

    @Override
    public void execute(Event<UIProfileSection> event) throws Exception {
      super.execute(event);

      UIBasicInfoSection uiForm = (UIBasicInfoSection) event.getSource();
      

      String firstName = uiForm.getUIStringInput(Profile.FIRST_NAME).getValue();
      String lastName = uiForm.getUIStringInput(Profile.LAST_NAME).getValue();
      String newEmail = uiForm.getUIStringInput(Profile.EMAIL).getValue();
      String fullName = firstName + " " + lastName;
      
      Identity viewerIdentity = Utils.getViewerIdentity(true);
      Profile profile = viewerIdentity.getProfile();
      boolean profileHasUpdated = false;
      if (!(profile.getProperty(Profile.FIRST_NAME)).equals(firstName)) {
        profile.setProperty(Profile.FIRST_NAME, firstName);
        profile.setProperty(Profile.FULL_NAME, fullName);
        profileHasUpdated = true;
      }
      if (!(profile.getProperty(Profile.LAST_NAME)).equals(lastName)) {
        profile.setProperty(Profile.LAST_NAME, lastName);
        profile.setProperty(Profile.FULL_NAME, fullName);
        profileHasUpdated = true;
      }
      if (!(profile.getProperty(Profile.EMAIL)).equals(newEmail)) {
        profile.setProperty(Profile.EMAIL, newEmail);
        profileHasUpdated = true;
      }
      if (profileHasUpdated) {
        Utils.getIdentityManager().updateProfile(profile);
        //updates profile
        Utils.getOwnerIdentity(true);
      }
      
      uiForm.setFirstLoad(false);
      Utils.updateWorkingWorkSpace();
    }
  }
  
  
}
