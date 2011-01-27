/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.social.core.space.spi;

import org.exoplatform.social.core.space.SpaceApplicationConfigPlugin;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.model.Space;

/**
 * Handler for working with space applications: install, activate, deactivate, uninstall...
 *
 * @author <a href="mailto:tungcnw@gmail.com">dang.tung</a>
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since  Oct 17, 2008
 */

public interface SpaceApplicationHandler {

  /**
   * Initializes home space applications and space applications.
   *
   * @param space
   * @param spaceApplicationConfigPlugin
   * @throws SpaceException
   * @since 1.2.0-GA
   */
  public void initApps(Space space, SpaceApplicationConfigPlugin spaceApplicationConfigPlugin) throws SpaceException;

  /**
   * De-initializes a space application.
   *
   * @param space
   * @throws SpaceException
   */
  public void deInitApp(Space space) throws SpaceException;

  /**
   * Installs an application to a space.
   *
   * @param space
   * @param appId
   * @throws SpaceException
   */
  public void installApplication(Space space, String appId) throws SpaceException;

  /**
   * Activates an installed application in a space.
   *
   * @param space
   * @param appId
   * @throws SpaceException
   */
  public void activateApplication(Space space, String appId, String appName) throws SpaceException;

  /**
   * Deactivates an installed application in a space.
   *
   * @param space
   * @param appId
   * @throws SpaceException
   */
  public void deactiveApplication(Space space, String appId) throws SpaceException;

  /**
   * Removes an application in a space.
   *
   * @param space
   * @param appId
   * @throws SpaceException
   */
  public void removeApplication(Space space, String appId, String appName) throws SpaceException;

  /**
   * Remove all applications in a space.
   *
   * @param space
   * @throws SpaceException
   */
  public void removeApplications(Space space) throws SpaceException;

  /**
   * Gets name.
   *
   * @return
   */
  public String getName();

}