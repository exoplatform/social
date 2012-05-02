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
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.Validate;

/**
 * Provides utility for working with jcr
 *
 * @author <a href="http://hanhvq@gmail.com">hanhvq (hanhvq at gmail dot com)</a>
 * @since Jun 9 2011
 * @since 1.1.6
 */
public class Util {
  
  /** The Constant NT_UNSTRUCTURED. */
  private static final String NT_UNSTRUCTURED = "nt:unstructured".intern();
  private static final String SLASH_STR = "/";
  
  /**
   * Creates nodes by a provided rootNode and path. If the patch does not exist, create that path with node type as
   * nt:unstructured.
   * <p/>
   * The path must be a valid JCR path. For example:
   * <p/>
   * <pre>
   *  Util.createNodes(aNode, "a");
   *  //or
   *  Util.createNodes(aNode, "a/b");
   * </pre>
   *
   * @param rootNode the root node
   * @param relPath  the relative path to create
   */
  public static void createNodes(Node rootNode, String relPath) {
    Validate.notNull(rootNode, "rootNode must not be null");
    Validate.notNull(relPath, "path must not be null");
    Node node;
    try {
      // path exists, does nothing
      // if path does not exist, PathNotFoundException will be thrown
      if (rootNode.getNode(relPath) != null) {
        return;
      }
    } catch (PathNotFoundException pne) {
      //It's ok, the provided path does not exist, create it.
    } catch (RepositoryException re) {
      throw new RuntimeException(re);
    }
    try {
      if (relPath.indexOf(SLASH_STR) < 0) {
        node = rootNode.addNode(relPath);
      } else {
        String[] ar = relPath.split(SLASH_STR);
        for (int i = 0; i < ar.length; i++) {
          if (rootNode.hasNode(ar[i])) {
            node = rootNode.getNode(ar[i]);
          } else {
            node = rootNode.addNode(ar[i], NT_UNSTRUCTURED);
          }
          rootNode = node;
        }
        if (rootNode.isNew()) {
          rootNode.getSession().save();
        } else {
          rootNode.getParent().save();
        }
      }
    } catch (RepositoryException re) {
      throw new RuntimeException(re);
    }
  }
}