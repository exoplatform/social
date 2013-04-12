/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.social.common.embedder;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.AbstractCommonTest;

/**
 * @since 4.0.0-GA
 */
public class EmbedderTest extends AbstractCommonTest {
  
  private static final Log LOG = ExoLogger.getLogger(EmbedderTest.class);
  
  private OembedEmbedder embedder;
  
  @Override
  public void setUp() throws Exception {
    super.setUp();
    embedder = (OembedEmbedder) getContainer().getComponentInstanceOfType(OembedEmbedder.class);
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  /**
   * Test youtube link
   */
  public void testYoutube() {
    // youtube video link, exist media object
    ExoSocialMedia videoObj = embedder.getExoSocialMedia("http://www.youtube.com/watch?v=CZUXUjhXzDo");
    if(videoObj == null) {
      LOG.warn("Can't connect to youtube");
    } else {
      assertEquals("YouTube",videoObj.getProvider());
    }
  } 
  
  /**
   * test slideshare link
   */
  public void testSlideShare() {
    // slideshare oembed response
    ExoSocialMedia slideObj = embedder.getExoSocialMedia("http://www.slideshare.net/sh1mmer/using-nodejs-to-make-html5-work-for-everyone");
    if(slideObj == null) {
      LOG.warn("Can't connect to slideshare");
    } else {
      assertEquals("SlideShare", slideObj.getProvider());
    }
  }
  
  /**
   * test links that dont match any url schemes
   */
  public void testNonMediaLink() {
    // whatever link that does not match any url schemes
    assertNull(embedder.getExoSocialMedia("www.google.com"));
    
    // youtube homepage, get no media object
    assertNull(embedder.getExoSocialMedia("www.youtube.com"));
  }
}
