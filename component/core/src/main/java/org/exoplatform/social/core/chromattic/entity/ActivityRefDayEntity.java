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

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.DefaultValue;
import org.chromattic.api.annotations.FormattedBy;
import org.chromattic.api.annotations.ManyToOne;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.NamingPrefix;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.Owner;
import org.chromattic.api.annotations.Path;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.chromattic.ext.format.BaseEncodingObjectFormatter;

@PrimaryType(name = "soc:activityrefday", orderable = true)
@FormattedBy(BaseEncodingObjectFormatter.class)
@NamingPrefix("soc")
public abstract class ActivityRefDayEntity implements NamedEntity, IndexNumber {

  @Path
  public abstract String getPath();

  @Name
  public abstract String getName();

  /**
   * The number of activities in the day. The default value is set to 0.
   */
  @Property(name = "soc:number")
  @DefaultValue({"0"})
  public abstract Integer getNumber();
  public abstract void setNumber(Integer number);

 
  @OneToMany
  @Owner
  @MappedBy("soc:refs")
  public abstract Map<String, ActivityRef> getActivityRefs();
  
  @OneToMany
  @Owner
  @MappedBy("soc:refs")
  public abstract List<ActivityRef> getActivityRefList();
  public abstract void setActivityRefList(List<ActivityRef> refList);
  
  @Create
  public abstract ActivityRef createRef();
  
  @Create
  public abstract ActivityRef createRef(String name);


  @ManyToOne
  public abstract ActivityRefMonthEntity getMonth();

  public void inc() {
    getMonth().inc();
    setNumber(getNumber() + 1);
  }

  public void desc() {
    getMonth().desc();
    setNumber(getNumber() - 1);
  }
}