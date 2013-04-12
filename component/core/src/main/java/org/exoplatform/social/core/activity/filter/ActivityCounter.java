/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.social.core.activity.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;

public class ActivityCounter implements Collection<ExoSocialActivity> {

  private String[] ids;
  private List<ExoSocialActivity> gotList = null;

  /**
   * Constructor with offset and limit
   * @param offset
   * @param limit
   * @param totalSize total size of nodes matched filter.
   */
  public ActivityCounter() {
    ids = new String[0];
    gotList = new ArrayList<ExoSocialActivity>();
  }
  
 

  @Override
  public int size() {
    return gotList.size();
  }

  @Override
  public boolean isEmpty() {
    return gotList.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    if (o instanceof ExoSocialActivity) {
      ExoSocialActivity a = (ExoSocialActivity) o;
      return ArrayUtils.indexOf(ids, a.getId()) >= 0;
    }
    
    //
    return false;
  }
  

  @Override
  public Iterator<ExoSocialActivity> iterator() {
    return gotList.iterator();
  }

  @Override
  public Object[] toArray() {
    return null;
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return null;
  }

  @Override
  public boolean add(ExoSocialActivity e) {
    if (contains(e)) {
      return false;
    }
    
    //
    ids = (String[]) ArrayUtils.add(ids, e.getId());
    gotList.add(e);
    return true;
  }
  
  public List<ExoSocialActivity> result() {
    return gotList;
  }

  @Override
  public boolean remove(Object o) {
    if (o instanceof ExoSocialActivity) {
      ExoSocialActivity a = (ExoSocialActivity) o;
      ids = (String[]) ArrayUtils.removeElement(ids, a.getId());
      return gotList.remove(o);
    }
    
    //
    return false;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return false;
  }

  @Override
  public boolean addAll(Collection<? extends ExoSocialActivity> c) {
    return false;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return false;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return false;
  }

  @Override
  public void clear() {
    ids = new String[0];
    gotList = new ArrayList<ExoSocialActivity>();
  }
}
