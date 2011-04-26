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
package org.exoplatform.social.common.jcr;

import javax.jcr.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.apache.commons.lang.Validate;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Provides utility for working with jcr
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Dec 22, 2010
 * @since 1.2.0-GA
 */
public class Util {

  /**
   * The logger
   */
  private static final Log LOG = ExoLogger.getLogger(Util.class);
  private static final String SLASH_STR = "/";
  /**
   * Gets properties name pattern from an array of property names.
   *
   * @param propertyNames
   * @return properties name pattern.
   */
  public static String getPropertiesNamePattern(String[] propertyNames) {
    Validate.notEmpty(propertyNames, "propertyNames must not be empty");

    StringBuilder sb = new StringBuilder(256);
    sb.append(propertyNames[0]);
    for (int i = 1; i < propertyNames.length; i++) {
      sb.append('|').append(propertyNames[i]);
    }
    return sb.toString();
  }

  /**
   * Converts array of Value to array of string.
   * Make sure these Values can getString().
   *
   * @param values
   * @return
   * @throws Exception
   */
  public static String[] convertValuesToStrings(Value[] values) throws Exception {
    if (values.length == 1) {
      return new String[]{values[0].getString()};
    }
    String[] strArray = new String[values.length];
    for (int i = 0; i < values.length; ++i) {
      strArray[i] = values[i].getString();
    }
    return strArray;
  }


  /**
   * Convert a map of string key and string value into a string array
   * where values are in the form key=value.
   *
   * @param templateParams
   * @return
   */
  public static String[] convertMapToStrings(Map<String, String> templateParams) {
    if (templateParams == null) {
      return null;
    }
    Set<String> keys = templateParams.keySet();
    String [] result = new String[keys.size()];
    int i = 0;
    for (String key : keys) {
      result[i++] = key + "=" + templateParams.get(key);
    }
    return result;
  }

  /**
   * Converts an array of {@link Value} into a map of string.
   * The values are expected to be of string type and in the form key=value
   *
   * @param values
   * @return
   */
  public static Map<String, String> convertValuesToMap(Value[] values) {
    if (values == null) {
      return null;
    }
    Map<String, String> result = new HashMap<String, String>();
    for (Value value : values) {
      try {
        String val = value.getString();
        int equalIndex = val.indexOf("=");
        if (equalIndex > 0) {
          result.put(val.split("=")[0], val.substring(equalIndex + 1));
        }
      } catch (Exception e) {
        LOG.warn(e.getMessage(), e);
      }
    }
    return result;
  }
  
  /**
   * Converts a list to an array.
   * 
   * @param list List to be converted to array.
   * @param type Type of list's and array's element.
   * @return An array with the same type of element in list.
   */
  public static <T> T[] convertListToArray(List<T> list, Class<T> type) {
    return list.toArray((T[])java.lang.reflect.Array.newInstance(type, list.size()));
  }

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
    Session session = null;
    try {
      session = rootNode.getSession();
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
            node = rootNode.addNode(ar[i], NodeTypes.NT_UNSTRUCTURED);
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
