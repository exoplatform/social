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

import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
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
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
        @EventConfig(listeners = UISpaceInfo.SaveActionListener.class)
      }
)
public class UISpaceInfo extends UIForm {

  
  public UISpaceInfo() throws Exception {
    addUIFormInput((UIFormStringInput)new UIFormStringInput("id","id",null).setRendered(false)).
    addUIFormInput(new UIFormStringInput("name","name",null).addValidator(MandatoryValidator.class)).
    addUIFormInput(new UIFormTextAreaInput("description","description",null)
        .addValidator(StringLengthValidator.class, 0, 255)).
    addUIFormInput(new UIFormStringInput("tag","tag",null));
  }
  
  public void setValue(Space space) throws Exception {
    invokeGetBindingBean(space);
  }
  
  static public class SaveActionListener extends EventListener<UISpaceInfo> {
    public void execute(Event<UISpaceInfo> event) throws Exception {
      UISpaceInfo uiSpaceInfo = event.getSource();
      SpaceService spaceSrc = uiSpaceInfo.getApplicationComponent(SpaceService.class);
      String id = uiSpaceInfo.getUIStringInput("id").getValue();
      Space space = spaceSrc.getSpaceById(id);
      uiSpaceInfo.invokeSetBindingBean(space);
      spaceSrc.saveSpace(space, false);
      UISpaceSetting uiSpaceSetting = uiSpaceInfo.getAncestorOfType(UISpaceSetting.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSpaceSetting);
    }
  }
}
