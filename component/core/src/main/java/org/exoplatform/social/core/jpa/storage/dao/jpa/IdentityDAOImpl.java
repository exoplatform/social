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
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.persistence.EntityExistsException;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;

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

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public class IdentityDAOImpl extends GenericDAOJPAImpl<IdentityEntity, Long> implements IdentityDAO {
  
  private static final Log LOG = ExoLogger.getLogger(IdentityDAOImpl.class);

  private static final int MAX_ITEMS_PER_IN_CLAUSE = 1000;

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
  public ListAccess<Map.Entry<IdentityEntity, ConnectionEntity>> findAllIdentitiesWithConnections(long identityId, String firstCharacterFieldName, char firstCharacter, String sortField, String sortDirection) {
    Query listQuery = getIdentitiesQuerySortedByField(OrganizationIdentityProvider.NAME, firstCharacterFieldName, firstCharacter, sortField, sortDirection);

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
  public List<String> getAllIdsByProviderSorted(String providerId, String firstCharacterFieldName, char firstCharacter, String sortField, String sortDirection, long offset, long limit) {
    Query query = getIdentitiesQuerySortedByField(providerId, firstCharacterFieldName, firstCharacter, sortField, sortDirection);
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
  
  public List<IdentityEntity> findIdentitiesByIDs(List<Long> ids) {
    TypedQuery<IdentityEntity> query = getEntityManager().createNamedQuery("SocIdentity.findIdentitiesByIDs", IdentityEntity.class);
    if (isOrcaleDialect()) {
      return getFromOracleDB(query, ids);
    } else {
      query.setParameter("ids", ids);
      return query.getResultList();
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
      connectionsQuery.setMaxResults(Integer.MAX_VALUE);
      List<ConnectionEntity> connectionsList = new ArrayList<>();
      if (isOrcaleDialect()) {
        connectionsList = getFromOracleDB(connectionsQuery, idsLong);
      } else {
        connectionsQuery.setParameter("ids", idsLong);
        connectionsList = connectionsQuery.getResultList();
      }
      Map<IdentityEntity, ConnectionEntity> map = new LinkedHashMap<>();
      for (Long identityId : idsLong) {
        IdentityEntity identityEntity = identitiesMap.get(identityId);
        if (identityEntity == null) {
          LOG.warn("Can't find identity with id '{}'", identityId);
          continue;
        }
        for (ConnectionEntity connectionEntity : connectionsList) {
          if(connectionEntity.getReceiver().getId() == identityEntity.getId() || connectionEntity.getSender().getId() == identityEntity.getId()) {
            map.put(identityEntity, connectionEntity);
            break;
          }
        }
        if (!map.containsKey(identityEntity)) {
          map.put(identityEntity, null);
        }
      }
      List<Entry<IdentityEntity, ConnectionEntity>> identities = new ArrayList<>(map.entrySet());
      identities.sort(new Comparator<Entry<IdentityEntity, ConnectionEntity>>() {
        @Override
        public int compare(Entry<IdentityEntity, ConnectionEntity> o1, Entry<IdentityEntity, ConnectionEntity> o2) {
          return idsLong.indexOf(o1.getKey().getId()) - idsLong.indexOf(o2.getKey().getId());
        }
      });
      return identities.toArray(new Map.Entry[0]);
    }

    @Override
    public int getSize() throws Exception {
      return countQuery.getSingleResult().intValue();
    }
  }

  private Query getIdentitiesQuerySortedByField(String providerId,
                                                String firstCharacterFieldName,
                                                char firstCharacter,
                                                String sortField,
                                                String sortDirection) {
    // Oracle and MSSQL support only 1/0 for boolean, Postgresql supports only
    // TRUE/FALSE, MySQL supports both
    String dbBoolFalse = isOrcaleDialect() || isMSSQLDialect() ? "0" : "FALSE";
    String dbBoolTrue = isOrcaleDialect() || isMSSQLDialect() ? "1" : "TRUE";
    // Oracle Dialect in Hibernate 4 is not registering NVARCHAR correctly, see
    // HHH-10495
    StringBuilder queryStringBuilder = null;
    if (isOrcaleDialect()) {
      queryStringBuilder = new StringBuilder("SELECT to_char(identity_1.remote_id), identity_1.identity_id \n");
    } else if (isMSSQLDialect()) {
      queryStringBuilder = new StringBuilder("SELECT try_convert(varchar(200), identity_1.remote_id) as remote_id , identity_1.identity_id, try_convert(varchar(200) \n");
    } else {
      queryStringBuilder = new StringBuilder("SELECT identity_1.remote_id, identity_1.identity_id \n");
    }
    queryStringBuilder.append(" FROM SOC_IDENTITIES identity_1 \n");
    if (StringUtils.isNotBlank(firstCharacterFieldName) && firstCharacter > 0) {
      queryStringBuilder.append(" INNER JOIN SOC_IDENTITY_PROPERTIES identity_prop_first_char \n");
      queryStringBuilder.append("   ON identity_1.identity_id = identity_prop_first_char.identity_id \n");
      queryStringBuilder.append("       AND identity_prop_first_char.name = '").append(firstCharacterFieldName).append("' \n");
      queryStringBuilder.append("       AND (lower(identity_prop_first_char.value) like '" + Character.toLowerCase(firstCharacter)
          + "%')\n");
    }
    if (StringUtils.isNotBlank(sortField) && StringUtils.isNotBlank(sortDirection)) {
      queryStringBuilder.append(" LEFT JOIN SOC_IDENTITY_PROPERTIES identity_prop \n");
      queryStringBuilder.append("   ON identity_1.identity_id = identity_prop.identity_id \n");
      queryStringBuilder.append("       AND identity_prop.name = '").append(sortField).append("' \n");
    }
    queryStringBuilder.append(" WHERE identity_1.provider_id = '").append(providerId).append("' \n");
    queryStringBuilder.append(" AND identity_1.deleted = ").append(dbBoolFalse).append(" \n");
    queryStringBuilder.append(" AND identity_1.enabled = ").append(dbBoolTrue).append(" \n");

    if (StringUtils.isNotBlank(sortField) && StringUtils.isNotBlank(sortDirection)) {
      queryStringBuilder.append(" ORDER BY lower(identity_prop.value) " + sortDirection);
    }
    return getEntityManager().createNativeQuery(queryStringBuilder.toString());
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

  /**
   *  in ORACLE, maximum number of expressions in a list is 1000.
   *  so we need to send more than one request if the number of ids is more than 1000 (1000 ids per request).
   *  @param <T> This is the type parameter
   * @param query The query.
   * @param ids The ids of users.
   * @return
*/
  public <T> List<T> getFromOracleDB(TypedQuery<T> query, List<Long> ids) {
    if (ids.size() <= MAX_ITEMS_PER_IN_CLAUSE) {
      query.setParameter("ids", ids);
      return query.getResultList();
    } else {
      List<Long> selectedIds = new ArrayList<>(ids);

      List<T> usersList = new ArrayList<>();

      // Retrieve identities by page of 1000
      int startIndex = 0;
      int endIndex = 0;
      while (endIndex < ids.size()) {
        // Compute sub list end index
        endIndex += MAX_ITEMS_PER_IN_CLAUSE;
        if (endIndex > ids.size()) {
          endIndex = ids.size();
        }
        // Compute identities entities for the subset
        List<Long> includedIds = selectedIds.subList(startIndex, endIndex);
        query.setParameter("ids", includedIds);
        usersList.addAll(query.getResultList());

        // Increment sub list start index
        startIndex = endIndex;
      }
      return usersList;
    }
  }

}
