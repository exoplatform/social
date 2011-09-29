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
public class ProfileRestOut extends HashMap<String, Object>{
  public static enum Field {
    /**
     *  Full name of Identity
     */
    FULL_NAME("fullName"),
    /**
     * The avatar URL of identity
     */
    AVATAR_URL("avatarUrl");
    
   /**
    * String type.
    */
    private final String fieldName;

   /**
    * private constructor.
    *
    * @param string string type
    */
    private Field(final String string) {
      fieldName = string;
    }
    
    public String toString() {
      return fieldName;
    }

  }
  
  /**
   * Default Constructor
   */
  public ProfileRestOut(){
    initialize();
  }
  
  /**
   * Construct the Profile model from Social profile.
   * @param profile
   * @since 1.2.2
   */
  public ProfileRestOut(Profile profile){
    this.setFullName(profile.getFullName());
    this.setAvatarUrl(profile.getAvatarUrl());    
  }
  
  /**
   * Sets fullName of profile
   * @param fullName
   * @since 1.2.2
   */
  public void setFullName(String fullName){
    if(fullName != null){
      this.put(Field.FULL_NAME.toString(), fullName);
    } else {
      this.put(Field.FULL_NAME.toString(), "");
    }
  }
  
  /**
   * Gets fullname of profile
   * @return
   * @since 1.2.2
   */
  public String getFullName(){
    return (String) this.get(Field.FULL_NAME.toString());
  }
  
  /**
   * Sets avatarURL of profile
   * @param avatarUrl
   * @since 1.2.2
   */
  public void setAvatarUrl(String avatarUrl){
    if(avatarUrl != null){
      this.put(Field.AVATAR_URL.toString(), avatarUrl);
    } else {
      this.put(Field.AVATAR_URL.toString(), "");
    }
  }

  /**
   * Gets avatarURL of profile
   * @param avatarUrl
   * @since 1.2.2
   */
  public String getAvatarUrl(){
    return (String) this.get(Field.AVATAR_URL.toString());
  }
  
  private void initialize(){
    this.setFullName("");
    this.setAvatarUrl("");
  }
}
