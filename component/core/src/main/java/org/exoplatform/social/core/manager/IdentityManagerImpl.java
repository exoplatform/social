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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
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
 * Class IdentityManagerImpl implements IdentityManager without caching.
 *
 * @author <a href="mailto:vien_levan@exoplatform.com">vien_levan</a>
 * @since Nov 24, 2010
 * @version 1.2.0-GA
 */
public class IdentityManagerImpl implements IdentityManager {
  /** Logger */
  private static final Log                   LOG               = ExoLogger.getExoLogger(IdentityManagerImpl.class);

  /** The identity providers */
  protected Map<String, IdentityProvider<?>> identityProviders = new HashMap<String, IdentityProvider<?>>();

  /** The storage */
  protected IdentityStorage                  identityStorage;

  /** The relationship manager */
  protected RelationshipManager              relationshipManager;

  /** lifecycle for profile */
  protected ProfileLifeCycle                 profileLifeCycle  = new ProfileLifeCycle();

  /**
   * Instantiates a new identity manager.
   *
   * @param identityStorage
   * @param defaultIdentityProvider the builtin default identity provider to use
   *          when when no other provider match
   */
  public IdentityManagerImpl(IdentityStorage identityStorage,
                             IdentityProvider<?> defaultIdentityProvider) {
    this.identityStorage = identityStorage;
    this.addIdentityProvider(defaultIdentityProvider);
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
  public List<Identity> getConnections(Identity ownerIdentity) throws Exception {
    relationshipManager = getRelationshipManager();
    return relationshipManager.findRelationships(ownerIdentity, Relationship.Type.CONFIRM);
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
  public List<Identity> getIdentities(String providerId, boolean loadProfile) {
    IdentityProvider<?> ip = getIdentityProvider(providerId);
    List<String> userids = ip.getAllUserId();
    List<Identity> ids = new ArrayList<Identity>();

    for (String userId : userids) {
      ids.add(this.getOrCreateIdentity(providerId, userId, loadProfile));
    }
    return ids;
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIdentitiesByProfileFilter(String providerId, ProfileFilter profileFilter) throws Exception {
    return identityStorage.getIdentitiesByProfileFilter(providerId, profileFilter, 0, 20);
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIdentitiesByProfileFilter(String providerId,
                                                     ProfileFilter profileFilter,
                                                     long offset,
                                                     long limit) throws Exception {
    return identityStorage.getIdentitiesByProfileFilter(providerId, profileFilter, offset, limit);
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIdentitiesByProfileFilter(ProfileFilter profileFilter) throws Exception {
    return getIdentitiesByProfileFilter(null, profileFilter, 0, 20);
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIdentitiesByProfileFilter(ProfileFilter profileFilter,
                                                     long offset,
                                                     long limit) throws Exception {
    return getIdentitiesByProfileFilter(null, profileFilter, offset, limit);
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIdentitiesFilterByAlphaBet(String providerId, ProfileFilter profileFilter) throws Exception {
    return identityStorage.getIdentitiesFilterByAlphaBet(providerId,
                                                         profileFilter,
                                                         0,
                                                         IdentityManager.SEARCH_LIMIT);
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIdentitiesFilterByAlphaBet(String providerId,
                                                      ProfileFilter profileFilter,
                                                      long offset,
                                                      long limit) throws Exception {
    return identityStorage.getIdentitiesFilterByAlphaBet(providerId, profileFilter, offset, limit);
  }

  /**
   * {@inheritDoc}
   */
  public List<Identity> getIdentitiesFilterByAlphaBet(ProfileFilter profileFilter) throws Exception {
    return getIdentitiesFilterByAlphaBet(null, profileFilter);
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
  // FIXME: Make it clear here when id=uuid vs id=providerId:remoteId vs id=providerId:uuid
  public Identity getIdentity(String id, boolean loadProfile) {
    Identity returnIdentity = null;
    // attempts to match a global id in the form "providerId:remoteId"
    if (GlobalId.isValid(id)) {
      GlobalId globalId = new GlobalId(id);
      String providerId = globalId.getDomain();
      String remoteId = globalId.getLocalId();
      returnIdentity = getOrCreateIdentity(providerId, remoteId, loadProfile);
      if (returnIdentity != null) {
        return returnIdentity;
      } else {
        // retry with providerId:nodeId
        String tempId = globalId.getLocalId();
        returnIdentity = this.getIdentityStorage().findIdentityById(tempId);
        if (returnIdentity != null) {
          id = tempId;
        }
      }
    } else {
      returnIdentity = this.getIdentityStorage().findIdentityById(id);
    }
    if (returnIdentity != null) {
      if (loadProfile) {
        this.getIdentityStorage().loadProfile(returnIdentity.getProfile());
      }
    }
    if (returnIdentity == null) {
      LOG.info("Can not get identity with id: " + id);
    }
    return returnIdentity;
  }

  /**
   * {@inheritDoc}
   */
  public Identity getIdentity(String providerId, String remoteId, boolean loadProfile) {
    Identity returnIdentity = null;
    IdentityProvider<?> identityProvider = getIdentityProvider(providerId);
    returnIdentity = identityProvider.getIdentityByRemoteId(remoteId);
    if (returnIdentity != null) {
      Identity storedIdentity = this.getIdentityStorage().findIdentity(providerId, remoteId);
      if (storedIdentity != null) {
        returnIdentity.setId(storedIdentity.getId());
        if (loadProfile) {
          this.getIdentityStorage().loadProfile(returnIdentity.getProfile());
        }
      } else {
        // save new identity
        this.getIdentityStorage().saveIdentity(returnIdentity);
      }
    }
    return returnIdentity;
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
  //TODO improve the performance by specifying what needs to be loaded
  public Identity getOrCreateIdentity(String providerId, String remoteId, boolean loadProfile) {
    Identity returnIdentity = null;
    IdentityProvider<?> identityProvider = this.getIdentityProvider(providerId);

    Identity identity1 = identityProvider.getIdentityByRemoteId(remoteId);
    Identity result = this.getIdentityStorage().findIdentity(providerId, remoteId);
    if (result == null) {
      if (identity1 != null) {
        // identity is valid for provider, but no yet
        // referenced in storage
        saveIdentity(identity1);
        this.getIdentityStorage().saveProfile(identity1.getProfile());
        result = identity1;
      } else {
        // Not found in provider, so return null
        return result;
      }
    } else {
      if (identity1 == null) {
        // in the case: identity is stored but identity is not found from
        // provider, delete that identity
        this.getIdentityStorage().deleteIdentity(result);
        return null;
      }
      if (loadProfile) {
        this.getIdentityStorage().loadProfile(result.getProfile());
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
  //TODO make easier api, this is not good.
  public void updateAvatar(Profile p) {
    this.saveProfile(p);
    this.profileLifeCycle.avatarUpdated(p.getIdentity().getRemoteId(), p);
  }

  /**
   * {@inheritDoc}
   */
  public Profile updateBasicInfo(Profile p) throws Exception {
    Profile newProfile = addOrModifyProfileProperties(p);
    profileLifeCycle.basicUpdated(newProfile.getIdentity().getRemoteId(), newProfile);
    LOG.debug("Update basic infomation successfully for user: " + newProfile);
    return newProfile;
  }

  /**
   * {@inheritDoc}
   */
  public void updateContactSection(Profile p) throws Exception {
    Profile newProfile = addOrModifyProfileProperties(p);
    profileLifeCycle.contactUpdated(newProfile.getIdentity().getRemoteId(), newProfile);
    LOG.debug("Update contact section successfully for user: " + newProfile);
  }

  /**
   * {@inheritDoc}
   */
  public void updateExperienceSection(Profile p) throws Exception {
    Profile newProfile = addOrModifyProfileProperties(p);
    profileLifeCycle.experienceUpdated(p.getIdentity().getRemoteId(), newProfile);
    LOG.debug("Update experience section successfully for user: " + newProfile);
  }

  /**
   * {@inheritDoc}
   */
  public void updateHeaderSection(Profile p) throws Exception {
    Profile newProfile = addOrModifyProfileProperties(p);
    profileLifeCycle.headerUpdated(p.getIdentity().getRemoteId(), newProfile);
    LOG.debug("Update header section successfully for user: " + newProfile);
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
  public Profile addOrModifyProfileProperties(Profile profile) throws Exception {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public Profile addOrModifyProfilePropertiesCache(Profile profile) {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public Identity getCachedProfile(Identity identity) {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  public IdentityStorage getStorage() {
    return this.identityStorage;
  }
}