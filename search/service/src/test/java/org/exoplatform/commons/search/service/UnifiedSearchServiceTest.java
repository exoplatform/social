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

import java.lang.reflect.Method;
import java.util.*;

import javax.ws.rs.core.MediaType;

import org.json.JSONException;
import org.json.JSONObject;

import org.exoplatform.commons.api.search.SearchService;
import org.exoplatform.commons.api.search.data.SearchResult;
import org.exoplatform.commons.search.driver.SearchServiceImpl;
import org.exoplatform.component.test.*;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.rest.tools.ByteArrayContainerResponseWriter;
import org.exoplatform.services.security.*;

/**
 * Created by The eXo Platform SAS
 * Author : Canh Pham Van
 *          canhpv@exoplatform.com
 * Mar 25, 2013  
 */

@ConfiguredBy({
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.portal.component.identity-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/controller-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/exo-configuration.xml"),  
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/test-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/rest-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/exo.test.portal-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/configuration.xml"),
})
public class UnifiedSearchServiceTest extends AbstractServiceTest implements ResourceContainer{
  
  protected static Log    LOG         = ExoLogger.getLogger(UnifiedSearchServiceTest.class);

  protected static String USER_ROOT   = "root";

  protected static String USER_JOHN   = "john";

  protected static String USER_DEMO   = "demo";  
  
  private static final String  BASE_URL     = "http://localhost:8080";

  private static final String  REST_CONTEXT = "";
  
  private static UnifiedSearchService unifiedSearchService;

  private SearchServiceImpl searchService;

  private Collection<MembershipEntry> membershipEntries = new ArrayList<MembershipEntry>();
  
  public UnifiedSearchServiceTest() throws Exception{
    System.setProperty("search.excluded-characters",".-_");
  }

  public void setUp() throws Exception {
    super.setUp();
    setMembershipEntry("/platform/administrators", "*", true);
    Identity identity = new Identity(USER_ROOT, membershipEntries); 
    ConversationState conversionState = new ConversationState(identity);
    ConversationState.setCurrent(conversionState);
    
    PortalContainer portalContainer = (PortalContainer)ExoContainerContext.getCurrentContainer();    
    unifiedSearchService = (UnifiedSearchService) portalContainer.getComponentInstanceOfType(UnifiedSearchService.class);
    searchService = (SearchServiceImpl)portalContainer.getComponentInstanceOfType(SearchService.class);
    registry(unifiedSearchService);       
  }
  
  public void tearDown() throws Exception {
    super.tearDown();

    removeResource(unifiedSearchService.getClass());
  }
  
  
  @Override
  public void beforeRunBare() {
    try {
      super.beforeRunBare();
    } catch (Exception e) {
      log.error(e);
    }
  }
  
  @Override
  protected void afterRunBare() {
    super.afterRunBare();
  }

  public void testReplaceSpecialCharacters() throws Exception {
    Method method = searchService.getClass().getDeclaredMethod("replaceSpecialCharacters", String.class);
    method.setAccessible(true);
    String result = (String) method.invoke(searchService,"space1");
    assertEquals("space1", result);
    result = (String) method.invoke(searchService,"space1.test");
    assertEquals("space1 test", result);
    result = (String) method.invoke(searchService,"space1_test");
    assertEquals("space1 test", result);
    result = (String) method.invoke(searchService,"space1-test");
    assertEquals("space1 test", result);
    result = (String) method.invoke(searchService,"space1+test");
    assertEquals("space1+test", result);
  }
  
  public void skipTestSearch() throws Exception {
    ContainerResponse response = service("GET", BASE_URL + REST_CONTEXT + "/search?q=root&types=all", "", null, null);
    assertEquals(200, response.getStatus());
    assertEquals(MediaType.APPLICATION_JSON, response.getContentType().toString());
        
    List<SearchResult> results = parseJsonData(response);
   
    assertEquals(4, results.size());
    
    assertEquals("calendar/details/Eventc4b1f07e7f0001010101c2cdd3cc2f8c", results.get(0).getUrl());
    assertEquals(587, results.get(0).getRelevancy());
    assertEquals("calendar/details/Eventc4b1f07e7f0001010101c2cdd3cc2f8d", results.get(1).getUrl());
    assertEquals(587, results.get(1).getRelevancy());
    assertEquals("/profile/root", results.get(2).getUrl());
    assertEquals(1000, results.get(2).getRelevancy());
    assertEquals("/profile/john", results.get(3).getUrl());
    assertEquals(900, results.get(3).getRelevancy());
  }  
  
  public void skipTestRegistry() throws Exception{
    ContainerResponse response = service("GET", BASE_URL + REST_CONTEXT + "/search/registry", "", null, null);
    assertEquals(200, response.getStatus());
    
    JSONObject obj = new JSONObject(response);
    String results = obj.getString("entity");
    String peopleType = null;
    String peopleClass = null;
    if (results.indexOf("people")> 0){
      peopleType = results.substring(results.indexOf("people"),results.indexOf("people") + "people".length());
      peopleClass = results.substring(results.indexOf("=")+1,results.indexOf("@"));
    }
    
    assertEquals("people", peopleType);
    assertEquals("org.exoplatform.commons.search.service.search.PeopleSearchConnector", peopleClass);
    results = results.substring(results.indexOf("@")+1);
        
    String eventType = null;
    String eventClass = null;
    if (results.indexOf("event")> 0){
      eventType = results.substring(results.indexOf("event"),results.indexOf("event") + "event".length());
      eventClass = results.substring(results.indexOf("=")+1,results.indexOf("@"));
    }
    assertEquals("event", eventType);
    assertEquals("org.exoplatform.commons.search.service.search.EventSearchConnector", eventClass);
        
  }
  
  public void skipTestSites() throws Exception{
    ContainerResponse response = service("GET", BASE_URL + REST_CONTEXT + "/search/sites", "", null, null);
    assertEquals(200, response.getStatus());
    JSONObject obj = new JSONObject(response);
    String site = obj.getString("entity");
    site = site.substring(1,site.length()-1);
    String[] arraySite = site.split(",");
    if (arraySite != null && arraySite.length == 2){
      assertEquals("intranet", arraySite[0]==null?"":arraySite[0].trim());
      assertEquals("acme", arraySite[1]==null?"":arraySite[1].trim());
    }
  }
  
  public void skipTestSearchSetting() throws Exception{    
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("POST", BASE_URL + REST_CONTEXT + "/search/setting/10/people/false/false/false", "", null,null);
    assertEquals(200, response.getStatus());   
    
    ContainerResponse responseGet = service("GET", BASE_URL + REST_CONTEXT + "/search/setting", "", null,null, writer);
    
    assertEquals(200, responseGet.getStatus());
    JSONObject obj = new JSONObject(new String(writer.getBody()));
    String resultsPerPage = obj.get("resultsPerPage").toString();
    String searchTypes = obj.get("searchTypes").toString();
    String searchCurrentSiteOnly = obj.get("searchCurrentSiteOnly").toString();
    String hideSearchForm = obj.get("hideSearchForm").toString();
    String hideFacetsFilter = obj.get("hideFacetsFilter").toString();    

    assertEquals(10, Integer.parseInt(resultsPerPage));
    assertEquals("people", searchTypes.substring(searchTypes.indexOf("people"),searchTypes.indexOf("people") + "people".length()));
    assertEquals(false, Boolean.parseBoolean(searchCurrentSiteOnly));
    assertEquals(false, Boolean.parseBoolean(hideSearchForm));
    assertEquals(false, Boolean.parseBoolean(hideFacetsFilter));
  }
  
  public void skipTestQuickSetting() throws Exception{    
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("POST", BASE_URL + REST_CONTEXT + "/search/setting/quicksearch/5/all/true", "", null,null);
    assertEquals(200, response.getStatus());   
    
    ContainerResponse responseGet = service("GET", BASE_URL + REST_CONTEXT + "/search/setting/quicksearch", "", null,null,writer);        
    assertEquals(200, responseGet.getStatus());
    JSONObject obj = new JSONObject(new String(writer.getBody()));    
    String resultsPerPage = obj.get("resultsPerPage").toString();
    String searchTypes = obj.get("searchTypes").toString();
    String searchCurrentSiteOnly = obj.get("searchCurrentSiteOnly").toString();

    assertEquals(5, Integer.parseInt(resultsPerPage));
    assertEquals("all", searchTypes.substring(searchTypes.indexOf("all"),searchTypes.indexOf("all") + "all".length()));
    assertEquals(true, Boolean.parseBoolean(searchCurrentSiteOnly));
  }
  
  public void skipTestEnabledSearchtypes() throws Exception{    
    ContainerResponse response = service("POST", BASE_URL + REST_CONTEXT + "/search/enabled-searchtypes/people", "", null,null);    
    assertEquals(200, response.getStatus());
    assertEquals("ok", response.getEntity().toString());
  }
  
  public List<SearchResult> parseJsonData(ContainerResponse response) throws JSONException{
    JSONObject obj = new JSONObject(response);
    String results = obj.getString("entity");
    List<SearchResult> resultList = new ArrayList<SearchResult>();

    results = results.substring(results.indexOf("{")+1);    
    results = results.substring(0, results.lastIndexOf("}"));

    while (results.indexOf("url") != -1){
      String data = results.substring(results.indexOf("{"),results.indexOf("}")+1);
      resultList.add(fetchData(data));
      results = results.substring(results.indexOf(data) + data.length());      
    }
    return resultList;
  }
  
  public SearchResult fetchData(String str){
        
    String url = null;
    String relavency = null;
    
    String[] arrayStr = str.split(",");
    if (arrayStr != null && arrayStr.length > 0){
      if (arrayStr[0]!=null && !arrayStr[0].isEmpty() && arrayStr[0].indexOf("=")>0)
      {
        url = arrayStr[0].substring(arrayStr[0].indexOf("=")+1);
        relavency = arrayStr[1].substring(arrayStr[1].indexOf("=")+1, arrayStr[1].indexOf("}"));
      }
    }
    SearchResult searchResult = new SearchResult(url, null, null, null, null, 0, relavency==null?0:Long.parseLong(relavency));
    return searchResult;
  }
  
  private void setMembershipEntry(String group, String membershipType, boolean isNew) {
    MembershipEntry membershipEntry = new MembershipEntry(group, membershipType);
    if (isNew) {
      membershipEntries.clear();
    }
    membershipEntries.add(membershipEntry);
  }  
  
}
