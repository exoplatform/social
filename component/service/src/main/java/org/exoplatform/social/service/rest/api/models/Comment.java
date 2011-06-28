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

/**
 * The comment model for Social Rest APIs.
 * 
 * @author <a href="http://phuonglm.net">phuonglm</a>
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jun 17, 2011
 */
public class Comment {
  private String identityId;
  private String text;
  private long postedTime;
  private String createdAt;
  
  public String getIdentityId() {
    return identityId;
  }
  public void setIdentityId(String identityId) {
    this.identityId = identityId;
  }
  public String getText() {
    return text;
  }
  public void setText(String text) {
    this.text = text;
  }
  public long getPostedTime() {
    return postedTime;
  }
  public void setPostedTime(long postedTime) {
    this.postedTime = postedTime;
  }
  public String getCreatedAt() {
    return createdAt;
  }
  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }
  
}
