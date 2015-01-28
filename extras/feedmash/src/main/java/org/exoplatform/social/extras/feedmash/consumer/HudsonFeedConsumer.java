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
package org.exoplatform.social.extras.feedmash.consumer;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.extras.feedmash.AbstractFeedmashJob;
import org.exoplatform.social.extras.feedmash.Application;
import org.quartz.JobDataMap;

import com.sun.syndication.feed.synd.SyndEntryImpl;

public class HudsonFeedConsumer extends AbstractFeedmashJob {
  private static final Log LOG = ExoLogger.getLogger(HudsonFeedConsumer.class);
  private static final String HUDSON_STATUS = "status";

  private String successIcon = "/eXoSkin/skin/images/themes/default/Icons/skinIcons/16x16/GreenFlag.gif";
  private String failureIcon = "/eXoSkin/skin/images/themes/default/Icons/skinIcons/16x16/RedFlag.gif";
  private String hudsonLogo = "http://wiki.hudson-ci.org/download/attachments/2916393/banner-100.png" +
                              "?version=1&modificationDate=1185846429000";
  private String baseUrl;

  private String project;

  enum BuildStatus {
    FAILURE, SUCCESS
  };



  @Override
  protected boolean accept(SyndEntryImpl entry) {
    if (alreadyChecked(entry.getUpdatedDate())) {
      return false;
    } else {
      return true;
    }
  }

  @Override
  protected void handle(SyndEntryImpl entry) {
    try {

      Identity targetStream = getIdentity(targetActivityStream);
      if (targetStream == null) {
        saveState(feedLastCheck, null);
        return;
      } else {
        saveState(feedLastCheck, entry.getPublishedDate());
      }

      String currentStatus = currentStatus(entry);
      LOG.debug("Publishing "+ currentStatus+" on : "+ targetStream.getRemoteId() + " stream");
      String message = message(currentStatus,entry.getLink(), entry.getTitle());

      Identity hudson = getHudsonIdentity();

      publishActivity(message, hudson, targetStream);

      saveState(HUDSON_STATUS, currentStatus);

    } catch (Exception e) {
      LOG.error("failed to publish hudson activity: " + e.getMessage(), e);

    }
  }

  @Override
  public void beforeJobExecute(JobDataMap dataMap) {
    super.beforeJobExecute(dataMap);
    successIcon = getStringParam(dataMap, "successIcon", successIcon);
    failureIcon = getStringParam(dataMap, "failureIcon", failureIcon);
    baseUrl = getStringParam(dataMap, "baseURL", null);
    project = getStringParam(dataMap, "project", null);
    if (feedUrl == null) {
      feedUrl = baseUrl + "/job/" + project + "/rssAll";
    }
    feedLastCheck = LAST_CHECKED + "." + feedUrl + "." + targetActivityStream;
  }

  private String currentStatus(SyndEntryImpl entry) {
    String currentStatus;

    if (entry.getTitle().contains(BuildStatus.SUCCESS.name())) {
      currentStatus = BuildStatus.SUCCESS.name();
    } else {
      currentStatus = BuildStatus.FAILURE.name();
    }
    return currentStatus;
  }

  private String message(String status, String link, String title) {
    String icon = (status == BuildStatus.FAILURE.name()) ? successIcon : successIcon;
    return "<img src=\""+icon+ "\" alt=\"failure\" title=\"failure\" /> " +
            "<a href=\""+ link+"\" target=\"_blank\">" + title + "</a>";
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