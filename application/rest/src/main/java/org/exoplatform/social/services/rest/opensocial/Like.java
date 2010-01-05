/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.social.services.rest.opensocial;

/**
 * Like.java - Like model
 * @author     hoatle <hoatlevan at gmail dot com>
 * @since      Dec 29, 2009
 * @copyright  eXo Platform SAS 
 */
/**
 * Model contain like detail information.
 *
 */
public class Like {
  private String _identityId,
                 _thumbnail,
                 _username,
                 _fullName;
  
  public void setIdentityId(String identityId) { 
    _identityId = identityId;
  }
  public String getIdentityId() {
    return _identityId;
  }
  public String getThumbnail() { 
    return _thumbnail;
  }
  public void setThumbnail(String thumbnail) { 
    _thumbnail = thumbnail;
  }
  public String getUsername() { 
    return _username;
  }
  public void setUsername(String username) { 
    _username = username;
  }
  public String getFullName() { 
    return _fullName;
  }
  public void setFullName(String fullName) { 
    _fullName = fullName;
  }
}
