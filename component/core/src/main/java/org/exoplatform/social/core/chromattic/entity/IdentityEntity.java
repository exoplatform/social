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
import org.chromattic.api.annotations.Path;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.exoplatform.social.core.storage.query.PropertyLiteralExpression;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@PrimaryType(name = "soc:identitydefinition")
public abstract class IdentityEntity {

  @Id
  public abstract String getId();

  @Path
  public abstract String getPath();

  @Name
  public abstract String getName();
  public abstract String setName(String name);

  /**
   * The provider Id is considered as a namespace for the remote Id.
   */
  @Property(name = "soc:providerId")
  public abstract String getProviderId();
  public abstract void setProviderId(String providerId);

  /**
   * The local Id from a provider Id.
   */
  @Property(name = "soc:remoteId")
  public abstract String getRemoteId();
  public abstract void setRemoteId(String remoteId);
  public static final PropertyLiteralExpression<String> remoteId =
      new PropertyLiteralExpression<String>(String.class, "soc:remoteId");

  /**
   * Show that if the provider Id is deleted or not via the provider.
   */
  @Property(name = "soc:isDeleted")
  public abstract Boolean isDeleted();
  public abstract void setDeleted(Boolean deleted);

  /**
   * Store the detailed information of an identity.
   */
  @MappedBy("soc:profile")
  @OneToOne
  @Owner
  public abstract ProfileEntity getProfile();
  public abstract void setProfile(ProfileEntity profile);

  /**
   * Store all activities in the activity stream of an identity.
   */
  @MappedBy("soc:activities")
  @OneToOne
  @Owner
  public abstract ActivityListEntity getActivityList();
  public abstract void setActivityList(ActivityListEntity activityListEntity);
  
  /**
   * Store all connection activities in the all stream of an identity.
   */
  @MappedBy("soc:connectionstream")
  @OneToOne
  @Owner
  public abstract ActivityRefListEntity getConnectionStream();
  public abstract void setConnectionStream(ActivityRefListEntity activityRefListEntity);
  
  /**
   * Store all space activities in the all stream of an identity.
   */
  @MappedBy("soc:spacestream")
  @OneToOne
  @Owner
  public abstract ActivityRefListEntity getSpaceStream();
  public abstract void setSpaceStream(ActivityRefListEntity activityRefListEntity);
  
  /**
   * Store all the activities in the my stream of an identity.
   */
  @MappedBy("soc:mystream")
  @OneToOne
  @Owner
  public abstract ActivityRefListEntity getMyStream();
  public abstract void setMyStream(ActivityRefListEntity activityRefListEntity);
  

  /**
   * Store all activities in the all stream of an identity.
   */
  @MappedBy("soc:allstream")
  @OneToOne
  @Owner
  public abstract ActivityRefListEntity getAllStream();
  public abstract void setAllStream(ActivityRefListEntity activityRefListEntity);

  /**
   * Store all the relationships which contain an identity inviting other identities to connect with himself.
   */
  @MappedBy("soc:sender")
  @OneToOne
  @Owner
  public abstract RelationshipListEntity getSender();
  public abstract void setSender(RelationshipListEntity sender);

  /**
   * Store all the relationships which contain an identity invited to connect by other identities.
   */
  @MappedBy("soc:receiver")
  @OneToOne
  @Owner
  public abstract RelationshipListEntity getReceiver();
  public abstract void setReceiver(RelationshipListEntity receiver);

  /**
   * Store all the relationships of an identity that is in connection with other identities.
   */
  @MappedBy("soc:relationship")
  @OneToOne
  @Owner
  public abstract RelationshipListEntity getRelationship();

  /**
   * Store all the relationships which contain an identity ignored by other identities.
   */
  @MappedBy("soc:ignored")
  @OneToOne
  @Owner
  public abstract RelationshipListEntity getIgnored();

  /**
   * Store all the relationships which contain an identity ignoring other identities.
   */
  @MappedBy("soc:ignore")
  @OneToOne
  @Owner
  public abstract RelationshipListEntity getIgnore();

  /**
   * Store all spaces of which an identity is a member.
   */
  @OneToOne
  @Owner
  @MappedBy("soc:spacemember")
  public abstract SpaceListEntity getSpaces();
  public abstract void setSpaces(SpaceListEntity spaces);

  /**
   * Store all spaces which an identity is pending for validation to join.
   */
  @OneToOne
  @Owner
  @MappedBy("soc:spacependingmember")
  public abstract SpaceListEntity getPendingSpaces();
  public abstract void setPendingSpaces(SpaceListEntity spaces);

  /**
   * Store all spaces which an identity is invited to join.
   */
  @OneToOne
  @Owner
  @MappedBy("soc:spaceinvitedmember")
  public abstract SpaceListEntity getInvitedSpaces();
  public abstract void setInvitedSpaces(SpaceListEntity spaces);

  /**
   * Store all spaces of which an identity is a manager.
   */
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
