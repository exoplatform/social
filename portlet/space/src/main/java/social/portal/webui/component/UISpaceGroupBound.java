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

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
/**
 * This UI component is used for setting space's bound to a group
 * 
 * If not set, a new group is created.
 * A popup window will be displayed for user to choose from existing group
 *
 * Created by The eXo Platform SAS
 * Author : hoatle
 *          hoatlevan@gmail.com
 *          hoat.le@exoplatform.com
 * Jul 1, 2009  
 */
@ComponentConfig(
  template = "app:/groovy/portal/webui/component/UISpaceGroupBound.gtmpl"
)
public class UISpaceGroupBound extends UIContainer {
  private final String USE_EXISTING_GROUP = "useExistingGroup";
  private final String POPUP_GROUP_BOUND = "UIPopupGroupBound";
  private final String SELECTED_GROUP = "groupId";
  public UISpaceGroupBound() throws Exception {
    UIFormCheckBoxInput<Boolean> useExisting = new UIFormCheckBoxInput<Boolean>(USE_EXISTING_GROUP, null, false);
    useExisting.setOnChange("ToogleUseGroup");
    addChild(useExisting);
    
    UIFormInputInfo uiFormInputInfo = new UIFormInputInfo(SELECTED_GROUP, SELECTED_GROUP, null);
    addChild(uiFormInputInfo);
    
    UIPopupWindow uiPopup = createUIComponent(UIPopupWindow.class, null, POPUP_GROUP_BOUND);
    uiPopup.setWindowSize(500, 0);
    addChild(uiPopup);
  }
 
}
