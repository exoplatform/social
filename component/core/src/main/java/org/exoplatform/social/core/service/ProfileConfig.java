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
package org.exoplatform.social.core.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.storage.api.IdentityStorage;


/**
 * The Class ProfileConfig.
 */
public class ProfileConfig {

  /** The force multi value. */
  private List<String> forceMultiValue = new ArrayList<String>();

  /** The node types. */
  private Map<String, String> nodeTypes = new HashMap<String, String>();

  /** The activityStorage. */
  private IdentityStorage storage = null;

  private static final Log LOG = ExoLogger.getLogger(ProfileConfig.class);
  
  /**
   * Instantiates a new profile config.
   *
   * @param params the params
   */
  public ProfileConfig(InitParams params) {
    Iterator it = params.getValuesParamIterator();
    while(it.hasNext()) {
      ValuesParam param = (ValuesParam) it.next();
      String name = param.getName();
      if(name.startsWith("nodetype.")) {
        String key = name.substring(9);
        nodeTypes.put(key, param.getValue());
      }
      if(name.equals("forceMultiValue")) {
        forceMultiValue = param.getValues();
      }
    }
  }

  /**
   * Gets the force multi value.
   *
   * @return the force multi value
   */
  public List getForceMultiValue() {
    return forceMultiValue;
  }

  /**
   * Sets the force multi value.
   *
   * @param forceMultiValue the new force multi value
   */
  public void setForceMultiValue(List forceMultiValue) {
    this.forceMultiValue = forceMultiValue;
  }

  /**
   * Checks if is forced multi value.
   *
   * @param fieldName the field name
   * @return true, if is forced multi value
   */
  public boolean isForcedMultiValue(String fieldName) {
    return this.forceMultiValue.contains(fieldName);
  }

  /**
   * Gets the node types.
   *
   * @return the node types
   */
  public Map<String, String> getNodeTypes() {
    return nodeTypes;
  }

  /**
   * Gets the node type.
   *
   * @param fieldName the field name
   * @return the node type
   */
  public String getNodeType(String fieldName) {
    return nodeTypes.get(fieldName);
  }

  /**
   * Sets the node types.
   *
   * @param nodeTypes the node types
   */
  public void setNodeTypes(Map<String, String> nodeTypes) {
    this.nodeTypes = nodeTypes;
  }

  /**
   * Gets the type.
   *
   * @param fieldName the field name
   * @param propertyName the property name
   * @return the type
   */
  public String getType(String fieldName, String propertyName) {
    if (this.storage == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      IdentityManager im = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);
      this.storage = im.getIdentityStorage();
    }
    try {
      String type = storage.getType(fieldName, propertyName);
      if(type != null)
        return type;
    } catch (Exception e) {
      LOG.debug("Could not get type of property. Return String as default.");
    }

    return "String";
  }
}
