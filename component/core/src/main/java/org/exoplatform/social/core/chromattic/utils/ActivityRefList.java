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
package org.exoplatform.social.core.chromattic.utils;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Collection;
import java.util.Locale;

import org.exoplatform.social.core.chromattic.entity.ActivityRef;
import org.exoplatform.social.core.chromattic.entity.ActivityRefDayEntity;
import org.exoplatform.social.core.chromattic.entity.ActivityRefListEntity;

public class ActivityRefList implements Collection<ActivityRef> {

  protected String[] MONTH_NAME = new DateFormatSymbols(Locale.ENGLISH).getMonths();

  private ActivityRefListEntity listEntity;

  public ActivityRefList(final ActivityRefListEntity listEntity) {
    this.listEntity = listEntity;
  }

  public int size() {
    return listEntity.getNumber();
  }

  public boolean isEmpty() {
    return listEntity.getNumber().equals(0);
  }

  public boolean contains(final Object o) {
    throw new RuntimeException();
  }

  public ActivityRefIterator iterator() {
    return new ActivityRefIterator(listEntity);
  }

  public Object[] toArray() {
    throw new RuntimeException();
  }

  public <T> T[] toArray(final T[] ts) {
    throw new RuntimeException();
  }
  
  public boolean add(final ActivityRef activityRef) {
    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
    calendar.setTimeInMillis(activityRef.getLastUpdated());

    String year = String.valueOf(calendar.get(Calendar.YEAR));
    String month = MONTH_NAME[calendar.get(Calendar.MONTH)];
    String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

    ActivityRefDayEntity dayEntity = listEntity.getYear(year).getMonth(month).getDay(day);
    
    //needs to check it existing or not in list
    ActivityRef ref = dayEntity.getActivityRefs().get(activityRef.getActivityEntity().getId());
    
    if (ref == null) {
      ref = dayEntity.createRef();
      dayEntity.getActivityRefs().put(activityRef.getActivityEntity().getId(), ref);
      dayEntity.inc();
    }
    
    //
    ref.setLastUpdated(activityRef.getLastUpdated());
    ref.setActivityEntity(activityRef.getActivityEntity());

    return true;
  }

  public boolean remove(final Object o) {
    throw new RuntimeException();
  }

  public boolean containsAll(final Collection<?> objects) {
    throw new RuntimeException();
  }

  public boolean addAll(final Collection<? extends ActivityRef> activityRefs) {
    throw new RuntimeException();
  }

  public boolean removeAll(final Collection<?> objects) {
    throw new RuntimeException();
  }

  public boolean retainAll(final Collection<?> objects) {
    throw new RuntimeException();
  }

  public void clear() {
    throw new RuntimeException();
  }
}