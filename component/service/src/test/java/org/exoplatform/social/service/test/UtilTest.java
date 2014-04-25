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
package org.exoplatform.social.service.test;

import org.exoplatform.social.service.rest.Util;

import junit.framework.TestCase;


/**
 * @since 1.2.0-GA
 */
public class UtilTest extends TestCase {
  /**
   * Performs testing for {@link Util#isValidURL(String)}
   * 
   * @throws Exception
   */
  public void testIsValidURL() throws Exception {
    assertTrue(Util.isValidURL("abcd.com"));
    assertTrue(Util.isValidURL("http://google.com"));
    assertTrue(Util.isValidURL("http://địachỉdoanhnghiệp.vn"));
    assertTrue(Util.isValidURL("http://www.google.com/language_tools?hl=en"));
    assertTrue(Util.isValidURL("https://mail.google.com/mail/?shva=1#inbox"));
    assertTrue(Util.isValidURL("http://a+b=sadasd.com.vn"));
    assertTrue(Util.isValidURL("mailto:abc@facebook.com"));
    assertTrue(Util.isValidURL("http://translate.google.com/#en|vi|What has changed?"));
    assertTrue(Util.isValidURL("translate.google.com/#en|vi|What has changed?"));
    assertFalse(Util.isValidURL(null));
    assertFalse(Util.isValidURL(""));
    assertFalse(Util.isValidURL("abc"));
    assertFalse(Util.isValidURL("a bc.com"));
    assertFalse(Util.isValidURL("abc.c om"));
    assertFalse(Util.isValidURL("abc : fsdfs"));
    assertFalse(Util.isValidURL("abc #$ vn"));
  }
  
  /**
   * Performs testing for {@link Util#getDecodeQueryURL(String)}
   * 
   * @throws Exception
   */
  public void testGetDecodeQueryURL() throws Exception {
    String url = "http://google.com";
    assertEquals(url, Util.getDecodeQueryURL(url));
    
    url = "translate.google.com/#en|vi|What has changed?";
    assertEquals(url, Util.getDecodeQueryURL(url));

    url = "translate.google.com/?translate=abc#en";
    assertEquals(url, Util.getDecodeQueryURL(url));
    
    url = "http://google.com?%3Cscript%3Ealert(%22Link_attached%22)%3C/script%3E";
    assertEquals("http://google.com?<script>alert(\"Link_attached\")</script>", Util.getDecodeQueryURL(url));
  }
}
