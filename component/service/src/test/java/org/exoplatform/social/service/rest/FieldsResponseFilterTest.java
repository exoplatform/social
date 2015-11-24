package org.exoplatform.social.service.rest;

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
import org.exoplatform.social.rest.impl.activity.ActivityRestResourcesV1;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class FieldsResponseFilterTest extends AbstractResourceTest {

  static private ActivityRestResourcesV1 activitySocialRestServiceV1;
  
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
    
    activitySocialRestServiceV1 = new ActivityRestResourcesV1();
    registry(activitySocialRestServiceV1);
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
    removeResource(activitySocialRestServiceV1.getClass());
  }
  
  public void testGetActivitiesWithFields() throws Exception {
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
    
    ContainerResponse response = service("GET", getURLResource("activities?limit=5&offset=0"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    CollectionEntity collections = (CollectionEntity) response.getEntity();
    //must return one activity of root and one of demo
    assertEquals(2, collections.getEntities().size());
    ActivityEntity activityEntity = getBaseEntity(collections.getEntities().get(0), ActivityEntity.class);
    
    assertEquals("demo activity", activityEntity.getTitle());
    activityEntity = getBaseEntity(collections.getEntities().get(1), ActivityEntity.class);
    assertEquals("root activity", activityEntity.getTitle());
    
    //
    response = service("GET", getURLResource("activities?limit=5&offset=0&fields=id,title"), "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    collections = (CollectionEntity) response.getEntity();
    activityEntity = getBaseEntity(collections.getEntities().get(0), ActivityEntity.class);
    
    assertNotNull(activityEntity.getId());
    assertNotNull(activityEntity.getTitle());
//    assertNull(activityEntity.getComments());
    
    activityManager.deleteActivity(maryActivity);
    activityManager.deleteActivity(demoActivity);
    activityManager.deleteActivity(rootActivity);
  }
}
