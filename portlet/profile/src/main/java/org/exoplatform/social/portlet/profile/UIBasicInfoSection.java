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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.webui.URLUtils;
import org.exoplatform.web.CacheUserProfileFilter;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.ExpressionValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;

/**
 * Created by The eXo Platform SARL
 * Modified : dang.tung
 *          tungcnw@gmail.com
 * Aug 11, 2009          
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/portal/webui/component/UIBasicInfoSection.gtmpl",
    events = {
        @EventConfig(listeners = UIProfileSection.EditActionListener.class, phase=Phase.DECODE)
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
    addChild(UIBasicInfoTitleBar.class, null, null);
    
    addUIFormInput(new UIFormStringInput(FIRST_NAME, FIRST_NAME, null)
                   .addValidator(MandatoryValidator.class)
                   .addValidator(StringLengthValidator.class, 3, 30)
                   .addValidator(ExpressionValidator.class, REGEX_EXPRESSION, INVALID_CHAR_MESSAGE));
    addUIFormInput(new UIFormStringInput(LAST_NAME, LAST_NAME, null)
                   .addValidator(MandatoryValidator.class)
                   .addValidator(StringLengthValidator.class, 3, 30)
                   .addValidator(ExpressionValidator.class, REGEX_EXPRESSION, INVALID_CHAR_MESSAGE));
    addUIFormInput(new UIFormStringInput("Email", "Email", null));

//    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
//    options.add(new SelectItemOption<String>(GENDER_DEFAULT));
//    options.add(new SelectItemOption<String>(MALE));
//    options.add(new SelectItemOption<String>(FEMALE));
//    addUIFormInput(new UIFormSelectBox(GENDER, GENDER, options));
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
  
//  /**
//   * Store profile information into database when form is submitted.
//   *
//   */
//  public static class SaveActionListener extends UIProfileSection.SaveActionListener {
//
//    public void execute(Event<UIProfileSection> event) throws Exception {
//      super.execute(event);
//      
//      UIProfileSection sect = event.getSource();    
//      
//      saveProfile((UIBasicInfoSection)sect);      
//      
//      //Get the UIHeaderSection to refresh it since it contains also the firstname and lastname
//      UIComponent parent = sect.getParent();
//      UIProfileSection header = parent.findFirstComponentOfType(UIHeaderSection.class);
//      if(header != null) {
//        event.getRequestContext().addUIComponentToUpdateByAjax(header);
//      }
//    }       
//  }  
  
  /**
   * Get information from profile and set value into components.
   * 
   * @throws Exception
   */
  public void setValue() throws Exception {
    ConversationState state = ConversationState.getCurrent();
    User user = (User) state.getAttribute(CacheUserProfileFilter.USER_PROFILE);
    
//    Profile profile = getProfile();
//    String firstName = (String) profile.getProperty(FIRST_NAME);
//    firstName = (firstName == null ? "": firstName);
//    String lastName = (String) profile.getProperty(LAST_NAME);
//    lastName = (lastName == null ? "": lastName);
//    String gender = (String) profile.getProperty(GENDER);
//    gender = (gender == null ? "": gender);
//    UIFormStringInput uiFirstName = getChildById(FIRST_NAME);
//    UIFormStringInput uiLastName = getChildById(LAST_NAME);
//    UIFormStringInput uiEmail = getChildById("Email");
//    UIFormSelectBox uiGender = getChildById(GENDER);
//    uiFirstName.setValue(user.getFirstName());
//    uiLastName.setValue(user.getLastName());
//    uiEmail.setValue(user.getEmail());
//    uiGender.setValue(gender);    
  }
  
//  /**
//   * Store profile information into database.
//   * 
//   * @param uiBasicInfoSection
//   * @throws Exception
//   */
//  private static void saveProfile(UIBasicInfoSection uiBasicInfoSection) throws Exception {
//    UIFormStringInput uiFirstName = uiBasicInfoSection.getChildById(FIRST_NAME);
//    UIFormStringInput uiLastName = uiBasicInfoSection.getChildById(LAST_NAME);
//    UIFormSelectBox uiGender = uiBasicInfoSection.getChildById(GENDER);
//    String firstName = uiFirstName.getValue();
//    String lastName = uiLastName.getValue();
//    String gender = uiGender.getValue();
//    gender = ("Gender".equals(gender) ? "" : gender);
//    
//    ExoContainer container = ExoContainerContext.getCurrentContainer();
//    IdentityManager im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
//    Profile p = uiBasicInfoSection.getProfile();
//    p.setProperty(FIRST_NAME, firstName);
//    p.setProperty(LAST_NAME, lastName);
//    p.setProperty(GENDER, gender);
//    
//    im.saveProfile(p);    
//  }
}
