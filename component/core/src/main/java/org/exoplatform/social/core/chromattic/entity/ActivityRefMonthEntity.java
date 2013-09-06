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

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.DefaultValue;
import org.chromattic.api.annotations.FormattedBy;
import org.chromattic.api.annotations.ManyToOne;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.NamingPrefix;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.Path;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.chromattic.ext.format.BaseEncodingObjectFormatter;

@PrimaryType(name = "soc:activityrefmonth", orderable = true)
@FormattedBy(BaseEncodingObjectFormatter.class)
@NamingPrefix("soc")
public abstract class ActivityRefMonthEntity implements NamedEntity, IndexNumber {

  @Path
  public abstract String getPath();

  @Name
  public abstract String getName();

  /**
   * The number of activities in the month. The default value is set to 0.
   */
  @Property(name = "soc:number")
  @DefaultValue({"0"})
  public abstract Integer getNumber();
  public abstract void setNumber(Integer number);

  /**
   * All the days containing activities in the month.
   */
  @OneToMany
  public abstract Map<String, ActivityRefDayEntity> getDays();

  @OneToMany
  public abstract List<ActivityRefDayEntity> getDaysList();

  @ManyToOne
  public abstract ActivityRefYearEntity getYear();
  
  @Create
  public abstract ActivityRefDayEntity newDay();
  
  @Create
  public abstract ActivityRefDayEntity newDay(String day);

  public void inc() {
    getYear().inc();
    setNumber(getNumber() + 1);
  }

  public void desc() {
    getYear().desc();
    setNumber(getNumber() - 1);
  }
  
  public ActivityRefDayEntity getDay(String day) {

    ActivityRefDayEntity dayEntity = getDays().get(day);

    if (dayEntity == null) {
      dayEntity = newDay();
      getDays().put(day, dayEntity);
      long longDay = Long.parseLong(day);
      for (int i = getDaysList().size() - 1; i >= 0 ; --i) {
        long longCurrent = Long.parseLong(getDaysList().get(i).getName());
        if (longCurrent < longDay) {
          getDaysList().add(i, dayEntity);
        }
      }
    }

    return dayEntity;

  }
  
  public ActivityRefDayEntity getDay(String day, AtomicBoolean newYearMonthday) {

    ActivityRefDayEntity dayEntity = getDays().get(day);

    if (dayEntity == null) {
      dayEntity = newDay();
      getDays().put(day, dayEntity);
      long longDay = Long.parseLong(day);
      for (int i = getDaysList().size() - 1; i >= 0 ; --i) {
        long longCurrent = Long.parseLong(getDaysList().get(i).getName());
        if (longCurrent < longDay) {
          getDaysList().add(i, dayEntity);
        }
      }
      
      newYearMonthday.set(true);
    }

    return dayEntity;

  }
}