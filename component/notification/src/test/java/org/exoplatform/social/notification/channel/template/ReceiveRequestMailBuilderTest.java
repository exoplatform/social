/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.social.notification.channel.template;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.NotificationInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.api.notification.plugin.AbstractNotificationPlugin;
import org.exoplatform.commons.notification.channel.MailChannel;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.social.notification.AbstractPluginTest;
import org.exoplatform.social.notification.plugin.RelationshipReceivedRequestPlugin;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 14, 2014  
 */
public class ReceiveRequestMailBuilderTest extends AbstractPluginTest {
  private ChannelManager manager;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    manager = getService(ChannelManager.class);
  }
  
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
  }
  

  @Override
  public AbstractTemplateBuilder getTemplateBuilder() {
    AbstractChannel channel = manager.getChannel(ChannelKey.key(MailChannel.ID));
    assertTrue(channel != null);
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(RelationshipReceivedRequestPlugin.ID)));
    return channel.getTemplateBuilder(PluginKey.key(RelationshipReceivedRequestPlugin.ID));
  }
  
  @Override
  public AbstractNotificationPlugin getPlugin() {
    return pluginService.getPlugin(PluginKey.key(RelationshipReceivedRequestPlugin.ID));
  }
  
  public void testSimpleCase() throws Exception {
    //
    makeRelationship(demoIdentity, rootIdentity);
    List<NotificationInfo> list = assertMadeNotifications(1);
    
    NotificationInfo ntf = list.get(0);
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(ntf.setTo("root"));
    MessageInfo message = buildMessageInfo(ctx);
    
    assertBody(message, "New connection request");
    assertSubject(message, demoIdentity.getProfile().getFullName() +" wants to connect with you on eXo");
    notificationService.clearAll();
  }
  
  public void testDigestWithPluginON() throws Exception {
    //
    turnOFF(getPlugin());
    //
    makeRelationship(johnIdentity, rootIdentity);
    assertMadeNotifications(0);
    
    //ON
    turnON(getPlugin());
    
    //Make more relationship
    makeRelationship(demoIdentity, rootIdentity);
    makeRelationship(maryIdentity, rootIdentity);
    //
    List<NotificationInfo> messages = new ArrayList<NotificationInfo>();
    List<NotificationInfo> list = assertMadeNotifications(2);
    for (NotificationInfo m : list) {
      m.setTo(rootIdentity.getRemoteId());
      messages.add(m);
    }
    
    Writer writer = new StringWriter();
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfos(messages);
    buildDigest(ctx, writer);
    
    assertDigest(writer, "You've received a connection request from Demo gtn, Mary Kelly.");
    notificationService.clearAll();
    
  }
  
  public void testDigestWithFeatureON() throws Exception {
    //
    turnFeatureOff();
    //
    makeRelationship(demoIdentity, rootIdentity);
    assertMadeNotifications(0);
    
    //ON
    turnFeatureOn();
    
    //Make more relationship
    makeRelationship(johnIdentity, rootIdentity);
    makeRelationship(maryIdentity, rootIdentity);
    //
    List<NotificationInfo> messages = new ArrayList<NotificationInfo>();
    List<NotificationInfo> list = assertMadeNotifications(2);
    for (NotificationInfo m : list) {
      m.setTo(rootIdentity.getRemoteId());
      messages.add(m);
    }
    
    Writer writer = new StringWriter();
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfos(messages);
    buildDigest(ctx, writer);
    
    assertDigest(writer, "You've received a connection request from John Anthony, Mary Kelly.");
    notificationService.clearAll();
    
  }
  
  public void testDigestCancelRequest() throws Exception {
    //Make more relationship
    makeRelationship(demoIdentity, rootIdentity);
    makeRelationship(maryIdentity, rootIdentity);
    makeRelationship(johnIdentity, rootIdentity);
    
    //
    List<NotificationInfo> messages = new ArrayList<NotificationInfo>();
    List<NotificationInfo> list = assertMadeNotifications(3);
    for (NotificationInfo m : list) {
      m.setTo(rootIdentity.getRemoteId());
      messages.add(m);
    }
    
    //cancel 2 request
    cancelRelationship(rootIdentity, demoIdentity);
    cancelRelationship(rootIdentity, johnIdentity);
    
    Writer writer = new StringWriter();
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfos(messages);
    buildDigest(ctx, writer);
    
    assertDigest(writer, "You've received a connection request from Mary Kelly.");
    notificationService.clearAll();
  }
  

}
