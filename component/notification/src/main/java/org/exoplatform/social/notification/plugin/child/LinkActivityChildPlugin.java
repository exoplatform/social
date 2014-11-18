package org.exoplatform.social.notification.plugin.child;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.ArgumentLiteral;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationChildPlugin;
import org.exoplatform.commons.api.notification.service.template.TemplateContext;
import org.exoplatform.commons.notification.NotificationUtils;
import org.exoplatform.commons.notification.template.TemplateUtils;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.notification.LinkProviderUtils;
import org.exoplatform.social.notification.Utils;
import org.exoplatform.social.service.rest.LinkShare;

public class LinkActivityChildPlugin extends AbstractNotificationChildPlugin {
  
  public final static ArgumentLiteral<String> ACTIVITY_ID = new ArgumentLiteral<String>(String.class, "activityId");
  
  public static final String                  ID                 = "LINK_ACTIVITY";

  public static final String                  LINK_PARAM         = "link";

  public static final String                  IMAGE_PARAM        = "image";

  public static final String                  TITLE_PARAM        = "title";

  public static final String                  DESCRIPTION_PARAM  = "description";

  public static final String                  COMMENT_PARAM      = "comment";

  public static final String                  VIEW_FULL_ACTIVITY = "view_full_activity";
  
  private ExoSocialActivity activity = null;
  
  private boolean isEmbedLink = false;
  
  public LinkActivityChildPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public String makeContent(NotificationContext ctx) {
    try {
      ActivityManager activityM = CommonsUtils.getService(ActivityManager.class);
      NotificationInfo notification = ctx.getNotificationInfo();
      String language = getLanguage(notification);
      String activityId = notification.getValueOwnerParameter(ACTIVITY_ID.getKey());
      activity = activityM.getActivity(activityId);
      if (activity.isComment()) {
        activity = activityM.getParentActivity(activity);
      }
      TemplateContext templateContext = new TemplateContext(ID, language);
      
      String url = activity.getTemplateParams().get(LINK_PARAM);
      
      templateContext.put("ACTIVITY_TITLE", NotificationUtils.processLinkTitle(activity.getTemplateParams().get(COMMENT_PARAM)));
      templateContext.put("LINK_TITLE", NotificationUtils.processLinkTitle(activity.getTemplateParams().get(TITLE_PARAM)));
      templateContext.put("LINK_URL", url);
      templateContext.put("THUMBNAIL_URL", getImageUrl(url));
      templateContext.put("IS_EMBED_LINK", isEmbedLink());
      templateContext.put("LINK_DESCRIPTION", Utils.formatContent(activity.getTemplateParams().get(DESCRIPTION_PARAM)));
      templateContext.put("ACTIVITY_URL", LinkProviderUtils.getRedirectUrl(VIEW_FULL_ACTIVITY, activity.getId()));
      //
      String content = TemplateUtils.processGroovy(templateContext);
      return content;
    } catch (Exception e) {
      return (activity != null) ? activity.getTitle() : "";
    }
  }

  @Override
  public String getId() {
    return ID;
  }

  @Override
  public boolean isValid(NotificationContext ctx) {
    return false;
  }

  private String getImageUrl(String url) {
    try {
      String imageUrl = activity.getTemplateParams().get(IMAGE_PARAM);
      if (imageUrl != null && imageUrl.length() > 0) {
        setEmbedLink(false);
        return imageUrl;
      }
      setEmbedLink(true);
      imageUrl = LinkShare.getInstance(url).getMediaObject().getThumbnailUrl();
      return imageUrl != null ? imageUrl : "";
    } catch (Exception e) {
      return "";
    }
  }

  /**
   * @return the isEmbedLink
   */
  public boolean isEmbedLink() {
    return isEmbedLink;
  }

  /**
   * @param isEmbedLink the isEmbedLink to set
   */
  public void setEmbedLink(boolean isEmbedLink) {
    this.isEmbedLink = isEmbedLink;
  }

  @Override
  protected String makeUIMessage(NotificationContext ctx) {
    return null;
  }

}