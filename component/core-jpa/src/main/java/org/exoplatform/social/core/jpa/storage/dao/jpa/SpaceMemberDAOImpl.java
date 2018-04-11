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

package org.exoplatform.social.core.jpa.storage.dao.jpa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.jpa.storage.dao.SpaceMemberDAO;
import org.exoplatform.social.core.jpa.storage.entity.SpaceEntity;
import org.exoplatform.social.core.jpa.storage.entity.SpaceMemberEntity;

public class SpaceMemberDAOImpl extends GenericDAOJPAImpl<SpaceMemberEntity, Long> implements SpaceMemberDAO {

  private static final Log LOG = ExoLogger.getLogger(SpaceMemberDAOImpl.class);

  @Override
  public void deleteBySpace(SpaceEntity entity) {
    Query query = getEntityManager().createNamedQuery("SpaceMember.deleteBySpace");
    query.setParameter("spaceId", entity.getId());
    query.executeUpdate();
  }

  @Override
  public SpaceMemberEntity getSpaceMemberShip(String remoteId, Long spaceId, SpaceMemberEntity.Status status) throws IllegalArgumentException {
    if (status == null) {
      TypedQuery<SpaceMemberEntity> query = getEntityManager().createNamedQuery("SpaceMember.getSpaceMemberShip", SpaceMemberEntity.class);
      query.setParameter("userId", remoteId);
      query.setParameter("spaceId", spaceId);
      try {
        List<SpaceMemberEntity> memberEntities = query.getResultList();
        if (memberEntities.size() > 1) {
          LOG.warn("we have found more than one result");
          return memberEntities.get(0);
        } else {
          return query.getSingleResult();
        }
      } catch (NoResultException ex) {
        return null;
      }
    } else {
      TypedQuery<SpaceMemberEntity> query = getEntityManager().createNamedQuery("SpaceMember.getMember", SpaceMemberEntity.class);
      query.setParameter("userId", remoteId);
      query.setParameter("spaceId", spaceId);
      query.setParameter("status", status);
      try {
        List<SpaceMemberEntity> memberEntities = query.getResultList();
        if (memberEntities.size() > 1) {
          LOG.warn("we have found more than one result");
          return memberEntities.get(0);
        } else {
          return query.getSingleResult();
        }
      } catch (NoResultException ex) {
        return null;
      }
    }
  }

  @Override
  public List<Long> getSpacesIdsByUserName(String userId, int offset, int limit) {
    TypedQuery<Long> query = getEntityManager().createNamedQuery("SpaceMember.getSpaceIdentitiesIdByMemberId", Long.class);
    query.setParameter("userId", userId);
    query.setParameter("status", SpaceMemberEntity.Status.MEMBER);
    try {
      if (limit > 0) {
        query.setFirstResult(offset);
        query.setMaxResults(limit);
      }
      return query.getResultList();
    } catch (NoResultException ex) {
      return null;
    }
  }

  @Override
  public List<String> getSpaceMembers(Long spaceId, SpaceMemberEntity.Status status, int offset, int limit) {
    if (status == null) {
      throw new IllegalArgumentException("Status is null");
    }
    if (spaceId == null || spaceId == 0) {
      throw new IllegalArgumentException("spaceId is null or equals to 0");
    }
    if (offset < 0) {
      throw new IllegalArgumentException("offset must be positive");
    }
    if (limit <= 0) {
      throw new IllegalArgumentException("limit must be > 0");
    }
    TypedQuery<String> query = getEntityManager().createNamedQuery("SpaceMember.getSpaceMembersByStatus",
            String.class);
    query.setParameter("status", status);
    query.setParameter("spaceId", spaceId);
    query.setFirstResult(offset);
    query.setMaxResults(limit);
    return query.getResultList();
  }

  @Override
  public List<String> sortSpaceMembers(List<String> usernames, String sortField) {
    if (usernames == null || usernames.isEmpty()) {
      return Collections.emptyList();
    }

    // Oracle Dialect in Hibernate 4 is not registering NVARCHAR correctly, see HHH-10495
    StringBuilder queryStringBuilder =
            isOrcaleDialect() ? new StringBuilder("SELECT to_char(identity_1.remote_id) , to_char(identity_prop.value) \n")
                    :isMSSQLDialect() ? new StringBuilder("SELECT try_convert(varchar(200), identity_1.remote_id) as remote_id , try_convert(varchar(200), identity_prop.value) as identity_prop_value \n")
                    : new StringBuilder("SELECT (identity_1.remote_id) , (identity_prop.value) \n");
    queryStringBuilder.append(" FROM SOC_IDENTITIES identity_1 \n");
    queryStringBuilder.append(" LEFT JOIN SOC_IDENTITY_PROPERTIES identity_prop \n");
    queryStringBuilder.append("   ON identity_1.identity_id = identity_prop.identity_id \n");
    queryStringBuilder.append("       AND identity_prop.name = '").append(sortField).append("' \n");
    queryStringBuilder.append(" WHERE identity_1.remote_id IN (");
    for (int i = 0; i < usernames.size(); i++) {
      if (i > 0) {
        queryStringBuilder.append(",");
      }
      queryStringBuilder.append("'").append(usernames.get(i)).append("'");
    }
    queryStringBuilder.append(")\n");
    queryStringBuilder.append("       AND identity_1.provider_id = '").append(OrganizationIdentityProvider.NAME).append("' \n");
    queryStringBuilder.append(" ORDER BY identity_prop.value ASC, identity_1.remote_id ASC");

    Query query = getEntityManager().createNativeQuery(queryStringBuilder.toString());
    List<?> resultList = query.getResultList();
    List<String> result = new ArrayList<>();
    for (Object object : resultList) {
      Object[] resultEntry = (Object[]) object;
      if (resultEntry[0] == null) {
        continue;
      }
      String username = resultEntry[0].toString();
      result.add(username);
      usernames.remove(username);
    }
    // Add usernames that don't have identities in SOC_IDENTITIES
    for (String username : usernames) {
      result.add(username);
    }
    return result;
  }

  @Override
  public int countSpaceMembers(Long spaceId, SpaceMemberEntity.Status status) {
    if (status == null) {
      throw new IllegalArgumentException("Status is null");
    }
    if (spaceId == null || spaceId == 0) {
      throw new IllegalArgumentException("spaceId is null or equals to 0");
    }
    TypedQuery<Long> query = getEntityManager().createNamedQuery("SpaceMember.countSpaceMembersByStatus", Long.class);
    query.setParameter("status", status);
    query.setParameter("spaceId", spaceId);
    return query.getSingleResult().intValue();
  }

}
