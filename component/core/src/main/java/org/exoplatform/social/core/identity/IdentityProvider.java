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
package org.exoplatform.social.core.identity;

import java.util.List;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;

/**
 * An identity provider represent a service that can serve identity it can be
 * eXo CS contact manager or a CRM tool for example.
 */
public abstract class IdentityProvider<T> {

  /**
   * The identifier can be the URL of the profile or if it's a CS contact
   * manager contact, it will be the UID of the contact
   *
   * @param remoteId
   * @return null if nothing is found, or the Identity object
   */
  public Identity getIdentityByRemoteId(String remoteId) {

    T target = findByRemoteId(remoteId);

    // target not found in provider
    if (target == null) {
      return null;
    }

    Identity identity = createIdentity(target);
    Profile profile = new Profile(identity);
    populateProfile(profile, target);
    identity.setProfile(profile);

    return identity;
  }

  /**
   * Name for this provider. Must be unique among the registered providers.
   *
   * @return
   */
  public abstract String getName();

  /**
   * Finds the remote object for an identity by ID.
   *
   * @param remoteId id of the remote object
   * @return
   */
  public abstract T findByRemoteId(String remoteId);

  /**
   * Creates a new identity for a given remote object
   *
   * @param remoteObject the remote object for the identity holding the profile
   * @return an new Identity object with name and remote id set
   */
  public abstract Identity createIdentity(T remoteObject);

  /**
   * Populate an identity profile for a given remote object
   *
   * @param profile the profile to be populated
   * @param remoteObject the remote object for the identity holding the profile
   */
  public abstract void populateProfile(Profile profile, T remoteObject);

  /**
   * this method is called after the IdentityManager has saved the identity
   * object.
   *
   * @param identity the identity
   */
  public void onSaveIdentity(Identity identity) {
    return;
  }

  /**
   * This method is called after the IdentityManager have saved the profile object.
   *
   * @param profile
   * @since 1.1.1
   */
  public void onSaveProfile(Profile profile) {
    return;
  }

  public List<String> getAllUserId() {
    throw new RuntimeException("getAllUserId() is not implemented for " + getClass());
  }
}
