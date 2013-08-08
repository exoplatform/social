package org.exoplatform.social.notification.mock;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;

public class MockSettingServiceImpl implements SettingService {

  @Override
  public void set(Context context, Scope scope, String key, SettingValue<?> value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void remove(Context context, Scope scope, String key) {
    // TODO Auto-generated method stub

  }

  @Override
  public void remove(Context context, Scope scope) {
    // TODO Auto-generated method stub

  }

  @Override
  public void remove(Context context) {
    // TODO Auto-generated method stub

  }

  @Override
  public SettingValue<?> get(Context context, Scope scope, String key) {
    // TODO Auto-generated method stub
    return null;
  }

}
