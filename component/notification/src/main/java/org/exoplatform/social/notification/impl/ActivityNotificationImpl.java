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
package org.exoplatform.social.notification.impl;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.listeners.SocialActivityListener;

public class ActivityNotificationImpl implements SocialActivityListener {

  @Override
  public void saveActivity(Identity streamOwner, ExoSocialActivity activity) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveActivity(ExoSocialActivity activity) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void updateActivity(ExoSocialActivity activity) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void deleteActivity(ExoSocialActivity activity) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void deleteActivity(String activityId) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveComment(ExoSocialActivity activity, ExoSocialActivity newComment) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void deleteComment(String activityId, String commentId) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void deleteComment(ExoSocialActivity activity, ExoSocialActivity comment) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void saveLike(ExoSocialActivity activity, Identity identity) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void deleteLike(ExoSocialActivity activity, Identity identity) {
    // TODO Auto-generated method stub
    
  }

}
