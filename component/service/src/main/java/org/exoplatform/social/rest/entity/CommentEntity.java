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

public class CommentEntity extends BaseEntity {
  private static final long serialVersionUID = -2508420154905846726L;

  public CommentEntity() {
  }

  public CommentEntity(String id) {
    super(id);
  }

  public CommentEntity setDataIdentity(LinkEntity identity) {
    setProperty("identity", identity.getData());
    return this;
  }

  public String getIdentity() {
    return getString("identity");
  }

  public void setIdentity(String identity) {
    setProperty("identity", identity);
  }

  public CommentEntity setBody(String body) {
    setProperty("body", body);
    return this;
  }

  public String getBody() {
    return getString("body");
  }

  public CommentEntity setPoster(String poster) {
    setProperty("poster", poster);
    return this;
  }

  public String getPoster() {
    return getString("poster");
  }

  public CommentEntity setCreateDate(String createDate) {
    setProperty("createDate", createDate);
    return this;
  }

  public String getCreateDate() {
    return getString("createDate");
  }

  public CommentEntity setUpdateDate(String updateDate) {
    setProperty("updateDate", updateDate);
    return this;
  }

  public String getUpdateDate() {
    return getString("updateDate");
  }

  public CommentEntity setMentions(List<DataEntity> mentions) {
    setProperty("mentions", mentions);
    return this;
  }

  public CommentEntity setActivity(String activity) {
    setProperty("activity", activity);
    return this;
  }

  public String getActivity() {
    return getString("activity");
  }



}
