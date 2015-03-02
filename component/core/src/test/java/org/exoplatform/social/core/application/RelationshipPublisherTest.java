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
package org.exoplatform.social.core.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.activity.model.ExoSocialActivity;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.ActivityManager;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.manager.RelationshipManager;
import org.exoplatform.social.core.relationship.RelationshipEvent;
import org.exoplatform.social.core.relationship.RelationshipEvent.Type;
import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.test.AbstractCoreTest;

/**
 * Unit Tests for {@link RelationshipPublisher}
 * @author hoat_le
 */
public class RelationshipPublisherTest extends  AbstractCoreTest {
  private final Log LOG = ExoLogger.getLogger(RelationshipPublisher.class);
  private ActivityManager activityManager;
  private IdentityManager identityManager;
  private IdentityStorage identityStorage;
  private RelationshipManager relationshipManager;
  private RelationshipPublisher relationshipPublisher;
  private List<ExoSocialActivity> tearDownActivityList;
  private Identity rootIdentity;
  private Identity demoIdentity;
  private Identity johnIdentity;
  private Identity maryIdentity;
  private Identity raulIdentity;
  private Identity paulIdentity;
  
  public void setUp() throws Exception {
    super.setUp();
    tearDownActivityList = new ArrayList<ExoSocialActivity>();
    activityManager = (ActivityManager) getContainer().getComponentInstanceOfType(ActivityManager.class);
    assertNotNull("activityManager must not be null", activityManager);
    identityManager =  (IdentityManager) getContainer().getComponentInstanceOfType(IdentityManager.class);
    assertNotNull("identityManager must not be null", identityManager);
    relationshipManager =  (RelationshipManager) getContainer().getComponentInstanceOfType(RelationshipManager.class);
    assertNotNull("relationshipManager must not be null", relationshipManager);
    relationshipPublisher = (RelationshipPublisher) getContainer().getComponentInstanceOfType(RelationshipPublisher.class);
    assertNotNull("relationshipPublisher must not be null", relationshipPublisher);
    identityStorage =  (IdentityStorage) getContainer().getComponentInstanceOfType(IdentityStorage.class);
    assertNotNull("identityStorage must not be null", identityStorage);
    
    rootIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "root", true);
    demoIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "demo", true);
    johnIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "john", true);
    maryIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "mary", true);
    raulIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "raul", true);
    paulIdentity = identityManager.getOrCreateIdentity(OrganizationIdentityProvider.NAME, "paul", true);
  }

  public void tearDown() throws Exception {
    for (ExoSocialActivity activity : tearDownActivityList) {
      try {
        activityManager.deleteActivity(activity.getId());
      } catch (Exception e) {
        LOG.warn("can not delete activity with id: " + activity.getId());
      }
    }
    identityManager.deleteIdentity(rootIdentity);
    identityManager.deleteIdentity(demoIdentity);
    identityManager.deleteIdentity(johnIdentity);
    identityManager.deleteIdentity(maryIdentity);
    identityManager.deleteIdentity(raulIdentity);
    identityManager.deleteIdentity(paulIdentity);
    super.tearDown();
  }
  
  public void testConfirmedManyRelationship() {
      
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(demoIdentity, rootIdentity);
    Relationship rootToJohnRelationship = relationshipManager.inviteToConnect(johnIdentity, rootIdentity);
    Relationship rootToMaryRelationship = relationshipManager.inviteToConnect(maryIdentity, rootIdentity);
    Relationship rootToRaulRelationship = relationshipManager.inviteToConnect(raulIdentity, rootIdentity);
    Relationship rootToPaulRelationship = relationshipManager.inviteToConnect(paulIdentity, rootIdentity);
    
    relationshipManager.confirm(rootIdentity, demoIdentity);
    relationshipPublisher.confirmed(new RelationshipEvent(Type.CONFIRM, relationshipManager, rootToDemoRelationship));
    relationshipManager.confirm(rootIdentity, johnIdentity);
    relationshipPublisher.confirmed(new RelationshipEvent(Type.CONFIRM, relationshipManager, rootToJohnRelationship));
    relationshipManager.confirm(rootIdentity, maryIdentity);
    relationshipPublisher.confirmed(new RelationshipEvent(Type.CONFIRM, relationshipManager, rootToMaryRelationship));
    relationshipManager.confirm(rootIdentity, raulIdentity);
    relationshipPublisher.confirmed(new RelationshipEvent(Type.CONFIRM, relationshipManager, rootToRaulRelationship));
    relationshipManager.confirm(rootIdentity, paulIdentity);
    relationshipPublisher.confirmed(new RelationshipEvent(Type.CONFIRM, relationshipManager, rootToPaulRelationship));
    
    String rootActivityId =  identityStorage.getProfileActivityId(rootIdentity.getProfile(), Profile.AttachedActivityType.RELATIONSHIP);
    assertNotNull(rootActivityId);
    ExoSocialActivity rootActivity = activityManager.getActivity(rootActivityId);
    List<ExoSocialActivity> rootComments = activityManager.getCommentsWithListAccess(rootActivity).loadAsList(0, 10);
    assertEquals(5, rootComments.size());
    assertEquals("I'm now connected with 5 user(s)",rootActivity.getTitle());
    
    String johnActivityId =  identityStorage.getProfileActivityId(johnIdentity.getProfile(), Profile.AttachedActivityType.RELATIONSHIP);
    String maryActivityId =  identityStorage.getProfileActivityId(maryIdentity.getProfile(), Profile.AttachedActivityType.RELATIONSHIP);
    String demoActivityId =  identityStorage.getProfileActivityId(demoIdentity.getProfile(), Profile.AttachedActivityType.RELATIONSHIP);
    String raulActivityId =  identityStorage.getProfileActivityId(raulIdentity.getProfile(), Profile.AttachedActivityType.RELATIONSHIP);
    String paulActivityId =  identityStorage.getProfileActivityId(paulIdentity.getProfile(), Profile.AttachedActivityType.RELATIONSHIP);
    activityManager.deleteActivity(rootActivityId);
    activityManager.deleteActivity(johnActivityId);
    activityManager.deleteActivity(maryActivityId);
    activityManager.deleteActivity(demoActivityId);
    activityManager.deleteActivity(raulActivityId);
    activityManager.deleteActivity(paulActivityId);
  }

  /**
   *
   */
  public void testConfirmed() {
    
    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    relationshipManager.confirm(rootIdentity, demoIdentity);
    relationshipPublisher.confirmed(new RelationshipEvent(Type.CONFIRM, relationshipManager, rootToDemoRelationship));
    
    String rootActivityId =  identityStorage.getProfileActivityId(rootIdentity.getProfile(), Profile.AttachedActivityType.RELATIONSHIP);
    assertNotNull(rootActivityId);
    ExoSocialActivity rootActivity = activityManager.getActivity(rootActivityId);
    List<ExoSocialActivity> rootComments = activityManager.getCommentsWithListAccess(rootActivity).loadAsList(0, 10);
    assertEquals(1, rootComments.size());
    assertEquals("I'm now connected with 1 user(s)",rootActivity.getTitle());
    assertEquals("I'm now connected with Demo gtn",rootComments.get(0).getTitle());
    
    String demoActivityId =  identityStorage.getProfileActivityId(demoIdentity.getProfile(), Profile.AttachedActivityType.RELATIONSHIP);
    assertNotNull(demoActivityId);
    ExoSocialActivity demoActivity = activityManager.getActivity(demoActivityId);
    List<ExoSocialActivity> demoComments = activityManager.getCommentsWithListAccess(demoActivity).loadAsList(0, 10);
    assertEquals(1, demoComments.size());
    assertEquals("I'm now connected with 1 user(s)",demoActivity.getTitle());
    assertEquals("I'm now connected with Root Root",demoComments.get(0).getTitle());
    
    Relationship rootToJohnRelationship = relationshipManager.inviteToConnect(rootIdentity, johnIdentity);
    relationshipManager.confirm(rootIdentity, johnIdentity);
    relationshipPublisher.confirmed(new RelationshipEvent(Type.CONFIRM, relationshipManager, rootToJohnRelationship));
    
    rootActivity = activityManager.getActivity(rootActivityId);
    rootComments = activityManager.getCommentsWithListAccess(rootActivity).loadAsList(0, 10);
    assertEquals(2, rootComments.size());
    assertEquals("I'm now connected with 2 user(s)",rootActivity.getTitle());
    assertEquals("I'm now connected with John Anthony",rootComments.get(1).getTitle());
    
    String johnActivityId =  identityStorage.getProfileActivityId(johnIdentity.getProfile(), Profile.AttachedActivityType.RELATIONSHIP);
    assertNotNull(johnActivityId);
    ExoSocialActivity johnActivity = activityManager.getActivity(johnActivityId);
    List<ExoSocialActivity> johnComments = activityManager.getCommentsWithListAccess(johnActivity).loadAsList(0, 10);
    assertEquals(1, johnComments.size());
    assertEquals("I'm now connected with 1 user(s)",johnActivity.getTitle());
    assertEquals("I'm now connected with Root Root",johnComments.get(0).getTitle());
    
    //remove a connection will re-updated activity's title
    relationshipManager.delete(rootToJohnRelationship);
    relationshipPublisher.removed(new RelationshipEvent(Type.REMOVE, relationshipManager, rootToJohnRelationship));
    
    rootActivity = activityManager.getActivity(rootActivityId);
    assertEquals("I'm now connected with 1 user(s)",rootActivity.getTitle());
    
    johnActivity = activityManager.getActivity(johnActivityId);
    assertEquals("I'm now connected with 0 user(s)",johnActivity.getTitle());
    
    activityManager.deleteActivity(johnActivity);
    activityManager.deleteActivity(rootActivity);
    activityManager.deleteActivity(demoActivity);
    
  }
  
  public void testConfirmedAndUpdateProfile() throws Exception {
    Profile profile = rootIdentity.getProfile();

    profile.setAttachedActivityType(Profile.AttachedActivityType.USER);
    assertNull(getActivityId(profile));

    //update the profile for the first time
    profile.setProperty(Profile.POSITION, "developer");
    profile.setListUpdateTypes(Arrays.asList(Profile.UpdateType.CONTACT));
    identityManager.updateProfile(profile);

    //from now, activity must not be null
    assertNotNull(getActivityId(profile));

    String activityId = getActivityId(profile);
    ExoSocialActivity activity = activityManager.getActivity(activityId);

    List<ExoSocialActivity> comments = activityManager.getCommentsWithListAccess(activity).loadAsList(0, 20);
    //Number of comments must be 1
    assertEquals(1, comments.size());
    assertEquals("Contact informations has been updated.", comments.get(0).getTitle());

    Relationship rootToDemoRelationship = relationshipManager.inviteToConnect(rootIdentity, demoIdentity);
    relationshipManager.confirm(rootIdentity, demoIdentity);
    relationshipPublisher.confirmed(new RelationshipEvent(Type.CONFIRM, relationshipManager, rootToDemoRelationship));

    String rootActivityId =  identityStorage.getProfileActivityId(rootIdentity.getProfile(), Profile.AttachedActivityType.RELATIONSHIP);
    assertNotNull(rootActivityId);
    ExoSocialActivity rootActivity = activityManager.getActivity(rootActivityId);
    List<ExoSocialActivity> rootComments = activityManager.getCommentsWithListAccess(rootActivity).loadAsList(0, 10);
    assertEquals(1, rootComments.size());
    assertEquals("I'm now connected with 1 user(s)",rootActivity.getTitle());
    assertEquals("I'm now connected with Demo gtn",rootComments.get(0).getTitle());
    String demoActivityId =  identityStorage.getProfileActivityId(demoIdentity.getProfile(), Profile.AttachedActivityType.RELATIONSHIP);
    assertNotNull(demoActivityId);
    ExoSocialActivity demoActivity = activityManager.getActivity(demoActivityId);
    List<ExoSocialActivity> demoComments = activityManager.getCommentsWithListAccess(demoActivity).loadAsList(0, 10);
    assertEquals(1, demoComments.size());
    assertEquals("I'm now connected with 1 user(s)",demoActivity.getTitle());
    assertEquals("I'm now connected with Root Root",demoComments.get(0).getTitle());
    relationshipManager.delete(rootToDemoRelationship);
    activityManager.deleteActivity(rootActivity);
    activityManager.deleteActivity(demoActivity);
  }

  private String getActivityId(Profile profile) {
    return identityStorage.getProfileActivityId(profile, Profile.AttachedActivityType.USER);
  }

}
