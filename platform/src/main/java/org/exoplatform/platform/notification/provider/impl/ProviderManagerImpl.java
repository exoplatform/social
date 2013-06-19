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
package org.exoplatform.platform.notification.provider.impl;

import java.util.List;

import org.exoplatform.platform.notification.Provider;
import org.exoplatform.platform.notification.provider.ProviderManager;
import org.picocontainer.Startable;

public class ProviderManagerImpl implements ProviderManager, Startable {

  
  public ProviderManagerImpl() {
    // TODO Auto-generated constructor stub
  }

  @Override
  public void start() {
    // TODO Auto-generated method stub

  }

  @Override
  public void stop() {
    // TODO Auto-generated method stub

  }

  @Override
  public void saveProvier(Provider provider) {
    // TODO Auto-generated method stub

  }

  @Override
  public Provider getProvier(String providerType) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Provider> getActiveProvier(boolean isAdmin) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<Provider> getAddProvier() {
    // TODO Auto-generated method stub
    return null;
  }

}
