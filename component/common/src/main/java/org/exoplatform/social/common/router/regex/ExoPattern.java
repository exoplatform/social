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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExoPattern {

  // Group pattern which uses to match Group /{group}/
  private static final Pattern            GROUP_PATTERN = Pattern.compile("\\(\\{(\\w+)\\}");

  private Pattern                         pattern;

  private String                          namedPattern;

  private Map<String, List<ExoGroupData>> groupInfo;

  public static ExoPattern compile(String regex) {
    return new ExoPattern(regex, 0);
  }

  public static ExoPattern compile(String regex, int flags) {
    return new ExoPattern(regex, flags);
  }

  private ExoPattern(String regex, int flags) {
    namedPattern = regex;
    pattern = buildStandardPattern(regex, flags);
    groupInfo = extractGroupInfo(regex);
  }

  public int indexOf(String groupName) {
    return indexOf(groupName, 0);
  }

  public int indexOf(String groupName, int index) {
    int idx = -1;
    if (groupInfo.containsKey(groupName)) {
      List<ExoGroupData> list = groupInfo.get(groupName);
      if (index < list.size()) {
        idx = list.get(index).getGroupIndex();
      }
    }
    return idx;
  }

  public ExoMatcher matcher(CharSequence input) {
    return new ExoMatcherImpl(this, input);
  }

  public boolean matches(String s) {
    return pattern.matcher(s).matches();
  }

  public Pattern pattern() {
    return pattern;
  }

  public String toString() {
    return namedPattern;
  }

  static private boolean isEscapedParent(String s, int pos) {
    int numSlashes = 0;
    while (pos > 0 && (s.charAt(pos - 1) == '\\')) {
      pos--;
      numSlashes++;
    }
    return numSlashes % 2 != 0;
  }

  static private boolean isNoncapturingParent(String s, int pos) {
    int len = s.length();
    boolean isLookbehind = false;
    if (pos >= 0 && pos + 4 < len) {
      String pre = s.substring(pos, pos + 4);
      isLookbehind = pre.equals("(?<=") || pre.equals("(?<!");
    }
    return (pos >= 0 && pos + 2 < len) && s.charAt(pos + 1) == '?'
        && (isLookbehind || s.charAt(pos + 2) != '<');
  }

  static private int countOpenParents(String s, int pos) {
    Pattern p = Pattern.compile("\\(");
    Matcher m = p.matcher(s.subSequence(0, pos));

    int numParens = 0;

    while (m.find()) {
      String match = m.group(0);

      // ignore escaped parents
      if (isEscapedParent(s, m.start()))
        continue;

      if (match.equals("(") && !isNoncapturingParent(s, m.start())) {
        numParens++;
      }
    }
    return numParens;
  }

  /**
   * Process extract given pattern string to Map of ArgumentName and GroupData
   * 
   * @param namedPattern
   * @return
   */
  static public Map<String, List<ExoGroupData>> extractGroupInfo(String namedPattern) {
    Map<String, List<ExoGroupData>> groupInfo = new LinkedHashMap<String, List<ExoGroupData>>();
    Matcher matcher = GROUP_PATTERN.matcher(namedPattern);
    while (matcher.find()) {

      int pos = matcher.start();

      // ignore escaped parent
      if (isEscapedParent(namedPattern, pos))
        continue;

      String name = matcher.group(1);
      int groupIndex = countOpenParents(namedPattern, pos);

      List<ExoGroupData> list;
      if (groupInfo.containsKey(name)) {
        list = groupInfo.get(name);
      } else {
        list = new ArrayList<ExoGroupData>();
      }
      list.add(new ExoGroupData(groupIndex, pos));
      groupInfo.put(name, list);
    }
    return groupInfo;
  }

  static private Pattern buildStandardPattern(String namedPattern, Integer flags) {

    StringBuilder s = new StringBuilder(namedPattern);
    Matcher m = GROUP_PATTERN.matcher(s);
    while (m.find()) {
      int start = m.start();
      int end = m.end();

      if (isEscapedParent(s.toString(), start)) {
        continue;
      }

      s.replace(start, end, "(");
      m.reset();
    }

    return Pattern.compile(s.toString(), flags);
  }

  static private class ExoGroupData {
    private int pos;

    private int groupIndex;

    ExoGroupData(int groupIndex, int pos) {
      this.groupIndex = groupIndex;
      this.pos = pos;
    }

    public int getGroupIndex() {
      return groupIndex;
    }

    public int getPos() {
      return pos;
    }

  }

}
