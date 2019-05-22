package org.exoplatform.social.rest.impl.activity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.MembershipEntry;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.rest.entity.ActivityEntity;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.CommentEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.rest.impl.comment.CommentRestResourcesTest;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class ActivityRestResourcesTest extends AbstractResourceTest {

  private ActivityRestResourcesV1 activityRestResourcesV1;

  private IdentityStorage         identityStorage;

  private ActivityManager         activityManager;

  private RelationshipManager     relationshipManager;

  private SpaceService            spaceService;

  private Identity                rootIdentity;

  private Identity                johnIdentity;

  private Identity                maryIdentity;

  private Identity                demoIdentity;

  private Identity                testSpaceIdentity;

  private final Logger            log = LoggerFactory.getLogger(ActivityRestResourcesTest.class);

  public void setUp() throws Exception {
    super.setUp();

    System.setProperty("gatein.email.domain.url", "localhost:8080");

    identityStorage = getContainer().getComponentInstanceOfType(IdentityStorage.class);
    activityManager = getContainer().getComponentInstanceOfType(ActivityManager.class);
    relationshipManager = getContainer().getComponentInstanceOfType(RelationshipManager.class);
    spaceService = getContainer().getComponentInstanceOfType(SpaceService.class);

    rootIdentity = new Identity("organization", "root");
    johnIdentity = new Identity("organization", "john");
    maryIdentity = new Identity("organization", "mary");
    demoIdentity = new Identity("organization", "demo");

    identityStorage.saveIdentity(rootIdentity);
    identityStorage.saveIdentity(johnIdentity);
    identityStorage.saveIdentity(maryIdentity);
    identityStorage.saveIdentity(demoIdentity);

    activityRestResourcesV1 = new ActivityRestResourcesV1();
    registry(activityRestResourcesV1);
  }

  public void tearDown() throws Exception {
    super.tearDown();
    removeResource(activityRestResourcesV1.getClass());
  }

  public void testGetSpaceActivity() throws Exception {
    startSessionAs("root");

    Space space = getSpaceInstance("test", "root");
    testSpaceIdentity = new Identity(SpaceIdentityProvider.NAME, "test");
    identityStorage.saveIdentity(testSpaceIdentity);
    try {
      ExoSocialActivity testSpaceActivity = new ExoSocialActivityImpl();
      testSpaceActivity.setTitle("Test space activity");
      activityManager.saveActivityNoReturn(testSpaceIdentity, testSpaceActivity);

      assertNotNull(testSpaceIdentity.getId());
      // Test get an activity(which is not a comment)
      ContainerResponse response = service("GET",
                                           "/" + VersionResources.VERSION_ONE + "/social/activities/" + testSpaceActivity.getId(),
                                           "",
                                           null,
                                           null);
      assertNotNull(response);
      assertEquals(200, response.getStatus());
      ActivityEntity activityEntity = getBaseEntity((DataEntity) response.getEntity(), ActivityEntity.class);
      assertNotNull(activityEntity);
      assertNotNull(activityEntity.getOwner());
      assertTrue(activityEntity.getOwner().contains("/social/spaces/" + space.getId()));

      // Test get a comment
      ExoSocialActivity testComment = new ExoSocialActivityImpl();
      testComment.setTitle("Test Comment");
      activityManager.saveComment(testSpaceActivity, testComment);
      response = service("GET",
                         "/" + VersionResources.VERSION_ONE + "/social/activities/comment" + testComment.getId(),
                         "",
                         null,
                         null);
      assertNotNull(response);
      assertEquals(200, response.getStatus());
      CommentEntity commentEntity = getBaseEntity((DataEntity) response.getEntity(), CommentEntity.class);
      assertNotNull(commentEntity);
      assertNotNull(commentEntity.getTitle());
      assertEquals(commentEntity.getTitle(), "Test Comment");
      // Test get an activity which is not a comment
      response = service("GET",
                         "/" + VersionResources.VERSION_ONE + "/social/activities/comment" + testSpaceActivity.getId(),
                         "",
                         null,
                         null);
      assertNotNull(response);
      assertEquals(404, response.getStatus());

      startSessionAs("John");
      // Test get a comment when logged user is not a member of space in which
      // the comment is posted
      response = service("GET",
                         "/" + VersionResources.VERSION_ONE + "/social/activities/comment" + testComment.getId(),
                         "",
                         null,
                         null);
      assertNotNull(response);
      assertEquals(401, response.getStatus());

      // Test get an activity when logged user is not a member of space in which
      // the activity is posted
      response = service("GET",
                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + testSpaceActivity.getId(),
                         "",
                         null,
                         null);
      assertNotNull(response);
      assertEquals(401, response.getStatus());

      startSessionAs("root");
      // Test get an activity which does not exist
      activityManager.deleteActivity(testSpaceActivity);
      response = service("GET",
                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + testSpaceActivity.getId(),
                         "",
                         null,
                         null);
      assertNotNull(response);
      assertEquals(404, response.getStatus());
    } catch (Exception exc) {
      log.error(exc);
    } finally {
      if (space != null) {
        spaceService.deleteSpace(space);
      }
    }
  }

  public void testGetSpaceActivityWithBody() throws Exception {
    startSessionAs("root");

    Space space = getSpaceInstance("test", "root");
    testSpaceIdentity = new Identity(SpaceIdentityProvider.NAME, "test");
    identityStorage.saveIdentity(testSpaceIdentity);
    try {
      ExoSocialActivity testSpaceActivity = new ExoSocialActivityImpl();
      testSpaceActivity.setTitle("Test space activity title");
      testSpaceActivity.setBody("test space activity body");
      activityManager.saveActivityNoReturn(testSpaceIdentity, testSpaceActivity);

      assertNotNull(testSpaceIdentity.getId());
      // Test get an activity(which is not a comment)
      ContainerResponse response = service("GET",
                                           "/" + VersionResources.VERSION_ONE + "/social/activities/" + testSpaceActivity.getId(),
                                           "",
                                           null,
                                           null);
      assertNotNull(response);
      assertEquals(200, response.getStatus());
      ActivityEntity activityEntity = getBaseEntity((DataEntity) response.getEntity(), ActivityEntity.class);
      assertNotNull(activityEntity);
      assertNotNull(activityEntity.getBody());
      assertEquals("Test space activity title", activityEntity.getTitle());
      assertEquals("test space activity body", activityEntity.getBody());

      assertNotNull(activityEntity.getOwner());
      assertTrue(activityEntity.getOwner().contains("/social/spaces/" + space.getId()));
    } finally {
      if (space != null) {
        spaceService.deleteSpace(space);
      }
    }
  }

  public void testGetActivitiesOfCurrentUser() throws Exception {
    startSessionAs("root");

    relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    relationshipManager.confirm(demoIdentity, rootIdentity);

    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);
    //
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);
    //
    ExoSocialActivity maryActivity = new ExoSocialActivityImpl();
    maryActivity.setTitle("mary activity");
    activityManager.saveActivityNoReturn(maryIdentity, maryActivity);

    ContainerResponse response = service("GET",
                                         "/" + VersionResources.VERSION_ONE + "/social/activities?limit=5&offset=0",
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    CollectionEntity collections = (CollectionEntity) response.getEntity();
    // must return one activity of root and one of demo
    assertEquals(2, collections.getEntities().size());
    List<String> activitiesTitle = new ArrayList<>(2);
    ActivityEntity entity = getBaseEntity(collections.getEntities().get(0), ActivityEntity.class);
    activitiesTitle.add(entity.getTitle());
    entity = getBaseEntity(collections.getEntities().get(1), ActivityEntity.class);
    activitiesTitle.add(entity.getTitle());
    assertTrue(activitiesTitle.contains("root activity"));
    assertTrue(activitiesTitle.contains("demo activity"));
  }

  public void testGetUpdatedDeletedActivityById() throws Exception {
    startSessionAs("root");

    relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    relationshipManager.confirm(demoIdentity, rootIdentity);

    //
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);

    ContainerResponse response = service("GET",
                                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + demoActivity.getId(),
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    ActivityEntity result = getBaseEntity(response.getEntity(), ActivityEntity.class);
    assertEquals(result.getTitle(), "demo activity");

    String input = "{\"title\":updated}";
    // root try to update demo activity
    response = getResponse("PUT", "/" + VersionResources.VERSION_ONE + "/social/activities/" + demoActivity.getId(), input);
    assertNotNull(response);
    // root is not the poster of activity then he can't modify it
    assertEquals(401, response.getStatus());

    // demo try to update demo activity
    startSessionAs("demo");
    response = getResponse("PUT", "/" + VersionResources.VERSION_ONE + "/social/activities/" + demoActivity.getId(), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    result = getBaseEntity(response.getEntity(), ActivityEntity.class);
    assertEquals(result.getTitle(), "updated");

    // demo delete his activity
    response =
             service("DELETE", "/" + VersionResources.VERSION_ONE + "/social/activities/" + demoActivity.getId(), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());

    assertNull(activityManager.getActivity(demoActivity.getId()));
  }

  public void testGetComments() throws Exception {
    startSessionAs("root");
    int nbComments = 5;
    // root posts one activity and some comments
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);
    //
    for (int i = 0; i < nbComments; i++) {
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("comment " + i);
      comment.setUserId(rootIdentity.getId());
      activityManager.saveComment(rootActivity, comment);
    }

    ContainerResponse response = service("GET",
                                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId()
                                             + "/comments",
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(5, collections.getEntities().size());

    startSessionAs("demo");
    response = service("GET",
                       "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId() + "/comments",
                       "",
                       null,
                       null);
    assertNotNull(response);
    // demo has no permission to view activity
    assertEquals(401, response.getStatus());

    // demo connects with root
    relationshipManager.inviteToConnect(demoIdentity, rootIdentity);
    relationshipManager.confirm(rootIdentity, demoIdentity);

    response = service("GET",
                       "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId() + "/comments",
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(5, collections.getEntities().size());

    // clean data
    activityManager.deleteActivity(rootActivity);
  }

  public void testGetCommentsWithReplies() throws Exception {
    startSessionAs("root");
    int nbComments = 5;
    int nbReplies = 5;
    // root posts one activity and some comments
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);
    //
    for (int i = 0; i < nbComments; i++) {
      ExoSocialActivity comment = new ExoSocialActivityImpl();
      comment.setTitle("comment " + i);
      comment.setUserId(rootIdentity.getId());
      activityManager.saveComment(rootActivity, comment);
      for (int j = 0; j < nbReplies; j++) {
        ExoSocialActivity commentReply = new ExoSocialActivityImpl();
        commentReply.setTitle("comment reply " + i + " - " + j);
        commentReply.setUserId(maryIdentity.getId());
        commentReply.setParentCommentId(comment.getId());
        activityManager.saveComment(rootActivity, commentReply);
      }
    }

    ContainerResponse response = service("GET",
                                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId()
                                             + "/comments",
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(5, collections.getEntities().size());

    response = service("GET",
                       "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId()
                           + "/comments?expand=subComments",
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(nbComments + nbComments * nbReplies, collections.getEntities().size());

    startSessionAs("demo");
    response = service("GET",
                       "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId() + "/comments",
                       "",
                       null,
                       null);
    assertNotNull(response);
    // demo has no permission to view activity
    assertEquals(401, response.getStatus());

    // demo connects with root
    relationshipManager.inviteToConnect(demoIdentity, rootIdentity);
    relationshipManager.confirm(rootIdentity, demoIdentity);

    response = service("GET",
                       "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId() + "/comments",
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(5, collections.getEntities().size());

    response = service("GET",
                       "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId()
                           + "/comments?expand=subComments",
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(nbComments + nbComments * nbReplies, collections.getEntities().size());

    // clean data
    activityManager.deleteActivity(rootActivity);
  }

  public void testPostComment() throws Exception {
    startSessionAs("root");

    // root posts one activity
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);

    // post a comment by root on the prevous activity
    String input = "{\"body\":comment1, \"title\":comment1}";
    ContainerResponse response = getResponse("POST",
                                             "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId()
                                                 + "/comments",
                                             input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CommentEntity result = getBaseEntity(response.getEntity(), CommentEntity.class);
    assertEquals("comment1", result.getTitle());

    assertEquals(1, activityManager.getCommentsWithListAccess(rootActivity).getSize());

    // clean data
    activityManager.deleteActivity(rootActivity);
  }

  public void testPostCommentReply() throws Exception {
    startSessionAs("root");

    // root posts one activity
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);

    // post a comment by root on the previous activity
    String input = "{\"body\":\"comment1 body\", \"title\":comment1}";
    ContainerResponse response = getResponse("POST",
                                             "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId()
                                                 + "/comments",
                                             input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CommentEntity comment = getBaseEntity(response.getEntity(), CommentEntity.class);
    assertEquals("comment1", comment.getTitle());
    assertEquals("comment1 body", comment.getBody());
    assertNotNull(comment.getId());

    assertEquals(1, activityManager.getCommentsWithListAccess(rootActivity).getSize());

    String commentReplyInput = "{\"body\":\"comment reply 1 body\", \"title\":\"comment reply 1\", \"parentCommentId\": "
        + comment.getId() + "}";
    response = getResponse("POST",
                           "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId() + "/comments",
                           commentReplyInput);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CommentEntity commentReply = getBaseEntity(response.getEntity(), CommentEntity.class);
    assertEquals("comment reply 1", commentReply.getTitle());
    assertEquals("comment reply 1 body", commentReply.getBody());
    assertEquals(comment.getId(), commentReply.getParentCommentId());

    assertEquals(1, activityManager.getCommentsWithListAccess(rootActivity).getSize());

    assertEquals(1, activityManager.getCommentsWithListAccess(rootActivity, true).getSize());

    assertEquals(2, activityManager.getCommentsWithListAccess(rootActivity, true).load(0, -1).length);

    // clean data
    activityManager.deleteActivity(rootActivity);
  }

  public void testGetLikes() throws Exception {
    startSessionAs("root");
    // root posts one activity and some comments
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);

    List<String> likerIds = new ArrayList<String>();
    likerIds.add(demoIdentity.getId());
    rootActivity.setLikeIdentityIds(likerIds.toArray(new String[likerIds.size()]));
    activityManager.updateActivity(rootActivity);

    ContainerResponse response = service("GET",
                                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId()
                                             + "/likes",
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    // clean data
    activityManager.deleteActivity(rootActivity);
  }

  public void testPostLike() throws Exception {
    startSessionAs("root");

    // root posts one activity
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);

    List<String> likerIds = new ArrayList<String>();
    likerIds.add(demoIdentity.getId());
    rootActivity.setLikeIdentityIds(likerIds.toArray(new String[likerIds.size()]));
    activityManager.updateActivity(rootActivity);

    ContainerResponse response = service("GET",
                                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId()
                                             + "/likes",
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());

    // post a like by root on the activity
    List<String> updatedLikes = new ArrayList<String>();
    updatedLikes.add(activityManager.getActivity(rootActivity.getId()).getLikeIdentityIds()[0]);
    updatedLikes.add(maryIdentity.getId());
    rootActivity.setLikeIdentityIds(updatedLikes.toArray(new String[updatedLikes.size()]));
    activityManager.updateActivity(rootActivity);

    response = service("GET",
                       "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId() + "/likes",
                       "",
                       null,
                       null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size());

    // clean data
    activityManager.deleteActivity(rootActivity);
  }

  public void testDeleteLike() throws Exception {
    startSessionAs("demo");

    // root posts one activity
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);

    List<String> likerIds = new ArrayList<String>();
    likerIds.add(demoIdentity.getId());
    demoActivity.setLikeIdentityIds(likerIds.toArray(new String[likerIds.size()]));
    activityManager.updateActivity(demoActivity);

    ContainerResponse response = service("DELETE",
                                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + demoActivity.getId()
                                             + "/likes/demo",
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    DataEntity activityEntity = (DataEntity) response.getEntity();
    assertNotNull(activityEntity);

    // clean data
    activityManager.deleteActivity(demoActivity);
  }

  public void testDeleteLikeWhenNoPermissionOnActivity() throws Exception {
    startSessionAs("root");

    // root posts one activity
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);

    startSessionAs("demo");

    ContainerResponse response = service("DELETE",
                                         "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId()
                                             + "/likes/demo",
                                         "",
                                         null,
                                         null);
    assertNotNull(response);
    assertEquals(401, response.getStatus());
    DataEntity activityEntity = (DataEntity) response.getEntity();
    // the activity data must not be returned since the user has not the
    // permissions to view it
    assertNull(activityEntity);

    // clean data
    activityManager.deleteActivity(rootActivity);
  }

  private Space getSpaceInstance(String prettyName, String creator) throws Exception {
    Space space = new Space();
    space.setDisplayName(prettyName);
    space.setPrettyName(space.getDisplayName());
    space.setRegistration(Space.OPEN);
    space.setDescription("add new space " + prettyName);
    space.setType(DefaultSpaceApplicationHandler.NAME);
    space.setVisibility(Space.PRIVATE);
    space.setRegistration(Space.VALIDATION);
    space.setPriority(Space.INTERMEDIATE_PRIORITY);
    this.spaceService.createSpace(space, creator);
    return space;
  }
}
