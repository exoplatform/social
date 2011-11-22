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

import org.exoplatform.social.common.xmlprocessor.Filter;
import org.exoplatform.social.common.xmlprocessor.model.XMLTagFilterPolicy;

import junit.framework.TestCase;

/**
 * Unit test for {@link XMLTagFilterPlugin}.
 */
public class XMLTagFilterPluginTest extends TestCase {

  public void testXMLTagFilter() {
    XMLTagFilterPolicy tagFilterPolicy = new XMLTagFilterPolicy();
    tagFilterPolicy.addAllowedTags("b", "i", "br");
    Set<String> aAttributes = new HashSet<String>();
    aAttributes.add("href");
    tagFilterPolicy.addAllowedTag("a", aAttributes);
    Filter xmlFilter = new XMLTagFilterPlugin(tagFilterPolicy);

    assertEquals("hello 1",
            xmlFilter.doFilter("hello 1"));

    assertEquals("&lt;c&gt;<a href=\"http://\">hello2</a>&lt;/c&gt;",
            xmlFilter.doFilter("<c><a HREF=\"http://\">hello2</a></c>"));

    assertEquals("3 < 5 >",
            xmlFilter.doFilter("3 < 5 >"));

    assertEquals("<b><i> hello 3</b> hello 4</i>",
            xmlFilter.doFilter("<b><i> hello 3</b> hello 4</i>"));

    assertEquals("<b> hello 5 <br /><i><i>&lt;h&gt;ee <b /><a><i>hello 10</i</a><i /> e&lt;/h&gt;</i></i></b>",
            xmlFilter.doFilter("<b ID=\"blablo\"> hello 5 <br   /><i><i><h>ee <b/><a><i>hello 10</i</a><i/> e</h></i></i></b>"));

    assertEquals("<b> hello 6 <br /><b>",
            xmlFilter.doFilter("<B> hello 6 <br/><b>"));
  }
}
