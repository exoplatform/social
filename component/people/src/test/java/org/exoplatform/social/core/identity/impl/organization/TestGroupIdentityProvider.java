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
  
  public void testGetByRemoteId() throws Exception {
    Identity identity = new Identity(GroupIdentityProvider.NAME, "foobarfoo");

    // identoty does not exist
    assertNull(groupIdentityProvider.getIdentityByRemoteId("foobarfoo"));
    
    // null identity
    assertNull(groupIdentityProvider.getIdentityByRemoteId(null));
    
    // identity for an existing group
    identityStorage.saveIdentity(identity);
    identity.setRemoteId("/platform/users");
    
    Identity actual = groupIdentityProvider.getIdentityByRemoteId("/platform/users");
    assertNotNull(actual);
    assertEquals(identity.getRemoteId(), actual.getRemoteId());
  
  }

}
