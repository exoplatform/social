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
 * along with this program; if not, see<http:www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.manager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.BaseActivityProcessorPlugin;
import org.exoplatform.social.core.activity.model.Activity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * Unit Test for {@link ActivityManager}, including cache tests.
 * @author hoat_le
 */
public class ActivityManagerTest extends AbstractCoreTest {
  private final Log LOG = ExoLogger.getLogger(ActivityManagerTest.class);
  private List<Activity> tearDownActivityList;
  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  private IdentityManager identityManager;
  private RelationshipManager relationshipManager;
  private ActivityManager activityManager;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    relationshipManager = (RelationshipManager) getContainer().getComponentInstanceOfType(RelationshipManager.class);
    activityManager =  (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    tearDownActivityList = new ArrayList<Activity>();
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root");
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john");
    maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary");
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo");

  }

  @Override
  public void tearDown() throws Exception {
    for (Activity activity : tearDownActivityList) {
      try {
        activityManager.deleteActivity(activity.getId());
      } catch (Exception e) {
        LOG.warn("can not delete activity with id: " + activity.getId());
      }
    }
    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(johnIdentity);
    identityManager.deleteIdentity(maryIdentity);
    identityManager.deleteIdentity(demoIdentity);
    super.tearDown();
  }

  /**
   * Test for {@link ActivityManager#saveActivity(Activity)}
   *
   * and {@link ActivityManager#saveActivity(Identity, Activity)}
   */
  public void testSaveActivity() {
    //save mal-formed activity
    {
      Activity malformedActivity = new Activity();
      malformedActivity.setTitle("malform");
      try {
        activityManager.saveActivity(malformedActivity);
      } catch (IllegalArgumentException e) {
        LOG.info("test with malfomred activity passes.");
      }
    }

    {
      final String activityTitle = "root activity";
      Activity rootActivity = new Activity();
      rootActivity.setTitle(activityTitle);
      rootActivity.setUserId(rootIdentity.getId());
      activityManager.saveActivity(rootActivity);

      assertNotNull("rootActivity.getId() must not be null", rootActivity.getId());

      //updates
      rootActivity.setTitle("Hello World");
      activityManager.saveActivity(rootActivity);

      tearDownActivityList.add(rootActivity);
    }

    {
      final String title = "john activity";
      Activity johnActivity = new Activity();
      johnActivity.setTitle(title);
      activityManager.saveActivity(johnIdentity, johnActivity);

      tearDownActivityList.add(johnActivity);

      assertNotNull("johnActivity.getId() must not be null", johnActivity.getId());
    }
    
    {
      Activity activity = new Activity();
      
      //test for reserving order of map values for i18n activity
      Map<String, String> templateParams = new LinkedHashMap<String, String>();
      templateParams.put("key1", "value 1");
      templateParams.put("key2", "value 2");
      templateParams.put("key3", "value 3");
      activity.setTemplateParams(templateParams);
      activity.setTitle("test template params order");
      activityManager.saveActivity(johnIdentity, activity);
      tearDownActivityList.add(activity);
      
      activity = activityManager.getActivity(activity.getId());
      assertNotNull("activity must not be null", activity);
      Map<String, String> gotTemplateParams = activity.getTemplateParams();
      List<String> values = new ArrayList(gotTemplateParams.values());
      assertEquals("value 1", values.get(0));
      assertEquals("value 2", values.get(1));
      assertEquals("value 3", values.get(2));
    }
  }


  /**
   * Test {@link ActivityManager#getActivity(String)}
   */
  public void testGetActivity() {
      List<Activity> rootActivities = activityManager.getActivities(rootIdentity);
      assertEquals("user's activities should have 0 element.", 0, rootActivities.size());

      Activity activity = new Activity();
      activity.setTitle("title");
      activity.setUserId(rootIdentity.getId());

      activityManager.saveActivity(rootIdentity, activity);

      rootActivities = activityManager.getActivities(rootIdentity);
      assertEquals("user's activities should have 1 element", 1, rootActivities.size());

      tearDownActivityList.addAll(rootActivities);
  }

  /**
   * Unit Test for:
   * <p>
   * {@link ActivityManager#deleteActivity(String)}
   * {@link ActivityManager#deleteActivity(Activity)}
   */
  public void testDeleteActivity() {
    assert true;
  }

  public  void testGetCommentWithHtmlContent(){
    String htmlString = "<span><strong>foo</strong>bar<script>zed</script></span>";
    String htmlRemovedString = "<span><strong>foo</strong>bar&lt;script&gt;zed&lt;/script&gt;</span>";
    
    Activity activity = new Activity();
    activity.setTitle("blah blah");
    activityManager.saveActivity(rootIdentity, activity);

    Activity comment = new Activity();
    comment.setTitle(htmlString);
    comment.setUserId(rootIdentity.getId());
    comment.setBody(htmlString);
    activityManager.saveComment(activity, comment);
    assertNotNull("comment.getId() must not be null", comment.getId());

    List<Activity> comments = activityManager.getComments(activity);
    assertEquals(1, comments.size());
    assertEquals(htmlRemovedString, comments.get(0).getBody());
    assertEquals(htmlRemovedString, comments.get(0).getTitle());
    tearDownActivityList.add(activity);    
  }
  
  public  void testGetComment(){
    Activity activity = new Activity();
    activity.setTitle("blah blah");
    activityManager.saveActivity(rootIdentity, activity);

    Activity comment = new Activity();
    comment.setTitle("comment blah blah");
    comment.setUserId(rootIdentity.getId());

    activityManager.saveComment(activity, comment);

    assertNotNull("comment.getId() must not be null", comment.getId());

    String[] commentsId = activity.getReplyToId().split(",");
    assertEquals(comment.getId(), commentsId[1]);
    tearDownActivityList.add(activity);
  }

  public  void testGetComments(){
    Activity activity = new Activity();
    activity.setTitle("blah blah");
    activityManager.saveActivity(rootIdentity, activity);

    List<Activity> comments = new ArrayList<Activity>();
    for (int i = 0; i < 10; i++) {
      Activity comment = new Activity();
      comment.setTitle("comment blah blah");
      comment.setUserId(rootIdentity.getId());
      activityManager.saveComment(activity, comment);
      assertNotNull("comment.getId() must not be null", comment.getId());

      comments.add(comment);
    }

    Activity assertActivity = activityManager.getActivity(activity.getId());
    String rawCommentIds = assertActivity.getReplyToId();
    String[] commentIds = rawCommentIds.split(",");
    for (int i = 1; i < commentIds.length; i++) {
      assertEquals(comments.get(i - 1).getId(), commentIds[i]);
    }
    tearDownActivityList.add(activity);
  }
  /**
   * Unit Test for:
   * <p>
   * {@link ActivityManager#deleteComment(String, String)}
   */
  public void testDeleteComment() {
    final String title = "Activity Title";
    {
      //FIXBUG: SOC-1194
      //Case: a user create an activity in his stream, then give some comments on it.
      //Delete comments and check
      Activity activity1 = new Activity();
      activity1.setUserId(demoIdentity.getId());
      activity1.setTitle(title);
      activityManager.saveActivity(demoIdentity, activity1);

      final int numberOfComments = 10;
      final String commentTitle = "Activity Comment";
      for (int i = 0; i < numberOfComments; i++) {
        Activity comment = new Activity();
        comment.setUserId(demoIdentity.getId());
        comment.setTitle(commentTitle + i);
        activityManager.saveComment(activity1, comment);
      }

      List<Activity> storedCommentList = activityManager.getComments(activity1);

      assertEquals("storedCommentList.size() must return: " + numberOfComments, numberOfComments, storedCommentList.size());

      //delete random 2 comments
      int index1 = new Random().nextInt(numberOfComments - 1);
      int index2 = index1;
      while (index2 == index1) {
        index2 = new Random().nextInt(numberOfComments - 1);
      }

      Activity tobeDeletedComment1 = storedCommentList.get(index1);
      Activity tobeDeletedComment2 = storedCommentList.get(index2);

      activityManager.deleteComment(activity1.getId(), tobeDeletedComment1.getId());
      activityManager.deleteComment(activity1.getId(), tobeDeletedComment2.getId());

      List<Activity> afterDeletedCommentList = activityManager.getComments(activity1);

      assertEquals("afterDeletedCommentList.size() must return: " + (numberOfComments - 2), numberOfComments - 2, afterDeletedCommentList.size());


      tearDownActivityList.add(activity1);

    }
  }

 /**
  * Unit Test for:
  * {@link ActivityManager#getActivities(Identity)}
  * {@link ActivityManager#getActivities(Identity, long, long)}
  */
 public void testGetActivities() {
   List<Activity> rootActivityList = activityManager.getActivities(rootIdentity);
   assertNotNull("rootActivityList must not be null", rootActivityList);
   assertEquals(0, rootActivityList.size());
   populateActivityMass(rootIdentity, 30);
   List<Activity> activities = activityManager.getActivities(rootIdentity);
   assertNotNull("activities must not be null", activities);
   assertEquals(20, activities.size());

   List<Activity> allActivities = activityManager.getActivities(rootIdentity, 0, 30);

   assertEquals(30, allActivities.size());

   tearDownActivityList.addAll(allActivities);
 }

 /**
  * Unit Test for:
  * <p>
  * {@link ActivityManager#getActivitiesOfConnections(Identity)}
  * {@link ActivityManager#getActivitiesOfConnections(org.exoplatform.social.core.identity.model.Identity, int, int)}
  */
 public void testGetActivitiesOfConnections() throws Exception {
   List<Activity> johnConnectionsActivityList = activityManager.getActivitiesOfConnections(johnIdentity);
   assertNotNull("johnConnectionsActivityList must not be null", johnConnectionsActivityList);
   assertEquals(0, johnConnectionsActivityList.size());

   Relationship johnDemoRelationship = relationshipManager.invite(johnIdentity, demoIdentity);

   relationshipManager.confirm(johnDemoRelationship);

   johnConnectionsActivityList = activityManager.getActivitiesOfConnections(johnIdentity);

   assertEquals("johnConnectionsActivityList.size() must return 0", 0, johnConnectionsActivityList.size());

   populateActivityMass(demoIdentity, 45);

   johnConnectionsActivityList = activityManager.getActivitiesOfConnections(johnIdentity);

   assertEquals("johnConnectionsActivityList.size() must return 30", 30, johnConnectionsActivityList.size());

   johnConnectionsActivityList = activityManager.getActivitiesOfConnections(johnIdentity, 0, 50);

   assertEquals(45, johnConnectionsActivityList.size());

   johnConnectionsActivityList = activityManager.getActivitiesOfConnections(johnIdentity, 20, 50);

   assertEquals(25, johnConnectionsActivityList.size());

   //Now demo create one activity in a space, make sure it is not listed in john's activities connections

   {
     Identity spaceIdentity = new Identity(SpaceIdentityProvider.NAME, "spaceTest");
     identityManager.saveIdentity(spaceIdentity);
     Activity demoActivityOnSpace = new Activity();
     demoActivityOnSpace.setUserId(demoIdentity.getId());
     demoActivityOnSpace.setTitle("Blah blah");
     activityManager.saveActivity(spaceIdentity, demoActivityOnSpace);
     tearDownActivityList.add(demoActivityOnSpace);
     List<Activity> activityListOfJohnConnections = activityManager.getActivitiesOfConnections(johnIdentity, 0, 100);
     identityManager.deleteIdentity(spaceIdentity);
     assertEquals("activityListOfJohnConnections.size() must be 45", 45, activityListOfJohnConnections.size());
   }


   relationshipManager.remove(johnDemoRelationship);
   tearDownActivityList.addAll(activityManager.getActivities(demoIdentity, 0, 50));

 }

 /**
  * Unit Test for:
  * <p>
  * {@link ActivityManager#getActivitiesOfUserSpaces(Identity)}
  */
 public void testGetActivitiesOfUserSpaces() {
   List<Activity> demoSpacesActivityList = activityManager.getActivitiesOfUserSpaces(demoIdentity);
   assertNotNull("demoSpacesActivityList must not be null", demoSpacesActivityList);
   assertEquals(0, demoSpacesActivityList.size());
 }

  /**
   *
   */
  public void testGetActivitiesByPagingWithoutCreatingComments() {
    final int totalActivityCount = 9;
    final int retrievedCount = 7;

    for (int i = 0; i < totalActivityCount; i++) {
      //root save on john's stream
      Activity activity = new Activity();
      activity.setTitle("blabla");
      activity.setUserId(rootIdentity.getId());

      activityManager.saveActivity(johnIdentity, activity);
    }

    List<Activity> activities = activityManager.getActivities(johnIdentity, 0, retrievedCount);
    assertEquals(retrievedCount, activities.size());

    tearDownActivityList.addAll(activityManager.getActivities(johnIdentity, 0, totalActivityCount));
  }

  /**
   * Test {@link ActivityManager#saveLike(Activity, Identity)}
   * 
   * @throws Exception
   * @since 1.1.9
   */
  public void testSaveLike() throws Exception {
    String encodeTitle = "espace testé à la plage";
    Activity likeActivity = new Activity();
    likeActivity.setTitle(encodeTitle);
    likeActivity.setBody(encodeTitle);
    
    activityManager.saveActivity(johnIdentity, likeActivity);
    
    Activity savedActivity = activityManager.getActivity(likeActivity.getId());
    
    String expectedTitle = savedActivity.getTitle();
    
    //john like this activity
    activityManager.saveLike(savedActivity, johnIdentity);
    Activity savedLikeActivity = activityManager.getActivity(likeActivity.getId());
    assertEquals(expectedTitle, savedLikeActivity.getTitle());
    assertEquals(expectedTitle, savedLikeActivity.getBody());
    
    tearDownActivityList.add(likeActivity);
    
    encodeTitle = "\"#'^`~@!&*()|\\[]{},./?$%%$^";
    Activity activity = new Activity();
    activity.setTitle(encodeTitle);
    activity.setBody(encodeTitle);
    
    activityManager.saveActivity(johnIdentity, activity);
    
    savedActivity = activityManager.getActivity(activity.getId());
    
    expectedTitle = savedActivity.getTitle();
    
    //john like this activity
    activityManager.saveLike(savedActivity, johnIdentity);
    savedActivity = activityManager.getActivity(activity.getId());
    assertEquals(expectedTitle, savedActivity.getTitle());
    assertEquals(expectedTitle, savedActivity.getBody());
    
    tearDownActivityList.add(activity);
  }
  
  /**
   * Test {@link ActivityManager#removeLike(Activity, Identity)}
   * 
   * @throws Exception
   * @since 1.1.9
   */
  public void testRemoveLike() throws Exception {
    String encodeTitle = "espace testé à la plage";
    Activity likeActivity = new Activity();
    likeActivity.setTitle(encodeTitle);
    likeActivity.setBody(encodeTitle);
    
    activityManager.saveActivity(johnIdentity, likeActivity);
    
    Activity savedActivity = activityManager.getActivity(likeActivity.getId());
    
    String expectedTitle = savedActivity.getTitle();
    
    //john like this activity
    activityManager.saveLike(savedActivity, johnIdentity);
    Activity savedLikeActivity = activityManager.getActivity(likeActivity.getId());
    assertEquals(expectedTitle, savedLikeActivity.getTitle());
    assertEquals(expectedTitle, savedLikeActivity.getBody());
    
    //john dislike this activity
    activityManager.removeLike(savedLikeActivity, johnIdentity);
    Activity removedLikeActivity = activityManager.getActivity(likeActivity.getId());
    assertEquals(expectedTitle, removedLikeActivity.getTitle());
    assertEquals(expectedTitle, removedLikeActivity.getBody());
    
    tearDownActivityList.add(likeActivity);
    
    encodeTitle = "\"#'^`~@!&*()|\\[]{},./?$%%$^";
    Activity activity = new Activity();
    activity.setTitle(encodeTitle);
    activity.setBody(encodeTitle);
    
    activityManager.saveActivity(johnIdentity, activity);
    
    savedActivity = activityManager.getActivity(activity.getId());
    
    expectedTitle = savedActivity.getTitle();
    
    //john like this activity
    activityManager.saveLike(savedActivity, johnIdentity);
    savedLikeActivity = activityManager.getActivity(activity.getId());
    assertEquals(expectedTitle, savedLikeActivity.getTitle());
    assertEquals(expectedTitle, savedLikeActivity.getBody());
    
    //john dislike this activity
    activityManager.removeLike(savedLikeActivity, johnIdentity);
    removedLikeActivity = activityManager.getActivity(activity.getId());
    assertEquals(expectedTitle, removedLikeActivity.getTitle());
    assertEquals(expectedTitle, removedLikeActivity.getBody());
    
    tearDownActivityList.add(activity);
  }
  
  /**
   * Test {@link ActivityManager#saveComment(Activity, Activity)}
   * 
   * @throws Exception
   * @since 1.1.9
   */
  public void testSaveComment() throws Exception {
    String encodeTitle = "espace testé à la plage";
    Activity commentActivity = new Activity();
    commentActivity.setTitle(encodeTitle);
    commentActivity.setBody(encodeTitle);
    
    activityManager.saveActivity(johnIdentity, commentActivity);
    
    Activity savedActivity = activityManager.getActivity(commentActivity.getId());
    
    String expectedTitle = savedActivity.getTitle();
    
    //john comment on this activity
    Activity comment = new Activity();
    comment.setTitle("comment blah");
    comment.setUserId(johnIdentity.getId());
    activityManager.saveComment(commentActivity, comment);
    Activity savedCommentActivity = activityManager.getActivity(commentActivity.getId());
    assertEquals(expectedTitle, savedCommentActivity.getTitle());
    assertEquals(expectedTitle, savedCommentActivity.getBody());
    
    tearDownActivityList.add(commentActivity);
    
    encodeTitle = "\"#'^`~@!&*()|\\[]{},./?$%%$^";
    Activity activity = new Activity();
    activity.setTitle(encodeTitle);
    activity.setBody(encodeTitle);
    
    activityManager.saveActivity(johnIdentity, activity);
    
    savedActivity = activityManager.getActivity(activity.getId());
    
    expectedTitle = savedActivity.getTitle();
    
    //john comment on this activity
    Activity comment2 = new Activity();
    comment2.setTitle("comment blah");
    comment2.setUserId(johnIdentity.getId());
    activityManager.saveComment(activity, comment2);
    savedCommentActivity = activityManager.getActivity(activity.getId());
    assertEquals(expectedTitle, savedCommentActivity.getTitle());
    assertEquals(expectedTitle, savedCommentActivity.getBody());
    
    tearDownActivityList.add(activity);
  }
  
  /**
   *
   */
  public void testAddProviders() {
    activityManager.addProcessor(new FakeProcessor(10));
    activityManager.addProcessor(new FakeProcessor(9));
    activityManager.addProcessor(new FakeProcessor(8));

    Activity activity = new Activity();
    activity.setTitle("Hello");
    activityManager.processActivitiy(activity);
    //just verify that we run in priority order
    assertEquals("Hello-8-9-10", activity.getTitle());
  }


  class FakeProcessor extends BaseActivityProcessorPlugin {
    public FakeProcessor(int priority) {
      super(null);
      super.priority = priority;
    }

    @Override
    public void processActivity(Activity activity) {
      activity.setTitle(activity.getTitle() + "-" + priority);
    }
  }

  private void populateActivityMass(Identity user, int number) {
    for (int i = 0; i < number; i++) {
      Activity activity = new Activity();
      activity.setTitle("title " + i);
      activity.setUserId(user.getId());
      try {
        activityManager.saveActivity(user, activity);
      } catch (Exception e) {
        LOG.error("can not save activity.", e);
      }
    }
  }
}