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



/**
 * Sequence of activities
 */
public class Stream {
  
  /** The favicon url. */
  private String faviconUrl;
  
  /** The source url. */
  private String sourceUrl;
  
  /** The title. */
  private String title;
  
  /** The url. */
  private String url;
  
  /**
   * internal uuid for this stream
   */
  private String streamId = null;
  
  /**
   * internal uuid for this stream owner
   */
  private String streamOwner = null;

  /**
   * Gets the favicon url.
   * 
   * @return the favicon url
   */
  public String getFaviconUrl() {
    return faviconUrl;
  }

  /**
   * Sets the favicon url.
   * 
   * @param faviconUrl the new favicon url
   */
  public void setFaviconUrl(String faviconUrl) {
    this.faviconUrl = faviconUrl;
  }

  /**
   * Gets the source url.
   * 
   * @return the source url
   */
  public String getSourceUrl() {
    return sourceUrl;
  }

  /**
   * Sets the source url.
   * 
   * @param sourceUrl the new source url
   */
  public void setSourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
  }

  /**
   * Gets the title.
   * 
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the title.
   * 
   * @param title the new title
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Gets the url.
   * 
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets the url.
   * 
   * @param url the new url
   */
  public void setUrl(String url) {
    this.url = url;
  }

  public String getStreamId() {
    return streamId;
  }

  public void setStreamId(String streamId) {
    this.streamId = streamId;
  }

  public String getStreamOwner() {
    return streamOwner;
  }

  public void setStreamOwner(String streamOwner) {
    this.streamOwner = streamOwner;
  }
}
