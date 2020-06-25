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
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.social.core.binding.model.GroupSpaceBinding;
import org.exoplatform.social.core.binding.model.GroupSpaceBindingReportAction;
import org.exoplatform.social.core.binding.model.UserSpaceBinding;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

public class SpaceBindingUserEventListener extends UserEventListener {
  private static final Log         LOG = ExoLogger.getLogger(SpaceBindingUserEventListener.class);

  private GroupSpaceBindingService groupSpaceBindingService;
  private SpaceService spaceService;
  
  @Override
  public void preSetEnabled(User user) throws Exception {
    if(!user.isEnabled()) {
      //user will be disabled
      //remove all userBindings as he was deleted
      removeAllUserBindings(user);
    }
  }
  
  @Override
  public void postSetEnabled(User user) throws Exception {
    if(user.isEnabled()) {
      //user was just enabled
      //readd userBindings
      createAllUserBindings(user);
    }
  }
  
  @Override
  public void postDelete(User user) throws Exception {
    removeAllUserBindings(user);
  }
  
  private void createAllUserBindings(User user) {
    RequestLifeCycle.begin(PortalContainer.getInstance());
    try {
      //get all his non space groups
      //for each check if a groupBinding exists
      //saveUserBinding for this groupBinding
      groupSpaceBindingService = CommonsUtils.getService(GroupSpaceBindingService.class);
      spaceService = CommonsUtils.getService(SpaceService.class);
      OrganizationService organizationService = CommonsUtils.getService(OrganizationService.class);
      Collection<Group> groups= organizationService.getGroupHandler().findGroupsOfUser(user.getUserName());
      
      groups.stream().filter(group -> !isASpaceGroup(group.getGroupName())).forEach(group -> {
        // Retrieve all bindings of the group.
        List<GroupSpaceBinding> groupSpaceBindings =
            groupSpaceBindingService.findGroupSpaceBindingsByGroup(group.getId());
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
  
          groupSpaceBindingService.saveUserBinding(user.getUserName(),
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

      });
    } catch (Exception e) {
      LOG.warn("Problem occurred when creating user bindings for user ({}): ", user.getUserName(), e);
    } finally {
      RequestLifeCycle.end();
    }
  }
  
  private void removeAllUserBindings(User user) {
    RequestLifeCycle.begin(PortalContainer.getInstance());
    try {
      groupSpaceBindingService = CommonsUtils.getService(GroupSpaceBindingService.class);
      spaceService = CommonsUtils.getService(SpaceService.class);
      // Get all user bindings.
      List<UserSpaceBinding> userSpaceBindings = groupSpaceBindingService.findUserBindingsByUser(user.getUserName());
      // Remove all user's bindings.
      for (UserSpaceBinding userSpaceBinding : userSpaceBindings) {
        Space space = spaceService.getSpaceById(userSpaceBinding.getGroupBinding().getSpaceId());
        long startTime=System.currentTimeMillis();
        
        // Retrieve bindingReportAction of synchronize.
        GroupSpaceBindingReportAction bindingReportAddSynchronizeAction =
            groupSpaceBindingService.findGroupSpaceBindingReportAction(userSpaceBinding.getGroupBinding().getId(),
                                                                       GroupSpaceBindingReportAction.SYNCHRONIZE_ACTION);
        // If bindingReportAction for synchronize is not already created, create it.
        if (bindingReportAddSynchronizeAction == null) {
          GroupSpaceBindingReportAction report = new GroupSpaceBindingReportAction(userSpaceBinding.getGroupBinding().getId(),
                                                                                   Long.parseLong(userSpaceBinding.getGroupBinding().getSpaceId()),
                                                                                   userSpaceBinding.getGroupBinding().getGroup(),
                                                                                   GroupSpaceBindingReportAction.SYNCHRONIZE_ACTION);
          bindingReportAddSynchronizeAction = groupSpaceBindingService.saveGroupSpaceBindingReport(report);
        }
        groupSpaceBindingService.deleteUserBinding(userSpaceBinding, bindingReportAddSynchronizeAction);
        // Finally save the end date for the bindingReportAction.
        bindingReportAddSynchronizeAction.setEndDate(new Date());
        groupSpaceBindingService.updateGroupSpaceBindingReportAction(bindingReportAddSynchronizeAction);
  
        long totalTime=System.currentTimeMillis() - startTime;
        LOG.info("service={} operation={} parameters=\"space:{},totalSpaceMembers:{},boundSpaceMembers:{}\" status=ok "
                     + "duration_ms={}",
                 GroupSpaceBindingService.LOG_SERVICE_NAME, GroupSpaceBindingService.LOG_UPDATE_OPERATION_NAME,
                 space.getPrettyName(),
                 space.getMembers().length,
                 groupSpaceBindingService.countBoundUsers(space.getId()),
                 totalTime);
      }
    } catch (Exception e) {
      LOG.warn("Problem occurred when removing user bindings for user ({}): ", user.getUserName(), e);
    } finally {
      RequestLifeCycle.end();
    }
  }
  
  private boolean isASpaceGroup(String groupName) {
    return groupName.startsWith("/spaces");
  }
}
