package org.exoplatform.social.user.portlet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.webui.application.WebuiRequestContext;

public class UserProfileHelper {
  final public static String KEY = "key";
  final public static String VALUE = "value";
  final public static String URL_KEY = "url";

  /**
   * @param s
   * @return
   */
  public static boolean isEmpty(String s) {
    return s == null || s.trim().length() == 0;
  }

  /**
   * @param currentProfile
   * @return
   * @throws Exception
   */
  public static List<Map<String, String>> getDisplayExperience(Profile currentProfile) throws Exception {
    List<Map<String, String>> pastExperiences = new ArrayList<Map<String, String>>();
    List<Map<String, String>> lastExperiences = new ArrayList<Map<String, String>>();
    List<Map<String, String>> experiences = getMultiValues(currentProfile, Profile.EXPERIENCES);
    if (experiences != null) {
      for (Map<String, String> srcExperience : experiences) {
        if(isCurrent(srcExperience)) {
          pastExperiences.add(theExperienceData(srcExperience, true));
        } else {
          lastExperiences.add(theExperienceData(srcExperience, false));
        }
      }
      pastExperiences.addAll(lastExperiences);
    }
    return pastExperiences;
  }
  
  public static List<Map<String, String>> getMultiValues(Profile currentProfile, String key) {
    return (List<Map<String, String>>) currentProfile.getProperty(key);
  }

  public static List<String> getURLValues(Profile currentProfile) {
    List<Map<String, String>> mapUrls = getMultiValues(currentProfile, Profile.CONTACT_URLS);
    List<String> urls = new ArrayList<String>();
    if (mapUrls != null) {
      for (Map<String, String> map : mapUrls) {
        urls.add(map.get(VALUE));
      }
    }
    return urls;
  }

  private static Map<String, String> theExperienceData(Map<String, String> srcExperience, boolean isCurrent) {
    Map<String, String> experience = new LinkedHashMap<String, String>();
    putExperienceData(srcExperience, experience, Profile.EXPERIENCES_COMPANY);
    putExperienceData(srcExperience, experience, Profile.EXPERIENCES_POSITION);
    putExperienceData(srcExperience, experience, Profile.EXPERIENCES_DESCRIPTION);
    putExperienceData(srcExperience, experience, Profile.EXPERIENCES_SKILLS);
    //
    putExperienceData(srcExperience, experience, Profile.EXPERIENCES_START_DATE);
    if(isCurrent) {
      experience.put(Profile.EXPERIENCES_IS_CURRENT, "true");
    } else {
      putExperienceData(srcExperience, experience, Profile.EXPERIENCES_END_DATE);
    }
    return experience;
  }
  
  private static boolean isCurrent(Map<String, String> srcExperience) {
    return Boolean.valueOf(String.valueOf(srcExperience.get(Profile.EXPERIENCES_IS_CURRENT)));
  }

  private static void putExperienceData(Map<String, String> srcExperience, Map<String, String> destExperience, String key) {
    String value = srcExperience.get(key);
    if (!isEmpty(value)) {
      destExperience.put(key, StringEscapeUtils.unescapeHtml(value));
    }
  }

  public static String getLabel(WebuiRequestContext context, String key) {
    if (context == null) {
      context = WebuiRequestContext.getCurrentInstance();
    }
    ResourceBundle res = context.getApplicationResourceBundle();
    try {
      return res.getString(key);
    } catch (Exception e) {
      return (key.indexOf(".") > 0) ? key.substring(key.lastIndexOf(".") + 1) : key;
    }
  }
}
