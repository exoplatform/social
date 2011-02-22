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
 * Plugin for configuring portlets that let Social to store spaceUrl in its portlet-preference.
 *
 * @since Jul 30, 2010
 * @deprecated Use preferences configuration from {@link org.exoplatform.social.core.space.SpaceApplicationConfigPlugin.SpaceApplication} instead.
 * Tobe removed at 1.3.x
 */
public class PortletPreferenceRequiredPlugin extends BaseComponentPlugin {

  /**
   * The initial parameters of this plugin
   */
  private final InitParams params;

  /**
   * Constructor.
   *
   * @param params
   */
  public PortletPreferenceRequiredPlugin(InitParams params) {
    this.params = params;
  }
  
  /**
   * Gets the list of portlets.
   *
   * @return all the portlet preferences required that associated to this plugin 
   */
  public List<String> getPortletPrefs() {
    Iterator<ValuesParam> valuesParamIterator = params.getValuesParamIterator();
    List<String> prefs = new ArrayList<String>();
    if (valuesParamIterator != null) {
      while (valuesParamIterator.hasNext()) {
        ValuesParam valuesParam = valuesParamIterator.next();
        prefs.addAll(valuesParam.getValues());
      }
    }
    return prefs;
  }
}
