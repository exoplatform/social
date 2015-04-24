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
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.notification.channel.MailChannel;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.notification.AbstractPluginTest;
import org.exoplatform.social.notification.plugin.SpaceInvitationPlugin;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 14, 2014  
 */
public class SpaceInvitationMailBuilderTest extends AbstractPluginTest {
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
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(SpaceInvitationPlugin.ID)));
    return channel.getTemplateBuilder(PluginKey.key(SpaceInvitationPlugin.ID));
  }
  
  @Override
  public BaseNotificationPlugin getPlugin() {
    return pluginService.getPlugin(PluginKey.key(SpaceInvitationPlugin.ID));
  }
  
  public void testSimpleCase() throws Exception {
    //
    Space space = getSpaceInstance(1);
    
    //Invite user to join space
    spaceService.addInvitedUser(space, maryIdentity.getRemoteId());
    List<NotificationInfo> list = assertMadeNotifications(1);
    
    //assert Message Info
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(list.get(0).setTo(rootIdentity.getRemoteId()));
    MessageInfo message = buildMessageInfo(ctx);
    
    assertSubject(message, "You've been invited to join " + space.getDisplayName() + " space");
    assertBody(message, "You've received an invitation to join");
    notificationService.clearAll();
  }
  
  public void testDigestWithPluginON() throws Exception {
    //OFF
    turnOFF(getPlugin());
    //
    Space space = getSpaceInstance(1);
    //Invite user to join space
    spaceService.addInvitedUser(space, maryIdentity.getRemoteId());
    assertMadeNotifications(0);
    
    //ON
    turnON(getPlugin());
    
    //Make more invitations
    Space space2 = getSpaceInstance(2);
    Space space3 = getSpaceInstance(3);
    spaceService.addInvitedUser(space2, maryIdentity.getRemoteId());
    spaceService.addInvitedUser(space3, maryIdentity.getRemoteId());
    
    //assert Digest message
    List<NotificationInfo> ntfs = assertMadeNotifications(2);
    List<NotificationInfo> messages = new ArrayList<NotificationInfo>();
    for (NotificationInfo m : ntfs) {
      m.setTo(maryIdentity.getRemoteId());
      messages.add(m);
    }
    Writer writer = new StringWriter();
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfos(messages);
    buildDigest(ctx, writer);
    
    assertDigest(writer, "You have been asked to joing the following spaces: my space 2, my space 3.");
    notificationService.clearAll();
    
  }
  
  public void testDigestWithFeatureON() throws Exception {
    //
    turnFeatureOff();
    
    //Make invitation
    Space space1 = getSpaceInstance(1);
    spaceService.addInvitedUser(space1, maryIdentity.getRemoteId());
    assertMadeNotifications(0);
    
    //ON
    turnFeatureOn();
    
    Space space2 = getSpaceInstance(2);
    Space space3 = getSpaceInstance(3); 
    spaceService.addInvitedUser(space2, maryIdentity.getRemoteId());
    spaceService.addInvitedUser(space3, maryIdentity.getRemoteId());
    //assert Digest message
    List<NotificationInfo> ntfs = assertMadeNotifications(2);
    List<NotificationInfo> messages = new ArrayList<NotificationInfo>();
    for (NotificationInfo m : ntfs) {
      m.setTo(maryIdentity.getRemoteId());
      messages.add(m);
    }
    Writer writer = new StringWriter();
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfos(messages);
    buildDigest(ctx, writer);
    
    assertDigest(writer, "You have been asked to joing the following spaces: my space 2, my space 3.");
    notificationService.clearAll();
    
  }
  public void testDigestCancelInvitation() throws Exception {
    //Make more invitations
    Space space1 = getSpaceInstance(1);
    Space space2 = getSpaceInstance(2);
    Space space3 = getSpaceInstance(3);
    Space space4 = getSpaceInstance(4);
    Space space5 = getSpaceInstance(5);
    spaceService.addInvitedUser(space1, demoIdentity.getRemoteId());
    spaceService.addInvitedUser(space2, demoIdentity.getRemoteId());
    spaceService.addInvitedUser(space3, demoIdentity.getRemoteId());
    spaceService.addInvitedUser(space4, demoIdentity.getRemoteId());
    spaceService.addInvitedUser(space5, demoIdentity.getRemoteId());
    
    //assert Digest message
    List<NotificationInfo> ntfs = assertMadeNotifications(5);
    List<NotificationInfo> messages = new ArrayList<NotificationInfo>();
    for (NotificationInfo m : ntfs) {
      m.setTo(demoIdentity.getRemoteId());
      messages.add(m);
    }
    
    //space2 cancel invitation to demo
    spaceService.removeInvitedUser(space2, demoIdentity.getRemoteId());
    
    Writer writer = new StringWriter();
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfos(messages);
    buildDigest(ctx, writer);
    
    assertDigest(writer, "You have been asked to joing the following spaces: my space 1, my space 3, my space 4 and 1 others.");
    notificationService.clearAll();
    
  }  
}
