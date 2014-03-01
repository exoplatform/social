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
package org.exoplatform.social.webui.space;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormRadioBoxInput;


@ComponentConfig(
  template = "war:/groovy/social/webui/space/UIVisibilityFormInputSet.gtmpl"
)
public class UISpaceVisibility extends UIFormInputSet {
  public static final String UI_SPACE_VISIBILITY   = "UIVisibility";
  public static final String UI_SPACE_REGISTRATION = "UIRegistration";
  private static final String VISIBILITY_BINDING    = "visibility";
  private static final String REGISTRATION_BINDING  = "registration";
  private static final String VISIBLE_VALIDATION_SPACE = "UISpaceVisibility.label.VisibleAndValidationSpace";

  /**
   * Constructor
   * @param name
   * @throws Exception
   */
  public UISpaceVisibility(String name) throws Exception {
    super(name);
    setComponentConfig(getClass(), null);
//    List<SelectItemOption<String>> spaceVisibility = new ArrayList<SelectItemOption<String>>(3);
//    SelectItemOption<String> publicOption = new SelectItemOption<String>(Space.PUBLIC);
//    spaceVisibility.add(publicOption);
    List<SelectItemOption<String>> spaceVisibility = new ArrayList<SelectItemOption<String>>(2);
    SelectItemOption<String> privateOption = new SelectItemOption<String>(Space.PRIVATE);
    spaceVisibility.add(privateOption);

    SelectItemOption<String> hiddenOption = new SelectItemOption<String>(Space.HIDDEN);
    spaceVisibility.add(hiddenOption);

    UIFormRadioBoxInput uiRadioVisibility = new UIFormRadioBoxInput(UI_SPACE_VISIBILITY,
                                                                    VISIBILITY_BINDING,
                                                                    spaceVisibility);
    uiRadioVisibility.setValue(Space.PRIVATE);
    addUIFormInput(uiRadioVisibility);

    List<SelectItemOption<String>> spaceRegistration = new ArrayList<SelectItemOption<String>>(3);

    SelectItemOption<String> openOption = new SelectItemOption<String>(Space.OPEN);
    spaceRegistration.add(openOption);

    SelectItemOption<String> validationOption = new SelectItemOption<String>(Space.VALIDATION);
    validationOption.setSelected(true);
    spaceRegistration.add(validationOption);

    SelectItemOption<String> closeOption = new SelectItemOption<String>(Space.CLOSE);
    spaceRegistration.add(closeOption);

    UIFormRadioBoxInput uiRadioRegistration = new UIFormRadioBoxInput(UI_SPACE_REGISTRATION,
                                                                      REGISTRATION_BINDING,
                                                                      spaceRegistration);
    uiRadioRegistration.setValue(Space.VALIDATION);
    addUIFormInput(uiRadioRegistration);

    UIFormInputInfo visibilityInfo = new UIFormInputInfo("Visibility", null, null);
    WebuiRequestContext webReqCtx = WebuiRequestContext.getCurrentInstance();
    ResourceBundle resApp = webReqCtx.getApplicationResourceBundle();
    String visibleAndOpen = resApp.getString(VISIBLE_VALIDATION_SPACE);
    visibilityInfo.setValue(visibleAndOpen);
    addUIFormInput(visibilityInfo);
  }
}
