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

package org.exoplatform.social.core.storage.cache.model.key;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.profile.ProfileFilter;

/**
 * Immutable identity key.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SpaceMemberIdentitiesKey extends MultitenantCacheKey {

  /**
   * 
   */
  private static final long serialVersionUID = 3738676181647045501L;


  private final String spaceId;


  private final ProfileFilter profileFilter;
  
  private final int limit;
  private final int offset;

  public SpaceMemberIdentitiesKey(final String spaceId,final ProfileFilter profileFilter,final int limit, final int offset) {
    this.spaceId = spaceId;
    this.profileFilter = profileFilter;
    this.limit = limit;
    this.offset = offset;
  }

  public String getSpaceId() {
    return spaceId;
  }
  
  public ProfileFilter getProfileFilter() {
    return profileFilter;
  }

  public int getLimit() {
    return limit;
  }

  public int getOffset() {
    return offset;
  }
  
  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SpaceMemberIdentitiesKey)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    SpaceMemberIdentitiesKey that = (SpaceMemberIdentitiesKey) o;

    if ((spaceId == null && that.spaceId == null) || spaceId.equals(that.spaceId) &&
        (limit == that.limit) &&
        (offset == that.offset) &&
        (profileFilter == null && that.profileFilter == null) &&
        (profileFilter.getCompany() == null && that.profileFilter.getCompany() == null) || profileFilter.getCompany().equals(that.profileFilter.getCompany()) &&
        (profileFilter.getExcludedIdentityList() == null && that.profileFilter.getExcludedIdentityList() == null) || profileFilter.getExcludedIdentityList().equals(that.profileFilter.getExcludedIdentityList()) &&
        (profileFilter.getFirstCharacterOfName() == that.profileFilter.getFirstCharacterOfName()) &&
        (profileFilter.getGender() == null && that.profileFilter.getGender() == null) || profileFilter.getGender().equals(that.profileFilter.getGender()) &&
        (profileFilter.getName() == null && that.profileFilter.getName() == null) || profileFilter.getName().equals(that.profileFilter.getName()) &&
        (profileFilter.getPosition() == null && that.profileFilter.getPosition() == null) || profileFilter.getPosition().equals(that.profileFilter.getPosition()) &&
        (profileFilter.getSkills() == null && that.profileFilter.getSkills() == null) || profileFilter.getSkills().equals(that.profileFilter.getSkills()) &&
        (profileFilter.getSkills() == null && that.profileFilter.getSkills() == null) || profileFilter.getSkills().equals(that.profileFilter.getSkills())) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 293 * result + (spaceId != null ? spaceId.hashCode() : 0) + (profileFilter != null ? profileFilter.hashCode() : 0) + limit + offset;
    return result;
  }

}
