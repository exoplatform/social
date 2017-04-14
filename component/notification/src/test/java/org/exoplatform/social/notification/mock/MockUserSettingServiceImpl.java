package org.exoplatform.social.notification.mock;

import org.exoplatform.commons.api.notification.NotificationContext;
import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;
import org.exoplatform.services.organization.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockUserSettingServiceImpl implements UserSettingService {

  private Map<String, UserSetting> settings = new HashMap<String, UserSetting>();
  
  public MockUserSettingServiceImpl() {
    
  }
  
  @Override
  public void save(UserSetting setting) {
    settings.put(setting.getUserId(), setting);
  }

  @Override
  public UserSetting get(String userId) {
    return settings.get(userId);
  }


  @Override
  public List<String> getUserSettingByPlugin(String pluginId) {
    List<String> userIds = new ArrayList<String>();
    for (UserSetting userSetting : settings.values()) {
      if (userSetting.isInDaily(pluginId) 
          || userSetting.isInWeekly(pluginId) 
          || userSetting.isActive(UserSetting.EMAIL_CHANNEL, pluginId)) {
        userIds.add(userSetting.getUserId());
      }
    }
    
    return userIds;
  }

  @Override
  public void initDefaultSettings(String userId) {
  }

  @Override
  public void initDefaultSettings(User[] users) {
  }

  @Override
  public List<UserSetting> getDigestSettingForAllUser(NotificationContext context,
                                                      int offset,
                                                      int limit) {
    return null;
  }

  @Override
  public List<UserSetting> getDigestDefaultSettingForAllUser(int offset, int limit) {
    return null;
  }

  @Override
  public List<UserSetting> getUserSettingWithDeactivate() {
    return null;
  }

  @Override
  public List<String> getUserHasSettingPlugin(String channelId, String pluginId) {
    return getUserSettingByPlugin(pluginId);
  }

  @Override
  public void saveLastReadDate(String userId, Long time) {
  }

}
