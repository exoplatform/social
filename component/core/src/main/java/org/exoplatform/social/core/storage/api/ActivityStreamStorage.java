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

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.space.model.Space;

public interface ActivityStreamStorage {
  /**
   * 
   * @param owner
   * @param activity
   */
  public void save(Identity owner, ExoSocialActivity activity);
  
  /**
   * 
   * @param owner
   * @param activity
   */
  public void delete(String activityId);
  
  public void connect(Identity sender, Identity receiver);
  
  public void deleteConnect(Identity sender, Identity receiver);
  
  public void update(Identity owner);
  
  public List<ExoSocialActivity> getFeed(Identity owner, int offset, int limit);
  
  public int getNumberOfFeed(Identity owner);
  
  public List<ExoSocialActivity> getConnections(Identity owner, int offset, int limit);
  
  public int getNumberOfConnections(Identity owner);
  
  public List<ExoSocialActivity> getSpaces(Identity owner, int offset, int limit);
  
  public int getNumberOfSpaces(Identity owner);
  
  public List<ExoSocialActivity> getMyActivities(Identity owner, int offset, int limit);
  
  public int getNumberOfMyActivities(Identity owner);
  
  public void createSpace(Space space);
  
  public void addSpaceMember(Space space,Identity member);
  
  public void removeSpaceMember(Space space,Identity member);
  
}
