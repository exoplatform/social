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

import java.util.Arrays;
import java.util.List;

import org.exoplatform.social.common.xmlprocessor.model.Node;

import junit.framework.TestCase;

/**
 * Unit Test for {@link DOMParser}.
 */
public class DOMParserTest extends TestCase {

  public void testCreateDOMTreeListOfString() {
    List<String> input;
    input = Arrays.asList("");
    Node rootNode;
    rootNode = DOMParser.createDOMTree(input);
    assertEquals(1, rootNode.getChildNodes().size());
    assertEquals("", rootNode.getChildNodes().get(0).getContent());

    input = Arrays.asList("<a>", " b ", "</a>", "<i>", "e", "<h>", "</i>");
    rootNode = DOMParser.createDOMTree(input);
    assertEquals(5, rootNode.getChildNodes().size());
    assertEquals(1, rootNode.getChildNodes().get(0).getChildNodes().size());
    assertEquals("a", rootNode.getChildNodes().get(0).getTitle());
    assertEquals(" b ", rootNode.getChildNodes().get(0).getChildNodes().get(0).getContent());
    assertEquals("<i>", rootNode.getChildNodes().get(1).getContent());
    assertEquals("e", rootNode.getChildNodes().get(2).getContent());
    assertEquals("<h>", rootNode.getChildNodes().get(3).getContent());
    assertEquals("</i>", rootNode.getChildNodes().get(4).getContent());
  }

  public void testCreateDOMTreeNodeListOfString() {
    List<String> input;
    input = Arrays.asList("");
    Node rootNode = new Node();

    DOMParser.createDOMTree(rootNode, input);
    assertEquals(1, rootNode.getChildNodes().size());
    assertEquals("", rootNode.getChildNodes().get(0).getContent());

    rootNode = new Node();

    input = Arrays.asList("<a>", " b ", "</a>", "<i>", "e", "<h>", "</i>");
    DOMParser.createDOMTree(rootNode, input);
    assertEquals(5, rootNode.getChildNodes().size());
    assertEquals(1, rootNode.getChildNodes().get(0).getChildNodes().size());
    assertEquals("a", rootNode.getChildNodes().get(0).getTitle());
    assertEquals(" b ", rootNode.getChildNodes().get(0).getChildNodes().get(0).getContent());
    assertEquals("<i>", rootNode.getChildNodes().get(1).getContent());
    assertEquals("e", rootNode.getChildNodes().get(2).getContent());
    assertEquals("<h>", rootNode.getChildNodes().get(3).getContent());
    assertEquals("</i>", rootNode.getChildNodes().get(4).getContent());
  }
  
  public void testSpecialCase() {
    String input = "<b></b>";
    List<String> xmlTokens = Tokenizer.tokenize(input);

    Node rootNode =  DOMParser.createDOMTree(xmlTokens);
    assertEquals("<b></b>", rootNode.toString());
    

  }
  
  public void testBrCase() {
    String input = "<br></br>";
    List<String> xmlTokens = Tokenizer.tokenize(input);

    Node rootNode =  DOMParser.createDOMTree(xmlTokens);
    assertEquals("<br />", rootNode.toString());
    

  }

}
