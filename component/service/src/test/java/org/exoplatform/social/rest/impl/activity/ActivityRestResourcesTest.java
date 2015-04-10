package org.exoplatform.social.rest.impl.activity;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.rest.entity.ActivityEntity;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.entity.CommentEntity;
import org.exoplatform.social.service.rest.api.VersionResources;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class ActivityRestResourcesTest extends AbstractResourceTest {
  
  private ActivityRestResourcesV1 activityRestResourcesV1;
  
  private IdentityStorage identityStorage;
  private ActivityManager activityManager;
  private RelationshipManager relationshipManager;
  private SpaceService spaceService;
  
  private List<Space> tearDownSpaceList;
  
  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  public void setUp() throws Exception {
    super.setUp();
    
    System.setProperty("gatein.email.domain.url", "localhost:8080");
    tearDownSpaceList = new ArrayList<Space>();
    
    identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    relationshipManager = (RelationshipManager) getContainer().getComponentInstanceOfType(RelationshipManager.class);
    spaceService = (SpaceService) getContainer().getComponentInstanceOfType(SpaceService.class);
    
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
    for (Space space : tearDownSpaceList) {
      Identity spaceIdentity = identityStorage.findIdentity(SpaceIdentityProvider.NAME, space.getPrettyName());
      if (spaceIdentity != null) {
        identityStorage.deleteIdentity(spaceIdentity);
      }
      spaceService.deleteSpace(space);
    }
    
    identityStorage.deleteIdentity(rootIdentity);
    identityStorage.deleteIdentity(johnIdentity);
    identityStorage.deleteIdentity(maryIdentity);
    identityStorage.deleteIdentity(demoIdentity);
    
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
    ActivityEntity entity = getBaseEntity(collections.getEntities().get(0), ActivityEntity.class);
    assertEquals("demo activity", entity.getTitle());
    entity = getBaseEntity(collections.getEntities().get(1), ActivityEntity.class);
    assertEquals("root activity", entity.getTitle());
    
    activityManager.deleteActivity(maryActivity);
    activityManager.deleteActivity(demoActivity);
    activityManager.deleteActivity(rootActivity);
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
    String input = "{\"body\":comment1}";
    ContainerResponse response = getResponse("POST", "/" + VersionResources.VERSION_ONE + "/social/activities/" + rootActivity.getId() + "/comments", input);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    CommentEntity result = getBaseEntity(response.getEntity(), CommentEntity.class);
    assertEquals("comment1", result.getBody());
    
    assertEquals(1, activityManager.getCommentsWithListAccess(rootActivity).getSize());
    
    //clean data
    activityManager.deleteActivity(rootActivity);
  }
}
