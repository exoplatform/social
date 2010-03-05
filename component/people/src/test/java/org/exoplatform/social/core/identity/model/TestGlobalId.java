package org.exoplatform.social.core.identity.model;

import junit.framework.TestCase;

public class TestGlobalId extends TestCase {

  
  public void testValidContructor() {    
    GlobalId id = new GlobalId("foo:bar"); // valid
    assertEquals("foo", id.getDomain());
    assertEquals("bar", id.getLocalId());
  }
  
  public void testIsValid() {
    assertFalse(GlobalId.isValid(null));
    assertFalse(GlobalId.isValid(":"));
    assertFalse(GlobalId.isValid(":dqsdqsd"));
    assertTrue(GlobalId.isValid("foo:")); // empty localId is allowed
    assertTrue(GlobalId.isValid("foo:bar"));
  }
  
  public void testMalformedId() {
    try {
      new GlobalId(null);
    }
    catch (IllegalArgumentException iae) {
      return ; // expected
    }
    fail("should have thrown an IllegalArgumentException");
    try {
      new GlobalId(":");
    }
    catch (IllegalArgumentException iae) {
      return ; // expected
    }
    fail("should have thrown an IllegalArgumentException");
    
    try {
      new GlobalId(":ssss");
    }
    catch (IllegalArgumentException iae) {
      return ; // expected
    }
    fail("should have thrown an IllegalArgumentException");
    
  }

 
  
}
