package org.exoplatform.social.rest.impl.activity;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.rest.entity.ActivityEntity;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.CommentEntity;
import org.exoplatform.social.rest.entity.DataEntity;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.test.AbstractResourceTest;

import java.util.ArrayList;
import java.util.List;

public class ActivityRestResourcesTest extends AbstractResourceTest {
  
  private ActivityRestResourcesV1 activityRestResourcesV1;
  
  private IdentityStorage identityStorage;
  private ActivityManager activityManager;
  private RelationshipManager relationshipManager;

  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  public void setUp() throws Exception {
    super.setUp();
    
    System.setProperty("gatein.email.domain.url", "localhost:8080");

    identityStorage = getContainer().getComponentInstanceOfType(IdentityStorage.class);
    activityManager = getContainer().getComponentInstanceOfType(ActivityManager.class);
    relationshipManager = getContainer().getComponentInstanceOfType(RelationshipManager.class);

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
    
    ContainerResponse response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/activities?limit=5&offset=0", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    //must return one activity of root and one of demo
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
    
    ContainerResponse response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/activities/" + demoActivity.getId(), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    ActivityEntity result = getBaseEntity(response.getEntity(), ActivityEntity.class);
    assertEquals(result.getTitle(), "demo activity");
    
    String input = "{\"title\":updated}";
    //root try to update demo activity
    response = getResponse("PUT", "/" + VersionResources.VERSION_ONE + "/social/activities/" + demoActivity.getId(), input);
    assertNotNull(response);
    //root is not the poster of activity then he can't modify it
    assertEquals(401, response.getStatus());
    
    //demo try to update demo activity
    startSessionAs("demo");
    response = getResponse("PUT", "/" + VersionResources.VERSION_ONE + "/social/activities/" + demoActivity.getId(), input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    result = getBaseEntity(response.getEntity(), ActivityEntity.class);
    assertEquals(result.getTitle(), "updated");
    
    //demo delete his activity
    response = service("DELETE", "/" + VersionResources.VERSION_ONE + "/social/activities/" + demoActivity.getId(), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    assertNull(activityManager.getActivity(demoActivity.getId()));
  }
  
  public void testGetComments() throws Exception {
    startSessionAs("root");
    int nbComments = 5;
    //root posts one activity and some comments
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
    
    ContainerResponse response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId() + "/comments", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(5, collections.getEntities().size());
    
    startSessionAs("demo");
    response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId() + "/comments", "", null, null);
    assertNotNull(response);
    //demo has no permission to view activity
    assertEquals(401, response.getStatus());
    
    //demo connects with root
    relationshipManager.inviteToConnect(demoIdentity, rootIdentity);
    relationshipManager.confirm(rootIdentity, demoIdentity);
    
    response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId() + "/comments", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(5, collections.getEntities().size());
    
    //clean data
    activityManager.deleteActivity(rootActivity);
  }
  
  public void testPostComment() throws Exception {
    startSessionAs("root");
    
    //root posts one activity
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);
    
    //post a comment by root on the prevous activity
    String input = "{\"body\":comment1, \"title\":comment1}";
    ContainerResponse response = getResponse("POST", "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId() + "/comments", input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CommentEntity result = getBaseEntity(response.getEntity(), CommentEntity.class);
    assertEquals("comment1", result.getTitle());
    
    assertEquals(1, activityManager.getCommentsWithListAccess(rootActivity).getSize());
    
    //clean data
    activityManager.deleteActivity(rootActivity);
  }
  
  public void testGetLikes() throws Exception {
    startSessionAs("root");
    //root posts one activity and some comments
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);
    
    List<String> likerIds = new ArrayList<String>();
    likerIds.add(demoIdentity.getId());
    rootActivity.setLikeIdentityIds(likerIds.toArray(new String[likerIds.size()]));
    activityManager.updateActivity(rootActivity);
    
    ContainerResponse response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId() + "/likes", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());
    
    //clean data
    activityManager.deleteActivity(rootActivity);
  }
  
  public void testPostLike() throws Exception {
    startSessionAs("root");
    
    //root posts one activity
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);
    
    List<String> likerIds = new ArrayList<String>();
    likerIds.add(demoIdentity.getId());
    rootActivity.setLikeIdentityIds(likerIds.toArray(new String[likerIds.size()]));
    activityManager.updateActivity(rootActivity);
    
    ContainerResponse response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId() + "/likes", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    assertEquals(1, collections.getEntities().size());
    
    //post a like by root on the activity
    List<String> updatedLikes = new ArrayList<String>();
    updatedLikes.add(activityManager.getActivity(rootActivity.getId()).getLikeIdentityIds()[0]);
    updatedLikes.add(maryIdentity.getId());
    rootActivity.setLikeIdentityIds(updatedLikes.toArray(new String[updatedLikes.size()]));
    activityManager.updateActivity(rootActivity);
    
    response = service("GET", "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId() + "/likes", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    collections = (CollectionEntity) response.getEntity();
    assertEquals(2, collections.getEntities().size());
    
    //clean data
    activityManager.deleteActivity(rootActivity);
  }

  public void testDeleteLike() throws Exception {
    startSessionAs("demo");

    //root posts one activity
    ExoSocialActivity demoActivity = new ExoSocialActivityImpl();
    demoActivity.setTitle("demo activity");
    activityManager.saveActivityNoReturn(demoIdentity, demoActivity);

    List<String> likerIds = new ArrayList<String>();
    likerIds.add(demoIdentity.getId());
    demoActivity.setLikeIdentityIds(likerIds.toArray(new String[likerIds.size()]));
    activityManager.updateActivity(demoActivity);

    ContainerResponse response = service("DELETE", "/" + VersionResources.VERSION_ONE + "/social/activities/" + demoActivity.getId() + "/likes/demo", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    DataEntity activityEntity = (DataEntity) response.getEntity();
    assertNotNull(activityEntity);

    //clean data
    activityManager.deleteActivity(demoActivity);
  }

  public void testDeleteLikeWhenNoPermissionOnActivity() throws Exception {
    startSessionAs("root");

    //root posts one activity
    ExoSocialActivity rootActivity = new ExoSocialActivityImpl();
    rootActivity.setTitle("root activity");
    activityManager.saveActivityNoReturn(rootIdentity, rootActivity);

    startSessionAs("demo");

    ContainerResponse response = service("DELETE", "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId() + "/likes/demo", "", null, null);
    assertNotNull(response);
    assertEquals(401, response.getStatus());
    DataEntity activityEntity = (DataEntity) response.getEntity();
    // the activity data must not be returned since the user has not the permissions to view it
    assertNull(activityEntity);

    //clean data
    activityManager.deleteActivity(rootActivity);
  }
}
