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

import java.util.HashSet;
import java.util.Set;

import org.exoplatform.social.common.xmlprocessor.DOMParser;
import org.exoplatform.social.common.xmlprocessor.Tokenizer;
import org.exoplatform.social.common.xmlprocessor.model.Node;
import org.exoplatform.social.common.xmlprocessor.model.XMLTagFilterPolicy;

import junit.framework.TestCase;

/**
 * Unit test for {@link DOMXMLTagFilterPlugin}.
 */
public class DOMXMLTagFilterPluginTest extends TestCase {

  public void testDoFilter() {
    XMLTagFilterPolicy tagFilterPolicyBasicText = new XMLTagFilterPolicy();
    tagFilterPolicyBasicText.addAllowedTags("b", "i", "br");
    Set<String> aAttributes = new HashSet<String>();
    aAttributes.add("href");
    tagFilterPolicyBasicText.addAllowedTag("a", aAttributes);
    Node rootNode;
    assertEquals(
            "",
            new DOMXMLTagFilterPlugin(tagFilterPolicyBasicText).doFilter(
                    DOMParser.createDOMTree(new Node(), Tokenizer.tokenize("")))
                    .toString());
    assertEquals(
            "hello 1",
            new DOMXMLTagFilterPlugin(tagFilterPolicyBasicText).doFilter(
                    DOMParser.createDOMTree(new Node(), Tokenizer.tokenize("hello 1")))
                    .toString());

    rootNode = new Node();
    assertEquals(
            "<c><a href=\"http://\">hello2</a></c>",
            new DOMXMLTagFilterPlugin(tagFilterPolicyBasicText).doFilter(
                    DOMParser.createDOMTree(rootNode,
                            Tokenizer.tokenize("<c><a HREF=\"http://\" id=\"hello\">hello2</a></c>")))
                    .toString());
    assertEquals(3, rootNode.getChildNodes().size());
    assertEquals("<c>", rootNode.getChildNodes().get(0).getContent());
    assertEquals("</c>", rootNode.getChildNodes().get(2).getContent());
    assertEquals("a", rootNode.getChildNodes().get(1).getTitle());
    assertEquals("hello2", rootNode.getChildNodes().get(1).getChildNodes().get(0).toString());

    rootNode = new Node();
    assertEquals(
            "<c><i>hello world</i><a href=\"http://\">hello2</a></c>",
            new DOMXMLTagFilterPlugin(tagFilterPolicyBasicText).doFilter(
                    DOMParser.createDOMTree(rootNode,
                            Tokenizer.tokenize("<c><i>hello world</i><a HREF=\"http://\" id=\"hello\">hello2</a></c>")))
                    .toString());
    assertEquals(4, rootNode.getChildNodes().size());
    assertEquals("<c>", rootNode.getChildNodes().get(0).getContent());
    assertEquals("</c>", rootNode.getChildNodes().get(3).getContent());
    assertEquals("i", rootNode.getChildNodes().get(1).getTitle());
    assertEquals("hello world", rootNode.getChildNodes().get(1).getChildNodes().get(0).toString());
    assertEquals("a", rootNode.getChildNodes().get(2).getTitle());
    assertEquals("hello2", rootNode.getChildNodes().get(2).getChildNodes().get(0).toString());
  }

}
