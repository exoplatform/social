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
package org.exoplatform.social.webui.space;

import org.exoplatform.portal.webui.container.UIContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.webui.Utils;
import org.exoplatform.social.webui.activity.UIActivitiesLoader;
import org.exoplatform.social.webui.composer.UIComposer.PostContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * UISpaceActivitiesDisplay.java
 * <p>
 * Displays space activities and its member's activities
 *
 * @author <a href="http://hoatle.net">hoatle</a>
 * @since Apr 6, 2010
 * @copyright eXo Platform SAS
 */
@ComponentConfig(
  template = "war:/groovy/social/webui/space/UISpaceActivitiesDisplay.gtmpl",
  events = {
    @EventConfig(listeners = UISpaceActivitiesDisplay.RefreshStreamActionListener.class)
  }
)

public class UISpaceActivitiesDisplay extends UIContainer {
  static private final Log LOG = ExoLogger.getLogger(UISpaceActivitiesDisplay.class);

  private Space space;
  private static final int ACTIVITY_PER_PAGE = 20;
  private UIActivitiesLoader activitiesLoader;

  /**
   * Constructor
   *
   * @throws Exception
   */
  public UISpaceActivitiesDisplay() throws Exception {
  }

  /**
   * Sets space to work with
   * @param space
   * @throws Exception
   */
  public void setSpace(Space space) throws Exception {
    this.space = space;
    init();
  }

  /**
   * Returns current space to work with
   * @return
   */
  public Space getSpace() {
    return space;
  }


  public UIActivitiesLoader getActivitiesLoader() {
    return activitiesLoader;
  }

  /**
   * initialize
   * @throws Exception
   */
  public void init() throws Exception {
    if (space == null) {
      LOG.warn("space is null! Can not display spaceActivites");
      return;
    }

    Identity spaceIdentity = Utils.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, 
                                                                            space.getPrettyName(), false);
    
    removeChild(UIActivitiesLoader.class);
    activitiesLoader = addChild(UIActivitiesLoader.class, null, "UIActivitiesLoader");
    activitiesLoader.setSpace(space);
    activitiesLoader.setPostContext(PostContext.SPACE);
    activitiesLoader.setLoadingCapacity(ACTIVITY_PER_PAGE);
    activitiesLoader.setActivityListAccess(Utils.getActivityManager().getActivitiesOfSpaceWithListAccess(spaceIdentity));
    activitiesLoader.init();
    
    //
    String remoteId = Utils.getOwnerRemoteId();
    Utils.getSpaceService().updateSpaceAccessed(remoteId, space);
    
  }

  public static class RefreshStreamActionListener extends EventListener<UISpaceActivitiesDisplay> {
    public void execute(Event<UISpaceActivitiesDisplay> event) throws Exception {
     UISpaceActivitiesDisplay uiSpaceActivities = event.getSource();
     uiSpaceActivities.init();
     event.getRequestContext().addUIComponentToUpdateByAjax(uiSpaceActivities);

     Utils.resizeHomePage();
   }
  }
}
