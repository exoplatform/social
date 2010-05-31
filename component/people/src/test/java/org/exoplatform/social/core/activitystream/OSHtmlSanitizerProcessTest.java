/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.activitystream;

import junit.framework.TestCase;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;

import java.util.ArrayList;
import java.util.Arrays;

public class OSHtmlSanitizerProcessTest extends TestCase {

  public void testInitParams() {

    OSHtmlSanitizerProcessor processor = new OSHtmlSanitizerProcessor(null);
    String [] actual = processor.getAllowedTags();
    assertEquals(OSHtmlSanitizerProcessor.OS_ALLOWED_TAGS, actual);
    InitParams params = new InitParams();
    processor = new OSHtmlSanitizerProcessor(params);
    actual = processor.getAllowedTags();
    assertEquals(OSHtmlSanitizerProcessor.OS_ALLOWED_TAGS, actual);
    params = new InitParams();
    ValuesParam allowedTagsParams = new ValuesParam();
    allowedTagsParams.setName("allowedTags");
    allowedTagsParams.setValues(new ArrayList<String>(Arrays.asList("foo","bar", "zed")));
    params.addParameter(allowedTagsParams);
    processor = new OSHtmlSanitizerProcessor(params);
    actual = processor.getAllowedTags();
//    AssertUtils.assertContains(actual, "foo","bar", "zed");
  }



  public void testEscapeHtml() {

    String [] allowed = new String []{"b", "i", "a", "span", "em", "strong", "p", "ol", "ul", "li", "br"};

    OSHtmlSanitizerProcessor processor = new OSHtmlSanitizerProcessor(null);
    processor.setAllowedTags(allowed);
    String sample = "this is a <b> tag to keep</b>";
    assertEquals(sample, processor.escapeHtml(sample));

    // tags with attributes
    sample = "text <a href='#' >bar</a> zed" ;
    assertEquals(sample, processor.escapeHtml(sample));

    // self closing tags
    sample = "<script href='#' />bar</a>";
    assertEquals("&lt;script href='#' /&gt;bar</a>", processor.escapeHtml(sample));

    // forbidden tag
    sample = "<script>foo</script>";
    assertEquals("&lt;script&gt;foo&lt;/script&gt;", processor.escapeHtml(sample));

    // embedded
    sample = "<span><strong>foo</strong>bar<script>zed</script></span>";
    assertEquals("<span><strong>foo</strong>bar&lt;script&gt;zed&lt;/script&gt;</span>", processor.escapeHtml(sample));
  }

}
