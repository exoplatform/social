package org.exoplatform.social.portlet.userprofile.helpers;

import java.util.Map;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.webui.Utils;

public class RecentActivitiesHelper {
  private static final String LINK_PARAM = "link";

  enum IconType {
    DEFAULT("", ""),
    LINK("LINK_ACTIVITY", "uiIconSocLinkMini"),
    DOC("DOC_ACTIVITY", "uiIconSocFileSharing"),
    SPACE("SPACE_ACTIVITY", "uiIconSocSpaceMini"),
    PROFILE("USER_PROFILE_ACTIVITY", "uiIconSocUserProfile"),
    FILE("files:spaces", "uiIconSocFileSharing"),
    CONTENT("contents:spaces", "uiIconSocContentMini"),
    CALENDAR("cs-calendar:spaces", "uiIconSocCalendarMini"),
    FORUM("ks-forum:spaces", "uiIconSocForumMini"),
    ANSWER("ks-answer:spaces", "uiIconSocAnswersMini"),
    POLL("ks-poll:spaces", "uiIconSocPoll"),
    WIKI("ks-wiki:spaces", "uiIconSocWikiMini");

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
      return iconClass;
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
  
  public static Profile getOwnerActivityProfile(ExoSocialActivity activity) {
    return Utils.getIdentityManager().getIdentity(activity.getUserId(), true).getProfile();
  }
}
