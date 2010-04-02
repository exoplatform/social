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
package social.portal.webui.component;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.UserPortalConfig;
import org.exoplatform.portal.config.model.PageNavigation;
import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceAttachment;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceUtils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
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
 * @author <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 * @since Sep 12, 2008
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/portal/webui/uiform/UISpaceInfo.gtmpl",
    events = {
        @EventConfig(listeners = UISpaceInfo.SaveActionListener.class, phase = Phase.PROCESS),
        @EventConfig(listeners = UISpaceInfo.ChangeAvatarActionListener.class)
    }
)
public class UISpaceInfo extends UIForm {
  private final String SPACE_PRIORITY = "priority";
  private final String PRIORITY_HIGH = "high";
  private final String PRIORITY_IMMEDIATE = "immediate";
  private final String PRIORITY_LOW = "low";
  private SpaceService spaceService = null;
  private final String POPUP_AVATAR_UPLOADER = "UIPopupAvatarUploader";
  /**
   * constructor
   * @throws Exception
   */
  public UISpaceInfo() throws Exception {
    addUIFormInput((UIFormStringInput)new UIFormStringInput("id","id",null).setRendered(false)).
    addUIFormInput(new UIFormStringInput("name","name",null).
                   addValidator(MandatoryValidator.class).
                   addValidator(ExpressionValidator.class, "^[\\p{L}][\\p{ASCII}]+$", "UISpaceInfo.msg.name-invalid").
                   addValidator(StringLengthValidator.class, 3, 30)).
    addUIFormInput(new UIFormTextAreaInput("description","description",null).
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
    addUIFormInput((UIFormStringInput)new UIFormStringInput("tag","tag",null).setRendered(false));
    
    UIPopupWindow uiPopup = createUIComponent(UIPopupWindow.class, null, POPUP_AVATAR_UPLOADER);
    uiPopup.setWindowSize(500, 0);
    addChild(uiPopup);
  }
  
  /**
   * Sets space for this ui component to work with.
   * @param space
   * @throws Exception
   */
  public void setValue(Space space) throws Exception {
    invokeGetBindingBean(space);
    //TODO: have to find the way to don't need the line code below. 
    getUIStringInput("tag").setValue(space.getTag());
  }

  /**
   * Gets image source url
   * @return image source url
   * @throws Exception
   */
  protected String getImageSource() throws Exception {
    SpaceService spaceService = getSpaceService();
    String id = getUIStringInput("id").getValue();
    Space space = spaceService.getSpaceById(id);
    if (space == null) {
      return null;
    }
    SpaceAttachment spaceAtt = (SpaceAttachment) space.getSpaceAttachment();
    if (spaceAtt != null) {
      return "/" + getPortalName()+"/rest/jcr/" + getRepository()+ "/" + spaceAtt.getWorkspace()
              + spaceAtt.getDataPath() + "/?rnd=" + System.currentTimeMillis();
    }
    return null;
  }
  
  /**
   * Triggers this action when user click on the Save button.
   * Creating a space from existing group or creating new group for this space.
   * Initialize some default applications in space component configuration.xml file.
   * @author hoatle
   *
   */
  static public class SaveActionListener extends EventListener<UISpaceInfo> {
    public void execute(Event<UISpaceInfo> event) throws Exception {
      UISpaceInfo uiSpaceInfo = event.getSource();
      SpaceService spaceService = uiSpaceInfo.getSpaceService();
      UIPortal uiPortal = Util.getUIPortal();

      PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
      WebuiRequestContext requestContext = event.getRequestContext();
      UIApplication uiApp = requestContext.getUIApplication();
      String id = uiSpaceInfo.getUIStringInput("id").getValue();
      String name = uiSpaceInfo.getUIStringInput("name").getValue();
      Space space = spaceService.getSpaceById(id);
      String spaceUrl = space.getUrl();
      if (space == null) {
        //redirect to spaces
        portalRequestContext.getResponse().sendRedirect(portalRequestContext.getPortalURI() + "spaces");
        return;
      }
      PageNode selectedNode = uiPortal.getSelectedNode();
      PageNode homeNode = null;
      boolean nameChanged = (space.getName() != name);
      if (nameChanged) {
        UserPortalConfig userPortalConfig = Util.getUIPortalApplication().getUserPortalConfig();
        List<PageNavigation> pageNavigations = userPortalConfig.getNavigations();
        DataStorage dataStorage = uiSpaceInfo.getApplicationComponent(DataStorage.class);
        String cleanedString = SpaceUtils.cleanString(name);
        space.setUrl(cleanedString);
        PageNavigation spaceNavigation = dataStorage.getPageNavigation(PortalConfig.GROUP_TYPE, space.getGroupId());
        for (PageNavigation pageNavigation : pageNavigations) {
          if (pageNavigation.getOwner().equals(spaceNavigation.getOwner())) {
            spaceNavigation = pageNavigation;
            break;
          }
        }
        homeNode = SpaceUtils.getHomeNode(spaceNavigation, spaceUrl);
        if (homeNode == null) {
          throw new Exception("homeNode is null!");
        }
        homeNode.setUri(cleanedString);
        homeNode.setName(cleanedString);
        homeNode.setLabel(name);
        List<PageNode> childNodes = homeNode.getNodes();
        PageNode childNode;
        String oldUri;
        String newUri;
        for (int i = 0; i < childNodes.size(); i++) {
          childNode = childNodes.get(i);
          oldUri = childNode.getUri();
          newUri = oldUri.replace(oldUri.substring(0, oldUri.lastIndexOf("/")), cleanedString);
          childNode.setUri(newUri);
          childNode.setName(newUri.substring(newUri.lastIndexOf("/") + 1, newUri.length()));
          if (selectedNode.getName().equals(childNode.getName())) {
            selectedNode = childNode;
          }
        }
        dataStorage.save(spaceNavigation);
        
        uiPortal.setSelectedNode(selectedNode);
        SpaceUtils.setNavigation(spaceNavigation);
      }
      
      uiSpaceInfo.invokeSetBindingBean(space);
      spaceService.saveSpace(space, false);
      if (nameChanged) {
        portalRequestContext.getResponse().sendRedirect(portalRequestContext.getPortalURI() + selectedNode.getUri());
        return;
      } else {
        uiApp.addMessage(new ApplicationMessage("UISpaceInfo.msg.update-success", null, ApplicationMessage.INFO));
        SpaceUtils.updateWorkingWorkSpace();
      }
    }
  }
  
  /**
   * Triggers this action for editing avatar. An UIAvatarUploader popup should be displayed.
   * @author hoatle
   *
   */
  static public class ChangeAvatarActionListener extends EventListener<UISpaceInfo> {

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
   * Gets spaceService
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
   * Gets organizationService
   * @return organizationService
   */
  public OrganizationService getOrganizationService() {
     return getApplicationComponent(OrganizationService.class);
  }
  
  /**
   * Gets dataSource
   * @return
   */
  public DataStorage getDataSource() {
      return getApplicationComponent(DataStorage.class);
  }
  /**
   * Gets current portal name
   * @return current portal name
   */
  private String getPortalName() {
    return PortalContainer.getCurrentPortalContainerName();
  }
  
  /**
   * Gets current repository name
   * @return current repository name
   * @throws Exception
   */
  private String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class);
    return rService.getCurrentRepository().getConfiguration().getName();
  }
}
