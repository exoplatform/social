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
package org.exoplatform.social.core.storage.streams;

import java.util.List;

import org.exoplatform.social.common.service.SocialServiceContext;
import org.exoplatform.social.common.service.impl.ProcessorContextImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.chromattic.entity.ActivityEntity;
import org.exoplatform.social.core.identity.model.Identity;

public class StreamProcessContext extends ProcessorContextImpl {
  
  public static String NEW_ACTIVITY_PROCESS = "NEW_ACTIVITY";
  public static String NEW_ACTIVITY_RELATIONS_PROCESS = "NEW_ACTIVITY_FOR_RELATIONS";
  public static String UPDATE_ACTIVITY_PROCESS = "UPDATE_ACTIVITY";
  public static String UPDATE_ACTIVITY_REF = "UPDATE_ACTIVITY_REF";
  public static String UPDATE_ACTIVITY_COMMENTER_PROCESS = "UPDATE_ACTIVITY_COMMENTER";
  public static String UPDATE_ACTIVITY_MENTIONER_PROCESS = "UPDATE_ACTIVITY_MENTIONER";
  public static String DELETE_ACTIVITY_PROCESS = "DELETE_ACTIVITY";
  public static String DELETE_COMMENT_PROCESS = "DELETE_COMMENT";
  public static String LIKE_ACTIVITY_PROCESS = "LIKE_ACTIVITY";
  public static String UNLIKE_ACTIVITY_PROCESS = "UNLIKE_ACTIVITY";
  public static String CONNECT_ACTIVITY_PROCESS = "CONNECT_ACTIVITY";
  public static String DELETE_CONNECT_ACTIVITY_PROCESS = "DELETE_CONNECT_ACTIVITY";
  public static String ADD_SPACE_MEMBER_ACTIVITY_PROCESS = "ADD_SPACE_MEMBER_ACTIVITY";
  public static String REMOVE_SPACE_MEMBER_ACTIVITY_PROCESS = "REMOVE_SPACE_MEMBER_ACTIVITY";
  public static String UPGRADE_STREAM_PROCESS = "UPGRADE_STREAM_ACTIVITY";
  public static String LAZY_UPGRADE_STREAM_PROCESS = "LAZY_UPGRADE_STREAM_ACTIVITY";
  public static String LOAD_ACTIVITIES_STREAM_PROCESS = "LOAD_ACTIVITIES_STREAM_ACTIVITY";
  
  public final static String OWNER = "OWNER";
  public final static String ACTIVITY = "ACTIVITY";
  public final static String ACTIVITY_ENTITY = "ACTIVITY_ENTITY";
  public final static String ACTIVITY_ID = "ACTIVITY_ID";
  public final static String SENDER = "SENDER";
  public final static String RECEIVER = "RECEIVER";
  public final static String SPACE_IDENTITY = "SPACE_IDENTITY";
  public final static String MENTIONERS = "MENTIONERS";
  public final static String COMMENTERS = "COMMENTERS";
  public final static String LIMIT = "LIMIT";
  public final static String OLD_LAST_UPDATED = "OLD_LAST_UPDATED";
  public final static String ACTIVITY_LIST = "ACTIVITY_LIST";
  
  
  public StreamProcessContext(String name, SocialServiceContext context) {
    super(name, context);
  }
  
  public static StreamProcessContext getIntance(String name, SocialServiceContext context) {
    return new StreamProcessContext(name, context);
  }

  
  public StreamProcessContext identity(Identity owner) {
    setProperty(OWNER, owner);
    return this;
  }
  
  public Identity getIdentity() {
    return getProperty(OWNER, Identity.class);
  }
  
  public StreamProcessContext activity(ExoSocialActivity activity) {
    setProperty(ACTIVITY, activity);
    return this;
  }
  
  public ExoSocialActivity getActivity() {
    return getProperty(ACTIVITY, ExoSocialActivity.class);
  }
  
  public StreamProcessContext activityEntity(ActivityEntity entity) {
    setProperty(ACTIVITY_ENTITY, entity);
    return this;
  }

  public ActivityEntity getActivityEntity() {
    return getProperty(ACTIVITY_ENTITY, ActivityEntity.class);
  }
  
  public StreamProcessContext activities(List<ExoSocialActivity> list) {
    setProperty(ACTIVITY_LIST, list);
    return this;
  }
  
  @SuppressWarnings("unchecked")
  public List<ExoSocialActivity> getActivities() {
    return getProperty(ACTIVITY_LIST, List.class);
  }
  
  public StreamProcessContext activityId(String activityId) {
    setProperty(ACTIVITY_ID, activityId);
    return this;
  }
  
  public String getActivityId() {
    return getProperty(ACTIVITY, String.class);
  }
  
  public StreamProcessContext sender(Identity sender) {
    setProperty(SENDER, sender);
    return this;
  }
  
  public Identity getSender() {
    return getProperty(SENDER, Identity.class);
  }
  
  public StreamProcessContext receiver(Identity receiver) {
    setProperty(RECEIVER, receiver);
    return this;
  }
  
  public Identity getReceiver() {
    return getProperty(RECEIVER, Identity.class);
  }
  
  public StreamProcessContext spaceIdentity(Identity spaceIdentity) {
    setProperty(SPACE_IDENTITY, spaceIdentity);
    return this;
  }
  
  public Identity getSpaceIdentity() {
    return getProperty(SPACE_IDENTITY, Identity.class);
  }
  
  public StreamProcessContext mentioners(String...mentioner) {
    setProperty(MENTIONERS, mentioner);
    return this;
  }
  
  public String[] getMentioners() {
    return getProperty(MENTIONERS, String[].class);
  }
  
  public StreamProcessContext commenters(String...commenter) {
    setProperty(COMMENTERS, commenter);
    return this;
  }
  
  public String[] getCommenters() {
    return getProperty(COMMENTERS, String[].class);
  }
  
  public StreamProcessContext limit(int limit) {
    setProperty(LIMIT, limit);
    return this;
  }
  
  public int getLimit() {
    return getProperty(LIMIT, int.class);
  }
  
  public StreamProcessContext oldLastUpdated(long oldLastUpdated) {
    setProperty(OLD_LAST_UPDATED, oldLastUpdated);
    return this;
  }
  
  public long getOldLastUpdated() {
    return getProperty(OLD_LAST_UPDATED, long.class);
  }
}
