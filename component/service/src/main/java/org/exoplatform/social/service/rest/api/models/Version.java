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
 * Version class to expose as json response object.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jun 15, 2011
 */
public class Version {

  /**
   * Gets the latest social rest api version.
   *
   * @return the string the latest social rest api version
   */
  public String getVersion() {
    return version;
  }

  /**
   * Sets the latest social rest api version.
   *
   * @param latestVersion the string the latest social rest api version
   */
  public void setVersion(String latestVersion) {
    this.version = latestVersion;
  }

  /**
   * The latest social rest api version.
   */
  private String version;


}
