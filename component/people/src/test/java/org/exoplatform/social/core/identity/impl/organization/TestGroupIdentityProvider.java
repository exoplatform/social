package org.exoplatform.social.core.identity.impl.organization;

import org.exoplatform.social.core.identity.JCRStorage;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.test.BasicPeopleServiceTestCase;

public class TestGroupIdentityProvider extends BasicPeopleServiceTestCase {

  public TestGroupIdentityProvider() throws Exception {
    super();
  }

  private GroupIdentityProvider groupIdentityProvider;
  private JCRStorage identityStorage;

  public void setUp() throws Exception {
    super.setUp();
    groupIdentityProvider = (GroupIdentityProvider) container.getComponentInstanceOfType(GroupIdentityProvider.class);
    identityStorage = (JCRStorage) container.getComponentInstanceOfType(JCRStorage.class);
  }

  public void testIsConfigured() {
    assertNotNull(groupIdentityProvider);
  }
  
  public void testGetByRemoteid() throws Exception {
    Identity identity = new Identity();
    identity.setProviderId(GroupIdentityProvider.NAME);
    identity.setRemoteId("foobarfoo");

    // identoty does not exist
    assertNull(groupIdentityProvider.getIdentityByRemoteId(identity));
    
    // null identity
    identity.setRemoteId(null);
    assertNull(groupIdentityProvider.getIdentityByRemoteId(identity));
    
    identity.setRemoteId("/platform/users");
    assertNull(groupIdentityProvider.getIdentityByRemoteId(identity));
    
    
    // identity for an existing group must have been saved prior
    identityStorage.saveIdentity(identity);
    identity.setRemoteId("/platform/users");
    Identity actual = groupIdentityProvider.getIdentityByRemoteId(identity);
    assertNotNull(actual);
    assertNotNull(actual.getId()); // must have an id
    assertEquals(identity.getRemoteId(), actual.getRemoteId());
 

    
  }

}
