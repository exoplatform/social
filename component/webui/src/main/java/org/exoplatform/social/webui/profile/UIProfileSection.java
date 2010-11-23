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
package org.exoplatform.social.webui.profile;

import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.webui.URLUtils;
import org.exoplatform.web.CacheUserProfileFilter;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Manages profile informations and actions relate to manage profile.<br>
 *
 * Modified : dang.tung
 *          tungcnw@gmail.com
 * Aug 11, 2009          
 */

@ComponentConfig(
  lifecycle = UIFormLifecycle.class
)
public abstract class UIProfileSection extends UIForm {
  /** The isEditMode is used for check the view mode. */
  private boolean isEditMode;
  
  /** Checks is multi part or not. */
  private boolean isMultipart = false;
  
  /** Store current property.*/
  private String currentProperty;
  
  /**
   * Gets profile.<br>
   * 
   * @param forceReload
   * 
   * @return profile.
   * 
   * @throws Exception
   */
  public Profile getProfile(boolean forceReload) throws Exception {
    UIProfile uiProfile = this.getAncestorOfType(UIProfile.class);
    return uiProfile.getProfile(forceReload);
  }

  /**
   * Checks the current display of title bar is can be edit.<br>
   * 
   * @return true if title bar is in edit mode.
   */
  public boolean isEditMode() {
    return this.isEditMode;
  }

  /**
   * Sets the edit mode for form.<br>
   * 
   * @param editMode
   */
  public void setEditMode(boolean editMode) {
    this.isEditMode = editMode;
  }

  /**
   * Checks the current user is right edit permission.<br>
   * 
   * @return true if current user has permission.
   */
  public boolean isEditable() {
    UIProfile pp = this.getAncestorOfType(UIProfile.class);
    return pp.isEditable();
  }

  /**
   * Checks is multiple parts or not.<br>
   * 
   * @return true if is multiple part.
   */
  @Override
  public boolean isMultipart() {
    return isMultipart;
  }

  /**
   * Sets multiple part.<br>
   * 
   * @param multipart
   */
  public void setMultipart(boolean multipart) {
    isMultipart = multipart;
  }

  /**
   * Gets current property.<br>
   * 
   * @return currentProperty
   */
  public String getCurrentProperty() {
    return currentProperty;
  }

  /**
   * Sets current property.<br>
   * 
   * @param currentProperty
   */
  public void setCurrentProperty(String currentProperty) {
    this.currentProperty = currentProperty;
  }

  /**
   * Get user
   *
   * @return
   * @throws Exception
   */
  public User getViewUser() throws Exception {
    String currentUserName = RequestContext.getCurrentInstance().getRemoteUser();
    String currentViewer = URLUtils.getCurrentUser();
    if ((currentViewer != null) && (!currentViewer.equals(currentUserName))) {
      UserHandler userHandler = getApplicationComponent(OrganizationService.class).getUserHandler();
      return userHandler.findUserByName(currentViewer);
    }
    ConversationState state = ConversationState.getCurrent();
    return (User) state.getAttribute(CacheUserProfileFilter.USER_PROFILE);
  }

  /**
   * Listens to edit event and changes the form to edit mode.<br>
   *
   */
  public static class EditActionListener extends EventListener<UIProfileSection> {

    @Override
    public void execute(Event<UIProfileSection> event) throws Exception {
      UIProfileSection sect = event.getSource();
      sect.setEditMode("true".equals(event.getRequestContext().getRequestParameter(OBJECTID)));
      event.getRequestContext().addUIComponentToUpdateByAjax(sect);
    }
  }

  /**
   * Listens to save event and change form to non edit mode.<br> 
   *
   */
  public static class SaveActionListener extends EventListener<UIProfileSection> {

    @Override
    public void execute(Event<UIProfileSection> event) throws Exception {
      UIProfileSection sect = event.getSource();
      sect.setEditMode(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(sect);
    }
  }

  /**
   * Listens to cancel event and change the form to non edit mode.<br>
   *
   */
  public static class CancelActionListener extends EventListener<UIProfileSection> {

    @Override
    public void execute(Event<UIProfileSection> event) throws Exception {
      UIProfileSection sect = event.getSource();
      sect.setEditMode(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(sect);
    }
  }
}
