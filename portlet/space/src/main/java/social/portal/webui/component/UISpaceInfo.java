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
import org.exoplatform.services.jcr.RepositoryService;
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
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.ExpressionValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;
/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Sep 12, 2008          
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/portal/webui/uiform/UISpaceInfo.gtmpl",
    events = {
        @EventConfig(listeners = UISpaceInfo.SaveActionListener.class),
        @EventConfig(listeners = UISpaceInfo.ChangeAvatarActionListener.class)
      }
)
public class UISpaceInfo extends UIForm {
  private final String SPACE_PRIORITY = "priority";
  //These priority variables should be set in Space.java model
  private final String PRIORITY_HIGH = "high";
  private final String PRIORITY_IMMEDIATE = "immediate";
  private final String PRIORITY_LOW = "low";
  private SpaceService spaceService = null;
  private final String POPUP_AVATAR_UPLOADER = "UIPopupAvatarUploader";
  
  public UISpaceInfo() throws Exception {
    addUIFormInput((UIFormStringInput)new UIFormStringInput("id","id",null).setRendered(false)).
    addUIFormInput(new UIFormStringInput("name","name",null).
                   addValidator(MandatoryValidator.class).
                   addValidator(ExpressionValidator.class, "^[\\p{L}][\\p{ASCII}]+$", "UISpaceInfo.msg.name-invalid").
                   addValidator(StringLengthValidator.class, 3, 30)).
    addUIFormInput(new UIFormTextAreaInput("description","description",null)
        .addValidator(StringLengthValidator.class, 0, 255));
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
  
  public void setValue(Space space) throws Exception {
    invokeGetBindingBean(space);
    //TODO: have to find the way to don't need the line code below. 
    getUIStringInput("tag").setValue(space.getTag());
  }

  protected String getImageSource() throws Exception {
    SpaceService spaceService = getSpaceService();
    String id = getUIStringInput("id").getValue();
    Space space = spaceService.getSpaceById(id);
    SpaceAttachment spaceAtt = (SpaceAttachment) space.getSpaceAttachment();
    if (spaceAtt != null) {
      return "/" + getPortalName()+"/rest/jcr/" + getRepository()+ "/" + spaceAtt.getWorkspace()
              + spaceAtt.getDataPath() + "/?rnd=" + System.currentTimeMillis();
    }
    return null;
  }
  
  private String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance() ;
    return pcontainer.getPortalContainerInfo().getContainerName() ;  
  }
  
  private String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;    
    return rService.getCurrentRepository().getConfiguration().getName() ;
  }
  
  static public class SaveActionListener extends EventListener<UISpaceInfo> {
    public void execute(Event<UISpaceInfo> event) throws Exception {
      UISpaceInfo uiSpaceInfo = event.getSource();
      SpaceService spaceService = uiSpaceInfo.getSpaceService();
      WebuiRequestContext requestContext = event.getRequestContext();
      String id = uiSpaceInfo.getUIStringInput("id").getValue();
      Space space = spaceService.getSpaceById(id);
      uiSpaceInfo.invokeSetBindingBean(space);
      spaceService.saveSpace(space, false);
      UIApplication uiApp = requestContext.getUIApplication();
      uiApp.addMessage(new ApplicationMessage("UISpaceInfo.msg.update-success", null, ApplicationMessage.INFO));
      SpaceUtils.updateWorkingWorkSpace();
    }
  }
  
  /**
   * Action trigger for editting avatar. An UIAvatarUploader popup should be displayed.
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
   * get {@SpaceService}
   * @return spaceService
   */
  public SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = getApplicationComponent(SpaceService.class);
    }
    return spaceService;
  }
}
