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

import java.util.Comparator;

/**
 * Util class for activity
 *
 * @author    <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since     Aug 9, 2010
 * @copyright eXo SAS
 */
public class Util {
  /**
   * Comparator used to order the activity by postedTime
   * @return
   */
  public static Comparator<Activity> activityComparator() {
    return new Comparator<Activity>() {

      public int compare(Activity a1, Activity a2) {
        if (a1 == null || a2 == null) {
          throw new IllegalArgumentException("Cannot compare null Activity");
        }
        return (int) (a2.getPostedTime() - a1.getPostedTime());
      }
    };
  }
}
