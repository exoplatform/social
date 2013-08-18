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
package org.exoplatform.social.notification.mock;

import java.util.List;

import org.exoplatform.commons.api.notification.model.GroupProvider;
import org.exoplatform.commons.api.notification.model.PluginInfo;
import org.exoplatform.commons.api.notification.plugin.GroupProviderPlugin;
import org.exoplatform.commons.api.notification.plugin.config.PluginConfig;
import org.exoplatform.commons.api.notification.service.setting.PluginConfigService;

public class MockProviderSettingService implements PluginConfigService {

  @Override
  public void registerPluginConfig(PluginConfig pluginConfig) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void registerGroupConfig(GroupProviderPlugin groupConfig) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public PluginConfig getPluginConfig(String pluginId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<GroupProvider> getGroupPlugins() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void savePlugin(String providerId, boolean isActive) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public boolean isActive(String providerId) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public List<String> getActivePluginIds() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<PluginInfo> getActivePlugins() {
    // TODO Auto-generated method stub
    return null;
  }

}
