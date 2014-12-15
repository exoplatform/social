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

import java.io.Writer;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.channel.template.AbstractTemplateBuilder;
import org.exoplatform.commons.api.notification.channel.template.TemplateProvider;
import org.exoplatform.commons.api.notification.model.MessageInfo;
import org.exoplatform.commons.api.notification.model.PluginKey;
import org.exoplatform.container.xml.InitParams;
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
public class WebTemplateProvider extends TemplateProvider {
  
  /** Defines the template builder for ActivityCommentPlugin*/
  private AbstractTemplateBuilder comment = new AbstractTemplateBuilder() {

    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      // TODO Auto-generated method stub
      return false;
    }
    
  };
  
  /** Defines the template builder for ActivityMentionPlugin*/
  private AbstractTemplateBuilder mention = new AbstractTemplateBuilder() {

    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      // TODO Auto-generated method stub
      return false;
    }
    
  };
  
  /** Defines the template builder for LikePlugin*/
  private AbstractTemplateBuilder like = new AbstractTemplateBuilder() {

    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      // TODO Auto-generated method stub
      return false;
    }
   
  };
  
  /** Defines the template builder for NewUserPlugin*/
  private AbstractTemplateBuilder newUser = new AbstractTemplateBuilder() {

    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      // TODO Auto-generated method stub
      return false;
    }
    
    
  };
  
  /** Defines the template builder for PostActivityPlugin*/
  private AbstractTemplateBuilder postActivity = new AbstractTemplateBuilder() {

    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      // TODO Auto-generated method stub
      return false;
    }
    
    
  };
  
  /** Defines the template builder for PostActivitySpaceStreamPlugin*/
  private AbstractTemplateBuilder postActivitySpace = new AbstractTemplateBuilder() {

    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      // TODO Auto-generated method stub
      return false;
    }
    
    
  };
  
  /** Defines the template builder for RelationshipReceivedRequestPlugin*/
  private AbstractTemplateBuilder relationshipReceived = new AbstractTemplateBuilder() {

    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      // TODO Auto-generated method stub
      return false;
    }
    
  };
  
  /** Defines the template builder for RequestJoinSpacePlugin*/
  private AbstractTemplateBuilder requestJoinSpace = new AbstractTemplateBuilder() {

    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      // TODO Auto-generated method stub
      return false;
    }
    
    
  };
  
  /** Defines the template builder for SpaceInvitationPlugin*/
  private AbstractTemplateBuilder spaceInvitation = new AbstractTemplateBuilder() {

    @Override
    protected MessageInfo makeMessage(NotificationContext ctx) {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    protected boolean makeDigest(NotificationContext ctx, Writer writer) {
      // TODO Auto-generated method stub
      return false;
    }
    
    
  };
  
  public WebTemplateProvider(InitParams initParams) {
    super(initParams);
    this.templateBuilders.put(PluginKey.key(ActivityCommentPlugin.ID), comment);
    this.templateBuilders.put(PluginKey.key(ActivityMentionPlugin.ID), mention);
    this.templateBuilders.put(PluginKey.key(LikePlugin.ID), like);
    this.templateBuilders.put(PluginKey.key(NewUserPlugin.ID), newUser);
    this.templateBuilders.put(PluginKey.key(PostActivityPlugin.ID), postActivity);
    this.templateBuilders.put(PluginKey.key(PostActivitySpaceStreamPlugin.ID), postActivitySpace);
    this.templateBuilders.put(PluginKey.key(RelationshipReceivedRequestPlugin.ID), relationshipReceived);
    this.templateBuilders.put(PluginKey.key(RequestJoinSpacePlugin.ID), requestJoinSpace);
    this.templateBuilders.put(PluginKey.key(SpaceInvitationPlugin.ID), spaceInvitation);
  }

}
