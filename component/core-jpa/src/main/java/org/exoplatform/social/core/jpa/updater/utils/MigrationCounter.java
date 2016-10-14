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
package org.exoplatform.social.core.jpa.updater.utils;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jun 29, 2015  
 */
public class MigrationCounter {
  
  private int batch = 0;
  private long batchWatch = 0;
  private int total = 0;
  private long totalWatch = 0;
  private int threshold = 0;
  
  public static MigrationCounterBuilder builder() {
    return new MigrationCounterBuilder(); 
  }
  
  public int getAndIncrementTotal() {
    this.total++;
    return total;
  }
  
  public int getAndIncrementBatch() {
    this.batch++;
    return this.batch;
  }
  
  public void newBatchAndWatch() {
    this.batch = 0;
    this.batchWatch = System.currentTimeMillis();
  }
  
  public long endBatchWatch() {
    return System.currentTimeMillis() - this.batchWatch;
  }
  
  public int getBatch() {
    return this.batch;
  }
  
  public void newTotal() {
    this.total = 0;
  }
  
  public void newTotalAndWatch() {
    this.total = 0;
    this.totalWatch = System.currentTimeMillis();
  }
  
  public long endTotalWatch() {
    return System.currentTimeMillis() - this.totalWatch;
  }
  
  public int getTotal() {
    return this.total;
  }
  
  public void newBatch() {
    this.batch = 0;
  }
  
  public boolean isPersistPoint() {
    return (total % threshold == 0);
  }
  
  public void resetBatch() {
    this.batch = 0;
  }
  
  public void reset() {
    this.batch = 0;
    this.total = 0;
    this.batchWatch = 0;
    this.totalWatch = 0;
  }

  public static class MigrationCounterBuilder {
    private int batch = 0;
    private int threshold = 0;
    
    public MigrationCounterBuilder startAtBatch(int fromBatch) {
      batch += fromBatch; 
      return this;
    }
    
    public MigrationCounterBuilder threshold(int threshold) {
      this.threshold = threshold; 
      return this;
    }
    
    public MigrationCounter build() {
      MigrationCounter counter = new MigrationCounter();
      counter.batch = this.batch;
      counter.threshold = this.threshold;
      return counter;
    }
    
  }

}
