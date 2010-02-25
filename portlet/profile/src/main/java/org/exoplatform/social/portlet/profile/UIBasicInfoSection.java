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

import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.webui.URLUtils;
import org.exoplatform.web.CacheUserProfileFilter;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormStringInput;
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
   * Get information from profile and set value into components.
   * 
   * @throws Exception
   */
  public void setValue() throws Exception {
    ConversationState state = ConversationState.getCurrent();
    User user = (User) state.getAttribute(CacheUserProfileFilter.USER_PROFILE);
  }
}
