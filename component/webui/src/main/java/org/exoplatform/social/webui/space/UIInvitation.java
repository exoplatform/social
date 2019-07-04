/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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
package org.exoplatform.social.webui.space;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.form.UIFormStringInput;

import java.util.*;

@ComponentConfig(
    template = "war:/groovy/social/webui/space/UIInvitation.gtmpl"
)

public class UIInvitation extends UIContainer {
  private final String USERS_SPACES = "users-spaces";
  private final String SPACE_PREFIX = "space::";
  private List<String> notFoundInvitees;
  private Map<String, String> invitees;
  private SpaceService spaceService;
  private IdentityManager identityManager;

  /**
   * constructor
   */
  public UIInvitation() {
    notFoundInvitees = new ArrayList<>();
    invitees = new HashMap<>();
    UIFormStringInput uiFormStringInput = new UIFormStringInput(USERS_SPACES, null, null);
    addChild(uiFormStringInput);
  }

  public void setInvitees(String invitees) {
    Map<String, String> inviteeNames = new HashMap<>();
    if (StringUtils.isNotBlank(invitees)) {
      SpaceService spaceService = getSpaceService();
      IdentityManager identityManager = getIdentityManager();
      String[] invitedList = invitees.split(",");
      String userId = ConversationState.getCurrent().getIdentity().getUserId();
      for (String invited : invitedList) {
        if (invited.equals(userId)) {
          continue;
        }
        Space space = spaceService.getSpaceByDisplayName(invited);
        if (space != null) {
          inviteeNames.putIfAbsent(SPACE_PREFIX + space.getPrettyName(), invited);
        } else {
          Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, invited, true);
          if (identity == null) {
            notFoundInvitees.add(invited);
          } else {
            Profile profile = identity.getProfile();
            inviteeNames.putIfAbsent(invited, profile.getFullName());
          }
        }
      }
    }
    this.invitees = inviteeNames;
  }

  public Map<String,String> getInvitees() {
    return invitees;
  }

  /**
   * gets selected identities from input
   */
  public List<Identity> getSelectedIdentities() {
    notFoundInvitees = new ArrayList<>();
    UIFormStringInput uiFormStringInput = getChild(UIFormStringInput.class);
    String input = uiFormStringInput.getValue();
    return getIdentities(input);
  }

  public String getRestURL() {
    StringBuilder builder = new StringBuilder();
    builder.append("/").append(PortalContainer.getCurrentRestContextName()).append("/social/people/suggest.json?");
    builder.append("currentUser=").append(RequestContext.getCurrentInstance().getRemoteUser());
    builder.append("&typeOfRelation=").append("user_to_invite");
    return builder.toString();
  }

  public List<String> getNotFoundInvitees() {
    return notFoundInvitees;
  }

  private List<Identity> getIdentities(String input) {
    List<Identity> identityList = new ArrayList<>();
    if (input != null) {
      String[] invitedList = input.split(",");
      IdentityManager identityManager = getIdentityManager();
      for (String invited : invitedList) {
        // If it's a space
        if (invited.startsWith(SPACE_PREFIX)) {
          String spaceName = invited.substring(SPACE_PREFIX.length());
          Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, spaceName, false);
          identityList.add(spaceIdentity);
        } else {
          Identity identity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, invited, false);
          if (identity == null) {
            notFoundInvitees.add(invited);
            continue;
          }
          identityList.add(identity);
        }
      }
    }
    return identityList;
  }

  /**
   * Gets spaceService.
   *
   * @return spaceService
   * @see SpaceService
   */
  private SpaceService getSpaceService() {
    if (spaceService == null) {
      spaceService = getApplicationComponent(SpaceService.class);
    }
    return spaceService;
  }

  /**
   * Gets identityManager.
   *
   * @return identityManager
   * @see IdentityManager
   */
  private IdentityManager getIdentityManager() {
    if (identityManager == null) {
      identityManager = getApplicationComponent(IdentityManager.class);
    }
    return identityManager;
  }
}
