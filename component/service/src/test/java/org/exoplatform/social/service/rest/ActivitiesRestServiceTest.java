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
package org.exoplatform.social.service.rest;

import org.apache.commons.lang.ArrayUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.service.test.AbstractResourceTest;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * ActivitiesRestServiceTest.java.
 *
 * @author <a href="http://hoatle.net">hoatle</a>
 * @since  Mar 4, 2010
 * @copyright eXo Platform SAS
 */
public class ActivitiesRestServiceTest extends AbstractResourceTest {
  static private PortalContainer container;
  static private ActivityManager activityManager;
  private IdentityManager identityManager;

  static private String activityId;

  private List<Identity> tearDownIdentityList;
  private List<ExoSocialActivity> tearDownActivityList;

  private Identity demoIdentity;
  private Identity johnIdentity;

  private String ACTIVITIES_RESOURCE_URL;
  private final String JSON_FORMAT = ".json";

  public void setUp() throws Exception {
    super.setUp();

    container = getContainer();
    activityManager = (ActivityManager) container.getComponentInstanceOfType(ActivityManager.class);
    identityManager = (IdentityManager) container.getComponentInstanceOfType(IdentityManager.class);

    assertNotNull(activityManager);
    assertNotNull(identityManager);

    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo", false);
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john", false);

    tearDownIdentityList = new ArrayList<Identity>();
    tearDownIdentityList.add(johnIdentity);

    tearDownActivityList = new ArrayList<ExoSocialActivity>();

    ACTIVITIES_RESOURCE_URL = "/" + container.getName() + "/social/activities/";

    addResource(ActivitiesRestService.class, null);

    populateData();
//    startSessionAs("root");
  }

  public void tearDown() throws Exception {

    //Removing test activities
    for (ExoSocialActivity activity: tearDownActivityList) {
      activityManager.deleteActivity(activity);
    }

    //Removing test identities
    for (Identity identity: tearDownIdentityList) {
      identityManager.deleteIdentity(identity);
    }

    //Removing Rest Activities resource
    removeResource(ActivitiesRestService.class);

    super.tearDown();
  }

  public void testDestroyActivity() throws Exception {
    ContainerResponse response = service("POST", "/portal/social/activities/destroy/123.json", "", null, null);
    assertEquals(500, response.getStatus());

    response = service("POST", "/portal/social/activities/destroy/123.xml", "", null, null);
    assertEquals(500, response.getStatus());
  }

  public void testShowLikes() throws Exception {

  }

  public void testUpdateLike() throws Exception {

  }

  public void testDestroyLike() throws Exception {
    //Load activity of demo user
    ExoSocialActivity activity = activityManager.getActivitiesWithListAccess(demoIdentity).load(0, 1)[0];
    String activityId = activity.getId();

    //Initialize Like data on Activity
    activityManager.saveLike(activity, demoIdentity);
    activityManager.saveLike(activity, johnIdentity);

    int initialLikeCount = activity.getLikeIdentityIds().length;

    //Check if the activity was liked successfully
    assertEquals(2, initialLikeCount);

    String destroyLikeURL = ACTIVITIES_RESOURCE_URL
        + activity.getId()
        + "/likes/destroy/"
        + johnIdentity.getId()
        + JSON_FORMAT;

    ContainerResponse response = service("POST", destroyLikeURL, "", null, null);

    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

    activity = activityManager.getActivity(activityId);
    String[] likeIdentities = activity.getLikeIdentityIds();
    //Check likes count
    assertEquals(initialLikeCount - 1, likeIdentities.length);

    //Assert the correct identity id was removed from activity's likes
    assertFalse(ArrayUtils.contains(likeIdentities, johnIdentity.getId()));
  }

  public void testShowComments() throws Exception {

  }

  public void testUpdateComment() throws Exception {


  }

  public void testDestroyComment() throws Exception {

  }

  private void populateData() throws Exception {
    populateActivitiesData(1, demoIdentity);
  }

  private void populateActivitiesData(int activitiesCount, Identity ownerIdentity) {
    for (int i = 0; i < activitiesCount; i++)
    {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity" + i);
      activity.setType("exosocial:core");
      activity.setPriority((float) 1.0);
      activity.setTitleId("");
      activity.setTemplateParams(new HashMap<String, String>());
      //Add the specified user identity as owner of the activity
      activity.setUserId(ownerIdentity.getId());

      activityManager.saveActivityNoReturn(ownerIdentity, activity);

      tearDownActivityList.add(activityManager.getActivity(activity.getId()));
    }
  }
}
