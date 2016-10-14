/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.jpa.storage.dao.ConnectionDAO;
import org.exoplatform.social.core.jpa.storage.dao.jpa.query.RelationshipQueryBuilder;
import org.exoplatform.social.core.jpa.storage.entity.ConnectionEntity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jun 4, 2015  
 */
public class ConnectionDAOImpl extends GenericDAOJPAImpl<ConnectionEntity, Long> implements ConnectionDAO {

  @Override
  @ExoTransactional
  public long count(Identity identity, Type status) {
    return RelationshipQueryBuilder.builder()
                                        .owner(identity)
                                        .status(status)
                                        .buildCount()
                                        .getSingleResult();
  }

  @Override
  public ConnectionEntity getConnection(Identity identity1, Identity identity2) {
    TypedQuery<ConnectionEntity> query = RelationshipQueryBuilder.builder()
                                                                 .sender(identity1)
                                                                 .receiver(identity2)
                                                                 .buildSingleRelationship();
    try {
      return query.getSingleResult();
    } catch (NoResultException e) {
      return null;
    } catch (NonUniqueResultException e) {
      return query.getResultList().get(0);
    }
  }

  @Override
  public ConnectionEntity getConnection(Long sender, Long reciver) {
    TypedQuery<ConnectionEntity> query = getEntityManager().createNamedQuery("SocConnection.findConnectionBySenderAndReceiver", ConnectionEntity.class);
    query.setParameter("sender", sender);
    query.setParameter("reciver", reciver);
    query.setMaxResults(1);

    try {
      return query.getSingleResult();
    } catch (NoResultException ex) {
      return null;
    }
  }

  @Override
  public List<ConnectionEntity> getConnections(Identity identity, Type status, long offset, long limit) {
    Long ownerId = Long.valueOf(identity.getId());

    String queryName = null;
    if (status == null || status == Type.ALL) {
      queryName = "SocConnection.getConnectionsWithoutStatus";
    } else {
      if(status == Type.INCOMING) {
        return getSenders(ownerId, Type.PENDING, (int) offset, (int) limit);
      } else if(status == Type.OUTGOING) {
        return getReceivers(ownerId, Type.PENDING, (int) offset, (int) limit);
      } else {
        queryName = "SocConnection.getConnectionsWithStatus";
      }
    }

    TypedQuery<ConnectionEntity> query = getEntityManager().createNamedQuery(queryName, ConnectionEntity.class);
    query.setParameter("identityId", ownerId);
    if (status != null && status != Type.ALL) {
      query.setParameter("status", status);
    }
    if (offset > 0) {
      query.setFirstResult((int) offset);
    }
    if (limit > 0) {
      query.setMaxResults((int) limit);
    }
    return query.getResultList();
  }

  @Override
  public List<ConnectionEntity> getConnections(Identity sender, Identity receiver, Type status) {
    return RelationshipQueryBuilder.builder()
                                   .sender(sender)
                                   .receiver(receiver)
                                   .status(status)
                                   .build()
                                   .getResultList();
  }

  @Override
  public int getConnectionsCount(Identity identity, Type status) {
    Long ownerId = Long.valueOf(identity.getId());

    String queryName = null;
    if (status == null || status == Type.ALL) {
      queryName = "SocConnection.countConnectionsWithoutStatus";
    } else if(status == Type.INCOMING) {
      return countSenderId(ownerId, Type.PENDING).intValue();
    } else if(status == Type.OUTGOING) {
      return countReceiverId(ownerId, Type.PENDING).intValue();
    } else {
      queryName = "SocConnection.countConnectionsWithStatus";
    }

    TypedQuery<Long> query = getEntityManager().createNamedQuery(queryName, Long.class);
    query.setParameter("identityId", ownerId);
    if (status != null && status != Type.ALL) {
      query.setParameter("status", status);
    }
    return query.getSingleResult().intValue();
  }

  @Override
  public List<ConnectionEntity> getLastConnections(Identity identity, int limit) {
    return RelationshipQueryBuilder.builder()
                                   .owner(identity)
                                   .status(Relationship.Type.CONFIRMED)
                                   .offset(0)
                                   .limit(limit)
                                   .buildLastConnections()
                                   .getResultList();
  }
  
  public List<ConnectionEntity> getConnectionsByFilter(Identity existingIdentity, ProfileFilter profileFilter, Type type, long offset, long limit) {
    return RelationshipQueryBuilder.builder()
                                   .owner(existingIdentity)
                                   .status(type)
                                   .offset(0)
                                   .limit(limit)
                                   .filter(profileFilter)
                                   .buildFilter()
                                   .getResultList();
  }

  @Override
  public int getConnectionsByFilterCount(Identity identity, ProfileFilter profileFilter, Type type) {
    return RelationshipQueryBuilder.builder()
                                   .owner(identity)
                                   .status(type)
                                   .filter(profileFilter)
                                   .buildFilterCount()
                                   .getSingleResult()
                                   .intValue();
  }

  @Override
  public List<Long> getSenderIds(long receiverId, Type status, int offset, int limit) {
    EntityManager em = getEntityManager();
    String queryName = null;
    if (status == null || status == Type.ALL) {
      queryName = "SocConnection.getSenderIdsByReceiverWithoutStatus";
    } else {
      queryName = "SocConnection.getSenderIdsByReceiverWithStatus";
    }
    TypedQuery<Long> query = em.createNamedQuery(queryName, Long.class);
    query.setParameter("identityId", receiverId);
    if (status != null && status != Type.ALL) {
      query.setParameter("status", status);
    }
    if (offset > 0) {
      query.setFirstResult(offset);
    }
    if (limit > 0) {
      query.setMaxResults(limit);
    }
    return query.getResultList();
  }

  @Override
  public List<Long> getReceiverIds(long senderId, Type status, int offset, int limit) {
    EntityManager em = getEntityManager();
    String queryName = null;
    if (status == null || status == Type.ALL) {
      queryName = "SocConnection.getReceiverIdsBySenderWithoutStatus";
    } else {
      queryName = "SocConnection.getReceiverIdsBySenderWithStatus";
    }
    TypedQuery<Long> query = em.createNamedQuery(queryName, Long.class);
    query.setParameter("identityId", senderId);
    if (status != null && status != Type.ALL) {
      query.setParameter("status", status);
    }
    if (offset > 0) {
      query.setFirstResult(offset);
    }
    if (limit > 0) {
      query.setMaxResults(limit);
    }
    return query.getResultList();
  }

  private Long countSenderId(long receiverId, Type status) {
    EntityManager em = getEntityManager();
    String queryName = null;
    if (status == null || status == Type.ALL) {
      queryName = "SocConnection.countSenderByReceiverWithoutStatus";
    } else {
      queryName = "SocConnection.countSenderByReceiverWithStatus";
    }
    TypedQuery<Long> query = em.createNamedQuery(queryName, Long.class);
    query.setParameter("identityId", receiverId);
    if (status != null && status != Type.ALL) {
      query.setParameter("status", status);
    }
    return query.getSingleResult();
  }

  private Long countReceiverId(long sender, Type status) {
    EntityManager em = getEntityManager();
    String queryName = null;
    if (status == null || status == Type.ALL) {
      queryName = "SocConnection.countReceiverBySenderWithoutStatus";
    } else {
      queryName = "SocConnection.countReceiverBySenderWithStatus";
    }
    TypedQuery<Long> query = em.createNamedQuery(queryName, Long.class);
    query.setParameter("identityId", sender);
    if (status != null && status != Type.ALL) {
      query.setParameter("status", status);
    }
    return query.getSingleResult();
  }

  private List<ConnectionEntity> getReceivers(long receiverId, Type status, int offset, int limit) {
    EntityManager em = getEntityManager();
    String queryName = null;
    if (status == null || status == Type.ALL) {
      queryName = "SocConnection.getReceiverBySenderWithoutStatus";
    } else {
      queryName = "SocConnection.getReceiverBySenderWithStatus";
    }
    TypedQuery<ConnectionEntity> query = em.createNamedQuery(queryName, ConnectionEntity.class);
    query.setParameter("identityId", receiverId);
    if (status != null && status != Type.ALL) {
      query.setParameter("status", status);
    }
    if (offset > 0) {
      query.setFirstResult(offset);
    }
    if (limit > 0) {
      query.setMaxResults(limit);
    }
    List<ConnectionEntity> receiversList = query.getResultList();
    return receiversList;
  }

  private List<ConnectionEntity> getSenders(long receiverId, Type status, int offset, int limit) {
    EntityManager em = getEntityManager();
    String queryName = null;
    if(status ==  null || status == Type.ALL) {
      queryName = "SocConnection.getSenderByReceiverWithoutStatus";
    } else {
      queryName = "SocConnection.getSenderByReceiverWithStatus";
    }
    TypedQuery<ConnectionEntity> query = em.createNamedQuery(queryName, ConnectionEntity.class);
    query.setParameter("identityId", receiverId);
    if(status !=  null && status != Type.ALL) {
      query.setParameter("status", status);
    }
    if (offset > 0) {
      query.setFirstResult(offset);
    }
    if (limit > 0) {
      query.setMaxResults(limit);
    }
    return query.getResultList();
  }

}
