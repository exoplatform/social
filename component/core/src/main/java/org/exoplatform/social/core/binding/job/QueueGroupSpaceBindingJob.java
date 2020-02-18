/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU Affero General Public License
* as published by the Free Software Foundation; either version 3
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.binding.job;

import org.exoplatform.commons.search.index.IndexingOperationProcessor;
import org.exoplatform.commons.utils.CommonsUtils;
import org.exoplatform.social.core.binding.spi.GroupSpaceBindingService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;


import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

@DisallowConcurrentExecution
public class QueueGroupSpaceBindingJob implements Job {
  private static final Log LOG = ExoLogger.getLogger(QueueGroupSpaceBindingJob.class);

  private GroupSpaceBindingService groupBindingService;
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    LOG.info("Start treating GroupSpaceBinding queue");
    GroupSpaceBindingQueue firstBindingQueue = null;
    do {
      try {
        firstBindingQueue=groupSpaceBindingService.findFirstGroupSpaceBindingQueue();
        if (firstBindingQueue != null) {
          // Get first binding from groupSpaceBindingQueue.
          GroupSpaceBinding firstBindingInBindingQueue = firstBindingQueue.getGroupSpaceBinding();
          LOG.info("Proceeding binding between space with ID: {} and group: {}",
                   firstBindingInBindingQueue.getSpaceId(),
                   firstBindingInBindingQueue.getGroup());
          // Bind users to space.
          groupSpaceBindingService.bindUsersFromGroupSpaceBinding(firstBindingInBindingQueue);
      
          // If totally proceeded remove it from groupSpaceBindingQueue.
          groupSpaceBindingService.deleteFromBindingQueue(firstBindingQueue);
        }
      } catch (Exception e) {
        LOG.error("Failed to treat GroupSpaceBinding queue", e);
      }
    } while (firstBindingQueue!=null);
    LOG.info("End treating GroupSpaceBinding queue");
  }
  
  private GroupSpaceBindingService getGroupBindingService() {
    if(groupBindingService == null) {
      groupBindingService = CommonsUtils.getService(GroupSpaceBindingService.class);
    }
    return groupBindingService;
  }

}
