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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.social.rest.api.RestUtils;
import org.json.JSONObject;


public class CollectionEntity extends LinkedHashMap<String, Object> {
  private static final long serialVersionUID = 5157400162426650346L;
  private int size   = -1;
  private int limit  = -1;
  private int offset = 0;

  public CollectionEntity(List<? extends DataEntity> entities, String key, int offset, int limit) {
     put(key, entities);
     this.offset = offset;
     this.limit = limit;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getLimit() {
    return limit;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
    this.offset = offset;
  }

  public List<? extends DataEntity> getEntities() {
    for (Map.Entry<String, Object> entry : entrySet()) {
      if (entry.getValue() instanceof List) {
        return (List<? extends DataEntity>) entry.getValue();
      }
    }
    return new ArrayList<DataEntity>();
  }

  public List<DataEntity> extractInfo(List<String> returnedProperties) {
    List<DataEntity> returnedInfos = new ArrayList<DataEntity>();
    for (DataEntity inEntity : getEntities()) {
      returnedInfos.add(RestUtils.extractInfo(inEntity, returnedProperties));
    }
    return returnedInfos;
  }

  @Override
  public String toString() {
    return toJSONObject().toString();
  }

  public JSONObject toJSONObject() {
    if (offset == 0 && limit == 0) {
      return new JSONObject(getEntities());
    }
    return new JSONObject(this);
  }
}
