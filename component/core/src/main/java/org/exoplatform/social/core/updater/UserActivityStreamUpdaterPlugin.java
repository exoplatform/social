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
package org.exoplatform.social.core.updater;

import org.exoplatform.commons.version.util.VersionComparator;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;

public class UserActivityStreamUpdaterPlugin extends AbstractUpdaterPlugin {
  
  private UserActivityStreamMigration streamMigration = null;
  
  public UserActivityStreamUpdaterPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    getStreamMigration().upgrade();
  }
  
  private UserActivityStreamMigration getStreamMigration() {
    if (this.streamMigration == null) {
       this.streamMigration = (UserActivityStreamMigration) PortalContainer.getInstance().getComponentInstanceOfType(UserActivityStreamMigration.class);
    }
    return streamMigration;
  }
  
  @Override
  public boolean shouldProceedToUpgrade(String newVersion, String previousVersion) {
    return VersionComparator.isAfter(newVersion, previousVersion);
  }
  
}