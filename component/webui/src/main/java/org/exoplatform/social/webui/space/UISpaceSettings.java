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

import java.util.ResourceBundle;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.ExpressionValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;

public class UISpaceSettings extends UIFormInputSet {
  private final String SPACE_DISPLAY_NAME             = "displayName";

  private final String SPACE_DESCRIPTION      = "description";

  // Message
  private final String MSG_INVALID_SPACE_NAME = "UISpaceSettings.msg.invalid_space_name";

  /** Html attribute title. */
  private static final String HTML_ATTRIBUTE_TITLE   = "title";
  
  /**
   * constructor
   *
   * @param name
   * @throws Exception
   */
  public UISpaceSettings(String name) throws Exception {
    super(name);
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    ResourceBundle resourceBundle = requestContext.getApplicationResourceBundle();
    UIFormStringInput spaceDisplayName = new UIFormStringInput(SPACE_DISPLAY_NAME, SPACE_DISPLAY_NAME, null);
    spaceDisplayName.setHTMLAttribute(HTML_ATTRIBUTE_TITLE, resourceBundle.getString("UISpaceSettings.label.spaceDisplayName"));
    addUIFormInput(spaceDisplayName.
                   addValidator(MandatoryValidator.class).
                   //addValidator(ExpressionValidator.class, "^[\\p{L}\\s\\d]+$", "ResourceValidator.msg.Invalid-char").
                   addValidator(ExpressionValidator.class, "^([\\p{L}\\s\\d]+[\\s]?)+$", MSG_INVALID_SPACE_NAME).
                   addValidator(StringLengthValidator.class, 3, 30));

    UIFormTextAreaInput description = new UIFormTextAreaInput(SPACE_DESCRIPTION, SPACE_DESCRIPTION, null);
    description.setHTMLAttribute(HTML_ATTRIBUTE_TITLE, resourceBundle.getString("UISpaceSettings.label.spaceDescription"));
    addUIFormInput(description.addValidator(StringLengthValidator.class, 0,255));
  }
}
