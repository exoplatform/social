/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.activity;

import org.exoplatform.social.common.lifecycle.AbstractLifeCycle;
import org.exoplatform.social.core.activity.ActivityLifeCycleEvent.Type;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;


public class ActivityLifeCycle extends AbstractLifeCycle<ActivityListener, ActivityLifeCycleEvent> {

  @Override
  protected void dispatchEvent(ActivityListener listener, ActivityLifeCycleEvent event) {
    switch(event.getType()) {
      case SAVE_ACTIVITY:
        listener.saveActivity(event);
        break;
      case UPDATE_ACTIVITY:
        listener.updateActivity(event);
        break;
      case SAVE_COMMENT: 
        listener.saveComment(event);
        break;
      case LIKE_ACTIVITY: 
        listener.likeActivity(event);
        break;
    }
  }
  
  public void saveActivity(ExoSocialActivity activity) {
    broadcast(new ActivityLifeCycleEvent(Type.SAVE_ACTIVITY, activity));
  }

  public void updateActivity(ExoSocialActivity activity) {
    broadcast(new ActivityLifeCycleEvent(Type.UPDATE_ACTIVITY, activity));
  }

  public void saveComment(ExoSocialActivity activity) {
    broadcast(new ActivityLifeCycleEvent(Type.SAVE_COMMENT, activity));
  }
  
  public void likeActivity(ExoSocialActivity activity) {
    broadcast(new ActivityLifeCycleEvent(Type.LIKE_ACTIVITY, activity));
  }
}
