/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.identity.model;

import org.exoplatform.social.core.profile.ProfileLoader;

/**
 * Represents persons or objects relevant to the social system.
 */
public class Identity {

  /** The id. */
  String  id;

  /** The remote id. */
  String  remoteId;

  /** The provider id. */
  String  providerId;

  /** Denotes whether this corresponding identity exists or not by the remote identity provider. 
  * 
  * @since 1.2.0-GA 
  */
  boolean isDeleted;
  
  private boolean isEnable;
  
  /** The profile. */
  volatile Profile profile;

  /** The profile loaded allowing to load the profile on demand */
  private volatile ProfileLoader profileLoader;

  /** The global id. */
  GlobalId globalId;

  /**
   * Instantiates a new identity.
   *
   * @param id the id
   */
  public Identity(String id) {
    this.id = id;
    this.isEnable = true;
  }

  /**
   * Instantiates a new identity
   *
   * @param providerId the provider id of identity
   * @param remoteId the remote id of identity
   */
  public Identity(String providerId, String remoteId) {
    this.remoteId = remoteId;
    this.providerId = providerId;
    this.isEnable = true;
  }

  /**
   * Gets the id.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the id.
   *
   * @param id the new id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
  * Checks whether this corresponding identity exists or not by the remote identity provider.
  * 
  * @return true if this corresponding identity is indicated as deleted by the remote identity provider. 
  * @since 1.2.0-GA 
  */
  public boolean isDeleted() {
    return isDeleted;
  }

  /**
  * Sets the isDeleted property to indicate if this identity is deleted or not by the remote identity provider.
  * 
  * @param isDeleted new value to set this identity is deleted or not.
  * @since 1.2.0-GA
  */
  public void setDeleted(boolean isDeleted) {
    this.isDeleted = isDeleted;
  }

  /**
   * @return the isEnable
   */
  public boolean isEnable() {
    return isEnable;
  }

  /**
   * @param isEnable the isEnable to set
   */
  public void setEnable(boolean isEnable) {
    this.isEnable = isEnable;
  }

  /**
   * Gets the profile.
   *
   * @return the profile
   */
  public Profile getProfile() {
    if (profile == null) {
      if (profileLoader == null) {
        profile = new Profile(this);
      } else {
        synchronized (this) {
          if (profile == null) {
            profile = profileLoader.load();
            // Get rid of the loader once it is loaded
            profileLoader = null;
          }
        }
      }
    }
    return profile;
  }

  /**
   * Sets the profile.
   *
   * @param profile the new profile
   */
  public void setProfile(Profile profile) {
    this.profile = profile;
  }

  /**
   * Sets the profile loader.
   *
   * @param profileLoader the new profile loader
   */
  public void setProfileLoader(ProfileLoader profileLoader) {
    this.profileLoader = profileLoader;
  }

  /**
   * Gets the remote id.
   *
   * @return the remote id
   */
  public String getRemoteId() {
    return remoteId;
  }

  /**
   * Sets the remote id.
   *
   * @param remoteId the new remote id
   */
  public void setRemoteId(String remoteId) {
    this.remoteId = remoteId;
  }

  /**
   * Gets the provider id.
   *
   * @return the provider id
   */
  public String getProviderId() {
    return providerId;
  }

  /**
   * Sets the provider id.
   *
   * @param providerId the new provider id
   */
  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }

  /**
   * @return the global id string of identity
   */
  @Override
  public String toString() {
    return getGlobalId().toString();
  }

  /**
   * @return global id of identity
   */
  public GlobalId getGlobalId() {
    if(globalId == null)
      globalId = GlobalId.create(providerId, remoteId);
    return globalId;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Identity other = (Identity)obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    return true;
  }
}