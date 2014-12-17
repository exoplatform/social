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
import java.util.LinkedHashSet;
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
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.notification.AbstractPluginTest;
import org.exoplatform.social.notification.plugin.NewUserPlugin;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 14, 2014  
 */
public class NewUserMailBuilderTest extends AbstractPluginTest {
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
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(NewUserPlugin.ID)));
    return channel.getTemplateBuilder(PluginKey.key(NewUserPlugin.ID));
  }
  
  @Override
  public AbstractNotificationPlugin getPlugin() {
    return pluginService.getPlugin(PluginKey.key(NewUserPlugin.ID));
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
    buildDigest(ctx, writer);
    assertDigest(writer, "Ghost gtn, Paul gtn have joined eXo.");
    
    tearDownIdentityList.add(ghostIdentity);
    tearDownIdentityList.add(raulIdentity);
    tearDownIdentityList.add(paulIdentity);
  }
}
