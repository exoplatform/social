/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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

public class ActivityFileRestIn {
  private String uploadId;

  private String storage;

  public String getUploadId() {
    return uploadId;
  }
  
  public void setUploadId(String uploadId) {
    this.uploadId = uploadId;
  }

  public String getStorage() {
    return storage;
  }
  
  public void setStorage(String storage) {
    this.storage = storage;
  }
}