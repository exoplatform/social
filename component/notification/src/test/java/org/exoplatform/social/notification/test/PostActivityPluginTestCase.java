package org.exoplatform.social.notification.test;

import java.util.Collection;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.NotificationKey;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.notification.AbstractPluginTest;
import org.exoplatform.social.notification.plugin.PostActivityPlugin;

public class PostActivityPluginTestCase extends AbstractPluginTest {
  
private AbstractNotificationPlugin postActivityPlugin;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    postActivityPlugin = pluginService.getPlugin(NotificationKey.key(PostActivityPlugin.ID));
    assertNotNull(postActivityPlugin);
    
    //By default the plugin and feature are active
    assertTrue(pluginSettingService.isActive(postActivityPlugin.getId()));
    assertTrue(exoFeatureService.isActiveFeature("notification"));
  }
  
  @Override
  protected void tearDown() throws Exception {
    //
    pluginSettingService.savePlugin(postActivityPlugin.getId(), true);
    exoFeatureService.saveActiveFeature("notification", true);
    
    postActivityPlugin = null;
    super.tearDown();
  }

  public void testWithAllActive() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("demo post activity on activity stream of root");
    activity.setUserId(demoIdentity.getId());
    activityManager.saveActivity(rootIdentity, activity);
    
    Collection<NotificationInfo> messages = notificationService.emails();
    assertEquals(1, messages.size());
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(messages.iterator().next().setTo("root"));
    MessageInfo info = postActivityPlugin.buildMessage(ctx);
    
    assertEquals("Demo gtn posted on your activity stream<br/>", info.getSubject());
  }

  @Override
  public AbstractNotificationPlugin getPlugin() {
    return postActivityPlugin;
  }
}
