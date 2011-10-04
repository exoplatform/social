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

import java.util.HashMap;

import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.service.rest.Util;


/**
 * The Activity Stream model for Social Rest APIs.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since 1.2.3
 */
public class ActivityStreamRestOut extends HashMap<String, Object> {

  /**
   * The enum fields as json keys
   */
  public static enum Field {
    TYPE("type"),
    PRETTY_ID("prettyId"),
    FAVICON_URL("faviconUrl"),
    TITLE("title"),
    PERMA_LINK("permaLink");

    /**
     * field name
     */
    private final String fieldName;

    /**
     * Private constructor.
     *
     * @param str the field name
     */
    private Field(final String str) {
      fieldName = str;
    }

    /**
     * Gets the string field name.
     *
     * @return the field name
     */
    @Override
    public String toString() {
      return fieldName;
    }
  }

  /**
   * Default constructor for initializing default values.
   */
  public ActivityStreamRestOut() {
    initialize();
  }

  /**
   * Constructor to construct object from {@link ActivityStream} instance.
   *
   * @param activityStream the activity stream instance.
   */
  public ActivityStreamRestOut(final ActivityStream activityStream) {
    initialize();
    this.setType(activityStream.getType().toString());
    this.setPrettyId(activityStream.getPrettyId());
    this.setFaviconUrl(activityStream.getFaviconUrl());
    this.setTitle(activityStream.getTitle());
    this.setPermaLink(activityStream.getPermaLink()); //TODO make sure absolute link
  }

  /**
   * Gets the activity stream type.
   *
   * @return the activity stream type.
   */
  public String getType() {
    return (String) get(Field.TYPE.toString());
  }

  /**
   * Sets the activity stream type.
   *
   * @param type the type
   */
  public void setType(String type) {
    if (type == null) {
      put(Field.TYPE.toString(), "");
    } else {
      put(Field.TYPE.toString(), type);
    }
  }

  /**
   * Gets the pretty id.
   *
   * @return the pretty id
   */
  public String getPrettyId() {
    return (String) get(Field.PRETTY_ID.toString());
  }

  /**
   * Sets the pretty id.
   *
   * @param prettyId the pretty id
   */
  public void setPrettyId(final String prettyId) {
    if (prettyId == null) {
      put(Field.PRETTY_ID.toString(), "");
    } else {
      put(Field.PRETTY_ID.toString(), prettyId);
    }
  }

  /**
   * Gets the activity stream title.
   *
   * @return the activity stream title
   */
  public String getTitle() {
    return (String) get(Field.TITLE.toString());
  }

  /**
   * Sets the activity stream title.
   *
   * @param title the activity stream title
   */
  public void setTitle(final String title) {
    if (title == null) {
      put(Field.TITLE.toString(), "");
    } else {
      put(Field.TITLE.toString(), title);
    }
  }

  /**
   * Gets the perma link.
   *
   * @return the perma link
   */
  public String getPermaLink() {
    return (String) get(Field.PERMA_LINK.toString());
  }

  /**
   * Sets the perma link of activity stream.
   *
   * @param permalink the perma link
   */
  public void setPermaLink(final String permalink) {
    if (permalink == null) {
      put(Field.PERMA_LINK.toString(), "");
    } else {
      put(Field.PERMA_LINK.toString(), Util.getBaseUrl() + permalink);
    }
  }

  /**
   * Gets the favicon url of activity stream.
   *
   * @return the favicon url
   */
  public String getFaviconUrl() {
    return (String) get(Field.FAVICON_URL.toString());
  }

  /**
   * Sets the favicon url.
   *
   * @param faviconUrl the favicon url
   */
  public void setFaviconUrl(String faviconUrl) {
    if (faviconUrl == null) {
      put(Field.FAVICON_URL.toString(), "");
    } else {
      put(Field.FAVICON_URL.toString(), Util.getBaseUrl() + faviconUrl);
    }
  }


  /**
   * Inits default values
   */
  private void initialize() {
    setType("");
    setPrettyId("");
    setFaviconUrl(null);
    setTitle("");
    setPermaLink(null);
  }

}
