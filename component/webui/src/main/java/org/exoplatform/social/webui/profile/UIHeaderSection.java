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

import java.util.ResourceBundle;

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.UserConfigurableValidator;

/**
 * Component is used for short user information (name, position) managing.<br>
 */

@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "war:/groovy/social/webui/profile/UIHeaderSection.gtmpl",
  events = {
    @EventConfig(listeners = UIHeaderSection.AddContactActionListener.class, phase = Phase.DECODE),
    @EventConfig(listeners = UIHeaderSection.AcceptContactActionListener.class, phase = Phase.DECODE),
    @EventConfig(listeners = UIHeaderSection.DenyContactActionListener.class, phase = Phase.DECODE),
    @EventConfig(listeners = UIProfileSection.EditActionListener.class, phase = Phase.DECODE),
    @EventConfig(listeners = UIProfileSection.CancelActionListener.class, phase = Phase.DECODE),
    @EventConfig(listeners = UIHeaderSection.SaveActionListener.class)
  }
)
public class UIHeaderSection extends UIProfileSection {

  /** Label for display invoke action */
  private static final String INVITATION_REVOKED_INFO = "UIHeaderSection.label.RevokedInfo";

  /** Label for display established invitation */
  private static final String INVITATION_ESTABLISHED_INFO = "UIHeaderSection.label.InvitationEstablishedInfo";

  /** Html attribute title. */
  private static final String HTML_ATTRIBUTE_TITLE   = "title";

  public static final String JOB_TITLE = "jobtitle";

  /**
   * Initializes components for header form.<br>
   */
  public UIHeaderSection() throws Exception {
    WebuiRequestContext requestContext = WebuiRequestContext.getCurrentInstance();
    ResourceBundle resourceBundle = requestContext.getApplicationResourceBundle();
    UIFormStringInput position = new UIFormStringInput(Profile.POSITION, Profile.POSITION, null);
    
    position.setHTMLAttribute(HTML_ATTRIBUTE_TITLE, resourceBundle.getString("UIHeaderSection.label.yourPosition"));
    addUIFormInput(position.addValidator(MandatoryValidator.class).addValidator(UserConfigurableValidator.class,
                        JOB_TITLE, UserConfigurableValidator.KEY_PREFIX + JOB_TITLE, false));
    setSubmitAction("return false;");
  }

  /**
   * Changes form into edit mode when edit button is clicked.<br>
   */
  public static class EditActionListener extends EventListener<UIHeaderSection> {
    @Override
    public void execute(Event<UIHeaderSection> event) throws Exception {
      UIHeaderSection uiHeader = event.getSource();
      uiHeader.setEditMode(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiHeader);
      uiHeader.setFirstLoad(false);
    }
  }

  /**
   * Changes form into edit mode when edit button is clicked.<br>
   */
  public static class CancelActionListener extends EventListener<UIHeaderSection> {
    @Override
    public void execute(Event<UIHeaderSection> event) throws Exception {
      UIHeaderSection uiHeader = event.getSource();
      uiHeader.setEditMode(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiHeader);
    }
  }

  /**
   * Stores profile information into database when form is submited.<br>
   */
  public static class SaveActionListener extends UIProfileSection.SaveActionListener {

    @Override
    public void execute(Event<UIProfileSection> event) throws Exception {
      super.execute(event);
      UIProfileSection sect = event.getSource();
      UIHeaderSection uiHeaderSect = (UIHeaderSection) sect;
      UIFormStringInput uiPosition = uiHeaderSect.getChildById(Profile.POSITION);
      String position = uiPosition.getValue();
      Profile p = uiHeaderSect.getProfile();
      p.setProperty(Profile.POSITION, sect.escapeHtml(position));
      Utils.getIdentityManager().updateProfile(p);
      sect.setFirstLoad(false);
    }
  }

  /**
   * Listens to add action then make request to invite person to make connection.<br>
   *   - Gets information of user is invited.<br>
   *   - Checks the relationship to confirm that there have not got connection yet.<br>
   *   - Saves the new connection.<br>
   *
   */
  public static class AddContactActionListener extends EventListener<UIHeaderSection> {
    @Override
    public void execute(Event<UIHeaderSection> event) throws Exception {
      UIHeaderSection uiHeaderSection = event.getSource();
      // Check if invitation is established by another user
      Relationship relationship = uiHeaderSection.getRelationship();
      if (relationship != null) {
        UIApplication uiApplication = event.getRequestContext().getUIApplication();
        uiApplication.addMessage(new ApplicationMessage(INVITATION_ESTABLISHED_INFO, null, ApplicationMessage.INFO));
        return;
      }
      Utils.getRelationshipManager().inviteToConnect(Utils.getViewerIdentity(), Utils.getOwnerIdentity());
      event.getRequestContext().addUIComponentToUpdateByAjax(uiHeaderSection);
    }
  }

  /**
   * Listens to accept actions then make connection to accepted person.<br>
   *   - Gets information of user who made request.<br>
   *   - Checks the relationship to confirm that there still got invited connection.<br>
   *   - Makes and Save the new relationship.<br>
   */
  public static class AcceptContactActionListener extends EventListener<UIHeaderSection> {
    @Override
    public void execute(Event<UIHeaderSection> event) throws Exception {
      UIHeaderSection uiHeaderSection = event.getSource();
      // Check if invitation is revoked or deleted by another user
      Relationship relationship = uiHeaderSection.getRelationship();
      if (relationship == null || relationship.getStatus().equals(Relationship.Type.IGNORED)) {
        UIApplication uiApplication = event.getRequestContext().getUIApplication();
        uiApplication.addMessage(new ApplicationMessage(INVITATION_REVOKED_INFO, null, ApplicationMessage.INFO));
        return;
      }
      Utils.getRelationshipManager().confirm(relationship.getReceiver(), relationship.getSender());
      Utils.updateWorkingWorkSpace();
    }
  }

  /**
   * Listens to deny action then delete the invitation.<br>
   *   - Gets information of user is invited or made request.<br>
   *   - Checks the relation to confirm that there have not got relation yet.<br>
   *   - Removes the current relation and save the new relation.<br> 
   *
   */
  public static class DenyContactActionListener extends EventListener<UIHeaderSection> {
    @Override
    public void execute(Event<UIHeaderSection> event) throws Exception {
      UIHeaderSection uiHeaderSection = event.getSource();
      // Check if invitation is revoked or deleted by another user
      UIApplication uiApplication = event.getRequestContext().getUIApplication();
      Relationship relationship = uiHeaderSection.getRelationship();
      if (relationship == null) {
        uiApplication.addMessage(new ApplicationMessage(INVITATION_REVOKED_INFO, null, ApplicationMessage.INFO));
        return;
      }
      Utils.getRelationshipManager().delete(relationship);
      Utils.updateWorkingWorkSpace();
    }
  }
  
  /**
   * Gets contact status between current user and identity that is checked.<br>
   * 
   * @return type of relationship status that equivalent the relationship.
   * 
   * @throws Exception
   */
  protected Relationship.Type getContactStatus() throws Exception {
    Relationship rl = getRelationship();
    if(rl == null) {
      return null;
    }
    return rl.getStatus();
  }
  
  /**
   * Gets relationship between current user and viewer identity.<br>
   * 
   * @return relationship.
   * 
   * @throws Exception
   */
  protected Relationship getRelationship() throws Exception {
    return Utils.getRelationshipManager().get(Utils.getOwnerIdentity(), Utils.getViewerIdentity());
  }
  
  /**
   * Gets position information from profile and set value into uicomponent.<br>
   *
   * @throws Exception
   */
  public void setValue() throws Exception {
    if (isFirstLoad() == false) {
      UIFormStringInput uiPosition = getChildById(Profile.POSITION);
      Profile profile = getProfile();
      String position = StringEscapeUtils.unescapeHtml((String) profile.getProperty(Profile.POSITION));
      position = (position == null ? "" : position);
      uiPosition.setValue(position);
      setFirstLoad(true);
    }
  }
  
 
}
