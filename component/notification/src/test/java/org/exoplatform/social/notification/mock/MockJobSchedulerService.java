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
package org.exoplatform.social.notification.mock;

import java.util.Date;
import java.util.List;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.scheduler.AddJobListenerComponentPlugin;
import org.exoplatform.services.scheduler.AddTriggerListenerComponentPlugin;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.PeriodInfo;
import org.exoplatform.services.scheduler.Task;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobListener;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerListener;

public class MockJobSchedulerService implements JobSchedulerService {

  @Override
  public void addJob(JobInfo jinfo, Date date) throws Exception {

  }

  @Override
  public void addPeriodJob(JobInfo jinfo, PeriodInfo pinfo) throws Exception {

  }

  @Override
  public void addPeriodJob(ComponentPlugin plugin) throws Exception {

  }

  @Override
  public void addCronJob(JobInfo jinfo, String exp) throws Exception {

  }

  @Override
  public void addCronJob(ComponentPlugin plugin) throws Exception {

  }

  @Override
  public boolean removeJob(JobInfo jinfo) throws Exception {
    return false;
  }

  @Override
  public void addPeriodJob(JobInfo jinfo, PeriodInfo pinfo, JobDataMap jdatamap) throws Exception {

  }

  @Override
  public void addCronJob(JobInfo jinfo, String exp, JobDataMap jdatamap) throws Exception {

  }

  @Override
  public void executeJob(String jname, String jgroup, JobDataMap jdatamap) throws Exception {

  }

  @Override
  public void addGlobalJobListener(ComponentPlugin plugin) throws Exception {

  }

  @Override
  public List<JobListener> getAllGlobalJobListener() throws Exception {
    return null;
  }

  @Override
  public JobListener getGlobalJobListener(String name) throws Exception {
    return null;
  }

  @Override
  public boolean removeGlobalJobListener(String name) throws Exception {
    return false;
  }

  @Override
  public void addJobListener(AddJobListenerComponentPlugin plugin) throws Exception {

  }

  @Override
  public List<JobListener> getAllJobListener() throws Exception {
    return null;
  }

  @Override
  public JobListener getJobListener(String name) throws Exception {
    return null;
  }

  @Override
  public boolean removeJobListener(String name) throws Exception {
    return false;
  }

  @Override
  public void addGlobalTriggerListener(ComponentPlugin plugin) throws Exception {

  }

  @Override
  public List<TriggerListener> getAllGlobalTriggerListener() throws Exception {
    return null;
  }

  @Override
  public TriggerListener getGlobalTriggerListener(String name) throws Exception {
    return null;
  }

  @Override
  public boolean removeGlobaTriggerListener(String name) throws Exception {
    return false;
  }

  @Override
  public TriggerState getTriggerState(String jobName, String groupName) throws Exception {
    return null;
  }

  @Override
  public void addTriggerListener(AddTriggerListenerComponentPlugin plugin) throws Exception {

  }

  @Override
  public List<TriggerListener> getAllTriggerListener() throws Exception {
    return null;
  }

  @Override
  public TriggerListener getTriggerListener(String name) throws Exception {
    return null;
  }

  @Override
  public boolean removeTriggerListener(String name) throws Exception {
    return false;
  }

  @Override
  public void queueTask(Task task) {

  }

  @Override
  public List<JobExecutionContext> getAllExcutingJobs() throws Exception {
    return null;
  }

  @Override
  public List<JobDetail> getAllJobs() throws Exception {
    return null;
  }

  @Override
  public void pauseJob(String jobName, String groupName) throws Exception {

  }

  @Override
  public void resumeJob(String jobName, String groupName) throws Exception {

  }

  @Override
  public Trigger[] getTriggersOfJob(String jobName, String groupName) throws Exception {
    return null;
  }

  @Override
  public Date rescheduleJob(String jobName, String groupName, Trigger newTrigger) throws Exception {
    return null;
  }

  @Override
  public JobDetail getJob(JobInfo jobInfo) throws Exception {
    return null;
  }

  @Override
  public boolean suspend() {
    return false;
  }

  @Override
  public boolean resume() {
    return false;
  }

}