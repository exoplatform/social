/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.social.service.rest.api.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;

/**
 * The activity rest list out model for Social Rest APIs.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since 1.2.3
 */
public class ActivityRestListOut extends HashMap<String, Object> {

  private static final int MAX_LIMIT = 100;
  private static final int MAX_NUMBER_OF_COMMENTS = 100;
  private static final int MAX_NUMBER_OF_LIKES = 100;

  /**
   * The enum fields as json keys
   */
  public static enum Field {
    ACTIVITIES("activities");

    /**
     * field name
     */
    private final String fieldName;

    /**
     * Private constructor.
     *
     * @param str the field name
     */
    private Field(final String str) {
      fieldName = str;
    }

    /**
     * Gets the string field name.
     *
     * @return the field name
     */
    @Override
    public String toString() {
      return fieldName;
    }
  }

  /**
   * Default constructor for initializing default values.
   */
  public ActivityRestListOut() {
    initialize();
  }

  /**
   * Constructors to set the list of activities and the number of comments, the number of likes.
   *
   * @param activityList     the activity list
   * @param numberOfComments the number of comments
   * @param numberOfLikes    the number of likes
   */
  public ActivityRestListOut(List<ExoSocialActivity> activityList, int numberOfComments,
                             int numberOfLikes, String portalContainerName) {
    if (activityList == null || activityList.size() == 0) {
      initialize();
      return;
    } else if (activityList.size() > MAX_LIMIT) {
      activityList = activityList.subList(0, MAX_LIMIT - 1);
    }
    numberOfComments = numberOfComments >= 0 ? numberOfComments : 0;
    numberOfComments = Math.min(numberOfComments, MAX_NUMBER_OF_COMMENTS);
    numberOfLikes = numberOfLikes >= 0 ? numberOfLikes : 0;
    numberOfLikes = Math.min(numberOfLikes, MAX_NUMBER_OF_LIKES);
    initialize(activityList, numberOfComments, numberOfLikes, portalContainerName);
  }


  /**
   * Initializes default values
   */
  private void initialize() {
    put(Field.ACTIVITIES.toString(), new ArrayList<ActivityRestOut>());
  }

  /**
   * Initializes the stream with numberOfComments, numberOfLikes.
   *
   * @param activityList        the activity list
   * @param numberOfComments    the number of comments
   * @param numberOfLikes       the number of likes
   * @param portalContainerName the portal container name
   */
  private void initialize(List<ExoSocialActivity> activityList, int numberOfComments,
                          int numberOfLikes, String portalContainerName) {
    List<ActivityRestOut> activityItems = new ArrayList<ActivityRestOut>();
    for (ExoSocialActivity activity : activityList) {
      ActivityRestOut activityItem = new ActivityRestOut(activity, portalContainerName);
      activityItem.setPosterIdentity(new IdentityRestOut(activity.getUserId(), portalContainerName));
      activityItem.setActivityStream(new ActivityStreamRestOut(activity.getActivityStream(), portalContainerName));
      activityItem.setNumberOfComments(numberOfComments, activity, portalContainerName);
      activityItem.setNumberOfLikes(numberOfLikes, activity, portalContainerName);
      activityItems.add(activityItem);

    }
    put(Field.ACTIVITIES.toString(), activityItems);
  }

}
