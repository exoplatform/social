/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.user.portlet;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/social/portlet/user/UIMiniConnectionsPortlet.gtmpl",
  events = {
    @EventConfig(listeners = UIMiniConnectionsPortlet.RemoveConnectionActionListener.class)
  }
)
public class UIMiniConnectionsPortlet extends UIAbstractUserPortlet {
  protected final static int MAX_DISPLAY = 12;
  private int allSize = 0;

  public UIMiniConnectionsPortlet() throws Exception {
  }

  public static class RemoveConnectionActionListener extends EventListener<UIMiniConnectionsPortlet> {
    @Override
    public void execute(Event<UIMiniConnectionsPortlet> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource());
    }
  }
  
  protected List<ProfileBean> loadPeoples() throws Exception {
    ListAccess<Identity> listAccess = Utils.getRelationshipManager().getConnectionsByFilter(currentProfile.getIdentity(), new ProfileFilter());
    allSize = listAccess.getSize();
    List<Identity> identities = Utils.getRelationshipManager().getLastConnections(currentProfile.getIdentity(), MAX_DISPLAY);
    List<ProfileBean> profileBeans = new ArrayList<ProfileBean>();
    for (Identity identity : identities) {
      profileBeans.add(new ProfileBean(identity));
    }
    return profileBeans;
  }
  
  protected int getAllSize() {
    return allSize;
  }
  
  protected class ProfileBean {
    private final String avatarURL;
    private final String displayName;
    private final String profileURL;
    private final String userId;

    public ProfileBean(Identity identity) {
      this.userId = identity.getRemoteId();
      //
      Profile profile = identity.getProfile();
      this.displayName = profile.getFullName();
      this.profileURL = profile.getUrl();
      String avatarURL = profile.getAvatarUrl();
      if (UserProfileHelper.isEmpty(avatarURL) || avatarURL.equalsIgnoreCase("null")) {
        avatarURL = "/eXoSkin/skin/images/system/UserAvtDefault.png";
      }
      this.avatarURL = avatarURL;
    }
    public String getUserId() {
      return userId;
    }
    public String getAvatarURL() {
      return avatarURL;
    }
    public String getDisplayName() {
      return displayName;
    }
    public String getProfileURL() {
      return profileURL;
    }
  }
}
