/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.social.service.rest.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.Response;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.service.LinkProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.service.rest.api.IdentityResources;
import org.exoplatform.social.service.rest.Util;
import org.exoplatform.social.service.test.AbstractResourceTest;

/**
 * Unit Test for {@link IdentityResources}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @author <a href="http://phuonglm.net">PhuongLM</a>
 * @since Jun 16, 2011
 */
public class IdentityResourcesTest extends AbstractResourceTest {

  private final String RESOURCE_URL = "/api/social/v1-alpha3/portal/";

  private IdentityManager identityManager;

  private Identity rootIdentity, johnIdentity, maryIdentity, demoIdentity;

  private List<Identity> tearDownIdentityList;

  /**
   * Adds {@link IdentityResources}.
   *
   * @throws Exception
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();

    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);

    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john", false);
    maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary", false);
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo", false);

    tearDownIdentityList = new ArrayList<Identity>();
    tearDownIdentityList.add(rootIdentity);
    tearDownIdentityList.add(johnIdentity);
    tearDownIdentityList.add(maryIdentity);
    tearDownIdentityList.add(demoIdentity);

    addResource(IdentityResources.class, null);
  }

  /**
   * Removes {@link IdentityResources}.
   *
   * @throws Exception
   */
  @Override
  public void tearDown() throws Exception {
    for (Identity identity: tearDownIdentityList) {
      identityManager.deleteIdentity(identity);
    }
    removeResource(IdentityResources.class);

    super.tearDown();
  }

  
  /**
   * Test {@link IdentityResources#getIdentityById(javax.ws.rs.core.UriInfo, String, String, String)}
   */
  public void testGetIdentityById() throws Exception{
    startSessionAs("demo");
    
    ContainerResponse response = service("GET", RESOURCE_URL+"identity/" + demoIdentity.getId() + ".json", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    HashMap entityHashMap = (HashMap) response.getEntity();
    HashMap profileHashMap = (HashMap) entityHashMap.get("profile");
    
    assertEquals(demoIdentity.getId(), entityHashMap.get("id"));
    assertEquals(demoIdentity.getProviderId(), entityHashMap.get("providerId"));
    assertEquals(demoIdentity.getRemoteId(), entityHashMap.get("remoteId"));
    
    assertEquals(demoIdentity.getProfile().getFullName(), profileHashMap.get("fullName"));
    assertEquals(Util.getBaseUrl() + LinkProvider.PROFILE_DEFAULT_AVATAR_URL, profileHashMap.get("avatarUrl"));
  }
  
  /**
   * Test {@link IdentityResources#getIdentityById(javax.ws.rs.core.UriInfo, String, String, String)}
   */
  public void testGetIdentityByIdWithAnonymous() throws Exception {
    //not authenticated
    ContainerResponse response = service("GET", RESOURCE_URL+"identity/" +
                                          demoIdentity.getId() + ".json", "", null, null);
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    
    //wrong portalContainer
    response = service("GET", "/api/social/v1-alpha3/notExistPortalContainer/identity/" +
        demoIdentity.getId() + ".json", "", null, null);
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    
    //Not supported type
    response = service("GET", RESOURCE_URL+"identity/" +
        demoIdentity.getId() + ".xml", "", null, null);
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    
    //IdentityId not exist
    response = service("GET", RESOURCE_URL+"identity/notExistIdentity.json", "", null, null);
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
  }
  

  
  /**
   * Test {@link IdentityResources#getIdentityById(javax.ws.rs.core.UriInfo, String, String, String)}
   */
  public void testGetIdentityByIdWithWrongPortalContainerName() throws Exception {
    startSessionAs("demo");
    ContainerResponse response = service("GET", "/api/social/v1-alpha3/notExistPortalContainer/identity/" + 
                                         demoIdentity.getId() +".json", "", null, null);
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    
    //Not supported type
    response = service("GET", "/api/social/v1-alpha3/notExistPortalContainer/identity/" +
        demoIdentity.getId() + ".xml", "", null, null);
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    
    //IdentityId not exist
    response = service("GET","/api/social/v1-alpha3/notExistPortalContainer/identity/notExistIdentity.json", "", null, null);
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  /**
   * Test {@link IdentityResources#getIdentityById(javax.ws.rs.core.UriInfo, String, String, String)}
   */
  public void testGetIdentityByIdWithWrongSupportedFormat() throws Exception {
    startSessionAs("demo");
    ContainerResponse response = service("GET", RESOURCE_URL + "identity/" + 
                                         demoIdentity.getId() +".xml", "", null, null);
    assertEquals(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatus());
    
    //IdentityId not exist
    response = service("GET", RESOURCE_URL+"identity/notExistIdentity.xml", "", null, null);
    assertEquals(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatus());
  }

  /**
   * Test {@link IdentityResources#getIdentityById(javax.ws.rs.core.UriInfo, String, String, String)}
   */
  public void testGetIdentityByIdWithWrongIdentityId() throws Exception {
    startSessionAs("demo");
    ContainerResponse response = service("GET", RESOURCE_URL + "identity/notExistIdentityId.json", "", 
        null, null);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }
  
  /**
   * Test {@link IdentityResources#getIdentityProviderIDAndRemoteID(javax.ws.rs.core.UriInfo, String, String, String, String)}
   */
  public void testGetIdentityProviderIdAndRemoteId() throws Exception{
    startSessionAs("demo");
    ContainerResponse response = service("GET", RESOURCE_URL+"identity/" + demoIdentity.getProviderId() + "/" + 
                                         demoIdentity.getRemoteId() + ".json", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    HashMap entityHashMap = (HashMap) response.getEntity();
    HashMap profileHashMap = (HashMap) entityHashMap.get("profile");
    
    assertEquals(demoIdentity.getId(), entityHashMap.get("id"));
    assertEquals(demoIdentity.getProviderId(), entityHashMap.get("providerId"));
    assertEquals(demoIdentity.getRemoteId(), entityHashMap.get("remoteId"));
    
    assertEquals(demoIdentity.getProfile().getFullName(), profileHashMap.get("fullName"));
    assertEquals(Util.getBaseUrl() + LinkProvider.PROFILE_DEFAULT_AVATAR_URL, profileHashMap.get("avatarUrl"));
  }
  
  /**
   * Test {@link IdentityResources#getIdentityProviderIDAndRemoteID(javax.ws.rs.core.UriInfo, String, String, String, String)}
   */
  public void testGetIdentityByProviderIdAndRemoteIdWithAnonymous() throws Exception {
    ContainerResponse response = service("GET", RESOURCE_URL+"identity/" + demoIdentity.getProviderId() + "/" + 
                                          demoIdentity.getRemoteId() + ".json", "", null, null);
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    
    // wrong portal Container
    response = service("GET", "/api/social/v1-alpha3/notExistPortalContainer/identity/" + 
        demoIdentity.getProviderId() + "/" + demoIdentity.getRemoteId() + 
        ".json", "", null, null);
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    
    //not supported media type
    response = service("GET", RESOURCE_URL + "identity/" + 
        demoIdentity.getProviderId() + "/" + demoIdentity.getRemoteId() + 
        ".xml", "", null, null);
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    
    // not exist providerId
    response = service("GET", RESOURCE_URL+"identity/notExistProvider/notExistRemoteId.json", "", 
        null, null);
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    
    // not exist remoteId
    response = service("GET", RESOURCE_URL+"identity/" + demoIdentity.getProviderId() + 
        "/notExistRemoteId.json", "", null, null);
    assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
    

  }
  
  /**
   * Test {@link IdentityResources#getIdentityProviderIDAndRemoteID(javax.ws.rs.core.UriInfo, String, String, String, String)}
   */  
  public void testGetIdentityByProviderIdAndRemoteIdWithWrongPortalContainerName() throws Exception {
    startSessionAs("demo");
    ContainerResponse response = service("GET", "/api/social/v1-alpha3/notExistPortalContainer/identity/" + 
                                         demoIdentity.getProviderId() + "/" + demoIdentity.getRemoteId() + 
                                         ".json", "", null, null);
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    
    //not supported media type
    response = service("GET", "/api/social/v1-alpha3/notExistPortalContainer/identity/" + 
        demoIdentity.getProviderId() + "/" + demoIdentity.getRemoteId() + 
        ".xml", "", null, null);
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    
    // not exist providerId
    response = service("GET", "/api/social/v1-alpha3/notExistPortalContainer/identity/notExistProviderId/demo.json", "", 
        null, null);
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    
    // not exist remoteId
    response = service("GET", "/api/social/v1-alpha3/notExistPortalContainer/identity/" + demoIdentity.getProviderId() + 
        "/notExistRemoteId.json", "", null, null);
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }


  /**
   * Test {@link IdentityResources#getIdentityProviderIDAndRemoteID(javax.ws.rs.core.UriInfo, String, String, String, String)}
   */
  public void testGetIdentityByProviderIdAndRemoteIdWithWrongSupportedFormat() throws Exception {
    startSessionAs("demo");
    ContainerResponse response = service("GET", RESOURCE_URL + "identity/" + 
                                         demoIdentity.getProviderId() + "/" + demoIdentity.getRemoteId() + 
                                         ".xml", "", null, null);
    assertEquals(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatus());
    
    // not exist providerId
    response = service("GET", RESOURCE_URL + "identity/notExistProviderId/" + demoIdentity.getRemoteId() + ".xml", "", 
        null, null);
    assertEquals(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatus());
    
    // not exist remoteId
    response = service("GET", RESOURCE_URL + "identity/" + demoIdentity.getProviderId() + 
        "/notExistRemoteId.xml", "", null, null);
    assertEquals(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatus());
  }
  
  /**
   * Test {@link IdentityResources#getIdentityProviderIDAndRemoteID(javax.ws.rs.core.UriInfo, String, String, String, String)}
   */
  public void testGetIdentityByProviderIdAndRemoteIdWithWrongProviderId() throws Exception {
    startSessionAs("demo");
    ContainerResponse response = service("GET", RESOURCE_URL+"identity/notExistProvider/"+demoIdentity.getRemoteId()+".json", "", 
        null, null);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus()); 
    
    // not exist remoteId
    response = service("GET", RESOURCE_URL + "identity/notExistProvider/notExistRemoteId.json", "", null, null);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }
  
  /**
   * Test {@link IdentityResources#getIdentityProviderIDAndRemoteID(javax.ws.rs.core.UriInfo, String, String, String, String)}
   */
  public void testGetIdentityByProviderIdAndRemoteIdWithWrongRemoteId() throws Exception {
    startSessionAs("demo");
    ContainerResponse response = service("GET", RESOURCE_URL+"identity/" + demoIdentity.getProviderId() + 
                                          "/notExistRemoteId.json", "", null, null);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }
  
  
}
