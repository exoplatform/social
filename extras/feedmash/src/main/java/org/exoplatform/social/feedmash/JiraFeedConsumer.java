package org.exoplatform.social.feedmash;



import java.util.Date;
import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.quartz.JobDataMap;

import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndEntryImpl;

public class JiraFeedConsumer extends AbstractFeedRepubJob {


  private static final Log LOG = ExoLogger.getLogger(JiraFeedConsumer.class);
  public JiraFeedConsumer() {}


  @Override
  public void beforeJobExecute(JobDataMap dataMap) {
    categoryMatch = dataMap.getString("categoryMatch");
  }

  protected void handle(SyndEntryImpl entry)  {
    try {
      String id = targetUser;
      LOG.debug("republishing jira activity on : "+ id + "'s stream, entry uri: " + entry.getLink());    
      Identity identity = getIdentity(targetUser);
      if (identity == null) {
        return;
      }

      ActivityManager activityManager = getExoComponent(ActivityManager.class);
      activityManager.recordActivity(identity, "jira", entry.getTitle(), entry.getDescription().getValue());
      saveState(LAST_CHECKED, new Date());
    } catch (Exception e) {
      LOG.error("failed to republish jira activity: " + e.getMessage(), e);
    }
  }



  protected boolean accept(SyndEntryImpl entry) {
    
    if (alreadyChecked(entry.getUpdatedDate())) {
      return false; // skipping entries already read
    }
    
    // find match by category
    List<SyndCategoryImpl> cats =  entry.getCategories(); 
    for (SyndCategoryImpl category : cats) {
      if (category.getName().matches(categoryMatch)) 
        return true;
    }
    
    return false; //(entry.getTitle().contains("created") || entry.getTitle().equals("resolved"));

  }
  


}
