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

import org.chromattic.api.RelationshipType;
import org.chromattic.api.annotations.Id;
import org.chromattic.api.annotations.ManyToOne;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.Owner;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;


@PrimaryType(name = "soc:activityref")
public abstract class ActivityRef {

  @Name
  public abstract String getName();
  public abstract void setName(String name);
  
  @Id
  public abstract String getId();
  
  @Property(name = "soc:lastUpdated")
  public abstract Long getLastUpdated();
  public abstract void setLastUpdated(Long lastUpdated);

  /**
   * Refer to a space entity.
   */
  @Owner
  @MappedBy("soc:target")
  @ManyToOne(type = RelationshipType.REFERENCE)
  public abstract ActivityEntity getActivityRef();
  public abstract void setActivityRef(ActivityEntity activityRef);
}