/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.social.common.service.utils;

import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ThreadHelper {
  public static final String DEFAULT_PATTERN = "Social-Thread ##counter# - #name#";
  private static final Pattern INVALID_PATTERN = Pattern.compile(".*#\\w+#.*");

  private static AtomicLong threadCounter = new AtomicLong();
  
  private ThreadHelper() {
  }
  
  private static long nextThreadCounter() {
      return threadCounter.getAndIncrement();
  }

  /**
   * Creates a new thread name with the given pattern
   * <p/>
   * @param pattern the pattern
   * @param name    the name
   * @return the thread name, which is unique
   */
  public static String resolveThreadName(String pattern, String name) {
      if (pattern == null) {
          pattern = DEFAULT_PATTERN;
      }

      // we support #longName# and #name# as name placeholders
      String longName = name;
      String shortName = name;
      // must quote the names to have it work as literal replacement
      shortName = Matcher.quoteReplacement(shortName);
      longName = Matcher.quoteReplacement(longName);

      // replace tokens
      String answer = pattern.replaceFirst("#counter#", "" + nextThreadCounter());
      answer = answer.replaceFirst("#longName#", longName);
      answer = answer.replaceFirst("#name#", shortName);

      // are there any #word# combos left, if so they should be considered invalid tokens
      if (INVALID_PATTERN.matcher(answer).matches()) {
          throw new IllegalArgumentException("Pattern is invalid: " + pattern);
      }

      return answer;
  }

}
