/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.notification.plugin;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.NotificationKey;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.AbstractPluginTest;
import org.exoplatform.social.notification.mock.MockMessageQueue;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Aug 20, 2013  
 */
public class RequestJoinSpacePluginTest extends AbstractPluginTest {
  
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

  @Override
  public AbstractNotificationPlugin getPlugin() {
    return pluginService.getPlugin(NotificationKey.key(RequestJoinSpacePlugin.ID));
  }

  public void testRequestJoinSpaceWhenPluginOn() throws Exception {
    //
    turnON(getPlugin());
    
    List<String> settings = new ArrayList<String>();
    settings.add(getPlugin().getId());
    
    setInstantlySettings(rootIdentity.getRemoteId(), settings);
    
    Space space = getSpaceInstance(1);
    spaceService.addPendingUser(space, maryIdentity.getRemoteId());
    
    //check instantly
    NotificationInfo ntf = MockMessageQueue.get();
    ntf.setTo(rootIdentity.getRemoteId());
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(ntf);
    
    MessageInfo message = buildMessageInfo(ctx);
    
    assertSubject(message, maryIdentity.getProfile().getFullName()+" has requested access to my space 1 space.<br/>");
    assertBody(message, "New access requirement to your space");
    
    setDailySetting(rootIdentity.getRemoteId(), settings);
    
    spaceService.addPendingUser(space, demoIdentity.getRemoteId());
    spaceService.addPendingUser(space, johnIdentity.getRemoteId());
    
    //Daily
    //assertEquals(1, notificationService.emails().size());
    List<NotificationInfo> ntfs = getNotificationInfos();
    List<NotificationInfo> messages = new ArrayList<NotificationInfo>();
    for (NotificationInfo m : ntfs) {
      m.setTo(rootIdentity.getRemoteId());
      messages.add(m);
    }
    Writer writer = new StringWriter();
    ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfos(messages);
    getPlugin().buildDigest(ctx, writer);
    
    assertTrue(writer.toString().indexOf("The following users have asked to join") > 0);
    
    //
    Identity ghostIdentity = identityManager.getOrCreateIdentity("organization", "ghost", true);
    Identity raulIdentity = identityManager.getOrCreateIdentity("organization", "raul", true);
    
    //Weekly
    setDailySetting(rootIdentity.getRemoteId(), new ArrayList<String>());//remove Daily settings
    setWeeklySetting(rootIdentity.getRemoteId(), settings);
    
    spaceService.addPendingUser(space, ghostIdentity.getRemoteId());
    spaceService.addPendingUser(space, raulIdentity.getRemoteId());
    
    writer = new StringWriter();
    ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfos(messages);
    getPlugin().buildDigest(ctx, writer);
    //
    assertTrue(writer.toString().indexOf("The following users have asked to join") > 0);
    
    tearDownIdentityList.add(ghostIdentity);
    tearDownIdentityList.add(raulIdentity);
    tearDownSpaceList.add(space);
    
  }
  
  public void testRequestJoinSpaceWhenPluginOff() throws Exception {
    //
    turnOff(getPlugin());
    //
    List<String> settings = new ArrayList<String>();
    settings.add(getPlugin().getId());
    
    setInstantlySettings(rootIdentity.getRemoteId(), settings);
    
    Space space = getSpaceInstance(1);
    spaceService.addPendingUser(space, maryIdentity.getRemoteId());
    
    //check instantly
    NotificationInfo ntf = MockMessageQueue.get();
    assertNull(ntf);
    
    //Daily
    setDailySetting(rootIdentity.getRemoteId(), settings);
    
    spaceService.addPendingUser(space, demoIdentity.getRemoteId());
    spaceService.addPendingUser(space, johnIdentity.getRemoteId());
    
    List<NotificationInfo> ntfs = getNotificationInfos();
    assertTrue(ntfs.size() == 0);
    
    //
    Identity ghostIdentity = identityManager.getOrCreateIdentity("organization", "ghost", true);
    Identity raulIdentity = identityManager.getOrCreateIdentity("organization", "raul", true);
    //clear notification when create new user
    getNotificationInfos();
    
    //Weekly
    setDailySetting(rootIdentity.getRemoteId(), new ArrayList<String>());//remove Daily settings
    setWeeklySetting(rootIdentity.getRemoteId(), settings);
    
    spaceService.addPendingUser(space, ghostIdentity.getRemoteId());
    spaceService.addPendingUser(space, raulIdentity.getRemoteId());
    
    //
    ntfs = getNotificationInfos();
    assertTrue(ntfs.size() == 0);
    
    tearDownIdentityList.add(ghostIdentity);
    tearDownIdentityList.add(raulIdentity);
    tearDownSpaceList.add(space);
    
  }
  
  public void testRequestJoinSpaceWhenFeatureOff() throws Exception {
    //
    turnFeatureOff();
    
    //
    List<String> settings = new ArrayList<String>();
    settings.add(getPlugin().getId());
    
    setInstantlySettings(rootIdentity.getRemoteId(), settings);
    
    Space space = getSpaceInstance(1);
    spaceService.addPendingUser(space, maryIdentity.getRemoteId());
    
    //check instantly
    NotificationInfo ntf = MockMessageQueue.get();
    assertNull(ntf);
    
    //Daily
    setDailySetting(rootIdentity.getRemoteId(), settings);
    
    spaceService.addPendingUser(space, demoIdentity.getRemoteId());
    spaceService.addPendingUser(space, johnIdentity.getRemoteId());
    
    List<NotificationInfo> ntfs = getNotificationInfos();
    assertTrue(ntfs.size() == 0);
    
    //
    Identity ghostIdentity = identityManager.getOrCreateIdentity("organization", "ghost", true);
    Identity raulIdentity = identityManager.getOrCreateIdentity("organization", "raul", true);
    
    //Weekly
    setDailySetting(rootIdentity.getRemoteId(), new ArrayList<String>());//remove Daily settings
    setWeeklySetting(rootIdentity.getRemoteId(), settings);
    
    spaceService.addPendingUser(space, ghostIdentity.getRemoteId());
    spaceService.addPendingUser(space, raulIdentity.getRemoteId());
    
    //
    ntfs = getNotificationInfos();
    assertTrue(ntfs.size() == 0);
    
    tearDownIdentityList.add(ghostIdentity);
    tearDownIdentityList.add(raulIdentity);
    tearDownSpaceList.add(space);
  }
}
