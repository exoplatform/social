package org.exoplatform.social.notification.mock;

import org.exoplatform.commons.api.notification.annotation.TemplateConfig;
import org.exoplatform.commons.api.notification.annotation.TemplateConfigs;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.notification.channel.template.WebTemplateProvider;
import org.exoplatform.social.notification.plugin.*;

@TemplateConfigs(templates = {
    @TemplateConfig(pluginId = ActivityCommentPlugin.ID, template = "classpath:/notification/web/templates/ActivityCommentPlugin.gtmpl"),
    @TemplateConfig(pluginId = ActivityReplyToCommentPlugin.ID, template = "classpath:/notification/web/templates/ActivityReplyToCommentPlugin.gtmpl"),
    @TemplateConfig(pluginId = ActivityMentionPlugin.ID, template = "classpath:/notification/web/templates/ActivityMentionPlugin.gtmpl"),
    @TemplateConfig(pluginId = LikePlugin.ID, template = "classpath:/notification/web/templates/LikePlugin.gtmpl"),
    @TemplateConfig(pluginId = EditCommentPlugin.ID, template = "classpath:/notification/web/templates/EditCommentPlugin.gtmpl"),
    @TemplateConfig(pluginId = EditActivityPlugin.ID, template = "classpath:/notification/web/templates/EditActivityPlugin.gtmpl"),
    @TemplateConfig(pluginId = LikeCommentPlugin.ID, template = "classpath:/notification/web/templates/LikeCommentPlugin.gtmpl"),
    @TemplateConfig(pluginId = NewUserPlugin.ID, template = "classpath:/notification/web/templates/NewUserPlugin.gtmpl"),
    @TemplateConfig(pluginId = PostActivityPlugin.ID, template = "classpath:/notification/web/templates/PostActivityPlugin.gtmpl"),
    @TemplateConfig(pluginId = PostActivitySpaceStreamPlugin.ID, template = "classpath:/notification/web/templates/PostActivitySpaceStreamPlugin.gtmpl"),
    @TemplateConfig(pluginId = RelationshipReceivedRequestPlugin.ID, template = "classpath:/notification/web/templates/RelationshipReceivedRequestPlugin.gtmpl"),
    @TemplateConfig(pluginId = RequestJoinSpacePlugin.ID, template = "classpath:/notification/web/templates/RequestJoinSpacePlugin.gtmpl"),
    @TemplateConfig(pluginId = SpaceInvitationPlugin.ID, template = "classpath:/notification/web/templates/SpaceInvitationPlugin.gtmpl")})
public class MockWebTemplateProvider extends WebTemplateProvider {

  public MockWebTemplateProvider(InitParams initParams) {
    super(initParams);
  }

}
