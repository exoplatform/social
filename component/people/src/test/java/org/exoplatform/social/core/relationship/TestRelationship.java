package org.exoplatform.social.core.relationship;

import org.exoplatform.social.core.identity.model.Identity;

import junit.framework.TestCase;

public class TestRelationship extends TestCase {

  public void testToString() {
    Identity id1 = new Identity("organization", "root");
    Identity id2 = new Identity("organization", "john");
    Relationship relationship = new Relationship(id1, id2);
    assertEquals("organization:root--[PENDING]--organization:john", relationship.toString());
  }

}
