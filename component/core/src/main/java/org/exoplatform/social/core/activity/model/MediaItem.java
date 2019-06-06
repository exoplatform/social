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
package org.exoplatform.social.core.activity.model;

/**
 * The Class MediaItem represent MediaItem in opensocial.
 */
public class MediaItem {

  public enum Type {
    AUDIO("audio"),
    IMAGE("image"),
    VIDEO("video");

    /**
     * The field type.
     */
    private final String jsonString;

    /**
     * Construct a field type based on the name.
     *
     * @param jsonString
     */
    Type(String jsonString) {
      this.jsonString = jsonString;
    }

    /**
     * @return a string representation of the enum.
     */
    @Override
    public String toString() {
      return this.jsonString;
    }
  }

  /** The mime type. */
  private String mimeType;

  /** The type. */
  private Type type;

  /** The url. */
  private String url;

  private String thumbnailUrl;

  /**
   * Gets the mime type.
   * 
   * @return the mime type
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * Sets the mime type.
   * 
   * @param mimeType the new mime type
   */
  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  /**
   * Gets the type.
   * 
   * @return the type
   */
  public Type getType() {
    return type;
  }

  /**
   * Sets the type.
   * 
   * @param type the new type
   */
  public void setType(Type type) {
    this.type = type;
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

  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public String getAlbumId() {
    throw new RuntimeException();
  }

  public void setAlbumId(final String albumId) {
    throw new RuntimeException();
  }

  public String getCreated() {
    throw new RuntimeException();
  }

  public void setCreated(final String created) {
    throw new RuntimeException();
  }

  public String getDescription() {
    throw new RuntimeException();
  }

  public void setDescription(final String description) {
    throw new RuntimeException();
  }

  public String getDuration() {
    throw new RuntimeException();
  }

  public void setDuration(final String duration) {
    throw new RuntimeException();
  }

  public String getFileSize() {
    throw new RuntimeException();
  }

  public void setFileSize(final String fileSize) {
    throw new RuntimeException();
  }

  public String getId() {
    throw new RuntimeException();
  }

  public void setId(final String id) {
    throw new RuntimeException();
  }

  public String getLanguage() {
    throw new RuntimeException();
  }

  public void setLanguage(final String language) {
    throw new RuntimeException();
  }

  public String getLastUpdated() {
    throw new RuntimeException();
  }

  public void setLastUpdated(final String lastUpdated) {
    throw new RuntimeException();
  }

  public String getNumComments() {
    throw new RuntimeException();
  }

  public void setNumComments(final String numComments) {
    throw new RuntimeException();
  }

  public String getNumViews() {
    throw new RuntimeException();
  }

  public void setNumViews(final String numViews) {
    throw new RuntimeException();
  }

  public String getNumVotes() {
    throw new RuntimeException();
  }

  public void setNumVotes(final String numVotes) {
    throw new RuntimeException();
  }

  public String getRating() {
    throw new RuntimeException();
  }

  public void setRating(final String rating) {
    throw new RuntimeException();
  }

  public String getStartTime() {
    throw new RuntimeException();
  }

  public void setStartTime(final String startTime) {
    throw new RuntimeException();
  }

  public String getTaggedPeople() {
    throw new RuntimeException();
  }

  public void setTaggedPeople(final String taggedPeople) {
    throw new RuntimeException();
  }

  public String getTags() {
    throw new RuntimeException();
  }

  public void setTags(final String tags) {
    throw new RuntimeException();
  }

  public String getTitle() {
    throw new RuntimeException();
  }

  public void setTitle(final String title) {
    throw new RuntimeException();
  }
}
