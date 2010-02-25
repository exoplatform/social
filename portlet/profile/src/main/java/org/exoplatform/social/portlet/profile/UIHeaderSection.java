/**
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
package org.exoplatform.social.portlet.profile;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.ExpressionValidator;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.StringLengthValidator;
/**
 * Component is used for short user information (name, position) managing.<br> 
 *
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/groovy/portal/webui/component/UIHeaderSection.gtmpl",
    events = {
        @EventConfig(listeners = UIProfileSection.EditActionListener.class, phase=Phase.DECODE),
        @EventConfig(listeners = UIProfileSection.CancelActionListener.class, phase=Phase.DECODE),
        @EventConfig(listeners = UIHeaderSection.SaveActionListener.class)
    }
)

public class UIHeaderSection extends UIProfileSection {
  /** POSITION. */
  final public static String POSITION = "position";
  
  /** REGEX EXPRESSION. */
  final public static String REGEX_EXPRESSION = "^\\p{L}[\\p{L}\\d._,\\s]+\\p{L}$";
  
  /** INVALID CHARACTER MESSAGE. */
  final public static String INVALID_CHAR_MESSAGE = "UIHeaderSection.msg.Invalid-char";
  
  /** 
   * Initializes components for header form.<br>
   */
  public UIHeaderSection() throws Exception { 
    addUIFormInput(new UIFormStringInput(POSITION, POSITION, null)
                   .addValidator(MandatoryValidator.class)
                   .addValidator(StringLengthValidator.class, 3, 30)
                   .addValidator(ExpressionValidator.class, REGEX_EXPRESSION, INVALID_CHAR_MESSAGE));
  }
  
  /**
   * Changes form into edit mode when edit button is clicked.<br>
   *
   */
  public static class EditActionListener extends EventListener<UIHeaderSection> {
    public void execute(Event<UIHeaderSection> event) throws Exception {
      UIHeaderSection uiHeader = event.getSource();
      uiHeader.setEditMode(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiHeader);
    }
  }  
  
  /**
   * Changes form into edit mode when edit button is clicked.<br>
   *
   */
  public static class CancelActionListener extends EventListener<UIHeaderSection> {
    public void execute(Event<UIHeaderSection> event) throws Exception {
      UIHeaderSection uiHeader = event.getSource();
      uiHeader.setEditMode(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiHeader);
    }
  }  
  
  /**
   * Stores profile information into database when form is submited.<br>
   *
   */
  public static class SaveActionListener extends UIProfileSection.SaveActionListener {

    public void execute(Event<UIProfileSection> event) throws Exception {
      super.execute(event);
      
      UIProfileSection sect = event.getSource();       
      UIHeaderSection uiHeaderSect = (UIHeaderSection)sect;
      
      UIFormStringInput uiPosition = uiHeaderSect.getChildById(POSITION);
      String position = uiPosition.getValue();
      
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      IdentityManager im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
      Profile p = uiHeaderSect.getProfile(true);
      p.setProperty(POSITION, position);
      
      im.saveProfile(p);      
    }
  }
  
  /**
   * Gets position information from profile and set value into uicomponent.<br>
   * 
   * @throws Exception
   */
  public void setValue() throws Exception {
    UIFormStringInput uiPosition = getChildById(POSITION);
    Profile profile = getProfile(false);
    String position = (String) profile.getProperty(POSITION);
    position = (position == null ? "": position);
    uiPosition.setValue(position);
  }
  
}
