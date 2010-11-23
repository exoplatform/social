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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.IdentityProviderPlugin;
import org.exoplatform.social.core.identity.model.GlobalId;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.profile.ProfileLifeCycle;
import org.exoplatform.social.core.profile.ProfileListener;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.IdentityStorage;

/**
 * The Class IdentityManager.
 */
public class IdentityManager {
  /** Logger */
  private static final Log LOG = ExoLogger.getExoLogger(IdentityManager.class);
  /** The limit search list to be returned for matching search criteria */
  private static final long SEARCH_LIMIT = 500;

  /** The identity providers. */
  private final Map<String, IdentityProvider<?>> identityProviders = new HashMap<String, IdentityProvider<?>>();

  /** The storage. */
  private IdentityStorage identityStorage;

  private RelationshipManager relationshipManager;

  private final ExoCache<String, Identity> identityCacheById;

  /**
   * identityCache
   */
  private final ExoCache<GlobalId, Identity> identityCache;

  /**
   * identityListCache with key = identityProvider
   */
  private final ExoCache<String, List<Identity>> identityListCache;
  /**
   * lifecycle for profile
   */
  private final ProfileLifeCycle profileLifeCycle = new ProfileLifeCycle();

  /**
   * Instantiates a new identity manager.
   *
   * @param identityStorage
   * @param defaultIdentityProvider the builtin default identity provider to use when when no other  provider match
   * @param cacheService
   */
  public IdentityManager(IdentityStorage identityStorage, IdentityProvider<?> defaultIdentityProvider, CacheService cacheService) {
    this.identityStorage = identityStorage;
    this.addIdentityProvider(defaultIdentityProvider);
    this.identityCacheById = cacheService.getCacheInstance(getClass().getName() + "identityCacheById");
    this.identityCache = cacheService.getCacheInstance(getClass().getName() + "identityCache");
    this.identityListCache = cacheService.getCacheInstance(getClass().getName() + "identityListCache");
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
  public Identity getIdentity(String id) {
    return getIdentity(id, true);
  }

  /**
   * Gets the identity by id optionally loading his profile
   *
   * Note: if id = uuid, you get the info from JCR if found.
   * if id = providerId:remoteId, you get the info from provider and currently with
   * this case, you get null for identity.getId(). This can cause some troubles.
   *
   * @param id ID be a social {@link GlobalId} or a raw identity such as in {@link Identity#getId()}
   * @param loadProfile the load profile true if load and false if doesn't.
   *        when loadProfile is true, gets profile from JCR
   * @return null if nothing is found, or the Identity object
   */
  //FIXME: Make it clear here when id=uuid vs id=providerId:remoteId vs id=providerId:uuid
  public Identity getIdentity(String id, boolean loadProfile) {
    Identity cachedIdentity = null;
    // attempts to match a global id in the form "providerId:remoteId"
    if (GlobalId.isValid(id)) {
      GlobalId globalId = new GlobalId(id);
      String providerId = globalId.getDomain();
      String remoteId = globalId.getLocalId();
      cachedIdentity = identityCache.get(globalId);
      if (cachedIdentity == null) {
        cachedIdentity = getOrCreateIdentity(providerId, remoteId, loadProfile);
        if (cachedIdentity != null) {
          identityCache.put(globalId, cachedIdentity);
          return cachedIdentity;
        }
        //retry with providerId:nodeId
        if (cachedIdentity == null) {
          String tempId = globalId.getLocalId();
          cachedIdentity = identityStorage.findIdentityById(tempId);
          if (cachedIdentity != null) {
            id = tempId;
          }
        }
      }

    } else {
      cachedIdentity = identityCacheById.get(id);
      if (cachedIdentity == null) {
        cachedIdentity = identityStorage.findIdentityById(id);
      }
    }

    if (cachedIdentity != null) {
      if (loadProfile) {
        identityStorage.loadProfile(cachedIdentity.getProfile());
      }
      identityCacheById.put(id, cachedIdentity);
    }
    if (cachedIdentity == null) {
      LOG.info("Can not get identity with id: " + id);
    }
    return cachedIdentity;
  }

  /**
   * Deletes an identity
   *
   * @param identity
   */
  public void deleteIdentity(Identity identity) {
    if (identity.getId() == null) {
      LOG.warn("identity.getId() must not be null of [" + identity + "]");
      return;
    }
    identityStorage.deleteIdentity(identity);
    identityCacheById.remove(identity.getId());
    identityCache.remove(identity.getGlobalId());
    identityListCache.remove(identity.getProviderId());
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
   */
  public Identity getOrCreateIdentity(String providerId, String remoteId) {
    return getOrCreateIdentity(providerId, remoteId, true);
  }

  /**
   * This function return an Identity object that specific to
   * a special type.
   * <p>
   * For example if the type is Linked'In, the identifier will be the URL of the profile
   * or if it's a CS contact manager contact, it will be the UID of the contact.</p>
   *  A new identity is created if it is found by provider, if not null will be returned.
   *
   * If identity is found by provider and not stored, store it. If stored, return it.
   *
   * If identity is not found by provider, return null. If stored, delete that stored identity.
   *
   *
   * @param providerId refering to the name of the Identity provider
   * @param remoteId   the identifier that identify the identity in the specific identity provider
   * @param loadProfile true to load profile
   * @return null if nothing is found, or the Identity object
   * TODO improve the performance by specifying what needs to be loaded
   */
  public Identity getOrCreateIdentity(String providerId, String remoteId, boolean loadProfile) {
    GlobalId globalIdCacheKey = GlobalId.create(providerId, remoteId);
    Identity cachedIdentity = identityCache.get(globalIdCacheKey);
    if (cachedIdentity == null) {
      IdentityProvider<?> identityProvider = getIdentityProvider(providerId);

      Identity identity1 = identityProvider.getIdentityByRemoteId(remoteId);
      Identity result = identityStorage.findIdentity(providerId, remoteId);
      //FIXME make it clear here when both identity1 and result != null.
      if(result == null) {
        if (identity1 != null) { // identity is valid for provider, but no yet referenced in storage
          saveIdentity(identity1);
          identityStorage.saveProfile(identity1.getProfile());
          result = identity1;
        } else {
          //Not found in provider, so return null
          return result;
        }
      } else {
        if (identity1 == null) {
          //in the case: identity is stored but identity is not found from provider, delete that identity
          identityStorage.deleteIdentity(result);
          return null;
        }
        if (loadProfile) {
          identityStorage.loadProfile(result.getProfile());
        }
      }
      cachedIdentity = result;
      if (cachedIdentity.getId() != null) {
        identityCache.put(globalIdCacheKey, cachedIdentity);
      }
    }
    return cachedIdentity;
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

  /**
   * Gets the identities by profile filter.
   *
   * @param providerId
   * @param profileFilter
   * @param offset
   * @param limit
   * @return identity list
   * @throws Exception
   */
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

  /**
   * Gets the identities by profile filter
   *
   * @param profileFilter
   * @param offset
   * @param limit
   * @return the identities by profile filter
   * @throws Exception
   */
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
    return identityStorage.getIdentitiesFilterByAlphaBet(providerId, profileFilter, 0, SEARCH_LIMIT);
  }

  /**
   *
   * @param providerId
   * @param profileFilter
   * @param offset
   * @param limit
   * @return the identitities list
   * @throws Exception
   */
  public List<Identity> getIdentitiesFilterByAlphaBet(String providerId, ProfileFilter profileFilter, long offset, long limit) throws Exception {
    return identityStorage.getIdentitiesFilterByAlphaBet(providerId, profileFilter, offset, limit);
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
   * Gets identity from the provider, not in JCR.
   * To make sure to gets the info from JCR, use {@link #getOrCreateIdentity(String, String, boolean)}
   *
   * @param providerId
   * @param remoteId
   * @param loadProfile
   * @return identity
   */
  public Identity getIdentity(String providerId, String remoteId, boolean loadProfile) {
    GlobalId globalIdCacheKey = GlobalId.create(providerId, remoteId);
    Identity cachedIdentity = identityCache.get(globalIdCacheKey);
    if (cachedIdentity == null) {
      IdentityProvider<?> identityProvider = getIdentityProvider(providerId);
      cachedIdentity = identityProvider.getIdentityByRemoteId(remoteId);
      if (cachedIdentity != null) {
        Identity storedIdentity = identityStorage.findIdentity(providerId, remoteId);
        if (storedIdentity != null) {
          cachedIdentity.setId(storedIdentity.getId());
          if (loadProfile) {
            identityStorage.loadProfile(cachedIdentity.getProfile());
          }
        } else {
          //save new identity
          identityStorage.saveIdentity(cachedIdentity);
        }
      }
      identityCache.put(globalIdCacheKey, cachedIdentity);
    }

    return cachedIdentity;
  }

  /**
   * Checks if identity existed or not
   * @param providerId
   * @param remoteId
   * @return true or false
   */
  public boolean identityExisted(String providerId, String remoteId) {
    IdentityProvider<?> identityProvider = getIdentityProvider(providerId);
    return identityProvider.getIdentityByRemoteId(remoteId) != null ? true : false;
  }

  /**
   * Save identity.
   *
   * @param identity the identity
   */
  public void saveIdentity(Identity identity) {
    identityStorage.saveIdentity(identity);
    getIdentityProvider(identity.getProviderId()).onSaveIdentity(identity);
    if (identity.getId() != null) {
      identityCacheById.remove(identity.getId());
      identityCache.remove(identity.getGlobalId());
    }
    identityListCache.remove(identity.getProviderId());
  }

  /**
   * Save a profile
   *
   * @param profile
   */
  public void saveProfile(Profile profile) {
    identityStorage.saveProfile(profile);
    getIdentityProvider(profile.getIdentity().getProviderId()).onSaveProfile(profile);
    removeCacheForProfileChange(profile);
  }

  /**
   * Add or modify properties of profile. Profile parameter is a lightweight that 
   * contains only the property that you want to add or modify. NOTE: The method will
   * not delete the properties on old profile when the param profile have not those keys.
   * 
   * @param profile
   * @throws Exception 
   */
  private Profile addOrModifyProfileProperties(Profile profile) throws Exception{
    identityStorage.addOrModifyProfileProperties(profile);
    Profile newProfile = addOrModifyProfilePropertiesCache(profile);
    String providerId = profile.getIdentity().getProviderId();
    if(newProfile == null)
      newProfile = getIdentity(providerId, true).getProfile();

    getIdentityProvider(providerId).onSaveProfile(newProfile);
    return newProfile;
  }

  /**
   * Add or modify properties of profile in cache. Profile parameter is a lightweight that 
   * contains only the property that you want to add or modify. NOTE: The method will
   * not delete the properties on old profile when the param profile have not those keys.
   * 
   * @param profile
   * @return
   */
  private Profile addOrModifyProfilePropertiesCache(Profile profile) {
    Profile cachedProfile = getCachedProfile(profile.getIdentity()).getProfile();
    if(cachedProfile == null)
      return null;
    cachedProfile.addOrModifyProperties(profile.getProperties());
    return cachedProfile;
  }

  /**
   * Get profile that was cached
   * 
   * @param identity
   * @return
   */
  private Identity getCachedProfile(Identity identity) {
    Identity cachedIdentity;
    if(identity == null)
      return null;
    String identityId = identity.getId();
    if (identityId != null) {
      cachedIdentity = identityCacheById.get(identityId);
      if (cachedIdentity != null)
        return cachedIdentity;
    }

    GlobalId globalId = identity.getGlobalId();
    if (globalId != null) {
      cachedIdentity = identityCache.get(globalId);
      if (cachedIdentity != null)
        return cachedIdentity;
    }

    String providerId = identity.getProviderId();
    List<Identity> listIdentity = identityListCache.get(providerId);
    if (listIdentity != null)
      for (Identity identityFromCache : listIdentity)
        if (identityFromCache.equals(identity))
          return identityFromCache;
    return null;
  }

  /**
   * Updates avatar
   *
   * @param p profile
   * @throws Exception 
   */
  //TODO make easier api, this is not good.
  public void updateAvatar(Profile p) throws Exception {
    saveProfile(p);
    profileLifeCycle.avatarUpdated(p.getIdentity().getRemoteId(), p);
    LOG.debug("Update avatar successfully for user: " + p);
  }

  /**
   *
   * @param p
   * @throws Exception 
   */
  public Profile updateBasicInfo(Profile p) throws Exception {
    Profile newProfile = addOrModifyProfileProperties(p);
    profileLifeCycle.basicUpdated(newProfile.getIdentity().getRemoteId(), newProfile);
    LOG.debug("Update basic infomation successfully for user: " + newProfile);
    return newProfile;
  }

  /**
   * Update the contact section of profile 
   * 
   * @param p
   * @throws Exception 
   */
  public void updateContactSection(Profile p) throws Exception {
    Profile newProfile = addOrModifyProfileProperties(p);
    profileLifeCycle.contactUpdated(newProfile.getIdentity().getRemoteId(), newProfile);
    LOG.debug("Update contact section successfully for user: " + newProfile);
  }

  /**
   * Update the experience section of profile 
   *
   * @param p
   * @throws Exception 
   */
  public void updateExperienceSection(Profile p) throws Exception {
    Profile newProfile = addOrModifyProfileProperties(p);
    profileLifeCycle.experienceUpdated(p.getIdentity().getRemoteId(), newProfile);
    LOG.debug("Update experience section successfully for user: " + newProfile);
  }

  /**
   * Update the header section of profile 
   *
   * @param p
   * @throws Exception 
   */
  public void updateHeaderSection(Profile p) throws Exception {
    Profile newProfile = addOrModifyProfileProperties(p);
    profileLifeCycle.headerUpdated(p.getIdentity().getRemoteId(), newProfile);
    LOG.debug("Update header section successfully for user: " + newProfile);
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
   * Gets all the identities from a providerId.
   *
   * @param providerId the provider id
   * @param loadProfile the load profile
   * @return the identities
   */
  public List<Identity> getIdentities(String providerId, boolean loadProfile) {
    IdentityProvider<?> ip = getIdentityProvider(providerId);
    List<String> userids = ip.getAllUserId();
    List<Identity> ids = new ArrayList<Identity>();

    for(String userId : userids) {
      ids.add(this.getOrCreateIdentity(providerId, userId, loadProfile));
    }
    return ids;
  }

  /**
   * Gets connections of an identity.
   *
   * @param ownerIdentity
   * @return list of identity
   * @throws Exception
   * @since 1.1.1
   */
  public List<Identity> getConnections(Identity ownerIdentity) throws Exception {
    List<Identity> connectionsList = getIdentities(OrganizationIdentityProvider.NAME);
    Iterator<Identity> itr = connectionsList.iterator();
    relationshipManager = getRelationshipManager();
    while (itr.hasNext()) {
      Identity identity = itr.next();
      if (relationshipManager.getConnectionStatus(identity, ownerIdentity) != Relationship.Type.CONFIRM) {
        itr.remove();
      }
    }
    return connectionsList;
  }

  /**
   * Gets the storage.
   *
   * @return the storage
   */
  public IdentityStorage getStorage() {
    return this.identityStorage;
  }

  /**
   * Sets identityStorage
   *
   * @param identityStorage
   */
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
   *
   * @param listener
   */
  public void registerProfileListener(ProfileListener listener) {
    profileLifeCycle.addListener(listener);
  }

  /**
   *
   * @param listener
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

  /**
   * Gets IdentityStorage
   *
   * @return identityStorage
   */
  public IdentityStorage getIdentityStorage() {
    return identityStorage;
  }

  /**
   * Removes cache when there is changes in profile
   *
   * @param profile
   */
  private void removeCacheForProfileChange(Profile profile) {
    Identity identity = profile.getIdentity();
    identityCacheById.remove(identity.getId());
    identityCache.remove(profile.getIdentity().getGlobalId());
    identityListCache.remove(identity.getProviderId());
  }

  private RelationshipManager getRelationshipManager() {
    if (relationshipManager == null) {
      relationshipManager = (RelationshipManager) PortalContainer.getInstance().getComponentInstanceOfType(RelationshipManager.class);
    }
    return relationshipManager;
  }
}
