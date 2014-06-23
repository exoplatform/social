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
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  @Override
  public AbstractNotificationPlugin getPlugin() {
    return pluginService.getPlugin(NotificationKey.key(RequestJoinSpacePlugin.ID));
  }

  public void testSimpleCase() throws Exception {
    //
    Space space = getSpaceInstance(1);
    //Make request to join space
    spaceService.addPendingUser(space, maryIdentity.getRemoteId());
    List<NotificationInfo> list = assertMadeNotifications(1);
    
    //assert Message Info
    NotificationInfo ntf = list.get(0);
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(ntf.setTo(rootIdentity.getRemoteId()));
    MessageInfo message = buildMessageInfo(ctx);
    
    //subject's max length = 50
    assertSubject(message, maryIdentity.getProfile().getFullName()+" has requested access to my space 1...");
    assertBody(message, "New access requirement to your space");
    notificationService.clearAll();
  }
  
  public void testPluginOFF() throws Exception {
    //
    Space space = getSpaceInstance(1);
    //Make request to join space
    spaceService.addPendingUser(space, demoIdentity.getRemoteId());
    List<NotificationInfo> list = assertMadeNotifications(1);
    
    //assert Message Info
    NotificationInfo ntf = list.get(0);
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(ntf.setTo(rootIdentity.getRemoteId()));
    MessageInfo message = buildMessageInfo(ctx);
    
    assertSubject(message, demoIdentity.getProfile().getFullName()+" has requested access to my space 1 space.");
    assertBody(message, "New access requirement to your space");
    notificationService.clearAll();
    
    //OFF
    turnOFF(getPlugin());
    
    //make request
    spaceService.addPendingUser(space, demoIdentity.getRemoteId());
    assertMadeNotifications(0);
    
    //Check other plugin
    makeRelationship(maryIdentity, rootIdentity);
    assertMadeNotifications(1);
    notificationService.clearAll();
    //
    turnON(getPlugin());
  }
  
  public void testPluginON() throws Exception {
    //OFF
    turnOFF(getPlugin());
    
    //
    Space space = getSpaceInstance(1);
    //Make request to join space
    spaceService.addPendingUser(space, demoIdentity.getRemoteId());
    assertMadeNotifications(0);
    
    //ON
    turnON(getPlugin());
    
    //Make request to join space
    spaceService.addPendingUser(space, maryIdentity.getRemoteId());
    List<NotificationInfo> list = assertMadeNotifications(1);
    
    //assert Message Info
    NotificationInfo ntf = list.get(0);
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(ntf.setTo(rootIdentity.getRemoteId()));
    MessageInfo message = buildMessageInfo(ctx);
    
    assertSubject(message, maryIdentity.getProfile().getFullName()+" has requested access to my space 1...");
    assertBody(message, "New access requirement to your space");
    notificationService.clearAll();
  }
  
  public void testDigestWithPluginON() throws Exception {
    //OFF
    turnOFF(getPlugin());
    
    //
    Space space = getSpaceInstance(1);
    //Make requests to join space
    spaceService.addPendingUser(space, demoIdentity.getRemoteId());
    assertMadeNotifications(0);
    
    //ON
    turnON(getPlugin());
    
    //make requests
    spaceService.addPendingUser(space, johnIdentity.getRemoteId());
    spaceService.addPendingUser(space, maryIdentity.getRemoteId());
    
    List<NotificationInfo> list = assertMadeNotifications(2);
    List<NotificationInfo> messages = new ArrayList<NotificationInfo>();
    for (NotificationInfo m : list) {
      m.setTo(rootIdentity.getRemoteId());
      messages.add(m);
    }
    Writer writer = new StringWriter();
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfos(messages);
    getPlugin().buildDigest(ctx, writer);
    
    assertDigest(writer, "The following users have asked to join the my space 1 space: John Anthony, Mary Kelly.");
    notificationService.clearAll();
    
  }
  
  public void testDigestWithPluginOFF() throws Exception {
    //
    Space space = getSpaceInstance(1);
    //Make requests to join space
    spaceService.addPendingUser(space, demoIdentity.getRemoteId());
    spaceService.addPendingUser(space, johnIdentity.getRemoteId());
    
    //assert Message Info
    List<NotificationInfo> list = assertMadeNotifications(2);
    List<NotificationInfo> messages = new ArrayList<NotificationInfo>();
    for (NotificationInfo m : list) {
      m.setTo(rootIdentity.getRemoteId());
      messages.add(m);
    }
    Writer writer = new StringWriter();
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfos(messages);
    getPlugin().buildDigest(ctx, writer);
    
    assertDigest(writer, "The following users have asked to join the my space 1 space: Demo gtn, John Anthony.");
    notificationService.clearAll();
    
    //OFF
    turnOFF(getPlugin());
    
    //Make request
    spaceService.addPendingUser(space, maryIdentity.getRemoteId());
    assertMadeNotifications(0);
    //
    turnON(getPlugin());
  }
  
  public void testDigestWithFeatureOFF() throws Exception {
    //
    Space space = getSpaceInstance(1);
    //Make requests to join space
    spaceService.addPendingUser(space, demoIdentity.getRemoteId());
    spaceService.addPendingUser(space, maryIdentity.getRemoteId());
    
    List<NotificationInfo> list = assertMadeNotifications(2);
    List<NotificationInfo> messages = new ArrayList<NotificationInfo>();
    for (NotificationInfo m : list) {
      m.setTo(rootIdentity.getRemoteId());
      messages.add(m);
    }
    Writer writer = new StringWriter();
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfos(messages);
    getPlugin().buildDigest(ctx, writer);
    
    assertDigest(writer, "The following users have asked to join the my space 1 space: Demo gtn, Mary Kelly.");
    notificationService.clearAll();
    
    //OFF
    turnFeatureOff();
    
    //Make request
    spaceService.addPendingUser(space, johnIdentity.getRemoteId());
    makeRelationship(johnIdentity, rootIdentity);
    
    assertMadeNotifications(0);
    //
    turnFeatureOn();
  }
  
  public void testDigestWithFeatureON() throws Exception {
    //
    turnFeatureOff();
    //
    Space space = getSpaceInstance(1);
    //Make requests to join space
    spaceService.addPendingUser(space, demoIdentity.getRemoteId());
    assertMadeNotifications(0);
    
    //ON
    turnFeatureOn();
    spaceService.addPendingUser(space, johnIdentity.getRemoteId());
    spaceService.addPendingUser(space, maryIdentity.getRemoteId());
    
    List<NotificationInfo> list = assertMadeNotifications(2);
    List<NotificationInfo> messages = new ArrayList<NotificationInfo>();
    for (NotificationInfo m : list) {
      m.setTo(rootIdentity.getRemoteId());
      messages.add(m);
    }
    Writer writer = new StringWriter();
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfos(messages);
    getPlugin().buildDigest(ctx, writer);
    
    assertDigest(writer, "The following users have asked to join the my space 1 space: John Anthony, Mary Kelly.");
    notificationService.clearAll();
    
  }

  public void testDigestCancelRequest() throws Exception {
    Space space = getSpaceInstance(1);
    spaceService.addPendingUser(space, demoIdentity.getRemoteId());
    spaceService.addPendingUser(space, johnIdentity.getRemoteId());
    spaceService.addPendingUser(space, maryIdentity.getRemoteId());
    
    List<NotificationInfo> list = assertMadeNotifications(3);
    List<NotificationInfo> messages = new ArrayList<NotificationInfo>();
    for (NotificationInfo m : list) {
      m.setTo(rootIdentity.getRemoteId());
      messages.add(m);
    }
    
    //john cancel his request to join space
    spaceService.removePendingUser(space, johnIdentity.getRemoteId());
    
    Writer writer = new StringWriter();
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfos(messages);
    getPlugin().buildDigest(ctx, writer);
    
    assertDigest(writer, "The following users have asked to join the my space 1 space: Demo gtn, Mary Kelly.");
    notificationService.clearAll();
    
  }
}
