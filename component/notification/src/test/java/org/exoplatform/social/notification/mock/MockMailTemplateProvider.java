package org.exoplatform.social.notification.mock;

import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.notification.channel.template.MailTemplateProvider;
import org.exoplatform.social.notification.plugin.*;

@TemplateConfigs(templates = {
    @TemplateConfig(pluginId = ActivityCommentPlugin.ID, template = "classpath:/notification/templates/ActivityCommentPlugin.gtmpl"),
    @TemplateConfig(pluginId = ActivityReplyToCommentPlugin.ID, template = "classpath:/notification/templates/ActivityReplyToCommentPlugin.gtmpl"),
    @TemplateConfig(pluginId = ActivityMentionPlugin.ID, template = "classpath:/notification/templates/ActivityMentionPlugin.gtmpl"),
    @TemplateConfig(pluginId = LikePlugin.ID, template = "classpath:/notification/templates/LikePlugin.gtmpl"),
    @TemplateConfig(pluginId = EditActivityPlugin.ID, template = "classpath:/notification/templates/EditActivityPlugin.gtmpl"),
    @TemplateConfig(pluginId = EditCommentPlugin.ID, template = "classpath:/notification/templates/EditCommentPlugin.gtmpl"),
    @TemplateConfig(pluginId = LikeCommentPlugin.ID, template = "classpath:/notification/templates/LikeCommentPlugin.gtmpl"),
    @TemplateConfig(pluginId = NewUserPlugin.ID, template = "classpath:/notification/templates/NewUserPlugin.gtmpl"),
    @TemplateConfig(pluginId = PostActivityPlugin.ID, template = "classpath:/notification/templates/PostActivityPlugin.gtmpl"),
    @TemplateConfig(pluginId = PostActivitySpaceStreamPlugin.ID, template = "classpath:/notification/templates/PostActivitySpaceStreamPlugin.gtmpl"),
    @TemplateConfig(pluginId = RelationshipReceivedRequestPlugin.ID, template = "classpath:/notification/templates/RelationshipReceivedRequestPlugin.gtmpl"),
    @TemplateConfig(pluginId = RequestJoinSpacePlugin.ID, template = "classpath:/notification/templates/RequestJoinSpacePlugin.gtmpl"),
    @TemplateConfig(pluginId = SpaceInvitationPlugin.ID, template = "classpath:/notification/templates/SpaceInvitationPlugin.gtmpl")})
public class MockMailTemplateProvider extends MailTemplateProvider {

  public MockMailTemplateProvider(InitParams initParams) {
    super(initParams);
  }
}
