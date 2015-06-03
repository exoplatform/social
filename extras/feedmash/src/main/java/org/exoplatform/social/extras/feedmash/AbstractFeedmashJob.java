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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
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

import com.google.common.collect.Lists;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import com.sun.syndication.io.impl.Base64;

public abstract class AbstractFeedmashJob implements Job {

  private static final Log      LOG          = ExoLogger.getLogger(AbstractFeedmashJob.class);

  protected static final String LAST_CHECKED = "lastChecked";

  protected String              targetActivityStream;

  protected String              portalContainer;

  protected String              feedUrl;

  protected Integer             rampup = 1;

  protected String              pluginName;
  
  protected String              feedLastCheck;
  
  protected String              username;
    
  protected String              password;

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
      
      URLConnection urlConnection = null;
      URL url = new URL(feedUrl);
      if(url.getUserInfo() != null){
        byte[] authEncBytes = Base64.encode(url.getUserInfo().getBytes());
        String authStringEnc = new String(authEncBytes);
        urlConnection = url.openConnection();
        urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
      } else if(username != null && password != null) {
        byte[] authEncBytes = Base64.encode((username + ":" + password).getBytes());
        String authStringEnc = new String(authEncBytes);
        urlConnection = url.openConnection();
        urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);       
      } else {
        urlConnection = url.openConnection();
      }
      
      InputStream is = urlConnection.getInputStream();
      SyndFeed feed = input.build(new XmlReader(is));
      
      List<SyndEntryImpl> entries = feed.getEntries();

      // process what we are interested in
      for (SyndEntryImpl entry : Lists.reverse(entries)) {
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
    return pluginName + "." + key;
  }

  protected boolean alreadyChecked(Date date) {
    Date lastChecked = (Date) getState(feedLastCheck);
    if (lastChecked == null) {
      return false; // case never checked
    } else {
      return !lastChecked.before(date);
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
    activityManager.saveActivityNoReturn(to, activity);

  }

  protected Identity getIdentity(String targetUser) {
    Identity identity = null;
    String[] identityInfo = null;
    try {
     if(targetUser != null && targetUser.split(":").length == 2){
       identityInfo = targetUser.split(":");
     } else {
       throw new Exception();
     }
      IdentityManager identityManager = getExoComponent(IdentityManager.class);
     identity = identityManager.getOrCreateIdentity(identityInfo[0], identityInfo[1], false);
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
    Identity identity = identityManager.getOrCreateIdentity(ApplicationsIdentityProvider.NAME, app.getId(), true);
    return identity;
  }

  private void init(JobDataMap dataMap) {
    pluginName = dataMap.getString("pluginName");
    targetActivityStream = dataMap.getString("targetActivityStream");
    portalContainer = dataMap.getString("portalContainer");
    feedUrl = dataMap.getString("feedURL");
    username = dataMap.getString("username");
    password = dataMap.getString("password");        
  }

  private boolean severIsStarting(JobDataMap dataMap) {
    // hack to before actually starting working otherwise picketlink fails
    if (rampup > 1) {
      --rampup;
      LOG.debug("waiting #" + rampup);
      return true;
    }

    return false;
  }

}
