/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.social.common.embedder;

/**
 * @since 4.0.0-GA  
 */
public class ExoSocialMedia {
  
  private String title;
  private String type;
  private String url;
  private String description;
  // html embed code
  private String html;
  // name of provider: youtube, vimeo,...
  private String provider;
  
  public void setTitle(String title) {
    this.title = title;
  }
  
  public String getTitle() {
    return title;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public String getType() {
    return type;
  }
  
  public void setUrl(String url) {
    this.url = url;
  }
  
  public String getUrl() {
    return url;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public String getDescription() {
    return description;
  }
  
  public void setHtml(String html) {
    this.html = html;
  }
  
  public String getHtml() {
    return html;
  }
  
  public void setProvider(String provider) {
    this.provider = provider;
  }
  
  public String getProvider() {
    return provider;
  }
  
  
}
