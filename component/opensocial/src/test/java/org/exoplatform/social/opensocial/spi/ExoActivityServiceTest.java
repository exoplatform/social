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
package org.exoplatform.social.opensocial.spi;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.shindig.social.core.model.ActivityImpl;
import org.apache.shindig.social.opensocial.model.Activity;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.impl.organization.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.opensocial.ExoBlobCrypterSecurityToken;
import org.exoplatform.social.opensocial.test.AbstractExoOpenSocialTest;
import org.exoplatform.social.space.Space;
import org.exoplatform.social.space.SpaceService;
import org.exoplatform.social.space.impl.SpaceIdentityProvider;

public class ExoActivityServiceTest extends AbstractExoOpenSocialTest {

  private static final String ORGANIZATION = OrganizationIdentityProvider.NAME;
  private static final String SPACE = SpaceIdentityProvider.NAME;

  /**
   * posting with the UserId = username
   *
   * @throws Exception
   */
  //TODO hoatle unsupported
  public void testPostToSelfStreamWithUsername() throws Exception {
    assertTrue(true);
/*    IdentityManager identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    // format : organization:username
    Identity identity = identityManager.getOrCreateIdentity(ORGANIZATION, "root");
    String userId = "root";
    Activity activity = createActivityOnOwnStream(userId);
    org.exoplatform.social.core.activitystream.model.Activity actual = getFirstActivity(identity);
    assertEquals(actual.getBody(), activity.getBody());
    assertEquals(actual.getUserId(), identity.getId());
    assertEquals(actual.getStreamOwner(), identity.getRemoteId());*/
  }

  /**
   * posting with the UserId having GlobalId notation (xx:yyy)
   *
   * @throws Exception
   */
  public void testPostToSelfStreamWithGlobalId() throws Exception {
    IdentityManager identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);

    // format : organization:username
    Identity identity = identityManager.getOrCreateIdentity(ORGANIZATION,
                                                            "john");
    String globalId = ORGANIZATION + ":john";
    Activity activity = createActivityOnOwnStream(globalId);
    org.exoplatform.social.core.activitystream.model.Activity actual = getFirstActivity(identity);
    assertEquals(actual.getBody(), activity.getBody());
    assertEquals(actual.getUserId(), identity.getId());
    assertEquals(actual.getStreamOwner(), identity.getRemoteId());
  }

  /**
   * posting with the UserId = UUID
   *
   * @throws Exception
   */
  public void testPostToSelfStreamWithUUID() throws Exception {
    IdentityManager identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);

    // format : organization:username
    Identity identity = identityManager.getOrCreateIdentity(ORGANIZATION,
                                                            "mary");
    String uuid = identity.getId();
    Activity activity = createActivityOnOwnStream(uuid);
    org.exoplatform.social.core.activitystream.model.Activity actual = getFirstActivity(identity);
    assertEquals(actual.getBody(), activity.getBody());
    assertEquals(actual.getUserId(), identity.getId());
    assertEquals(actual.getStreamOwner(), identity.getRemoteId());
  }

  /**
   * posting with the UserId = organization:UUID
   *
   * @throws Exception
   */
  //TODO hoatle unsupported
  public void testPostToSelfStreamWithGlobalUUID() throws Exception {
    assertTrue(true);
/*    IdentityManager identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    // format : organization:username
    Identity identity = identityManager.getOrCreateIdentity(ORGANIZATION,
                                                            "jack");
    String globalId = ORGANIZATION + ":" + identity.getId();
    Activity activity = createActivityOnOwnStream(globalId);
    org.exoplatform.social.core.activitystream.model.Activity actual = getFirstActivity(identity);
    assertEquals(actual.getBody(), activity.getBody());
    assertEquals(actual.getUserId(), identity.getId());
    assertEquals(actual.getStreamOwner(), identity.getRemoteId());*/
  }

  private org.exoplatform.social.core.activitystream.model.Activity getFirstActivity(Identity identity) throws Exception {
    ActivityManager activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    List<org.exoplatform.social.core.activitystream.model.Activity> activities = activityManager.getActivities(identity);
    assertNotNull(activities);
    assertEquals(activities.size(), 1);
    org.exoplatform.social.core.activitystream.model.Activity act = activities.get(0);
    return act;
  }

  private Activity createActivityOnOwnStream(String objectId) {
    ExoActivityService activityService = new ExoActivityService();
    UserId viewer = new UserId(UserId.Type.viewer, null);
    GroupId self = new GroupId(GroupId.Type.self, null);
    String appId = "test";
    Set<String> fields = Collections.emptySet();
    Activity activity = new ActivityImpl();
    activity.setBody("");
    activity.setTitle("");
    FakeToken token = new FakeToken();
    token.ownerId = objectId;
    token.viewerId = objectId;
    activityService.createActivity(viewer, self, appId, fields, activity, token);
    return activity;
  }

  public void testPostToSpaceWithGlobalId() throws Exception {
    ExoActivityService activityService = new ExoActivityService();
    IdentityManager identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    Identity root = identityManager.getOrCreateIdentity(ORGANIZATION, "root");
    String rootIdentityId = root.getId();

    Space space = new Space();
    space.setName("space1");
    space.setGroupId("/spaces/space1");
    space.setDescription("desc");
    SpaceService spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
    spaceService.saveSpace(space, true);

    Identity spaceIdentity = identityManager.getOrCreateIdentity(SPACE,"space:space1");
    String spaceId = spaceIdentity.getRemoteId();

    UserId viewer = new UserId(UserId.Type.viewer, null);
    String globalId = "space:space1";// we are passing a globalId
    GroupId spaceGroup = new GroupId(GroupId.Type.groupId, globalId);
    String appId = "test";
    Set<String> fields = Collections.emptySet();
    Activity activity = new ActivityImpl();
    activity.setBody("root sur space1");
    activity.setTitle("Root Root");
    FakeToken token = new FakeToken();
    token.ownerId = rootIdentityId;
    token.viewerId = rootIdentityId;
    activityService.createActivity(viewer, spaceGroup, appId, fields, activity, token);

    org.exoplatform.social.core.activitystream.model.Activity actual = getFirstActivity(spaceIdentity);
    assertEquals(actual.getBody(), activity.getBody());
    assertEquals(actual.getUserId(), rootIdentityId);
    assertEquals(actual.getStreamOwner(), spaceId);

  }



  public void postToSpaceWithUUID() throws Exception {
    ExoActivityService activityService = new ExoActivityService();
    IdentityManager identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    Identity poster = identityManager.getOrCreateIdentity(ORGANIZATION, "root");
    String posterIdentityId = poster.getId();

    Space space = new Space();
    space.setName("space1");
    space.setGroupId("/spaces/space1");
    space.setDescription("desc");
    SpaceService spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
    spaceService.saveSpace(space, true);

    Identity spaceIdentity = identityManager.getOrCreateIdentity(SPACE,
                                                                 "space:space1");
    String spaceId = spaceIdentity.getRemoteId();

    UserId viewer = new UserId(UserId.Type.viewer, null);
    String uuid = spaceIdentity.getId(); // we are passing an UUID
    GroupId spaceGroup = new GroupId(GroupId.Type.groupId, uuid);
    String appId = "test";
    Set<String> fields = Collections.emptySet();
    Activity activity = new ActivityImpl();
    activity.setBody("ron sur space1");
    activity.setTitle("Ron Ron");
    FakeToken token = new FakeToken();
    token.ownerId = posterIdentityId;
    token.viewerId = posterIdentityId;
    activityService.createActivity(viewer, spaceGroup, appId, fields, activity, token);

    org.exoplatform.social.core.activitystream.model.Activity actual = getFirstActivity(spaceIdentity);
    assertEquals(actual.getBody(), activity.getBody());
    assertEquals(actual.getUserId(), posterIdentityId);
    assertEquals(actual.getStreamOwner(), spaceId);

  }

  public class FakeToken extends ExoBlobCrypterSecurityToken {
    public String ownerId;

    public String viewerId;

    public FakeToken() {
      super(null, "default", "eXo");
    }

    public String getActiveUrl() {
      return "http://localhost:8080/social/social/rpc";
    }

    public String getAppId() {
      return getAppUrl();
    }

    public String getAppUrl() {
      return "http://localhost:8080/rest-socialdemo/jcr/repository/portal-system/production/app:gadgets/app:statusUpdate2/app:data/app:resources/activities2.xml";
    }

    public String getPortalContainer() {
      return "portal";
    }

    public String getHostName() {
      return "localhost";
    }

    public long getModuleId() {
      return 1566685858;
    }

    public String getOwnerId() {
      return ownerId;
    }

    public String getTrustedJson() {
      return "trusted";
    }

    public String getViewerId() {
      return viewerId;
    }

    public boolean isAnonymous() {
      return false;
    }
  }

}
