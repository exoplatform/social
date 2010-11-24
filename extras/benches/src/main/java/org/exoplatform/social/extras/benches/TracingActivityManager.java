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
package org.exoplatform.social.extras.benches;

import java.util.List;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.CachingActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.ActivityStorage;
import org.exoplatform.social.core.storage.ActivityStorageException;


/**
 * replacement component for ActivityManager that decorates entry methods by
 * perf logs
 *
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class TracingActivityManager extends CachingActivityManager {

  private static final Log LOG = ExoLogger.getExoLogger(TracingActivityManager.class);

  private ActivityManager  activityManager;


  /**
   * Constructor
   *
   * @param activityStorage
   * @param identityManager
   * @param cacheService
   * @throws Exception
   */
  public TracingActivityManager(ActivityStorage activityStorage,
                                IdentityManager identityManager, CacheService cacheService) throws Exception {
    super(activityStorage, identityManager, cacheService);
    this.activityManager = new CachingActivityManager (activityStorage, identityManager, cacheService);
  }

  public List<ExoSocialActivity> getActivities(Identity identity) {
    long t1 = System.currentTimeMillis();
    try {
      return activityManager.getActivities(identity);
    } catch (ActivityStorageException e) {
      LOG.warn("Failed to getActivities");
    } finally {
      LOG.info("getActivities() : " + (System.currentTimeMillis() - t1) + "ms");
    }
    return null;
  }
}