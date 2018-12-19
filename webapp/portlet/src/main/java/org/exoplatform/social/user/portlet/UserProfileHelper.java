package org.exoplatform.social.user.portlet;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.webui.application.WebuiRequestContext;

public class UserProfileHelper {
  
  public static final String CSS_ICON_PREFIX = "uiIconSoc";
  
  final public static String KEY = "key";
  final public static String VALUE = "value";
  final public static String URL_KEY = "url";
  final public static String OTHER_KEY = "other";
  final public static String DEFAULT_PROTOCOL = "http://";

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
    if(StringUtils.isNotBlank(email)) {
      infos.put(Profile.EMAIL, email);
    }
    //
    String jobTitle = currentProfile.getPosition();
    if(StringUtils.isNotBlank(jobTitle)) {
      infos.put(Profile.POSITION, StringEscapeUtils.escapeHtml4(jobTitle));
    }
    String gender = currentProfile.getGender();
    if(StringUtils.isNotBlank(gender)) {
      infos.put(Profile.GENDER, StringEscapeUtils.escapeHtml4(gender));
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
   * Convert profile experiences as a list of maps, with current experience set as the first item in the list
   *
   * @param currentProfile The user profile
   * @return A list of experiences with the current experience as the first one in the list
   */
  public static List<Map<String, String>> getSortedExperiences(Profile currentProfile) {
    List<Map<String, String>> experiences = getMultiValues(currentProfile, Profile.EXPERIENCES);
    if (experiences != null) {
      experiences.sort((exp1, exp2) -> isCurrent(exp1) ? -1 : 1);
    }
    return experiences;
  }
  
  public static List<Map<String, String>> getMultiValues(Profile currentProfile, String key) {
    return (List<Map<String, String>>) currentProfile.getProperty(key);
  }

  public static List<String> getURLValues(Profile currentProfile) {
    List<Map<String, String>> mapUrls = getMultiValues(currentProfile, Profile.CONTACT_URLS);
    List<String> urls = new ArrayList<String>();
    if (mapUrls != null) {
      for (Map<String, String> map : mapUrls) {
        urls.add(StringEscapeUtils.unescapeHtml4(map.get(VALUE)));
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
    StringBuilder classBuilder = new StringBuilder();
    if (key.length() > 0) {
      classBuilder.append(CSS_ICON_PREFIX).append(Character.toUpperCase(key.charAt(0)));
      if (key.length() > 1) {
        classBuilder.append(key.substring(1));
      }
    }
    return classBuilder.toString();
  }

  /**
   * Escape HTML entities in the properties of the given experience
   * @param experience The experience to escape
   * @return The experience escaped
   */
  public static Map<String, String> escapeExperience(Map<String, String> experience) {
    Map<String, String> escapedExperience = new LinkedHashMap<>();
    putExperienceData(experience, escapedExperience, Profile.EXPERIENCES_ID);
    putExperienceData(experience, escapedExperience, Profile.EXPERIENCES_COMPANY);
    putExperienceData(experience, escapedExperience, Profile.EXPERIENCES_POSITION);
    putExperienceData(experience, escapedExperience, Profile.EXPERIENCES_DESCRIPTION);
    putExperienceData(experience, escapedExperience, Profile.EXPERIENCES_SKILLS);
    //
    putExperienceData(experience, escapedExperience, Profile.EXPERIENCES_START_DATE);
    putExperienceData(experience, escapedExperience, Profile.EXPERIENCES_END_DATE);

    return escapedExperience;
  }

  private static boolean isCurrent(Map<String, String> srcExperience) {
    return Boolean.valueOf(String.valueOf(srcExperience.get(Profile.EXPERIENCES_IS_CURRENT)));
  }

  private static void putExperienceData(Map<String, String> srcExperience, Map<String, String> destExperience, String key) {
    String value = srcExperience.get(key);
    if (StringUtils.isNotBlank(value)) {
      destExperience.put(key, StringEscapeUtils.escapeHtml4(value));
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
      Map<String, List<String>> mainValue = new LinkedHashMap<String, List<String>>();
      
      for (Map<String, String> map : multiValues) {
        List<String> values = new ArrayList<String>();
        String key = map.get(KEY);
        String value = StringEscapeUtils.escapeHtml4(map.get(VALUE));
        if (mainValue.containsKey(key)) {
          values.addAll(mainValue.get(key));
          values.add(value);
        } else {
          values.add(value);
        }

          mainValue.put(key, values);
      }

        infos.put(mainKey, mainValue);
    }
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
