/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.social.user.profile;

import java.util.List;
import java.util.Map;

import org.exoplatform.social.core.identity.model.Profile;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Feb 26, 2015  
 */
public abstract class UserProfileComparator {
  
  abstract boolean hasChanged();
  
  /**
   * Compare the old value stored by property 'key' with the new one. If it has been changed then update it 
   * else ignore it
   * 
   * @param profile user's profile before update
   * @param key the property to be updated
   * @param newValue
   * @return
   */
  public boolean hasChanged(Profile profile, String key, String newValue) {
    String oldValue = (String) profile.getProperty(key);
    if (oldValue != null ? !oldValue.equals(newValue) : newValue != null) {
      profile.setProperty(key, newValue);
      return true;
    } else {
      return false;
    }
  }
  
  /**
   * Compare the old value stored by property 'key' with the new one. If it has been changed then update it 
   * else ignore it
   * 
   * @param profile user's profile before update
   * @param key the property to be updated
   * @param newValue
   * @return
   */
  public boolean hasChanged(Profile profile, String key, List<Map<String, String>> newValue) {
    List<Map<String, String>> oldValue = (List<Map<String, String>>) profile.getProperty(key);
    if ((oldValue == null || oldValue.isEmpty()) && (newValue == null || newValue.isEmpty())) {
      return false;
    }
    if (oldValue == null || (! isEquals(oldValue, newValue))) {
      profile.setProperty(key, newValue);
      return true;
    }
    return false;
  }
  
  private boolean isEquals(List<Map<String, String>> list1, List<Map<String, String>> list2) {
    if (list2 == null) return true;
    if (list1.size() != list2.size()) {
      return false;
    }
    int size = list1.size();
    for (int i = 0; i < size; i++) {
      if (! isEqual(list1.get(i), list2.get(i))) {
        return false;
      }
    }
    return true;
  }
  
  private boolean isEqual(Map<String, String> m1, Map<String, String> m2) {
    if (m1.size() != m2.size())
      return false;
    for (String key : m1.keySet()) {
      String val1 = String.valueOf(m1.get(key));
      String val2 = String.valueOf(m2.get(key));
      if ((val1 == null && val2 != null) || (val1 != null && !val1.equals(val2)))
        return false;
    }
    return true;
  }
  
}
