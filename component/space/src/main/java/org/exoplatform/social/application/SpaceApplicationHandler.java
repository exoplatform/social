/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 */
package org.exoplatform.social.application;

import java.util.List;

import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceException;
import org.exoplatform.social.space.spi.SpaceApplicationLifeCycleListener;
import org.exoplatform.social.space.spi.SpaceLifeCycleListener;

/**
 * Created by The eXo Platform SARL
 * Author : dang.tung
 *          tungcnw@gmail.com
 * Oct 17, 2008          
 */

public interface SpaceApplicationHandler {
  /**
   * Initialize HomeSpacePortlet application as a root page node of a space.
   * Add apps to this space.
   * @param space
   * @throws SpaceException
   */
  public void initApp(Space space, String homeNodeApp, List<String> apps) throws SpaceException;
  
  /**
   * De-initialize 
   * @param space
   * @throws SpaceException
   */
  public void deInitApp(Space space) throws SpaceException;
  
  /**
   * Install an application to a space
   * @param space
   * @param appId
   * @throws SpaceException
   */
  public void installApplication(Space space, String appId) throws SpaceException;
  
  /**
   * Activate an installed application in a space
   * @param space
   * @param appId
   * @throws SpaceException
   */
  public void activateApplication(Space space, String appId) throws SpaceException;
  
  /**
   * Deactivate an installed application in a space
   * @param space
   * @param appId
   * @throws SpaceException
   */
  public void deactiveApplication(Space space, String appId) throws SpaceException;
  
  /**
   * Remove an application in a space
   * @param space
   * @param appId
   * @throws SpaceException
   */
  public void removeApplication(Space space, String appId) throws SpaceException;
  
  /**
   * Remove all applications in a space
   * @param space
   * @throws SpaceException
   */
  public void removeApplications(Space space) throws SpaceException;
  
  /**
   * Get name
   * @return
   */
  public String getName();
  

  
}