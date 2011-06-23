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

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.service.rest.api.models.Activity;
import org.exoplatform.social.service.test.AbstractResourceTest;
import org.json.JSONWriter;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit Test for {@link ActivityResources}.
 *
 * @author <a href="http://hoatle.net">hoatle (hoatlevan at gmail dot com)</a>
 * @since Jun 16, 2011
 */
public class ActivityResourcesTest extends AbstractResourceTest {

  private final String RESOURCE_URL = "/api/social/v1-alpha1/portal/activity";

  private IdentityManager identityManager;
  private ActivityManager activityManager;
  private Identity rootIdentity;
  private Identity johnIdentity;
  private ExoSocialActivity activity;

  /**
   * Adds {@link ActivityResources}.
   *
   * @throws Exception
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();
    addResource(ActivityResources.class, null);

    //
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    activityManager =  (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", false);
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john", false);

    //
    activity = new ExoSocialActivityImpl();
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

    //
    activityManager.saveActivityNoReturn(rootIdentity, activity);

    ExoSocialActivity comment1 = new ExoSocialActivityImpl();
    comment1.setTitle("comment 1");
    comment1.setUserId(rootIdentity.getId());

    ExoSocialActivity comment2 = new ExoSocialActivityImpl();
    comment2.setTitle("comment 2");
    comment2.setUserId(rootIdentity.getId());

    activityManager.saveComment(activity, comment1);
    activityManager.saveComment(activity, comment2);

  }

  /**
   * Removes {@link ActivityResources}.
   *
   * @throws Exception
   */
  @Override
  public void tearDown() throws Exception {
    removeResource(ActivityResources.class);

    //
    identityManager.deleteIdentity(rootIdentity);

    super.tearDown();
  }

  /**
   * Tests access permission to get an activity.
   *
   * @throws Exception
   */
  public void testGetActivityByIdForAccess() throws Exception {

    testAccessNotFoundResourceWithAuthentication("root", "GET", RESOURCE_URL+"/fake.json", null);

    // TODO : unauthorized
    // TODO : forbidden

  }


  /**
   * Tests {@link ActivityResources#getActivityById(javax.ws.rs.core.UriInfo, String, String, String, String, String,
   * String)} with json format.
   */
  public void testGetActivityByIdWithJsonFormat() throws Exception {

    ContainerResponse response = service("GET", RESOURCE_URL+"/" + activity.getId() + ".json", "", null, null);

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof Activity)) {
      fail();
    }

    Activity got = (Activity) response.getEntity();

    assertEquals(activity.getId(), got.getId());
    assertEquals(activity.getTitle(), got.getTitle());
    assertEquals(activity.getPriority(), activity.getPriority());
    assertEquals(activity.getAppId(), got.getAppId());
    assertEquals(activity.getType(), got.getType());
    assertEquals(activity.getTitleId(), got.getTitleId());
    assertEquals(activity.getTemplateParams().size(), activity.getTemplateParams().size());
    assertEquals(false, got.isLiked());
    assertEquals(null, got.getLikedByIdentities());
    assertEquals(null, got.getComments());
    assertEquals(rootIdentity.getId(), got.getIdentityId());
    assertEquals(null, got.getPosterIdentity());
    assertEquals(null, got.getActivityStream());
  }

  /**
   * Tests {@link ActivityResources#getActivityById(javax.ws.rs.core.UriInfo, String, String, String, String, String,
   * String)} with json format.
   */
  public void testGetActivityByIdWithComment1WithJsonFormat() throws Exception {

    ContainerResponse response = service("GET", RESOURCE_URL+"/" + activity.getId() + ".json?numberOfComments=1", "", null, null);

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof Activity)) {
      fail();
    }

    Activity got = (Activity) response.getEntity();

    assertEquals(activity.getId(), got.getId());
    assertEquals(activity.getTitle(), got.getTitle());
    assertEquals(activity.getPriority(), activity.getPriority());
    assertEquals(activity.getAppId(), got.getAppId());
    assertEquals(activity.getType(), got.getType());
    assertEquals(activity.getTitleId(), got.getTitleId());
    assertEquals(activity.getTemplateParams().size(), activity.getTemplateParams().size());
    assertEquals(false, got.isLiked());
    assertEquals(null, got.getLikedByIdentities());
    assertEquals(1, got.getComments().length);
    assertEquals(rootIdentity.getId(), got.getIdentityId());
    assertEquals(null, got.getPosterIdentity());
    assertEquals(null, got.getActivityStream());
  }

  /**
   * Tests {@link ActivityResources#getActivityById(javax.ws.rs.core.UriInfo, String, String, String, String, String,
   * String)} with json format.
   */
  public void testGetActivityByIdWithComment10WithJsonFormat() throws Exception {

    ContainerResponse response = service("GET", RESOURCE_URL+"/" + activity.getId() + ".json?numberOfComments=10", "", null, null);

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof Activity)) {
      fail();
    }

    Activity got = (Activity) response.getEntity();

    assertEquals(activity.getId(), got.getId());
    assertEquals(activity.getTitle(), got.getTitle());
    assertEquals(activity.getPriority(), activity.getPriority());
    assertEquals(activity.getAppId(), got.getAppId());
    assertEquals(activity.getType(), got.getType());
    assertEquals(activity.getTitleId(), got.getTitleId());
    assertEquals(activity.getTemplateParams().size(), activity.getTemplateParams().size());
    assertEquals(false, got.isLiked());
    assertEquals(null, got.getLikedByIdentities());
    assertEquals(2, got.getComments().length);
    assertEquals(rootIdentity.getId(), got.getIdentityId());
    assertEquals(null, got.getPosterIdentity());
    assertEquals(null, got.getActivityStream());
  }

  /**
   * Tests {@link ActivityResources#getActivityById(javax.ws.rs.core.UriInfo, String, String, String, String, String,
   * String)} with json format.
   */
  public void testGetActivityByIdWithPosterIdTrueWithJsonFormat() throws Exception {

    ContainerResponse response = service("GET", RESOURCE_URL+"/" + activity.getId() + ".json?posterIdentity=true", "", null, null);

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof Activity)) {
      fail();
    }

    Activity got = (Activity) response.getEntity();

    assertEquals(activity.getId(), got.getId());
    assertEquals(activity.getTitle(), got.getTitle());
    assertEquals(activity.getPriority(), activity.getPriority());
    assertEquals(activity.getAppId(), got.getAppId());
    assertEquals(activity.getType(), got.getType());
    assertEquals(activity.getTitleId(), got.getTitleId());
    assertEquals(activity.getTemplateParams().size(), activity.getTemplateParams().size());
    assertEquals(false, got.isLiked());
    assertEquals(null, got.getLikedByIdentities());
    assertEquals(null, got.getComments());
    assertEquals(rootIdentity.getId(), got.getIdentityId());
    assertEquals(johnIdentity.getId(), got.getPosterIdentity());
    assertEquals(null, got.getActivityStream());
  }

  /**
   * Tests {@link ActivityResources#getActivityById(javax.ws.rs.core.UriInfo, String, String, String, String, String,
   * String)} with json format.
   */
  public void testGetActivityByIdWithPosterIdTWithJsonFormat() throws Exception {

    ContainerResponse response = service("GET", RESOURCE_URL+"/" + activity.getId() + ".json?posterIdentity=t", "", null, null);

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof Activity)) {
      fail();
    }

    Activity got = (Activity) response.getEntity();

    assertEquals(activity.getId(), got.getId());
    assertEquals(activity.getTitle(), got.getTitle());
    assertEquals(activity.getPriority(), activity.getPriority());
    assertEquals(activity.getAppId(), got.getAppId());
    assertEquals(activity.getType(), got.getType());
    assertEquals(activity.getTitleId(), got.getTitleId());
    assertEquals(activity.getTemplateParams().size(), activity.getTemplateParams().size());
    assertEquals(false, got.isLiked());
    assertEquals(null, got.getLikedByIdentities());
    assertEquals(null, got.getComments());
    assertEquals(rootIdentity.getId(), got.getIdentityId());
    assertEquals(johnIdentity.getId(), got.getPosterIdentity());
    assertEquals(null, got.getActivityStream());
  }

  /**
   * Tests {@link ActivityResources#getActivityById(javax.ws.rs.core.UriInfo, String, String, String, String, String,
   * String)} with json format.
   */
  public void testGetActivityByIdWithPosterId1WithJsonFormat() throws Exception {

    ContainerResponse response = service("GET", RESOURCE_URL+"/" + activity.getId() + ".json?posterIdentity=1", "", null, null);

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof Activity)) {
      fail();
    }

    Activity got = (Activity) response.getEntity();

    assertEquals(activity.getId(), got.getId());
    assertEquals(activity.getTitle(), got.getTitle());
    assertEquals(activity.getPriority(), activity.getPriority());
    assertEquals(activity.getAppId(), got.getAppId());
    assertEquals(activity.getType(), got.getType());
    assertEquals(activity.getTitleId(), got.getTitleId());
    assertEquals(activity.getTemplateParams().size(), activity.getTemplateParams().size());
    assertEquals(false, got.isLiked());
    assertEquals(null, got.getLikedByIdentities());
    assertEquals(null, got.getComments());
    assertEquals(rootIdentity.getId(), got.getIdentityId());
    assertEquals(johnIdentity.getId(), got.getPosterIdentity());
    assertEquals(null, got.getActivityStream());
  }

  /**
   * Tests {@link ActivityResources#getActivityById(javax.ws.rs.core.UriInfo, String, String, String, String, String,
   * String)} with json format.
   */
  public void testGetActivityByIdWithStreamWithJsonFormat() throws Exception {

    ContainerResponse response =
        service("GET", RESOURCE_URL+"/" + activity.getId() + ".json?activityStream=true", "", null, null);

    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof Activity)) {
      fail();
    }

    Activity got = (Activity) response.getEntity();

    assertEquals(activity.getId(), got.getId());
    assertEquals(activity.getTitle(), got.getTitle());
    assertEquals(activity.getPriority(), activity.getPriority());
    assertEquals(activity.getAppId(), got.getAppId());
    assertEquals(activity.getType(), got.getType());
    assertEquals(activity.getTitleId(), got.getTitleId());
    assertEquals(activity.getTemplateParams().size(), activity.getTemplateParams().size());
    assertEquals(false, got.isLiked());
    assertEquals(null, got.getLikedByIdentities());
    assertEquals(null, got.getComments());
    assertEquals(rootIdentity.getId(), got.getIdentityId());
    assertEquals(null, got.getPosterIdentity());
    assertEquals(activity.getStreamTitle(), got.getActivityStream().getTitle());
    assertEquals(activity.getStreamOwner(), got.getActivityStream().getPrettyId());
    assertEquals(activity.getStreamFaviconUrl(), got.getActivityStream().getFaviconUrl());
    assertEquals(activity.getStreamUrl(), got.getActivityStream().getPermalink());
  }

  /**
   * Tests access permission to create a new activity.
   *
   * @throws Exception
   */
  public void testCreateNewActivityForAccess() throws Exception {

    // TODO : unauthorized
    // TODO : forbidden

  }

  /**
   * Tests {@link ActivityResources#deleteExistingActivityById(javax.ws.rs.core.UriInfo, String, String, String)}
   * with json format.
   */
  public void testDeleteDELETEActivityWithJsonFormat() throws Exception {

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

    ContainerResponse response = service("DELETE", RESOURCE_URL+"/" + activity.getId() + ".json", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    try {
      ExoSocialActivity got2 = activityManager.getActivity(activity.getId());
      fail();
    }
    catch (Exception e) {
      // ok
    }

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

    ContainerResponse response = service("POST", RESOURCE_URL+"/destroy/" + activity.getId() + ".json", "", null, null);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    try {
      ExoSocialActivity got2 = activityManager.getActivity(activity.getId());
      fail();
    }
    catch (Exception e) {
      // ok
    }

  }

  /**
   * Tests {@link ActivityResources#createNewActivity(javax.ws.rs.core.UriInfo, String, String, String,
   * org.exoplatform.social.service.rest.api.models.Activity)}
   * with json format.
   */
  public void testCreateNewActivityWithJsonFormat() throws Exception {

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
        service("POST", RESOURCE_URL + "/new.json?identityId=" + rootIdentity.getId(), "", h, data);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    //
    assertNotNull(response.getEntity());
    if (!(response.getEntity() instanceof Activity)) {
      fail();
    }
    Activity got = (Activity) response.getEntity();

    assertNotNull(got.getId());
    assertEquals(title, got.getTitle());

  }

}
