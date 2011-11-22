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

import org.exoplatform.social.common.xmlprocessor.DOMParser;
import org.exoplatform.social.common.xmlprocessor.Tokenizer;
import org.exoplatform.social.common.xmlprocessor.model.Node;

import junit.framework.TestCase;

public class DOMLineBreakerFilterPluginTest extends TestCase {

  public void testDOMLineBreakerFilter() {
    assertEquals(
            "",
            new DOMLineBreakerFilterPlugin().doFilter(
                    DOMParser.createDOMTree(new Node(), Tokenizer.tokenize("")))
                    .toString());
    assertEquals(
            "hello 1",
            new DOMLineBreakerFilterPlugin().doFilter(
                    DOMParser.createDOMTree(new Node(), Tokenizer.tokenize("hello 1")))
                    .toString());

    assertEquals(
            "hello 1<br />hello 2",
            new DOMLineBreakerFilterPlugin().doFilter(
                    DOMParser.createDOMTree(new Node(),
                            Tokenizer.tokenize("hello 1\nhello 2"))).toString());
    assertEquals(
            "hello 1<br />hello 2",
            new DOMLineBreakerFilterPlugin().doFilter(
                    DOMParser.createDOMTree(new Node(),
                            Tokenizer.tokenize("hello 1\r\nhello 2"))).toString());
    assertEquals(
            "hello 1 <br /> hello 2",
            new DOMLineBreakerFilterPlugin().doFilter(
                    DOMParser.createDOMTree(new Node(),
                            Tokenizer.tokenize("hello 1 <br /> hello 2"))).toString());
    assertEquals(
            "hello 1 <br /> hello 2 <c>",
            new DOMLineBreakerFilterPlugin().doFilter(
                    DOMParser.createDOMTree(new Node(),
                            Tokenizer.tokenize("hello 1 <br /> hello 2 <c>"))).toString());

  }
}
