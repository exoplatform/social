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

package org.exoplatform.social.portlet.miniConnections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.inject.Inject;

import juzu.Path;
import juzu.View;
import juzu.request.RenderContext;
import juzu.template.Template;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.portlet.userprofile.helpers.UserProfileHelper;
import org.exoplatform.social.webui.Utils;

public class MiniConnections {
  private static final Log LOG = ExoLogger.getLogger(MiniConnections.class);
  
  private final static int MAX_DISPLAY = 12;
  private final static String DEFAULT_AVATAR_URL = "/social-resources/skin/images/ShareImages/UserAvtDefault.png";
  
  @Inject
  @Path("index.gtmpl") Template index;
  
  @Inject
  ResourceBundle bundle;  
  
  private Locale locale = Locale.ENGLISH;
  private int allSize = 0;
  
  @View
  public void index(RenderContext renderContext) {
    
    if (renderContext != null) {
      locale = renderContext.getUserContext().getLocale();
    }
    if (bundle == null) {
      bundle = renderContext.getApplicationContext().resolveBundle(locale);
    }
    
    Map<String, Object> parameters = new HashMap<String, Object>();
    Identity identity = Utils.getOwnerIdentity(true);
    
    if (identity != null) {
      parameters.put("_ctx", UserProfileHelper.getContext(bundle));
      try {
        parameters.put("profiles", loadPeoples());
      } catch (Exception e) {
        LOG.error("Could not load profile." + e);
      }
      parameters.put("size", getAllSize());
      parameters.put("MAX_DISPLAY", MAX_DISPLAY);
      parameters.put("isOwner", Utils.isOwner());
      parameters.put("currentRemoteId", identity.getRemoteId());
    }

    index.render(parameters);
  }
  
  protected List<ProfileBean> loadPeoples() throws Exception {
    ListAccess<Identity> listAccess = Utils.getRelationshipManager()
        .getConnectionsByFilter(UserProfileHelper.getCurrentProfile().getIdentity(), new ProfileFilter());
    Identity[] identities = listAccess.load(0, MAX_DISPLAY);
    allSize = listAccess.getSize();
    List<ProfileBean> profileBeans = new ArrayList<ProfileBean>();
    for (int i = 0; i < identities.length; i++) {
      profileBeans.add(new ProfileBean(identities[i]));
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
        avatarURL = DEFAULT_AVATAR_URL;
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
