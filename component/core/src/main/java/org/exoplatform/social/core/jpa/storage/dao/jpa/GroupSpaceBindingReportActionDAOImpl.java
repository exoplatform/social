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
import org.exoplatform.social.core.jpa.storage.dao.GroupSpaceBindingReportActionDAO;
import org.exoplatform.social.core.jpa.storage.entity.GroupSpaceBindingReportActionEntity;

public class GroupSpaceBindingReportActionDAOImpl extends GenericDAOJPAImpl<GroupSpaceBindingReportActionEntity, Long>
    implements GroupSpaceBindingReportActionDAO {

  

  @Override
  public GroupSpaceBindingReportActionEntity findGroupSpaceBindingReportAction(long bindingId, String action) {
    TypedQuery<GroupSpaceBindingReportActionEntity> query =
                                                          getEntityManager().createNamedQuery("SocGroupSpaceBindingReportAction.findGroupSpaceBindingReportAction",
                                                                                              GroupSpaceBindingReportActionEntity.class);
    query.setParameter("bindingId", bindingId);
    query.setParameter("action", action);
    try {
      return query.getSingleResult();
    } catch (NoResultException ex) {
      return null;
    }
  }

  @Override
  public List<GroupSpaceBindingReportActionEntity> getGroupSpaceBindingReportActionsOrderedByEndDate() {
    TypedQuery<GroupSpaceBindingReportActionEntity> query =
                                                          getEntityManager().createNamedQuery("SocGroupSpaceBindingReportAction.getGroupSpaceBindingReportActions",
                                                                                              GroupSpaceBindingReportActionEntity.class);
    return query.getResultList();
  }
}
