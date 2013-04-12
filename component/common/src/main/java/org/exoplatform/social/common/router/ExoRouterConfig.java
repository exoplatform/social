/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.social.common.router;

import java.util.Iterator;
import java.util.Map;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

public class ExoRouterConfig extends BaseComponentPlugin {

  /**
   * The Logger.
   */
  private static final Log LOG = ExoLogger.getLogger(ExoRouterConfig.class);
  
  /**
   * The associated Action Name and URI Pattern key mapping.
   */
  private Map<String, String> routeMapping;
  
  public ExoRouterConfig() {
  }
  
  public ExoRouterConfig(InitParams initParams) {
    if (initParams == null) {
      LOG.warn("Failed to register this plugin: initParams is null");
      return;
    }
    
    Iterator<ObjectParameter> itr = initParams.getObjectParamIterator();
    if (!itr.hasNext()) {
      LOG.warn("Failed to register this route configuration: no <object-param>");
      return;
    }
    
    ObjectParameter objectParameter = itr.next();
    ExoRouterConfig routeConfig = (ExoRouterConfig) objectParameter.getObject();

    if (routeConfig.getRouteMapping() == null ||
        routeConfig.getRouteMapping().size() == 0) {
      LOG.warn("Failed to register route configuration: no <entry> found for <object-param> config");
      return;
    }
    routeMapping = routeConfig.getRouteMapping();
    
  }
  
  /**
   * Gets the associated activity key type mapping from URI Pattern.
   * @return
   */
  public Map<String, String> getRouteMapping() {
    return routeMapping;
  }

  /**
   * Sets the associated ActionName key type mapping from this URI Pattern.
   *
   * @param mapping the hash map of key as ActionName and value as URI Pattern
   */
  public void setRouteMapping(Map<String, String> mapping) {
    if (mapping == null || mapping.size() == 0) {
      LOG.warn("mapping is null or size = 0");
      return;
    }
    routeMapping = mapping;
  }
}
