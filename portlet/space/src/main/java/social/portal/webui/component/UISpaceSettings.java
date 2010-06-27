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

import org.exoplatform.social.space.Space;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.*;
import org.exoplatform.webui.form.validator.ExpressionValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * This UI is used in UIAddSpaceForm. <br />
 * Setting variables: <br />
 * - Name <br />
 * - Priority <br />
 * - Description <br />
 * <p/>
 * Created by The eXo Platform SAS
 *
 * @author <a href="mailto:hoatlevan@gmail.com">hoatle</a>
 * @since Jul 1, 2009
 */

public class UISpaceSettings extends UIFormInputSet {
  private final String SPACE_NAME             = "name";

  private final String SPACE_PRIORITY         = "priority";

  private final String SPACE_DESCRIPTION      = "description";

  // These priority variables should be set in Space.java model
  private final String PRIORITY_HIGH          = "high";

  private final String PRIORITY_INTERMEDIATE  = "intermediate";

  private final String PRIORITY_LOW           = "low";

  // Message
  private final String MSG_INVALID_SPACE_NAME = "UISpaceSettings.msg.invalid_space_name";

  /**
   * constructor
   *
   * @param name
   * @throws Exception
   */
  public UISpaceSettings(String name) throws Exception {
    super(name);
    addUIFormInput(new UIFormStringInput(SPACE_NAME, SPACE_NAME, null).
                   addValidator(MandatoryValidator.class).
                   //addValidator(ExpressionValidator.class, "^[\\p{L}\\s\\d]+$", "ResourceValidator.msg.Invalid-char").
                   addValidator(ExpressionValidator.class, "^([\\p{L}\\d]+[\\s]?)+$", MSG_INVALID_SPACE_NAME).
                   addValidator(StringLengthValidator.class, 3, 30));

    List<SelectItemOption<String>> priorityList = new ArrayList<SelectItemOption<String>>(3);
    SelectItemOption<String> pHight = new SelectItemOption<String>(PRIORITY_HIGH,
                                                                   Space.HIGH_PRIORITY);
    SelectItemOption<String> pImmediate = new SelectItemOption<String>(PRIORITY_INTERMEDIATE,
                                                                       Space.INTERMEDIATE_PRIORITY);
    SelectItemOption<String> pLow = new SelectItemOption<String>(PRIORITY_LOW, Space.LOW_PRIORITY);
    priorityList.add(pHight);
    priorityList.add(pImmediate);
    priorityList.add(pLow);
    pImmediate.setSelected(true);
    UIFormSelectBox selectPriority = new UIFormSelectBox(SPACE_PRIORITY,
                                                         SPACE_PRIORITY,
                                                         priorityList);
    addUIFormInput(selectPriority);
    UIFormInputInfo priorityInfo = new UIFormInputInfo("Priority", null, null);
    WebuiRequestContext webReqCtx = WebuiRequestContext.getCurrentInstance();
    ResourceBundle resApp = webReqCtx.getApplicationResourceBundle();
    String interMePrio = resApp.getString("UISpaceSettings.label.InterMePrio");
    priorityInfo.setValue(interMePrio);
    addUIFormInput(priorityInfo);
    addUIFormInput(new UIFormTextAreaInput(SPACE_DESCRIPTION, SPACE_DESCRIPTION, null).
                   addValidator(StringLengthValidator.class, 0,255));
  }
}
