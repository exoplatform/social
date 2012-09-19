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


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.common.AbstractCommonTest;
import org.exoplatform.social.common.xmlprocessor.filters.DOMContentEscapeFilterPlugin;
import org.exoplatform.social.common.xmlprocessor.filters.DOMLineBreakerFilterPlugin;
import org.exoplatform.social.common.xmlprocessor.filters.LineBreakerFilterPlugin;
import org.exoplatform.social.common.xmlprocessor.filters.XMLBalancerFilterPlugin;
import org.exoplatform.social.common.xmlprocessor.model.XMLTagFilterPolicy;
import org.exoplatform.social.common.xmlprocessor.model.XMLTagFilterPolicy.AllowedTag;

/**
 * Unit Test for {@link XMLProcessor}.
 *
 * @author Ly Minh Phuong - http://phuonglm.net
 */
public class XMLProcessorTest extends AbstractCommonTest {

  private XMLProcessor xmlProcessor;

  private PortalContainer portalContainer;

  private List<Filter> tearDownFilter;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    portalContainer = PortalContainer.getInstance();
    xmlProcessor = (XMLProcessor) portalContainer.getComponentInstanceOfType(XMLProcessor.class);
    tearDownFilter = new ArrayList<Filter>();
  }

  @Override
  public void tearDown() throws Exception {
    for (Filter filter : tearDownFilter) {
      xmlProcessor.removeFilter(filter);
    }
    super.tearDown();
  }

  /**
   * Tests {@link XMLProcessor#addFilter(Filter)}.
   */
  public void testAddFilter() {
    LineBreakerFilterPlugin filter = new LineBreakerFilterPlugin();
    tearDownFilter.add(filter);
    xmlProcessor.addFilter(filter);
  }

  /**
   * Tests {@link XMLProcessor#removeFilter(Filter)}.
   */
  public void testRemoveFilter() {
    xmlProcessor.removeFilter(new LineBreakerFilterPlugin());
  }

  /**
   * Tests {@link XMLProcessor#addFilterPlugin(BaseXMLFilterPlugin)}
   * and {@link XMLProcessor#removeFilterPlugin(BaseXMLFilterPlugin)}.
   */
  public void testAddAndRemoveFilterPlugin() {
    FakeXMLFilterPluginPlugin fakeXMLFilterPlugin = new FakeXMLFilterPluginPlugin();
    xmlProcessor.addFilterPlugin(fakeXMLFilterPlugin);
    xmlProcessor.removeFilterPlugin(fakeXMLFilterPlugin);
  }

  /**
   * Tests {@link XMLProcessor#process(Object)}.
   */
  public void testProcess() {
    Object output = xmlProcessor.process("<h3>hello world our there</h3><d>");
    assertEquals("<h3>hello world our there</h3><d>", output);
  }


  /**
   * Tests {@link XMLProcessor#process(Object)} with:
   * {@link org.exoplatform.social.common.xmlprocessor.filters.LineBreakerFilterPlugin}, {@link org.exoplatform.social.common.xmlprocessor.filters.XMLBalancerFilterPlugin}.
   */
  public void testXMLBalancer() {
    XMLTagFilterPolicy tagFilterPolicy = new XMLTagFilterPolicy();
    tagFilterPolicy.addAllowedTags("div", "p", "b", "br", "a");
    XMLProcessor xmlProcessor = new XMLProcessorImpl();
    LineBreakerFilterPlugin breakLineFilter = new LineBreakerFilterPlugin();
    XMLBalancerFilterPlugin xmlBalancer = new XMLBalancerFilterPlugin(tagFilterPolicy);

    xmlProcessor.addFilter(breakLineFilter);
    xmlProcessor.addFilter(xmlBalancer);

    assertEquals(null,
            xmlProcessor.process(null));
    assertEquals("", xmlProcessor.process(""));
    assertEquals("hello 1", xmlProcessor.process("hello 1"));
    assertEquals("hello 1<br /> hello2",
            xmlProcessor.process("hello 1\n hello2"));
    assertEquals("hello 1&lt;&gt; hello2",
            xmlProcessor.process("hello 1<> hello2"));
    assertEquals("<a>hello 1</a>", xmlProcessor.process("<a>hello 1"));
    assertEquals("hello 1&lt;/a&gt;", xmlProcessor.process("hello 1</a>"));
    assertEquals("<a>Hello 2<a><b></b></a></a>", xmlProcessor.process("<a<b>Hello 2<a><b>"));

  }


  /**
   * Fake xml filter plugin
   */
  static class FakeXMLFilterPluginPlugin extends BaseXMLFilterPlugin {
    @Override
    public Object doFilter(Object input) {
      return null;
    }
  }

}
