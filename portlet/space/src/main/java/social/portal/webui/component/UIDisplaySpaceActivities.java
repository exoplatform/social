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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceIdentityProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * UIDisplaySpaceActivities.java
 * <p>
 * Displays space activities and its member's activities
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

  /**
   * Sets space to work with
   * @param space
   */
  public void setSpace(Space space) {
    space_ = space;
  }

  /**
   * Returns current space to work with
   * @return
   */
  public Space getSpace() {
    return space_;
  }
  
  /**
   * Gets user's avatar image source by userIdentityId
   * @param userIdentityId
   * @return
   * @throws Exception
   */
  public String getUserAvatarImageSource(String userIdentityId) throws Exception {
    Identity userIdentity = identityManager_.getIdentity(userIdentityId, true);
    if (userIdentity == null) {
      return null;
    }
    Profile userProfile = userIdentity.getProfile();
    return userProfile.getAvatarImageSource(PortalContainer.getInstance());
  }
  /**
   * Gets user's full name by its userIdentityId
   * @param userIdentityId
   * @return
   * @throws Exception
   */
  public String getUserFullName(String userIdentityId) throws Exception {
    identityManager_ = getIdentityManager();
    Identity userIdentity = identityManager_.getIdentity(userIdentityId, true);
    if (userIdentity == null) {
      return null;
    }
    Profile userProfile = userIdentity.getProfile();
    return userProfile.getFullName();
  }
  
  /**
   * Gets user profile uri
   * @param userIdentityId
   * @return
   * @throws Exception
   */
  public String getUserProfileUri(String userIdentityId) throws Exception {
    identityManager_ = getIdentityManager();
    Identity userIdentity = identityManager_.getIdentity(userIdentityId, true);
    if (userIdentity == null) {
      return null;
    }
    return "/"+ PortalContainer.getCurrentPortalContainerName() +"/private/classic/activities/" + userIdentity.getRemoteId();
  }

  /**
   * Gets prettyTime by timestamp
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

  /**
   * Gets space activity list
   * @return
   * @throws Exception
   */
  public List<Activity> getActivityList() throws Exception {
    identityManager_ = getIdentityManager();
    Identity spaceIdentity = identityManager_.getOrCreateIdentity(SpaceIdentityProvider.NAME,
                                                                  space_.getId());
    activityManager_ = getActivityManager();
    List<Activity> activityList = activityManager_.getActivities(spaceIdentity);
    //TODO make sure: activities are in time order
    Collections.reverse(activityList);
    return activityList;
  }

  /**
   * Gets identityManager
   * @return
   */
  private IdentityManager getIdentityManager() {
    return getApplicationComponent(IdentityManager.class);
  }

  /**
   * Gets activityManager
   * @return
   */
  private ActivityManager getActivityManager() {
    return getApplicationComponent(ActivityManager.class);
  }
}
