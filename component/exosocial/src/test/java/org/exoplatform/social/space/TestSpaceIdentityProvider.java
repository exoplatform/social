package org.exoplatform.social.space;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

import org.exoplatform.commons.testing.jcr.AbstractJCRTestCase;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.JCRStorage;
import org.exoplatform.social.core.identity.model.Identity;
import org.testng.annotations.Test;

@ConfiguredBy({@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/exo.social.component.exosocial.configuration.xml")})
public class TestSpaceIdentityProvider extends AbstractJCRTestCase {

  @Test
  public void testGetIdentityByRemoteId() throws Exception {

    Space space = new Space();
    String spaceId = "111";
    space.setId(spaceId);
    
    SpaceService spaceService = mock(SpaceService.class);
    when(spaceService.getSpaceById(spaceId)).thenReturn(space);
    
    Identity spaceIdentity = new Identity(SpaceIdentityProvider.NAME, spaceId);
    String identityId = "00001";
    spaceIdentity.setId(identityId);
    JCRStorage identityStorage = mock(JCRStorage.class);
    when(identityStorage.findIdentity(SpaceIdentityProvider.NAME, spaceId)).thenReturn(spaceIdentity);
    
    SpaceIdentityProvider identityProvider = new SpaceIdentityProvider(spaceService);    
    Identity identity = identityProvider.getIdentityByRemoteId(spaceId);
    assertNotNull(identity);
    
  }
  
  @Test
  public void testWithSpaceName() throws Exception {

    Space space = new Space();
    space.setDescription("blabla");
    space.setGroupId("/platform/users");
    space.setApp("app");
    space.setName("space1");

    SpaceService spaceService = getComponent(SpaceService.class);
    spaceService.saveSpace(space, true);
    
    String spaceId = space.getId();
    
    IdentityManager identityManager = getComponent(IdentityManager.class);
    Identity identity = identityManager.getOrCreateIdentity(SpaceIdentityProvider.NAME, spaceId);
    
    SpaceIdentityProvider identityProvider = new SpaceIdentityProvider(spaceService);    
    Identity identity2 = identityProvider.getIdentityByRemoteId("space:space1");
    assertEquals(identity2.getRemoteId(), identity.getRemoteId());
    
  }
  
  
}
