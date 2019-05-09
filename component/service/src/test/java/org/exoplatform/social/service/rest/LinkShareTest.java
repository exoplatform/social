package org.exoplatform.social.service.rest;

import org.junit.Test;

import static org.junit.Assert.*;

public class LinkShareTest {
  @Test
  public void shouldNotFetchLinkPreviewWhenPreviewIsDisabled() throws Exception {
    // Given
    String previousPropertyValue = System.getProperty(LinkShare.ACTIVITY_LINK_PREVIEW_ENABLED_PROPERTY);
    System.setProperty(LinkShare.ACTIVITY_LINK_PREVIEW_ENABLED_PROPERTY, "false");

    try {
      // When
      LinkShare linkShare = LinkShare.getInstance("http://dummy.url.com");

      // Then
      assertNotNull(linkShare);
      assertEquals("http://dummy.url.com", linkShare.getLink());
      assertEquals("", linkShare.getDescription());
      assertNull(linkShare.getMediaObject());
    } finally {
      if (previousPropertyValue == null) {
        System.clearProperty(LinkShare.ACTIVITY_LINK_PREVIEW_ENABLED_PROPERTY);
      } else {
        System.setProperty(LinkShare.ACTIVITY_LINK_PREVIEW_ENABLED_PROPERTY, previousPropertyValue);
      }
    }
  }
}
