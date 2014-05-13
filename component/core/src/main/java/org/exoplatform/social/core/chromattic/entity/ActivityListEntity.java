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
import org.chromattic.api.annotations.NamingPrefix;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.Path;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.chromattic.ext.format.BaseEncodingObjectFormatter;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@PrimaryType(name = "soc:activitylist", orderable = true)
@FormattedBy(BaseEncodingObjectFormatter.class)
@NamingPrefix("soc")
public abstract class ActivityListEntity {

  @Path
  public abstract String getPath();

  /**
   * The number of activities in the activities list. The default value is set to 0.
   */
  @Property(name = "soc:number")
  @DefaultValue({"0"})
  public abstract Integer getNumber();
  public abstract void setNumber(Integer number);

  /**
   * All the years containing activities in the list.
   */
  @OneToMany
  public abstract Map<String, ActivityYearEntity> getYears();

  @OneToMany
  public abstract List<ActivityYearEntity> getYearsList();

  @Create
  public abstract ActivityYearEntity newYear();

  @Create
  public abstract ActivityEntity createActivity(String name);

  public void inc() {
    setNumber(getNumber() + 1);
  }
  
  public void desc() {
    setNumber(getNumber() - 1);
  }

  public ActivityYearEntity getYear(String year) {

    ActivityYearEntity yearEntity = getYears().get(year);

    if (yearEntity == null) {
      yearEntity = newYear();
      getYears().put(year, yearEntity);
      getYearsList().add(yearEntity);
    }

    return yearEntity;

  }
}
