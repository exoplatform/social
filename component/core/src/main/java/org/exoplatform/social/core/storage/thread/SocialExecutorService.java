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
package org.exoplatform.social.core.storage.thread;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class SocialExecutorService<T> {
  
  private final int DEFAULT_THREAD_NUMBER = 30;
  
  private ExecutorService executor; 
  
  Map<String, Future<T>> futureCollections = new HashMap<String, Future<T>>();
  
  private String lastId = "";
  
  /** Logger */
  private static final Log LOG = ExoLogger.getLogger(SocialExecutorService.class);
  
  public SocialExecutorService(int threadConfigNumber) {
    if (threadConfigNumber > 0)
      this.executor = Executors.newFixedThreadPool(threadConfigNumber);
    else 
      this.executor = Executors.newFixedThreadPool(DEFAULT_THREAD_NUMBER);
  }

  public ExecutorService getExecutor() {
    return executor;
  }

  public void setExecutor(ExecutorService executor) {
    this.executor = executor;
  }
  
  public T submit(Callable<T> task, String id) {
    try {
      futureCollections.put(id, this.executor.submit(task));
      //
      lastId = id;
      
    } catch (Exception e) {
      LOG.warn("Execution task fail." + e);
    }
    
    return null;
  }
  
  public void shutdown() {
    
    try {
      // Disable new tasks from being submitted
      this.executor.shutdown();
      // Wait a while for existing tasks to terminate
      if (!this.executor.awaitTermination(1, TimeUnit.SECONDS)) {
        
        this.executor.shutdownNow(); 
        
        // Wait a while for tasks to respond to being cancelled
        if (!this.executor.awaitTermination(1, TimeUnit.SECONDS)) {
          LOG.warn("Pool did not terminate");
        }
      }
      
    } catch (Exception e) {
      LOG.warn("Could not shutdown executor." + e);
    } finally {
      //release resource
      this.futureCollections = null;
      this.executor = null;
    }
    
  }

  public Map<String, Future<T>> getFutureCollections() {

    try {

      Future<T> ft = this.futureCollections.get(lastId);

      if ( ft.get() != null ) {
        Map<String, Future<T>> result = new HashMap<String, Future<T>>(this.futureCollections);
        
        //clear map
        this.futureCollections.clear();
        
        return result;
      }

    } catch (NullPointerException e) {
      return Collections.emptyMap();
    } catch (Exception e) {
      LOG.warn("Gets Future Collections fail. " + e);
    }

    return Collections.emptyMap();
  }

}
