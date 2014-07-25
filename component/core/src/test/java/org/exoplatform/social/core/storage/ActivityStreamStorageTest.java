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
package org.exoplatform.social.core.storage;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.application.RelationshipPublisher;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.manager.RelationshipManagerImpl;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.ActivityStreamStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;


public class ActivityStreamStorageTest extends AbstractCoreTest {
  private final Log LOG = ExoLogger.getLogger(ActivityStreamStorageTest.class);
  private IdentityStorage identityStorage;
  private ActivityStorage activityStorage;
  private ActivityStreamStorage streamStorage;
  private RelationshipManagerImpl relationshipManager;
  private List<ExoSocialActivity> tearDownActivityList;
  private List<Space> tearDownSpaceList;

  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;
 
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    activityStorage = (ActivityStorage) getContainer().getComponentInstanceOfType(ActivityStorage.class);
    streamStorage = (ActivityStreamStorage) getContainer().getComponentInstanceOfType(ActivityStreamStorage.class);
    relationshipManager = (RelationshipManagerImpl) getContainer().getComponentInstanceOfType(RelationshipManager.class);
    
    //
    assertNotNull("identityManager must not be null", identityStorage);
    assertNotNull("activityStorage must not be null", activityStorage);
    rootIdentity = new Identity(OrganizationIdentityProvider.NAME, "root");
    johnIdentity = new Identity(OrganizationIdentityProvider.NAME, "john");
    maryIdentity = new Identity(OrganizationIdentityProvider.NAME, "mary");
    demoIdentity = new Identity(OrganizationIdentityProvider.NAME, "demo");
    
    identityStorage.saveIdentity(rootIdentity);
    identityStorage.saveIdentity(johnIdentity);
    identityStorage.saveIdentity(maryIdentity);
    identityStorage.saveIdentity(demoIdentity);

    assertNotNull("rootIdentity.getId() must not be null", rootIdentity.getId());
    assertNotNull("johnIdentity.getId() must not be null", johnIdentity.getId());
    assertNotNull("maryIdentity.getId() must not be null", maryIdentity.getId());
    assertNotNull("demoIdentity.getId() must not be null", demoIdentity.getId());

    tearDownActivityList = new ArrayList<ExoSocialActivity>();
    tearDownSpaceList = new ArrayList<Space>();
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
    for (Space space : tearDownSpaceList) {
      Identity spaceIdentity = identityStorage.findIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
      if (spaceIdentity != null) {
        identityStorage.deleteIdentity(spaceIdentity);
      }
      spaceService.deleteSpace(space);
    }
    super.tearDown();
  }

  public void testSaveActivity() throws ActivityStorageException {
    final String activityTitle = "activity Title";

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activityStorage.saveActivity(rootIdentity, activity);

    assertNotNull("activity.getId() must not be null", activity.getId());

    tearDownActivityList.addAll(activityStorage.getUserActivities(rootIdentity, 0, 1));
    
    assertEquals(1, streamStorage.getNumberOfFeed(rootIdentity));

  }
  
  public void testLike() throws ActivityStorageException {
    final String activityTitle = "activity Title";

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activityStorage.saveActivity(rootIdentity, activity);

    assertNotNull("activity.getId() must not be null", activity.getId());

    tearDownActivityList.addAll(activityStorage.getUserActivities(rootIdentity, 0, 1));
    
    assertEquals(1, streamStorage.getNumberOfFeed(rootIdentity));
    
    //like
    activity.setLikeIdentityIds(new String[] {maryIdentity.getId()});
    
    activityStorage.updateActivity(activity);
    
    assertEquals(1, streamStorage.getNumberOfFeed(maryIdentity));
    assertEquals(1, streamStorage.getNumberOfMyActivities(maryIdentity));

  }
  /*
  public void testUnlike() throws ActivityStorageException {
    final String activityTitle = "activity Title";

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activityStorage.saveActivity(rootIdentity, activity);

    assertNotNull("activity.getId() must not be null", activity.getId());

    tearDownActivityList.addAll(activityStorage.getUserActivities(rootIdentity, 0, 1));
    
    assertEquals(1, streamStorage.getNumberOfFeed(rootIdentity));
    
    //like
    activity.setLikeIdentityIds(new String[] {maryIdentity.getId()});
    activityStorage.updateActivity(activity);
    assertEquals(1, streamStorage.getNumberOfFeed(maryIdentity));
    assertEquals(1, streamStorage.getNumberOfMyActivities(maryIdentity));
    
    //unlike
    activity.setLikeIdentityIds(new String[] {});
    activityStorage.updateActivity(activity);
    
    assertEquals(0, streamStorage.getNumberOfMyActivities(maryIdentity));
    assertEquals(0, streamStorage.getNumberOfFeed(maryIdentity));
  } */
  
  public void testSaveMentionActivity() throws ActivityStorageException {
    final String activityTitle = "activity Title";

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle + " @demo ");
    activityStorage.saveActivity(rootIdentity, activity);

    assertNotNull("activity.getId() must not be null", activity.getId());

    tearDownActivityList.addAll(activityStorage.getUserActivities(rootIdentity, 0, 1));
    
    assertEquals(1, streamStorage.getNumberOfFeed(rootIdentity));
    
    assertEquals(1, streamStorage.getNumberOfFeed(demoIdentity));
    assertEquals(1, streamStorage.getNumberOfMyActivities(demoIdentity));
    
    {
      activity = new ExoSocialActivityImpl();
      activity.setTitle(activityTitle + " @mary ");
      activityStorage.saveActivity(maryIdentity, activity);
      
      assertEquals(1, streamStorage.getNumberOfFeed(maryIdentity));
      assertEquals(1, streamStorage.getNumberOfMyActivities(maryIdentity));
    }
    
    {
      activity = new ExoSocialActivityImpl();
      activity.setTitle(activityTitle + " @mary @john");
      activityStorage.saveActivity(maryIdentity, activity);
      
      assertEquals(2, streamStorage.getNumberOfFeed(maryIdentity));
      assertEquals(2, streamStorage.getNumberOfMyActivities(maryIdentity));
      
      assertEquals(1, streamStorage.getNumberOfFeed(johnIdentity));
      assertEquals(1, streamStorage.getNumberOfMyActivities(johnIdentity));
    }
    
    tearDownActivityList.addAll(activityStorage.getUserActivities(maryIdentity, 0, 10));

  }
  
  public void testSaveCommentActivity() throws ActivityStorageException {
    final String activityTitle = "activity Title";

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle + " @demo ");
    activityStorage.saveActivity(rootIdentity, activity);

    assertNotNull("activity.getId() must not be null", activity.getId());

    tearDownActivityList.addAll(activityStorage.getUserActivities(rootIdentity, 0, 1));
    
    assertEquals(1, streamStorage.getNumberOfFeed(rootIdentity));
    
    assertEquals(1, streamStorage.getNumberOfFeed(demoIdentity));
    assertEquals(1, streamStorage.getNumberOfMyActivities(demoIdentity));
    
    {
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle(activityTitle);
      comment.isComment(true);
      comment.setUserId(maryIdentity.getId());
      activityStorage.saveComment(activity, comment);
      
      assertEquals(1, streamStorage.getNumberOfFeed(maryIdentity));
      assertEquals(1, streamStorage.getNumberOfMyActivities(maryIdentity));
    }
    
    {
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle(activityTitle  + " @mary @john");
      comment.isComment(true);
      comment.setUserId(maryIdentity.getId());
      activityStorage.saveComment(activity, comment);
      
      assertEquals(1, streamStorage.getNumberOfFeed(maryIdentity));
      assertEquals(1, streamStorage.getNumberOfMyActivities(maryIdentity));
      
    }
    
  }
  
  public void testDeleteActivity() throws ActivityStorageException {
    final String activityTitle = "activity Title";

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity = activityStorage.saveActivity(rootIdentity, activity);

    assertNotNull("activity.getId() must not be null", activity.getId());

    assertEquals(1, streamStorage.getNumberOfFeed(rootIdentity));
    
    //
    activityStorage.deleteActivity(activity.getId());
    assertEquals(0, streamStorage.getNumberOfFeed(rootIdentity));

  }

  public void testGetActivity() throws ActivityStorageException {
    final String activityTitle = "activity title";
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);
    
    List<ExoSocialActivity> got = streamStorage.getFeed(demoIdentity, 0, 1);
    assertEquals(1, got.size());

  }
  
  public void testUpdateActivity() throws ActivityStorageException {
    final String activityTitle = "activity Title";

    ExoSocialActivity activity1 = new ExoSocialActivityImpl();
    activity1.setTitle(activityTitle + " 1");
    activityStorage.saveActivity(rootIdentity, activity1);
    
    ExoSocialActivity activity2 = new ExoSocialActivityImpl();
    activity2.setTitle(activityTitle + " 2");
    activityStorage.saveActivity(rootIdentity, activity2);
    
    { //checks what's hot
      List<ExoSocialActivity> list = streamStorage.getFeed(rootIdentity, 0, 10);
      assertEquals(2, list.size());
      
      ExoSocialActivity ac2 = list.get(0);
      assertEquals(activity2.getTitle(), ac2.getTitle());
      
      ExoSocialActivity ac1 = list.get(1);
      assertEquals(activity1.getTitle(), ac1.getTitle());
    }
    
    //update
    ExoSocialActivity comment = new ExoSocialActivityImpl();
    comment.setTitle(activityTitle  + " @mary @john");
    comment.isComment(true);
    comment.setUserId(maryIdentity.getId());
    activityStorage.saveComment(activity1, comment);
    
    LOG.info("<======================updated whatshot=======================>");
    
    { //checks what's hot
      List<ExoSocialActivity> list = streamStorage.getFeed(rootIdentity, 0, 10);
      assertEquals(2, list.size());
      
      ExoSocialActivity ac1 = list.get(0);
      assertEquals(activity1.getTitle(), ac1.getTitle());
      
      ExoSocialActivity ac2 = list.get(1);
      assertEquals(activity2.getTitle(), ac2.getTitle()); 
    }
    
    comment = new ExoSocialActivityImpl();
    comment.setTitle(activityTitle  + " more one");
    comment.isComment(true);
    comment.setUserId(maryIdentity.getId());
    activityStorage.saveComment(activity1, comment);
    
    LOG.info("<======================updated whatshot 1=======================>");
    
    { //checks what's hot
      List<ExoSocialActivity> list = streamStorage.getFeed(rootIdentity, 0, 10);
      assertEquals(2, list.size());
    }
    
    LOG.info("<======================add teardown=======================>");
    tearDownActivityList.addAll(activityStorage.getUserActivities(rootIdentity, 0, 10));
  }
  
  public void testConnectionsActivity() throws ActivityStorageException {
    
    Relationship demoMaryConnection = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
    relationshipManager.confirm(demoMaryConnection);
    
    final String activityTitle = "activity title";
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);
    
    List<ExoSocialActivity> got = streamStorage.getFeed(demoIdentity, 0, 1);
    assertEquals(1, got.size());
    
    got = streamStorage.getConnections(demoIdentity, 0, 1);
    assertEquals(0, got.size());
    
    got = streamStorage.getFeed(maryIdentity, 0, 1);
    assertEquals(1, got.size());
    
    relationshipManager.delete(demoMaryConnection);
    
    //
    got = streamStorage.getFeed(maryIdentity, 0, 1);
    assertEquals(0, got.size());
    
    got = streamStorage.getConnections(maryIdentity, 0, 1);
    assertEquals(0, got.size());
    
    got = streamStorage.getFeed(demoIdentity, 0, 1);
    assertEquals(1, got.size());
    
    got = streamStorage.getConnections(demoIdentity, 0, 1);
    assertEquals(0, got.size());
  }
  
public void testConnectionsExistActivities() throws ActivityStorageException {
    
    RelationshipPublisher relationshipPublisher = (RelationshipPublisher) getContainer().getComponentInstanceOfType(RelationshipPublisher.class);
    relationshipManager.addListenerPlugin(relationshipPublisher);
    
    
    final String activityTitle = "activity title";
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);
    
    Relationship demoMaryConnection = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
    relationshipManager.confirm(demoMaryConnection);
    
    LOG.info("<======================demo: demo-mary=======================>");
    List<ExoSocialActivity> got = streamStorage.getFeed(demoIdentity, 0, 10);
    assertEquals(3, got.size());
    
    LOG.info("<======================mary: demo-mary=======================>");
    got = streamStorage.getFeed(maryIdentity, 0, 10);
    assertEquals(3, got.size());
    
    LOG.info("<======================john: demo-mary=======================>");
    got = streamStorage.getFeed(johnIdentity, 0, 10);
    assertEquals(0, got.size());
    
    LOG.info("<======================Relationship=>demo-john=======================>");
    //
    Relationship demoJohnConnection = relationshipManager.inviteToConnect(demoIdentity, johnIdentity);
    relationshipManager.confirm(demoJohnConnection);
    LOG.info("<======================john: demo-mary; demo-john=======================>");
    got = streamStorage.getFeed(johnIdentity, 0, 5);
    assertEquals(3, got.size());
    
    LOG.info("<======================demo: demo-mary ;demo-john=======================>");
    got = streamStorage.getFeed(demoIdentity, 0, 5);
    assertEquals(4, got.size());
    
    LOG.info("<======================mary: demo-mary; demo-john=======================>");
    got = streamStorage.getFeed(maryIdentity, 0, 5);
    assertEquals(3, got.size());
    //
    
    LOG.info("<======================mary-john=======================>");
    Relationship maryJohnConnection = relationshipManager.inviteToConnect(maryIdentity, johnIdentity);
    relationshipManager.confirm(maryJohnConnection);
    LOG.info("<======================mary: demo-mary; demo-john; mary-john=======================>");
    got = streamStorage.getFeed(maryIdentity, 0, 10);
    assertEquals(4, got.size());
    
    relationshipManager.delete(demoMaryConnection);
    relationshipManager.delete(demoJohnConnection);
    relationshipManager.delete(maryJohnConnection);
    
    relationshipManager.unregisterListener(relationshipPublisher);
  }
  
  public void testConnectionsExistActivitiesCounter() throws ActivityStorageException {
    
    RelationshipPublisher relationshipPublisher = (RelationshipPublisher) getContainer().getComponentInstanceOfType(RelationshipPublisher.class);
    relationshipManager.addListenerPlugin(relationshipPublisher);
    
    final String activityTitle = "activity title";
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);
    
    Relationship demoMaryConnection = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
    relationshipManager.confirm(demoMaryConnection);
    
    assertEquals(3, streamStorage.getNumberOfFeed(demoIdentity));
    assertEquals(3, streamStorage.getNumberOfFeed(maryIdentity));
    
    relationshipManager.delete(demoMaryConnection);

    relationshipManager.unregisterListener(relationshipPublisher);
  }
  
 public void testSpaceActivities() throws Exception {
    
   SpaceService spaceService = this.getSpaceService();
   Space space = this.getSpaceInstance(spaceService, 0);
   Identity spaceIdentity = this.getIdentityManager().getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), false);
   
   int totalNumber = 1;
   //demo posts activities to space
   for (int i = 0; i < totalNumber; i ++) {
     ExoSocialActivity activity = new ExoSocialActivityImpl();
     activity.setTitle("activity title " + i);
     activity.setUserId(demoIdentity.getId());
     activityStorage.saveActivity(spaceIdentity, activity);
     tearDownActivityList.add(activity);
   }
   
   assertEquals(1, streamStorage.getNumberOfMySpaces(demoIdentity));
   
    //
    {
      spaceService.addMember(space, maryIdentity.getRemoteId());
      assertEquals(2, streamStorage.getMySpaces(maryIdentity, 0, -1).size());
    }

    {
      spaceService.removeMember(space, maryIdentity.getRemoteId());
      assertEquals(0, streamStorage.getMySpaces(maryIdentity, 0, -1).size());
    }
   
    String activityId = identityStorage.getProfileActivityId(spaceIdentity.getProfile(), Profile.AttachedActivityType.SPACE);
    activityStorage.deleteActivity(activityId);
    tearDownSpaceList.add(space);
  }
 
 public void testGetActivityByPoster() throws ActivityStorageException {
   
   Relationship demoMaryConnection = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
   relationshipManager.confirm(demoMaryConnection);
   
   final String activityTitle = "activity title";
   ExoSocialActivity activity = new ExoSocialActivityImpl();
   activity.setTitle(activityTitle);
   activityStorage.saveActivity(demoIdentity, activity);
   tearDownActivityList.add(activity);
   
   List<ExoSocialActivity> got = activityStorage.getActivitiesByPoster(demoIdentity, 0, 10);
   assertEquals(1, got.size());
   
   activity = new ExoSocialActivityImpl();
   activity.setTitle(activityTitle);
   activity.setUserId(maryIdentity.getId());
   activityStorage.saveActivity(demoIdentity, activity);
   tearDownActivityList.add(activity);
   
   got = activityStorage.getActivitiesByPoster(maryIdentity, 0, 10);
   assertEquals(1, got.size());
 }
 
  /**
   * Gets the space service.
   * 
   * @return the space service
   */
  private SpaceService getSpaceService() {
    return (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
  }
  
  /**
   * Gets the identity manager.
   * 
   * @return the identity manager
   */
  private IdentityManager getIdentityManager() {
    return (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
  }
  
  /**
   * Gets an instance of the space.
   * 
   * @param spaceService
   * @param number
   * @return
   * @throws Exception
   * @since 1.2.0-GA
   */
  private Space getSpaceInstance(SpaceService spaceService, int number) throws Exception {
    Space space = new Space();
    space.setDisplayName("my space " + number);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + number);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PUBLIC);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    space.setGroupId("/spaces/my_space_" + number);
    space.setUrl(space.getPrettyName());
    String[] managers = new String[] {"demo"};
    String[] members = new String[] {"demo"};
    String[] invitedUsers = new String[] {"mary"};
    String[] pendingUsers = new String[] {"john",};
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);
    spaceService.saveSpace(space, true);
    return space;
  }
 
}
