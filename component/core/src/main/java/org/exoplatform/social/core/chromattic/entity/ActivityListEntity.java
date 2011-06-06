/*
* Copyright (C) 2003-2009 eXo Platform SAS.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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

import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@PrimaryType(name = "soc:activitylist")
@FormattedBy(BaseEncodingObjectFormatter.class)
@NamingPrefix("soc")
public abstract class ActivityListEntity {

  @Path
  public abstract String getPath();

  @Property(name = "soc:number")
  @DefaultValue({"0"})
  public abstract Integer getNumber();
  public abstract void setNumber(Integer number);
  
  @OneToMany
  public abstract Map<String, ActivityYearEntity> getYears();

  @Create
  public abstract ActivityYearEntity newYear();

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
    }

    return yearEntity;

  }
}
