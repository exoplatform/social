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

import org.chromattic.api.RelationshipType;
import org.chromattic.api.annotations.Id;
import org.chromattic.api.annotations.ManyToOne;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.Owner;
import org.chromattic.api.annotations.Path;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@PrimaryType(name = "soc:relationshipdefinition")
public abstract class RelationshipEntity {

  @Id
  public abstract String getId();

  @Name
  public abstract String getName();

  @Path
  public abstract String getPath();
  
  @Property(name = "soc:status")
  public abstract String getStatus();
  public abstract void setStatus(String status);

  @MappedBy("soc:from")
  @ManyToOne(type = RelationshipType.REFERENCE)
  @Owner
  public abstract IdentityEntity getFrom();
  public abstract void setFrom(IdentityEntity fromEntity);

  @MappedBy("soc:to")
  @ManyToOne(type = RelationshipType.REFERENCE)
  @Owner
  public abstract IdentityEntity getTo();
  public abstract void setTo(IdentityEntity toEntity);

  @MappedBy("soc:reciprocal")
  @ManyToOne(type = RelationshipType.REFERENCE)
  @Owner
  public abstract RelationshipEntity getReciprocal();
  public abstract void setReciprocal(RelationshipEntity reciprocal);

  @ManyToOne
  public abstract RelationshipListEntity getParent();

  public boolean isReceiver() {
    return "receiver".equals(getParent().getName());
  }
  
  public boolean isSender() {
    return "sender".equals(getParent().getName());
  }
}