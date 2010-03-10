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

// TODO: Auto-generated Javadoc
/**
 * The Class Identity.
 */
public class Identity {
    
    /** The id. */
    String  id;
    
    /** The remote id. */
    String  remoteId;
    
    /** The provider id. */
    String  providerId;
    
    /** The display name. */
    String  displayName;
    
    /** The profile. */
    Profile profile;

    /**
     * Instantiates a new identity.
     */
    public Identity() {
    }

    /**
     * Instantiates a new identity.
     * 
     * @param id the id
     */
    public Identity(String id) {
        this.id = id;
    }
    
    /**
     * Instantiates a new identity
     * 
     * @param remoteId the remote id of identity
     * @param providerId the provider id of identity
     */
    public Identity(String remoteId, String providerId) {
    	this.remoteId = remoteId;
    	this.providerId = providerId;
    }

    /**
     * Instantiates a new identity.
     * 
     * @param id the id
     * @param displayName the display name
     * @param profile the profile
     */
    public Identity(String id, String displayName, Profile profile) {
        this.id = id;
        this.displayName = displayName;
        this.profile = profile;
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
     * Gets the display name.
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name.
     * 
     * @param displayName the new display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets the profile.
     * 
     * @return the profile
     */
    public Profile getProfile() {
        if (profile == null)
            profile = new Profile(this);
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
}
