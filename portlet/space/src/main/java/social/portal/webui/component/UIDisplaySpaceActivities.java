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
import java.util.List;

import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceIdentityProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * UIDisplaySpaceActivities.java
 * <p>
 * Displays space activities and its member's activities
 * 
 * @author <a href="http://hoatle.net">hoatle</a>
 * @since Apr 6, 2010
 * @copyright eXo Platform SAS
 */
@ComponentConfig(
  template = "app://groovy/portal/webui/component/UIDisplaySpaceActivities.gtmpl",
  events = {
    @EventConfig(listeners = UIDisplaySpaceActivities.UpdateNewActionListener.class),
    @EventConfig(listeners = UIDisplaySpaceActivities.MoreActionListener.class)
  }
)
public class UIDisplaySpaceActivities extends UIContainer {
  static private final Log LOG = ExoLogger.getLogger(UIDisplaySpaceActivities.class);
  private Space           space_;
  
  private List<Activity> activityList_;

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
   * @throws Exception 
   */
  public void setSpace(Space space) throws Exception {
    space_ = space;
    init();
  }

  /**
   * Returns current space to work with
   * @return
   */
  public Space getSpace() {
    return space_;
  }
  
  /**
   * initialize
   * @throws Exception
   */
  private void init() throws Exception {
    if (space_ == null) {
      LOG.warn("space_ is null! Can not display spaceActivites");
      return;
    }
    setChildren(null); //TODO hoatle handle this for better performance
    Identity spaceIdentity = getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, space_.getId());
    activityList_ = getActivityManager().getActivities(spaceIdentity);
    if (activityList_ != null) Collections.reverse(activityList_); //TODO sort by postedTime needed
    addChild(UIActivitiesContainer.class, null, null).setActivityList(activityList_).setIndex(0);
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
  
  static public class UpdateNewActionListener extends EventListener<UIDisplaySpaceActivities> {

    @Override
    public void execute(Event<UIDisplaySpaceActivities> event) throws Exception {
      // TODO Auto-generated method stub
      
    }
    
  }
  
  /**
   * If any older activities available, populates uiActivityList for a new UIActivitiesContainer.
   * @author hoatle
   *
   */
  static public class MoreActionListener extends EventListener<UIDisplaySpaceActivities> {

    @Override
    public void execute(Event<UIDisplaySpaceActivities> event) throws Exception {
      
    }
    
  }
  
  static public class PostCommentActionListener extends EventListener<UIActivity> {

    @Override
    public void execute(Event<UIActivity> event) throws Exception {
      
    }
    
  }
}
