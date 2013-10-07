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
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class UserActivityStreamUpdaterPlugin extends AbstractUpdaterPlugin {
  
  private static final Log LOG = ExoLogger.getLogger(UserActivityStreamUpdaterPlugin.class);
  
  private UserActivityStreamMigration streamMigration = null;
  
  public int limit = -1;
  
  public UserActivityStreamUpdaterPlugin(InitParams initParams) {
    super(initParams);
    if (initParams.containsKey("limit")) {
      try {
        String value = initParams.getValueParam("limit").getValue();
        if (value != null) {
          limit = Integer.valueOf(value);
        }
      } catch (NumberFormatException e) {
        LOG.warn("Integer number expected for property " + name);
      }

    }
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    getStreamMigration().upgrade(this.limit);
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