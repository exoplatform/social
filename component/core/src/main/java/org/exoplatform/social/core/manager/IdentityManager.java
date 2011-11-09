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

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.IdentityProviderPlugin;
import org.exoplatform.social.core.identity.SpaceMemberFilterListAccess.Type;
import org.exoplatform.social.core.identity.model.GlobalId;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.profile.ProfileListener;
import org.exoplatform.social.core.profile.ProfileListenerPlugin;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.api.IdentityStorage;

/**
 * The Interface IdentityManager.
 *
 * @author <a href="mailto:vien_levan@exoplatform.com">vien_levan</a>
 * @author <a href="http://hoatle.net">hoatle</a>
 */
public interface IdentityManager {

  /**
   * Gets or creates an Identity object provided by a identity provider.
   *
   * @param providerId               the name of identity provider name id.
   * @param remoteId                 the identifier that identifies the identity in the specific identity provider.
   * @param forceLoadOrReloadProfile force load or reload profile
   * @return the Identity object provided from identity provider.
   */
  Identity getOrCreateIdentity(String providerId, String remoteId, boolean forceLoadOrReloadProfile);

  /**
   * Gets the stored identity by its identity id, this id is its uuid stored by JCR.
   *
   * @param identityId               the stored JCR uuid.
   * @param forceLoadOrReloadProfile force load or reload profile
   * @return the found identity object
   */
  Identity getIdentity(String identityId, boolean forceLoadOrReloadProfile);

  /**
   * Updates existing identity's properties.
   *
   * @param identity the identity to be updated.
   * @return the updated identity.
   * @since  1.2.0-GA
   */
  Identity updateIdentity(Identity identity);

  /**
   * Deletes an existing identity.
   *
   * @param identity the existing identity.
   */
  void deleteIdentity(Identity identity);

  /**
   * Hard deletes an existing identity.
   *
   * @param identity the existing identity.
   */
  void hardDeleteIdentity(Identity identity);

  /**
   * Gets identity list access which contains all the identities connected with the provided identity.
   *
   * @param identity the provided identity
   * @return the identity list access
   * @since  1.2.0-GA
   */
  ListAccess<Identity> getConnectionsWithListAccess(Identity identity);

  /**
   * Gets a profile associated with a provided identity.
   *
   * @param identity the provided identity
   * @return the profile associated with the provided identity
   * @since  1.2.0-GA
   */
  Profile getProfile(Identity identity);

  /**
   * Updates an existing profile.
   *
   * @param existingProfile the existing profile
   * @since  1.2.0-GA
   */
  void updateProfile(Profile existingProfile);

  /**
   * Gets identity list access which contains all the identities from a provided provider which are filtered by profile
   * filter.
   *
   * @param providerId               the provider name id
   * @param profileFilter            the profile filter
   * @param forceLoadOrReloadProfile force load or reload profile or not
   * @return the identity list access
   * @since 1.2.0-GA
   */
  ListAccess<Identity> getIdentitiesByProfileFilter(String providerId, ProfileFilter profileFilter,
                                                  boolean forceLoadOrReloadProfile);
  
  ListAccess<Identity> getSpaceIdentityByProfileFilter(Space space, ProfileFilter profileFilter, Type type,
                                                       boolean forceLoadorReloadProfile);
  
  /**
   * Adds an identity provider to identity manager.
   *
   * @param identityProvider an identity provider
   */
  void addIdentityProvider(IdentityProvider<?> identityProvider);

  /**
   * Remove an existing identity provider.
   *
   * @param identityProvider the existing identity provider
   * @since 1.2.0-GA
   */
  void removeIdentityProvider(IdentityProvider<?> identityProvider);

  /**
   * Registers a profile listener plugin by external compnent plugin mechanism.
   *
   * For example:
   * <pre>
   *  &lt;external-component-plugins&gt;
   *    &lt;target-component&gt;org.exoplatform.social.core.manager.IdentityManager&lt;/target-component&gt;
   *    &lt;component-plugin&gt;
   *      &lt;name&gt;ProfileUpdatesPublisher&lt;/name&gt;
   *      &lt;set-method&gt;registerProfileListener&lt;/set-method&gt;
   *      &lt;type&gt;org.exoplatform.social.core.application.ProfileUpdatesPublisher&lt;/type&gt;
   *    &lt;/component-plugin&gt;
   *  &lt;/external-component-plugins&gt;
   * </pre>
   *
   * @param profileListenerPlugin a profile listener plugin
   * @since 1.2.0-GA
   */
  void registerProfileListener(ProfileListenerPlugin profileListenerPlugin);

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
   * @deprecated Use {@link #getIdentity(String, boolean)} instead.
   *             Will be removed by 1.3.x
   */
  Identity getIdentity(String id);

  /**
   * Gets the identity by remote id.
   *
   * @param providerId the provider id
   * @param remoteId the remote id
   * @return the identity
   * @deprecated Use {@link #getOrCreateIdentity(String, String, boolean)} instead.
   *             Will be moved by 1.3.x
   */
  Identity getOrCreateIdentity(String providerId, String remoteId);

  /**
   * Gets the identities by profile filter.
   *
   * @param providerId the provider id
   * @param profileFilter the profile filter
   * @return the identities by profile filter
   * @throws Exception the exception
   * @deprecated Use {@link #getIdentitiesByProfileFilter(String, org.exoplatform.social.core.profile.ProfileFilter,
   * boolean)} instead. Will be removed by 1.3.x
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
   * @deprecated Use {@link #getIdentitiesByProfileFilter(String, org.exoplatform.social.core.profile.ProfileFilter,
   * boolean)} instead. Will be removed by 1.3.x
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
   * @deprecated Use {@link #getIdentitiesByProfileFilter(String, org.exoplatform.social.core.profile.ProfileFilter,
   * boolean)} instead. Will be removed by 1.3.x
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
   * @deprecated Use {@link #getIdentitiesByProfileFilter(String, org.exoplatform.social.core.profile.ProfileFilter,
   * boolean)} instead. Will be removed by 1.3.x
   */
  List<Identity> getIdentitiesByProfileFilter(ProfileFilter profileFilter, long offset, long limit) throws Exception;

  /**
   * Gets the identities filter by alpha bet.
   *
   * @param providerId the provider id
   * @param profileFilter the profile filter
   * @return the identities filter by alpha bet
   * @throws Exception the exception
   * @deprecated Use {@link #getIdentitiesByProfileFilter(String, org.exoplatform.social.core.profile.ProfileFilter,
   * boolean)} instead. Will be removed by 1.3.x
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
   * @deprecated Use {@link #getIdentitiesByProfileFilter(String, org.exoplatform.social.core.profile.ProfileFilter,
   * boolean)} instead. Will be removed by 1.3.x
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
   * @deprecated Use {@link #getIdentitiesByProfileFilter(String, org.exoplatform.social.core.profile.ProfileFilter,
   * boolean)} instead. Will be removed by 1.3.x
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
   * @deprecated Use {@link #getOrCreateIdentity(String, String, boolean)} instead.
   *             Will be removed by 1.3.x
   */
  Identity getIdentity(String providerId, String remoteId, boolean loadProfile);

  /**
   * Gets the number of indentities.
   * 
   * @return Number of identities.
   * @deprecated Will be removed by 1.3.x
   */
  long getIdentitiesCount(String providerId);
  
  /**
   * Checks if identity existed or not.
   *
   * @param providerId
   * @param remoteId
   * @return true or false
   * @deprecated Will be removed by 1.3.x
   */
  boolean identityExisted(String providerId, String remoteId);

  /**
   * Saves identity.
   *
   * @param identity the identity
   * @deprecated Use {@link #getOrCreateIdentity(String, String, boolean)} instead.
   *             Will be removed by 1.3.x
   */
  void saveIdentity(Identity identity);

  /**
   * Saves a profile.
   *
   * @param profile
   * @deprecated Use {@link #updateProfile(org.exoplatform.social.core.identity.model.Profile)}  instead.
   *             Will be removed by 1.3.x
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
   * @deprecated Will be removed by 1.3.x
   */
  void addOrModifyProfileProperties(Profile profile) throws Exception;

  /**
   * Updates avatar.
   *
   * @param p
   * @throws Exception
   * @deprecated Will be removed by 1.3.x
   */
  void updateAvatar(Profile p);

  /**
   * Updates basic info.
   *
   * @param p
   * @throws Exception
   * @deprecated Will be removed by 1.3.x
   */
  void updateBasicInfo(Profile p) throws Exception;

  /**
   * Updates the contact section of profile.
   *
   * @param p
   * @throws Exception
   * @deprecated Will be removed by 1.3.x
   */
  void updateContactSection(Profile p) throws Exception;

  /**
   * Updates the experience section of profile.
   *
   * @param p
   * @throws Exception
   * @deprecated Will be removed by 1.3.x
   */
  void updateExperienceSection(Profile p) throws Exception;

  /**
   * Updates the header section of profile.
   *
   * @param p
   * @throws Exception
   * @deprecated Will be removed by 1.3.x
   */
  void updateHeaderSection(Profile p) throws Exception;

  /**
   * Gets the identities.
   *
   * @param providerId the provider id
   * @return the identities
   * @throws Exception the exception
   * @deprecated Use {@link #getIdentities(String, boolean)} instead.
   *             Will be removed by 1.3.x
   */
  List<Identity> getIdentities(String providerId) throws Exception;

  /**
   * Gets all the identities from a providerId.
   *
   * @param providerId the provider id
   * @param loadProfile the load profile
   * @return the identities
   * @deprecated Use {@link #getIdentities(String, boolean)} instead.
   *             Will be removed by 1.3.x
   */
  List<Identity> getIdentities(String providerId, boolean loadProfile) throws Exception;

  /**
   * Gets connections of an identity.
   *
   * @param ownerIdentity
   * @return list of identity
   * @throws Exception
   * @since 1.1.1
   * @deprecated Use {@link #getConnectionsWithListAccess(org.exoplatform.social.core.identity.model.Identity)}
   *             instead. Will be removed by 1.3.x
   */
  List<Identity> getConnections(Identity ownerIdentity) throws Exception;

  /**
   * Gets the identity activityStorage.
   *
   * @return the activityStorage
   * @deprecated Will be removed by 1.3.x
   */
  IdentityStorage getIdentityStorage();

  /**
   * Gets identityStorage.
   *
   * @return identityStorage
   * @deprecated Will be removed by 1.3.x
   */
  IdentityStorage getStorage();

  /**
   * Registers the profile listener.
   *
   * @param listener
   * @deprecated Will be removed by 1.3.x
   */
  void registerProfileListener(ProfileListener listener);

  /**
   * Unregisters the profile listener.
   *
   * @param listener
   * @deprecated Will be removed by 1.3.x
   */
  void unregisterProfileListener(ProfileListener listener);

  /**
   * Registers a profile listener component plugin.
   *
   * @param plugin
   * @deprecated Use {@link #registerProfileListener(org.exoplatform.social.core.profile.ProfileListenerPlugin)}
   *             instead. Will be removed by 1.3.x
   */
  void addProfileListener(ProfileListenerPlugin plugin);
}
