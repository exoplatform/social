/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.social.notification.mock;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          thanhvc@exoplatform.com
 * Aug 19, 2013  
 */
public class MockSettingServiceImpl implements SettingService {
  
  Map<String, SettingValue<?>> settingMap = new HashMap<String, SettingValue<?>>();

  @Override
  public void set(Context context, Scope scope, String key, SettingValue<?> value) {
    settingMap.put(key, value);

  }

  @Override
  public void remove(Context context, Scope scope, String key) {

  }

  @Override
  public void remove(Context context, Scope scope) {

  }

  @Override
  public void remove(Context context) {

  }

  @Override
  public SettingValue<?> get(Context context, Scope scope, String key) {
    return settingMap.get(key);
  }

}
