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

import org.exoplatform.social.common.xmlprocessor.DOMParser;
import org.exoplatform.social.common.xmlprocessor.Tokenizer;
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

    assertEquals("rootNode.toString() must be <b style=\"header\"></b>","<b style=\"header\"></b>", rootNode.toString());
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

    assertEquals("rootNode.toString() must be <b style=\"header\">","<b style=\"header\">",
                            rootNode.getChildNodes().get(0).getContent());
  }
  /**
   *  unit test for {@link Node#isTextNode()}
   */
  public void testIsTextNode(){
    Node testNode = new Node();
    testNode.setContent("hello");
    assertTrue("testNode.isTextNode() must be true", testNode.isTextNode());
    
    Node testNode2 = new Node();
    Node testNode3 = new Node();
    testNode2.addChildNode(testNode3);
    testNode3.setParentNode(testNode2);
    assertFalse("testNode2.isTextNode() must be false", testNode2.isTextNode());
    
    Node testNode4 = new Node();
    testNode4.setTitle("hello");
    assertFalse("testNode2.isTextNode() must be false", testNode4.isTextNode());
  }
  
  /**
   *  unit test for {@link Node#isRootNode()}
   */
  public void testIsRootNode(){
    Node testNode = new Node();
    assertTrue("testNode.isRootNode() must be true", testNode.isRootNode());
    
    Node testNode2 = new Node();
    Node testNode3 = new Node();
    testNode3.setParentNode(testNode2);
    testNode2.addChildNode(testNode3);
    
    assertFalse("testNode3.testIsRootNode() must be false", testNode3.isRootNode());
  }
  
  /**
   *  unit test for {@link Node#isEmptyNode()}
   */
  public void testIsSelfClosedNode(){
    Node testNode = new Node();
    testNode.setTitle("a");
    
    Attributes testNodeAttribute = new Attributes();
    testNodeAttribute.put("href", "http://google.com");
    
    assertTrue("testNode.isSelfCloseNode() must be true", testNode.isEmptyNode());
  }
  
  public void testInsertAfterWithRefNode(){
    Node rootNode = DOMParser.createDOMTree(Tokenizer.tokenize("<a>help</a><b>hello</b>"));
    Node nodeToAdd = new Node();
    nodeToAdd.setContent("test Node");
    
    rootNode.insertAfter(rootNode.getChildNodes().get(0), nodeToAdd);
    
    assertEquals("nodeToAdd must be inserted after <a> node", "<a>help</a>test Node<b>hello</b>", rootNode.toString());        
  }
  
  public void testInsertAfterWithPosition(){
    Node rootNode = DOMParser.createDOMTree(Tokenizer.tokenize("<a>help</a><b>hello</b>"));
    Node nodeToAdd = new Node();
    nodeToAdd.setContent("test Node");
    
    rootNode.insertAfter(0, nodeToAdd);
    
    assertEquals("nodeToAdd must be inserted after <a> node", "<a>help</a>test Node<b>hello</b>", rootNode.toString());        
  }
  
  public void testSetGet(){
    Node node = new Node();

    node.setTitle("abc");
    assertEquals("abc", node.getTitle());

    node.setContent("def");
    assertEquals("def", node.getContent());
  }
}
