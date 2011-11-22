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

import org.apache.commons.lang.StringEscapeUtils;
import org.exoplatform.social.common.xmlprocessor.BaseXMLFilterPlugin;
import org.exoplatform.social.common.xmlprocessor.model.Node;


/**
 * The filter escapes all content of the DOMTree to make sure it cleaned.
 * <b>Note:</b> this filter cannot detect that content escaped or not so make sure that you don't use it twice or
 * using it with escaped content.
 *
 * @author Ly Minh Phuong - http://phuonglm.net
 * @since  1.2.1
 */
public class DOMContentEscapeFilterPlugin extends BaseXMLFilterPlugin {

  /**
   * {@inheritDoc}
   */
  @Override
  public Object doFilter(Object input) {
    if (input instanceof Node) {
      nodeFilter((Node) input);
    }
    return input;
  }

  /**
   * Filter by nodes.
   *
   * @param node a node
   */
  private void nodeFilter(Node node) {
    LinkedList<Node> currentChildNode = node.getChildNodes();
    if (node.getParentNode() != null) {
      if (!node.getContent().isEmpty()) {
        node.setContent(StringEscapeUtils.escapeHtml(node.getContent()));
      }
    }
    for (int i = 0; i < currentChildNode.size(); i++) {
      nodeFilter(currentChildNode.get(i));
    }

  }

}
