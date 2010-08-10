package org.exoplatform.social.core.identity.provider;
//package org.exoplatform.social.core.identity.impl.organization;
//
//import org.exoplatform.social.AbstractPeopleTest;
//import org.exoplatform.social.core.identity.JCRStorage;
//import org.exoplatform.social.core.identity.model.Identity;
//
//public class TestGroupIdentityProvider extends AbstractPeopleTest {
//  private GroupIdentityProvider groupIdentityProvider;
//  private JCRStorage identityStorage;
//
//  public void testIsConfigured() {
//    assertNotNull(groupIdentityProvider);
//  }
//
//  public void testGetByRemoteId() throws Exception {
//    Identity identity = new Identity(GroupIdentityProvider.NAME, "foobarfoo");
//
//    // identity does not exist
//    assertNull(groupIdentityProvider.getIdentityByRemoteId("foobarfoo"));
//
//    // null identity
//    assertNull(groupIdentityProvider.getIdentityByRemoteId(null));
//
//    // identity for an existing group
//    identityStorage.saveIdentity(identity);
//    identity.setRemoteId("/platform/users");
//
//    Identity actual = groupIdentityProvider.getIdentityByRemoteId("/platform/users");
//    assertNotNull(actual);
//    assertEquals(actual.getRemoteId(), identity.getRemoteId());
//
//  }
//
//}
