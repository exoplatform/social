/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
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
package org.exoplatform.social.common.service.thread;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class ThreadPoolConfig implements Serializable, Cloneable {

  private static final long serialVersionUID = 1L;
  
  private String id;
  private Boolean defaultProfile;
  private Integer poolSize;
  private Integer maxPoolSize;
  private Long keepAliveTime;
  private TimeUnit timeUnit;
  private Integer maxQueueSize;
  
  public ThreadPoolConfig() {}
  
  public ThreadPoolConfig(String id) {
    this.id = id;
  }
  

  /**
   * Gets the id of this profile
   *
   * @return the id of this profile
   */
  public String getId() {
      return id;
  }

  /**
   * Sets the id of this profile
   *
   * @param id profile id
   */
  public void setId(String id) {
      this.id = id;
  }

  /**
   * Whether this profile is the default profile (there can only be one).
   *
   * @return <tt>true</tt> if its the default profile, <tt>false</tt> otherwise
   */
  public Boolean isDefaultProfile() {
      return defaultProfile != null && defaultProfile;
  }

  /**
   * Sets whether this profile is the default profile (there can only be one).
   *
   * @param defaultProfile the option
   */
  public void setDefaultProfile(Boolean defaultProfile) {
      this.defaultProfile = defaultProfile;
  }

  /**
   * Gets the core pool size (threads to keep minimum in pool)
   *
   * @return the pool size
   */
  public Integer getPoolSize() {
      return poolSize;
  }

  /**
   * Sets the core pool size (threads to keep minimum in pool)
   *
   * @param poolSize the pool size
   */
  public void setPoolSize(Integer poolSize) {
      this.poolSize = poolSize;
  }

  /**
   * Gets the maximum pool size
   *
   * @return the maximum pool size
   */
  public Integer getMaxPoolSize() {
      return maxPoolSize;
  }

  /**
   * Sets the maximum pool size
   *
   * @param maxPoolSize the max pool size
   */
  public void setMaxPoolSize(Integer maxPoolSize) {
      this.maxPoolSize = maxPoolSize;
  }

  /**
   * Gets the keep alive time for inactive threads
   *
   * @return the keep alive time
   */
  public Long getKeepAliveTime() {
      return keepAliveTime;
  }

  /**
   * Sets the keep alive time for inactive threads
   *
   * @param keepAliveTime the keep alive time
   */
  public void setKeepAliveTime(Long keepAliveTime) {
      this.keepAliveTime = keepAliveTime;
  }

  /**
   * Gets the time unit used for keep alive time
   *
   * @return the time unit
   */
  public TimeUnit getTimeUnit() {
      return timeUnit;
  }

  /**
   * Sets the time unit used for keep alive time
   *
   * @param timeUnit the time unit
   */
  public void setTimeUnit(TimeUnit timeUnit) {
      this.timeUnit = timeUnit;
  }

  /**
   * Gets the maximum number of tasks in the work queue.
   * <p/>
   * Use <tt>-1</tt> or <tt>Integer.MAX_VALUE</tt> for an unbounded queue
   *
   * @return the max queue size
   */
  public Integer getMaxQueueSize() {
      return maxQueueSize;
  }

  /**
   * Sets the maximum number of tasks in the work queue.
   * <p/>
   * Use <tt>-1</tt> or <tt>Integer.MAX_VALUE</tt> for an unbounded queue
   *
   * @param maxQueueSize the max queue size
   */
  public void setMaxQueueSize(Integer maxQueueSize) {
      this.maxQueueSize = maxQueueSize;
  }
  
  
}
