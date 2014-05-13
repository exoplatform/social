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
package org.exoplatform.social.core.chromattic.entity;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.DefaultValue;
import org.chromattic.api.annotations.FormattedBy;
import org.chromattic.api.annotations.NamingPrefix;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.Path;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.chromattic.ext.format.BaseEncodingObjectFormatter;

@PrimaryType(name = "soc:activityreflist")
@FormattedBy(BaseEncodingObjectFormatter.class)
@NamingPrefix("soc")
public abstract class ActivityRefListEntity {
  
  protected String[] MONTH_NAME = new DateFormatSymbols(Locale.ENGLISH).getMonths();

  @Path
  public abstract String getPath();

  /**
   * The number of activity references in the activities list. The default value is set to 0.
   */
  @Property(name = "soc:number")
  @DefaultValue({"0"})
  public abstract Integer getNumber();
  public abstract void setNumber(Integer number);
  
  /**
   * The last migration is last updated time of migrated activity. 
   * The default value is set to 0.
   */
  @Property(name = "soc:lastMigration")
  @DefaultValue({"0"})
  public abstract Long getLastMigration();
  public abstract void setLastMigration(Long number);

  /**
   * All the years containing activities in the list.
   */
  @OneToMany
  public abstract Map<String, ActivityRefYearEntity> getYears();

  @OneToMany
  public abstract List<ActivityRefYearEntity> getYearsList();

  @Create
  public abstract ActivityRefYearEntity newYear();
  
  @Create
  public abstract ActivityRefYearEntity newYear(String name);
  
  public void inc() {
    setNumber(getNumber() + 1);
  }
  
  public void desc() {
    setNumber(getNumber() - 1);
  }

  public ActivityRefYearEntity getYear(String year) {
    ActivityRefYearEntity yearEntity = null;
    if (getYears() == null) {
      yearEntity = newYear(year);
    } else {
      yearEntity = getYears().get(year);
    }

    if (yearEntity == null) {
      yearEntity = newYear();
      getYears().put(year, yearEntity);
      long longYear = Long.parseLong(year);
      for (int i = getYearsList().size() - 1; i >= 0 ; --i) {
        long longCurrent = Long.parseLong(getYearsList().get(i).getName());
        if (longCurrent < longYear) {
          getYearsList().add(i, yearEntity);
        }
      }
    }

    return yearEntity;

  }
  
  public ActivityRefYearEntity getYear(String year, AtomicBoolean newYearMonthDay) {
    ActivityRefYearEntity yearEntity = null;
    if (getYears() == null) {
      yearEntity = newYear(year);
    } else {
      yearEntity = getYears().get(year);
    }

    if (yearEntity == null) {
      yearEntity = newYear();
      getYears().put(year, yearEntity);
      long longYear = Long.parseLong(year);
      for (int i = getYearsList().size() - 1; i >= 0 ; --i) {
        long longCurrent = Long.parseLong(getYearsList().get(i).getName());
        if (longCurrent < longYear) {
          getYearsList().add(i, yearEntity);
        }
      }
      
      newYearMonthDay.set(true);
    }

    return yearEntity;

  }
  
  public ActivityRef getOrCreated(ActivityEntity entity, boolean isHidden) {
    Long lastUpdated = entity.getLastUpdated() != null ? entity.getLastUpdated() : entity.getPostedTime();
    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
    calendar.setTimeInMillis(lastUpdated);

    String year = String.valueOf(calendar.get(Calendar.YEAR));
    String month = MONTH_NAME[calendar.get(Calendar.MONTH)];
    String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

    ActivityRefDayEntity dayEntity = this.getYear(year).getMonth(month).getDay(day);
    
    //needs to check it existing or not in list
    ActivityRef ref = dayEntity.getActivityRefs().get(entity.getId());
    
    if (ref == null) {
      ref = dayEntity.createRef();
      ref.setName(entity.getId());
      dayEntity.getActivityRefList().add(ref);
      ref.setLastUpdated(lastUpdated);
      ref.setActivityEntity(entity);
      if (!isHidden) {
        dayEntity.inc();
      }
      
    }
    
    return ref;
  }
  
  public ActivityRef get(ActivityEntity entity, Long oldLastUpdated) {
    Long lastUpdated = oldLastUpdated;
    //In some cases, migrated Activity from 3.5.x, ActivityRef's lastUpdated is NULL
    //uses instead of Activity's postedTime.
    if (oldLastUpdated == null) {
      lastUpdated = entity.getLastUpdated() != null ? entity.getLastUpdated() : entity.getPostedTime();
    }
    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
    calendar.setTimeInMillis(lastUpdated);

    String year = String.valueOf(calendar.get(Calendar.YEAR));
    String month = MONTH_NAME[calendar.get(Calendar.MONTH)];
    String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

    ActivityRefDayEntity dayEntity = this.getYear(year).getMonth(month).getDay(day);
    
    //needs to check it existing or not in list
    ActivityRef ref = dayEntity.getActivityRefs().get(entity.getId());
    if (ref == null) {
      ref = dayEntity.getActivityRefs().get("" + lastUpdated);
    }
    
    return ref;
  }
  
  public ActivityRefDayEntity getActivityRefDay(long lastUpdated) {
    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
    calendar.setTimeInMillis(lastUpdated);

    String year = String.valueOf(calendar.get(Calendar.YEAR));
    String month = MONTH_NAME[calendar.get(Calendar.MONTH)];
    String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

    return this.getYear(year).getMonth(month).getDay(day);
  }
  
 
  /**
   * Needs to update the ActivityRef to top of stream (what's hot functionally)
   * when adds new comment to referenced Activity. 
   * 1. Remove old one 
   * 2. Creates
   * new one with new position in stream.
   * 
   * @param entity
   * @param old
   * @param oldLastUpdated
   */
  public void update(ActivityEntity entity, ActivityRef old, long oldLastUpdated, boolean isHidden) {
    Calendar oldCalendar = Calendar.getInstance(Locale.ENGLISH);
    oldCalendar.setTimeInMillis(oldLastUpdated);

    String oldYear = String.valueOf(oldCalendar.get(Calendar.YEAR));
    String oldMonth = MONTH_NAME[oldCalendar.get(Calendar.MONTH)];
    String oldDay = String.valueOf(oldCalendar.get(Calendar.DAY_OF_MONTH));
    
    Long lastUpdated = entity.getLastUpdated() != null ? entity.getLastUpdated() : entity.getPostedTime();
    
    Calendar newCalendar = Calendar.getInstance(Locale.ENGLISH);
    newCalendar.setTimeInMillis(lastUpdated);

    String newYear = String.valueOf(newCalendar.get(Calendar.YEAR));
    String newMonth = MONTH_NAME[newCalendar.get(Calendar.MONTH)];
    String newDay = String.valueOf(newCalendar.get(Calendar.DAY_OF_MONTH));
    
    ActivityRefDayEntity oldDayEntity = this.getYear(oldYear).getMonth(oldMonth).getDay(oldDay);
    ActivityRef oldRef = oldDayEntity.getActivityRefs().get(entity.getId());
    //needs to check it existing or not in list
    if (oldRef == null) {
      oldRef = oldDayEntity.getActivityRefs().get(old.getName());
    }
    
    if (oldRef != null) {
      //In the case oldLastUpdated and newLastUpdated are different 
      //that mean: 
      // IF the new ActivityRef and old one don't have the same parent node then:
      // 1. Removes old one
      // 2 Creates new one.
      // ELSE (the same parent node)
      // just updated lastUpdated
      if (!oldYear.equalsIgnoreCase(newYear) || !oldMonth.equalsIgnoreCase(newMonth) || !oldDay.equalsIgnoreCase(newDay)) {
        oldDayEntity.getActivityRefs().remove(oldRef.getName());
        if (!isHidden) {
          oldDayEntity.desc();
        }
        getOrCreated(entity, isHidden);
      } else {
        oldRef.setLastUpdated(lastUpdated);
      }
    }
    
  }
  
  public Map<String, ActivityRef> refs(long lastUpdated) {
    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
    calendar.setTimeInMillis(lastUpdated);

    String year = String.valueOf(calendar.get(Calendar.YEAR));
    String month = MONTH_NAME[calendar.get(Calendar.MONTH)];
    String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

    ActivityRefDayEntity dayEntity = this.getYear(year).getMonth(month).getDay(day);
    
    return dayEntity.getActivityRefs();
  }
  
  public boolean isOnlyUpdate(long oldLastUpdated, long newLastUpdated) {
    Calendar oldCalendar = Calendar.getInstance(Locale.ENGLISH);
    oldCalendar.setTimeInMillis(oldLastUpdated);
    
    Calendar newCalendar = Calendar.getInstance(Locale.ENGLISH);
    newCalendar.setTimeInMillis(newLastUpdated);

    String oldYear = String.valueOf(oldCalendar.get(Calendar.YEAR));
    String oldMonth = MONTH_NAME[oldCalendar.get(Calendar.MONTH)];
    String oldDay = String.valueOf(oldCalendar.get(Calendar.DAY_OF_MONTH));
    
    String newYear = String.valueOf(newCalendar.get(Calendar.YEAR));
    String newMonth = MONTH_NAME[newCalendar.get(Calendar.MONTH)];
    String newDay = String.valueOf(newCalendar.get(Calendar.DAY_OF_MONTH));
    
    boolean isOnlyUpdate = oldYear.equals(newYear) && oldMonth.equals(newMonth) && oldDay.equals(newDay);
    return isOnlyUpdate;
  }
  
  public boolean isOnlyUpdate(ActivityRef oldRef, long newLastUpdated) {
    ActivityRefDayEntity day = oldRef.getDay();
    String oldYear = day.getMonth().getYear().getName();
    String oldMonth = day.getMonth().getName();
    String oldDay = day.getName();
    
    Calendar newCalendar = Calendar.getInstance(Locale.ENGLISH);
    newCalendar.setTimeInMillis(newLastUpdated);
    String newYear = String.valueOf(newCalendar.get(Calendar.YEAR));
    String newMonth = MONTH_NAME[newCalendar.get(Calendar.MONTH)];
    String newDay = String.valueOf(newCalendar.get(Calendar.DAY_OF_MONTH));
    
    boolean isOnlyUpdate = oldYear.equals(newYear) && oldMonth.equals(newMonth) && oldDay.equals(newDay);
    return isOnlyUpdate;
  }

  
  public ActivityRef remove(ActivityEntity entity, boolean isHidden, Long oldLastUpdated) {
    Long lastUpdated = oldLastUpdated;
    //In some cases, migrated Activity from 3.5.x, ActivityRef's lastUpdated is NULL
    //uses instead of Activity's postedTime.
    if (oldLastUpdated == null) {
      lastUpdated = entity.getLastUpdated() != null ? entity.getLastUpdated() : entity.getPostedTime();
    }
    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
    calendar.setTimeInMillis(lastUpdated);

    String year = String.valueOf(calendar.get(Calendar.YEAR));
    String month = MONTH_NAME[calendar.get(Calendar.MONTH)];
    String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

    ActivityRefDayEntity dayEntity = this.getYear(year).getMonth(month).getDay(day);
    
    //needs to check it existing or not in list
    ActivityRef ref = dayEntity.getActivityRefs().remove(entity.getId());
    
    //remove by lastUpdated
    if (ref == null) {
      ref = dayEntity.getActivityRefs().remove("" + lastUpdated);
    }
    
    if (ref != null) {
      if (!isHidden) {
        dayEntity.desc();
      }
      
      ref = null;
    }
    
    return ref;
  }
  
}