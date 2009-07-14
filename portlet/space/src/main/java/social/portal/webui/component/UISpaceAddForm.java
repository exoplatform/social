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
package social.portal.webui.component;

import org.exoplatform.social.space.Space;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.organization.account.UIGroupSelector;

/**
 * UIAddSpaceForm to create new space
 * 
 * Created by The eXo Platform SAS
 * Author : hoatle
 *          hoatlevan@gmail.com
 * Jun 29, 2009  
 */
@ComponentConfig(
   lifecycle = UIFormLifecycle.class,
   template =  "system:/groovy/webui/form/UIFormTabPane.gtmpl",
   events = {
    @EventConfig(listeners = UISpaceAddForm.CreateActionListener.class),
    @EventConfig(listeners = UISpaceAddForm.ToogleUseGroupActionListener.class),
    @EventConfig(listeners = UISpaceAddForm.SelectGroupActionListener.class)
  }
)
public class UISpaceAddForm extends UIFormTabPane {
  private final String SPACE_SETTINGS = "Settings";
  private final String SPACE_VISIBILITY = "Visibility";
  private final String SPACE_GROUP_BOUND = "GroupBound";
  
  
  public UISpaceAddForm() throws Exception {
    super("UIAddSpaceForm");
    UIFormInputSet uiSpaceSettings = new UISpaceSettings(SPACE_SETTINGS);
    addChild(uiSpaceSettings);
    
    UIFormInputSet uiSpaceVisibility = new UISpaceVisibility(SPACE_VISIBILITY);
    addChild(uiSpaceVisibility);
    
    addChild(UISpaceGroupBound.class, null, SPACE_GROUP_BOUND);
    
    setActions(new String[] {"Create"});
    setSelectedTab(1);
  }
  
  static public class CreateActionListener extends EventListener<UISpaceAddForm> {

    @Override
    public void execute(Event<UISpaceAddForm> event) throws Exception {
    	//TODO: hoatle Create new group or binding to existing group if remote user is manager of that group
      UISpaceAddForm uiAddForm = event.getSource();
      Space space = new Space();
      uiAddForm.invokeSetBindingBean(space);
    }
    
  }
  
  static  public class SelectGroupActionListener extends EventListener<UISpaceAddForm> {   
    public void execute(Event<UISpaceAddForm> event) throws Exception {
     WebuiRequestContext context = event.getRequestContext();
     String groupId = context.getRequestParameter(OBJECTID);
     UISpaceAddForm uiAddForm = event.getSource();
     UISpaceGroupBound uiGroupBound = uiAddForm.getChild(UISpaceGroupBound.class);
     UIFormInputInfo uiFormInputInfo = uiGroupBound.getChild(UIFormInputInfo.class);
     uiFormInputInfo.setValue(groupId);
    }
  }

  static public class ToogleUseGroupActionListener extends EventListener<UISpaceAddForm> {

    @SuppressWarnings("unchecked")
    @Override
    public void execute(Event<UISpaceAddForm> event) throws Exception {
      UISpaceAddForm uiSpaceAddForm = event.getSource();
      UISpaceGroupBound uiSpaceGroupBound = uiSpaceAddForm.getChild(UISpaceGroupBound.class);
      UIFormCheckBoxInput<Boolean> uiUseExistingGroup = uiSpaceGroupBound.getChild(UIFormCheckBoxInput.class);
      if (uiUseExistingGroup.isChecked()) {
        UIPopupWindow uiPopup = uiSpaceGroupBound.getChild(UIPopupWindow.class);
        UIGroupSelector uiGroupSelector = uiSpaceAddForm.createUIComponent(UIGroupSelector.class, null, null);
        uiPopup.setUIComponent(uiGroupSelector);
        uiPopup.setShowMask(true);
        uiPopup.setShow(true);
      } else {
    	  UIFormInputInfo uiFormInputInfo = uiSpaceGroupBound.getChild(UIFormInputInfo.class);
    	  uiFormInputInfo.setValue(null);
      }
    }
    
  }
  

}
