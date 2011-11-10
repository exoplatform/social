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

package org.exoplatform.social.core.chromattic.utils;

import org.exoplatform.social.core.chromattic.entity.ActivityDayEntity;
import org.exoplatform.social.core.chromattic.entity.ActivityEntity;
import org.exoplatform.social.core.chromattic.entity.ActivityListEntity;
import org.exoplatform.social.core.chromattic.entity.ActivityMonthEntity;
import org.exoplatform.social.core.chromattic.entity.ActivityYearEntity;
import org.exoplatform.social.core.chromattic.entity.IndexNumber;
import org.exoplatform.social.core.chromattic.entity.NamedEntity;

import java.util.Iterator;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class ActivityIterator implements Iterator<ActivityEntity> {

  private final ActivityListEntity listEntity;

  //
  private Iterator<ActivityYearEntity> yearIterator;
  private Iterator<ActivityMonthEntity> monthIterator;
  private Iterator<ActivityDayEntity> dayIterator;
  private Iterator<ActivityEntity> entityIterator;

  ActivityIterator(final ActivityListEntity listEntity) {

    this.listEntity = listEntity;
    this.yearIterator = listEntity.getYears().values().iterator();

    if (yearIterator.hasNext()) {
      this.monthIterator = yearIterator.next().getMonths().values().iterator();
      if (monthIterator.hasNext()) {
        this.dayIterator = monthIterator.next().getDays().values().iterator();
        if (dayIterator.hasNext()) {
          this.entityIterator = dayIterator.next().getActivities().iterator();
        }
      }
    }

  }

  public boolean hasNext() {

    if (entityIterator != null && entityIterator.hasNext()) {
      return true;
    }
    else if (dayIterator != null && dayIterator.hasNext()) {
      return true;
    }
    else if (monthIterator != null && monthIterator.hasNext()) {
      return true;
    }
    else if (yearIterator.hasNext()) {
      return true;
    }

    return false;
  }

  public int moveTo(ActivityEntity activity) {

    ActivityDayEntity day = activity.getDay();
    ActivityMonthEntity month = day.getMonth();
    ActivityYearEntity year = month.getYear();

    int nb = 0;
    nb += moveIterator(yearIterator, year.getName());
    nb += moveIterator(monthIterator, month.getName());
    nb += moveIterator(dayIterator, day.getName());
    nb += moveIterator(entityIterator, activity.getName());

    return nb;

  }

  public ActivityEntity next() {

    if (entityIterator.hasNext()) {
      return entityIterator.next();
    }
    else if (dayIterator.hasNext()) {
      entityIterator = dayIterator.next().getActivities().iterator();
      if (entityIterator.hasNext()) {
        return entityIterator.next();
      }
    }
    else if (monthIterator.hasNext()) {
      dayIterator = monthIterator.next().getDays().values().iterator();
      if (dayIterator.hasNext()) {
        entityIterator = dayIterator.next().getActivities().iterator();
        if (entityIterator.hasNext()) {
          return entityIterator.next();
        }
      }
    }
    else if (yearIterator.hasNext()) {
      monthIterator = yearIterator.next().getMonths().values().iterator();
      if (monthIterator.hasNext()) {
        dayIterator = monthIterator.next().getDays().values().iterator();
        if (dayIterator.hasNext()) {
          entityIterator = dayIterator.next().getActivities().iterator();
          if (entityIterator.hasNext()) {
            return entityIterator.next();
          }
        }
      }
    }

    return next();
    
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