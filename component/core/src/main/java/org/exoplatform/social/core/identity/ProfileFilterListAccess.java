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
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.common.ListAccessValidator;
import org.exoplatform.social.common.jcr.Util;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.profile.ProfileFilter;
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
    ListAccessValidator.validateIndex(offset, limit, getSize());
    List<Identity> identities = new ArrayList<Identity>();
    if (profileFilter.getFirstCharacterOfName() != EMPTY_CHARACTER) {
      identities = identityStorage.getIdentitiesByFirstCharacterOfName(providerId, profileFilter, offset,
                                                                       limit, forceLoadProfile);
    } else {
      identities = identityStorage.getIdentitiesByProfileFilter(providerId, profileFilter, offset,
                                                                limit, forceLoadProfile);
    }
    
    return Util.convertListToArray(identities, Identity.class);
  }

  /**
   * {@inheritDoc}
   */
  public int getSize() throws Exception {
    int size = 0; 
    if (profileFilter.getFirstCharacterOfName() != EMPTY_CHARACTER) {
      size = identityStorage.getIdentitiesByFirstCharacterOfNameCount(providerId, profileFilter);
    } else {
      size = identityStorage.getIdentitiesByProfileFilterCount(providerId, profileFilter);
    }
    
    return size;
  }
}
