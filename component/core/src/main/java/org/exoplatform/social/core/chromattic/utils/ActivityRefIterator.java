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

import java.util.*;

import org.exoplatform.social.core.chromattic.entity.*;

public class ActivityRefIterator implements Iterator<ActivityRef> {

  private final ActivityRefListEntity listEntity;

  //
  private Iterator<ActivityRefYearEntity> yearIterator;
  private Iterator<ActivityRefMonthEntity> monthIterator;
  private Iterator<ActivityRefDayEntity> dayIterator;
  private Iterator<ActivityRef> entityIterator;

  ActivityRefIterator(final ActivityRefListEntity listEntity) {

    this.listEntity = listEntity;
    this.yearIterator = listEntity.getYears().values().iterator();

    if (yearIterator.hasNext()) {
      this.monthIterator = yearIterator.next().getMonths().values().iterator();
      if (monthIterator.hasNext()) {
        this.dayIterator = monthIterator.next().getDays().values().iterator();
        if (dayIterator.hasNext()) {
          this.entityIterator = orderRefs();
        }
      }
    }

  }

  private Iterator<ActivityRef> orderRefs() {
    List<ActivityRef> got = new ArrayList<ActivityRef>(dayIterator.next().getActivityRefList());
    // We use this local cache to avoid accessing the JCR at each call
    final Map<String, Long> cache = new HashMap<String, Long>();
    Collections.sort(got, new Comparator<ActivityRef>() {
      public int compare(ActivityRef o1, ActivityRef o2) {
        Long co2 = getActivityRefLastUpdated(cache, o2);
        Long co1 = getActivityRefLastUpdated(cache, o1);
        return (int) (co2 - co1);
      }
    });
    return got.iterator();
  }

  /**
   * Gives the value of lastUpdated from the provided cache if it can be found otherwise it will be retrieved
   * from the related ActivityEntity if it exists or directly from the ActivityRef thanks to getLastUpdated()
   * or getName()
   */
  private static Long getActivityRefLastUpdated(Map<String, Long> cache, ActivityRef o) {
    // Due to change using ActivityId as ActivityRef's name instead of Activity's lastUpdated
    Long co = cache.get(o.getId());
    if (co == null) {
      ActivityEntity ae = o.getActivityEntity();
      co = ae == null ? o.getLastUpdated() : ae.getLastUpdated();
      //In some cases, migrated Activity from 3.5.x, ActivityRef's lastUpdated is NULL
      //uses instead of ActivityRef's name.
      if (co == null) {
        try {
          co = Long.parseLong(o.getName());
        } catch (NumberFormatException e) {
          co = System.currentTimeMillis();
        }
      }
      cache.put(o.getId(), co);
    }
    return co;
  }

  public boolean hasNext() {

    boolean nothing = true;

    if (entityIterator != null && entityIterator.hasNext()) {
      return true;
    }
    else if (dayIterator != null && dayIterator.hasNext()) {
      entityIterator = orderRefs();
      nothing = false;
      if (entityIterator.hasNext()) {
        return true;
      }
    }
    else if (monthIterator != null && monthIterator.hasNext()) {
      dayIterator = monthIterator.next().getDays().values().iterator();
      nothing = false;
      if (dayIterator.hasNext()) {
        entityIterator = orderRefs();
        if (entityIterator.hasNext()) {
          return true;
        }
      }
    }
    else if (yearIterator != null && yearIterator.hasNext()) {
      monthIterator = yearIterator.next().getMonths().values().iterator();
      nothing = false;
      if (monthIterator.hasNext()) {
        dayIterator = monthIterator.next().getDays().values().iterator();
        if (dayIterator.hasNext()) {
          entityIterator = orderRefs();
          if (entityIterator.hasNext()) {
            return true;
          }
        }
      }
    }

    if (nothing) {
      return false;
    }
    else {
      return hasNext();
    }
  }

  public int moveTo(ActivityRef activityRef) {

    ActivityRefDayEntity day = activityRef.getDay();
    ActivityRefMonthEntity month = day.getMonth();
    ActivityRefYearEntity year = month.getYear();

    int nb = 0;
    nb += moveIterator(yearIterator, year.getName());
    nb += moveIterator(monthIterator, month.getName());
    nb += moveIterator(dayIterator, day.getName());
    nb += moveIterator(entityIterator, activityRef.getName());

    return nb;

  }

  public ActivityRef next() {

    if (hasNext()) {
      return entityIterator.next();
    }
    else {
      throw new RuntimeException();
    }

  }

  public void remove() {
    throw new RuntimeException();
  }

  private int moveIterator(Iterator<? extends NamedEntity> it, String name) {

    int nb = 0;
    while (it.hasNext()) {
      NamedEntity got = it.next();
      if (got.getName().equals(name)) {
        return nb;
      }
      else {
        if (got instanceof IndexNumber) {
          nb += ((IndexNumber) got).getNumber();
        }
        else {
          ++nb;
        }
      }
    }

    return nb;
  }
}
