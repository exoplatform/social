/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.search.Sorting.SortBy;
import org.exoplatform.social.core.storage.api.IdentityStorage;

/**
 * ListAccess is used in loading identity with the input profile filter.
 * With this list we can manage the size of returned list by offset and limit.
 * 
 * @author <a href="http://hanhvq@gmail.com">hanhvq (gmail dot com)</a>
 * @since 1.2.0-GA
 */
public class ProfileFilterListAccess implements ListAccess<Identity> {
  private static final char EMPTY_CHARACTER = '\u0000';
  private IdentityStorage identityStorage; 
  private ProfileFilter profileFilter;
  
  /**
   * The id of provider.
   */
  String providerId;
  
  /**
   * Force to load profile or not.
   */
  boolean forceLoadProfile;
  
  /** The type */
  Type type;
  
  /**
   * The Profile list access Type Enum.
   */
  public enum Type {
    /** Gets all of Profiles. */
    ALL,
    /** Gets Profile by the first character of Name. */
    BY_FIRST_CHARACTER_OF_NAME,
    /** Gets Profile for Mention feature. */
    MENTION,
    /** Provides Unified Search Profile. */
    UNIFIED_SEARCH
  }
  
  /**
   * Constructor.
   * 
   * @param identityStorage 
   * @param providerId Id of provider.
   * @param profileFilter Filter object as extract's condition.
   * @param forceLoadProfile True then force to load profile.
   */
  public ProfileFilterListAccess(IdentityStorage identityStorage, String providerId, ProfileFilter profileFilter,
                                 boolean forceLoadProfile) {
    this.identityStorage = identityStorage;
    this.profileFilter = profileFilter;
    this.providerId = providerId;
    this.forceLoadProfile = forceLoadProfile;
  }
  
  /**
   * Constructor.
   * 
   * @param identityStorage The identity storage 
   * @param providerId Id of provider.
   * @param profileFilter Filter object as extract's condition.
   * @param forceLoadProfile True then force to load profile.
   * @param type Type of which list provide for
   */
  public ProfileFilterListAccess(IdentityStorage identityStorage, String providerId, ProfileFilter profileFilter,
                                 boolean forceLoadProfile, Type type) {
    this.identityStorage = identityStorage;
    this.profileFilter = profileFilter;
    this.providerId = providerId;
    this.forceLoadProfile = forceLoadProfile;
    this.type = type;
  }

  /**
   * Gets provider id.
   * @return
   */
  public String getProviderId() {
    return providerId;
  }

  /**
   * Sets provider id.
   * @param providerId
   */
  public void setProviderId(String providerId) {
    this.providerId = providerId;
  }

  /**
   * {@inheritDoc}
   */
  public Identity[] load(int offset, int limit) throws Exception, IllegalArgumentException {
    int usedLimit = limit;
    if (profileFilter != null && profileFilter.getViewerIdentity() != null) {
      // If viewer is added in filter, he shouldn't be retured in results
      // Thus, we get the results + 1 in order to delete the identity
      // of viewer if retrieved
      usedLimit++;
    }
    List<? extends Identity> identities = new ArrayList<>();
    //
    if (type != null) {
      switch(type) {
      case UNIFIED_SEARCH:
        identities = identityStorage.getIdentitiesForUnifiedSearch(providerId, profileFilter, offset, usedLimit);
        break;
      default: 
          break;
      }
    } else if (profileFilter == null || profileFilter.isEmpty()) {
      if (profileFilter == null || profileFilter.getViewerIdentity() == null) {
        if (profileFilter == null) {
          identities = identityStorage.getIdentities(providerId,
                                                     offset,
                                                     usedLimit);
        } else {
          String firstCharFieldName = profileFilter.getFirstCharFieldName();
          char firstCharacter = profileFilter.getFirstCharacterOfName();
          Sorting sorting = profileFilter.getSorting();
          String sortFieldName = sorting == null || sorting.sortBy == null ? null : sorting.sortBy.getFieldName();
          String sortDirection = sorting == null || sorting.sortBy == null ? null : sorting.orderBy.name();
          identities = identityStorage.getIdentities(providerId,
                                                     firstCharFieldName,
                                                     firstCharacter,
                                                     sortFieldName,
                                                     sortDirection,
                                                     offset,
                                                     usedLimit);
        }
      } else {
        String firstCharFieldName = profileFilter.getFirstCharFieldName();
        char firstCharacter = profileFilter.getFirstCharacterOfName();
        Sorting sorting = profileFilter.getSorting();
        String sortFieldName = sorting == null || sorting.sortBy == null ? null : sorting.sortBy.getFieldName();
        String sortDirection = sorting == null || sorting.sortBy == null ? null : sorting.orderBy.name();
        identities = identityStorage.getIdentitiesWithRelationships(profileFilter.getViewerIdentity().getId(),
                                                                    firstCharFieldName,
                                                                    firstCharacter,
                                                                    sortFieldName,
                                                                    sortDirection,
                                                                    offset,
                                                                    usedLimit);
      }
    } else {
      identities = identityStorage.getIdentitiesForMentions(providerId, profileFilter, null, offset,
                                                            usedLimit, forceLoadProfile);
    }
    if (profileFilter != null && profileFilter.getViewerIdentity() != null) {
      Iterator<? extends Identity> iterator = identities.iterator();
      while (iterator.hasNext()) {
        Identity identity = (Identity) iterator.next();
        if (identity.equals(profileFilter.getViewerIdentity())) {
          iterator.remove();
        }
      }
      // Remove last element if the used limit to request data from DB
      // is incremented by 1
      if (identities.size() > limit) {
        identities.remove(identities.size() -1);
      }
    }
    return identities.toArray(new Identity[0]);
  }

  /**
   * {@inheritDoc}
   */
  public int getSize() throws Exception {
    int size = 0; 
    if (profileFilter.getFirstCharacterOfName() != EMPTY_CHARACTER) {
      size = identityStorage.getIdentitiesByFirstCharacterOfNameCount(providerId, profileFilter);
    } else if (profileFilter.isEmpty()) {
      if(profileFilter.getViewerIdentity() == null) {
        size = identityStorage.getIdentitiesByProfileFilterCount(providerId, profileFilter);
      } else {
        size = identityStorage.countIdentitiesWithRelationships(profileFilter.getViewerIdentity().getId());
        // Remove Count of viewer identity
        size--;
      }
    } else {
      size = identityStorage.getIdentitiesForMentionsCount(providerId, profileFilter, null);
    }

    return size;
  }
}
