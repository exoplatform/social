/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.social.webui;


public class RequestNavInfo {

  private final String siteType;

  private final String siteName;

  private final String path;

  public RequestNavInfo(String siteType, String siteName, String path) {
    this.siteType = siteType != null ? siteType : "";
    this.siteName = siteName != null ? siteName : "";

    //in the case .../home#comments needs to take care.
    if (path == null | "home".equals(path) | path.indexOf("home#") >= 0) {
      this.path = "";
    } else {
      this.path = path;
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof RequestNavInfo)) {
      return false;
    } else {
      RequestNavInfo data = (RequestNavInfo) obj;
      return siteType.equals(data.siteType) && siteName.equals(data.siteName)
          && path.equals(data.path);
    }
  }
}
