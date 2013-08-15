package org.exoplatform.social.notification.mock;

import java.util.List;

import org.exoplatform.commons.api.notification.model.UserSetting;
import org.exoplatform.commons.api.notification.service.setting.UserSettingService;

public class MockUserSettingServiceImpl implements UserSettingService {

  @Override
  public void save(UserSetting notificationSetting) {
    // TODO Auto-generated method stub

  }

  @Override
  public UserSetting get(String userId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<UserSetting> getDaily(int offset, int limit) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public long getNumberOfDaily() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public List<UserSetting> getDefaultDaily() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getUserSettingByPlugin(String pluginId) {
    // TODO Auto-generated method stub
    return null;
  }

}
