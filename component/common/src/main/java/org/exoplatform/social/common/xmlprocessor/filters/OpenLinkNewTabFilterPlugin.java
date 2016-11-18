/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.social.common.xmlprocessor.filters;

import org.exoplatform.social.common.xmlprocessor.BaseXMLFilterPlugin;
import org.exoplatform.social.common.xmlprocessor.DOMParser;
import org.exoplatform.social.common.xmlprocessor.Tokenizer;
import org.exoplatform.social.common.xmlprocessor.model.Node;

import java.util.LinkedList;
import java.util.List;

/**
 * This OpenLinkNewTabFilterPlugin is a plugin for XMLProcessor which will force all links have to be open in new tab.
 * It will set attribute target="_blank" if this attribute is omitted or had other value.
 * For example:
 * {@literal <a href="http://abc.com">abc</a> => <a href="http://abc.com" target="_blank">abc</a>}
 * {@literal <a href="http://abc.com" target="frame_name">abc</a> => <a href="http://abc.com" target="_blank">abc</a>}
 *
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class OpenLinkNewTabFilterPlugin extends BaseXMLFilterPlugin {
  @Override
  public Object doFilter(Object input) {
    if (input instanceof String) {
      return filterLinkTags((String)input);
    }
    return input;
  }

  private String filterLinkTags(String xmlString){
    List<String> xmlTokens = Tokenizer.tokenize(xmlString);
    Node rootNode = DOMParser.createDOMTree(xmlTokens);
    nodeFilter(rootNode);
    return rootNode.toString();
  }

  private void nodeFilter(Node currentNode) {
    if ("a".equalsIgnoreCase(currentNode.getTitle())) {
      String target = currentNode.getAttributes().get("target");
      if (target == null || !"_blank".equalsIgnoreCase(target)) {
        currentNode.getAttributes().put("target", "_blank");
      }
      return;
    }
    LinkedList<Node> currentChildNode = currentNode.getChildNodes();
    for (int i = 0; i < currentChildNode.size(); i++) {
      nodeFilter(currentChildNode.get(i));
    }
  }
}
