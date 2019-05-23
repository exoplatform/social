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

import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringEscapeUtils;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.SpaceTemplate;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.SpacesAdministrationService;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.space.spi.SpaceTemplateService;
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
import org.exoplatform.webui.form.*;

import static org.exoplatform.social.webui.space.UISpaceSettings.SPACE_TEMPLATE;

/**
 * UIAddSpaceForm to create new space. By using this UIForm, user can create a
 * brand new space or a space from an existing group
 *
 * @author <a href="mailto:hoatlevan@gmail.com">hoatle</a>
 * @since Jun 29, 2009
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl",
  events = {
    @EventConfig(listeners = UISpaceAddForm.CreateActionListener.class),
    @EventConfig(listeners = UISpaceAddForm.ChangeTemplateActionListener.class, phase = Phase.DECODE)
  }
)

public class UISpaceAddForm extends UIForm {

  private static final Log LOG = ExoLogger.getLogger(UISpaceAddForm.class);
  
  static private final String MSG_ERROR_SPACE_CREATION = "UISpaceAddForm.msg.error_space_creation";
  static private final String MSG_ERROR_DATASTORE = "UISpaceAddForm.msg.error_space_not_saved";
  static private final String MSG_ERROR_UNABLE_TO_INIT_APP = "UISpaceAddForm.msg.error_unable_to_init_app";
  static private final String MSG_ERROR_UNABLE_TO_ADD_CREATOR = "UISpaceAddForm.msg.error_unable_to_add_creator";
  static private final String MSG_ERROR_UNABLE_TO_ADD_APPLICATION = "UISpaceAddForm.msg.error_unable_to_add_application";
  static private final String MSG_ERROR_RETRIEVING_USER = "UISpaceAddForm.msg.error_unable_to_retrieve_user";
  static private final String MSG_SPACE_CREATION_SUCCESS = "UISpaceAddForm.msg.space_creation_success";
  static private final String MSG_ERROR_SPACE_ALREADY_EXIST = "UISpaceAddForm.msg.error_space_already_exist";
  static private final String MSG_ERROR_SPACE_PERMISSION = "UISpaceAddForm.msg.error_space_permission";
  private final String SPACE_SETTINGS = "UISpaceSettings";
  private final String SPACE_VISIBILITY = "UISpaceVisibility";
  
  /**
   * Constructor: add 3 UI component to this UIFormTabPane:
   * <br>
   * <pre>
   * {@link UISpaceSettings}
   * {@link UISpaceVisibility}
   * {@link UIInvitation}
   * </pre>
   *
   * @throws Exception
   */
  public UISpaceAddForm() throws Exception {
    UISpaceSettings uiSpaceSettings = new UISpaceSettings(SPACE_SETTINGS);
    addChild(uiSpaceSettings);

    UIFormInputSet uiSpaceVisibility = new UISpaceVisibility(SPACE_VISIBILITY);

    addChild(uiSpaceVisibility);

    addChild(UIInvitation.class, null, null);

    setActions(new String[]{"Create"});
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
      SpacesAdministrationService spacesAdministrationService =  CommonsUtils.getService(SpacesAdministrationService.class);
      UIInvitation uiInvitation = uiAddForm.getChild(UIInvitation.class);
      List<Identity> invitedIdentities = uiInvitation.getSelectedIdentities();
      List<String> notFoundList = uiInvitation.getNotFoundInvitees();
      if (notFoundList.size() > 0) {
        StringBuilder sb = new StringBuilder();
        boolean isSeparated = false;
        for (String i : notFoundList) {
          if (isSeparated) {
            sb.append(", ");
          }
          sb.append("'").append(i).append("'");
          isSeparated = true;
        }
        uiApplication.addMessage(new ApplicationMessage("UIUserInvitation.msg.invalid-input",
            new String[]{sb.toString()},
            ApplicationMessage.ERROR));;
        return;
      }
      String creator = ctx.getRemoteUser();          
      Space space = new Space();
      uiAddForm.invokeSetBindingBean(space);
      space.setDisplayName(space.getDisplayName().trim());
      String spaceDisplayName = uiAddForm.getUIStringInput(UISpaceSettings.SPACE_DISPLAY_NAME).getValue();
      String spaceDescription = uiAddForm.getUIFormTextAreaInput(UISpaceSettings.SPACE_DESCRIPTION).getValue();
      String spaceVisibility = uiAddForm.findFirstComponentOfType(UISpaceVisibility.class).getVisibility();
      space.setDisplayName(spaceDisplayName.trim());
      space.setDescription(StringEscapeUtils.escapeHtml(spaceDescription));
      space.setPrettyName(space.getDisplayName());     
      space.setVisibility(spaceVisibility);
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
        if(!spacesAdministrationService.canCreateSpace(ctx.getRemoteUser())) {
          throw new SpaceException(SpaceException.Code.SPACE_PERMISSION);
        } else {   
          if (invitedIdentities != null) {// create space and invite identities to join it
            space = spaceService.createSpace(space, creator, invitedIdentities);
          } else { // Create new space
            space = spaceService.createSpace(space, creator);
          }
        }
      } catch (SpaceException se) {
        if (se.getCode() == SpaceException.Code.SPACE_ALREADY_EXIST) {
          msg = MSG_ERROR_SPACE_ALREADY_EXIST;
          uiApplication.addMessage(new ApplicationMessage(msg, null, ApplicationMessage.WARNING));
          return;
        } else if (se.getCode() == SpaceException.Code.SPACE_PERMISSION) {
          msg = MSG_ERROR_SPACE_PERMISSION;
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
      uiPopup.setUIComponent(null);
      uiPopup.setRendered(false);
      uiPopup.setShow(false);
      Utils.updateWorkingWorkSpace();
      // TODO Re-check and re-confirm that navigation is ok then re-direct into Home of space.
      JavascriptManager jsManager = ctx.getJavascriptManager();
      jsManager.addJavascript("try { window.location.href='" + Utils.getSpaceHomeURL(space) + "' } catch(e) {" +
              "window.location.href('" + Utils.getSpaceHomeURL(space) + "') }");
    }
  }

  static public class ChangeTemplateActionListener extends EventListener<UISpaceAddForm> {
    public void execute(Event<UISpaceAddForm> event) throws Exception {
      UISpaceAddForm uiSpaceAddForm = event.getSource();
      UISpaceSettings uiSpaceSettings = uiSpaceAddForm.getChild(UISpaceSettings.class);
      String templateName = uiSpaceSettings.getUIFormSelectBox(SPACE_TEMPLATE).getValue();
      SpaceTemplateService spaceTemplateService = PortalContainer.getInstance().getComponentInstanceOfType(SpaceTemplateService.class);
      SpaceTemplate spaceTemplate = spaceTemplateService.getSpaceTemplateByName(templateName);
      String visibility = spaceTemplate.getVisibility();
      String registration = spaceTemplate.getRegistration();
      UISpaceVisibility uiSpaceVisibility = uiSpaceAddForm.findFirstComponentOfType(UISpaceVisibility.class);
      UISpaceTemplateDescription uiSpaceTemplateDescription = uiSpaceSettings.getChild(UISpaceTemplateDescription.class);
      UIFormRadioBoxInput uiRegistration = uiSpaceVisibility.findComponentById(UISpaceVisibility.UI_SPACE_REGISTRATION);
      uiSpaceTemplateDescription.setTemplateName(templateName);
      uiSpaceVisibility.setVisibility(visibility);
      uiRegistration.setValue(registration);
      WebuiRequestContext ctx = event.getRequestContext();
      ctx.addUIComponentToUpdateByAjax(uiSpaceTemplateDescription);
      ctx.addUIComponentToUpdateByAjax(uiSpaceVisibility);
    }
  }
}
