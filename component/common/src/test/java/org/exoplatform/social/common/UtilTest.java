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
package org.exoplatform.social.common;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import junit.framework.TestCase;


/**
 * Author : hanhvq@exolpatform.com
 * May 10, 2011 
 * @since 1.2.0-GA
 */
public class UtilTest extends TestCase {
  
  /** Logger */
  private static final Log LOG = ExoLogger.getLogger(UtilTest.class);
  
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
  
  public void testIsYoutubeLink() throws Exception {
    try {
      Util.isYoutubeLink(null);
      fail("Expecting IllegalArgumentException");
    } catch (IllegalArgumentException iae) {
      assertEquals("Link must be not null", iae.getMessage());
    }
    
    final String validYoutubeLink = "http://www.youtube.com/watch?v=fLexgOxsZu0";
    final String invalidYoutubeLink = "http://www.youtube.com/watch?v=";
    
    assertTrue(Util.isYoutubeLink(validYoutubeLink));
    assertFalse(Util.isYoutubeLink(invalidYoutubeLink));
  }
  
  public void testGetOembedData() throws Exception {
    try {
      assertNotNull(Util.getOembedData("http://www.youtube.com/watch?v=fLexgOxsZu0"));
    } catch (Exception e) {
      LOG.warn(e.getMessage());
    }
  }
}
