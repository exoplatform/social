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

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

public class BaseEntity implements Serializable {
  private static final long serialVersionUID = -7245526640639649852L;
  private DataEntity dataEntity = new DataEntity();
  public BaseEntity() {
  }

  public BaseEntity setProperty(String name, Object value) {
    dataEntity.setProperty(name, value);
    return this;
  }

  protected Object getProperty(String name) {
    return dataEntity.get(name);
  }

  protected String getString(String name) {
    Object o = dataEntity.get(name);
    if (o == null) {
      return null;
    }
    return String.valueOf(o);
  }

  public BaseEntity(String id) {
    if (!StringUtils.isEmpty(id)) {
      setId(id);
    }
  }

  public BaseEntity setId(String id) {
    setProperty("id", id);
    return this;
  }

  public String getId() {
    return getString("id");
  }

  public BaseEntity setHref(String href) {
    setProperty("href", href);
    return this;
  }
  
  public String getHref() {
    return (String) dataEntity.get("href");
  }

  public DataEntity getDataEntity() {
    return dataEntity;
  }

  public void setDataEntity(DataEntity dataEntity) {
    this.dataEntity = dataEntity;
  }

  @Override
  public String toString() {
    return toJSONObject().toString();
  }

  public JSONObject toJSONObject() {
    return new JSONObject(this);
  }
}
