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

import org.exoplatform.commons.api.notification.GroupProvider;
import org.exoplatform.commons.api.notification.model.ProviderData;
import org.exoplatform.commons.api.notification.plugin.GroupProviderPlugin;
import org.exoplatform.commons.api.notification.plugin.model.PluginConfig;
import org.exoplatform.commons.api.notification.service.setting.ProviderSettingService;

public class MockProviderSettingService implements ProviderSettingService {

  @Override
  public void registerPluginConfig(PluginConfig pluginConfig) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void registerGroupConfig(GroupProviderPlugin groupConfig) {
    
  }
  
  @Override
  public PluginConfig getPluginConfig(String pluginId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<GroupProvider> getGroupProviders() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void saveActiveProviders(List<ProviderData> providerDatas) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public List<String> getActiveProviderIds() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean getActiveFeature() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void saveActiveFeature(boolean isActive) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public List<ProviderData> getActiveProviders() {
    // TODO Auto-generated method stub
    return null;
  }

}
