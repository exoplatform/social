package org.exoplatform.social.core.storage;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;
import org.exoplatform.social.core.test.MaxQueryNumber;

public class ActivityStorageNewTest extends AbstractCoreTest {

  private IdentityStorage         identityStorage;

  private ActivityStorage         activityStorage;

  private IdentityManager         identityManager;

  private RelationshipManager     relationshipManager;

  private List<ExoSocialActivity> tearDownActivityList;

  private Identity                rootIdentity;

  private Identity                johnIdentity;

  private Identity                maryIdentity;

  private Identity                demoIdentity;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    activityStorage = (ActivityStorage) getContainer().getComponentInstanceOfType(ActivityStorage.class);
    identityManager = (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    relationshipManager = (RelationshipManager) getContainer().getComponentInstanceOfType(RelationshipManager.class);
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

  /**
   * Checks clean data.
   * 
   * @since 4.0.0
   */
  private void checkCleanData() {
    assertEquals("assertEquals(activityStorage.getActivities(rootIdentity).size() must be 0",0,
                 activityStorage.getUserActivities(rootIdentity,0,
                                                   activityStorage.getNumberOfUserActivities(rootIdentity)).size());
    assertEquals("assertEquals(activityStorage.getActivities(johnIdentity).size() must be 0",0,
                 activityStorage.getUserActivities(johnIdentity,0,
                                                   activityStorage.getNumberOfUserActivities(johnIdentity)).size());
    assertEquals("assertEquals(activityStorage.getActivities(maryIdentity).size() must be 0",0,
                 activityStorage.getUserActivities(maryIdentity,0,
                                                   activityStorage.getNumberOfUserActivities(maryIdentity)).size());
    assertEquals("assertEquals(activityStorage.getActivities(demoIdentity).size() must be 0",0,
                 activityStorage.getUserActivities(demoIdentity,0,
                                                   activityStorage.getNumberOfUserActivities(demoIdentity)).size());
  }

  /**
   * Creates activities.
   * 
   * @param number
   * @param ownerStream
   * @since 4.0.0
   */
  private void createActivities(int number, Identity ownerStream) {
    for (int i = 0; i < number; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activityStorage.saveActivity(ownerStream, activity);
      tearDownActivityList.add(activity);
    }
  }

  /**
   * Gets the relationship manager.
   * 
   * @return
   * @since 4.0.0
   */
  private RelationshipManager getRelationshipManager() {
    return (RelationshipManager) getContainer().getComponentInstanceOfType(RelationshipManager.class);
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
   * Gets an instance of the space.
   * 
   * @param spaceService
   * @param number
   * @return
   * @throws Exception
   * @since 4.0.0
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
    space.setGroupId("/space/space" + number);
    space.setUrl(space.getPrettyName());
    String[] managers = new String[] { "demo", "tom" };
    String[] members = new String[] { "raul", "ghost", "dragon" };
    String[] invitedUsers = new String[] { "register1", "mary" };
    String[] pendingUsers = new String[] { "jame", "paul", "hacker" };
    space.setInvitedUsers(invitedUsers);
    space.setPendingUsers(pendingUsers);
    space.setManagers(managers);
    space.setMembers(members);
    spaceService.saveSpace(space, true);
    return space;
  }

  /**
   * Tests {@link ActivityStorage#getNewerOnUserActivities(Identity, Long, int)}
   * .
   */
  @MaxQueryNumber(100)
  public void testGetNewerOnUserActivities() {
    checkCleanData();
    createActivities(2, demoIdentity);
    Long sinceTime = activityStorage.getUserActivities(demoIdentity, 0, 10).get(0).getPostedTime();
    assertEquals(0, activityStorage.getNewerOnUserActivities(demoIdentity, sinceTime, 10).size());
    createActivities(2, maryIdentity);
    assertEquals(0, activityStorage.getNewerOnUserActivities(demoIdentity, sinceTime, 10).size());
    createActivities(2, demoIdentity);
    assertEquals(2, activityStorage.getNewerOnUserActivities(demoIdentity, sinceTime, 10).size());

    // Delete the activity at this sinceTime will don't change the result
    // We just add 2 more activities of demoIdentity so the position of the
    // activity that we get the sinceTime has
    // changed from 0 to 2
    String id = activityStorage.getUserActivities(demoIdentity, 0, 10).get(2).getId();
    for (ExoSocialActivity activity : tearDownActivityList) {
      if (id == activity.getId()) {
        tearDownActivityList.remove(activity);
        break;
      }
    }
    activityStorage.deleteActivity(id);
    assertEquals(2, activityStorage.getNewerOnUserActivities(demoIdentity, sinceTime, 10).size());
  }

  /**
   * Tests {@link ActivityStorage#getOlderOnUserActivities(Identity, Long, int)}
   * .
   */
  @MaxQueryNumber(100)
  public void testGetOlderOnUserActivities() {
    checkCleanData();
    createActivities(2, demoIdentity);
    Long maxTime = activityStorage.getUserActivities(demoIdentity, 0, 10).get(0).getPostedTime();
    assertEquals(1, activityStorage.getOlderOnUserActivities(demoIdentity, maxTime, 10).size());
    createActivities(2, maryIdentity);
    assertEquals(1, activityStorage.getOlderOnUserActivities(demoIdentity, maxTime, 10).size());
    createActivities(2, demoIdentity);
    assertEquals(1, activityStorage.getOlderOnUserActivities(demoIdentity, maxTime, 10).size());
    maxTime = activityStorage.getUserActivities(demoIdentity, 0, 10).get(0).getPostedTime();
    assertEquals(3, activityStorage.getOlderOnUserActivities(demoIdentity, maxTime, 10).size());

    // Delete the activity at this maxTime will don't change the result
    String id = activityStorage.getUserActivities(demoIdentity, 0, 10).get(0).getId();
    for (ExoSocialActivity activity : tearDownActivityList) {
      if (id == activity.getId()) {
        tearDownActivityList.remove(activity);
        break;
      }
    }
    activityStorage.deleteActivity(id);
    assertEquals(3, activityStorage.getOlderOnUserActivities(demoIdentity, maxTime, 10).size());
  }

  /**
   * Tests {@link ActivityStorage#getNewerOnActivityFeed(Identity, Long, int)}.
   */
  @MaxQueryNumber(100)
  public void testGetNewerOnActivityFeed() {
    createActivities(3, demoIdentity);
    Long sinceTime = activityStorage.getActivityFeed(demoIdentity, 0, 10).get(0).getPostedTime();
    assertEquals(0, activityStorage.getNewerOnActivityFeed(demoIdentity, sinceTime, 10).size());
    createActivities(1, demoIdentity);
    assertEquals(1, activityStorage.getNewerOnActivityFeed(demoIdentity, sinceTime, 10).size());
    createActivities(2, maryIdentity);
    relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
    assertEquals(1, activityStorage.getNewerOnActivityFeed(demoIdentity, sinceTime, 10).size());
    relationshipManager.confirm(demoIdentity, maryIdentity);
    createActivities(2, maryIdentity);
    assertEquals(5, activityStorage.getNewerOnActivityFeed(demoIdentity, sinceTime, 10).size());

    // Delete the activity at this sinceTime will don't change the result
    String id = activityStorage.getUserActivities(demoIdentity, 0, 10).get(1).getId();
    for (ExoSocialActivity activity : tearDownActivityList) {
      if (id == activity.getId()) {
        tearDownActivityList.remove(activity);
        break;
      }
    }
    activityStorage.deleteActivity(id);
    assertEquals(5, activityStorage.getNewerOnActivityFeed(demoIdentity, sinceTime, 10).size());
  }

  /**
   * Tests {@link ActivityStorage#getOlderOnActivityFeed(Identity, Long, int)}.
   */
  @MaxQueryNumber(100)
  public void testGetOlderOnActivityFeed() {
    createActivities(3, demoIdentity);
    Long maxTime = activityStorage.getActivityFeed(demoIdentity, 0, 10).get(2).getPostedTime();
    assertEquals(0, activityStorage.getOlderOnActivityFeed(demoIdentity, maxTime, 10).size());

    // Delete the activity at this maxTime will don't change the result
    String id = activityStorage.getUserActivities(demoIdentity, 0, 10).get(2).getId();
    for (ExoSocialActivity activity : tearDownActivityList) {
      if (id == activity.getId()) {
        tearDownActivityList.remove(activity);
        break;
      }
    }
    activityStorage.deleteActivity(id);
    assertEquals(0, activityStorage.getOlderOnActivityFeed(demoIdentity, maxTime, 10).size());
  }

  /**
   * Test
   * {@link ActivityStorage#getNewerOnActivitiesOfConnections(Identity, Long, int)}
   * 
   * @since 4.0.0
   */
  @MaxQueryNumber(100)
  public void testGetNewerOnActivitiesOfConnections() {
    List<Relationship> relationships = new ArrayList<Relationship>();
    this.createActivities(3, maryIdentity);
    this.createActivities(1, demoIdentity);
    this.createActivities(2, johnIdentity);
    this.createActivities(2, rootIdentity);

    List<ExoSocialActivity> maryActivities = activityStorage.getActivitiesOfIdentity(maryIdentity,0,10);
    assertNotNull("maryActivities must not be null", maryActivities);
    assertEquals("maryActivities.size() must return: 3", 3, maryActivities.size());

    Long sinceTime = maryActivities.get(2).getPostedTime();

    List<ExoSocialActivity> activities = activityStorage.getNewerOnActivitiesOfConnections(johnIdentity,sinceTime,10);
    assertNotNull("activities must not be null", activities);
    assertEquals("activities.size() must return: 0", 0, activities.size());

    activities = activityStorage.getNewerOnActivitiesOfConnections(demoIdentity, sinceTime, 10);
    assertNotNull("activities must not be null", activities);
    assertEquals("activities.size() must return: 0", 0, activities.size());

    activities = activityStorage.getNewerOnActivitiesOfConnections(maryIdentity, sinceTime, 10);
    assertNotNull("activities must not be null", activities);
    assertEquals("activities.size() must return: 0", 0, activities.size());

    RelationshipManager relationshipManager = this.getRelationshipManager();
    Relationship maryDemoRelationship = relationshipManager.inviteToConnect(maryIdentity,demoIdentity);
    relationshipManager.confirm(maryIdentity, demoIdentity);
    relationships.add(maryDemoRelationship);

    activities = activityStorage.getNewerOnActivitiesOfConnections(maryIdentity, sinceTime, 10);
    assertNotNull("activities must not be null", activities);
    assertEquals("activities.size() must return: 1", 1, activities.size());

    activities = activityStorage.getNewerOnActivitiesOfConnections(demoIdentity, sinceTime, 10);
    assertNotNull("activities must not be null", activities);
    assertEquals("activities.size() must return: 2", 2, activities.size());

    // Delete the activity at this sinceTime will don't change the result
    String id = activityStorage.getUserActivities(maryIdentity, 0, 10).get(2).getId();
    for (ExoSocialActivity activity : tearDownActivityList) {
      if (id == activity.getId()) {
        tearDownActivityList.remove(activity);
        break;
      }
    }
    activityStorage.deleteActivity(id);
    assertEquals("activities.size() must return: 2", 2, 
                 activityStorage.getNewerOnActivitiesOfConnections(demoIdentity, sinceTime, 10).size());

    Relationship maryJohnRelationship = relationshipManager.inviteToConnect(maryIdentity,johnIdentity);
    relationshipManager.confirm(maryIdentity, johnIdentity);
    relationships.add(maryJohnRelationship);

    activities = activityStorage.getNewerOnActivitiesOfConnections(maryIdentity, sinceTime, 10);
    assertNotNull("activities must not be null", activities);
    assertEquals("activities.size() must return: 3", 3, activities.size());

    Relationship maryRootRelationship = relationshipManager.inviteToConnect(maryIdentity,rootIdentity);
    relationshipManager.confirm(maryIdentity, rootIdentity);
    relationships.add(maryRootRelationship);

    activities = activityStorage.getNewerOnActivitiesOfConnections(maryIdentity, sinceTime, 10);
    assertNotNull("activities must not be null", activities);
    assertEquals("activities.size() must return: 5", 5, activities.size());

    for (Relationship rel : relationships) {
      relationshipManager.delete(rel);
    }
  }

  /**
   * Test
   * {@link ActivityStorage#getOlderOnActivitiesOfConnections(Identity, Long, int)}
   * 
   * @since 4.0.0
   */
  @MaxQueryNumber(100)
  public void testGetOlderOnActivitiesOfConnections() {
    List<Relationship> relationships = new ArrayList<Relationship>();

    this.createActivities(3, maryIdentity);
    this.createActivities(1, demoIdentity);
    this.createActivities(2, johnIdentity);
    this.createActivities(2, rootIdentity);

    List<ExoSocialActivity> rootActivities = activityStorage.getActivitiesOfIdentity(rootIdentity,0,10);
    assertNotNull("rootActivities must not be null", rootActivities);
    assertEquals("rootActivities.size() must return: 2", 2, rootActivities.size());

    Long maxTime = rootActivities.get(1).getPostedTime();

    List<ExoSocialActivity> activities;

    activities = activityStorage.getOlderOnActivitiesOfConnections(rootIdentity, maxTime, 10);
    assertNotNull("activities must not be null", activities);
    assertEquals("activities.size() must return: 0", 0, activities.size());

    activities = activityStorage.getOlderOnActivitiesOfConnections(johnIdentity, maxTime, 10);
    assertNotNull("activities must not be null", activities);
    assertEquals("activities.size() must return: 0", 0, activities.size());

    RelationshipManager relationshipManager = this.getRelationshipManager();

    Relationship rootJohnRelationship = relationshipManager.inviteToConnect(rootIdentity,johnIdentity);
    relationshipManager.confirm(rootIdentity, johnIdentity);
    relationships.add(rootJohnRelationship);

    activities = activityStorage.getOlderOnActivitiesOfConnections(rootIdentity, maxTime, 10);
    assertNotNull("activities must not be null", activities);
    assertEquals("activities.size() must return: 2", 2, activities.size());

    // Delete the activity at this maxTime will don't change the result
    String id = activityStorage.getUserActivities(rootIdentity, 0, 10).get(1).getId();
    for (ExoSocialActivity activity : tearDownActivityList) {
      if (id == activity.getId()) {
        tearDownActivityList.remove(activity);
        break;
      }
    }
    activityStorage.deleteActivity(id);
    assertEquals("activities.size() must return: 2", 2, 
                 activityStorage.getOlderOnActivitiesOfConnections(rootIdentity, maxTime, 10).size());

    Relationship rootDemoRelationship = relationshipManager.inviteToConnect(rootIdentity,demoIdentity);
    relationshipManager.confirm(rootIdentity, demoIdentity);
    relationships.add(rootDemoRelationship);

    activities = activityStorage.getOlderOnActivitiesOfConnections(rootIdentity, maxTime, 10);
    assertNotNull("activities must not be null", activities);
    assertEquals("activities.size() must return: 3", 3, activities.size());

    Relationship rootMaryRelationship = relationshipManager.inviteToConnect(rootIdentity,maryIdentity);
    relationshipManager.confirm(rootIdentity, maryIdentity);
    relationships.add(rootMaryRelationship);

    activities = activityStorage.getOlderOnActivitiesOfConnections(rootIdentity, maxTime, 10);
    assertNotNull("activities must not be null", activities);
    assertEquals("activities.size() must return: 6", 6, activities.size());

    for (Relationship rel : relationships) {
      relationshipManager.delete(rel);
    }
  }

  /**
   * Test
   * {@link ActivityStorage#getNewerOnUserSpacesActivities(Identity, Long, int)}
   * 
   * @throws Exception
   * @since 4.0.0
   */
  @MaxQueryNumber(100)
  public void testGetNewerOnUserSpacesActivities() throws Exception {
    SpaceService spaceService = this.getSpaceService();
    Space space = this.getSpaceInstance(spaceService, 0);
    Identity spaceIdentity = 
        this.identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,space.getPrettyName(),false);

    int totalNumber = 10;

    long sinceTime = 0;

    String id="";
    // demo posts activities to space
    for (int i = 0; i < totalNumber; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(demoIdentity.getId());
      activityStorage.saveActivity(spaceIdentity, activity);
      tearDownActivityList.add(activity);
      if (i == 0) {
        sinceTime = activity.getPostedTime();
        id=activity.getId();
      }
    }

    space = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("space must not be null", space);
    assertEquals("space.getDisplayName() must return: my space 0","my space 0",space.getDisplayName());
    assertEquals("space.getDescription() must return: add new space 0","add new space 0",space.getDescription());

    List<ExoSocialActivity> activities = activityStorage.getNewerOnUserSpacesActivities(demoIdentity,sinceTime,10);
    assertNotNull("activities must not be null", activities);
    assertEquals("activities.size() must return: 9", 9, activities.size());

    Space space2 = this.getSpaceInstance(spaceService, 1);
    Identity spaceIdentity2 = 
        this.identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,space2.getPrettyName(),false);

    // demo posts activities to space2
    for (int i = 0; i < totalNumber; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(demoIdentity.getId());
      activityStorage.saveActivity(spaceIdentity2, activity);
      tearDownActivityList.add(activity);
    }

    space2 = spaceService.getSpaceByDisplayName(space2.getDisplayName());
    assertNotNull("space2 must not be null", space2);
    assertEquals("space2.getDisplayName() must return: my space 1","my space 1",space2.getDisplayName());
    assertEquals("space2.getDescription() must return: add new space 1","add new space 1",space2.getDescription());

    activities = activityStorage.getNewerOnUserSpacesActivities(demoIdentity, sinceTime, 20);
    assertNotNull("activities must not be null", activities);
    assertEquals("activities.size() must return: 19", 19, activities.size());
    
    // Delete the activity at this sinceTime will don't change the result
    for (ExoSocialActivity activity : tearDownActivityList) {
      if (id == activity.getId()) {
        tearDownActivityList.remove(activity);
        break;
      }
    }
    assertEquals("activities.size() must return: 19", 19, 
                 activityStorage.getNewerOnUserSpacesActivities(demoIdentity, sinceTime, 20).size());

    spaceService.deleteSpace(space);
    spaceService.deleteSpace(space2);
  }

  /**
   * Test
   * {@link ActivityStorage#getOlderOnUserSpacesActivities(Identity, Long, int)}
   * 
   * @throws Exception
   * @since 4.0.0
   */
  @MaxQueryNumber(100)
  public void testGetOlderOnUserSpacesActivities() throws Exception {
    SpaceService spaceService = this.getSpaceService();
    Space space = this.getSpaceInstance(spaceService, 0);
    Identity spaceIdentity = 
        this.identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,space.getPrettyName(),false);

    int totalNumber = 10;

    long maxTime = 0;
    
    String id="";

    // demo posts activities to space
    for (int i = 0; i < totalNumber; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(demoIdentity.getId());
      activityStorage.saveActivity(spaceIdentity, activity);
      tearDownActivityList.add(activity);
      if (i == totalNumber - 1) {
        maxTime = activity.getPostedTime();
        id = activity.getId();
      }
    }

    space = spaceService.getSpaceByDisplayName(space.getDisplayName());
    assertNotNull("space must not be null", space);
    assertEquals("space.getDisplayName() must return: my space 0","my space 0",space.getDisplayName());
    assertEquals("space.getDescription() must return: add new space 0","add new space 0",space.getDescription());

    List<ExoSocialActivity> activities = activityStorage.getOlderOnUserSpacesActivities(demoIdentity,maxTime,10);
    assertNotNull("activities must not be null", activities);
    assertEquals("activities.size() must return: 10", 10, activities.size());

    Space space2 = this.getSpaceInstance(spaceService, 1);
    Identity spaceIdentity2 = 
        this.identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME,space2.getPrettyName(),false);

    // demo posts activities to space2
    for (int i = 0; i < totalNumber; i++) {
      ExoSocialActivity activity = new ExoSocialActivityImpl();
      activity.setTitle("activity title " + i);
      activity.setUserId(demoIdentity.getId());
      activityStorage.saveActivity(spaceIdentity2, activity);
      tearDownActivityList.add(activity);
      if (i == totalNumber - 1) {
        maxTime = activity.getPostedTime();
      }
    }

    space2 = spaceService.getSpaceByDisplayName(space2.getDisplayName());
    assertNotNull("space2 must not be null", space2);
    assertEquals("space2.getDisplayName() must return: my space 1","my space 1",space2.getDisplayName());
    assertEquals("space2.getDescription() must return: add new space 1","add new space 1",space2.getDescription());

    activities = activityStorage.getOlderOnUserSpacesActivities(demoIdentity, maxTime, 20);
    assertNotNull("activities must not be null", activities);
    assertEquals("activities.size() must return: 20", 20, activities.size());
    
    // Delete the activity at this maxTime will don't change the result
    for (ExoSocialActivity activity : tearDownActivityList) {
      if (id == activity.getId()) {
        tearDownActivityList.remove(activity);
        break;
      }
    }
    assertEquals("activities.size() must return: 20", 20, 
                 activityStorage.getOlderOnUserSpacesActivities(demoIdentity, maxTime, 20).size());

    spaceService.deleteSpace(space);
    spaceService.deleteSpace(space2);
  }

  /**
   * Test {@link ActivityStorage#getNewerComments(ExoSocialActivity, Long, int)}
   * 
   * @since 4.0.0
   */
  @MaxQueryNumber(100)
  public void testGetNewerComments() {
    int totalNumber = 10;
    String activityTitle = "activity title";

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(rootIdentity.getId());
    activityStorage.saveActivity(rootIdentity, activity);
    tearDownActivityList.add(activity);

    for (int i = 0; i < totalNumber; i++) {
      // John comments on Root's activity
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("john comment " + i);
      comment.setUserId(johnIdentity.getId());
      activityStorage.saveComment(activity, comment);
    }

    for (int i = 0; i < totalNumber; i++) {
      // John comments on Root's activity
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("demo comment " + i);
      comment.setUserId(demoIdentity.getId());
      activityStorage.saveComment(activity, comment);
    }

    List<ExoSocialActivity> comments = activityStorage.getComments(activity, 0, 10);
    assertNotNull("comments must not be null", comments);
    assertEquals("comments.size() must return: 10", 10, comments.size());

    Long sinceTime = comments.get(0).getPostedTime();
    List<ExoSocialActivity> newerComments = activityStorage.getNewerComments(activity,sinceTime,10);
    assertNotNull("newerComments must not be null", newerComments);
    assertEquals("newerComments.size() must return: 10", 10, newerComments.size());

    sinceTime = activityStorage.getComments(activity, 0, 20).get(10).getPostedTime();
    newerComments = activityStorage.getNewerComments(activity, sinceTime, 20);
    assertNotNull("newerComments must not be null", newerComments);
    assertEquals("newerComments.size() must return: 9", 9, newerComments.size());

    sinceTime = activityStorage.getComments(activity, 0, 20).get(19).getPostedTime();
    newerComments = activityStorage.getNewerComments(activity, sinceTime, 20);
    assertNotNull("newerComments must not be null", newerComments);
    assertEquals("newerComments.size() must return: 0", 0, newerComments.size());
  }

  /**
   * Test {@link ActivityStorage#getOlderComments(ExoSocialActivity, Long, int)}
   * 
   * @since 4.0.0
   */
  @MaxQueryNumber(100)
  public void testGetOlderComments() {
    int totalNumber = 10;
    String activityTitle = "activity title";

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity.setUserId(rootIdentity.getId());
    activityStorage.saveActivity(rootIdentity, activity);
    tearDownActivityList.add(activity);

    for (int i = 0; i < totalNumber; i++) {
      // John comments on Root's activity
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("john comment " + i);
      comment.setUserId(johnIdentity.getId());
      activityStorage.saveComment(activity, comment);
    }

    List<ExoSocialActivity> comments = activityStorage.getComments(activity, 0, 10);
    assertNotNull("comments must not be null", comments);
    assertEquals("comments.size() must return: 10", 10, comments.size());

    Long maxTime = comments.get(0).getPostedTime();

    List<ExoSocialActivity> olderComments = activityStorage.getOlderComments(activity, maxTime, 10);
    assertNotNull("olderComments must not be null", olderComments);
    assertEquals("olderComments.size() must return: 0", 0, olderComments.size());

    maxTime = comments.get(9).getPostedTime();

    olderComments = activityStorage.getOlderComments(activity, maxTime, 10);
    assertNotNull("olderComments must not be null", olderComments);
    assertEquals("olderComments.size() must return: 9", 9, olderComments.size());

    maxTime = comments.get(5).getPostedTime();

    olderComments = activityStorage.getOlderComments(activity, maxTime, 10);
    assertNotNull("olderComments must not be null", olderComments);
    assertEquals("olderComments.size() must return: 5", 5, olderComments.size());
  }

}
