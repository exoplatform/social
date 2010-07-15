/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.social.common.jcr;

import org.exoplatform.container.xml.InitParams;


/**
 * The Class DataLocationPlugin.
 */
public class DataLocationPlugin extends ManagedPlugin {

  /** The workspace. */
  private String workspace;

  /**
   * Instantiates a new data location plugin.
   *
   * @param params the params
   * @throws Exception the exception
   */
  public DataLocationPlugin(InitParams params) throws Exception {
    this.workspace = params.getValueParam("workspace").getValue();
  }

  /**
   * Gets the workspace.
   *
   * @return the workspace
   */
  public String getWorkspace() {
    return workspace;
  }

}
