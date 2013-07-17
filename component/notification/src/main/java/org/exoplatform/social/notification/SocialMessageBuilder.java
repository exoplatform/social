package org.exoplatform.social.notification;

import org.exoplatform.commons.api.notification.AbstractMessageBuilder;
import org.exoplatform.commons.api.notification.ArgumentLiteral;
import org.exoplatform.commons.api.notification.MessageInfo;
import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;

public abstract class SocialMessageBuilder extends AbstractMessageBuilder<MessageInfo> {

  public final static ArgumentLiteral<String> ACTIVITY_ID = new ArgumentLiteral<String>(String.class, "activityId");
  public final static ArgumentLiteral<ExoSocialActivity> ACTIVITY = new ArgumentLiteral<ExoSocialActivity>(ExoSocialActivity.class, "activity");
  
  protected String getActivityId(NotificationContext ctx) {
    return null;
  }
  
	public AbstractMessageBuilder<MessageInfo> ACTIVITY_MENTION = new SocialMessageBuilder() {

    @Override
    public MessageInfo make(NotificationContext ctx) {
      return null;
    }
	  
  };
  
  
	
}
