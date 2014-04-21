/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.social.plugin.link;

import junit.framework.TestCase;

/**
 * Unit tests for {@link UILinkUtil}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since  1.2.0-GA
 * @since  Apr 13, 2011
 */
public class UILinkUtilTest extends TestCase {

  public void testIsImageLink() {
    try {
      UILinkUtil.isImageLink(null);
      fail("Expecting IllegalArgumentException");
    } catch (IllegalArgumentException iae) {
      assertEquals("link must not be null", iae.getMessage());
    }
    final String notImageLink = "http://exoplatform.com";
    final String jpgImageLink = "http://exoplatform.com/path/img.jpg";
    final String gifImageLink = "http://exoplatform.com/path/img.gif";
    final String jpegImageLink = "http://exoplatform.com/path/img.jpeg";
    final String bmpImageLink = "http://exoplatform.com/path/img.bmp";
    final String pngImageLink = "http://exoplatform.com/path/img.png";
    final String tifImageLink = "http://exoplatform.com/path/img.tif";
    final String imageWithParams = "http://exoplatform.com/path/img.jpg?width=300";

    assertFalse(UILinkUtil.isImageLink(notImageLink));
    assertTrue(UILinkUtil.isImageLink(jpgImageLink));
    assertTrue(UILinkUtil.isImageLink(gifImageLink));
    assertTrue(UILinkUtil.isImageLink(bmpImageLink));
    assertTrue(UILinkUtil.isImageLink(pngImageLink));
    assertTrue(UILinkUtil.isImageLink(tifImageLink));
    //Need to handle this case
    //assertTrue(UILinkUtil.isImageLink(imageWithParams));
  }
  
  public void testSimpleEscapeHtml() {
    String input = null;
    assertEquals(UILinkUtil.simpleEscapeHtml(input), "");
    input = "test";
    assertEquals(UILinkUtil.simpleEscapeHtml(input), "test");
    input = "<test";
    assertEquals(UILinkUtil.simpleEscapeHtml(input), "&lt;test");
    input = "<test>";
    assertEquals(UILinkUtil.simpleEscapeHtml(input), "&lt;test&gt;");
    input = "<a href=\"abc.com\">test</a>";
    assertEquals(UILinkUtil.simpleEscapeHtml(input), "&lt;a href=&#34;abc.com&#34;&gt;test&lt;/a&gt;");
    input = "<script type=\"text/javascript\">alert('test');</script>";
    assertEquals(UILinkUtil.simpleEscapeHtml(input), "&lt;script type=&#34;text/javascript&#34;&gt;alert(&#39;test&#39;);&lt;/script&gt;");
    input = "<script>alert('test');</script>";
    assertEquals(UILinkUtil.simpleEscapeHtml(input), "&lt;script&gt;alert(&#39;test&#39;);&lt;/script&gt;");
  }
}
