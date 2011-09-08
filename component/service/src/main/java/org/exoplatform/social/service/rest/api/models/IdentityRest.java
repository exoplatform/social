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
 * @since 1.2.2
 */
public class IdentityRest extends HashMap<String, Object> {

  private static final long serialVersionUID = 4373990937109717268L;
  public static final String ID = "id";
  public static final String REMOTE_ID = "remoteId";
  public static final String PROVIDER_ID = "providerId";
  public static final String PROFILE = "profile";

  /**
   * Default constructor
   */
  public IdentityRest() {
  }
  
  /**
   * Construct the Identity model from Social's identityId.
   * @param identityId
   * @since 1.2.2
   */
  public IdentityRest(String identityId) {
    IdentityManager identityManager =  Util.getIdentityManager();
    Identity identity = identityManager.getIdentity(identityId, false);
    this.put(ID,identity.getId());
    this.put(REMOTE_ID,identity.getRemoteId());
    this.put(PROVIDER_ID, identity.getProviderId());
    this.put(PROFILE, new ProfileRest(identity.getProfile()));
  }
  
  /**
   * Construct the Identity model from Social's identity.
   * @param identity
   * @since 1.2.2
   */
  public IdentityRest(Identity identity) {
    this.setId(identity.getId());
    this.setRemoteId(identity.getRemoteId());
    this.setProviderId(identity.getProviderId());
    this.setProfile(new ProfileRest(identity.getProfile()));
  }  
  
  /**
   * Gets Id of Identity
   * @since 1.2.2
   */
  public String getId() {
    return (String) this.get(ID);
  }
  
  /**
   * Sets Id of Identity
   * @param id
   */
  public void setId(String id) {
    if(id != null){
      this.put(ID,id);
    }
  }
  
  /**
   * Gets remoteId of Identity
   * @return
   * @since 1.2.2
   */
  public String getRemoteId() {
    return (String) this.get(REMOTE_ID);
  }
  
  /**
   * Sets remoteId of Identity
   * @param remoteId
   * @since 1.2.2
   */
  public void setRemoteId(String remoteId) {
    if(remoteId != null){
      this.put(REMOTE_ID,remoteId);
    }
  }
  
  /**
   * Gets providerId of Identity
   * @param remoteId
   * @since 1.2.2
   */
  public String getProviderId() {
    return (String) this.get(PROVIDER_ID);
  }
  
  /**
   * Sets providerId of Identity
   * @param remoteId
   * @since 1.2.2
   */  
  public void setProviderId(String providerId) {
    if(providerId != null){
      this.put(PROVIDER_ID, providerId);
    }
  }
  
  /**
   * Sets profile of Identity
   * @param remoteId
   * @since 1.2.2
   */  
  public void setProfile(ProfileRest profile){
    if(profile != null){
      this.put(PROFILE, profile);
    }
  }
  
  /**
   * Gets profile of Identity
   * @param remoteId
   * @since 1.2.2
   */
  public ProfileRest getProfile(){
    return (ProfileRest) this.get(PROFILE);
  }
}
