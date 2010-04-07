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
package social.portal.webui.component;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceAttachment;
import org.exoplatform.social.space.SpaceIdentityProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * UIComposer.java
 * <p>
 * Allows users to type messages and then postMessage is broadcasted to its
 * parent.
 * 
 * @author <a href="http://hoatle.net">hoatle</a>
 * @since Apr 6, 2010
 * @copyright eXo Platform SAS
 */
@ComponentConfig(template = "app://groovy/portal/webui/component/UIDisplaySpaceActivities.gtmpl")
public class UIDisplaySpaceActivities extends UIContainer {
  private Space           space_;

  private IdentityManager identityManager_;

  private ActivityManager activityManager_;

  /**
   * Constructor
   * 
   * @throws Exception
   */
  public UIDisplaySpaceActivities() throws Exception {

  }

  public void setSpace(Space space) {
    space_ = space;
  }

  public Space getSpace() {
    return space_;
  }

  public String getImageSource() throws Exception {
    SpaceAttachment spaceAttachment = space_.getSpaceAttachment();
    if (spaceAttachment != null) {
      return "/" + getRestContext() + "/jcr/" + getRepository() + "/"
          + spaceAttachment.getWorkspace() + spaceAttachment.getDataPath() + "/?rnd="
          + System.currentTimeMillis();
    }
    return null;
  }

  /***
   * gets prettyTime
   * @param timestamp
   * @return
   */
  public String toPrettyTime(long postedTime) {
    //TODO use app resource
    long time = (new Date().getTime() - postedTime) / 1000;
    long value = 0;
    if (time < 60) {
      return "less than a minute ago";
    } else {
      if (time < 120) {
        return "about a minute ago";
      } else {
        if (time < 3600) {
          value = Math.round(time / 60);
          return "about " + value + " minutes ago";
        } else {
          if (time < 7200) {
            return "about an hour ago";
          } else {
            if (time < 86400) {
              value = Math.round(time / 3600);
              return "about " + value + " hours ago";
            } else {
              if (time < 172800) {
                return "about a day ago";
              } else {
                if (time < 2592000) {
                  value = Math.round(time / 86400);
                  return "about " + value + " days ago";
                } else {
                  if (time < 5184000) {
                    return "about a month ago";
                  } else {
                    value = Math.round(time / 2592000);
                    return "about " + value + " months ago";
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  public List<Activity> getActivityList() throws Exception {
    identityManager_ = getIdentityManager();
    Identity spaceIdentity = identityManager_.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                  space_.getId());
    activityManager_ = getActivityManager();
    List<Activity> activityList = activityManager_.getActivities(spaceIdentity);
    Collections.reverse(activityList);
    return activityList;
  }

  private IdentityManager getIdentityManager() {
    return getApplicationComponent(IdentityManager.class);
  }

  private ActivityManager getActivityManager() {
    return getApplicationComponent(ActivityManager.class);
  }

  private String getRestContext() {
    return PortalContainer.getCurrentRestContextName();
  }

  /**
   * gets current repository name
   * 
   * @return repository name
   * @throws Exception
   */
  private String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class);
    return rService.getCurrentRepository().getConfiguration().getName();
  }
}
