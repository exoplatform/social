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

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityExistsException;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.exoplatform.commons.api.persistence.ExoTransactional;
import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.search.ExtendProfileFilter;
import org.exoplatform.social.core.jpa.storage.dao.IdentityDAO;
import org.exoplatform.social.core.jpa.storage.dao.jpa.query.ProfileQueryBuilder;
import org.exoplatform.social.core.jpa.storage.entity.ConnectionEntity;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity;
import org.exoplatform.social.core.relationship.model.Relationship.Type;

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
  public ListAccess<Map.Entry<IdentityEntity, ConnectionEntity>> findAllIdentitiesWithConnections(long identityId, String sortField, char firstChar) {
    Query listQuery = getIdentitiesQuerySortedByField(OrganizationIdentityProvider.NAME, sortField, firstChar);

    TypedQuery<ConnectionEntity> connectionsQuery = getEntityManager().createNamedQuery("SocConnection.findConnectionsByIdentityIds", ConnectionEntity.class);

    TypedQuery<Long> countQuery = getEntityManager().createNamedQuery("SocIdentity.countIdentitiesByProviderWithExcludedIdentity", Long.class);
    countQuery.setParameter("providerId", OrganizationIdentityProvider.NAME);

    return new IdentityWithRelationshipListAccess(identityId, listQuery, connectionsQuery, countQuery);
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
  public List<String> getAllIdsByProviderSorted(String providerId, String sortField, char firstChar, long offset, long limit) {
    Query query = getIdentitiesQuerySortedByField(providerId, sortField, firstChar);
    return getResultsFromQuery(query, 0, offset, limit, String.class);
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

  public List<IdentityEntity> findIdentitiesByIDs(List<?> ids) {
    TypedQuery<IdentityEntity> query = getEntityManager().createNamedQuery("SocIdentity.findIdentitiesByIDs", IdentityEntity.class);
    query.setParameter("ids", ids);
    return query.getResultList();
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

  public class IdentityWithRelationshipListAccess implements ListAccess<Map.Entry<IdentityEntity, ConnectionEntity>> {
    private final Query identityQuery;
    private final TypedQuery<ConnectionEntity> connectionsQuery;
    private final TypedQuery<Long> countQuery;
    private final long identityId;

    public IdentityWithRelationshipListAccess(long identityId, Query identityQuery, TypedQuery<ConnectionEntity> connctionsQuery, TypedQuery<Long> countQuery) {
      this.identityQuery = identityQuery;
      this.connectionsQuery = connctionsQuery;
      this.countQuery = countQuery;
      this.identityId = identityId;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map.Entry<IdentityEntity, ConnectionEntity>[] load(int offset, int limit) throws Exception, IllegalArgumentException {
      List<Object> ids = getResultsFromQuery(identityQuery, 1, offset, limit, Object.class);

      if(ids.isEmpty()) {
        return new Map.Entry[0];
      }
      List<Long> idsLong = ids.stream().map(i -> Long.parseLong(i.toString())).collect(Collectors.toList());
      List<IdentityEntity> identitiesList = findIdentitiesByIDs(idsLong);
      Map<Long, IdentityEntity> identitiesMap = identitiesList.stream().collect(Collectors.toMap(identity -> identity.getId(), Function.identity()));
      connectionsQuery.setParameter("identityId", identityId);
      connectionsQuery.setParameter("ids", idsLong);
      connectionsQuery.setMaxResults(Integer.MAX_VALUE);
      List<ConnectionEntity> connectionsList = connectionsQuery.getResultList();
      Map<IdentityEntity, ConnectionEntity> map = new LinkedHashMap<IdentityEntity, ConnectionEntity>();
      for (Long identityId : idsLong) {
        IdentityEntity identityEntity = identitiesMap.get(identityId);
        if (identityEntity == null) {
          LOG.error("Can't find identity with id '{}'", identityId);
          continue;
        }
        CONN: for (ConnectionEntity connectionEntity : connectionsList) {
          if(connectionEntity.getReceiver().getId() == identityEntity.getId() || connectionEntity.getSender().getId() == identityEntity.getId()) {
            map.put(identityEntity, connectionEntity);
            break CONN;
          }
        }
        if (!map.containsKey(identityEntity)) {
          map.put(identityEntity, null);
        }
      }
      return map.entrySet().toArray(new Map.Entry[0]);
    }

    @Override
    public int getSize() throws Exception {
      return countQuery.getSingleResult().intValue();
    }
  }

  private Query getIdentitiesQuerySortedByField(String providerId, String sortField, char firstCharacter) {
    // Oracle and MSSQL support only 1/0 for boolean, Postgresql supports only TRUE/FALSE, MySQL supports both
    String dbBoolFalse = isOrcaleDialect() || isMSSQLDialect() ? "0" : "FALSE";
    String dbBoolTrue = isOrcaleDialect() || isMSSQLDialect() ? "1" : "TRUE";
    // Oracle Dialect in Hibernate 4 is not registering NVARCHAR correctly, see HHH-10495
    StringBuilder queryStringBuilder =
            isOrcaleDialect() ? new StringBuilder("SELECT to_char(identity_1.remote_id), identity_1.identity_id, to_char(identity_prop.value) \n")
                    :isMSSQLDialect() ? new StringBuilder("SELECT try_convert(varchar(200), identity_1.remote_id) as remote_id , identity_1.identity_id, try_convert(varchar(200), identity_prop.value) as identity_prop_value \n")
                    : new StringBuilder("SELECT (identity_1.remote_id), identity_1.identity_id, (identity_prop.value) \n");
    queryStringBuilder.append(" FROM SOC_IDENTITIES identity_1 \n");
    if (firstCharacter > 0) {
      queryStringBuilder.append(" INNER JOIN SOC_IDENTITY_PROPERTIES identity_prop_first_char \n");
      queryStringBuilder.append("   ON identity_1.identity_id = identity_prop_first_char.identity_id \n");
      queryStringBuilder.append("       AND identity_prop_first_char.name = '").append(Profile.LAST_NAME).append("' \n");
      queryStringBuilder.append("       AND (lower(identity_prop_first_char.value) like '" + Character.toLowerCase(firstCharacter) + "%')\n");
    }
    queryStringBuilder.append(" LEFT JOIN SOC_IDENTITY_PROPERTIES identity_prop \n");
    queryStringBuilder.append("   ON identity_1.identity_id = identity_prop.identity_id \n");
    queryStringBuilder.append("       AND identity_prop.name = '").append(sortField).append("' \n");
    queryStringBuilder.append(" WHERE identity_1.provider_id = '").append(providerId).append("' \n");
    queryStringBuilder.append(" AND identity_1.deleted = ").append(dbBoolFalse).append(" \n");
    queryStringBuilder.append(" AND identity_1.enabled = ").append(dbBoolTrue).append(" \n");
    queryStringBuilder.append(" ORDER BY identity_prop.value ASC");

    Query query = getEntityManager().createNativeQuery(queryStringBuilder.toString());
    return query;
  }


  private <T> List<T> getResultsFromQuery(Query query, int fieldIndex, long offset, long limit, Class<T> clazz) {
    if (limit > 0) {
      query.setMaxResults((int) limit);
    }
    if (offset >= 0) {
      query.setFirstResult((int) offset);
    }

    List<?> resultList = query.getResultList();
    List<T> result = new ArrayList<T>();
    for (Object object : resultList) {
      Object[] resultEntry = (Object[]) object;
      Object resultObject = resultEntry[fieldIndex];
      if (resultObject == null) {
        continue;
      }
      result.add((T) resultObject);
    }
    return result;
  }

}
