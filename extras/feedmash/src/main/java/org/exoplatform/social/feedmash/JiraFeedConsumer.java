package org.exoplatform.social.feedmash;

import java.util.Date;
import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.application.Application;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.quartz.JobDataMap;

import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndEntryImpl;

public class JiraFeedConsumer extends AbstractFeedRepubJob {

  private static final Log LOG = ExoLogger.getLogger(JiraFeedConsumer.class);

  public JiraFeedConsumer() {
  }

  private String baseUrl;

  private String project;

  @Override
  public void beforeJobExecute(JobDataMap dataMap) {
    baseUrl = getStringParam(dataMap, "baseURL", null);
    project = getStringParam(dataMap, "project", null);
    if (feedUrl == null) {
      feedUrl = baseUrl + "/plugins/servlet/streams?key=" + project;
    }
    categoryMatch = dataMap.getString("categoryMatch");
  }

  protected void handle(SyndEntryImpl entry) {
    try {
      String id = targetUser;
      LOG.debug("republishing jira activity on : " + id + "'s stream, entry uri: "
          + entry.getLink());
      Identity targetStream = getIdentity(targetUser);
      if (targetStream == null) {
        return;
      }

      Identity jira = getJiraIdentity();

      ActivityManager activityManager = getExoComponent(ActivityManager.class);
      Activity activity = new Activity();
      activity.setTitle("");
      activity.setBody(entry.getTitle());
      activity.setAppId("feedmash:" + getClass());
      activity.setUserId(jira.getId());
      activityManager.saveActivity(targetStream, activity);
      saveState(LAST_CHECKED, new Date());
    } catch (Exception e) {
      LOG.error("failed to republish jira activity: " + e.getMessage(), e);
    }
  }

  private Identity getJiraIdentity() throws Exception {
    Application jiraApp = jiraApp();
    return getAppIdentity(jiraApp);
  }
  

  private Application jiraApp() {
    Application application = new Application();
    application.setId("jira-" + project);
    application.setName(project + " on JIRA");
    String url = baseUrl + "/browse/" + project;
    application.setUrl(url);
    return application;
  }

  protected boolean accept(SyndEntryImpl entry) {

    if (alreadyChecked(entry.getUpdatedDate())) {
      return false; // skipping entries already read
    }

    // find match by category
    List<SyndCategoryImpl> cats = entry.getCategories();
    for (SyndCategoryImpl category : cats) {
      if (category.getName().matches(categoryMatch))
        return true;
    }

    return false; // (entry.getTitle().contains("created") ||
                  // entry.getTitle().equals("resolved"));

  }

}
