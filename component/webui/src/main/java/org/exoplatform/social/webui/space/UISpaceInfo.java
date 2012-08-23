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
import org.apache.commons.lang.StringUtils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfigService;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.UIAvatarUploadContent;
import org.exoplatform.social.webui.UIAvatarUploader;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.url.navigation.NodeURL;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UITabPane;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.ExpressionValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;

/**
 * UISpaceInfo.java used for managing space's name, description, priority...<br />
 * Created by The eXo Platform SARL
 *
 * @author <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 * @since Sep 12, 2008
 */

@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "classpath:groovy/social/webui/space/UISpaceInfo.gtmpl",
  events = {
    @EventConfig(listeners = UISpaceInfo.SaveActionListener.class, phase = Phase.PROCESS),
    @EventConfig(listeners = UISpaceInfo.ChangeAvatarActionListener.class)
  }
)
public class UISpaceInfo extends UIForm {
  
  private static final Log LOG = ExoLogger.getLogger(UISpaceInfo.class);
  
  private static final String SPACE_PRIORITY = "priority";
  private static final String PRIORITY_HIGH = "high";
  private static final String PRIORITY_IMMEDIATE = "immediate";
  private static final String PRIORITY_LOW = "low";
  private static final String SPACE_ID = "id";
  private static final String SPACE_DISPLAY_NAME = "displayName";
  private static final String SPACE_DESCRIPTION = "description";
  private SpaceService spaceService = null;
  private final String POPUP_AVATAR_UPLOADER = "UIPopupAvatarUploader";
  
  /** Html attribute title. */
  private static final String HTML_ATTRIBUTE_TITLE   = "title";
  
  /**
   * constructor
   * 
   * @throws Exception
   */
  public UISpaceInfo() throws Exception {
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    ResourceBundle resourceBundle = requestContext.getApplicationResourceBundle();
    UIFormStringInput spaceId = new UIFormStringInput(SPACE_ID, SPACE_ID, null);
    spaceId.setHTMLAttribute(HTML_ATTRIBUTE_TITLE, resourceBundle.getString("UISpaceInfo.label.SpaceId"));
    addUIFormInput((UIFormStringInput)spaceId.setRendered(false));

    UIFormStringInput spaceDisplayNameInput = new UIFormStringInput(SPACE_DISPLAY_NAME, SPACE_DISPLAY_NAME, null);
    
    addUIFormInput(spaceDisplayNameInput.
                   addValidator(MandatoryValidator.class).
                   //addValidator(ExpressionValidator.class, "^[\\p{L}\\s\\d]+$", "ResourceValidator.msg.Invalid-char").
                   addValidator(ExpressionValidator.class, "^([\\p{L}\\d]+[\\s]?)+$", "UISpaceInfo.msg.name-invalid").
                   addValidator(StringLengthValidator.class, 3, 30));

    addUIFormInput(new UIFormTextAreaInput(SPACE_DESCRIPTION, SPACE_DESCRIPTION, null).
                   addValidator(StringLengthValidator.class, 0, 255));

    List<SelectItemOption<String>> priorityList = new ArrayList<SelectItemOption<String>>(3);
    SelectItemOption<String> pHigh = new SelectItemOption<String>(PRIORITY_HIGH, Space.HIGH_PRIORITY);
    SelectItemOption<String> pImmediate = new SelectItemOption<String>(PRIORITY_IMMEDIATE, Space.INTERMEDIATE_PRIORITY);
    SelectItemOption<String> pLow = new SelectItemOption<String>(PRIORITY_LOW, Space.LOW_PRIORITY);
    priorityList.add(pHigh);
    priorityList.add(pImmediate);
    priorityList.add(pLow);
    UIFormSelectBox selectPriority = new UIFormSelectBox(SPACE_PRIORITY, SPACE_PRIORITY, priorityList);
    addUIFormInput(selectPriority);
    //temporary disable tag
    UIFormStringInput tag = new UIFormStringInput("tag","tag",null);
    tag.setHTMLAttribute(HTML_ATTRIBUTE_TITLE, resourceBundle.getString("UISpaceInfo.label.tag"));
    addUIFormInput((UIFormStringInput)tag.setRendered(false));

    UIPopupWindow uiPopup = createUIComponent(UIPopupWindow.class, null, POPUP_AVATAR_UPLOADER);
    uiPopup.setWindowSize(500, 0);
    addChild(uiPopup);
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
    UIFormTextAreaInput descriptionInput = getUIFormTextAreaInput(SPACE_DESCRIPTION);
    descriptionInput.setValue(StringEscapeUtils.unescapeHtml(descriptionInput.getValue().trim()));
    //TODO: have to find the way to don't need the line code below.
    getUIStringInput("tag").setValue(space.getTag());
  }

  public void saveAvatar(UIAvatarUploadContent uiAvatarUploadContent, Space space) throws Exception {
    SpaceService spaceService = getSpaceService();

    space.setAvatarAttachment(uiAvatarUploadContent.getAvatarAttachment());
    spaceService.updateSpace(space);
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

      PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIApplication uiApp = requestContext.getUIApplication();
      String id = uiSpaceInfo.getUIStringInput(SPACE_ID).getValue();
      String name = uiSpaceInfo.getUIStringInput(SPACE_DISPLAY_NAME).getValue();
      Space space = spaceService.getSpaceById(id);
      String oldDisplayName = space.getDisplayName();
      if (space == null) {
        //redirect to spaces
        portalRequestContext.getResponse().sendRedirect(Utils.getURI("all-spaces"));
        return;
      }
      
      UserNode selectedNode = uiPortal.getSelectedUserNode();
      UserNode renamedNode = null;
      
      boolean nameChanged = (!space.getDisplayName().equals(name));
      if (nameChanged) {
        String cleanedString = SpaceUtils.cleanString(name);
        space.setUrl(cleanedString);
        if (spaceService.getSpaceByUrl(cleanedString) != null) {
          uiApp.addMessage(new ApplicationMessage("UISpaceInfo.msg.current-name-exist", null, ApplicationMessage.INFO));
          return;
        }
        
        renamedNode = uiSpaceInfo.renamePageNode(name, space);
        if (renamedNode == null) {
          return;
        }
      }
      uiSpaceInfo.invokeSetBindingBean(space);
      String description = space.getDescription();
      space.setDescription(StringUtils.isEmpty(description) ? " " : StringEscapeUtils.escapeHtml(description)); 

      if (nameChanged) {
        space.setDisplayName(oldDisplayName);
        spaceService.renameSpace(space, name);
      } else {
        spaceService.updateSpace(space);
      }
      
      //uiSpaceInfo.setCurrentSpace(space);
      
      if (nameChanged) {
        if (renamedNode != null) {
          //update space navigation (change name).
          selectedNode = renamedNode;  
          PortalRequestContext prContext = Util.getPortalRequestContext();
          prContext.createURL(NodeURL.TYPE).setNode(selectedNode);
          portalRequestContext.getResponse().sendRedirect(Utils.getSpaceURL(selectedNode));
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
      UIPopupWindow uiPopup = uiSpaceInfo.getChild(UIPopupWindow.class);
      UIAvatarUploader uiAvatarUploader = uiSpaceInfo.createUIComponent(UIAvatarUploader.class, null, null);
      uiPopup.setUIComponent(uiAvatarUploader);
      uiPopup.setShow(true);
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
      UserNode parentNode = SpaceUtils.getParentNode().getParent().getParent();
      if (parentNode == null) {
        parentNode = SpaceUtils.getParentNode().getParent();
      }
      if (parentNode == null || parentNode.getChild(SpaceUtils.cleanString(space.getPrettyName())) == null) {
        return null;
      }
      
      UserNode renamedNode = parentNode.getChild(SpaceUtils.cleanString(space.getPrettyName()));
      
      renamedNode.setLabel(newNodeLabel);

      String newNodeName = SpaceUtils.cleanString(newNodeLabel);
      if (parentNode.getChild(newNodeName) != null) {
        newNodeName = newNodeName + "_" + System.currentTimeMillis();
      }
      renamedNode.setName(newNodeName);

      Page page = configService.getPage(renamedNode.getPageRef());
      if (page != null) {
        page.setTitle(newNodeLabel);
        dataService.save(page);
      }

      SpaceUtils.getUserPortal().saveNode(parentNode, null);
      
      SpaceUtils.changeSpaceUrlPreference(renamedNode, space, newNodeLabel);
      
      for (UserNode childNode : renamedNode.getChildren()) {
        SpaceUtils.changeSpaceUrlPreference(childNode, space, newNodeLabel);
      }
      return renamedNode;
    } catch (Exception e) {
      LOG.warn(e.getMessage() , e);
      return null;
    }
  }
}
