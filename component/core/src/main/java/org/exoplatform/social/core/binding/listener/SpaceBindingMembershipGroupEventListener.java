/***************************************************************************	
 * Copyright (C) 2003-2019 eXo Platform SAS.	
 *
 * This program is free software; you can redistribute it and/or	
 * modify it under the terms of the GNU Affero General Public License	
 * as published by the Free Software Foundation; either version 3	
 * of the License, or (at your option) any later version.	
 *
 * This program is distributed in the hope that it will be useful,	
 * but WITHOUT ANY WARRANTY; without even the implied warranty of	
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the	
 * GNU General Public License for more details.	
 *
 * You should have received a copy of the GNU General Public License	
 * along with this program; if not, see<http://www.gnu.org/licenses/>.	
 *
 **************************************************************************/
package org.exoplatform.social.core.binding.listener;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserStatus;
import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.model.GroupSpaceBindingReportAction;
import org.exoplatform.social.core.binding.model.UserSpaceBinding;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class SpaceBindingMembershipGroupEventListener extends MembershipEventListener {
  private static final Log         LOG = ExoLogger.getLogger(SpaceBindingMembershipGroupEventListener.class);

  private GroupSpaceBindingService groupSpaceBindingService;

  private SpaceService             spaceService;

  private OrganizationService      organizationService;

  @Override
  public void postSave(Membership m, boolean isNew) throws Exception {
    String userName = m.getUserName();
    String groupId = m.getGroupId();
    if (isNew && !isASpaceGroup(groupId)) {
      RequestLifeCycle.begin(PortalContainer.getInstance());
      try {
        if (isActive(userName) && isUserNewMemberToGroup(userName, groupId)) {
          groupSpaceBindingService = CommonsUtils.getService(GroupSpaceBindingService.class);
          spaceService = CommonsUtils.getService(SpaceService.class);

          // Retrieve all bindings of the group.
          List<GroupSpaceBinding> groupSpaceBindings = groupSpaceBindingService.findGroupSpaceBindingsByGroup(m.getGroupId());
          // For each bound space of the group add a user binding to it.
          for (GroupSpaceBinding groupSpaceBinding : groupSpaceBindings) {
            Space space = spaceService.getSpaceById(groupSpaceBinding.getSpaceId());
            long startTime = System.currentTimeMillis();
  
           
            
            // Retrieve bindingReportAction of synchronize.
            GroupSpaceBindingReportAction bindingReportAddSynchronizeAction =
                                                                            groupSpaceBindingService.findGroupSpaceBindingReportAction(groupSpaceBinding.getId(),
                                                                                                                                       GroupSpaceBindingReportAction.SYNCHRONIZE_ACTION);
            // If bindingReportAction for synchronize is not already created, create it.
            if (bindingReportAddSynchronizeAction == null) {
              GroupSpaceBindingReportAction report =
                                                   new GroupSpaceBindingReportAction(groupSpaceBinding.getId(),
                                                                                     Long.parseLong(groupSpaceBinding.getSpaceId()),
                                                                                     groupSpaceBinding.getGroup(),
                                                                                     GroupSpaceBindingReportAction.SYNCHRONIZE_ACTION);
              bindingReportAddSynchronizeAction = groupSpaceBindingService.saveGroupSpaceBindingReport(report);
            }

            groupSpaceBindingService.saveUserBinding(userName,
                                                     groupSpaceBinding,
                                                     space,
                                                     bindingReportAddSynchronizeAction);
  
            // Finally save the end date for the bindingReportAction.
            bindingReportAddSynchronizeAction.setEndDate(new Date());
            groupSpaceBindingService.updateGroupSpaceBindingReportAction(bindingReportAddSynchronizeAction);

            
            long totalTime = System.currentTimeMillis() - startTime;
            LOG.info("service={} operation={} parameters=\"space:{},totalSpaceMembers:{},boundSpaceMembers:{}\" status=ok "
                + "duration_ms={}",
                     GroupSpaceBindingService.LOG_SERVICE_NAME,
                     GroupSpaceBindingService.LOG_UPDATE_OPERATION_NAME,
                     space.getPrettyName(),
                     space.getMembers().length,
                     groupSpaceBindingService.countBoundUsers(space.getId()),
                     totalTime);
          }
        }
      } catch (Exception e) {
        LOG.warn("Problem occurred when saving user bindings for user ({}) from group ({}): ", userName, groupId, e);
      } finally {
        RequestLifeCycle.end();
      }
    }
  }
  
  private boolean isActive(String userName) throws Exception {
    organizationService = CommonsUtils.getOrganizationService();
    return organizationService.getUserHandler().findUserByName(userName, UserStatus.ENABLED) != null;
  }
  
  @Override
  public void postDelete(Membership m) throws Exception {
    String userName = m.getUserName();
    String groupId = m.getGroupId();
    if (!isASpaceGroup(groupId)) {
      RequestLifeCycle.begin(PortalContainer.getInstance());
      try {
        if (isActive(userName) && isUserNoMoreMemberOfGroup(userName, groupId)) {
          groupSpaceBindingService = CommonsUtils.getService(GroupSpaceBindingService.class);
          spaceService = CommonsUtils.getService(SpaceService.class);
          // Retrieve removed user's all bindings.
          List<UserSpaceBinding> userSpaceBindings = groupSpaceBindingService.findUserBindingsByGroup(m.getGroupId(),
                                                                                                      m.getUserName());
          // Remove them.
          for (UserSpaceBinding userSpaceBinding : userSpaceBindings) {
            GroupSpaceBinding binding = userSpaceBinding.getGroupBinding();
            Space space = spaceService.getSpaceById(binding.getSpaceId());
            long startTime = System.currentTimeMillis();

            // Retrieve bindingReportAction of synchronize.
            GroupSpaceBindingReportAction bindingReportAddSynchronizeAction =
                                                                            groupSpaceBindingService.findGroupSpaceBindingReportAction(binding.getId(),
                                                                                                                                       GroupSpaceBindingReportAction.SYNCHRONIZE_ACTION);
            // If bindingReportAction for synchronize is not already created, create it.
            if (bindingReportAddSynchronizeAction == null) {
              GroupSpaceBindingReportAction report = new GroupSpaceBindingReportAction(binding.getId(),
                                                                                       Long.parseLong(binding.getSpaceId()),
                                                                                       binding.getGroup(),
                                                                                       GroupSpaceBindingReportAction.SYNCHRONIZE_ACTION);
              bindingReportAddSynchronizeAction = groupSpaceBindingService.saveGroupSpaceBindingReport(report);
            }

            groupSpaceBindingService.deleteUserBinding(userSpaceBinding, bindingReportAddSynchronizeAction);
            // Finally save the end date for the bindingReportAction.
            bindingReportAddSynchronizeAction.setEndDate(new Date());
            groupSpaceBindingService.updateGroupSpaceBindingReportAction(bindingReportAddSynchronizeAction);

            long totalTime = System.currentTimeMillis() - startTime;
            LOG.info("service={} operation={} parameters=\"space:{},totalSpaceMembers:{},boundSpaceMembers:{}\" status=ok "
                + "duration_ms={}",
                     GroupSpaceBindingService.LOG_SERVICE_NAME,
                     GroupSpaceBindingService.LOG_UPDATE_OPERATION_NAME,
                     space.getPrettyName(),
                     space.getMembers().length,
                     groupSpaceBindingService.countBoundUsers(space.getId()),
                     totalTime);
          }
        }
      } catch (Exception e) {
        LOG.warn("Problem occurred when removing user bindings for user ({}): ", userName, e);
      } finally {
        RequestLifeCycle.end();
      }
    }
  }

  private boolean isASpaceGroup(String groupName) {
    return groupName.startsWith("/spaces");
  }

  private boolean isUserNoMoreMemberOfGroup(String userName, String groupId) throws Exception {
    organizationService = CommonsUtils.getOrganizationService();
    Collection<Membership> userMemberships = organizationService.getMembershipHandler()
                                                                .findMembershipsByUserAndGroup(userName, groupId);
    return userMemberships.isEmpty();
  }

  private boolean isUserNewMemberToGroup(String userName, String groupId) throws Exception {
    organizationService = CommonsUtils.getOrganizationService();
    Collection<Membership> userMemberships = organizationService.getMembershipHandler()
                                                                .findMembershipsByUserAndGroup(userName, groupId);
    // If user has more than 1 membership then he is a member before of the group.
    return userMemberships.size() == 1;
  }

}
