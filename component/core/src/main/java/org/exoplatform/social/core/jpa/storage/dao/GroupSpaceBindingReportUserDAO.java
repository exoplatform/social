package org.exoplatform.social.core.jpa.storage.dao;

import java.util.List;

import org.exoplatform.commons.api.persistence.GenericDAO;
import org.exoplatform.social.core.jpa.storage.entity.GroupSpaceBindingReportActionEntity;
import org.exoplatform.social.core.jpa.storage.entity.GroupSpaceBindingReportUserEntity;

public interface GroupSpaceBindingReportUserDAO extends GenericDAO<GroupSpaceBindingReportUserEntity, Long> {
  /**
   * Gets all BindingReportUser by BindingReportAction
   * 
   * @param bindingReportActionId
   * @return
   */
  List<GroupSpaceBindingReportUserEntity> findBindingReportUsersByBindingReportAction(long bindingReportActionId);
  
  /**
   * Get reports for csv generation
   *
   * @param spaceId
   * @param groupSpaceBindingId
   * @param group
   * @param action
   * @return
   */
  List<GroupSpaceBindingReportUserEntity> findReportsForCSV(long spaceId,
                                                              long groupSpaceBindingId,
                                                              String group,
                                                              String action);
}
