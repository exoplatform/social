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
import javax.persistence.TypedQuery;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.jpa.storage.dao.GroupSpaceBindingQueueDAO;
import org.exoplatform.social.core.jpa.storage.entity.GroupSpaceBindingEntity;
import org.exoplatform.social.core.jpa.storage.entity.GroupSpaceBindingQueueEntity;

public class GroupSpaceBindingQueueDAOImpl extends GenericDAOJPAImpl<GroupSpaceBindingQueueEntity, Long>
    implements GroupSpaceBindingQueueDAO {

  @Override
  public GroupSpaceBindingQueueEntity findFirstGroupSpaceBindingQueue() {
    TypedQuery<GroupSpaceBindingQueueEntity> query =
                                                   getEntityManager().createNamedQuery("SocGroupSpaceBindingQueue.findFirstGroupSpaceBindingQueue",
                                                                                       GroupSpaceBindingQueueEntity.class);
    query.setMaxResults(1);
    try {
      return query.getSingleResult();
    } catch (NoResultException ex) {
      return null;
    }
  }

  @Override
  public List<GroupSpaceBindingEntity> getGroupSpaceBindingsFromQueueByAction(String action) {
    TypedQuery<GroupSpaceBindingEntity> query =
                                              getEntityManager().createNamedQuery("SocGroupSpaceBindingQueue.getGroupSpaceBindingsFromQueueByAction",
                                                                                  GroupSpaceBindingEntity.class);
    query.setParameter("action", action);
    return query.getResultList();
  }

  @Override
  public List<GroupSpaceBindingQueueEntity> getAllFromBindingQueue() {
    TypedQuery<GroupSpaceBindingQueueEntity> query =
                                                   getEntityManager().createNamedQuery("SocGroupSpaceBindingQueue.getAllFromBindingQueueOrderedById",
                                                                                       GroupSpaceBindingQueueEntity.class);
    return query.getResultList();
  }
}
