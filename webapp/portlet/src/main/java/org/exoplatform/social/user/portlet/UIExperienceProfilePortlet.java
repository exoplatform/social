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
import java.util.Map;

import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/social/portlet/user/UIExperienceProfilePortlet.gtmpl"
)
public class UIExperienceProfilePortlet extends UIAbstractUserPortlet {
  final protected static String EXPERIENCES_IS_CURRENT = Profile.EXPERIENCES_IS_CURRENT;

  public UIExperienceProfilePortlet() throws Exception {
  }

  protected String getAboutMe() {
    String about = (String) currentProfile.getProperty(Profile.ABOUT_ME);
    return UserProfileHelper.isEmpty(about) ? "" : about;
  }
  
  protected List<Map<String, String>> getExperience() throws Exception {
    return UserProfileHelper.getDisplayExperience(currentProfile);
  }
}
