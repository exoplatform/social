/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.social.service.test;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;

import org.exoplatform.services.rest.ContainerResponseWriter;
import org.exoplatform.services.rest.impl.ContainerRequest;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.EnvironmentContext;
import org.exoplatform.services.rest.impl.InputHeadersMap;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.rest.tools.DummyContainerResponseWriter;
import org.exoplatform.services.test.mock.MockHttpServletRequest;
import org.exoplatform.social.core.activity.model.ActivityStream;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.service.rest.Util;
import org.exoplatform.social.service.rest.api.models.ActivityRestListOut;
import org.exoplatform.social.service.rest.api.models.ActivityRestOut;
import org.exoplatform.social.service.rest.api.models.IdentityRestOut;
import org.exoplatform.ws.frameworks.json.impl.JsonDefaultHandler;
import org.exoplatform.ws.frameworks.json.impl.JsonException;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;
import org.exoplatform.ws.frameworks.json.impl.JsonParserImpl;
import org.exoplatform.ws.frameworks.json.value.JsonValue;

/**
 * AbstractResourceTest.java <br />
 * Provides <code>service</code> method to test rest service.
 * @author <a href="http://hoatle.net">hoatle</a>
 * @since Mar 3, 2010
 */
public abstract class AbstractResourceTest extends AbstractServiceTest {

  /**
   * gets response with provided writer
   * @param method
   * @param requestURI
   * @param baseURI
   * @param headers
   * @param data
   * @param writer
   * @return
   * @throws Exception
   */
  public ContainerResponse service(String method,
                                   String requestURI,
                                   String baseURI,
                                   Map<String, List<String>> headers,
                                   byte[] data,
                                   ContainerResponseWriter writer) throws Exception {

    if (headers == null) {
      headers = new MultivaluedMapImpl();
    }

    ByteArrayInputStream in = null;
    if (data != null) {
      in = new ByteArrayInputStream(data);
    }

    EnvironmentContext envctx = new EnvironmentContext();
    HttpServletRequest httpRequest = new SocialMockHttpServletRequest("",
                                                                      in,
                                                                      in != null ? in.available() : 0,
                                                                      method,
                                                                      headers);
    envctx.put(HttpServletRequest.class, httpRequest);
    EnvironmentContext.setCurrent(envctx);
    ContainerRequest request = new ContainerRequest(method,
                                                    new URI(requestURI),
                                                    new URI(baseURI),
                                                    in,
                                                    new InputHeadersMap(headers));
    ContainerResponse response = new ContainerResponse(writer);
    requestHandler.handleRequest(request, response);
    return response;
  }

  /**
   * gets response without provided writer
   * @param method
   * @param requestURI
   * @param baseURI
   * @param headers
   * @param data
   * @return
   * @throws Exception
   */
  public ContainerResponse service(String method,
                                   String requestURI,
                                   String baseURI,
                                   MultivaluedMap<String, String> headers,
                                   byte[] data) throws Exception {
    return service(method, requestURI, baseURI, headers, data, new DummyContainerResponseWriter());
  }

  /**
   * Asserts if the provided jsonString is equal to an entity object's string.
   *
   * @param jsonString the provided json string
   * @param entity the provided entity object
   */
  public void assertJsonStringEqualsEntity(String jsonString, Object entity) throws JsonException {
    JsonParserImpl jsonParser = new JsonParserImpl();
    JsonDefaultHandler jsonDefaultHandler = new JsonDefaultHandler();
    jsonParser.parse(new InputStreamReader(new ByteArrayInputStream(jsonString.getBytes())), jsonDefaultHandler);

    JsonValue firstJsonValue = jsonDefaultHandler.getJsonObject();
    assertNotNull("firstJsonValue must not be null", firstJsonValue);

    JsonValue secondJsonValue = new JsonGeneratorImpl().createJsonObject(entity);
    assertNotNull("secondJsonValue must not be null", secondJsonValue);

    assertEquals(firstJsonValue.toString(), secondJsonValue.toString());
  }

  /**
   * Asserts if the provided xmlString is equal to an entity object's string.
   *
   * @param xmlString the provided xml string
   * @param entity the provided entity object
   */
  public void assertXmlStringEqualsEntity(String xmlString, Object entity) {
    //TODO implement this
  }


  /**
   * Tests: an anonymous user that accesses a resource requires authentication.
   *
   * @param method the http method string
   * @param resourceUrl the resource url to access
   * @param data the data if any
   * @throws Exception
   */
  protected void testAccessResourceAsAnonymous(String method, String resourceUrl, MultivaluedMap<String,String> h, byte[] data) throws Exception {
    testStatusCodeOfResource(null, method, resourceUrl, h, data, 401);

  }


  /**
   * Tests: an authenticated user that accesses a resource that is forbidden, has no permission.
   *
   * @param username the portal user name
   * @param method the http method string
   * @param resourceUrl the resource url to access
   * @param data the data if any
   * @throws Exception
   */
  protected void testAccessResourceWithoutPermission(String username, String method, String resourceUrl, byte[] data)
                                                  throws Exception {
    testStatusCodeOfResource(username, method, resourceUrl, null, data, 403);
  }


  /**
   * Tests: an authenticated user that accesses a resource that is not found.
   *
   * @param username the portal user name
   * @param method the http method string
   * @param resourceUrl the resource url to access
   * @param data the data if any
   * @throws Exception
   */
  protected void testAccessNotFoundResourceWithAuthentication(String username, String method, String resourceUrl,
                                                              byte[] data) throws Exception {
    testStatusCodeOfResource(username, method, resourceUrl, null, data, 404);
  }
  
  /**
   * Tests if the return status code matches the response of the request with provided username, method, resourceUrl, headers, and data.
   *
   * @param username the portal user name if userName == null mean not authenticated.
   * @param method the http method string
   * @param resourceUrl the resource url to access
   * @param h the header MultivalueMap
   * @param data the data if any
   * @param statusCode the expected status code of response
   * @throws Exception
   */
  protected void testStatusCodeOfResource(String username, String method, String resourceUrl, 
                                                              MultivaluedMap<String,String> h,
                                                              byte[] data, int statusCode) throws Exception {
    if(username != null){
      startSessionAs(username);
    } else {
      endSession();
    }
    ContainerResponse containerResponse = service(method, resourceUrl, "", h, data);

    assertEquals("The response code of resource("+resourceUrl+") is not expected.)",statusCode, containerResponse.getStatus());
  }
  /**
   * Comare ExoSocialActivity with entity HashMap
   * @param activity
   * @param entity
   */
  protected void compareActivity(ExoSocialActivity activity, HashMap<String, Object> entity){
    assertNotNull("entity must not be null", entity);
    
    assertEquals("activity.getId() must equal:", activity.getId(), entity.get("id"));
    
    assertEquals("activity.getTitle() must equal: " + activity.getTitle() == null ? "" :activity.getTitle(),
                  activity.getTitle() == null ? "" : activity.getTitle(),
                  entity.get("title"));

    //TODO check this (Phuong)
    /*
    assertEquals("activity.getLikedByIdentities() size be equal:",
                    activity.getLikeIdentityIds() == null ? 0 : activity.getLikeIdentityIds().length,
                    ((ArrayList<HashMap<String, Object>>)entity.get("likedByIdentities")).size());
    
    String[] identityIds = activity.getLikeIdentityIds();
    ArrayList<HashMap<String, Object>> likedIdentities = (ArrayList<HashMap<String, Object>>)entity.get("likedByIdentities");
    if(identityIds != null){
      for(int i = 0; i < identityIds.length; i++){
        IdentityManager identityManager =  Util.getIdentityManager();
        Identity likedIdentity = identityManager.getIdentity(identityIds[i],true);
        compareIdentity(likedIdentity, likedIdentities.get(i));
      }
    }
    */
    
    assertEquals("entity.getAppId() must equal: " + activity.getAppId() == null ? "" :activity.getAppId(),
                  activity.getAppId() == null ? "" :activity.getAppId(), entity.get("appId"));
    
    assertEquals("activity.getType() must equal: " + activity.getType() == null ? "" : activity.getType(),
                  activity.getType() == null ? "" : activity.getType(), entity.get("type"));
    
    assertEquals("entity.PostedTime() must equal: " + activity.getPostedTime() == null ? 0 : activity.getPostedTime(),
                  activity.getPostedTime() == null ? 0 : activity.getPostedTime(),
                  (Long) entity.get("postedTime"));
    
    assertTrue(((Long) entity.get("lastUpdated")).longValue() > 0);
    
    assertNotNull("entity.get(\"createdAt\"): ", entity.get("createdAt"));
    
    Float expectedPriority;
    if(activity.getPriority() == null){
      expectedPriority = new Float(0);
    } else {
      expectedPriority = activity.getPriority();
    }
    
    assertEquals("activity.getPriority() must equal:",
                  expectedPriority,
                  entity.get("priority"));
    assertEquals(activity.getTemplateParams() == null ? new HashMap() : activity.getTemplateParams(),
                  entity.get("templateParams"));
    assertEquals("activity.getTitleId() must return: " + activity.getTitleId() == null ? "" : activity.getTitleId(),
                  activity.getTitleId() == null ? "" : activity.getTitleId() ,
                  entity.get("titleId"));
    Identity streamOwnerIdentity = Util.getOwnerIdentityIdFromActivity(activity);
    assertEquals("activity.getIdentityId() must return: " + streamOwnerIdentity.getId() == null ? "" : streamOwnerIdentity.getId(),
                  streamOwnerIdentity.getId() == null ? "" : streamOwnerIdentity.getId(),
                  entity.get("identityId"));
    
    assertEquals("TotalNumberOfComments must be equal:",
                  Util.getActivityManager().getCommentsWithListAccess(activity).getSize(),
                  entity.get("totalNumberOfComments"));
  }
  /**
   * Compares ActivityStream with entity HashMap
   * @param activityStream
   * @param entity
   */
  protected void compareActivityStream(ActivityStream activityStream, HashMap<String, Object> entity){
    assertNotNull("entity must not be null", entity);
    assertEquals("activityStream.getPrettyId() must equal: " + activityStream.getPrettyId() == null ? "" :activityStream.getPrettyId(),
        activityStream.getPrettyId() == null ? "" : activityStream.getPrettyId(),
        entity.get("prettyId"));
    assertEquals("activityStream.getFaviconUrl() must equal: " + activityStream.getFaviconUrl() == null ? "" :activityStream.getPrettyId(),
        activityStream.getFaviconUrl() == null ? "" : activityStream.getFaviconUrl(),
        entity.get("faviconUrl"));
    assertEquals("activityStream.getTitle() must equal: " + activityStream.getTitle() == null ? "" :activityStream.getTitle(),
        activityStream.getTitle() == null ? "" : activityStream.getTitle(),
        entity.get("title"));
    assertEquals("activityStream.getPermaLink() must equal: " + activityStream.getPermaLink() == null ? "" : Util.getBaseUrl() + activityStream.getPermaLink(),
        activityStream.getPermaLink() == null ? "" : Util.getBaseUrl() +  activityStream.getPermaLink(),
        entity.get("permaLink"));
  }
  
  /**
   * Compares Identity with entity HashMap
   * @param identity
   * @param entity
   */
  protected void compareIdentity(Identity identity, HashMap<String, Object> entity){
    assertNotNull("entity must not be null", entity);
    assertEquals("identity.Id() must equal: " + identity.getId() == null ? "" :identity.getId(),
        identity.getId() == null ? "" : identity.getId(),
        entity.get("id"));
    assertEquals("identity.getProviderId() must equal: " + identity.getProviderId() == null ? "" :identity.getProviderId(),
        identity.getProviderId() == null ? "" : identity.getProviderId(),
        entity.get("providerId"));
    assertEquals("identity.getRemoteId() must equal: " + identity.getRemoteId() == null ? "" :identity.getRemoteId(),
        identity.getRemoteId() == null ? "" : identity.getRemoteId(),
        entity.get("remoteId"));
    
    HashMap<String, Object > profileEntity = (HashMap<String, Object>) entity.get("profile");
    compareProfile(identity.getProfile(),profileEntity);
  }
  
  /**
   * Compares Profile with entity HashMap
   * @param profile
   * @param entity
   */
  protected void compareProfile(Profile profile, HashMap<String, Object> entity){
    assertNotNull("entity must not be null", entity);
    assertEquals("profile.getPrettyId() must equal: " + profile.getFullName() == null ? "" :profile.getFullName(),
        profile.getFullName() == null ? "" : profile.getFullName(),
        entity.get("fullName"));
    assertTrue("profile.getAvatarUrl() must be start with \"http\" " + profile.getAvatarUrl() == null ? "" :profile.getAvatarUrl(),
        ((String)entity.get("avatarUrl")).startsWith("http"));
    //TODO: compare full default avatar URL/ absolute avatar URL
  }
  /**
   * Compares ExoSocialActivity's comment with entity HashMap
   * @param activity
   * @param entity
   */
  protected void compareComment(ExoSocialActivity activity, HashMap<String, Object> entity){
    assertNotNull("entity must not be null", entity);
    assertEquals("activity.getId() must be equal:" + activity.getId() == null ? "" :activity.getId(),
        activity.getId() == null ? "" :activity.getId(), entity.get("id"));
    Identity posterIdentity = Util.getOwnerIdentityIdFromActivity(activity);
    compareIdentity(posterIdentity, (HashMap<String, Object>) entity.get("posterIdentity"));
    assertEquals("activity.getId() must be equal:" + activity.getTitle() == null ? "" :activity.getTitle(),
        activity.getTitle() == null ? "" :activity.getTitle(), entity.get("text"));

    assertEquals("activity.getId() must be equal:" + activity.getPostedTime() == null ? 0 :activity.getPostedTime(),
        activity.getPostedTime() == null ? 0 :activity.getPostedTime(), (Long) entity.get("postedTime"));
  }

  /**
   * Compares the list of activities.
   *
   * @param activityList the activity list
   * @param responseEntity the response entity
   */
  protected void compareActivities(List<ExoSocialActivity> activityList, ActivityRestListOut responseEntity) {
    List<ActivityRestOut> entityList = (List<ActivityRestOut>) responseEntity.
                                                               get(ActivityRestListOut.Field.ACTIVITIES.toString());
    assertEquals("entityList.size() must return: " + activityList.size(), activityList.size(), entityList.size());

    for (int i = 0; i < entityList.size(); i++) {
      ExoSocialActivity activity = activityList.get(i);
      ActivityRestOut entity = entityList.get(i);
      compareActivity(activity, entity);
    }

  }

  /**
   * Compare number of comments.
   *
   * @param activityList
   * @param responseEntity
   * @param numberOfComments
   */
  protected void compareNumberOfComments(List<ExoSocialActivity> activityList, ActivityRestListOut responseEntity,
                                         int numberOfComments) {
    List<ActivityRestOut> entityList = (List<ActivityRestOut>) responseEntity.
                                                               get(ActivityRestListOut.Field.ACTIVITIES.toString());
    ActivityManager activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    for (int i = 0; i < entityList.size(); i++) {
      ExoSocialActivity activity = activityList.get(i);
      int commentNumber = Math.min(numberOfComments, activityManager.getCommentsWithListAccess(activity).getSize());
      ActivityRestOut entity = entityList.get(i);
      assertEquals("entity.getComments().size() must return: " + commentNumber,
              commentNumber,
              entity.getComments().size()); 
    }
  }


  /**
   * Compare number of likes.
   *
   * @param activityList
   * @param responseEntity
   * @param numberOfLikes
   */
  protected void compareNumberOfLikes(List<ExoSocialActivity> activityList, ActivityRestListOut responseEntity,
                                      int numberOfLikes) {
    List<ActivityRestOut> entityList = (List<ActivityRestOut>) responseEntity.
                                                               get(ActivityRestListOut.Field.ACTIVITIES.toString());
    for (int i = 0; i < entityList.size(); i++) {
      ExoSocialActivity activity = activityList.get(i);
      int likeNumber = Math.min(numberOfLikes, activity.getLikeIdentityIds().length);
      ActivityRestOut entity = entityList.get(i);
      assertEquals("entity.getLikedByIdentities().size() must return: " + likeNumber,
              likeNumber,
              entity.getLikedByIdentities().size());
      int activityLikedIdentityIdLength = activity.getLikeIdentityIds().length;
      List<IdentityRestOut> likedByIdentities = entity.getLikedByIdentities();

      for (int j = 0; j < likedByIdentities.size(); j++) {
        assertEquals("likedByIdentities.get(j).getId() must return: " +
                activity.getLikeIdentityIds()[activityLikedIdentityIdLength - j - 1],
                activity.getLikeIdentityIds()[activityLikedIdentityIdLength - j - 1], likedByIdentities.get(j).getId());
      }

    }
  }
  
  /**
   * Compare number of likes.
   *
   * @param activity
   * @param responseEntity
   * @param numberOfLikes
   */
  protected void compareNumberOfLikes(ExoSocialActivity activity, ActivityRestOut responseEntity,
                                      int numberOfLikes) {
    int likeNumber = Math.min(numberOfLikes, activity.getLikeIdentityIds().length);
    assertEquals("entity.getLikedByIdentities().size() must return: " + likeNumber,
            likeNumber,
            responseEntity.getLikedByIdentities().size());
    int activityLikedIdentityIdLength = activity.getLikeIdentityIds().length;
    List<IdentityRestOut> likedByIdentities = responseEntity.getLikedByIdentities();
    for (int i = 0; i < likedByIdentities.size(); i++) {
      assertEquals("likedByIdentities.get(i).getId() must return: " +
                   activity.getLikeIdentityIds()[activityLikedIdentityIdLength - i - 1],
                   activity.getLikeIdentityIds()[activityLikedIdentityIdLength - i - 1],
                   likedByIdentities.get(i).getId());
    }

  }
}
