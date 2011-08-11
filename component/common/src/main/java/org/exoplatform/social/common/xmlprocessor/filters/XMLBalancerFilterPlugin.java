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
package org.exoplatform.social.common.xmlprocessor.filters;

import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.social.common.xmlprocessor.BaseXMLFilterPlugin;
import org.exoplatform.social.common.xmlprocessor.DOMParser;
import org.exoplatform.social.common.xmlprocessor.Tokenizer;
import org.exoplatform.social.common.xmlprocessor.model.Node;

/**
 * This Filter try Smart way to autoCorrect the typo in HTML input (auto close opened TAG, escape wrong TAG).
 *
 * @author Ly Minh Phuong - http://phuonglm.net
 */
public class XMLBalancerFilterPlugin extends BaseXMLFilterPlugin {
  /**
   * {@inheritDoc}
   */
  @Override
  public Object doFilter(Object input) {
    if (input instanceof String) {
      input = createDOMTree((String) input).toString();
    }
    return input;
  }

  /**
   * Creates DOM tree From String input and Balance it.
   *
   * @param xmlString
   * @return
   */
  private Node createDOMTree(String xmlString) {
    Node rootNode = new Node();
    Node parsingNode = rootNode;
    Node currentNode = rootNode;

    List<String> xmlTokens = Tokenizer.tokenize(xmlString);

    for (String token : xmlTokens) {
      Matcher startMatcher = DOMParser.TAGSTARTPATTERN.matcher(token);
      Matcher endMatcher = DOMParser.TAGCLOSEPATTERN.matcher(token);

      if (DOMParser.COMMENTPATTERN.matcher(token).find()) {
        parsingNode = new Node();
        parsingNode.setParentNode(currentNode);
        currentNode.addChildNode(parsingNode);
        parsingNode.setTitle(token);
      } else if (startMatcher.find()) {
        String tag = startMatcher.group(1).toLowerCase();

        parsingNode = new Node();
        parsingNode.setParentNode(currentNode);
        parsingNode.setTitle(tag);

        String tokenBody = startMatcher.group(2);

        Matcher attributes = DOMParser.ATTRIBUTESPATTERN.matcher(tokenBody);

        while (attributes.find()) {
          String attr = attributes.group(1).toLowerCase();
          String val = attributes.group(4) == null ? attributes.group(3) : attributes.group(4);
          parsingNode.addAttribute(attr, val);
        }
        currentNode.addChildNode(parsingNode);
        if (!DOMParser.SELFTCLOSETAGPATTERN.matcher(token).find()) {
          currentNode = parsingNode;
        }
      } else if (endMatcher.find()) {
        String tag = endMatcher.group(1).toLowerCase();
        Node searchOpenedNode = currentNode;
        while (!searchOpenedNode.getTitle().equals(tag)
                && !(searchOpenedNode.getParentNode() == null)) {
          searchOpenedNode = searchOpenedNode.getParentNode();
        }
        if (searchOpenedNode.getParentNode() == null) {
          Node invalidNode = new Node();
          invalidNode.setContent(StringEscapeUtils.escapeHtml(token));
          currentNode.addChildNode(invalidNode);
        } else if (searchOpenedNode.getTitle().equals(currentNode.getTitle())) {
          currentNode = currentNode.getParentNode();
        } else {
          currentNode = searchOpenedNode.getParentNode();
        }

      } else {
        parsingNode = new Node();
        parsingNode.setParentNode(currentNode);
        parsingNode.setContent(StringEscapeUtils.escapeHtml(token));

        currentNode.addChildNode(parsingNode);
      }
    }
    return rootNode;
  }
}
