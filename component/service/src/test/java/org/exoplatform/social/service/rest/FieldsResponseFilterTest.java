package org.exoplatform.social.service.rest;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.rest.entity.ActivityEntity;
import org.exoplatform.social.rest.entity.CollectionEntity;
import org.exoplatform.social.rest.impl.activity.ActivityRestResourcesV1;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class FieldsResponseFilterTest extends AbstractResourceTest {

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
    
    addResource(ActivityRestResourcesV1.class, null);
  }

  public void tearDown() throws Exception {
    super.tearDown();
    removeResource(ActivityRestResourcesV1.class);
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

    activityManager.deleteActivity(maryActivity);
    activityManager.deleteActivity(demoActivity);
    activityManager.deleteActivity(rootActivity);
  }
}
