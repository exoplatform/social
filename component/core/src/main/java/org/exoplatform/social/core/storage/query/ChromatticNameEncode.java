/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
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
package org.exoplatform.social.core.storage.query;

import org.chromattic.api.format.FormatterContext;

/**
 * This class clone from org.chromattic.ext.format.BaseEncodingObjectFormatter
 * 
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$ 
 */
public class ChromatticNameEncode {

  private static boolean isSpecialChar(char c) {
    return getCode(c) != null;
  }

  private static String getCode(char c) {
    if (c == 0x9
      || c == 0xA
      || c == 0xD
      || (c >= 0x20 && c <= 0xD7FF)
      || (c >= 0xE000 && c <= 0xFFFD)
      || (c >= 0x10000 && c <= 0x10FFFF)) {
      switch (c) {
        case '{':
          return "00";
        case '}':
          return "01";
        case '.':
          return "02";
        case '/':
          return "03";
        case ':':
          return "04";
        case '[':
          return "05";
        case ']':
          return "06";
        case '|':
          return "07";
        case '*':
          return "08";
        case '%':
          return "09";
        default:
          return null;
      }
    }
    else {
      throw new UnsupportedOperationException();
    }
  }

  private static final char[] table = new char[]{
    '{', '}', '.', '/', ':', '[', ']', '|', '*', '%'
  };

  private String decode(String s, int from) {
    StringBuffer buffer = new StringBuffer(s.length());
    buffer.append(s, 0, from);
    int to = s.length();
    while (from < to) {
      char c = s.charAt(from++);
      if (c == '%') {
        if (from + 1 >= to) {
          throw new IllegalStateException("Cannot decode wrong name " + s);
        }
        char c1 = s.charAt(from++);
        if (c1 != '0') {
          throw new IllegalStateException("Cannot decode wrong name " + s);
        }
        char c2 = s.charAt(from++);
        if (c2 < '0' || c2 > '9') {
          throw new IllegalStateException("Cannot decode wrong name " + s);
        }
        buffer.append(table[c2 - '0']);
      }
      else {
        buffer.append(c);
      }
    }
    return buffer.toString();
  }

  public String decodeNodeName(FormatterContext context, String internalName) {
    int length = internalName.length();
    for (int i = 0; i < length; i++) {
      char c = internalName.charAt(i);
      if (c == '%') {
        return decode(internalName, i);
      }
    }
    return internalName;
  }

  private static String encode(String s, int from) {
    StringBuffer buffer = new StringBuffer((s.length() * 5) >> 2);
    buffer.append(s, 0, from);
    int to = s.length();
    while (from < to) {
      char c = s.charAt(from++);
      String code = getCode(c);
      if (code != null) {
        buffer.append('%');
        buffer.append(code);
      }
      else {
        buffer.append(c);
      }
    }
    return buffer.toString();
  }

  public static String encodeNodeName(String externalName) {
    int length = externalName.length();

    //
    for (int i = 0; i < length; i++) {
      char c = externalName.charAt(i);
      if (isSpecialChar(c)) {
        externalName = encode(externalName, i);
        break;
      }
    }

    //
    return externalName;
  }
}
