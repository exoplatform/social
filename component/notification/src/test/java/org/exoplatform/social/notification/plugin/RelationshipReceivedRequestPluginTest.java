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
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.notification.AbstractPluginTest;
import org.exoplatform.social.notification.mock.MockMessageQueue;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Aug 20, 2013  
 */
public class RelationshipReceivedRequestPluginTest extends AbstractPluginTest {

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
    return pluginService.getPlugin(NotificationKey.key(RelationshipRecievedRequestPlugin.ID));
  }
  
  public void testInstantly() throws Exception {
    //
    List<String> settings = new ArrayList<String>();
    settings.add(getPlugin().getId());

    setInstantlySettings(demoIdentity.getRemoteId(), settings);

    Relationship relationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);

    NotificationInfo ntf = MockMessageQueue.get();
    assertNotNull(ntf);
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(ntf.setTo("demo"));
    MessageInfo message = getPlugin().buildMessage(ctx);
    
    assertBody(message, "New connection request");
    
    relationshipManager.remove(relationship);
    
  }
  
  public void testInstantlyWithDaily() throws Exception {
    //
    List<String> settings = new ArrayList<String>();
    settings.add(getPlugin().getId());

    setInstantlySettings(demoIdentity.getRemoteId(), settings);
    setWeeklySetting(demoIdentity.getRemoteId(), settings);

    Relationship relationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);

    NotificationInfo ntf = MockMessageQueue.get();
    assertNotNull(ntf);
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfo(ntf.setTo("demo"));
    MessageInfo message = getPlugin().buildMessage(ctx);
    
    assertBody(message, "New connection request");
    
    Relationship relationship2 = relationshipManager.inviteToConnect(maryIdentity, demoIdentity);
    
    //
    List<NotificationInfo> messages = new ArrayList<NotificationInfo>();
    for (NotificationInfo m : getNotificationInfos()) {
      m.setTo(demoIdentity.getRemoteId());
      messages.add(m);
    }
    
    Writer writer = new StringWriter();
    ctx = NotificationContextImpl.cloneInstance();
    ctx.setNotificationInfos(messages);
    getPlugin().buildDigest(ctx, writer);
    
    System.err.println("testInstantlyWithDaily with Body == "+ writer.toString());
    
    relationshipManager.remove(relationship);
    relationshipManager.remove(relationship2);
    
  }
  
}
