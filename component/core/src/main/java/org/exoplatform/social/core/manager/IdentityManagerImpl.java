/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.IdentityProviderPlugin;
import org.exoplatform.social.core.identity.ProfileFilterListAccess;
import org.exoplatform.social.core.identity.SpaceMemberFilterListAccess;
import org.exoplatform.social.core.identity.SpaceMemberFilterListAccess.Type;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.Profile.UpdateType;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.profile.ProfileLifeCycle;
import org.exoplatform.social.core.profile.ProfileListener;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.webui.exception.MessageException;

/**
 * Class IdentityManagerImpl implements IdentityManager without caching.
 *
 * @author <a href="mailto:vien_levan@exoplatform.com">vien_levan</a>
 * @since Nov 24, 2010
 * @version 1.2.0-GA
 */
public class IdentityManagerImpl implements IdentityManager {
  /** Logger */
  private static final Log                   LOG               = ExoLogger.getExoLogger(IdentityManagerImpl.class);
  
  /** The offset for list access loading. */
  private static final int                   OFFSET = 0;
  
  /** The limit for list access loading. */
  private static final int                   LIMIT = 200;
  
  /** The identity providers */
  protected Map<String, IdentityProvider<?>> identityProviders = new HashMap<String, IdentityProvider<?>>();

  /** The identity providers */
  protected Map<String, ProfileListener> profileListeners = new HashMap<String, ProfileListener>();
  
  /** The activityStorage */
  protected IdentityStorage                  identityStorage;

  /** The relationship manager */
  protected RelationshipManager              relationshipManager;

  /** lifecycle for profile */
  protected ProfileLifeCycle                 profileLifeCycle  = new ProfileLifeCycle();

  /**
   * Instantiates a new identity manager.
   *
   * @param identityStorage
   * @param defaultIdentityProvider the built-in default identity provider to use
   *          when no other provider matches
   */
  public IdentityManagerImpl(IdentityStorage identityStorage,
                             IdentityProvider<?> defaultIdentityProvider) {
    this.identityStorage = identityStorage;
    this.addIdentityProvider(defaultIdentityProvider);
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getLastIdentities(int limit) {
    ProfileFilter profileFilter = new ProfileFilter();
    profileFilter.setSorting(new Sorting(Sorting.SortBy.DATE, Sorting.OrderBy.DESC));
    return identityStorage.getIdentitiesForUnifiedSearch(OrganizationIdentityProvider.NAME, profileFilter, 0, limit);
  }

  /**
   * {@inheritDoc}
   */
  public ListAccess<Identity> getConnectionsWithListAccess(Identity identity) {
    return getRelationshipManager().getConnections(identity);
  }

  /**
   * {@inheritDoc}
   */
  public ListAccess<Identity> getIdentitiesByProfileFilter(String providerId, ProfileFilter profileFilter,
                                                           boolean forceLoadProfile) {
    return (new ProfileFilterListAccess(identityStorage, providerId, profileFilter, forceLoadProfile));
  }

  
  /**
   * {@inheritDoc}
   */
  public ListAccess<Identity> getSpaceIdentityByProfileFilter(Space space, ProfileFilter profileFilter, Type type,
                                                           boolean forceLoadProfile) {
    return (new SpaceMemberFilterListAccess(identityStorage, space, profileFilter, type));
  }
  
  /**
   * {@inheritDoc}
   */
  public Profile getProfile(Identity identity) {
    Profile profile = identity.getProfile();
    if (profile.getId() == null) {
      profile = identityStorage.loadProfile(profile);
      identity.setProfile(profile);
    }
    return profile;
  }

  /**
   * {@inheritDoc}
   */
  public void updateProfile(Profile existingProfile) throws MessageException {
    identityStorage.updateProfile(existingProfile);
    broadcastUpdateProfileEvent(existingProfile);
    this.getIdentityProvider(existingProfile.getIdentity().getProviderId()).onUpdateProfile(existingProfile);
  }

  /**
   * {@inheritDoc}
   */
  public void registerProfileListener(ProfileListenerPlugin profileListenerPlugin) {
    profileLifeCycle.addListener(profileListenerPlugin);
  }
  
  /**
   * {@inheritDoc}
   */
  public Identity updateIdentity(Identity identity) {
    return identityStorage.updateIdentity(identity);
  }
  
  /**
   * {@inheritDoc}
   */
  public void addIdentityProvider(IdentityProvider<?> idProvider) {
    if (idProvider != null) {
      LOG.debug("Registering identity provider for " + idProvider.getName() + ": " + idProvider);
      identityProviders.put(idProvider.getName(), idProvider);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void removeIdentityProvider(IdentityProvider<?> identityProvider) {
    if (identityProvider != null) {
      LOG.debug("Removing identity provider for " + identityProvider.getName() + ": " + identityProvider);
      identityProviders.remove(identityProvider.getName());
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public void deleteIdentity(Identity identity) {
    if (identity.getId() == null) {
      LOG.warn("identity.getId() must not be null of [" + identity + "]");
      return;
    }
    this.getIdentityStorage().deleteIdentity(identity);
  }

  /**
   * {@inheritDoc}
   */
  public void hardDeleteIdentity(Identity identity) {
    if (identity.getId() == null) {
      LOG.warn("identity.getId() must not be null of [" + identity + "]");
      return;
    }
    this.getIdentityStorage().hardDeleteIdentity(identity);
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getConnections(Identity ownerIdentity) throws Exception {
    return Arrays.asList(getConnectionsWithListAccess(ownerIdentity).load(OFFSET, LIMIT));
  }

  
  /**
   * {@inheritDoc}
   */
  public List<Identity> getIdentities(String providerId) throws Exception {
    return getIdentities(providerId, true);
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIdentities(String providerId, boolean loadProfile) throws Exception {
    return Arrays.asList(getIdentitiesByProfileFilter(providerId, new ProfileFilter(), loadProfile).load(OFFSET, LIMIT));
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIdentitiesByProfileFilter(String providerId, ProfileFilter profileFilter) throws Exception {
    return Arrays.asList(getIdentitiesByProfileFilter(providerId, profileFilter, false).load(OFFSET, LIMIT));
  }
  
  /**
   * {@inheritDoc}
   */
  public ListAccess<Identity> getIdentitiesForUnifiedSearch(String providerId,
                                                            ProfileFilter profileFilter) {
    return (new ProfileFilterListAccess(identityStorage, providerId, profileFilter, true, ProfileFilterListAccess.Type.UNIFIED_SEARCH));
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIdentitiesByProfileFilter(String providerId,
                                                     ProfileFilter profileFilter,
                                                     long offset,
                                                     long limit) throws Exception {
    return Arrays.asList(getIdentitiesByProfileFilter(providerId, profileFilter, false).load((int)offset, (int)limit));
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIdentitiesByProfileFilter(ProfileFilter profileFilter) throws Exception {
    return Arrays.asList(getIdentitiesByProfileFilter(null, profileFilter, false).load(OFFSET, LIMIT));
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIdentitiesByProfileFilter(ProfileFilter profileFilter,
                                                     long offset,
                                                     long limit) throws Exception {
    return Arrays.asList(getIdentitiesByProfileFilter(null, profileFilter, false).load((int) offset, (int)limit));
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIdentitiesFilterByAlphaBet(String providerId, ProfileFilter profileFilter) throws Exception {
    return Arrays.asList(getIdentitiesByProfileFilter(providerId, profileFilter, false).load(OFFSET, LIMIT));
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIdentitiesFilterByAlphaBet(String providerId,
                                                      ProfileFilter profileFilter,
                                                      long offset,
                                                      long limit) throws Exception {
    return Arrays.asList(getIdentitiesByProfileFilter(providerId, profileFilter, false).load((int)offset, (int)limit));
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIdentitiesFilterByAlphaBet(ProfileFilter profileFilter) throws Exception {
    return Arrays.asList(getIdentitiesByProfileFilter(null, profileFilter, false).load(OFFSET, LIMIT));
  }

  /**
   * {@inheritDoc}
   */
  public Identity getIdentity(String id) {
    return getIdentity(id, true);
  }

  /**
   * {@inheritDoc}
   */
  public long getIdentitiesCount(String providerId) {
    return identityStorage.getIdentitiesCount(providerId);
  }

  
  /**
   * {@inheritDoc}
   */
  public Identity getIdentity(String identityId, boolean forceLoadOrReloadProfile) {
    Identity returnIdentity = this.getIdentityStorage().findIdentityById(identityId);

    if (returnIdentity != null) {
      if (forceLoadOrReloadProfile) {
        Profile profile = this.getIdentityStorage().loadProfile(returnIdentity.getProfile());
        returnIdentity.setProfile(profile);
      }
    } else {
      LOG.info("Can not get identity with id: " + identityId);
    }
    return returnIdentity;
  }

  /**
   * {@inheritDoc}
   */
  public Identity getIdentity(String providerId, String remoteId, boolean loadProfile) {
    return getOrCreateIdentity(providerId, remoteId, loadProfile);
  }

  /**
   * {@inheritDoc}
   */
  public Identity getOrCreateIdentity(String providerId, String remoteId) {
    return getOrCreateIdentity(providerId, remoteId, true);
  }

  /**
   * {@inheritDoc}
   */
  public Identity getOrCreateIdentity(String providerId, String remoteId, boolean forceLoadOrReloadProfile) {
    Identity returnIdentity = null;
    IdentityProvider<?> identityProvider = this.getIdentityProvider(providerId);

    Identity identityFoundByRemoteProvider = identityProvider.getIdentityByRemoteId(remoteId);
    Identity result = this.getIdentityStorage().findIdentity(providerId, remoteId);
    if (result == null) {
      if (identityFoundByRemoteProvider != null) {
        // identity is valid for provider, but no yet
        // referenced in activityStorage
        saveIdentity(identityFoundByRemoteProvider);
        this.getIdentityStorage().saveProfile(identityFoundByRemoteProvider.getProfile());
        result = identityFoundByRemoteProvider;
        
        // case of create new space or user, an event will be called only in case of create user
        if (OrganizationIdentityProvider.NAME.equals(providerId)) {
          profileLifeCycle.createProfile(result.getProfile());
        }
      } else {
        // Not found in provider, so return null
        return result;
      }
    } else {
      if (identityFoundByRemoteProvider == null) {
        // in the case: identity is stored but identity is not found from
        // remote provider, sets that identity as deleted
        if (!result.isDeleted()) {
          result.setDeleted(true);
          identityStorage.updateIdentity(result);
        }
        return result;
      }
      if (forceLoadOrReloadProfile) {
        Profile profile = this.getIdentityStorage().loadProfile(result.getProfile());
        result.setProfile(profile);
      }
    }
    returnIdentity = result;
    return returnIdentity;
  }

  /**
   * {@inheritDoc}
   */
  public boolean identityExisted(String providerId, String remoteId) {
    IdentityProvider<?> identityProvider = getIdentityProvider(providerId);
    return identityProvider.getIdentityByRemoteId(remoteId) != null ? true : false;
  }

  /**
   * {@inheritDoc}
   */
  public void registerIdentityProviders(IdentityProviderPlugin plugin) {
    List<IdentityProvider<?>> pluginProviders = plugin.getProviders();
    if (pluginProviders != null) {
      for (IdentityProvider<?> identityProvider : pluginProviders) {
        this.addIdentityProvider(identityProvider);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void saveIdentity(Identity identity) {
    this.getIdentityStorage().saveIdentity(identity);
    this.getIdentityProvider(identity.getProviderId()).onSaveIdentity(identity);
  }

  /**
   * {@inheritDoc}
   */
  public void saveProfile(Profile profile) {
    this.getIdentityStorage().saveProfile(profile);
    this.getIdentityProvider(profile.getIdentity().getProviderId()).onSaveProfile(profile);
  }

  /**
   * {@inheritDoc}
   */
  public void updateAvatar(Profile p) throws MessageException {
    updateProfile(p);
  }

  /**
   * {@inheritDoc}
   */
  public void updateBasicInfo(Profile p) throws Exception {
    updateProfile(p);
  }

  /**
   * {@inheritDoc}
   */
  public void updateContactSection(Profile p) throws Exception {
    updateProfile(p);
  }

  /**
   * {@inheritDoc}
   */
  public void updateExperienceSection(Profile p) throws Exception {
    updateProfile(p);
  }

  /**
   * {@inheritDoc}
   */
  public void updateHeaderSection(Profile p) throws Exception {
    updateProfile(p);
  }

  /**
   * Gets identityProvider.
   *
   * @param providerId
   * @return
   */
  public IdentityProvider<?> getIdentityProvider(String providerId) {
    IdentityProvider<?> provider = identityProviders.get(providerId);
    if (provider == null) {
      throw new RuntimeException("No suitable identity provider exists for " + providerId);
    }
    return provider;
  }

  /**
   * Gets identityStorage.
   *
   * @return identityStorage
   */
  public IdentityStorage getIdentityStorage() {
    return this.identityStorage;
  }

  /**
   * Sets identityStorage.
   *
   * @param identityStorage
   */
  public void setIdentityStorage(IdentityStorage identityStorage) {
    this.identityStorage = identityStorage;
  }

  /**
   * Gets relationshipManager.
   *
   * @return relationshipManager
   */
  public RelationshipManager getRelationshipManager() {
    if (relationshipManager == null) {
      relationshipManager = (RelationshipManager) PortalContainer.getInstance()
                                                                 .getComponentInstanceOfType(RelationshipManager.class);
    }
    return relationshipManager;
  }

  /**
   * {@inheritDoc}
   */
  public void addProfileListener(ProfileListenerPlugin plugin) {
    registerProfileListener(plugin);
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
   * {@inheritDoc}
   */
  public void addOrModifyProfileProperties(Profile profile) throws Exception {
    this.getIdentityStorage().addOrModifyProfileProperties(profile);
    this.getIdentityProvider(profile.getIdentity().getProviderId()).onSaveProfile(profile);
  }

  /**
   * {@inheritDoc}
   */
  public IdentityStorage getStorage() {
    return this.identityStorage;
  }

  /**
   * Broadcasts update profile event depending on type of update. 
   * 
   * @param profile
   * @since 1.2.0-GA
   */
  protected void broadcastUpdateProfileEvent(Profile profile) {
    for (UpdateType type : profile.getListUpdateTypes()) {
      type.updateActivity(profileLifeCycle, profile);
    }
  }

  @Override
  public void processEnabledIdentity(String remoteId, boolean isEnable) {
    Identity identity = getOrCreateIdentity(OrganizationIdentityProvider.NAME, remoteId, false);
    this.getIdentityStorage().processEnabledIdentity(identity, isEnable);
  }
}
