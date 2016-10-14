/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
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

public class StringUtil {

  // Code point of last 3 bytes UTF-8 character
  private static final int LAST_3_BYTE_CHAR = "\uFFFF".codePointAt(0);

  /**
   * Finds and removes UTF-8 character that need more than 3 bytes
   * 
   * @param input the input string potentially containing invalid character
   * @return string
   */
  public static String removeLongUTF(String input) {
    if (input == null) {
      return input;
    }
    StringBuilder sb = new StringBuilder(input);

    for (int i = 0; i < sb.length(); i++) {
      int codePoint = sb.codePointAt(i);
      if (codePoint > LAST_3_BYTE_CHAR) {
        int count = Character.charCount(codePoint);
        sb.replace(i, i + count, "");
        i -= 1;
      }
    }
    return sb.toString();
  }
}
