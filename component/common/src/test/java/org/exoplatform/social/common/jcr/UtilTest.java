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
package org.exoplatform.social.common.jcr;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import org.exoplatform.social.common.AbstractCommonTest;

/**
 * Created by The eXo Platform SAS
 * Author : hanhvq@exoplatform.com
 * Jun 16, 2011  
 */
public class UtilTest extends AbstractCommonTest {

  /**
    * Unit Test for {@link Util#createNodes(javax.jcr.Node, String)}.
    */
  public void testCreateNodes() {
    try {
      Util.createNodes(null, "abc");
      fail("Expecting IllegalArgumentException.");
    } catch (IllegalArgumentException iae) {
      assertEquals("rootNode must not be null", iae.getMessage());
    }
  
    try {
      Util.createNodes(getRootNode(), null);
      fail("Expecting IllegalArgumentException.");
    } catch (IllegalArgumentException iae) {
      assertEquals("path must not be null", iae.getMessage());
    }

    try {
      Util.createNodes(null, null);
      fail("Expecting IllegalArgumentException.");
    } catch (IllegalArgumentException iae) {
      assertEquals("rootNode must not be null", iae.getMessage());
    }

    createNodes("a", "a", false);
    createNodes("a", "a", false);
    createNodes("b/c/d", "b/c/d", false);
    createNodes("b/c/d", "b/c/d", false);
    createNodes("t/u/", "t/u", false);
    //does not support these paths
  
    try {
      createNodes("/e/f", "/e/f", true);
      fail("RuntimeException is expected with the relPath: /e/f");
    } catch (RuntimeException re) {
    }
    
  //tearDown
    try {
      getRootNode().getNode("a").remove();
      getRootNode().getNode("b/c/d").remove();
    } catch (RepositoryException re) {
      throw new RuntimeException(re);
    } finally {
      sessionManager.closeSession(true);
    }
  }

  private void createNodes(String providedPath, String expectedPath, boolean expectedToFail) {
    Node rootNode = getRootNode();
    Util.createNodes(rootNode, providedPath);
    if (expectedToFail) {
      return;
    }
  
    try {
      Node foundNode = rootNode.getNode(expectedPath);
      assertNotNull("foundNode must not be null", foundNode);
    } catch (RepositoryException e) {
      fail("Failed to get path: /" + providedPath);
    }
  }
  
  private Node getRootNode() {
    Session session = sessionManager.getOrOpenSession();
    Node rootNode = null;
    try {
      rootNode = session.getRootNode();
    } catch (RepositoryException e) {
      fail(e.getMessage());
    }
    assertNotNull("rootNode must not be null", rootNode);
      return rootNode;
  }
}
