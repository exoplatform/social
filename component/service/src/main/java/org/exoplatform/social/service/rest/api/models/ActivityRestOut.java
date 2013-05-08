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
import java.util.Map;

import org.exoplatform.social.common.RealtimeListAccess;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.service.rest.Util;



/**
 * The Activity model for Social Rest APIs.
 * @author <a href="http://phuonglm.net">phuonglm</a>
 * @since 1.2.3
 */
public class ActivityRestOut extends HashMap<String, Object>{
  public static enum Field {
    /**
     * The id.
     */
    ID("id"),
    /**
     * The title.
     */
    TITLE("title"),
    /** 
     * Activity Body message 
     */
    BODY("body"),
    /**
     * The priority from 0 to 1. 1 is the higher priority.
     */
    PRIORITY("priority"),
    /**
     * The application id.
     */
    APPID("appId"),
    /**
     * The activity type.
     */
    TYPE("type"),
    /**
     * The posted timestamp.
     */
    POSTED_TIME("postedTime"),
    /**
     * The date (human format).
     */
    CREATED_AT("createdAt"),
    /**
     * The last updated timestamp.
     */
    LAST_UPDATED("lastUpdated"),
    /**
     * The title id.
     */
    TITLE_ID("titleId"),
    /**
     * The template parameters.
     */
    TEMPLATE_PARAMS("templateParams"),
    /**
     * Is liked or not by the authenticated user who makes the request
     */
    LIKED("liked"),
    /**
     * The identities who like.
     */
    LIKED_BY_IDENTITIES("likedByIdentities"),
    /**
     * The comments wrapper.
     */
    COMMENTS("comments"),
    /**
     * The number of comment.
     */
    TOTAL_NUMBER_OF_COMMENTS("totalNumberOfComments"),
    /**
     * The poster identity id.
     */
    POSTER_IDENTITY("posterIdentity"),
    /**
     * The owner identity id.
     */
    IDENTITY_ID("identityId"),
    /**
     * The Activity stream details.
     */
    ACTIVITY_STREAM("activityStream"),
    /**
     * The total number of user like this activity
     */
    TOTAL_NUMBER_OF_LIKES("totalNumberOfLikes");
    
    
   /**
    * String type.
    */
    private final String fieldName;

   /**
    * private constructor.
    *
    * @param string string type
    */
    private Field(final String string) {
      fieldName = string;
    }
    
    public String toString() {
      return fieldName;
    }

  }

  
  /**
   * Default constructor, used by JAX-RS.
   */
  public ActivityRestOut() {
    initialize();
  }


  public ActivityRestOut(final ExoSocialActivity activity, String portalContainerName) {
    initialize();
    this.setId(activity.getId());
    this.setTitle(activity.getTitle());
    this.setBody(activity.getBody()); 
    this.setPriority(activity.getPriority());
    this.setAppId(activity.getAppId());
    this.setType(activity.getType());
    this.setPostedTime(activity.getPostedTime());
    this.setLastUpdatedTime(activity.getUpdated().getTime());
    this.setCreatedAt(Util.convertTimestampToTimeString(getPostedTime()));
    this.setTitleId(activity.getTitleId());
    this.setTemplateParams(activity.getTemplateParams());
    
    if(activity.getLikeIdentityIds() != null){
      this.setTotalNumberOfLikes(activity.getLikeIdentityIds().length);
    } else {
      this.setTotalNumberOfLikes(null);
    }
    
    if(Util.isLikedByIdentity(Util.getAuthenticatedUserIdentity(portalContainerName).getId(),activity)){
      this.setLiked(true);
    } else {
      this.setLiked(false);
    }
    
    RealtimeListAccess<ExoSocialActivity> commentRealtimeListAccess = Util.getActivityManager(portalContainerName).
                                                                           getCommentsWithListAccess(activity);
    this.setTotalNumberOfComments(commentRealtimeListAccess.getSize());
    
    Identity streamOwnerIdentity = Util.getOwnerIdentityIdFromActivity(portalContainerName, activity);
    if(streamOwnerIdentity != null){
      this.put(Field.IDENTITY_ID.toString(),streamOwnerIdentity.getId());
    }
  }
  
  public String getId() {
    return (String) this.get(Field.ID.toString());
  }

  public void setId(final String id) {
    if(id != null){
      this.put(Field.ID.toString(), id);
    } else {
      this.put(Field.ID.toString(), "");
    }
  }

  public String getTitle() {
    return (String) this.get(Field.TITLE.toString());
  }

  public void setTitle(final String title) {
    if(title != null){
      this.put(Field.TITLE.toString(), title);
    } else {
      this.put(Field.TITLE.toString(), "");
    }
  }

  public String getBody() {
    return (String) this.get(Field.BODY.toString());
  }

  public void setBody(final String body) {
    if(body != null){
      this.put(Field.BODY.toString(), body);
    } else {
      this.put(Field.BODY.toString(), "");
    }
  }
  
  public Float getPriority() {
    return (Float) this.get(Field.PRIORITY.toString());
  }

  public void setPriority(final Float priority) {
    if(priority != null){
      this.put(Field.PRIORITY.toString(), priority);
    } else {
      this.put(Field.PRIORITY.toString(), new Float(0));
    }
  }

  public String getAppId() {
    return (String) this.get(Field.APPID.toString());
  }

  public void setAppId(final String appId) {
    if(appId != null){
      this.put(Field.APPID.toString(), appId);
    } else {
      this.put(Field.APPID.toString(), "");
    }
  }

  public String getType() {
    return (String) this.get(Field.TYPE.toString());
  }

  public void setType(final String type) {
    if(type != null){
      this.put(Field.TYPE.toString(), type);
    } else {
      this.put(Field.TYPE.toString(), "");
    }
    
  }

  public Long getPostedTime() {
    return (Long) this.get(Field.POSTED_TIME.toString());
  }
  
  public Long getLastUpdatedTime() {
    return (Long) this.get(Field.LAST_UPDATED.toString());
  }

  public void setPostedTime(final Long postedTime) {
    if(postedTime != null){
      this.put(Field.POSTED_TIME.toString(), postedTime);
    } else {
      this.put(Field.POSTED_TIME.toString(), new Long(0));
    }
  }
  
  public void setLastUpdatedTime(final Long updatedTime) {
    if(updatedTime != null){
      this.put(Field.LAST_UPDATED.toString(), updatedTime);
    } else {
      this.put(Field.LAST_UPDATED.toString(), new Long(0));
    }
  }

  public String getCreatedAt() {
    return (String) this.get(Field.CREATED_AT.toString());
  }

  public void setCreatedAt(String createdAt) {
    if(createdAt != null){
      this.put(Field.CREATED_AT.toString(), createdAt);
    } else {
      this.put(Field.CREATED_AT.toString(), "");
    }
  }

  public String getTitleId() {
    return (String) this.get(Field.TITLE_ID.toString());
  }

  public void setTitleId(String titleId) {
    if(titleId != null){
      this.put(Field.TITLE_ID.toString(), titleId);
    } else {
      this.put(Field.TITLE_ID.toString(), "");
    }
  }

  public Map<String, String> getTemplateParams() {
    return (Map<String, String>) this.get(Field.TEMPLATE_PARAMS.toString());
  }

  public void setTemplateParams(Map<String, String> map) {
    if(map != null){
      this.put(Field.TEMPLATE_PARAMS.toString(), map);
    } else {
      this.put(Field.TEMPLATE_PARAMS.toString(), new HashMap<String, String>());
    }
  }

  public Boolean getLiked() {
    return (Boolean) this.get(Field.LIKED.toString());
  }

  public void setLiked(Boolean liked) {
    if(liked != null){
      this.put(Field.LIKED.toString(), liked);
    } else {
      this.put(Field.LIKED.toString(), new Boolean(false));
    }
  }

  public ArrayList<IdentityRestOut> getLikedByIdentities() {
    return (ArrayList<IdentityRestOut>) this.get(Field.LIKED_BY_IDENTITIES.toString());
  }

  public void setLikedByIdentities(List<IdentityRestOut> likedByIdentities) {
    if(likedByIdentities != null){
      this.put(Field.LIKED_BY_IDENTITIES.toString(), likedByIdentities);
    } else {
      this.put(Field.LIKED_BY_IDENTITIES.toString(), new ArrayList<IdentityRestOut>());
    }
  }

  public List<CommentRestOut> getComments() {
    return (List<CommentRestOut>) this.get(Field.COMMENTS.toString());
  }

  public void setComments(List<CommentRestOut> comments) {
    if(comments != null){
      this.put(Field.COMMENTS.toString(), comments);
    } else {
      this.put(Field.COMMENTS.toString(), new ArrayList());
    }
  }

  public Integer getTotalNumberOfComments() {
    return (Integer) this.get(Field.TOTAL_NUMBER_OF_COMMENTS.toString());
  }

  public void setTotalNumberOfComments(Integer numberOfComments) {
    if(numberOfComments != null){
      this.put(Field.TOTAL_NUMBER_OF_COMMENTS.toString(), numberOfComments);
    } else {
      this.put(Field.TOTAL_NUMBER_OF_COMMENTS.toString(), new Integer(0));
    }
  }

  public IdentityRestOut getPosterIdentity() {
    return (IdentityRestOut) this.get(Field.POSTER_IDENTITY.toString());
  }

  public void setPosterIdentity(IdentityRestOut posterIdentity) {
    if(posterIdentity != null){
      this.put(Field.POSTER_IDENTITY.toString(), posterIdentity);
    } else {
      this.put(Field.POSTER_IDENTITY.toString(), new HashMap<String, Object>());
    }
  }

  public String getIdentityId() {
    return (String) this.get(Field.IDENTITY_ID.toString());
  }

  public void setIdentityId(String identityId) {
    if(identityId != null){
      this.put(Field.IDENTITY_ID.toString(), identityId);
    } else {
      this.put(Field.IDENTITY_ID.toString(), "");
    }
  }

  public ActivityStreamRestOut getActivityStream() {
    return (ActivityStreamRestOut) this.get(Field.ACTIVITY_STREAM.toString());
  }

  public void setActivityStream(final ActivityStreamRestOut activityStream) {
    if(activityStream != null){
      this.put(Field.ACTIVITY_STREAM.toString(), activityStream);
    } else {
      this.put(Field.ACTIVITY_STREAM.toString(), new HashMap<String, Object>());
    }
  }

  public Integer getTotalNumberOfLikes() {
    return (Integer) this.get(Field.TOTAL_NUMBER_OF_LIKES.toString());
  }


  public void setTotalNumberOfLikes(Integer totalNumberOfLikes) {
    if(totalNumberOfLikes != null){
      this.put(Field.TOTAL_NUMBER_OF_LIKES.toString(), totalNumberOfLikes);
    } else {
      this.put(Field.TOTAL_NUMBER_OF_LIKES.toString(), new Integer(0));
    }
  }
  
 /** 
  * Sets the number of likes to be returned.
  *
  * @param numberOfLikes the number of likes
  * @param activity the existing activity
  * @param portalContainerName the portal container name
  */
  public void setNumberOfLikes(int numberOfLikes, ExoSocialActivity activity, String portalContainerName) {
    if (numberOfLikes <= 0) {
      return;
    }
    String[] likeIdentityIds = activity.getLikeIdentityIds();
    numberOfLikes = Math.min(numberOfLikes, likeIdentityIds.length);
    List<IdentityRestOut> identityRests = new ArrayList<IdentityRestOut>(numberOfLikes);
    for (int i = 0; i < numberOfLikes; i++) {
      // got the latest at the end to the top
      identityRests.add(new IdentityRestOut(likeIdentityIds[likeIdentityIds.length - i - 1], portalContainerName));
    }
    setLikedByIdentities(identityRests);
  }

 /**
  * Sets the number of comments to be returned.
  *
  * @param numberOfComments the number of comments
  * @param activity the existing activity
  * @param portalContainerName the portal container name
  */
  public void setNumberOfComments(int numberOfComments, ExoSocialActivity activity, String portalContainerName) {
    if (numberOfComments <= 0) {
      return;
    }
    ActivityManager activityManager = Util.getActivityManager(portalContainerName);
    IdentityManager identityManager = Util.getIdentityManager(portalContainerName);
    RealtimeListAccess<ExoSocialActivity> rcla = activityManager.getCommentsWithListAccess(activity);
    ExoSocialActivity[] comments = rcla.load(0, numberOfComments);
    numberOfComments = Math.min(comments.length, numberOfComments);
    List<CommentRestOut> commentRests = new ArrayList<CommentRestOut>(numberOfComments);
    for (int i = 0; i < numberOfComments; i++) {
      ExoSocialActivity currentComment = comments[i];
      CommentRestOut commentRestOut = new CommentRestOut(comments[i], portalContainerName);
      commentRestOut.setPosterIdentity(new IdentityRestOut(identityManager.getIdentity(currentComment.getUserId(), false)));
      commentRests.add(commentRestOut);
    }
    setComments(commentRests);
  }
  
  private void initialize(){
    this.setId("");
    this.setTitle("");
    this.setBody("");
    this.setPriority(new Float(0));
    this.setAppId("");
    this.setType("");
    this.setPostedTime(new Long(0));
    this.setCreatedAt("");
    this.setTitleId("");
    this.setTemplateParams(new HashMap<String, String>());
    this.setLiked(false);
    this.setLikedByIdentities(new ArrayList<IdentityRestOut>());
    this.setComments(new ArrayList<CommentRestOut>());
    this.setTotalNumberOfComments(0);
    this.setTotalNumberOfLikes(0);
    this.setPosterIdentity(null);
    this.setIdentityId("");
    this.setActivityStream(null);
  }
}
