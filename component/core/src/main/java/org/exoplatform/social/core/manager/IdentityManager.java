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

import java.util.List;

import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.IdentityProviderPlugin;
import org.exoplatform.social.core.identity.model.GlobalId;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.profile.ProfileListener;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;
import org.exoplatform.social.core.storage.IdentityStorage;

/**
 * The Interface IdentityManager.
 *
 * @author <a href="mailto:vien_levan@exoplatform.com">vien_levan</a>
 */
public interface IdentityManager {
  /** The limit search list to be returned for matching search criteria */
  long SEARCH_LIMIT = 500;

  /**
   * Registers one or more {@link IdentityProvider} through an
   * {@link IdentityProviderPlugin}.
   *
   * @param plugin
   */
  void registerIdentityProviders(IdentityProviderPlugin plugin);

  /**
   * Gets the identity by id and also loads his profile.
   *
   * @param id ID can be a social {@link GlobalId} or a raw identity such as in
   *          {@link Identity#getId()}
   * @return null if nothing is found, or the Identity object
   * @see #getIdentity(String, boolean)
   */
  Identity getIdentity(String id);

  /**
   * Gets the identity by id optionally loading his profile Note: if id = uuid,
   * you get the info from JCR if found. if id = providerId:remoteId, you get
   * the info from provider and currently with this case, you get null for
   * identity.getId(). This can cause some troubles.
   *
   * @param id ID be a social {@link GlobalId} or a raw identity such as in
   *          {@link Identity#getId()}
   * @param loadProfile the load profile true if load and false if doesn't. when
   *          loadProfile is true, gets profile from JCR
   * @return null if nothing is found, or the Identity object
   */
  Identity getIdentity(String id, boolean loadProfile);

  /**
   * Deletes an identity.
   *
   * @param identity
   */
  void deleteIdentity(Identity identity);

  /**
   * Adds the identity provider.
   *
   * @param idProvider the id provider
   */
  void addIdentityProvider(IdentityProvider<?> idProvider);

  /**
   * Gets the identity by remote id.
   *
   * @param providerId the provider id
   * @param remoteId the remote id
   * @return the identity
   */
  Identity getOrCreateIdentity(String providerId, String remoteId);

  /**
   * This function returns an Identity object that specific to a special type.
   * <p>
   * For example if the type is Linked'In, the identifier will be the URL of the
   * profile or if it's a CS contact manager contact, it will be the UID of the
   * contact.
   * </p>
   * A new identity is created if it is found by provider, if not null will be
   * returned. If identity is found by provider and not stored, store it. If
   * stored, return it. If identity is not found by provider, return null. If
   * stored, delete that stored identity.
   *
   * @param providerId refering to the name of the Identity provider
   * @param remoteId the identifier that identify the identity in the specific
   *          identity provider
   * @param loadProfile true to load profile
   * @return null if nothing is found, or the Identity object
   */
  Identity getOrCreateIdentity(String providerId, String remoteId, boolean loadProfile);

  /**
   * Gets the identities by profile filter.
   *
   * @param providerId the provider id
   * @param profileFilter the profile filter
   * @return the identities by profile filter
   * @throws Exception the exception
   */
  List<Identity> getIdentitiesByProfileFilter(String providerId, ProfileFilter profileFilter) throws Exception;

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
  List<Identity> getIdentitiesByProfileFilter(String providerId,
                                              ProfileFilter profileFilter,
                                              long offset,
                                              long limit) throws Exception;

  /**
   * Gets the identities by profile filter.
   *
   * @param profileFilter the profile filter
   * @return the identities by profile filter
   * @throws Exception the exception
   */
  List<Identity> getIdentitiesByProfileFilter(ProfileFilter profileFilter) throws Exception;

  /**
   * Gets the identities by profile filter.
   *
   * @param profileFilter
   * @param offset
   * @param limit
   * @return the identities by profile filter
   * @throws Exception
   */
  List<Identity> getIdentitiesByProfileFilter(ProfileFilter profileFilter, long offset, long limit) throws Exception;

  /**
   * Gets the identities filter by alpha bet.
   *
   * @param providerId the provider id
   * @param profileFilter the profile filter
   * @return the identities filter by alpha bet
   * @throws Exception the exception
   */
  List<Identity> getIdentitiesFilterByAlphaBet(String providerId, ProfileFilter profileFilter) throws Exception;

  /**
   * Gets the identities fileter by alpha bet with offset and limit.
   *
   * @param providerId
   * @param profileFilter
   * @param offset
   * @param limit
   * @return the identitities list
   * @throws Exception
   */
  List<Identity> getIdentitiesFilterByAlphaBet(String providerId,
                                               ProfileFilter profileFilter,
                                               long offset,
                                               long limit) throws Exception;

  /**
   * Gets the identities filter by alpha bet.
   *
   * @param profileFilter the profile filter
   * @return the identities filter by alpha bet
   * @throws Exception the exception
   */
  List<Identity> getIdentitiesFilterByAlphaBet(ProfileFilter profileFilter) throws Exception;

  /**
   * Gets identity from the provider, not in JCR. To make sure to gets the info
   * from JCR, use {@link #getOrCreateIdentity(String, String, boolean)}.
   *
   * @param providerId
   * @param remoteId
   * @param loadProfile
   * @return identity
   */
  Identity getIdentity(String providerId, String remoteId, boolean loadProfile);

  /**
   * Checks if identity existed or not.
   *
   * @param providerId
   * @param remoteId
   * @return true or false
   */
  boolean identityExisted(String providerId, String remoteId);

  /**
   * Saves identity.
   *
   * @param identity the identity
   */
  void saveIdentity(Identity identity);

  /**
   * Saves a profile.
   *
   * @param profile
   */
  void saveProfile(Profile profile);

  /**
   * Adds or modifies properties of profile. Profile parameter is a lightweight
   * that contains only the property that you want to add or modify. NOTE: The
   * method will not delete the properties on old profile when the param profile
   * have not those keys.
   *
   * @param profile
   * @throws Exception
   */
  Profile addOrModifyProfileProperties(Profile profile) throws Exception;

  /**
   * Adds or modifies properties of profile in cache. Profile parameter is a
   * lightweight that contains only the property that you want to add or modify.
   * NOTE: The method will not delete the properties on old profile when the
   * param profile have not those keys.
   *
   * @param profile
   * @return
   */
  Profile addOrModifyProfilePropertiesCache(Profile profile);

  /**
   * Gets profile that was cached.
   *
   * @param identity
   * @return
   */
  Identity getCachedProfile(Identity identity);

  /**
   * Updates avatar.
   *
   * @param p
   * @throws Exception
   */
  void updateAvatar(Profile p);

  /**
   * Updates basic info.
   *
   * @param p
   * @throws Exception
   */
  Profile updateBasicInfo(Profile p) throws Exception;

  /**
   * Updates the contact section of profile.
   *
   * @param p
   * @throws Exception
   */
  void updateContactSection(Profile p) throws Exception;

  /**
   * Updates the experience section of profile.
   *
   * @param p
   * @throws Exception
   */
  void updateExperienceSection(Profile p) throws Exception;

  /**
   * Updates the header section of profile.
   *
   * @param p
   * @throws Exception
   */
  void updateHeaderSection(Profile p) throws Exception;

  /**
   * Gets the identities.
   *
   * @param providerId the provider id
   * @return the identities
   * @throws Exception the exception
   */
  List<Identity> getIdentities(String providerId) throws Exception;

  /**
   * Gets all the identities from a providerId.
   *
   * @param providerId the provider id
   * @param loadProfile the load profile
   * @return the identities
   */
  List<Identity> getIdentities(String providerId, boolean loadProfile);

  /**
   * Gets connections of an identity.
   *
   * @param ownerIdentity
   * @return list of identity
   * @throws Exception
   * @since 1.1.1
   */
  List<Identity> getConnections(Identity ownerIdentity) throws Exception;

  /**
   * Gets the identity storage.
   *
   * @return the storage
   */
  IdentityStorage getIdentityStorage();

  /**
   * Gets identityStorage.
   * @deprecated should use method getIdentityStorage()
   * @return identityStorage
   */
  IdentityStorage getStorage();

  /**
   * Registers the profile listener.
   *
   * @param listener
   */
  void registerProfileListener(ProfileListener listener);

  /**
   * Unregisters the profile listener.
   *
   * @param listener
   */
  void unregisterProfileListener(ProfileListener listener);

  /**
   * Registers a profile listener component plugin.
   *
   * @param plugin
   */
  void addProfileListener(ProfileListenerPlugin plugin);
}