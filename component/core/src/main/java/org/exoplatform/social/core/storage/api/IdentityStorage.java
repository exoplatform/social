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

package org.exoplatform.social.core.storage.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.exoplatform.social.core.identity.SpaceMemberFilterListAccess.Type;
import org.exoplatform.social.core.identity.model.ActiveIdentityFilter;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.IdentityWithRelationship;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.model.Profile.AttachedActivityType;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.IdentityStorageException;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public interface IdentityStorage {

  /**
   * Saves identity.
   *
   * @param identity the identity
   * @throws IdentityStorageException
   */
  public void saveIdentity(final Identity identity) throws IdentityStorageException;

  /**
   * Updates existing identity's properties.
   *
   * @param identity the identity to be updated.
   * @return the updated identity.
   * @throws IdentityStorageException
   * @since  1.2.0-GA
   */
  public Identity updateIdentity(final Identity identity) throws IdentityStorageException;
  
  /**
   * Updates existing identity's membership in OrganizationService.
   *
   * @param remoteId the remoteId to be updated membership.
   * @throws IdentityStorageException
   * @since  4.0.0
   */
  public void updateIdentityMembership(final String remoteId) throws IdentityStorageException;

  /**
   * Gets the identity by his id.
   *
   * @param nodeId the id of identity
   * @return the identity
   * @throws IdentityStorageException
   */
  public Identity findIdentityById(final String nodeId) throws IdentityStorageException;

  /**
   * Deletes an identity
   *
   * @param identity
   * @throws IdentityStorageException
   */
  public void deleteIdentity(final Identity identity) throws IdentityStorageException;

  /**
   * Hard delete an identity
   *
   * @param identity
   * @throws IdentityStorageException
   */
  public void hardDeleteIdentity(final Identity identity) throws IdentityStorageException;

  /**
   * Load profile.
   *
   * @param profile the profile
   * @throws IdentityStorageException
   */
  public Profile loadProfile(Profile profile) throws IdentityStorageException;

  /**
   * Gets the identity by remote id.
   *
   * @param providerId the identity provider
   * @param remoteId   the id
   * @return the identity by remote id
   * @throws IdentityStorageException
   */
  public Identity findIdentity(final String providerId, final String remoteId) throws IdentityStorageException;

  /**
   * Saves profile.
   *
   * @param profile the profile
   * @throws IdentityStorageException
   */
  public void saveProfile(final Profile profile) throws IdentityStorageException;

  /**
   * Updates profile.
   *
   * @param profile the profile
   * @throws IdentityStorageException
   * @since 1.2.0-GA
   */
  public void updateProfile(final Profile profile) throws IdentityStorageException;

  /**
   * Gets total number of identities in storage depend on providerId.
   * @throws IdentityStorageException
   */
  public int getIdentitiesCount (final String providerId) throws IdentityStorageException;

  /**
   * Gets the identities by profile filter.
   *
   * @param providerId Id of provider.
   * @param profileFilter    Information of profile that used in filtering.
   * @param offset           Start index of list to be get.
   * @param limit            End index of list to be get.
   * @param forceLoadOrReloadProfile Load profile or not.
   * @return the identities by profile filter.
   * @throws IdentityStorageException
   * @since 1.2.0-GA
   */
  public List<Identity> getIdentitiesByProfileFilter(
      final String providerId, final ProfileFilter profileFilter, long offset, long limit,
      boolean forceLoadOrReloadProfile)
      throws IdentityStorageException;
  
  /**
   * Gets the identities by profile filter.
   *
   * @param providerId Id of provider.
   * @param profileFilter    Information of profile that used in filtering.
   * @param offset           Start index of list to be get.
   * @param limit            End index of list to be get.
   * @param forceLoadOrReloadProfile Load profile or not.
   * @return the identities by profile filter.
   * @throws IdentityStorageException
   * @since 4.0.0-Alpha1
   */
  public List<Identity> getIdentitiesForMentions(final String providerId,
                                                 final ProfileFilter profileFilter,
                                                 org.exoplatform.social.core.relationship.model.Relationship.Type type,
                                                 long offset,
                                                 long limit,
                                                 boolean forceLoadOrReloadProfile) throws IdentityStorageException;
  
  /**
   * Gets the count identities by profile filter.
   *
   * @param providerId Id of provider.
   * @param profileFilter    Information of profile that used in filtering.
   * @return the number of filtered identities
   * @throws IdentityStorageException
   * @since 4.4.0
   */
  public int getIdentitiesForMentionsCount(final String providerId,
                                           final ProfileFilter profileFilter,
                                           org.exoplatform.social.core.relationship.model.Relationship.Type type) throws IdentityStorageException;
  
  /**
   * Gets the identities for Unified Search.
   *
   * @param providerId Id of provider.
   * @param profileFilter    Information of profile that used in filtering.
   * @param offset           Start index of list to be get.
   * @param limit            End index of list to be get.
   * @return the identities
   * @throws IdentityStorageException
   * @since 4.0.x
   */
  public List<Identity> getIdentitiesForUnifiedSearch(final String providerId,
                                                      final ProfileFilter profileFilter,
                                                      long offset, long limit) throws IdentityStorageException;

  /**
   * Counts the number of identity by profile filter.
   *
   * @param providerId Id of Provider.
   * @param profileFilter Information of profile are used in filtering.
   * @return Number of identities that are filtered by profile.
   * @throws IdentityStorageException
   * @since 1.2.0-GA
   */
  public int getIdentitiesByProfileFilterCount(final String providerId, final ProfileFilter profileFilter)
      throws IdentityStorageException;

  /**
   * Counts the number of identities that match the first character of name.
   *
   * @param providerId
   * @param profileFilter Profile filter object.
   * @return Number of identities that start with the first character of name.
   * @throws IdentityStorageException
   * @since 1.2.0-GA
   */
  public int getIdentitiesByFirstCharacterOfNameCount(final String providerId, final ProfileFilter profileFilter)
      throws IdentityStorageException;

  /**
   * Gets the identities that match the first character of name.
   *
   * @param providerId Id of provider.
   * @param profileFilter Profile filter object.
   * @param offset   Start index of list to be get.
   * @param limit    End index of list to be get.
   * @param forceLoadOrReloadProfile Load profile or not.
   * @return Identities that have name start with the first character.
   * @throws IdentityStorageException
   * @deprecated use method getIdentities or getIdentitiesForUnifiedSearch instead
   * @since 1.2.0-GA
   */
  @Deprecated
  public List<Identity> getIdentitiesByFirstCharacterOfName(final String providerId, final ProfileFilter profileFilter,
      long offset, long limit, boolean forceLoadOrReloadProfile) throws IdentityStorageException;

  /**
   * Gets the type.
   *
   * @param nodetype the nodetype
   * @param property the property
   * @return the type
   * @throws IdentityStorageException
   */
  public String getType(final String nodetype, final String property);

  /**
   * Add or modify properties of profile and persist to JCR. Profile parameter is a lightweight that
   * contains only the property that you want to add or modify. NOTE: The method will
   * not delete the properties on old profile when the param profile have not those keys.
   *
   * @param profile
   * @throws IdentityStorageException
   */
  public void addOrModifyProfileProperties(final Profile profile) throws IdentityStorageException;
  
  /**
   * get Space's member Identity and filter it by Profile Filter
   * @param space
   * @param profileFilter
   * @param offset
   * @param limit
   * @return
   * @throws IdentityStorageException
   */
  public List<Identity> getSpaceMemberIdentitiesByProfileFilter(final Space space, 
                                                                final ProfileFilter profileFilter,
                                                                Type type,
                                                                long offset, long limit)
                                                                throws IdentityStorageException;
  
  /**
   * Updates profile activity id by type.
   * 
   * @param identity
   * @param activityId
   * @param type Type of activity id to get.
   * @since 4.0.0.Alpha1
   */
  public void updateProfileActivityId(Identity identity, String activityId, AttachedActivityType type);
  
  /**
   * Gets profile activity id by type.
   * 
   * @param profile
   * @param type Type of activity id to get.
   * @return Profile activity id.
   * @since 4.0.0.Alpha1
   */
  public String getProfileActivityId(Profile profile, AttachedActivityType type);
  
  /**
   * Gets the active user list base on the given ActiveIdentityFilter.
   * 1. N days who last login less than N days.
   * 2. UserGroup who belongs to this group.
   * 
   * @param filter
   * @return 
   * @since 4.1.0
   */
  public Set<String> getActiveUsers(ActiveIdentityFilter filter);
  
  /**
   * Process enable/disable Identity
   * 
   * @param identity The Identity enable
   * @param isEnable true if the user is enable, false if not
   * @since 4.2.x
   */
  public void processEnabledIdentity(Identity identity, boolean isEnable);

  /**
   * Gets all identities from the store with the relationship that they have with current user identity.
   *
   * @param identityId user viewer identity id.
   * @param offset   Start index of list to be get.
   * @param limit    End index of list to be get.
   * @return Identities that have name start with the first character.
   * @since 4.4.0
   */
  List<IdentityWithRelationship> getIdentitiesWithRelationships(String identityId, int offset, int limit);

  /**
   * Gets all identities from the store with the relationship that they have with current user identity.
   * The firstChar parameter will be used to filter on identities fullname|lastname|firstname first character.
   * 
   * @param identityId
   * @param firstCharFieldName
   * @param firstChar
   * @param sortFieldName 
   * @param sortDirection 
   * @param offset
   * @param limit
   * @return
   */
  default List<IdentityWithRelationship> getIdentitiesWithRelationships(String identityId, String firstCharFieldName, char firstChar, String sortFieldName, String sortDirection, int offset, int limit) {
    throw new UnsupportedOperationException("This operation is not supported using current implementation of service IdentityStorage");
  }

  /**
   * Counts the number of identities
   *
   * @param identityId Id of Identity.
   * @return Number of identities.
   * @throws Exception
   * @since 4.4.0
   */
  int countIdentitiesWithRelationships(String identityId) throws Exception;
  
  /**
   *  Gets a the avatar stream for a given identity
   *
   * @param identity
   * @return
   */
  InputStream getAvatarInputStreamById(Identity identity) throws IOException;

  /**
   *  Gets a the avatar stream for a given identity
   *
   * @param identity
   * @return
   */
  InputStream getBannerInputStreamById(Identity identity) throws IOException;

  /**
   * count Space's members by status and filter it by Profile Filter
   * 
   * @param space
   * @param profileFilter
   * @param type
   * @return
   */
  public int countSpaceMemberIdentitiesByProfileFilter(Space space, ProfileFilter profileFilter, Type type);

  /**
   * Get list of identities by providerId
   * 
   * @param providerId
   * @param offset
   * @param limit
   * @return
   */
  default public List<Identity> getIdentities(String providerId, long offset, long limit) {
    throw new UnsupportedOperationException("This operation is not supported using current implementation of service IdentityStorage");
  }

  /**
   * Get list of identities by providerId
   * The firstChar parameter will be used to filter on identities fullname/firstname/lastname first character.
   * 
   * @param providerId
   * @param firstCharacterFieldName
   * @param firstCharacter
   * @param sortField
   * @param sortDirection
   * @param offset
   * @param limit
   * @return
   */
  default public List<Identity> getIdentities(String providerId, String firstCharacterFieldName, char firstCharacter, String sortField, String sortDirection, long offset, long limit) {
    throw new UnsupportedOperationException("This operation is not supported using current implementation of service IdentityStorage");
  }

  /**
   * Sorts a list of user identities using a field
   * 
   * @param identityRemoteIds
   * @param sortField
   * @return
   */
  public List<String> sortIdentities(List<String> identityRemoteIds, String sortField);

}
