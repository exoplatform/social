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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.exoplatform.social.common.xmlprocessor.model.Node;

/**
 * DOMParser utility
 *
 * @author Ly Minh Phuong - http://phuonglm.net
 */
public class DOMParser {
  public static final Pattern COMMENTPATTERN = Pattern.compile("<!--.*"); // <!--.........>
  public static final Pattern TAGSTARTPATTERN = Pattern
          .compile("<(?i)(\\w+\\b)\\s*(.*)/?>$"); // <tag ....props.....>
  public static final Pattern TAGCLOSEPATTERN = Pattern
          .compile("</(?i)(\\w+\\b)\\s*>$"); // </tag .........>
  public static final Pattern SELFTCLOSETAGPATTERN = Pattern.compile("<.+/\\s*?>");
  public static final Pattern ATTRIBUTESPATTERN = Pattern
          .compile("(\\S*)\\s*=\\s*(\"([^\"]*)\"|'([^']*)')"); // prop="...."

  /**
   * Creates the XML DOM tree from XML token List tree.
   *
   * @param xmlTokens The HTML token array.
   * @return TreeNode contain the content parsed from token list.
   */

  public static Node createDOMTree(List<String> xmlTokens) {
    return createDOMTree(new Node(), xmlTokens);
  }

  /**
   * Creates the XML DOM tree from XML token List tree as childNodeList of currentNode.
   *
   * @param currentNode The Node to add childNode to.
   * @param xmlTokens   The HTML token array
   * @return TreeNode contain the content parsed from token list
   */
  public static Node createDOMTree(Node currentNode, List<String> xmlTokens) {
    Node parsingNode;

    for (int i = 0; i < xmlTokens.size(); i++) {
      String token = xmlTokens.get(i);
      Matcher startMatcher = TAGSTARTPATTERN.matcher(token);
      Matcher endMatcher = TAGCLOSEPATTERN.matcher(token);

      if (COMMENTPATTERN.matcher(token).find()) {
        parsingNode = new Node();
        parsingNode.setParentNode(currentNode);
        currentNode.addChildNode(parsingNode);
        parsingNode.setTitle(token.substring(4, token.length() - 3));
      } else if (startMatcher.find()) {
        String tag = startMatcher.group(1).toLowerCase();

        if (SELFTCLOSETAGPATTERN.matcher(token).find()) {
          parsingNode = new Node();
          parsingNode.setParentNode(currentNode);
          currentNode.addChildNode(parsingNode);
          parsingNode.setTitle(tag);
          String tokenBody = startMatcher.group(2);
          Matcher attributes = ATTRIBUTESPATTERN.matcher(tokenBody);

          while (attributes.find()) {
            String attr = attributes.group(1).toLowerCase();
            String val = attributes.group(4) == null ? attributes.group(3) : attributes.group(4);
            parsingNode.addAttribute(attr, val);
          }
        } else {
          int findDeep = 0;
          int matchedEnd = 0;
          for (int j = i + 1; j < xmlTokens.size(); j++) {
            Matcher startFindMatcher = TAGSTARTPATTERN
                    .matcher(xmlTokens.get(j));
            Matcher endFindMatcher = TAGCLOSEPATTERN.matcher(xmlTokens.get(j));
            if (startFindMatcher.find()) {
              if (!SELFTCLOSETAGPATTERN.matcher(xmlTokens.get(j)).find()) {
                findDeep++;
              }
            } else if (endFindMatcher.find()) {
              if (endFindMatcher.group(1).toLowerCase().equals(tag)
                      && findDeep == 0) {
                matchedEnd = j;
                break;
              } else {
                findDeep--;
              }
            }
          }
          if (matchedEnd > 0) {
            parsingNode = new Node();
            parsingNode.setParentNode(currentNode);
            parsingNode.setTitle(tag);

            String tokenBody = startMatcher.group(2);

            Matcher attributes = ATTRIBUTESPATTERN.matcher(tokenBody);

            while (attributes.find()) {
              String attr = attributes.group(1).toLowerCase();
              String val = attributes.group(4) == null ? attributes.group(3) : attributes.group(4);
              parsingNode.addAttribute(attr, val);
            }
            currentNode.addChildNode(parsingNode);
            createDOMTree(parsingNode, xmlTokens.subList(i + 1, matchedEnd));
            i = matchedEnd;
          } else {
            parsingNode = new Node();
            parsingNode.setParentNode(currentNode);
            currentNode.addChildNode(parsingNode);
            parsingNode.setContent(token);
          }
        }
      } else if (endMatcher.find()) {
        parsingNode = new Node();
        parsingNode.setParentNode(currentNode);
        parsingNode.setContent(token);
        currentNode.addChildNode(parsingNode);
      } else {
        parsingNode = new Node();
        parsingNode.setParentNode(currentNode);
        parsingNode.setContent(token);
        currentNode.addChildNode(parsingNode);
      }
    }
    return currentNode;
  }
}
