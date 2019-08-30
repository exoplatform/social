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

import org.apache.commons.lang3.StringUtils;
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
  public List<String> sortSpaceMembers(List<String> userNames,
                                       String firstCharacterFieldName,
                                       char firstCharacter,
                                       String sortField,
                                       String sortDirection) {
    if (userNames == null || userNames.isEmpty()) {
      return Collections.emptyList();
    }

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
      queryStringBuilder =
                         new StringBuilder("SELECT try_convert(varchar(200), identity_1.remote_id) as remote_id , identity_1.identity_id, try_convert(varchar(200) \n");
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
    queryStringBuilder.append(" WHERE identity_1.remote_id IN (");
    for (int i = 0; i < userNames.size(); i++) {
      if (i > 0) {
        queryStringBuilder.append(",");
      }
      queryStringBuilder.append("'").append(userNames.get(i)).append("'");
    }
    queryStringBuilder.append(")\n");
    queryStringBuilder.append(" AND identity_1.deleted = ").append(dbBoolFalse).append(" \n");
    queryStringBuilder.append(" AND identity_1.enabled = ").append(dbBoolTrue).append(" \n");

    if (StringUtils.isNotBlank(sortField) && StringUtils.isNotBlank(sortDirection)) {
      queryStringBuilder.append(" ORDER BY lower(identity_prop.value) " + sortDirection);
    }

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
    }
    return result;
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
    StringBuilder queryStringBuilder = new StringBuilder("SELECT spaceMember.USER_ID \n");
    queryStringBuilder.append(" FROM SOC_SPACES_MEMBERS spaceMember \n");
    queryStringBuilder.append(" INNER JOIN SOC_IDENTITIES identity \n");
    queryStringBuilder.append("   ON spaceMember.USER_ID = identity.REMOTE_ID \n");
    queryStringBuilder.append("       AND identity.ENABLED = TRUE ");
    queryStringBuilder.append("       AND identity.DELETED = FALSE ");
    queryStringBuilder.append("       WHERE spaceMember.STATUS = '").append(status.ordinal()).append("' \n");
    queryStringBuilder.append("       AND spaceMember.SPACE_ID = '").append(spaceId).append("' \n");
    Query query = getEntityManager().createNativeQuery(queryStringBuilder.toString());
    query.setFirstResult(offset);
    query.setMaxResults(limit);
    return query.getResultList();
  }

  @Override
  public int countSpaceMembers(Long spaceId, SpaceMemberEntity.Status status) {
    if (status == null) {
      throw new IllegalArgumentException("Status is null");
    }
    if (spaceId == null || spaceId == 0) {
      throw new IllegalArgumentException("spaceId is null or equals to 0");
    }
    StringBuilder queryStringBuilder = new StringBuilder("SELECT count(*) \n");
    queryStringBuilder.append(" FROM SOC_SPACES_MEMBERS spaceMember \n");
    queryStringBuilder.append(" INNER JOIN SOC_IDENTITIES identity \n");
    queryStringBuilder.append("   ON spaceMember.USER_ID = identity.REMOTE_ID \n");
    queryStringBuilder.append("       AND identity.DELETED = FALSE ");
    queryStringBuilder.append("       AND identity.ENABLED = TRUE ");
    queryStringBuilder.append("       WHERE spaceMember.STATUS = '").append(status.ordinal()).append("' \n");
    queryStringBuilder.append("       AND spaceMember.SPACE_ID = '").append(spaceId).append("' \n");
    Query query = getEntityManager().createNativeQuery(queryStringBuilder.toString());
    return ((Number) query.getSingleResult()).intValue();
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
}
