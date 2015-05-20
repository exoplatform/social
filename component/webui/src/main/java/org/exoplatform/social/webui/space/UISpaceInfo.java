/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.model.Space.UpdatedField;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.UIAvatarUploadContent;
import org.exoplatform.social.webui.UIAvatarUploader;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.composer.PopupContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.url.navigation.NodeURL;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.UITabPane;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.ExpressionValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;


@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "war:/groovy/social/webui/space/UISpaceInfo.gtmpl",
  events = {
    @EventConfig(listeners = UISpaceInfo.SaveActionListener.class, phase = Phase.PROCESS),
    @EventConfig(listeners = UISpaceInfo.ChangeAvatarActionListener.class)
  }
)
public class UISpaceInfo extends UIForm {
  
  private static final Log LOG = ExoLogger.getLogger(UISpaceInfo.class);
  
  public static final String SPACE_ID = "id";

  private static final String SPACE_DISPLAY_NAME            = "displayName";
  private static final String SPACE_DESCRIPTION             = "description";
  private static final String SPACE_TAG                     = "tag";
  private SpaceService spaceService = null;
  private final static String POPUP_AVATAR_UPLOADER = "UIPopupAvatarUploader";
  private static final String MSG_DEFAULT_SPACE_DESCRIPTION = "UISpaceAddForm.msg.default_space_description";
  
  /** Html attribute title. */
  private static final String HTML_ATTRIBUTE_TITLE          = "title";
  private static final String HTML_ATTRIBUTE_PLACEHOLDER    = "placeholder";
  
  /**
   * constructor
   * 
   * @throws Exception
   */
  public UISpaceInfo() throws Exception {
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    ResourceBundle resourceBundle = requestContext.getApplicationResourceBundle();
    UIFormStringInput spaceId = new UIFormStringInput(SPACE_ID, SPACE_ID, null).setRendered(false);
    spaceId.setHTMLAttribute(HTML_ATTRIBUTE_TITLE, resourceBundle.getString("UISpaceInfo.label.SpaceId"));
    addUIFormInput(spaceId);

    UIFormStringInput spaceDisplayName = new UIFormStringInput(SPACE_DISPLAY_NAME, SPACE_DISPLAY_NAME, null);
    spaceDisplayName.setHTMLAttribute(HTML_ATTRIBUTE_PLACEHOLDER, resourceBundle.getString("UISpaceSettings.label.spaceDisplayName"));
    addUIFormInput(spaceDisplayName.
                   addValidator(MandatoryValidator.class).
                   addValidator(ExpressionValidator.class, "^([\\p{L}\\d\']+[\\s]?)+$", "UISpaceInfo.msg.name-invalid").
                   addValidator(StringLengthValidator.class, 3, 30));

    UIFormTextAreaInput description = new UIFormTextAreaInput(SPACE_DESCRIPTION, SPACE_DESCRIPTION, null);
    description.setHTMLAttribute(HTML_ATTRIBUTE_PLACEHOLDER, resourceBundle.getString("UISpaceSettings.label.spaceDescription"));
    addUIFormInput(description.addValidator(StringLengthValidator.class, 0, 255));

    //temporary disable tag
    UIFormStringInput tag = new UIFormStringInput(SPACE_TAG, SPACE_TAG, null).setRendered(false);
    tag.setHTMLAttribute(HTML_ATTRIBUTE_TITLE, resourceBundle.getString("UISpaceInfo.label.tag"));
    addUIFormInput(tag);

    PopupContainer popupContainer = createUIComponent(PopupContainer.class, null, null);
    addChild(popupContainer);
  }

  /**
   * Sets the current space to ui component.
   * 
   * @param space
   * @throws Exception
   * @since 1.2.8
   */
  protected void setCurrentSpace(Space space) throws Exception {
    // reset current space to component of uispacesetting.
    UITabPane uiTabPane = this.getAncestorOfType(UITabPane.class);
    uiTabPane.setSelectedTab(1);
    UISpaceInfo uiSpaceInfo = uiTabPane.getChild(UISpaceInfo.class);
    uiSpaceInfo.setValue(space);
    UISpaceMember uiSpaceMember = uiTabPane.getChild(UISpaceMember.class);
    uiSpaceMember.setValue(space.getId());
    uiSpaceMember.setSpaceURL(space.getUrl());
    UISpaceApplication uiSpaceApplication = uiTabPane.getChild(UISpaceApplication.class);
    uiSpaceApplication.setValue(space);
    UISpacePermission uiSpacePermission = uiTabPane.getChild(UISpacePermission.class);
    uiSpacePermission.setValue(space);
  }
  
  /**
   * Sets space for this ui component to work with.
   *
   * @param space
   * @throws Exception
   */
  public void setValue(Space space) throws Exception {
    invokeGetBindingBean(space);
    UIFormTextAreaInput description = getUIFormTextAreaInput(SPACE_DESCRIPTION);
    description.setValue(StringEscapeUtils.unescapeHtml(description.getValue()));
    //TODO: have to find the way to don't need the line code below.
    getUIStringInput(SPACE_TAG).setValue(space.getTag());
  }

  public void saveAvatar(UIAvatarUploadContent uiAvatarUploadContent, Space space) throws Exception {
    SpaceService spaceService = getSpaceService();
    space.setAvatarAttachment(uiAvatarUploadContent.getAvatarAttachment());
    spaceService.updateSpace(space);
    space.setEditor(Utils.getViewerRemoteId());
    spaceService.updateSpaceAvatar(space);
  }

  /**
   * Gets image source url.
   *
   * @return image source url
   * @throws Exception
   */
  protected String getImageSource() throws Exception {
    SpaceService spaceService = getSpaceService();
    String id = getUIStringInput(SPACE_ID).getValue();
    Space space = spaceService.getSpaceById(id);
    return space.getAvatarUrl();
  }

  /**
   * Triggers this action when user click on the Save button.
   * Creating a space from existing group or creating new group for this space.
   * Initialize some default applications in space component configuration.xml file.
   *
   * @author hoatle
   */
  public static class SaveActionListener extends EventListener<UISpaceInfo> {
    public void execute(Event<UISpaceInfo> event) throws Exception {
      UISpaceInfo uiSpaceInfo = event.getSource();
      SpaceService spaceService = uiSpaceInfo.getSpaceService();
      UIPortal uiPortal = Util.getUIPortal();

      String id = uiSpaceInfo.getUIStringInput(SPACE_ID).getValue();
      String name = uiSpaceInfo.getUIStringInput(SPACE_DISPLAY_NAME).getValue();
      Space space = spaceService.getSpaceById(id);
      String oldDisplayName = space.getDisplayName();
      String existingDescription = space.getDescription();
      
      if (space == null) {
        //redirect to spaces
        event.getRequestContext().sendRedirect(Utils.getURI("all-spaces"));
        return;
      }
      
      UserNode selectedNode = uiPortal.getSelectedUserNode();
      UserNode renamedNode = null;
      
      boolean nameChanged = (!space.getDisplayName().equals(name));
      UIPortletApplication uiApp = uiSpaceInfo.getAncestorOfType(UIPortletApplication.class);
      if (nameChanged) {

        String cleanedString = SpaceUtils.cleanString(name);
        if (spaceService.getSpaceByUrl(cleanedString) != null) {
          // reset to origin values
          uiSpaceInfo.getUIStringInput(SPACE_DISPLAY_NAME).setValue(oldDisplayName);
          uiSpaceInfo.getUIFormTextAreaInput(SPACE_DESCRIPTION).setValue(existingDescription);

          // 
          uiApp.addMessage(new ApplicationMessage("UISpaceInfo.msg.current-name-exist", null, ApplicationMessage.INFO));
          return;
        }
        
        renamedNode = uiSpaceInfo.renamePageNode(name, space);
        if (renamedNode == null) {
          return;
        }
        
      }
      uiSpaceInfo.invokeSetBindingBean(space);
      
      String spaceDescription = space.getDescription();
      if (spaceDescription == null || spaceDescription.trim().length() == 0) {
        ResourceBundle resourceBundle = event.getRequestContext().getApplicationResourceBundle();
        space.setDescription(resourceBundle.getString(MSG_DEFAULT_SPACE_DESCRIPTION));
        uiSpaceInfo.getUIFormTextAreaInput(SPACE_DESCRIPTION).setValue(space.getDescription());
      } else {
        space.setDescription(StringEscapeUtils.escapeHtml(space.getDescription()));
        if (!existingDescription.equals(spaceDescription)) {
          space.setField(UpdatedField.DESCRIPTION);  
        }
      }

      space.setEditor(Utils.getViewerRemoteId());
      
      if (nameChanged) {
        space.setDisplayName(oldDisplayName);
        String remoteId = Utils.getViewerRemoteId();
        spaceService.renameSpace(remoteId, space, name);
        
        // rename group label
        OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer().
            getComponentInstanceOfType(OrganizationService.class);
        GroupHandler groupHandler = organizationService.getGroupHandler();
        Group group = groupHandler.findGroupById(space.getGroupId());
        group.setLabel(space.getDisplayName());
        groupHandler.saveGroup(group, true);
      } else {
        spaceService.updateSpace(space);
      }
      
      if (nameChanged) {
        if (renamedNode != null) {
          //update space navigation (change name).
          selectedNode = renamedNode;  
          PortalRequestContext prContext = Util.getPortalRequestContext();
          prContext.createURL(NodeURL.TYPE).setNode(selectedNode);
          event.getRequestContext().sendRedirect(Utils.getSpaceURL(selectedNode));
          return;
        }
      } else {
        uiApp.addMessage(new ApplicationMessage("UISpaceInfo.msg.update-success", null, ApplicationMessage.INFO));
      }
    }
  }

  /**
   * Triggers this action for editing avatar. An UIAvatarUploader popup should be displayed.
   *
   * @author hoatle
   */
  public static class ChangeAvatarActionListener extends EventListener<UISpaceInfo> {

    @Override
    public void execute(Event<UISpaceInfo> event) throws Exception {
      UISpaceInfo uiSpaceInfo = event.getSource();
      PopupContainer popupContainer = uiSpaceInfo.getChild(PopupContainer.class);
      popupContainer.activate(UIAvatarUploader.class, 500, POPUP_AVATAR_UPLOADER);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
    }
  }

  /**
   * Gets spaceService.
   *
   * @return spaceService
   * @see SpaceService
   */
  public SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = getApplicationComponent(SpaceService.class);
    }
    return spaceService;
  }

  /**
   * Gets organizationService.
   *
   * @return organizationService
   */
  public OrganizationService getOrganizationService() {
    return getApplicationComponent(OrganizationService.class);
  }

  /**
   * Gets dataSource.
   *
   * @return
   */
  public DataStorage getDataSource() {
    return getApplicationComponent(DataStorage.class);
  }

  /**
   * Rename page node.
   * 
   * @param newNodeLabel
   * @param space
   * @return
   * @since 1.2.8
   */
  private UserNode renamePageNode(String newNodeLabel, Space space) {
    UserPortalConfigService configService = getApplicationComponent(UserPortalConfigService.class);

    DataStorage dataService = getApplicationComponent(DataStorage.class);

    try {

      UserNode renamedNode = SpaceUtils.getSpaceUserNode(space);
      UserNode parentNode = renamedNode.getParent();
      String newNodeName = SpaceUtils.cleanString(newNodeLabel);
      
      
      if (parentNode.getChild(newNodeName) != null) {
        newNodeName = newNodeName + "_" + System.currentTimeMillis();
      }
      
      //
      renamedNode.setLabel(newNodeLabel);
      renamedNode.setName(newNodeName);

      Page page = dataService.getPage(renamedNode.getPageRef().format());
      if (page != null) {
        page.setTitle(newNodeLabel);
        dataService.save(page);
      }
      
      SpaceUtils.getUserPortal().saveNode(parentNode, null);

      space.setUrl(newNodeName);
      SpaceUtils.changeSpaceUrlPreference(renamedNode, space, newNodeLabel);
      
      List<UserNode> userNodes =  new ArrayList<UserNode>(renamedNode.getChildren());
      for (UserNode childNode : userNodes) {
        SpaceUtils.changeSpaceUrlPreference(childNode, space, newNodeLabel);
      }
      return renamedNode;
    } catch (Exception e) {
      LOG.warn(e.getMessage() , e);
      return null;
    }
  }
}
