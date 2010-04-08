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
package org.exoplatform.social.portlet.profile;

import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.webui.URLUtils;
import org.exoplatform.web.CacheUserProfileFilter;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestContext;
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
import org.exoplatform.webui.form.validator.StringLengthValidator;

/**
 * Component manages basic information.
 * This is one part of profile management beside contact and experience.<br>
 * 
 * Modified : dang.tung
 *          tungcnw@gmail.com
 * Aug 11, 2009          
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/portal/webui/component/UIBasicInfoSection.gtmpl",
    events = {
    	@EventConfig(listeners = UIBasicInfoSection.EditActionListener.class, phase=Phase.DECODE),
        @EventConfig(listeners = UIBasicInfoSection.SaveActionListener.class),
        @EventConfig(listeners = UIBasicInfoSection.CancelActionListener.class, phase=Phase.DECODE)        
    }
)

public class UIBasicInfoSection extends UIProfileSection {
  /** FIRST NAME. */
  final public static String FIRST_NAME = "firstName";
  /** LAST NAME. */
  final public static String LAST_NAME = "lastName";
  /** GENDER. */
  final public static String GENDER = "gender";
  /** DEFAULT GENDER. */
  final public static String GENDER_DEFAULT = "Gender";
  /** MALE. */
  final public static String MALE = "male";
  /** FEMALE. */
  final public static String FEMALE = "female";
  /** REGEX EXPRESSION. */
  final public static String REGEX_EXPRESSION = "^\\p{L}[\\p{L}\\d._,\\s]+\\p{L}$";
  /** INVALID CHARACTER MESSAGE. */
  final public static String INVALID_CHAR_MESSAGE = "UIBasicInfoSection.msg.Invalid-char";
  
  public UIBasicInfoSection() throws Exception {
    String username = Util.getPortalRequestContext().getRemoteUser();
    OrganizationService service = this.getApplicationComponent(OrganizationService.class);
    User useraccount = service.getUserHandler().findUserByName(username);
    
    addChild(UITitleBar.class, null, null);
    
    UIFormStringInput userName = new UIFormStringInput("userName", "userName", username);
    userName.setEditable(false);
    addUIFormInput(userName);
    addUIFormInput(new UIFormStringInput(FIRST_NAME, FIRST_NAME, useraccount.getFirstName())
                   .addValidator(MandatoryValidator.class)
                   .addValidator(StringLengthValidator.class, 3, 30)
                   .addValidator(ExpressionValidator.class, REGEX_EXPRESSION, INVALID_CHAR_MESSAGE));
    addUIFormInput(new UIFormStringInput(LAST_NAME, LAST_NAME, useraccount.getLastName())
                   .addValidator(MandatoryValidator.class)
                   .addValidator(StringLengthValidator.class, 3, 30)
                   .addValidator(ExpressionValidator.class, REGEX_EXPRESSION, INVALID_CHAR_MESSAGE));
    addUIFormInput(new UIFormStringInput("email", "email", useraccount.getEmail()).addValidator(
            MandatoryValidator.class).addValidator(EmailAddressValidator.class));
  }
  
  public User getViewUser() throws Exception {
    RequestContext context = RequestContext.getCurrentInstance();
    String currentUserName = context.getRemoteUser();
    String currentViewer = URLUtils.getCurrentUser();
    
    if((currentViewer != null) && (currentViewer != currentUserName)) {
      OrganizationService orgSer = getApplicationComponent(OrganizationService.class);
      UserHandler userHandler = orgSer.getUserHandler();
      return userHandler.findUserByName(currentViewer);      
    }
    
    ConversationState state = ConversationState.getCurrent();
    return (User) state.getAttribute(CacheUserProfileFilter.USER_PROFILE);
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
   *
   */
  public static class EditActionListener extends UIProfileSection.EditActionListener {

    public void execute(Event<UIProfileSection> event) throws Exception {
      super.execute(event);
      UIProfileSection sect = event.getSource();
      UIBasicInfoSection uiForm = (UIBasicInfoSection)sect;
      String username = Util.getPortalRequestContext().getRemoteUser();
	  OrganizationService service = uiForm.getApplicationComponent(OrganizationService.class);
	  User user = service.getUserHandler().findUserByName(username);
      	  
	  uiForm.getUIStringInput("firstName").setValue(user.getFirstName());
      uiForm.getUIStringInput("lastName").setValue(user.getLastName());
      uiForm.getUIStringInput("email").setValue(user.getEmail());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
      event.getRequestContext().addUIComponentToUpdateByAjax(sect);
    }
  }
  
  /**
   *  Stores profile information into database when form is submitted.<br>
   *
   */
  public static class SaveActionListener extends UIProfileSection.SaveActionListener {

    public void execute(Event<UIProfileSection> event) throws Exception {
      super.execute(event);
      
      UIProfileSection sect = event.getSource();
      UIBasicInfoSection uiForm = (UIBasicInfoSection)sect;
      
      OrganizationService service = uiForm.getApplicationComponent(OrganizationService.class);
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
      UIApplication uiApp = context.getUIApplication();

      String userName = uiForm.getUIStringInput("userName").getValue();
      User user = service.getUserHandler().findUserByName(userName);
      String oldEmail = user.getEmail();
      String newEmail = uiForm.getUIStringInput("email").getValue();

      // Check if mail address is already used
      Query query = new Query();
      query.setEmail(newEmail);
      if (service.getUserHandler().findUsers(query).getAll().size() > 0 && !oldEmail.equals(newEmail))
      {
         //Be sure it keep old value
         user.setEmail(oldEmail);
         Object[] args = {userName};
         uiApp.addMessage(new ApplicationMessage("UIAccountInputSet.msg.email-exist", args));
         return;
      }
      
      //TODO: save in profile
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      IdentityManager im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
      
      Profile p = sect.getProfile(true);
      
      p.setProperty(Profile.FIRST_NAME, uiForm.getUIStringInput("firstName").getValue());
      p.setProperty(Profile.LAST_NAME, uiForm.getUIStringInput("lastName").getValue());
      p.setProperty(Profile.USERNAME, newEmail); 
      im.updateBasicInfo(p);
      
      user.setFirstName(uiForm.getUIStringInput("firstName").getValue());
      user.setLastName(uiForm.getUIStringInput("lastName").getValue());
      user.setEmail(newEmail);
      uiApp.addMessage(new ApplicationMessage("UIAccountProfiles.msg.update.success", null));
      service.getUserHandler().saveUser(user, true);

      UIProfile uiProfile = uiForm.getParent();
      context.addUIComponentToUpdateByAjax(uiProfile.getChild(UIHeaderSection.class));
      UIWorkingWorkspace uiWorkingWS = Util.getUIPortalApplication().getChild(UIWorkingWorkspace.class);
      uiWorkingWS.updatePortletsByName("profile");
      ConversationState state = ConversationState.getCurrent();
      if (userName.equals(((User)state.getAttribute(CacheUserProfileFilter.USER_PROFILE)).getUserName()))
      {
         state.setAttribute(CacheUserProfileFilter.USER_PROFILE, user);
         uiWorkingWS.updatePortletsByName("UserInfoPortlet");
      }
    }
  }
}
