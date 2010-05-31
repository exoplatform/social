/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.benches;

import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.jcr.SocialDataLocation;


/**
 * replacement component for ActivityManager that decorates entry methods by
 * perf logs
 *
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class TracingActivityManager extends ActivityManager {

  private ActivityManager  activityManager;

  private static final Log LOG = ExoLogger.getExoLogger(TracingActivityManager.class);

  public TracingActivityManager(SocialDataLocation dataLocation,
                                IdentityManager identityManager) throws Exception {
    super(dataLocation, identityManager);
    this.activityManager = new ActivityManager (dataLocation, identityManager);
  }

  public List<Activity> getActivities(Identity identity) throws Exception {
    long t1 = System.currentTimeMillis();
    try {
      return activityManager.getActivities(identity);
    } finally {
      LOG.info("getActivities() : " + (System.currentTimeMillis() - t1) + "ms");
    }
  }
}
