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
package org.exoplatform.social.core.storage.api;

import java.util.List;

import org.exoplatform.social.common.service.ProcessContext;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;


public interface ActivityStreamStorage {
  
  /**
   * Saves the activity into a streams.
   * 
   * @param ctx 
   * 1. ctx.getIdentity()  
   * Owner is User Identity: Adds given {@link org.exoplatform.social.core.activity.model.ExoSocialActivity}
   * to Connections's Feed and Connections streams.
   * 
   * Owner is Space Identity: Adds given {@link org.exoplatform.social.core.activity.model.ExoSocialActivity}
   * to Space's Member's Feed and My Spaces streams.
   * 2. ctx.getActivity() given activity to records the stream
   * 
   * @since 4.0.2, 4.1.0
   */
  public void save(ProcessContext ctx);
  

  public void delete(String activityId);
  
  public void unLike(Identity removedLike, ExoSocialActivity activity);
  
  public void connect(Identity sender, Identity receiver);
  
  public void deleteConnect(Identity sender, Identity receiver);
  
  /**
   * Updates the activity when has actions such as like, mentions, unlike, or add comment
   * 
   * @param ctx 
   * 1. ctx.getActivity() given activity to records the streams
   * 2. ctx.getOldUpdated() oldUpdated of given the Activity. 
   * @since 4.0.2, 4.1.0
   */
  public void update(ProcessContext ctx);
  
  /**
   * Adds new member for existing space
   * 
   * @param ctx 
   * 1. ctx.getIdentity() the member is added to Space as member
   * 2. ctx.getIdentitySpace() Space Identity
   * 
   * @since 4.0.2, 4.1.0
   */
  public void addSpaceMember(ProcessContext ctx);
  
  /**
   * Removes the member from existing space
   * 
   * @param ctx 
   * 1. ctx.getIdentity() the member is removed from the Space.
   * 2. ctx.getIdentitySpace() Space Identity
   * 
   * @since 4.0.2, 4.1.0
   */
  public void removeSpaceMember(ProcessContext ctx);
  
  public List<ExoSocialActivity> getFeed(Identity owner, int offset, int limit);
  
  public int getNumberOfFeed(Identity owner);
  
  public List<ExoSocialActivity> getConnections(Identity owner, int offset, int limit);
  
  public int getNumberOfConnections(Identity owner);
  
  public List<ExoSocialActivity> getMySpaces(Identity owner, int offset, int limit);
  
  public int getNumberOfMySpaces(Identity owner);
  
  public List<ExoSocialActivity> getSpaceStream(Identity owner, int offset, int limit);
  
  public int getNumberOfSpaceStream(Identity owner);
  
  public List<ExoSocialActivity> getMyActivities(Identity owner, int offset, int limit);
  
  public int getNumberOfMyActivities(Identity owner);
  
}
