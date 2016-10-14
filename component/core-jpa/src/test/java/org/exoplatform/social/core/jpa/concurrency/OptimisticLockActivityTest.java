/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.social.core.jpa.concurrency;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * May 28, 2015  
 */
public class OptimisticLockActivityTest extends AbstractLockOptimisticModeTest {
  
  private final Log LOG = ExoLogger.getLogger(OptimisticLockActivityTest.class);

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }
  
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }
  
  public void testInsertComment() throws Exception {
    assertNotNull(createdActivity);
    
    doInTransaction(new TransactionVoidCallable() {
      @Override
      public void execute() {
        ExoSocialActivity comment = oneOfComment("root comment 1", rootIdentity);
        activityStorage.saveComment(createdActivity, comment);
      }
    });
    
    doInTransaction(new TransactionVoidCallable() {
      @Override
      public void execute() {
        ExoSocialActivity comment = oneOfComment("root comment 2", rootIdentity);
        activityStorage.saveComment(createdActivity, comment);
        
      }
    });
    
    doInTransaction(new TransactionVoidCallable() {
      @Override
      public void execute() {
        ExoSocialActivity comment = oneOfComment("root comment 3", rootIdentity);
        comment.isComment(true);
        activityStorage.saveComment(createdActivity, comment);
        
      }
    });
    
    List<ExoSocialActivity> got = activityStorage.getComments(createdActivity, 0, 10);
    assertEquals(3, got.size());
  }
  
  /**
   * Scenario:
   * 1. Sync: Gets Activity by ID from EntityManager by Synchronous
   * 2. Async: Gets the Activity by ID from EntityManaget, updated the title
   * 3. Updated the title of Activity at step #1
   * 
   * @throws Exception
   */
  public void testImplicitOptimisticLocking() throws Exception {
    final CountDownLatch latch = new CountDownLatch(1); 
    doInTransaction(new TransactionVoidCallable() {
      @Override
      public void execute() {
        //final ExoSocialActivity got = activityStorage.getActivity(createdActivity.getId());
        try {
          executeSync(new VoidCallable() {
            @Override
            public void execute() {
              doInTransaction(new TransactionVoidCallable() {
                @Override
                public void execute() {
                  ExoSocialActivity _got = activityStorage.getActivity(createdActivity.getId());
                  _got.setTitle("Activity title updated 1");
                  activityStorage.updateActivity(_got);
                  LOG.info("Updated the title as 'Activity title updated 1'");
                }
                
                @Override
                protected void afterTransactionCompletion() {
                  LOG.info("Starts to assert the activity title in asynchronous mode.");
                  ExoSocialActivity latest = activityStorage.getActivity(createdActivity.getId());
                  assertEquals("Activity title updated 1", latest.getTitle());
                  latch.countDown();
                }
              });
            }
          });
        } catch (Exception e) {
          fail(e.getMessage());
        }
      }
    });
    
    doInTransaction(new TransactionVoidCallable() {

      @Override
      protected void beforeTransactionCompletion() {
        try {
          LOG.info("CountDownLatch waiting here!");
          latch.await();
        } catch (InterruptedException rex) {
          LOG.error(rex);
        }
      }

      @Override
      public void execute() {
        //Here is opening new EntityManager and get latest the status of Activity entity
        LOG.info("Starts to assert the activity title in synchronous mode.");
        ExoSocialActivity latest = activityStorage.getActivity(createdActivity.getId());
        assertEquals("Activity title updated 1", latest.getTitle());
      }
    });
  }
}
