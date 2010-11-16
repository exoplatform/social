/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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

import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;

/**
 * Use this class to know the stream context and its information. As defined in
 * http://activitystrea.ms/
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Nov 10, 2010
 */
public interface ActivityStream {

  /**
   * The type of activity stream, can be user or space.
   */
  public static enum Type {
    /**
     * User Stream Type.
     */
    USER(OrganizationIdentityProvider.NAME),
    /**
     * Space Stream Type.
     */
    SPACE(SpaceIdentityProvider.NAME);

    /**
     * String type.
     */
    private final String typeString;

    /**
     * private constructor.
     * @param string string type
     */
    private Type(final String string) {
      typeString = string;
    }

    /**
     * Returns string type.
     *
     * @return string type of context, can be "user" or "space"
     */
    @Override
    public String toString() {
      return typeString;
    }
  }

  /**
   * Sets type (could be "space" or "user").
   *
   * @param name the type name
   */
  void setType(String name);

  /**
   * Gets stream Id (internal storage).
   * @return stream uuid
   */
  String getId();

  /**
   * Sets stream Id (internal storage).
   * @param id the uuid of published-node
   */
  void setId(String id);

  /**
   * Gets prettyId name. Can be: "root" or "space_abc".
   * @return pretty id
   */
  String getPrettyId();


  /**
   * Sets prettyId name.
   * @param prettyId the prettyId name
   */
  void setPrettyId(String prettyId);

  /**
   * Gets stream type, could be Type.USER or Type.SPACE.
   * @return the Type of ActivityManager
   */
  Type getType();

  /**
   * Sets stream type.
   * @param type the Type of ActivityManager
   */
  void setType(Type type);

  /**
   * Gets a string specifying the URL for the stream's favicon.
   *
   * @return the favicon Url
   */
  String getFaviconUrl();

  /**
   * Sets a string specifying the URL for the stream's favicon.
   *
   * @param faviconUrl the favicon Url
   */
  void setFaviconUrl(String faviconUrl);

  /**
   * Gets a human-readable name for this stream. This property MAY be null if
   * the stream has no title or name.
   *
   * @return the stream's title
   */
  String getTitle();

  /**
   * Sets a human-readable name for this stream. This property MAY be null if
   * the stream has no title or name.
   *
   * @param title the stream title
   */
  void setTitle(String title);

  /**
   * Gets the link to the UI endpoint of the stream in portal.
   * <p/>
   * A string specifying the stream's URL
   *
   * @return the permanent link for this activity stream
   */
  String getPermaLink();

  /**
   * Sets the link to the UI endpoint of the stream in portal.
   * <p/>
   * A string specifying the stream's URL
   *
   * @param permaLink the permanent link for this activity stream
   */
  void setPermaLink(String permaLink);
}
