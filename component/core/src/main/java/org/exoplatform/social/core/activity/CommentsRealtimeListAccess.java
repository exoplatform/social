/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.social.core.activity;

import java.util.List;

import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.common.jcr.Util;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.storage.api.ActivityStorage;

/**
 * The realtime list access for comments of activities.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since May 9, 2011
 */
public class CommentsRealtimeListAccess implements RealtimeListAccess<ExoSocialActivity> {

  /**
   * The activity activityStorage.
   */
  private ActivityStorage activityStorage;

  /**
   * The existing activity.
   */
  private ExoSocialActivity existingActivity;


  /**
   * The constructor.
   *
   * @param theActivityStorage
   * @param theExistingActivity
   */
  public CommentsRealtimeListAccess(ActivityStorage theActivityStorage, ExoSocialActivity theExistingActivity) {
    this.activityStorage = theActivityStorage;
    this.existingActivity = theExistingActivity;
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> loadAsList(int index, int limit) {
    return activityStorage.getComments(existingActivity, index, limit);
  }

  /**
   * {@inheritDoc}
   */
  public ExoSocialActivity[] load(int index, int limit) {
    return Util.convertListToArray(loadAsList(index, limit), ExoSocialActivity.class);
  }

  /**
   * {@inheritDoc}
   */
  public int getSize() {
    return activityStorage.getNumberOfComments(existingActivity);
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> loadNewer(ExoSocialActivity baseComment, int length) {
    return activityStorage.getNewerComments(existingActivity, baseComment, length);
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfNewer(ExoSocialActivity baseComment) {
    return activityStorage.getNumberOfNewerComments(existingActivity, baseComment);
  }

  /**
   * {@inheritDoc}
   */
  public List<ExoSocialActivity> loadOlder(ExoSocialActivity baseComment, int length) {
    return activityStorage.getOlderComments(existingActivity, baseComment, length);
  }

  /**
   * {@inheritDoc}
   */
  public int getNumberOfOlder(ExoSocialActivity baseComment) {
    return activityStorage.getNumberOfOlderComments(existingActivity, baseComment);
  }
}
