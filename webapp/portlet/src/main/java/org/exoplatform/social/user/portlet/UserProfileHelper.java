package org.exoplatform.social.user.portlet;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.webui.application.WebuiRequestContext;

public class UserProfileHelper {
  final public static String KEY = "key";
  final public static String VALUE = "value";
  final public static String URL_KEY = "url";
  final public static String OTHER_KEY = "other";
  final public static String DEFAULT_PROTOCOL = "http://";
  final private static Pattern ESCAPE_HTML_PATTERN = Pattern.compile("<(.*?)>", Pattern.CASE_INSENSITIVE);
  final private static Pattern UNESCAPE_HTML_PATTERN = Pattern.compile("&lt;(.*?)&gt;", Pattern.CASE_INSENSITIVE);

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

  enum StatusIconCss {
    DEFAULT("", ""),
    ONLINE("online", "uiIconUserOnline"),
    OFFLINE("offline", "uiIconUserOffline"),
    AVAILABLE("available", "uiIconUserAvailable"),
    INVISIBLE("invisible", "uiIconUserInvisible"),
    AWAY("away", "uiIconUserAway"),
    DONOTDISTURB("donotdisturb", "uiIconUserDonotdisturb");

    private final String key;
    private final String iconCss;
    
    StatusIconCss(String key, String iconCss) {
      this.key = key;
      this.iconCss = iconCss;
    }
    String getKey() {
      return this.key;
    }
    public String getIconCss() {
      return iconCss;
    }
    public static String getIconCss(String key) {
      for (StatusIconCss iconClass : StatusIconCss.values()) {
        if (iconClass.getKey().equals(key)) {
          return iconClass.getIconCss();
        }
      }
      return DEFAULT.getIconCss();
    }
  }
  
  /**
   * @param currentProfile
   * @return
   */
  public static Map<String, Object> getDisplayProfileInfo(Profile currentProfile) {
    Map<String, Object> infos = new LinkedHashMap<String, Object>();
    String email = currentProfile.getEmail();
    // LDAP user might not have email
    if(!isEmpty(email)) {
      infos.put(Profile.EMAIL, email);
    }
    //
    String jobTitle = currentProfile.getPosition();
    if(!isEmpty(jobTitle)) {
      infos.put(Profile.POSITION, encodeHTML(jobTitle));
    }
    String gender = currentProfile.getGender();
    if(!isEmpty(gender)) {
      infos.put(Profile.GENDER, encodeHTML(gender));
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
        urls.add(decodeHTML(map.get(VALUE)));
      }
    }
    return urls;
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
  
  public static boolean isGender(String key) {
    if (key == null) return false;
    return Profile.GENDER.equals(key);
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
      destExperience.put(key, value);
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
      
      List<String> valuesOfOther = new ArrayList<String>();
      for (Map<String, String> map : multiValues) {
        List<String> values = new ArrayList<String>();
        String key = map.get(KEY);
        String value = encodeHTML(map.get(VALUE));
        if (mainValue.containsKey(key)) {
          values.addAll(mainValue.get(key));
          values.add(value);
        } else {
          values.add(value);
        }
        
        if (OTHER_KEY.equals(key)) {
          valuesOfOther.addAll(values);
        } else {
          mainValue.put(key, values);
        }
      }
      
      //
      if (valuesOfOther.size() > 0) {
        LinkedHashMap<String, List<String>> newValues = new LinkedHashMap<String, List<String>>();
        newValues.putAll(mainValue);
        newValues.put(OTHER_KEY, valuesOfOther);
        infos.put(mainKey, newValues);
      } else {
        infos.put(mainKey, mainValue);
      }
    }
  }

  public static String encodeHTML(String s) {
    if (isEmpty(s)) {
      return s;
    }
    return ESCAPE_HTML_PATTERN.matcher(s).replaceAll("&lt;$1&gt;");
  }

  public static String decodeHTML(String s) {
    if (isEmpty(s)) {
      return s;
    }
    return UNESCAPE_HTML_PATTERN.matcher(s).replaceAll("<$1>");
  }
  
  /**
   * Checks if input email is existing already or not.
   * 
   * @param email Input email to check.
   * @return true if email is existing in system.
   */
  public static boolean isExistingEmail(String email) {
    try {
      Query query = new Query();
      query.setEmail(email);
      OrganizationService service = CommonsUtils.getService(OrganizationService.class);
      return service.getUserHandler().findUsersByQuery(query).getSize() > 0;
    } catch (Exception e) {
      return false;
    }
  }
}