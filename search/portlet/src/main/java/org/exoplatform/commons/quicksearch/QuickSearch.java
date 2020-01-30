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
package org.exoplatform.commons.quicksearch;


import juzu.Path;
import juzu.Response;
import juzu.View;
import juzu.bridge.portlet.JuzuPortlet;
import juzu.impl.request.Request;
import juzu.request.ApplicationContext;
import juzu.request.RequestContext;
import juzu.request.UserContext;
import juzu.template.Template;

import javax.inject.Inject;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;

/**
 * Created by The eXo Platform SAS
 * Author : Canh Pham Van
 *          canhpv@exoplatform.com
 * Nov 26, 2012  
 */
public class QuickSearch {

  @Inject
  @Path("index.gtmpl")
  Template index;
  
  @Inject
  @Path("edit.gtmpl")
  Template edit;
  
  @Inject
  PortletPreferences portletPreferences;  
  
  @Inject
  SettingService settingService;

  @Inject
  ResourceBundle bundle;  

  
  @View
  public Response.Content index(RequestContext requestContext){
    Map<String, Object> parameters = new HashMap<String, Object>();
    QuickSearch_.index().setProperty(JuzuPortlet.PORTLET_MODE, PortletMode.EDIT);
    PortletMode mode = requestContext.getProperty(JuzuPortlet.PORTLET_MODE);
    SettingValue<?> resultsPerPageSettingValue = settingService.get(Context.GLOBAL, Scope.WINDOWS, "resultsPerPage");
    if (resultsPerPageSettingValue == null) {
      String resultsPerPage = portletPreferences.getValue("resultsPerPage", "10");
      settingService.set(Context.GLOBAL, Scope.WINDOWS, "resultsPerPage", new SettingValue<Long>(Long.parseLong(resultsPerPage)));
    }
    SettingValue<?> searchTypesSettingValue = settingService.get(Context.GLOBAL, Scope.WINDOWS, "searchTypes");
    if (searchTypesSettingValue == null) {
      String searchTypes = portletPreferences.getValue("searchTypes", "all");
      settingService.set(Context.GLOBAL, Scope.WINDOWS, "searchTypes", new SettingValue<String>(searchTypes));
    }

    SettingValue<?> searchCurrentSiteOnlySettingValue = settingService.get(Context.GLOBAL, Scope.WINDOWS, "searchCurrentSiteOnly");
    if (searchCurrentSiteOnlySettingValue == null) {
      String searchCurrentSiteOnly = portletPreferences.getValue("searchCurrentSiteOnly", "false");
      settingService.set(Context.GLOBAL, Scope.WINDOWS, "searchCurrentSiteOnly", new SettingValue<Boolean>(Boolean.parseBoolean(searchCurrentSiteOnly)));
    }
    if (PortletMode.EDIT == mode){
      return edit.ok(parameters);
    }else {
      return index.ok(parameters);
    }
  }  
}
