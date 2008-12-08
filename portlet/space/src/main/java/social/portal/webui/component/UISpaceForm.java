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


import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Sep 07, 2008          
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/groovy/portal/webui/uiform/UISpaceForm.gtmpl",
    events = {
        @EventConfig(listeners = UISpaceForm.CreateSpaceActionListener.class )
      }
)
public class UISpaceForm extends UIForm implements UIPopupComponent{

  final static private String SPACE_NAME = "spaceName";
  public UISpaceForm() throws Exception {
    addUIFormInput(new UIFormStringInput(SPACE_NAME,SPACE_NAME,null).addValidator(MandatoryValidator.class));
  }
  
  static public class CreateSpaceActionListener extends EventListener<UISpaceForm> {
    public void execute(Event<UISpaceForm> event) throws Exception {
      UISpaceForm uiForm = event.getSource();
      UIManageSpacesPortlet uiPorlet = uiForm.getAncestorOfType(UIManageSpacesPortlet.class);
      SpaceService spaceService = uiPorlet.getApplicationComponent(SpaceService.class);

      WebuiRequestContext requestContext = event.getRequestContext();
      UIApplication uiApp = requestContext.getUIApplication();

      String spaceName = ((UIFormStringInput)uiForm.getChildById(SPACE_NAME)).getValue();
      String creator = requestContext.getRemoteUser();

      try {
        spaceService.createSpace(spaceName, creator);
        uiForm.getAncestorOfType(UIPopupContainer.class).deActivate();
        requestContext.addUIComponentToUpdateByAjax(uiPorlet);        
      } catch (SpaceException e) {
        if(e.getCode() == SpaceException.Code.SPACE_ALREADY_EXIST) {
          uiApp.addMessage(new ApplicationMessage("UISpaceForm.msg.space-exist", null));
          requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        }
      }
    }
  }
  

  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
    
}
