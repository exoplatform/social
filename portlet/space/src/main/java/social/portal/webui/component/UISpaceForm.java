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

import org.apache.velocity.runtime.parser.node.SetExecutor;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.SpaceUtils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.ExpressionValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;

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
public class UISpaceForm extends UIForm{

  final static private String SPACE_NAME = "spaceName";
//  final static private String SPACE_PUBLIC = "UISpacePublic";
  final static private String SPACE_PRIVATE = "UISpacePrivate";
  final static private String SPACE_HIDDEN = "UISpaceHidden";
  public UISpaceForm() throws Exception {
    addUIFormInput(new UIFormStringInput(SPACE_NAME,null,null).
                   addValidator(MandatoryValidator.class).
                   addValidator(ExpressionValidator.class, "^[\\p{L}][\\p{ASCII}]+$", "UISpaceForm.msg.name-invalid").
                   addValidator(StringLengthValidator.class, 3, 30));
    
//    List<SelectItemOption<String>> spacePublic = new ArrayList<SelectItemOption<String>>(1);
//    spacePublic.add(new SelectItemOption<String>(Space.PRIVATE));
//    UIFormRadioBoxInput uiRadioInput = new UIFormRadioBoxInput(SPACE_PUBLIC,"",spacePublic);
//    addUIFormInput(uiRadioInput);
    
    List<SelectItemOption<String>> spacePrivate = new ArrayList<SelectItemOption<String>>(1);
    spacePrivate.add(new SelectItemOption<String>(Space.PRIVATE));
    UIFormRadioBoxInput uiRadioInput = new UIFormRadioBoxInput(SPACE_PRIVATE, "", spacePrivate);
    addUIFormInput(uiRadioInput);
    
    List<SelectItemOption<String>> spaceHidden = new ArrayList<SelectItemOption<String>>(1);
    spaceHidden.add(new SelectItemOption<String>(Space.HIDDEN));
    UIFormRadioBoxInput uiRadioInputOne = new UIFormRadioBoxInput(SPACE_HIDDEN, "", spaceHidden);
    addUIFormInput(uiRadioInputOne);
    setId(this.getName());
    setSubmitAction(event("CreateSpace") + "; return false;");
  }
  
  static public class CreateSpaceActionListener extends EventListener<UISpaceForm> {
    public void execute(Event<UISpaceForm> event) throws Exception {
      UISpaceForm uiForm = event.getSource();
      UISpacesManage uiSpaceManage =  uiForm.getAncestorOfType(UISpacesManage.class);
      SpaceService spaceService = uiSpaceManage.getApplicationComponent(SpaceService.class);
      UIManageSpaceWorkingArea uiSpaceWorkingArea = uiSpaceManage.getChild(UIManageSpaceWorkingArea.class);
      
      WebuiRequestContext requestContext = event.getRequestContext();
      UIApplication uiApp = requestContext.getUIApplication();

      String spaceName = ((UIFormStringInput)uiForm.getChildById(SPACE_NAME)).getValue();      
      String shortName = SpaceUtils.cleanString(spaceName);
      if(shortName.length() < 3) {
        uiApp.addMessage(new ApplicationMessage("UISpaceForm.msg.length-invalid", null));
        return;
      }
      
      // get visibility of space
      String visibility = ((UIFormRadioBoxInput)uiForm.getChildById(SPACE_PRIVATE)).getValue();
      if(visibility.equals("")) 
        visibility = ((UIFormRadioBoxInput)uiForm.getChildById(SPACE_HIDDEN)).getValue();
      String creator = requestContext.getRemoteUser();
      
      Space space = new Space();
      try {
        space.setName(spaceName);
        space.setVisibility(visibility);
        spaceService.createSpace(space, creator);
        UIPopupWindow uiPopup = uiForm.getParent();
        uiPopup.setShow(false);
        requestContext.addUIComponentToUpdateByAjax(uiPopup);
        requestContext.addUIComponentToUpdateByAjax(uiSpaceWorkingArea);
      } catch (SpaceException e) {
        e.printStackTrace();
        if(e.getCode() == SpaceException.Code.SPACE_ALREADY_EXIST) {
          uiApp.addMessage(new ApplicationMessage("UISpaceForm.msg.space-exist", null));
          requestContext.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        }
      }
    }
  }
  
}
