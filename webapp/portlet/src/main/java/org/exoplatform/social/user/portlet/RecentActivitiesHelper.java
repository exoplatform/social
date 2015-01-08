package org.exoplatform.social.user.portlet;

import java.util.Map;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;

public class RecentActivitiesHelper {
  private static final String LINK_PARAM         = "link";

  enum IconType {
    DEFAULT("", ""),
    LINK("LINK_ACTIVITY", "LinkMini"),
    DOC("DOC_ACTIVITY", "FileSharing"),
    SPACE("SPACE_ACTIVITY", "SpaceMini"),
    PROFILE("USER_PROFILE_ACTIVITY", "UserProfile"),
    FILE("files:spaces", "FileSharing"),
    CONTENT("contents:spaces", "ContentMini"),
    CALENDAR("cs-calendar:spaces", "CalendarMini"),
    FORUM("ks-forum:spaces", "ForumMini"),
    ANSWER("ks-answer:spaces", "AnswersMini"),
    POLL("ks-poll:spaces", "Poll"),
    WIKI("ks-wiki:spaces", "WikiMini");

    private final static String UI_SOC = "uiIconSoc";
    private final String type;
    private final String iconClass;
    
    IconType(String type, String iconClass) {
      this.type = type;
      this.iconClass = iconClass;
    }
    String getType() {
      return this.type;
    }
    public String getIconClass() {
      return UI_SOC + iconClass;
    }
    public static IconType getIconType(String type) {
      for (IconType iconType : IconType.values()) {
        if (iconType.getType().equals(type)) {
          return iconType;
        }
      }
      return DEFAULT;
    }
  }

  public static String getLink(ExoSocialActivity activity) {
    String activityType = activity.getType();
    Map<String, String> templateParams = activity.getTemplateParams();
    if (activityType.equals(IconType.LINK.getType())) {
      return templateParams.get(LINK_PARAM);
    }
    return null;
  }
  
  /**
   * 
   * @param activityType
   * @return
   */
  public static String getActivityTypeIcon(String activityType) {
    return IconType.getIconType(activityType).getIconClass();
  }
}
