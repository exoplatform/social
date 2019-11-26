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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.binary.StringUtils;

import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.search.Sorting;

/**
 * Immutable identity filter key.
 * This key is used to cache the search results.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class IdentityFilterKey implements CacheKey {

  private static final long serialVersionUID = 6711481249085725845L;

  private String providerId;
  private String remoteId;
  private String name;
  private String position;
  private String company;
  private String skills;
  private String firstCharFieldName;
  private char firstChar;
  private List<IdentityKey> excluded;
  private List<String> onlineRemoteIds;
  private String all;
  private Sorting sorting;
  
  /**
   * Constructor for case using remoteId as key.
   * @param providerId
   * @param remoteId
   * @param filter
   */
  public IdentityFilterKey(final String providerId, final String remoteId, final ProfileFilter filter) {
    this.remoteId = remoteId;
    this.providerId = providerId; 
    if (filter != null) {
      this.name = filter.getName();
      this.position = filter.getPosition();
      this.company = filter.getCompany();
      this.skills = filter.getSkills();
      this.firstChar = filter.getFirstCharacterOfName();
      this.firstCharFieldName = filter.getFirstCharFieldName();

      List<IdentityKey> keys = new ArrayList<IdentityKey>();
      for (Identity i : filter.getExcludedIdentityList()) {
        keys.add(new IdentityKey(i));
      }

      this.excluded = Collections.unmodifiableList(keys);
      
      this.onlineRemoteIds = Collections.unmodifiableList(filter.getOnlineRemoteIds());
      this.all = filter.getAll();
      this.sorting = filter.getSorting();
    }
  }

  public IdentityFilterKey(final String providerId, final ProfileFilter filter) {
    this(providerId, null, filter);
  }

  public String getProviderId() {
    return providerId;
  }

  public String getName() {
    return name;
  }

  public String getPosition() {
    return position;
  }

  public String getCompany() {
    return company;
  }

  public String getSkills() {
    return skills;
  }

  public char getFirstChar() {
    return firstChar;
  }

  public List<IdentityKey> getExcluded() {
    return excluded;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof IdentityFilterKey)) return false;
    if (!super.equals(o)) return false;

    IdentityFilterKey that = (IdentityFilterKey) o;

    if (firstChar != that.firstChar) return false;
    if (!StringUtils.equals(firstCharFieldName, that.firstCharFieldName)) return false;
    if (all != null ? !all.equals(that.all) : that.all != null) return false;
    if (company != null ? !company.equals(that.company) : that.company != null) return false;
    if (excluded != null ? !excluded.equals(that.excluded) : that.excluded != null) return false;
    if (onlineRemoteIds != null ? !onlineRemoteIds.equals(that.onlineRemoteIds) : that.onlineRemoteIds != null) return false;
    if (name != null ? !name.equals(that.name) : that.name != null) return false;
    if (position != null ? !position.equals(that.position) : that.position != null) return false;
    if (providerId != null ? !providerId.equals(that.providerId) : that.providerId != null) return false;
    if (remoteId != null ? !remoteId.equals(that.remoteId) : that.remoteId != null) return false;
    if (skills != null ? !skills.equals(that.skills) : that.skills != null) return false;
    if (sorting != null ? !sorting.equals(that.sorting) : that.sorting != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (providerId != null ? providerId.hashCode() : 0);
    result = 31 * result + (remoteId != null ? remoteId.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (position != null ? position.hashCode() : 0);
    result = 31 * result + (company != null ? company.hashCode() : 0);
    result = 31 * result + (skills != null ? skills.hashCode() : 0);
    result = 31 * result + (firstCharFieldName != null ? firstCharFieldName.hashCode() : 0);
    result = 31 * result + (int) firstChar;
    result = 31 * result + (excluded != null ? excluded.hashCode() : 0);
    result = 31 * result + (onlineRemoteIds != null ? onlineRemoteIds.hashCode() : 0);
    result = 31 * result + (all != null ? all.hashCode() : 0);
    result = 31 * result + (sorting != null ? sorting.hashCode() : 0);
    return result;
  }

}
