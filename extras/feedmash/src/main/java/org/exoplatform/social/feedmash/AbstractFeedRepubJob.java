package org.exoplatform.social.feedmash;

import java.net.URL;
import java.util.Date;
import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public abstract class AbstractFeedRepubJob implements Job {

  private static final Log LOG = ExoLogger.getLogger(AbstractFeedRepubJob.class);
  
  protected static final String LAST_CHECKED = "lastChecked";
  
  protected String  categoryMatch;

  protected String  targetUser;

  protected String  portalContainer;

  protected String  feedUrl;

  protected Integer rampup = 5;
  
  protected String pluginName;

  @SuppressWarnings("unchecked")
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      JobDataMap dataMap = context.getJobDetail().getJobDataMap();
      pluginName = dataMap.getString("pluginName");
      targetUser = dataMap.getString("targetActivityStream");
      portalContainer = dataMap.getString("portalContainer");
      
      feedUrl = dataMap.getString("feedURL");

      // hack to before actually starting working otherwise picketlink fails
      // starting...
      rampup = (Integer) dataMap.get("rampup");
      if (rampup == null) {
        rampup = 2;
      }
      if (rampup > 1) {
        dataMap.put("rampup", --rampup);
        LOG.debug("waiting #" + rampup);
        return;
      }

      beforeJobExecute(dataMap);

      SyndFeedInput input = new SyndFeedInput();
      SyndFeed feed = input.build(new XmlReader(new URL(feedUrl)));

      List<SyndEntryImpl> entries = feed.getEntries();
      for (SyndEntryImpl entry : entries) {
        if (accept(entry)) {
          handle(entry);
        }
      }

    } catch (Exception e) {
      throw new JobExecutionException(e);
    }

  }



  protected abstract void handle(SyndEntryImpl entry);

  protected abstract boolean accept(SyndEntryImpl entry);

  public void beforeJobExecute(JobDataMap dataMap) {
    // void by default
  }

  protected Object getState(String key) {
    String uniqueKey = getStateKey(key);
    return getExoComponent(MashupStateHolder.class).getState(uniqueKey);
  }

  protected void saveState(String key, Object state) {
    String uniqueKey = getStateKey(key);
    getExoComponent(MashupStateHolder.class).saveState(uniqueKey, state);
  }


  protected String getStateKey(String key) {
    return pluginName+ "."+ key;
  }

  protected boolean alreadyChecked(Date date) {    
    Date lastChecked = (Date) getState(LAST_CHECKED);
    if (lastChecked == null) {
      return false; // case never checked
    } else {
      return date.before(lastChecked);
    }
  }

  
  
  @SuppressWarnings("unchecked")
  protected <T> T getExoComponent(Class<T> type) {
    return (T) ExoContainerContext.getContainerByName(portalContainer)
                                  .getComponentInstanceOfType(type);
  }

  
  protected Identity getIdentity(String targetUser) {
    Identity identity = null;
    try {
    IdentityManager identityManager = getExoComponent(IdentityManager.class);
    identity = identityManager.getIdentity(targetUser);
     } catch (Exception e) {
      LOG.warn("Could not find identity for " + targetUser + ": " + e.getMessage());
    }
    return identity;
  }
  
}
