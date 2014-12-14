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

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.social.notification.AbstractPluginTest;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Dec 14, 2014  
 */
public abstract class AbstractTemplateBuilderTest extends AbstractPluginTest {
  
  public abstract AbstractTemplateBuilder getTemplateBuilder();
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  /**
   * It will be invoked after the notification will be created.
   * 
   * Makes the Message Info by the plugin and NotificationContext
   * @ctx the provided NotificationContext
   * @return
   */
  @Override
  protected MessageInfo buildMessageInfo(NotificationContext ctx) {
    AbstractTemplateBuilder templateBuilder = getTemplateBuilder();
    MessageInfo massage = templateBuilder.buildMessage(ctx);
    assertNotNull(massage);
    return massage;
  }
}
