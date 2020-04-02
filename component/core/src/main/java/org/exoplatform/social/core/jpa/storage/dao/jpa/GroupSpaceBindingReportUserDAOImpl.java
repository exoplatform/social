package org.exoplatform.social.core.jpa.storage.dao.jpa;

import java.util.List;

import javax.persistence.TypedQuery;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.social.core.binding.model.GroupSpaceBindingReportUser;
import org.exoplatform.social.core.jpa.storage.dao.GroupSpaceBindingReportUserDAO;
import org.exoplatform.social.core.jpa.storage.entity.GroupSpaceBindingReportActionEntity;
import org.exoplatform.social.core.jpa.storage.entity.GroupSpaceBindingReportUserEntity;

public class GroupSpaceBindingReportUserDAOImpl extends GenericDAOJPAImpl<GroupSpaceBindingReportUserEntity, Long>
    implements GroupSpaceBindingReportUserDAO {
  @Override
  public List<GroupSpaceBindingReportUserEntity> findBindingReportUsersByBindingReportAction(long bindingReportActionId) {
    TypedQuery<GroupSpaceBindingReportUserEntity> query =
                                                        getEntityManager().createNamedQuery("SocGroupSpaceBindingReportUser.findBindingReportUsersByBindingReportAction",
                                                                                            GroupSpaceBindingReportUserEntity.class);
    query.setParameter("bindingReportActionId", bindingReportActionId);
    return query.getResultList();
  }
  
  @Override
  public List<GroupSpaceBindingReportUserEntity> findReportsForCSV(long spaceId,
                                                                    long groupSpaceBindingId,
                                                                    String group,
                                                                    String action) {
    TypedQuery<GroupSpaceBindingReportUserEntity> query =
        getEntityManager().createNamedQuery("SocGroupSpaceBindingReportUser.findReportForCSV",
                                            GroupSpaceBindingReportUserEntity.class);
    query.setParameter("spaceId", spaceId);
    query.setParameter("groupSpaceBindingId", groupSpaceBindingId);
    query.setParameter("group", group);
    query.setParameter("action", action);
    return query.getResultList();
  }
}
