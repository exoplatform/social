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

import java.util.ResourceBundle;

import org.exoplatform.social.application.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.organization.account.UIGroupSelector;

/**
 * UIAddSpaceForm to create new space. By using this UIForm, user can create a
 * brand new space or a space from an existing group
 * 
 * @author hoatle hoatlevan@gmail.com
 * @since Jun 29, 2009
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "system:/groovy/webui/form/UIFormTabPane.gtmpl",
  events = {
    @EventConfig(listeners = UISpaceAddForm.CreateActionListener.class),
    @EventConfig(listeners = UISpaceAddForm.ToggleUseGroupActionListener.class, phase = Phase.DECODE)
  }
)

public class UISpaceAddForm extends UIFormTabPane {

  // Message for UIApplication
  static private final String MSG_DEFAULT_SPACE_DESCRIPTION       = "UISpaceAddForm.msg.default-space-description";
  
  static private final String MSG_ERROR_SPACE_CREATION            = "UISpaceAddForm.msg.error-space-creation";

  static private final String MSG_ERROR_DATASTORE                 = "UISpaceAddForm.msg.error-space-not-saved";

  static private final String MSG_ERROR_UNABLE_TO_INIT_APP        = "UISpaceAddForm.msg.error-unable-to-init-app";

  static private final String MSG_ERROR_UNABLE_TO_ADD_CREATOR     = "UISpaceAddForm.msg.error-unable-to-add-creator";

  static private final String MSG_ERROR_UNABLE_TO_ADD_APPLICATION = "UISpaceAddForm.msg.error-unable-to-add-application";

  static private final String MSG_SPACE_CREATION_SUCCESS          = "UISpaceAddForm.msg.space-creation-success";

  static private final String MSG_ERROR_SPACE_ALREADY_EXIST       = "UISpaceAddForm.msg.error-space-already-exist";

  private final String        SPACE_SETTINGS                      = "Settings";

  private final String        SPACE_VISIBILITY                    = "Visibility";

  private final String        SPACE_GROUP_BOUND                   = "GroupBound";

  /**
   * Constructor: add 3 UI component to this UIFormTabPane:
   * 
   * <pre>
   * {@link UISpaceSettings}
   * {@link UISpaceVisibility}
   * {@link UISpaceGroupBound}
   * </pre>
   * 
   * @throws Exception
   */
  public UISpaceAddForm() throws Exception {
    super("UISpaceAddForm");
    UIFormInputSet uiSpaceSettings = new UISpaceSettings(SPACE_SETTINGS);
    addChild(uiSpaceSettings);

    UIFormInputSet uiSpaceVisibility = new UISpaceVisibility(SPACE_VISIBILITY);
    addChild(uiSpaceVisibility);

    addChild(UISpaceGroupBound.class, null, SPACE_GROUP_BOUND);

    setActions(new String[] { "Create" });
    setSelectedTab(1);
  }

  /**
   * listener for create space action
   */
  static public class CreateActionListener extends EventListener<UISpaceAddForm> {
    @SuppressWarnings("unchecked")
    @Override
    public void execute(Event<UISpaceAddForm> event) throws Exception {
      UISpaceAddForm uiAddForm = event.getSource();
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApplication = ctx.getUIApplication();
      SpaceService spaceService = uiAddForm.getApplicationComponent(SpaceService.class);
      UISpaceGroupBound uiGroupBound = uiAddForm.getChild(UISpaceGroupBound.class);
      UIFormCheckBoxInput<Boolean> uiUseExisting = uiGroupBound.getChild(UIFormCheckBoxInput.class);
      String creator = ctx.getRemoteUser();
      ResourceBundle resApp = ctx.getApplicationResourceBundle();
      Space space = new Space();
      uiAddForm.invokeSetBindingBean(space);
      if (space.getDescription() == null) {
        space.setDescription(resApp.getString(MSG_DEFAULT_SPACE_DESCRIPTION));
      }
      String msg = "";
      try {
        if (uiUseExisting.isChecked()) {// create space from an existing group
          UIFormInputInfo uiSelectedGroup = uiGroupBound.getChild(UIFormInputInfo.class);
          space = spaceService.createSpace(space, creator, uiSelectedGroup.getValue());
        } else { // Create new space
          space = spaceService.createSpace(space, creator);
        }
        space.setType(DefaultSpaceApplicationHandler.NAME);
        spaceService.saveSpace(space, true);
        spaceService.initApp(space);
        // Install some more applications
        spaceService.installApplication(space, "UserListPortlet");
        spaceService.activateApplication(space, "UserListPortlet");

        spaceService.installApplication(space, "SpaceSettingPortlet");
        spaceService.activateApplication(space, "SpaceSettingPortlet");
        
      } catch (SpaceException se) {
        if (se.getCode() == SpaceException.Code.SPACE_ALREADY_EXIST) {
          msg = MSG_ERROR_SPACE_ALREADY_EXIST;
        } else if (se.getCode() == SpaceException.Code.UNABLE_TO_ADD_CREATOR) {
          msg = MSG_ERROR_UNABLE_TO_ADD_CREATOR;
        } else if (se.getCode() == SpaceException.Code.ERROR_DATASTORE) {
          msg = MSG_ERROR_DATASTORE;
        } else if (se.getCode() == SpaceException.Code.UNABLE_TO_INIT_APP) {
          msg = MSG_ERROR_UNABLE_TO_INIT_APP;
        } else if (se.getCode() == SpaceException.Code.UNABLE_TO_ADD_APPLICATION) {
          msg = MSG_ERROR_UNABLE_TO_ADD_APPLICATION;
        } else {
          msg = MSG_ERROR_SPACE_CREATION;
        }
        uiApplication.addMessage(new ApplicationMessage(msg, null, ApplicationMessage.ERROR));
        return;
      }
      msg = UISpaceAddForm.MSG_SPACE_CREATION_SUCCESS;
      uiApplication.addMessage(new ApplicationMessage(msg, null, ApplicationMessage.INFO));
      UIPopupWindow uiPopup = uiAddForm.getParent();
      uiPopup.setShow(false);
      UIManageMySpaces uiManageMySpaces = uiPopup.getParent();
      ctx.addUIComponentToUpdateByAjax(uiManageMySpaces);
    }
  }

  /**
   * listener for toggle use existing group action When this action is
   * triggered, a group selector poup will show up for choosing.
   */
  static public class ToggleUseGroupActionListener extends EventListener<UISpaceAddForm> {
    @SuppressWarnings("unchecked")
    @Override
    public void execute(Event<UISpaceAddForm> event) throws Exception {
      UISpaceAddForm uiSpaceAddForm = event.getSource();
      UISpaceGroupBound uiSpaceGroupBound = uiSpaceAddForm.getChild(UISpaceGroupBound.class);
      UIFormCheckBoxInput<Boolean> uiUseExistingGroup = uiSpaceGroupBound.getChild(UIFormCheckBoxInput.class);
      if (uiUseExistingGroup.isChecked()) {
        UIPopupWindow uiPopup = uiSpaceGroupBound.getChild(UIPopupWindow.class);
        UIGroupSelector uiGroupSelector = uiSpaceAddForm.createUIComponent(UIGroupSelector.class,
                                                                           null,
                                                                           null);
        uiPopup.setUIComponent(uiGroupSelector);
        uiPopup.setShowMask(true);
        uiPopup.setShow(true);
      } else {
        UIFormInputInfo uiFormInputInfo = uiSpaceGroupBound.getChild(UIFormInputInfo.class);
        uiFormInputInfo.setValue(null);
      }
    }

  }

}
