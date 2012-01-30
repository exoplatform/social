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
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.mop.user.UserNavigation;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.portal.mop.user.UserPortal;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.UIAvatarUploadContent;
import org.exoplatform.social.webui.UIAvatarUploader;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
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
  private static final String SPACE_PRIORITY = "priority";
  private static final String PRIORITY_HIGH = "high";
  private static final String PRIORITY_IMMEDIATE = "immediate";
  private static final String PRIORITY_LOW = "low";
  private static final String SPACE_ID = "id";
  private static final String SPACE_DISPLAY_NAME = "displayName";
  private static final String SPACE_DESCRIPTION = "description";
  private SpaceService spaceService = null;
  private final String POPUP_AVATAR_UPLOADER = "UIPopupAvatarUploader";
  private static final String MSG_DEFAULT_SPACE_DESCRIPTION = "UISpaceAddForm.msg.default_space_description";
  
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
    spaceDisplayNameInput.setEditable(false);
    
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
   * Sets space for this ui component to work with.
   *
   * @param space
   * @throws Exception
   */
  public void setValue(Space space) throws Exception {
    invokeGetBindingBean(space);
    String descValue = ((UIFormTextAreaInput) this.getChildById(SPACE_DESCRIPTION)).getValue();
    ((UIFormTextAreaInput) this.getChildById(SPACE_DESCRIPTION)).setValue(StringEscapeUtils.unescapeHtml(descValue));
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
      if (space == null) {
        //redirect to spaces
        portalRequestContext.getResponse().sendRedirect(Utils.getURI("all-spaces"));
        return;
      }
      String spaceUrl = space.getUrl();
      UserNode selectedNode = uiPortal.getSelectedUserNode();
      UserNode homeNode = null;
      boolean nameChanged = (!space.getDisplayName().equals(name));
      if (nameChanged) {
        String cleanedString = SpaceUtils.cleanString(name);
        if (spaceService.getSpaceByUrl(cleanedString) != null) {
          uiApp.addMessage(new ApplicationMessage("UISpaceInfo.msg.current-name-exist", null, ApplicationMessage.INFO));
          return;
        }
        UserPortalConfig userPortalConfig = Util.getUIPortalApplication().getUserPortalConfig();
        UserPortal userPortal = userPortalConfig.getUserPortal();
        List<UserNavigation> pageNavigations = userPortal.getNavigations();
        space.setUrl(cleanedString);
        UserNavigation spaceNavigation = SpaceUtils.getGroupNavigation(space.getGroupId());
        for (UserNavigation pageNavigation : pageNavigations) {
          if (pageNavigation.getKey().getName().equals(spaceNavigation.getKey().getName())) {
            spaceNavigation = pageNavigation;
            break;
          }
        }
        homeNode = SpaceUtils.getHomeNodeWithChildren(spaceNavigation, spaceUrl);
        if (homeNode == null) {
          throw new Exception("homeNode is null!");
        }
        SpaceUtils.changeSpaceUrlPreference(homeNode, space, name);
        //homeNode.setUri(cleanedString);
        homeNode.setName(cleanedString);
        homeNode.setLabel(name);
        Collection<UserNode> childNodes = homeNode.getChildren();
        UserNode childNode;
        String oldUri;
        String newUri;
        while(childNodes.iterator().hasNext()) {
          childNode = childNodes.iterator().next();
          SpaceUtils.changeSpaceUrlPreference(childNode, space, name);
          oldUri = childNode.getURI();
          newUri = oldUri.replace(oldUri.substring(0, oldUri.lastIndexOf("/")), cleanedString);
          //Need to checking ???
          //childNode.setUri(newUri);
          childNode.setName(newUri.substring(newUri.lastIndexOf("/") + 1, newUri.length()));
          if (selectedNode.getName().equals(childNode.getName())) {
            selectedNode = childNode;
          }
        }
       
        //Need to get userPortal clear the caching.
        userPortalConfig.getUserPortal().saveNode(homeNode, null);
        SpaceUtils.setNavigation(spaceNavigation);
      }
      uiSpaceInfo.invokeSetBindingBean(space);
      
      String spaceDescription = space.getDescription();
      if (spaceDescription == null || spaceDescription.trim().length() == 0) {
        ResourceBundle resourceBundle = requestContext.getApplicationResourceBundle();
        space.setDescription(resourceBundle.getString(MSG_DEFAULT_SPACE_DESCRIPTION));
        uiSpaceInfo.getUIFormTextAreaInput(SPACE_DESCRIPTION).setValue(space.getDescription());
      } else {
        space.setDescription(StringEscapeUtils.escapeHtml(space.getDescription()));
      }

      spaceService.updateSpace(space);
      if (nameChanged) {
        //update Space Navigation (change name).
        UISpaceSetting uiSpaceSetting = uiSpaceInfo.getAncestorOfType(UISpaceSetting.class);
        portalRequestContext.getResponse().sendRedirect(Utils.getSpaceURL(selectedNode));
        return;
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

}
