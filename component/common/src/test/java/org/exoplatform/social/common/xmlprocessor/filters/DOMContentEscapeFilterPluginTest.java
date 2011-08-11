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

/**
 * Unit test for {@link DOMContentEscapeFilterPlugin}.
 */
public class DOMContentEscapeFilterPluginTest extends TestCase {

  /**
   * Tests {@link DOMContentEscapeFilterPlugin#doFilter(Object)}.
   */
  public void testDOMContentEscape() {
    assertEquals(
            "",
            new DOMContentEscapeFilterPlugin().doFilter(
                    DOMParser.createDOMTree(new Node(), Tokenizer.tokenize("")))
                    .toString());

    assertEquals(
            "hello 1\r\nhello 2",
            new DOMContentEscapeFilterPlugin().doFilter(
                    DOMParser.createDOMTree(new Node(),
                            Tokenizer.tokenize("hello 1\r\nhello 2"))).toString());

    assertEquals(
            "&lt;b&gt; = hello 1 &amp;&quot;\\ hello 2 &lt;a&gt;",
            new DOMContentEscapeFilterPlugin().doFilter(
                    DOMParser.createDOMTree(new Node(),
                            Tokenizer.tokenize("<b> = hello 1 &\"\\ hello 2 <a>")))
                    .toString());
  }
}
