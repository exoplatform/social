/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.social.portlet.profile;

import java.util.*;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.webui.URLUtils;
import org.exoplatform.web.application.RequestContext;

/**
 * Contains some common methods for using as utility.<br> 
 * 
 */
public class Utils {

  /**
   * Sorts list by key.<br>
   * 
   * @param l
   *        List for sorting.
   * @param key1
   *        First condition for sort.
   * @param key2
   *        Second condition for sort.
   * @param reverse
   *        Revert list or not.
   * @return sorted list.
   */
  static public List sort(List l, String key1, String key2, boolean reverse) {
    if (l == null)
      return null;
    Collections.sort(l, new MapSorter(key1, key2));
    if (reverse)
      Collections.reverse(l);
    return l;
  }

  /**
   * Sorts list by key.<br>
   * 
   * @param l
   *        List for sorting.
   * @param key1
   *        First condition for sort.
   * @param key2
   *        Second condition for sort.
   *        
   * @return sorted list.
   */
  static public List sort(List l, String key1, String key2) {
    return sort(l, key1, key2, false);
  }

  /**
   * Sorts list by key.<br>
   * 
   * @param l
   *        List for sorting.
   * @param key
   *        Condition for sort.
   *        
   * @return sorted list.
   */
  static public List sort(List l, String key) {
    return sort(l, key, null, false);
  }

  /**
   * Selects an object from list.<br>
   * 
   * @param l
   *        List for selecting.
   * @param key
   *        Key for selecting.
   * @param value
   *        Value as second condition for selecting.
   * @param onlyFirst
   *        If select the first element.
   *        
   * @return object that match the key.
   */
  static private Object select(List<Map> l, String key, String value, boolean onlyFirst) {
    List res = new ArrayList<Map>();

    for (Map m : l) {
      if(m.containsKey(key) && m.get(key).equals(value)) {       
        if(onlyFirst)
          return m;
        else
          res.add(m);
      }
    }
    return res;
  }

  /**
   * Selects an object from list.<br>
   * 
   * @param l
   *        List for selecting.
   * @param key
   *        Key for selecting.
   * @param value
   *        Value as second condition for selecting.
   *        
   * @return object that match the key.
   */
  static public List select(List<Map> l, String key, String value) {
    return (List) select(l, key, value, false);
  }

  /**
   * Selects the first element of the list.<br>
   * 
   * @param l
   *        List for selecting.
   * @param key
   *        Key for selecting.
   * @param value
   *        Value as second condition for selecting.
   *        
   * @return object that match the key.
   */
  static public Map selectFirst(List<Map> l, String key, String value) {
    return (Map) select(l, key, value, true);
  }

  /**
   * Gets current identity of login user.<br>
   * 
   * @return identity
   *         Current identity of login user.
   *         
   * @throws Exception
   */
  static public Identity getCurrentIdentity() throws Exception {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      IdentityManager identityManager = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
      RequestContext context = RequestContext.getCurrentInstance();
      String currentUserName = context.getRemoteUser();
      if (URLUtils.getCurrentUser() != null) currentUserName = URLUtils.getCurrentUser() ;
      
      return identityManager.getIdentityByRemoteId(OrganizationIdentityProvider.NAME, currentUserName);
  }
  
  /**
   * Sorts the map depend on key.<br> 
   *
   */
  protected static class MapSorter implements Comparator {
    private String key1;
    private String key2;

    /**
     * Default Constructor.<br>
     * 
     * @param key1
     *        First condition.
     * @param key2
     *        Second condition.
     */
    public MapSorter(String key1, String key2) {
      this.key1 = key1;
      this.key2 = key2;
    }

    /**
     * Compares two objects.<br>
     * 
     * @param o1
     *        First object.
     *        
     * @param o2
     *        Second object.
     * 
     * @return int 
     */
    public int compare(Object o1, Object o2) {
      int res = compare(o1, o2, key1);
      if(res == 0 && key2 != null)
        res = compare(o1, o2, key2);
      return res;
    }

    /**
     * Compares two objects with key.<br>
     * 
     * @param o1
     *        First object.
     *        
     * @param o2
     *        Second object.
     *        
     * @param key
     *        Compare condition.
     *        
     * @return int
     */
    public int compare(Object o1, Object o2, String key) {
      HashMap h1 = null, h2 = null;

      if (o1 instanceof HashMap) {
        h1 = (HashMap) o1;
      }
      if (o2 instanceof HashMap) {
        h2 = (HashMap) o2;
      }
      if(h1 == null && h2 == null)
        return 0;
      if(h1 == null)
        return -1;
      if(h2 == null)
        return 1;
      if(!h1.containsKey(key) && !h2.containsKey(key))
        return 0;
      if(!h1.containsKey(key))
        return -1;
      if(!h2.containsKey(key))
        return 1;
      Object v1 = h1.get(key);
      Object v2 = h2.get(key);
      if(v1 instanceof String)
        return ((String)v1).compareToIgnoreCase((String)v2);
      else
        return ((Comparable)v1).compareTo(v2);
    }
  }
}
