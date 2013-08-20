package org.exoplatform.social.notification.test;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.NotificationKey;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.notification.AbstractCoreTest;
import org.exoplatform.social.notification.plugin.NewUserPlugin;

public class NewUserPluginTestCase extends AbstractCoreTest {
  
  private AbstractNotificationPlugin newUserPlugin;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    newUserPlugin = pluginService.getPlugin(NotificationKey.key(NewUserPlugin.ID));
    assertNotNull(newUserPlugin);
    
    //By default the plugin and feature are active
    assertTrue(pluginSettingService.isActive(newUserPlugin.getId()));
    assertTrue(exoFeatureService.isActiveFeature("notification"));
  }
  
  @Override
  protected void tearDown() throws Exception {
    //
    pluginSettingService.savePlugin(newUserPlugin.getId(), true);
    exoFeatureService.saveActiveFeature("notification", true);
    
    newUserPlugin = null;
    super.tearDown();
  }
  
  public void testInstantly() throws Exception {
    Identity ghostIdentity = identityManager.getOrCreateIdentity("organization", "ghost", true);
    
    Collection<NotificationInfo> messages = notificationService.emails();
    assertEquals(1, messages.size());
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(messages.iterator().next().setTo("mary"));
    MessageInfo info = newUserPlugin.buildMessage(ctx);
    
    assertEquals(info.getSubject(), "Ghost gtn has joined eXo<br/>");
    
    //And when the plugin is not active
    pluginSettingService.savePlugin(newUserPlugin.getId(), false);
    Identity raulIdentity = identityManager.getOrCreateIdentity("organization", "raul", true);
    messages = notificationService.emails();
    assertEquals(0, messages.size());
    
    //Active the plugin but turn off the feature
    pluginSettingService.savePlugin(newUserPlugin.getId(), true);
    exoFeatureService.saveActiveFeature("notification", false);
    Identity paulIdentity = identityManager.getOrCreateIdentity("organization", "paul", true);
    messages = notificationService.emails();
    assertEquals(0, messages.size());
    
    identityManager.deleteIdentity(ghostIdentity);
    identityManager.deleteIdentity(raulIdentity);
    identityManager.deleteIdentity(paulIdentity);
  }
  
  public void testDigestWithAllActive() throws Exception {
    Identity ghostIdentity = identityManager.getOrCreateIdentity("organization", "ghost", true);
    Identity raulIdentity = identityManager.getOrCreateIdentity("organization", "raul", true);
    Identity paulIdentity = identityManager.getOrCreateIdentity("organization", "paul", true);
    
    Collection<NotificationInfo> messages = notificationService.emails();
    assertEquals(3, messages.size());
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    List<NotificationInfo> list = new ArrayList<NotificationInfo>();
    for (NotificationInfo message : messages) {
      list.add(message.setTo(rootIdentity.getRemoteId()));
    }
    ctx.setNotificationInfos(list);
    Writer writer = new StringWriter();
    newUserPlugin.buildDigest(ctx, writer);
    
    identityManager.deleteIdentity(ghostIdentity);
    identityManager.deleteIdentity(raulIdentity);
    identityManager.deleteIdentity(paulIdentity);
  }
  
  public void testDigestWithPluginOff() throws Exception {
    //turn off the plugin
    pluginSettingService.savePlugin(newUserPlugin.getId(), false);
    
    Identity ghostIdentity = identityManager.getOrCreateIdentity("organization", "ghost", true);
    Identity raulIdentity = identityManager.getOrCreateIdentity("organization", "raul", true);
    Identity paulIdentity = identityManager.getOrCreateIdentity("organization", "paul", true);
    
    //No messages
    Collection<NotificationInfo> messages = notificationService.emails();
    assertEquals(0, messages.size());
    
    identityManager.deleteIdentity(ghostIdentity);
    identityManager.deleteIdentity(raulIdentity);
    identityManager.deleteIdentity(paulIdentity);
  }
  
  public void testDigestWithFeatureOff() throws Exception {
    
    //turn off the feature
    exoFeatureService.saveActiveFeature("notification", false);
    
    Identity ghostIdentity = identityManager.getOrCreateIdentity("organization", "ghost", true);
    Identity raulIdentity = identityManager.getOrCreateIdentity("organization", "raul", true);
    Identity paulIdentity = identityManager.getOrCreateIdentity("organization", "paul", true);
    
    //No messages
    Collection<NotificationInfo> messages = notificationService.emails();
    assertEquals(0, messages.size());
    
    identityManager.deleteIdentity(ghostIdentity);
    identityManager.deleteIdentity(raulIdentity);
    identityManager.deleteIdentity(paulIdentity);
  }
}
