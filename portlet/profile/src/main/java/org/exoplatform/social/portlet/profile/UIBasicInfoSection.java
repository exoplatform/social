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
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Profile;
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
        @EventConfig(listeners = UIProfileSection.EditActionListener.class, phase=Phase.DECODE),
        @EventConfig(listeners = UIBasicInfoSection.SaveActionListener.class),
        @EventConfig(listeners = UIProfileSection.CancelActionListener.class)
    }
)

public class UIBasicInfoSection extends UIProfileSection {

  public UIBasicInfoSection() throws Exception {
    addChild(UITitleBar.class, null, null);
    
    addUIFormInput(new UIFormStringInput("firstName", "firstname", null)
                   .addValidator(MandatoryValidator.class)
                   .addValidator(StringLengthValidator.class, 3, 30)
                   .addValidator(ExpressionValidator.class, "^\\p{L}[\\p{L}\\d._,\\s]+\\p{L}$", "UIBasicInfoSection.msg.Invalid-char"));
    addUIFormInput(new UIFormStringInput("lastName", "lastname", null)
                   .addValidator(MandatoryValidator.class)
                   .addValidator(StringLengthValidator.class, 3, 30)
                   .addValidator(ExpressionValidator.class, "^\\p{L}[\\p{L}\\d._,\\s]+\\p{L}$", "UIBasicInfoSection.msg.Invalid-char"));

    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    options.add(new SelectItemOption<String>("male")) ;
    options.add(new SelectItemOption<String>("female")) ;
    addUIFormInput(new UIFormSelectBox("gender", "gender", options));
  }
  
  /**
   * Store profile information into database when form is submitted.
   *
   */
  public static class SaveActionListener extends UIProfileSection.SaveActionListener {

    public void execute(Event<UIProfileSection> event) throws Exception {
      super.execute(event);
      
      UIProfileSection sect = event.getSource();    
      
      saveProfile((UIBasicInfoSection)sect);      
      
      //Get the UIHeaderSection to refresh it since it contains also the firstname and lastname
      UIComponent parent = sect.getParent();
      UIProfileSection header = parent.findFirstComponentOfType(UIHeaderSection.class);
      if(header != null) {
        event.getRequestContext().addUIComponentToUpdateByAjax(header);
      }
    }       
  }  
  
  /**
   * Get information from profile and set value into uicomponent.
   * 
   * @throws Exception
   */
  public void setValue() throws Exception {
    Profile profile = getProfile();
    String firstName = (String) profile.getProperty("firstName");
    firstName = (firstName == null ? "": firstName);
    String lastName = (String) profile.getProperty("lastName");
    lastName = (lastName == null ? "": lastName);
    String gender = (String) profile.getProperty("gender");
    UIFormStringInput uiFirstName = getChildById("firstName");
    UIFormStringInput uiLastName = getChildById("lastName");
    UIFormSelectBox uiGender = getChildById("gender");
    uiFirstName.setValue(firstName);
    uiLastName.setValue(lastName);
    uiGender.setValue(gender);    
  }
  
  /**
   * Store profile information into database.
   * 
   * @param uiBasicInfoSection
   * @throws Exception
   */
  private static void saveProfile(UIBasicInfoSection uiBasicInfoSection) throws Exception {
    UIFormStringInput uiFirstName = uiBasicInfoSection.getChildById("firstName");
    UIFormStringInput uiLastName = uiBasicInfoSection.getChildById("lastName");
    UIFormSelectBox uiGender = uiBasicInfoSection.getChildById("gender");
    String firstName = uiFirstName.getValue();
    String lastName = uiLastName.getValue();
    String gender = uiGender.getValue();
    
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    IdentityManager im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
    Profile p = uiBasicInfoSection.getProfile();
    p.setProperty("firstName", firstName);
    p.setProperty("lastName", lastName);
    p.setProperty("gender", gender);
    
    im.saveProfile(p);    
  }
}
