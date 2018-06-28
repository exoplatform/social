/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.notification.impl;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.NotificationMessageUtils;
import org.exoplatform.commons.api.notification.channel.AbstractChannel;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.model.*;
import org.exoplatform.commons.api.notification.plugin.BaseNotificationPlugin;
import org.exoplatform.commons.api.notification.service.WebNotificationService;
import org.exoplatform.commons.notification.channel.WebChannel;
import org.exoplatform.commons.notification.impl.NotificationContextImpl;
import org.exoplatform.commons.notification.net.WebNotificationSender;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;
import org.exoplatform.social.notification.plugin.RequestJoinSpacePlugin;
import org.exoplatform.social.notification.plugin.SocialNotificationUtils;
import org.exoplatform.social.notification.plugin.SpaceInvitationPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpaceNotificationImpl extends SpaceListenerPlugin {

  private WebNotificationService webNotificationService;

  private static final Log LOG = ExoLogger.getExoLogger(SpaceNotificationImpl.class);

  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {}

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {}

  @Override
  public void applicationAdded(SpaceLifeCycleEvent event) {}

  @Override
  public void applicationRemoved(SpaceLifeCycleEvent event) {}

  @Override
  public void applicationActivated(SpaceLifeCycleEvent event) {}

  @Override
  public void applicationDeactivated(SpaceLifeCycleEvent event) {}

  @Override
  public void joined(SpaceLifeCycleEvent event) {
    WebNotificationService webNotificationService = getWebNotificationService();
    if (webNotificationService != null) {
      Space space = event.getSpace();
      String userId = event.getTarget();
      WebNotificationFilter webNotificationFilter = new WebNotificationFilter(userId);
      webNotificationFilter.setParameter("spaceId", space.getId());
      PluginKey pluginKey = new PluginKey(SpaceInvitationPlugin.ID);
      webNotificationFilter.setPluginKey(pluginKey);
      List<NotificationInfo> webNotifs = webNotificationService.getNotificationInfos(webNotificationFilter, 0, -1);
      Map<String, String> ownerParameter = new HashMap<String, String>();
      ownerParameter.put("spaceId", space.getId());
      ownerParameter.put("status", "accepted");
      for (NotificationInfo info : webNotifs) {
        info.setTo(userId);
        info.key(new PluginKey("SpaceInvitationPlugin"));
        info.setOwnerParameter(ownerParameter);
        updateNotification(info);
      }
    } else {
      LOG.error("Cannot update web notfication. WebNotificationService is null");
    }
  }

  @Override
  public void left(SpaceLifeCycleEvent event) {}

  @Override
  public void grantedLead(SpaceLifeCycleEvent event) {}

  @Override
  public void revokedLead(SpaceLifeCycleEvent event) {}

  @Override
  public void spaceRenamed(SpaceLifeCycleEvent event) {}

  @Override
  public void spaceDescriptionEdited(SpaceLifeCycleEvent event) {}

  @Override
  public void spaceAvatarEdited(SpaceLifeCycleEvent event) {}

  @Override
  public void spaceAccessEdited(SpaceLifeCycleEvent event) {}
  
  @Override
  public void addInvitedUser(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String userId = event.getTarget();
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance().append(SocialNotificationUtils.REMOTE_ID, userId)
                                                             .append(SocialNotificationUtils.SPACE, space);
    
    ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(SpaceInvitationPlugin.ID))).execute(ctx);
  }
  
  @Override
  public void addPendingUser(SpaceLifeCycleEvent event) {
    Space space = event.getSpace();
    String userId = event.getTarget();
    
    NotificationContext ctx = NotificationContextImpl.cloneInstance().append(SocialNotificationUtils.REMOTE_ID, userId)
                                                             .append(SocialNotificationUtils.SPACE, space);
    
    ctx.getNotificationExecutor().with(ctx.makeCommand(PluginKey.key(RequestJoinSpacePlugin.ID))).execute(ctx);
    
  }

  @Override
  public void spaceBannerEdited(SpaceLifeCycleEvent event) {}

  private WebNotificationService getWebNotificationService() {
    if (webNotificationService == null) {
      webNotificationService = CommonsUtils.getService(WebNotificationService.class);
    }
    return webNotificationService;
  }

  private MessageInfo updateNotification(NotificationInfo notification) {
    NotificationContext nCtx = NotificationContextImpl.cloneInstance().setNotificationInfo(notification);
    BaseNotificationPlugin plugin = nCtx.getPluginContainer().getPlugin(notification.getKey());
    if (plugin == null) {
      return null;
    }
    try {
      AbstractChannel channel = nCtx.getChannelManager().getChannel(ChannelKey.key(WebChannel.ID));
      AbstractTemplateBuilder builder = channel.getTemplateBuilder(notification.getKey());
      MessageInfo msg = builder.buildMessage(nCtx);
      msg.setMoveTop(false);
      WebNotificationSender.sendJsonMessage(notification.getTo(), msg);
      notification.setTitle(msg.getBody());
      notification.with(NotificationMessageUtils.SHOW_POPOVER_PROPERTY.getKey(), "true")
          .with(NotificationMessageUtils.READ_PORPERTY.getKey(), "false");
      getWebNotificationService().save(notification);
      return msg;
    } catch (Exception e) {
      LOG.error("Can not update space invitation notification.", e.getMessage());
      return null;
    }
  }
}
