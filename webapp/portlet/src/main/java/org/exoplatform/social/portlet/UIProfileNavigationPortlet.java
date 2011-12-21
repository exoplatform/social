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
package org.exoplatform.social.portlet;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.webui.UIAvatarUploader;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.web.controller.QualifiedName;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Manages the navigation of connections.<br>
 *   - Decides which node is current selected.<br>
 *   - Checked is view by current user or by another.<br>
 */

@ComponentConfig(
 lifecycle = UIApplicationLifecycle.class,
 template = "app:/groovy/social/portlet/UIProfileNavigationPortlet.gtmpl",
 events = {
   @EventConfig(listeners = UIProfileNavigationPortlet.ChangePictureActionListener.class)
 }
)
public class UIProfileNavigationPortlet extends UIPortletApplication {
  private final String POPUP_AVATAR_UPLOADER = "UIPopupAvatarUploader";
  
  /**
   * Default Constructor.<br>
   * 
   * @throws Exception
   */
  public UIProfileNavigationPortlet() throws Exception {
    UIPopupWindow uiPopup = createUIComponent(UIPopupWindow.class, null, POPUP_AVATAR_UPLOADER);
	uiPopup.setWindowSize(510, 0);
	addChild(uiPopup);
  }

  /**
   * Returns the current selected node.<br>
   * 
   * @return selected node.
   */
  public String getSelectedNode() {
    PortalRequestContext pcontext = Util.getPortalRequestContext();
    
    String path = pcontext.getControllerContext().getParameter(QualifiedName.parse("gtn:path"));
    
    if (path.contains("/")) {
     return path.split("/")[0]; 
    }
    return path;
  }
  
  /**
   * Gets relationship between current user and viewer identity.<br>
   * 
   * @return relationship.
   * 
   * @throws Exception
   */
  public Relationship getRelationship() throws Exception {
    return Utils.getRelationshipManager().get(Utils.getOwnerIdentity(), Utils.getViewerIdentity());
  }

  /**
   * Action trigger for editting picture. An UIAvatarUploader popup should be displayed.
   * @since 1.2.2
   */
  public static class ChangePictureActionListener extends EventListener<UIProfileNavigationPortlet> {

    @Override
    public void execute(Event<UIProfileNavigationPortlet> event) throws Exception {
      UIProfileNavigationPortlet uiProfileNavigation = event.getSource();
      UIPopupWindow uiPopup = uiProfileNavigation.getChild(UIPopupWindow.class);
      UIAvatarUploader uiAvatarUploader = uiProfileNavigation.createUIComponent(UIAvatarUploader.class, null, null);
      uiPopup.setUIComponent(uiAvatarUploader);
      uiPopup.setShow(true);
    }
  }
}
