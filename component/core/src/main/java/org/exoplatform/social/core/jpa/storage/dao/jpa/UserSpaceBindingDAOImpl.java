/*
 * Copyright (C) 2003-2019 eXo Platform SAS.
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

package org.exoplatform.social.core.jpa.storage.dao.jpa;

import java.util.List;

import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.jpa.storage.dao.UserSpaceBindingDAO;
import org.exoplatform.social.core.jpa.storage.entity.UserSpaceBindingEntity;

public class UserSpaceBindingDAOImpl extends GenericDAOJPAImpl<UserSpaceBindingEntity, Long> implements UserSpaceBindingDAO {

  @Override
  public List<UserSpaceBindingEntity> findUserBindingsByMember(Long spaceId, String userName) {
    TypedQuery<UserSpaceBindingEntity> query = getEntityManager().createNamedQuery("SocUserSpaceBinding.findUserBindingsbyMember",
                                                                                   UserSpaceBindingEntity.class);
    query.setParameter("spaceId", spaceId);
    query.setParameter("userName", userName);
    return query.getResultList();
  }

  @Override
  public void deleteAllUserBindings(String userName) {
    Query query = getEntityManager().createNamedQuery("SocUserSpaceBinding.deleteAllUserBindings");
    query.setParameter("userName", userName);
    query.executeUpdate();
  }

  @Override
  public boolean hasUserBindings(Long spaceId, String userName) {
      TypedQuery<Long> query = getEntityManager().createNamedQuery("SocUserSpaceBinding.countBindingsForMembers",Long.class);
      query.setParameter("spaceId", spaceId);
      query.setParameter("userName", userName);
      return query.getSingleResult().intValue()>0;
  }

}
