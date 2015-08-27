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

import java.util.List;

import javax.portlet.MimeResponse;
import javax.portlet.ResourceRequest;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.json.JSONObject;

@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/social/portlet/user/UIMiniConnectionsPortlet.gtmpl",
  events = {
    @EventConfig(listeners = UIMiniConnectionsPortlet.RemoveConnectionActionListener.class)
  }
)
public class UIMiniConnectionsPortlet extends UIAbstractUserPortlet {
  protected final static int MAX_DISPLAY = 12;
  private static final String PROFILE_LOADING_RESOURCE = "profile-loading";
  private static final String SIZE_LOADING_RESOURCE = "size-loading";

  public UIMiniConnectionsPortlet() throws Exception {
  }
  
  @Override
  public void afterProcessRender(WebuiRequestContext context) {
    super.afterProcessRender(context);
    context.getJavascriptManager().getRequireJS()
           .require("SHARED/user-profile", "userprofile").addScripts("userprofile.loadingProfile('" + getId() + "');");
  }

  private int getAllSize() throws Exception {
    return Utils.getRelationshipManager().getConnectionsByFilter(currentProfile.getIdentity(), new ProfileFilter()).getSize();
  }

  @Override
  public void serveResource(WebuiRequestContext context) throws Exception {
    super.serveResource(context);
    ResourceRequest req = context.getRequest();
    MimeResponse res = context.getResponse();
    String resourceId = req.getResourceID();
    if (PROFILE_LOADING_RESOURCE.equals(resourceId)) {
      res.setContentType("text/html");
      //
      res.getWriter().write(profileListHTML());
    } else if (SIZE_LOADING_RESOURCE.equals(resourceId)) {
      res.setContentType("application/json");
      int size = getAllSize();
      JSONObject object = new JSONObject();
      object.put("size", size);
      object.put("showAll", (size > 0));
      //
      res.getWriter().write(object.toString());
    }
  }

  private String profileListHTML() throws Exception {
    StringBuilder html = new StringBuilder("");
    List<Identity> identities = Utils.getRelationshipManager().getLastConnections(currentProfile.getIdentity(), MAX_DISPLAY);
    for (Identity identity : identities) {
      ProfileBean profile = new ProfileBean(identity);
      html.append("<a href=\"").append(profile.getProfileURL()).append("\" class=\"avatarXSmall\" data-link=\"")
          .append(event("RemoveConnection")).append("\">\n  <img alt=\"").append(profile.getDisplayName())
          .append("\" src=\"").append(profile.getAvatarURL()).append("\"/>\n</a>\n");
    }
    return html.toString();
  }

  public static class RemoveConnectionActionListener extends EventListener<UIMiniConnectionsPortlet> {
    @Override
    public void execute(Event<UIMiniConnectionsPortlet> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource());
    }
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
