/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
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
package org.exoplatform.social.core.jpa.storage.dao;

import java.util.List;
import java.util.Set;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity.Status;

public interface SpaceMemberDAO extends GenericDAO<SpaceMemberEntity, Long> {
    void deleteBySpace(SpaceEntity entity);

    SpaceMemberEntity getSpaceMemberShip(String remoteId, Long spaceId, SpaceMemberEntity.Status status);

    List<Long> getSpacesIdsByUserName(String userId, int offset, int limit);

    /**
     * Get space members switch status
     * 
     * @param spaceId
     * @param status equals to MEMBER, MANAGER, PENDING, INVITED or IGNORED
     * @param offset
     * @param limit
     * @return
     */
    List<String> getSpaceMembers(Long spaceId, Status status, int offset, int limit);

  /**
   * Sort user identity remote ids
   * 
   * @param userNames
   * @param firstCharacterFieldName
   * @param firstCharacter
   * @param sortField
   * @param sortDirection
   * @param filterDisabled
   * @return {@link List} of userNames sorted by sortField
   */
  List<String> sortSpaceMembers(List<String> userNames,
                                String firstCharacterFieldName,
                                char firstCharacter,
                                String sortField,
                                String sortDirection,
                                boolean filterDisabled);

    /**
     * Count space members switch status
     * 
     * @param spaceId
     * @param status equals to MEMBER, MANAGER, PENDING, INVITED or IGNORED
     * @return
     */
    int countSpaceMembers(Long spaceId, Status status);

}
