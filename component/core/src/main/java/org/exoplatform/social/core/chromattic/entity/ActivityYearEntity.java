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

package org.exoplatform.social.core.chromattic.entity;

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.DefaultValue;
import org.chromattic.api.annotations.FormattedBy;
import org.chromattic.api.annotations.Id;
import org.chromattic.api.annotations.ManyToOne;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.NamingPrefix;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.Path;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.chromattic.ext.format.BaseEncodingObjectFormatter;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@PrimaryType(name = "soc:activityyear", orderable = true)
@FormattedBy(BaseEncodingObjectFormatter.class)
@NamingPrefix("soc")
public abstract class ActivityYearEntity implements NamedEntity, IndexNumber {

  private List<String> MONTH_NAME = Arrays.asList(new DateFormatSymbols(Locale.ENGLISH).getMonths());

  @Path
  public abstract String getPath();

  @Name
  public abstract String getName();

  @Id
  public abstract String getId();

  /**
   * The number of activities in the year. The default value is set to 0.
   */
  @Property(name = "soc:number")
  @DefaultValue({"0"})
  public abstract Integer getNumber();
  public abstract void setNumber(Integer number);

  /**
   * All the months containing activities in the year.
   */
  @OneToMany
  public abstract Map<String, ActivityMonthEntity> getMonths();

  @OneToMany
  public abstract List<ActivityMonthEntity> getMonthsList();

  @ManyToOne
  public abstract ActivityListEntity getList();
  
  @Create
  public abstract ActivityMonthEntity newMonth();

  public void inc() {
    getList().inc();
    setNumber(getNumber() + 1);
  }

  public void desc() {
    getList().desc();
    setNumber(getNumber() - 1);
  }

  public ActivityMonthEntity getMonth(String month) {

    ActivityMonthEntity monthEntity = getMonths().get(month);

    if (monthEntity == null) {
      monthEntity = newMonth();
      getMonths().put(month, monthEntity);
      getMonthsList().add(monthEntity);
    }

    return monthEntity;

  }

}
