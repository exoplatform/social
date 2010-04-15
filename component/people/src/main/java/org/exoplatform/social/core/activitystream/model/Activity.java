/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.core.activitystream.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * An activity is a piece of message aimed at sharing some status, mood, idea, notification, data...
 */
public class Activity  {
  
  /** The  Constant label comment. */
  static final public String IS_COMMENT = "IS_COMMENT";
  
  /** The body. */
  private String body = null;

  /** The body. */
  private String bodyId = null;
  
  /** The external id. */
  private String externalId = null;
  
  /** The id. */
  private String id = null;
  
  /** The updatedTimestamp. */
  private Long updatedTimestamp = null;
  
  /** The media items. */
  private List<org.apache.shindig.social.opensocial.model.MediaItem> mediaItems = null;
  
  /** The posted time. */
  private Long postedTime = null;
  
  /** The priority. */
  private Integer priority = null;
  
  /** The stream. */
  private Stream stream = null;
  
  /** The template params. */
  private Map<String, String> templateParams = null;
  
  /** The title. */
  private String title = null;

  /** The titleId. */
  private String titleId = null;
  
  /** The url. */
  private String url = null;
  
  /** The user id. */
  private String userId = null;
  
  /** The type. */
  private String type = null;
  
  /** The replyTo id. */
  private String replyToId = null;
 
  /** The hidden. */
  private boolean hidden = false;
  
  /** The like identity ids. */
  private String[] likeIdentityIds = null;

  
  
  /**
   * Instantiates a new activity based on userId, type, title and his body.
   * 
   * @param userId the user id
   * @param type the type
   * @param title the title
   * @param body the body
   */
  public Activity(String userId, String type, String title, String body) {
    this.userId = userId;
    this.type = type;
    this.title = title;
    this.body = body;
  }

  /**
   * Instantiates a new activity.
   */
  public Activity() {
    
  }


  /**
   * Gets the body.
   * 
   * @return the body
   */
  public String getBody() {
    return body;
  }

  /**
   * Sets the body.
   * 
   * @param body the new body
   */
  public void setBody(String body) {
    this.body = body;
  }

  /**
   * Gets the external id.
   * 
   * @return the external id
   */
  public String getExternalId() {
    return externalId;
  }

  /**
   * Sets the external id.
   * 
   * @param externalId the new external id
   */
  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  /**
   * Gets the id.
   * 
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the id.
   * 
   * @param id the new id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the updatedTimestamp.
   * 
   * @return the updatedTimestamp
   */
  public Long getUpdatedTimestamp() {
    return updatedTimestamp;
  }

  /**
   * Sets the updatedTimestamp.
   * 
   * @param timestamp the new updatedTimestamp
   */
  public void setUpdatedTimestamp(Long timestamp) {
    this.updatedTimestamp = timestamp;
  }

  /**
   * Gets the media items.
   * 
   * @return the media items
   * @see org.exoplatform.social.core.activitystream.model.MediaItem
   */
  public List<org.apache.shindig.social.opensocial.model.MediaItem> getMediaItems() {
    return mediaItems;
  }

  /**
   * Sets the media items.
   * 
   * @param mediaItems the new media items
   * @see org.exoplatform.social.core.activitystream.model.MediaItem
   */
  public void setMediaItems(List<org.apache.shindig.social.opensocial.model.MediaItem> mediaItems) {
    this.mediaItems = mediaItems;
  }

  /**
   * Gets the posted time.
   * 
   * @return the posted time
   */
  public Long getPostedTime() {
    return postedTime;
  }

  /**
   * Sets the posted time.
   * 
   * @param postedTime the new posted time
   */
  public void setPostedTime(Long postedTime) {
    this.postedTime = postedTime;
  }


  public Float getPriority() {
    if (priority == null) {
      return null;
    }
    return (priority>0) ? (priority/100F) : 0F;
  }
  
  /**
   * Gets the priority.
   * 
   * @return the priority
   */
  public Integer getIntPriority() {
    return priority;
  }
  
  /**
   * Sets the priority.
   * 
   * @param priority a number between 0 and 100
   */
  public void setPriority(Integer priority) {
    if (priority == null)  {
      this.priority = null;
      return;
    }
    if (priority < 0 || priority > 100)
      throw new IllegalArgumentException("the priority should be between 0 and 100");
    this.priority = priority;
  }


  /**
   * should be between 0 and 1
   */
  public void setPriority(Float priority) {
    if (priority == null) {
      this.priority = null;
      return;
    }
    if (priority > 0) {
      setPriority(new Float(priority*100).intValue());
    } else {
      setPriority(0);
    }
    
  }

  /**
   * Gets the stream.
   * 
   * @return the stream
   */
  public Stream getStream() {
    if (stream == null) {
      stream = new Stream();
    }
    return stream;
  }

  /**
   * Sets the stream.
   * 
   * @param stream the new stream
   */
  public void setStream(Stream stream) {
    this.stream = stream;
  }

  /**
   * Gets the template params.
   * 
   * @return the template params
   */
  public Map<String, String> getTemplateParams() {
    return templateParams;
  }

  /**
   * Sets the template params.
   * 
   * @param templateParams the template params
   */
  public void setTemplateParams(Map<String, String> templateParams) {
    this.templateParams = templateParams;
  }

  /**
   * Gets the title.
   * 
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the title.
   * 
   * @param title the new title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Gets the url.
   * 
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets the url.
   * 
   * @param url the new url
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Gets the user id.
   * 
   * @return the user id
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Sets the user id.
   * 
   * @param userId the new user id
   */
  public void setUserId(String userId) {
    this.userId = userId;
  }

  /**
   * Gets the replyto id.
   * 
   * @return the replyto id
   */
  public String getReplyToId() {
      return replyToId;
  }

  /**
   * Sets the replyto id.
   * 
   * @param replytoId the new replyto id
   */
  public void setReplyToId(String replyToId) {
      this.replyToId = replyToId;
  }

  /**
   * Gets the type.
   * 
   * @return the type
   */
  public String getType() {
      return type;
  }

  /**
   * Sets the type.
   * 
   * @param type the new type
   */
  public void setType(String type) {
      this.type = type;
  }

  /**
   * Checks if is hidden.
   * 
   * @return true, if is hidden
   */
  public boolean isHidden() {
      return hidden;
  }

  /**
   * Sets the hidden.
   * 
   * @param hidden the new hidden
   */
  public void setHidden(boolean hidden) {
      this.hidden = hidden;
  }
  
  /**
   * Sets the like identity ids.
   * 
   * @param likeIdentityIds the new like identity ids
   */
  public void setLikeIdentityIds(String[] likeIdentityIds) {
   this.likeIdentityIds = likeIdentityIds;
  }
  
  /**
   * Gets the like identity ids.
   * 
   * @return the like identity ids
   */
  public String[] getLikeIdentityIds() {
    return likeIdentityIds;
  }

  public String getBodyId() {
    return bodyId;
  }

  public void setBodyId(String bodyId) {
    this.bodyId = bodyId;
  }

  public String getTitleId() {
    return titleId;
  }

  public void setTitleId(String titleId) {
    this.titleId = titleId;
  }

  public String getStreamUrl() {
    return getStream().getUrl();
  }

  public void setStreamUrl(String streamUrl) {
    this.getStream().setUrl(streamUrl);
  }

  public String getStreamSourceUrl() {
    return getStream().getSourceUrl();
  }

  public void setStreamSourceUrl(String streamSourceUrl) {
    getStream().setSourceUrl(streamSourceUrl);
  }

  public String getStreamTitle() {
    return getStream().getTitle();
  }

  public void setStreamTitle(String streamTitle) {
    this.getStream().setTitle(streamTitle);
  }

  public String getStreamFaviconUrl() {
    return getStream().getFaviconUrl();
  }

  public void setStreamFaviconUrl(String streamFaviconUrl) {
    getStream().setFaviconUrl(streamFaviconUrl);
  }


  public Date getUpdated() {
    return (updatedTimestamp!=null) ? new Date(updatedTimestamp) : null;
  }

  public void setUpdated(Date updated) {
    if (updated != null) {
      updatedTimestamp = updated.getTime();
    } else {
      updatedTimestamp = null;
    }
  }

  public String getAppId() {
    if (type != null && type.startsWith("opensocial:")) {
      return type.substring("opensocial:".length());
    }
    return null;
  }

  public void setAppId(String appId) {
    if (appId!=null && !appId.startsWith("opensocial:")) {
      setType("opensocial:" + appId);
    }
  }
}
