/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.notification.context;

import org.exoplatform.commons.utils.PropertyChangeSupport;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.RelationshipEvent;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.model.Space;

public class NotificationContext {
  private ExoSocialActivity      activity;

  private Profile                profile;

  private Space                  space;

  private Relationship           relationship;

  private RelationshipManager    relationshipManager;

  private String                 activityId;

  private String                 spaceId;

  private String                 remoteId;

  private RelationshipEvent.Type type;
  
  
  private PropertyChangeSupport pcs;
  
  public PropertyChangeSupport getPcs() {
    return pcs;
  }

  public void setPcs(PropertyChangeSupport pcs) {
    this.pcs = pcs;
  }

  
  public ExoSocialActivity getActivity() {
    return activity;
  }

  public void setActivity(ExoSocialActivity activity) {
    this.activity = activity;
  }

  public Profile getProfile() {
    return profile;
  }

  public void setProfile(Profile profile) {
    this.profile = profile;
  }

  public Relationship getRelationship() {
    return relationship;
  }

  public void setRelationship(Relationship relationship) {
    this.relationship = relationship;
  }

  public Space getSpace() {
    return space;
  }

  public void setSpace(Space space) {
    this.space = space;
  }

  public RelationshipManager getRelationshipManager() {
    return relationshipManager;
  }

  public void setRelationshipManager(RelationshipManager relationshipManager) {
    this.relationshipManager = relationshipManager;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public String getSpaceId() {
    return spaceId;
  }

  public void setSpaceId(String spaceId) {
    this.spaceId = spaceId;
  }

  public String getRemoteId() {
    return remoteId;
  }

  public void setRemoteId(String remoteId) {
    this.remoteId = remoteId;
  }

  public RelationshipEvent.Type getType() {
    return type;
  }

  public void setType(RelationshipEvent.Type type) {
    this.type = type;
  }

  public static NotificationContext makeActivityNofification(ExoSocialActivity activity) {
    NotificationContext ctx = new NotificationContext();
    ctx.setActivity(activity);
    ctx.setActivityId(activity.getId());
    return ctx;
  }

  public static NotificationContext makeProfileNofification(Profile profile) {
    NotificationContext ctx = new NotificationContext();
    ctx.setProfile(profile);
    ctx.setRemoteId(profile.getIdentity().getRemoteId());
    return ctx;
  }
  
  public static NotificationContext makeSpaceNofification(Space space, String userId) {
    NotificationContext ctx = new NotificationContext();
    ctx.setSpace(space);
    ctx.setRemoteId(userId);
    return ctx;
  }

  public static NotificationContext makeRelationshipNofification(RelationshipManager relationshipManager, Relationship relationship) {
    NotificationContext ctx = new NotificationContext();
    ctx.setRelationshipManager(relationshipManager);
    ctx.setRelationship(relationship);
    return ctx;
  }
}
