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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.SpaceException;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.service.rest.api.ActivityResources;
import org.exoplatform.social.service.rest.api.models.ActivityRestOut;
import org.exoplatform.social.service.rest.api.models.IdentityRestOut;
import org.exoplatform.social.service.test.AbstractResourceTest;
import org.json.JSONWriter;

/**
 * Unit Test for {@link ActivityResources}.
 *
 * @author <a href="http://phuonglm.net">PhuongLM</a>
 * @since 1.2.3
 */
public class ActivityResourcesTest extends AbstractResourceTest {

  private final String RESOURCE_URL = "/api/social/v1-alpha3/portal/activity";

  private IdentityManager identityManager;
  private ActivityManager activityManager;
  private RelationshipManager relationshipManager;
  private SpaceService spaceService;

  private Identity rootIdentity, johnIdentity, maryIdentity, demoIdentity;

  private List<Identity> tearDownIdentityList;
  private List<ExoSocialActivity> tearDownActivityList;
  private List<Relationship> tearDownRelationshipList;
  private List<Space> tearDownSpaceList;

  /**
   * Adds {@link ActivityResources}.
   *
   * @throws Exception
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();

    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    relationshipManager = (RelationshipManager) getContainer().getComponentInstanceOfType(RelationshipManager.class);
    spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);

    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john", false);
    maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary", false);
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo", false);

    tearDownIdentityList = new ArrayList<Identity>();
    tearDownIdentityList.add(rootIdentity);
    tearDownIdentityList.add(johnIdentity);
    tearDownIdentityList.add(maryIdentity);
    tearDownIdentityList.add(demoIdentity);

    tearDownActivityList = new ArrayList<ExoSocialActivity>();
    tearDownRelationshipList = new ArrayList<Relationship>();
    tearDownSpaceList = new ArrayList<Space>();

    addResource(ActivityResources.class, null);
  }

  /**
   * Removes {@link ActivityResources}.
   *
   * @throws Exception
   */
  @Override
  public void tearDown() throws Exception {

    //Removing the relationships
    for (Relationship relationship: tearDownRelationshipList) {
      relationshipManager.delete(relationship);
    }
    
    //Removing the activities
    for (ExoSocialActivity activity: tearDownActivityList) {
      activityManager.deleteActivity(activity);
    }

    //Removing the spaces
    for (Space space: tearDownSpaceList) {
      spaceService.deleteSpace(space);
    }
    for (Identity identity: tearDownIdentityList) {
      identityManager.deleteIdentity(identity);
    }
    removeResource(ActivityResources.class);

    super.tearDown();
  }

  /*
   * General Test Case
   *   - Wrong Portal Container
   *   - Not Authenticated
   *   - the require parameter is "" or missing
   */
  
  /**
   * General Test Case of GET activity
   *
   * @throws Exception
   */
  public void testGetActivityByIdGeneralCase() throws Exception {
    String resourceUrl = RESOURCE_URL+"/1a2b3c4d5e.json";
    // unauthorized
    testAccessResourceAsAnonymous("GET", resourceUrl, null, null);
    //not found
    testAccessNotFoundResourceWithAuthentication("demo", "GET", resourceUrl, null);
    // Wrong Portal Container name
    testStatusCodeOfResource("demo", "GET", "/api/social/v1-alpha3/wrongPortalContainerName/activity/1a2b3c4d5e.json", null, null,
                             Response.Status.BAD_REQUEST.getStatusCode());
    // Unsupported media type
    testStatusCodeOfResource("demo", "GET", "/api/social/v1-alpha3/portal/activity/1a2b3c4d5e.xml", null, null,
        Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
  }

  /**
   * General Test Case of POST activity
   *
   * @throws Exception
   */
  public void testPostActivityGeneralCase() throws Exception {
    String resourceUrl = RESOURCE_URL+".json";
    StringWriter writer = new StringWriter();
    JSONWriter jsonWriter = new JSONWriter(writer);
    jsonWriter
        .object()
        .key("title")
        .value("hello")
        .endObject();
    byte[] data = writer.getBuffer().toString().getBytes("UTF-8");

    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    h.putSingle("content-type", "application/json");
    h.putSingle("content-length", "" + data.length);
    
    // Unauthorized
    testAccessResourceAsAnonymous("POST", resourceUrl,h, data);
    // Wrong Portal Container name
    testStatusCodeOfResource("demo", "POST", "/api/social/v1-alpha3/wrongPortalContainerName/activity.json", h, data,
                             Response.Status.BAD_REQUEST.getStatusCode());
    
    // title == "" or missing title
    writer = new StringWriter();
    jsonWriter = new JSONWriter(writer);
    jsonWriter
    .object()
    .key("title")
    .value("")
    .endObject();
    data = writer.getBuffer().toString().getBytes("UTF-8");
    h.putSingle("content-length", "" + data.length);

    testStatusCodeOfResource("demo", "POST", "/api/social/v1-alpha3/portal/activity.json", h, data,
                            Response.Status.BAD_REQUEST.getStatusCode());
    writer = new StringWriter();
    jsonWriter = new JSONWriter(writer);
    jsonWriter
    .object()
    .endObject();
    data = writer.getBuffer().toString().getBytes("UTF-8");
    h.putSingle("content-length", "" + data.length);
    testStatusCodeOfResource("demo", "POST", "/api/social/v1-alpha3/portal/activity.json", h, data,
                            Response.Status.BAD_REQUEST.getStatusCode());
    
    h = new MultivaluedMapImpl();
    h.putSingle("content-type", "application/xml");
    h.putSingle("content-length", "" + data.length);
    
    // Unsupported media type
    testStatusCodeOfResource("demo", "POST", "/api/social/v1-alpha3/portal/activity.xml", h, data,
        Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
  }

  /**
   * General Test Case of DELETE activity
   *
   * @throws Exception
   */
  public void testDeleteActivityGeneralCase() throws Exception {
    String resourceUrl = RESOURCE_URL+"/12912903.json";
    
    // Unauthorized
    testAccessResourceAsAnonymous("DELETE", resourceUrl, null, null);
    // Wrong Portal Container name
    testStatusCodeOfResource("demo", "DELETE", "/api/social/v1-alpha3/wrongPortalContainerName/activity/128318387123.json", null, null,
                             Response.Status.BAD_REQUEST.getStatusCode());

    // Not Found 
    testStatusCodeOfResource("demo", "DELETE", "/api/social/v1-alpha3/portal/activity/2131445234213.json", null, null,
                             Response.Status.NOT_FOUND.getStatusCode());
    
    // Unsupported media type
    testStatusCodeOfResource("demo", "DELETE", "/api/social/v1-alpha3/portal/activity/2131445234213.xml", null, null,
        Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
    
    String resourceUrlPostDelete = RESOURCE_URL+"/destroy/12912903.json";
    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    h.putSingle("content-type", "application/json");
    h.putSingle("content-length", "0");
    
    // Unauthorized
    testAccessResourceAsAnonymous("POST", resourceUrlPostDelete, h, null);
    
    // Wrong Portal Container name
    testStatusCodeOfResource("demo", "POST", "/api/social/v1-alpha3/wrongPortalContainerName/activity/destroy/128318387123.json", h, null,
                             Response.Status.BAD_REQUEST.getStatusCode());
    
    // Not Found 
    testStatusCodeOfResource("demo", "POST", "/api/social/v1-alpha3/portal/activity/destroy/2131445234213.json", h, null,
                             Response.Status.NOT_FOUND.getStatusCode());
    // Unsupported media type
    testStatusCodeOfResource("demo", "POST", "/api/social/v1-alpha3/portal/activity/destroy/2131445234213.xml", null, null,
        Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
  }

  
  /**
   * General Test Case of GET comments
   *
   * @throws Exception
   */
  public void testGetCommentsGeneralCase() throws Exception {
    String resourceUrl = RESOURCE_URL+"/21381903819082/comments.json";
    // unauthorized
    testAccessResourceAsAnonymous("GET", resourceUrl, null, null);
    //not found
    testAccessNotFoundResourceWithAuthentication("demo", "GET", resourceUrl, null);
    // Wrong Portal Container name
    testStatusCodeOfResource("demo", "GET", "/api/social/v1-alpha3/wrongPortalContainerName/activity/1a2b3c4d5e/comments.json", null, null,
                             Response.Status.BAD_REQUEST.getStatusCode());
    // Unsupported media type
    testStatusCodeOfResource("demo", "GET", "/api/social/v1-alpha3/portal/activity/1a2b3c4d5e/comments.xml", null, null,
        Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
  }

  /**
   * General Test Case of POST comment
   *
   * @throws Exception
   */
  public void testPostCommentGeneralCase() throws Exception {
    createActivities(demoIdentity, demoIdentity, 1);
    ExoSocialActivity[] activities = activityManager.getActivitiesWithListAccess(demoIdentity).load(0, 1);
    
    String resourceUrl = RESOURCE_URL+"/098129848752/comment.json";
    StringWriter writer = new StringWriter();
    JSONWriter jsonWriter = new JSONWriter(writer);
    jsonWriter
        .object()
        .key("text")
        .value("comment for activity")
        .endObject();
    byte[] data = writer.getBuffer().toString().getBytes("UTF-8");

    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    h.putSingle("content-type", "application/json");
    h.putSingle("content-length", "" + data.length);
    
    // Unauthorized
    testAccessResourceAsAnonymous("POST", resourceUrl,h, data);
    // Wrong Portal Container name
    testStatusCodeOfResource("demo", "POST", "/api/social/v1-alpha3/wrongPortalContainerName/activity/231241312/comment.json", h, data,
                             Response.Status.BAD_REQUEST.getStatusCode());
    
    // Unsupported media type
    testStatusCodeOfResource("demo", "POST", "/api/social/v1-alpha3/portal/activity/"+activities[0].getId()+"/comment.xml", h, data,
        Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
    
    // title == "" or missing title
    writer = new StringWriter();
    jsonWriter = new JSONWriter(writer);
    jsonWriter
    .object()
    .key("text")
    .value("")
    .endObject();
    data = writer.getBuffer().toString().getBytes("UTF-8");
    h.putSingle("content-length", "" + data.length);
    testStatusCodeOfResource("demo", "POST", "/api/social/v1-alpha3/portal/activity/"+activities[0].getId()+"/comment.json", h, data,
                             Response.Status.BAD_REQUEST.getStatusCode());
    

    
    writer = new StringWriter();
    jsonWriter = new JSONWriter(writer);
    jsonWriter
    .object()
    .endObject();
    data = writer.getBuffer().toString().getBytes("UTF-8");
    h.putSingle("content-length", "" + data.length);
    testStatusCodeOfResource("demo", "POST", "/api/social/v1-alpha3/portal/activity/"+activities[0].getId()+"/comment.json", h, data,
        Response.Status.BAD_REQUEST.getStatusCode());

  }

  /**
   * General Test Case of DELETE Comment
   *
   * @throws Exception
   */
  public void testDeleteCommentGeneralCase() throws Exception {
    String resourceUrl = RESOURCE_URL+"/12912903/comment/293984912.json";
    
    // Unauthorized
    testAccessResourceAsAnonymous("DELETE", resourceUrl, null, null);
    // Wrong Portal Container name
    testStatusCodeOfResource("demo", "DELETE", "/api/social/v1-alpha3/wrongPortalContainerName/activity/128318387123/comment/2304982984.json", null, null,
                             Response.Status.BAD_REQUEST.getStatusCode());
    // Not Found 
    testStatusCodeOfResource("demo", "DELETE", "/api/social/v1-alpha3/portal/activity/128318387123/comment/2131445234213.json", null, null,
                             Response.Status.NOT_FOUND.getStatusCode());
    
    // Unsupported media type
    testStatusCodeOfResource("demo", "DELETE", "/api/social/v1-alpha3/portal/activity/1a2b3c4d5e/comment/2131445234213.xml", null, null,
        Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
    
    String resourceUrlPostDelete = RESOURCE_URL+"/24802934/comment/destroy/12912903.json";
    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    h.putSingle("content-type", "application/json");
    h.putSingle("content-length", "0");
    
    // Unauthorized
    testAccessResourceAsAnonymous("POST", resourceUrlPostDelete,h, null);
    
    // Wrong Portal Container name
    testStatusCodeOfResource("demo", "POST", "/api/social/v1-alpha3/wrongPortalContainerName/activity/128318387123/comment/destroy/128318387123.json", h, null,
                             Response.Status.BAD_REQUEST.getStatusCode());
    
    // Not Found 
    testStatusCodeOfResource("demo", "POST", "/api/social/v1-alpha3/portal/activity/128318387123/comment/destroy/2131445234213.json", h, null,
                             Response.Status.NOT_FOUND.getStatusCode());
    // Unsupported media type
    testStatusCodeOfResource("demo", "POST", "/api/social/v1-alpha3/portal/activity/1a2b3c4d5e/comment/destroy/2131445234213.xml", h, null,
        Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
  }

  /**
   * General Test Case of POST Like
   *
   * @throws Exception
   */
  public void testPostLikeGeneralCase() throws Exception {
    String resourceUrl = RESOURCE_URL+"/1a2b3c4e5e/like.json";


    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    h.putSingle("content-type", "application/json");
    h.putSingle("content-length", "0");
    
    // Unauthorized
    testAccessResourceAsAnonymous("POST", resourceUrl, null, null);
    // Wrong Portal Container name
    testStatusCodeOfResource("demo", "POST", "/api/social/v1-alpha3/wrongPortalContainerName/activity/1a2b3c4e5e/like.json", null, null,
                             Response.Status.BAD_REQUEST.getStatusCode());
    // Unsupported media type
    testStatusCodeOfResource("demo", "POST", "/api/social/v1-alpha3/portal/activity/1a2b3c4d5e/like.xml", h, null,
        Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
  }

  /**
   * General Test Case of Unlike activity
   *
   * @throws Exception
   */
  public void testDeleteLikeGeneralCase() throws Exception {
    String resourceUrl = RESOURCE_URL+"/12912903/like.json";
    
    // Unauthorized
    testAccessResourceAsAnonymous("DELETE", resourceUrl, null, null);
    // Wrong Portal Container name
    testStatusCodeOfResource("demo", "DELETE", "/api/social/v1-alpha3/wrongPortalContainerName/activity/1a2b3c4e5e/like.json", null, null,
                             Response.Status.BAD_REQUEST.getStatusCode());
    
    testStatusCodeOfResource("demo", "DELETE", "/api/social/v1-alpha3/portal/activity/1a2b3c4e5e/like.xml", null, null,
        Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
    
    String resourceUrlPostDelete = RESOURCE_URL+"/213123/like/destroy.json";
    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    h.putSingle("content-type", "application/json");
    h.putSingle("content-length", "0");
    
    // Unauthorized
    testAccessResourceAsAnonymous("POST", resourceUrlPostDelete,h, null);
    
    // Wrong Portal Container name
    testStatusCodeOfResource("demo", "POST", "/api/social/v1-alpha3/wrongPortalContainerName/activity/13123123/like/destroy.json", h, null,
                             Response.Status.BAD_REQUEST.getStatusCode());
    
    // Not Found 
    testStatusCodeOfResource("demo", "POST", "/api/social/v1-alpha3/portal/activity/13123123/like/destroy.json", h, null,
                             Response.Status.NOT_FOUND.getStatusCode());
    
    // Unsupported media type
    testStatusCodeOfResource("demo", "POST", "/api/social/v1-alpha3/portal/activity/1a2b3c4d5e/like/destroy.xml", h, null,
        Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode());
  }
  
  
  /**
   * Tests
   * {@link ActivityResources#getActivityById(javax.ws.rs.core.UriInfo, String, String, String, String, String, String)}
   * with json format.
   */
  public void testGetActivityByIdWithJsonFormat() throws Exception {
    createActivities(demoIdentity, demoIdentity, 1);
    ExoSocialActivity demoActivity = activityManager.getActivitiesWithListAccess(demoIdentity).load(0, 1)[0];
    activityManager.saveLike(demoActivity, demoIdentity);
    activityManager.saveLike(demoActivity, maryIdentity);
    activityManager.saveLike(demoActivity, johnIdentity);
    activityManager.saveLike(demoActivity, rootIdentity);
    
    createComment(demoActivity, demoIdentity, 30);
    connectIdentities(demoIdentity, johnIdentity, true);
    
    String resourceUrl = RESOURCE_URL+"/" + demoActivity.getId() + ".json";
    demoActivity = activityManager.getActivitiesWithListAccess(demoIdentity).load(0, 1)[0];
    
    { // get activity by id without any query param
      startSessionAs("demo");
      ContainerResponse containerResponse = service("GET", resourceUrl, "", null, null);
      assertEquals("containerResponse.getStatus() must return " + 200, 200, containerResponse.getStatus());
      
      assertTrue("Type of response's content must be: " + MediaType.APPLICATION_JSON_TYPE,
          containerResponse.getContentType().toString().startsWith(MediaType.APPLICATION_JSON_TYPE.toString()));
      HashMap<String, Object> entity =  (HashMap<String, Object>) containerResponse.getEntity();
      compareActivity(demoActivity, entity);
    }
    
    {//gets activity by specifying posterIdentity
      String posterIdentityResourceUrl = resourceUrl + "?poster_identity=true";
      startSessionAs("john");
      ContainerResponse containerResponse = service("GET", posterIdentityResourceUrl, "", null, null);
      assertEquals("containerResponse.getStatus() must return: " + 200, 200, containerResponse.getStatus());
      HashMap<String, Object> entity = (HashMap<String, Object>) containerResponse.getEntity();
      compareActivity(demoActivity, entity);
      compareIdentity(demoIdentity, (HashMap<String, Object>) entity.get("posterIdentity"));
    }

    {//gets activity by specifying totalNumberOfComments
      //creating the comments for unit testing.
      startSessionAs("john");
      createComment(demoActivity, johnIdentity, 30);
      String numberOfCommentsResourceUrl = resourceUrl + "?number_of_comments=20";
      ContainerResponse containerResponse = service("GET", numberOfCommentsResourceUrl, "", null, null);
      assertEquals("containerResponse.getStatus() must return: " + 200, 200, containerResponse.getStatus());
      
      HashMap<String, Object> entity = (HashMap<String, Object>) containerResponse.getEntity();
      compareActivity(demoActivity, entity);
      ArrayList<HashMap<String,Object>> comments = (ArrayList<HashMap<String,Object>>) entity.get("comments");
      assertEquals(20, comments.size());
      
    }

    {//gets activity by specifying activityStream
      String posterIdentityResourceUrl = resourceUrl + "?activity_stream=1";
      startSessionAs("john");
      ContainerResponse containerResponse = service("GET", posterIdentityResourceUrl, "", null, null);
      assertEquals("containerResponse.getStatus() must return: " + 200, 200, containerResponse.getStatus());
      HashMap<String, Object> entity = (HashMap<String, Object>) containerResponse.getEntity();
      assertNotNull("entity must not be null", entity);
      HashMap<String , Object> activityStream = (HashMap) entity.get("activityStream");
      compareActivity(demoActivity, entity);
      compareActivityStream(demoActivity.getActivityStream() ,activityStream);
    }
    
    {//gets activity by specifying numberoflikes
      String posterIdentityResourceUrl = resourceUrl + "?number_of_likes=2";
      startSessionAs("john");
      ContainerResponse containerResponse = service("GET", posterIdentityResourceUrl, "", null, null);
      assertEquals("containerResponse.getStatus() must return: " + 200, 200, containerResponse.getStatus());
      HashMap<String, Object> entity = (HashMap<String, Object>) containerResponse.getEntity();
      assertNotNull("entity must not be null", entity);
      ArrayList<HashMap<String,Object>> likedByIdentities = (ArrayList<HashMap<String,Object>>) entity.get("likedByIdentities");
      compareActivity(demoActivity, entity);
      compareNumberOfLikes(demoActivity, (ActivityRestOut)entity, 2);
    }
    
    {//Tests with full optional params
      startSessionAs("john");
      String allOfOptionalParamsResourceUrl = resourceUrl + "?poster_identity=true&number_of_comments=20&activity_stream=1&?number_of_likes=2";
      ContainerResponse containerResponse = service("GET", allOfOptionalParamsResourceUrl, "", null, null);
      assertEquals("containerResponse.getStatus() must return: " + 200, 200, containerResponse.getStatus());
      HashMap<String, Object> entity = (HashMap<String, Object>) containerResponse.getEntity();
      compareActivity(demoActivity, entity);
      
      //assert the activityStream
      HashMap<String, Object> activityStream = (HashMap<String, Object>) entity.get("activityStream");
      compareActivityStream(demoActivity.getActivityStream() ,activityStream);
      
      ArrayList<HashMap<String,Object>> comments = (ArrayList<HashMap<String,Object>>) entity.get("comments");
      assertEquals(20, comments.size());
      for (HashMap<String, Object> comment : comments) {
        IdentityRestOut identityRestOut = (IdentityRestOut) comment.get("posterIdentity");
        assertEquals("demo", identityRestOut.getRemoteId());
      }

    }
    //forbidden
    {
      createSpaces(1);
      Space space = spaceService.getSpaceByPrettyName("my_space_1");
      Identity spaceIdentity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, "my_space_1",false);
      
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title");
      activity.setAppId("appId");
      activity.setType("type");
      activity.setPriority(0.5F);
      activity.setTitleId("title id");
      activity.setUserId(demoIdentity.getId());

      Map<String, String> params = new HashMap<String, String>();
      params.put("key1", "value1");
      params.put("key2", "value2");
      activity.setTemplateParams(params);
      activityManager.saveActivityNoReturn(spaceIdentity, activity);
      tearDownActivityList.add(activity);
      
      testStatusCodeOfResource("john", "GET", RESOURCE_URL+"/" + activity.getId() + ".json", null, null,
          Response.Status.FORBIDDEN.getStatusCode());
    }
    
    {
      {//Tests with full optional params
        startSessionAs("john");
        String allOfOptionalParamsResourceUrl = resourceUrl + "?poster_identity=true&number_of_comments=-10&activity_stream=1&?number_of_likes=-10";
        ContainerResponse containerResponse = service("GET", allOfOptionalParamsResourceUrl, "", null, null);
        assertEquals("containerResponse.getStatus() must return: " + 200, 200, containerResponse.getStatus());
        HashMap<String, Object> entity = (HashMap<String, Object>) containerResponse.getEntity();
        compareActivity(demoActivity, entity);
        
        //assert the activityStream
        HashMap<String, Object> activityStream = (HashMap<String, Object>) entity.get("activityStream");
        compareActivityStream(demoActivity.getActivityStream() ,activityStream);
        
        ArrayList<HashMap<String,Object>> comments = (ArrayList<HashMap<String,Object>>) entity.get("comments");
        assertEquals(0, comments.size());
        compareNumberOfLikes(demoActivity, (ActivityRestOut)entity, 0);
      }      
    }
  }


  /**
   * Tests access permission to create a new activity.
   *
   * @throws Exception
   */
  public void testCreateNewActivityForAccess() throws Exception {
    StringWriter writer = new StringWriter();
    JSONWriter jsonWriter = new JSONWriter(writer);
    jsonWriter
        .object()
        .key("title")
        .value("hello world")
        .endObject();
    byte[] data = writer.getBuffer().toString().getBytes("UTF-8");

    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    h.putSingle("content-type", "application/json");
    h.putSingle("content-length", "" + data.length);
    
    //forbidden
    testStatusCodeOfResource("demo", "POST", RESOURCE_URL + ".json?identity_id="+maryIdentity.getId(), h, data, 
        Response.Status.FORBIDDEN.getStatusCode());
    //OK
    connectIdentities(demoIdentity, maryIdentity, true);
    testStatusCodeOfResource("demo", "POST", RESOURCE_URL + ".json?identity_id="+maryIdentity.getId(), h, data, 
        Response.Status.OK.getStatusCode());
  }

  /**
   * Tests {@link ActivityResources#deleteExistingActivityById(javax.ws.rs.core.UriInfo, String, String, String)}
   * with json format.
   */
  public void testDeleteActivityWithJsonFormat() throws Exception {
    startSessionAs("demo");
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("title");
    activity.setAppId("appId");
    activity.setType("type");
    activity.setPriority(0.5F);
    activity.setTitleId("title id");
    activity.setUserId(demoIdentity.getId());

    Map<String, String> params = new HashMap<String, String>();
    params.put("key1", "value1");
    params.put("key2", "value2");
    activity.setTemplateParams(params);
    activityManager.saveActivityNoReturn(rootIdentity, activity);
    
    ExoSocialActivity got1 = activityManager.getActivity(activity.getId());
    assertNotNull(got1);
    
    //Forbidden
    testStatusCodeOfResource("mary", "DELETE", RESOURCE_URL+"/" + activity.getId() + ".json", null, null,
        Response.Status.FORBIDDEN.getStatusCode());
    
    startSessionAs("demo");
    ContainerResponse response = service("DELETE", RESOURCE_URL+"/" + activity.getId() + ".json", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    HashMap<String, Object> activityRest = (HashMap<String, Object>)response.getEntity();
    assertEquals(got1.getId(), activityRest.get("id"));
    
    assertEquals(null, activityManager.getActivity(activity.getId()));

  }

  /**
   * Tests {@link ActivityResources#deleteExistingActivityById(javax.ws.rs.core.UriInfo, String, String, String)}
   * with json format.
   */
  public void testDeletePOSTActivityWithJsonFormat() throws Exception {

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("title");
    activity.setAppId("appId");
    activity.setType("type");
    activity.setPriority(0.5F);
    activity.setTitleId("title id");
    activity.setUserId(johnIdentity.getId());

    Map<String, String> params = new HashMap<String, String>();
    params.put("key1", "value1");
    params.put("key2", "value2");
    activity.setTemplateParams(params);
    activityManager.saveActivityNoReturn(rootIdentity, activity);

    ExoSocialActivity got1 = activityManager.getActivity(activity.getId());
    assertNotNull(got1);
    //Forbidden
    testStatusCodeOfResource("mary", "POST", RESOURCE_URL+"/destroy/" + activity.getId() + ".json", null, null,
        Response.Status.FORBIDDEN.getStatusCode());
    
    startSessionAs("john");
    connectIdentities(johnIdentity, rootIdentity, true);
        
    ContainerResponse response = service("POST", RESOURCE_URL+"/destroy/" + activity.getId() + ".json", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    HashMap<String, Object> activityRest = (HashMap<String, Object>)response.getEntity();
    assertEquals(got1.getId(), activityRest.get("id"));

    try {
      assertEquals(null, activityManager.getActivity(activity.getId()));
    }
    catch (Exception e) {
      // ok
    }

  }

  /**
   * Tests {@link ActivityResources#createNewActivity(javax.ws.rs.core.UriInfo, String, String, String, Activity)}
   * with json format.
   */
  public void testCreateNewActivityWithJsonFormat() throws Exception {
    startSessionAs("john");
    String title = "hello !";
    StringWriter writer = new StringWriter();
    JSONWriter jsonWriter = new JSONWriter(writer);
    jsonWriter
        .object()
        .key("title")
        .value(title)
        .endObject();
    byte[] data = writer.getBuffer().toString().getBytes("UTF-8");

    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    h.putSingle("content-type", "application/json");
    h.putSingle("content-length", "" + data.length);

    //
    ContainerResponse response =
        service("POST", RESOURCE_URL + ".json?identity_id=" + johnIdentity.getId(), "", h, data);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    //
    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof HashMap)) {
      fail();
    }
    HashMap<String, Object> got = (HashMap<String, Object>) response.getEntity();

    assertNotNull(got.get("id"));
    assertEquals(johnIdentity.getId(), got.get("identityId"));
    assertEquals(title, got.get("title"));

  }

  /**
   * Tests {@link ActivityResources#createLikeActivityById(javax.ws.rs.core.UriInfo, String, String, String) 
   */
  public void testCreateLikeActivityById() throws Exception {
    createActivities(demoIdentity, demoIdentity, 1);
    ExoSocialActivity expectedActivity = activityManager.getActivitiesWithListAccess(demoIdentity).load(0, 1)[0];
    
    startSessionAs("demo");
    ContainerResponse response =
      service("POST", RESOURCE_URL+"/" + expectedActivity.getId() + "/like.json", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    expectedActivity = activityManager.getActivitiesWithListAccess(demoIdentity).load(0, 1)[0];
    assertEquals("The activity liked array must contain Demo's IdentityID",expectedActivity.getLikeIdentityIds()[0], demoIdentity.getId());
    
    connectIdentities(demoIdentity, rootIdentity, true);
    startSessionAs("root");
    response =
      service("POST", RESOURCE_URL+"/" + expectedActivity.getId() + "/like.json", "", null, null);
    
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    HashMap<String, Object> got = (HashMap<String, Object>) response.getEntity();
    assertEquals(true, got.get("liked"));
    
    expectedActivity = activityManager.getActivitiesWithListAccess(demoIdentity).load(0, 1)[0];
    assertEquals("The activity liked array must contain Demo's IdentityID",expectedActivity.getLikeIdentityIds()[1], rootIdentity.getId());
    
    //Forbidden
    testStatusCodeOfResource("mary", "POST", RESOURCE_URL+"/" + expectedActivity.getId() + "/like.json", null, null,
        Response.Status.FORBIDDEN.getStatusCode());
  }
  
  public void testGetLikedIdentities() throws Exception {
    createActivities(demoIdentity, demoIdentity, 1);
    
    ExoSocialActivity expectedActivity = activityManager.getActivitiesWithListAccess(demoIdentity).load(0, 1)[0];
    
    activityManager.saveLike(expectedActivity, rootIdentity);
    activityManager.saveLike(expectedActivity, demoIdentity);
    activityManager.saveLike(expectedActivity, johnIdentity);
    activityManager.saveLike(expectedActivity, maryIdentity);
    
    startSessionAs("demo");
    ContainerResponse response =
      service("GET", RESOURCE_URL+"/" + expectedActivity.getId() + "/likes.json", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    
    HashMap<String, Object> got = (HashMap<String, Object>) response.getEntity();
    assertEquals("Number of liked Identities must equal 4",4, got.get("totalNumberOfLikes"));
    
    ArrayList<HashMap<String, Object>> identityArrayList = (ArrayList<HashMap<String, Object>>) got.get("likesByIdentities");
    assertEquals(maryIdentity.getId(), identityArrayList.get(0).get("id"));
    assertEquals(johnIdentity.getId(), identityArrayList.get(1).get("id"));
    assertEquals(demoIdentity.getId(), identityArrayList.get(2).get("id"));
    assertEquals(rootIdentity.getId(), identityArrayList.get(3).get("id"));
  }
  
  /**
   * Tests {@link ActivityResources#deleteLikeActivityById(javax.ws.rs.core.UriInfo, String, String, String)
   * Tests {@link ActivityResources#postDeleteLikeActivityById(javax.ws.rs.core.UriInfo, String, String, String)
   */
  public void testDeleteLikeActivityById() throws Exception {
    testCreateLikeActivityById();
    ExoSocialActivity expectedActivity = activityManager.getActivitiesWithListAccess(demoIdentity).load(0, 1)[0];
    startSessionAs("demo");
    assertEquals("Number of user liked this Activty must be 2", 2, expectedActivity.getLikeIdentityIds().length);
    ContainerResponse response = service("DELETE", RESOURCE_URL+"/" + expectedActivity.getId() + "/like.json", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    expectedActivity = activityManager.getActivitiesWithListAccess(demoIdentity).load(0, 1)[0];
    assertEquals("Number of user liked this Activty must be 1", 1, expectedActivity.getLikeIdentityIds().length);
    
    String demoIdentiyID = demoIdentity.getId();
    String [] likedIdentityIds = expectedActivity.getLikeIdentityIds();
    
    for(String likedUserIdentityID: likedIdentityIds){
      if(likedUserIdentityID.equals(demoIdentiyID)){
        fail("Demo's IdentityId must be deleted from getLikeIdentityIds");
      }
    }
    //Forbidden
    testStatusCodeOfResource("mary", "DELETE", RESOURCE_URL+"/" + expectedActivity.getId() + "/like.json", null, null,
        Response.Status.FORBIDDEN.getStatusCode());
    
    startSessionAs("root");
    response = service("POST", RESOURCE_URL+"/" + expectedActivity.getId() + "/like/destroy.json", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    expectedActivity = activityManager.getActivitiesWithListAccess(demoIdentity).load(0, 1)[0];
    assertEquals("Number of user liked this Activty must be 0", 0, expectedActivity.getLikeIdentityIds().length);
    
    HashMap<String, Object> got = (HashMap<String, Object>) response.getEntity();
    assertEquals(false, got.get("liked"));

    String rootIdentiyID = rootIdentity.getId();
    likedIdentityIds = expectedActivity.getLikeIdentityIds();
    for(String likedUserIdentityID: likedIdentityIds){
      if(likedUserIdentityID.equals(rootIdentiyID)){
        fail("Root's IdentityId must be deleted from getLikeIdentityIds");
      }
    }
    //Forbidden
    testStatusCodeOfResource("mary", "POST", RESOURCE_URL+"/" + expectedActivity.getId() + "/like/destroy.json", null, null,
        Response.Status.FORBIDDEN.getStatusCode());
  }
  
  /**
   * Test {@link ActivityResources#createCommentActivityById(javax.ws.rs.core.UriInfo, String, String, String, Comment)}
   */
  public void testCreateComment() throws Exception{
    createActivities(johnIdentity, maryIdentity, 1);
    connectIdentities(johnIdentity, maryIdentity, true);
    connectIdentities(demoIdentity, maryIdentity, true);
    connectIdentities(demoIdentity, johnIdentity, true);
    ExoSocialActivity expectedActivity = activityManager.getActivitiesWithListAccess(maryIdentity).load(0, 1)[0];
    
    startSessionAs("mary");
    StringWriter writer = new StringWriter();
    JSONWriter jsonWriter = new JSONWriter(writer);
    jsonWriter
        .object()
        .key("text")
        .value("mary comment to john's activity")
        .endObject();
    byte[] data = writer.getBuffer().toString().getBytes("UTF-8");

    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    h.putSingle("content-type", "application/json");
    h.putSingle("content-length", "" + data.length);
    
    ContainerResponse response = service("POST", RESOURCE_URL+"/" + expectedActivity.getId() + "/comment.json", "", h, data);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    HashMap<String, Object> got = (HashMap<String, Object>) response.getEntity();
    assertEquals("mary comment to john's activity", got.get("text"));
    
    startSessionAs("demo");
    writer = new StringWriter();
    jsonWriter = new JSONWriter(writer);
    jsonWriter
        .object()
        .key("text")
        .value("demo comment to john's activity")
        .endObject();
    data = writer.getBuffer().toString().getBytes("UTF-8");

    h = new MultivaluedMapImpl();
    h.putSingle("content-type", "application/json");
    h.putSingle("content-length", "" + data.length);
    
    response = service("POST", RESOURCE_URL+"/" + expectedActivity.getId() + "/comment.json", "", h, data);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    
    //Forbidden
    testStatusCodeOfResource("root", "POST", RESOURCE_URL+"/" + expectedActivity.getId() + "/comment.json", h, data,
        Response.Status.FORBIDDEN.getStatusCode());
  }
  /**
   * Test {@link ActivityResources#getCommentActivityById(javax.ws.rs.core.UriInfo, String, String, String)
   */
  public void testGetComments() throws Exception{
    testCreateComment();
    ExoSocialActivity expectedActivity = activityManager.getActivitiesWithListAccess(maryIdentity).load(0, 1)[0];

    startSessionAs("john");
    ContainerResponse response = service("GET", RESOURCE_URL+"/" + expectedActivity.getId() + "/comments.json", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    
    HashMap<String, Object> resultEntity = (HashMap<String, Object>) response.getEntity();
    ArrayList comments = (ArrayList) resultEntity.get("comments");
    assertEquals("totalNumberOfComments of user liked this Activty must be 2",2,resultEntity.get("totalNumberOfComments"));
    assertEquals("Number of comments this Activty must be 2",2,comments.size());
    compareIdentity(maryIdentity, (HashMap<String, Object>) ((HashMap<String, Object>)comments.get(0)).get("posterIdentity"));
    assertEquals("John's comment text must be \"mary comment to john's activity\"",
        "mary comment to john's activity",
        ((HashMap<String, Object>)comments.get(0)).get("text"));

    compareIdentity(demoIdentity, (HashMap<String, Object>) ((HashMap<String, Object>)comments.get(1)).get("posterIdentity"));
    assertEquals("Demo's comment text must be \"demo comment to john's activity\"",
        "demo comment to john's activity",
        ((HashMap<String, Object>)comments.get(1)).get("text"));
  }
  
  /**
   * Test {@link ActivityResources#deleteCommentById(javax.ws.rs.core.UriInfo, String, String, String, String)
   * Test {@link ActivityResources#postDeleteCommentById(javax.ws.rs.core.UriInfo, String, String, String, String)
   */
  public void testDeleteComment() throws Exception{
    testCreateComment();
    ExoSocialActivity expectedActivity = activityManager.getActivitiesWithListAccess(maryIdentity).load(0, 1)[0];
    ListAccess<ExoSocialActivity> commentActivitysListAccess= activityManager.getCommentsWithListAccess(expectedActivity);
    
    assertEquals("commentActivitys Size must be equal 2", 2 , commentActivitysListAccess.getSize());
    
    ExoSocialActivity[] commentActivitys = commentActivitysListAccess.load(0, 2);
    
    //Forbidden
    testStatusCodeOfResource("root", "POST", RESOURCE_URL+"/" + expectedActivity.getId() + "/comment/destroy/"+ commentActivitys[1].getId() +".json", null, null,
        Response.Status.FORBIDDEN.getStatusCode());
    
    startSessionAs("mary");
    ContainerResponse response = service("DELETE", RESOURCE_URL+"/" + expectedActivity.getId() + "/comment/"+ 
                                  commentActivitys[0].getId() +".json", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    
    HashMap<String,Object> entity = (HashMap<String, Object>) response.getEntity();
    assertEquals("The id of response activity must be equal: " + commentActivitys[0].getId(), 
                  commentActivitys[0].getId(),entity.get("id"));
    
    commentActivitysListAccess= activityManager.getCommentsWithListAccess(expectedActivity);
    assertEquals("commentActivitys Size must be equal 1", 1 , commentActivitysListAccess.getSize());
    startSessionAs("demo");
    response = service("POST", RESOURCE_URL+"/" + expectedActivity.getId() + "/comment/destroy/"+ commentActivitys[1].getId() +".json", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    
    entity = (HashMap<String, Object>) response.getEntity();
    assertEquals("The id of response activity must be equal: " + commentActivitys[0].getId(), 
                  commentActivitys[1].getId(),entity.get("id"));
    
    commentActivitysListAccess= activityManager.getCommentsWithListAccess(expectedActivity);
    assertEquals("commentActivitys Size must be equal 0", 0 , commentActivitysListAccess.getSize());
  }
  
  /**
   * An identity posts an activity to an identity's activity stream with a number of activities.
   *
   * @param posterIdentity the identity who posts activity
   * @param identityStream the identity who has activity stream to be posted.
   * @param number the number of activities
   */
  private void createActivities(Identity posterIdentity, Identity identityStream, int number) {
    for (int i = 0; i < number; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setType("exosocial:core");
      activity.setTitle("title " + i);
      activity.setUserId(posterIdentity.getId());
      activityManager.saveActivityNoReturn(identityStream, activity);
      activity = activityManager.getActivity(activity.getId());
      tearDownActivityList.add(activity);
    }
  }

  /**
   * Creates a comment to an existing activity.
   *
   * @param existingActivity the existing activity
   * @param posterIdentity the identity who comments
   * @param number the number of comments
   */
  private void createComment(ExoSocialActivity existingActivity, Identity posterIdentity, int number) {
    int maxActivities = 20;
    for (int i = 0; i < number; i++) {
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("comment " + i);
      comment.setUserId(posterIdentity.getId());
      activityManager.saveComment(existingActivity, comment);
      comment = activityManager.getCommentsWithListAccess(existingActivity).loadAsList(0, maxActivities).get(0);
    }
  }

  /**
   * Gets an instance of the space.
   *
   * @param number the number to be created
   */
  private void createSpaces(int number) {
    for (int i = 0; i < number; i++) {
      Space space = new Space();
      space.setDisplayName("my space " + number);
      space.setPrettyName(space.getDisplayName());
      space.setRegistration(Space.OPEN);
      space.setDescription("add new space " + number);
      space.setType(DefaultSpaceApplicationHandler.NAME);
      space.setVisibility(Space.PUBLIC);
      space.setRegistration(Space.VALIDATION);
      space.setPriority(Space.INTERMEDIATE_PRIORITY);
      space.setGroupId("/space/space" + number);
      String[] managers = new String[]{"demo"};
      String[] members = new String[]{"demo", "mary"};
      String[] invitedUsers = new String[]{"john"};
      String[] pendingUsers = new String[]{};
      space.setInvitedUsers(invitedUsers);
      space.setPendingUsers(pendingUsers);
      space.setManagers(managers);
      space.setMembers(members);
      space.setUrl("/space/" + space.getPrettyName());
      try {
        spaceService.saveSpace(space, true);
        tearDownSpaceList.add(space);
      } catch (SpaceException e) {
        fail("Could not create a new space");
      }
    }
  }

  /**
   * Connects 2 identities, if toConfirm = true, they're connected. If false, in pending connection type.
   *
   * @param senderIdentity the identity who sends connection request
   * @param receiverIdentity the identity who receives connnection request
   * @param beConfirmed boolean value
   */
  private void connectIdentities(Identity senderIdentity, Identity receiverIdentity, boolean beConfirmed) {
    relationshipManager.inviteToConnect(senderIdentity, receiverIdentity);
    if (beConfirmed) {
      relationshipManager.confirm(receiverIdentity, senderIdentity);
    }
    tearDownRelationshipList.add(relationshipManager.get(senderIdentity, receiverIdentity));
  }

}
