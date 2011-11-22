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
package org.exoplatform.social.common.xmlprocessor.model;

import junit.framework.TestCase;

/**
 * Unit Test for {@link Node}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jun 18, 2011
 */
public class NodeTest extends TestCase {

  public void testGetParentNode() {
    Node parentNode = new Node();
    Node childNode = new Node();

    childNode.setParentNode(parentNode);

    assertEquals("The childNode.getParentNode() should return parentNode",
                      parentNode, childNode.getParentNode());
  }

  public void testNodeToString() {
    Node rootNode = new Node();
    Node childNode = new Node();
    childNode.setTitle("b");

    Attributes attributes = new Attributes();
    attributes.put("style", "header");
    childNode.setAttributes(attributes);

    rootNode.addChildNode(childNode);
    childNode.setParentNode(rootNode);

    assertEquals("rootNode.toString() must be <b style=\"header\" />","<b style=\"header\" />", rootNode.toString());
  }


  public void testConvertToContent() {
    Node rootNode = new Node();
    Node childNode = new Node();
    childNode.setTitle("b");

    Attributes attributes = new Attributes();
    attributes.put("style", "header");
    childNode.setAttributes(attributes);

    rootNode.addChildNode(childNode);
    childNode.setParentNode(rootNode);

    childNode.convertToContent();

    assertEquals("rootNode.toString() must be <b style=\"header\" />","<b style=\"header\" />",
                            rootNode.getChildNodes().get(0).getContent());
  }

  public void testSetGet(){
    Node node = new Node();

    node.setTitle("abc");
    assertEquals("abc", node.getTitle());

    node.setContent("def");
    assertEquals("def", node.getContent());
  }
}
