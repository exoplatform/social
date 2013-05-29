/*
 * Copyright (C) 2003-2013 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.storage;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.activity.model.ExoSocialActivityImpl;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.ActivityStreamStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;


public class ActivityStreamStorageTest extends AbstractCoreTest {
  private final Log LOG = ExoLogger.getLogger(ActivityStreamStorageTest.class);
  private IdentityStorage identityStorage;
  private ActivityStorage activityStorage;
  private ActivityStreamStorage streamStorage;
  private RelationshipManager relationshipManager;
  private List<ExoSocialActivity> tearDownActivityList;

  private Identity rootIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity demoIdentity;
 
  
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    identityStorage = (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    activityStorage = (ActivityStorage) getContainer().getComponentInstanceOfType(ActivityStorage.class);
    streamStorage = (ActivityStreamStorage) getContainer().getComponentInstanceOfType(ActivityStreamStorage.class);
    relationshipManager = (RelationshipManager) getContainer().getComponentInstanceOfType(RelationshipManager.class);
    
    //
    assertNotNull("identityManager must not be null", identityStorage);
    assertNotNull("activityStorage must not be null", activityStorage);
    rootIdentity = new Identity(OrganizationIdentityProvider.NAME, "root");
    johnIdentity = new Identity(OrganizationIdentityProvider.NAME, "john");
    maryIdentity = new Identity(OrganizationIdentityProvider.NAME, "mary");
    demoIdentity = new Identity(OrganizationIdentityProvider.NAME, "demo");
    
    identityStorage.saveIdentity(rootIdentity);
    identityStorage.saveIdentity(johnIdentity);
    identityStorage.saveIdentity(maryIdentity);
    identityStorage.saveIdentity(demoIdentity);

    assertNotNull("rootIdentity.getId() must not be null", rootIdentity.getId());
    assertNotNull("johnIdentity.getId() must not be null", johnIdentity.getId());
    assertNotNull("maryIdentity.getId() must not be null", maryIdentity.getId());
    assertNotNull("demoIdentity.getId() must not be null", demoIdentity.getId());

    tearDownActivityList = new ArrayList<ExoSocialActivity>();
  }

  @Override
  protected void tearDown() throws Exception {
    for (ExoSocialActivity activity : tearDownActivityList) {
      activityStorage.deleteActivity(activity.getId());
    }
    identityStorage.deleteIdentity(rootIdentity);
    identityStorage.deleteIdentity(johnIdentity);
    identityStorage.deleteIdentity(maryIdentity);
    identityStorage.deleteIdentity(demoIdentity);
    super.tearDown();
  }

  public void testSaveActivity() throws ActivityStorageException {
    final String activityTitle = "activity Title";

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activityStorage.saveActivity(rootIdentity, activity);

    assertNotNull("activity.getId() must not be null", activity.getId());

    tearDownActivityList.addAll(activityStorage.getUserActivities(rootIdentity, 0, 1));
    
    assertEquals(1, streamStorage.getNumberOfFeed(rootIdentity));

  }
  
  public void testDeleteActivity() throws ActivityStorageException {
    final String activityTitle = "activity Title";

    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activity = activityStorage.saveActivity(rootIdentity, activity);

    assertNotNull("activity.getId() must not be null", activity.getId());

    assertEquals(1, streamStorage.getNumberOfFeed(rootIdentity));
    
    //
    activityStorage.deleteActivity(activity.getId());
    assertEquals(0, streamStorage.getNumberOfFeed(rootIdentity));

  }

  public void testGetActivity() throws ActivityStorageException {
    final String activityTitle = "activity title";
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);
    
    List<ExoSocialActivity> got = streamStorage.getFeed(demoIdentity, 0, 1);
    assertEquals(1, got.size());

  }
  
  public void testConnectionsActivity() throws ActivityStorageException {
    
    Relationship demoMaryConnection = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
    relationshipManager.confirm(demoMaryConnection);
    
    final String activityTitle = "activity title";
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);
    
    List<ExoSocialActivity> got = streamStorage.getFeed(demoIdentity, 0, 1);
    assertEquals(1, got.size());
    
    got = streamStorage.getConnections(demoIdentity, 0, 1);
    assertEquals(0, got.size());
    
    got = streamStorage.getFeed(maryIdentity, 0, 1);
    assertEquals(1, got.size());
    
    relationshipManager.delete(demoMaryConnection);
    
    //
    got = streamStorage.getFeed(maryIdentity, 0, 1);
    assertEquals(0, got.size());
    
    got = streamStorage.getConnections(maryIdentity, 0, 1);
    assertEquals(0, got.size());
    
    got = streamStorage.getFeed(demoIdentity, 0, 1);
    assertEquals(1, got.size());
    
    got = streamStorage.getConnections(demoIdentity, 0, 1);
    assertEquals(0, got.size());
  }
  
  public void testConnectionsActivityCounter() throws ActivityStorageException {
    
    Relationship demoMaryConnection = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
    relationshipManager.confirm(demoMaryConnection);
    
    final String activityTitle = "activity title";
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);
    
    assertEquals(1, streamStorage.getNumberOfFeed(demoIdentity));
    assertEquals(0, streamStorage.getNumberOfConnections(demoIdentity));
    
    assertEquals(1, streamStorage.getNumberOfFeed(maryIdentity));
    assertEquals(1, streamStorage.getNumberOfConnections(maryIdentity));

    
    relationshipManager.delete(demoMaryConnection);
    
    //
    
    assertEquals(0, streamStorage.getNumberOfFeed(maryIdentity));
    assertEquals(0, streamStorage.getNumberOfConnections(maryIdentity));
    
    assertEquals(1, streamStorage.getNumberOfFeed(demoIdentity));
    assertEquals(0, streamStorage.getNumberOfConnections(demoIdentity));
  }
  
  public void testConnectionsExistActivities() throws ActivityStorageException {
    
    final String activityTitle = "activity title";
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);
    
    Relationship demoMaryConnection = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
    relationshipManager.confirm(demoMaryConnection);
    
    List<ExoSocialActivity> got = streamStorage.getFeed(demoIdentity, 0, 1);
    assertEquals(1, got.size());
    
    got = streamStorage.getFeed(maryIdentity, 0, 1);
    assertEquals(1, got.size());
    
    relationshipManager.delete(demoMaryConnection);
  }
  
  public void testConnectionsExistActivitiesCounter() throws ActivityStorageException {
    
    final String activityTitle = "activity title";
    ExoSocialActivity activity = new ExoSocialActivityImpl();
    activity.setTitle(activityTitle);
    activityStorage.saveActivity(demoIdentity, activity);
    tearDownActivityList.add(activity);
    
    Relationship demoMaryConnection = relationshipManager.inviteToConnect(demoIdentity, maryIdentity);
    relationshipManager.confirm(demoMaryConnection);
    
    assertEquals(1, streamStorage.getNumberOfFeed(demoIdentity));
    assertEquals(1, streamStorage.getNumberOfFeed(maryIdentity));
    
    relationshipManager.delete(demoMaryConnection);

  }
 
}
