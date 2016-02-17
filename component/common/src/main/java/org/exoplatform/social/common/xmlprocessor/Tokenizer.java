/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.social.common.xmlprocessor;

import java.util.ArrayList;
import java.util.List;


/**
 * XML scanner/tokenizer
 *
 * @author Ly Minh Phuong - http://phuonglm.net
 */
public class Tokenizer {
  /**
   * Splits tag of XML String to an arrayList
   *
   * @param html
   * @return list of HTML tags
   */
  public static List<String> tokenize(String html) {
    ArrayList<String> tokens = new ArrayList<String>();
    int pos = 0;
    String token = "";
    int len = html.length();
    while (pos < len) {
      char c = html.charAt(pos);

      String ahead = html.substring(pos, pos > len - 4 ? len : pos + 4);

      // a comment is starting
      if ("<!--".equals(ahead)) {
        // store the current token
        if (token.length() > 0) {
          tokens.add(token);
        }

        // clear the token
        token = "";

        // search the end of <......>
        int end = moveToMarkerEnd(pos, "-->", html);
        tokens.add(html.substring(pos, end));
        pos = end;

        // a new "<" token is starting
      } else if ('<' == c) {

        // store the current token
        if (token.length() > 0) {
          tokens.add(token);
        }

        // clear the token
        token = "";

        // search the end of <......>
        int end = moveToMarkerEnd(pos, ">", html);
        tokens.add(html.substring(pos, end));
        pos = end;

      } else {
        token = token + c;
        pos++;
      }

    }
    if (token.length() > 0) {
      tokens.add(token);
    }
    return tokens;
  }

  private static int moveToMarkerEnd(int pos, String marker, String s) {
    int i = s.indexOf(marker, pos);
    if (i > -1) {
      pos = i + marker.length();
    } else {
      pos = s.length();
    }
    return pos;
  }
}
