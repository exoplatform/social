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

import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * This UI is used in UIAddSpaceForm.
 * Setting variables:
 *        - Name
 *        - Priority
 *        - Description
 *        
 * Created by The eXo Platform SAS
 * Author : hoatle
 *          hoatlevan@gmail.com
 * Jul 1, 2009  
 */

public class UISpaceSettings extends UIFormInputSet {
  private final String SPACE_NAME = "name";
  private final String SPACE_PRIORITY = "priority";
  private final String SPACE_DESCRIPTION = "description";
  
  //These priority variables should be set in Space.java model
  private final String PRIORITY_HIGH = "hight";
  private final String PRIORITY_MIDDLE = "middle";
  private final String PRIORITY_LOW = "low";
  
  public UISpaceSettings(String name) throws Exception {
    super(name);
    addUIFormInput(new UIFormStringInput(SPACE_NAME, SPACE_NAME, null));
    
    // TODO: Add Priority Options HIGH (1) - MIDDLE (2) - LOW (3)
    List<SelectItemOption<String>> priorityList = new ArrayList<SelectItemOption<String>>(3);
    SelectItemOption<String> pHight = new SelectItemOption<String>(PRIORITY_HIGH, "1");
    SelectItemOption<String> pMiddle = new SelectItemOption<String>(PRIORITY_MIDDLE, "2");
    SelectItemOption<String> pLow = new SelectItemOption<String>(PRIORITY_LOW, "3");
    priorityList.add(pHight);
    priorityList.add(pMiddle);
    priorityList.add(pLow);
    pMiddle.setSelected(true);
        
    UIFormSelectBox selectPriority = new UIFormSelectBox(SPACE_PRIORITY, SPACE_PRIORITY, priorityList);
    addUIFormInput(selectPriority);
    // Add Description
    addUIFormInput(new UIFormTextAreaInput(SPACE_DESCRIPTION, SPACE_DESCRIPTION, null));
  }
}
