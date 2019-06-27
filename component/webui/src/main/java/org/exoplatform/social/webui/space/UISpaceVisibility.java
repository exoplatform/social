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
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormRadioBoxInput;


@ComponentConfig(
    template = "war:/groovy/social/webui/space/UIVisibilityFormInputSet.gtmpl",
    events = {
        @EventConfig(listeners = UISpaceVisibility.ChangeVisibilityActionListener.class)
    }
)
public class UISpaceVisibility extends UIFormInputSet {
  public static final String UI_SPACE_REGISTRATION = "UIRegistration";
  private static final String REGISTRATION_BINDING  = "registration";
  private static final String VISIBLE_SPACE  = "UISpaceVisibility.label.VisibleSpace";
  private static final String HIDDEN_SPACE  = "UISpaceVisibility.label.HiddenSpace";
  private static final String OPEN_SPACE  = "UISpaceVisibility.label.OpenSpace";
  private static final String VALIDATION_SPACE  = "UISpaceVisibility.label.ValidationSpace";
  private static final String CLOSE_SPACE  = "UISpaceVisibility.label.CloseSpace";
  private String visibility;

  /**
   * Constructor
   * @param name
   * @throws Exception
   */
  public UISpaceVisibility(String name) throws Exception {
    super(name);
    setComponentConfig(getClass(), null);
    setVisibility(Space.PRIVATE);

    List<SelectItemOption<String>> spaceRegistration = new ArrayList<SelectItemOption<String>>(3);

    SelectItemOption<String> openOption = new SelectItemOption<String>(Space.OPEN);
    openOption.setSelected(true);
    spaceRegistration.add(openOption);

    SelectItemOption<String> validationOption = new SelectItemOption<String>(Space.VALIDATION);
    spaceRegistration.add(validationOption);

    SelectItemOption<String> closeOption = new SelectItemOption<String>(Space.CLOSE);
    spaceRegistration.add(closeOption);

    UIFormRadioBoxInput uiRadioRegistration = new UIFormRadioBoxInput(UI_SPACE_REGISTRATION,
                                                                      REGISTRATION_BINDING,
                                                                      spaceRegistration);
    uiRadioRegistration.setValue(Space.OPEN);
    addUIFormInput(uiRadioRegistration);
    UIFormInputInfo visibilityInfo = new UIFormInputInfo("Visibility", null, null);
    WebuiRequestContext webReqCtx = WebuiRequestContext.getCurrentInstance();
    ResourceBundle resApp = webReqCtx.getApplicationResourceBundle();
    String visible = resApp.getString(VISIBLE_SPACE);
    visibilityInfo.setValue(visible);
    addUIFormInput(visibilityInfo);
    UIFormInputInfo registrationInfo = new UIFormInputInfo("Registration", null, null);
    String validation = resApp.getString(VALIDATION_SPACE);
    registrationInfo.setValue(validation);
    addUIFormInput(registrationInfo);
  }

  public void setVisibility(String visibility) {
    this.visibility = visibility;
  }

  public String getVisibility() {
    return visibility;
  }

  public void setVisibilityInfo(ResourceBundle resourceBundle, String visibility) {
    UIFormInputInfo uiFormInputInfo = getUIFormInputInfo("Visibility");
    String visibilityInfo = resourceBundle.getString(VISIBLE_SPACE);
    if (Space.HIDDEN.equals(visibility)) {
      visibilityInfo = resourceBundle.getString(HIDDEN_SPACE);
    }
    uiFormInputInfo.setValue(visibilityInfo);
  }

  public void setRegistrationInfo(ResourceBundle resourceBundle, String registration) {
    UIFormInputInfo uiFormInputInfo = getUIFormInputInfo("Registration");
    String registrationInfo = resourceBundle.getString(VALIDATION_SPACE);
    if (Space.OPEN.equals(registration)) {
      registrationInfo = resourceBundle.getString(OPEN_SPACE);
    } else if (Space.CLOSE.equals(registration)) {
      registrationInfo = resourceBundle.getString(CLOSE_SPACE);
    }
    uiFormInputInfo.setValue(registrationInfo);
  }

  static public class ChangeVisibilityActionListener extends EventListener<UISpaceVisibility> {
    public void execute(Event<UISpaceVisibility> event) throws Exception {
      UISpaceVisibility uiSpaceVisibility = event.getSource();
      String oldVisibility = uiSpaceVisibility.getVisibility();
      String visibility = Space.HIDDEN.equals(oldVisibility) ? Space.PRIVATE : Space.HIDDEN;
      uiSpaceVisibility.setVisibility(visibility);
      WebuiRequestContext ctx = event.getRequestContext();
      ResourceBundle resApp = ctx.getApplicationResourceBundle();
      uiSpaceVisibility.setVisibilityInfo(resApp, visibility);
      ctx.addUIComponentToUpdateByAjax(uiSpaceVisibility);
    }
  }
}
