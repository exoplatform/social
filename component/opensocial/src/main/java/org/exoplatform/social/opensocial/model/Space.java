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
package org.exoplatform.social.opensocial.model;

import org.apache.shindig.protocol.model.Exportablebean;

import com.google.inject.ImplementedBy;

@ImplementedBy(SpaceImpl.class)
@Exportablebean
public interface Space {

  /**
   * An enumeration of fields in the json Space object.
   */
  public static enum Field {
    /**
     * the json field for id of space
     */
    ID("id"),
    /**
     * the json field for displayName of space
     */
    DISPLAY_NAME("displayName");

    /**
     * the json key for this field.
     */
    private final String jsonString;

    /**
     * Construct the a field enum.
     * @param jsonString the json key for the field.
     */
    private Field(String jsonString) {
      this.jsonString = jsonString;
    }

    @Override
    public String toString() {
      return this.jsonString;
    }
  }

  public void setId(String id);

  public String getId();

  public void setDisplayName(String displayName);

  public String getDisplayName();

}
