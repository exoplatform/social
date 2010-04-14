package org.exoplatform.social.core.identity;

import org.exoplatform.social.core.identity.model.Identity;

import junit.framework.TestCase;

public class TestIdentity extends TestCase {

  public void testToString() {
    Identity id = new Identity("organization", "root");
    assertEquals(id.toString(), "organization:root");
  }

}
