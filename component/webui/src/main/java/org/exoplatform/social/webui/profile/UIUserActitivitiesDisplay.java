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
package org.exoplatform.social.webui.profile;

import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.webui.activity.UIActivitiesContainer;
import org.exoplatform.social.webui.composer.UIComposer.PostContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Displays user's activities
 *
 * @author    <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since     Jul 30, 2010
 * @copyright eXo SAS
 */
@ComponentConfig(
  template = "classpath:groovy/social/webui/profile/UIUserActivitiesDisplay.gtmpl"
)
public class UIUserActitivitiesDisplay extends UIContainer {

  static private final Log LOG = ExoLogger.getLogger(UIUserActitivitiesDisplay.class);

  private String ownerName;
  private List<Activity> activityList;
  private UIActivitiesContainer uiActivitiesContainer;
  /**
   * constructor
   */
  public UIUserActitivitiesDisplay() {

  }

  /**
   * sets activity stream owner (user remote Id)
   *
   * @param ownerName
   * @throws Exception
   */
  public void setOwnerName(String ownerName) throws Exception {
    this.ownerName= ownerName;
    init();
  }

  public String getOwnerName() {
    return ownerName;
  }

  /**
   * initialize
   *
   * @throws Exception
   */
  private void init() throws Exception {
    if (ownerName == null) {
      LOG.error("ownerName is null! Can not display userActivities");
      return;
    }
    Identity ownerIdentity = getIdentityManager().getOrCreateIdentity(OrganizationIdentityProvider.NAME, ownerName);
    activityList = getActivityManager().getActivities(ownerIdentity);
    removeChild(UIActivitiesContainer.class);

    uiActivitiesContainer = addChild(UIActivitiesContainer.class, null, null);
    uiActivitiesContainer.setPostContext(PostContext.PEOPLE);
    uiActivitiesContainer.setOwnerName(this.ownerName);
    uiActivitiesContainer.setActivityList(activityList);

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
