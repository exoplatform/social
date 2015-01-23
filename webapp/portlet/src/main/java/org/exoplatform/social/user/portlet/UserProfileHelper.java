package org.exoplatform.social.user.portlet;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.json.JSONObject;

public class UserProfileHelper {
  final public static String KEY = "key";
  final public static String VALUE = "value";
  final public static String URL_KEY = "url";
  final public static String DEFAULT_PROTOCOL = "http://";

  enum IconClass {
    DEFAULT("", ""),
    GTALK("gtalk", "uiIconSocGtalk"),
    MSN("msn", "uiIconSocMSN"),
    SKYPE("skype", "uiIconSocSkype"),
    YAHOO("yahoo", "uiIconSocYahoo"),
    OTHER("other", "uiIconSocOther");

    private final String key;
    private final String iconClass;
    
    IconClass(String key, String iconClass) {
      this.key = key;
      this.iconClass = iconClass;
    }
    String getKey() {
      return this.key;
    }
    public String getIconClass() {
      return iconClass;
    }
    public static String getIconClass(String key) {
      for (IconClass iconClass : IconClass.values()) {
        if (iconClass.getKey().equals(key)) {
          return iconClass.getIconClass();
        }
      }
      return DEFAULT.getIconClass();
    }
  }
  
  /**
   * @param currentProfile
   * @return
   */
  public static Map<String, Object> getDisplayProfileInfo(Profile currentProfile) {
    Map<String, Object> infos = new LinkedHashMap<String, Object>();
    infos.put(Profile.EMAIL, currentProfile.getEmail());
    //
    String jobTitle = currentProfile.getPosition();
    if(!isEmpty(jobTitle)) {
      infos.put(Profile.POSITION, jobTitle);
    }
    String gender = currentProfile.getGender();
    if(!isEmpty(gender)) {
      infos.put(Profile.GENDER, gender);
    }
    //
    putInfoData(currentProfile, infos, Profile.CONTACT_PHONES);
    //
    putInfoData(currentProfile, infos, Profile.CONTACT_IMS);
    //
    putInfoData(currentProfile, infos, Profile.CONTACT_URLS);
    //
    return infos;
  }
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

  public static void initProfilePopup(String id) throws Exception {
    JSONObject object = new JSONObject();
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    object.put("StatusTitle", getLabel(context, "UserProfilePopup.label.Loading"));
    String[] keys = new String[]{"Connect", "Confirm", "CancelRequest", "RemoveConnection", "Ignore"};
    for (int i = 0; i < keys.length; i++) {
      object.put(keys[i], getLabel(context, "UserProfilePopup.label." + keys[i]));
    }
    //
    context.getJavascriptManager().getRequireJS().require("SHARED/social-ui-profile", "profile")
           .addScripts("profile.initUserProfilePopup('" + id + "', " + object.toString() + ");");
  }
  
  public static boolean isString(Object s) {
    return s instanceof String;
  }
  
  public static boolean isURL(String key) {
    if (key == null) return false;
    return Profile.CONTACT_URLS.equals(key);
  }
  
  public static String toAbsoluteURL(String url) {
    URI uri;
    try {
      uri = new URI(url);
      if (uri.isAbsolute()) {
        return url;
      }
    } catch (URISyntaxException e) {
      return DEFAULT_PROTOCOL + url;
    }
    
    return DEFAULT_PROTOCOL + url;
  }
  
  public static boolean isIMs(String key) {
    if (key == null) return false;
    return Profile.CONTACT_IMS.equals(key);
  }
  
  public static String getIconCss(String key) {
    return IconClass.getIconClass(key);
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
  
  private static void putInfoData(Profile currentProfile, Map<String, Object> infos, String mainKey) {
    List<Map<String, String>> multiValues = getMultiValues(currentProfile, mainKey);
    if (multiValues != null && multiValues.size() > 0) {
      Map<String, List<String>> mainValue = new HashMap<String, List<String>>();
      
      for (Map<String, String> map : multiValues) {
        List<String> values = new ArrayList<String>();
        String key = map.get(KEY);
        String value = map.get(VALUE);
        if (mainValue.containsKey(key)) {
          values.addAll(mainValue.get(key));
          values.add(value);
        } else {
          values.add(value);
        }
        
        mainValue.put(key, values);
      }
      //
      infos.put(mainKey, mainValue);
    }
  }
}
