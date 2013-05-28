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
   * All the years containing activities in the list.
   */
  @OneToMany
  public abstract Map<String, ActivityRefYearEntity> getYears();

  @OneToMany
  public abstract List<ActivityRefYearEntity> getYearsList();

  @Create
  public abstract ActivityRefYearEntity newYear();

  public void inc() {
    setNumber(getNumber() + 1);
  }
  
  public void desc() {
    setNumber(getNumber() - 1);
  }

  public ActivityRefYearEntity getYear(String year) {

    ActivityRefYearEntity yearEntity = getYears().get(year);

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
  
  public ActivityRef get(ActivityEntity entity) {
    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
    calendar.setTimeInMillis(entity.getLastUpdated());

    String year = String.valueOf(calendar.get(Calendar.YEAR));
    String month = MONTH_NAME[calendar.get(Calendar.MONTH)];
    String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

    ActivityRefDayEntity dayEntity = this.getYear(year).getMonth(month).getDay(day);
    
    //needs to check it existing or not in list
    ActivityRef ref = dayEntity.getActivityRefs().get(entity.getName());
    
    if (ref == null) {
      ref = dayEntity.createRef();
      dayEntity.getActivityRefs().put(entity.getName(), ref);
      dayEntity.inc();
    }
    
    return ref;
  }
  
  public ActivityRef remove(ActivityEntity entity) {
    Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
    calendar.setTimeInMillis(entity.getLastUpdated());

    String year = String.valueOf(calendar.get(Calendar.YEAR));
    String month = MONTH_NAME[calendar.get(Calendar.MONTH)];
    String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));

    ActivityRefDayEntity dayEntity = this.getYear(year).getMonth(month).getDay(day);
    
    //needs to check it existing or not in list
    ActivityRef ref = dayEntity.getActivityRefs().get(entity.getName());
    
    if (ref != null) {
      dayEntity.getActivityRefs().remove(entity.getName());
      dayEntity.desc();
    }
    
    return ref;
  }

}