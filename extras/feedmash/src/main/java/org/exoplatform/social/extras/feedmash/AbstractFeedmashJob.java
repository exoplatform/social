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
package org.exoplatform.social.extras.feedmash;

import java.net.URL;
import java.util.Date;
import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

public abstract class AbstractFeedmashJob implements Job {

  private static final Log      LOG          = ExoLogger.getLogger(AbstractFeedmashJob.class);

  protected static final String LAST_CHECKED = "lastChecked";

  protected String              targetActivityStream;

  protected String              portalContainer;

  protected String              feedUrl;

  protected Integer             rampup       = 5;

  protected String              pluginName;

  /**
   * Feedmash job. Provides support for fetching the job. Lets subclasses filter
   * and process the matching entries.
   */
  @SuppressWarnings("unchecked")
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      JobDataMap dataMap = context.getJobDetail().getJobDataMap();

      // read job settings
      init(dataMap);

      // make sure server has finished starting
      if (severIsStarting(dataMap)) {
        return;
      }

      // let subclass do something before proceeding
      beforeJobExecute(dataMap);

      // Read the feed
      SyndFeedInput input = new SyndFeedInput();
      SyndFeed feed = input.build(new XmlReader(new URL(feedUrl)));
      List<SyndEntryImpl> entries = feed.getEntries();

      // process what we are interested in
      for (SyndEntryImpl entry : entries) {
        if (accept(entry)) {
          handle(entry);
        }
      }

      saveState(LAST_CHECKED, new Date());
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
    return pluginName + "." + key;
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
    ExoContainer container = ExoContainerContext.getContainerByName(portalContainer);
    ExoContainerContext.setCurrentContainer(container);
    return (T) container.getComponentInstanceOfType(type);
  }

  /**
   * Publish an activity
   *
   * @param message body of the activity
   * @param from owner of the activity
   * @param to target of the activity
   * @throws Exception
   */
  protected void publishActivity(String message, Identity from, Identity to) throws Exception {

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(message);
    activity.setAppId("feedmash:" + getClass());
    activity.setUserId(from.getId());

    ActivityManager activityManager = getExoComponent(ActivityManager.class);
    activityManager.saveActivity(to, activity);

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

  protected String getStringParam(JobDataMap dataMap, String name, String defaultValue) {
    String value = dataMap.getString(name);
    return (value == null) ? defaultValue : value;
  }

  protected Identity getAppIdentity(Application app) throws Exception {

    IdentityManager identityManager = getExoComponent(IdentityManager.class);
    // appExists(app, identityManager);

    ApplicationsIdentityProvider appIdentityProvider = new ApplicationsIdentityProvider();
    appIdentityProvider.addApplication(app);
    identityManager.addIdentityProvider(appIdentityProvider);
    Identity identity = identityManager.getOrCreateIdentity(ApplicationsIdentityProvider.NAME,
                                                            app.getId());
    return identity;
  }

  @SuppressWarnings("unused")
  private boolean appExists(Application application, IdentityManager identityManager) {
    boolean exists = false;
    try {
      exists = identityManager.identityExisted(ApplicationsIdentityProvider.NAME,
                                               application.getId());
    } catch (Exception e) {
      return false;

    }
    return exists;
  }

  private void init(JobDataMap dataMap) {
    pluginName = dataMap.getString("pluginName");
    targetActivityStream = dataMap.getString("targetActivityStream");
    portalContainer = dataMap.getString("portalContainer");
    feedUrl = dataMap.getString("feedURL");
  }

  private boolean severIsStarting(JobDataMap dataMap) {
    // hack to before actually starting working otherwise picketlink fails
    // starting...
    rampup = (Integer) dataMap.get("rampup");
    if (rampup == null) {
      rampup = 2;
    }
    if (rampup > 1) {
      dataMap.put("rampup", --rampup);
      LOG.debug("waiting #" + rampup);
      return true;
    }

    return false;
  }

}
