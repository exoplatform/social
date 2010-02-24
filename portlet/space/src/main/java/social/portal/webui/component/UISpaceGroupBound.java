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

import org.exoplatform.services.organization.Group;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.organization.account.UIGroupSelector;
/**
 * This UI component is used for setting space's bound to a group <br />
 * 
 * If not set, a new group is created. <br />
 * A popup window will be displayed for user to choose from existing group <br />
 *
 * Created by The eXo Platform SAS
 * @author  <a href="mailto:hoatlevan@gmail.com">hoatle</a>
 * @since   Jul 1, 2009  
 */
@ComponentConfigs({
  @ComponentConfig(
    template = "app:/groovy/portal/webui/component/UISpaceGroupBound.gtmpl",
    events = {@EventConfig(listeners = UISpaceGroupBound.SelectGroupActionListener.class, phase=Phase.DECODE) }
  ),
  @ComponentConfig(
    type = UIPopupWindow.class,
    id = "SelectGroup",
    template =  "system:/groovy/webui/core/UIPopupWindow.gtmpl",
    events = @EventConfig(listeners = UISpaceGroupBound.ClosePopupActionListener.class, name = "ClosePopup")
 )
})

public class UISpaceGroupBound extends UIContainer {
  private final String USE_EXISTING_GROUP = "useExistingGroup";
  private final String POPUP_GROUP_BOUND = "UIPopupGroupBound";
  private final String SELECTED_GROUP = "groupId";
  /**
   * constructor
   * @throws Exception
   */
  public UISpaceGroupBound() throws Exception {
    UIFormCheckBoxInput<Boolean> uiUseExisting = new UIFormCheckBoxInput<Boolean>(USE_EXISTING_GROUP, null, false);
    uiUseExisting.setOnChange("ToggleUseGroup");
    addChild(uiUseExisting);
    UIFormInputInfo uiFormInputInfo = new UIFormInputInfo(SELECTED_GROUP, null, null);
    addChild(uiFormInputInfo);
    UIPopupWindow uiPopup = createUIComponent(UIPopupWindow.class, "SelectGroup", POPUP_GROUP_BOUND);
    uiPopup.setWindowSize(500, 0);
    addChild(uiPopup);
  }
  
  /**
   * gets selected group from group bound
   * @return selected group
   */
  @SuppressWarnings("unchecked")
  public String getSelectedGroup() {
    UIFormCheckBoxInput<Boolean> uiCheckBox = getChild(UIFormCheckBoxInput.class);
    if(uiCheckBox.isChecked()) {
      UIFormInputInfo uiInfo = getChild(UIFormInputInfo.class);
      return uiInfo.getValue();
    }
    return null;
  }
  
  /**
   * triggers this action when user clicks on select group on UIGroupSelector
   */
  static public class SelectGroupActionListener extends EventListener<UIGroupSelector> {
    public void execute(Event<UIGroupSelector> event) throws Exception {
      WebuiRequestContext context = event.getRequestContext();
      String groupId = context.getRequestParameter(OBJECTID);
      UIGroupSelector uiGroupSelector = event.getSource();
      UISpaceGroupBound uiGroupBound = uiGroupSelector.getAncestorOfType(UISpaceGroupBound.class);
      UIFormInputInfo uiFormInputInfo = uiGroupBound.getChild(UIFormInputInfo.class);
      uiFormInputInfo.setValue(groupId);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiGroupBound);
    }
  }
  
  /**
   * Check if user selected a group or  not when closing the popup,
   * if not un-check the checked check box
   */
  static public class ClosePopupActionListener extends EventListener<UIPopupWindow> {
    @SuppressWarnings("unchecked")
    @Override
    public void execute(Event<UIPopupWindow> event) throws Exception {
      UIPopupWindow uiPopup = event.getSource();
      UISpaceGroupBound uiGroupBound = uiPopup.getAncestorOfType(UISpaceGroupBound.class);
      UIGroupSelector uiGroupSelector = (UIGroupSelector)uiPopup.getUIComponent();
      Group group = uiGroupSelector.getCurrentGroup();
      if (group == null) {
        UIFormCheckBoxInput<Boolean> uiUseExisting = uiGroupBound.getChild(UIFormCheckBoxInput.class);
        uiUseExisting.setChecked(false);
      } else {
        UIFormInputInfo uiSelected = uiGroupBound.getChild(UIFormInputInfo.class);
        uiSelected.setValue(group.getId());
      }
      uiPopup.setShow(false);
    }
  }
}
