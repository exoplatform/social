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
package org.exoplatform.social.service.rest;

/**
 * LinkShareRequest.java - LikeShareRequest model
 *
 * @author     hoatle <hoatlevan at gmail dot com>
 * @since      Jan 5, 2010
 * @copyright  eXo Platform SAS 
 */
public class LinkShareRequest {
  private String _link, _lang;
  /**
   * sets link
   * @param link
   */
  public void setLink(String link) {
    _link = link;
  }
  /**
   * gets link
   * @return link
   */
  public String getLink() {
    return _link;
  }
  /**
   * sets language
   * @param lang
   */
  public void setLang(String lang) {
    _lang = lang;
  }
  /**
   * gets language
   * @return language
   */
  public String getLang() {
    return _lang;
  }
  /**
   * verifies if this request is valid
   * @return true or false
   */
  public boolean verify() {
    if (_link != null && _link.length() > 0) {
      return true;
    }
    return false;
  }
}
