package org.exoplatform.social.service.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.exoplatform.social.service.rest.api.models.ActivitiesCollections;
import org.exoplatform.social.service.rest.impl.activity.ActivitySocialRestServiceV1;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class FieldsResponseFilterTest extends AbstractResourceTest {

  static private ActivitySocialRestServiceV1 activitySocialRestServiceV1;
  
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
    
    activitySocialRestServiceV1 = new ActivitySocialRestServiceV1();
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
    unregistry(activitySocialRestServiceV1);
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
    
    ContainerResponse response = service("GET", "/v1/social/activities?limit=5&offset=0", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    ActivitiesCollections collections = (ActivitiesCollections) response.getEntity();
    //must return one activity of root and one of demo
    assertEquals(2, collections.getActivities().size());
    Map<String, Object> result = collections.getActivities().get(0);
    assertEquals("demo activity", result.get(RestProperties.TITLE));
    result = collections.getActivities().get(1);
    assertEquals("root activity", result.get(RestProperties.TITLE));
    
    //
    response = service("GET", "/v1/social/activities?limit=5&offset=0&fields=id,title", "", null, null);
    assertNotNull(response);
    assertEquals(200, response.getStatus());
    
    collections = (ActivitiesCollections) response.getEntity();
    result = collections.getActivities().get(0);
    
    assertNotNull(result.get(RestProperties.ID));
    assertNotNull(result.get(RestProperties.TITLE));
    assertNull(result.get(RestProperties.COMMENTS));
    
    activityManager.deleteActivity(maryActivity);
    activityManager.deleteActivity(demoActivity);
    activityManager.deleteActivity(rootActivity);
  }
}
