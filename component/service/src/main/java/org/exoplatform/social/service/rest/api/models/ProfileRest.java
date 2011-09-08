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

package org.exoplatform.social.service.rest.api.models;

import java.util.HashMap;

import org.exoplatform.social.core.identity.model.Profile;

/**
 * The Profile model for Social Rest APIs.
 * 
 * @author <a href="http://phuonglm.net">phuonglm</a>
 * @since 1.2.2
 */
public class ProfileRest extends HashMap<String, Object> {
  private static final long serialVersionUID = 8890238970289549032L;

  public static final String FULLNAME = "fullName";
  public static final String AVATARURL = "avatarUrl";
  
  /**
   * Default Constructor
   */
  public ProfileRest(){
  }
  
  /**
   * Construct the Profile model from Social profile.
   * @param profile
   * @since 1.2.2
   */
  public ProfileRest(Profile profile){
    this.setFullName(profile.getFullName());
    this.setAvatarUrl(profile.getAvatarUrl());    
  }
  
  /**
   * Sets fullname of profile
   * @param fullName
   * @since 1.2.2
   */
  public void setFullName(String fullName){
    if(fullName != null){
      this.put(FULLNAME, fullName);
    }
  }
  
  /**
   * Gets fullname of profile
   * @return
   * @since 1.2.2
   */
  public String getFullName(){
    return (String) this.get(FULLNAME);
  }
  
  /**
   * Sets avatarURL of profile
   * @param avatarUrl
   * @since 1.2.2
   */
  public void setAvatarUrl(String avatarUrl){
    if(avatarUrl != null){
      this.put(AVATARURL, avatarUrl);
    }
  }

  /**
   * Gets avatarURL of profile
   * @param avatarUrl
   * @since 1.2.2
   */
  public String getAvatarUrl(){
    return (String) this.get(AVATARURL);
  }
  
}
