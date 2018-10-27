package org.exoplatform.social.core.updater;

import java.util.List;

import static org.exoplatform.commons.notification.impl.AbstractService.EXO_IS_ACTIVE;

import org.exoplatform.commons.api.notification.model.PluginInfo;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.setting.PluginSettingService;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import org.exoplatform.commons.persistence.impl.EntityManagerService;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class EditActivityNotificationSettingsUpgradePlugin extends UpgradeProductPlugin {

    private static final Log LOG = ExoLogger.getLogger(EditActivityNotificationSettingsUpgradePlugin.class);

    private SettingService settingService;

    private UserSettingService userSettingService;

    private PluginSettingService pluginSettingService;

    private EntityManagerService entityManagerService;

    public EditActivityNotificationSettingsUpgradePlugin(SettingService settingService,
                                                         UserSettingService userSettingService,
                                                         PluginSettingService pluginSettingService,
                                                         EntityManagerService entityManagerService,
                                                         InitParams initParams) {
        super(settingService, initParams);
        this.settingService = settingService;
        this.userSettingService = userSettingService;
        this.pluginSettingService = pluginSettingService;
        this.entityManagerService = entityManagerService;
    }

    @Override
    public void processUpgrade(String oldVersion, String newVersion) {
        int pageSize = 20;
        int current = 0;
        ExoContainer currentContainer = ExoContainerContext.getCurrentContainer();
        try {
            LOG.info("=== Start initialisation of Edit Activity Notifications settings");
            LOG.info("  Starting activating Edit Activity Notifications for users");

            PluginInfo editActivityConfig = findPlugin("org.exoplatform.social.notification.plugin.EditActivityPlugin");
            PluginInfo editCommentConfig = findPlugin("org.exoplatform.social.notification.plugin.EditCommentPlugin");
            List<Context> usersContexts;

            entityManagerService.startRequest(currentContainer);
            long startTime = System.currentTimeMillis();
            do {
                LOG.info("  Progression of users Edit Activity Notifications settings initialisation : {} users", current);

                // Get all users who already update their notification settings
                usersContexts = settingService.getContextsByTypeAndScopeAndSettingName(Context.USER.getName(), Scope.APPLICATION.getName(),
                        "NOTIFICATION", EXO_IS_ACTIVE, current, pageSize);

                if (usersContexts != null) {
                    for (Context userContext : usersContexts) {
                        try {
                            entityManagerService.endRequest(currentContainer);
                            entityManagerService.startRequest(currentContainer);

                            String userName = userContext.getId();
                            UserSetting userSetting = this.userSettingService.get(userName);
                            updateSetting(userSetting, editActivityConfig);
                            updateSetting(userSetting, editCommentConfig);
                            userSettingService.save(userSetting);
                        } catch (Exception e) {
                            LOG.error("  Error while activating Edit Activity Notifications for user " + userContext.getId(), e);
                        }
                    }
                    current += usersContexts.size();
                }
            } while (usersContexts != null && !usersContexts.isEmpty());
            long endTime = System.currentTimeMillis();
            LOG.info("  Users Edit Activity Notifications settings initialised in " + (endTime - startTime) + " ms");
        } catch (Exception e) {
            LOG.error("Error while initialisation of users Edit Activity Notifications settings - Cause : " + e.getMessage(), e);
        } finally {
            entityManagerService.endRequest(currentContainer);
        }

        LOG.info("=== {} users with modified notifications settings have been found and processed successfully", current);
        LOG.info("=== End initialisation of Edit Activity Notifications settings");
    }

    private PluginInfo findPlugin(String type) {
        for (PluginInfo plugin : pluginSettingService.getAllPlugins()) {
            if (plugin.getType().equals(type)) {
                return plugin;
            }
        }
        return null;
    }

    private void updateSetting(UserSetting userSetting, PluginInfo config) {
        for (String defaultConf : config.getDefaultConfig()) {
            for (String channelId : config.getAllChannelActive()) {
                if (UserSetting.FREQUENCY.getFrequecy(defaultConf) == UserSetting.FREQUENCY.INSTANTLY) {
                    userSetting.addChannelPlugin(channelId, config.getType());
                } else {
                    userSetting.addPlugin(config.getType(), UserSetting.FREQUENCY.getFrequecy(defaultConf));
                }
            }
        }
    }
}
