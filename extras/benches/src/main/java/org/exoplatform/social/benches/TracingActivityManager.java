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
