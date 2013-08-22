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
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.AbstractPluginTest;
import org.exoplatform.social.notification.mock.MockMessageQueue;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Aug 20, 2013  
 */
public class SpaceInvitationPluginTest extends AbstractPluginTest {

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
    return pluginService.getPlugin(NotificationKey.key(SpaceInvitationPlugin.ID));
  }

  public void testSpaceInvitationWhenPluginOn() throws Exception {
    //
    List<String> settings = new ArrayList<String>();
    settings.add(getPlugin().getId());
    
    setInstantlySettings(rootIdentity.getRemoteId(), settings);
    
    Space space = getSpaceInstance(1);
    spaceService.addInvitedUser(space, maryIdentity.getRemoteId());
    
    //check instantly
    NotificationInfo ntf = MockMessageQueue.get();
    ntf.setTo(rootIdentity.getRemoteId());
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(ntf);
    
    MessageInfo message = buildMessageInfo(ctx);
    
    assertSubject(message, "You've been invited to join " + space.getDisplayName() + " space<br/>");
    assertBody(message, "You've received an invitation to join");
    
    //Daily
    setDailySetting(rootIdentity.getRemoteId(), settings);
    
    Space space2 = getSpaceInstance(2);
    Space space3 = getSpaceInstance(3);
    spaceService.addInvitedUser(space2, maryIdentity.getRemoteId());
    spaceService.addInvitedUser(space3, maryIdentity.getRemoteId());
    
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
    
    assertTrue(writer.toString().indexOf("You have been asked to joing the following spaces:") >= 0);
    
    //
    Space space4 = getSpaceInstance(4);
    Space space5 = getSpaceInstance(5);
    
    //Weekly
    setDailySetting(rootIdentity.getRemoteId(), new ArrayList<String>());//remove Daily settings
    setWeeklySetting(rootIdentity.getRemoteId(), settings);
    
    spaceService.addInvitedUser(space4, maryIdentity.getRemoteId());
    spaceService.addInvitedUser(space5, maryIdentity.getRemoteId());
    
    writer = new StringWriter();
    ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfos(messages);
    getPlugin().buildDigest(ctx, writer);
    //
    assertTrue(writer.toString().indexOf("You have been asked to joing the following spaces:") >= 0);
    
    tearDownSpaceList.add(space);
    tearDownSpaceList.add(space2);
    tearDownSpaceList.add(space3);
    tearDownSpaceList.add(space4);
    tearDownSpaceList.add(space5);
    
  }
  
  public void testSpaceInvitationWhenPluginOff() throws Exception {
    //
    turnOFF(getPlugin());
    //
    checkNotificationWhenPluginOrFeatureOff();
    
  }
  
  public void testSpaceInvitationWhenFeatureOff() throws Exception {
    //
    turnFeatureOff();
    //
    checkNotificationWhenPluginOrFeatureOff();
    
  }

  private void checkNotificationWhenPluginOrFeatureOff() throws Exception {
    //
    List<String> settings = new ArrayList<String>();
    settings.add(getPlugin().getId());
    
    setInstantlySettings(rootIdentity.getRemoteId(), settings);
    
    Space space = getSpaceInstance(1);
    spaceService.addInvitedUser(space, maryIdentity.getRemoteId());
    
    //check instantly
    NotificationInfo ntf = MockMessageQueue.get();
    assertNull(ntf);
    
    //Daily
    setDailySetting(rootIdentity.getRemoteId(), settings);
    
    Space space2 = getSpaceInstance(2);
    Space space3 = getSpaceInstance(3);
    spaceService.addInvitedUser(space2, maryIdentity.getRemoteId());
    spaceService.addInvitedUser(space3, maryIdentity.getRemoteId());
    
    List<NotificationInfo> ntfs = getNotificationInfos();
    assertTrue(ntfs.size() == 0);
    
    //
    Space space4 = getSpaceInstance(4);
    Space space5 = getSpaceInstance(5);
    
    //Weekly
    setDailySetting(rootIdentity.getRemoteId(), new ArrayList<String>());//remove Daily settings
    setWeeklySetting(rootIdentity.getRemoteId(), settings);
    
    spaceService.addInvitedUser(space4, maryIdentity.getRemoteId());
    spaceService.addInvitedUser(space5, maryIdentity.getRemoteId());
    
    //
    ntfs = getNotificationInfos();
    assertTrue(ntfs.size() == 0);
    
    tearDownSpaceList.add(space);
    tearDownSpaceList.add(space2);
    tearDownSpaceList.add(space3);
    tearDownSpaceList.add(space4);
    tearDownSpaceList.add(space5);
  }
  
}
