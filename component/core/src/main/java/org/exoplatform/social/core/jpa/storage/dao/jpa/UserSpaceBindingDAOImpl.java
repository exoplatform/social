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

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.jpa.storage.dao.UserSpaceBindingDAO;
import org.exoplatform.social.core.jpa.storage.entity.UserSpaceBindingEntity;

public class UserSpaceBindingDAOImpl extends GenericDAOJPAImpl<UserSpaceBindingEntity, Long> implements UserSpaceBindingDAO {

  @Override
  public List<UserSpaceBindingEntity> findUserBindingsBySpace(Long spaceId, String userName) {
    TypedQuery<UserSpaceBindingEntity> query = getEntityManager().createNamedQuery("SocUserSpaceBinding.findUserBindingsBySpace",
                                                                                   UserSpaceBindingEntity.class);
    query.setParameter("spaceId", spaceId);
    query.setParameter("userName", userName);
    return query.getResultList();
  }

  @Override
  public List<UserSpaceBindingEntity> findUserBindingsByGroup(String group, String userName) {
    TypedQuery<UserSpaceBindingEntity> query = getEntityManager().createNamedQuery("SocUserSpaceBinding.findUserBindingsByGroup",
                                                                                   UserSpaceBindingEntity.class);
    query.setParameter("group", group);
    query.setParameter("userName", userName);
    return query.getResultList();
  }

  @Override
  public List<UserSpaceBindingEntity> findUserAllBindingsByGroup(String group) {
    TypedQuery<UserSpaceBindingEntity> query =
                                             getEntityManager().createNamedQuery("SocUserSpaceBinding.findUserAllBindingsByGroup",
                                                                                 UserSpaceBindingEntity.class);
    query.setParameter("group", group);
    return query.getResultList();
  }

  @Override
  public List<UserSpaceBindingEntity> findUserAllBindingsByUser(String userName) {
    TypedQuery<UserSpaceBindingEntity> query =
                                             getEntityManager().createNamedQuery("SocUserSpaceBinding.findUserAllBindingsByUser",
                                                                                 UserSpaceBindingEntity.class);
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
  public List<UserSpaceBindingEntity> getUserBindings(Long spaceId, String userName) {
    TypedQuery<UserSpaceBindingEntity> query = getEntityManager().createNamedQuery("SocUserSpaceBinding.findAllUserBindingsByUserAndSpace", UserSpaceBindingEntity.class);
    query.setParameter("spaceId", spaceId);
    query.setParameter("userName", userName);
    return query.getResultList();
  }
  
  @Override
  public long countUserBindings(Long spaceId, String userName) {
    return (Long) getEntityManager().createNamedQuery("SocUserSpaceBinding.countAllUserBindingsByUserAndSpace")
                                     .setParameter("userName", userName)
                                     .setParameter("spaceId", spaceId)
                                     .getSingleResult();
  }
  
  @Override
  public List<UserSpaceBindingEntity> findBoundUsersByBindingId(long bindingId) {
    TypedQuery<UserSpaceBindingEntity> query =
                                             getEntityManager().createNamedQuery("SocUserSpaceBinding.findBoundUsersByBindingId",
                                                                                 UserSpaceBindingEntity.class);
    query.setParameter("bindingId", bindingId);
    return query.getResultList();
  }

  @Override
  public Boolean isUserBoundAndMemberBefore(Long spaceId, String userName) {
    UserSpaceBindingEntity userSpaceBindingEntity;
    TypedQuery<UserSpaceBindingEntity> query =
                                             getEntityManager().createNamedQuery("SocUserSpaceBinding.isUserBoundAndMemberBefore",
                                                                                 UserSpaceBindingEntity.class);
    query.setParameter("spaceId", spaceId);
    query.setParameter("userName", userName);
    query.setMaxResults(1);
    try {
      userSpaceBindingEntity = query.getSingleResult();
    } catch (NoResultException ex) {
      return null;
    }
    return userSpaceBindingEntity.isMemberBefore();
  }

}
