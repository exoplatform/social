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

import java.util.LinkedList;

import org.exoplatform.social.common.xmlprocessor.BaseXMLFilterPlugin;
import org.exoplatform.social.common.xmlprocessor.model.Node;

/**
 * This filter checks if DOM tree Content contain native line-break character and replace it with
 * <code>&lt;br /&gt;</code> tags.
 *
 * @author Ly Minh Phuong - http://phuonglm.net
 * @since  1.2.1
 */
public class DOMLineBreakerFilterPlugin extends BaseXMLFilterPlugin {

  /**
   * Filters any new line characters by new line html tag (&lt;br&gt;).
   *
   * @param input the input data
   * @return the filtered input
   */
  public Object doFilter(Object input) {
    if (input instanceof Node) {
      nodeFilter((Node) input);
    }
    return input;
  }

  private Node nodeFilter(Node currentNode) {
    LinkedList<Node> currentChildNode = currentNode.getChildNodes();
    if (currentNode.getParentNode() != null) {
      if (!currentNode.getContent().isEmpty()) {
        LinkedList<Node> parentChildNodes = currentNode.getParentNode()
                .getChildNodes();
        int startPostion = parentChildNodes.indexOf(currentNode);
        int i = startPostion;
        Node parentNode = currentNode.getParentNode();
        String content = currentNode.getContent();
        String[] contentArray = content.split("\\r?\\n");

        if (contentArray.length > 1) {
          currentNode.setContent(contentArray[0]);
          for (int j = 1; j < contentArray.length; j++) {
            Node nodeToAdd = new Node();
            nodeToAdd.setTitle("br");
            nodeToAdd.setParentNode(parentNode);
            parentChildNodes.add(++i, nodeToAdd);
            nodeToAdd = new Node();
            nodeToAdd.setContent(contentArray[i]);
            nodeToAdd.setParentNode(parentNode);
            parentChildNodes.add(++i, nodeToAdd);
          }
        }
      }
    }
    for (int i = 0; i < currentChildNode.size(); i++) {
      nodeFilter(currentChildNode.get(i));
    }
    return currentNode;

  }

}
