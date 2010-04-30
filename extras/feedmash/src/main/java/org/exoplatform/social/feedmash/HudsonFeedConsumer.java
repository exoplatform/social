package org.exoplatform.social.feedmash;

import java.util.Date;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.identity.model.Identity;

import com.sun.syndication.feed.synd.SyndEntryImpl;

public class HudsonFeedConsumer extends AbstractFeedRepubJob {


  private static final Log LOG = ExoLogger.getLogger(HudsonFeedConsumer.class);
  private static final String HUDSON_STATUS = "status";

  enum BuildStatus {
    FAILURE, SUCCESS
  };

  @Override
  protected boolean accept(SyndEntryImpl entry) {
    if (alreadyChecked(entry.getUpdatedDate())) {
      return false;
    }
    String currentStatus = (String) getState(HUDSON_STATUS);

    if (currentStatus == null) {
      return true;
    } else {

      if (currentStatus.equals(BuildStatus.FAILURE.name())) {
        return entry.getTitle().contains(BuildStatus.SUCCESS.name());
      } else {
        return entry.getTitle().contains(BuildStatus.FAILURE.name());
      }
    }

  }

  @Override
  protected void handle(SyndEntryImpl entry) { 
    try {
      String currentStatus;

      if (entry.getTitle().contains(BuildStatus.SUCCESS.name())) {
        currentStatus = BuildStatus.SUCCESS.name();
      } else {
        currentStatus = BuildStatus.FAILURE.name();
      }

      LOG.debug("publishing hudson build status change on : "+ targetUser + "'s stream, status: " + currentStatus);   

      Identity identity = getIdentity(targetUser);
      if (identity == null) {
        return;
      }
      ActivityManager activityManager = getExoComponent(ActivityManager.class);
      String link = entry.getLink();
      activityManager.recordActivity(identity, "hudson", entry.getTitle(), link);
      saveState(HUDSON_STATUS, currentStatus);
      saveState(LAST_CHECKED, new Date());

    } catch (Exception e) {
      LOG.error("failed to publish hudson activity: " + e.getMessage(), e);
      
    }
  }



}
