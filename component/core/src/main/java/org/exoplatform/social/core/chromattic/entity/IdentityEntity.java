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

import java.util.Map;

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.Id;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Owner;
import org.chromattic.api.annotations.Path;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Properties;
import org.chromattic.api.annotations.Property;
import org.exoplatform.social.core.storage.query.PropertyLiteralExpression;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@PrimaryType(name = "soc:identitydefinition")
public abstract class IdentityEntity {
  public final static String RELATIONSHIP_NUMBER_PARAM = "relationshipNo";
  public final static String LATEST_ACTIIVTY_CREATED_TIME_PARAM = "latestActivityCreatedTime";
  public final static String LATEST_LAZY_CREATED_TIME_PARAM = "latestLazyCreatedTime";

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
   * Store activities's streams
   */
  @MappedBy("soc:streams")
  @OneToOne
  @Owner
  public abstract StreamsEntity getStreams();
  public abstract void setStreams(StreamsEntity streams);

  /**
   * Store all activities in the activity stream of an identity.
   */
  @MappedBy("soc:activities")
  @OneToOne
  @Owner
  public abstract ActivityListEntity getActivityList();
  public abstract void setActivityList(ActivityListEntity activityListEntity);
  
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
  public static final PropertyLiteralExpression<String> spacemember =
      new PropertyLiteralExpression<String>(String.class, "soc:spacemember");

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
  
  @Create
  public abstract StreamsEntity createStreams();
  
  @Properties
  public abstract Map<String, String> getProperties();
  
  /**
   * Gets the time what latest activity created time
   * if the properties is not existing, return 0
   * otherwise return latest time.
   * 
   * @return long value
   */
  public long getLatestActivityCreatedTime() {
    if (hasProperty(LATEST_ACTIIVTY_CREATED_TIME_PARAM)) {
      String value = getProperty(LATEST_ACTIIVTY_CREATED_TIME_PARAM);
      try {
        return Long.parseLong(value);
      } catch (NumberFormatException e) {
        return 0;
      }
    }
    return 0;
  }
  
  /**
   * Sets the latest activity created time.
   * 1. it is set value in the case user loads Feed or Connections stream and is de-active state.
   * 2. it is set value in the case the connections's post new activity, the latest post time will be set.
   * 3. the case delete the activity, don't need update anything.
   * 
   * @param time the created time of the latest activity
   */
  public void setLatestActivityCreatedTime(long time) {
    setProperty(LATEST_ACTIIVTY_CREATED_TIME_PARAM, String.valueOf(time));
  }
  
  /**
   * Gets the latest lazy created time.
   * 
   * @return
   */
  public long getLatestLazyCreatedTime() {
    if (hasProperty(LATEST_LAZY_CREATED_TIME_PARAM)) {
      String value = getProperty(LATEST_LAZY_CREATED_TIME_PARAM);
      try {
        return Long.parseLong(value);
      } catch (NumberFormatException e) {
        return 0;
      }
    }
    return 0;
  }
  
  /**
   * Sets the latest lazy created time to execute the lazy creating activity ref
   * - it is set value in the case user is de-active, and login first time
   * 
   * @param time the created time of the latest activity
   */
  public void setLatestLazyCreatedTime(long time) {
    setProperty(LATEST_LAZY_CREATED_TIME_PARAM, String.valueOf(time));
  }
  
  public String getProperty(String key) {
    return getProperties().get(key);
  }
  
  public boolean hasProperty(String key) {
    return getProperties().containsKey(key);
  }

  public void setProperty(String key, String value) {
    getProperties().put(key, value);
  }
  
}
