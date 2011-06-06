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

import org.chromattic.api.annotations.Id;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@PrimaryType(name = "soc:profilexp")
public abstract class ProfileXpEntity {

  @Id
  public abstract String getId();

  @Property(name = "soc:skills")
  public abstract String getSkills();
  public abstract void setSkills(String skills);

  @Property(name = "soc:position")
  public abstract String getPosition();
  public abstract void setPosition(String position);

  @Property(name = "soc:startDate")
  public abstract String getStartDate();
  public abstract void setStartDate(String startDate);

  @Property(name = "soc:endDate")
  public abstract String getEndDate();
  public abstract void setEndDate(String endDate);

  @Property(name = "soc:description")
  public abstract String getDescription();
  public abstract void setDescription(String description);

  @Property(name = "soc:company")
  public abstract String getCompany();
  public abstract void setCompany(String company);

  public boolean isCurrent() {
    return getEndDate() == null;
  }
}
