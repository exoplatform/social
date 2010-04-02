package org.exoplatform.social.space;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import junit.framework.TestCase;

import org.exoplatform.social.core.identity.JCRStorage;
import org.exoplatform.social.core.identity.model.Identity;

public class TestSpaceIdentityProvider extends TestCase {

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
  
}
