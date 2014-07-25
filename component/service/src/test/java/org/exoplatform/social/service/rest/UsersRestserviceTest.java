package org.exoplatform.social.service.rest;

import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.manager.ActivityManagerImpl;
import org.exoplatform.social.core.space.impl.SpaceServiceImpl;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.service.rest.impl.user.UserSocialRestServiceV1;
import org.exoplatform.social.service.test.AbstractResourceTest;

public class UsersRestserviceTest extends AbstractResourceTest {
  
  static private UserSocialRestServiceV1 usersRestService;
  
  private IdentityStorage identityStorage;
  private ActivityManagerImpl activityManager;
  private SpaceServiceImpl spaceService;
  
  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;

  public void setUp() throws Exception {
    super.setUp();
    
    identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    activityManager = (ActivityManagerImpl) getContainer().getComponentInstanceOfType(ActivityManagerImpl.class);
    spaceService = (SpaceServiceImpl) getContainer().getComponentInstanceOfType(SpaceServiceImpl.class);
    
    rootIdentity = new Identity("organization", "root");
    johnIdentity = new Identity("organization", "john");
    maryIdentity = new Identity("organization", "mary");
    demoIdentity = new Identity("organization", "demo");
    
    identityStorage.saveIdentity(rootIdentity);
    identityStorage.saveIdentity(johnIdentity);
    identityStorage.saveIdentity(maryIdentity);
    identityStorage.saveIdentity(demoIdentity);
    
    usersRestService = new UserSocialRestServiceV1();
    registry(usersRestService);
  }

  public void tearDown() throws Exception {
    identityStorage.deleteIdentity(rootIdentity);
    identityStorage.deleteIdentity(johnIdentity);
    identityStorage.deleteIdentity(maryIdentity);
    identityStorage.deleteIdentity(demoIdentity);
    
    super.tearDown();
    unregistry(usersRestService);
  }

  public void testGetUsers() throws Exception {
    startSessionAs("root");
    ContainerResponse response = service("GET", "/v1/social/users?limit=5&offset=0", "", null, null);
    System.out.println(response.toString());
    assertNotNull(response);
    assertEquals(200, response.getStatus());
  }
}
