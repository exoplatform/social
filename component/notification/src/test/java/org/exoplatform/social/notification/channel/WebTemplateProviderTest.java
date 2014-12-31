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
package org.exoplatform.social.notification.channel;

import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.ChannelManager;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.ChannelKey;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.commons.notification.channel.WebChannel;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.social.notification.AbstractCoreTest;
import org.exoplatform.social.notification.channel.template.WebTemplateProvider;
import org.exoplatform.social.notification.mock.MockWebTemplateProvider;
import org.exoplatform.social.notification.plugin.ActivityCommentPlugin;
import org.exoplatform.social.notification.plugin.ActivityMentionPlugin;
import org.exoplatform.social.notification.plugin.LikePlugin;
import org.exoplatform.social.notification.plugin.NewUserPlugin;
import org.exoplatform.social.notification.plugin.PostActivityPlugin;
import org.exoplatform.social.notification.plugin.PostActivitySpaceStreamPlugin;
import org.exoplatform.social.notification.plugin.RelationshipReceivedRequestPlugin;
import org.exoplatform.social.notification.plugin.RequestJoinSpacePlugin;
import org.exoplatform.social.notification.plugin.SpaceInvitationPlugin;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 14, 2014  
 */
public class WebTemplateProviderTest extends AbstractCoreTest {
  private ChannelManager manager;
  private InitParams initParams;
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    manager = getService(ChannelManager.class);
    initParams = new InitParams();
    
    ValueParam valueParam = new ValueParam();
    valueParam.setName(TemplateProvider.CHANNEL_ID_KEY);
    valueParam.setValue(WebChannel.ID);
    initParams.addParameter(valueParam);
    manager.registerOverrideTemplateProvider(new WebTemplateProvider(initParams));
  }
  
  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    manager.registerOverrideTemplateProvider(new MockWebTemplateProvider(initParams));
  }
  
  public void testChannelSize() throws Exception {
    assertTrue(manager.sizeChannels() > 0);
  }
  
  public void testWebTemplateProvider() throws Exception {
    AbstractChannel channel = manager.getChannel(ChannelKey.key(WebChannel.ID));
    assertTrue(channel != null);
    
    String actual = channel.getTemplateFilePath(PluginKey.key(ActivityCommentPlugin.ID));
    String expected = "war:/intranet-notification/templates/ActivityCommentPlugin.gtmpl";
    assertEquals(expected, actual);
    
    actual = channel.getTemplateFilePath(PluginKey.key(ActivityMentionPlugin.ID));
    expected = "war:/intranet-notification/templates/ActivityMentionPlugin.gtmpl";
    assertEquals(expected, actual);
    
    actual = channel.getTemplateFilePath(PluginKey.key(LikePlugin.ID));
    expected = "war:/intranet-notification/templates/LikePlugin.gtmpl";
    assertEquals(expected, actual);
    
    actual = channel.getTemplateFilePath(PluginKey.key(NewUserPlugin.ID));
    expected = "war:/intranet-notification/templates/NewUserPlugin.gtmpl";
    assertEquals(expected, actual);
    
    actual = channel.getTemplateFilePath(PluginKey.key(PostActivityPlugin.ID));
    expected = "war:/intranet-notification/templates/PostActivityPlugin.gtmpl";
    assertEquals(expected, actual);
    
    actual = channel.getTemplateFilePath(PluginKey.key(PostActivitySpaceStreamPlugin.ID));
    expected = "war:/intranet-notification/templates/PostActivitySpaceStreamPlugin.gtmpl";
    assertEquals(expected, actual);
    
    actual = channel.getTemplateFilePath(PluginKey.key(RelationshipReceivedRequestPlugin.ID));
    expected = "war:/intranet-notification/templates/RelationshipReceivedRequestPlugin.gtmpl";
    assertEquals(expected, actual);
    
    actual = channel.getTemplateFilePath(PluginKey.key(RequestJoinSpacePlugin.ID));
    expected = "war:/intranet-notification/templates/RequestJoinSpacePlugin.gtmpl";
    assertEquals(expected, actual);
    
    actual = channel.getTemplateFilePath(PluginKey.key(SpaceInvitationPlugin.ID));
    expected = "war:/intranet-notification/templates/SpaceInvitationPlugin.gtmpl";
    assertEquals(expected, actual);
  }
  
  public void testWebTemplateBuilder() throws Exception {
    AbstractChannel channel = manager.getChannel(ChannelKey.key(WebChannel.ID));
    assertTrue(channel != null);
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(ActivityCommentPlugin.ID)));
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(ActivityMentionPlugin.ID)));
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(LikePlugin.ID)));
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(NewUserPlugin.ID)));
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(PostActivityPlugin.ID)));
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(PostActivitySpaceStreamPlugin.ID)));
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(RelationshipReceivedRequestPlugin.ID)));
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(RequestJoinSpacePlugin.ID)));
    assertTrue(channel.hasTemplateBuilder(PluginKey.key(SpaceInvitationPlugin.ID)));
  }
    
}
