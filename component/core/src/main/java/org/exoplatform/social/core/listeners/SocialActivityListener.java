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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.listeners;

import org.exoplatform.social.common.lifecycle.LifeCycleListener;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;

public interface SocialActivityListener extends LifeCycleListener<ActivityLifeCycleEvent> {
  
  void saveActivity(Identity streamOwner, ExoSocialActivity activity);

  void saveActivity(ExoSocialActivity activity);

  void updateActivity(ExoSocialActivity activity);

  void deleteActivity(ExoSocialActivity activity);

  void deleteActivity(String activityId);

  void saveComment(ExoSocialActivity activity, ExoSocialActivity newComment);

  void deleteComment(String activityId, String commentId);

  void deleteComment(ExoSocialActivity activity, ExoSocialActivity comment);

  void saveLike(ExoSocialActivity activity, Identity identity);

  void deleteLike(ExoSocialActivity activity, Identity identity);
}
