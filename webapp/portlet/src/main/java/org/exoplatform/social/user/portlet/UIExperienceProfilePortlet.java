/***************************************************************************
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.social.user.portlet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.utils.TimeConvertUtils;

@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/groovy/social/portlet/user/UIExperienceProfilePortlet.gtmpl"
)
public class UIExperienceProfilePortlet extends UIAbstractUserPortlet {
  final private static String DATE_FORMAT_MMDDYYYY = "MM/dd/yyyy";
  final private static String DISPLAY_FORMAT_EEDDYYYY = "EE dd, yyyy";
  final private static String EXPERIENCES_IS_CURRENT = Profile.EXPERIENCES_IS_CURRENT;
  public UIExperienceProfilePortlet() throws Exception {
  }

  protected boolean isOwner() {
    return currentProfile.getIdentity().getRemoteId().equals(Utils.getViewerRemoteId());
  }
  
  protected String getAboutMe() {
    String about = (String) currentProfile.getProperty("aboutMe");
    return StringUtils.isEmpty(about) ? "" : about;
  }

  public List<Map<String, String>> getExperience() throws Exception {
    List<Map<String, String>> pastExperiences = new ArrayList<Map<String, String>>();
    List<Map<String, Object>> experiences = (List<Map<String, Object>>) currentProfile.getProperty(Profile.EXPERIENCES);
    if (experiences != null) {
      Map<String, String> experience;
      for (Map<String, Object> map : experiences) {
        experience = new LinkedHashMap<String, String>();
        putData(map, experience, Profile.EXPERIENCES_COMPANY);
        putData(map, experience, Profile.EXPERIENCES_POSITION);
        putData(map, experience, Profile.EXPERIENCES_DESCRIPTION);
        putData(map, experience, Profile.EXPERIENCES_SKILLS);
        //
        putDate(map, experience, Profile.EXPERIENCES_START_DATE);
        putEndDate(map, experience);
        //
        pastExperiences.add(experience);
      }
    }
    return pastExperiences;
  }

  private void putData(Map<String, Object> srcExperience, Map<String, String> destExperience, String key) {
    String value = (String) srcExperience.get(key);
    if (value != null && value.trim().length() > 0) {
      destExperience.put(key, value);
    }
  }
  
  private void putDate(Map<String, Object> srcExperience, Map<String, String> destExperience, String key) {
    String value = getDateValue(srcExperience, key);
    if (value != null && value.trim().length() > 0) {
      destExperience.put(key, value);
    }
  }
  
  private String getDateValue(Map<String, Object> srcExperience, String key) {
    String value = (String) srcExperience.get(key);
    try {
      SimpleDateFormat sd = new SimpleDateFormat(DATE_FORMAT_MMDDYYYY, Locale.ENGLISH);
      Date date = sd.parse(value.trim());
      return TimeConvertUtils.convertXTimeAgo(date, DISPLAY_FORMAT_EEDDYYYY, TimeConvertUtils.MONTH);
    } catch (Exception e) {
      return value;
    }
  }

  private void putEndDate(Map<String, Object> srcExperience, Map<String, String> destExperience) {
    String key = Profile.EXPERIENCES_END_DATE;
    String value = getDateValue(srcExperience, key);
    if (value != null && value.trim().length() > 0) {
      destExperience.put(key, value);
    } else if (srcExperience.containsKey(EXPERIENCES_IS_CURRENT)
        && Boolean.valueOf(srcExperience.get(EXPERIENCES_IS_CURRENT) + "")) {
      destExperience.put(EXPERIENCES_IS_CURRENT, "true");
    }
  }
  
}
