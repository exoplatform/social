package org.exoplatform.social.notification.plugin;

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
import org.exoplatform.social.notification.AbstractPluginTest;
import org.exoplatform.social.notification.mock.MockMessageQueue;
import org.exoplatform.social.notification.plugin.NewUserPlugin;

public class NewUserPluginTest extends AbstractPluginTest {
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    //By default the plugin and feature are active
    assertTrue(pluginSettingService.isActive(getPlugin().getId()));
    assertTrue(exoFeatureService.isActiveFeature("notification"));
  }
  
  @Override
  protected void tearDown() throws Exception {
    //
    turnON(getPlugin());
    turnFeatureOn();
    
    super.tearDown();
  }
  
  public void testInstantly() throws Exception {
    Identity ghostIdentity = identityManager.getOrCreateIdentity("organization", "ghost", true);
    
    Collection<NotificationInfo> messages = notificationService.emails();
    assertEquals(1, messages.size());
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(messages.iterator().next().setTo("mary"));
    MessageInfo info = buildMessageInfo(ctx);
    
    assertSubject(info, "Ghost gtn has joined eXo<br/>");
    
    //And when the plugin is not active
    turnOff(getPlugin());
    Identity raulIdentity = identityManager.getOrCreateIdentity("organization", "raul", true);
    NotificationInfo ntf = MockMessageQueue.get();
    assertNull(ntf);
    
    //Active the plugin but turn off the feature
    turnON(getPlugin());
    turnFeatureOff();

    Identity paulIdentity = identityManager.getOrCreateIdentity("organization", "paul", true);
    ntf = MockMessageQueue.get();
    assertNull(ntf);
    
    identityManager.deleteIdentity(ghostIdentity);
    identityManager.deleteIdentity(raulIdentity);
    identityManager.deleteIdentity(paulIdentity);
  }
  
  public void testDigestWithAllActive() throws Exception {
    Identity ghostIdentity = identityManager.getOrCreateIdentity("organization", "ghost", true);
    Identity raulIdentity = identityManager.getOrCreateIdentity("organization", "raul", true);
    Identity paulIdentity = identityManager.getOrCreateIdentity("organization", "paul", true);
    
    //Digest
    Collection<NotificationInfo> messages = notificationService.emails();
    assertEquals(3, messages.size());
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    List<NotificationInfo> list = new ArrayList<NotificationInfo>();
    for (NotificationInfo message : messages) {
      list.add(message.setTo(rootIdentity.getRemoteId()));
    }
    ctx.setNotificationInfos(list);
    Writer writer = new StringWriter();
    getPlugin().buildDigest(ctx, writer);
    assertDigest(writer, "Ghost gtn, Raul gtn, Paul gtn have joined eXo.");
    
    identityManager.deleteIdentity(ghostIdentity);
    identityManager.deleteIdentity(raulIdentity);
    identityManager.deleteIdentity(paulIdentity);
  }
  
  public void testDigestWithPluginOff() throws Exception {
    //turn off the plugin
    turnOff(getPlugin());
    
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

  @Override
  public AbstractNotificationPlugin getPlugin() {
    return pluginService.getPlugin(NotificationKey.key(NewUserPlugin.ID));
  }
}
