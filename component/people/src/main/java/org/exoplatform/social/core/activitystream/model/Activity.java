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

import java.util.Map;
import java.util.List;

public class Activity {
  private String body = null;
  private String externalId = null;
  private String id = null;
  private Long updated = null;
  private List<MediaItem> mediaItems = null;
  private Long postedTime = null;
  private Integer priority = null;
  private Stream stream = null;
  private Map<String, String> templateParams = null;
  private String title = null;
  private String url = null;
  private String userId = null;
  private String type = null;
  private String replytoId = null;
  private boolean hidden = false;

  public Activity(String userId, String type, String title, String body) {
    this.userId = userId;
    this.type = type;
    this.title = title;
    this.body = body;
  }

  public Activity() {
    
  }


  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Long getUpdated() {
    return updated;
  }

  public void setUpdated(Long updated) {
    this.updated = updated;
  }

  public List<MediaItem> getMediaItems() {
    return mediaItems;
  }

  public void setMediaItems(List<MediaItem> mediaItems) {
    this.mediaItems = mediaItems;
  }

  public Long getPostedTime() {
    return postedTime;
  }

  public void setPostedTime(Long postedTime) {
    this.postedTime = postedTime;
  }

  public Integer getPriority() {
    return priority;
  }

    /**
     *
     * @param priority a number between 0 and 100
     */
  public void setPriority(Integer priority) {
    if (priority < 0 || priority > 100)
     throw new IllegalArgumentException("the priority should be between 0 and 100");
    this.priority = priority;
  }

  public Stream getStream() {
    return stream;
  }

  public void setStream(Stream stream) {
    this.stream = stream;
  }

  public Map<String, String> getTemplateParams() {
    return templateParams;
  }

  public void setTemplateParams(Map<String, String> templateParams) {
    this.templateParams = templateParams;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

    public String getReplytoId() {
        return replytoId;
    }

    public void setReplytoId(String replytoId) {
        this.replytoId = replytoId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
