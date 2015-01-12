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

package org.exoplatform.social.portlet.basicProfile;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import juzu.Path;
import juzu.View;
import juzu.request.RenderContext;
import juzu.template.Template;

import org.exoplatform.social.portlet.userprofile.helpers.UserProfileHelper;

public class BasicProfile {
  
  @Inject
  @Path("index.gtmpl") Template index;
  
  @View
  public void index(RenderContext renderContext) {
    
    Map<String, Object> parameters = new HashMap<String, Object>();
    
    parameters.put("_ctx", UserProfileHelper.getContext(renderContext));
    parameters.put("profile", getProfileInfo());

    index.render(parameters);
  }

  protected Map<String, Object> getProfileInfo() {
    return UserProfileHelper.getDisplayProfileInfo(UserProfileHelper.getCurrentProfile());
  }
}



















