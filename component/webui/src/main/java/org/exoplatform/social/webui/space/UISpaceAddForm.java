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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.UISocialGroupSelector;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.JavascriptManager;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.input.UICheckBoxInput;

/**
 * UIAddSpaceForm to create new space. By using this UIForm, user can create a
 * brand new space or a space from an existing group
 *
 * @author <a href="mailto:hoatlevan@gmail.com">hoatle</a>
 * @since Jun 29, 2009
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "system:/groovy/webui/form/UIFormTabPane.gtmpl",
  events = {
    @EventConfig(listeners = UIFormTabPane.SelectTabActionListener.class, phase = Phase.DECODE),
    @EventConfig(listeners = UISpaceAddForm.CreateActionListener.class),
    @EventConfig(listeners = UISpaceAddForm.ToggleUseGroupActionListener.class, phase = Phase.DECODE),
    @EventConfig(listeners = UISpaceAddForm.ChangeOptionActionListener.class, phase = Phase.DECODE)
  }
)

public class UISpaceAddForm extends UIFormTabPane {

  private static final Log LOG = ExoLogger.getLogger(UISpaceAddForm.class);
  
  static private final String MSG_DEFAULT_SPACE_DESCRIPTION = "UISpaceAddForm.msg.default_space_description";
  static private final String MSG_ERROR_SPACE_CREATION = "UISpaceAddForm.msg.error_space_creation";
  static private final String MSG_ERROR_DATASTORE = "UISpaceAddForm.msg.error_space_not_saved";
  static private final String MSG_ERROR_UNABLE_TO_INIT_APP = "UISpaceAddForm.msg.error_unable_to_init_app";
  static private final String MSG_ERROR_UNABLE_TO_ADD_CREATOR = "UISpaceAddForm.msg.error_unable_to_add_creator";
  static private final String MSG_ERROR_UNABLE_TO_ADD_APPLICATION = "UISpaceAddForm.msg.error_unable_to_add_application";
  static private final String MSG_ERROR_RETRIEVING_USER = "UISpaceAddForm.msg.error_unable_to_retrieve_user";
  static private final String MSG_SPACE_CREATION_SUCCESS = "UISpaceAddForm.msg.space_creation_success";
  static private final String MSG_ERROR_SPACE_ALREADY_EXIST = "UISpaceAddForm.msg.error_space_already_exist";
  private final String SPACE_SETTINGS = "UISpaceSettings";
  private final String SPACE_VISIBILITY = "UISpaceVisibility";
  
  /**
   * Constructor: add 3 UI component to this UIFormTabPane:
   * <p/>
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

    addChild(UISpaceGroupBound.class, null, null);

    setActions(new String[]{"Create"});
    setSelectedTab(1);
  }

  @Override
  public String getLabel(ResourceBundle res, String key) {
    return new StringBuffer(super.getLabel(res, key)).append((key.indexOf("action.") < 0) ? ":" : "").toString();
  }

  /**
   * listener for create space action
   */
  static public class CreateActionListener extends EventListener<UISpaceAddForm> {
    @Override
    public void execute(Event<UISpaceAddForm> event) throws Exception {
      UISpaceAddForm uiAddForm = event.getSource();
      WebuiRequestContext ctx = event.getRequestContext();
      UIApplication uiApplication = ctx.getUIApplication();
      SpaceService spaceService = uiAddForm.getApplicationComponent(SpaceService.class);
      UISpaceGroupBound uiGroupBound = uiAddForm.getChild(UISpaceGroupBound.class);
      String selectedGroup = uiGroupBound.getSelectedGroup();
      String creator = ctx.getRemoteUser();
      ResourceBundle resApp = ctx.getApplicationResourceBundle();
      String defaultDescription = resApp.getString(MSG_DEFAULT_SPACE_DESCRIPTION);
      Space space = new Space();
      uiAddForm.invokeSetBindingBean(space);
      space.setDisplayName(space.getDisplayName().trim());
      space.setPrettyName(space.getDisplayName());
      String spaceDescription = space.getDescription();
      if ((spaceDescription == null) || (spaceDescription.trim().length() == 0) 
                                     || spaceDescription.trim().equals(defaultDescription)) {
        space.setDescription(StringUtils.EMPTY);
      } else {
        space.setDescription(StringEscapeUtils.escapeHtml(spaceDescription));  
      }
      String msg = MSG_SPACE_CREATION_SUCCESS;
      try {
        // Checks user is still existing or not.
        SpaceUtils.checkUserExisting(ctx.getRemoteUser());
        
        //validate the display name
        if (spaceService.getSpaceByDisplayName(space.getDisplayName()) != null) {
          throw new SpaceException(SpaceException.Code.SPACE_ALREADY_EXIST);
        }
        
        //
        Space got;
        String prettyName = space.getPrettyName();
        int count = 1;
        while ((got = spaceService.getSpaceByPrettyName(prettyName)) != null) {
          //
          if (count == 1 && got.getDisplayName().equalsIgnoreCase(space.getDisplayName())) {
            throw new SpaceException(SpaceException.Code.SPACE_ALREADY_EXIST);
          }
          prettyName = space.getPrettyName() + "_" + count;
          //
          ++count;
        }
        space.setPrettyName(prettyName);
        
        ExoContainer container = ExoContainerContext.getCurrentContainer();
        IdentityManager idm = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
        Identity identity = idm.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), true);
        if (identity != null) {
          space.setPrettyName(SpaceUtils.buildPrettyName(space));
        }

        space.setType(DefaultSpaceApplicationHandler.NAME);
        if (selectedGroup != null) {// create space from an existing group
          space = spaceService.createSpace(space, creator, selectedGroup);
        } else { // Create new space
          space = spaceService.createSpace(space, creator);
        }
      } catch (SpaceException se) {
        if (se.getCode() == SpaceException.Code.SPACE_ALREADY_EXIST) {
          msg = MSG_ERROR_SPACE_ALREADY_EXIST;
          uiApplication.addMessage(new ApplicationMessage(msg, null, ApplicationMessage.WARNING));
          return;
        } else if (se.getCode() == SpaceException.Code.UNABLE_TO_ADD_CREATOR) {
          msg = MSG_ERROR_UNABLE_TO_ADD_CREATOR;
        } else if (se.getCode() == SpaceException.Code.ERROR_DATASTORE) {
          msg = MSG_ERROR_DATASTORE;
        } else if (se.getCode() == SpaceException.Code.UNABLE_TO_INIT_APP) {
          msg = MSG_ERROR_UNABLE_TO_INIT_APP;
        } else if (se.getCode() == SpaceException.Code.UNABLE_TO_ADD_APPLICATION) {
          msg = MSG_ERROR_UNABLE_TO_ADD_APPLICATION;
        } else if (se.getCode() == SpaceException.Code.ERROR_RETRIEVING_USER) {
          msg = MSG_ERROR_RETRIEVING_USER;
        } else {
          msg = MSG_ERROR_SPACE_CREATION;
        }
        LOG.error("Failed to create a new space", se);
        uiApplication.addMessage(new ApplicationMessage(msg, null, ApplicationMessage.ERROR));
        return;
      }
      SpaceUtils.endRequest();
      UIPopupWindow uiPopup = uiAddForm.getParent();
      uiPopup.setShow(false);
      Utils.updateWorkingWorkSpace();
      // TODO Re-check and re-confirm that navigation is ok then re-direct into Home of space.
      JavascriptManager jsManager = ctx.getJavascriptManager();
      jsManager.addJavascript("try { window.location.href='" + Utils.getSpaceHomeURL(space) + "' } catch(e) {" +
              "window.location.href('" + Utils.getSpaceHomeURL(space) + "') }");
    }
  }

  /**
   * listener for toggle use existing group action When this action is triggered, a group selector
   * poup will show up for choosing.
   */
  static public class ToggleUseGroupActionListener extends EventListener<UISpaceAddForm> {
    @Override
    public void execute(Event<UISpaceAddForm> event) throws Exception {
      UISpaceAddForm uiSpaceAddForm = event.getSource();
      UISpaceGroupBound uiSpaceGroupBound = uiSpaceAddForm.getChild(UISpaceGroupBound.class);
      UICheckBoxInput uiUseExistingGroup = uiSpaceGroupBound.getChild(UICheckBoxInput.class);
      if (uiUseExistingGroup.isChecked()) {
        UIPopupWindow uiPopup = uiSpaceGroupBound.getChild(UIPopupWindow.class);
        UISocialGroupSelector uiGroupSelector = uiSpaceAddForm.createUIComponent(UISocialGroupSelector.class,
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

  static public class ChangeOptionActionListener extends EventListener<UISpaceAddForm> {
    private final String VISIBLE_OPEN_SPACE = "UISpaceVisibility.label.VisibleAndOpenSpace";
    private final String VISIBLE_VALIDATION_SPACE = "UISpaceVisibility.label.VisibleAndValidationSpace";
    private final String VISIBLE_CLOSE_SPACE = "UISpaceVisibility.label.VisibleAndCloseSpace";
    private final String HIDDEN_SPACE = "UISpaceVisibility.label.HiddenSpace";

    @Override
    public void execute(Event<UISpaceAddForm> event) throws Exception {
      UISpaceAddForm uiSpaceAddForm = event.getSource();
      WebuiRequestContext ctx = event.getRequestContext();
      ResourceBundle resApp = ctx.getApplicationResourceBundle();

      String visibleAndOpenSpace = resApp.getString(VISIBLE_OPEN_SPACE);
      String visibleAndValidationSpace = resApp.getString(VISIBLE_VALIDATION_SPACE);
      String visibleAndCloseSpace = resApp.getString(VISIBLE_CLOSE_SPACE);
      String hiddenSpace = resApp.getString(HIDDEN_SPACE);

      //Space space = new Space();
      //uiSpaceAddForm.invokeSetBindingBean(space);
      UIFormInputSet uiSpaceVisibility = uiSpaceAddForm.getChildById(uiSpaceAddForm.SPACE_VISIBILITY);
      UIFormRadioBoxInput selectPriority = uiSpaceVisibility.getChildById(UISpaceVisibility.UI_SPACE_VISIBILITY);
      UIFormRadioBoxInput selectRegistration = uiSpaceVisibility.getChildById(UISpaceVisibility.UI_SPACE_REGISTRATION);
      
      UIFormInputInfo uiFormInfo = uiSpaceVisibility.getChild(UIFormInputInfo.class);

      String currentVisibility = selectPriority.getValue();
      String currentRegistration = selectRegistration.getValue();
      boolean isPrivate = Space.PRIVATE.equals(currentVisibility);
      boolean isOpen = Space.OPEN.equals(currentRegistration);
      boolean isValidation = Space.VALIDATION.equals(currentRegistration);
      boolean isClose = Space.CLOSE.equals(currentRegistration);
      if (isPrivate && isOpen) {
        uiFormInfo.setValue(visibleAndOpenSpace);
      } else if (isPrivate && isValidation) {
        uiFormInfo.setValue(visibleAndValidationSpace);
      } else if (isPrivate && isClose) {
        uiFormInfo.setValue(visibleAndCloseSpace);
      } else {
        uiFormInfo.setValue(hiddenSpace);
      }
    }
  }
}
