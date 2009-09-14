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

import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Feb 19, 2009          
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
        @EventConfig(listeners = UISpacePermission.SaveActionListener.class)
      }
)
public class UISpacePermission extends UIForm {

  final static private String SPACE_VISIBILITY = "Visibility";
  final static private String SPACE_REGISTRATION = "Registration";
  private String spaceId;
  
  public UISpacePermission() throws Exception {
    List<SelectItemOption<String>> spaceVisibility = new ArrayList<SelectItemOption<String>>(3);
    spaceVisibility.add(new SelectItemOption<String>(Space.PUBLIC));
    spaceVisibility.add(new SelectItemOption<String>(Space.PRIVATE));
    spaceVisibility.add(new SelectItemOption<String>(Space.HIDDEN));
    UIFormRadioBoxInput uiRadioVisibility = new UIFormRadioBoxInput(SPACE_VISIBILITY,null,spaceVisibility);
    addUIFormInput(uiRadioVisibility);
    
    List<SelectItemOption<String>> spaceRegistration = new ArrayList<SelectItemOption<String>>(3);
    spaceRegistration.add(new SelectItemOption<String>(Space.OPEN));
    spaceRegistration.add(new SelectItemOption<String>(Space.VALIDATION));
    spaceRegistration.add(new SelectItemOption<String>(Space.CLOSE));
    UIFormRadioBoxInput uiRadioRegistration = new UIFormRadioBoxInput(SPACE_REGISTRATION, null, spaceRegistration);
    addUIFormInput(uiRadioRegistration);
  }
  
  public void setValue(Space space) throws Exception {
    String visibility = space.getVisibility();
    ((UIFormRadioBoxInput)getChildById(SPACE_VISIBILITY)).setValue(visibility);
    String registration = space.getRegistration();
    ((UIFormRadioBoxInput)getChildById(SPACE_REGISTRATION)).setValue(registration);
    spaceId = space.getId();
  }
  
  static public class SaveActionListener extends EventListener<UISpacePermission> {
    public void execute(Event<UISpacePermission> event) throws Exception {
      UISpacePermission uiSpacePermission = event.getSource();
      SpaceService spaceSrc = uiSpacePermission.getApplicationComponent(SpaceService.class);
      WebuiRequestContext requestContext = event.getRequestContext();
      String visibility = ((UIFormRadioBoxInput)uiSpacePermission.getChildById(SPACE_VISIBILITY)).getValue();
      String registration = ((UIFormRadioBoxInput)uiSpacePermission.getChildById(SPACE_REGISTRATION)).getValue();
      Space space = spaceSrc.getSpaceById(uiSpacePermission.spaceId);
      space.setVisibility(visibility);
      space.setRegistration(registration);
      spaceSrc.saveSpace(space, false);
      UIApplication uiApp = requestContext.getUIApplication();
      uiApp.addMessage(new ApplicationMessage("UISpacePermission.msg.update-success", null, ApplicationMessage.INFO));
      requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      requestContext.addUIComponentToUpdateByAjax(uiSpacePermission);
    }
  }
}
