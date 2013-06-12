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
package org.exoplatform.social.common.service;

import java.util.List;

public interface SocialServiceContext {

  /**
   * Returns the lifecycle services used to handle lifecycle notifications
   *
   * @return the lifecycle services
   */
  List<LifecycleService> getLifecycleServices();

  /**
   * Adds the given lifecycle services to be used.
   *
   * @param lifecycleServices the service
   */
  void addLifecycleService(LifecycleService lifecycleService);
  
  SocialServiceExecutor getServiceExecutor();
  
  ExecutorServiceManager getExecutorServiceManager();
}
