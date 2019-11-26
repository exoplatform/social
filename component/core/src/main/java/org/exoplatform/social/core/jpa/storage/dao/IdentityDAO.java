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

package org.exoplatform.social.core.jpa.storage.dao;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.jpa.search.ExtendProfileFilter;
import org.exoplatform.social.core.jpa.storage.entity.ConnectionEntity;
import org.exoplatform.social.core.jpa.storage.entity.IdentityEntity;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:tuyennt@exoplatform.com">Tuyen Nguyen The</a>.
 */
public interface IdentityDAO extends GenericDAO<IdentityEntity, Long> {
  IdentityEntity findByProviderAndRemoteId(String providerId, String remoteId);
  long countIdentityByProvider(String providerId);

  ListAccess<IdentityEntity> findIdentities(ExtendProfileFilter filter);

  List<Long> getAllIds(int offset, int limit);  

  List<Long> getAllIdsByProvider(String providerId, int offset, int limit);

  ListAccess<Map.Entry<IdentityEntity, ConnectionEntity>> findAllIdentitiesWithConnections(long identityId,
                                                                                           String firstCharacterFieldName,
                                                                                           char firstCharacter,
                                                                                           String sortField,
                                                                                           String sortDirection);

  /**
   * set the DELETED flag to true
   * @param identityId the identity Id
   */
  void setAsDeleted(long identityId);

  /**
   * delete definitely an identity
   * @param identityId the identity Id
   */
  void hardDeleteIdentity(long identityId);

  /**
   * Get all identities by providerId sorted by sortField
   * 
   * @param providerId
   * @param firstCharacterFieldName
   * @param firstCharacter
   * @param sortField
   * @param sortDirection
   * @param offset
   * @param limit
   * @return
   */
  List<String> getAllIdsByProviderSorted(String providerId, String firstCharacterFieldName, char firstCharacter, String sortField, String sortDirection, long offset, long limit);
}
