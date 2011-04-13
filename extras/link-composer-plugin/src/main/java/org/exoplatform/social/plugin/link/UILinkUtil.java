/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.social.plugin.link;

import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;

/**
 * Utility class for link composer plugin.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Apr 13, 2011
 */
final public class UILinkUtil {

  /**
   * Checks if a provided link is am image link.
   * @param link the provided link
   * @return true if the provided link is an image link, otherwise, false.
   */
  public static boolean isImageLink(String link) {
    Validate.notNull(link, "link must not be null");
    Pattern pattern = Pattern.compile("(?-i)(\\.jpg|\\.gif|\\.jpeg|\\.bmp|\\.png|\\.tif)$");
    return pattern.matcher(link).find();
  }
}
