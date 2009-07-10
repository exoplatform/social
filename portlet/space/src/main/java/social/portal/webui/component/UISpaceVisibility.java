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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.social.space.Space;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormRadioBoxInput;

/**
 * This class is used to build UI for setting space's visibility
 * Setting variables:
 *        - Visibility: Private or Hidden
 *        - Registration: Open, Validation or Close 
 * Created by The eXo Platform SAS
 * Author : hoatle
 *          hoatlevan@gmail.com
 *          hoat.le@exoplatform.com
 * Jul 1, 2009  
 */

public class UISpaceVisibility extends UIFormInputSet {
  private final String SPACE_VISIBILITY = "visibility";
  private final String SPACE_REGISTRATION = "registration";
  
  public UISpaceVisibility(String name) throws Exception {
    super(name);
    List<SelectItemOption<String>> spaceVisibility = new ArrayList<SelectItemOption<String>>(2);
    
    SelectItemOption<String> privateOption = new SelectItemOption<String>(Space.PRIVATE);
    privateOption.setSelected(true);
    spaceVisibility.add(privateOption);
    
    SelectItemOption<String> hiddenOption = new SelectItemOption<String>(Space.HIDDEN);
    spaceVisibility.add(hiddenOption);
    
    UIFormRadioBoxInput uiRadioVisibility = new UIFormRadioBoxInput(SPACE_VISIBILITY, SPACE_VISIBILITY, spaceVisibility);
    addUIFormInput(uiRadioVisibility);
    
    List<SelectItemOption<String>> spaceRegistration = new ArrayList<SelectItemOption<String>>(3);
    
    SelectItemOption<String> openOption = new SelectItemOption<String>(Space.OPEN);
    openOption.setSelected(false);
    spaceRegistration.add(openOption);
    
    SelectItemOption<String> validationOption = new SelectItemOption<String>(Space.VALIDATION);
    validationOption.setSelected(true);
    spaceRegistration.add(validationOption);
    
    SelectItemOption<String> closeOption = new SelectItemOption<String>(Space.CLOSE);
    spaceRegistration.add(closeOption);
    
    UIFormRadioBoxInput uiRadioRegistration = new UIFormRadioBoxInput(SPACE_REGISTRATION, SPACE_VISIBILITY, spaceRegistration);
    addUIFormInput(uiRadioRegistration);
  }
}
