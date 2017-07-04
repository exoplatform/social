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

import org.exoplatform.social.core.search.Sorting;
import org.exoplatform.social.core.space.SpaceFilter;

/**
 * Immutable space filter key.
 * This key is used to cache the search results.
 *
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SpaceFilterKey extends ScopeCacheKey {
  private static final long serialVersionUID = 2363449672896832814L;

  private String userId;
  private char firstCharacterOfSpaceName;
  private String spaceNameSearchCondition;
  private String appId;
  private SpaceType type;
  private Sorting sorting;

  public SpaceFilterKey(String userId, SpaceFilter filter, SpaceType type) {
    this.userId = userId;
    if (filter != null) {
      this.firstCharacterOfSpaceName = filter.getFirstCharacterOfSpaceName();
      this.spaceNameSearchCondition = filter.getSpaceNameSearchCondition();
      this.sorting = filter.getSorting();
      this.appId = filter.getAppId();
    }
    this.type = type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SpaceFilterKey)) return false;
    if (!super.equals(o)) return false;

    SpaceFilterKey that = (SpaceFilterKey) o;
    
    if (appId != that.appId) {
      return false;
    }

    if (firstCharacterOfSpaceName != that.firstCharacterOfSpaceName) return false;
    if (sorting != null ? !sorting.equals(that.sorting) : that.sorting != null) return false;
    if (spaceNameSearchCondition != null ? !spaceNameSearchCondition.equals(that.spaceNameSearchCondition) : that.spaceNameSearchCondition != null)
      return false;
    if (type != that.type) return false;
    if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (userId != null ? userId.hashCode() : 0);
    result = 31 * result + (appId != null ? appId.hashCode() : 0);
    result = 31 * result + (int) firstCharacterOfSpaceName;
    result = 31 * result + (spaceNameSearchCondition != null ? spaceNameSearchCondition.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    result = 31 * result + (sorting != null ? sorting.hashCode() : 0);
    return result;
  }

}
