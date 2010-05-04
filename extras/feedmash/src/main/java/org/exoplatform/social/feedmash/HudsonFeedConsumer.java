package org.exoplatform.social.feedmash;

import java.util.Date;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.application.Application;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;
import org.quartz.JobDataMap;

import com.sun.syndication.feed.synd.SyndEntryImpl;

public class HudsonFeedConsumer extends AbstractFeedRepubJob {


  private static final Log LOG = ExoLogger.getLogger(HudsonFeedConsumer.class);
  private static final String HUDSON_STATUS = "status";
  
  private String successIcon = "/eXoResources/skin/DefaultSkin/skinIcons/16x16/icons/GreenFlag.gif";
  private String failureIcon = "/eXoResources/skin/DefaultSkin/skinIcons/16x16/icons/RedFlag.gif";
  private String hudsonLogo = "http://wiki.hudson-ci.org/download/attachments/2916393/banner-100.png?version=1&modificationDate=1185846429000";
  private String baseUrl;

  private String project;
  
  enum BuildStatus {
    FAILURE, SUCCESS
  };
  
  
  @Override
  public void beforeJobExecute(JobDataMap dataMap) {
    successIcon = getStringParam(dataMap, "successIcon", successIcon);
    failureIcon = getStringParam(dataMap, "failureIcon", failureIcon);
    baseUrl = getStringParam(dataMap, "baseURL", null);
    project = getStringParam(dataMap, "project", null);
    if (feedUrl == null) {
      feedUrl = baseUrl + "/job/" + project + "/rssAll";
    }
  }

  protected String getStringParam(JobDataMap dataMap, String name, String defaultValue) {
    String value = dataMap.getString(name);
    return (value == null) ? defaultValue : value;
  }

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
      String message = null;
      if (entry.getTitle().contains(BuildStatus.SUCCESS.name())) {
        currentStatus = BuildStatus.SUCCESS.name();
      } else {
        currentStatus = BuildStatus.FAILURE.name();
      }

      Identity targetStream = getIdentity(targetUser);
      if (targetStream == null) {
        return;
      }
       

      LOG.debug("Publishing "+ currentStatus+" on : "+ targetStream.getRemoteId() + "'s stream");   
      
      ActivityManager activityManager = getExoComponent(ActivityManager.class);
      Identity hudson = getHudsonIdentity();
      message = message(currentStatus,entry.getLink(), entry.getTitle()); 
      Activity activity = new Activity();
      activity.setTitle("");
      activity.setBody(message);
      activity.setAppId("feedmash:" + getClass());
      activity.setUserId(hudson.getId());
      activityManager.saveActivity(targetStream, activity);

      saveState(HUDSON_STATUS, currentStatus);
      saveState(LAST_CHECKED, new Date());

    } catch (Exception e) {
      LOG.error("failed to publish hudson activity: " + e.getMessage(), e);
      
    }
  }

  private String message(String status, String link, String title) {
    String icon = (status == BuildStatus.SUCCESS.name()) ? successIcon : failureIcon;
    return "<img src=\""+icon+ "\" alt=\"failure\" title=\"failure\" />&nbsp;<a href=\""+ link+"\">" + title + "</a>"; 
  }

  private Identity getHudsonIdentity() throws Exception {
    return getAppIdentity(hudsonApp());
  }

  private Application hudsonApp() {
    Application application = new Application();
    application.setId("Hudson-" + project);
    application.setName("Hudson " + "(" +project + ")");
    String url = baseUrl + "/job/" + project;
    application.setIcon(hudsonLogo);
    application.setUrl(url);
    return application;
  }


}
