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

import org.chromattic.api.RelationshipType;
import org.chromattic.api.annotations.Id;
import org.chromattic.api.annotations.ManyToOne;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.Owner;
import org.chromattic.api.annotations.Path;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.exoplatform.social.core.storage.query.PropertyLiteralExpression;

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

  /**
   * The status of the relationship, including three values: PENDING, CONFIRMED, and IGNORED.
   */
  @Property(name = "soc:status")
  public abstract String getStatus();
  public abstract void setStatus(String status);

  /**
   * The receiver identity. It refers to the soc:identity node type.
   */
  @MappedBy("soc:from")
  @ManyToOne(type = RelationshipType.REFERENCE)
  @Owner
  public abstract IdentityEntity getFrom();
  public abstract void setFrom(IdentityEntity fromEntity);

  /**
   * The sender identity. It refers to the soc:identity node type.
   */
  @MappedBy("soc:to")
  @ManyToOne(type = RelationshipType.REFERENCE)
  @Owner
  public abstract IdentityEntity getTo();
  public abstract void setTo(IdentityEntity toEntity);

  /**
   * Denote if the relationship is one way or two ways. It refers to the _soc\:relationshipdefinition_ node type.
   */
  @MappedBy("soc:reciprocal")
  @ManyToOne(type = RelationshipType.REFERENCE)
  @Owner
  public abstract RelationshipEntity getReciprocal();
  public abstract void setReciprocal(RelationshipEntity reciprocal);

  /**
   * The time when the relationship is created.
   */
  @Property(name = "soc:createdTime")
  public abstract Long getCreatedTime();
  public abstract void setCreatedTime(Long createdTime);
  public static final PropertyLiteralExpression<Long> createdTime =
      new PropertyLiteralExpression<Long>(Long.class, "soc:createdTime");

  @ManyToOne
  public abstract RelationshipListEntity getParent();

  public boolean isReceiver() {
    return "receiver".equals(getParent().getName());
  }
  
  public boolean isSender() {
    return "sender".equals(getParent().getName());
  }
}