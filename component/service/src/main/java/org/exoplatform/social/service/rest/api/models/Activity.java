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

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.service.rest.Util;

import java.util.Date;
import java.util.Map;

/**
 * The Activity model for Social Rest APIs.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jun 17, 2011
 */
public class Activity {

  /**
   * The id.
   */
  private String id;

  /**
   * The title.
   */
  private String title;

  /**
   * The priority from 0 to 1. 1 is the higher priority.
   */
  private Float priority;

  /**
   * The application id.
   */
  private String appId;

  /**
   * The activity type.
   */
  private String type;

  /**
   * The posted timestamp.
   */
  private long postedTime;

  /**
   * The date (human format).
   */
  private String createdAt;

  /**
   * Tje title id.
   */
  private String titleId;

  /**
   * The template parameters.
   */
  private Map<String, String> templateParams;

  /**
   * Got at least one like.
   */
  private boolean liked;

  /**
   * The identities who like.
   */
  private Identity[] likedByIdentities;

  /**
   * The comments wrapper.
   */
  private Comment[] comments;

  /**
   * The number of comment.
   */
  private int numberOfComments;

  /**
   * The poster identity id.
   */
  private Identity posterIdentity;

  /**
   * The owner identity id.
   */
  private String identityId;

  /**
   * The Activity stream details.
   */
  private ActivityStream activityStream;

  /**
   * Default constructor, used by JAX-RS.
   */
  public Activity() {
  }


  /**
   * Initialize constructor.
   *
   * @param id The id.
   * @param title The title.
   * @param priority The priority.
   * @param appId The application id.
   * @param type The activity type.
   * @param postedTime The timestamp.
   * @param createdAt The human date.
   * @param titleId The title id.
   * @param templateParams The template parameters.
   * @param liked Is liked
   * @param likedByIdentities The identity ids who like.
   * @param identityId The owner identity id.
   */
  public Activity(
      final String id,
      final String title,
      final Float priority,
      final String appId,
      final String type,
      final long postedTime,
      final String createdAt,
      final String titleId,
      final Map<String, String> templateParams,
      final boolean liked,
      final Identity[] likedByIdentities,
      final String identityId) {

    this.id = id;
    this.title = title;
    this.priority = priority;
    this.appId = appId;
    this.type = type;
    this.postedTime = postedTime;
    this.createdAt = createdAt;
    this.titleId = titleId;
    this.templateParams = templateParams;
    this.liked = liked;
    this.likedByIdentities = likedByIdentities;
    this.identityId = identityId;
  }

  public Activity(final ExoSocialActivity activity) {
    Identity[] identitysIdentities = null ;
    if(activity.getLikeIdentityIds() != null && activity.getLikeIdentityIds().length > 0){
      String[] getLikeIdentityIds = activity.getLikeIdentityIds();
      identitysIdentities = new Identity[getLikeIdentityIds.length];
      for (int i = 0; i < identitysIdentities.length; i++) {
        identitysIdentities[i] = new Identity(getLikeIdentityIds[i]);
      }
    }

   this.id = activity.getId();
   this.title = activity.getTitle();
   this.priority = activity.getPriority();
   this.appId = activity.getAppId();
   this.type = activity.getType();
   this.postedTime = activity.getPostedTime();
   this.createdAt = Util.convertTimestampToTimeString(getPostedTime());
   this.titleId = activity.getTitleId();
   this.templateParams = activity.getTemplateParams();
   this.likedByIdentities = identitysIdentities;
   this.identityId = activity.getUserId();
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public Float getPriority() {
    return priority;
  }

  public void setPriority(final Float priority) {
    this.priority = priority;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(final String appId) {
    this.appId = appId;
  }

  public String getType() {
    return type;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public long getPostedTime() {
    return postedTime;
  }

  public void setPostedTime(final long postedTime) {
    this.postedTime = postedTime;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(final String createdAt) {
    this.createdAt = createdAt;
  }

  public String getTitleId() {
    return titleId;
  }

  public void setTitleId(final String titleId) {
    this.titleId = titleId;
  }

  public Map<String, String> getTemplateParams() {
    return templateParams;
  }

  public void setTemplateParams(final Map<String, String> templateParams) {
    this.templateParams = templateParams;
  }

  public boolean isLiked() {
    return liked;
  }

  public void setLiked(final boolean liked) {
    this.liked = liked;
  }

  public Identity[] getLikedByIdentities() {
    return likedByIdentities;
  }

  public void setLikedByIdentities(final Identity[] likedByIdentities) {
    this.likedByIdentities = likedByIdentities;
  }

  public Comment[] getComments() {
    return comments;
  }

  public void setComments(final Comment[] comments) {
    this.comments = comments;
  }

  public int getNumberOfComments() {
    return numberOfComments;
  }

  public void setNumberOfComments(final int numberOfComments) {
    this.numberOfComments = numberOfComments;
  }

  public Identity getPosterIdentity() {
    return posterIdentity;
  }

  public void setPosterIdentity(final Identity posterIdentity) {
    this.posterIdentity = posterIdentity;
  }

  public String getIdentityId() {
    return identityId;
  }

  public void setIdentityId(final String identityId) {
    this.identityId = identityId;
  }

  public ActivityStream getActivityStream() {
    return activityStream;
  }

  public void setActivityStream(final ActivityStream activityStream) {
    this.activityStream = activityStream;
  }
}
