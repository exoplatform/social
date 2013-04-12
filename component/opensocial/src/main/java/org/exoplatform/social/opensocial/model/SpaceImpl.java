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


public class SpaceImpl implements Space {

  private String displayName;
  private String id;

  public SpaceImpl() {

  }

  public String getDisplayName() {
    return displayName;
  }

  public String getId() {
    return id;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public void setId(String id) {
    this.id = id;
  }

}
