package org.exoplatform.social.webui.activity.news;

import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;

@ComponentConfig(lifecycle = UIFormLifecycle.class, template = "war:/groovy/social/webui/activity/UINewsActivity.gtmpl", events = {
    @EventConfig(listeners = BaseUIActivity.LoadLikesActionListener.class),
    @EventConfig(listeners = BaseUIActivity.ToggleDisplayCommentFormActionListener.class),
    @EventConfig(listeners = BaseUIActivity.LikeActivityActionListener.class),
    @EventConfig(listeners = BaseUIActivity.SetCommentListStatusActionListener.class),
    @EventConfig(listeners = BaseUIActivity.PostCommentActionListener.class),
    @EventConfig(listeners = BaseUIActivity.DeleteActivityActionListener.class),
    @EventConfig(listeners = BaseUIActivity.DeleteCommentActionListener.class),
    @EventConfig(listeners = BaseUIActivity.LikeCommentActionListener.class),
    @EventConfig(listeners = BaseUIActivity.EditActivityActionListener.class),
    @EventConfig(listeners = BaseUIActivity.EditCommentActionListener.class) })
public class UINewsActivity extends BaseUIActivity {
  public static final String ACTIVITY_TYPE = "news";
}
