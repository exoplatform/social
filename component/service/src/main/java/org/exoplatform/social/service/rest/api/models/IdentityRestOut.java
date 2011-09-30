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

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.service.rest.Util;

/**
 * The Identity model for Social Rest APIs.
 * 
 * @author <a href="http://phuonglm.net">phuonglm</a>
 * @since 1.2.3
 */
public class IdentityRestOut extends HashMap<String, Object> {

  public static enum Field {
    ID("id"),
    REMOTE_ID("remoteId"),
    PROVIDER_ID("providerId"),
    PROFILE("profile");
    
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
   * Default constructor
   */
  public IdentityRestOut() {
    initialize();
  }
  
  /**
   * Construct the Identity model from Social's identityId.
   * @param identityId
   * @param portalContainerName
   */
  public IdentityRestOut(String identityId, String portalContainerName) {
    IdentityManager identityManager =  Util.getIdentityManager(portalContainerName);
    Identity identity = identityManager.getIdentity(identityId, true);
    
    this.setId(identity.getId());
    this.setRemoteId(identity.getRemoteId());
    this.setProviderId(identity.getProviderId());
    this.setProfile(new ProfileRestOut(identity.getProfile()));
    Util.buildAbsoluteAvatarURL(this);
  }
  
  /**
   * Construct the Identity model from Social's identity.
   * @param identity
   * @since 1.2.2
   */
  public IdentityRestOut(Identity identity) {
    this.setId(identity.getId());
    this.setRemoteId(identity.getRemoteId());
    this.setProviderId(identity.getProviderId());
    this.setProfile(new ProfileRestOut(identity.getProfile()));
    Util.buildAbsoluteAvatarURL(this);
  }  
  
  /**
   * Gets Id of Identity
   */
  public String getId() {
    return (String) this.get(Field.ID.toString());
  }
  
  /**
   * Sets Id of Identity
   * @param id
   */
  public void setId(String id) {
    if(id != null){
      this.put(Field.ID.toString(), id);
    } else {
      this.put(Field.ID.toString(), "");
    }
  }
  
  /**
   * Gets remoteId of Identity
   * @return
   */
  public String getRemoteId() {
    return (String) this.get(Field.REMOTE_ID.toString());
  }
  
  /**
   * Sets remoteId of Identity
   * @param remoteId
   * @since 1.2.2
   */
  public void setRemoteId(String remoteId) {
    if(remoteId != null){
      this.put(Field.REMOTE_ID.toString(), remoteId);
    } else {
      this.put(Field.REMOTE_ID.toString(), "");
    }
  }
  
  /**
   * Gets providerId of Identity
   * @param remoteId
   */
  public String getProviderId() {
    return (String) this.get(Field.PROVIDER_ID.toString());
  }
  
  /**
   * Sets providerId of Identity
   * @param remoteId
   */  
  public void setProviderId(String providerId) {
    if(providerId != null){
      this.put(Field.PROVIDER_ID.toString(), providerId);
    } else {
      this.put(Field.PROVIDER_ID.toString(), "");
    }
  }

  /**
   * Gets profile of Identity
   * @param remoteId
   */
  public ProfileRestOut getProfile(){
    return (ProfileRestOut) this.get(Field.PROFILE.toString());
  }
  
  /**
   * Sets profile of Identity
   * @param remoteId
   * 
   */  
  public void setProfile(ProfileRestOut profile){
    if(profile != null){
      this.put(Field.PROFILE.toString(), profile);
    } else {
      this.put(Field.PROFILE.toString(), new HashMap<String, Object>());
      Util.buildAbsoluteAvatarURL(this);
    }
  }
  
  private void initialize(){
    this.setId("");
    this.setProfile(new ProfileRestOut());
    this.setProviderId("");
    this.setRemoteId("");
  }
}
