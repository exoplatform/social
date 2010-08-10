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
package org.exoplatform.social.core.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.common.jcr.SocialDataLocation;
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.IdentityProviderPlugin;
import org.exoplatform.social.core.identity.model.GlobalId;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.profile.ProfileLifeCycle;
import org.exoplatform.social.core.profile.ProfileListener;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;
import org.exoplatform.social.core.storage.IdentityStorage;

/**
 * The Class IdentityManager.
 */
public class IdentityManager {
  private static final Log LOG = ExoLogger.getExoLogger(IdentityManager.class);

  /** The identity providers. */
  private Map<String, IdentityProvider<?>> identityProviders = new HashMap<String, IdentityProvider<?>>();

  /** The storage. */
  private IdentityStorage identityStorage;

  /**
   * lifecycle for profile
   */
  private ProfileLifeCycle profileLifeCycle = new ProfileLifeCycle();

  /**
   * Instantiates a new identity manager.
   *
   * @param dataLocation the data location
   * @param defaultIdentityProvider the builtin default identity provider to use when when no other  provider match
   * @throws Exception the exception
   */
  public IdentityManager(SocialDataLocation dataLocation, IdentityProvider<?> defaultIdentityProvider) throws Exception {
    this.identityStorage = new IdentityStorage(dataLocation);
    this.addIdentityProvider(defaultIdentityProvider);
  }


  /**
   * Registers one or more {@link IdentityProvider} through an {@link IdentityProviderPlugin}
   * @param plugin
   */
  public void registerIdentityProviders(IdentityProviderPlugin plugin) {
    List<IdentityProvider<?>> pluginProviders =  plugin.getProviders();
    if (pluginProviders != null) {
      for (IdentityProvider<?> identityProvider : pluginProviders) {
        this.addIdentityProvider(identityProvider);
      }
    }
  }

  /**
   * Gets the identity by id and also loads his profile
   *
   * @param id ID can be a social {@link GlobalId} or a raw identity such as in {@link Identity#getId()}
   * @return null if nothing is found, or the Identity object
   * @see #getIdentity(String, boolean)
   */
  public Identity getIdentity(String id) throws Exception {
    return getIdentity(id, true);
  }

  /**
   * Gets the identity by id optionnaly loading his profile
   *
   * @param id ID be a social {@link GlobalId} or a raw identity such as in {@link Identity#getId()}
   * @param loadProfile the load profile true if load and false if doesn't
   * @return null if nothing is found, or the Identity object
   */
  public Identity getIdentity(String id, boolean loadProfile) throws Exception {
    Identity identity = null;

    // attempts to match a global id in the form "providerId:remoteId"
    if (GlobalId.isValid(id)) {
      GlobalId globalId = new GlobalId(id);
      String providerId = globalId.getDomain();
      String remoteId = globalId.getLocalId();
      identity = getOrCreateIdentity(providerId, remoteId, loadProfile);
    }

    // attempts to find a raw id
    if (identity == null) {
      identity = identityStorage.findIdentityById(id);
    }

    if (identity == null)
      return null;

    // TODO :suspicious
    if(loadProfile)
      identityStorage.loadProfile(identity.getProfile());

    return identity;
  }


  /**
   * Adds the identity provider.
   *
   * @param idProvider the id provider
   */
  public void addIdentityProvider(IdentityProvider<?> idProvider) {
    if (idProvider != null) {
      LOG.debug("Registering identity provider for " + idProvider.getName() + ": " + idProvider);
      identityProviders.put(idProvider.getName(), idProvider);
    }
  }


  /**
   * Gets the identity by remote id.
   *
   * @param providerId the provider id
   * @param remoteId the remote id
   * @return the identity
   * @throws Exception the exception
   */
  public Identity getOrCreateIdentity(String providerId, String remoteId) throws Exception {
    return getOrCreateIdentity(providerId, remoteId, true);
  }

  /**
   * Gets the identities by profile filter.
   *
   * @param providerId the provider id
   * @param profileFilter the profile filter
   * @return the identities by profile filter
   * @throws Exception the exception
   */
  public List<Identity> getIdentitiesByProfileFilter(String providerId, ProfileFilter profileFilter) throws Exception {
    return identityStorage.getIdentitiesByProfileFilter(providerId, profileFilter, 0, 20);
  }

  public List<Identity> getIdentitiesByProfileFilter(String providerId, ProfileFilter profileFilter, long offset, long limit) throws Exception {
    return identityStorage.getIdentitiesByProfileFilter(providerId, profileFilter, offset, limit);
  }

  /**
   * Gets the identities by profile filter.
   *
   * @param profileFilter the profile filter
   * @return the identities by profile filter
   * @throws Exception the exception
   */
  public List<Identity> getIdentitiesByProfileFilter(ProfileFilter profileFilter) throws Exception {
    return getIdentitiesByProfileFilter(null, profileFilter, 0, 20);
  }

  public List<Identity> getIdentitiesByProfileFilter(ProfileFilter profileFilter, long offset, long limit) throws Exception {
    return getIdentitiesByProfileFilter(null, profileFilter, offset, limit);
  }
  /**
   * Gets the identities filter by alpha bet.
   *
   * @param providerId the provider id
   * @param profileFilter the profile filter
   * @return the identities filter by alpha bet
   * @throws Exception the exception
   */
  public List<Identity> getIdentitiesFilterByAlphaBet(String providerId, ProfileFilter profileFilter) throws Exception {
    return identityStorage.getIdentitiesFilterByAlphaBet(providerId, profileFilter, 0, 20);
  }

  /**
   * Gets the identities filter by alpha bet.
   *
   * @param profileFilter the profile filter
   * @return the identities filter by alpha bet
   * @throws Exception the exception
   */
  public List<Identity> getIdentitiesFilterByAlphaBet(ProfileFilter profileFilter) throws Exception {
    return getIdentitiesFilterByAlphaBet(null, profileFilter);
  }

  /**
   * This function return an Identity object that specific to
   * a special type.
   * <p>
   * For example if the type is Linked'In, the identifier will be the URL of the profile
   * or if it's a CS contact manager contact, it will be the UID of the contact.</p>
   *  A new identity is created if it does not exist.
   *
   * @param providerId refering to the name of the Identity provider
   * @param remoteId   the identifier that identify the identity in the specific identity provider
   * @param loadProfile true to load profile
   * @return null if nothing is found, or the Identity object
   * TODO improve the performance by specifying what needs to be loaded
   * @throws Exception the exception
   */
  public Identity getOrCreateIdentity(String providerId, String remoteId, boolean loadProfile) throws Exception {

    IdentityProvider<?> identityProvider = getIdentityProvider(providerId);

    Identity identity1 = identityProvider.getIdentityByRemoteId(remoteId);
    Identity result = identityStorage.findIdentity(providerId, remoteId);

    if(result == null) {
      if (identity1 != null) { // identity is valid for provider, but no yet referenced in storage
        saveIdentity(identity1);
        identityStorage.saveProfile(identity1.getProfile());
        result = identity1;
      } else {
        result = new Identity(providerId, remoteId);
        saveIdentity(result);
        if (loadProfile) {
          identityStorage.loadProfile(result.getProfile());
        }
      }
    } else if (loadProfile) {
      identityStorage.loadProfile(result.getProfile());
    }
    return result;
  }

  /**
   * Gets identity
   * @param providerId
   * @param remoteId
   * @param loadProfile
   * @return
   * @throws Exception
   */
  public Identity getIdentity(String providerId, String remoteId, boolean loadProfile) throws Exception {
    IdentityProvider<?> identityProvider = getIdentityProvider(providerId);
    Identity identity = identityProvider.getIdentityByRemoteId(remoteId);
    if (identity == null) return null;
    if (loadProfile) {
      identityStorage.loadProfile(identity.getProfile());
    }
    return identity;
  }

  /**
   * Checks if identity existed or not
   * @param providerId
   * @param remoteId
   * @return
   */
  public boolean identityExisted(String providerId, String remoteId) {
    IdentityProvider<?> identityProvider = getIdentityProvider(providerId);
    return identityProvider.getIdentityByRemoteId(remoteId) != null ? true : false;
  }

  /**
   * Save identity.
   *
   * @param identity the identity
   * @throws Exception the exception
   */
  public void saveIdentity(Identity identity) throws Exception {
    identityStorage.saveIdentity(identity);
    getIdentityProvider(identity.getProviderId()).onSaveIdentity(identity);
  }

  public void updateAvatar(Profile p) throws Exception {
    identityStorage.saveProfile(p);
    profileLifeCycle.avatarUpdated(p.getIdentity().getRemoteId(), p);
  }

  public void updateBasicInfo(Profile p) throws Exception {
    identityStorage.saveProfile(p);
    profileLifeCycle.basicUpdated(p.getIdentity().getRemoteId(), p);
  }

  public void updateContactSection(Profile p) throws Exception {
    identityStorage.saveProfile(p);
    profileLifeCycle.contactUpdated(p.getIdentity().getRemoteId(), p);
  }

  public void updateExperienceSection(Profile p) throws Exception {
    identityStorage.saveProfile(p);
    profileLifeCycle.experienceUpdated(p.getIdentity().getRemoteId(), p);
  }

  public void updateHeaderSection(Profile p) throws Exception {
    identityStorage.saveProfile(p);
    profileLifeCycle.headerUpdated(p.getIdentity().getRemoteId(), p);
  }

  /**
   * Gets the identities.
   *
   * @param providerId the provider id
   * @return the identities
   * @throws Exception the exception
   */
  public List<Identity> getIdentities(String providerId) throws Exception {
    return getIdentities(providerId, true);
  }

  /**
   * Gets the identities.
   *
   * @param providerId the provider id
   * @param loadProfile the load profile
   * @return the identities
   * @throws Exception the exception
   */
  public List<Identity> getIdentities(String providerId, boolean loadProfile) throws Exception {
    IdentityProvider ip = getIdentityProvider(providerId);
    List<String> userids = ip.getAllUserId();
    List<Identity> ids = new ArrayList<Identity>();

    for(String userId : userids) {
      ids.add(this.getOrCreateIdentity(providerId, userId, loadProfile));
    }
    return ids;
  }

  /**
   * Gets the storage.
   *
   * @return the storage
   */
  public IdentityStorage getStorage() {
    return this.identityStorage;
  }

  public void setIdentityStorage(IdentityStorage identityStorage) {
    this.identityStorage = identityStorage;
  }

  private IdentityProvider<?> getIdentityProvider(String providerId) {
    IdentityProvider<?> provider = identityProviders.get(providerId);
    if (provider == null) {
      throw new RuntimeException("No suitable identity provider exists for " + providerId);
    }
    return provider;
  }

  /**
   * {@inheritDoc}
   */
  public void registerProfileListener(ProfileListener listener) {
    profileLifeCycle.addListener(listener);
  }

  /**
   * {@inheritDoc}
   */
  public void unregisterProfileListener(ProfileListener listener) {
    profileLifeCycle.removeListener(listener);
  }

  /**
   * Registers a profile listener component plugin
   * @param plugin
   */
  public void addProfileListener(ProfileListenerPlugin plugin) {
    registerProfileListener(plugin);
  }

  public IdentityStorage getIdentityStorage() {
    return identityStorage;
  }
}
