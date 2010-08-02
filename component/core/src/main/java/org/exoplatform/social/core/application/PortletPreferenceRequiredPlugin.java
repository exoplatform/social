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
package org.exoplatform.social.core.application;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 30, 2010  
 */
public class PortletPreferenceRequiredPlugin extends BaseComponentPlugin {

  /**
   * The initial parameter of this plugin
   */
  private final InitParams params;
  
  public PortletPreferenceRequiredPlugin(InitParams params) {
    this.params = params;
  }
  
  /**
   * @return all the portlet preferences required that associated to this plugin 
   */
  public List<String> getPortletPrefs() {
    Iterator<?> iterator = params.getValuesParamIterator();
    List<String> prefs = null;
    List values = new ArrayList();
    if (iterator != null) {
      while (iterator.hasNext()) {
        ValuesParam valuesParam = (ValuesParam) iterator.next();
        values = valuesParam.getValues();
        for (Object value : values) {
          if (prefs == null) {
            prefs = new ArrayList<String>();
          }
          prefs.add((String)value);  
        }
      }
    }
    
    return prefs;
  }
}
