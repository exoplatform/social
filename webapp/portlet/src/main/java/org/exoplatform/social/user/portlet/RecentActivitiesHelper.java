package org.exoplatform.social.user.portlet;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.exoplatform.social.core.activity.model.ActivityPluginType;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.webui.Utils;

public class RecentActivitiesHelper {
  private static final String LINK_PARAM         = "link";
  private static final String LINK_TITLE         = "comment";
  private static final String TOPIC_LINK         = "TopicLink";
  private static final String PAGE_URL           = "page_url";
  private static final String EVENT_LINK         = "EventLink";
  private static final String EVENT_TYPE         = "EventType";
  private static final String TASK               = "TaskAdded";

  enum Type {
    DEFAULT("", ""),
    LINK(ActivityPluginType.LINK.getName(), "uiIconSocLinkMini"),
    DOC(ActivityPluginType.DOC.getName(), "uiIconSocFileSharing"),
    SPACE(ActivityPluginType.SPACE.getName(), "uiIconSocSpaceMini"),
    PROFILE(ActivityPluginType.PROFILE.getName(), "uiIconSocUserProfile"),
    FILE(ActivityPluginType.FILE.getName(), "uiIconSocFileSharing"),
    CONTENT(ActivityPluginType.CONTENT.getName(), "uiIconSocContentMini"),
    CALENDAR(ActivityPluginType.CALENDAR.getName(), "uiIconSocCalendarMini"),
    TASK(ActivityPluginType.TASK.getName(), "uiIconSocTaskSharing"),
    FORUM(ActivityPluginType.FORUM.getName(), "uiIconSocForumMini"),
    ANSWER(ActivityPluginType.ANSWER.getName(), "uiIconSocAnswersMini"),
    POLL(ActivityPluginType.POLL.getName(), "uiIconSocPoll"),
    WIKI(ActivityPluginType.WIKI.getName(), "uiIconSocWikiMini");

    private final String type;
    private final String iconClass;
    
    Type(String type, String iconClass) {
      this.type = type;
      this.iconClass = iconClass;
    }
    String getType() {
      return this.type;
    }
    public String getIconClass() {
      return iconClass;
    }
    public static Type getIconType(String type) {
      for (Type iconType : Type.values()) {
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
    if (activityType.equals(Type.LINK.getType())) {
      return templateParams.get(LINK_PARAM);
    } else if (activityType.equals(Type.DOC.getType()) || activityType.equals(Type.FILE.getType())
        || activityType.equals(Type.POLL.getType()) || activityType.equals(Type.ANSWER.getType())) {
      return LinkProvider.getSingleActivityUrl(activity.getId());
    } else if (activityType.equals(Type.CALENDAR.getType())) {
      return templateParams.get(EVENT_LINK);
    } else if (activityType.equals(Type.FORUM.getType())) {
      return templateParams.get(TOPIC_LINK);
    } else if (activityType.equals(Type.WIKI.getType())) {
      return templateParams.get(PAGE_URL);
    }
    
    return null;
  }
  
  public static String getLinkTitle(ExoSocialActivity activity) {
    Map<String, String> templateParams = activity.getTemplateParams();
    String linkTitle = templateParams.get(LINK_TITLE);
    if (StringUtils.isNotBlank(linkTitle)) {
      return linkTitle;
    }
    return null;
  }
  /**
   * 
   * @param activity
   * @return
   */
  public static String getActivityTypeIcon(ExoSocialActivity activity) {
    String activityType = activity.getType();
    if (activityType.equals(Type.CALENDAR.getType()) &&
        TASK.equals(activity.getTemplateParams().get(EVENT_TYPE))) {
      return Type.getIconType(TASK).getIconClass(); 
    }
    return Type.getIconType(activityType).getIconClass();
  }
  
  public static Profile getOwnerActivityProfile(ExoSocialActivity activity) {
    return Utils.getIdentityManager().getIdentity(activity.getUserId(), true).getProfile();
  }
}
