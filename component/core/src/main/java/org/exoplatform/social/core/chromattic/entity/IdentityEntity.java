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
import org.chromattic.api.annotations.Id;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Owner;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@PrimaryType(name = "soc:identitydefinition")
public abstract class IdentityEntity {

  @Id
  public abstract String getId();

  @Name
  public abstract String getName();
  public abstract String setName(String name);

  @Property(name = "soc:providerId")
  public abstract String getProviderId();
  public abstract void setProviderId(String providerId);

  @Property(name = "soc:remoteId")
  public abstract String getRemoteId();
  public abstract void setRemoteId(String remoteId);

  @Property(name = "soc:isDeleted")
  public abstract Boolean isDeleted();
  public abstract void setDeleted(Boolean deleted);


  @MappedBy("soc:profile")
  @OneToOne
  @Owner
  public abstract ProfileEntity getProfile();
  public abstract void setProfile(ProfileEntity profile);

  @MappedBy("soc:activities")
  @OneToOne
  @Owner
  public abstract ActivityListEntity getActivityList();
  public abstract void setActivityList(ActivityListEntity activityListEntity);

  @MappedBy("soc:sender")
  @OneToOne
  @Owner
  public abstract RelationshipListEntity getSender();
  public abstract void setSender(RelationshipListEntity sender);

  @MappedBy("soc:receiver")
  @OneToOne
  @Owner
  public abstract RelationshipListEntity getReceiver();
  public abstract void setReceiver(RelationshipListEntity receiver);

  @MappedBy("soc:relationship")
  @OneToOne
  @Owner
  public abstract RelationshipListEntity getRelationship();

  @OneToOne
  @Owner
  @MappedBy("soc:spacemember")
  public abstract SpaceListEntity getSpaces();
  public abstract void setSpaces(SpaceListEntity spaces);

  @OneToOne
  @Owner
  @MappedBy("soc:spacependingmember")
  public abstract SpaceListEntity getPendingSpaces();
  public abstract void setPendingSpaces(SpaceListEntity spaces);

  @OneToOne
  @Owner
  @MappedBy("soc:spaceinvitedmember")
  public abstract SpaceListEntity getInvitedSpaces();
  public abstract void setInvitedSpaces(SpaceListEntity spaces);

  @OneToOne
  @Owner
  @MappedBy("soc:spacemanagermember")
  public abstract SpaceListEntity getManagerSpaces();
  public abstract void setManagerSpaces(SpaceListEntity spaces);

  @Create
  public abstract ProfileEntity createProfile();

  @Create
  public abstract RelationshipEntity createRelationship();
}
