/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.social.common.router.regex;

import java.util.regex.Matcher;


public class ExoMatcherImpl implements ExoMatcher {

  private Matcher matcher;
  private ExoPattern parentPattern;

  ExoMatcherImpl(ExoPattern parentPattern, CharSequence input) {
    this.parentPattern = parentPattern;
    this.matcher = parentPattern.pattern().matcher(input);
  }

  public boolean matches() {
    return matcher.matches();
  }

  public boolean find() {
    return matcher.find();
  }

  public String group() {
    return matcher.group();
  }

  public String group(int group) {
    return matcher.group(group);
  }

  public int groupCount() {
    return matcher.groupCount();
  }

  public String group(String groupName) {
    return group(groupIndex(groupName));
  }

  private int groupIndex(String groupName) {
    int idx = parentPattern.indexOf(groupName);
    return idx > -1 ? idx + 1 : -1;
  }

  public int start() {
    return matcher.start();
  }

  public int start(int group) {
    return matcher.start(group);
  }

  public int end() {
    return matcher.end();
  }

  public int end(int group) {
    return matcher.end(group);
  }

  public String replaceAll(String replacement) {
    return matcher.replaceAll(replacement);
  }

  public boolean equals(Object obj) {
    return matcher.equals(obj);
  }

  public int hashCode() {
    return matcher.hashCode();
  }

  public String toString() {
    return matcher.toString();
  }

}
