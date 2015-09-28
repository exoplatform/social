/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
*/

package org.exoplatform.social.rest.entity;

import java.util.List;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;

public class ActivityEntity extends BaseEntity {
  private static final long serialVersionUID = 8770364706590680865L;

  public ActivityEntity() {
  }

  public ActivityEntity(String id) {
    super(id);
  }

  public ActivityEntity(ExoSocialActivity activity) {
    super(activity.getId());
    setTitle(activity.getTitle());
    setBody(activity.getBody());
    setLink(activity.getPermaLink());
    setType(activity.getType());
  }

  public ActivityEntity setDatIdentity(LinkEntity identity) {
    setProperty("identity", identity.getData());
    return this;
  }

  public String getIdentity() {
    return getString("identity");
  }

  public void setIdentity(String identity) {
    setProperty("identity", identity);
  }

  public ActivityEntity setTitle(String title) {
    setProperty("title", title);
    return this;
  }

  public String getTitle() {
    return getString("title");
  }

  public ActivityEntity setBody(String body) {
    setProperty("body", body);
    return this;
  }

  public String getBody() {
    return getString("body");
  }

  public ActivityEntity setOwner(DataEntity owner) {
    setProperty("owner", owner);
    return this;
  }

  public String getOwner() {
    return getString("owner");
  }
  
  public ActivityEntity setLink(String link) {
    setProperty("link", link);
    return this;
  }

  public String getLink() {
    return getString("link");
  }

  public ActivityEntity setAttachments(List<DataEntity> attachments) {
    setProperty("attachments", attachments);
    return this;
  }

  public ActivityEntity setType(String type) {
    setProperty("type", type);
    return this;
  }

  public String getType() {
    return getString("type");
  }

  public ActivityEntity setCreateDate(String createDate) {
    setProperty("createDate", createDate);
    return this;
  }

  public String getCreateDate() {
    return getString("createDate");
  }

  public ActivityEntity setUpdateDate(String updateDate) {
    setProperty("updateDate", updateDate);
    return this;
  }

  public String getUpdateDate() {
    return getString("updateDate");
  }

  public ActivityEntity setPriority(String priority) {
    setProperty("priority", priority);
    return this;
  }

  public String getPriority() {
    return getString("priority");
  }

  public ActivityEntity setMentions(List<DataEntity> mentions) {
    setProperty("mentions", mentions);
    return this;
  }

  public ActivityEntity setLikes(LinkEntity likes) {
    setProperty("likes", likes.getData());
    return this;
  }

  public String getLikes() {
    return getString("likes");
  }

  public ActivityEntity setComments(LinkEntity comments) {
    setProperty("comments", comments.getData());
    return this;
  }

  public String getComments() {
    return getString("comments");
  }

  public ActivityEntity setActivityStream(DataEntity activityStream) {
    setProperty("activityStream", activityStream);
    return this;
  }

  public DataEntity getActivityStream() {
    return (DataEntity) getProperty("activityStream");
  }
}
