package org.exoplatform.social.notification.upgrade;

import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.version.util.VersionComparator;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.profile.ProfileFilter;

public class UserNotificationSettingUpdaterPlugin extends UpgradeProductPlugin {
  
  private static final Log LOG = ExoLogger.getLogger(UserNotificationSettingUpdaterPlugin.class);

  public UserNotificationSettingUpdaterPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    IdentityManager identityManager = CommonsUtils.getService(IdentityManager.class);
    int offset = 0, limit = 30;
    ListAccess<Identity> list = identityManager.getIdentitiesByProfileFilter(OrganizationIdentityProvider.NAME, new ProfileFilter(), false);
    int loaded = upgradeInRange(offset, offset + limit, list);
    
    while (loaded == limit) {
      offset = offset + loaded;
      loaded = upgradeInRange(offset, offset + limit, list);
    }
    
  }

  @Override
  public boolean shouldProceedToUpgrade(String newVersion, String previousVersion) {
    return VersionComparator.isAfter(newVersion, previousVersion);
  }
  
  /**
   * Use lazyload to upgrade old users
   * 
   * @param offset
   * @param limit
   * @param list the list access of users
   * @return the size of identities after load
   */
  private int upgradeInRange(int offset, int limit, ListAccess<Identity> list) {
    UserSettingService userSettingService = CommonsUtils.getService(UserSettingService.class);
    int size = 0;
    try {
      Identity[] identities = list.load(offset, limit);
      size = identities.length;
      if (size == 0) return size;
      for (Identity identity : identities) {
        userSettingService.get(identity.getRemoteId());
      }
      
    } catch (Exception e) {
      LOG.error("Failed to upgrade user notification setting", e);
    }
    return size;
  }
}
