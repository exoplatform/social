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
package org.exoplatform.social.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * UIActivitiesContainer.java
 *
 * @author    <a href="http://hoatle.net">hoatle</a>
 * @since 	  Apr 12, 2010
 * @copyright eXo Platform SAS
 */
@ComponentConfig(
  template = "classpath:groovy/social/webui/UIActivitiesContainer.gtmpl",
  events = {
    @EventConfig(listeners = UIActivitiesContainer.PostCommentActionListener.class)
  }
)
public class UIActivitiesContainer extends UIContainer {
  private List<Activity> activityList_;
  /* this index is used for UISpaceActivitiesDisplay to know and manage updates*/
  private int index_;
  /**
   * constructor
   */
  public UIActivitiesContainer() {
  }

  public UIActivitiesContainer setActivityList(List<Activity> activityList) throws Exception {
    activityList_ = activityList;
    init();
    return this;
  }

  public void setIndex(int index) {
    index_ = index;
  }

  public int getIndex() {
    return index_;
  }

  public int getSize() {
    return activityList_.size();
  }


  /**
   * initializes ui component child
   * @throws Exception
   */
  private void init() throws Exception {
    //sort activity list needed ?
    if (activityList_ == null) activityList_ = new ArrayList<Activity>();
    for (Activity activity : activityList_) {
      addChild(UIActivity.class, null, "UIActivity" + activity.getId()).setActivity(activity);
    }
  }

  static public class PostCommentActionListener extends EventListener<UIActivity> {

    @Override
    public void execute(Event<UIActivity> event) throws Exception {

    }
  }
}
