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

import java.util.Iterator;
import java.util.ResourceBundle;

import org.exoplatform.services.organization.Group;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.webui.UISocialGroupSelector;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.input.UICheckBoxInput;

@ComponentConfigs({
  @ComponentConfig(
    template = "war:/groovy/social/webui/space/UISpaceGroupBound.gtmpl",
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
  private final String USE_EXISTING_GROUP = "UseExistingGroupCheckBox";
  private final String POPUP_GROUP_BOUND = "UIPopupGroupBound";
  private final String SELECTED_GROUP = "groupId";
  private final String ANY_MEMBERSHIP_TYPE = "*";

  /** Html attribute title. */
  private static final String HTML_ATTRIBUTE_TITLE   = "title";
  
  /**
   * constructor
   * @throws Exception
   */
  public UISpaceGroupBound() throws Exception {
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    ResourceBundle resourceBundle = requestContext.getApplicationResourceBundle();
    UICheckBoxInput uiUseExisting = new UICheckBoxInput(USE_EXISTING_GROUP, null, false);
    uiUseExisting.setHTMLAttribute(HTML_ATTRIBUTE_TITLE, resourceBundle.getString("UISpaceGroupBound.label.useExistingGroup"));
    uiUseExisting.setId(USE_EXISTING_GROUP);
    uiUseExisting.setOnChange("ToggleUseGroup");
    addChild(uiUseExisting);
    
    UIFormInputInfo uiFormInputInfo = new UIFormInputInfo(SELECTED_GROUP, null, null);
    uiFormInputInfo.setId(SELECTED_GROUP);
    addChild(uiFormInputInfo);
    UIPopupWindow uiPopup = createUIComponent(UIPopupWindow.class, "SelectGroup", POPUP_GROUP_BOUND);
    uiPopup.setWindowSize(550, 0);
    addChild(uiPopup);
  }

  /**
   * gets selected group from group bound
   * @return selected group
   */
  @SuppressWarnings("unchecked")
  public String getSelectedGroup() {
    UICheckBoxInput uiCheckBox = getChild(UICheckBoxInput.class);
    if(uiCheckBox.isChecked()) {
      UIFormInputInfo uiInfo = getChild(UIFormInputInfo.class);
      return uiInfo.getValue();
    }
    return null;
  }

  /**
   * Check current user is manager of group or not.
   * 
   * @return True if current user has one group that he is manager of that group.
   * @throws Exception
   */
  protected boolean hasGroupWithManagerRole() throws Exception {
    String adminMSType = SpaceUtils.getUserACL().getAdminMSType();

    Iterator<MembershipEntry> iter = ConversationState.getCurrent().getIdentity().getMemberships().iterator();
    while (iter.hasNext()) {
      String msType = iter.next().getMembershipType();
      // has any or super membership type
      if (ANY_MEMBERSHIP_TYPE.equals(msType) || adminMSType.equals(msType)) {
        return true;
      }
    }

    return false;
  }

  /**
   * triggers this action when user clicks on select group on UIGroupSelector
   */
  static public class SelectGroupActionListener extends EventListener<UISocialGroupSelector> {
    public void execute(Event<UISocialGroupSelector> event) throws Exception {
      WebuiRequestContext context = event.getRequestContext();
      String groupId = context.getRequestParameter(OBJECTID);
      UISocialGroupSelector uiGroupSelector = event.getSource();
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
      UISocialGroupSelector uiGroupSelector = (UISocialGroupSelector)uiPopup.getUIComponent();
      Group group = uiGroupSelector.getCurrentGroup();
      if (group == null) {
        UICheckBoxInput uiUseExisting = uiGroupBound.getChild(UICheckBoxInput.class);
        uiUseExisting.setChecked(false);
      } else {
        UIFormInputInfo uiSelected = uiGroupBound.getChild(UIFormInputInfo.class);
        uiSelected.setValue(group.getId());
      }
      uiPopup.setShow(false);
    }
  }
}
