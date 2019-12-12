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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.utils.TimeConvertUtils;

@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "war:/groovy/social/portlet/user/UIExperienceProfilePortlet.gtmpl"
)
public class UIExperienceProfilePortlet extends UIAbstractUserPortlet {
  private static final String DATE_FORMAT_MMDDYYYY = "MM/dd/yyyy";
  private static final String DATE_FORMAT_MMMMDDYYYY = "MMMM dd, yyyy";

  public UIExperienceProfilePortlet() throws Exception {
  }

  protected String getAboutMe() {
    String about = (String) currentProfile.getProperty(Profile.ABOUT_ME);
    return StringUtils.isBlank(about) ? "" : StringEscapeUtils.escapeHtml4(about);
  }
  
  protected List<Map<String, String>> getExperiences() {
    List<Map<String, String>> experiences = UserProfileHelper.getSortedExperiences(currentProfile);
    List<Map<String, String>> escapedExperiences = new ArrayList<>();
    if(experiences != null) {
      for (Map<String, String> experience : experiences) {
        Map<String, String> escapedExperience = UserProfileHelper.escapeExperience(experience);
        String startDate = escapedExperience.get(Profile.EXPERIENCES_START_DATE);
        if (StringUtils.isNotBlank(startDate)) {
          escapedExperience.put(Profile.EXPERIENCES_START_DATE, convertDate(startDate));
        }
        String endDate = escapedExperience.get(Profile.EXPERIENCES_END_DATE);
        if (StringUtils.isNotBlank(endDate)) {
          escapedExperience.put(Profile.EXPERIENCES_END_DATE, convertDate(endDate));
        }
        escapedExperiences.add(escapedExperience);
      }
    }
    return escapedExperiences;
  }
  
  /**
   * Convert string date from format MM/dd/yyyy to MMMM dd, yyy
   * @param sDate string date input. 
   * @return
   */
  private String convertDate(String sDate) {
    try {
      SimpleDateFormat sd = new SimpleDateFormat(DATE_FORMAT_MMDDYYYY, Locale.ENGLISH);
      return TimeConvertUtils.getFormatDate(sd.parse(sDate), DATE_FORMAT_MMMMDDYYYY);
    } catch (Exception e) {
      return sDate;
    }
  }
}
