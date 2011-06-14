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

package org.exoplatform.social.core.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.application.RelationshipPublisher.TitleId;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class ActivityStorageNewTestCase extends AbstractCoreTest {
  private IdentityStorage identityStorage;
  private ActivityStorage activityStorage;
  private RelationshipStorage relationshipStorage;
  private List<ExoSocialActivity> tearDownActivityList;

  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    activityStorage = (ActivityStorage) getContainer().getComponentInstanceOfType(ActivityStorage.class);
    relationshipStorage = (RelationshipStorage) getContainer().getComponentInstanceOfType(RelationshipStorage.class);

    assertNotNull(identityStorage);
    assertNotNull(activityStorage);
    assertNotNull(relationshipStorage);

    rootIdentity = new Identity("organization", "root");
    johnIdentity = new Identity("organization", "john");
    maryIdentity = new Identity("organization", "mary");
    demoIdentity = new Identity("organization", "demo");

    identityStorage.saveIdentity(rootIdentity);
    identityStorage.saveIdentity(johnIdentity);
    identityStorage.saveIdentity(maryIdentity);
    identityStorage.saveIdentity(demoIdentity);

    assertNotNull(rootIdentity.getId());
    assertNotNull(johnIdentity.getId());
    assertNotNull(maryIdentity.getId());
    assertNotNull(demoIdentity.getId());

    tearDownActivityList = new ArrayList<ExoSocialActivity>();
  }

  @Override
  protected void tearDown() throws Exception {

    for (ExoSocialActivity activity : tearDownActivityList) {
      activityStorage.deleteActivity(activity.getId());
    }

    identityStorage.deleteIdentity(rootIdentity);
    identityStorage.deleteIdentity(johnIdentity);
    identityStorage.deleteIdentity(maryIdentity);
    identityStorage.deleteIdentity(demoIdentity);

    super.tearDown();
  }

  public void testActivityCount() throws Exception {

    // fill 10 activities
    for (int i = 0; i < 10; ++i) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title " + i);
      activityStorage._createActivity(rootIdentity, activity);
    }

    //
    assertEquals(10, activityStorage.getActivitiesCount(rootIdentity));

    // remove 5 activities
    Iterator<ExoSocialActivity> it = activityStorage.getActivities(rootIdentity).iterator();

    for (int i = 0; i < 5; ++i) {
      activityStorage.deleteActivity(it.next().getId());
    }

    //
    assertEquals(5, activityStorage.getActivitiesCount(rootIdentity));
  }

  /**
   * Test {@link org.exoplatform.social.core.storage.ActivityStorage#getActivity(String)}
   */
  public void testUserPostActivityToSpace() throws ActivityStorageException {
    // Create new Space and its Identity
    Space space = getSpaceInstance();
    SpaceIdentityProvider spaceIdentityProvider = (SpaceIdentityProvider) getContainer().getComponentInstanceOfType(SpaceIdentityProvider.class);
    Identity spaceIdentity = spaceIdentityProvider.createIdentity(space);
    identityStorage.saveIdentity(spaceIdentity);
    
    // john posted activity on created Space
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("Space's Activity");
    activity.setUserId(johnIdentity.getId());

    activityStorage.saveActivity(spaceIdentity, activity);
    
    // Get posted Activity and check
    ExoSocialActivity gotActivity = activityStorage.getActivity(activity.getId());
    
    assertEquals("userId must be " + johnIdentity.getId(), johnIdentity.getId(), gotActivity.getUserId());
    
    identityStorage.deleteIdentity(spaceIdentity);
  }

  public void testActivityOrder() throws Exception {
    // fill 10 activities
    for (int i = 0; i < 10; ++i) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title " + i);
      activityStorage._createActivity(rootIdentity, activity);
    }

    int i = 9;
    for (ExoSocialActivity activity : activityStorage.getActivities(rootIdentity)) {
      assertEquals("title " + i, activity.getTitle());
      --i;
    }
  }

  public void testActivityOrder2() throws Exception {
    // fill 10 activities
    for (int i = 0; i < 10; ++i) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title " + i);
      activityStorage._createActivity(rootIdentity, activity);
    }

    // remove 5 activities
    Iterator<ExoSocialActivity> it = activityStorage.getActivities(rootIdentity).iterator();

    for (int i = 0; i < 5; ++i) {
      activityStorage.deleteActivity(it.next().getId());
    }

    // fill 10 others
    for (int i = 0; i < 10; ++i) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title " + i);
      activityStorage._createActivity(rootIdentity, activity);
    }

    List<ExoSocialActivity> activityies = activityStorage.getActivities(rootIdentity);
    int i = 0;
    int[] values = {9, 8, 7, 6, 5, 4, 3, 2, 1, 0, 4, 3, 2, 1, 0};
    for (ExoSocialActivity activity : activityies) {
      assertEquals("title " + values[i], activity.getTitle());
      ++i;
    }
  }

  public void testCommentOrder() throws Exception {
    // fill 10 activities
    for (int i = 0; i < 10; ++i) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("title " + i);
      activityStorage._createActivity(rootIdentity, activity);

      // fill 10 comments for each activity
      for(int j = 0; j < 10; ++j) {
        ExoSocialActivity comment = new ExoSocialActivityImpl();
        comment.setTitle("title " + i + j);
        comment.setUserId(rootIdentity.getId());
        activityStorage.saveComment(activity, comment);
      }
    }

    int i = 9;
    for (ExoSocialActivity activity : activityStorage.getActivities(rootIdentity)) {
      int j = 0;
      for (String commentId : activity.getReplyToId().split(",")) {
        if (!"".equals(commentId)) {
          assertEquals("title " + i + j, activityStorage.getActivity(commentId).getTitle());
          ++j;
        }
      }
      --i;
    }
  }

  public void testDeleteComment() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("activity title");

    activityStorage.saveActivity(rootIdentity, activity);

    activity = activityStorage.getActivity(activity.getId());

    for (int i = 0; i < 10; ++i) {
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("comment title " + i);
      comment.setUserId(rootIdentity.getId());

      activityStorage.saveComment(activity, comment);
    }

    assertEquals(11, activityStorage.getActivity(activity.getId()).getReplyToId().split(",").length);

    int i = 0;
    activity = activityStorage.getActivity(activity.getId());
    for (String commentId : activity.getReplyToId().split(",")) {
      if (!"".equals(commentId) && i < 5) {
        activityStorage.deleteActivity(commentId);
        ++i;
      }
    }

    assertEquals(6, activityStorage.getActivity(activity.getId()).getReplyToId().split(",").length);
  }

  public void testLike() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("activity title");

    activityStorage.saveActivity(rootIdentity, activity);

    activity.setLikeIdentityIds(new String[] {rootIdentity.getId(), johnIdentity.getId(), demoIdentity.getId()});

    activityStorage.saveActivity(rootIdentity, activity);

    List<ExoSocialActivity> activities = activityStorage.getActivities(rootIdentity);

    assertEquals(1, activities.size());
    assertEquals(3, activities.get(0).getLikeIdentityIds().length);

    List<String> ids = Arrays.asList(activities.get(0).getLikeIdentityIds());

    assertTrue(ids.contains(rootIdentity.getId()));
    assertTrue(ids.contains(johnIdentity.getId()));
    assertTrue(ids.contains(demoIdentity.getId()));
    assertTrue(!ids.contains(maryIdentity.getId()));
  }

  public void testContactActivities() throws Exception {

    //
    assertEquals(0, activityStorage.getActivitiesOfConnections(Arrays.asList(rootIdentity, johnIdentity), 0, 100).size());

    for (int i = 0; i < 10; ++i) {

      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("root activity " + i);
      activityStorage.saveActivity(rootIdentity, activity);

      ExoSocialActivity activity2 = new ExoSocialActivityImpl();
      activity2.setTitle("john activity " + i);
      activityStorage.saveActivity(johnIdentity, activity2);

      ExoSocialActivity activity3 = new ExoSocialActivityImpl();
      activity3.setTitle("mary activity " + i);
      activityStorage.saveActivity(maryIdentity, activity3);
    }

    //
    List<ExoSocialActivity> activities = activityStorage.getActivitiesOfConnections(Arrays.asList(rootIdentity, johnIdentity), 0, 100);
    assertEquals(20, activities.size());

    int i = 9;
    Iterator<ExoSocialActivity> it = activities.iterator();
    while (it.hasNext()) {

      ExoSocialActivity activity = it.next();
      assertEquals("john activity " + i, activity.getTitle());

      activity = it.next();
      assertEquals("root activity " + i, activity.getTitle());
      --i;
    }

  }

  public void testTimeStamp() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("activity title");
    activityStorage.saveActivity(rootIdentity, activity);

    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setUserId(rootIdentity.getId());
    comment.setTitle("comment title");

    activityStorage.saveComment(activity, comment);

    List<ExoSocialActivity> activities = activityStorage.getActivities(rootIdentity);

    assertEquals(1, activities.size());
    assertFalse(activities.get(0).getPostedTime() == 0);
    assertEquals(2, activities.get(0).getReplyToId().split(",").length);

    ExoSocialActivity gotComment = activityStorage.getActivity(activities.get(0).getReplyToId().split(",")[1]);
    assertFalse(gotComment.getPostedTime() == 0);

  }
  
  public void testRelationshipActivity() throws Exception {
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle("I am now connected with @receiverRemoteId");
    activity.setType("exosocial:relationship");
    //Shindig's Activity's fields
    activity.setAppId("appId");
    activity.setBody("body");
    activity.setBodyId("bodyId");
    activity.setTitleId(TitleId.CONNECTION_REQUESTED.toString());
    activity.setExternalId("externalId");
    //activity.setId("id");
    activity.setUrl("http://www.exoplatform.org");
    activity.setUserId(demoIdentity.getId());
    
    Map<String,String> params = new HashMap<String,String>();
    params.put("SENDER", "senderRemoteId");
    params.put("RECEIVER", "receiverRemoteId");
    params.put("RELATIONSHIP_UUID", "relationship_id");
    activity.setTemplateParams(params);
    
    activityStorage.saveActivity(rootIdentity, activity);
    
    List<ExoSocialActivity> activities = activityStorage.getActivities(rootIdentity);
    assertNotNull(activities);
    assertEquals(1, activities.size());
    
    for(ExoSocialActivity element : activities) {
     
      //title
      assertNotNull(element.getTitle());
      //type
      assertNotNull(element.getType());
      //appId
      assertNotNull(element.getAppId());
      //body
      assertNotNull(element.getBody());
      //bodyId
      assertNotNull(element.getBodyId());
      //titleId
      assertEquals(TitleId.CONNECTION_REQUESTED.toString(), element.getTitleId());
      //externalId
      assertNotNull(element.getExternalId());
      //id
      //assertNotNull(element.getId());
      //url
      assertEquals("http://www.exoplatform.org", element.getUrl());
      //id
      assertNotNull(element.getUserId());
      //templateParams
      assertNotNull(element.getTemplateParams());
      
    }
    
    
  }

  
  /**
   * Gets an instance of Space.
   *
   * @return an instance of space
   */
  private Space getSpaceInstance() {
    Space space = new Space();
    space.setDisplayName("my space");
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space");
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/space/space");
    String[] managers = new String[] {"john", "demo"};
    space.setManagers(managers);
    return space;
  }
  
  // TODO : test many days
}
