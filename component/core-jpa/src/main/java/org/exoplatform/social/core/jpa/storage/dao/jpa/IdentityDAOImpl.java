/*
 * Copyright (C) 2015 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.social.core.jpa.storage.dao.jpa;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.search.ExtendProfileFilter;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.jpa.storage.dao.jpa.query.ProfileQueryBuilder;
import org.exoplatform.social.core.jpa.storage.entity.ConnectionEntity;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity;
import org.exoplatform.social.core.relationship.model.Relationship.Type;

import javax.persistence.EntityExistsException;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.lang.reflect.Array;
import java.util.*;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class IdentityDAOImpl extends GenericDAOJPAImpl<IdentityEntity, Long> implements IdentityDAO {
  
  private static final Log LOG = ExoLogger.getLogger(IdentityDAOImpl.class);

  @Override
  public IdentityEntity create(IdentityEntity entity) {
    IdentityEntity exists = findByProviderAndRemoteId(entity.getProviderId(), entity.getRemoteId());
    if (exists != null) {
      throw new EntityExistsException("Identity is existed with ProviderID=" + entity.getProviderId() + " and RemoteId=" + entity.getRemoteId());
    }
    return super.create(entity);
  }

  @Override
  public IdentityEntity findByProviderAndRemoteId(String providerId, String remoteId) {
    TypedQuery<IdentityEntity> query = getEntityManager().createNamedQuery("SocIdentity.findByProviderAndRemoteId", IdentityEntity.class);
    query.setParameter("providerId", providerId);
    query.setParameter("remoteId", remoteId);

    try {
      return query.getSingleResult();
    } catch (NoResultException ex) {
      return null;
    }
  }

  @Override
  public long countIdentityByProvider(String providerId) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery("SocIdentity.countIdentityByProvider", Long.class);
    query.setParameter("providerId", providerId);
    return query.getSingleResult();
  }

  @Override
  public List<Long> getAllIds(int offset, int limit) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery("SocIdentity.getAllIds", Long.class);
    if (limit > 0) {
      query.setFirstResult(offset);
      query.setMaxResults(limit);
    }
    return query.getResultList();
  }

  @Override
  public List<Long> getAllIdsByProvider(String providerId, int offset, int limit) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery("SocIdentity.getAllIdsByProvider", Long.class);
    query.setParameter("providerId", providerId);
    if (limit > 0) {
      query.setFirstResult(offset);
      query.setMaxResults(limit);
    }
    return query.getResultList();
  }

  @Override
  public ListAccess<Map.Entry<IdentityEntity, ConnectionEntity>> findAllIdentitiesWithConnections(long identityId) {
    TypedQuery<IdentityEntity> query = getEntityManager().createNamedQuery("SocIdentity.nativeFindIdentitiesByProviderWithExcludedIdentity", IdentityEntity.class);
    //TypedQuery<IdentityEntity> query = getEntityManager().createNamedQuery("SocIdentity.findIdentitiesByProviderWithExcludedIdentity", IdentityEntity.class);
    query.setParameter("identityId", identityId);
    query.setParameter("providerId", OrganizationIdentityProvider.NAME);

    TypedQuery<ConnectionEntity> connectionsQuery = getEntityManager().createNamedQuery("SocConnection.findConnectionsByIdentityIds", ConnectionEntity.class);

    TypedQuery<Long> countQuery = getEntityManager().createNamedQuery("SocIdentity.countIdentitiesByProviderWithExcludedIdentity", Long.class);
    countQuery.setParameter("identityId", identityId);
    countQuery.setParameter("providerId", OrganizationIdentityProvider.NAME);

    return new IdentityWithRelationshipListAccess(query, connectionsQuery, countQuery);
  }

  @Override
  public ListAccess<IdentityEntity> findIdentities(ExtendProfileFilter filter) {
    if (filter.getConnection() != null) {
      Identity owner = filter.getConnection();
      Long ownerId = Long.valueOf(owner.getId());
      Type status = filter.getConnectionStatus();
      List<Long> connections = getConnections(ownerId, status);
      if (connections.isEmpty()) {
        return new JPAListAccess<>(IdentityEntity.class);
      } else if (filter.getIdentityIds() == null || filter.getIdentityIds().isEmpty()) {
        filter.setIdentityIds(connections);
      } else {
        filter.getIdentityIds().retainAll(connections);
      }
    }

    ProfileQueryBuilder qb = ProfileQueryBuilder.builder()
            .withFilter(filter);
    TypedQuery[] queries = qb.build(getEntityManager());

    return new JPAListAccess<>(IdentityEntity.class, queries[0], queries[1]);
  }

  @Override
  @ExoTransactional
  public void setAsDeleted(long identityId) {
    IdentityEntity entity = find(identityId);
    if (entity != null) {
      entity.setDeleted(true);
      update(entity);
    }
  }

  @Override
  @ExoTransactional
  public void hardDeleteIdentity(long identityId) {
    IdentityEntity entity = find(identityId);
    if (entity != null) {
      delete(entity);
    }
  }

  @SuppressWarnings("unchecked")
  private List<Long> getConnections(Long ownerId, Type status) {
    String queryName = null;
    Class<?> returnType = null;
    if (status == null || status == Type.ALL) {
      queryName = "SocConnection.getConnectionsWithoutStatus";
      returnType = ConnectionEntity.class;
    } else if (status == Type.INCOMING) {
      queryName = "SocConnection.getSenderIdsByReceiverWithStatus";
      returnType = Long.class;
      status = Type.PENDING;
    } else if (status == Type.OUTGOING) {
      queryName = "SocConnection.getReceiverIdsBySenderWithStatus";
      returnType = Long.class;
      status = Type.PENDING;
    } else {
      queryName = "SocConnection.getConnectionsWithStatus";
      returnType = ConnectionEntity.class;
    }

    Query query = getEntityManager().createNamedQuery(queryName);
    query.setParameter("identityId", ownerId);
    if (status != null && status != Type.ALL) {
      query.setParameter("status", status);
    }
    if (returnType == Long.class) {
      return query.getResultList();
    } else {
      List<Long> ids = new ArrayList<Long>();
      List<ConnectionEntity> connectionEntities = query.getResultList();
      for (ConnectionEntity connectionEntity : connectionEntities) {
        if (connectionEntity.getReceiver().getId() == ownerId) {
          ids.add(connectionEntity.getSender().getId());
        } else if (connectionEntity.getSender().getId() == ownerId) {
          ids.add(connectionEntity.getReceiver().getId());
        } else {
          LOG.warn("Neither sender neither receiver corresponds to owner with id {}. ", ownerId);
        }
      }
      return ids;
    }
  }

  public static class JPAListAccess<T> implements ListAccess<T> {
    private final TypedQuery<T> selectQuery;
    private final TypedQuery<Long> countQuery;
    private final Class<T> clazz;

    public JPAListAccess(Class<T> clazz) {
      this.clazz = clazz;
      this.selectQuery = null;
      this.countQuery = null;
    }
                         
    public JPAListAccess(Class<T> clazz, TypedQuery<T> selectQuery, TypedQuery<Long> countQuery) {
      this.clazz = clazz;
      this.selectQuery = selectQuery;
      this.countQuery = countQuery;
    }

    @Override
    public T[] load(int offset, int limit) throws Exception, IllegalArgumentException {
      if (selectQuery == null) {
        return (T[]) Array.newInstance(clazz, 0);
      }
      if (limit > 0 && offset >= 0) {
        selectQuery.setFirstResult(offset);
        selectQuery.setMaxResults(limit);
      } else {
        selectQuery.setMaxResults(Integer.MAX_VALUE);
      }

      List<T> list = selectQuery.getResultList();
      if (list != null && list.size() > 0) {
        T[] arr = (T[])Array.newInstance(clazz, list.size());
        return list.toArray(arr);
      } else {
        return (T[])Array.newInstance(clazz, 0);
      }
    }

    @Override
    public int getSize() throws Exception {
      if (countQuery == null) {
        return 0;
      }
      return countQuery.getSingleResult().intValue();
    }
  }

  public static class IdentityWithRelationshipListAccess implements ListAccess<Map.Entry<IdentityEntity, ConnectionEntity>> {
    private final TypedQuery<IdentityEntity> identityQuery;
    private final TypedQuery<ConnectionEntity> connectionsQuery;
    private final TypedQuery<Long> countQuery;

    public IdentityWithRelationshipListAccess(TypedQuery<IdentityEntity> identityQuery, TypedQuery<ConnectionEntity> connctionsQuery, TypedQuery<Long> countQuery) {
      this.identityQuery = identityQuery;
      this.connectionsQuery = connctionsQuery;
      this.countQuery = countQuery;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map.Entry<IdentityEntity, ConnectionEntity>[] load(int offset, int limit) throws Exception, IllegalArgumentException {
      if (limit > 0 && offset >= 0) {
        identityQuery.setFirstResult(offset);
        identityQuery.setMaxResults(limit);
      } else {
        identityQuery.setMaxResults(Integer.MAX_VALUE);
      }

      List<Long> ids = new ArrayList<>();
      List<IdentityEntity> identitiesList = identityQuery.getResultList();
      for (IdentityEntity identityEntity : identitiesList) {
        ids.add(identityEntity.getId());
      }

      if(ids.isEmpty()) {
        return new Map.Entry[0];
      }
      connectionsQuery.setParameter("identityId", identityQuery.getParameterValue("identityId"));
      connectionsQuery.setParameter("ids", ids);
      connectionsQuery.setMaxResults(Integer.MAX_VALUE);
      List<ConnectionEntity> connectionsList = connectionsQuery.getResultList();

      //use linked hashmap to keep order from orderby
      Map<IdentityEntity, ConnectionEntity> map = new LinkedHashMap<IdentityEntity, ConnectionEntity>();
      for (IdentityEntity identityEntity : identitiesList) {
        map.put(identityEntity, null);
        for (ConnectionEntity connectionEntity : connectionsList) {
          if(connectionEntity.getReceiver().getId() == identityEntity.getId() || connectionEntity.getSender().getId() == identityEntity.getId()) {
            map.put(identityEntity, connectionEntity);
          }
        }
      }
      return map.entrySet().toArray(new Map.Entry[0]);
    }

    @Override
    public int getSize() throws Exception {
      return countQuery.getSingleResult().intValue();
    }
  }

}
