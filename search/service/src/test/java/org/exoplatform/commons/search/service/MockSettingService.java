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
package org.exoplatform.commons.search.service;

import org.exoplatform.commons.api.settings.SettingService;
import org.exoplatform.commons.api.settings.SettingValue;
import org.exoplatform.commons.api.settings.data.Context;
import org.exoplatform.commons.api.settings.data.Scope;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.List;


/**
 * Created by The eXo Platform SAS
 * Author : Canh Pham Van
 *          canhpv@exoplatform.com
 * Mar 29, 2013  
 */
public class MockSettingService implements SettingService{
  Map<String, SettingValue<?>> mymap;

  
  public MockSettingService() {
    
    mymap = new HashMap<String,SettingValue<?>>();
    
    KeySetting resultsPerPageKey = new KeySetting(Context.USER.toString(), Scope.WINDOWS.toString(), "resultsPerPage");
    SettingValue<Long> resultsPerPage = new SettingValue<Long>(10l);

    KeySetting searchTypesKey = new KeySetting(Context.USER.toString(), Scope.WINDOWS.toString(), "searchTypes");
    SettingValue<String> searchTypes = new SettingValue<String>("people");

    KeySetting searchCurrentSiteOnlyKey = new KeySetting(Context.USER.toString(), Scope.WINDOWS.toString(), "searchCurrentSiteOnly");
    SettingValue<Boolean> searchCurrentSiteOnly = new SettingValue<Boolean>(false);

    KeySetting hideSearchFormKey = new KeySetting(Context.USER.toString(), Scope.WINDOWS.toString(), "hideSearchForm");
    SettingValue<Boolean> hideSearchForm = new SettingValue<Boolean>(false);

    KeySetting hideFacetsFilterKey = new KeySetting(Context.USER.toString(), Scope.WINDOWS.toString(), "hideFacetsFilter");
    SettingValue<Boolean> hideFacetsFilter = new SettingValue<Boolean>(false);

    KeySetting enabledSearchTypesKey = new KeySetting(Context.GLOBAL.toString(), Scope.APPLICATION.toString(), "enabledSearchTypes");
    SettingValue<String> enabledSearchTypes = new SettingValue<String>("people,event");
    
    mymap.put(resultsPerPageKey.toString(), resultsPerPage);
    mymap.put(searchTypesKey.toString(), searchTypes);
    mymap.put(searchCurrentSiteOnlyKey.toString(), searchCurrentSiteOnly);
    mymap.put(hideSearchFormKey.toString(), hideSearchForm);
    mymap.put(hideFacetsFilterKey.toString(), hideFacetsFilter);
    mymap.put(enabledSearchTypesKey.toString(), enabledSearchTypes);
  }

  @Override
  public void set(Context context, Scope scope, String key, SettingValue<?> value) {
    KeySetting newKey = new KeySetting(Context.USER.toString(), Scope.WINDOWS.toString(), key);
    mymap.put(newKey.toString(), value);        
  }

  @Override
  public void remove(Context context, Scope scope, String key) {
  }

  @Override
  public void remove(Context context, Scope scope) {
  }

  @Override
  public void remove(Context context) {
  }

  @Override
  public SettingValue<?> get(Context context, Scope scope, String key) {   
    KeySetting sKey = new KeySetting(context.toString(), scope.toString(), key);    
    return mymap.get(sKey.toString());
  }

  @Override
  public Map<Scope, Map<String, SettingValue<String>>> getSettingsByContext(Context context) {
    return null;
  }

  @Override
  public List<Context> getContextsByTypeAndScopeAndSettingName(String contextType,
                                                               String scopeType,
                                                               String scopeName,
                                                               String settingName,
                                                               int offset,
                                                               int limit) {
    return null;
  }

  @Override
  public Map<String, SettingValue> getSettingsByContextAndScope(String contextType,
                                                                String contextName,
                                                                String scopeType,
                                                                String scopeName) {
    return null;
  }

  @Override
  public Set<String> getEmptyContextsByTypeAndScopeAndSettingName(String contextType,
                                                                  String scopeType,
                                                                  String scopeName,
                                                                  String settingName,
                                                                  int offset,
                                                                  int limit) {
    return null;
  }

  @Override
  public void save(Context context) {
  }
    

}

class KeySetting{
  private String key1;
  private String key2;
  private String key3;
 
  
  KeySetting(String k1, String k2,String k3){
    key1 = k1;
    key2 = k2;
    key3 = k3;
  }
  public String toString(){
    return ((new StringBuilder()).append(key1).append(key2).append(key3)).toString();
  }
}
