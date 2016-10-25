/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.social.core.jpa.updater.utils;


import org.chromattic.ext.format.BaseEncodingObjectFormatter;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 * exo@exoplatform.com
 * Oct 17, 2016
 */
public class IdentityUtil {
  private static final BaseEncodingObjectFormatter formatter = new BaseEncodingObjectFormatter();

  public static String getIdentityName(String nodeName) {
    String name = nodeName.replace("soc:", "");

    return formatter.decodeNodeName(null, name);
  }
}
