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
package org.exoplatform.social.service.rest.api.models;

import junit.framework.TestCase;

/**
 * Unit test for {@link CommentRestIn}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Sep 28, 2011
 * @since 1.2.3
 */
public class CommentRestInTest extends TestCase {

  /**
   * Tests: default values to check
   */
  public void testDefaultValues() {
    CommentRestIn commentRestIn = new CommentRestIn();
    assertNull("commentRestIn.getText() must be null", commentRestIn.getText());
  }

  /**
   * Tests: set values to check
   */
  public void testSetValues() {
    CommentRestIn commentRestIn = new CommentRestIn();
    String text = "Comment!";
    commentRestIn.setText(text);
    assertEquals("commentRestIn.getText() must return: " + text, text, commentRestIn.getText());
  }

  /**
   * Tests: check valid input
   *
   */
  public void testIsValid() {
    CommentRestIn commentRestIn = new CommentRestIn();
    assertFalse("commentRestIn.isValid() must be false", commentRestIn.isValid());
    commentRestIn.setText("Comment!");
    assertTrue("commentRestIn.isValid() must be true", commentRestIn.isValid());
  }

}
