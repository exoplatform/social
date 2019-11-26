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

import javax.persistence.*;

import org.apache.commons.lang3.StringUtils;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.jpa.storage.dao.ConnectionDAO;
import org.exoplatform.social.core.jpa.storage.dao.jpa.query.RelationshipQueryBuilder;
import org.exoplatform.social.core.jpa.storage.entity.ConnectionEntity;
import org.exoplatform.social.core.profile.ProfileFilter;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.relationship.model.Relationship.Type;
import org.exoplatform.social.core.search.Sorting;

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
    long senderId = Long.parseLong(identity1.getId());
    long receiverId = Long.parseLong(identity2.getId());
    return getConnection(senderId, receiverId);
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
  @SuppressWarnings("unchecked")
  public List<ConnectionEntity> getConnections(Identity identity, Type status, String firstCharacterField, char firstCharacter, long offset, long limit, Sorting sorting) {
    String sortFieldName = sorting == null || sorting.sortBy == null ? null : sorting.sortBy.getFieldName();
    String sortDirection = sorting == null || sorting.orderBy == null ? Sorting.OrderBy.ASC.name() : sorting.orderBy.name();
    Query query = getConnectionsQuery(identity.getId(), status, firstCharacterField, firstCharacter, sortFieldName, sortDirection);
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
    if (receiver == null && sender == null) {
      throw new IllegalArgumentException("Sender and receiver are null. Can't query the whole database.");
    }
    TypedQuery<ConnectionEntity> query = null;
    if (sender == null) {
      if (status == null) {
        query = getEntityManager().createNamedQuery("SocConnection.getSenderByReceiverWithoutStatus", ConnectionEntity.class);
        long id = Long.parseLong(receiver.getId());
        query.setParameter("identityId", id);
      } else {
        query = getEntityManager().createNamedQuery("SocConnection.getSenderByReceiverWithStatus", ConnectionEntity.class);
        long id = Long.parseLong(receiver.getId());
        query.setParameter("identityId", id);
        query.setParameter("status", status);
      }
    } else {
      if (receiver == null && status == null) {
        query = getEntityManager().createNamedQuery("SocConnection.getReceiverBySenderWithoutStatus", ConnectionEntity.class);
        long id = Long.parseLong(sender.getId());
        query.setParameter("identityId", id);
      } else if (receiver == null) {
        query = getEntityManager().createNamedQuery("SocConnection.getReceiverBySenderWithStatus", ConnectionEntity.class);
        long id = Long.parseLong(sender.getId());
        query.setParameter("identityId", id);
        query.setParameter("status", status);
      } else if (status == null) {
        query = getEntityManager().createNamedQuery("SocConnection.findConnectionBySenderAndReceiver", ConnectionEntity.class);
        long id = Long.parseLong(sender.getId());
        query.setParameter("senderId", id);
        id = Long.parseLong(receiver.getId());
        query.setParameter("receiverId", id);
      } else {
        query = getEntityManager().createNamedQuery("SocConnection.findConnectionBySenderAndReceiverWithStatus",
                                                    ConnectionEntity.class);
        long id = Long.parseLong(sender.getId());
        query.setParameter("senderId", id);
        id = Long.parseLong(receiver.getId());
        query.setParameter("receiverId", id);
        query.setParameter("status", status);
      }
    }
    return query.getResultList();
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
    if (identity == null) {
      throw new IllegalArgumentException("identity is null. Can't query the whole database.");
    }
    TypedQuery<ConnectionEntity> query = getEntityManager().createNamedQuery("SocConnection.getConnectionsWithStatus", ConnectionEntity.class);
    long id = Long.parseLong(identity.getId());
    query.setParameter("identityId", id);
    query.setParameter("status", Relationship.Type.CONFIRMED);

    if (limit > 0) {
      query.setMaxResults(limit);
    }

    return query.getResultList();
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

  private Query getConnectionsQuery(String identityId, Type status, String firstCharacterField, char firstCharacter, String sortField, String sortDirection) {
    StringBuilder queryStringBuilder = new StringBuilder("SELECT c.* FROM SOC_CONNECTIONS c \n");
    if (firstCharacter > 0) {
      queryStringBuilder.append(" INNER JOIN SOC_IDENTITY_PROPERTIES identity_prop_first_char \n");
      queryStringBuilder.append("   ON identity_prop_first_char.identity_id <> ").append(identityId).append(" \n");
      queryStringBuilder.append("       AND (identity_prop_first_char.identity_id = c.sender_id OR identity_prop_first_char.identity_id = c.receiver_id) \n");
      queryStringBuilder.append("       AND identity_prop_first_char.name = '").append(firstCharacterField).append("' \n");
      queryStringBuilder.append("       AND (lower(identity_prop_first_char.value) like '" + Character.toLowerCase(firstCharacter) + "%')\n");
    }
    if (StringUtils.isNotBlank(sortField)) {
      queryStringBuilder.append(" LEFT JOIN SOC_IDENTITY_PROPERTIES identity_prop \n");
      queryStringBuilder.append("   ON identity_prop.identity_id <> ").append(identityId).append(" \n");
      queryStringBuilder.append("       AND (identity_prop.identity_id = c.sender_id OR identity_prop.identity_id = c.receiver_id) \n");
      queryStringBuilder.append("       AND identity_prop.name = '").append(sortField).append("' \n");
    }
    switch(status) {
      case ALL:
        queryStringBuilder.append("WHERE (c.sender_id = ").append(identityId).append(" OR c.receiver_id = ").append(identityId).append(") \n");
        break;
      case CONFIRMED:
        queryStringBuilder.append("WHERE (c.sender_id =  ").append(identityId).append("  OR c.receiver_id = ").append(identityId).append(") \n");
        queryStringBuilder.append(" AND c.status = ").append(Type.CONFIRMED.ordinal()).append(" \n");
        break;
      case PENDING:
        queryStringBuilder.append("WHERE (c.sender_id = ").append(identityId).append("  OR c.receiver_id = ").append(identityId).append(") \n");
        queryStringBuilder.append(" AND c.status = ").append(Type.PENDING.ordinal()).append(" \n");
        break;
      case INCOMING:
        queryStringBuilder.append("WHERE c.receiver_id = ").append(identityId).append(" \n");
        queryStringBuilder.append(" AND c.status = ").append(Type.PENDING.ordinal()).append(" \n");
        break;
      case OUTGOING:
        queryStringBuilder.append("WHERE c.sender_id = ").append(identityId).append(" \n");
        queryStringBuilder.append(" AND c.status = ").append(Type.PENDING.ordinal()).append(" \n");
        break;
      case IGNORED:
        queryStringBuilder.append("WHERE (c.sender_id = ").append(identityId).append("  OR c.receiver_id = ").append(identityId).append(") \n");
        queryStringBuilder.append(" AND c.status = ").append(Type.IGNORED.ordinal()).append(" \n");
        break;
      default:
        break;
    }
    if (sortField != null) {
      queryStringBuilder.append(" ORDER BY lower(identity_prop.value) ").append(sortDirection);
    }
    return getEntityManager().createNativeQuery(queryStringBuilder.toString(), ConnectionEntity.class);
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
