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

import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.web.CacheUserProfileFilter;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.EmailAddressValidator;
import org.exoplatform.webui.form.validator.ExpressionValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.ResourceValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;

/**
 * Component manages basic information. This is one part of profile management
 * beside contact and experience.<br>
 * Modified : dang.tung tungcnw@gmail.com Aug 11, 2009
 */

@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "classpath:groovy/social/webui/profile/UIBasicInfoSection.gtmpl",
  events = {
    @EventConfig(listeners = UIBasicInfoSection.EditActionListener.class, phase = Phase.DECODE),
    @EventConfig(listeners = UIBasicInfoSection.SaveActionListener.class),
    @EventConfig(listeners = UIBasicInfoSection.CancelActionListener.class, phase = Phase.DECODE)
  }
)
public class UIBasicInfoSection extends UIProfileSection {

  /** REGEX EXPRESSION. */
  public static final String USER_NAME_VALIDATOR_REGEX = "^[\\p{L}][\\p{L}._\\-\\d]+$";

  /** INVALID CHARACTER MESSAGE. */
  public static final String INVALID_CHAR_MESSAGE = "UIBasicInfoSection.msg.Invalid-char";

  public UIBasicInfoSection() throws Exception {
    String username = Utils.getViewerRemoteId();
    OrganizationService service = this.getApplicationComponent(OrganizationService.class);
    User useraccount = service.getUserHandler().findUserByName(username);

    addChild(UITitleBar.class, null, null);

    UIFormStringInput userName = new UIFormStringInput(Profile.USERNAME, Profile.USERNAME, username);
    userName.setEditable(false);
    addUIFormInput(userName.addValidator(MandatoryValidator.class).addValidator(StringLengthValidator.class, 3, 30)
                   .addValidator(ResourceValidator.class).addValidator(ExpressionValidator.class,
                   USER_NAME_VALIDATOR_REGEX, "ResourceValidator.msg.Invalid-char"));
    
    addUIFormInput(new UIFormStringInput(Profile.FIRST_NAME,
                                         Profile.FIRST_NAME,
                                         useraccount.getFirstName()).
                   addValidator(MandatoryValidator.class).
                   addValidator(StringLengthValidator.class, 1, 45));

    addUIFormInput(new UIFormStringInput(Profile.LAST_NAME,
                                         Profile.LAST_NAME,
                                         useraccount.getLastName()).
                   addValidator(MandatoryValidator.class).
                   addValidator(StringLengthValidator.class, 1, 45));

    addUIFormInput(new UIFormStringInput(Profile.EMAIL, Profile.EMAIL, useraccount.getEmail()).
                   addValidator(MandatoryValidator.class).
                   addValidator(EmailAddressValidator.class));
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
   * Changes form into edit mode when user click eddit button.<br>
   */
  public static class EditActionListener extends UIProfileSection.EditActionListener {

    @Override
    public void execute(Event<UIProfileSection> event) throws Exception {
      super.execute(event);
      UIProfileSection sect = event.getSource();
      UIBasicInfoSection uiForm = (UIBasicInfoSection) sect;
      String username = Utils.getViewerRemoteId();
      OrganizationService service = uiForm.getApplicationComponent(OrganizationService.class);
      User user = service.getUserHandler().findUserByName(username);

      uiForm.getUIStringInput(Profile.FIRST_NAME).setValue(user.getFirstName());
      uiForm.getUIStringInput(Profile.LAST_NAME).setValue(user.getLastName());
      uiForm.getUIStringInput(Profile.EMAIL).setValue(user.getEmail());
      WebuiRequestContext requestContext = event.getRequestContext();
      requestContext.addUIComponentToUpdateByAjax(uiForm);
      requestContext.addUIComponentToUpdateByAjax(sect);
    }
  }

  /**
   * Stores profile information into database when form is submitted.<br>
   */
  public static class SaveActionListener extends UIProfileSection.SaveActionListener {
    private static final String MSG_KEY_UI_ACCOUNT_INPUT_SET_EMAIL_EXIST   = "UIAccountInputSet.msg.email-exist";
//    private static final String MSG_KEY_UI_ACCOUNT_PROFILES_UPDATE_SUCCESS = "UIAccountProfiles.msg.update.success";
//    private static final String PORTLET_NAME_USER_PROFILE_TOOLBAR_PORTLET  = "UserProfileToolBarPortlet";
//    private static final String PORTLET_NAME_USER_PROFILE_PORTLET          = "ProfilePortlet";

    @Override
    public void execute(Event<UIProfileSection> event) throws Exception {
      super.execute(event);

      UIBasicInfoSection uiForm = (UIBasicInfoSection) event.getSource();

      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      UIApplication uiApp = context.getUIApplication();

      String userName = uiForm.getUIStringInput(Profile.USERNAME).getValue();
      String firstName = uiForm.getUIStringInput(Profile.FIRST_NAME).getValue();
      String lastName = uiForm.getUIStringInput(Profile.LAST_NAME).getValue();
      String newEmail = uiForm.getUIStringInput(Profile.EMAIL).getValue();
      OrganizationService service = uiForm.getApplicationComponent(OrganizationService.class);
      User user = service.getUserHandler().findUserByName(userName);
      String oldEmail = user.getEmail();

      // Check if mail address is already used
      Query query = new Query();
      query.setEmail(newEmail);
      if (!oldEmail.equals(newEmail) && service.getUserHandler().findUsers(query).getAll().size() > 0) {
        // Be sure it keep old value
        user.setEmail(oldEmail);
        Object[] args = { userName };
        uiApp.addMessage(new ApplicationMessage(MSG_KEY_UI_ACCOUNT_INPUT_SET_EMAIL_EXIST, args));
        return;
      }

      user.setFirstName(userName);
      user.setFirstName(firstName);
      user.setLastName(lastName);
      user.setEmail(newEmail);
      service.getUserHandler().saveUser(user, true);
      ConversationState.getCurrent().setAttribute(CacheUserProfileFilter.USER_PROFILE,user);

      Utils.updateWorkingWorkSpace();
    }
  }
}