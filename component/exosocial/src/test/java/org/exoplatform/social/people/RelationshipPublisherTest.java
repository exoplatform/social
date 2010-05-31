/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.people;

import java.util.List;

import org.exoplatform.social.core.activitystream.ActivityManager;
import org.exoplatform.social.core.activitystream.model.Activity;
import org.exoplatform.social.core.identity.IdentityManager;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.relationship.Relationship;
import org.exoplatform.social.core.relationship.RelationshipManager;
import org.exoplatform.social.relationship.spi.RelationshipEvent;
import org.exoplatform.social.relationship.spi.RelationshipEvent.Type;
import org.exoplatform.social.test.AbstractExoSocialTest;

public class RelationshipPublisherTest extends  AbstractExoSocialTest {

  public void testConfirmed() throws Exception {
    ActivityManager activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    assertNotNull("activityManager must not be null", activityManager);
    IdentityManager identityManager =  (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    RelationshipManager relationshipManager =  (RelationshipManager) getContainer().getComponentInstanceOfType(RelationshipManager.class);
    // inits root and john's identities
    Identity mary = identityManager.getIdentity("organization:mary");
    Identity john = identityManager.getIdentity("organization:john");
    RelationshipPublisher publisher = (RelationshipPublisher) getContainer().getComponentInstanceOfType(RelationshipPublisher.class);
    RelationshipEvent event = new RelationshipEvent(Type.CONFIRM, relationshipManager, new Relationship(mary, john));
    publisher.confirmed(event);
    List<Activity> maryActivities = activityManager.getActivities(mary);

    assertEquals(1, maryActivities.size());
    assertTrue(maryActivities.get(0).getTitleId().equals("CONNECTION_CONFIRMED"));
    assertTrue(maryActivities.get(0).getTemplateParams().get("Requester").contains("mary"));
    assertTrue(maryActivities.get(0).getTemplateParams().get("Accepter").contains("john"));

    List<Activity> johnActivities = activityManager.getActivities(john);
    assertEquals(1, johnActivities.size());
    assertTrue(johnActivities.get(0).getTitleId().equals("CONNECTION_CONFIRMED"));
    assertTrue(johnActivities.get(0).getTemplateParams().get("Requester").contains("john"));
    assertTrue(johnActivities.get(0).getTemplateParams().get("Accepter").contains("mary"));
  }
}
