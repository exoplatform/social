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
import java.util.Map;

import org.exoplatform.commons.api.notification.plugin.ActiveProviderPlugin;
import org.exoplatform.commons.api.notification.plugin.GroupProviderModel;
import org.exoplatform.commons.api.notification.plugin.GroupProviderPlugin;
import org.exoplatform.commons.api.notification.service.setting.ProviderSettingService;

public class MockProviderSettingService implements ProviderSettingService {

  @Override
  public void registerActiveProviderPlugin(ActiveProviderPlugin activeProviderPlugin) {
    

  }

  @Override
  public void registerGroupProviderPlugin(GroupProviderPlugin groupProviderPlugin) {
    

  }

  @Override
  public List<String> getActiveProviderIds(boolean isAdmin) {
    
    return null;
  }

  @Override
  public Map<String, Boolean> getActiveProviderIdForSetting() {
    
    return null;
  }

  @Override
  public void setActiveProviders(Map<String, Boolean> mapProviderId) {
    

  }

  @Override
  public List<GroupProviderModel> getGroupProviders() {
    
    return null;
  }

  @Override
  public boolean getActiveFeature() {
    
    return true;
  }

  @Override
  public void saveActiveFeature(boolean isActive) {
    

  }

}
