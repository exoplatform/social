package org.exoplatform.social.notification.plugin;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.NotificationKey;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.notification.AbstractPluginTest;

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
  
  public void testSimpleCase() throws Exception {
    //STEP 1 create new user
    Identity ghostIdentity = identityManager.getOrCreateIdentity("organization", "ghost", true);
    tearDownIdentityList.add(ghostIdentity);
    // will sent 4 mails to 4 users existing.
    List<NotificationInfo> list = assertMadeNotifications(4);
    NotificationInfo newUserNotification = list.get(0);
    
    //STEP 2 assert Message info
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(newUserNotification.setTo("mary"));
    MessageInfo info = buildMessageInfo(ctx);
    
    assertSubject(info, "Ghost gtn has joined eXo");
    assertBody(info, "New user on eXo");
  }
  
  public void testPluginONOFF() throws Exception {
    //by default the plugin is ON
    Identity ghostIdentity = identityManager.getOrCreateIdentity("organization", "ghost", true);
    tearDownIdentityList.add(ghostIdentity);
    assertMadeNotifications(4);
    notificationService.clearAll();
    
    //turn off the plugin
    turnOFF(getPlugin());
    
    Identity raulIdentity = identityManager.getOrCreateIdentity("organization", "raul", true);
    tearDownIdentityList.add(raulIdentity);
    assertMadeNotifications(0);
    
    //turn on the plugin
    turnON(getPlugin());
    Identity paulIdentity = identityManager.getOrCreateIdentity("organization", "paul", true);
    tearDownIdentityList.add(paulIdentity);
    assertMadeNotifications(4);
    notificationService.clearAll();
  }
  
  public void testFeatureONOFF() throws Exception {
    //by default the feature is ON
    Identity ghostIdentity = identityManager.getOrCreateIdentity("organization", "ghost", true);
    tearDownIdentityList.add(ghostIdentity);
    
    assertMadeNotifications(4);
    notificationService.clearAll();
    
    //turn off the feature
    turnFeatureOff();
    
    Identity raulIdentity = identityManager.getOrCreateIdentity("organization", "raul", true);
    tearDownIdentityList.add(raulIdentity);
    assertMadeNotifications(0);
    
    //turn on the feature
    turnFeatureOn();
    Identity paulIdentity = identityManager.getOrCreateIdentity("organization", "paul", true);
    tearDownIdentityList.add(paulIdentity);
    assertMadeNotifications(4);
    notificationService.clearAll();
  }
  
  public void testDigest() throws Exception {
    Identity ghostIdentity = identityManager.getOrCreateIdentity("organization", "ghost", true);
    Identity raulIdentity = identityManager.getOrCreateIdentity("organization", "raul", true);
    Identity paulIdentity = identityManager.getOrCreateIdentity("organization", "paul", true);
    
    //Digest
    // will sent 12 mails to 4 users existing.
    List<NotificationInfo> list = assertMadeNotifications(12);
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    // remove duplicate message
    list = new ArrayList<NotificationInfo>(new LinkedHashSet<NotificationInfo>(list));

    list.get(0).setTo(rootIdentity.getRemoteId());
    
    CommonsUtils.getService(OrganizationService.class).getUserHandler().removeUser("raul", true);
    
    ctx.setNotificationInfos(list);
    Writer writer = new StringWriter();
    getPlugin().buildDigest(ctx, writer);
    assertDigest(writer, "Ghost gtn, Paul gtn have joined eXo.");
    
    tearDownIdentityList.add(ghostIdentity);
    tearDownIdentityList.add(raulIdentity);
    tearDownIdentityList.add(paulIdentity);
  }
  
  @Override
  public AbstractNotificationPlugin getPlugin() {
    return pluginService.getPlugin(NotificationKey.key(NewUserPlugin.ID));
  }
}
